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

package org.jandjzone.buildroid.util;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.jandjzone.buildroid.objs.BuildRequest.BuildParameter;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Commonly used methods.
 * @author Jason
 *
 */
public class CommonUtil {
	private static final Gson GSON = new Gson();
	/**
	 * Parse a string to int
	 * @param ori
	 * @param defaultValue
	 * @return
	 */
	public static int parseToInt(String ori,int defaultValue){
		try{
			return Integer.parseInt(ori);
		}catch(Exception e){}
		
		return defaultValue;
	}
	
	/**
	 * Format date to specific format
	 * @param date
	 * @param format
	 * @return
	 */
	public static final String formatDate(Date date,String format){
        if(date==null||format==null)return null;

        try{
            DateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(date);
        }catch(Exception e){}
        return null;
    }
	
	/**
	 * Convert an object to JSON
	 * @param src
	 * @return
	 */
	public static String toJson(Object src) {
        return GSON.toJson(src);
    }
	
	/**
	 * Convert JSON to a specific object
	 * @param json
	 * @param classOfT
	 * @return
	 * @throws JsonSyntaxException
	 */
	public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return GSON.fromJson(json,classOfT);
    }
	
	/**
	 * Convert JSON to a specific object
	 * @param json
	 * @param classOfT
	 * @return
	 * @throws JsonSyntaxException
	 */
    public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        return GSON.fromJson(json,typeOfT);
    }
    
    /**
	 * Convert JSON to a specific object
	 * <p>No exceptions, returns null if any wrong happens.
	 * @param json
	 * @param classOfT
	 * @return
	 * @throws JsonSyntaxException
	 */
    public static <T> T fromJsonNoException(String json, Class<T> classOfT) {
        try {
            return fromJson(json, classOfT);
        } catch (JsonSyntaxException e){
            return null;
        } catch (Exception e){
            return null;
        }
    }
    
    /**
	 * Convert JSON to a specific object
	 * <p>No exceptions, returns null if any wrong happens.
	 * @param json
	 * @param classOfT
	 * @return
	 * @throws JsonSyntaxException
	 */
    public static <T> T fromJsonNoException(String json, Type typeOfT) {
        try {
            return fromJson(json, typeOfT);
        } catch (JsonSyntaxException e){
            return null;
        } catch (Exception e){
            return null;
        }
    }
	
    //Following static variables are for (un)wraping sensitive data.
    
	private static String[] SENSITIVE_ARRAY = {"<sensitive>","</sensitive>"};
	private static String[] BLANK_ARRAY = {"",""};
	private static String SENSITIVE_WRAPER = "<sensitive>?</sensitive>";
	private static String SENSITIVE_REGEX = "<sensitive>.*</sensitive>";
	private static String STARS = "xxxxxx";
	
	/**
	 * Wrap data with sensitive tag.
	 * <p>
	 * For example, string "password" will be <sensitive>password</sensitive> after being wrapped.
	 * @param unSensitive
	 * @return
	 */
	public static String wrapSensitiveData(String unSensitive){
		if(StringUtils.isBlank(unSensitive))return unSensitive;
		return StringUtils.replace(SENSITIVE_WRAPER, "?", unSensitive);
	}
	
	/**
	 * Unwrap sensitive data
	 * <p>
	 * <sensitive>password</sensitive> will be "password" after being unwrap.
	 * @param sensitive
	 * @return
	 */
	public static String unwrapSensitiveData(String sensitive){
		if(StringUtils.isEmpty(sensitive))return sensitive;
		
		return StringUtils.replaceEach(sensitive, SENSITIVE_ARRAY, BLANK_ARRAY);
	}
	
	/**
	 * Unwrap an array of sensitive data. See {@link #unwrapSensitiveData(String)}
	 * @param sensitives
	 * @return
	 */
	public static String[] unwrapSensitiveData(String[] sensitives){
		if(sensitives == null)return sensitives;
		
		for(int i=0;i<sensitives.length;i++){
		    sensitives[i] = unwrapSensitiveData(sensitives[i]);
		}
		return sensitives;
	}
	
	/**
	 * <p>
	 * Replace sensitive data to six stars character. 
	 * This is for output of build process to avoid being viewed by others.
	 * </p>
	 * @param sensitive
	 * @return
	 */
	public static String starsSensitiveData(String sensitive){
        if(StringUtils.isEmpty(sensitive))return sensitive;
		
		return StringUtils.replaceAll(sensitive, SENSITIVE_REGEX, STARS);
	}
	
	/**
	 * Append an array of string to a string.
	 * @param toBeAppends
	 * @return
	 */
	public static String appendString(String... toBeAppends){
		if(toBeAppends == null)return null;
		StringBuffer stringBuffer = new StringBuffer();
		for(String str:toBeAppends){
			if(str == null)continue;
			stringBuffer.append(str);
		}
		
		return stringBuffer.toString();
	}
	
	/**
	 * Append an array of string to a file path separated by file system separators.
	 * @param toBeAppends
	 * @return
	 */
	public static String appendFilePath(String... toBeAppends){
		if(toBeAppends == null)return null;
		StringBuffer stringBuffer = new StringBuffer();
		for(String str:toBeAppends){
			if(str == null)continue;
			if(stringBuffer.length() > 0){
				stringBuffer.append(Constants.FILE_SEPERATOR);
			}
			stringBuffer.append(str);
		}
		
		return stringBuffer.toString();
	}
	

	/**
	 * List files for given directory and file/dir filters.
	 * @param dirPath
	 * @param fileFilter
	 * @param dirFilter
	 * @return
	 */
	public static Collection<File> listFiles(String dirPath, IOFileFilter fileFilter, IOFileFilter dirFilter){
		try{
			return FileUtils.listFiles(new File(dirPath), fileFilter, dirFilter);
		}catch(Exception e){
			return null;
		}
	}
}
