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

import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.parser.FtpCmdParser;

/**
 * Interface of FTP Commands.
 * 
 * @author Lars Behnke
 */
public interface FtpCmd extends Cloneable {

    /**
     * Sets the command token.
     * 
     * @param token The command token (e.g. STOR, RETR, ...)
     */
    void setToken(String token);

    /**
     * Returns the command token.
     * 
     * @return The command token (e.g. STOR, RETR, ...)
     */
    String getToken();

    /**
     * Passes the optional command argument.
     * 
     * @param args The arguments as text
     */
    void setArguments(String args);

    /**
     * Passes the FTP context shared by all command instances of a user FTP session.
     * 
     * @param ctx The FTP context.
     */
    void setCtx(FtpSessionContext ctx);

    /**
     * Executes the command.
     * 
     * @throws FtpCmdException Thrown on any exception occuring while the command is executed.
     */
    void execute() throws FtpCmdException;

    /**
     * Handles any request on the control connection that was sent while the current command is
     * executed. For instance, while storing or retrieving the client may issue an abort or status
     * command.
     * 
     * @param req The request that has to be handled.
     * @return True is request was handled, otherwise false.
     */
    boolean handleAsyncCmd(String req);

    /**
     * Returns a short help text for the command.
     * 
     * @return The help text.
     */
    String getHelp();

    /**
     * Returns whether authentication is needed or not.
     * 
     * @return True, if the command requires authentication.
     */
    boolean isAuthenticationRequired();

    /**
     * Getter method for the java bean <code>parser</code>.
     * 
     * @return Returns the value of the java bean <code>parser</code>.
     */
    FtpCmdParser getParser();

    /**
     * Setter method for the java bean <code>parser</code>.
     * 
     * @param parser The value of parser to set.
     */
    void setParser(FtpCmdParser parser);
    boolean isExtension();
}
