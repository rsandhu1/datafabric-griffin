package au.org.arcs.griffin.location;

import java.util.LinkedList;
import java.util.List;

public class IfCondition {
	private String country;
	private String region;
	private List<String> selects;
	public IfCondition(String country, String region){
		this.country=country;
		this.region=region;
		this.selects=new LinkedList<String>();
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public List<String> getSelects() {
		return selects;
	}
	public void setSelects(List<String> selects) {
		this.selects = selects;
	}
	
}
