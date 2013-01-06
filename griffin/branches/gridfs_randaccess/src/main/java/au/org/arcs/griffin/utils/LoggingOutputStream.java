package au.org.arcs.griffin.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
 

public class LoggingOutputStream extends ByteArrayOutputStream{
    private String lineSeparator; 
    
    private Log log; 
    private String level; 
 
    /** 
     * Constructor 
     * @param logger Logger to write to 
     * @param level Level at which to write the log message 
     */ 
    public LoggingOutputStream(Log log, String level) { 
        super(); 
        this.log = log; 
        this.level = level; 
        lineSeparator = System.getProperty("line.separator"); 
    } 
 
    /** 
     * upon flush() write the existing contents of the OutputStream
     * to the logger as a log record. 
     * @throws java.io.IOException in case of error 
     */ 
    public void flush() throws IOException { 
 
        String record; 
        synchronized(this) { 
            super.flush(); 
            record = this.toString(); 
            super.reset(); 
 
            if (record.length() == 0 || record.equals(lineSeparator)) { 
                // avoid empty records 
                return; 
            } 
 
            if (level!=null&&level.equalsIgnoreCase("stdout"))
            	log.info(record);
            else if (level!=null&&level.equalsIgnoreCase("stderr"))
            	log.error(record);
//            logger.logp(level, "", "", record); 
        } 
    } 

}
