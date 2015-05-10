//This is an experiment to find the fastest way to visit all the DC metro stations.
import java.util.*;
import java.io.File;
public class Metro {
	public static final byte transferTime = 5; //5 minutes estimated transfer time when switching lines
	public static final int maxRecursionDepth = 91 * 3; //maximum depth of recursion
	public static int bestTime = 600; //surely this can be done in less than 10 hours
	public static void main(String[] args) {
		MetroSystem theMetro = MetroSystem.getInstance();
		System.out.println("Enter a starting station:");
		System.out.println(goEverywhere(new Scanner(System.in).nextLine()));
	}

	public static LinkedList<Object> goEverywhere(String start)  {
		LinkedList<MetroSystem.Station> tempList = new LinkedList<MetroSystem.Station>();
		tempList.add(MetroSystem.getInstance().stations.get(start));
		return goEverywhere(tempList, (MetroSystem.Line)tempList.get(0).lines.toArray()[0], 0);
	}

	public static LinkedList<Object> goEverywhere(LinkedList<MetroSystem.Station> visited, MetroSystem.Line currentLine, int weight) {
		MetroSystem.Station currentStation = visited.peek();

		if(visited.size() >= maxRecursionDepth || weight > bestTime) { //when it is at max recursion depth or taking more than the best so far it quits
			//System.out.println("Max Recursion Depth Reached.");
			/*System.out.println("Failure!");
			for(int k = visited.size() - 1; k >= 0; k--)
				System.out.print(visited.get(k).name + " ");
			System.out.println((weight / 60) + " hours, " + (weight - weight / 60 * 60) + " minutes\n");*/
			return null;	
		}
		if(currentStation.neighbors.keySet().size() == 1 && getTimesVisited(currentStation, visited) > 1) //if an endpoint has been visited more than once, then quit because that is obviously bad
			return null;
		else if(visited.containsAll(MetroSystem.getInstance().stations.values())) { //the base case, if it has visited all the stations
			System.out.println("Success!");
			for(int k = visited.size() - 1; k >= 0; k--)
				System.out.print(visited.get(k).name + ", ");
			System.out.println((weight / 60) + " hours, " + (weight - weight / 60 * 60) + " minutes\n");
			LinkedList<Object> tempList = new LinkedList<Object>(visited);
			tempList.push(weight);
			bestTime = weight;
			return tempList;
		}

		ArrayList<LinkedList<Object>> possiblePaths = new ArrayList<LinkedList<Object>>();
		MetroSystem.Station[] rankedNeighbors = rankNeighbors(currentStation, visited);

		for(MetroSystem.Station s: rankedNeighbors) { //loop over all 
			if(s.lines.contains(currentLine)) { //if no transfer is needed
				LinkedList<MetroSystem.Station> tempV = new LinkedList<MetroSystem.Station>(visited);
				tempV.push(s);
				possiblePaths.add(goEverywhere(tempV, currentLine, weight + currentStation.neighbors.get(s)));
			}
			else if(currentStation.transfer) { //if a transfer is needed and the current station is a transfer point
				for(MetroSystem.Line l : s.lines) {
					LinkedList<MetroSystem.Station> tempV = new LinkedList<MetroSystem.Station>(visited);
					tempV.push(s);
					possiblePaths.add(goEverywhere(tempV, l, weight + currentStation.neighbors.get(s) + transferTime));
				}
			}
		}
		
		int minWeight = Integer.MAX_VALUE;
		LinkedList<Object> bestPath = null;
		for(LinkedList<Object> path : possiblePaths) {
			if(path != null && ((Integer)path.peek()).intValue() < minWeight)
				bestPath = path;
		}
		return bestPath;
	}

	public static final MetroSystem.Station[] rankNeighbors(MetroSystem.Station s, LinkedList<MetroSystem.Station> visited) { //ranks neighbors, inefficient but ok since stations have few neighbors
		Map<MetroSystem.Station, Integer> neighbors = s.neighbors;
		Map<MetroSystem.Station, Double> weightedNeighbors = new HashMap<MetroSystem.Station, Double>();
		MetroSystem.Station prev;
		try {
		prev = visited.get(1);
		} catch(IndexOutOfBoundsException e) { prev = null; }

		for(MetroSystem.Station n : neighbors.keySet()) {
			int timesVisited = getTimesVisited(n, visited);
			if( ( (timesVisited <= 5 && n.transfer) || (timesVisited <= 2) ) && ( !n.equals(prev) || neighbors.keySet().size() == 1 ) ) { //throws out a transfer that has been visited more than 5 times or a normal station visited more than 2 times.
				//The preceding line also throws out a station it just visited unless it is at an endpoint, which might throw out a viable solution but saves a lot of time.

				/*int weight = 0;
				if(n.equals(prev) && !s.transfer)
					weight = Integer.MAX_VALUE / 2;
				else if(!visited.contains(n))
					weight -= 10000;
				if(n.transfer)
					weight -= 500;
				weight += 1000 * timesVisited;*/

				//double weight = 1.0 * neighbors.get(n) * (0.5 * bestTime - visited.size());
				double weight = Math.exp(1.0 * visited.size() *  1.0 * timesVisited / bestTime - 1.0 + 1.0 * Boolean.compare(n.equals(prev), false) * visited.size());

				weightedNeighbors.put(n, weight);
			}
		}
		MetroSystem.Station[] neighborArray = new MetroSystem.Station[weightedNeighbors.keySet().size()];
		//System.out.print("Neighbors of " + s + ": ");
		for(int k = 0; k < neighborArray.length; k++) {
			MetroSystem.Station next = min(weightedNeighbors);
			//System.out.print(next + " " + weightedNeighbors.get(next) + " ");
			weightedNeighbors.remove(next);
			neighborArray[k] = next;
		}
		//System.out.println("\n");
		
		/*if(neighborArray.length > 0) {
			System.out.print("Neighbors of " + s + " | ");
			for(MetroSystem.Station n : neighborArray)
				System.out.print(n + " ");
			System.out.println("");
		}*/
		return neighborArray;
	}

	public static final MetroSystem.Station min(Map<MetroSystem.Station, Double> m) {
		double minWeight = Double.MAX_VALUE;
		MetroSystem.Station minNeighbor = null;
		for(MetroSystem.Station s : m.keySet())
			if(m.get(s) < minWeight) {
				minWeight = m.get(s);
				minNeighbor = s;
			}
		return minNeighbor;
	}

	public static final int getTimesVisited(MetroSystem.Station s, LinkedList<MetroSystem.Station> visited) { //inefficient but better than copying a HashTable
		int count = 0;
		for(MetroSystem.Station v : visited)
			if(v.equals(s))
				count += 1;
		return count;
	}

	private static final class MetroSystem {
		private Map<String, Station> stations = new HashMap<String, Station>();
		public static final MetroSystem theMetro = new MetroSystem("metromap.data");

		private MetroSystem(String filename) {
			try {
				Scanner s = new Scanner(new File(filename));
				String ln = s.nextLine();
				Line line = Line.Green;
				while(!ln.equals("%end")) { //this loop adds station names
					if(ln.charAt(0) =='%') {
						line = Line.valueOf(ln.split("%")[1]);
					}
					else if(ln.charAt(0) != '&') {
						Station st = new Station(ln);
						if(stations.keySet().contains(ln)) {
							st = stations.get(ln);
							stations.remove(ln);
						}

						st.lines.add(line);
						stations.put(ln, st);
					}
					ln = s.nextLine();
				}
				ln = s.nextLine();
				while(!ln.equals("%end")) { //this loop handles transfers
					if(!ln.equals("%transfer")) {
						stations.get(ln).transfer = true;
					}
					ln = s.nextLine();
				}
				ln = s.nextLine(); ln = s.nextLine();
				while(!ln.equals("%end")) { //this loop handles the weights (times)
					String[] sArray = ln.split("~");
					//System.out.println(sArray[0]);
					//System.out.println(sArray[1]);
					//System.out.println(sArray[2]);
					Station station1 = stations.get(sArray[0]);
					Station station2 = stations.get(sArray[1]);
					Integer weight = Integer.parseInt(sArray[2]);

					station1.neighbors.put(station2, weight);
					station2.neighbors.put(station1, weight);

					ln = s.nextLine();
					}

			} catch(java.io.FileNotFoundException e) {System.err.println("Fail."); System.exit(-1);}
			//for(Station st: stations.values())
			//	System.out.println(st + " " + st.neighbors);
		}

		public static MetroSystem getInstance() {
			return theMetro;
		}
		
		private class Station {
			private String name;
			private Map<Station, Integer> neighbors = new HashMap<Station, Integer>();
			private Set<Line> lines = new HashSet<Line>();
			private boolean transfer = false;
			public Station(String n) {
				name = n;
			}
			public boolean equals(Object o) {
				if(o instanceof Station)
					return ((Station)o).name.equals(this.name);
				else
					return false;
			}
			public int hashCode() {
				return name.hashCode();
			}
			public String toString() {
				return (transfer ? "*" : "") + name + " " + lines.toString();
			}
		}

		public static enum Line {Red, Blue, Green, Orange, Yellow, YellowPrime, Silver};
	}
}