/*
 * LocalRandomAccessFileObjectImpl.java
 * 
 * Implementation of local file system storage interface.
 * 
 * Created: 2010-01-04 Shunde Zhang <shunde.zhang@arcs.org.au>
 * Changed:
 * 
 * Copyright (C) 2010 Australian Research Collaboration Service
 * 
 * Some rights reserved
 * 
 * http://www.arcs.org.au/
 */

package au.org.arcs.griffin.filesystem.impl.localfs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.filesystem.RandomAccessFileObject;

public class LocalRandomAccessFileObjectImpl implements RandomAccessFileObject {
	private static Log log = LogFactory.getLog(LocalRandomAccessFileObjectImpl.class);
	private RandomAccessFile raf;
	
	public LocalRandomAccessFileObjectImpl(File file, String type) throws IOException{
		raf=new RandomAccessFile(file,type);
	}
	
	public void close() throws IOException {
		raf.close();

	}

	public long length() throws IOException {
		// TODO Auto-generated method stub
		return raf.length();
	}

	public int read() throws IOException {
		// TODO Auto-generated method stub
		return raf.read();
	}

	public int read(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		return raf.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return raf.read(b, off, len);
	}

	public String readLine() throws IOException {
		// TODO Auto-generated method stub
		return raf.readLine();
	}

	public void seek(long offset) throws IOException {
		// TODO Auto-generated method stub
		raf.seek(offset);
	}

	public void write(int b) throws IOException {
		// TODO Auto-generated method stub
		raf.write(b);
	}

	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		raf.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		raf.write(b,off,len);
	}

}
