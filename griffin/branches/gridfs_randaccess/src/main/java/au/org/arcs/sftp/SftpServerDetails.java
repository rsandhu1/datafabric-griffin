package au.org.arcs.sftp;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.griffin.utils.NetUtils;
import au.org.arcs.sftp.config.SftpAppProperties;
import au.org.arcs.sftp.config.SftpConfigProperties;

/**
 * SFTP to Irods server details. Includes the options and backend griffin
 * fileSystem
 * 
 * @author John Curtis
 */
public class SftpServerDetails implements SftpServerConstants {

    private static Log log = LogFactory.getLog(SftpServerDetails.class);
    private SftpAppProperties appProperties = null;
    private String beanPath = "";
    private String logDetailsPath = "";
    private File context_file = null;
    // Bean data
    private SftpConfigProperties options = null;
    private FileSystem fileSystem = null;

    public SftpServerDetails(String beanPath, String logDetailsPath) {
        // First load app resources
        appProperties = new SftpAppProperties();
        try {
            appProperties.load();
        } catch (IOException e) {
            log.error("Unable to load app resources: "
                      + e.getMessage(), e);
            System.exit(1);
        }

        // now get default paths for beans and log details
        this.beanPath = beanPath;
        if (beanPath == null || beanPath.isEmpty()) {
            this.beanPath = "./" + DEFAULT_BEAN_RES;// look in jar directory for
                                                    // context.xml
        }
        this.logDetailsPath = logDetailsPath;
        if (logDetailsPath == null || logDetailsPath.isEmpty()) {
            this.logDetailsPath = "./" + DEFAULT_LOG_RES;// look in jar
                                                         // directory for
                                                         // log4j.properties
        }
    }

    public SftpServerDetails(String[] args) {
        this(args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : "");
    }

    public String getDefaultBeanPath() {
        return beanPath;
    }

    public String getDefaultLogDetailsPath() {
        return logDetailsPath;
    }

    public void loadBeans() throws IOException {
        if (fileSystem != null) {
            return; // Already loaded
        }
        if (appProperties == null) {
            throw new IOException(appProperties.getAppTitle()
                    + " application properties not set");
        }
        
        // load the application context
        ApplicationContext appContext = loadApplicationContext(getDefaultBeanPath());
        if (appContext == null) {
            throw new IOException(appProperties.getAppTitle()
                    + " application context file not found: "
                    + getContextFile());
        }

        // Load server options bean
        this.options = (SftpConfigProperties) appContext.getBean(DEFAULT_BEAN_SFTP_OPTIONS);
        // Set application home dir if needed
        this.initHomeDir();
        // Set application log dir if needed
        // this.initLoggingDir();

        // Load file system bean
        this.fileSystem = (FileSystem) appContext.getBean(DEFAULT_BEAN_FILE_SYSTEM);

        fileSystem.init();
    }

    public File getContextFile() {
        return context_file;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public SftpConfigProperties getOptions() {
        return options;
    }

    /**
     * {@inheritDoc}
     */
    public SftpAppProperties getAppProperties() {
        assert appProperties != null : "Null app resource properties";
        return appProperties;
    }

    /**
     * {@inheritDoc}
     */
    public void setAppProperties(SftpAppProperties properties) {
        this.appProperties = properties;
    }

    /**
     * {@inheritDoc}
     */
    public String getAppTitle() {
        String label = null;
        if (getOptions() != null) {
            label = getOptions().getServerLabel();
        }
        if (label == null || label.isEmpty()) {
            if (getAppProperties() != null) {
                label = getAppProperties().getAppTitle();
            }
        }
        return label;
    }

    /**
     * {@inheritDoc}
     */
    public int getInternalFileBufferSize() {
        int buffer = getOptions().getInternalFileBufferSizeKb() * 1024;

        if (buffer < MIN_INTERNAL_FILE_BUFFER) {
            return 0; // No internal buffering
        }
        if (buffer > MAX_INTERNAL_FILE_BUFFER) {
            return MAX_INTERNAL_FILE_BUFFER;
        }
        return buffer;
    }

    /**
     * {@inheritDoc}
     */
    public boolean useFileBufferThread() {
        return getOptions().useFileBufferThread();
    }

    public KeyPairProvider getKeyPairProvider() throws IOException {
        File key_file = checkHostKeyFile();
        String path = key_file.getCanonicalPath();

        boolean wantBouncyCastle = false;
        if (path.endsWith(".pem")) {
            wantBouncyCastle = true;
        } else if (path.endsWith(".ser")) {
            wantBouncyCastle = false;
        } else {
            wantBouncyCastle = SecurityUtils.isBouncyCastleRegistered();
        }
        if (wantBouncyCastle && !SecurityUtils.isBouncyCastleRegistered()) {
            throw new IllegalStateException("BouncyCastle must be registered as a JCE provider");
        }
        
        // TODO check other formats
        // return new FileKeyPairProvider(path));
        if (wantBouncyCastle) {
            return new PEMGeneratorHostKeyProvider(path); // Want host.pem
        } else {
            return new SimpleGeneratorHostKeyProvider(path);// Want host.ser
        }
    }

    /**
     * Creates the context object passed to the user session.
     * 
     * @return The session context.
     */
    public SftpSessionContext createSftpContext(String user,
                                                FileSystemConnection connection,
                                                ResourceBundle res_bundle) {
        return new SftpSessionContext(this, user, connection, res_bundle);
    }

    private ApplicationContext loadApplicationContext(String beanResFile) {
        context_file = new File("/opt/griffin/" + beanResFile);

        ApplicationContext appContext = null;
        if (getContextFile().exists()) {
            appContext = new FileSystemXmlApplicationContext("file:/opt/griffin/"
                                                             + beanResFile);
        }
        /*
         * else //Use res version {
         * log.error(getAppTitle()+" application context not found: " +
         * getContextFile() + ". Trying to read context from classpath...");
         * appContext = new ClassPathXmlApplicationContext(new String[]{ "/" +
         * DEFAULT_BEAN_RES }); }
         */
        return appContext;
    }

    // Set application root dir using the context file Path if needed
    // Priority is:
    // 1. System.property(SftpConstants.SYS_HOME_DIR) can be set separately
    // 2. options.getProperty(OPT_ROOT_DIR) in the context file if above not
    // already set
    // 3. Parent directory of the context file if OPT_ROOT_DIR not added to
    // context file
    // 4. System.getProperty("user.home")/sftpirods if all else fails
    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     */
    private void initHomeDir() throws IOException {
        if (getSystemHomeDirectory() != null) {
            return; // Done
        }

        // Check for explicit directory in options
        String context_dir = options.getHomeDirectory();
        if (setHomeDir(context_dir)) {
            return;
        }
        
        // Fail if we had one but unable to set
        if (context_dir != null && context_dir.length() > 0) {
            throw new IOException("Unable to set the context home directory: "
                                  + context_dir);
        }

        // Now try parent of the context menu
        File context_file_abs = context_file.getAbsoluteFile();
        if (setHomeDir(context_file_abs.getParent())) {
            return;
        }
        
        // Fail if we had a valid but unable to set
        if (context_file_abs.exists()) {
            throw new IOException("Unable to set root directory to the context files parent: "
                                  + context_file_abs.getParent());
        }

        File defaultDir = new File(System.getProperty("user.home"), "sftpirods");
        if (setHomeDir(defaultDir.getAbsolutePath())) {
            return;
        }
        
        throw new IOException("Unable to set a valid root directory.");
    }

    /**
     * {@inheritDoc}
     */
    private boolean setHomeDir(String dir_path) {
        if (dir_path == null) {
            return false;
        }

        // Make canonical
        dir_path = FilenameUtils.normalizeNoEndSeparator(dir_path);
        if (dir_path.length() == 0) {
            return false;
        }

        File dir = new File(dir_path);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }

        // Set system home directory
        System.setProperty(SYS_HOME_DIR, dir.getAbsolutePath());
        // Also need to set Griffin version
        // System.setProperty(FileSystem.GRIFFIN_HOME_DIR,
        // dir.getAbsolutePath());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public static String getSystemHomeDirectory() {
        return System.getProperty(SYS_HOME_DIR);
    }

    // Set application root dir using the context file Path if needed
    // Priority is:
    // 1. System.property(SftpConstants.SYS_LOG_DIR) can be set separately
    // 2. options.getProperty(OPT_LOG_DIR) in the context file if above not
    // already set
    // 3. Sub dir "logs" in getSystemHomeDirectory()
    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     */
    /*
     * private void initLoggingDir() throws IOException { if
     * (getSystemLoggingDirectory() != null) return; //Done
     * 
     * //Check for explicit directory in options String
     * opt_log_dir=options.getLoggingDirectory(); if(setLoggingDir(opt_log_dir))
     * return; //Fail if we had one but unable to set if(opt_log_dir!=null &&
     * opt_log_dir.length()>0) throw new
     * IOException("Unable to set the context log directory: "+opt_log_dir);
     * 
     * //Use the system dir String home_dir=getSystemHomeDirectory(); if
     * (home_dir == null || home_dir.isEmpty()) throw new
     * IOException("Expected a system home directory to be set");
     * 
     * File log_dir=new File(home_dir,"logs"); log_dir.mkdir();
     * if(!log_dir.exists() || !log_dir.isDirectory()) throw new
     * IOException("Failed to create log directory: "
     * +log_dir.getAbsolutePath()); if(setLoggingDir(log_dir.getAbsolutePath()))
     * return;
     * 
     * throw new IOException("Unable to set a valid logging directory."); }
     */

    /**
     * {@inheritDoc}
     */
    /*
     * private boolean setLoggingDir(String dir_path) { if(dir_path==null)
     * return false;
     * 
     * // Make canonical dir_path =
     * FilenameUtils.normalizeNoEndSeparator(dir_path); if(dir_path.length()==0)
     * return false;
     * 
     * File dir=new File(dir_path); if(!dir.exists() || !dir.isDirectory())
     * return false;
     * 
     * System.setProperty(SYS_LOG_DIR, dir.getAbsolutePath()); return true; }
     */
    /**
     * {@inheritDoc}
     */
    // public static String getSystemLoggingDirectory()
    // {
    // return System.getProperty(SYS_LOG_DIR);
    // }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     */
    private File checkHostKeyFile() throws IOException {
        File keyfile = options.getHostKeyFile();
        if (keyfile == null) {
            throw new IOException("Host key location not defined in config file");
        }
        if (!keyfile.isFile()) {
            throw new IOException("Host key location is NOT a file");
        }
        return keyfile;
    }

    public boolean checkBlackList(InetAddress clientAddr) {
        String ipBlackList = options.getBlackList(NetUtils.isIPv6(clientAddr));
        if (NetUtils.checkIPMatch(ipBlackList, clientAddr)) {
            return false;
        }
        return true;
    }

    public boolean wantIOBuffering() {
        return getOptimalFilePacketBytes() > 0;
    }

    public int getOptimalFilePacketBytes() {
        int ideal_packet_bytes = -1;
        if (fileSystem != null) {
            ideal_packet_bytes = 20000; // fileSystem.getOptimalPacketBytes();
        }
        if (ideal_packet_bytes > MIN_FILE_TRANSFER_SIZE
                && ideal_packet_bytes <= MAX_FILE_TRANSFER_SIZE) {
            return ideal_packet_bytes;
        }
        // return -1; //indicate file buffering not needed
        return getInitialChannelPacketBytes();
    }

    public void logSetup(Log log, InetAddress machine_addr) {
        log.info("Attempting to Start " + getAppTitle() + " on port "
                 + getOptions().getSftpPort() + "...");
        log.info("Local ip address: " + machine_addr.getHostAddress());

        log.info("Application resource stream: "
                 + getAppProperties().getResource());
        getAppProperties().logAll(log,
                                  getAppTitle()
                                  + " application resource settings");

        log.info("Application context file: " + getContextFile());
        getOptions().logAll(log, getAppTitle() + " context file settings");

        log.info(getAppTitle() + " home directory: " + getSystemHomeDirectory());
        // log.info(getAppName()+" log directory: "+getSystemLoggingDirectory());
    }

    static public int getInitialChannelPacketBytes() {
        // TODO We use the current Irods setting. need something more generic
        return 256000; // JargonFileSystemImpl.IDEAL_IRODS_PACKET_SIZE; //256k
    }

    static public int getInitialChannelWindowBytes() {
        // x32 As for default, so == 16Mb
        return getInitialChannelPacketBytes() * 32; // As for default==16Mb
    }
}
