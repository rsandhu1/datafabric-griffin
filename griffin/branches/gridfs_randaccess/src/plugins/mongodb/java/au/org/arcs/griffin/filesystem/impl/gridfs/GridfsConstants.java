/*
 * GridfsConstants.java
 * 
 * Constants and defaults for the MongoDB GridFS storage backend.
 * 
 * Created: 2010-10-07 Guy K. Kloss <guy.kloss@aut.ac.nz>
 * Changed: 2012-12-06 Guy K. Kloss <guy.kloss@aut.ac.nz>
 * 
 * Version: $Id$
 * 
 * Copyright (C) 2012 Auckland University of Technology, New Zealand
 * 
 * Some rights reserved
 * 
 * http://www.aut.ac.nz/
 */
 
package au.org.arcs.griffin.filesystem.impl.gridfs;


/**
 * Constants and defaults for the MongoDB GridFS storage backend.
 *
 * @version $Revision: 1.1 $
 * @author Guy K. Kloss
 */
public class GridfsConstants {
    public static final String FILE_SEP = "/";
    public static final String DEFAULT_SERVER_HOST = "localhost";
    public static final int DEFAULT_SERVER_PORT = 27017;
    public static final String SERVER_TYPE = "gridfs";
    public static final String BUCKET_NAME = "fs";
}
