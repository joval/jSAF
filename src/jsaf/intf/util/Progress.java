// Copyright (C) 2020 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.util;

/**
 * An IProducer/IObserver message enumeration for conveying progress updates for long-running operations.
 *
 * @since 1.6.3
 * @author David A. Solin
 * @version %I% %G%
 */
public enum Progress {
    /**
     * Notification indicating the beginning of an update message sequence. The argument is a String representing the artifact being updated.
     */
    START,

    /**
     * Notification indicating the percentage completion has incremented by one.  Argument is a Progress.Update showing percentage complete and
     * number of bytes read so far.
     */
    UPDATE,

    /**
     * Notification signifying the end of the last update message sequence. The argument is null.
     */
    FINISH;

    /**
     * Bean for containing an UPDATE message.
     */
    public static class Update {
	private short percent;
	private long bytesRead;

	public Update(short percent, long bytesRead) {
	    this.percent = percent;
	    this.bytesRead = bytesRead;
	}

	public short getPercent() {
	    return percent;
	}

	public long getBytesRead() {
	    return bytesRead;
	}
    }
}
