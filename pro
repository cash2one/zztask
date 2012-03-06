#!/bin/sh  
#memcached auto-start   
#  
# description: Auto-starts memcached  
# processname: memcached  
# pidfile: /var/memcached.pid   
 
case $1 in  
p)  
    mvn clean package -Pproduction -Dmaven.test.skip
    ;;
pt)
	mvn clean package -Ptest -Dmaven.test.skip
	;;
ci)
	mvn clean install -Dmaven.test.skip
	;;
ee)
	mvn eclipse:eclipse -Dwtpversion=2.0
	;;
ec)
	mvn eclipse:clean
	;;
*)  
    echo 'p:package production; pt:package test; ci:clean install; ee: eclipse'
    ;;  
esac  
  
exit 0
