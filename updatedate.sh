#!/bin/env bash

old="24 April"
new="29 April"

find ./                -name '*.md'   | xargs sed -i 's/'"$old"'/'"$new"'/'

find ./shramp/         -name '*.java' | xargs sed -i 's/'"$old"'/'"$new"'/'

find ./python/         -name '*.py'   | xargs sed -i 's/'"$old"'/'"$new"'/'

find ./app/src/main/rs -name '*.rs'   | xargs sed -i 's/'"$old"'/'"$new"'/'
