// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.util.Map;

/**
 * Interface to Windows license data.
 *
 * @see <a href="http://www.geoffchappell.com/viewer.htm?doc=studies/windows/km/ntoskrnl/api/ex/slmem/productpolicy.htm">Storage of License Values</a>
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public interface ILicenseData {
    public interface IEntry {
	int TYPE_SZ	= 1;
	int TYPE_BINARY	= 2;
	int TYPE_DWORD	= 4;

	int getType();

	String getName();

	String toString();
    }

    public interface IBinaryEntry extends IEntry {
	byte[] getData();
    }

    public interface IDwordEntry extends IEntry {
	int getData();
    }

    public interface IStringEntry extends IEntry {
	String getData();
    }

    Map<String, IEntry> getEntries();
}
