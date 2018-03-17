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
package org.jandjzone.buildroid.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jandjzone.buildroid.build.BuildUtil;
import org.jandjzone.buildroid.build.ProjectUtilities;
import org.jandjzone.buildroid.objs.BuildRequest;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.util.CommonUtil;

/**
 * Android build rest services resource class.
 * @author Jason
 *
 */
@Path("/")
public class BuildResource {
	@GET
	@Path("/getProjectList")
	public Response getDeviceList(@Context HttpHeaders httpHeaders) {
		List<ProjectInfo> projectList = ProjectUtilities.getProjectList();
		return Response.status(200).entity(CommonUtil.toJson(projectList)).build();
	}
	
	@POST
	@Path("/build")
    @Consumes(MediaType.APPLICATION_JSON)
	public Response build(String json_string) throws Exception {
		BuildRequest buildRequest = CommonUtil.fromJsonNoException(json_string, BuildRequest.class);
		if(buildRequest == null || !buildRequest.validRequest()) {
			return Response.status(200).entity(CommonUtil.toJson(BaseResult.RESULT_INVALID_REQUEST_ERROR)).build();
		}

		ProjectInfo projectInfo = ProjectUtilities.getProjectInfoById(buildRequest.getProject_id());
		if(projectInfo == null) {
			return Response.status(200).entity(CommonUtil.toJson(BaseResult.RESULT_PROJECT_NOT_EXIST_ERROR)).build();
		}
		
		/**
		 * Trying to get the lock of this project, will notify user if fail to do so.
		 */
		if(!projectInfo.tryLock()) {
			return Response.status(200).entity(CommonUtil.toJson(BaseResult.RESULT_BUILDING_ONGOING_ERROR)).build();
		}
		
		try{
			buildRequest.trimParameters();
			BuildUtil.build(buildRequest);
			return Response.status(200).entity(CommonUtil.toJson(BaseResult.RESULT_SUCCESS)).build();
		} finally {
			//Remember unlock in case of any exception.
			projectInfo.unlock();
		}
	}
}
