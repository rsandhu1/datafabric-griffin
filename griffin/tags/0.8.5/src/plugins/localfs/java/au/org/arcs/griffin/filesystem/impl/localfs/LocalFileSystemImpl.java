/*
 * LocalFileSystemImpl.java
 * 
 * Implementation of local file system storage interface.
 * 
 * Created: 2010-01-04 Shunde Zhang <shunde.zhang@arcs.org.au>
 * Changed:
 * 
 * Copyright (C) 2010 Australian Research Collaboration Service
 * 
 * Some rights reserved
 * 
 * http://www.arcs.org.au/
 */

package au.org.arcs.griffin.filesystem.impl.localfs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.griffin.usermanager.UserManager;
import au.org.arcs.griffin.usermanager.model.GroupDataList;
import au.org.arcs.griffin.usermanager.model.UserData;

/**
 * Implementation of local file system storage interface.
 *
 * @author Guy K. Kloss
 */
public class LocalFileSystemImpl implements FileSystem {

    private static Log log = LogFactory.getLog(LocalFileSystemImpl.class);
    private String rootPath;
    private UserManager userManager;

    /**
     * Getter method for the java bean <code>userManager</code>.
     * 
     * @return Returns the value of the java bean <code>userManager</code>.
     */
    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * Setter method for the java bean <code>userManager</code>.
     * 
     * @param userManager The value of userManager to set.
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Returns the root path of the file system.
     * 
     * @return The root path.
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * Sets the root path of the file system.
     * 
     * @param rootPath A new root path to set.
     */
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#createFileSystemConnection(org.ietf.jgss.GSSCredential)
     */
    public FileSystemConnection createFileSystemConnection(GSSCredential credential)
            throws FtpConfigException, IOException {
        try {
            String dn = credential.getName().toString();
            UserData userData = userManager.authenticate(dn);
            // setAttribute(FtpConstants.ATTR_USER_DATA, userData);
            GroupDataList groupList = userManager.getGroupDataList(userData.getUid());
            // setAttribute(ATTR_GROUP_DATA, groupList);
            return new LocalFileSystemConnectionImpl(rootPath, userData,
                                                     groupList);
        } catch (GSSException e) {
            log.error("Error with GSS authentication: " + e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#exit()
     */
    public void exit() {
        // Intentionally left blank.
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#getSeparator()
     */
    public String getSeparator() {
        return File.separator;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystem#init()
     */
    public void init() throws IOException {
        if (rootPath == null) {
            throw new IOException("rootPath is not configured.");
        }
        File root = new File(rootPath);
        if (!root.exists()) {
            FileUtils.forceMkdir(root);
        }
        if (!root.exists()) {
            throw new IOException("rootPath does not exist or is not accessible.");
        }
        log.debug("load user and permission config");
        userManager.load();
    }
}
