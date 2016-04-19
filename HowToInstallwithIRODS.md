# Introduction #

This installation guide is mainly about how to set up with iRODS.

# Details #

We assume you want to put griffin in /opt/griffin, and iRODS runs as the 'rods' user (Unix user). We run Griffin also as 'rods' so it can easily interact with iRODS.

  * download and untar the package
  * Edit /etc/default/griffin
```
APP_HOME=/opt/griffin
APP_USER=rods
JAVA_OPTIONS="-DGRIFFIN_HOME=$APP_HOME -Djava.library.path=$APP_HOME -Dlog4j.configuration=file:$APP_HOME/log4j.properties -server -Xms512m -Xmx768m -Djavax.net.ssl.trustStore=/opt/davis/etc/ssl.keystore -Djavax.net.ssl.trustStorePassword=123456 -DX509_CERT_DIR=/etc/grid-security/certificates -Duserid=$(id $APP_USER -u)"
```
    * trustStore is useful if you want to user LDAP authentication in iRODS 3.2
    * userid is useful if you want to use public key authentication in SFTP/GridFTP over SSH
  * copy griffin to /etc/init.d
  * Edit log4j.properties and update your path
```
log4j.appender.R.File=/opt/griffin/logs/griffin.log
```
  * change permission of everything under /opt/griffin to be owned by 'rods'
  * Edit configuration file griffin-ctx.xml
    * [iRODS](IrodsConfiguration.md)
    * [SSH Configuration](SSHnPublicKeyAuth.md)
  * Start Griffin using 'service griffin start'