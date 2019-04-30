#!/bin/env bash

rm -rf pydoc

files=`find shramp -name '*.py' | egrep -v '__' | sed 's/\//\./' | sed 's/\.py//'`

mkdir pydoc

pydoc3 -w shramp $files

mv *.html pydoc
