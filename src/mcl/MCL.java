/***********************************************************************
 MCL: Markov Clustering. 
 Author: InJae Yu, U Kang

-------------------------------------------------------------------------
File: MCL.java
 - Markov Clustering. Main Class
 
Version: 1.0
 ***********************************************************************/
package mcl;

import graph.Edge;
import graph.Graph;
import graph.Node;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import matrix.CSCMatrix;

/**
 * <pre>
 * Input : Graph, MCL Parameters
 * Output : Cluster output
 * </pre>
 * 
 * @author InJaeYu
 *
 */
public class MCL {
	/**
	 * balance factor for B-MCL
	 */
	public static double b_Factor;

	/**
	 * 1 : (R, MLR-MCL), 2 : MCL
	 */
	public static int mcl_Mode;
	/**
	 * Current state of the coarsened graph
	 */
	public static Graph curGraph;
	/**
	 * true : curGraph is initial graph
	 */
	public static boolean finalGraph = true;
	/**
	 * Set of coarsened graphs
	 */
	public static ArrayList<Graph> graphSet;
	/**
	 * Calculated one-norm for checking convergence.
	 */
	public static double norm;

	/**
	 * Store cluster number as key, set of node indices as value to represent a
	 * cluster.
	 */
	public static HashMap<Integer, HashSet<Integer>> result;
	/**
	 * Store each processed column data after matrix operations or
	 * projectionFlow operation.
	 */
	public static TreeMap<Integer, Double>[] columnContainer;

	/**
	 * true if result columns are normalized
	 */
	public static boolean normalized;
	/**
	 * Indicate the columns to process per each thread
	 */
	public static int[] startingIndex;
	/**
	 * The Number of Thread. Default is 1.
	 */
	public static int num_thread = 1;

	/*
	 * M : default flow matrix Mg : initial flow matrix for R-MCL Ms : temporary
	 * result to calculate one norm from M
	 */
	/**
	 * Column pointer set of M
	 */
	public static int[] col_ptr;
	/**
	 * Row indices set of M
	 */
	public static int[] row_Indices;
	/**
	 * Values of M
	 */
	public static double[] values;
	/**
	 * Column pointer set of Mr
	 */
	public static int[] col_ptr_reg;
	/**
	 * Row Indices set of Mr
	 */
	public static int[] row_Indices_reg;
	/**
	 * Values of Mr
	 */
	public static double[] values_reg;
	/**
	 * Maximum size of column of Mr
	 */
	public static int max_column_size;
	/**
	 * Column pointer set of Ms
	 */
	public static int[] col_ptr_subst;
	/**
	 * Row Indices of Ms
	 */
	public static int[] row_Indices_subst;
	/**
	 * Values of Ms
	 */
	public static double[] values_subst;

	/**
	 * Mass Vector of B-MCL
	 */
	public static double[] mass;
	/**
	 * Propensity Vector of B-MCL
	 */
	public static double[] propensity;

	/**
	 * Using Shotgun Coarsening
	 */
	public static int COARSE_SC = 1;
	/**
	 * Using Heavy Edge Matching Coarsening
	 */
	public static int COARSE_HEM = 2;
	/**
	 * Regularized MCL(R, B-MCL)
	 */
	public static int MCL_REG = 1;
	/**
	 * Using default MCL
	 */
	public static int MCL_NAIVE = 2;
	/**
	 * Inflation Factor
	 */
	public static double inf_factor = 2;
	/**
	 * Prune Parameter 1
	 */
	public static double prunParam1 = 0.7;
	/**
	 * Prune Parameter 2
	 */
	public static double prunParam2 = 1.2;
	public static Graph initgraph;
	/**
	 * @param fileName
	 *            path of file
	 * @param outputPath
	 *            path for output
	 * @param coarsen_mode
	 *            coarsening strategy
	 * @param coarseLevel
	 *            coarsening level
	 * @param bFactor
	 *            balance factor.
	 * @param mcl_mode
	 *            mcl mode
	 * @param thread
	 *            the number of thread
	 * @param epsilon
	 *            A number for checking convergence
	 * @return json array of clusters
	 * @throws IOException
	 *             cannot find the file, or not correct graph file
	 * @throws InterruptedException
	 *             Thread error
	 * @throws ExecutionException
	 *             Thread error
	 */
	public static String run(String fileName, String outputPath,
			int coarsen_mode, int coarseLevel, double bFactor, int mcl_mode,
			int thread, double epsilon, int rand_seed, double skiprate) throws IOException,
			InterruptedException, ExecutionException {

		b_Factor = bFactor;
		num_thread = thread;
		mcl_Mode = mcl_mode;
		String[] p = fileName.split("/");

		/* Configure result file name */
		String resultName = p[p.length - 1];

		if (mcl_mode == MCL_NAIVE)
			resultName += "_D-MCL_";
		else {
			if (b_Factor == 0)
				resultName += "_R-MCL_";
			else
				resultName += "_B-MCL-" + bFactor + "_";
		}
		resultName += coarsen_mode == COARSE_SC ? "SC_" : "HEM_";
		resultName += "Level-" + coarseLevel + "_";
		resultName += "numT-" + num_thread;
		resultName += "_conv-norm";

		/* Prepare to write result */
		BufferedWriter resultOfEachFile = new BufferedWriter(new FileWriter(
				outputPath + "/" + resultName + ".result"));
		BufferedWriter AssignOfEachFile = new BufferedWriter(new FileWriter(
				outputPath + "/" + resultName + ".assign"));
		BufferedWriter DistOfEachFile = new BufferedWriter(new FileWriter(
				outputPath + "/" + resultName + ".dist"));

		/* Graph file dir */
		Path path = Paths.get(fileName);

		/* Store coarsened graph */
		graphSet = new ArrayList<>();
		initgraph = new Graph(path,skiprate);
		graphSet.add(initgraph);

		int i;
		//int coarse_levlel=coarseLevel;
		System.out.print("Graph Coarsed as ");
		/* Coarse Graph until reaches desired depth */
		/*
		for (i = 0; i < coarseLevel; i++) {
			System.out.print(graphSet.get(i).size() + " ");
			graphSet.add(graphSet.get(i).Coarse(coarsen_mode, rand_seed));
			
		}
		*/
		int init_size = graphSet.get(0).size();
		System.out.print(init_size + " ");

		for (i = 0; i < coarseLevel; i++) {
			graphSet.add(graphSet.get(i).Coarse(coarsen_mode, rand_seed));
			System.out.print(graphSet.get(i+1).size() + " ");

		}
		System.out.println();
		curGraph = graphSet.get(graphSet.size() - 1);

		/* Add Self Loop */
		curGraph.getMap().values().forEach(v -> {
			if (!v.getAdj_list().containsKey(v.getIndex())) {
				v.getAdj_list().put(v.getIndex(), new Edge(v, 1));
			}
		});

		double start = System.currentTimeMillis();

		/* Initialize Matrix */
		CSCMatrix flowMatrix;
		flowMatrix = new CSCMatrix(curGraph);

		col_ptr = flowMatrix.getCol_ptr();
		values = flowMatrix.getVal();
		row_Indices = flowMatrix.getRow_ind();

		if (mcl_mode == MCL_REG) {
			/* Adjacency matrix of current level of graph */
			col_ptr_reg = flowMatrix.getCol_ptr();
			values_reg = flowMatrix.getVal();
			row_Indices_reg = flowMatrix.getRow_ind();
			max_column_size = flowMatrix.getMax_col_size();
		}

		int numof_iteration = 0;

		/* Run MCL for 4 times for each level of graph */
		for (i = graphSet.size() - 1; i > 0; i--) {

			finalGraph = false;
			startingIndex = problemRange(curGraph.size());

			/* Run 4 Times */
			for (int j = 0; j < 4; j++) {
				/* Multiplication, Inflation, Pruning */
				matrixMultInfPrune();
				numof_iteration++;
			}

			/* Recover graph */
			curGraph = graphSet.get(i - 1);
			projectFlow();

			/* Add Self loop to the recovered graph */
			curGraph.getMap().values().forEach(v -> {
				if (!v.getAdj_list().containsKey(v.getIndex())) {
					v.getAdj_list().put(v.getIndex(), new Edge(v, 1));
				}
			});
			flowMatrix = new CSCMatrix(curGraph);

			col_ptr_reg = flowMatrix.getCol_ptr();
			values_reg = flowMatrix.getVal();
			row_Indices_reg = flowMatrix.getRow_ind();
			max_column_size = flowMatrix.getMax_col_size();
		}

		finalGraph = true;
		startingIndex = problemRange(curGraph.size());

		/* Run Until Converges */
		int work = 0;
		while (work < 30) {
			/* Multiplication, Inflation, Pruning */
			matrixMultInfPrune();

			/* Calculate 1-Norm */
			calcNorm();

			/* If the 1-Norm is less than epsilon, end the algorithm */
			if (norm < epsilon)
				break;

			values = values_subst;
			col_ptr = col_ptr_subst;
			row_Indices = row_Indices_subst;

			work++;
			numof_iteration++;
		}

		double endTime = System.currentTimeMillis();

		/* Interpret Cluster */
		result = new HashMap<Integer, HashSet<Integer>>();
		int maxIndex;
		double maxValue;

		for (i = 0; i < col_ptr.length - 1; i++) {
			if (col_ptr[i] == col_ptr[i + 1])
				continue;
			maxIndex = row_Indices[col_ptr[i]];
			maxValue = values[col_ptr[i]];
			for (int k = col_ptr[i]; k < col_ptr[i + 1]; k++) {
				if (values[k] > maxValue) {
					maxValue = values[k];
					maxIndex = row_Indices[k];
				}
			}
			if (result.containsKey(maxIndex))
				result.get(maxIndex).add(i);
			else {
				result.put(maxIndex, new HashSet<>());
				result.get(maxIndex).add(i);
			}
		}

		/* Calculate Average NCut */
		HashMap<Integer, Integer> count = new HashMap<Integer, Integer>();
		double Ncut = 0;

		/* Calculate NCut */
		for (HashSet<Integer> cluster : result.values()) {

			if (count.containsKey(cluster.size()))
				count.put(cluster.size(), count.get(cluster.size()) + 1);
			else
				count.put(cluster.size(), 1);

			Ncut += Ncut(cluster);
		}
		System.out.println(String.format("%-15s%-15s%-15s", "size",
				"#of Clusters", "#of Nodes"));
		DistOfEachFile.write(String.format("%-15s%-15s%-15s\n", "size",
				"#of Clusters", "#of Nodes"));
		count.entrySet()
				.stream()
				.sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
				.forEach(
						e -> {
							System.out.println(String.format("%-15d%-15d%-15d",
									e.getKey(), e.getValue(),
									e.getValue() * e.getKey()));
							try {
								DistOfEachFile.write(String.format(
										"%-15d%-15d%-15d\n", e.getKey(),
										e.getValue(), e.getValue() * e.getKey()));
							} catch (Exception e3) {
								// TODO Auto-generated catch block
								e3.printStackTrace();
							}
						});

		/* Write Result */
		resultOfEachFile
				.write("coarsen_mode\tcoarseLevel\tb_Factor\tmcl_mode\ttime\tNCut\tAVG_Ncut\tClusterNum\t#ofThread\t#ofIteration\n");
		resultOfEachFile.write(coarsen_mode + "\t" + coarseLevel + "\t"
				+ b_Factor + "\t" + +mcl_mode + "\t"
				+ ((endTime - start) / 1000) + "\t" + Ncut + "\t" + Ncut
				/ result.size() + "\t" + result.size() + "\t" + num_thread
				+ "\t" + numof_iteration + "\ta");
		System.out.println(String.format(
				"%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%-15s",
				"Name", "coarsen_mode", "coarseLevel", "b_Factor", "mcl_mode", "time",
				"NCut", "AVG_Ncut", "ClusterNum", "#ofThread", "ofIteration", "SkipRate"));
		System.out.println(String.format(
				"%-15s%-15s%-15d%-15f%-15s%-15f%-15f%-15f%-15d%-15d%-15d%-15f",
				p[p.length - 1],coarsen_mode == 1 ? "SC" : "HEM", coarseLevel, b_Factor,
				mcl_mode == MCL_REG ? "Regularized" : "Basic",
				((endTime - start) / 1000), Ncut, Ncut / result.size(),
				result.size(), num_thread, numof_iteration, skiprate));

		resultOfEachFile.close();

		DistOfEachFile.close();

		i = 0;
		Iterator<HashSet<Integer>> iter = result.values().iterator();

		StringBuilder json = new StringBuilder();
		json.append('{');
		HashSet<Integer> set;
		AssignOfEachFile.write(String.format("%-15s%-15s\n", "cluster", "index"));
		while (iter.hasNext()) {
			json.append("\"" + i + "\" : ");
			set = iter.next();
			for (int index : set) {
				AssignOfEachFile.write(String.format("%-15d%-15d\n", i,index));
			}
			json.append(set.toString());
			i++;
			if (iter.hasNext())
				json.append(',');
		}
		json.append('}');
		AssignOfEachFile.close();

		return json.toString();
	}

	/**
	 * Matrix multiplication, inflation, pruning by thread
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void matrixMultInfPrune() throws InterruptedException,
			ExecutionException {

		if (mcl_Mode == MCL_REG && b_Factor > 0)
			prepareBMCL();

		columnContainer = new TreeMap[curGraph.size()];

		ExecutorService executor = Executors.newFixedThreadPool(num_thread);

		for (int i = 0; i < num_thread; i++)
			executor.execute(new mult_inf_pruneThread(i));

		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.out.println("matrix multiplication thread crushed");
			e.printStackTrace();
		}
		normalized = true;
		buildCSC();

	}

	/**
	 * Calculate the propensity vector
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void prepareBMCL() throws InterruptedException,
			ExecutionException {

		/* Execute only for B-MCL */
		if (b_Factor == 0)
			return;

		/* Calculate mass vector */
		mass = new double[curGraph.size()];
		for (int i = 0; i < row_Indices.length; i++)
			mass[row_Indices[i]] += values[i];

		propensity = new double[curGraph.size()];
		ExecutorService executorService = Executors
				.newFixedThreadPool(num_thread);

		for (int i = 0; i < num_thread; i++)
			executorService.execute(new propensityThread(i));

		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE,
					TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.out.println("inflate thread crushed");
			e.printStackTrace();
		}

	}

	/**
	 * Build CSC Format matrix from thread results
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void buildCSC() throws InterruptedException,
			ExecutionException {
		/* Update directly to M */
		if (!finalGraph) {
			col_ptr = new int[columnContainer.length + 1];
			for (int i = 0; i < columnContainer.length; i++) {
				col_ptr[i + 1] = col_ptr[i] + columnContainer[i].size();
			}
			row_Indices = new int[col_ptr[col_ptr.length - 1]];
			values = new double[col_ptr[col_ptr.length - 1]];
		}
		/* Need to calculate the norm. Update to Ms */
		else {
			col_ptr_subst = new int[columnContainer.length + 1];
			for (int i = 0; i < columnContainer.length; i++) {
				col_ptr_subst[i + 1] = col_ptr_subst[i]
						+ columnContainer[i].size();
			}
			row_Indices_subst = new int[col_ptr_subst[col_ptr_subst.length - 1]];
			values_subst = new double[col_ptr_subst[col_ptr_subst.length - 1]];
		}

		ExecutorService executorService = Executors
				.newFixedThreadPool(num_thread);

		for (int i = 0; i < num_thread; i++)
			executorService.execute(new CSCStoreThread(i));

		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE,
					TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.out.println("inflate thread crushed");
			e.printStackTrace();
		}

	}

	/**
	 * From the curGraph, expand the flow matrix into bigger size matrix
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static void projectFlow() throws InterruptedException,
			ExecutionException {

		columnContainer = new TreeMap[curGraph.size()];

		for (int j = 0; j < curGraph.size(); j++)
			columnContainer[j] = new TreeMap<Integer, Double>();

		int from;
		ArrayList<Node> to;

		for (int i = 0; i < col_ptr.length - 1; i++) {
			for (int j = col_ptr[i]; j < col_ptr[i + 1]; j++) {

				to = curGraph.getMap().get(curGraph.getNodeMap()[i])
						.getSuperNode();

				from = curGraph.getNodeMap()[row_Indices[j]];

				for (Node n : to) {
					columnContainer[n.getIndex()].put(from, values[j]);
				}
			}
		}
		/* Change the problem set for each thread to bigger size problem. */
		startingIndex = problemRange(columnContainer.length);
		normalized = false;
		buildCSC();
	}

	/**
	 * Calculate the column of result of expand, inflate, and prune.
	 * 
	 * @author InJae
	 *
	 */
	static class mult_inf_pruneThread implements Runnable {

		int start;

		public mult_inf_pruneThread(int start) {
			this.start = start;
		}

		@Override
		public void run() {

			int index;
			double tot;
			double cur_val;
			double update_val;
			double pruneThreshold;

			double[] values_b = null;
			ArrayList<Integer> remove_candidate;

			TreeMap<Integer, Double> multCol = new TreeMap<>();

			if (b_Factor > 0 && mcl_Mode == MCL_REG)
				values_b = new double[max_column_size];

			/* i : column Index of m2 */
			for (int i = startingIndex[start]; i < startingIndex[start + 1]; i++) {

				multCol = new TreeMap<>();
				/* multiply */
				if (mcl_Mode == MCL_REG) {
					/* B-MCL */
					if (b_Factor > 0) {
						/* Balance the Mr */
						tot = 0;

						for (int j = col_ptr_reg[i]; j < col_ptr_reg[i + 1]; j++)
							values_b[j - col_ptr_reg[i]] = values_reg[j]
									/ propensity[row_Indices_reg[j]];

						/* Multiplication */
						for (int j = col_ptr_reg[i]; j < col_ptr_reg[i + 1]; j++) {

							for (int k = col_ptr[row_Indices_reg[j]]; k < col_ptr[row_Indices_reg[j] + 1]; k++) {
								index = row_Indices[k];
								update_val = values[k]
										* values_b[j - col_ptr_reg[i]];

								if (multCol.containsKey(index))
									multCol.put(index, multCol.get(index)
											+ update_val);
								else
									multCol.put(index, update_val);

							}
						}
					}
					/* R-MCL */
					else {
						for (int j = col_ptr_reg[i]; j < col_ptr_reg[i + 1]; j++) {

							for (int k = col_ptr[row_Indices_reg[j]]; k < col_ptr[row_Indices_reg[j] + 1]; k++) {
								index = row_Indices[k];
								update_val = values[k] * values_reg[j];
								if (multCol.containsKey(index))
									multCol.put(index, multCol.get(index)
											+ update_val);
								else
									multCol.put(index, update_val);

							}
						}

					}
				}
				/* MCL */
				else {
					for (int j = col_ptr[i]; j < col_ptr[i + 1]; j++) {

						for (int k = col_ptr[row_Indices[j]]; k < col_ptr[row_Indices[j] + 1]; k++) {
							index = row_Indices[k];
							update_val = values[k] * values[j];
							if (multCol.containsKey(index))
								multCol.put(index, multCol.get(index)
										+ update_val);
							else
								multCol.put(index, update_val);

						}
					}
				}

				/* inflation */
				tot = 0;
				for (int col : multCol.keySet()) {
					cur_val = Math.pow(multCol.get(col), inf_factor);
					multCol.put(col, cur_val);
					tot += cur_val;
				}

				for (int col : multCol.keySet())
					multCol.put(col, multCol.get(col) / tot);

				/* pruning */
				pruneThreshold = prunThreshold(multCol);
				remove_candidate = new ArrayList<Integer>();
				for (Integer col : multCol.keySet()) {
					if (multCol.get(col) < pruneThreshold)
						remove_candidate.add(col);
				}
				for (int col : remove_candidate)
					multCol.remove(col);

				columnContainer[i] = multCol;
			}

		}
	}

	/**
	 * From given vector v and parameters, calculate the prune threshold
	 * 
	 * @param column
	 *            inflated column vector
	 * @return prune threshold
	 */
	public static double prunThreshold(TreeMap<Integer, Double> column) {

		double max = 0;
		double max_centre = 0;
		for (double val : column.values()) {
			if (val > max) {
				max = val;
			}
			max_centre += Math.pow(val, 2);
		}
		/*
		 * double ret = prunParam1 * max_centre (1 - prunParam2 * (max -
		 * max_centre));
		 */
		double ret = prunParam1 * Math.pow(max_centre, prunParam2);
		return ret > max ? max : ret;
	}

	/**
	 * Calculate the propensity vector
	 * 
	 * @author InJae
	 *
	 */
	static class propensityThread implements Runnable {
		int start;

		public propensityThread(int start) {
			this.start = start;
		}

		@Override
		public void run() {
			double tot;

			for (int i = startingIndex[start]; i < startingIndex[start + 1]; i++) {
				tot = 0;

				for (int j = col_ptr[i]; j < col_ptr[i + 1]; j++)
					tot += mass[row_Indices[j]] * values[j];

				propensity[i] = Math.pow(tot, b_Factor);
			}
		}
	}

	/**
	 * CSC format builder thread
	 * 
	 * @author InJae
	 *
	 */
	static class CSCStoreThread implements Runnable {

		int start;

		public CSCStoreThread(int start) {
			this.start = start;
		}

		@Override
		public void run() {
			TreeMap<Integer, Double> column;
			int columnStart;
			Iterator<Integer> index_iter;
			int row_index;
			double tot;
			if (!finalGraph) {
				for (int i = startingIndex[start]; i < startingIndex[start + 1]; i++) {
					column = columnContainer[i];
					columnStart = col_ptr[i];
					index_iter = column.keySet().iterator();
					if (normalized)
						while (index_iter.hasNext()) {
							row_index = index_iter.next();
							row_Indices[columnStart] = row_index;
							values[columnStart++] = column.get(row_index);
						}
					else {
						tot = 0;
						for (double v : column.values())
							tot += v;

						while (index_iter.hasNext()) {
							row_index = index_iter.next();
							row_Indices[columnStart] = row_index;
							values[columnStart++] = column.get(row_index) / tot;
						}
					}

				}
			} else {
				for (int i = startingIndex[start]; i < startingIndex[start + 1]; i++) {
					column = columnContainer[i];
					columnStart = col_ptr_subst[i];
					index_iter = column.keySet().iterator();
					while (index_iter.hasNext()) {
						row_index = index_iter.next();
						row_Indices_subst[columnStart] = row_index;
						values_subst[columnStart++] = column.get(row_index);
					}

				}
			}

		}

	}

	/**
	 * Calculate the one-norm of subtraction of previous and current state of
	 * the matrix
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void calcNorm() throws InterruptedException,
			ExecutionException {
		norm = 0;

		ExecutorService executor = Executors.newFixedThreadPool(num_thread);
		ArrayList<Future<Double>> normResult = new ArrayList<>();
		for (int i = 0; i < num_thread; i++)
			normResult.add(executor.submit(new normThread(i)));

		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.out.println("matrix multiplication thread crushed");
			e.printStackTrace();
		}

		for (int i = 0; i < num_thread; i++) {
			norm += normResult.get(i).get();
		}

	}

	/**
	 * Calculate the norm of each subtracted column
	 * 
	 * @author InJae
	 *
	 */
	static class normThread implements Callable<Double> {
		int start;

		public normThread(int start) {
			this.start = start;
		}

		@Override
		public Double call() throws Exception {
			double ret = 0;
			HashMap<Integer, Double> checkDup;
			for (int i = startingIndex[start]; i < startingIndex[start + 1]; i++) {
				checkDup = new HashMap<>();

				for (int j = col_ptr_subst[i]; j < col_ptr_subst[i + 1]; j++)
					checkDup.put(row_Indices_subst[j], values_subst[j]);

				for (int j = col_ptr[i]; j < col_ptr[i + 1]; j++) {
					if (checkDup.containsKey(row_Indices[j]))
						checkDup.put(row_Indices[j],
								checkDup.get(row_Indices[j]) - values[j]);
					else
						checkDup.put(row_Indices[j], values[j]);
				}
				for (double val : checkDup.values())
					ret += Math.abs(val);

			}
			return ret;
		}

	}

	/**
	 * Calculate the NCut of the cluster output
	 * 
	 * @param cluster
	 *            input cluster
	 * @return NCut of a cluster
	 */
	public static double Ncut(HashSet<Integer> cluster) {
		Node cur;
		int totDegree = 0;
		int cutEdge = 0;
		for (int i : cluster) {
			cur = curGraph.getMap().get(i);
			totDegree += cur.degree();
			Set<Integer> edgeset = cur.getAdj_list().keySet();
			for (int j : edgeset) {
				if (!cluster.contains(j))
					cutEdge++;
			}
		}
		return ((double) ((double) cutEdge) / ((double) totDegree));
	}

	/**
	 * Indicate the range of subproblem to each thread
	 * 
	 * @param col
	 *            size of column
	 * @return split the problem of each thread should cover
	 */
	public static int[] problemRange(int col) {
		startingIndex = new int[num_thread + 1];
		for (int i = 0; i < num_thread; i++) {
			startingIndex[i] = col / num_thread * i;
		}
		startingIndex[num_thread] = col;
		return startingIndex;
	}

	/**
	 * Get input, and run the algorithm.
	 * 
	 * @param args
	 *            parameters of algorithm
	 * @throws IOException
	 *             Cannot find the file
	 * @throws InterruptedException
	 *             Thread Error
	 * @throws ExecutionException
	 *             Thread Error
	 */
	public static void main(String[] args) throws IOException,
			InterruptedException, ExecutionException {

	
		String outputPath = args[1];
		int coarseMode;
		if (args[2].equals("-sc"))
			coarseMode = COARSE_SC;
		else if (args[2].equals("-hem"))
			coarseMode = COARSE_HEM;
		else {
			System.out.println("Wrong coarsen scheme parameter");
			return;
		}

		int coarseLevel = 0;
		try {
			coarseLevel = Integer.parseInt(args[3]);
		} catch (Exception e) {
			System.out.println("Please set the coarse level");
		}
		if (coarseLevel < 0) {
			System.out.println("CoarseLevel should be non-negative");
		}

		double bFactor = 0;
		try {
			bFactor = Double.parseDouble(args[4]);
		} catch (Exception e) {
			System.out.println("Please set the balance factor");
		}
		if (bFactor < 0) {
			System.out.println("BalanceFactor should be non-negative");
		}

		int mcl_mode;
		if (args[5].equals("-reg"))
			mcl_mode = MCL_REG;
		else if (args[5].equals("-basic"))
			mcl_mode = MCL_NAIVE;
		else {
			System.out.println("Wrong MCL_MODE paramter");
			return;
		}

		int numThread = 1;
		try {
			numThread = Integer.parseInt(args[6]);
		} catch (Exception e) {
			System.out.println("Please set the number of thread");
		}
		if (numThread < 1) {
			System.out.println("# of Threads should be non-negative");
			return;
		}
		
		double elipson=1;
		try {
			elipson = Double.parseDouble(args[7]);
		} catch (Exception e) {
			System.out.println("Please set the threshlod for stop MCL");
		}
		if (numThread < 0) {
			System.out.println("Please set positive threshold");
			return;
		}
		
		int seed=-1;
		try {
			seed = Integer.parseInt(args[8]);
		} catch (Exception e) {
			System.out.println("Please set the random seed number");
		}
		
		double skiprate=0.5;
		if (coarseMode == COARSE_SC ) {
			
			try {
				skiprate = Double.parseDouble(args[9]);
			} catch (Exception e) {
				System.out.println("Please set the threshlod for stop MCL");
			}
			if( skiprate <= 0 || skiprate >=1) {
				System.out.println("Please set skip rate between 0 to 1 ");
				return;
			}
			
		}		
		
		MCL.run(args[0], outputPath, coarseMode,coarseLevel, bFactor, mcl_mode, numThread, elipson,seed, skiprate);
		if(args.length > 10) {
			if( args[10] != null && args[11] != null) {
				ClusterMeasure cm;
				double[] ret;
				cm = new ClusterMeasure(initgraph, result, args[10],args[11], true, Integer.MAX_VALUE, 0);					
				ret = cm.measure();
				System.out.println("Accuracy : " + ret[0]);
			}	
		}
		
	

	}
}

