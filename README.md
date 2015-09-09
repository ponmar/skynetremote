# README #

Skynet Remote is a Java based GUI for supervision and control of a [Skynet][skynet_site] system. This is done by using the [Skynet API][skynet_api_site]. This project is mostly used as a demonstration for how the API can be used and is continuously updated for the latest API version.

[Skynet][skynet_site] is a home security and home automation system.

### Releases ###

See [downloads][skynetremote_downloads] for an easy to run pre-built JAR file. If your Java version is too old to run this build you need to rebuild Skynet Remote, and possibly [Skynet API][skynet_api_site], for your Java version.

### How to build? ###
This project can either be built and run from within Eclipse, or by exporting a JAR file to be executed outside Eclipse.

How to build in Eclipse:

* Add appropriate SWT jar file for your platform to the project in Eclipse and remove any previous SWT jar file from project if available
* Press build

How to export a runnable JAR file:

* Open the project in Eclipse
* Add appropriate SWT jar file for your target platform to the project in Eclipse (download [here][swt_site] if SWT for your target platform is not included in this repository)
* Right click on project and select "Export..."
* Select "Runnable JAR file" and press "Next"
* Set the export destination to "SkynetRemote\SkynetRemote-<version>.jar"
* Select "Package required libraries into generated JAR"
* Click "Finish"

### How to run? ###

Either download and run a release from [downloads][skynetremote_downloads], or run from Eclipse (without a JAR-file).

### Contribution guidelines ###

You are very welcome to fork this project and make your own customized GUI.

Please send comments regarding [Skynet][skynet_site], [Skynet API][skynet_api_site] and [Skynet Remote][skynetremote_site] to pontus.markstrom@gmail.com.

### Licenses ###

Licenses for included software is placed in the licenses directory.

### Screenshot ###

![skynetremote_screenshot.png](https://bitbucket.org/repo/xxXqE8/images/3532958321-skynetremote_screenshot.png)

[skynet_site]: http://pihack.no-ip.org/pontus/projects/skynet/
[skynet_api_site]: http://pihack.no-ip.org/pontus/projects/skynet/#api
[skynetremote_site]: https://bitbucket.org/pontusmarkstrom/skynet-remote/overview
[skynetremote_downloads]: https://bitbucket.org/pontusmarkstrom/skynet-remote/downloads
[swt_site]: https://www.eclipse.org/swt/