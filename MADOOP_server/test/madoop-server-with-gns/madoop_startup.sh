#! /bin/sh

# first change pwd to the current directory where this script is stored.
cd "$(dirname "$0")"

#change this script
#/bin/su sbhunia

# Carry out specific functions when asked to by the system
case "$1" in
  start)
    echo "Starting Madoop"
    /bin/su sbhunia ./bootstrap.sh reset2  > madoop_terminal.out &
    ;;
  stop)
    echo "Stopping Madoop"
    /bin/su sbhunia ./bootstrap.sh stop  > madoop_terminal.out &
    ;;
  *)
    echo "Usage: gns-service {start|stop}"
    exit 1
    ;;
esac

exit 0


