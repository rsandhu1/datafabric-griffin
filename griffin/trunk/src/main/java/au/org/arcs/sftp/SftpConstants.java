package au.org.arcs.sftp;

import org.apache.sshd.common.channel.AbstractChannel;

/**
 * General constants of the application.
 * 
 * @author John Curtis
 */
public interface SftpConstants
{
	//The Packet and Window is stored in the ChannelPipedInputStream for handling data flow
	public static final int DEFAULT_PACKET_SIZE=AbstractChannel.DEFAULT_PACKET_SIZE;//0x8000	- 32k
	public static final int DEFAULT_WINDOW_SIZE=AbstractChannel.DEFAULT_WINDOW_SIZE;//0x200000	- 2Mb
	
	public static final int	MIN_PACKET_SIZE = 0x1000;	//4k generally all clients start with this for IO
	public static final int	MAX_PACKET_SIZE = AbstractChannel.DEFAULT_PACKET_SIZE;
	
	//SFTP packet/window buffering constants
	public static final int	MIN_FILE_TRANSFER_SIZE = 0x8000;	//below 32k why bother!
	public static final int	MAX_FILE_TRANSFER_SIZE = 0x200000;	//2Mb upper end

	//Internal file buffering
	public static final int	MIN_INTERNAL_FILE_BUFFER = MAX_PACKET_SIZE;	//Must be larger than largest Packet!
	public static final int	MAX_INTERNAL_FILE_BUFFER = 0x800000;		//8Mb upper end(?)
	public static final int	MIN_BUFFERING_FILE_SIZE = MIN_FILE_TRANSFER_SIZE;
	
	//Thread constants
	public static final int		DEFAULT_IOACTION_TIMEOUT_MS	= 60000;	//60 seconds max wait for data
	public static final int		DEFAULT_IOACTION_SLEEP_MS	= 20;		//20ms when no data to read/write
}
