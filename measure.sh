#!/bin/bash

cd $(dirname $(readlink -f $0))

#help message
help()
{
    echo "PS-MCL - Parallel Shotgun Coarsening Markov Clustering"
    echo " "
    echo "Usage: $0 [INPUT (Graph File)] [OutputPath] [CoarseMode] [Coarse Level] [Balance Factor] [MCL Mode] [Number of Thread] [epsilon] [rand_seed] [skip rate]"
    echo "CoarseMode        : -sc or -hem"
    echo "Coarse Level      : non-negative Integer"
    echo "Balance Factor    : non-negative Double"
    echo "MCL Mode          : -reg or -basic"
    echo "Number of Thread  : positive Integer"
	echo "epsilon			: run until the error is under epsilon"
	echo "rand_seed			: random seed number"
	echo "skip rate			: skip rate of nodes in shotgun coarsening"
	echo "reference			: reference cluster file"
	echo "mapping file		: node numbering file"
}

if [ $# -lt 6 ];then
    echo "Error: Please provide parameters"
    echo "========================================="
    help
    exit 127
fi




QUERY="java -jar bin/PS-MCL.jar"
for arg in "$@"; do 
    QUERY="$QUERY ${arg}"
done

		
echo $QUERY
echo =====================
$QUERY
