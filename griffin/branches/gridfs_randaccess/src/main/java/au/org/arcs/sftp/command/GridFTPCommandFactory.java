package au.org.arcs.sftp.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.command.UnknownCommand;

import org.springframework.context.ApplicationContext;

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
