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

import org.ietf.jgss.GSSCredential;

import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.FileSystemConnection;
import edu.sdsc.grid.io.RemoteFileSystem;
import edu.sdsc.grid.io.irods.IRODSAccount;
import edu.sdsc.grid.io.irods.IRODSFileSystem;

/**
 * An implementation for Jargon.
 * @author Shunde Zhang
 */
public class JargonFileSystemConnectionImpl implements FileSystemConnection {

    private static Log log = LogFactory.getLog(JargonFileSystemConnectionImpl.class);
    private RemoteFileSystem remoteFileSystem;
    private String user;
    private String homeCollection;

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
    public JargonFileSystemConnectionImpl(String serverName, int serverPort,
                                          String serverType,
                                          GSSCredential credential,
                                          String defaultResource)
                throws NullPointerException, IOException {
        if (serverType.equalsIgnoreCase("irods")) {
            log.debug("server:" + serverName + " serverPort:" + serverPort
                    + " credential:" + credential.toString());
            IRODSAccount account = new IRODSAccount(serverName, serverPort,
                                                    credential);
            if (defaultResource != null) {
                account.setDefaultStorageResource(defaultResource);
            }
            remoteFileSystem = new IRODSFileSystem(account);
            user = account.getUserName();
            homeCollection = account.getHomeDirectory();
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
    public JargonFileSystemConnectionImpl(String serverName, int serverPort,
                                          String serverType, String username,
                                          String zoneName,
                                          GSSCredential credential,
                                          String defaultResource)
                throws NullPointerException, IOException {
        if (serverType.equalsIgnoreCase("irods")) {
            log.debug("server:" + serverName + " serverPort:" + serverPort
                    + " user: " + username + "@" + zoneName + " credential:"
                    + credential.toString());
            IRODSAccount account = new IRODSAccount(serverName, serverPort,
                                                    username, "",
                                                    "/" + zoneName
                                                    + "/home/" + username,
                                                    zoneName, "");
            if (defaultResource != null) {
                account.setDefaultStorageResource(defaultResource);
            }
            account.setGSSCredential(credential);
            remoteFileSystem = new IRODSFileSystem(account);
            user = account.getUserName();
            homeCollection = account.getHomeDirectory();
        }
    }

    /**
     * {@inheritDoc}
     */
    public FileObject getFileObject(String path) {
        return new JargonFileObject(this, remoteFileSystem, path);
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
        if (remoteFileSystem != null) {
            if (remoteFileSystem instanceof IRODSFileSystem) {
                log.debug("closing irods connecton:" + remoteFileSystem);
                ((IRODSFileSystem) remoteFileSystem).close();
                remoteFileSystem = null;
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    public boolean isConnected() {
        return remoteFileSystem.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    public long getFreeSpace(String path) {
        return -1;
    }
}
