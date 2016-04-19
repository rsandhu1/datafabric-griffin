0.8.0 is a backup version after re-structuring the source code. '''A re-install is needed''' if you want to upgrade a version prior to 0.8.0 to a version after 0.8.0.

## 0.8.5 ##
Upgraded jargon to 3.0

## 0.8.4 ##
  * Fixed two problems reported by Giacomo when using GO
```
1. The MLST reply (get info on file/dir) is not ending all lines with \r\n, but simply \n in the middle line.  
That is not per the ftp protocol.  This is a big problem because we have to balance interoperability vs allowing 
all possible 'special characters' in file names, like newlines themselves.

2. Doing a directory listing (MLSD) on '/' returns '/' in the output itself, which we don't like.  
We don't want any names returned to have / in them; the RFC examples all show relative path names. 
 I did a dirlisting of /CINECA and that looked ok, were it not for the problem in #1 still causing problems.
```
## 0.8.3 ##
  * Fixed the "Range Marker" issue reported by Karl
```
can't parse range marker: 111 Range Marker -2

fxp: fxp/range.cpp:103: void range_do_range(Connection*, File*, cc_response_t*): Assertion `0' failed.
Exited due to signal 6
```
  * Allow REST to accpet 0-N as interpreted as REST N, as per Karl's request (for GO)

## 0.8.2 ##
  * Updated jargon to 2.5
  * fixed HELP command (for GlobusOnline) so that it returns 5xx if the requested command is not implemented.

## 0.8.1 ##
We fixed a few things for griffin to work with Globus Online. The jargon plugin has been tested with iRODS 2.4.

New features:

  * Support mode E for dir listing

Bug fix:

  * MSLD can return proper ending characters
```
if using stream mode and ascii type, (guc without -fast and uberftp) send /r/r/n
if using e block mode and binary type, (guc with -fast), send /r/n
(gridftp spec says /r/n)
```

How to install:

  * see INSTALL file
```
1. extract tar ball to somewhere, e.g. /opt
2. (optional) create a soft link to griffin, e.g. /opt/griffin
3. make sure the account you use to run griffin owns /opt/griffin
4. make sure you have access to your server's key/cert files, e.g. in /etc/grid-security
5. run install.sh
6. start griffin and have fun.
```