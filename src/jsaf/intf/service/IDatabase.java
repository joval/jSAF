// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.service;

import java.sql.Connection;
import java.sql.SQLException;

import jsaf.intf.identity.ICredential;

/**
 * A session service interface for interacting with a relational database.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IDatabase {
    /**
     * An enumeration of relational ratabase engines.
     */
    enum Engine {
	MSSQL(1433),
	ORACLE(1521);

	private int port;

	private Engine(int port) {
	    this.port = port;
	}

	public int getDefaultPort() {
	    return port;
	}
    }

    /**
     * Get a JDBC connection to the specified database.
     *
     * @param name Corresponds to the desired Oracle Service Name or MS SQL database instance name.
     * @param cred The database login credential.
     *
     * @since 1.0
     */
    Connection getConnection(String name, ICredential cred) throws SQLException;
}
