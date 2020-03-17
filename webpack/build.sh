#!/bin/bash

npm run build
if [ $? -eq 0 ]
then
    cp -r dist/* ../src/main/resources/public/
fi
