# PS-MCL : Parallel Shotgun Coarsening Markov Clustering.

This is an implementation of PS-MCL (Parallel Shotgun Coarsening MCL).
This also includes implementations for the original MCL, R-MCL and MLR-MCL.


## Prerequisites

Java 1.8 or higher, tested on Ubuntu.


IMPORTANT!
- Before you use it, please check that the script files are executable. If not, you may manually modify the permission of scripts or you may type **make install** to do the same work.
- You may type **make demo** if you want to just try PS-MCL.


To run PS-MCL, you need to do the followings:
- prepare an undirected edge file.  The format should be **node DELIMITER node**.


## Command

- ./PS-MCL.sh [INPUT (Graph Edge File Path)] [Output Directory] [CoarseMode] [Coarse Level] [Balance Factor] [MCL Mode] [Number of Thread] [epsilon] [rand_seed] [skip_rate]

### Arguments

You should provide following arguments.

	(a) INPUT	: path of input data
	(b) Output Directory	: directory for output data
	(c) CoarseMode	: -sc or -hem. "-sc" for Shotgun Coarsening which is proposed method, and "-hem" for Heavy Edge Matching.
	(d) Coarse Level	: The number of coarsening step. Should be an non-negative integer.
	(e) Balance Factor	: Balance factor for B-MCL. If this is 0, R-MCL will be executed.
	(f) MCL Mode	: -basic or -reg. Run MCL with "-basic". Run R,B-MCL with "-reg"(B-MCL is specialized by R-MCL with balance factor larger than 0).
	(g) Number of Thread	: the number of threads to be used
	(h) epsilon		: run until the error is under epsilon
	(i) rand_seed	: random seed number
	(i) skip_rate	: Float number from 0~1 when using "-sc" mode. 

If each node label in input edge file is String, the program will generate filename_**nodemap** and  filename_**edges**, which are nodelabel-integer node mappging file and integer-integer edge file, respectively.


## Output
The output will consist of following files.
- "Data, MCL Mode, Coarsen Info, Thread Info"**.result** : Contains running time, average Ncut, and other options.
- "Data, MCL Mode, Coarsen Info, Thread Info"**.assign** : Each row represents 1) cluster number and 2) node index belongs to it.
- "Data, MCL Mode, Coarsen Info, Thread Info"**.dist** : Each row represents 1) cluster size, 2) # of clusters having that size, 3) total number of nodes in the clusters with that size.



## Measurement
# Command
- ./measure.sh [*.assign] [node mapping file] [reference file] [reference type] [max cluster size] [min cluster size]

You can evaluate your clustering result with reference clusters.  Set [reference type] **-row** if each row of the reference file matches to one cluster(ex. dataset/reference/homo/allComplexes). If the each row consist of node name and cluster name, use **-col**(ex. dataset/reference/yeast/cyc2008). Set max and min cluster size to calculate accuracy of the output.


## Demo Run
Run **make demo** in the source folder. The MCL will start a demo run PS-MCL for "dataset/bionetworks/biogrid-homo/homo" with 3 coarsening step and 3 threads. 

## Scripts for existing MCL based methods

To use MCL, MLR-MCL, use provided scripts MCL.sh, MLR-MCL.sh(Multi-Level)
- MCL.sh : ./MCL [INPUT (Graph File Path)] [Output Directory] [epsilon] [seed]
- MLR-MCL.sh : ./R-MCL [INPUT (Graph File Path)] [Output Directory] [Coarse Level] [Balance Factor] [epsilon] [seed]



## Rebuilding source codes
MCL distribution includes the source code. You can modify the code and rebuild
the code. The source codes are in 'src' directory.
Since the binary file PS-MCL.jar already exists in 'bin' directory, normally you
don't need to build the code again. Thus, this is the instruction when you 
modify the source code and build it.
To build the source code, use the script 'compile_PS-MCL.sh'. When you
execute the script, it automatically compile the source codes in 'src' and make
a jar file PS-MCL.jar in 'bin' directory. 


## Contact
InJae Yu (ijyu@mmc.kaist.ac.kr), KAIST
