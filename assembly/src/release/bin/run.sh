#!/bin/sh

usage()
{
    echo "Usage: $0 {start|stop|restart}"
    exit 1
}

[ $# -gt 0 ] || usage

if [ -z "$JAVA_HOME" ] ;  then
	echo "Cannot find JAVA_HOME. Please set JAVA_HOME."
    exit 1
fi

unset _LIBJARS

_LIBJARS=../classes

for i in ../lib/* ; do
	if [ "$_LIBJARS" != "" ]; then
		_LIBJARS=${_LIBJARS}:$i
	else
		_LIBJARS=$i
	fi
done

JAVA_OPTS="$JAVA_OPTS -Xms256m -Xmx256m"

RUN_CMD="$JAVA_HOME/bin/java \
	-classpath $_LIBJARS \
	$JAVA_OPTS \
	-Dlog4j.configuration=file:../conf/log4j.properties \
	com.hs.mail.container.simple.SimpleSpringContainer ../conf/applicationContext.xml"

ACTION=$1
APP_PID=app.pid
APP_CONSOLE=app.console

case "$ACTION" in
  start)
        if [ -f $APP_PID ]
        then
            echo "Already Running!!"
            exit 1
        fi

	    echo Application starts...
	    nohup sh -c "exec $RUN_CMD >>$APP_CONSOLE 2>&1" >/dev/null &
	    echo $! > $APP_PID
        echo "Application running pid="`cat $APP_PID`
	    ;;

  stop)
        PID=`cat $APP_PID 2>/dev/null`
        echo "Shutting down Application: $PID"
        kill $PID 2>/dev/null
        sleep 2
        kill -9 $PID 2>/dev/null
        rm -f $APP_PID
        echo "STOPPED `date`" >>$APP_CONSOLE
        ;;

  restart)
        $0 stop $*
        sleep 5
        $0 start $*
        ;;

*)
        usage
        ;;
esac

exit 0

