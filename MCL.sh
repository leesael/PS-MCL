#!/bin/bash

cd $(dirname $(readlink -f $0))

#help message
help()
{
    echo "MCL - Markov Clustering"
    echo " "
    echo "Usage: $0 [INPUT (Graph File)] [OutputPath] [epsilon] [rand_seed]"
    echo "epsilon			: run until the error is under epsilon"
	echo "rand_seed			: random seed number"
}

QUERY="java -jar bin/PS-MCL.jar -mcl $1 $2 -hem 0 0 -basic 1 $3 0"
		
echo $QUERY
echo =====================
$QUERY
