/*
 * GridfsFileSystemImpl.java
 * 
 * Implementation of a general file system connecting to MongoDB GridFS.
 * 
 * Created: 2010-10-07 Guy K. Kloss <guy.kloss@aut.ac.nz>
 * Changed: 2012-12-06 Guy K. Kloss <guy.kloss@aut.ac.nz>
 * 
 * Version: $Id$
 * 
 * Copyright (C) 2012 Auckland University of Technology, New Zealand
 * 
 * Some rights reserved
 * 
 * http://www.aut.ac.nz/
 */
 
package au.org.arcs.griffin.filesystem.impl.gridfs;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.filesystem.FileSystemConnection;

/**
 * Implementation of the {@link au.org.arcs.griffin.filesystem.FileSystem) 
 * interface linking up general file system related stuff.
 * 
 * @version $Revision: 1.1 $
 * @author Guy K. Kloss
 */
public class GridfsFileSystemImpl implements FileSystem {

    private String _serverName = null;
    private int _serverPort = GridfsConstants.DEFAULT_SERVER_PORT;
    private String _serverType = GridfsConstants.SERVER_TYPE;
    private String _dbName = null;
    private String _bucketName = GridfsConstants.BUCKET_NAME;
    private String _user = null;
    private char[] _password = null;
    
    private static Log log = LogFactory.getLog(GridfsFileObject.class);
    

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#getSeparator()
     */
    public String getSeparator() {
        return GridfsConstants.FILE_SEP;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#init()
     */
    public void init() throws IOException {
        // We'll have every GridfsFileSystemConnection establish its own
        // DB connection. Maybe this is not a good idea, maybe it is. That's
        // something to evaluate for the future.
        // Anyway ... therefore, nothing to do here.
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#createFileSystemConnection(org.ietf.jgss.GSSCredential)
     */
    public FileSystemConnection createFileSystemConnection(
            GSSCredential credential) throws FtpConfigException, IOException {
        try {
            log.debug("Connected with DN = " + credential.getName().toString());
            return new GridfsFileSystemConnectionImpl(this._serverName,
                                                      this._serverPort,
                                                      this._serverType,
                                                      this._dbName,
                                                      this._bucketName,
                                                      this._user,
                                                      this._password,
                                                      credential);
        } catch (NullPointerException e) {
            log.error("Could not connect to MongoDB: '"
                      + e.getStackTrace().toString());
            throw new FtpConfigException("Problem with GridFS storage backend configuration: "
                                         + e.getMessage());
        } catch (GSSException e) {
            log.error("Problem with access credentials: '"
                      + e.getMessage());
            throw new FtpConfigException("Problem with access credentials: "
                                         + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#createFileSystemConnection(java.lang.String, java.lang.String)
     */
    @Override
    public FileSystemConnection createFileSystemConnection(
            String username, String password)
            throws FtpConfigException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#createFileSystemConnectionWithPublicKey(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public FileSystemConnection createFileSystemConnectionWithPublicKey(
            String username, String sshKeyType, String base64KeyString)
            throws FtpConfigException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#exit()
     */
    public void exit() {
        // We don't need to close any connections according to the MongoDB
        // documentation. Easy as ... :-)
    }

 
    /**
     * Gets server name for connect.
     * @return Returns the serverName.
     */
    public String getServerName() {
        return _serverName;
    }
    
    /**
     * Sets server name for connect.
     * @param serverName The serverName to set.
     */
    public void setServerName(String serverName) {
        this._serverName = serverName;
    }

    /**
     * Gets server port for connect.
     * @return Returns the serverPort.
     */
    public int getServerPort() {
        return _serverPort;
    }

    /**
     * Sets server port for connect.
     * @param serverPort The serverPort to set.
     */
    public void setServerPort(int serverPort) {
        this._serverPort = serverPort;
    }
    
    /**
     * Gets server type for this connection (should be "gridfs").
     * @return Returns the serverType.
     */
    public String getServerType() {
        return _serverType;
    }

    /**
     * Sets server type for this connection (should be "gridfs").
     * @param serverType The serverType to set.
     */
    public void setServerType(String serverType) {
        this._serverType = serverType;
    }

    /**
     * Gets DB name for connect.
     * @return Returns the dbName.
     */
    public String getDbName() {
        return _dbName;
    }

    /**
     * Sets DB name for connect.
     * @param dbName The dbName to set.
     */
    public void setDbName(String dbName) {
        this._dbName = dbName;
    }

    /**
     * Gets GridFS bucket name for storage.
     * @return Returns the bucket name.
     */
    public String getBucketName() {
        return _bucketName;
    }

    /**
     * Sets GridFS bucket name for storage.
     * @param bucketName The bucket name to set.
     */
    public void setBucketName(String bucketName) {
        this._bucketName = bucketName;
    }

    /**
     * Gets DB user for connect.
     * @return Returns the user.
     */
    public String getUser() {
        return _user;
    }

    /**
     * Sets DB user for connect.
     * @param user The user to set.
     */
    public void setUser(String user) {
        this._user = user;
    }

    /**
     * Gets DB password for connect.
     * @return Returns the password.
     */
    public char[] getPassword() {
        return _password;
    }

    /**
     * Sets DB password for connect.
     * @param password The password to set.
     */
    public void setPassword(char[] password) {
        this._password = password;
    }
}
