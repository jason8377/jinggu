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

package org.jandjzone.buildroid.build.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jandjzone.buildroid.build.GradleOutputListener;
import org.jandjzone.buildroid.build.helper.CommandLineExecuter;
import org.jandjzone.buildroid.build.helper.GradleCommandLineExecuter;
import org.jandjzone.buildroid.objs.ProjectInfo;
import org.jandjzone.buildroid.objs.ProjectInfo.TaskInfo;
import org.jandjzone.buildroid.util.CommonUtil;
import org.jandjzone.buildroid.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * <p>Run gradle tasks and sign the apk if needed using zipalign and apksigner</p>
 * <pre>
 * 1. Add a task at the bottom in app/build.gradle by altering the file directly.
        task fetchBuildProperties {
            doFirst {
	            println "The sdk.dir is:"+"${android.getSdkDirectory().getAbsolutePath()}"
		        println("The buildToolsVersion is:"+android.buildToolsVersion)
		        
		        android.applicationVariants.all { variant ->
		            variant.outputs.each { output ->
		                println "APK output file:" + output.outputFile
		            }
		        }
            }
        }
   2. gradlew fetchBuildProperties
      This command will return the following information if no exceptions.
      sdk.dir: by checking if output contains "The sdk.dir is:"
      buildToolsVersion: by checking if output contains "The buildToolsVersion is:"
      APK output full path.
   3. gradlew [build tasks]
   4. Use zipalign to compress the apk
      C:\Users\Jason\AppData\Local\Android\Sdk\build-tools\26.0.2\zipalign -f -v 4 D:\temp\JandJAndroid\app\build\outputs\apk\release\app-release-unsigned.apk D:\temp\JandJAndroid\app\build\outputs\apk\release\app-release.apk
   
      "Verification succesful" contained in the output is considered success.
   5. Use apksigner to sign the apk(Android SDK Build Tools should be 24.0.3 and higher)
      C:\Users\Jason\AppData\Local\Android\Sdk\build-tools\26.0.2\apksigner sign -v --ks d:/temp/test_luo_new.jks --ks-key-alias your_alias --ks-pass pass:123456 --key-pass pass:123456 --v1-signing-enabled true --v2-signing-enabled true D:\temp\JandJAndroid\app\build\outputs\apk\release\app-release.apk
   
      Output is "Signed" would be considered success.
 * </pre>
 * @author Jason
 *
 */
public class TaskGradleCommandLine extends BaseTask implements GradleOutputListener {
	private static final Logger logger = LoggerFactory.getLogger(TaskGradleCommandLine.class);

	private static final String BUILD_TOOLS_FOLDER = "build-tools";
	private static final String ZIPALIGN = "zipalign";
	private static final String APKSIGNER = "apksigner";
	
	private static final String FETCH_BUILD_INFO = "fetchBuildInfo";
	private static String FETCH_BUILD_INFO_BODY;
	
	private static final String SDK_DIR_MATCH_STR = "The sdk.dir is:";
	private static final String TOOLS_VERSION_MATCH_STR = "The buildToolsVersion is:";
	private static final String APK_OUTPUT_MATCH_STR = "APK output file:";
	
	private static final String APK_VERIFY_FAILURE = "ERROR: No JAR signatures";
	static{
		StringBuilder taskBuilder = new StringBuilder();
		
		taskBuilder.append(System.lineSeparator())
		.append("task fetchBuildInfo {").append(System.lineSeparator())
        .append("    doFirst {").append(System.lineSeparator())
        .append("        println \"The sdk.dir is:\"+\"${android.getSdkDirectory().getAbsolutePath()}\"").append(System.lineSeparator())
        .append("        println(\"The buildToolsVersion is:\"+android.buildToolsVersion)").append(System.lineSeparator())
        
        .append("        android.applicationVariants.all { variant ->").append(System.lineSeparator())
        .append("            variant.outputs.each { output ->").append(System.lineSeparator())
        .append("                println \"APK output file:\" + output.outputFile").append(System.lineSeparator())
        .append("            }").append(System.lineSeparator())
        .append("        }").append(System.lineSeparator())
        
        .append("    }").append(System.lineSeparator())
        .append("}");
		
		FETCH_BUILD_INFO_BODY = taskBuilder.toString();
		taskBuilder = null;
	}
	
	private BuildConfig buildConfig;
	
	public TaskGradleCommandLine(ProjectInfo projectInfo, TaskInfo taskInfo) {
		super(projectInfo,taskInfo);
	}

	/**
	 * Check if must parameters are missing
	 * @return
	 */
	@Override
	protected List<String> checkArguments() throws JsonSyntaxException,Exception {
		List<String> missingArguments = new ArrayList<String>();
		
		buildConfig = parseArguments(BuildConfig.class);
		//Remove empty tasks.
		buildConfig.removeEmptyTasks();
		
		if(buildConfig.getGradle_tasks() == null 
				|| buildConfig.getGradle_tasks().length == 0){
			missingArguments.add("gradle tasks");
		}
		
		if(buildConfig.isSignApk()) {
			if(buildConfig.getSigningConfig() == null){
				missingArguments.add("SigningConfig");
			}
		}
		
		return missingArguments;
	}
	
	@Override
	protected void run() throws Exception {
		String gradlePath = CommonUtil.appendFilePath(projectInfo.getProject_path()
				,projectInfo.getProject_module()
				,"build.gradle");
		File fileGradle = new File(gradlePath);
		
		String gradleFileString = FileUtils.readFileToString(fileGradle, Constants.TEXT_ENCODING);
		if(!StringUtils.contains(gradleFileString, FETCH_BUILD_INFO)){
		    //Add a task
		    FileUtils.write(fileGradle, FETCH_BUILD_INFO_BODY,Constants.TEXT_ENCODING, true);
		}
		
		//Run the new added task:fetchBuildProperties
		if(projectInfo.getLastBuildRequest().getSdk_dir() == null 
				|| projectInfo.getLastBuildRequest().getBuildToolsVersion() == null){
			if(!SystemUtils.IS_OS_WINDOWS){
			    //Before running gradlew, make sure it is executable.
				String gradlewFilePath = projectInfo.getProject_path() + "/gradlew";
				File gradlew = new File(gradlewFilePath);
				gradlew.setExecutable(true);
			}
			
		    String[] tasksBuildInfo = {FETCH_BUILD_INFO};
		    new GradleCommandLineExecuter(projectInfo,tasksBuildInfo,new GradleOutputListener(){
		        public void gradleOutput(String output ,Object... arguments){
		            progressOutput(output,arguments);
		            if(StringUtils.startsWith(output,SDK_DIR_MATCH_STR)){
		            	projectInfo.getLastBuildRequest().setSdk_dir(StringUtils.substringAfter(output, SDK_DIR_MATCH_STR));
		            }
				    if(StringUtils.startsWith(output,TOOLS_VERSION_MATCH_STR)){
				    	projectInfo.getLastBuildRequest().setBuildToolsVersion(StringUtils.substringAfter(output, TOOLS_VERSION_MATCH_STR));
				    }
				    if(StringUtils.startsWith(output,APK_OUTPUT_MATCH_STR)){
					    String apkPath = StringUtils.substringAfter(output, APK_OUTPUT_MATCH_STR);
					    projectInfo.getLastBuildRequest().getApkOutputFile().add(apkPath);
				    }
			    }
		    }).start();
		}

		String sdk_dir = projectInfo.getLastBuildRequest().getSdk_dir();
		String buildToolsVersion = projectInfo.getLastBuildRequest().getBuildToolsVersion();
		List<String> apkOutputFile = projectInfo.getLastBuildRequest().getApkOutputFile();
		if(sdk_dir == null || buildToolsVersion == null){
			throw new Exception("Failed to get sdk.dir and buildToolsVersion from running task " + FETCH_BUILD_INFO);
		}
		//Delete any APK built previously.
		for(String apkPath:apkOutputFile){
			File fileApk = new File(apkPath);
			if(fileApk.exists())fileApk.delete();
		}
		
		//Delete reports tests folder
		String reportsTestPath = CommonUtil.appendFilePath(projectInfo.getProject_path()
				,projectInfo.getProject_module()
				,Constants.BUILD_FOLDER
				,Constants.REPORT_FOLDER
				,Constants.TEST_FOLDER);
		File fileTestReports = new File(reportsTestPath);
		FileUtils.deleteQuietly(fileTestReports);
		
		//Delete reports tests folder
		String reportsAndroidTestPath = CommonUtil.appendFilePath(projectInfo.getProject_path()
						,projectInfo.getProject_module()
						,Constants.BUILD_FOLDER
						,Constants.REPORT_FOLDER
						,Constants.ANDROID_TEST_FOLDER);
		File fileAndroidTestReports = new File(reportsAndroidTestPath);
		FileUtils.deleteQuietly(fileAndroidTestReports);
		
		//Now it is time to build the app.
		Exception buildException = null;
		try{
		    new GradleCommandLineExecuter(projectInfo,buildConfig.getGradle_tasks(),this).start();
		}catch(Exception e){
			if(taskInfo.isBlockProcess()){
				buildException = e;
			}
		}
		
		//Count how many apks are generated
		int apkCount = 0;
		for(String apkPath:apkOutputFile){
			File fileApk = new File(apkPath);
			if(fileApk.exists())apkCount ++;
		}
		progressOutput("{}{} APKs were generated.", System.lineSeparator() ,apkCount);
		
		//If signature is needed
		if(apkCount >0) {
			//Use zipalign tools to compress the APK([sdk.dir]/build-tools/26.0.2/zipalign)
			String zipalignPath = CommonUtil.appendFilePath(sdk_dir
					,BUILD_TOOLS_FOLDER
					,buildToolsVersion
					,ZIPALIGN);
			//Use apksigner to sign APK([sdk.dir]/build-tools/26.0.2/apksigner)
			String apksignerPath = CommonUtil.appendFilePath(sdk_dir
					,BUILD_TOOLS_FOLDER 
					,buildToolsVersion
					,APKSIGNER);
			
			/*
			 * Go through all the apks, 
			 * check if each of them signed or not,
			 * if not, sign it.
			 */
			for(String apkPath:apkOutputFile){
				File fileApk = new File(apkPath);
				if(!fileApk.exists())continue;
				if(!buildConfig.isSignApk()){
					moveFirOrDir(fileApk);
					continue;
				}else{
					if(isApkSigned(apksignerPath,apkPath)){
						//Here is the logic to check the apk is signed or not, if yes, skip it.
						progressOutput("Skipping signing APK(signed already, it may be a debug version) {}", apkPath);
						moveFirOrDir(fileApk);
						continue;
					}
				}
				/**
				 * Noted by Jason
				 * For more information regarding the usage of zipalign
				 * visit https://developer.android.com/studio/command-line/zipalign.html
				 */
				String apkPathIn = apkPath;
				
				String fileNameOut = replaceUnsignApkName(fileApk.getName());
				String apkPathOut = fileApk.getParent() + Constants.FILE_SEPERATOR + fileNameOut;
				
				if(buildConfig.getSigningConfig().isZipalignEnabled()){
				    String[] zipalignArgument = {
					    	"-f"         //Overwrite existing apk
						    //,"-v"        //verbose output (Jason: this output generate too many useless lines, comment it for now)
						    ,"4"         //4-byte boundaries
						    ,apkPathIn   //APK to be compressed
						    ,apkPathOut  //APK output
				    };
				    new CommandLineExecuter(zipalignPath, zipalignArgument, new GradleOutputListener(){
					    public void gradleOutput(String output ,Object... arguments){
						    progressOutput(output,arguments);
					    }
				    }).start();
				    progressOutput("APK has been zipaligned successfully {}", apkPath);
				}
				
				String[] apksignerArgument = {
						"sign"
						,"-v"
						,"--ks"
						,buildConfig.getSigningConfig().getStoreFile()
						,"--ks-key-alias"
						,buildConfig.getSigningConfig().getKeyAlias()
						,"--ks-pass"
						,"pass:" + buildConfig.getSigningConfig().getStorePassword()
						,"--key-pass"
						,"pass:" + buildConfig.getSigningConfig().getKeyPassword()
						,"--v1-signing-enabled"
						,buildConfig.getSigningConfig().isSigning_enabled_v1() + ""
						,"--v2-signing-enabled"
						,buildConfig.getSigningConfig().isSigning_enabled_v2() + ""
						,apkPathOut
				};
				new CommandLineExecuter(apksignerPath ,"Failed to load signer"
						, apksignerArgument, new GradleOutputListener(){
					public void gradleOutput(String output ,Object... arguments){
						progressOutput(output,arguments);
					}
				}).start();
				moveFirOrDir(new File(apkPathOut));
				progressOutput("APK has been signed successfully {}", apkPath);
			}
		}
		
		//Move reports folder
		if(fileTestReports.exists()){
			moveFirOrDir(fileTestReports);
		}
		if(fileAndroidTestReports.exists()){
			moveFirOrDir(fileAndroidTestReports);
		}
		if(buildException != null){
			throw buildException;
		}
	}
	
	/**
	 * Remove "unsign" characters for an APK name 
	 * @param unsignedApkFileName
	 * @return
	 */
	private String replaceUnsignApkName(String unsignedApkFileName){
		if(StringUtils.endsWithIgnoreCase(unsignedApkFileName, "-unsigned.apk")){
			unsignedApkFileName = StringUtils.replace(unsignedApkFileName, "-unsigned.apk", ".apk");
		}else{
			unsignedApkFileName = StringUtils.replaceIgnoreCase(unsignedApkFileName, ".apk" ,"-signed.apk");
		}
		return unsignedApkFileName;
	}
	
	/**
	 * Move a file or a directory.
	 * @param file
	 */
	private void moveFirOrDir(File file){
		String projectLogPath = checkProjectLogPath();
		
		try {
			if(file.isFile()){
			    FileUtils.copyToDirectory(file, new File(projectLogPath));
			}else{
				FileUtils.copyDirectoryToDirectory(file, new File(projectLogPath));
			}
		} catch (IOException e) {
			progressOutput("Failed to move {} to {}" , file.getPath() , projectLogPath);
		}
	}

	@Override
	public void gradleOutput(String output, Object... arguments) {
		progressOutput(output,arguments);
	}
	
	/**
	 * Check if an APK is signed or not by calling "apksigner verify" command
	 * @param apksignerPath
	 * @param apkPath
	 * @return
	 */
	private static boolean isApkSigned(String apksignerPath,String apkPath){
		String[] apkVerifyArgument = {
				"verify"
				,apkPath
		};
		try {
			new CommandLineExecuter(apksignerPath,APK_VERIFY_FAILURE, apkVerifyArgument, new GradleOutputListener(){
				public void gradleOutput(String output ,Object... arguments){
					//progressOutput(output,arguments);
				}
			}).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.warn(e.toString());
			return false;
		}
		return true;
	}
	
	private static class BuildConfig {
		private String[] gradle_tasks;
		private boolean signApk;
		private SigningConfig signingConfig;
		
		/**
		 * Remove any empty task which may cause trouble for command line.
		 */
		public void removeEmptyTasks(){
			if(gradle_tasks == null || gradle_tasks.length == 0)return;
			
			List<String> taskList = new ArrayList<String>();
			for(String task:gradle_tasks){
				if(StringUtils.isBlank(task))continue;
				taskList.add(task);
			}
			
			if(taskList.size() == 0)return;
			gradle_tasks = taskList.toArray(new String[taskList.size()]);
		}
		
		public String[] getGradle_tasks() {
			return gradle_tasks;
		}
		public void setGradle_tasks(String[] gradle_tasks) {
			this.gradle_tasks = gradle_tasks;
		}
		public boolean isSignApk() {
			return signApk;
		}
		public void setSignApk(boolean signApk) {
			this.signApk = signApk;
		}
		public SigningConfig getSigningConfig() {
			return signingConfig;
		}
		public void setSigningConfig(SigningConfig signingConfig) {
			this.signingConfig = signingConfig;
		}
	}
	private static class SigningConfig {
		private boolean zipalignEnabled = true;
		private String storeFile;
		private String keyAlias;
		private String storePassword;
		private String keyPassword;
		private boolean signing_enabled_v1 = true;
		private boolean signing_enabled_v2 = true;
		public boolean isZipalignEnabled() {
			return zipalignEnabled;
		}
		public void setZipalignEnabled(boolean zipalignEnabled) {
			this.zipalignEnabled = zipalignEnabled;
		}
		public String getStoreFile() {
			return storeFile;
		}
		public void setStoreFile(String storeFile) {
			this.storeFile = storeFile;
		}
		public String getKeyAlias() {
			return keyAlias;
		}
		public void setKeyAlias(String keyAlias) {
			this.keyAlias = keyAlias;
		}
		public String getStorePassword() {
			return storePassword;
		}
		public void setStorePassword(String storePassword) {
			this.storePassword = storePassword;
		}
		public String getKeyPassword() {
			return keyPassword;
		}
		public void setKeyPassword(String keyPassword) {
			this.keyPassword = keyPassword;
		}
		public boolean isSigning_enabled_v1() {
			return signing_enabled_v1;
		}
		public void setSigning_enabled_v1(boolean signing_enabled_v1) {
			this.signing_enabled_v1 = signing_enabled_v1;
		}
		public boolean isSigning_enabled_v2() {
			return signing_enabled_v2;
		}
		public void setSigning_enabled_v2(boolean signing_enabled_v2) {
			this.signing_enabled_v2 = signing_enabled_v2;
		}
	}
}
