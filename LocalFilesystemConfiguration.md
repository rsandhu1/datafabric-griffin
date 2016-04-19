# Configuration of Local File System Storage Backend #
The local storage end point can be configured in the main configuration file. It is important to adjust the `rootPath`
```
    <bean id="fileSystem"
            class="au.org.arcs.griffin.filesystem.impl.localfs.LocalFileSystemImpl"
            singleton="true">
        <property name="rootPath" value="/data/testroot" />
        <property name="userManager" ref="userManager" />
    </bean>
    
    <bean id="userFileReader"
            class="au.org.arcs.griffin.usermanager.impl.XmlFileReader"
            singleton="true">
    </bean>

    <bean id="userManager"
            class="au.org.arcs.griffin.usermanager.impl.XmlFileUserManager"
            singleton="true"
        <property name="fileReader" ref="userFileReader" />
    </bean>
```

For the user management a `griffin-users.xml` file must be created. A sample can be found below. For further information see this URL, but beware that some minor things have changed: http://hermesftp.sourceforge.net/users.html

```
<?xml version="1.0" encoding="UTF-8"?>
<user-manager>
    <groups>
        <group name="guest" >
            <limits>
                <limit name="Bytes downloaded" value="1000000" />
                <limit name="Bytes uploaded"   value="0" />
                <limit name="Files downloaded" value="100" />
                <limit name="Files uploaded"   value="0" />
                <limit name="Upload rate"      value="0" />
                <limit name="Download rate"    value="100" />
            </limits>
            <permissions>
                <permission flag="r" path="/guest/**" />
            </permissions>
        </group>

        <group name="users" >
            <limits>
                <limit name="Bytes downloaded" value="-1" />
                <limit name="Bytes uploaded"   value="-1" />
                <limit name="Files downloaded" value="-1" />
                <limit name="Files uploaded"   value="-1" />
                <limit name="Upload rate"      value="1000" />
                <limit name="Download rate"    value="1000" />
            </limits>
            <permissions>
                <permission flag="rw" path="/${user}/**" />
                <permission flag="rw" path="/${user}" />
                <permission flag="r"  path="/" />
            </permissions>
        </group>

        <group name="administrators" >
            <limits>
                <limit name="Bytes downloaded" value="-1" />
                <limit name="Bytes uploaded"   value="-1" />
                <limit name="Files downloaded" value="-1" />
                <limit name="Files uploaded"   value="-1" />
                <limit name="Upload rate"      value="-1" />
                <limit name="Download rate"    value="-1" />
            </limits>
            <permissions>
                <permission flag="rw" path="/**" />
                <permission flag="rw" path="/" />
            </permissions>
        </group>
    </groups>

    <users default-dir="${user}">
        <user uid="admin"
                fullname="Administrator"
                adminrole="true">
            <group-ref name="users" />
            <group-ref name="administrators" />
        </user>

        <user uid="anonymous"
                fullname="Anonymous User"
                dir="/guest">
            <group-ref name="guest" />
        </user>

        <user uid="fooman"
                fullname="Foo Man User">
            <group-ref name="users" />
        </user>
    </users>
    
    <mappings>
        <mapping dn="/C=NZ/O=BeSTGRID/OU=Massey University/CN=Guy Kloss"
                uid="fooman" />
        <mapping dn="/C=AU/O=APACGrid/OU=SAPAC/CN=Shunde Zhang"
                uid="admin" />
    </mappings>

</user-manager>
```