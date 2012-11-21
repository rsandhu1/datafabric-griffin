/*
 * GridfsConstants.java
 * 
 * Constants and defaults for the MongoDB GridFS storage backend.
 * 
 * Created: 2010-10-07 Guy K. Kloss <g.kloss@massey.ac.nz>
 * Changed:
 * 
 * Version: $Id$
 * 
 * Copyright (C) 2010 Massey University, New Zealand
 * 
 * All rights reserved
 * 
 * http://www.massey.ac.nz/~gkloss/
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
    public static final int SERVER_PORT = 27017;
    public static final String SERVER_TYPE = "gridfs";
    public static final String BUCKET_NAME = "fs";
}
