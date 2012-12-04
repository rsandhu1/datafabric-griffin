package au.org.arcs.griffin.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File object interface
 * (for file system entries such as files and directories).
 * 
 * @author Shunde Zhang
 * 
 */

public interface FileObject {

    /**
     * @return Name of file system entry.
     */
    public String getName();

    /**
     * ??? check against getCanonicalPath()
     *
     * @return Full path name of file system entry.
     */
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

    /**
     * ??? check against getPath()
     *
     * @return Full path name of file system entry.
     */
    public String getCanonicalPath() throws IOException;

    /**
     * @return Array of entries within the current entry (directory listing).
     * @throws IOException
     */
    public FileObject[] listFiles() throws IOException;

    /**
     * @return Length (size) of entry in bytes.
     */
    public long length();

    /**
     * @return Time stamp of last modification.
     */
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

    /**
     * ??? Delete file system entry?
     * 
     * @return ??? True on success.
     */
    public boolean delete();

    /**
     * @return Parent entry of current entry (parent directory).
     */
    public FileObject getParent();

    /**
     * ??? Make a new directory. (Wouldn't this require a parameter?)
     * 
     * @return ??? True on success.
     */
    public boolean mkdir();

    /**
     * ??? Rename current entry to a different name.
     * 
     * @param aFile ??? New entry with altered file name/path.
     * @return ??? True on success.
     */
    public boolean renameTo(FileObject aFile);

    /**
     * Sets the last modified time stamp to a new value.
     * 
     * @param t New time stamp value
     * @return ??? True on success.
     */
    public boolean setLastModified(long t);
    
    /**
     * ??? Make a new file system entry. (Wouldn't this require a parameter?)
     * 
     * @return ??? True on success.
     */
    public boolean create();
    
    /**
     * @return Owner of file system entry.
     */
    public String getOwner();
    
    /**
     * ??? Gives reading access to the file system entry (file) content as a stream.
     * 
     * @return Reading access to content as a stream.
     * @throws IOException If file access fails or privileges are insufficient.
     */
    public OutputStream getOutputStream() throws IOException;
    
    /**
     * ??? Gives writing access to the file system entry (file) content as a stream.
     * 
     * @return Writing access to content as a stream.
     * @param offset ??? Offset in bytes from where the write should start.
     * @throws IOException If file access fails or privileges are insufficient.
     */
    public InputStream getInpuStream(long offset) throws IOException;
}
