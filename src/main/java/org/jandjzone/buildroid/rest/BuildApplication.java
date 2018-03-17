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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.jandjzone.buildroid.build.ProjectUtilities;
import org.jandjzone.buildroid.util.BoneCPUtil;
import org.jandjzone.buildroid.util.PropertiesConfig;

/**
 * Jersey Application class.
 * @author Jason
 *
 */
@ApplicationPath("/rest")
public class BuildApplication extends Application {
	static{
		PropertiesConfig.initConfiguration();
		BoneCPUtil.inializeDatabaseConnection();
		ProjectUtilities.loadProjectConfigurations();
	}
	@Context ServletContext servletContext;
	
    @Override
    public Set<Class<?>> getClasses() {
    	/**
    	 * Get real path.
    	 */
    	if(PropertiesConfig.REAL_PATH == null) {
    	    PropertiesConfig.REAL_PATH = servletContext.getRealPath("");
    	    //Make build history directory if it doesn't exist.
    	    File historyFolder = new File(PropertiesConfig.REAL_PATH + "/build_history");
			if(!historyFolder.exists())historyFolder.mkdir();
    	}
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register root resource
        classes.add(BuildResource.class);
        return classes;
    }
}
