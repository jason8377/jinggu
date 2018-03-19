# Looking for a lightweight Android web-based build tool? This might be the one.
<img src="http://www.jandjzone.com/images/screenshot1.png"></img>

<li>Written in pure Java.
<li>Web based which allows you to deploy to a Servlet based server for easy access.
<li>Multi projects support and flexibility,extendability of tasks.
<li>Tasks are plugin based. There are some built-in tasks currently and you can create your own.
<li>Build history is built-in supported.
<li>Arguments can be passed from HTML forms to tasks.
<li>WebSocket supported allows you to monitor the output of build.

# Requirements
<li>JAVA 8
<li>Maven 3
<li>Eclipse EE IDE(Optional, you can use Maven)
<li>Servlet based server (This project is testing on Tomcat 7)
<li>Android SDK

# Quick Start
Buildroid is a Maven project using Eclipse IDE. To compile it, you can eigher use Maven directly or Eclipse. 
Here I am going to use Eclipse to demonstrate how to compile and setup. 
Before proceeding, make sure you have all the softwares listed above installed and setup.

<li>Download source code <a href="https://github.com/jason8377/Buildroid">https://github.com/jason8377/Buildroid</a>
<li>Launch Eclipse, import project and then go to Maven -> Update project to update project dependencies.
<li>Open src/main/resources/server.properties, change ANDROID_SDK_DIR to your Android SDK directly. Example as below for Window or Mac.
<pre>
# Windows
ANDROID_SDK_DIR=C\\:\\\\Users\\\\Username\\\\AppData\\\\Local\\\\Android\\\\Sdk
or
# Mac
ANDROID_SDK_DIR=/Users/Username/Library/Android/sdk
</pre>
<li>Open src/main/resources/Projects.json, change "project_path" and "working_directory" for each testing
Android project point to existing folders in you file system.
<li>In Eclipse, right click your project, choose Run As -> Maven Build, input clean install for Goals input field then click Run button.
<li>Once build is done, if you see BUILD SUCCESS, congraturation, you are good to deploy it to your build server.
<li>Navigate to your project folder, you will see Buildroid folder under target, copy it to webapp folder in your J2EE server.
<li>Open browser, type in http://ip:port/buildroid
<li>Enjoy.

# Android projects and tasks configuration
Projects are configured in Projects.json locates in WEB-INF/classes. You can move it to any CLASS_PATH location if you want.
<li>Project config
<pre>
{
  //Unique project ID
  "project_id" : "google-maps-demo"
  //Project name
  ,"project_name" : "Google Maps Sample"
  /**
   * The path of your project where the root build.gradle locates.
   * If "D:/temp/" exists, "GoogleMapsSample" will be created automatically the first time if it doesn't exist.
   */
  ,"project_path" : "D:/temp/GoogleMapsSample"
  /**
   * The input form of your project. Will be "forms/form_default.jsp" if it's not specified.
   * All forms are located in WEB-INF/views/forms and you can create your own.
   */
  ,"form_view" : "forms/form_google_maps.jsp"
  /**
   * Task list
   * Will be executed from top to bottom
   */
  ,"task_list" : [......]
}
</pre>
<li>Task: org.jandjzone.buildroid.build.tasks.TaskGitPuller<br>
TaskGitPuller is a plugin performs cloning or updating code from your remote Git repository.
<pre>
{
  "task_name" : "Update code from Git"
  ,"task_class" : "org.jandjzone.buildroid.build.tasks.TaskGitPuller"
  ,"arguments" : {
      /**
       * Working directory of project Git repository
       * This can be different with "project_path" mentioned above as your repository might cover multi projects
       */
      "working_directory" : "D:/temp/AndroidHelloWorld"
      
	  /**
       * Git URL, can be the following.
       * http://github.com/codepath/android_hello_world.git or
       * https://github.com/codepath/android_hello_world.git or
       * ssh://[your user name]@github.com/codepath/android_hello_world.git
       */
      ,"git_url" : "https://github.com/codepath/android_hello_world.git"
      
	  //If you are using asymmetry-based algorithm, specify private key path here
      ,"private_key_path" : ""
      
	  //For https which needs user name as credential
      ,"credential_user" : ""
      
	  //For https and SSH which needs password as credential
      ,"credential_password" : ""
      
	  /**
       * Commit number or branch name of your project code
       * Can be hard coded or passed from input form with the argument name wrapped with <>.
       */
	  ,"commit_number" : "<commit_number>"
  }
}
</pre>
<li>Task: org.jandjzone.buildroid.build.tasks.TaskUpdateAppVersion<br>
TaskUpdateAppVersion changes the versionCode and versionName in you app/build.gradle.
It doesn't have any argument definition here but accepts HTML form elements directly.<br>
To do this, you will need versionCode and versionName as id in your form. See form_default.jsp for example.
<pre>
{
  "task_name" : "Update App Version"
  ,"task_class" : "org.jandjzone.buildroid.build.tasks.TaskUpdateAppVersion"
}
</pre>
<li>Task: org.jandjzone.buildroid.build.tasks.TaskUpdateResource<br>
An Android project contains many XML files, like AndroidManifest.xml and other resources. In most cases,
you can differ your APPs by using combinations of buildType,flavor and dimensions(introduced since Gralde3).<br>
However, in some cases, you want to change XML based settings during build process instead of 
changing the code in your repository.<br>
TaskUpdateResource allows you change XML node/attribute text passed from HTML forms.<br>
Note that one task can handle multi nodes/attributes in a single file at a time. 
If you want to change different files, create new tasks.<br>
<strong>Example of changing app name.</strong>
<pre>
{
  "task_name" : "Update App Name"
  ,"task_class" : "org.jandjzone.buildroid.build.tasks.TaskUpdateResource"
  ,"arguments" : {
    "resource_path" : "app/src/main/res/values/strings.xml"
    ,"update_nodes" : [
      {
        "node_location" : "resources->string->#text"
        //app_name should be defined in you input form.
        ,"new_value" : "&lt;app_name&gt;"
        ,"attribute_filter" : [
          {"attr_name" : "name", "attr_value" : "app_name"}
        ]
      }
	  ,{
        "node_location" : "resources->string->#text"
        //server_url should be defined in you input form.
        ,"new_value" : "&lt;server_url&gt;"
        ,"attribute_filter" : [
          {"attr_name" : "name", "attr_value" : "server_url"}
        ]
      }
    ]
  }
}
</pre>
<strong>Example of changing app icon.</strong> Note that node "application" is not duplicating, you can ignore attribute_filter.
<pre>
{
  "task_name" : "Update App Icon"
  ,"task_class" : "org.jandjzone.buildroid.build.tasks.TaskUpdateResource"
  ,"arguments" : {
    "resource_path" : "app/src/main/AndroidManifest.xml"
    ,"update_nodes" : [
      {
        "node_location" : "manifest->application->android:icon->#text"
        //app_icon should be defined in you input form.
        ,"new_value" : "&lt;app_icon&gt;"
      }
    ]
  }
}
</pre>
<li>Task: org.jandjzone.buildroid.build.tasks.TaskGradleCommandLine<br>
TaskGradleCommandLine executes gradle tasks according to your DSL definition of your project.<br>
Tips: You can get the list of tasks by executing "gradlew task".<br>
<strong>Example: test,cAT and assembleDebug together</strong>
<pre>
{
  "task_name" : "Build"
  ,"task_class" : "org.jandjzone.buildroid.build.tasks.TaskGradleCommandLine"
  ,"arguments" : {
    "gradle_tasks" : ["test","cAT","assembleDebug"]
  }
}
</pre>
<strong>Example: cAT</strong><br>
The benefit of splitting task is that if it fails, the build process can proceed by setting blockProcess to false.
<pre>
{
  "task_name" : "UI testing"
  ,"task_class" : "org.jandjzone.buildroid.build.tasks.TaskGradleCommandLine"
  ,"blockProcess" : false
  ,"arguments" : {
    "gradle_tasks" : ["cAT"]
  }
}
</pre>
<strong>Example: Sign release APP</strong>
<pre>
{
  "task_name" : "Sign APK"
  ,"task_class" : "org.jandjzone.buildroid.build.tasks.TaskGradleCommandLine"
  ,"arguments" : {
    "gradle_tasks" : ["assembleRelease"]
    ,"signApk" : true
    ,"signingConfig" : {
      "zipalignEnabled" : true
       ,"storeFile" : "D:/jason/document/test_luo_new.jks"
       /**
        * keyAlias,storePassword,keyPassword,v1_signing_enabled,v2_signing_enabled should be defined in you input form.
        * See form_signing.jsp for more information.
        */
       ,"keyAlias" : "&lt;keyAlias&gt;"
       ,"storePassword" : "&lt;storePassword&gt;"
       ,"keyPassword" : "&lt;keyPassword&gt;"
       ,"v1-signing-enabled" : &lt;v1_signing_enabled&gt;
       ,"v2-signing-enabled" : &lt;v2_signing_enabled&gt;
    }
  }
}
</pre>
<li>Task: org.jandjzone.buildroid.build.tasks.TaskSaveBuildHistory<br>
TaskSaveBuildHistory performs saving build history to a embedded database which is SQLite. 
No need have it in your task list, it will be called at the end of build process. <br>
If you want the build history to be saved into another database, you can customize TaskSaveBuildHistory
by using the bonecp database connection pool.

# Create your own tasks
Coming soon.

# Todo list
<li>Svn puller task
<li>Email sender task
<li>Background task
