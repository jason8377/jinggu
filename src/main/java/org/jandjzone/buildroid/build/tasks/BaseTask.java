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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.objs.ProjectInfo.TaskInfo;
import org.jandjzone.buildroid.util.CommonUtil;
import org.jandjzone.buildroid.util.Constants;
import org.jandjzone.buildroid.util.PropertiesConfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Base class of a task for all implementations.
 * @author Jason
 *
 */
public abstract class BaseTask {
	private static String ARGUMENT_BRACKET = "{}";
	
    protected ProjectInfo projectInfo;
    protected TaskInfo taskInfo;
	
    /**
     * Constructor of a task.
     * <p>projectInfo and taskInfo are mandatory
     * @param projectInfo
     * @param taskInfo
     */
	public BaseTask(ProjectInfo projectInfo, TaskInfo taskInfo) {
		this.projectInfo = projectInfo;
		this.taskInfo = taskInfo;
	}
	
	/**
	 * Start running a task. It is a wrapper of {@link #run()} by providing
	 * initialization and argument checking.
	 * @throws Exception
	 */
	public final void start() throws Exception {
		/**
		 * Note that task name is highlighted
		 * by make it inline(HTML5) h5 tag to occupy a single line.
		 */
		progressOutput("<h5 style='display:inline'>{}({})</h5>" ,taskInfo.getTask_name() ,getClass().getName());
		
		//If this task need validation of a project before proceeding.
		if(needProjectValidation()){
			projectValidation();
		}
		
		/**
		 * Arguments checking which is actually implemented by concrete tasks.
		 */
		List<String> missingArguments = checkArguments();
		if(missingArguments != null && missingArguments.size() >0 ){
			throw new Exception(listMissingArguments(missingArguments));
		}
		
		run();
		
		//Print a new line when task is finished.
		progressOutput();
	}
	
	/**
	 * Print an array of fields to a string separated by dot.
	 * @param missingArguments
	 * @return
	 */
	private String listMissingArguments(List<String> missingArguments){
		StringBuilder stringBuilder = new StringBuilder();
        if(missingArguments != null && missingArguments.size() >0 ){
        	stringBuilder.append("Missing or invalid arguments: ");
        	boolean firstOne = true;
			for(String argument:missingArguments){
				if(!firstOne){
					stringBuilder.append(" ,");
				}
				stringBuilder.append(argument);
				firstOne = false;
			}
		}
        return stringBuilder.toString();
	}
	
	/**
	 * Check if the project is a gradle one by:
	 * <ui>
	 * <li>The folder is not empty
	 * <li>The folder should contain a build.gralde file.
	 * </ui>
	 * @throws Exception
	 */
	private void projectValidation() throws Exception {
		File fileProject = new File(projectInfo.getProject_path());
		if(!fileProject.exists() || !fileProject.isDirectory()){
			throw new Exception("The project path doesn't exist in file system or is not a directory.");
		};
		
		File gradleFile = new File(projectInfo.getProject_path() + "/build.gradle");
		if(!gradleFile.exists() || !gradleFile.isFile()){
			throw new Exception("The project doesn't look like an Android project(build.gradle missing).");
		}
    	
    	//Check if local.properties exists
    	FileUtils.write(
    			new File(projectInfo.getProject_path()+"/local.properties")
    			,"sdk.dir=" + PropertiesConfig.ANDROID_SDK_DIR
    			,Constants.TEXT_ENCODING);
	}
	
	/**
	 * Print out logs to connected socket clients.
	 * @param output
	 * @param arguments
	 */
	protected void progressOutput(String output ,Object... arguments) {
		if(output == null)return;
		
		/**
		 * There should be a better way to replace {} with arguments,
		 * don't have enough time so just leave it for now.
		 */
		int argumentIndex = 0;
		while(output.indexOf(ARGUMENT_BRACKET) != -1){
			Object argument = null;
			if(arguments != null && argumentIndex<arguments.length){
				argument = arguments[argumentIndex];
			}
			output = StringUtils.replaceOnce(output, ARGUMENT_BRACKET, argument==null?"":argument.toString());
			
			argumentIndex ++;
		}
		
		projectInfo.getLastBuildRequest().appendOutput(output);
	}
	
	/**
	 * Print out a new line
	 */
	protected void progressOutput() {
		progressOutput(Constants.EMPTY_STRING);
	}
	
	/**
	 * Get custom JSON argument string for the task.
	 * <pre>
	 * {
        "task_name" : "Update code"
        ,"task_class" : "org.jandjzone.buildroid.build.tasks.TaskGitPuller"
        ,"arguments" : {
                  "working_directory" : "D:/temp/AndroidUITestingExample"
                  ,"git_url" : "https://github.com/jason8377/AndroidUITestingExample.git"
                  ,"private_key_path" : ""
                  ,"credential_user" : ""
                  ,"credential_password" : ""
             }
        }
        Note that the "arguments" JSON string will not be parsed by framework
        and will be passed to specific task because only task self can understand
        the structure of it.
	 * </pre> 
	 */
	protected String getArgumentsString() {
		JsonElement argumentElement = taskInfo.getArguments();
		return argumentElement==null?"":argumentElement.toString();
	}

	/**
	 * Parse custom argument to an Object for given type
	 * @param argumentClass
	 * @return
	 * @throws JsonSyntaxException
	 * @throws Exception
	 */
	protected <T> T parseArguments(Class<T> argumentClass) throws JsonSyntaxException,Exception {
		JsonElement argumentElement = taskInfo.getArguments();
		if(argumentElement == null){
			throw new Exception("Argument field is empty.");
		}
		String arguments = argumentElement.toString();
		
		//Replace all variables from UI(html form)
		arguments = projectInfo.getLastBuildRequest().replaceFormVariables(arguments);
		
		return CommonUtil.fromJson(arguments, argumentClass);
	}
	

	private String buildHistoryPath = CommonUtil.appendString(PropertiesConfig.REAL_PATH 
			,Constants.FILE_SEPERATOR
			,Constants.BUILD_HISTORY_FOLDER);
	
	protected String checkProjectLogPath(){
		File buildHistory = new File(buildHistoryPath);
		if(!buildHistory.exists())buildHistory.mkdir();

		String projectLogPath = CommonUtil.appendString(buildHistoryPath
				,Constants.FILE_SEPERATOR
				,projectInfo.getLastBuildRequest().getBuildSequence());
		File projectLog = new File(projectLogPath);
		if(!projectLog.exists())projectLog.mkdir();
		
		return projectLogPath;
	}
	
	/**
	 * <p>Whether or not the task needs project validation</p>
	 * <p>Most tasks should return true, the exception is the
	 * code puller like Git or SVN tasks as before the task is executed, the
	 * project path is empty.</p>
	 * <p>If true is return, the validation will do the following:</p>
	 * <pre>
	 *   The project directory exists or not in the file system.
	 *   If there is a build.gradle file in it.
	 *   If sdk.dir is set in server.properties which should point to the sdk home directory in the computer.
	 *   Update sdk.dir in local.properties file.
	 * </pre>
	 * @return
	 */
	protected boolean needProjectValidation(){
		return true;
	}
	
	/**
	 * 
	 * @return
	 * @throws JsonSyntaxException
	 * @throws Exception
	 */
    protected abstract List<String> checkArguments() throws JsonSyntaxException,Exception;
    
    /**
     * Run the task which implemented by concrete tasks.
     * @throws Exception
     */
	protected abstract void run() throws Exception;
}
