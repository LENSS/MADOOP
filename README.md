# MADOOP
This application is an example of Mobile Edge Computing using Mobile Hadoop and MapReduce.


## Network Architecture

- The Network consists of mainly these components:
  - The EPC is the main server where Madoop master, DNSMasq and other services should be running.
  - A WiFi access point. The access point is only for providing the communication backbone. It __must not__ run any DHCP server. Since the EPC is running DHCP server.
  - Android mobile phones. These phones will run the binary APK. Android phones get their IP from the DHCP server running at the EPC.
- This setup requires that there is a DNS (dnsmasq) running in the EPC. This dnsmasq provides a unique hostname to the EPC. The mobile clients of the MADOOP tries to connect to the MADOOp master using this hostname.  

## MADOOP Client on Android phones
#### Building the APK (In Ubuntu 16.04):
- MADOOP source code for Android could be downloaded from the LENSS repository.
- MADOOP can now work with the newest version of Android SDK and NDK. There are some libraries that are not supported by Android SDK and NDK anymore.
- If Android studio was used on the current computer before, it downloded some of the latest libraries in the `/home/username` folder which should be removed first. Remove `Android`, `.android` and `AndroidStudio3.2` (check for other versions as well) folders.
- Download Android Studio 3.2.
- Open the MADOOP android project in Android studio and follow the default instructions.
- Install SDK when asked. But for NDK, download NDK version 16 from this repository. https://developer.android.com/ndk/downloads/older_releases and store the extracted files inside  `~/Android/Sdk/ndk-bundle/android-ndk-r16b`.
- Select the project and then go to File->Project structure. Under SDK Location, specify the correct NDK in the Android NDK location box.
- When building the project, the Android studio might suggest to update Gradle script. Do not accept it.
- Go to Build->Build Bundles/APK->Build APK.This should build the APK inside the folder `/app/build/outputs/apk/debug`.
- Use `adb install app-debug.apk` command to install the app to a phone.  

#### Setting the phone (MADOOP_client) before using MADOOP
- The assiciated files to run MADDOP are stored in `/MADOOP_Client/Associated_Files` folder.
- The Madoop facerecognition app requires a face-database. Store these 3 files `facemodel.xml`, `mask.jpg` and `shape_predictor_68_face_landmarks.dat` files in `/sdcard/Documents/Madoop4/` folder of each phone that uses this App. Use command `adb push` to transfer the files.
- Also, in the same folder, create 3 empty folders: with the names: `input`, `faces` and `temp`. These 3 foders must be empty.
- The face pictures which are to be recognized against the data base, should be stored in `/sdcard/StreamFDPic/` folder. Use command adb pull to pull the content. Check Sample photos folder.

#### Special case: Training:
- Acummulate all the photos for training in a folder called `Orig` and push the folder to phone's `/sdcard/Documents/Orig` directory `adb push Orig /sdcard/Documents`. The training photos should be named in this format: `s01_01.jpg` where `s01` is the person's identifier.
- Create another empty folder with name `/sdcard/Documents/Train`.
- Also, don't forget to put other files as stated in the earlier section.
- From the menu (top-right corner), select Train Photos.
- After training, The face database will be stored inside `/sdcard/Documents/FaceReco` folder.

## Instructions for Madoop Server

#### Accessing the server
- The EPC can be accessed with username `lenss-epc` and password `lenss_people`. The IP address is always 192.168.1.250 and the hostname is lens-epc

#### Installing prerequisites
- Oracle Java 1.8. See the instructions here. http://tipsonubuntu.com/2016/07/31/install-oracle-java-8-9-ubuntu-16-04-linux-mint-18/
- Ant `sudo apt-get install ant`
- Openssh server `sudo apt-get install openssh-server`


#### Setting up hostnames:
- Since Hadoop uses names instead of IP as destination address, all the nodes should be configured correctly such that they can be reached with the hostnames.
- The EPC should have a static IP of `192.168.1.250`.
- Make sure the hostname `lens-epc` is the hostname of the EPC. This should be written in stored in the files `/etc/hostname` and in  `/etc/hosts`, there should be an entry `192.168.1.250    lenss-epc`. Remember to disable the line `192.168.1.1  lenss-epc`
- In `/etc/hosts` file, add mapping entries for each phone (phone name to its ip). For each phone there should be an entry `192.168.1.131    phone-192-168-1-131`. Do it for each phone IP. The ip of the phone is randomly assigned by DHCP, so we need to check from the phone and add it to this file).
- Make sure that we can ping the phone using its hostname such as `ping phone-192-168-1-131`. This ping should be tested from both server and phone.


#### Setting up dnsmasq
- DNSMasq can be installed on Ubuntu using command lines. The instructions can be found at https://help.ubuntu.com/community/Dnsmasq. `sudo apt-get install dnsmasq`
- Edit the `/etc/dnsmasq.conf` as in the example provided in the code. Change the `interface=en01` and `server=192.168.1.250` to the particular interface and address respectively where you want the dhcp to run.
- Make sure dnsmasq is running.
- Start Dnsmasq using command `sudo service dnsmasq start`
- Check the status using command `sudo service dnsmasq status`


#### Setup for LTE interface
- The phones connected to LTE also need this DNS service such that they can communicate using hostname.In the current setup, [NextEPC](http://nextepc.org) is used to configure the EPC. Its configuration file is stored in `/etc/nextepc/pgw.conf`. To point to the local DNS server, edit the lines under `dns:`.


#### Running MADOOP Server
- Get the source code from Github. `git clone https://github.tamu.edu/sbhunia/MADOOP_server`
- Build the source code. (go to the hadoop-0.20.2 folder and run `ant`)
- In conf folder of the Android APP, in file `mapred-site.xml`, replace "lenss_epc" with the new machine name. Same applies to another two files: `core-site.xml` and `slaves`.
- The hadoop server tries to logon to the own computer via ssh to start the services. Thus it would be helpful to store the ssl keys (ssh-keygen and ssh-copy-id).
- To run madoop, just type ``./bootstrap.sh reset2`` in madoop root folder.



#### Troubleshooting
- The MADOOP app generates logs which are stored in `sdcard/Madoop4/log`.
- If the logfile in the phone shows error like *address already in use*, restart the phone and Madoop master.
- At the server side, the logs are stored at ``/home/lenss_epc/madoop/hadoop-0.20.2/logs/``




## Executing The APP
- The tasktracker and Job tracker might be running from previous use. Go to Setting->Apps->select Distressnet NG -> tap on Force Quit. This will stop all the trackers.
- Open the Madoop app. It askes for the current IP. Check the IP address of the phone (Setting->WiFi->click on the gear icon on top->see the IP address) and save it on the APP.
- Make sure the photos are stored in the correct folder.
- Click on Upload. It will preprocess the photos and upload to HDFS.
- On menue item (top right corner), check the boxes for DataNode and Task Tracker.
- click on `Go` button. It will do the face recognition and show the detected face with their name from a face database.  
- If you want to upload new photos, click on Menu and then Purge DFS.
- If using multiple phones, click `upload` button from all phones. But click `Go` button from one phone only.


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
