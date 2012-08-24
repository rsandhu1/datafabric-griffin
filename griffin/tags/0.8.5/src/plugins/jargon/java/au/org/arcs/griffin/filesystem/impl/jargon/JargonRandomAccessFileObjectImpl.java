/*
 * JargonRandomAccessFileObjectImpl.java
 * 
 * Implementation of Jargon/iRODS file system storage interface for random 
 * access files.
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

import au.org.arcs.griffin.filesystem.RandomAccessFileObject;
import edu.sdsc.grid.io.RemoteFile;
import edu.sdsc.grid.io.RemoteRandomAccessFile;
import edu.sdsc.grid.io.irods.IRODSFile;
import edu.sdsc.grid.io.irods.IRODSRandomAccessFile;

/**
 * an implementation for jargon
 * 
 * @author Shunde Zhang
 * 
 */
public class JargonRandomAccessFileObjectImpl implements RandomAccessFileObject {

    private RemoteRandomAccessFile raf;

    /**
     * Constructor.
     * 
     * @param file Jargon remote file instance.
     * @param mode File access mode, @see java.io.RandomAccessFile. Mostly 
     *          "r" and "rw" should be suppported. 
     * @throws IOException If file access fails or privileges are insufficient.
     */
    public JargonRandomAccessFileObjectImpl(RemoteFile file, String mode)
            throws IOException {
        if (file instanceof IRODSFile) {
            raf = new IRODSRandomAccessFile((IRODSFile) file, mode);
        } else {
            throw new IOException("Object type is not recognizable.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        raf.close();
    }

    /**
     * {@inheritDoc}
     */
    public int read() throws IOException {
        return raf.read();
    }

    /**
     * {@inheritDoc}
     */
    public int read(byte[] b) throws IOException {
        return raf.read(b);
    }

    /**
     * {@inheritDoc}
     */
    public int read(byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    public String readLine() throws IOException {
        return raf.readLine();
    }

    /**
     * {@inheritDoc}
     */
    public void seek(long offset) throws IOException {
        raf.seek(offset);
    }

    /**
     * {@inheritDoc}
     */
    public long length() throws IOException {
        return raf.length();
    }

    /**
     * {@inheritDoc}
     */
    public void write(int b) throws IOException {
        raf.write(b);
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] b) throws IOException {
        raf.write(b);
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
    }
}
