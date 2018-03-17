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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jandjzone.buildroid.build.GradleOutputListener;
import org.jandjzone.buildroid.util.CommonUtil;

/**
 * Executor implementation of Command line.
 * <pre>
 * The output of a gradle command may be predictable by checking whether it
 * contains "BUILD FAILED" or "BUILD SUCCESSFUL".
 * However for other command line like "zipalign","apksigner" it is difficult.
 * You can implement the checking by providing a specific failing message.
 * See {@link #CommandLineExecuter(String,String,String[],GradleOutputListener)} 
 * <pre>
 * 
 * @author Jason
 *
 */
public class CommandLineExecuter extends BaseExecuter {
	
	private final String FAILURE_STRING_MATCH = "BUILD FAILED in ";
	
	private String commandPath;
	private String fail_string_match = FAILURE_STRING_MATCH;
	
	/**
	 * Constructor of a command
	 * @param commandPath
	 * @param arguments
	 * @param outputListener
	 */
	public CommandLineExecuter(String commandPath
			,String[] arguments
			,GradleOutputListener outputListener){
		super(arguments,outputListener);
		
		this.commandPath = commandPath;
	}
	/**
	 * Constructor of a command with specific fail message
	 * @param commandPath
	 * @param arguments
	 * @param outputListener
	 */
	public CommandLineExecuter(String commandPath
			,String fail_string_match
			,String[] arguments
			,GradleOutputListener outputListener){
		super(arguments,outputListener);
		
		this.fail_string_match = fail_string_match;
		if(StringUtils.isBlank(fail_string_match)){
			this.fail_string_match = FAILURE_STRING_MATCH;
		}
		this.commandPath = commandPath;
	}

	@Override
	protected void run() throws Exception {
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
            	if(line.contains(fail_string_match))anyFailure = true;
            }
            if(anyFailure){
            	throw new Exception(line);
            }
        } catch(Exception e) {
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
