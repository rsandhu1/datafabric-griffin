package au.org.arcs.griffin.filesystem.impl.jargon;



public class CachedFile  {
	private long length;
	private boolean isDir;
	private long lastModified;
	private boolean canWrite;
	private boolean canRead;
	private String canonicalPath;
	
	
//	public CachedFile(RemoteFileSystem rfs, String path, String filename) throws NullPointerException {
//		super(rfs, path, filename);
//	}
//
//	public CachedFile(RemoteFileSystem rfs, String filePath) throws NullPointerException {
//		super(rfs, filePath);
//	}
//
//	@Override
//	public String getResource() throws IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void replicate(String arg0) throws IOException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public boolean canWrite() {
//		// TODO Auto-generated method stub
//		return this.canWrite;
//	}
//	
//	public boolean canRead() {
//		return this.canRead;
//	}
//
//	@Override
//	public String getName() {
//		// TODO Auto-generated method stub
//		return super.getName();
//	}
//
//	@Override
//	public boolean isDirectory() {
//		// TODO Auto-generated method stub
//		return isDir;
//	}
//
//	@Override
//	public boolean isFile() {
//		// TODO Auto-generated method stub
//		return !isDir;
//	}
//
//	@Override
//	public boolean isHidden() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public long lastModified() {
//		// TODO Auto-generated method stub
//		return this.lastModified;
//	}
//
//	@Override
//	public long length() {
//		// TODO Auto-generated method stub
//		return length;
//	}
//	public void setLength(long length){
//		this.length=length;
//	}
//
//	@Override
//	public boolean setLastModified(long arg0) {
//		// TODO Auto-generated method stub
//		this.lastModified=arg0;
//		return true;
//	}
//	
//	public void setDirFlag(boolean isDir) {
//		this.isDir=isDir;
//	}
//	public void setCanWriteFlag(boolean canWrite){
//		this.canWrite=canWrite;
//	}
//	public void setCanReadFlag(boolean canRead){
//		this.canRead=canRead;
//	}
//
//	public String getCanonicalPath() {
//		return getPath()+File.separator+getName();
//	}
//
//	public void setCanonicalPath(String canonicalPath) {
//		this.canonicalPath = canonicalPath;
//	}
	
}
