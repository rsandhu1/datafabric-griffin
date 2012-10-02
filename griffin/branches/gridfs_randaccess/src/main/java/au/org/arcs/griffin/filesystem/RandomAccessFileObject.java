package au.org.arcs.griffin.filesystem;

import java.io.IOException;

/**
 * Random access file object interface
 * File system has to support this in order to support partial read/write
 * 
 * @author Shunde Zhang
 *
 */
public interface RandomAccessFileObject {
	public void seek(long offset) throws IOException;
	public int read() throws IOException;
	public int read(byte[] b) throws IOException;
	public int read(byte[] b, int off, int len) throws IOException;
	public void close() throws IOException;
	public String readLine() throws IOException;
	public void write(int b) throws IOException;
	public void write(byte[] b) throws IOException;
	public void write(byte[] b, int off, int len) throws IOException;
	public long length() throws IOException;
}
