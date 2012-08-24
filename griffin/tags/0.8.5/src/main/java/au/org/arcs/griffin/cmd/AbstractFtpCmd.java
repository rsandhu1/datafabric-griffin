/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */

package au.org.arcs.griffin.cmd;

import java.text.MessageFormat;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.parser.FtpCmdParser;

/**
 * Abstract ancestor of FTP command classes that provides some functionallity shared by different
 * command classes.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public abstract class AbstractFtpCmd implements FtpCmd, FtpConstants {

    private static Log        log = LogFactory.getLog(AbstractFtpCmd.class);

    private String            token;

    private String            arguments;

    private FtpSessionContext ctx;

    private boolean           responded;

    private FtpCmdParser      parser;
    /**
     * Returns a message resource string.
     * 
     * @param msgKey The message key.
     * @param args The arguments.
     * @return The message.
     */
    protected String msg(String msgKey, Object[] args) {
        String msg = msg(msgKey);
        if (args != null) {
            msg = MessageFormat.format(msg, args);
        }
        return msg;
    }

    /**
     * Returns a message resource string.
     * 
     * @param msgKey The message key.
     * @return The message.
     */
    protected String msg(String msgKey) {
        return getCtx().getRes(msgKey);
    }

    /**
     * Returns a message resource string.
     * 
     * @param msgKey The message key.
     * @param arg An single message argument.
     * @return The message.
     */
    protected String msg(String msgKey, String arg) {
        return msg(msgKey, new Object[] {arg});
    }

    /**
     * Writes out the response to a client command.
     * 
     * @param text The response.
     */
    public void out(String text) {
        responded = !text.startsWith("150");
        if (getCtx().getReplyType().equals("clear")) {
            getCtx().getClientResponseWriter().println(text);
            getCtx().getClientResponseWriter().flush();
        } else if (getCtx().getReplyType().equals("mic")) {
            secure_reply(text, "631");
        } else if (getCtx().getReplyType().equals("enc")) {
            secure_reply(text, "632"); // used to be 633
        } else if (getCtx().getReplyType().equals("conf")) {
            secure_reply(text, "632");
        }
    }

    private void secure_reply(String answer, String code) {
        answer = answer+"\r\n";
        byte[] data = answer.getBytes();
        MessageProp prop = new MessageProp(0, false);
        try{
            data = getCtx().getServiceContext().wrap(data, 0, data.length, prop);
        } catch ( GSSException e ) {
        	getCtx().getClientResponseWriter().println("500 Reply encryption error: " + e);
        	getCtx().getClientResponseWriter().flush();
            return;
        }
        log.info("Printing encrypted msg: "+answer);
        Base64 base64=new Base64();
        getCtx().getClientResponseWriter().println(code + " " + new String(base64.encode(data)));
        getCtx().getClientResponseWriter().flush();
	}

	/**
     * Writes the message identified by the passed key to the control stream. If additional
     * arguments are passed they are integrated into the message string.
     * 
     * @param msgKey The message key as defined in the resource file.
     * @param args The optional arguments.
     */
    protected void msgOut(String msgKey, Object[] args) {
        String msg = msg(msgKey, args);
        out(msg);
    }

    /**
     * Convenience method that prints out a message to the control channel..
     * 
     * @param msgKey The key of the message.
     */
    protected void msgOut(String msgKey) {
        msgOut(msgKey, (Object[]) null);
    }

    /**
     * Convenience method that prints out a message to the control channel.
     * 
     * @param msgKey The key of the message.
     * @param argument Text argument.
     */
    protected void msgOut(String msgKey, String argument) {
        msgOut(msgKey, new Object[] {argument});
    }

    /**
     * Returns a path argument.
     * 
     * @return The path
     */
    protected String getPathArg() {
        return getAbsPath(getArguments());
    }

    /**
     * Returns the absolute (virtual) path of the passed rel. path.
     * 
     * @param path The relative path;
     * @return The absolute path
     */
    protected String getAbsPath(String path) {
        String absolutePath;
        String fileSeparator = getCtx().getFileSystem().getSeparator();
        String virtualPath = path.replace("~",
                FilenameUtils.concat(fileSeparator,
                                     getCtx().getFileSystemConnection()
                                             .getHomeDir()));
        
        if (virtualPath.startsWith(fileSeparator)) {
        	virtualPath = FilenameUtils.normalizeNoEndSeparator(virtualPath);
        }else
           	virtualPath = FilenameUtils.concat(getCtx().getRemoteDir(), virtualPath);
        log.debug("path:"+virtualPath);
//        virtualPath = FilenameUtils.normalizeNoEndSeparator(virtualPath);
        if (virtualPath == null || virtualPath.length()==0) {
            virtualPath = fileSeparator;
        }
//        if (virtualPath.startsWith(fileSeparator)) {
//            absolutePath = virtualPath;
//        } else {
//            absolutePath = FilenameUtils.concat(getCtx().getRemoteDir(), virtualPath);
//            absolutePath = FilenameUtils.concat(fileSeparator, absolutePath);
//        }
        return FilenameUtils.separatorsToUnix(virtualPath);
    }

    /**
     * @return Returns the file offset to be used, or null not offset defined.
     */
    protected long getAndResetFileOffset() {
        Long fileOffsetObj = (Long) getCtx().getAttribute(ATTR_FILE_OFFSET);
        getCtx().setAttribute(ATTR_FILE_OFFSET, null);
        long fileOffset = fileOffsetObj == null ? 0 : fileOffsetObj.longValue();
        return fileOffset;
    }

    /**
     * {@inheritDoc}
     */
    public void setArguments(String args) {
        this.arguments = args;

    }

    /**
     * Returns the arguments previously passed to the instance.
     * 
     * @return the command line arguments.
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Getter method for the java bean <code>ctx</code>.
     * 
     * @return Returns the value of the java bean <code>ctx</code>.
     */
    public FtpSessionContext getCtx() {
        return ctx;
    }

    /**
     * Setter method for the java bean <code>ctx</code>.
     * 
     * @param ctx The value of ctx to set.
     */
    public void setCtx(FtpSessionContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Gets the permission on the current path.
     * 
     * @return The permission constant.
     */
    public int getPermission() {
        return getCtx().getPermission(getCtx().getRemoteDir());
    }

    /**
     * Returns the command token.
     * 
     * @return The command token.
     */
    public String getToken() {
        return token;
    }

    /**
     * {@inheritDoc}
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return True if response has been sent.
     */
    public boolean isResponded() {
        return responded;
    }

    /**
     * {@inheritDoc}
     */
    public boolean handleAsyncCmd(String req) {
        return false;
    }
    
    public FtpCmdParser getParser() {
        return parser;
    }

    public void setParser(FtpCmdParser parser) {
        this.parser = parser;
    }

}
