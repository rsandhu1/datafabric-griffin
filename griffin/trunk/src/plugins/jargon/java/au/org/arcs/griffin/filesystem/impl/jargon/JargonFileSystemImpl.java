package au.org.arcs.griffin.filesystem.impl.jargon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.RodsGenQueryEnum;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.filesystem.FileSystemConnection;

/**
 * an implementation for jargon
 * @author Shunde Zhang
 *
 */

public class JargonFileSystemImpl implements FileSystem {
	
	private static Log log = LogFactory.getLog(JargonFileSystemImpl.class);
	private String serverName;
	private int serverPort;
	private String defaultAuthType;
	private String mapFile;
	private Map<String,String> mapping;
	private String defaultResource;
	private UpdateThread updateThread;
	private int updateInterval;
	private IRODSSession iRODSFileSystem;
	private IRODSProtocolManager iRODSProtocolManager;
	private String zoneName;
	private String adminCertFile;
	private String adminKeyFile;
	private String jargonInternalCacheBufferSize;
	
	public String getJargonInternalCacheBufferSize() {
		return jargonInternalCacheBufferSize;
	}

	public void setJargonInternalCacheBufferSize(
			String jargonInternalCacheBufferSize) {
		this.jargonInternalCacheBufferSize = jargonInternalCacheBufferSize;
	}

	public String getAdminCertFile() {
		return adminCertFile;
	}

	public void setAdminCertFile(String adminCertFile) {
		this.adminCertFile = adminCertFile;
	}

	public String getAdminKeyFile() {
		return adminKeyFile;
	}

	public void setAdminKeyFile(String adminKeyFile) {
		this.adminKeyFile = adminKeyFile;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}
	
	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public String getDefaultResource() {
		return defaultResource;
	}

	public void setDefaultResource(String defaultResource) {
		this.defaultResource = defaultResource;
	}

	public String getMapFile() {
		return mapFile;
	}

	public void setMapFile(String mapFile) {
		this.mapFile = mapFile;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getDefaultAuthType() {
		return defaultAuthType;
	}

	public void setDefaultAuthType(String defaultAuthType) {
		this.defaultAuthType = defaultAuthType;
	}

	public FileSystemConnection createFileSystemConnection(
			GSSCredential credential) throws FtpConfigException, IOException{
		if (mapping==null){
			try {
				FileSystemConnection connection = new JargonFileSystemConnectionImpl(this, serverName, serverPort, credential, defaultResource);
				return connection;
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				throw new FtpConfigException(e.getMessage());
			}
		}else{
			try {
				String user=mapping.get(credential.getName().toString());
				if (user==null) throw new FtpConfigException("User DN \""+credential.getName().toString()+"\" is not found in mapfile.");
				if (user.indexOf("@")<0||user.split("@").length!=2) throw new FtpConfigException("Username in mapfile should be in form username@zone_name.");
				FileSystemConnection connection = new JargonFileSystemConnectionImpl(this, serverName, serverPort, user.split("@")[0], user.split("@")[1], credential, defaultResource);
				return connection;
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				throw new FtpConfigException(e.getMessage());
			} catch (GSSException e) {
				// TODO Auto-generated catch block
				throw new FtpConfigException(e.getMessage());
			}
		}
	}

	public void init() throws IOException{
		try {
			iRODSProtocolManager=IRODSSimpleProtocolManager.instance();
			iRODSProtocolManager.initialize();
			iRODSFileSystem=new IRODSSession(iRODSProtocolManager);
			if (jargonInternalCacheBufferSize!=null) {
				try {
					SettableJargonProperties overrideJargonProperties = new SettableJargonProperties();
					overrideJargonProperties.setInternalCacheBufferSize(Integer.parseInt(jargonInternalCacheBufferSize));
					iRODSFileSystem.setJargonProperties(overrideJargonProperties);
				}catch (Exception e2){
					e2.printStackTrace();
				}
			}
		} catch (JargonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new IOException(e1.getMessage());
		}
		log.debug("JargonFileSystemImpl is checking map file in "+System.getProperty(FtpConstants.GRIFFIN_HOME));
		if (getMapFile()!=null){
			mapping=new HashMap<String,String>();
			if (updateInterval==0) updateInterval=10;
			readMapFile();
			updateThread=new UpdateThread();
			updateThread.running=true;
			updateThread.start();
		}
	}
	
	class UpdateThread extends Thread{
		public boolean running;
		public void run(){
			while (running){
				try {
					Thread.sleep(updateInterval*60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.error("update thread exit because it is interrupted.");
					break;
				}
				try {
					readMapFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.error("update thread exit because of error.");
					break;
				}
			}
		}
	}

	public String getSeparator() {
		// TODO Auto-generated method stub
		return IRODSFile.PATH_SEPARATOR;
	}

	public void exit() {
		try {
			iRODSFileSystem.closeSession();
		} catch (JargonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			iRODSProtocolManager.destroy();
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("stopping update thread...");
		updateThread.running=false;
		updateThread.interrupt();
	}
	
	protected void readMapFile() throws IOException{
		mapping.clear();
		String path=System.getProperty(FtpConstants.GRIFFIN_HOME)+File.separator+getMapFile();
		BufferedReader reader=null;
		if (new File(path).exists()){
			try {
				reader=new BufferedReader(new FileReader(path));
		        String line = null; //not declared within while loop
		        /*
		        * readLine is a bit quirky :
		        * it returns the content of a line MINUS the newline.
		        * it returns null only for the END of the stream.
		        * it returns an empty String if two newlines appear in a row.
		        */
		       while (( line = reader.readLine()) != null){
		    	   log.debug("read "+line);
		          if (line.startsWith("\"")){
		        	  int n=line.indexOf("\"",1);
		        	  if (n>-1){
		        		  String dn=line.substring(1,n);
		        		  String user=line.substring(n+1).trim();
		        		  if (user.indexOf("@")>-1&&user.split("@").length==2){
				        	   log.debug("found mapping: "+user+" -> "+dn);
				        	   mapping.put(dn, user);
		        		  }
		        	  }
		          }
		       }
		    } catch (IOException e) {
		    	log.error("Error when reading mapfile.");
				// TODO Auto-generated catch block
				throw e;
			}
		    finally {
		    	try {
					if (reader!=null) reader.close();
				} catch (IOException e) {
			    	log.error("Error when closing mapfile.");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}
	}

	public IRODSSession getIRODSFileSystem() {
		return iRODSFileSystem;
	}

	@Override
	public FileSystemConnection createFileSystemConnection(String username,
			String password) throws FtpConfigException, IOException {
		// TODO Auto-generated method stub
		try {
			if (defaultAuthType==null) defaultAuthType="irods";
			FileSystemConnection connection = new JargonFileSystemConnectionImpl(this, serverName, serverPort, defaultAuthType, username, password, zoneName, defaultResource);
			return connection;
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			throw new FtpConfigException(e.getMessage());
		}
	}

	@Override
	public FileSystemConnection createFileSystemConnectionWithPublicKey(
			String username, String sshKeyType, String base64KeyString)
			throws FtpConfigException, IOException {
		// TODO Auto-generated method stub
		if (adminCertFile==null||adminKeyFile==null) throw new FtpConfigException("admin key/cert file is not configured");
		GlobusCredential adminCred;
		try {
			adminCred = new GlobusCredential(adminCertFile, adminKeyFile);
	        GSSCredential gssCredential = new GlobusGSSCredentialImpl(adminCred, GSSCredential.INITIATE_AND_ACCEPT);
        	IRODSAccount adminAccount=IRODSAccount.instance(serverName,serverPort,gssCredential);
//	        	adminAccount.setZone(config.getInitParameter("zone-name", null));
//		        adminAccount.setUserName(config.getInitParameter("adminUsername", "rods"));
	        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
	        UserAO userAO=irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(adminAccount);
//		        password=getRandomPassword(12);
	        
//		        createUser(irodsFileSystem,commonName,String.valueOf(password),sharedToken);
	        
	        StringBuilder sb = new StringBuilder();
			sb.append(RodsGenQueryEnum.COL_USER_DN.getName());
			sb.append(" LIKE '");
			sb.append(sshKeyType);
			sb.append(" ");
			sb.append(base64KeyString);
			sb.append(" %' and ");
			sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
			sb.append(" LIKE '");
			sb.append(username);
			sb.append("'");
			List<User> users = userAO.findWhere(sb.toString());
			if (users.size()>0) {
				username=users.get(0).getName();
		        String password=userAO.getTemporaryPasswordForASpecifiedUser(username);
				FileSystemConnection connection = new JargonFileSystemConnectionImpl(this, serverName, serverPort, "irods", username, password, zoneName, defaultResource);
				return connection;
			}
		} catch (GlobusCredentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GSSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
