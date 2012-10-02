package au.org.arcs.griffin.streams;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.filesystem.RandomAccessFileObject;

public class SynchronizedOutputStream {
	private static Log          log                 = LogFactory.getLog(SynchronizedOutputStream.class);
	private RandomAccessFileObject raf;
	public SynchronizedOutputStream(RandomAccessFileObject raf){
		this.raf=raf;
	}
	synchronized public void write(EDataBlock eDataBlock) throws IOException {
		raf.seek(eDataBlock.getOffset());
		raf.write(eDataBlock.getData());
		log.debug("written "+eDataBlock);
	}
	public void close() throws IOException{
		raf.close();
	}
}
