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

LIB_DIR=`dirname "${BASE_DIR}"`"/lib"
cd "${BASE_DIR}"

NAME=`echo "${PROGRAM}" | sed 's/^man_//g' | sed 's/[.]sh$//g'`
LANGUAGES="en nl"

for LANG in ${LANGUAGES}
	do
	export "LANG=${LANG}"
	FILENAME="${NAME}_${LANG}"
	mandoc -K utf-8 -T man -mdoc  "${FILENAME}.mdoc" >  "${FILENAME}.man.1"
	mandoc -K utf-8 -T html -mdoc  "${FILENAME}.mdoc" >  "${FILENAME}.html"
	mandoc -K utf-8 -T pdf -mdoc  "${FILENAME}.mdoc" >  "${FILENAME}.pdf"
	java '-Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl' '-Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl' -cp "${LIB_DIR}/xml-apis.jar:${LIB_DIR}/xercesImpl.jar:${LIB_DIR}/saxon9he.jar"  "net.sf.saxon.Transform" "-s:${FILENAME}.html" "-xsl:m2h.xsl" "-o:../help/MuseScore2Html_Commandline_${LANG}.html"
	done
