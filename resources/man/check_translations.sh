#!/bin/bash

PROGRAM=`basename $0`
BASE_DIR=`dirname $0`
CUR_DIR=`pwd`
cd "${BASE_DIR}"
BASE_DIR=`pwd`
cd "${CUR_DIR}"
case "${BASE_DIR}" in
  "/") SCRIPT="/${PROGRAM}" ;;
    *) SCRIPT="${BASE_DIR}/${PROGRAM}" ;;
esac

#-------------------------------------------------------------------------------

cd "${BASE_DIR}"
DIR=`dirname "${BASE_DIR}"`
BUILD_DIR=`dirname "${DIR}"`"/build"
JAR="${BUILD_DIR}/musescore2html.jar"
CLASS="musescore2html.Translations"

if [ ! -f "${JAR}" ]
then
  echo "${JAR} not present"
  exit
fi

LANGUAGES="en nl"

for LANG in ${LANGUAGES}
	do
	java -cp "${JAR}" "${CLASS}" -l "${LANG}"
	echo "--------------------------------------------------------------------------------"
	done
