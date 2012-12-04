package au.org.arcs.sftp.command;

import java.util.Date;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.command.UnknownCommand;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.context.ApplicationContext;

import au.org.arcs.griffin.common.FtpServerOptions;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.session.FtpSession;
import au.org.arcs.griffin.utils.AbstractAppAwareBean;
import au.org.arcs.sftp.SftpServerSession;

public class GridFTPCommandFactory implements CommandFactory {
	private static Log log = LogFactory.getLog(GridFTPCommandFactory.class);
	
	private ApplicationContext ctx;
	public GridFTPCommandFactory(ApplicationContext ctx){
		this.ctx=ctx;
	}
	@Override
	public Command createCommand(String command) {
		log.debug("command:"+command);
		if (command.indexOf("/etc/grid-security/sshftp")>-1) {
			return (GridFTPCommand) ctx.getBean("gridftpCommand");
		}
		return new UnknownCommand(command);
	}

}
