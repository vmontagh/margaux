#!/bin/bash
# file: clean.sh

echo "Kill all the previous H2 Databases."
while read p; do
  kill $p
done <scripts/processID.log

sleep 1

echo "Remove the database IDs"
rm scripts/processID.log

echo "Remove the previous database logs"
#rm db/alloy_props*
pushd db
 find alloy_props* -type f -delete
popd

echo "Remove all the temporary generated files."
#rm relational_props/tmp/*
pushd relational_props/tmp/
find . -type f -delete
popd 

echo "Remove all the java logs."
#rm tmp/*.log*
pushd tmp
find *.log* -type f -delete
popd


echo "Start H2 database"
java -cp lib/h2*.jar org.h2.tools.Server & 
pID=`echo $!`
echo "A H2 database created as the process id:" $pID
echo $pID >> scripts/processID.log

echo "REDAY TO START THE FRAMEWORK............"
