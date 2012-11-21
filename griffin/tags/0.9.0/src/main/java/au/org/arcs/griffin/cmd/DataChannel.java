package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.io.OutputStream;

import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.SynchronizedInputStream;
import au.org.arcs.griffin.streams.SynchronizedOutputStream;

public interface DataChannel extends Runnable{
	public static int DIRECTION_GET=0;
	public static int DIRECTION_PUT=1;
    /**
     * Initializes the passive channel.
     * 
     * @throws IOException Error on initializing the data channel.
     * @return Information about the data channel is provided.
     */
    DataChannelInfo init() throws IOException;

    /**
     * Provides the socket for data transfer. Multiple calls of this method do not result in
     * multiple socket instance. One instance is created and cached.
     * 
     * @return The Socket.
     * @throws IOException Error on creating the data channel.
     */
    void start() throws IOException;

    /**
     * Closes the socket, if necessary.
     */
    void closeChannel();
    void write(byte[] b) throws IOException;
    void write(byte[] b, int start, int len) throws IOException;
    int read(byte[] b) throws IOException;
    int read(byte[] b, int start, int len) throws IOException;
    void setFileObject(FileObject file);
    void setDirection(int direction);
    void setDataChannelProvider(DataChannelProvider provider);
    void setSynchronizedInputStream(SynchronizedInputStream sis);
    void setSynchronizedOutputStream(SynchronizedOutputStream sos);

	OutputStream getOutputStream();
}
