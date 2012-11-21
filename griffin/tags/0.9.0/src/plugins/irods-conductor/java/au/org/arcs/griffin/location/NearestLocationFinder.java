package au.org.arcs.griffin.location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.exception.FtpConfigException;

public class NearestLocationFinder {
	
	private static Log          log                          = LogFactory.getLog(NearestLocationFinder.class);
	
	private static final String DEFAULT_LOCATION_FILE = "griffin-location.xml";
    private static final String XPATH_LOCATIONS               = "/location-selector/locations";
    private static final String ELEM_LOCATION               = "location";
    private static final String ATTR_ID                     = "id";
    private static final String ATTR_HOSTNAME                     = "hostname";
    private static final String ATTR_PORT                     = "port";
    private static final String ELEM_RESOURCE                     = "resource";
    private static final String XPATH_SELECTOR               = "/location-selector/selector";
    private static final String ELEM_IF               = "if";
    private static final String ATTR_COUNTRY                     = "country";
    private static final String ATTR_REGION                     = "region";
    private static final String ELEM_SELECT               = "select";
    private static final String XPATH_ELSE               = "/location-selector/selector/else";

    private String              filename;
    
    private List<GridFTPLocation> gridftpServers;
    private List<IfCondition> ifConditions;
    private String elseCondition;
    
    private String geoDataFile;
    private LookupService lookupService;
	
	public NearestLocationFinder(String geoDataFile) throws FtpConfigException {
		this.geoDataFile=geoDataFile;
		loadConfig();
	}
	private void loadConfig() throws FtpConfigException {
        File file = null;
        try {
            SAXReader reader = new SAXReader();
            file = new File(getFilename());
            BufferedReader br;
            if (file.exists()) {
                br = new BufferedReader(new FileReader(file));
            } else {
                InputStream is = getClass().getResourceAsStream("/" + DEFAULT_LOCATION_FILE);
                br = new BufferedReader(new InputStreamReader(is));
            }
            Document doc = reader.read(br);
            process(doc);
        } catch (IOException e) {
            throw new FtpConfigException("Reading " + getFilename() + " failed.");
        } catch (DocumentException e) {
            log.error(e.toString());
            throw new FtpConfigException("Error while processing the configuration file " + file + ".");
        }
        try {
        	if (!geoDataFile.startsWith("/")) geoDataFile=System.getProperty(FtpConstants.GRIFFIN_HOME)+File.separator+geoDataFile;
        	lookupService = new LookupService(geoDataFile, LookupService.GEOIP_MEMORY_CACHE );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FtpConfigException("Error while loading the configuration file " + file + ".");
		}
	}
	private void process(Document doc){
		gridftpServers=new ArrayList<GridFTPLocation>();
        Element locationsElement = (Element) doc.selectSingleNode(XPATH_LOCATIONS);
        List<Element> locationElements = locationsElement.selectNodes(ELEM_LOCATION);
        for (Element locationElement : locationElements) {
            String id = locationElement.attributeValue(ATTR_ID);
            String hostname = locationElement.attributeValue(ATTR_HOSTNAME);
            int port = Integer.parseInt(locationElement.attributeValue(ATTR_PORT));
            GridFTPLocation locationData = new GridFTPLocation(id, hostname, port);
            gridftpServers.add(locationData);
            List<Element> resourceElements = locationElement.selectNodes(ELEM_RESOURCE);
            for (Element resourceElement : resourceElements) {
            	locationData.getResources().add(resourceElement.getText());
            }
        }
		
        ifConditions=new ArrayList<IfCondition>();
        Element selectorElement = (Element) doc.selectSingleNode(XPATH_SELECTOR);
        List<Element> ifElements = selectorElement.selectNodes(ELEM_IF);
        for (Element ifElement : ifElements) {
            String country = ifElement.attributeValue(ATTR_COUNTRY);
            String region = ifElement.attributeValue(ATTR_REGION);
            IfCondition ifData = new IfCondition(country, region);
            ifConditions.add(ifData);
            List<Element> selectElements = ifElement.selectNodes(ELEM_SELECT);
            for (Element selectElement : selectElements) {
            	ifData.getSelects().add(selectElement.getText());
            }
        }
        Element elseElement = (Element) doc.selectSingleNode(XPATH_ELSE);
        if (elseElement!=null){
        	elseCondition=elseElement.getText();
        }
	}
    /**
     * Getter method for the java bean <code>filename</code>.
     * 
     * @return Returns the value of the java bean <code>filename</code>.
     */
    public String getFilename() {
        if (filename == null || filename.length() == 0) {
            String ctxDir = System.getProperty("griffin.ctx.dir");
            File file;
            if (ctxDir != null) {
                file = new File(ctxDir, DEFAULT_LOCATION_FILE);
            } else {
                file = new File(DEFAULT_LOCATION_FILE);
            }
            filename = file.getAbsolutePath();
        }
        log.info("Location configuration file: " + filename);
        return filename;
    }
    
	public String getGeoDataFile() {
		return geoDataFile;
	}
	public void setGeoDataFile(String geoDataFile) {
		this.geoDataFile = geoDataFile;
	}
	public List<String> findLocation(String remoteIp) {
		Location loc = lookupService.getLocation(remoteIp);
		if (loc!=null) {
			String country=loc.countryCode;
			String region=loc.region;
			log.debug("remoteIp "+remoteIp+" is from "+country+":"+region);
			for (IfCondition ifCondition:ifConditions){
				if (ifCondition.getCountry().equals(country)&&ifCondition.getRegion().equals(region)){
					return ifCondition.getSelects();
				}
			}
		}
		if (elseCondition!=null) {
			List<String> select=new LinkedList<String>();
			select.add(elseCondition);
			return select;
		}
		return null;
	}
	public GridFTPLocation getLocationById(String loc){
		for (GridFTPLocation location:gridftpServers){
			if (location.getId().equals(loc)){
				return location;
			}
		}
		return null;
	}
}
