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
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ResourceBundle;

import org.apache.mina.util.Base64;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.sftp.utils.SftpLog;
import au.org.arcs.sftp.utils.TypesWriter;

/**
* Implements <code>PublickeyAuthenticator</code> to skip Public Key Authentication check. Always fails!
* 
 * @author John Curtis
 */
public class SftpPublickeyAuthenticator implements PublickeyAuthenticator
{
	private final Logger		log	= LoggerFactory.getLogger(getClass());

	private SftpServerDetails	server_details;

	public SftpPublickeyAuthenticator(SftpServerDetails serverDetails)
	{
		super();
		this.server_details = serverDetails;
	}
	
	public SftpServerDetails getDetails()
	{
		return server_details;
	}
	
	// The authenticator is invoked once for each key the client presents
	// during authentication. Your implementation needs to see if the
	// supplied key is on the list of authorized keys for the given username,
	// if it is you return true, if it is not, you return false.
	//
	// When your implementation returns true, MINA SSHD will verify that the
	// client actually has the private half of the key pair. If it does,
	// your authenticator will be called a second time with that key. If you
	// still return true, the client will be authenticated.

	public boolean authenticate(String username, PublicKey key, ServerSession server_session)
	{
		log.debug("username:"+username+" is logging in with a key...");
		log.debug("format: "+key.getFormat());
//		log.debug("key: "+key.getClass()+" "+key.toString());
		byte[] encoded=key.getEncoded();
		log.debug("encoded: "+encoded.length);
//		if (encoded==null) {
//			log.warn("public key is empty.");
//			return false;
//		}
		TypesWriter tw = new TypesWriter();
		String sshKeyType = null;
		if (key instanceof RSAPublicKey) {
			 
	 		tw.writeString("ssh-rsa");
	 		tw.writeMPInt(((RSAPublicKey)key).getPublicExponent());
	 		tw.writeMPInt(((RSAPublicKey)key).getModulus());
	 		sshKeyType = "ssh-rsa";
		} else if (key instanceof DSAPublicKey) {
			 
	 		tw.writeString("ssh-dss");
	 		tw.writeMPInt(((DSAPublicKey)key).getParams().getP());
	 		tw.writeMPInt(((DSAPublicKey)key).getParams().getQ());
	 		tw.writeMPInt(((DSAPublicKey)key).getParams().getG());
	 		tw.writeMPInt(((DSAPublicKey)key).getY());
	 		sshKeyType = "ssh-dss";
		}
		byte[] sshkey=tw.getBytes();
		log.debug("keyString: "+sshKeyType+" "+getBase64WithoutNewline(sshkey));
		// File f = new File("/Users/" + username + "/.ssh/authorized_keys");
		// return true;
		SftpServerSession sftp_server_session = (SftpServerSession) server_session;
		log.debug("ctx: "+sftp_server_session.getSftpSessionContext());
		try
		{

			if (sftp_server_session == null)
				throw new IOException(server_details.getAppTitle()+" session is invalid");
			
			String res_label=server_details.getOptions().getResources();
			if (res_label == null)
				throw new IOException(server_details.getAppTitle()+" resource label not set in config file");
			ResourceBundle res_bundle=ResourceBundle.getBundle(res_label);
			if (res_bundle == null)
				throw new IOException(server_details.getAppTitle()+" resource bundle not loaded");
			FileSystemConnection fileSystemConnection = null;
			fileSystemConnection = server_details.getFileSystem().createFileSystemConnectionWithPublicKey(username, sshKeyType, getBase64WithoutNewline(sshkey));
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

		return true; // We are only using UN/PW entry via Globus
	}
	
	  private String getBase64WithoutNewline(byte[] b) {
		    String b64Str = new String(Base64.encodeBase64(b));
		    String result = b64Str.replaceAll("[\r\n]", "");
		    result = result.replaceAll("[\n]", "");
		    return result;
		  }
}
