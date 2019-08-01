#!/bin/bash

CURBIN="${BASH_SOURCE-$0}"
CURBIN="$(dirname "${CURBIN}")"
CURBINDIR="$(cd "${CURBIN}"; pwd)"
# echo "CURBINDIR=$CURBINDIR"

JAVA_HOME="$CURBINDIR/jre8"

if [ ! -d "$JAVA_HOME" ]; then
  echo "JAVA_HOME=$JAVA_HOME not exist!"
  exit 1
fi

PATH=$JAVA_HOME/bin:$PATH
#echo "JAVA_HOME=$JAVA_HOME"
#echo "PATH=$PATH"

# HTTP GET
java -Dfile.encoding=UTF-8 -jar fdhyren-loadrunner-2.0.jar -n200 -c10 \
     url=http://localhost:7456/sdf/xxx?name=123 "$@"

# HTTP POST
#java -Dfile.encoding=UTF-8 -jar fdhyren-loadrunner-2.0.jar -n200 -c10 \
#     url=http://localhost:7456/sdf/xxx?name=123 post=./p.txt

# JDBC
#java -Dfile.encoding=UTF-8 -jar fdhyren-loadrunner-2.0.jar -n200 -c8 \
#     class=jdbc connType=conn driver=org.postgresql.Driver \
#     url=jdbc:postgresql://localhost:5432/postgres user=fd passwd=xxx123 \
#     sql="select * from test"
