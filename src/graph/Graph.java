/***********************************************************************
 MCL: Markov Clustering. 
 Author: InJae Yu, U Kang

-------------------------------------------------------------------------
File: Graph.java
 - Building graph from tab based edge file
 
Version: 1.0
***********************************************************************/
package graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.stream.Stream;

public class Graph {
	/**
	 * key : node inxex ,value : node
	 */
	public HashMap<Integer, Node> map;
	/**
	 * node (NodeMap[i]) is coarsed to node i in the coarsened graph
	 */
	private int[] NodeMap;

	/**
	 * match[i] : node i is merged with node match[i]
	 */
	private int[] match;
	/**
	 * coarse[i] : node i is coarsend to node (coarse[i]) in coarsened graph
	 */
	private int[] coarse;
	/**
	 * the number of nodes in the graph
	 */
	private int size;

	private Random rand;
	double skiprate;

	/**Construct the graph from edge file. If node has non-integer label, it creates integer node graph first, and construct graph.
	 * @param file graph file directory
	 * @param labeldNode if true, the node is represented as string, not a number
	 * @throws IOException file not found
	 * 
	 */
	public Graph(Path file, double skiprate) throws IOException {
		this.skiprate = skiprate;
		StringTokenizer st;
		Node a, b;
		int index1, index2;
		int max = 0;
		BufferedReader in = new BufferedReader(new FileReader(file.toFile()));
		boolean flag = false;
		while (true) {
			try {
				String line = in.readLine();
				if (line == null)
					break;
				if (line.startsWith("#") || line.startsWith("%"))
					continue;
				st = new StringTokenizer(line, " \t,");
				try {
					index1 = Integer.parseInt(st.nextToken());
				}
				catch(Exception e){
					flag = true;
					break;
				}
				break;
			}
			catch (IOException e) {

				e.printStackTrace();
				break;
			}
		}

		if(flag) {
			map = new HashMap<>();
			HashMap<String, Integer> nameMap = new HashMap<String, Integer>();
			BufferedWriter out = new BufferedWriter(new FileWriter(
					file.getFileName()+"_edges"));
			BufferedWriter mapping = new BufferedWriter(new FileWriter(file.getFileName()+"_nodemap"));
			
			
			try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
				for (String line : (Iterable<String>) lines::iterator) {
					if (line.startsWith("#") || line.startsWith("%"))
						continue;
					st = new StringTokenizer(line, " \t, ");

					String from = "";
					String to = "";
					try {
						from = st.nextToken();
						to = st.nextToken();
					} catch (Exception e) {
						System.out.println(line);
						continue;
					}

					if (!nameMap.containsKey(from)) {
						mapping.write(from + "\t" + nameMap.size() + "\n");
						nameMap.put(from, nameMap.size());
					}
					if (!nameMap.containsKey(to)) {
						mapping.write(to + "\t" + nameMap.size() + "\n");
						nameMap.put(to, nameMap.size());
					}
					index1 = nameMap.get(from);
					index2 = nameMap.get(to);

					if (index1 == index2) {
						continue;
					}

					if (map.containsKey(index1)) {
						a = map.get(index1);
					} else {
						a = new Node(index1, 1);
						map.put(index1, a);
					}

					if (a.getAdj_list().containsKey(index2))
						continue;

					if (map.containsKey(index2)) {
						b = map.get(index2);
					} else {
						b = new Node(index2, 1);
						map.put(index2, b);
					}

					if (index1 == index2)
						new Edge(a, 1);
					else
						new Edge(a, b, 1);

					out.write(index1 + "\t" + index2 + "\n");

					max = max > (index1 > index2 ? index1 : index2) ? max
							: (index1 > index2 ? index1 : index2);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			size = max + 1;
			out.close();
			mapping.close();
			System.out.println("Generating " + file.getFileName()+"_edges" +" : Done");
			System.out.println("Generating " + file.getFileName()+"_nodemap" +" : Done");
		}
		else {
			in = new BufferedReader(new FileReader(file.toFile()));
			String line = null;
			map = new HashMap<>();

			while (true) {
				try {
					line = in.readLine();
					if (line == null)
						break;
					if (line.startsWith("#") || line.startsWith("%"))
						continue;
					st = new StringTokenizer(line, " \t,");
					index1 = Integer.parseInt(st.nextToken());
					index2 = Integer.parseInt(st.nextToken());
					if (map.containsKey(index1)) {
						a = map.get(index1);
					} else {
						a = new Node(index1, 1);
						map.put(index1, a);

					}
					if (map.containsKey(index2)) {
						b = map.get(index2);
					} else {
						b = new Node(index2, 1);
						map.put(index2, b);
					}
					if (a.getAdj_list().containsKey(index2)) {
						a.getAdj_list().get(index2).addWeight(1);
					} else {
						if (index1 == index2) {
							new Edge(a, 1);
						} else
							new Edge(a, b, 1);
					}
					max = max > (index1 > index2 ? index1 : index2) ? max
							: (index1 > index2 ? index1 : index2);
				}

				catch (IOException e) {

					e.printStackTrace();
					break;
				}
			}
			size = max + 1;
		}

	}

	/**
	 * Naive constructor
	 */
	public Graph(double skiprate) {
		this.map = new HashMap<>();
		this.skiprate= skiprate;
	}

	/**
	 * @return Node set of the graph 
	 */
	public HashMap<Integer, Node> getMap() {
		return map;
	}

	/**
	 * set the size of the graph 
	 * @param size size of node
	 * 	 */
	private void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return size of the graph
	 */
	public int size() {
		return size;
	}

	/**
	 * Coarse an graph
	 * @param coarse_mode HEM or SC
	 * @return coarsened graph
	 */
	public Graph Coarse(int coarse_mode, int seed) {

		ArrayList<Integer> shuffle = new ArrayList<Integer>();
		match = new int[this.size()];
		for (int i = 0; i < size; i++) {
			match[i] = -2;
			shuffle.add(i);
		}
		if(seed<0)
			rand = new Random();
		else
			rand = new Random(seed);
		Collections.shuffle(shuffle, rand);
		
		for (int i = 0; i < this.size(); i++) {
			try {
				this.getMap().get(i).matchingNode(coarse_mode, rand, skiprate);
				match[i] = -1;
			} catch (Exception e) {
			
			}
		}
		int coarseG_size = 0;
		/* node i and  node (match[i]) are merged together */ 
		for (int i = 0; i < size; i++) {
			if (match[i] == -1) {
				match[i] = i;
				coarseG_size++;
				for (Node v : this.getMap().get(i).getSuperNode()) {
					match[v.getIndex()] = i;
				}
			}
		}

		Graph g = new Graph(skiprate);
		coarse = new int[this.size()];
		NodeMap = new int[coarseG_size];

		for (int i = 0; i < coarse.length; i++) {
			coarse[i] = -1;
		}

		int num = 0;
		int j;
		for (int i = 0; i < this.size(); i++) {
			j = shuffle.get(i);
			if (match[j] != -2) {
				if (coarse[match[j]] == -1) {
					coarse[match[j]] = num;
					NodeMap[num] = match[j];
					coarse[j] = num;
					g.getMap().put(num,
							new Node(num, this.getMap().get(j).getWeight()));
					num++;
				} else {
					coarse[j] = coarse[match[j]];
					g.getMap().get(coarse[j]).addWeight(this.getMap().get(j));
				}
			}
		}

		Node v, proj_v, proj_neighbor;
		for (int i = 0; i < this.size(); i++) {
			if (match[i] != -2) {
				v = map.get(i);
				/* supernode of node i */
				proj_v = g.getMap().get(coarse[i]);

				for (Edge e : v.getAdj_list().values()) {
					if (e.isUntouched()) {
						/* supernode of incident node */
						proj_neighbor = g.getMap().get(
								coarse[e.incident(v).getIndex()]);
						
						/* Insert Edge */
						if (proj_neighbor.getIndex() != proj_v.getIndex())
							proj_v.insertEdge(proj_neighbor, e.getWeight());
						else
							proj_v.insertEdge(null, e.getWeight());

						e.setUntouched(false);
					}
				}
			}
		}

		g.setSize(g.getMap().size());

		return g;

	}

	public int[] getMatch() {
		return match;
	}

	public int[] getCoarse() {
		return coarse;
	}

	public int[] getNodeMap() {
		return NodeMap;
	}

}
