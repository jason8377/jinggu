# Buildroid, a lightweight Android web-based build tool

# Requirements
<li>JAVA 8
<li>Maven 3
<li>Eclipse EE IDE(Optional, you can use Maven)
<li>Servlet based server (I'm using Tomcat 7 for testing)
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
<li>Open browser, type in http://ip:port/build
<li>Enjoy building.

# Tutorial
Reference of built-in build tasks will be cover as well as how to implement custom tasks.
  Coming soon.

# Todo list
<li>Tutorial
<li>Svn puller task
<li>Email sender task
<li>Background task
