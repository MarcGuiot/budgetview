#!/bin/bash
#jour period en second
period=`expr 7 \* 86400`
if [ "$#" -gt 0 ]
then
 period=`expr $1 \* 86400`
fi
cat - | awk -f filter.awk -v period=$period | grep INFO: | awk -f count.awk
