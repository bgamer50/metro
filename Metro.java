//This is an experiment to find the fastest way to visit all the DC metro stations.
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
public class Metro {
	public static void main(String[] args) {
		MetroSystem theMetro = MetroSystem.getInstance();
	}
	public static java.util.LinkedList<MetroSystem.Station> goEverywhere(java.util.LinkedList<MetroSystem.Station> visited) {
		MetroSystem.Station current = visited.peek();
		return null;
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
					System.out.println(sArray[0]);
					System.out.println(sArray[1]);
					System.out.println(sArray[2]);
					Station station1 = stations.get(sArray[0]);
					Station station2 = stations.get(sArray[1]);
					Integer weight = Integer.parseInt(sArray[2]);

					station1.neighbors.put(station2, weight);
					station2.neighbors.put(station1, weight);

					ln = s.nextLine();
					}

			} catch(java.io.FileNotFoundException e) {System.err.println("Fail."); System.exit(-1);}
			for(Station st: stations.values())
				System.out.println(st + " " + st.neighbors);
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