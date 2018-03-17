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
package org.jandjzone.buildroid.spring_mvc.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jandjzone.buildroid.build.ProjectUtilities;
import org.jandjzone.buildroid.build.tasks.TaskSaveBuildHistory;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.spring_mvc.controller.BuildHistory.BuildAttachment;
import org.jandjzone.buildroid.util.CommonUtil;
import org.jandjzone.buildroid.util.Constants;
import org.jandjzone.buildroid.util.PropertiesConfig;
import org.jandjzone.buildroid.util.SqliteUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for build history.
 * @author Jason
 *
 */
@Controller
public class BuildHistoryController {
	@RequestMapping(value="/history",method=RequestMethod.GET)
	public String buildHistory(HttpServletRequest request
			,Model model
			,@RequestParam(required=false) String project_id) {
		
		Connection connection = null;
		PreparedStatement pstmt  = null;
        ResultSet rs = null;
        
        project_id = StringUtils.trimToEmpty(project_id);
		try {
			connection = SqliteUtil.getConnection();
		    if(connection==null){
		    	throw new Exception("No database connection.");
		    }
		    
		    List<BuildHistory> historyList = new ArrayList<BuildHistory>();
		    
		    pstmt = connection.prepareStatement(TaskSaveBuildHistory.QUERY_HISTORY);
		    pstmt.setString(1, "%" + project_id + "%");
		    rs = pstmt.executeQuery();
		    
		    
	        
		    while (rs.next()) {
	        	BuildHistory buildHistory = new BuildHistory();
	        	buildHistory.setBuild_sequence(rs.getString("build_sequence"));
	        	buildHistory.setProject_id(rs.getString("project_id"));
	        	buildHistory.setUser(rs.getString("user"));
	        	buildHistory.setStart_time(rs.getString("start_time"));
	        	buildHistory.setEnd_time(rs.getString("end_time"));
	        	buildHistory.setDuration(rs.getInt("duration"));
	        	buildHistory.setBuild_parameters(rs.getString("build_parameters"));
	        	
	        	//Project path in webapp
	        	String projectLinkPath = CommonUtil.appendString(Constants.BUILD_HISTORY_FOLDER , "/" 
        				, buildHistory.getBuild_sequence() , "/");
	        	buildHistory.setBuild_log_path(projectLinkPath);
	        	
	        	//Project log file path
	        	String projectLogPath = CommonUtil.appendString(PropertiesConfig.REAL_PATH
	        			,Constants.FILE_SEPERATOR , Constants.BUILD_HISTORY_FOLDER
	        			,Constants.FILE_SEPERATOR , buildHistory.getBuild_sequence());
	        	
	        	//Build log
	        	File buildLog = new File(projectLogPath + Constants.FILE_SEPERATOR + Constants.BUILD_LOG_FILE);
	        	if(buildLog.exists()){
	        		buildHistory.setBuild_log(Constants.BUILD_LOG_FILE);
	        	}

	        	//APK list
	        	Collection<File> apkFileCollection = CommonUtil.listFiles(projectLogPath
	        			,Constants.APK_FILE_FILTER ,null);
	        	if(apkFileCollection != null && !apkFileCollection.isEmpty()){
		        	List<BuildAttachment> apkList = new ArrayList<BuildAttachment>();
		        	Iterator<File> fileIterator = apkFileCollection.iterator();
		        	while(fileIterator.hasNext()){
		        		File apkFile = fileIterator.next();
		        		apkList.add(new BuildAttachment(apkFile.getName(),apkFile.getName()));
		        	}
	        	    buildHistory.setApk_list(apkList);
	        	}
	        	
	        	//Unit test reports list
	        	String unitTestPath = projectLogPath + Constants.FILE_SEPERATOR + Constants.TEST_FOLDER;
	        	Collection<File> testReportCollection = CommonUtil.listFiles(unitTestPath
	        			,Constants.TEST_REPORT_FILE_FILTER,Constants.DIR_FILTER);
	        	if(testReportCollection != null && !testReportCollection.isEmpty()){
		        	List<BuildAttachment> fileList = new ArrayList<BuildAttachment>();
		        	Iterator<File> fileIterator = testReportCollection.iterator();
		        	while(fileIterator.hasNext()){
		        		File reportFile = fileIterator.next();
		        		File reportFileParent = reportFile.getParentFile();
		        		String webPath = getFilePath(reportFile,Constants.TEST_FOLDER);
		        		String displayName = reportFileParent==null?reportFile.getName():reportFileParent.getName();
		        		fileList.add(new BuildAttachment(webPath,displayName));
		        	}
	        	    buildHistory.setTest_result(fileList);
	        	}
	        	
	        	//UI test reports list
	        	String androidTestPath = projectLogPath + Constants.FILE_SEPERATOR + Constants.ANDROID_TEST_FOLDER;
	        	Collection<File> androidTestReportCollection = CommonUtil.listFiles(androidTestPath
	        			,Constants.TEST_REPORT_FILE_FILTER,Constants.DIR_FILTER);
	        	if(androidTestReportCollection != null && !androidTestReportCollection.isEmpty()){
		        	List<BuildAttachment> fileList = new ArrayList<BuildAttachment>();
		        	Iterator<File> fileIterator = androidTestReportCollection.iterator();
		        	while(fileIterator.hasNext()){
		        		File reportFile = fileIterator.next();
		        		File reportFileParent = reportFile.getParentFile();
		        		String webPath = getFilePath(reportFile,Constants.ANDROID_TEST_FOLDER);
		        		String displayName = reportFileParent==null?reportFile.getName():reportFileParent.getName();
		        		fileList.add(new BuildAttachment(webPath,displayName));
		        	}
	        	    buildHistory.setAndroidTest_result(fileList);
	        	}

	        	ProjectInfo projectInfo = ProjectUtilities.getProjectInfoById(buildHistory.getProject_id());
	        	if(projectInfo != null){
	        	    buildHistory.setProject_name(projectInfo.getProject_name());
	        	}
	        	
	        	historyList.add(buildHistory);
	        }
	        model.addAttribute("historyList",historyList);
	        
	        //Add common-use project lists.
		    List<ProjectInfo> projectList = ProjectUtilities.getProjectList();
		    model.addAttribute("projectList",projectList);
		    ProjectInfo selectedProjectInfo = ProjectUtilities.getProjectInfoById(project_id);
		    if(selectedProjectInfo != null) {
		        model.addAttribute("project_id_selected",selectedProjectInfo.getProject_id());
		        model.addAttribute("project_name_selected",selectedProjectInfo.getProject_name());
		    }else{
		    	model.addAttribute("project_name_selected","Choose a project");
		    }
		}catch(SQLException e){
			e.printStackTrace();
			model.addAttribute("errorDescription",e.toString());
			return "error";
		}catch(Exception e){
			e.printStackTrace();
			model.addAttribute("errorDescription",e.toString());
			return "error";
		}finally{
			try{
	    		if(rs!=null)rs.close();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
			try{
	    		if(pstmt!=null)pstmt.close();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
			try{
	    		if(connection!=null)connection.close();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
		}
		
		return "build_history";
	}
	
	
	private static String getFilePath(File file, String untilFileName){
		if(!file.exists())return null;
		
		StringBuffer filePathBuilder = new StringBuffer();
		File parentFile = file; 
		while(parentFile != null){
			if(filePathBuilder.length()>0){
				filePathBuilder.insert(0,"/");
			}
			filePathBuilder.insert(0,parentFile.getName());
			if(untilFileName!= null && parentFile.getName().equals(untilFileName)){
			    break;
			}
			parentFile = parentFile.getParentFile();
		}
		
		return filePathBuilder.toString();
	}
}
