package au.org.arcs.griffin.filesystem;

import java.io.IOException;

/**
 * File system connection interface, one user has one connection to a file
 * system in each GridFTP session.
 * 
 * @version $Revision: 1.1 $
 * @author Shunde Zhang
 */
public interface FileSystemConnection {

    /**
     * Access to a referenced resource in the (virtual) file system.
     * 
     * @param path (Virtual) path to resource.
     * @return Reference to indicated file object.
     */
    public FileObject getFileObject(String path);

    /** @return (Virtual) home directory path of user. */
    public String getHomeDir();

    /** @return User (ID) of user connected. */
    public String getUser();

    /**
     * Closes file system connection.
     * 
     * @throws IOException If the file system backend chokes.
     */
    public void close() throws IOException;

    /** @return State of file system connection.. */
    public boolean isConnected();

    /**
     * Queries the free space of given resource in virtual file system on
     * storage backend.
     * 
     * @param path (Virtual) path to resource.
     * @return Free storage space (in bytes).
     */
    public long getFreeSpace(String path);
}
