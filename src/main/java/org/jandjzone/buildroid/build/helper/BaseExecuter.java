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

import org.jandjzone.buildroid.build.GradleOutputListener;
import org.jandjzone.buildroid.util.CommonUtil;
import org.jandjzone.buildroid.util.Constants;

/**
 * Base executer
 * @author Jason
 *
 */
public abstract class BaseExecuter {
	protected String[] arguments;
	protected GradleOutputListener outputListener;
	
	/**
	 * Constructor of gradle executer.
	 * @param projectInfo The ProjectInfo object passed from BaseTask
	 * @param arguments Tasks or options for this gradle run
	 * @param outputListener The callback of this run, mostly for output purpose.
	 */
	public BaseExecuter(String[] arguments
			,GradleOutputListener outputListener){
		this.arguments = arguments;
		this.outputListener = outputListener;
	}
	
	/**
	 * The callback will be triggered here.
	 * @param outputListener
	 * @param output
	 * @param arguments
	 */
	protected void outputCallback(String output ,Object... arguments){
		if(outputListener == null || output == null)return;
		outputListener.gradleOutput(output,arguments);
	}
	
	/**
	 * <p>The wrapper method of run which does some initialization checking.</p>
	 * @throws Exception
	 */
	public void start() throws Exception {
		if(arguments == null || arguments.length ==0){
			throw new Exception("Tasks is empty.");
		}
		
		run();
	}
	
	/**
	 * Concrete implements should override this method
	 * @throws Exception
	 */
	protected abstract void run() throws Exception;

	/**
	 * Print the output for an string array separated by a space. 
	 * @param arguments
	 * @return
	 */
	protected static String printArray(String[] arguments) {
		StringBuilder outputBuilder = new StringBuilder();
		if(arguments != null && arguments.length >0){
			for(String argument:arguments){
				if(outputBuilder.length()>0)outputBuilder.append(Constants.ONE_SPACE);
			    outputBuilder.append(CommonUtil.starsSensitiveData(argument));
			}
		}
		return outputBuilder.toString();
	}
}
