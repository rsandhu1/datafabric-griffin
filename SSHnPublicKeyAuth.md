# Introduction #

From 0.9.0, SSH has been added to Griffin, along with public key authentication.


# Details #

In griffin-ctx.xml, need to add ssh configs:

```

        <bean id="sftpirods-options" class="au.org.arcs.sftp.config.SftpConfigProperties" singleton="true">
            <constructor-arg>
                  <props>
                          <!-- Optional include the SFTP Server Label, overrides the Label stored in app.properties-->
        <prop key="server.label">sFTP to iRods Server</prop>
                <!-- Must include the SResource location-->
        <prop key="resources.name">sftpirods-res</prop>
                <!-- Must include the SSH host key file location-->
        <prop key="sftp.hostkey.file">/opt/isftp/key.ser</prop>
                <!-- Default server port to use. 22 is the SFTP default, but we use 8080 by default to avoid conflicts with general SSH usage-->
        <prop key="sftp.port">2222</prop>
        <!--<prop key="internal.buffer.size">327680</prop>-->
                <!-- If don't include sftp.home.dir assumes directory containing this file-->
        <!-- <prop key="sftp.home.dir">/ARCS</prop> -->
        <!-- If don't include sftp.log.dir assumes /logs inside directory containing this file-->
        <!-- <prop key="sftp.log.dir">/ARCS/logs</prop> -->
        <!-- List of IP adresses to block -->
        <!-- <prop key="ipv4.black.list">!127.0.0.1,!192.*.*.*</prop> -->
            </props>
          </constructor-arg>
        </bean>

        <bean id="gridftpCommand" class="au.org.arcs.sftp.command.GridFTPCommand" singleton="false">
                <property name="cmdReader" ref="cmdReader" />
                <property name="options" ref="options" />
                <property name="resources" value="griffin-resources"/>
                <property name="fileSystem" ref="fileSystem" />
                <property name="ftpEventListener" ref="server" />
        </bean>

        <bean id="server" class="au.org.arcs.griffin.server.impl.GsiFtpServer" singleton="true">
                <property name="name" value="GSI FTP Server" />
                <property name="options" ref="options" />
                <property name="resources" value="griffin-resources"/>
                <property name="fileSystem" ref="fileSystem" />
                <property name="sshEnabled" value="true"/>
        </bean>

        <bean id="fileSystem" class="au.org.arcs.griffin.filesystem.impl.jargon.JargonFileSystemImpl" singleton="true">
                <property name="serverName" value="localhost" />
                <property name="serverPort" value="1247" />
                <property name="zoneName" value="testZone" />
                <property name="defaultAuthType" value="irods" />
                <property name="jargonInternalCacheBufferSize" value="512000" />

                <!-- if connecting to a slave, this is needed for irods2.3 and previous versions. -->
                <!-- 
                <property name="mapFile" value="irods-mapfile" />
                 -->
                <!-- update interval, default is 10 mins -->
                <!-- <property name="updateInterval" value="10" /> -->
                <!-- default resource can be specified here. if no default resource is specified, it will be decided by the rule engine. -->
                <!-- <property name="defaultResource" value="arcs-df.vpac.org" /> -->
        </bean>


```

Note that you need **zoneName** in fileSystem definition because it may use username/password for authentication. **defaultAuthType** can be irods (irods username password) or pam (pam auth for irods, from irods 3.2).

Then in /etc/default/griffin you need to add something to JAVA\_OPTIONS:

```
JAVA_OPTIONS="-DGRIFFIN_HOME=$APP_HOME -Djava.library.path=$APP_HOME -Dlog4j.configuration=file:$APP_HOME/log4j.properties -server -Xms512m -Xmx768m -Djavax.net.ssl.trustStore=/opt/davis/etc/ssl.keystore -Djavax.net.ssl.trustStorePassword=123456 -DX509_CERT_DIR=/etc/grid-security/certificates -Duserid=$(id $APP_USER -u)"
```

**userid** is needed if you want to use SSH public key authentication. And the account that runs irods should have .irodsEnv and .irodsA set up with irods admin permission (can generate temp password for other users) so better run griffin using the same user as running irods

You need to generate a ssh server key:
```
java -classpath griffin.jar au.org.arcs.sftp.utils.KeyGen
```

Then add the ssh public key to the irods user:
```
iadmin aua username "ssh-public-key"
```
The **ssh-public-key** is in openssh format (just get it from .ssh)
```
ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA0uo/hYfSEfnKabqB2r2Zxb451PtQ8XkPKxKf2YSRDTW2pnw2ZzeJc2YbASqjw5XPGQRhjnmnAwfmrzTjT5+GpLn7ygHTZVJzYj26QTU/uXxuZo/xUG9WNUxtPxTp1ogaKYDA8UGUjjMsSh/H2VHAz0vX8gjh6XhxfRX0nXzrwir+MJy1cnDv59Lg5cZwdVl1r5NOekB70TMUj3HaKg3+FppRSqWLLGDY7A+9/uEfdFubv9sWEMGXnqWfddufnIxQVWgNg1bel/6cFWp9eqgK8Fcuu8swwRsFUaKyYJVvMHDt12Yj1f1uc41lpNLs0fOjImwZOIS6gl/xInEq88LS+Q== shunde@shunde-client.ivec.org
```