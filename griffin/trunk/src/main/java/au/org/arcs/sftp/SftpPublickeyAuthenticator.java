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

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import org.apache.mina.util.Base64;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public boolean authenticate(String username, PublicKey key, ServerSession session)
	{
		log.debug("username:"+username+" is logging in with a key...");
//		log.debug("key: "+key.getClass()+" "+key.toString());
		byte[] encoded=key.getEncoded();
		log.debug("encoded: "+encoded.length);
//		if (encoded==null) {
//			log.warn("public key is empty.");
//			return false;
//		}
		byte[] keyString=Base64.encodeBase64(encoded);
		log.debug("keyString: "+keyString);
//		if (key instanceof RSAPublicKey) {
//			byte[] keyString=Base64.decode(encoded);
//			log.debug("keyString: "+new String(keyString));
//		}
		// File f = new File("/Users/" + username + "/.ssh/authorized_keys");
		// return true;
		return false; // We are only using UN/PW entry via Globus
	}
}
