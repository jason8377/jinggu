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

import java.lang.reflect.Constructor;

import org.apache.commons.lang3.StringUtils;
import org.jandjzone.buildroid.build.tasks.BaseTask;
import org.jandjzone.buildroid.build.tasks.TaskSaveBuildHistory;
import org.jandjzone.buildroid.objs.BuildRequest;
import org.jandjzone.buildroid.objs.MessageBuildEnd;
import org.jandjzone.buildroid.objs.MessageBuildStart;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.objs.ProjectInfo.TaskInfo;
import org.jandjzone.buildroid.util.CommonUtil;
import org.jandjzone.buildroid.util.PropertiesConfig;
import org.jandjzone.buildroid.websocket.BuildWebSocketHandler;

/**
 * Build utilities class.
 * @author Jason
 *
 */
public class BuildUtil {
	/**
	 * Entry of a build.
	 * @param buildRequest
	 */
	public static void build(BuildRequest buildRequest) {
		//Broadcast start building message to connected clients.
		BuildWebSocketHandler.broadCastBuildMessage(CommonUtil.toJson(new MessageBuildStart(buildRequest.getProject_id())));
				
		/**
		 * Not necessary to do null check here.
		 */
		ProjectInfo projectInfo = ProjectUtilities.getProjectInfoById(buildRequest.getProject_id());
		projectInfo.setLastBuildRequest(buildRequest);
		projectInfo.getLastBuildRequest().buildStarting();
		try{
			//Check if sdk.dir is set in local.properties.
	    	if(StringUtils.isBlank(PropertiesConfig.ANDROID_SDK_DIR)){
	    		throw new Exception("The sdk.dir is not set. Go to server.properties "
	    				+ "then add ANDROID_SDK_DIR pointing to your local SDK.");
	    	}
			/**
			 * Here only check if the project path is empty instead of 
			 * the existence of it in the file system because for the first time
			 * of the build, the project is not cloned yet from remote repository.   
			 */
			if(StringUtils.isBlank(projectInfo.getProject_path())){
				throw new Exception("Project path is empty.");
			}
			
			//Execute task one by one
			TaskInfo[] taskList = projectInfo.getTask_list();
			if(taskList == null || taskList.length ==0){
				throw new Exception("No tasks to be executed.");
			}
			for(TaskInfo taskInfo:taskList){
				try{
				    BaseTask baseTask = newTaskInstance(projectInfo,taskInfo);
				    baseTask.start();
				}catch(Exception e){
					//For task which fails will block the whole build process.
					if(taskInfo.isBlockProcess()){
						throw e;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			projectInfo.getLastBuildRequest().appendOutput(e.toString());
		}finally{
			projectInfo.getLastBuildRequest().buildEnding();
			//Save build history to database
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTask_name("Save Build Result");
			taskInfo.setTask_class(TaskSaveBuildHistory.class.getName());
			BaseTask baseTask = new TaskSaveBuildHistory(projectInfo,taskInfo);
			try {
				baseTask.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				projectInfo.getLastBuildRequest().appendOutput(e.toString());
			}
			
			//Broadcast end building message to connected clients.
        	BuildWebSocketHandler.broadCastBuildMessage(CommonUtil.toJson(new MessageBuildEnd(buildRequest.getProject_id())));
		}
	}
	
	/**
	 * Create an instance of a task for given project and task. 
	 * @param projectInfo
	 * @param taskInfo
	 * @return
	 * @throws Exception
	 */
	private static BaseTask newTaskInstance(ProjectInfo projectInfo, TaskInfo taskInfo) throws Exception {
		Class<?> clazz = Class.forName(taskInfo.getTask_class());
		Constructor<?> ctor = clazz.getConstructor(ProjectInfo.class, TaskInfo.class);
		Object objectTobeCreated = ctor.newInstance(new Object[] {projectInfo ,taskInfo});
		return (BaseTask)objectTobeCreated;
	}
	
}
