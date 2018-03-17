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

package org.jandjzone.buildroid.objs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.tooling.events.ProgressEvent;
import org.jandjzone.buildroid.util.CommonUtil;
import org.jandjzone.buildroid.util.Constants;
import org.jandjzone.buildroid.websocket.BuildWebSocketHandler;

/**
 * Build request class.
 * @author Jason
 *
 */
public class BuildRequest {
	private String project_id;
	//User who requests the build.
	private String user_name;
	private ArrayList<BuildParameter> build_parameters;
	
	
	public String getProject_id() {
		return project_id;
	}

	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	
	public static class BuildParameter{
		private String parameter_name;
		private String parameter_value;
		public String getParameter_name() {
			return parameter_name;
		}
		public void setParameter_name(String parameter_name) {
			this.parameter_name = parameter_name;
		}
		public String getParameter_value() {
			return parameter_value;
		}
		public void setParameter_value(String parameter_value) {
			this.parameter_value = parameter_value;
		}
		
		public BuildParameter starsSensitiveData(){
			BuildParameter buildParameter = new BuildParameter();
			buildParameter.setParameter_name(this.parameter_name);
			buildParameter.setParameter_value(CommonUtil.starsSensitiveData(this.parameter_value));
			return buildParameter;
		}
	}
	
	private String sdk_dir;
	private String buildToolsVersion;
	private List<String> apkOutputFile = new ArrayList<String>();
	
	private String buildSequence;
	private String startBuildTime;
	private long startBuildMillis;
	private String endBuildTime;
	private long endBuildMillis;
	private String buildOutputString;
	/**
	 * This linked list is for log tracking purpose.
	 */
	private LinkedList<ProgressEvent> buildLog = new LinkedList<ProgressEvent>();
	private StringBuffer buildOutput = new StringBuffer();
	
	public void buildStarting(){
		this.buildSequence = Constants.DATE_FORMAT_1.format(new java.util.Date())
				+ "_" + RandomStringUtils.randomNumeric(4);
		this.startBuildTime = Constants.DATE_FORMAT_3.format(new java.util.Date());
		this.startBuildMillis = System.currentTimeMillis();
	}
	public void buildEnding(){
		this.endBuildTime = Constants.DATE_FORMAT_3.format(new java.util.Date());
		this.endBuildMillis = System.currentTimeMillis();
	}
	
	public void appendOutput(ProgressEvent event) {
        if(event==null)return;
        buildLog.add(event);
        
        appendOutput(event.getDisplayName());
    }
	public void appendOutput(String output) {
        if(output==null)return;
        
        //Broadcast build message.
        BuildWebSocketHandler.broadCastBuildMessage(CommonUtil.toJson(new MessageBuildOutput(project_id,output)));
        
        buildOutput.append(output).append(System.lineSeparator());
        /**
         * Avoid converting to string every time getBuildLog() is called.
         */
        buildOutputString = buildOutput.toString();
    }
	
	public String getBuildLog(){
		return buildOutputString==null?"":buildOutputString;
	}

	public boolean validRequest() {
		if(project_id == null || user_name == null)return false;
		
		return true;
	}

	public ArrayList<BuildParameter> getBuild_parameters() {
		return build_parameters;
	}
	
	/**
	 * Replace a string by variables from HTML form
	 * <variable name>
	 * @param toBeReplaced
	 * @return
	 */
	public String replaceFormVariables(String toBeReplaced){
		if(toBeReplaced == null || build_parameters == null)return toBeReplaced;
		
		for(BuildParameter buildParameter:build_parameters){
			if(StringUtils.isBlank(buildParameter.getParameter_name()))continue;
			if(buildParameter.getParameter_value() == null)continue;
			String variableName = "<"+buildParameter.getParameter_name() + ">";
			if(toBeReplaced.indexOf(variableName) == -1)continue;
			toBeReplaced = StringUtils.replaceAll(toBeReplaced, variableName, buildParameter.getParameter_value());
		}
		
		return toBeReplaced;
	}
	
	public String getParameter(String paraName) {
		if(build_parameters == null || paraName == null)return null;
		
		for(BuildParameter para:build_parameters){
			if(para.getParameter_name()==null || para.getParameter_value() == null)continue;
			if(para.getParameter_name().equals(paraName)){
				return para.getParameter_value();
			}
		}
		return null;
	}

	public void setBuild_parameters(ArrayList<BuildParameter> build_parameters) {
		this.build_parameters = build_parameters;
	}
	
	/**
	 * Trim parameter name and value to avoid confusion.
	 */
	public void trimParameters(){
		if(build_parameters != null){
		    for(BuildParameter para:build_parameters){
			    para.setParameter_name(StringUtils.trim(para.getParameter_name()));
			    para.setParameter_value(StringUtils.trim(para.getParameter_value()));
		    }
		}
	}

	public String getBuildSequence() {
		return buildSequence;
	}
	public String getStartBuildTime() {
		return startBuildTime;
	}
	public long getStartBuildMillis() {
		return startBuildMillis;
	}
	public String getEndBuildTime() {
		return endBuildTime;
	}
	public long getEndBuildMillis() {
		return endBuildMillis;
	}
	public String getJsonParameters() {
		return getJsonParameters(false);
	}
	public String getJsonParameters(boolean starsSensitiveData) {
		if(build_parameters == null || build_parameters.size() ==0)return null;
		
		ArrayList<BuildParameter> parameterList = build_parameters;
		if(starsSensitiveData){
			parameterList = new ArrayList<BuildParameter>();
			for(BuildParameter buildParameter:build_parameters){
				parameterList.add(buildParameter.starsSensitiveData());
			}
		}
		return CommonUtil.toJson(parameterList);
	}

	public String getSdk_dir() {
		return sdk_dir;
	}

	public void setSdk_dir(String sdk_dir) {
		this.sdk_dir = sdk_dir;
	}

	public String getBuildToolsVersion() {
		return buildToolsVersion;
	}

	public void setBuildToolsVersion(String buildToolsVersion) {
		this.buildToolsVersion = buildToolsVersion;
	}

	public List<String> getApkOutputFile() {
		return apkOutputFile;
	}

	public void setApkOutputFile(List<String> apkOutputFile) {
		this.apkOutputFile = apkOutputFile;
	}
}
