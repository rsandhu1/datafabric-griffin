package au.org.arcs.griffin.filesystem.impl.jargon;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.GetParams;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.irods.jargon.core.connection.GSIIRODSAccount;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.connection.IRODSAccount.AuthScheme;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
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
    private String myProxyServerHost;
    private int myProxyServerPort;
	
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

	public String getMyProxyServerHost() {
		return myProxyServerHost;
	}

	public void setMyProxyServerHost(String myProxyServerHost) {
		this.myProxyServerHost = myProxyServerHost;
	}

	public int getMyProxyServerPort() {
		return myProxyServerPort;
	}

	public void setMyProxyServerPort(int myProxyServerPort) {
		this.myProxyServerPort = myProxyServerPort;
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
			String authType = defaultAuthType;
			if (username.indexOf('\\')>-1){
				authType=username.substring(0,username.indexOf('\\'));
				username=username.substring(username.indexOf('\\')+1);
			} else if (username.indexOf('/')>-1) {
				authType=username.substring(0,username.indexOf('/'));
				username=username.substring(username.indexOf('/')+1);
			}
			log.debug("authType:"+authType);
			if (authType.equalsIgnoreCase("myproxy")) {
		        try{
		            MyProxy mp = new MyProxy(myProxyServerHost, myProxyServerPort);
		            GetParams getRequest = new GetParams();
		            getRequest.setCredentialName(null);
		            getRequest.setLifetime(3600);
//		            getRequest.setLifetime(DavisConfig.GSSCREDENTIALLIFETIME);
		            getRequest.setPassphrase(password);
		            getRequest.setUserName(username);
		            GSSCredential gssCredential = mp.get(null,getRequest);
		            if (gssCredential == null) {
		            	log.debug("can't get gssCredential from myproxy: "+ myProxyServerHost);
		            	throw new IOException("can't get gssCredential from myproxy");
		            }
		            try {
						log.debug("gssCredential: "+ gssCredential.getName().toString());
					} catch (GSSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					FileSystemConnection connection = new JargonFileSystemConnectionImpl(this, serverName, serverPort, gssCredential, defaultResource);
					return connection;
		        }
		        catch(MyProxyException e){
		        	log.error("Caught MyProxy exception: ",e);
		        	throw new IOException(e.getMessage());
		        }
		        catch(Exception e){
		        	log.error("Caught exception during myproxy login: ",e);
		        	throw new IOException(e.getMessage());
		        }

			}else{

				FileSystemConnection connection = new JargonFileSystemConnectionImpl(this, serverName, serverPort, authType, username, password, zoneName, defaultResource);
				return connection;
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			throw new FtpConfigException(e.getMessage());
		}
	}

//	@Override
//	public FileSystemConnection createFileSystemConnectionWithPublicKey(
//			String username, String sshKeyType, String base64KeyString)
//			throws FtpConfigException, IOException {
//		// TODO Auto-generated method stub
//		if (adminCertFile==null||adminKeyFile==null) throw new FtpConfigException("admin key/cert file is not configured");
//		GlobusCredential adminCred;
//		try {
//			adminCred = new GlobusCredential(adminCertFile, adminKeyFile);
//	        GSSCredential gssCredential = new GlobusGSSCredentialImpl(adminCred, GSSCredential.INITIATE_AND_ACCEPT);
//        	IRODSAccount adminAccount=IRODSAccount.instance(serverName,serverPort,gssCredential);
//        	adminAccount.setAuthenticationScheme(AuthScheme.GSI);
////	        	adminAccount.setZone(config.getInitParameter("zone-name", null));
////		        adminAccount.setUserName(config.getInitParameter("adminUsername", "rods"));
//	        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
//	        UserAO userAO=irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(adminAccount);
////		        password=getRandomPassword(12);
//	        
////		        createUser(irodsFileSystem,commonName,String.valueOf(password),sharedToken);
//	        
//	        StringBuilder sb = new StringBuilder();
//			sb.append(RodsGenQueryEnum.COL_USER_DN.getName());
//			sb.append(" LIKE '");
//			sb.append(sshKeyType);
//			sb.append(" ");
//			sb.append(base64KeyString);
//			sb.append(" %' and ");
//			sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
//			sb.append(" LIKE '");
//			sb.append(username);
//			sb.append("'");
//			List<User> users = userAO.findWhere(sb.toString());
//			if (users.size()>0) {
//				username=users.get(0).getName();
//				log.debug("found matched user:"+username);
//		        String password=userAO.getTemporaryPasswordForASpecifiedUser(username);
//		        log.debug("got temp password:"+password);
//		        irodsFileSystem.closeAndEatExceptions(adminAccount);
//				FileSystemConnection connection = new JargonFileSystemConnectionImpl(this, serverName, serverPort, "irods", username, password, zoneName, defaultResource);
//				return connection;
//			}
//		} catch (GlobusCredentialException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (GSSException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NullPointerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JargonException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return null;
//	}

	public FileSystemConnection createFileSystemConnectionWithPublicKey(
			String username, String sshKeyType, String base64KeyString)
			throws FtpConfigException, IOException {
		// TODO Auto-generated method stub
		log.debug("userhome:"+System.getProperty("user.home"));
		File irodsEnvFile=new File(System.getProperty("user.home")+File.separator+".irods"+File.separator+".irodsEnv");
		File irodsAFile=new File(System.getProperty("user.home")+File.separator+".irods"+File.separator+".irodsA");
		log.debug("irodsEnvFile:"+irodsEnvFile);
		if (!irodsEnvFile.exists()||!irodsAFile.exists()) throw new FtpConfigException("cannot find admin user config in .irods");
		try {
        	IRODSAccount adminAccount=readUserInfo(irodsEnvFile,irodsAFile);
	        IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
	        UserAO userAO=irodsFileSystem.getIRODSAccessObjectFactory().getUserAO(adminAccount);
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
				log.debug("found matched user:"+username);
		        String password=userAO.getTemporaryPasswordForASpecifiedUser(username);
		        log.debug("got temp password:"+password);
		        irodsFileSystem.closeAndEatExceptions(adminAccount);
				FileSystemConnection connection = new JargonFileSystemConnectionImpl(this, serverName, serverPort, "irods", username, password, zoneName, defaultResource);
				return connection;
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	private IRODSAccount readUserInfo(File envFile, File aFile) throws IOException, FtpConfigException {
		DataInputStream in = null;
		try{
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream(envFile);
			  // Get the object of DataInputStream
			  in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  //Read File Line By Line
			  String username=null;
			  while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				  if (strLine.trim().startsWith("irodsUserName")) {
					  username=strLine.substring(strLine.indexOf(" ")+1).replaceAll("'", "").replaceAll("\"", "").trim();
				  }
			  }
			  String aString=readAuth(aFile);
			  log.debug("aString:"+aString);
			  String password=obfiDecode(aString.toCharArray(),  ((int)aFile.lastModified() & 0xffff));
			  log.debug("username:"+username+";pass:"); //+password);
			  if (username==null||password==null) throw new FtpConfigException("cannot find username or password of the admin user");
			  return new IRODSAccount(serverName, serverPort,
						username, password,
	                      "/" + zoneName
	                      + "/home/" + username,
	                      zoneName, "");
		}catch (IOException e){//Catch exception if any
			  e.printStackTrace();
			  throw new IOException(e.getMessage());
		}finally{
			  try {
				  //Close the input stream
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
    private String readAuth(File authFile) throws IOException  {
    	int index = 0;
    	
    	InputStream authReader = null;
		try {
			authReader = new BufferedInputStream(new FileInputStream(authFile));
	    	byte authContents[] = new byte[(int) authFile.length()];
	    	authReader.read(authContents);

	    	String auth = new String(authContents);

	    	StringTokenizer authTokens = new StringTokenizer(auth, System
	            .getProperty("line.separator")
	            + "\n");
			String token;
			while (authTokens.hasMoreTokens()) {
			    token = authTokens.nextToken();
			
			    if (token.startsWith("#")) {
			            // ignore comments
			    } else {
			            index = token.indexOf(System.getProperty("line.separator"))
			                            + token.indexOf("\n") + 1;
			
			            if (index >= 0)
			                    auth = token.substring(0, index);
			            else
			                    auth = token;
			    }
			}
			return auth;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} finally {
			
			try {
				authReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

    
    private String obfiDecode(char[] in, int timeVal) throws IOException {
       int i;
       long seq;
//       char *p1;

       int rval;
       int wheel_len;
       int[] wheel=new int[26+26+10+15];
       int j, addin, addin_i, kpos, found, nout=0;
       char[] headstring=new char[10];
       int ii, too_short;
       char[] my_out, my_in;
       int not_en, encodedTime;
       int uid=0;
       int out_idx=0;
       /*
       get uid to use as part of the key
      */
       log.debug("in len:"+in.length);
       try {
    	   uid=Integer.parseInt(System.getProperty("userid"));
           log.debug("userid:"+uid);
           uid = uid &0xf5f;  /* keep it fairly small and not exactly uid */
       }catch (Exception e){
    	   throw new IOException(e.getMessage());
       }
//       log.debug("uid:"+uid);
       wheel_len=26+26+10+15;

    /*
     Set up an array of characters that we will transpose.
    */
       j=0;
       for (i=0;i<10;i++) wheel[j++]=(int)'0' + i;
       for (i=0;i<26;i++) wheel[j++]=(int)'A' + i;
       for (i=0;i<26;i++) wheel[j++]=(int)'a' + i;
       for (i=0;i<15;i++) wheel[j++]=(int)'!' + i;

//       for (p1=in,i=0;i<6;i++) {
//          if (*p1++ == '\0') too_short=1;
//       }
       // check if password string is too short
       too_short=in.length>6?0:1;

       kpos=6;
       int p1=0;
       for (i=0;i<kpos;i++,p1++);
//       rval = (int)*p1;
//       log.debug("p1:"+p1+" "+in[p1]);
       rval = in[p1] - 'e';
//       log.debug("rval:"+rval);
       if (rval > 15 || rval < 0 || too_short==1) {  /* invalid key or too short */
//          while ((*out++ = *in++) != '\0')  ;   /* return input string */
//          return AUTH_FILE_NOT_ENCRYPTED;
          return new String(in);
       }

       seq = 0;
       if (rval==0) seq = 0xd768b678;
       if (rval==1) seq = 0xedfdaf56;
       if (rval==2) seq = 0x2420231b;
       if (rval==3) seq = 0x987098d8;
       if (rval==4) seq = 0xc1bdfeee;
       if (rval==5) seq = 0xf572341f;
       if (rval==6) seq = 0x478def3a;
       if (rval==7) seq = 0xa830d343;
       if (rval==8) seq = 0x774dfa2a;
       if (rval==9) seq = 0x6720731e;
       if (rval==10)seq = 0x346fa320;
       if (rval==11)seq = 0x6ffdf43a;
       if (rval==12)seq = 0x7723a320;
       if (rval==13)seq = 0xdf67d02e;
       if (rval==14)seq = 0x86ad240a;
       if (rval==15)seq = 0xe76d342e;

       addin_i=0;
       my_out = headstring;
       my_in = in;
       int my_in_idx=1;   /* skip leading '.' */
       int my_out_idx=0;
       for (ii=0;;) {
          ii++;
          if (ii==6) {
             not_en = 0;
             if (in[0] != '.') {
                not_en = 1;  /* is not 'encrypted' */
             }
//             log.debug("headstring="+new String(headstring));
             if (headstring[0] !='S' - ( (rval&0x7)*2)) {
               not_en=1;
               log.debug("not s");
             }

             //timeVal is the file's modification time
//             if (timeVal==0) {
//                timeVal = obfiTimeval();
//             }
             encodedTime = ((headstring[1] - 'a')<<4) + (headstring[2]-'a') +
                           ((headstring[3] - 'a')<<12) + ((headstring[4]-'a')<<8);

//             if (obfiTimeCheck(encodedTime, timeVal)) not_en=1;

//             log.debug("timeVal="+timeVal+" encodedTime="+encodedTime);

             out_idx = my_out_idx;   /* start outputing for real */
//             log.debug("not_en:"+not_en);
             if (not_en == 1) {
//                 while ((*out++ = *in++) != '\0')  ;   /* return input string */
//                 return AUTH_FILE_NOT_ENCRYPTED;
                 return new String(in);
             }
             my_in_idx++;   /* skip key */
          }
          else {
             found=0;
             addin = (int) ((seq>>addin_i)&0x1f);
//             addin += extra; no extra at this time
              addin += uid;
             addin_i+=3;
             if (addin_i>28)addin_i=0;
//             log.debug("my_in_idx:"+my_in_idx);
             for (i=0;i<wheel_len;i++) {
                if (my_in[my_in_idx] == (char)wheel[i]) {
                   j = i - addin;
//                   log.debug("j="+j);

                   while (j<0) {
                     j+=wheel_len;
                   }
//                   log.debug("j2="+j+"; char is:"+(char)wheel[j]);

                   my_out[my_out_idx++] = (char)wheel[j];
//                   log.debug("my_out:"+new String(my_out));
                   nout++;
                   found = 1;
                   break;
                }
             }
//             log.debug("found="+found);
             if (found==0) {
                if (my_in_idx>=my_in.length-1) { //(*my_in == '\0') {
//                   *my_out++ = '\0';
//                	log.debug("out_idx="+out_idx+" my_out_idx="+my_out_idx);
                   return new String(my_out,out_idx,my_out_idx-out_idx);
                }
//                else {*my_out++=*my_in; nout++;}
                else {my_out[my_out_idx++]=my_in[my_in_idx]; nout++;}
             }
             my_in_idx++;
          }
       }
    }
    boolean obfiTimeCheck(int time1, int time2) {
      int fudge=20;
      int delta;

      log.debug("time1="+time1+" time2="+time2); 

      delta=time1-time2;
      if (delta < 0) delta = 0-delta;
      if (delta < fudge) return false;

      if (time1<65000) time1+=65535;
      if (time2<65000) time2+=65535;

      delta=time1-time2;
      if (delta < 0) delta = 0-delta;
      if (delta < fudge) return false;

      return true;
    }

}
