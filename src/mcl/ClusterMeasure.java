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
	HashMap<Integer, HashSet<Integer>> oCluster;
	HashMap<String, HashSet<Integer>> rCluster;
	
	String rPath;
	String fMap;
	String oPath;
	int cmin=0;
	int cmax=10;
	public ClusterMeasure(String oPath, String rPath, String fMap, boolean type, int cmax, int cmin) throws IOException, InterruptedException, ExecutionException {
		this.oPath = oPath;
		this.rPath= rPath;
		this.fMap = fMap;
		this.cmax = cmax;
		this.cmin = cmin;
		interpCluster(type);
		
		HashMap<Integer, HashSet<Integer>> scluster = new HashMap<>();
		int i=0;
		for(HashSet<Integer> c : this.oCluster.values()) {
			if(c.size()>=cmin && c.size() <= cmax)
				scluster.put(i++, c);
		}
		this.oCluster = scluster;
	} 
	
	public void interpCluster(boolean type) throws IOException,
	InterruptedException, ExecutionException{
		Path rPath = Paths.get(this.rPath);		
		Path oPath = Paths.get(this.oPath);
		Path cMap = Paths.get(this.fMap);
		
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
			in = new BufferedReader(new FileReader(rPath.toFile()));
			rCluster = new HashMap<String, HashSet<Integer>>();
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
						rCluster.put(Integer.toString(rCluster.size()), newCluster);
				}
				catch (IOException e) {

					e.printStackTrace();
					break;
				}
			}
			while(line!=null);
	
		}
		else {
			in = new BufferedReader(new FileReader(rPath.toFile()));
			rCluster = new HashMap<String, HashSet<Integer>>();
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
					if(rCluster.containsKey(cname))
						rCluster.get(cname).add(rvmap.get(v));
					else {
						HashSet<Integer> newCluster = new HashSet<>();
						newCluster.add(rvmap.get(v));
						rCluster.put(cname, newCluster);
					}
				}
				catch (IOException e) {

					e.printStackTrace();
					break;
				}
			}
			while(line!=null);
		}
		in = new BufferedReader(new FileReader(oPath.toFile()));
		this.oCluster = new HashMap<>();
		do{
			try {
				line = in.readLine();
				if (line == null) {
					continue;
				}
				st = new StringTokenizer(line, " \t,");
				int cnum = Integer.parseInt(st.nextToken());
				int nnum = Integer.parseInt(st.nextToken());
				
				if(this.oCluster.containsKey(cnum))
					this.oCluster.get(cnum).add(nnum);
				else
					this.oCluster.put(cnum, new HashSet<Integer>());
				
			}
			catch (IOException e) {				
				e.printStackTrace();
				break;
			}
			
		}
		while(line!=null);
		
	}
	
	
	public double[] measure(){

		if (oCluster.size()==0){
			return new double[]{0};
		}
		double[][] T=new double[rCluster.size()][oCluster.size()];

		
		int i,j;
		j=0;
		for(HashSet<Integer> c : oCluster.values()) {
			if(c.size()<4) {
				j++;
				continue;
			}
			i=0;
			for(HashSet<Integer> r : rCluster.values()) {
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
		
		for(i=0;i<rCluster.size();i++) {
			sn+=Arrays.stream(T[i]).max().getAsDouble();			
		}
		for(HashSet<Integer> c : rCluster.values()) {
			rsize+=c.size();
		}
		sn/=rsize;
		
		double ppv=0;
		double csize=0;
		
		i=0;
		for(HashSet<Integer> c:oCluster.values()) {
			if(c.size()<3) {
				i++;
				continue;
			}

			double max=0;
			for(j=0;j<rCluster.size();j++) {
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
