/*
 * GridfsConfig.java
 * 
 * Container for MongoDB/GridFS storage backend configuration options.
 * 
 * Created: 2012-12-23 Guy K. Kloss <guy.kloss@aut.ac.nz>
 * Changed:
 * 
 * Copyright (C) 2012 Auckland University of Technology, New Zealand
 * 
 * Some rights reserved
 * 
 * http://www.aut.ac.nz/
 */
 
package au.org.arcs.griffin.filesystem.impl.gridfs;


/**
 * Container for MongoDB/GridFS storage backend configuration options.
 *
 * @author Guy K. Kloss
 */
public class GridfsConfig {
    private String _serverName = GridfsConstants.SERVER_NAME;
    private int _serverPort = GridfsConstants.DEFAULT_SERVER_PORT;
    private String _serverType = GridfsConstants.SERVER_TYPE;
    private String _dbName = null;
    private String _bucketName = GridfsConstants.BUCKET_NAME;
    private String _user = null;
    private char[] _password = null;
    private int _defaultPermissions = GridfsConstants.DEFAULT_PERMISSIONS;
    
    private int _umask = GridfsConstants.DEFAULT_UMASK;
    
    /**
    * Gets server name for connect.
    * @return Returns the serverName.
    */
   public String getServerName() {
       return _serverName;
   }
   
   /**
    * Sets server name for connect.
    * @param serverName The serverName to set.
    */
   public void setServerName(String serverName) {
       this._serverName = serverName;
   }

   /**
    * Gets server port for connect.
    * @return Returns the serverPort.
    */
   public int getServerPort() {
       return _serverPort;
   }

   /**
    * Sets server port for connect.
    * @param serverPort The serverPort to set.
    */
   public void setServerPort(int serverPort) {
       this._serverPort = serverPort;
   }
   
   /**
    * Gets server type for this connection (should be "gridfs").
    * @return Returns the serverType.
    */
   public String getServerType() {
       return _serverType;
   }

   /**
    * Sets server type for this connection (should be "gridfs").
    * @param serverType The serverType to set.
    */
   public void setServerType(String serverType) {
       this._serverType = serverType;
   }

   /**
    * Gets DB name for connect.
    * @return Returns the dbName.
    */
   public String getDbName() {
       return _dbName;
   }

   /**
    * Sets DB name for connect.
    * @param dbName The dbName to set.
    */
   public void setDbName(String dbName) {
       this._dbName = dbName;
   }

   /**
    * Gets GridFS bucket name for storage.
    * @return Returns the bucket name.
    */
   public String getBucketName() {
       return _bucketName;
   }

   /**
    * Sets GridFS bucket name for storage.
    * @param bucketName The bucket name to set.
    */
   public void setBucketName(String bucketName) {
       this._bucketName = bucketName;
   }

   /**
    * Gets DB user for connect.
    * @return Returns the user.
    */
   public String getUser() {
       return _user;
   }

   /**
    * Sets DB user for connect.
    * @param user The user to set.
    */
   public void setUser(String user) {
       this._user = user;
   }

   /**
    * Gets DB password for connect.
    * @return Returns the password.
    */
   public char[] getPassword() {
       return _password;
   }

   /**
    * Sets DB password for connect.
    * @param password The password to set.
    */
   public void setPassword(char[] password) {
       this._password = password;
   }

   /**
    * Permission settings (UN*X style permissions as octal integer).
    * @return Returns default permission settings.
    */
   public int getDefaultPermissions() {
       return _defaultPermissions;
   }
   
   /**
    * Permission settings (UN*X style permissions as octal integer).
    * @param defaultPermissions Default permissions to set.
    */
   public void setDefaultPermissions(int defaultPermissions) {
       this._defaultPermissions = defaultPermissions;
   }
   
   /**
    * File mode creation mask (UN*X style as octal integer).
    * @return Returns file creation umask.
    */
   public int getUmask() {
       return _umask;
   }

   /**
    * File mode creation mask (UN*X style as octal integer).
    * @param umask The file creation umask to set.
    */
   public void setUmask(int umask) {
       this._umask = umask;
   }
}
