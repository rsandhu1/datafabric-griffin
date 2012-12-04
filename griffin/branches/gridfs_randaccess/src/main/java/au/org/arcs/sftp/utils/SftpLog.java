package au.org.arcs.sftp.utils;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import au.org.arcs.sftp.SftpSessionContext;

/**
 * Log support class
 */
public class SftpLog extends Logger
{
	private SftpSessionContext	ctx;
	private Level default_priority=Level.INFO;
	
	protected SftpLog(String name)
	{
		super(name);
	}


	/**
	 * Returns a message resource string.
	 * 
	 * @param msgKey
	 *            The message key.
	 * @param args
	 *            The arguments.
	 * @return The message.
	 */
	protected String msg(String msgKey, Object[] args)
	{
		String msg = msg(msgKey);
		if (args != null)
		{
			msg = MessageFormat.format(msg, args);
		}
		return msg;
	}

	/**
	 * Returns a message resource string.
	 * 
	 * @param msgKey
	 *            The message key.
	 * @return The message.
	 */
	protected String msg(String msgKey)
	{
		return ctx.getRes(msgKey);
	}

	/**
	 * Returns a message resource string.
	 * 
	 * @param msgKey
	 *            The message key.
	 * @param arg
	 *            An single message argument.
	 * @return The message.
	 */
	protected String msg(String msgKey, String arg)
	{
		return msg(msgKey, new Object[]{ arg });
	}

	/**
	 * Writes the message identified by the passed key to the control stream. If additional arguments are passed they
	 * are integrated into the message string.
	 * 
	 * @param msgKey
	 *            The message key as defined in the resource file.
	 * @param args
	 *            The optional arguments.
	 */
	protected void msgOut(String msgKey, Object[] args)
	{
		String msg = msg(msgKey, args);
		super.log(default_priority,msg);
	}

	/**
	 * Convenience method that prints out a message to the control channel..
	 * 
	 * @param msgKey
	 *            The key of the message.
	 */
	protected void msgOut(String msgKey)
	{
		msgOut(msgKey, (Object[]) null);
	}

	/**
	 * Convenience method that prints out a message
	 * 
	 * @param msgKey
	 *            The key of the message.
	 * @param argument
	 *            Text argument.
	 */
	protected void msgOut(String msgKey, String argument)
	{
		msgOut(msgKey, new Object[]
		{ argument });
	}

	/**
	 * Writes out the log
	 * 
	 * @param text
	 *            The response.
	 */
	public void log(String text)
	{
		super.log(default_priority,text);
	}


	/**
	 * Getter method for the java bean <code>ctx</code>.
	 * 
	 * @return Returns the value of the SftpSessionContext <code>ctx</code>.
	 */
	public SftpSessionContext getCtx()
	{
		return ctx;
	}

	/**
	 * Setter method for the SftpSessionContext <code>ctx</code>.
	 * 
	 * @param ctx
	 *            The value of ctx to set.
	 */
	public void setCtx(SftpSessionContext ctx)
	{
		this.ctx = ctx;
	}
	
	
	
	static public void logError(Log log,Exception e)
	{
		log.error(e.toString());
		e.printStackTrace();
	}
	static public void logError(Log log,String desc,Exception e)
	{
		log.error(desc,e);
		e.printStackTrace();
	}
	static public void logError(org.slf4j.Logger log,Exception e)
	{
		log.error(e.toString());
		e.printStackTrace();
	}
	static public void logError(org.slf4j.Logger log,String desc,Exception e)
	{
		log.error(desc,e);
		e.printStackTrace();
	}
	static public void logDebug(Log log,String desc)
	{
		log.debug(desc);
	}
	static public void logDebug(org.slf4j.Logger log,String desc)
	{
		log.debug(desc);
	}
}
