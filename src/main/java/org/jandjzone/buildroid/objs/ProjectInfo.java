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

import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;

/**
 * Project configuration class
 * Buildroid supports multiple projects with different configurations.
 * @author Jason
 *
 */
public class ProjectInfo {
	private static final String DEAULT_MODULE_NAME = "app";
	private static final String DEFAULT_FORM_VIEW = "forms/form_default.jsp";
	
	/**
	 * Noted by Jason
	 * Every project holds a lock which avoids the project being built concurrently. 
	 */
	private ReentrantLock buildLock = new ReentrantLock();
	public boolean isLocked(){
		return buildLock.isLocked();
	}
	public boolean tryLock(){
		return buildLock.tryLock();
	}
	public void unlock(){
		buildLock.unlock();
	}
	private BuildRequest lastBuildRequest;
	
	
	private String project_id;
	private String project_name;
	private String project_path;
	private String project_module;  //default is app
	private String form_view;
	private TaskInfo[] task_list;
	
	public String getProject_id() {
		return project_id;
	}
	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}
	public String getProject_name() {
		return project_name;
	}
	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}
	public BuildRequest getLastBuildRequest() {
		return lastBuildRequest;
	}
	public void setLastBuildRequest(BuildRequest lastBuildRequest) {
		this.lastBuildRequest = lastBuildRequest;
	}
	public String getProject_path() {
		return project_path;
	}
	public void setProject_path(String project_path) {
		this.project_path = project_path;
	}
	public TaskInfo[] getTask_list() {
		return task_list;
	}
	public void setTask_list(TaskInfo[] task_list) {
		this.task_list = task_list;
	}
	public String getForm_view() {
		if(StringUtils.isBlank(form_view)){
			return DEFAULT_FORM_VIEW;
		}else{
		    return form_view;
		}
	}
	public void setForm_view(String form_view) {
		this.form_view = form_view;
	}

	/**
	 * Get parameter from form (web UI)
	 * @param parameterName
	 * @return
	 */
	public String getFormParameter(String parameterName){
		return getLastBuildRequest().getParameter(parameterName);
	}

	public String getProject_module() {
		return StringUtils.isBlank(project_module)?DEAULT_MODULE_NAME:project_module;
	}
	public void setProject_module(String project_module) {
		this.project_module = project_module;
	}

	public static class TaskInfo {
	    private String task_name;
	    private String task_class;
	    /**
	     * <p>This property indicates whether or not block 
	     * whole process if it fails. For example, 
	     * unit or Ui testing, user want to proceed even testing fails.
	     * default is true.</p>
	     */
	    private boolean blockProcess = true;
	    private JsonElement arguments;

	    public String getTask_name () {
	        return task_name;
	    }
	    public void setTask_name (String task_name) {
	        this.task_name = task_name;
	    }
	    public String getTask_class () {
	        return task_class;
	    }
	    public void setTask_class (String task_class) {
	        this.task_class = task_class;
	    }
		public JsonElement getArguments() {
			return arguments;
		}
		public void setArguments(JsonElement arguments) {
			this.arguments = arguments;
		}
		public boolean isBlockProcess() {
			return blockProcess;
		}
		public void setBlockProcess(boolean blockProcess) {
			this.blockProcess = blockProcess;
		}
	}
}
