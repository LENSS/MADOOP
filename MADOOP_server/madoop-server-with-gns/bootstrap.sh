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
  echo "Usage: bootstrap.sh <COMMAND>"
  echo "where COMMAND is one of:"
  echo "  reset          restart MR framework"
  echo "  reset2         restart MR framework"
  echo "  restore        restart MR framework + dfs"
  echo "  restore2       restart MR framework + dfs"
  echo "  stop           stop MR framework"
  echo "  dfs            upload input to DFS"
  echo "  test           run test code"
  echo "  dfstest        dfs + run"
  echo "  all            do all above"
  echo "  all2           do all above"
}

# if no args specified, show usage
if [ $# = 0 ]; then
    Usage
    exit 1
fi

# echo "Entering hadoop....."

# get arguments
CMD=$1

if [[ ( "$CMD" = "stop" ) ]]; then
  echo "Stopping all..."
  bin/stop-all.sh
fi

if [[ ( "$CMD" = "reset" || "$CMD" = "restore" || "$CMD" = "all" ) ]]; then
  echo "Stopping all..."
  bin/stop-all.sh
  sleep 1
  echo "Cleaning all..."
  bin/clean-all.sh
  sleep 1
  # rm /tmp/hadoop* /tmp/hsperfdata_wei/ /tmp/Jetty_0_0_0_0_500* -rf
  # rm logs -rf
  echo "Formating DFS..."
  bin/hadoop namenode -format
  sleep 1
  echo "Starting all..."
  bin/start-all.sh
fi

if [[ ( "$CMD" = "reset2" || "$CMD" = "restore2" || "$CMD" = "all2" ) ]]; then
  echo "Stopping all..."
  bin/stop-all.sh
  sleep 1
  echo "Cleaning all..."
  bin/clean-all.sh
  sleep 1
  # rm /tmp/hadoop* /tmp/hsperfdata_wei/ /tmp/Jetty_0_0_0_0_500* -rf
  # rm logs -rf
  echo "Formating DFS..."
  bin/hadoop namenode -format
  sleep 1
  echo "Starting all..."
  bin/start-all2.sh
fi


if [[ ( "$CMD" = "dfs" || "$CMD" = "restore" || "$CMD" = "restore2" ) ]]; then
  echo "Uploading input to DFS..."
  sleep 1
  bin/hadoop fs -put bin input
fi

if [[ ( "$CMD" = "test" ) ]]; then
  echo "Running the test..."
  sleep 1
  ./run.sh run dfs
fi

if [[ ( "$CMD" = "dfstest" || "$CMD" = "all" || "$CMD" = "all2" ) ]]; then
  echo "Uploading input to DFS..."
  sleep 1
  bin/hadoop fs -put bin/* input
  sleep 2
  echo "Running the test..."
  ./run.sh run dfs
fi

#bin/hadoop jar hadoop-*-examples.jar wordcount input output
#bin/hadoop fs -get output output cat output/*
#bin/stop-all.sh
