/***********************************************************************
 MCL: Markov Clustering. 
 Author: InJae Yu, U Kang

-------------------------------------------------------------------------
File: Matrix.java
 - Convert the graph to CSC format matrix
 
Version: 1.0
***********************************************************************/
package matrix;

import graph.Edge;
import graph.Graph;
import graph.Node;

import java.util.ArrayList;
import java.util.Collections;

/**
 * CSC format matrix
 * @author InJae
 *
 */
public class CSCMatrix {
	/**
	 * values of the matrix
	 */
	public double[] val;
	/**
	 * row indices of the matrix
	 */
	public int[] row_ind;
	/**
	 * column pointer set of the matrix
	 */
	int[] col_ptr;
	/**
	 * size of the maximum column 
	 */
	int max_col_size;
	/**	
	 * @return value array
	 */
	public double[] getVal() {
		return val;
	}

	/**
	 * @return row index array
	 */
	public int[] getRow_ind() {
		return row_ind;
	}

	/**
	 * @return column_ptr array
	 */
	public int[] getCol_ptr() {
		return col_ptr;
	}

	/**Construct new CSC Matrix from graph g
	 * @param g
	 * 
	 */
	public CSCMatrix(Graph g){
		int totEdge=0;
		for(Node v : g.getMap().values()){
			totEdge+=v.getAdj_list().size();
		}
		
		val = new double[totEdge];
		row_ind = new int[totEdge];
		col_ptr = new int[g.size()+1];
		
		col_ptr[0]=0;
		Node current;
		double tot=0;
		int index=0;
		int ptr_index=1;
		ArrayList<Integer> sort;
		max_col_size=0;
		for(int i=0;i<g.size();i++){
			
			current = g.getMap().get(i);
			if(current!=null){
				tot=0;
				sort = new ArrayList<Integer>();
				for(Edge e: current.getAdj_list().values()){
					tot+=e.getWeight()*(1/e.getFrom().getWeight() + 1/e.getTo().getWeight());
					sort.add(e.incident(current).getIndex());
				}
				Collections.sort(sort);
				for(int num:sort){
					Edge e = current.getAdj_list().get(num);
					val[index]=e.getWeight()*(1/e.getFrom().getWeight() + 1/e.getTo().getWeight())/tot;
					row_ind[index++]=current.getAdj_list().get(num).incident(current).getIndex();
				}
				col_ptr[ptr_index]
						=col_ptr[ptr_index++-1]+current.getAdj_list().size();
				max_col_size = (max_col_size > current.getAdj_list().size()) ? max_col_size :current.getAdj_list().size(); 
			}
			else{
				col_ptr[ptr_index]=col_ptr[ptr_index++-1];
			}
		}
	}

	/**
	 * @return maximum column size
	 */
	public int getMax_col_size() {
		return max_col_size;
	}
	
}