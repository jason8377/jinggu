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

package org.jandjzone.buildroid.build.tasks;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.objs.ProjectInfo.TaskInfo;
import org.jandjzone.buildroid.util.CommonUtil;
import org.jandjzone.buildroid.util.Constants;
import org.jandjzone.buildroid.util.PropertiesConfig;
import org.jandjzone.buildroid.util.SqliteUtil;

/**
 * Save build history to sqlite with its database file locates in build_history directory.
 * <p>
 * The build file(txt) and APKs as well as test reports(html,js,css) will not be saved to 
 * embedded file-based database, instead they will be moved to build_history folder
 * for future references.
 * @author Jason
 *
 */
public class TaskSaveBuildHistory extends BaseTask {
	private static String CREATE_TABLE_STATEMENT;
	private static String INSERT_HISTORY;
	public static String QUERY_HISTORY;
	static{
		StringBuffer sqlBuffer = new StringBuffer();
		
		//Create table statement
		sqlBuffer.append("CREATE TABLE IF NOT EXISTS build_history (\n")
		.append(" id integer PRIMARY KEY,\n")
		.append(" build_sequence text,\n")
		.append(" project_id text NOT NULL,\n")
		.append(" user text,\n")
		.append(" start_time text,\n")
		.append(" end_time text,\n")
		.append(" duration integer NOT NULL,\n")
		.append(" build_parameters text\n")
		.append(");");
		CREATE_TABLE_STATEMENT = sqlBuffer.toString();
		
		sqlBuffer.setLength(0);
		
		sqlBuffer.append("insert into build_history(\r")
		.append(" build_sequence,\n")
		.append(" project_id,\n")
		.append(" user,\n")
		.append(" start_time,\n")
		.append(" end_time,\n")
		.append(" duration,\n")
		.append(" build_parameters\n")
		.append(") values(?,?,?,?,?,?,?)");
		INSERT_HISTORY = sqlBuffer.toString();
		
		sqlBuffer.setLength(0);
		
		sqlBuffer.append("select\r")
		.append("  build_sequence,\n")
		.append("  project_id,\n")
		.append("  user,\n")
		.append("  start_time,\n")
		.append("  end_time,\n")
		.append("  duration,\n")
		.append("  build_parameters\n")
		.append("from build_history where 1=1\n")
		.append("  and project_id like ?")
		.append("order by start_time desc");
		QUERY_HISTORY = sqlBuffer.toString();
		
		sqlBuffer = null;
	}
	
	public TaskSaveBuildHistory(ProjectInfo projectInfo, TaskInfo taskInfo) {
		super(projectInfo,taskInfo);
	}
	
	@Override
	protected List<String> checkArguments() {
		List<String> missingArguments = new ArrayList<String>();
		
		return missingArguments;
	}
	
	@Override
	protected void run() throws Exception {
		Connection connection = null;
		try {
			
			connection = SqliteUtil.getConnection();
		    if(connection==null){
		    	throw new Exception("No database connection.");
		    }
		    
		    createTableIfNotExist(connection);
		    insertBuildHistory(connection);
		    progressOutput("Build history has been saved.{}", System.lineSeparator());
		    

			//Append APK link to build log
			String projectLogPath = CommonUtil.appendFilePath(PropertiesConfig.REAL_PATH
        			,Constants.BUILD_HISTORY_FOLDER
        			,projectInfo.getLastBuildRequest().getBuildSequence());
        	Collection<File> apkFileCollection = CommonUtil.listFiles(projectLogPath
        			,Constants.APK_FILE_FILTER ,null);
        	if(apkFileCollection != null && !apkFileCollection.isEmpty()){
        		StringBuilder apkListBuilder = new StringBuilder();
	        	Iterator<File> fileIterator = apkFileCollection.iterator();
	        	while(fileIterator.hasNext()){
	        		File apkFile = fileIterator.next();
	        		apkListBuilder.append("<a href='javascript:download_apk(\"")
	        		.append(projectInfo.getLastBuildRequest().getBuildSequence())
	        		.append("\",\"")
	        		.append(apkFile.getName())
	        		.append("\")' style='display:block'>")
	        		.append(apkFile.getName())
	        		.append("</a>");
	        	}
	        	progressOutput(apkListBuilder.toString());
        	}
		    
		    //Save build log to file system
		    writeBuildLog();
		}catch(SQLException e){
			progressOutput(e.toString());
			e.printStackTrace();
		}catch(Exception e){
			progressOutput(e.toString());
			e.printStackTrace();
		}finally{
		    if(connection!=null)connection.close();
		}
	}
	
	private void writeBuildLog() throws IOException{
		String buildLogPath = checkProjectLogPath() + Constants.FILE_SEPERATOR + "build_log.txt";
		FileUtils.write(new File(buildLogPath), projectInfo.getLastBuildRequest().getBuildLog(), Constants.TEXT_ENCODING);
	}
	
	private void createTableIfNotExist(Connection connection) throws SQLException {
        Statement stmt = null;
		try{
			stmt = connection.createStatement();
	        stmt.execute(CREATE_TABLE_STATEMENT);
		}catch(SQLException e){
			throw e;
		}finally{
			if(stmt != null)stmt.close();
		}
	}
	
	private void insertBuildHistory(Connection conn) throws SQLException {
		PreparedStatement pstmt = null;
		try{
            pstmt = conn.prepareStatement(INSERT_HISTORY);
            pstmt.setString(1, projectInfo.getLastBuildRequest().getBuildSequence());
            pstmt.setString(2, projectInfo.getProject_id());
            pstmt.setString(3, projectInfo.getLastBuildRequest().getUser_name());
            pstmt.setString(4, projectInfo.getLastBuildRequest().getStartBuildTime());
            pstmt.setString(5, projectInfo.getLastBuildRequest().getEndBuildTime());
            long buildDuration = projectInfo.getLastBuildRequest().getEndBuildMillis()
            	    	- projectInfo.getLastBuildRequest().getStartBuildMillis();
            pstmt.setLong(6, TimeUnit.MILLISECONDS.toSeconds(buildDuration));
            pstmt.setString(7, projectInfo.getLastBuildRequest().getJsonParameters(true));
            pstmt.executeUpdate();
		}catch(SQLException e){
			throw e;
		}finally{
			if(pstmt != null)pstmt.close();
		}
    }
}
