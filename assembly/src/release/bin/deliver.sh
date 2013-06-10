#!/bin/sh

TMEMP_DIR=`grep "^temp_directory=" ../conf/default.properties | cut -d= -f2` 
if [ -z "$TEMP_DIR" ]; then
	TEMP_DIR=../temp
fi
PATH=$TMEMP_DIR/deliver.input.$$

cat > $PATH

SENDER=$1
shift
RECIPIENTS=$*

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

RUN_CMD="$JAVA_HOME/bin/java \
	-classpath $_LIBJARS \
	-Dlog4j.configuration=../conf/log4j.properties \
	com.hs.mail.Deliver -c ../conf/default.properties -p $PATH -f $SENDER -r $RECIPIENTS

$RUN_CMD

exit $?
