package au.org.arcs.griffin.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.DataChannel;

/**
 * E data block wrapper
 * @author Shunde Zhang
 *
 */

public class    EDataBlock implements BlockModeConstants{
	private static Log          log                 = LogFactory.getLog(EDataBlock.class);
    private byte[] header;
    private byte[]  data;
    private String _myName="unknown";
    private long preferredBufferSize=1048576;
    
    public long getPreferredBufferSize() {
		return preferredBufferSize;
	}
	public EDataBlock(String name){
    	_myName = name;
    }
    public EDataBlock(String name, long preferredBufferSize){
    	_myName = name;
    	this.preferredBufferSize=preferredBufferSize;
    }

    public EDataBlock()
    {   }

    public byte[] getHeader() {
        return header;
    }
    public void setData(byte[] b){
    	this.data=b;
    }

    public byte getDescriptors() 
    {
        return header[0];
    }
    
    public boolean isDescriptorSet(int descriptor)
    {
        return (header[0] & descriptor) != 0;
    }
    
    public void setDCCountTo1()
    {
        for ( int i = 9; i < 17; i++ )
            header[i] = 0;
        header[16]= 1;
    }
    
    public void setDescriptor(int descriptor)
    {
        header[0] = (byte)(header[0] | descriptor);
    }
    
    public void unsetDescriptor(int descriptor)
    {
        header[0] = (byte)(header[0] & ~descriptor);
    }
    
    public byte[] getData() {
        return data;
    }

    public long getSize() {
        long size = 0;
        for ( int i = 1; i < 9; i++ )
        {
            size = (size << 8) | ((int)header[i] & 0xFF);
        }
        return size;
    }

    public long getDataChannelCount() {
        //XXX probably throwing an exception would be a better plan here...
        if( isDescriptorSet( DESC_CODE_EODC) )
        {
            return getOffset();
        }
        else
        {
            return -1;
        }
    }


    public long getOffset() {
        long offset = 0;
        for ( int i = 9; i < 17; i++ )
        {
            offset = (offset << 8) | ((int)header[i] & 0xFF);
        }
        return offset;
    }
    
    public void clearHeader(){
    	if (header==null) header=new byte[EBLOCK_HEADER_LENGTH];
    	for (int i=0;i<EBLOCK_HEADER_LENGTH;i++) header[i]=0;
    }
    
    public void writeHeader(OutputStream os) throws IOException{
    	for (int i=0;i<EBLOCK_HEADER_LENGTH;i++) 
    		os.write(header[i]);
    	os.flush();
    }
    
    public void setOffset(long offset){
    	for (int i=16;i>=9;i--){
    		header[i]=(byte) ((byte)offset & 0x000000FFL);
    		offset = offset >>> 8;
    	}
    	
    }
    public void setSize(long size){
    	for (int i=8;i>=1;i--){
    		header[i]=(byte) ((byte)size & 0x000000FFL);
    		size = size >>> 8;
    	}
    }

    public long read(InputStream str) {
        header = new byte[EBLOCK_HEADER_LENGTH];
        int     len = 0;

        while( len < EBLOCK_HEADER_LENGTH ) 
        {
            int n;
            try 
            {
                n = str.read(header, len, EBLOCK_HEADER_LENGTH - len);
            } 
            catch( Exception e ) 
            {
                break;
            }
            if ( n <= 0 )
            {
                break;
            }
            len += n;
        }

        if( len < EBLOCK_HEADER_LENGTH )
            return -1;

        long size = 0;
        for ( int i = 1; i < 9; i++ )
            size = (size << 8) | ((int)header[i] & 0xFF);

        
        try
        {   
            data = new byte[(int)size]; 
        }
        catch( OutOfMemoryError e )
        {   
            //System.out.println("EDataBlock(" + _myName + ").read(): exception: " + e);
            throw e;
        }

        
        int n = 0;
        while( n < size ) {
            int nr;
            try {
                nr = str.read(data, n, (int)(size) - n);
            } 
            catch( Exception e ) 
            {
                break;
            }
            if( nr <= 0 )
                break;
            n += nr;
        }
        if( n < getSize() )
            n = -1;
    //System.out.println("EDataBlock(" + _myName + ").read(): returning " + n);
        return n;
    }

    public long read(DataChannel dc) {
        header = new byte[EBLOCK_HEADER_LENGTH];
        int     len = 0;

        while( len < EBLOCK_HEADER_LENGTH ) 
        {
            int n;
            try 
            {
                n = dc.read(header, len, EBLOCK_HEADER_LENGTH - len);
            } 
            catch( Exception e ) 
            {
                break;
            }
            if ( n <= 0 )
            {
                break;
            }
            len += n;
        }

        if( len < EBLOCK_HEADER_LENGTH )
            return -1;

        long size = 0;
        for ( int i = 1; i < 9; i++ )
            size = (size << 8) | ((int)header[i] & 0xFF);

        
        try
        {   
            data = new byte[(int)size]; 
        }
        catch( OutOfMemoryError e )
        {   
            //System.out.println("EDataBlock(" + _myName + ").read(): exception: " + e);
            throw e;
        }

        
        int n = 0;
        while( n < size ) {
            int nr;
            try {
                nr = dc.read(data, n, (int)(size) - n);
            } 
            catch( Exception e ) 
            {
                break;
            }
            if( nr <= 0 )
                break;
            n += nr;
        }
        if( n < getSize() )
            n = -1;
    //System.out.println("EDataBlock(" + _myName + ").read(): returning " + n);
        return n;
    }

    public String toString()
    {
    	StringBuffer descSB=new StringBuffer();
    	for (int i=7;i>=0;i--){
    		descSB.append(header[0]&(1<<i)).append(" ");
    	}
        return "EDataBlock("+_myName+"), descriptor="+descSB+", size="+getSize()+", offset="+getOffset();
    }

	public void writeHeader(DataChannel dc) throws IOException {
		dc.write(header);
	}
	public void write(DataChannel dc) throws IOException {
		dc.write(header);
		dc.write(data,0,(int) getSize());
	}
}

