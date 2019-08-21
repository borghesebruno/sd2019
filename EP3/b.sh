#!/bin/bash

rm -rf files
mkdir files
cd files

no_of_files=3;
counter=1;
while [ $counter -le $no_of_files ];
do
    echo file $counter;
    rand=`shuf -i 10-100 -n 1`;
    touch random-$counter-$rand.txt;
    counter=`expr $counter + 1`;
done

cd ..