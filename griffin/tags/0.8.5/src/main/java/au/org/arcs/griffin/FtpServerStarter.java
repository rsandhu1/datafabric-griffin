/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */

package au.org.arcs.griffin;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import au.org.arcs.griffin.common.BeanConstants;
import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpServerOptions;
import au.org.arcs.griffin.server.FtpServer;
import au.org.arcs.griffin.utils.IOUtils;
import au.org.arcs.griffin.utils.LoggingOutputStream;
import au.org.arcs.griffin.utils.NetUtils;
import au.org.arcs.griffin.utils.SecurityUtil;

/**
 * Griffin FTP application.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public final class FtpServerStarter {

    //private static final int THREAD_ALIVE_CHECK_INTERVAL = 1000;

    private static final int PASSWORD_ARG_COUNT          = 3;

    private static Log       log                         = LogFactory.getLog(FtpServerStarter.class);

    /**
     * Constructor.
     */
    public FtpServerStarter() {
        super();
    }

    /**
     * Entry point of the application.
     * 
     * @param args Optionally the bean resource file can be passed.
     */
    public static void main(String[] args) {
        // TODO Use commons-cli
        if (args.length > 0 && args[0].trim().equalsIgnoreCase("-password")) {
            generatePassword(args);
        } else {
            log.info("Starting Griffin FTP Server...");
            PluginManager.startApplication(FtpServerStarter.class.getName(), "startServer", args);
            log.info("Griffin FTP Server ready.");
        }
    }
    
    private void redirectOutErr(){
        // now rebind stdout/stderr to logger                                  
        Log log;                                                         
        LoggingOutputStream los;                                               

        log = LogFactory.getLog("stdout");                                   
        los = new LoggingOutputStream(log, "stdout");          
        System.setOut(new PrintStream(los, true));                             

        log = LogFactory.getLog("stderr");                                   
        los= new LoggingOutputStream(log, "stderr");           
        System.setErr(new PrintStream(los, true));                             

    }

    private static void generatePassword(String[] args) {
        if (args.length != PASSWORD_ARG_COUNT) {
            System.err
                .println("Please adhere to the following synthax: FtpServerApp password <password> <algorithm>");
            return;
        }
        String password = args[1];
        String algorithm = args[2];
        try {
            String hash = SecurityUtil.encodePassword(password, algorithm);
            System.out.print("Hash: " + hash + "\n");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("ERROR: " + e);
        }
    }

    /**
     * Starts the FTP servers(s).
     * 
     * @param args The arguments passed with main method.
     */
    public void startServer(String[] args) {
//        if (!NetUtils.isSSLAvailable()) {
//            System.exit(1);
//        }
        
    	redirectOutErr();
//    	System.out.println("test redirection - out");
//    	System.err.println("test redirection - err");
    	
        String beanRes = args.length > 0 ? args[0] : FtpConstants.DEFAULT_BEAN_RES;
        File file = new File(beanRes);

        logPaths(file);
        if (System.getProperty(FtpConstants.GRIFFIN_HOME)==null)
        	System.setProperty(FtpConstants.GRIFFIN_HOME,file.getParent());

        /* Prepare three main threads */
        ApplicationContext appContext = getApplicationContext(beanRes, file);
        FtpServer svr = (FtpServer) appContext.getBean(BeanConstants.BEAN_SERVER);
//        FtpServer sslsvr = (FtpServer) appContext.getBean(BeanConstants.BEAN_SSL_SERVER);
//        ConsoleServer console = (ConsoleServer) appContext.getBean(BeanConstants.BEAN_CONSOLE);

        /* Log settings */

        logOptions(svr.getOptions());

        /* Check local ip addresses */
        InetAddress addr = NetUtils.getMachineAddress(true);
        if (addr == null) {
            log.error("No local network ip address available.");
            System.exit(1);
        }
        log.info("Local ip address: " + addr);

        /* Start servers */
        Thread svrThread;
//        Thread sslSvrThread;
        try {
            svrThread = new Thread(svr);
            svrThread.start();
//            sslSvrThread = new Thread(sslsvr);
//            sslSvrThread.start();

            /* Start web console */
//            if (svr.getOptions().getBoolean("console.enabled", false)) {
//                console.start();
//            }
            
            /* Register Shutdown Hook */
            List<FtpServer> serverList = new ArrayList<FtpServer>();
            serverList.add(svr);
//            serverList.add(sslsvr);
            addShutdownHook(serverList);
            
        } catch (Exception e) {
            log.error("Unexpected error", e);
        }
    }

    /**
     * Add shutdown hook.
     */
    private static void addShutdownHook(final List<FtpServer> servers) {

        Runnable shutdownHook = new Runnable() {
            public void run() {
                for (FtpServer ftpServer : servers) {
                    log.info("Stopping server '" + ftpServer.getName() + "'.");
                    ftpServer.abort();
                }
                log.info("All servers down.");
            }
        };
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(shutdownHook));
    }
    
    private void logPaths(File file) {
//        log.info("Hermes Home: " + IOUtils.getHomeDir());

        log.info("Application context: " + file);
        if (file != null && file.getParent() != null) {
            System.setProperty("griffin.ctx.dir", file.getParent());
            log.info("Application context path: " + file.getParent());
        }
    }

    private static ApplicationContext getApplicationContext(String beanRes, File file) {
        ApplicationContext appContext;
        if (file.exists()) {
            appContext = new FileSystemXmlApplicationContext("file:"+beanRes);
        } else {
            log.error("Griffin FTP application context not found: " + file
                    + ". Trying to read context from classpath...");
            appContext = new ClassPathXmlApplicationContext(
                new String[] {"/" + FtpConstants.DEFAULT_BEAN_RES});
        }
        return appContext;
    }

    private static void logOptions(FtpServerOptions aOptions) {
        log.info(aOptions.getAppTitle());
        log.info("Version " + aOptions.getAppVersion());
        log.info("Build info: " + aOptions.getAppBuildInfo());

        log.info("Ftp server options:");
        Set<Object> keyset = aOptions.getProperties().keySet();
        for (Object key : keyset) {
            String value = aOptions.getProperty(key.toString());
            log.info("    " + key + ": " + value);
        }
    }
}
