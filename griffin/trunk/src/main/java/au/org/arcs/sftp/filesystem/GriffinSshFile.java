package au.org.arcs.sftp.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.sshd.server.SshFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.RafInputStream;
import au.org.arcs.griffin.streams.RafOutputStream;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * Implements SshFile to provide Griffin generic file system backend via FileObjectCache
 * 
 * @author John Curtis
 */
public class GriffinSshFile implements SshFile
{
	private final Logger		log	= LoggerFactory.getLogger(getClass());
	private FileObject	fileObject;

	/**
	 * Constructor, internal do not use directly.
	 */
	protected GriffinSshFile(FileObject fileObject)
	{		
		this.fileObject = fileObject;
	}

//	protected GriffinSshFile(FileObjectCache fileObjectCache)
//	{		
//		this.fileObject = fileObjectCache;
//	}

	/**
	 * Get full name.
	 */
	public String getAbsolutePath()
	{
		//TODO only used in SFTP for feed back so is this FTP abs path OK
		return fileObject.getPath();
		//return FilenameUtils.normalizeNoEndSeparator(fileObject.getPath());
	}

	/**
	 * Get short name.
	 */
	public String getName()
	{
		log.debug("getName:"+fileObject.getName());
		return fileObject.getName();
	}

	/**
	 * Is it a directory?
	 */
	public boolean isDirectory()
	{
		log.debug("isDirectory("+fileObject.getName()+")?"+fileObject.isDirectory());
		return fileObject.isDirectory();
	}

	/**
	 * Is it a file?
	 */
	public boolean isFile()
	{
		return fileObject.isFile();
	}

	@Override
	public boolean isExecutable()
	{
		//return for directories as solves issues with dir copy to linux systems
		return isDirectory() && (isReadable() || isWritable());
	}


	/**
	 * Does this file exists?
	 */
	public boolean doesExist()
	{
		return fileObject.exists();
	}

	/**
	 * Get file size.
	 */
	public long getSize()
	{
		return fileObject.length();
	}

	/**
	 * Get last modified time.
	 */
	public long getLastModified()
	{
		return fileObject.lastModified();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean setLastModified(long time)
	{
		return fileObject.setLastModified(time);
	}

	/**
	 * Check read permission.
	 */
	public boolean isReadable()
	{
		return (fileObject.getPermission()&FtpConstants.PRIV_READ)>0;
	}

	/**
	 * Check file write permission.
	 */
	public boolean isWritable()
	{
		if (!fileObject.exists()){
			return (fileObject.getParent().getPermission()&FtpConstants.PRIV_WRITE)>0;
		}
		return (fileObject.getPermission()&FtpConstants.PRIV_WRITE)>0;
	}

	/**
	 * Has delete permission.
	 */
	public boolean isRemovable()
	{
		// root cannot be deleted
		if ("/".equals(fileObject.getPath()))
			return false;
		//In the case that the permission is not explicitly denied for this
		//file we will check if the parent file has write permission as most
		//systems consider that a file can be deleted when their parent directory is writable.
		return parentIsWritable();
	}

	public boolean parentIsWritable()
	{
		FileObject parent = fileObject.getParent();
		return parent == null ? false : (parent.getPermission()&FtpConstants.PRIV_WRITE)>0;
	}

	public SshFile getParentFile()
	{
		return new GriffinSshFile(fileObject.getParent());
	}

	/**
	 * Delete file.
	 */
	public boolean delete()
	{
		return fileObject.delete();
	}

	@Override
	public boolean create() throws IOException
	{
		return fileObject.create();
	}


	/**
	 * Truncate file to length 0.
	 */
	public void truncate() throws IOException
	{
//		fileObject.truncate();
	}

	/**
	 * Move file object.
	 */
	public boolean move(final SshFile dest)
	{
		GriffinSshFile griffin_file=(GriffinSshFile)dest;
		if (griffin_file!=null)
			return fileObject.renameTo(griffin_file.fileObject);
		return false;
	}

	/**
	 * Create directory.
	 */
	public boolean mkdir()
	{
		return fileObject.mkdir();
	}

	/**
	 * List files. If not a directory or does not exist, null will be returned.
	 */
	@Override
	public List<SshFile> listSshFiles()
	{
		log.debug("listSshFiles");
        // is a directory
        if (!fileObject.isDirectory()) {
            return null;
        }
		FileObject[] files_cache=null;
		try {
			files_cache = fileObject.listFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("listSshFiles error", e);
		}
		log.debug("listSshFiles files_cache:"+files_cache);
		if(files_cache==null)
			return null;
		
        // now make Griffin files
		List<SshFile> list=new ArrayList<SshFile>(files_cache.length);
 		for (FileObject foc : files_cache)
		{
 			if(!foc.getName().isEmpty())
 				list.add(new GriffinSshFile(foc));
		}
 		log.debug("listSshFiles result:"+list);
		return list;
	}

	/**
	 * Create output stream for writing.
	 */
	public OutputStream createOutputStream(final long offset) throws IOException
	{
		// permission check
//		if (!isWritable())
//			throw new IOException("No write permission : " + getName());
		// create output stream
		log.debug("createOutputStream:"+fileObject.getName()+";offset:"+offset);
		if (offset==0)
			return fileObject.getOutputStream();
		return new RafOutputStream(fileObject.getRandomAccessFileObject("rw"), offset);
	}

	/**
	 * Create input stream for reading.
	 */
	public InputStream createInputStream(final long offset) throws IOException
	{
		// permission check
		if (!isReadable())
			throw new IOException("No read permission : " + getName());
		// move to the appropriate offset and create input stream
		log.debug("createInputStream:"+fileObject.getName()+";offset:"+offset);
//		return new RafInputStream(fileObject, offset);
		return fileObject.getInpuStream(offset);
	}

	public void handleClose()
	{
		// No-op
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof GriffinSshFile)
			try {
				return FilenameUtils.equalsNormalizedOnSystem(fileObject.getCanonicalPath(),(((GriffinSshFile) obj).fileObject.getCanonicalPath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return false;
	}

	@Override
	public String getOwner() {
		// TODO Auto-generated method stub
		return fileObject.getOwner();
	}
}
