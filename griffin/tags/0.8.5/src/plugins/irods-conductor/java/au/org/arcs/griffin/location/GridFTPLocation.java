package au.org.arcs.griffin.location;

import java.util.ArrayList;
import java.util.List;

public class GridFTPLocation {
	private String id;
	private String hostname;
	private int port;
	private List<String> resources;
	public GridFTPLocation(String id, String hostname, int port){
		this.id=id;
		this.hostname=hostname;
		this.port=port;
		this.resources=new ArrayList<String>();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public List<String> getResources() {
		return resources;
	}
	public void setResources(List<String> resources) {
		this.resources = resources;
	}
	public String toString(){
		StringBuffer buffer=new StringBuffer();
		buffer.append(hostname).append(":").append(port);
		return buffer.toString();
	}
}
