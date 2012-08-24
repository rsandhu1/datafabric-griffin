package au.org.arcs.griffin.session.impl;

import java.util.ResourceBundle;

import au.org.arcs.griffin.common.FtpEventListener;
import au.org.arcs.griffin.common.FtpServerOptions;
import au.org.arcs.griffin.filesystem.FileSystem;

public class GridFTPConductorSessionContextImpl extends FtpSessionContextImpl{
	
	private static String ATTRIBUTE_REMOTE_GRIDFTP_CONTROL_CHANNELS="remoteGridFTPControlChannels";

	public GridFTPConductorSessionContextImpl(FtpServerOptions options,
			FileSystem fileSystem, ResourceBundle resourceBundle,
			FtpEventListener listener) {
		super(options, fileSystem, resourceBundle, listener);
	}

}
