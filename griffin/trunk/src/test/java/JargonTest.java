import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;


public class JargonTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GlobusCredential gCert;
		IRODSSession iRODSFileSystem;
		IRODSProtocolManager iRODSProtocolManager;
		try {
			gCert = GlobusCredential.getDefaultCredential();
			GSSCredential cert = new GlobusGSSCredentialImpl(gCert, GSSCredential.INITIATE_AND_ACCEPT);
			iRODSProtocolManager=IRODSSimpleProtocolManager.instance();
			iRODSProtocolManager.initialize();
			iRODSFileSystem=new IRODSSession(iRODSProtocolManager);
			IRODSAccount account = IRODSAccount.instance("192.102.251.157", 1247,
                    cert);
//			IRODSAccount account = IRODSAccount.instance("192.102.251.157", 1247,
//                    "shunde", "123456", "/testZone/home/shunde", "testZone", "");
			System.out.println(account.getAuthenticationScheme());
			IRODSFileFactory fileFactory = new IRODSFileFactoryImpl(iRODSFileSystem, account);
			System.out.println(account.getUserName());
		} catch (GlobusCredentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GSSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
