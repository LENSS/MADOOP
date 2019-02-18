#!/bin/bash



# Run a shell command on all slave hosts.
#
# Environment Variables
#
#   HADOOP_SLAVES    File naming remote hosts.
#     Default is ${HADOOP_CONF_DIR}/slaves.
#   HADOOP_CONF_DIR  Alternate conf dir. Default is ${HADOOP_HOME}/conf.
#   HADOOP_SLAVE_SLEEP Seconds to sleep between spawning remote commands.
#   HADOOP_SSH_OPTS Options passed to ssh when running remote commands.
##

echo "Stopping all"
bin/stop-all.sh
bin/bootstrap.sh

echo "Do ant cleaning"
ant clean
echo "Do ant compiling"
ant compile

bin=`cd ..; pwd`
tar -czf $bin/hadoop-0.20.2.tgz ../hadoop-0.20.2


for slave in `cat "conf/slaves"`; do

 echo $slave
  if [ "$slave" = "wei-dell" ] ; then
    echo "Skipping $slave"
  else
     scp $bin/hadoop-0.20.2.tgz $slave:/home/wei/postdoc/
     ssh $slave "cd /home/wei/postdoc/ ; rm hadoop-0.20.2 -rf ; tar -xzf hadoop-0.20.2.tgz"
  fi

   
done

wait

