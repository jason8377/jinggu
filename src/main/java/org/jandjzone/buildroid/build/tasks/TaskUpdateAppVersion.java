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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.objs.ProjectInfo.TaskInfo;
import org.jandjzone.buildroid.util.CommonUtil;
import org.jandjzone.buildroid.util.Constants;

/**
 * Updates fields in build.gradle under [app] module before building.
 * There should be a better way to update fields in DSL file, just leave it for now for contributors.
 * <p>
 * Fields are matched according regular expressions.
 * <ul>
 * <li>Field versionCode uses expression in {@link #isLineVersionCode}.
 * <li>Field versionName uses expression in {@link #isLineVersionName}.
 * </ul>
 * <p>
 * Note that you can hard code values you are going to update or have
 * them passed at runtime from UI forms by calling {@link #getFormParameter()}.
 * 
 * @author Jason
 *
 */
public class TaskUpdateAppVersion extends BaseTask {
	private static final String para_versionCode = "versionCode";
	private String versionCode;
	
	private static final String para_versionName = "versionName";
	private String versionName;
	
	public TaskUpdateAppVersion(ProjectInfo projectInfo, TaskInfo taskInfo) {
		super(projectInfo ,taskInfo);
	}

	/**
	 * Check if mandatory parameters are missing
	 * @return
	 */
	@Override
	protected List<String> checkArguments() {
		List<String> missingArguments = new ArrayList<String>();
		
		versionCode = projectInfo.getFormParameter(para_versionCode);
		if(StringUtils.isBlank(versionCode)){
			missingArguments.add(para_versionCode);
		}
		
		versionName = projectInfo.getFormParameter(para_versionName);
		if(StringUtils.isBlank(versionName)){
			missingArguments.add(para_versionName);
		}
		
		return missingArguments;
	}
	
	/**
	 * Run you command line task here
	 */
	@Override
	protected void run() throws Exception {
		String gradlePath = CommonUtil.appendFilePath(projectInfo.getProject_path()
				,projectInfo.getProject_module() 
				,Constants.GRADLE_FILE_NAME);
		File destFile = new File(gradlePath);
		
		StringBuffer fileText = new StringBuffer();
		List<String> lineList = FileUtils.readLines(destFile ,Constants.TEXT_ENCODING);
		if(lineList != null){
			for(String line:lineList){
				if(isLineVersionCode(line)){
					fileText.append("        versionCode ")
					.append(versionCode)
					.append(System.lineSeparator());
					
					//progress update
					progressOutput("versionCode changed: {}", versionCode);
				}else if(isLineVersionName(line)){
					fileText.append("        versionName ")
					.append("\"")
					.append(versionName)
					.append("\"")
					.append(System.lineSeparator());
					
					//progress update
					progressOutput("versionName changed: {}", versionName);
				}else{
				    fileText.append(line).append(System.lineSeparator());
				}
			}
		}
		FileUtils.write(destFile, fileText.toString(), Constants.TEXT_ENCODING);
	}
	
	/**
	 * Check if the line matches 'versionCode 1' pattern.
	 * @param line
	 * @return
	 */
	private static boolean isLineVersionCode(String line) {
    	if(line==null)return false;
    	line = line.trim();
    	if(line.length()==0)return false;
    	
    	StringBuffer sbExpress = new StringBuffer();
    	sbExpress.append("(\\p{Blank})*")       //blank or tab
    	         .append("versionCode")         //versionCode
    	         .append("(\\p{Blank})+")       //blank or tab
    	         .append("(\\d){1,}")           //any digital
    	         .append(".*")                  //any character
    	;
    	Pattern pattern = Pattern.compile(sbExpress.toString());
    	Matcher matcher = pattern.matcher(line);
    	return matcher.matches();
    }
	/**
	 * Check if the line matches 'versionName "1.0"' pattern.
	 * @param line
	 * @return
	 */
	private static boolean isLineVersionName(String line) {
    	if(line==null)return false;
    	line = line.trim();
    	if(line.length()==0)return false;
    	
    	StringBuffer sbExpress = new StringBuffer();
    	sbExpress.append("(\\p{Blank})*")       //blank or tab
    	         .append("versionName")       //versionName
    	         .append("(\\p{Blank})+")       //blank or tab
    	         .append("\"")                  //"
    	         .append(".*")                  //any character
    	         .append("\"")                  //"
    	         .append(".*")                  //any character
    	;
    	
    	Pattern pattern = Pattern.compile(sbExpress.toString());
    	Matcher matcher = pattern.matcher(line);
    	return matcher.matches();
    }
}
