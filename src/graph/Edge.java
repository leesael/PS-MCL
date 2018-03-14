/***********************************************************************
 MCL: Markov Clustering. 
 Author: InJae Yu, U Kang

-------------------------------------------------------------------------
File: Edge.java
 - Edge class
 
Version: 1.0
***********************************************************************/

package graph;


/**
 * edge class of MCL
 * @author InJaeYu
 *
 */
public class Edge {
	private Node from;
	private Node to;
	private double weight;
	private boolean untouched = true;


	/** Construct edge from node From to node To having weight w
	 * @param to
	 * @param from
	 * @param w weight
	 */
	public Edge(Node from, Node to, double w) {
		this.from = from;
		this.to = to;
		this.weight = w;
		from.addEdge(this);
		to.addEdge(this);
	}

	/**Add loop edge  to node with weight
	 * @param node
	 * @param weight
	 */
	public Edge(Node node, double weight) {
		this.from = node;
		this.to = node;
		this.weight = weight;
		node.addEdge(this);
	}

	/**Return true if the edge is loop.
	 * @return is_loop
	 */
	public boolean isLoop() {
		return (from.getIndex() == to.getIndex());
	}

	/**Return from_node
	 * @return from_node
	 */
	public Node getFrom() {
		return from;
	}

	/**Return to_node
	 * @return to_node
	 */
	public Node getTo() {
		return to;
	}

	/**Return the weight of this edge
	 * @return edge_weight
	 */
	public double getWeight() {
		return weight;
	}

	/**Add weight to this edge by w
	 * @param w
	 */
	public void addWeight(double w){
		weight+=w;
	}

	/**Return the incident node not equal with from_node
	 * @param from_node
	 * @return incident_node
	 */
	public Node incident(Node from_node) {		
		return from.getIndex() != from_node.getIndex() ? from : to;
	}
	
	/**Return true if this edge is not visited
	 * @return is_untoched
	 */
	public boolean isUntouched() {
		return untouched;
	}

	/**Touch this edge
	 * @param untouched
	 */
	public void setUntouched(boolean untouched) {
		this.untouched = untouched;
	}
	
	/**
	 * Remove this node from neighbor nodes
	 */
	public void remove() {
		from.removeEdge(this);
		to.removeEdge(this);
	}

}