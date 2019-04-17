#!/bin/bash

# bin/stop-all.sh
# sleep 1
# bin/clean-all.sh
# sleep 1
echo "Cleaning HDFS and LOG files"
rm /tmp/hadoop* /tmp/hsperfdata_wei/ /tmp/Jetty_0_0_0_0_500* -rf
rm logs -rf
# bin/hadoop namenode -format
# sleep 1
# bin/start-all.sh
# sleep 1
# bin/hadoop fs -put conf input
# sleep 2
# ./run.sh run dfs
#bin/hadoop jar hadoop-*-examples.jar wordcount input output
#bin/hadoop fs -get output output cat output/*
#bin/stop-all.sh
