/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package au.org.arcs.sftp.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import au.org.arcs.sftp.SftpConstants;
import au.org.arcs.sftp.SftpProtocolConstants;

/**
 * Copied from org.apache.sshd.common.util.Buffer to use locally 
 * with some modifications & additions.
 * Can't overload Buffer as defined final.
 * 
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 * @author John Curtis
 */
public class PacketBuffer
{
	private byte[]		data;
	private int			rpos;
	private int			wpos;

	public PacketBuffer()
	{
		this(0);
	}

	public PacketBuffer(int size)
	{
		this(new byte[size], false);
	}

	public PacketBuffer(byte[] data)
	{
		this(data, 0, data.length, true);
	}

	public PacketBuffer(byte[] data, boolean read)
	{
		this(data, 0, data.length, read);
	}

	public PacketBuffer(byte[] data, int off, int len)
	{
		this(data, off, len, true);
	}

	public PacketBuffer(byte[] data, int off, int len, boolean read)
	{
		assert len>=0 && len<=data.length: "Bad buffer length";
		assert off>=0 && off<=len: "Bad buffer offset";
		this.data = data;
		this.rpos = off;
		this.wpos = read ? len : 0;
	}
	
	//Load SFTP packet from stream
	public PacketBuffer(DataInputStream dis) throws IOException
	{
		putPacket(dis);
	}
	

	@Override
	public String toString()
	{
		return "PacketBuffer [rpos=" + rpos + ", wpos=" + wpos + ", size=" + data.length + "]";
	}

	public int getDataLength()
	{
		return data==null ? 0:data.length;
	}
	public boolean haveData()
	{
		return getDataLength()>0;
	}

	public int rpos()
	{
		return rpos;
	}
	public int wpos()
	{
		return wpos;
	}
	public int getpos(boolean read)
	{
		return read ? rpos:wpos;
	}
	//Unread data
	public int available()
	{
		return wpos - rpos;
	}
	//Write buffer available before realloc needed
	public int getHeadRoom()
	{
		return getDataLength()-wpos;
	}

	protected byte[] rawdata()
	{
		return data;
	}

	protected void rpos(int rpos)
	{
		this.rpos = rpos;
	}

	protected void wpos(int wpos)
	{
		ensureCapacity(wpos - this.wpos);
		this.wpos = wpos;
	}

	public void compact()
	{
		if (available() > 0)
		{
			System.arraycopy(data, rpos, data, 0, wpos - rpos);
		}
		wpos -= rpos;
		rpos = 0;
	}

	public byte[] getCompactData()
	{
		int l = available();
		if (l > 0)
		{
			byte[] b = new byte[l];
			System.arraycopy(data, rpos, b, 0, l);
			return b;
		}
		else
		{
			return new byte[0];
		}
	}

	public void clear()
	{
		rpos = 0;
		wpos = 0;
	}

	/*
	 * ====================== Read methods ======================
	 */

	public byte getByte()
	{
		ensureAvailable(1);
		return data[rpos++];
	}

	public int getInt()
	{
		return (int) getUInt();
	}

	public long getUInt()
	{
		ensureAvailable(4);
		long l = ((data[rpos++] << 24) & 0xff000000L) | ((data[rpos++] << 16) & 0x00ff0000L) | ((data[rpos++] << 8) & 0x0000ff00L)
				| ((data[rpos++]) & 0x000000ffL);
		return l;
	}

	public long getLong()
	{
		ensureAvailable(8);
		long l = ((data[rpos++] << 56) & 0xff00000000000000L) | ((data[rpos++] << 48) & 0x00ff000000000000L)
				| ((data[rpos++] << 40) & 0x0000ff0000000000L) | ((data[rpos++] << 32) & 0x000000ff00000000L)
				| ((data[rpos++] << 24) & 0x00000000ff000000L) | ((data[rpos++] << 16) & 0x0000000000ff0000L)
				| ((data[rpos++] << 8) & 0x000000000000ff00L) | ((data[rpos++]) & 0x00000000000000ffL);
		return l;
	}

	public boolean getBoolean()
	{
		return getByte() != 0;
	}

	public String getString()
	{
		int len = getInt();
		if (len < 0 || len > SftpConstants.MAX_PACKET_SIZE)
		{
			throw new IllegalStateException("Bad item length: " + len);
		}
		ensureAvailable(len);
		String s = new String(data, rpos, len);
		rpos += len;
		return s;
	}

	public BigInteger getMPInt()
	{
		return new BigInteger(getBytes());
	}

	public byte[] getBytes()
	{
		int len = getInt();
		if (len < 0 || len > SftpConstants.MAX_PACKET_SIZE)
		{
			throw new IllegalStateException("Bad item length: " + len);
		}
		byte[] b = new byte[len];
		getRawBytes(b);
		return b;
	}

	public void getRawBytes(byte[] buf, int off, int len)
	{
		ensureAvailable(len);
		System.arraycopy(data, rpos, buf, off, len);
		rpos += len;
	}

	public void getRawBytes(byte[] buf)
	{
		getRawBytes(buf, 0, buf.length);
	}

	//get bytes and return length 
	//Same as getRawBytes but doesn't throw exception on overrun
	//if not enough just get what can and return actual read length
	public int getRemainingBytes(byte[] buf, int off, int len)
	{
		assert off>=0 && off<=buf.length:"Packet buffer: Bad offset";
		assert (off+len)>=0 && (off+len)<=buf.length:"Packet buffer: Bad length";		
		int copy_len=Math.min(available(),len);
		if(copy_len>0)
			System.arraycopy(data, rpos, buf, off, copy_len);
		rpos += copy_len;
		return copy_len;
	}

	public int getRemainingBytes(byte[] buf)
	{
		return getRemainingBytes(buf,0,buf.length);
	}

	private void ensureAvailable(int a)
	{
		if (available() < a)
		{
			throw new RuntimeException("Buffer Underflow");
		}
	}

	/*
	 * ====================== Write methods ======================
	 */

	public void putByte(byte b)
	{
		ensureCapacity(1);
		data[wpos++] = b;
	}

	/**
	 * Writes 32 bits
	 * 
	 * @param i
	 */
	public void putInt(long i)
	{
		ensureCapacity(4);
		data[wpos++] = (byte) (i >> 24);
		data[wpos++] = (byte) (i >> 16);
		data[wpos++] = (byte) (i >> 8);
		data[wpos++] = (byte) (i);
	}

	/**
	 * Writes 64 bits
	 * 
	 * @param i
	 */
	public void putLong(long i)
	{
		ensureCapacity(8);
		data[wpos++] = (byte) (i >> 56);
		data[wpos++] = (byte) (i >> 48);
		data[wpos++] = (byte) (i >> 40);
		data[wpos++] = (byte) (i >> 32);
		data[wpos++] = (byte) (i >> 24);
		data[wpos++] = (byte) (i >> 16);
		data[wpos++] = (byte) (i >> 8);
		data[wpos++] = (byte) (i);
	}

	public void putBoolean(boolean b)
	{
		putByte(b ? (byte) 1 : (byte) 0);
	}

	public void putBytes(byte[] b, int off, int len)
	{
		putInt(len);
		ensureCapacity(len);
		System.arraycopy(b, off, data, wpos, len);
		wpos += len;
	}

	public void putBytes(byte[] b)
	{
		putBytes(b, 0, b.length);
	}

	public void putString(String string)
	{
		putString(string.getBytes());
	}

	public void putString(byte[] str)
	{
		putInt(str.length);
		putRawBytes(str);
	}

	public void putMPInt(BigInteger bi)
	{
		putMPInt(bi.toByteArray());
	}

	public void putMPInt(byte[] foo)
	{
		int i = foo.length;
		if ((foo[0] & 0x80) != 0)
		{
			i++;
			putInt(i);
			putByte((byte) 0);
		}
		else
		{
			putInt(i);
		}
		putRawBytes(foo);
	}

	public void putRawBytes(byte[] d, int off, int len)
	{
		ensureCapacity(len);
		System.arraycopy(d, off, data, wpos, len);
		wpos += len;
	}

	public void putRawBytes(byte[] d)
	{
		putRawBytes(d, 0, d.length);
	}

	private void ensureCapacity(int capacity)
	{
		int curr_len=getDataLength();	//Works for null data
		if (curr_len - wpos < capacity)
		{
			int cw = wpos + capacity;
			byte[] tmp = new byte[SftpUtils.getNextPowerOf2(cw)];
			if(curr_len>0)
				System.arraycopy(data, 0, tmp, 0, curr_len);
			data = tmp;
		}
	}
	
	/*
	 * ====================== Stream methods ======================
	 */
	
	//Fill buffer with a SFTP packet from input stream
	public void putPacket(DataInputStream dis) throws IOException
	{
		int length = dis.readInt();
		if (length < SftpProtocolConstants.MIN_SIZEOF_PACKET_HEADER || 
			length>SftpConstants.MAX_PACKET_SIZE)
			throw new IOException("Bad SFTP packet length on read: "+length);
		ensureCapacity(length);
		//Blocking read exact number of bytes or exception thrown IOException/EOFException
		dis.readFully(data,wpos(),length);
		//Mark buffer written amount - ready for reading
		this.wpos+=length;
	}

	//get SFTP packet and write to output stream
	public void getPacket(DataOutputStream dos) throws IOException
	{
		if (available() < SftpProtocolConstants.MIN_SIZEOF_PACKET_HEADER || 
				available()>SftpConstants.MAX_PACKET_SIZE)
				throw new IOException("Bad SFTP packet length on write: "+available());
		dos.writeInt(available());
		dos.write(data, rpos(), available());
		dos.flush();
		//Mark buffer as all read ie. we have read all available buffered data
		this.rpos = this.wpos;
	}

	//Fill buffer from input stream data
	//NB. returns -1 if EOF
	public int streamToBuffer(InputStream is, int len) throws IOException
	{
		assert len>=0:"Bad buffer read length";
		ensureCapacity(len);		
		int readLen = is.read(data,wpos(),len);
		//Mark buffer written amount - ready for reading
		if(readLen>0)
			this.wpos+=readLen;
		return readLen;
	}

	//get available buffer and write to output stream
	public int streamFromBuffer(OutputStream os,boolean compact) throws IOException
	{
		int write_count=available();
		os.write(data, rpos(), write_count);
		os.flush();
		//Mark buffer as all read ie. we have read all available buffered data
		this.rpos = this.wpos;
		if(compact)
			this.compact();
		return write_count;
	}
}
