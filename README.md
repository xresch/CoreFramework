# Introduction
The CoreFramework(CFW) is a web application boilerplate projects using Jetty, Bootstrap, H2 and various other cool stuff. 
Following Features are provided by the framework:
- User & Permission Management
- Active Directory Integration
- Dashboarding
- Job Scheduling
- Configuration Management
- API
- Integrated Manual Pages
- User Notifications
- Mailing
- Integrated System Analytics
- Prometheus /metric entpoint

# Server Setup
The following is a general setup guide for the server and applications using the Core Framework(CFW). Depending on the application there might be additional steps needed to make the system fully functional.

## System Requirements
*   **Java:** JDK 1.8 or higher. JRE is not enough, The H2 database needs the javac executable on the classpath to be able to compile database procedures.
*   **OS:** Operating systems that support Java. Some applications might have limited support for operating systems.
*   **Capacity:** Depends on the implementations used and the load on the system, a minimum of 4GB RAM and 4 x 1.6 GHz Cores is recommended.

## Setup Guide

1.  **Install JDK:** Install JDK 1.8 or higher. The /bin-Folder has to be added to the PATH environment variable so that the javac executable can be found.
2.  **Download:** Get the .zip-File with the application binaries.
3.  **Unzip:** Unzip the files to the folder where the application should be located.
4.  **Configuration:** Open ./config/cfw.properties and adjust the properties to fit your needs.
5.  **Start:** Start the application using the start script in the root folder.
6.  **Default Login:** Use http://localhost:{port}/ to access the application. Use the default user 'admin' with password 'admin'.
7.  **Change Default Password:** Change the default password of the admin user using "Menu Bar >> Username >> Change Password".
8.  **Configuration(Optional):** Go to "Menu Bar >> Admin >> Configuration" to adjust the configuration of your application.
9.  **Context Settings(Optional):** If your application provides Context Settings, you might need to add such settings before you can make use of certain features. Go to "Menubar >> Admin >> Context Settings" to add context settings for your needs.
10.  **Restart:** Stop the application, delete the log files, start the application to see if restarts properly and no major issues are present in the logs.
11.  **System Startup:** Add the application to your system startup to make sure it runs after the machine gets restarted.
12.  **Manual:** After your application is running you can access the other manual pages directly in the application under "Menu Bar >> Manual".
13.  **Application Setup Guides:** The application built on the Core Framework might ship with additional setup steps you might want to follow.

## Troubleshooting

### General
Here some tips for troubleshooting the application:

*   **Log Files:** Check the log files for WARNING or SEVERE messages.
*   **More Logs:** To get more detailed logs, adjust the log level in ./config/logging.properties, which is a confugration file for java.util.logging.
*   **CPU Sampling:** If you have performance issues, you can use "Menu Bar >> Admin >> System Analytics >> CPU Sampling" to get insights into what methods consume the most time in the application.
*   **DB Statistics:** You can see soem Database statistics under "Menu Bar >> Admin >> System Analytics >> DB Analytics".

### Known Issues
*   **JVM Bind Exception:** This issue is either caused by the ports defined in ./config/cfw.properties are already in use or are not available for other reasons.
*   **JavaC / DB Procedure not found:** H2 procedures need the javac executable in the PATH system variable in order to be compiled. Check if the javac is available from the command line by executing the command 'javac.exe' for windows or 'javac' for linux.

# Development Setup
You can find an example project in the following GitHub repository: [CoreFramework_ExtensionExample](https://github.com/xresch/CoreFramework_ExtensionExample)

## Needed Tools

The following tools are needed to develop with the Core Framework:

*   Java OpenJDK 8 or higher
*   Eclipse IDE
*   Apache Maven
*   Git Client(e.g. Tortoise Git)

The following tools are optional or needed depending on what you are coding:

*   Database Tool that can access the H2 database (e.g. DBeaver)
*   LDAP Server (e.g. Apache Studio)
*   Mail Testing Tool (e.g. Papercut)

## Eclipse Setup and Running the Application

The following steps show how to setup a new Eclipse workspace with the aforementioned example project. You can as well add your own existing project or create a new project in the same workspace based on the sample project.

1.  Checkout the Sample Project: [https://github.com/xresch/CoreFramework_ExtensionExample.git](https://github.com/xresch/CoreFramework_ExtensionExample.git)
2.  Open Eclipse and create a new workspace.
3.  File >> Import... >> Existing project into workspace
4.  Select the folder of the sample project, select the checkbox in the Projects section.
5.  Make sure the following checkbox is **NOT** selected: "Copy project into workspace"
6.  Click Finish
7.  Right click on the Project >> Run As >> Maven install. If this fails, you need to check your maven is setup correctly.
8.  Navigate to the following class in the Project Explorer and choose "Right Click >> Run As >> Java Application": com.xresch.cfw.example._main._MainForTesting.

**Note: :** The same procedure applies when you create a fork of the Core Framework itself.

## Start Developing

You can now start developing based on the code you find in the sample project and the following development docs.

## Further Information

### CFW Maven Dependency

To setup a development environment create a maven project and add the following dependency to your .pom-File. For a full example please refer to the pom-file in the sample project.

    <!-- https://mvnrepository.com/artifact/com.xresch/cfw -->
    <dependency>
    	<groupId>com.xresch</groupId>
    	<artifactId>cfw</artifactId>
    	<version>4.0.0</version>
    	<scope>provided</scope>
    </dependency>	

### Testing the Application

To test your application without copying it into the extension folder of the cfw server, create a main class like this:

    public class _MainForTesting {

        public static void main( String[] args ) throws Exception
        {
        	//------------------------------------
        	// Load Application Extension
        	CFWAppInterface app = CFW.loadExtentionApplication();

        	//------------------------------------
        	// Start Application
        	CFW.initializeApp(app, args);
        }
    }

Afterwards you can access the application under [http://localhost:8888/](http://localhost:8888/) (port is defined in ./config/cfw.properties).

### Export and Install Application

To export your application do the following:

1.  mvn clean install

2.  <pre>Copy the resulting jar file and all it's dependencies to {CFW_SERVER_ROOT}/extensions.</pre>

3.  If you have added additional files on the disc, copy them to the correct location, like:
    *   /config
    *   /resources
