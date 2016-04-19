# Configuration of iRODS Storage Backend #
The iRODS end point can be changed in the main configuration file.
```
    <bean id="fileSystem"
            class="au.org.arcs.griffin.filesystem.impl.jargon.JargonFileSystemImpl"
            singleton="false">
        <property name="serverName" value="arcs-df.vpac.org" />
        <property name="serverPort" value="1247" />
        <property name="serverType" value="irods" />
    </bean>
```