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

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Properties loader from server.properties.</p>
 * @author Jason
 *
 */
public class PropertiesConfig {
	private static final Logger logger = LoggerFactory.getLogger(PropertiesConfig.class);
	
	//Real path of the webapp locates.
	public static String REAL_PATH;
	public static String REST_URL;
	public static String WEBSOCKET_URL;
	
	//The following are Android SDK configurations
	public static String ANDROID_SDK_DIR;
	
	//The following are database configurations
	public static String DATABASE_JDBC_CLASSNAME;
	public static String DATABASE_JDBC_URL;
	public static String DATABASE_USER_NAME;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MIN_CONNECTION = 5;
	public static int DATABASE_MAX_CONNECTION = 10;
	
	/**
	 * Load system configurations
	 */
	public static synchronized void initConfiguration(){
		Properties properties = new Properties();
        try {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("server.properties");
            properties.load(stream);
            
            ANDROID_SDK_DIR = properties.getProperty("ANDROID_SDK_DIR");
            
            //database jdbc class name
			DATABASE_JDBC_CLASSNAME = properties.getProperty("DATABASE_JDBC_CLASSNAME");
			
			//database url
			DATABASE_JDBC_URL = properties.getProperty("DATABASE_JDBC_URL");
			
			//database user name
			DATABASE_USER_NAME = properties.getProperty("DATABASE_USER_NAME");
			//database password
			DATABASE_PASSWORD = properties.getProperty("DATABASE_PASSWORD");
			//database minimum connection
			DATABASE_MIN_CONNECTION = CommonUtil.parseToInt(properties.getProperty("DATABASE_MIN_CONNECTION"),DATABASE_MIN_CONNECTION);
			//database maximum connection
			DATABASE_MAX_CONNECTION = CommonUtil.parseToInt(properties.getProperty("DATABASE_MAX_CONNECTION"),DATABASE_MAX_CONNECTION);
			
			if(logger.isInfoEnabled()){
				logger.info("ANDROID_SDK_DIR {}" ,ANDROID_SDK_DIR);
				logger.info("DATABASE_JDBC_CLASSNAME {}" ,DATABASE_JDBC_CLASSNAME);
				logger.info("DATABASE_JDBC_URL {}" ,DATABASE_JDBC_URL);
				logger.info("DATABASE_USER_NAME {}" ,DATABASE_USER_NAME);
				logger.info("DATABASE_MIN_CONNECTION {}" ,DATABASE_MIN_CONNECTION);
				logger.info("DATABASE_MAX_CONNECTION {}" ,DATABASE_MAX_CONNECTION);
			}
			
			stream.close();
        }catch(Exception e){
        	logger.error("Failed to load configuration from server.properties {}" ,e.toString());
            e.printStackTrace();
        }
	}
}