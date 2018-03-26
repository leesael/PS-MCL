#!/bin/bash

cd $(dirname $(readlink -f $0))

#help message
help()
{
    echo "B-MCL : Balanced Regularized Markov Clustering"
    echo " "
    echo "Usage: $0 [INPUT (Graph File)] [OutputPath] [Coarse Level] [Balance Factor] [epsilon] [rand_seed]"
    echo "CoarseMode        : -sc or -hem"
    echo "Coarse Level      : non-negative Integer"
    echo "Balance Factor    : non-negative Double"
	echo "epsilon			: run until the error is under epsilon"
	echo "rand_seed			: random seed number"
}


QUERY="java -jar bin/PS-MCL.jar -mcl $1 $2 -hem $3 $4 -reg 1 $5 0"


		
echo $QUERY
echo =====================
$QUERY
