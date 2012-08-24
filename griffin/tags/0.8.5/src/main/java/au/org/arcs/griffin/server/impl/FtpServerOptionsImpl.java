/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */

package au.org.arcs.griffin.server.impl;

import java.io.File;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;

import org.gridforum.jgss.ExtendedGSSContext;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpServerOptions;
import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.utils.IOUtils;
import au.org.arcs.griffin.utils.SecurityUtil;
import au.org.arcs.griffin.utils.StringUtils;

/**
 * The FTP server options used throughout the application.
 * 
 * @author Lars Behnke
 */
public class FtpServerOptionsImpl implements FtpServerOptions, FtpConstants {

    private static final int    DEFAULT_BUFFER_SIZE       = 1024;

    private static final int    DEFAULT_FTP_IMPL_SSL_PORT = 990;

    private static final int    DEFAULT_FTP_PORT          = 21;

    private static final String SYS_OPT_VERSION           = "version";

    private static final String SYS_OPT_TITLE             = "title";

    private static final String SYS_OPT_BUILD_INFO        = "info";

    private static Log          log                       = LogFactory.getLog(FtpServerOptionsImpl.class);

    private Properties          properties;

    private Integer[]           allowedPassivePorts;

    private SSLContext          sslContext;

    protected String service_key;
    protected String service_cert;
    protected String service_trusted_certs;

    /**
     * {@inheritDoc}
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    public int getBufferSize() {
        return getInt(OPT_BUFFER_SIZE, DEFAULT_BUFFER_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    public String getRootDir() {
        File defaultDir = new File(System.getProperty("user.home"), "griffin");
        String dir = getString(OPT_REMOTE_DIR, defaultDir.getAbsolutePath());
        return dir;
    }

    /**
     * {@inheritDoc}
     */
    public int getFtpPort() {
        return getInt(OPT_FTP_PORT, DEFAULT_FTP_PORT);
    }

    /**
     * {@inheritDoc}
     */
    public int getImplicitSslPort() {
        return getInt(OPT_SSL_PORT_IMPLICIT, DEFAULT_FTP_IMPL_SSL_PORT);
    }

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(String optionName, boolean defaultValue) {
        boolean result;
        String boolStr = getProperties().getProperty(optionName);
        if ("true".equalsIgnoreCase(boolStr) || "false".equalsIgnoreCase(boolStr)) {
            result = Boolean.valueOf(boolStr).booleanValue();
        } else {
            result = defaultValue;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public int getInt(String optionName, int defaultValue) {
        int result = defaultValue;
        String intStr = getProperties().getProperty(optionName);
        if (intStr != null && intStr.length() > 0) {
            try {
                result = Integer.parseInt(intStr);
            } catch (NumberFormatException e) {
                log.error("Invalid integer: " + intStr + ". Fall back to default value " + result);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String getString(String optionName, String defaultValue) {
        String result = getProperties().getProperty(optionName);
        if (result == null || result.length() == 0) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getStringArray(String optionName, String[] defaultValues) {
        String[] result = defaultValues;
        String strList = getProperties().getProperty(optionName);
        if (strList != null && strList.length() > 0) {
            result = strList.split(SEPARATOR);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public int[] getIntArray(String optionName, int[] defaultValues) {
        int[] result = defaultValues;
        String strList = getProperties().getProperty(optionName);
        if (strList != null && strList.length() > 0) {
            String[] elems = strList.split(SEPARATOR);
            result = new int[elems.length];
            for (int i = 0; i < elems.length; i++) {
                result[i] = Integer.parseInt(elems[i].trim());
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Integer[] getAllowedTCPPorts() {
        if (allowedPassivePorts == null) {
            String allowedPorts = getString(OPT_ALLOWED_TCP_PORTS, null);
            allowedPassivePorts = StringUtils.parseIntegerList(allowedPorts);
        }
        return allowedPassivePorts;
    }
    public Integer[] getAllowedUDPPorts() {
        if (allowedPassivePorts == null) {
            String allowedPorts = getString(OPT_ALLOWED_UDP_PORTS, null);
            allowedPassivePorts = StringUtils.parseIntegerList(allowedPorts);
        }
        return allowedPassivePorts;
    }

    /**
     * {@inheritDoc}
     */
    public SSLContext getSslContext() throws FtpConfigException {
        if (sslContext == null) {
            char[] ksPass;
            String ksFile = getProperty(OPT_SSL_KEYSTORE_FILE);
            if (ksFile == null || ksFile.length() == 0) {
                throw new FtpConfigException("Keystore file not defined.");
            }
            String ksPassStr = getProperty(OPT_SSL_KEYSTORE_PASS);
            ksPass = ksPassStr == null ? new char[0] : ksPassStr.toCharArray();
            try {
                sslContext = SecurityUtil.createSslContext(ksFile, ksPass);
            } catch (SecurityException e) {
                throw new FtpConfigException(e.getMessage());
            }
        }
        return sslContext;
    }

	public GSSContext getGSSContext() throws FtpConfigException {
//		if (gssContext!=null) return gssContext;
        GlobusCredential serviceCredential;
        try {
            serviceCredential = new GlobusCredential(getProperty(SERVICE_CERT), getProperty(SERVICE_KEY));
        }
        catch (GlobusCredentialException gce) {
            String errmsg = "couldn't load " +
                            "host globus credentials: " + gce.toString();
            log.error(errmsg);
            throw new FtpConfigException(new GSSException(GSSException.NO_CRED, 0, errmsg).getMessage());
        }
        log.debug("service cert:"+serviceCredential.getSubject());
        try {
	        GSSCredential cred = new GlobusGSSCredentialImpl(serviceCredential,
	                                                    GSSCredential.ACCEPT_ONLY);
	        TrustedCertificates trusted_certs =
	                               TrustedCertificates.load(getProperty(SERVICE_TRUSTED_CERTS));
	        GSSManager manager = ExtendedGSSManager.getInstance();
	        ExtendedGSSContext context =
	                               (ExtendedGSSContext)manager.createContext(
	                                       null,
	                                       GSSConstants.MECH_OID,
	                                       cred,
	                                       GSSContext.DEFAULT_LIFETIME);
	                               
//	                               manager.createContext(cred);
	
	        context.setOption(GSSConstants.GSS_MODE, GSIConstants.MODE_GSI);
	        context.setOption(GSSConstants.TRUSTED_CERTIFICATES, trusted_certs);
	
//	        gssContext = context;
	        return context;
        }catch (GSSException e){
        	throw new FtpConfigException(e.getMessage());
        }
	}

    /**
     * {@inheritDoc}
     */
    public String getAppTitle() {
        return getSystemProperties().getProperty(SYS_OPT_TITLE);
    }

    /**
     * {@inheritDoc}
     */
    public String getAppVersion() {
        return getSystemProperties().getProperty(SYS_OPT_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    public String getAppBuildInfo() {
        return getSystemProperties().getProperty(SYS_OPT_BUILD_INFO);
    }

    /**
     * {@inheritDoc}
     */
    public Properties getSystemProperties() {
        return IOUtils.getAppProperties();
    }


}
