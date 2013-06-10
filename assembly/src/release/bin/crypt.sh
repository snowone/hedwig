#!/bin/sh

usage()
{
	echo "Usage: $0 scheme password"
	echo "scheme:"
	echo "crypt            Traditional DES-crypted password"
	echo "  md5            MD5 based salted password"
	exit 1
}

[ $# -gt 1 ] || usage

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

RUN_CMD="$JAVA_HOME/bin/java -classpath $_LIBJARS

SCHEME=$1

case "$SCHEME" in
  crypt)
        $RUN_CMD com.hs.mail.security.login.CryptPasswordEncoder $2
        ;;
  md5) 
        $RUN_CMD com.hs.mail.security.login.MD5PasswordEncoder $2
        ;;
*)
        usage
        ;;
esac

exit 0
