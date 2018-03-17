/*******************************************************************************
 * Copyright (C) 2018 Jason Luo
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package org.jandjzone.buildroid.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * <p>This is just a simple implementation of sqlite.
 * Every time interacting with it, a new connection is created and
 * you are responsible to close the connection after using.</p>
 * 
 * <p>You are encouraged to use other database like mysql which
 * connection pool is supported for BoneCP by setting
 * properties in server.properties.</p>
 * 
 * @author Jason
 *
 */
public class SqliteUtil {
	private static String SQLITE_URL = null; 
	static{
		try{
			Class.forName("org.sqlite.JDBC");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public synchronized static Connection getConnection() {
		if(SQLITE_URL == null){
			SQLITE_URL = "jdbc:sqlite:"+PropertiesConfig.REAL_PATH+"/build_history/buildroid.db";
		}
        try {
        	Connection sqliteConn = DriverManager.getConnection(SQLITE_URL);
        	return sqliteConn;
        } catch (SQLException e) {
        	e.printStackTrace();
        	return null;
        }
    }
}
