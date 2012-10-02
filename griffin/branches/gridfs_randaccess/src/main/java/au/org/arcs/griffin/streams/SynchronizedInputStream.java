package au.org.arcs.griffin.streams;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SynchronizedInputStream {
	private static Log          log                 = LogFactory.getLog(SynchronizedInputStream.class);
//	private static int BUFFER_SIZE=1048576;
//	private byte[] buffer;
	private long idx;
	private InputStream in;
//	private boolean eof;
//	private int endding;
	
	public SynchronizedInputStream(InputStream in){
		this.in=in;
		this.idx=0;
	}
	public SynchronizedInputStream(InputStream in, long offset){
		this.in=in;
		this.idx=offset;
	}
	
//	@Override
//	public int read() throws IOException {
//        if (buffer == null) {
//            if (eof) {
//                return -1;
//            }
//    		buffer=new byte[BUFFER_SIZE];
//            int count=in.read(buffer);
//            log.debug("read "+count);
//            endding=count;
//            if (count<buffer.length) {
//            	eof=true;
//            }
//        }
//        int result = buffer[(int)idx];
//        idx++;
//        if (idx >= endding) {
//            buffer = null;
//            idx = 0;
//        }
//        return result;
//	}
//	
//	synchronized public int read(byte[] b) throws IOException {
//		return super.read(b);
//	}
	
	synchronized public int read(EDataBlock eDataBlock) throws IOException {
		byte[] b=new byte[(int) eDataBlock.getPreferredBufferSize()];
		int count=in.read(b);
		if (count>-1) {
			log.debug("read offset="+idx+" length="+count);
			eDataBlock.setOffset(idx);
			eDataBlock.setSize(count);
			eDataBlock.setData(b);
			idx+=count;
		}
		return count;
	}
	
	public void close() throws IOException{
		in.close();
	}

}
