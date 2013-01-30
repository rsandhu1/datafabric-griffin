/*
 * JargonFileSystemConnectionImpl.java
 * 
 * Implementation of Jargon file system storage interface.
 * 
 * Created: Shunde Zhang <shunde.zhang@arcs.org.au>
 * Changed:
 * 
 * Copyright (C) 2010 Australian Research Collaboration Service
 * 
 * Some rights reserved
 * 
 * http://www.arcs.org.au/
 */

package au.org.arcs.griffin.filesystem.impl.jargon;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.myproxy.GetParams;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.irods.jargon.core.connection.GSIIRODSAccount;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSAccount.AuthScheme;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;

import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.FileSystemConnection;

/**
 * An implementation for Jargon.
 * @author Shunde Zhang
 */
public class JargonFileSystemConnectionImpl implements FileSystemConnection {

    private static Log log = LogFactory.getLog(JargonFileSystemConnectionImpl.class);
    private IRODSFileFactory fileFactory;
    private IRODSAccount account;
    private String user;
    private String homeCollection;
    private JargonFileSystemImpl jargonFileSystem;

    /**
     * Constructor.
     * 
     * @param serverName Name of iRODS server.
     * @param serverPort Port of iRODS server.
     * @param serverType Type of iRODS server (must be "irods").
     * @param credential GSS credential.
     * @param defaultResource Default resource to use.
     * @throws NullPointerException On configuration problems. 
     * @throws IOException On access problems. 
     */
    public JargonFileSystemConnectionImpl(JargonFileSystemImpl jargonFileSystem, String serverName, int serverPort,
                                          GSSCredential credential,
                                          String defaultResource)
                throws NullPointerException, IOException {
    	this.jargonFileSystem=jargonFileSystem;
        log.debug("server:" + serverName + " serverPort:" + serverPort
                + " credential:" + credential.toString());
//            if (defaultResource != null) {
//                account.setDefaultStorageResource(defaultResource);
//            }
        try {
//            	IRODSCommands cmd=jargonFileSystem.getIRODSFileSystem().currentConnection(account);
            account = GSIIRODSAccount.instance(serverName, serverPort, credential, defaultResource==null?"":defaultResource);
			fileFactory = new IRODSFileFactoryImpl(jargonFileSystem.getIRODSFileSystem(), account);
            user = account.getUserName();
            homeCollection = account.getHomeDirectory();
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
    }

    /**
     * Constructor.
     * 
     * @param serverName Name of iRODS server.
     * @param serverPort Port of iRODS server.
     * @param serverType Type of iRODS server (must be "irods").
     * @param username iRODS user name.
     * @param zoneName iRODS zone name.
     * @param credential GSS credential.
     * @param defaultResource Default resource to use.
     * @throws NullPointerException On configuration problems. 
     * @throws IOException On access problems.
     */
    public JargonFileSystemConnectionImpl(JargonFileSystemImpl jargonFileSystem, String serverName, int serverPort,
                                          String username,String zoneName,
                                          GSSCredential credential,
                                          String defaultResource)
                throws NullPointerException, IOException {
    	this.jargonFileSystem=jargonFileSystem;
        log.debug("server:" + serverName + " serverPort:" + serverPort
                + " user: " + username + "@" + zoneName + " credential:"
                + credential.toString());
        try {
            account = GSIIRODSAccount.instance(serverName, serverPort, credential, defaultResource==null?"":defaultResource);
            account.setZone(zoneName);
            account.setUserName(username);
//            account.setDefaultStorageResource(defaultResource);
            account.setHomeDirectory("/" + zoneName + "/home/" + username);
//			account = GSIIRODSAccount.instance(serverName, serverPort,
//													credential, 
//			                                        "/" + zoneName
//			                                        + "/home/" + username,
//			                                        defaultResource);
//	        account.setAuthenticationScheme(AuthScheme.GSI);
			fileFactory = new IRODSFileFactoryImpl(jargonFileSystem.getIRODSFileSystem(), account);
            user = account.getUserName();
            homeCollection = account.getHomeDirectory();
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
    }

    public JargonFileSystemConnectionImpl(JargonFileSystemImpl jargonFileSystem, String serverName, int serverPort,
            String defaultAuthType, String username, String password,
            String zoneName,
            String defaultResource)
				throws NullPointerException, IOException {
    	this.jargonFileSystem=jargonFileSystem;
		String authType=defaultAuthType;
		log.debug("server:" + serverName + " serverPort:" + serverPort
				+ " user: " + username + "@" + zoneName + " authType:" + authType
				 );
		account = new IRODSAccount(serverName, serverPort,
									username, password,
			                      "/" + zoneName
			                      + "/home/" + username,
			                      zoneName, defaultResource==null?"":defaultResource);
    	if (authType.equalsIgnoreCase("pam")) 
    		account.setAuthenticationScheme(AuthScheme.PAM);
        try {
			fileFactory = new IRODSFileFactoryImpl(jargonFileSystem.getIRODSFileSystem(), account);
            user = account.getUserName();
            homeCollection = account.getHomeDirectory();
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            try {
				this.jargonFileSystem.getIRODSFileSystem().closeSession(account);
			} catch (JargonException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new IOException(e.getMessage());
		} 
		
	}

    
    /**
     * {@inheritDoc}
     */
    public FileObject getFileObject(String path) {
        try {
			return new JargonFileObject(this, path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }

    /**
     * {@inheritDoc}
     */
    public String getHomeDir() {
        return homeCollection;
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
    public void close() throws IOException {
        if (fileFactory != null) {
            log.debug("closing irods connecton:" + fileFactory);
            try {
				this.jargonFileSystem.getIRODSFileSystem().closeSession(this.account);
			} catch (JargonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
            fileFactory = null;
        }

    }

    /**
     * {@inheritDoc}
     */
    public boolean isConnected() {
        try {
			return jargonFileSystem.getIRODSFileSystem().currentConnection(account).isConnected();
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    }

    /**
     * {@inheritDoc}
     */
    public long getFreeSpace(String path) {
        return -1;
    }

	public IRODSFileFactory getFileFactory() {
		return fileFactory;
	}

    
}
