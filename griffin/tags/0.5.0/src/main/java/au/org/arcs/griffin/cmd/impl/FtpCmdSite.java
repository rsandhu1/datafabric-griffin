package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b> SITE PARAMETERS (SITE) </b>
 * <p>
 * This command is used by the server to provide services
 * specific to his system that are essential to file transfer
 * but not sufficiently universal to be included as commands in
 * the protocol.  The nature of these services and the
 * specification of their syntax can be stated in a reply to
 * the HELP SITE command.
 * 
 * @author Shunde Zhang
 *
 */
public class FtpCmdSite extends AbstractFtpCmd {

	public void execute() throws FtpCmdException {
		String arg=getArguments();
		
        if (arg.equals("")) {
            out("500 must supply the site specific command");
            return;
        }

        String args[] = arg.split(" ");

        if (args[0].equalsIgnoreCase("BUFSIZE")) {
            if (args.length != 2) {
                out("500 command must be in the form 'SITE BUFSIZE <number>'");
                return;
            }
            int bufsize;
            try {
                bufsize = Integer.parseInt(args[1]);
            } catch(NumberFormatException ex) {
                out("500 bufsize argument must be integer");
                return;
            }

            if (bufsize < 1) {
                out("500 bufsize must be positive.  Probably large, but at least positive");
                return;
            }

            getCtx().setBufferSize(bufsize);
            out("200 bufsize set to " + bufsize);

        } else if (args[0].equalsIgnoreCase("CHMOD")) {
            if (args.length != 3) {
                out("500 command must be in the form 'SITE CHMOD <octal perms> <file/dir>'");
                return;
            }
            //TODO do something
            out("200 ok");
        }else{
    		StringBuffer msg=new StringBuffer();
    		msg.append("214-The following commands are recognized:\r\n");
    		msg.append("    ALLO    ESTO    RNTO    APPE    DCAU    MODE    SIZE    STRU\r\n");
    		msg.append("    TYPE    DELE    SITE    CWD     ERET    FEAT    LIST    NLST\r\n");
    		msg.append("    MLSD    MLST    PORT    PROT    EPRT    PWD     QUIT    REST\r\n");
    		msg.append("    STAT    SYST    MKD     RMD     CDUP    HELP    NOOP    EPSV\r\n");
    		msg.append("    PASV    TREV    SBUF    MDTM    CKSM    OPTS    PASS    SPAS\r\n");
    		msg.append("    PBSZ    SPOR    RETR    STOR    USER    RNFR    LANG\r\n");
    		msg.append("214 End");
    		out(msg.toString());
        }
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "help on site";
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
