package au.org.arcs.sftp;

/**
 * SFTP protocol specific constants
 * 
 * @author John Curtis
 */
public interface SftpProtocolConstants
{
	/**
	 * Properties key for the maximum of available open handles per session.
	 */
	public static final String	MAX_OPEN_HANDLES_PER_SESSION		= "max-open-handles-per-session";
	
	public static final int		MIN_SIZEOF_PACKET_HEADER			= 5;		//at least 4 byte id/version plus type byte					

	public static final int		LOWER_SFTP_IMPL						= 3;		// Only support v3 for now
	public static final int		HIGHER_SFTP_IMPL					= 3;

	public static final int		SSH_FXP_INIT						= 1;
	public static final int		SSH_FXP_VERSION						= 2;
	public static final int		SSH_FXP_OPEN						= 3;
	public static final int		SSH_FXP_CLOSE						= 4;
	public static final int		SSH_FXP_READ						= 5;
	public static final int		SSH_FXP_WRITE						= 6;
	public static final int		SSH_FXP_LSTAT						= 7;
	public static final int		SSH_FXP_FSTAT						= 8;
	public static final int		SSH_FXP_SETSTAT						= 9;
	public static final int		SSH_FXP_FSETSTAT					= 10;
	public static final int		SSH_FXP_OPENDIR						= 11;
	public static final int		SSH_FXP_READDIR						= 12;
	public static final int		SSH_FXP_REMOVE						= 13;
	public static final int		SSH_FXP_MKDIR						= 14;
	public static final int		SSH_FXP_RMDIR						= 15;
	public static final int		SSH_FXP_REALPATH					= 16;
	public static final int		SSH_FXP_STAT						= 17;
	public static final int		SSH_FXP_RENAME						= 18;
	public static final int		SSH_FXP_READLINK					= 19;
	public static final int		SSH_FXP_SYMLINK						= 20;
	public static final int		SSH_FXP_LINK						= 21;
	public static final int		SSH_FXP_BLOCK						= 22;
	public static final int		SSH_FXP_UNBLOCK						= 23;

	public static final int		SSH_FXP_STATUS						= 101;
	public static final int		SSH_FXP_HANDLE						= 102;
	public static final int		SSH_FXP_DATA						= 103;
	public static final int		SSH_FXP_NAME						= 104;
	public static final int		SSH_FXP_ATTRS						= 105;

	public static final int		SSH_FXP_EXTENDED					= 200;
	public static final int		SSH_FXP_EXTENDED_REPLY				= 201;
}
