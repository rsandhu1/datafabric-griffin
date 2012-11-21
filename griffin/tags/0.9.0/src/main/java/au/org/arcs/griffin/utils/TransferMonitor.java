/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */

package au.org.arcs.griffin.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;

/**
 * Controls the upload/download bandwidth.
 * 
 * @author Behnke
 */
public class TransferMonitor {

	private static Log          log                 = LogFactory.getLog(TransferMonitor.class);
    private static final int SLEEP_INTERVAL = 100;

    private double           maxRate;

    private long             startTime;

    private long             transferredBytes;
    
    private AbstractFtpCmd cmd;
    
    private static final int PERF_MARKER_INTERVAL = 5000;
    
    private long lastPerfMarkerTime;
    
    private DecimalFormat decFormatter = new DecimalFormat("###.0");

	private boolean showPerfMarker;
	
	private Map<Long, Long> rangeMarkers;

    /**
     * Constructor.
     */
    public TransferMonitor() {
        this(-1);
    }

    /**
     * Constructor.
     * 
     * @param maxRate KB per second.
     */
    public TransferMonitor(double maxRate) {
    	this.maxRate = maxRate;
    	this.rangeMarkers = new HashMap<Long, Long>();
    }

    /**
     * Initializes the object.
     * 
     * @param maxRate The maximum transfer rate.
     */
    public void init(double maxRate, AbstractFtpCmd cmd) {
        this.maxRate = maxRate;
        startTime = System.currentTimeMillis();
        transferredBytes = 0;
        this.cmd=cmd;
    }
    
    public void sendPerfMarker(){
    	if (!showPerfMarker) return;
        lastPerfMarkerTime=System.currentTimeMillis();
    	StringBuffer perf=new StringBuffer("112-Perf Marker\r\n");
    	perf.append(" Timestamp:  "+decFormatter.format(lastPerfMarkerTime/1000d)+"\r\n");
    	perf.append(" Stripe Index: 0\r\n");
    	perf.append(" Stripe Bytes Transferred: "+transferredBytes+"\r\n");
    	perf.append(" Total Stripe Count: 1\r\n");
    	perf.append("112 End.");
    	cmd.out(perf.toString());
    	log.debug("transferredBytes:"+transferredBytes);
    	if (transferredBytes>0) {
    		printRangeMarker(cmd);
    	}
    }
    
    public void printRangeMarker(AbstractFtpCmd cmd){
		StringBuffer range=new StringBuffer("111 Range Marker ");
//		log.debug("b4 rangeMarkers:"+rangeMarkers);
//		copy=Collections.synchronizedMap(new HashMap<Long, Long>(rangeMarkers));
//		this.rangeMarkers.clear();
		Map<Long, Long> copy=makeCopy();
//		log.debug("after rangeMarkers:"+rangeMarkers);
//		log.debug("copy:"+copy);
		Set<Long> keys=copy.keySet();
		List<Long> starts=new ArrayList<Long>(keys);
		if (starts.size()==0) return;
		Collections.sort(starts);
		long end=-2;
		for (long start:starts){
			if (start>end) {
				if (end>0) range.append(end).append(",");
				range.append(start).append("-");
			}
			end=copy.get(start);
		}
		range.append(end);
//		range.append("\r\n");
		cmd.out(range.toString());
    }
    
    synchronized Map<Long, Long> makeCopy(){
    	Map<Long, Long> copy=new HashMap<Long, Long>(rangeMarkers);
		this.rangeMarkers.clear();
		return copy;
    }
    
    synchronized void addItem(long start, long end){
    	rangeMarkers.put(start, end);
    }

    /**
     * Returns the current transfer rate.
     * 
     * @return The transfer rate in KB per seconds.
     */
    public double getCurrentTransferRate() {
        double seconds = (System.currentTimeMillis() - startTime) / 1024d;
        if (seconds <= 0) {
            seconds = 1;
        }
        return (transferredBytes / 1024d) / seconds;

    }

    /**
     * Updates the transfer rate statistics. If the maximum rate has been exceeded, the method
     * pauses.
     * 
     * @param byteCount The number of bytes previously transfered.
     */
    public void execute(long start, long byteCount) {
        transferredBytes += byteCount;
        log.debug("adding item:"+start+" "+(start+byteCount));
        if (showPerfMarker) addItem(start, start+byteCount);
        log.debug("added item:"+start+" "+(start+byteCount));
//		rangeMarkers.put(start, start+byteCount);
        if (System.currentTimeMillis()-lastPerfMarkerTime>PERF_MARKER_INTERVAL) sendPerfMarker();
//        while (maxRate >= 0 && getCurrentTransferRate() > maxRate) {
//            try {
//                Thread.sleep(SLEEP_INTERVAL);
//            } catch (InterruptedException e) {
//                break;
//            }
//        }
    }
    
    public long getTransferredBytes(){
    	return this.transferredBytes;
    }

	public void hidePerfMarker() {
		showPerfMarker = false;
		
	}

	public void showPerfMarker() {
		showPerfMarker = true;
		
	}

}
