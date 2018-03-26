package mcl;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
	ArrayList<HashSet<Integer>> oCluster;
	ArrayList<HashSet<Integer>> rCluster;
	
	String rPath;
	String fMap;
	String oPath;
	int cmin=0;
	int cmax=10;
	public ClusterMeasure(String oPath, String fMap,String rPath,  boolean type, int cmax, int cmin) throws IOException, InterruptedException, ExecutionException {
		this.oPath = oPath;
		this.fMap = fMap;
		this.rPath= rPath;
		this.cmax = cmax;
		this.cmin = cmin;
		interpCluster(type);		
	} 
	
		
	public double Accuracy(){

		if (oCluster.size()==0){
			return 0;
		}
		double[][] T=new double[rCluster.size()][oCluster.size()];

		
		int i,j;
		j=0;
		for(HashSet<Integer> c : oCluster) {
			if(c.size()<4) {
				j++;
				continue;
			}
			i=0;
			for(HashSet<Integer> r : rCluster) {
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
		for(HashSet<Integer> c : rCluster) {
			rsize+=c.size();
		}
		sn/=rsize;
		
		double ppv=0;
		double csize=0;
		
		i=0;
		for(HashSet<Integer> c:oCluster) {
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
		
		return Math.sqrt(sn * ppv);
		
		
	}
	
	
	
	private void interpCluster(boolean type) throws IOException,
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
		HashMap<String, HashSet<Integer>>  rCluster = new HashMap<String, HashSet<Integer>>();
		if(type) {
			in = new BufferedReader(new FileReader(rPath.toFile()));
			
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
		this.rCluster = new ArrayList<>();
		for(HashSet<Integer> c : rCluster.values()) {
			if(c.size()<=this.cmax && c.size() >= this.cmin) {
				this.rCluster.add(c);
			}
				
		}
		
		
		in = new BufferedReader(new FileReader(oPath.toFile()));
		HashMap<Integer, HashSet<Integer>> oCLuster = new HashMap<Integer, HashSet<Integer>>();
		do{
			try {
				line = in.readLine();
				if (line == null) {
					continue;
				}
				st = new StringTokenizer(line, " \t,");
				int cnum = Integer.parseInt(st.nextToken());
				int nnum = Integer.parseInt(st.nextToken());
				
				if(oCLuster.containsKey(cnum))
					oCLuster.get(cnum).add(nnum);
				else {
					oCLuster.put(cnum, new HashSet<Integer>());
					oCLuster.get(cnum).add(nnum);
				}
					
				
			}
			catch (Exception e) {				
				continue;
			}
			
		}
		while(line!=null);
		
		this.oCluster = new ArrayList<>();
		for(HashSet<Integer> c : oCLuster.values()) {
			if(c.size()<=this.cmax && c.size() >= this.cmin) {
				this.oCluster.add(c);
			}
				
		}
		
	
	}
}

