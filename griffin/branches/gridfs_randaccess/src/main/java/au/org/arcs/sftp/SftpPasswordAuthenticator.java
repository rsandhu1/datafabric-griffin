/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package au.org.arcs.sftp;

import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.sftp.utils.SftpLog;

/**
 * Implements <code>PasswordAuthenticator</code> to allow the Griffin FileSystem to authenticate the client username/password
 * 
 * @author John Curtis
 */
public class SftpPasswordAuthenticator implements PasswordAuthenticator
{
	private final Logger		log	= LoggerFactory.getLogger(getClass());

	private SftpServerDetails	server_details;

	public SftpPasswordAuthenticator(SftpServerDetails serverDetails)
	{
		super();
		server_details = serverDetails;
	}

	public boolean authenticate(String username, String password, ServerSession server_session)
	{
		FileSystemConnection fileSystemConnection = null;
		try
		{
			//Grab our session data
			SftpServerSession sftp_server_session = (SftpServerSession) server_session;
			if (sftp_server_session == null)
				throw new IOException(server_details.getAppTitle()+" session is invalid");
			
			String res_label=server_details.getOptions().getResources();
			if (res_label == null)
				throw new IOException(server_details.getAppTitle()+" resource label not set in config file");
			ResourceBundle res_bundle=ResourceBundle.getBundle(res_label);
			if (res_bundle == null)
				throw new IOException(server_details.getAppTitle()+" resource bundle not loaded");
			log.debug("username:"+username+" is logging in...");
			fileSystemConnection = server_details.getFileSystem().createFileSystemConnection(username, password);
			if (fileSystemConnection == null)
				return false;
			SftpSessionContext ctx = server_details.createSftpContext(username, fileSystemConnection, res_bundle);
			if (ctx == null)
			{
				fileSystemConnection.close();
				return false;
			}
			sftp_server_session.setSftpSessionContext(ctx);
		}
		catch (Exception e)
		{
			SftpLog.logError(log,e);
			return false;
		}
		return true;
	}
}
