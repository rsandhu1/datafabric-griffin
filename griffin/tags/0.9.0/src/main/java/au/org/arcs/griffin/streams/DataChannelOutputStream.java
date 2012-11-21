package au.org.arcs.griffin.streams;

import java.io.IOException;
import java.io.OutputStream;

import au.org.arcs.griffin.cmd.DataChannel;

public class DataChannelOutputStream extends OutputStream {

	private DataChannel dataChannel;
	public DataChannelOutputStream(DataChannel dataChannel){
		this.dataChannel=dataChannel;
	}
	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub

	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		dataChannel.write(b, off, len);
	}
	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		dataChannel.write(b);
	}
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		dataChannel.closeChannel();
	}
	
}
