package au.org.arcs.griffin.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;

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
	private ArrayBlockingQueue<EDataBlock> dataQueue;
	private int bufferSize;
	
	public SynchronizedInputStream(InputStream in, int bufferSize){
		this(in,0,bufferSize);
	}
	public SynchronizedInputStream(InputStream in, long offset, int bufferSize){
		this.in=in;
		this.idx=offset;
		this.dataQueue=new ArrayBlockingQueue<EDataBlock>(10);
		this.bufferSize=bufferSize;
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
	
	public void feedQueue() throws IOException{
		byte[] b=new byte[bufferSize];
		int count;
		while ((count = in.read(b))>-1){
			EDataBlock eDataBlock=new EDataBlock("main-thread", bufferSize);
			eDataBlock.clearHeader();
			log.debug("read offset="+idx+" length="+count);
			eDataBlock.setOffset(idx);
			eDataBlock.setSize(count);
			eDataBlock.setData(b);
			idx+=count;
			try {
				dataQueue.put(eDataBlock);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
		}
	}
	
	synchronized public int read(EDataBlock eDataBlock) throws IOException {
		try {
			EDataBlock nextBlock=dataQueue.take();
			eDataBlock.setOffset(nextBlock.getOffset());
			eDataBlock.setSize(nextBlock.getSize());
			eDataBlock.setData(nextBlock.getData());
			return (int) nextBlock.getSize();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
	
	public void close() throws IOException{
		in.close();
	}
	
	public void addEndingMarker(int threadNum){
		for (int i=0;i<threadNum;i++) {
			EDataBlock eDataBlock=new EDataBlock("main-thread", bufferSize);
			eDataBlock.clearHeader();
			eDataBlock.setSize(-1);
			try {
				dataQueue.put(eDataBlock);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
