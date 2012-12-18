/*
 * GridfsFileObject.java
 * 
 * File object that links to the GridFS instance.
 * 
 * Created: 2010-09-18 Guy K. Kloss <guy.kloss@aut.ac.nz>
 * Changed:
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;


/**
 * Implementation of the {@link au.org.arcs.griffin.filesystem.FileObject} 
 * interface linking to the GridFS stored content.
 *
 * @version $Revision: 1.1 $
 * @author Guy K. Kloss
 */
public class GridfsFileObject implements FileObject {
    private String _path = null;
    private GridFSFile _fileHandle = null;
    
    private boolean _exists = false;
    private GridfsFileSystemConnectionImpl _connection = null;
    
    /**
     * Constructor.
     * 
     * @param path Absolute file path in storage system.
     * @param connection Connection to the GridFS connection object.
     */
    public GridfsFileObject(String path,
            GridfsFileSystemConnectionImpl connection) {
        if (connection == null) {
            // Bail out if we don't have a working GridFS connection.
            return;
        }
        this._path = path;
        this._connection = connection;
        
        DBObject query = new BasicDBObject("filename", path);
        DBCursor results = this._connection.getFs().getFileList(query);
        if (results.count() == 0) {
            this._exists = false;
            return;
        }

        DBObject newest = results.sort(new BasicDBObject("uploadDate", -1)).limit(1).next();
        ObjectId newest_id = new ObjectId(newest.get("_id").toString());
        this._fileHandle = this._connection.getFs().find(newest_id); 
        this._exists = true;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getName()
     */
    public String getName() {
        String[] parts = this._path.split(GridfsConstants.FILE_SEP);
        return parts[parts.length - 1];
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getPath()
     */
    public String getPath() {
        return this._path;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#exists()
     */
    public boolean exists() {
        return this._exists;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#isFile()
     */
    public boolean isFile() {
        return !this.isDirectory();
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#isDirectory()
     */
    public boolean isDirectory() {
        if (this._exists) {
            String contentType = this._fileHandle.getContentType();
            if (contentType != null && contentType.equals("collection")) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getPermission()
     */
    public int getPermission() {
        // TODO Auto-generated method stub
        // See JargonFileObject, for now just read privilege.
        return FtpConstants.PRIV_READ;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getCanonicalPath()
     */
    public String getCanonicalPath() throws IOException {
        // The path stored in the GridFS is always canonical.
        // Or did I misunderstand this?
        return this._path;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#listFiles()
     */
    public FileObject[] listFiles() {
        if (this.isFile()) {
            // Bail out, a file does not have a file list.
            return null;
        }
        
        String pathEscaped = this.getPath().replace(GridfsConstants.FILE_SEP,
                                                    "\\" + GridfsConstants.FILE_SEP);
        Pattern searchPattern = Pattern.compile("/^" + pathEscaped);
        DBObject query = new BasicDBObject("filename", searchPattern);
        List<GridFSDBFile> foundEntries = this._connection.getFs().find(query);
        
        if (foundEntries.isEmpty()) {
            return null;
        }

        // "Uniquify" entries (only one entry per path), and no deeper than
        // one level below current collection path.
        Set<String> uniqueEntries = new HashSet<String>();
        
        for (GridFSFile item : foundEntries) {
            String itemPath = item.getFilename();
            int index = itemPath.indexOf(GridfsConstants.FILE_SEP,
                                         this._path.length());
            String subPath = itemPath.substring(0, index);
            uniqueEntries.add(subPath);
        }

        FileObject[] results = new FileObject[uniqueEntries.size()];
        int index = 0;
        for (String item : uniqueEntries) {
            results[index] = new GridfsFileObject(item, this._connection);
            index++;
        }
        
        return results;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#length()
     */
    public long length() {
        if (this._exists) {
            return this._fileHandle.getLength();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#lastModified()
     */
    public long lastModified() {
        if (this._exists) {
            return this._fileHandle.getUploadDate().getTime();
        }
        return 0L;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getRandomAccessFileObject(java.lang.String)
     */
    public RandomAccessFileObject getRandomAccessFileObject(String mode)
            throws IOException {
        return new GridfsRandomAccessFileObjectImpl(this, mode);
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#delete()
     */
    public boolean delete() {
        if (this._exists) {
            this._connection.getFs().remove(this._path);
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getParent()
     */
    public FileObject getParent() {
        if (this._path.equals(GridfsConstants.FILE_SEP)) {
            return new GridfsFileObject(GridfsConstants.FILE_SEP,
                                        this._connection);
        } else {
            return new GridfsFileObject(this._path.substring(0, this._path.lastIndexOf(GridfsConstants.FILE_SEP)),
                                        this._connection);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#mkdir()
     */
    public boolean mkdir() {
        if (!this._exists) {
            GridFSInputFile newEntry = this._connection.getFs().createFile(new byte[0]);
            newEntry.setContentType("collection");
            newEntry.setFilename(this._path);
            newEntry.save();
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#renameTo(au.org.arcs.griffin.filesystem.FileObject)
     */
    public boolean renameTo(FileObject file) {
        if (this._exists) {
            this._fileHandle.put("filename", file.getName());
            this._fileHandle.save();
            return true;
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
        if (this._exists) {
            this._fileHandle.put("uploadDate", new Date(t));
            this._fileHandle.save();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the GridFS file handle of the file object.
     * 
     * @return Returns the file handle.
     */
    public GridFSFile getFileHandle() {
        return _fileHandle;
    }

    /**
     * Return the GridFS connection object.
     *  
     * @return Returns _connection.
     */
    public GridfsFileSystemConnectionImpl getConnection() {
        return _connection;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#create()
     */
    @Override
    public boolean create() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getOwner()
     */
    @Override
    public String getOwner() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getInpuStream(long)
     */
    @Override
    public InputStream getInpuStream(long offset) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
