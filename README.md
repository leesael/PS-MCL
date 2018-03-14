################################################################################
# PS-MCL : Parallel Shotgun Coarsening Markov Clustering.
#
# Author: InJae Yu (myhome9830@kaist.ac.kr), KAIST
#		  YongSub Lim(ddiyong@kaist.ac.kr), KAIST
#         U Kang (ukang@kaist.ac.kr), KAIST 
#
# Version : 1.0
# Date : August 17, 2015
# Main Contact: U Kang
#
#
################################################################################

1. General information

This is an implementation of PS-MCL (Parallel Shotgun Coarsening MCL)
This also includes implementations for the original MCL, R-MCL and MLR-MCL.


2. Minimal Environment

This tool needs
	(a) Java 1.8 or higher

This software was tested on Ubuntu.
	

3. How to run PS-MCL

IMPORTANT!
- Before you use it, please check that the script files are executable. If not,
you may manually modify the permission of scripts or you may type "make install"
to do the same work.
- You may type "make demo" if you want to just try PS-MCL.


To run MCL, you need to do the followings:
- prepare an undirected edge file.
  The format should be "node_index DELIMITER node_index". 
  

PS-MCL.jar is located in /bin

Command : ./PS-MCL [INPUT (Graph File Path)] [Output Directory] [CoarseMode] [Coarse Level] [Balance Factor] [MCL Mode] [Number of Thread] [epsilon] [rand_seed]

You should provide following arguments.

	(a) INPUT 	: path of input data
	(b) Output Directory : directory for output data
	(c) CoarseMode		: -sc or -hem. "-sc" for Shotgun Coarsening which is proposed method, and "-hem" for Heavy Edge Matching.
	(d) Coarse Level		: The number of coarsening step. Should be an non-negative integer.
	(e) Balance Factor		: Balance factor for B-MCL. If this is 0, R-MCL will be executed.
	(f) MCL Mode		: -basic or -reg. Run MCL with "-basic". Run R,B-MCL with "-reg"(B-MCL is specialized by R-MCL with balance factor larger than 0).
	(g) Number of Thread		: the number of threads to be used
	(h) epsilon		: run until the error is under epsilon
	(i) rand_seed		: random seed number
	(i) skip rate		: Float number from 0~1 when using "-sc" mode. 



4. Demo Run
Type "make" in the source folder. The MCL will start a demo run B-MCL for "dataset/ETC/Yeast-2/Yeast-2" with 3 coarsening step.
File "Yeast-2" contains 7,049 edges between 2,223 nodes. If the application runs well, you will see the cluster size distribution as
"size of cluster \t # of clusters \t # of nodes contained in such size of cluster" and detailed result in the console.


5. Output Explanation
The output will consist of following files.
"Data, MCL Mode, Coarsen Info, Thread Info".result
"Data, MCL Mode, Coarsen Info, Thread Info".assign
"Data, MCL Mode, Coarsen Info, Thread Info".dist

ex) ./PS-MCL dataset/SUNY/DIP/DIP ./ -sc 4 1.5 -reg 4 1 
makes 3 outputs
==================================================================================================================================================
DIP_B-MCL-1.5_SC_Level-5_numT-4_conv-norm.result		: records time, NCut, # of clusters

coarsen_mode   coarseLevel    b_Factor       mcl_mode       time           NCut           AVG_Ncut       ClusterNum     #ofThread      ofIteration    
SC             5              1.500000       Regularized    5.109000       647.902975     0.398710       1625           4              50      
==================================================================================================================================================
DIP_B-MCL-1.5_SC_Level-5_numT-4_conv-norm.assign 	: cluster assignment of each node

cluster        index          
0              128            
0              144            
0              129            
==================================================================================================================================================
DIP_B-MCL-1.5_SC_Level-5_numT-4_conv-norm.dist		: distribution of size of clusters

size           #of Clusters   #of Nodes      
1              6              6              
2              17             34             
3              32             96             
4              27             108            
5              51             255            
6              50             300           
==================================================================================================================================================
.result and .dist files' contents will be also printed in console.



6. Scripts for existing MCL based methods

To use MCL, R-MCL, B-MCL, use provided scripts MCL, R-MCL(Multi-Level), and B-MCL(Multi-Level).
MCL : ./MCL [INPUT (Graph File Path)] [Output Directory] [epsilon]
R-MCL : ./R-MCL [INPUT (Graph File Path)] [Output Directory] [Coarse Level] [epsilon]
B-MCL : ./B-MCL [INPUT (Graph File Path)] [Output Directory] [Coarse Level] [Balance Factor] [epsilon]


7. Rebuilding source codes

MCL distribution includes the source code. You can modify the code and rebuild
the code. The source codes are in 'src' directory.
Since the binary file PS-MCL.jar already exists in 'bin' directory, normally you
don't need to build the code again. Thus, this is the instruction when you 
modify the source code and build it.
To build the source code, use the script 'compile_PS-MCL.sh'. When you
execute the script, it automatically compile the source codes in 'src' and make
a jar file PS-MCL.jar in 'bin' directory. 
