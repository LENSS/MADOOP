# MADOOP
This application is an example of Mobile Edge Computing using Mobile Hadoop and MapReduce.

## components
- Madoop Server, basically hadoop-0.20.2.
- Madoop Client (android)
- GNS for name resolving

## Network Architecture
- The Network consists of mainly these components:
  - The field-server (EPC) is the main server where Madoop master,  GNS-server, other services should be running.
  - A WiFi access point. The access point is only for providing the communication backbone. It __must not__ run any DHCP server. Since the EPC is running DHCP server.
  - LTE eNodeB.
  - Android mobile phones. These phones will run the binary APK. Android phones get their IP from the DHCP server.
- This setup requires that there is a GNS server running in the EPC. Each device will run GNS service which updates the hostname and IP address of the devices at the GNS server. GNS also runs a DNS server which translates hostname to IP.  

## MADOOP Client on Android phones
#### Building the APK (In Ubuntu 16.04):
- MADOOP source code for Android could be downloaded from the LENSS repository.
- MADOOP can now work with the newest version of Android SDK and NDK-16b. There are some libraries that are not supported by Android SDK and NDK anymore.
- If Android studio was used on the current computer before, it downloded some of the latest libraries in the `/home/username` folder which should be removed first. Remove `Android`, `.android` and `AndroidStudio3.2` (check for other versions as well) folders.
- Download Android Studio 3.2.
- Open the MADOOP android project in Android studio and follow the default instructions.
- Install SDK when asked. But for NDK, download NDK version 16 from this repository. https://developer.android.com/ndk/downloads/older_releases and store the extracted files inside  `~/Android/Sdk/ndk-bundle/android-ndk-r16b`.
- Select the project and then go to File->Project structure. Under SDK Location, specify the correct NDK in the Android NDK location box.
- When building the project, the Android studio might suggest to update Gradle script. Do not accept it.
- Go to Build->Build Bundles/APK->Build APK.This should build the APK inside the folder `/app/build/outputs/apk/debug`.
- Use `adb install Madoop-debug.apk` command to install the app to a phone.  

#### Setting the phone (MADOOP_client) before using MADOOP
- The assiciated files to run MADDOP are stored in `/MADOOP_Client/Associated_Files` folder in Github.
- The Madoop facerecognition app requires a face-database. Store these 3 files `facemodel.xml`, `mask.jpg` and `shape_predictor_68_face_landmarks.dat` files in `/sdcard/distressnet/Madoop4/Madoop4_documents/` folder of each phone that uses this App. Use command `adb push Madoop4_documents /sdcard/distressnet/Madoop4/` to transfer the files.
- Also, in the same folder, create 3 empty folders: with the names: `input`, `faces` and `temp`. These 3 foders must be empty.
- The face pictures which are to be recognized against the data base, should be stored in `/sdcard/distressnet/MStorm/StreamFDPic` folder. Use command adb pull to pull the content. Check Sample photos folder.

#### Special case: Training:
- Acummulate all the photos for training in a folder called `Orig` and push the folder to phone's `/sdcard/distressnet/Madoop4/FaceReco/Orig` directory. Also,  Create another empty folder with name `/sdcard/distressnet/Madoop4/FaceReco/Train`. Use the command `adb push FaceReco /sdcard/distressnet/Madoop4/`. The training photos should be named in this format: `s01_01.jpg` where `s01` is the person's identifier.
- Also, don't forget to put other files as stated in the earlier section.
- From the menu (top-right corner), select Train Photos. After certain time, the App might show a sign to stop the app as it is busy. Press the wait option.
- After training, The `facemodel.xml` will be stored inside `/sdcard/distressnet/Madoop4/FaceReco` folder. This file is to be copied to all the phones in the directory stated in earlier section.
- It will take long time to process the photos and then training.


## Executing The APP
- The tasktracker and Job tracker might be running from previous use. Go to `Setting`->`Apps`->select `Distressnet NG` -> tap on Force Quit. This will stop all the trackers.
- Make sure the photos are stored in the correct folder.
- Clik on menu and then purge DFS.
- Click on Upload. It will preprocess the photos and upload to HDFS.
- On menue item (top right corner), check the boxes for DataNode and Task Tracker.
- click on `Go` button. It will do the face recognition and show the detected face with their name from a face database.  
- If you want to upload new photos, click on Menu and then Purge DFS.
- If using multiple phones, click `upload` button from all phones. But click `Go` button from one phone only.


## Instructions for Madoop Server

#### Installing prerequisites
- Oracle Java 1.8. See the instructions here. http://tipsonubuntu.com/2016/07/31/install-oracle-java-8-9-ubuntu-16-04-linux-mint-18/
- Ant `sudo apt-get install ant`
- Openssh server `sudo apt-get install openssh-server`
- GNS-service-desktop. Please check the instructions inside the LENSS/GNS repository.


#### Setting up hostnames:
- Since Hadoop uses names instead of IP as destination address, all the nodes should be configured correctly such that they can be reached with the hostnames.
- The EPC should have a static IP. It should also run GNS-service-desktop.
- Make sure the hostname `lens-epc` (This should be same as the accountname used in the GNSservice) is the hostname of the EPC. This should be written in stored in the files `/etc/hostname` and in  `/etc/hosts`.
- Use `ssh-copy-id lenss-epc` for password-less ssh to the own machine. Hadoop uses multiple ssh connections to own machine to start different services.
- Make sure that we can ping the phone using its hostname such as `ping sagor10` (where the other device use sagor10 as accountname in GNS). This ping should be tested from both server and phone.


#### Setup for DNS (through GNS)
- If the WiFi phones did not get the correct DNS address where the GNS server is running, statically assign the proper address.
- The phones connected to LTE also need this DNS service such that they can communicate using hostname. In the current setup, [NextEPC](http://nextepc.org) is used to configure the EPC. Its configuration file is stored in `/etc/nextepc/pgw.conf`. To point to the local DNS server, edit the lines under `dns:`.


#### Building the executable
- Get the source code from Github LENSS MADOOP repository.
- Build the source code. (go to the hadoop-0.20.2 folder and run `ant`)

#### Running MADOOP Server
- Get the `madoop-server-with-gns.tar.gz` from the github repository and extract the files.
- In conf folder of the Android APP, in file `mapred-site.xml`, replace "lenss_epc" with the new machine name (same as the account name used in the GNS-service-desktop). Same applies to another two files: `core-site.xml` and `slaves`.
- The hadoop server tries to logon to the own computer via ssh to start the services. Thus it would be helpful to store the ssl keys (ssh-keygen and ssh-copy-id).
- To run madoop, you can use ``sudo ./madoop_startup.sh start`` and for stopping the service, use `sudo ./madoop_startup.sh stop`




#### Troubleshooting
- The MADOOP app generates logs which are stored in `/sdcard/distressnet/Madoop4/log`.
- If the logfile in the phone shows error like *address already in use*, restart the phone and Madoop master.
- At the server side, the logs are stored at ``/home/lenss_epc/madoop/hadoop-0.20.2/logs/``
- Sometimes the EPC IP Tables creates problem in routing data from LTE to WiFi or vice versa. In this case, resetting the IP table in the EPC allows the data to be routed correctly.
- Sometimes the application might not have allowed some permission. Go to `Setting`->`Apps`->`DistressnetNG`->`Permission` and allow all the permission.






## Contents of different files in the server:

Content of `mapred-site.xml`:
```
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<!-- Put site-specific property overrides in this file. -->

<configuration>

  <property>
    <name>mapred.job.tracker</name>
    <value>lenss-epc:9001</value>
  </property>
  <property>
    <name>mapred.local.dir</name>
    <value>/tmp/hadoop/mapred/local</value>
  </property>
  <property>
    <name>mapred.system.dir</name>
    <value>/tmp/hadoop/mapred/system</value>
  </property>

</configuration>
```
Content of `core-site.xml`

```
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<!-- Put site-specific property overrides in this file. -->

<configuration>

  <property>
    <name>fs.default.name</name>
    <value>hdfs://lenss-epc:9000/</value>
  </property>
  <property>
    <name>hadoop.tmp.dir</name>
    <value>/tmp/hadoop/tmp</value>
    <description>A base for other temporary directories.</description>
  </property>

</configuration>
```
Contents of `slaves`

```
lenss-epc
#vm-1
#vm-2
#vm-3
#vm-4
```

## Statically mapping the hostnames on each devices.
If Setting up DNS masq is troublesome, then we can run the application by statically mapping hostname to IP address on each phones. `/etc/hosts` is a read-only file in Android. We have to access using root to change this file. I used these commands:

```
adb shell
su root
mount -o rw,remount /system
vi /etc/hosts
```
In this folder put all the hostname mapping for EPC and mobile devices.
