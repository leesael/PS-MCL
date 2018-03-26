#!/bin/bash

cd $(dirname $(readlink -f $0))

#help message
help()
{
    echo "PS-MCL - Parallel Shotgun Coarsening Markov Clustering"
    echo " "
    echo "Usage: $0 [cluster-node] [mapping file] [reference] [refType] [min] [max]"
    echo "cluster-node		: *.assign file"
    echo "mapping file		: node numbering file"
    echo "reference			: reference cluster file"
	echo "refType 			: -row or -col, -row if each reference cluster represented in one row."
	echo "max				: maximum cluster size, if not set, it's Integer.Maxvalue"
	echo "min				: minimum cluster size, if not set, it's 0"
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
