// Copyright (C) 2014 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.database;

import java.sql.Connection;

import jsaf.intf.system.ISession;

/**
 * An interface for a session that provides access to a database.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IDatabase extends ISession {
    /**
     * The IANA-assigned port number for MS SQL Server.
     */
    int MSSQL_PORT = 1433;

    /**
     * The default port number for an Oracle SQL*Net listener (NOTE: Not an IANA-assigned number).
     */
    int ORACLE_PORT = 1521;

    /**
     * Get a JDBC connection to the database.
     *
     * @since 1.0
     */
    Connection getConnection() throws Exception;
}
