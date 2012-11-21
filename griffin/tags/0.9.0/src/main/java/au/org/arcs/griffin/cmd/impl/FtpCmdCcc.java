package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b> CLEAR COMMAND CHANNEL </b>
 *    This command does not take an argument.
 *
 *    It is desirable in some environments to use a security mechanism
 *     perform any integrity checking on the subsequent commands.  This
 *    might be used in an environment where IP security is in place,
 *    insuring that the hosts are authenticated and that TCP streams
 *    cannot be tampered, but where user authentication is desired.
 *
 *    If unprotected commands are allowed on any connection, then an
 *    attacker could insert a command on the control stream, and the
 *    server would have no way to know that it was invalid.  In order to
 *    prevent such attacks, once a security data exchange completes
 *    successfully, if the security mechanism supports integrity, then
 *    integrity (via the MIC or ENC command, and 631 or 632 reply) must
 *    be used, until the CCC command is issued to enable non-integrity
 *    protected control channel messages.  The CCC command itself must
 *    be integrity protected.
 *    Once the CCC command completes successfully, if a command is not
 *    protected, then the reply to that command must also not be
 *    protected.  This is to support interoperability with clients which
 *    do not support protection once the CCC command has been issued.
 *    
 * @author Shunde Zhang
 */

public class FtpCmdCcc extends AbstractFtpCmd {

	public void execute() throws FtpCmdException {
        // We should never received this, only through MIC, ENC or CONF,
        // in which case it will be intercepted by secure_command()
		msgOut("533 CCC must be protected");
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "ccc";
	}

	public boolean isAuthenticationRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
