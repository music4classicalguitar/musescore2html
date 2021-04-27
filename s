#!/bin/bash

PROGRAM="$0"
BASE_DIR=`dirname $0`

# Handle symlinks
while [ -L "$PROGRAM" ]; do
	PROGRAM=`readlink ${READLINK_OPTION} "$PROGRAM"`
	BASE_DIR=`dirname "${PROGRAM}"`
done
pushd "`dirname \"$PROGRAM\"`" > /dev/null

popd > /dev/null

#-------------------------------------------------------------------------------
cd "${BASE_DIR}"

LINES="Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 1 Andante
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 17 Moderato
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 18 Andantino
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 2 Andantino
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 3 Larghetto
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 5 Allegretto
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 12 Andantino moderato
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 13 Andante
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 14 Andante
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 15 Allegretto
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 16 Moderato
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 19 Moderato
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 20 Tempo di minuetto
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 21 Andante
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 22 Allegretto
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 23 Andante
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 24 Allegro moderato
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 4
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 6
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 10
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 7 Andante
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 8 Allegretto
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 9 Andante
Fernando Sor - Op. 35 Vingt quatre exercices très faciles - No. 11 Allegretto"

LINES=`cat s.txt | sed 's/[.]mscz//g' | sort -r`
N=`echo "${LINES}" | wc -l`
I=1
while [ ${I} -le ${N} ]
  do
  LINE=`echo "${LINES}" | head -n ${I} | tail -n 1`
  ARGS="${ARGS} \"${LINE}\""
  I=`expr ${I} + 1`
  done
#echo "${ARGS}"

eval java -cp build/dist/bin/musescore2html_1.2/resources/lib/musescore2html.jar musescore2html.NaturalOrderComparator ${ARGS}
