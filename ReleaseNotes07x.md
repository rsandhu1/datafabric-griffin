This release has been tested with the new globus file transfer service - globus.org.

Change note for 0.7.3 (11 Oct 2010):

  * upgraded jargon to 2.4.0 (svn://irodssvn.ucsd.edu/tags/Jargon\_2\_4\_0), This is recommended for users who are using iRODS 2.4+
  * correct welcome message on login

Change note for 0.7.2 (22 July 2010):

  * Client connection can be closed properly when timeout

Change note for 0.7.1 (31 May 2010):

  * Support data protection level C and P (PROT C and PROT P)

Bug fixes:

  * proper data channel re-use
  * proper perf marker messages
  * a few bugs related to parallel transfer

New feature:

  * this version is packaged with new jargon 2.3.0
  * for iRODS
    * irods-mapfile for non-IES iRODS version 2.3.0 and above
    * can specify default resource
```
	<bean id="fileSystem" class="au.org.arcs.griffin.filesystem.impl.jargon.JargonFileSystemImpl" singleton="true">
		<property name="serverName" value="arcs-df.vpac.org" />
		<property name="serverPort" value="1247" />
		<property name="serverType" value="irods" />
		<!-- if connecting to a slave, this is needed for irods2.3 and previous versions. -->
		<property name="mapFile" value="irods-mapfile" />
		<!-- update interval, default is 10 mins -->
		<!-- <property name="updateInterval" value="10" /> -->
		<!-- default resource can be specified here. if no default resource is specified, it will be decided by the rule engine. -->
		<!-- <property name="defaultResource" value="arcs-df.vpac.org" /> -->
	</bean>
```
  * Griffin can work with local file system now
    * In griffin-ctx.xml, need to add the following, ''rootPath'' is the root in gridftp context, where the data is stored, note that all files will be owned by the user who runs griffin. Permissions are specified in griffin-users.xml file.
```
	<bean id="fileSystem" class="au.org.arcs.griffin.filesystem.impl.localfs.LocalFileSystemImpl" singleton="true">
		<property name="rootPath" value="/data/testroot" />
		<property name="userManager" ref="userManager" />
	</bean>
	
	<bean id="userFileReader" class="au.org.arcs.griffin.usermanager.impl.XmlFileReader" singleton="true">
	</bean>

	<bean id="userManager" class="au.org.arcs.griffin.usermanager.impl.XmlFileUserManager" singleton="true">
		<property name="fileReader" ref="userFileReader"/>
	</bean>
```
    * griffin-users.xml file has mappings, users and groups information as well as permissions. here is an example:
      * The cert "/C=AU/O=APACGrid/OU=SAPAC/CN=Shunde Zhang" is mapped to the user 'user'.
      * The user's default dir is /user, it is a member of group 'users'.
      * group 'users' can read-and-write all files under /user, also can read root '/'.
```
	<groups>
		<group name="users" >
		    <limits>
		    	<limit name="Bytes downloaded" value="-1"/>
		    	<limit name="Bytes uploaded" value="-1"/>
		    	<limit name="Files downloaded" value="-1"/>
		    	<limit name="Files uploaded" value="-1"/>
		    	<limit name="Upload rate" value="1000"/>
		    	<limit name="Download rate" value="1000"/>
		    </limits>
		    <permissions>
		    	<permission flag="rw" path="/${user}/**"/>
		    	<permission flag="r" path="/"/>
			</permissions>
		</group>
	</groups>
	<users default-dir="/${user}">
		<user uid="user" fullname="Test User">
			<group-ref name="users"/>
		</user>
	</users>
	<mappings>
		<mapping dn="/C=AU/O=APACGrid/OU=SAPAC/CN=Shunde Zhang" uid="user"/>
	</mappings>
```