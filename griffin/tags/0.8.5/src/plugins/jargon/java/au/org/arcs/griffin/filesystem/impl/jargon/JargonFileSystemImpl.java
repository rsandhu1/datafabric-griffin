package au.org.arcs.griffin.filesystem.impl.jargon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.filesystem.FileSystemConnection;

import edu.sdsc.grid.io.irods.IRODSFile;

/**
 * an implementation for jargon
 * @author Shunde Zhang
 *
 */

public class JargonFileSystemImpl implements FileSystem {
	
	private static Log log = LogFactory.getLog(JargonFileSystemImpl.class);
	private String serverName;
	private int serverPort;
	private String serverType;
	private String mapFile;
	private Map<String,String> mapping;
	private String defaultResource;
	private UpdateThread updateThread;
	private int updateInterval;
	
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

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
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

	public FileSystemConnection createFileSystemConnection(
			GSSCredential credential) throws FtpConfigException, IOException{
		if (mapping==null){
			try {
				FileSystemConnection connection = new JargonFileSystemConnectionImpl(serverName, serverPort, serverType, credential, defaultResource);
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
				FileSystemConnection connection = new JargonFileSystemConnectionImpl(serverName, serverPort, serverType, user.split("@")[0], user.split("@")[1], credential, defaultResource);
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
		return String.valueOf(IRODSFile.PATH_SEPARATOR_CHAR);
	}

	public void exit() {
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


}
