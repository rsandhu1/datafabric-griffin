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

package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>OPTS Command</b>
 * <p>
 * The OPTS (options) command allows a user-PI to specify the desired behavior of a server-FTP
 * process when another FTP command (the target command) is later issued. The exact behavior, and
 * syntax, will vary with the target command indicated, and will be specified with the definition of
 * that command. Where no OPTS behavior is defined for a particular command there are no options
 * available for that command.
 * <p>
 * Request Syntax:
 * 
 * <pre>
 *            opts             = opts-cmd SP command-name
 *            [ SP command-options ] CRLF
 *            opts-cmd         = &quot;opts&quot;
 *            command-name     = &lt;any FTP command which allows option setting&gt;
 *            command-options  = &lt;format specified by individual FTP command&gt;
 * </pre>
 * 
 * Response Syntax:
 * 
 * <pre>
 *            opts-response    = opts-good / opts-bad
 *            opts-good        = &quot;200&quot; SP response-message CRLF
 *            opts-bad         = &quot;451&quot; SP response-message CRLF /
 *            &quot;501&quot; SP response-message CRLF
 *            response-message = *TCHAR
 * </pre>
 * 
 * An "opts-good" response (200 reply) MUST be sent when the command- name specified in the OPTS
 * command is recognized, and the command- options, if any, are recognized, and appropriate. An
 * "opts-bad" response is sent in other cases. A 501 reply is appropriate for any permanent error.
 * That is, for any case where simply repeating the command at some later time, without other
 * changes of state, will also be an error. A 451 reply should be sent where some temporary
 * condition at the server, not related to the state of communications between user and server,
 * prevents the command being accepted when issued, but where if repeated at some later time, a
 * changed environment for the server-FTP process may permit the command to succeed. If the OPTS
 * command itself is not recognized, a 500 or 502 reply will, of course, result.
 * <p>
 * The OPTS command MUST be implemented whenever the FEAT command is implemented. Because of that,
 * there is no indication in the list of features returned by FEAT to indicate that the OPTS command
 * itself is supported. Neither the FEAT command, nor the OPTS command, have any optional
 * functionality, thus there are no "OPTS FEAT" or "OPTS OPTS" commands.
 * <p>
 * Security Considerations: No significant new security issues, not already present in the FTP
 * protocol, are believed to have been created by this extension. However, this extension does
 * provide a mechanism by which users can determine the capabilities of an FTP server, and from
 * which additional information may be able to be deduced. While the same basic information could be
 * obtained by probing the server for the various commands, if the FEAT command were not provided,
 * that method may reveal an attacker by logging the attempts to access various extension commands.
 * This possibility is not considered a serious enough threat to be worthy of any remedial action.
 * The security of any additional features that might be reported by the FEAT command, and
 * manipulated by the OPTS command, should be addressed where those features are defined.
 * <p>
 * <i>[Excerpt from RFC-2389, Hethmon and Elz]</i>
 * </p>
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdOpts extends AbstractFtpCmd {

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        String response;
        String[] argParts = getArguments().split("\\s+");
        if (argParts.length == 2 && argParts[0].equalsIgnoreCase("UTF8")) {
            getCtx().setAttribute(ATTR_FORCE_UTF8, Boolean.valueOf(argParts[1].equalsIgnoreCase("ON")));
            response = msg(MSG200);
        } else if (argParts.length == 2 && argParts[0].equalsIgnoreCase("RETR")) {
            String[] st = argParts[1].split("=");
            String real_opt = st[0];
            String real_value= st[1];
            if (!real_opt.equalsIgnoreCase("Parallelism")) {
                out("501 Unrecognized option: " + real_opt + " (" + real_value + ")");
                return;
            }

            st = real_value.split(",|;");
            getCtx().setParallelStart(Integer.parseInt(st[0]));
            getCtx().setParallelMin(Integer.parseInt(st[1]));
            getCtx().setParallelMax(Integer.parseInt(st[2]));

//            if (_maxStreamsPerClient > 0) {
//                _parallelStart = Math.min(_parallelStart, _maxStreamsPerClient);
//                _parallelMin = Math.min(_parallelMin, _maxStreamsPerClient);
//                _parallelMax = Math.min(_parallelMax, _maxStreamsPerClient);
//            }

            response="200 Parallel streams set (" + argParts[1] + ")";

        } else if (argParts.length == 3 && argParts[0].equalsIgnoreCase("STOR")) {
            if (!argParts[1].equalsIgnoreCase("EOF")) {
                out("501 Unrecognized option: " + argParts[1] + " (" + argParts[2] + ")");
                return;
            }
            if (!argParts[2].equals("1")) {
            	getCtx().setConfirmEOFs(true);
                out("200 EOF confirmation is ON");
                return;
            }
            if (!argParts[2].equals("0")) {
            	getCtx().setConfirmEOFs(false);
                out("200 EOF confirmation is OFF");
                return;
            }
            response="501 Unrecognized option value: " + argParts[2];
//        } else if (argParts.length == 2 && argParts[0].equalsIgnoreCase("CKSM")) {
//            if (argParts[1] ==  null) {
//                out("501 CKSM option command requires algorithm type");
//                return;
//            }
//
//            try {
//                if (!argParts[1].equalsIgnoreCase("NONE")) {
//                    _optCheckSumFactory = ChecksumFactory.getFactory(algo);
//                } else {
//                    _optCheckSumFactory = null;
//                }
//                response=("200 OK");
//            } catch (NoSuchAlgorithmException e) {
//            	response=("504 Unsupported checksum type: " + argParts[1]);
//            }
        } else {
            response = msg(MSG451);
        }
        out(response);
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Specifies the desired behavior of the FTP server.";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return true;
    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
