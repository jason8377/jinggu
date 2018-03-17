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

import java.lang.reflect.Type;
import java.util.List;

import org.jandjzone.buildroid.objs.BuildRequest.BuildParameter;
import org.jandjzone.buildroid.util.CommonUtil;

import com.google.gson.reflect.TypeToken;

public class BuildHistory {
	private String id;
	private String build_sequence;
	private String project_id;
	private String project_name;
	private String user;
	private String start_time;
	private String end_time;
	private int duration;
	private String build_parameters;
	private String build_log_path;
	private String build_log;
	private List<BuildAttachment> apk_list;
	private List<BuildAttachment> test_result;
	private List<BuildAttachment> androidTest_result;
	
	private List<BuildParameter> parameterList;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBuild_sequence() {
		return build_sequence;
	}
	public void setBuild_sequence(String build_sequence) {
		this.build_sequence = build_sequence;
	}
	public String getProject_id() {
		return project_id;
	}
	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getBuild_parameters() {
		return build_parameters;
	}
	public void setBuild_parameters(String build_parameters) {
		this.build_parameters = build_parameters;
		
		try{
			Type type = new TypeToken<List<BuildParameter>>() {}.getType();
			setParameterList(CommonUtil.fromJson(build_parameters, type));
		}catch(Exception e){}
	}
	public String getProject_name() {
		return project_name;
	}
	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}
	public List<BuildParameter> getParameterList() {
		return parameterList;
	}
	public void setParameterList(List<BuildParameter> parameterList) {
		this.parameterList = parameterList;
	}
	public List<BuildAttachment> getApk_list() {
		return apk_list;
	}
	public void setApk_list(List<BuildAttachment> apk_list) {
		this.apk_list = apk_list;
	}
	public String getBuild_log() {
		return build_log;
	}
	public void setBuild_log(String build_log) {
		this.build_log = build_log;
	}
	public String getBuild_log_path() {
		return build_log_path;
	}
	public void setBuild_log_path(String build_log_path) {
		this.build_log_path = build_log_path;
	}
	public List<BuildAttachment> getTest_result() {
		return test_result;
	}
	public void setTest_result(List<BuildAttachment> test_result) {
		this.test_result = test_result;
	}
	public List<BuildAttachment> getAndroidTest_result() {
		return androidTest_result;
	}
	public void setAndroidTest_result(List<BuildAttachment> androidTest_result) {
		this.androidTest_result = androidTest_result;
	}
	
	
	public static class BuildAttachment {
		private String path;
		private String diaplayName;
		
		public BuildAttachment(String path ,String diaplayName){
			this.path = path;
			this.diaplayName = diaplayName;
		}
		
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public String getDiaplayName() {
			return diaplayName;
		}
		public void setDiaplayName(String diaplayName) {
			this.diaplayName = diaplayName;
		}
	}
}
