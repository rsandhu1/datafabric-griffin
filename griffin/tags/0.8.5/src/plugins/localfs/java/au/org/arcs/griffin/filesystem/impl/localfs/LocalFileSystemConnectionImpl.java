/*
 * LocalFileSystemConnectionImpl.java
 * 
 * Implementation of the local file system connection.
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
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.griffin.usermanager.model.GroupDataList;
import au.org.arcs.griffin.usermanager.model.UserData;
import au.org.arcs.griffin.utils.VarMerger;

/**
 * Implementation of the local file system connection.
 *
 * @author Guy K. Kloss
 */
public class LocalFileSystemConnectionImpl implements FileSystemConnection {

    private static Log log = LogFactory.getLog(LocalFileSystemConnectionImpl.class);

    /** User data configuration for this particular user. */
    private UserData userData;
    
    /** Data of a configured user group. */
    private GroupDataList groupDataList;
    
    /** Absolute path of root directory in local file system. */
    private String rootPath;

    /** Indicates whether the file system is connected. */
    private boolean isConnected;
    
    /** User's home directory (absolute?). TODO: Check this! */
    private String homeDir;

    /**
     * Constructor.
     * 
     * @param myRootPath Root directory in local file system.
     * @param myUserData Configuration for a particular user.
     * @param myGroupDataList Configuration for list of groups.
     * @throws IOException If configuration file reading fails.
     */
    public LocalFileSystemConnectionImpl(String myRootPath, UserData myUserData,
            GroupDataList myGroupDataList) throws IOException {

        this.rootPath = myRootPath;
        this.userData = myUserData;
        this.groupDataList = myGroupDataList;

        homeDir = FilenameUtils.normalizeNoEndSeparator(FilenameUtils.concat(FtpConstants.PATH_SEPARATOR,
                                                                             getStartDir()));
        log.debug("Default (home) dir for user \"" + userData.getUid() + "\": "
                  + homeDir);
        String fsHomeDir = FilenameUtils.concat(this.rootPath, getStartDir());
        log.debug("Default (home) file system path for user \"" + userData.getUid() + "\": "
                  + fsHomeDir);
        File dir = new File(fsHomeDir);
        if (!dir.exists()) {
            FileUtils.forceMkdir(dir);
        }
        isConnected = true;
    }

    /**
     * Gets user data as configured.
     * 
     * @return Configuration for a particular user.
     */
    public UserData getUserData() {
        return userData;
    }

    /**
     * Sets user data.
     * 
     * @param newUserData Configuration for a particular user.
     */
    public void setUserData(UserData newUserData) {
        this.userData = newUserData;
    }

    /**
     * Get data of a user group.
     * 
     * @return Configuration for list of groups.
     */
    public GroupDataList getGroupDataList() {
        return groupDataList;
    }

    /**
     * Sets group data.
     * 
     * @param aGroupDataList Configuration for list of groups.
     */
    public void setGroupDataList(GroupDataList aGroupDataList) {
        this.groupDataList = aGroupDataList;
    }

    /**
     * Gets root directory in local file system.  All FTP paths are relative 
     * to it.
     * 
     * @return Root directory.
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * Sets root directory in local file system.  All FTP paths are relative 
     * to it.
     * 
     * @param aRootPath Root directory.
     */
    public void setRootPath(String aRootPath) {
        this.rootPath = aRootPath;
    }

    /**
     * Gets the path to user's virtual, absolute home directory.
     * 
     * @return User's home directory.
     * @throws FtpConfigException If place holders in the configuration file
     *      cannot be resolved.
     */
    private String getStartDir() throws FtpConfigException {
        if (userData == null) {
            throw new FtpConfigException("User data not available");
        }
        VarMerger varMerger = new VarMerger(userData.getDir());
        Properties props = new Properties();
        props.setProperty("ftproot", FilenameUtils.separatorsToUnix(rootPath));
        props.setProperty("user", userData.getUid());
        varMerger.merge(props);
        if (!varMerger.isReplacementComplete()) {
            throw new FtpConfigException("Unresolved placeholders in user configuration file found.");
        }
        return varMerger.getText();
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#close()
     */
    public void close() throws IOException {
        // Local file system does not need closing.  Empty on purpose.
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#getFileObject(java.lang.String)
     */
    public FileObject getFileObject(String path) {
        return new LocalFileObject(path, this);
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#getFreeSpace(java.lang.String)
     */
    public long getFreeSpace(String path) {
        // We are assuming all files are located on the same physical file
        // system, so we need to determine the free space from the absolute
        // path of the root of our logical file system.
        return new File(rootPath).getFreeSpace();
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#getHomeDir()
     */
    public String getHomeDir() {
        return homeDir;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#getUser()
     */
    public String getUser() {
        return userData.getUid();
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileSystemConnection#isConnected()
     */
    public boolean isConnected() {
        return isConnected;
    }
}
