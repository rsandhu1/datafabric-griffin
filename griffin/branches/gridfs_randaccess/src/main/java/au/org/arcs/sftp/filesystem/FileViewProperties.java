package au.org.arcs.sftp.filesystem;

import org.apache.sshd.common.channel.AbstractChannel;

import au.org.arcs.sftp.utils.SftpProperties;

/**
 * File view Properties
 * 
 * @author John Curtis
 */
public class FileViewProperties extends SftpProperties
{
	private static final long	serialVersionUID	= 7749183641900838472L;

	/** The key for data buffer size. */
	public static final String	INTERNAL_BUFFER_SIZE= "internal.buffer.size";
	/** The key for data buffer size. */
	public static final String	USE_BUFFER_THREAD= "use.buffer.thread";
	
	//Default values for properties
	private static final int		DEFAULT_INTERNAL_BUFFER_SIZE= 0;
	private static final boolean	DEFAULT_USE_BUFFER_THREAD= false;
	
	/**
	 * {@inheritDoc}
	 */
	public int getInternalBufferSize()
	{
		int buffer=getInt(INTERNAL_BUFFER_SIZE, DEFAULT_INTERNAL_BUFFER_SIZE);
		if(buffer<=0)
			return 0;
		if(buffer<AbstractChannel.DEFAULT_PACKET_SIZE)
			return 0;
		return buffer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setInternalBufferSize(int size)
	{
		setInt(INTERNAL_BUFFER_SIZE,size);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean useBufferThread()
	{
		return getBoolean(USE_BUFFER_THREAD, DEFAULT_USE_BUFFER_THREAD);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setUseBufferThread(boolean use_buffer_thread)
	{
		setBoolean(USE_BUFFER_THREAD,use_buffer_thread);
	}
	
}
