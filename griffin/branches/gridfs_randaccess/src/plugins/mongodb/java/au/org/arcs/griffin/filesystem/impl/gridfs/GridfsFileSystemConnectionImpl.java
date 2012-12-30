/*
 * GridfsFileSystemConnectionImpl.java
 * 
 * Implementation for access of a MongoDB GridFS storage resource.
 * 
 * Created: 2010-09-19 Guy K. Kloss <g.kloss@massey.ac.nz>
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
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.FileSystemConnection;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;


/**
 * Implementation of the 
 * {@link au.org.arcs.griffin.filesystem.FileSystemConnection} interface
 * for the access of file objects in a MongoDB GridFS storage resource. This 
 * class handles the access to the GridFS system.
 *
 * @version $Revision: 1.1 $
 * @author Guy K. Kloss
 */
public class GridfsFileSystemConnectionImpl implements FileSystemConnection {
    private GridfsConfig _config = null;
    private Mongo _mongoInstance = null;
    private DB _db = null;
    private GridFS _fs = null;
    private GSSCredential _credential = null;

    private static Log log = LogFactory.getLog(GridfsFileObject.class);

    /**
     * Constructor.
     * 
     * @param config Configuration container.
     * @param credential Generic credential of connection.
     * @throws IOException
     *             In case the connection cannot be established.
     */
    public GridfsFileSystemConnectionImpl(GridfsConfig config,
                                          GSSCredential credential) throws IOException {
        if (!config.getServerType().equalsIgnoreCase(GridfsConstants.SERVER_TYPE)) {
            // Bail out if we're wrong here.
            return;
        }
        
        this._config = config;
        this._credential = credential;
        
        try {
            log.debug("MongoDB/GridFS server: " + config.getServerName()
                      + ", port: " + config.getServerPort()
                      + ", DB name: " + config.getDbName());
            this._mongoInstance = new Mongo(config.getServerName(),
                                            config.getServerPort());
            this._db = this._mongoInstance.getDB(config.getDbName());
            log.debug("Got connection to MongoDB.");
            if (config.getUser() != null && !config.getUser().isEmpty()) {
                this._db.authenticate(config.getUser(), config.getPassword());
                log.debug("Authenticated at MongoDB with user \""
                          + config.getUser() + "\".");
            }
            this._fs = new GridFS(this._db, config.getBucketName());
            log.debug("Established connection to GridFS.");
        } catch (UnknownHostException e) {
            log.error("Could not connect to storage host DB '"
                      + config.getServerName() + "': " + e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (MongoException e) {
            log.error("Could not connect to MongoDB database: "
                      + e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#getFileObject(java.lang.String)
     */
    public FileObject getFileObject(String path) {
        return new GridfsFileObject(path, this);
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#getHomeDir()
     */
    public String getHomeDir() {
        // We don't have home directories as we don't track (local) user accounts.
        return "/";
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#getUser()
     */
    public String getUser() {
        try {
            return this._credential.getName().toString();
        } catch (GSSException e1) {
            log.error(e1.getStackTrace());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#close()
     */
    public void close() throws IOException {
        this._fs = null;
        this._db = null;
        this._mongoInstance.close();
        this._mongoInstance = null;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#isConnected()
     */
    public boolean isConnected() {
        if (this._fs != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#getFreeSpace(java.lang.String)
     */
    public long getFreeSpace(String path) {
        // Other implementations that don't know how to find out return -1,
        // so let's do that, too.
        return -1;
    }

    /**
     * Returns a GridFS handle.
     * 
     * @return Returns the fs.
     */
    public GridFS getFs() {
        return this._fs;
    }
    
    /**
     * Configuration for this MongoDB/GridFS storage backend.
     * @return Returns the configuration container.
     */
    public GridfsConfig getConfig() {
        return _config;
    }
}
