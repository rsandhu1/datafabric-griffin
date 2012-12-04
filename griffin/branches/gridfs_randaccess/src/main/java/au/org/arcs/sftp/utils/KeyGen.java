package au.org.arcs.sftp.utils;

import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

public class KeyGen {
	public static void main(String[] args){
		if (args.length<3) {
			System.out.println("usage: java -cp isftp.jar au.org.arcs.sftp.utils.KeyGen key-file-name-with-full-path algorithm key-size");
			System.out.println("example: java -cp isftp.jar au.org.arcs.sftp.utils.KeyGen ./key.ser DSA 1024");
			System.exit(1);
		}
		String path=args[0];
		String algorithm=args[1];
		int keySize=Integer.parseInt(args[2]);
		SimpleGeneratorHostKeyProvider provider=new SimpleGeneratorHostKeyProvider(path, algorithm, keySize);
		provider.loadKeys();
	}
}
