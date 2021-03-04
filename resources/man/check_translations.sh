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
CompareUseJar()
{
DIR=`dirname "${BASE_DIR}"`
BUILD_DIR=`dirname "${DIR}"`"/build"
JAR="${BUILD_DIR}/musescore2html.jar"
CLASS="musescore2html.Translations"
DIR=`dirname "${DIR}"`

cd "${DIR}"
ant dist

if [ ! -f "${JAR}" ]
then
  echo "${JAR} not present"
  exit
fi

for LANG in ${LANGUAGES}
	do
	java -cp "${JAR}" "${CLASS}" -l "${LANG}"
	echo "--------------------------------------------------------------------------------"
	done
}

#-------------------------------------------------------------------------------
Compare()
{
DIR=`dirname "${BASE_DIR}"`
DIR=`dirname "${DIR}"`

FILE0="src/musescore2html/Translations.properties"
FILES="src/musescore2html/Translations_en.properties
src/musescore2html/Translations_nl.properties"

TMP_FILE0="/tmp/Translations.properties.0"
cat "${DIR}/${FILE0}" | sed 's/ [=] .*//g' | grep -v "^$" | sort > "${TMP_FILE0}"
echo "Duplicate keys in '${FILE0}':"
uniq -d "${TMP_FILE0}"
echo

cat "${DIR}/${FILE0}" | sed 's/ = .*//g' | grep -v "^$" | sort -u > "${TMP_FILE0}"

N=`echo "${FILES}" | wc -l`
N=`expr ${N} + 0`
I=1
while [ ${I} -le ${N} ]
  do
  FILE=`echo "${FILES}" | head -n ${I} | tail -n 1`
  TMP_FILE="/tmp/Translations.properties.${I}"
  cat "${DIR}/${FILE}" | sed 's/ = .*//g' | grep -v "^$" | sort -u > "${TMP_FILE}"
  echo "Duplicate keys in '${FILE}':"
  cat "${DIR}/${FILE}" | sed 's/ = .*//g' | grep -v "^$" | sort | uniq -d
  echo
  echo "Missing keys in '${FILE}':"
  comm -23 "${TMP_FILE0}" "${TMP_FILE}"
  echo
  echo "Unknown keys in '${FILE}':"
  comm -13 "${TMP_FILE0}" "${TMP_FILE}"
  echo
  I=`expr ${I} + 1`
  done
}

#-------------------------------------------------------------------------------

DIR=`dirname "${BASE_DIR}"`
DIR=`dirname "${DIR}"`

LANGUAGES="en nl"

if [ -z "$1" ]
then
  Compare
else
  CompareUseJar
fi
