#!/bin/bash

# resolve links - $0 may be a softlink
THIS="$0"
while [ -h "$THIS" ]; do
  ls=`ls -ld "$THIS"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    THIS="$link"
  else
    THIS=`dirname "$THIS"`/"$link"
  fi
done


Usage(){
  echo "Usage: run.sh COMMAND"
  echo "where COMMAND is one of:"
  echo "  run               run the mapreduce code"
  echo "  kernel            recompile hadoop kernel and run the mapreduce code"
  echo "  app               recompile mapreduce code and run it"
  echo "  all               recompile everything and run the mapreduce code"
}

# if no args specified, show usage
if [ $# = 0 ]; then
    Usage
    exit 1
fi

# echo "Entering hadoop....."

# get arguments
COMMAND=$1
shift
FS=$1


# some directories
THIS_DIR=`dirname "$THIS"`
HADOOP_HOME=`cd "$THIS_DIR" ; pwd`
# echo rm -fr $HADOOP_HOME/output

# figure out which class to run
if [ "$COMMAND" = "run" ] ; then
  echo " "  
elif [ "$COMMAND" = "kernel" ] ; then
  echo " "  
  ant clean-classes
  ant compile
elif [ "$COMMAND" = "app" ] ; then
  echo " "  
  # ant clean-examples
  # ant compile-examples
  # ant examples
  ant compile-users
elif [ "$COMMAND" = "all" ] ; then
  echo " "  
  ant clean
  # ant compile-examples
  ant compile-users
else
  echo " "  
  Usage
  exit 1
fi

echo "rm output folder locally"
rm -fr $HADOOP_HOME/output

if [ "$FS" = "dfs" ] ; then
  echo "rm output folder on DFS"
  $HADOOP_HOME/bin/hadoop dfs -rmr output
fi

echo "Run WordCount program"
# $HADOOP_HOME/bin/hadoop org.apache.hadoop.examples.WordCount input output
$HADOOP_HOME/bin/hadoop edu.tamu.lenss.madoop4.WordCount input output
# $HADOOP_HOME/bin/hadoop jar $HADOOP_HOME/build/hadoop-*-dev-examples.jar wordcount input output
$HADOOP_HOME/bin/hadoop dfs -get output output
cat $HADOOP_HOME/output/*

