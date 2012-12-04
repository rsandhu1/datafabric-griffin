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

package au.org.arcs.griffin.session.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;

import au.org.arcs.griffin.cmd.DataChannelProvider;
import au.org.arcs.griffin.cmd.SocketProvider;
import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpEventListener;
import au.org.arcs.griffin.common.FtpServerOptions;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.exception.FtpQuotaException;
import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.griffin.usermanager.model.GroupDataList;
import au.org.arcs.griffin.usermanager.model.UserData;
import au.org.arcs.griffin.utils.LoggingReader;
import au.org.arcs.griffin.utils.LoggingWriter;
import au.org.arcs.griffin.utils.TransferMonitor;

/**
 * This class servers as a means of transportation for data shared by a single FTP session.
 * Instances of the <code>FtpSessionContextImpl</code> class are passed to each of the commands
 * while executing a FTP command sequence. The command objects read connection settings and other
 * options from the context. In turn data that may concern the general state of the FTP session can
 * be stored in the context.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
/**
 * TODO: guy: Enter comment!
 *
 * @version $Revision: 1.1 $
 * @author Guy K. Kloss
 */
public class FtpSessionContextImpl implements FtpConstants, FtpSessionContext {

    private static Log          log               = LogFactory.getLog(FtpSessionContextImpl.class);

    private static int          portIdx;

    private String              user;

    private String              password;

    private boolean             authenticated;

    private int                 dataType          = DT_ASCII;

    private int                 transmissionMode  = MODE_STREAM;

    private int                 storageStructure  = STRUCT_FILE;

    private String              remoteDir;

    private Socket              clientSocket;

    protected BufferedReader      clientCmdReader;

    protected PrintWriter         clientResponseWriter;

    private FtpServerOptions    options;

    private FtpEventListener    eventListener;

    private ResourceBundle      resourceBundle;

    private SocketProvider      dataSocketProvider;

    private FileSystem          fileSystem;

    private Date                creationTime;

    private Map<String, Object> attributes;

    private Map<String, Long>   sessionStatistics = Collections.synchronizedMap(new HashMap<String, Long>());

    private GSSContext serviceContext;
    
    private String replyType;
    
    private GSSName gssIdentity;
    
    protected FileSystemConnection fileSystemConnection;
    
    private int parallelStart;
    private int parallelMin;
    private int parallelMax;
    
    private boolean confirmEOFs;
    private int bufferSize;
    
    private int networkStack;
    private DataChannelProvider dataChannelProvider;
    
    private int dcauType;
    private TransferMonitor transferMonitor;
    
    private int controlChannelMode;
    private InetAddress clientInetAddress;
    private InetAddress localInetAddress;

    /**
     * Constructor.
     * 
     * @param options The server options.
     * @param fileSystem The file system.
     * @param resourceBundle The resource bundle that contains messages and texts.
     * @param listener The listener that is informed on session events.
     */
    public FtpSessionContextImpl(FtpServerOptions options,
            FileSystem fileSystem, ResourceBundle resourceBundle, FtpEventListener listener) {
        super();
        this.fileSystem = fileSystem;
        this.resourceBundle = resourceBundle;
        this.options = options;
        this.attributes = Collections.synchronizedMap(new HashMap<String, Object>());
        this.eventListener = listener;
        transferMonitor = new TransferMonitor();
    }
    
    /**
     * {@inheritDoc}
     */
    public TransferMonitor getTransferMonitor() {
        return this.transferMonitor;
    }

    /**
     * {@inheritDoc}
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    public FtpServerOptions getOptions() {
        return options;
    }

    /**
     * {@inheritDoc}
     */
    public String getOption(String key) {
        return getOptions().getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    public String getPassword() {
        return password;
    }

    /**
     * {@inheritDoc}
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * {@inheritDoc}
     */
    public String getRemoteDir() {
        if (remoteDir == null) {
            remoteDir = getOptions().getRootDir();
        }
        return remoteDir;
    }

    /**
     * {@inheritDoc}
     */
    public String getRemoteRelDir() {
        // Directory storage paths are handled in the filesystem implementations,
        // So we are only returning here what getRemoteDir() does.
        return getRemoteDir();
    }

    /**
     * {@inheritDoc}
     */
    public void setRemoteDir(String remoteDir) {
        this.remoteDir = FilenameUtils.normalizeNoEndSeparator(remoteDir);
    }

    /**
     * {@inheritDoc}
     */
    public String getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * {@inheritDoc}
     */
    public FtpEventListener getEventListener() {
        return eventListener;
    }

    /**
     * {@inheritDoc}
     */
    public String getRes(String id) {
        return resourceBundle.getString(id);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * {@inheritDoc}
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * {@inheritDoc}
     */
    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    /**
     * {@inheritDoc}
     */
    public int getStorageStructure() {
        return storageStructure;
    }

    /**
     * {@inheritDoc}
     */
    public void setStorageStructure(int storageStructure) {
        this.storageStructure = storageStructure;
    }

    /**
     * {@inheritDoc}
     */
    public int getTransmissionMode() {
        return transmissionMode;
    }

    /**
     * {@inheritDoc}
     */
    public void setTransmissionMode(int transmissionMode) {
        this.transmissionMode = transmissionMode;
    }

    /**
     * {@inheritDoc}
     */
    public SocketProvider getDataSocketProvider() {
        return dataSocketProvider;
    }

    /**
     * {@inheritDoc}
     */
    public void setDataSocketProvider(SocketProvider provider) {
        this.dataSocketProvider = provider;
    }

    /**
     * {@inheritDoc}
     */
    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * {@inheritDoc}
     */
    public void setClientSocket(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.clientResponseWriter = new LoggingWriter(new OutputStreamWriter(clientSocket.getOutputStream()),
            true);
        this.clientCmdReader = new LoggingReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    /**
     * {@inheritDoc}
     */
    public PrintWriter getClientResponseWriter() {
        return clientResponseWriter;
    }

    /**
     * {@inheritDoc}
     */
    public BufferedReader getClientCmdReader() {
        return clientCmdReader;
    }

    /**
     * {@inheritDoc}
     */
    public int getPermission(String path) {
        int result = PRIV_NONE;
        try {
            GroupDataList list = (GroupDataList) getAttribute(ATTR_GROUP_DATA);
            result = list.getPermission(path, getUser(), options.getRootDir());
        } catch (FtpConfigException e) {
            log.error(e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @throws GSSException 
     * @throws IOException 
     * @throws FtpConfigException 
     */
    public void authenticate() throws FtpConfigException, IOException, GSSException {
        authenticated = false;
        String dirName = null;
        fileSystemConnection = fileSystem.createFileSystemConnection(getServiceContext().getDelegCred());
        setAttribute(ATTR_LOGIN_TIME, new Date());
        dirName = fileSystemConnection.getHomeDir();
        user = fileSystemConnection.getUser();
        setRemoteDir(dirName);
        log.debug("fs.isconnected:" + fileSystemConnection.isConnected());
        authenticated = true;
    }

    /**
     * {@inheritDoc}
     */
    public UserData getUserData() {
        return (UserData) getAttribute(ATTR_USER_DATA);
    }

    /**
     * {@inheritDoc}
     */
    public void resetCredentials() {
        authenticated = false;
        setUser(null);
        setPassword(null);
    }

    /**
     * {@inheritDoc}
     */
    public void closeSockets() {
        if (getDataSocketProvider() != null) {
            getDataSocketProvider().closeSocket();
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#disconnectFileSystem()
     */
    public void disconnectFileSystem() {
        if (getFileSystemConnection() != null) {
            try {
                getFileSystemConnection().close();
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getCharset() {
        String charset;
        Boolean forceUtf8 = (Boolean) getAttribute(ATTR_FORCE_UTF8);
        if (forceUtf8 != null && forceUtf8.booleanValue()) {
            charset = "UTF-8";
        } else {
            String key = getDataType() == DT_EBCDIC ? OPT_CHARSET_EBCDIC : OPT_CHARSET_ASCII;
            charset = getOptions().getProperty(key);
        }
        return charset;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getNextPassiveTCPPort() {
        Integer port;
        Integer[] allowedPorts = getOptions().getAllowedTCPPorts();
        if (allowedPorts == null || allowedPorts.length == 0) {

            /* Let the system decide which port to use. */
            port = new Integer(0);
        } else {

            /* Get the port from the user defined list. */
            port = allowedPorts[portIdx++];
            if (portIdx >= allowedPorts.length) {
                portIdx = 0;
            }
        }
        return port;
    }
    
    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getNextPassiveUDPPort()
     */
    public Integer getNextPassiveUDPPort() {
        Integer port;
        Integer[] allowedPorts = getOptions().getAllowedUDPPorts();
        if (allowedPorts == null || allowedPorts.length == 0) {

            /* Let the system decide which port to use. */
            port = new Integer(0);
        } else {

            /* Get the port from the user defined list. */
            port = allowedPorts[portIdx++];
            if (portIdx >= allowedPorts.length) {
                portIdx = 0;
            }
        }
        return port;

    }

    /**
     * {@inheritDoc}
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * {@inheritDoc}
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Long> getSessionStatistics() {
        return sessionStatistics;
    }

    private int getUpperLimit(String globalOptionKey, String groupLimitKey) {
        long result = -1;
        long globalLimit = getOptions().getInt(globalOptionKey, -1);

        GroupDataList list = (GroupDataList) getAttribute(ATTR_GROUP_DATA);
        log.debug("group data list:" + list);
        long groupLimit = list.getUpperLimit(groupLimitKey);

        if (globalLimit < 0) {
            result = groupLimit;
        } else if (groupLimit < 0) {
            result = globalLimit;
        } else {
            result = Math.max(groupLimit, globalLimit);
        }

        return (int) result;
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxDownloadRate() {
        return getUpperLimit(OPT_MAX_DOWNLOAD_RATE, STAT_DOWNLOAD_RATE);
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxUploadRate() {
        return getUpperLimit(OPT_MAX_UPLOAD_RATE, STAT_UPLOAD_RATE);
    }

    /**
     * Increases a particular resource consumption by the passed value.
     * 
     * @param countKey The name of the statistic.
     * @param value The value
     * @throws FtpQuotaException Thrown if a resource limit has been reached.
     */
    public void updateIncrementalStat(String countKey, long value) throws FtpQuotaException {

        /* All sessions of user */
//        getUserManager().updateIncrementalStatistics(getUser(), countKey, value);

        /* Current session */
        Map<String, Long> sessionStats = getSessionStatistics();
        Long consumptionObj = (Long) sessionStats.get(countKey);
        long consumption = consumptionObj == null ? 0 : consumptionObj.longValue();
        sessionStats.put(countKey, new Long(consumption + value));
    }

    /**
     * Updates the upload or download transfer rate taking the passed value into account.
     * 
     * @param avgKey The name of the statistic.
     * @param value The value
     */
    public void updateAverageStat(String avgKey, int value) {

        /* All sessions of user */
//        getUserManager().updateAverageStatistics(getUser(), avgKey, value);

        /* Current session */
        String countKey = "Sample count (" + avgKey + ")";
        Map<String, Long> sessionStats = getSessionStatistics();
        Long prevAvgObj = (Long) sessionStats.get(avgKey);
        long prevAvg = prevAvgObj == null ? 0 : prevAvgObj.longValue();
        Long prevCountObj = (Long) sessionStats.get(countKey);
        long prevCount = prevCountObj == null ? 0 : prevCountObj.longValue();
        long currentAvg = (prevAvg * prevCount + value) / (prevCount + 1);
        sessionStats.put(avgKey, new Long(currentAvg));
        sessionStats.put(countKey, new Long(prevCount + 1));
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setServiceContext(org.ietf.jgss.GSSContext)
     */
    public void setServiceContext(GSSContext gssContext) {
        this.serviceContext = gssContext;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getServiceContext()
     */
    public GSSContext getServiceContext() {
        return serviceContext;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getReplyType()
     */
    public String getReplyType() {
        return replyType;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setReplyType(java.lang.String)
     */
    public void setReplyType(String replyType) {
        this.replyType = replyType;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getGSSIdentity()
     */
    public GSSName getGSSIdentity() {
        return this.gssIdentity;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setGSSIdentity(org.ietf.jgss.GSSName)
     */
    public void setGSSIdentity(GSSName identity) {
        this.gssIdentity = identity;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getFileSystemConnection()
     */
    public FileSystemConnection getFileSystemConnection() {
        return fileSystemConnection;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getFileSystem()
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getParallelStart()
     */
    public int getParallelStart() {
        return parallelStart;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setParallelStart(int)
     */
    public void setParallelStart(int parallelStart) {
        this.parallelStart = parallelStart;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getParallelMin()
     */
    public int getParallelMin() {
        return parallelMin;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setParallelMin(int)
     */
    public void setParallelMin(int parallelMin) {
        this.parallelMin = parallelMin;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getParallelMax()
     */
    public int getParallelMax() {
        return parallelMax;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setParallelMax(int)
     */
    public void setParallelMax(int parallelMax) {
        this.parallelMax = parallelMax;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#isConfirmEOFs()
     */
    public boolean isConfirmEOFs() {
        return confirmEOFs;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setConfirmEOFs(boolean)
     */
    public void setConfirmEOFs(boolean confirmEOFs) {
        this.confirmEOFs = confirmEOFs;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getBufferSize()
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setBufferSize(int)
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getNetworkStack()
     */
    public int getNetworkStack() {
        return networkStack;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setNetworkStack(int)
     */
    public void setNetworkStack(int networkStack) {
        this.networkStack = networkStack;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getDataChannelProvider()
     */
    public DataChannelProvider getDataChannelProvider() {
        return this.dataChannelProvider;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setDataChannelProvider(au.org.arcs.griffin.cmd.DataChannelProvider)
     */
    public void setDataChannelProvider(DataChannelProvider dataChannelProvider) {
        this.dataChannelProvider = dataChannelProvider;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#closeDataChannels()
     */
    public void closeDataChannels() {
        if (getDataChannelProvider() != null) {
            getDataChannelProvider().closeProvider();
            this.dataChannelProvider = null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#getDCAU()
     */
    public int getDCAU() {
        return this.dcauType;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.common.FtpSessionContext#setDCAU(int)
     */
    public void setDCAU(int dcauType) {
        this.dcauType = dcauType;
    }

	@Override
	public int getControlChannelMode() {
		// TODO Auto-generated method stub
		return this.controlChannelMode;
	}

	@Override
	public void setControlChannelMode(int mode) {
		// TODO Auto-generated method stub
		this.controlChannelMode=mode;
		this.authenticated=true;
	}

	public InetAddress getClientInetAddress() {
		if (this.clientSocket!=null) return clientSocket.getInetAddress();
		return clientInetAddress;
	}

	public void setClientInetAddress(InetAddress clientInetAddress) {
		this.clientInetAddress = clientInetAddress;
	}

	public InetAddress getLocalInetAddress() {
		if (this.clientSocket!=null) return clientSocket.getLocalAddress();
		return localInetAddress;
	}

	public void setLocalInetAddress(InetAddress localInetAddress) {
		this.localInetAddress = localInetAddress;
	}
	
	
}
