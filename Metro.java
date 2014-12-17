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
		if(visited.size() == )
		MetroSystem.Station current = visited.peek();
	}

	private static final class MetroSystem {
		private Set<Station> stations = new HashSet<Station>();
		public static final MetroSystem theMetro = new MetroSystem("metromap.data");

		private MetroSystem(String filename) {
			super();
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
						if(stations.contains(st))
							for(Station t : stations)
								if(t.equals(st)) {
									st = t;
									stations.remove(st);
									break;
								}

						st.lines.add(line);
						stations.add(st);
					}
					ln = s.nextLine();
				}
				ln = s.nextLine();
				while(!ln.equals("%end")) { //this loop handles transfers
					if(!ln.equals("%transfer")) {
						for(Station t: stations)
							if(t.name.equals(ln))
								t.transfer = true;
					}
					ln = s.nextLine();
				}
			} catch(java.io.FileNotFoundException e) {System.err.println("Fail."); System.exit(-1);}
			System.out.println(stations);
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