package au.org.arcs.griffin.filesystem;

import java.io.IOException;

/**
 * File object interface
 * 
 * @author Shunde Zhang
 * 
 */

public interface FileObject {

    public String getName();

    public String getPath();

    /**
     * Check for the existence of a resource in the storage system.
     * 
     * @return True if it exists.
     */
    public boolean exists();

    /**
     * Check for a file.
     * 
     * @return True if it is a file.
     */
    public boolean isFile();

    /**
     * Check for a directory.
     * 
     * @return True if it is a directory.
     */
    public boolean isDirectory();

    /**
     * Permissions as UNIX file system like integer.
     * 
     * @return Permissions.
     */
    public int getPermission();

    public String getCanonicalPath() throws IOException;

    public FileObject[] listFiles() throws IOException;

    public long length();

    public long lastModified();

    /**
     * Returns an object that can be used for reading/writing data.
     * 
     * @param mode File access mode, @see java.io.RandomAccessFile. Mostly 
     *          "r" and "rw" should be suppported.
     * @return The random access file object.
     * @throws IOException If file access fails or privileges are insufficient.
     */
    public RandomAccessFileObject getRandomAccessFileObject(String mode)
            throws IOException;

    public boolean delete();

    public FileObject getParent();

    public boolean mkdir();

    public boolean renameTo(FileObject aFile);

    public boolean setLastModified(long t);
    
    public boolean create();
    
    public String getOwner();
}
