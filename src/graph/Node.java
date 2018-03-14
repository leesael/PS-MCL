/***********************************************************************
 MCL: Markov Clustering. 
 Author: InJae Yu, U Kang

-------------------------------------------------------------------------
File: Node.java
 - Node class
 
Version: 1.0
***********************************************************************/
package graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

/**node class of MCL
 * @author InJaeYu 
 */
public class Node {
	private int index;
	private HashMap<Integer, Edge> adj_list;

	private double weight;

	private ArrayList<Node> superNode;
	public static  int COARSE_SC = 1;
	public static  int COARSE_HEM = 2;

	/**
	 * Construct new node with index and weight
	 * 
	 * @param index index of node
	 * @param weight weight of Node
	 */
	public Node(int index, double weight) {
		this.index = index;
		adj_list = new HashMap<>();
		this.weight = weight;
	}

	/**
	 * Return the superNode of this node
	 * 
	 * @return superNode
	 */
	public ArrayList<Node> getSuperNode() {
		return superNode;
	}

	/**
	 * Return weight of this node
	 * 
	 * @return weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Return index of this node
	 * 
	 * @return index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Insert an edge between "this" node and "to" node with weight
	 * 
	 * @param to insert a new edge between this node
	 * @param weight weight of the edge
	 */
	public void insertEdge(Node to, double weight) {
		/* "to" node is not specified, add loop */
		if (to == null) {
			/* Add loop */
			if (!this.adj_list.containsKey(index)) {
				new Edge(this, weight);
			}
			/* Add weight to loop */
			else {
				this.adj_list.get(index).addWeight(weight);
			}

		} else {
			/* Add edge */
			if (!this.adj_list.containsKey(to.getIndex())) {
				new Edge(this, to, weight);
			}
			/* Add weight */
			else {
				this.adj_list.get(to.getIndex()).addWeight(weight);
			}
		}
	}

	/**
	 * Insert an edge to this node
	 * 
	 * @param e edge to be add
	 */
	public void addEdge(Edge e) {
		this.adj_list.put(e.incident(this).getIndex(), e);
	}

	/**
	 * Return adj_list of this node
	 * 
	 * @return adjacency list
	 */
	public HashMap<Integer, Edge> getAdj_list() {
		return adj_list;
	}

	/**
	 * Find the contracting edge and insert this node to superNode
	 * 
	 * @param coarse_mode coarsening mode
	 */
	public void matchingNode(int coarse_mode, Random rand, double skiprate) {
		/* Coarsening with SC strategy  */
		if (coarse_mode == COARSE_SC) {

			/* Already matched */
			if (this.superNode != null)
				return;

			/* Has no incident edge, superNode is itself */
			if (this.getAdj_list().size() == 0) {
				superNode = new ArrayList<>();
				superNode.add(this);
				return;
			}


			/*
			 * Set the supernode as itself for 50% possibility. This will make
			 * one coarse step can reduce the size of graph up to 50%
			 */
			if (this.getAdj_list().size() > 0 && (rand.nextDouble() < skiprate)) {
				superNode = new ArrayList<>();
				superNode.add(this);
				return;
			}

			/* Select the contracting edge */
			Edge e = this.getAdj_list().values().stream().max(compSC).get();
			/* If the selected edge is loop, set the supernode as itself */
			if (e.isLoop()) {
				superNode = new ArrayList<>();
				superNode.add(this);
				return;
			}
			/* Merge the supernodes of neighbor nodes */
			else {

				Node to = e.incident(this);
				
				if (to.superNode == null) {
					superNode = new ArrayList<>();
					superNode.add(this);
					superNode.add(to);
					to.superNode = this.superNode;
				}

				else {
					to.superNode.add(this);
					this.superNode = to.superNode;
				}
			}
		}
		/* Coarsening with HEM strategy */
		else {
			/* If already matched, stop */
			if (superNode != null)
				return;
			Edge e;
			try {
				e = this.getAdj_list().values().stream().filter(line -> {
					return line.incident(this).superNode == null;
				}).max(compHEM).get();

			} catch (Exception E) {
				/* All of this node's neighbor are already matched */
				superNode = new ArrayList<>();
				superNode.add(this);
				return;

			}
			/* bind two nodes into one supernode*/
			superNode = new ArrayList<>();
			superNode.add(this);

			if (!e.isLoop())
				superNode.add(e.incident(this));

			e.incident(this).superNode = superNode;
			return;
		}
	}

	/**Return the size of adjacency list
	 * @return size # of incident edges
	 */
	public int degree() {
		return adj_list.size();
	}

	/** Increase the weight of this node by weight of node v
	 * @param node node to be merged
	 */
	public void addWeight(Node node) {
		this.weight += node.getWeight();
	}

	/**Remove e from this node's adjacency list
	 * @param e edge to be removed
	 */
	public void removeEdge(Edge e) {
		this.getAdj_list().remove(e);
	}

	final Comparator<Edge> compSC = (e1, e2) -> {
		double e1W = e1.getWeight();
		double e2W = e2.getWeight();

		if (e1W > e2W)
			return 1;

		else if (e1W == e2W) {
			Node v1 = e1.incident(this);
			Node v2 = e2.incident(this);

			if (v1.getWeight() > v2.getWeight()) {
				return -1;
			} else if (v1.getWeight() < v2.getWeight()) {
				return 1;
			}

			else {
				return 0;
			}
		} else {
			return -1;
		}
	};

	final Comparator<Edge> compHEM = (e1, e2) -> {
		double e1W = e1.getWeight();
		double e2W = e2.getWeight();

		if (e1W > e2W)
			return 1;
		else if (e1W == e2W) {
			if (e1.isLoop() && !e2.isLoop())
				return -1;
			else if (!e1.isLoop() && e2.isLoop())
				return 1;
			else
				return 0;
		}

		else
			return -1;

	};


}
