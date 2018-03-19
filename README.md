# Looking for a lightweight Android web-based build tool? This might be the one for you.
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
<li>Enjoy building.

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
<li>Task: org.jandjzone.buildroid.build.tasks.TaskGitPuller
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
       * Git URL, can be the following three.
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
<li>Task: org.jandjzone.buildroid.build.tasks.TaskUpdateAppVersion
<pre>
Coming soon
</pre>
<li>Task: org.jandjzone.buildroid.build.tasks.TaskUpdateResource
<pre>
Coming soon
</pre>
<li>Task: org.jandjzone.buildroid.build.tasks.TaskGradleCommandLine
<pre>
Coming soon
</pre>
<li>Task: org.jandjzone.buildroid.build.tasks.TaskSaveBuildHistory
<pre>
Coming soon
</pre>

# Create your own tasks
Coming soon.

# Todo list
<li>Svn puller task
<li>Email sender task
<li>Background task
