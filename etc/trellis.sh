#!/bin/sh

. /etc/init.d/functions

BASEDIR="/var/lib/trellis/"
USER="trellis"

PROG="trellis"
CMD="java -jar ./build/libs/trellis.jar server config.yml"
PIDFILE="${BASEDIR}/trellis.pid"
RETVAL=0

start () {
    echo -n $"Starting ${PROG} "
    if ( [ -f ${PIDFILE} ] )
    then
        echo -n "${PROG} is already running."
        failure ; echo
        RETVAL=1
        return
    fi
    touch ${PIDFILE} ; chown ${USER} ${PIDFILE}
    runuser ${USER} -c "${CMD} > /dev/null &
    echo \$! > ${PIDFILE}"
    success ; echo
}

stop () {
    echo -n $"Stopping ${PROG} "
    if ( [ ! -f ${PIDFILE} ] )
    then
        echo -n "${PROG} is not running."
        failure ; echo
        RETVAL=1
        return
    fi
    killproc -p ${PIDFILE}
    RETVAL=$?
    echo
    if [ $RETVAL -eq 0 ] ; then
        rm -f ${PIDFILE}
    fi
}

status () {
    if ( [ -f ${PIDFILE} ] )
    then
        echo -n "${PROG} is running."
	else
        echo -n "${PROG} is not running."
    fi
	echo
}

restart () {
    stop
    start
}


# See how we were called.
case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  status)
    status
    ;;
  restart)
    restart
    ;;
  *)
    echo $"Usage: $0 {start|stop}"
    RETVAL=2
    ;;
esac

exit $RETVAL
