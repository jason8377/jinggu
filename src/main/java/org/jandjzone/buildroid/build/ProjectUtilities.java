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

package org.jandjzone.buildroid.build;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Project configurations in Projects.json locates in WEB-INF/classes
 * will be loaded after APP is launched.
 * @author Jason
 *
 */
public class ProjectUtilities {
	private static final String CONFIGURATION_FILE_NAME = "Projects.json";
	private static final Logger logger = LoggerFactory.getLogger(ProjectUtilities.class);
	
	/**
	 * ArrayList which saves all project configurations in Projects.json located in any CLASSPATH directory.
	 * Once configuration changed, webserver should restart.
	 */
	private static List<ProjectInfo> projectList = new ArrayList<>();
	
	/**
	 * Load project configuration.
	 */
	public static synchronized void loadProjectConfigurations(){
		//Clear the list.
		projectList.clear();
		
		InputStream stream = null;
		BufferedReader reader = null;
		try {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIGURATION_FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(stream, Constants.TEXT_ENCODING));
            String line;
            StringBuilder stringBuilder = new StringBuilder(); 
            while ((line = reader.readLine()) != null) {
            	stringBuilder.append(line);
            }
            Gson gson = new Gson();
            projectList = gson.fromJson(stringBuilder.toString(), new TypeToken<ArrayList<ProjectInfo>>(){}.getType()); 
            stringBuilder = null;
            logger.info("There are {} project(s) in {}", projectList.size(), CONFIGURATION_FILE_NAME);
		} catch (Exception e){
			logger.error(e.toString());
			e.printStackTrace();
		} finally {
			if(reader != null){
				try{
				    reader.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(stream != null){
				try{
					stream.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Get project info by id.
	 * @param project_id
	 * @return
	 */
	public static ProjectInfo getProjectInfoById(String project_id){
		if(project_id == null)return null;
		
		if(projectList != null){
			for(ProjectInfo projectInfo:projectList) {
				if(projectInfo.getProject_id() == null)continue;
				if(projectInfo.getProject_id().equals(project_id))return projectInfo;
			}
		}
		return null;
	}
	
	/**
	 * Get all projects.
	 * @return
	 */
	public static List<ProjectInfo> getProjectList() {
		return projectList == null?new ArrayList<>():projectList;
	}
}
