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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.jandjzone.buildroid.build.GradleOutputListener;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.util.CommonUtil;

/**
 * Implementation of Gradle tooling API
 * <pre>
 * This is another implementation of executing gradle by tooling API.
 * It is not widely used for some limitations(minimum gradle version..).
 * </pre>
 * 
 * @author Jason
 *
 */
public class GradleToolingExecuter extends BaseExecuter {
	protected ProjectInfo projectInfo;
	
	private GradleConnectionException failure;
	/**
	 * Constructor of tooling API
	 * @param projectInfo
	 * @param arguments
	 * @param outputListener
	 */
	public GradleToolingExecuter(ProjectInfo projectInfo
			,String[] arguments
			,GradleOutputListener outputListener){
		super(arguments,outputListener);
		this.projectInfo = projectInfo;
	}

	@Override
	protected void run() throws Exception {
		ProjectConnection connection = null;
		try {
        	File pathProject = new File(projectInfo.getProject_path());
        	
        	connection = GradleConnector.newConnector()
        			.forProjectDirectory(pathProject)
        			.connect();
            BuildLauncher build = connection.newBuild();
            
            build.forTasks(CommonUtil.unwrapSensitiveData(arguments));
            //Redirect the build output to custom OutputStream which will be redirected to connected sockets.
            build.setStandardOutput(new GradleOutputStream());
            
            build.run(new ResultHandler<Void>(){
            	public void onComplete(Void voi1){
            		//Do nothing
            	}
            	public void onFailure(GradleConnectionException failureInner){
            		failure = failureInner;
            	}
            });
            //The run of gradle is synchronous so it is safe to check the failure here.
            if(failure != null)throw failure;
        } catch (Exception e) {
        	e.printStackTrace();
        	throw e;
        } finally {
        	if(connection != null) {
        		try{
        		    connection.close();
        		}catch(Exception e1){
        			e1.printStackTrace();
        		}
        	}
        }
	}
	
	private class GradleOutputStream extends OutputStream {
		private StringBuffer bufferOutput = new StringBuffer();
		public GradleOutputStream(){}
		
		@Override
		public void write(byte[] bytes) throws IOException {
			write(bytes, 0, bytes.length);
		}

		@Override
		public void write(byte[] bytes, int off, int len) throws IOException {
			String output = new String(bytes,off,len);
			//In case of new line, send out message and clear string buffer.
			if(len == 2 && output.equals(System.lineSeparator())){
				outputCallback(bufferOutput.toString());
				bufferOutput.setLength(0);
			}else{
				bufferOutput.append(output);
			}
		}

		@Override
		public void write(int b) throws IOException {}
	}
}
