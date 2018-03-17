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

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.jandjzone.buildroid.build.ProjectUtilities;
import org.jandjzone.buildroid.objs.BuildRequest;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.util.PropertiesConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for build.
 * @author Jason
 *
 */
@Controller
public class BuildController {
	/**
	 * Build without project_id path.
	 * @param model
	 * @return
	 */
	@RequestMapping(value={"","/"},method=RequestMethod.GET)
	public String build(Model model) {
		List<ProjectInfo> projectList = ProjectUtilities.getProjectList();
		if(projectList.size() > 0){
			return "redirect:" + projectList.get(0).getProject_id();
		}else{
			return "index";
		}
	}
	/**
	 * Build with project_id path.
	 * Will redirect to the first project if it doesn't exist and there is at least one project. 
	 * @param model
	 * @param project_id
	 * @return
	 */
	@RequestMapping(value="/{project_id}",method=RequestMethod.GET)
	public String build(HttpServletRequest request
			,Model model
			, @PathVariable("project_id") String project_id) {
		initBaseUrl(request);
		
	    //Add common-use project lists.
	    List<ProjectInfo> projectList = ProjectUtilities.getProjectList();
	    model.addAttribute("projectList",projectList);
	    model.addAttribute("restUrl",PropertiesConfig.REST_URL);
	    model.addAttribute("websocketUrl",PropertiesConfig.WEBSOCKET_URL);
	    
	    //Add selected project_id
	    ProjectInfo selectedProjectInfo = ProjectUtilities.getProjectInfoById(project_id);
	    if(selectedProjectInfo != null) {
	        model.addAttribute("project_id_selected",selectedProjectInfo.getProject_id());
		    //model.addAttribute("projectLocked",selectedProjectInfo.isLocked()?"disabled":"");
	        //model.addAttribute("projectLocked",isBuildLocked()?"disabled":"");
	        model.addAttribute("isBuilding",isBuildLocked()?"true":"false");
		    model.addAttribute("form_view",selectedProjectInfo.getForm_view());
	        BuildRequest buildRequest = selectedProjectInfo.getLastBuildRequest();
	        if(buildRequest != null) {
	            model.addAttribute("build_log",buildRequest.getBuildLog());
	        }
	    } else {
	    	if(projectList.size() > 0){
	    	    return "redirect:" + projectList.get(0).getProject_id();
	    	}
	    }
	      
	    return "index";
    }
	
	/**
	 * If any project is building
	 * @return
	 */
	private boolean isBuildLocked(){
		List<ProjectInfo> projectList = ProjectUtilities.getProjectList();
		if(projectList != null && projectList.size() >0){
			for(ProjectInfo projectInfo:projectList){
				if(projectInfo.isLocked())return true;
			}
		}
		return false;
	}
	
	/**
	 * Generate base url according http request
	 * @param request
	 */
	private static void initBaseUrl( HttpServletRequest request ) {
		if(PropertiesConfig.WEBSOCKET_URL != null
				&& PropertiesConfig.REST_URL != null)return;
		
		StringBuilder urlBuilder = new StringBuilder();
		if (request.getServerPort() == 443){
			urlBuilder.append("wss://");
		}else{
			urlBuilder.append("ws://");
		}
		urlBuilder.append(request.getServerName())
		          .append(":").append(request.getServerPort())
		          .append(request.getContextPath()).append("/ws");
		PropertiesConfig.WEBSOCKET_URL = urlBuilder.toString();
		
		urlBuilder.setLength(0);
		urlBuilder.append(request.getScheme()).append("://").append(request.getServerName())
        .append(":").append(request.getServerPort())
        .append(request.getContextPath()).append("/rest");
        PropertiesConfig.REST_URL = urlBuilder.toString();
        
        urlBuilder = null;
	}
}
