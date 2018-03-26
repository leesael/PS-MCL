#!/bin/bash

cd $(dirname $(readlink -f $0))

#help message
help()
{
    echo "PS-MCL - Parallel Shotgun Coarsening Markov Clustering"
    echo " "
    echo "Usage: $0 [cluster-node] [reference] [mapping file]"
    echo "cluster-node		: *.assign file"
    echo "reference			: reference cluster file"
	echo "mapping file		: node numbering file"
}

if [ $# -lt 6 ];then
    echo "Error: Please provide parameters"
    echo "========================================="
    help
    exit 127
fi




QUERY="java -jar bin/PS-MCL.jar -measure"
for arg in "$@"; do 
    QUERY="$QUERY ${arg}"
done

		
echo $QUERY
echo =====================
$QUERY
