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
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * BoneCP database connection pool utility class.
 * @author Jason
 *
 */
public class BoneCPUtil {
	private static final Logger logger = LoggerFactory.getLogger(BoneCPUtil.class);
	
	private static BoneCP connectionPool = null;
	
	/**
	 * Initialize database.
	 * <p>
	 * Will only be triggered when application is launched.
	 */
	public static void inializeDatabaseConnection(){
		if(StringUtils.isAllBlank(PropertiesConfig.DATABASE_JDBC_CLASSNAME
				,PropertiesConfig.DATABASE_JDBC_URL)){
			return;
		}
		try {
			Class.forName(PropertiesConfig.DATABASE_JDBC_CLASSNAME);
			
			logger.info("Found " + PropertiesConfig.DATABASE_JDBC_CLASSNAME + " in classpath.");
		} catch (Exception e) {
			logger.error("Failed to load " + PropertiesConfig.DATABASE_JDBC_CLASSNAME 
					+ ", make sure it is included in you classpath.");
			e.printStackTrace();
			return;
		}
		try {
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(PropertiesConfig.DATABASE_JDBC_URL);
			config.setUsername(PropertiesConfig.DATABASE_USER_NAME); 
			config.setPassword(PropertiesConfig.DATABASE_PASSWORD);
			config.setMinConnectionsPerPartition(PropertiesConfig.DATABASE_MIN_CONNECTION);
			config.setMaxConnectionsPerPartition(PropertiesConfig.DATABASE_MAX_CONNECTION);
			config.setDefaultAutoCommit(false);
			connectionPool = new BoneCP(config);
			
			logger.info("Database is initialized successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get database connection.
	 * @return
	 */
	public synchronized static Connection getConnection(){
		if(connectionPool!=null){
		    try {
		    	Connection connection = connectionPool.getConnection();
		    	
		    	if(logger.isInfoEnabled()){
		    		logger.info("Database connection is returned.");
		    	}
		    	
				return connection;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(logger.isWarnEnabled()){
    		logger.warn("Database null connection is returned, "
		            + "please check availability of database or increase connection pool.");
    	}
		return null;
	}
}
