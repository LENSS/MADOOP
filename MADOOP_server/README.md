# MADOOP
Mobile Hadoop and MapReduce


## Network Architecture

- The Network consists of mainly these components:
  - The EPC is the main server where Madoop master, DNSMasq and other services should be running.
  - A WiFi access point. The access point is only for providing the communication backbone. It __must not__ run any DHCP server. Since the EPC is running DHCP server.
  - Android mobile phones. These phones will run the binary APK. Android phones get their IP from the DHCP server running at the EPC.
- This setup requires that there is a DNS (dnsmasq) running in the EPC. This dnsmasq provides a unique hostname to the EPC. The mobile clients of the MADOOP tries to connect to the MADOOp master using this hostname.  



## Setting the phone (MADOOP_client) before using MADOOP
- For the application to work, this folder must be copied to every phone. `/sdcard/Documents/Madoop4/`. However, there are 3 folders inside this folder: input, faces and temp which to be emptied before usage.
- The face pictures should be stored in `/sdcard/StreamFDPic/` folder. Use command adb pull to pull the content. Use push command to copy folder from computer to phone.  `adb push gns_workplace/Madoop4 /sdcard/Documents/`

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
