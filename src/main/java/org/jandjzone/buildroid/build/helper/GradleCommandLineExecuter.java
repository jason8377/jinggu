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

package org.jandjzone.buildroid.build.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.jandjzone.buildroid.build.GradleOutputListener;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.util.CommonUtil;

/**
 * Implementation of gradlew.
 * (gradlew.bat for window, gradle for bash)
 * @author Jason
 *
 */
public class GradleCommandLineExecuter extends BaseExecuter {
	private final String FAILURE_STRING = "BUILD FAILED";
	private final String FAILURE_STRING_MATCH = "BUILD FAILED in ";
	
	protected ProjectInfo projectInfo;
	
	/**
	 * Constructor of a gradlew for a given project.
	 * @param projectInfo
	 * @param arguments
	 * @param outputListener
	 */
	public GradleCommandLineExecuter(ProjectInfo projectInfo
			,String[] arguments
			,GradleOutputListener outputListener){
		super(arguments,outputListener);
		
		this.projectInfo = projectInfo;
	}

	@Override
	protected void run() throws Exception {
        String commandPath = projectInfo.getProject_path() + "/gradlew";
        
		outputCallback("{}Start running command {} with arguments {}" , System.lineSeparator(),commandPath ,printArray(arguments));
		
		InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
        	List<String> commandList = new ArrayList<String>();
        	if(SystemUtils.IS_OS_WINDOWS){
        		/**
        		 * Note that if I added /bin/bash on Linux/Mac, it's not accepting arguments, don't know why
        		 */
        		//commandList.add(SystemUtils.IS_OS_WINDOWS?"cmd.exe":"/bin/bash");
        	    commandList.add("cmd.exe");
        	    //commandList.add(SystemUtils.IS_OS_WINDOWS?"/C":"-c");
        	    commandList.add("/C");
        	}
        	commandList.add(commandPath);
        	
        	for(String oneTask:arguments){
        		commandList.add(CommonUtil.unwrapSensitiveData(oneTask));
        	}
			
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
			
			Map<String, String> env = processBuilder.environment();
	        //set JAVA_OPTS to empty to dismiss options from servlet container like tomcat
	        env.put("JAVA_OPTS", "");
			
			processBuilder.directory(new File(projectInfo.getProject_path()));
			
			processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput();
			
            Process process = processBuilder.start();
            inputStream = process.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            boolean anyFailure = false;
            while ((line = bufferedReader.readLine()) != null) {
            	outputCallback(line);
            	if(line.contains(FAILURE_STRING_MATCH))anyFailure = true;
            }
            if(anyFailure){
            	throw new Exception(FAILURE_STRING);
            }
        } catch(Exception e) {
        	e.printStackTrace();
        	throw e;
        } finally {
        	try{
        		if(bufferedReader != null)bufferedReader.close();
        	}catch(Exception e){}
        	try{
        		if(inputStreamReader != null)inputStreamReader.close();
        	}catch(Exception e){}
        	try{
        		if(inputStream != null)inputStream.close();
        	}catch(Exception e){}
        }
	}
}
