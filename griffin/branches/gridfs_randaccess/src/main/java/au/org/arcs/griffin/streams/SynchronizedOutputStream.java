package au.org.arcs.griffin.streams;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;

public class SynchronizedOutputStream {
	private static Log          log                 = LogFactory.getLog(SynchronizedOutputStream.class);
	private RandomAccessFileObject raf;
	private ArrayBlockingQueue<EDataBlock> dataQueue;
	private FtpSessionContext ctx;
	public SynchronizedOutputStream(RandomAccessFileObject raf, FtpSessionContext ctx){
		this.raf=raf;
		this.ctx=ctx;
		this.dataQueue=new ArrayBlockingQueue<EDataBlock>(10);
	}
	synchronized public void write(EDataBlock eDataBlock) throws IOException {
		try {
			EDataBlock newBlock=new EDataBlock("main-thread", eDataBlock.getPreferredBufferSize());
			newBlock.clearHeader();
			newBlock.setOffset(eDataBlock.getOffset());
			newBlock.setSize(eDataBlock.getSize());
			newBlock.setData(eDataBlock.getData());
			dataQueue.put(newBlock);
		} catch (InterruptedException e) {
		    log.error(e, e);
            throw new IOException(e.getMessage());
		}
	}
	public void close() throws IOException{
		raf.close();
	}
	public void pollQueue() throws IOException {
		while (true) {
			try {
				EDataBlock eDataBlock=dataQueue.take();
				log.debug("got a new data block:"+eDataBlock);
				if (eDataBlock.getSize()==0) {
					log.debug("got no data, eod; dataQueue.isEmpty()?"+dataQueue.isEmpty());
					if (dataQueue.isEmpty()) break;
				}else{
					log.debug("going to write "+eDataBlock.getData().length+" bytes");
					raf.seek(eDataBlock.getOffset());
					raf.write(eDataBlock.getData());
					log.debug("written "+eDataBlock);
	                ctx.updateIncrementalStat(FtpConstants.STAT_BYTES_UPLOADED, eDataBlock.getSize());
	                ctx.getTransferMonitor().execute(eDataBlock.getOffset(), eDataBlock.getSize());
				}
			} catch (InterruptedException e) {
			    log.error(e, e);
                throw new IOException(e.getMessage());
			}
		}
	}
}
