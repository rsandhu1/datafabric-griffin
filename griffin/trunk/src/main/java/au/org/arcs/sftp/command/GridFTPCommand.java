package au.org.arcs.sftp.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.FileSystemAware;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.arcs.griffin.cmd.FtpCmd;
import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpServerOptions;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.exception.FtpCmdResponseException;
import au.org.arcs.griffin.exception.FtpIllegalCmdException;
import au.org.arcs.griffin.exception.FtpQuitException;
import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.parser.FtpCmdReader;
import au.org.arcs.sftp.SftpServerSession;

public class GridFTPCommand implements Command, Runnable, FtpConstants, SessionAware, FileSystemAware{
	protected static final Logger log = LoggerFactory.getLogger(GridFTPCommand.class);
    protected static final int OK = 0;
    protected static final int WARNING = 1;
    protected static final int ERROR = 2;
    
    private static final int  DEFAULT_IDLE_SECONDS = 60;

    private static final int  COMMAND_TIMEOUT      = 3000;
			
    protected InputStream in;
    protected OutputStream out;
    protected OutputStream err;
    protected ExitCallback callback;
    protected IOException error;
    private String name;
    private FtpSessionContext ftpContext;
    private FtpCmdReader      cmdReader;
    private boolean           terminated;
    private ServerSession	  serverSession;
    private FileSystemView root;
    private FtpServerOptions options;
    private String resources;
    private FileSystem fileSystem;
    
    public GridFTPCommand(String args) {
    	if (args==null||args.length()==0){
    		error = new IOException("GridFTP command is empty.");
    	}else{
        	String[] cmdList=args.split(" ");
        	this.name=cmdList[0];
    	}
    }
    public GridFTPCommand(){
    }
    
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    public void start(Environment env) throws IOException {
        if (error != null) {
            throw error;
        }
        new Thread(this, "GridFTPCommand: " + name).start();
    }
    public void destroy() {
    }

    public void run() {
    	log.debug("serverSession:"+serverSession);
    	log.debug("root:"+root);
		SftpServerSession session=(SftpServerSession)serverSession;
		log.debug("local address: "+serverSession.getIoSession().getLocalAddress());
		log.debug("remote address: "+serverSession.getIoSession().getRemoteAddress());
		FtpSessionContext ctx=new SSHFtpSessionContextImpl(options, fileSystem, ResourceBundle
	            .getBundle(getResources()), null);
        ctx.setCreationTime(new Date());
        // set default reply type to be clear
        ctx.setReplyType("clear");
    	((SSHFtpSessionContextImpl)ctx).setFileSystemConnection(session.getSftpSessionContext().getFileSystemConnection());
    	((SSHFtpSessionContextImpl)ctx).setInputStream(in);
    	((SSHFtpSessionContextImpl)ctx).setOutputStream(out);
    	ctx.setControlChannelMode(1);
        ctx.setBufferSize(options.getBufferSize());
        ctx.setNetworkStack(NETWORK_STACK_TCP);
        ctx.setDCAU(DCAU_NONE);
    	this.ftpContext=ctx;
    	
        int exitValue = OK;
        String exitMessage = null;
		try {
			ackLogin();
            getCmdReader().setCtx(getFtpContext());
            getCmdReader().start();
            long startWaiting = System.currentTimeMillis();
            while (!isTerminated()) {
                FtpCmd cmd = null;
                try {
                    cmd = getCmdReader().waitForNextCommand(COMMAND_TIMEOUT);
                    terminated = !executeCmd(cmd);
                    startWaiting = System.currentTimeMillis();
                } catch (FtpIllegalCmdException e) {
                    String msg = formatResString(MSG500_CMD, new Object[] {e.getCmdLine()});
                    out(msg);
                } catch (SocketTimeoutException e) {
                    long maxIdleSecs = getFtpContext().getOptions().getInt(OPT_MAX_IDLE_SECONDS,
                        DEFAULT_IDLE_SECONDS);
                    if (System.currentTimeMillis() - startWaiting > (maxIdleSecs * MILLI)) {
                        out(formatResString(MSG421, new Object[0]));
                        log.info("Session timeout after " + maxIdleSecs
                                 + " seconds for user " + getFtpContext().getUser());
                        terminated = true;
                    }
                }
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            log.error("Session closed because of error while executing command", e);
		} finally {
            terminated = true;
            getCmdReader().abort();
            if (callback != null) {
                callback.onExit(exitValue, exitMessage);
            }
        }
		log.debug("GridFTPCommand: " + name+" finished.");
    }
    
    private void ackLogin() throws IOException{
		String loginMessage="220 df1-dev.ivec.org GridFTP Server 6.5 (gcc64, 1323378368-83) [unknown] ready.\r\n";
		out.write(loginMessage.getBytes());
		out.flush();
    }
    private void out(String msg) {
        getFtpContext().getClientResponseWriter().println(msg);
        getFtpContext().getClientResponseWriter().flush();
    }

    private String formatResString(String resourceKey, Object[] args) {
        String msg = getFtpContext().getRes(resourceKey);
        if (args != null) {
            msg = MessageFormat.format(msg, args);
        }
        return msg;
    }
    private boolean executeCmd(FtpCmd cmd) throws FtpCmdException {
        boolean proceed = true;
        if (cmd != null) {
            synchronized (cmd) {
                if (cmd.isAuthenticationRequired() && !getFtpContext().isAuthenticated()) {
                    String msg = getFtpContext().getRes(MSG530);
                    out(msg);
                } else {
                    try {
                        cmd.setCtx(getFtpContext());
                        cmd.execute();
                    } catch (FtpQuitException e) {
                        proceed = false;
                    } catch (FtpCmdResponseException e) {
                        log.error("Problem executing command: " + e.getMessage());
                        out(e.getMessage());
                    } finally {
                        cmd.notifyAll();
                    }
                }
            }
        }
        return proceed;
    }
   protected String readLine() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (;;) {
            int c = in.read();
            log.debug("read:"+c);
            if (c == '\n') {
                return baos.toString();
            } else if (c == -1) {
                throw new IOException("End of stream");
            } else {
                baos.write(c);
            }
        }
    }
	public FtpCmdReader getCmdReader() {
		return cmdReader;
	}
	public void setCmdReader(FtpCmdReader cmdReader) {
		this.cmdReader = cmdReader;
	}
	public FtpSessionContext getFtpContext() {
		return ftpContext;
	}
	public void setFtpContext(FtpSessionContext ftpContext) {
		this.ftpContext = ftpContext;
	}
    /**
     * Getter method for the java bean <code>terminated</code>.
     * 
     * @return Returns the value of the java bean <code>terminated</code>.
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * @see au.org.arcs.griffin.session.FtpSession#abort()
     */
    public void abort() {
        terminated = true;
    }
	@Override
	public void setSession(ServerSession session) {
		this.serverSession=session;
		
	}
    public void setFileSystemView(FileSystemView view) {
        this.root = view;
    }
	public FtpServerOptions getOptions() {
		return options;
	}
	public void setOptions(FtpServerOptions options) {
		this.options = options;
	}
	public String getResources() {
		return resources;
	}
	public void setResources(String resources) {
		this.resources = resources;
	}
	public FileSystem getFileSystem() {
		return fileSystem;
	}
	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}
}
