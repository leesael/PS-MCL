package mcl;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import graph.Graph;

public class ClusterMeasure {
	HashMap<String, Integer> vmap;
	HashMap<String, Integer> rvmap;
	HashMap<Integer, HashSet<Integer>> cluster;
	HashMap<String, HashSet<Integer>> reference;
	
	String fCluster;
	String fMap;
	int cmin=0;
	int cmax=10;
	public ClusterMeasure(Graph G, HashMap<Integer, HashSet<Integer>> cluster, String fCluster, String fMap, boolean type, int cmax, int cmin) throws IOException, InterruptedException, ExecutionException {
		this.cluster = cluster;
		this.fCluster = fCluster;
		this.fCluster = fCluster;
		this.fMap = fMap;
		this.cmax = cmax;
		this.cmin = cmin;
		interpCluster(type);
		
		HashMap<Integer, HashSet<Integer>> scluster = new HashMap<>();
		int i=0;
		for(HashSet<Integer> c : this.cluster.values()) {
			if(c.size()>=cmin && c.size() <= cmax)
				scluster.put(i++, c);
		}
		this.cluster = scluster;
	} 
	
	public void interpCluster(boolean type) throws IOException,
	InterruptedException, ExecutionException{
		Path cPath = Paths.get(fCluster);
		Path cMap = Paths.get(fMap);
		vmap = new HashMap<>();
		BufferedReader in = new BufferedReader(new FileReader(cMap.toFile()));
		String line = null;
		String vname;
		StringTokenizer st;
		int vnum;
		do{
			try {
				line = in.readLine();
				if (line == null) {
					continue;
				}
				st = new StringTokenizer(line, " \t,");
				vname = st.nextToken();
				vnum = Integer.parseInt(st.nextToken());
				vmap.put(vname, vnum);
				
			}
			catch (IOException e) {

				e.printStackTrace();
				break;
			}
			
		}
		while(line!=null);
		if(type) {
			in = new BufferedReader(new FileReader(cPath.toFile()));
			reference = new HashMap<String, HashSet<Integer>>();
			rvmap = (HashMap<String, Integer>) vmap.clone();
			do {
				try {
					line = in.readLine();
					if (line == null) {
						continue;
					}
					st = new StringTokenizer(line, " \t,");
					HashSet<Integer> newCluster = new HashSet<>();
					while(st.hasMoreTokens()) {
						String v = st.nextToken();
						if (!rvmap.containsKey(v)) {
							rvmap.put(v, rvmap.size());
						}
						newCluster.add(rvmap.get(v));
					}
					// take reference cluster only if the size is bigger than 2
					if(newCluster.size()>=3 && newCluster.size() <= cmax)
						reference.put(Integer.toString(reference.size()), newCluster);
				}
				catch (IOException e) {

					e.printStackTrace();
					break;
				}
			}
			while(line!=null);
	
		}
		else {
			in = new BufferedReader(new FileReader(cPath.toFile()));
			reference = new HashMap<String, HashSet<Integer>>();
			rvmap = (HashMap<String, Integer>) vmap.clone();
			do {
				try {
					line = in.readLine();
					if (line == null) {
						continue;
					}
					st = new StringTokenizer(line, " \t,");
					String v = st.nextToken();
					if (!rvmap.containsKey(v)) {
						rvmap.put(v, rvmap.size());
					}
					String cname = "";
					while(st.hasMoreTokens()) {
						cname+=st.nextToken();
					}
					if(reference.containsKey(cname))
						reference.get(cname).add(rvmap.get(v));
					else {
						HashSet<Integer> newCluster = new HashSet<>();
						newCluster.add(rvmap.get(v));
						reference.put(cname, newCluster);
					}
				}
				catch (IOException e) {

					e.printStackTrace();
					break;
				}
			}
			while(line!=null);
		}
	}
	
	
	public double[] measure(){

		if (cluster.size()==0){
			return new double[]{0};
		}
		double[][] T=new double[reference.size()][cluster.size()];

		
		int i,j;
		j=0;
		for(HashSet<Integer> c : cluster.values()) {
			if(c.size()<4) {
				j++;
				continue;
			}
			i=0;
			for(HashSet<Integer> r : reference.values()) {
				Set<Integer> intersection = new HashSet<Integer>(c);
				intersection.retainAll(r);
				if(intersection.size()>0)
					T[i][j]+=intersection.size();				
				i++;
			}
			
			j++;			
		}
		
		double sn=0;
		double rsize=0;
		
		for(i=0;i<reference.size();i++) {
			sn+=Arrays.stream(T[i]).max().getAsDouble();			
		}
		for(HashSet<Integer> c : reference.values()) {
			rsize+=c.size();
		}
		sn/=rsize;
		
		double ppv=0;
		double csize=0;
		
		i=0;
		for(HashSet<Integer> c:cluster.values()) {
			if(c.size()<3) {
				i++;
				continue;
			}

			double max=0;
			for(j=0;j<reference.size();j++) {
				if(max < T[j][i])
					max=T[j][i];
			}
			ppv+=max;
			csize+=c.size();
			i++;
		}
		ppv/=csize;
		
		double ACC = Math.sqrt(sn * ppv);
		
		double[] ret= new double[] {
				ACC
		};
		
		return ret;
	}
}
