# 0.9.0 #

First of the 0.9.x series. Added a few new things. (It absorbed the code from http://code.google.com/p/datafabric-isftp/)

  * GridFTP now supports sshftp (ssh over GridFTP)
  * sftp (the one from SSH)
  * jargon core (latest jargon release)
  * public key authentication for ssh (sftp and sshftp)
    * need to add public key to iRODS using iadmin lua username public-key-string (your ssh pub key)
    * set up .irodsEnv and .irodsA in $HOME/.irods for the account that runs griffin, this irods user must be an irods admin user who can generate temp password for other users.
  * PAM auth from the latest jargon core