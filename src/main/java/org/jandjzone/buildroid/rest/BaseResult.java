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

/**
 * Rest service result base class.
 * @author Jason
 *
 */
public class BaseResult {
	private int result_code;
	private String result_desc;
	
	public BaseResult(){}
	public BaseResult(int result_code, String result_desc){
		this.result_code = result_code;
		this.result_desc = result_desc;
	}
	
	public boolean isSuccess(){
		return result_code==0?true:false;
	}
	
	public int getResult_code() {
		return result_code;
	}
	public void setResult_code(int result_code) {
		this.result_code = result_code;
	}
	public String getResult_desc() {
		return result_desc;
	}
	public void setResult_desc(String result_desc) {
		this.result_desc = result_desc;
	}
	
    public static final BaseResult RESULT_SUCCESS = new BaseResult();
	
	public static final int RESULT_CODE_UNKNOWN = 1;
	public static final String RESULT_DESC_UNKNOWN = "Unknown Error";
	public static final BaseResult RESULT_UNKNOWN_ERROR = new BaseResult(RESULT_CODE_UNKNOWN,RESULT_DESC_UNKNOWN);
	
	public static final int RESULT_CODE_NETWORK = 2;
	public static final String RESULT_DESC_NETWORK = "Network Error";
	public static final BaseResult RESULT_NETWORK_ERROR = new BaseResult(RESULT_CODE_NETWORK,RESULT_DESC_NETWORK);
	
	public static final int RESULT_CODE_INVALID_REQUEST = 3;
	public static final String RESULT_DESC_INVALID_REQUEST = "Invalid build request";
	public static final BaseResult RESULT_INVALID_REQUEST_ERROR = new BaseResult(RESULT_CODE_INVALID_REQUEST,RESULT_DESC_INVALID_REQUEST);
	
	public static final int RESULT_CODE_PROJECT_NOT_EXIST = 4;
	public static final String RESULT_DESC_PROJECT_NOT_EXIST = "Project doesn't exist";
	public static final BaseResult RESULT_PROJECT_NOT_EXIST_ERROR = new BaseResult(RESULT_CODE_PROJECT_NOT_EXIST,RESULT_DESC_PROJECT_NOT_EXIST);
	
	public static final int RESULT_CODE_BUILDING_ONGOING = 5;
	public static final String RESULT_DESC_BUILDING_ONGOING = "Project is being built";
	public static final BaseResult RESULT_BUILDING_ONGOING_ERROR = new BaseResult(RESULT_CODE_BUILDING_ONGOING,RESULT_DESC_BUILDING_ONGOING);
}
