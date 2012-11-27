package au.org.arcs.sftp.command;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ResourceBundle;

import au.org.arcs.griffin.common.FtpEventListener;
import au.org.arcs.griffin.common.FtpServerOptions;
import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.griffin.session.impl.FtpSessionContextImpl;
import au.org.arcs.griffin.utils.LoggingReader;
import au.org.arcs.griffin.utils.LoggingWriter;

public class SSHFtpSessionContextImpl extends FtpSessionContextImpl {

	public SSHFtpSessionContextImpl(FtpServerOptions options,
			FileSystem fileSystem, ResourceBundle resourceBundle,
			FtpEventListener listener) {
		super(options, fileSystem, resourceBundle, listener);
		// TODO Auto-generated constructor stub
	}

	public void setInputStream(InputStream is) {
        this.clientCmdReader = new LoggingReader(new InputStreamReader(is));
	}

	public void setOutputStream(OutputStream os) {
        this.clientResponseWriter = new LoggingWriter(new OutputStreamWriter(os),
                true);

	}
	
	public void setFileSystemConnection(FileSystemConnection fileSystemConnection){
		this.fileSystemConnection=fileSystemConnection;
	}
}
