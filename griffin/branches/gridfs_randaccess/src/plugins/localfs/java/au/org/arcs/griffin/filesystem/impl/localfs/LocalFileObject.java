/*
 * LocalFileObject.java
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;
import au.org.arcs.griffin.usermanager.model.GroupDataList;

/**
 * Implementation of local file system storage interface.
 * 
 * @author Shunde Zhang
 */
public class LocalFileObject implements FileObject {

    private static Log log = LogFactory.getLog(LocalFileObject.class);
    private File localFile;
    private LocalFileSystemConnectionImpl connection;

    /** Relative, canonicalised path in the GridFTP server context. */
    private String canonicalPath;

    /** Relative path in the GridFTP server context. */
    private String path;

    /**
     * Constructor.
     * 
     * @param aPath
     *            File/directory path.
     * @param aConnection
     *            Connection to file system handler.
     */
    public LocalFileObject(String aPath,
            LocalFileSystemConnectionImpl aConnection) {
        this.path = aPath;
        if (aPath.equals(".") || aPath.equals("..")) {
            this.canonicalPath = aPath;
        } else {
            this.canonicalPath = FilenameUtils.normalizeNoEndSeparator(aPath);
        }
        this.connection = aConnection;
        this.localFile = new File(connection.getRootPath(), canonicalPath);
    }

    /**
     * Handle to the actual file.
     * 
     * @return Local file.
     */
    public File getLocalFile() {
        return this.localFile;
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#delete()
     */
    public boolean delete() {
        return localFile.delete();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#exists()
     */
    public boolean exists() {
        return localFile.exists();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#getCanonicalPath()
     */
    public String getCanonicalPath() throws IOException {
        return canonicalPath;
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#getName()
     */
    public String getName() {
        return localFile.getName();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#getParent()
     */
    public FileObject getParent() {
        if (canonicalPath.equals(FtpConstants.PATH_SEPARATOR)) {
            return new LocalFileObject(FtpConstants.PATH_SEPARATOR, connection);
        } else {
            return new LocalFileObject(canonicalPath.substring(0, canonicalPath.lastIndexOf(FtpConstants.PATH_SEPARATOR)),
                                       connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#getPath()
     */
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#getPermission()
     */
    public int getPermission() {
        int result = FtpConstants.PRIV_NONE;
        try {
            GroupDataList list = connection.getGroupDataList();
            String absoluteVirtualPath = null;
            if (canonicalPath.startsWith(FtpConstants.PATH_SEPARATOR)) {
                absoluteVirtualPath = canonicalPath;
            } else {
                absoluteVirtualPath = FilenameUtils.concat(FtpConstants.PATH_SEPARATOR,
                                                           canonicalPath);
            }
            result = list.getPermission(absoluteVirtualPath,
                                        connection.getUser(),
                                        connection.getRootPath());
        } catch (FtpConfigException e) {
            log.error(e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#getRandomAccessFileObject(java.lang.String)
     */
    public RandomAccessFileObject getRandomAccessFileObject(String type)
            throws IOException {
        return new LocalRandomAccessFileObjectImpl(localFile, type);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#isDirectory()
     */
    public boolean isDirectory() {
        return localFile.isDirectory();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#isFile()
     */
    public boolean isFile() {
        return localFile.isFile();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#lastModified()
     */
    public long lastModified() {
        return localFile.lastModified();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#length()
     */
    public long length() {
        return localFile.length();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#listFiles()
     */
    public FileObject[] listFiles() {
        File[] fileList = localFile.listFiles();
        FileObject[] list = new FileObject[fileList.length];
        
//        // Add two entries for current and parent directory.
//        list[0] = new LocalFileObject(".", connection);
//        list[1] = new LocalFileObject("..", connection);
        
        String myPath;
        for (int i = 0; i < fileList.length; i++) {
            try {
                myPath = fileList[i].getCanonicalPath();
                list[i] = new LocalFileObject(myPath.substring(connection.getRootPath().length() + 1),
                                                  connection);
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#mkdir()
     */
    public boolean mkdir() {
        return localFile.mkdir();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#renameTo(au.org.arcs.griffin.filesystem.FileObject)
     */
    public boolean renameTo(FileObject aFile) {
        if (aFile instanceof LocalFileObject) {
            return this.localFile.renameTo(((LocalFileObject) aFile).getLocalFile());
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.FileObject#setLastModified(long)
     */
    public boolean setLastModified(long t) {
        return localFile.setLastModified(t);
    }
}
