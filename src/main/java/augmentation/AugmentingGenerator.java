package augmentation;

import generate.handler.GeneratorHandler;
import generate.handler.SystemOutHandler;
import graph.group.GraphDiscretePartitionRefiner;
import graph.model.Graph;
import group.Permutation;
import group.PermutationGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class AugmentingGenerator {
	
	private GeneratorHandler handler;
	
	private GraphOrbitRepresentativeGenerator representativeGenerator;
	
	public AugmentingGenerator() {
		this(new SystemOutHandler());
	}
	
	public AugmentingGenerator(GeneratorHandler handler) {
		this(-1, handler);
	}
	
	public AugmentingGenerator(int maxDegree, GeneratorHandler handler) {
		this.handler = handler;
		this.representativeGenerator = new GraphOrbitRepresentativeGenerator(maxDegree);
	}
	
	public void output(Graph parent, Graph g) {
		handler.handle(parent, g);
	}
	
	public PermutationGroup getGroup(Graph g) {
		GraphDiscretePartitionRefiner refiner = new GraphDiscretePartitionRefiner();
//		System.out.println("getting group for " + g.toStringIncludingSize());
		return refiner.getAutomorphismGroup(g);
	}
	
	public SortedSet<Integer> getOrbit(int element, PermutationGroup autG) {
		SortedSet<Integer> orbit = new TreeSet<Integer>();
		orbit.add(element);
		for (Permutation p : autG.all()) {
			SortedSet<Integer> newElements = new TreeSet<Integer>();
			for (int orbitElement : orbit) {
				newElements.add(p.get(orbitElement));
			}
			orbit.addAll(newElements);
		}
		return orbit;
	}
	
	public List<SortedSet<Integer>> getOrbitCombinations(Graph g) {
		representativeGenerator.setGraph(g);
		return representativeGenerator.getOrbitCombinations(getGroup(g), g.getVertexCount());
	}
	
	
	public List<UpperObject> getUpperObjects(Graph g) {
		return getUpperObjects(g, getGroup(g));
	}
	
	public List<UpperObject> getUpperObjects(Graph g, PermutationGroup autG) {
		List<UpperObject> upperObjects = new ArrayList<UpperObject>();
		representativeGenerator.setGraph(g);
		for (SortedSet<Integer> combination : representativeGenerator.getOrbitCombinations(autG, g.getVertexCount())) {
			upperObjects.add(new UpperObject(g, combination));
		}
		return upperObjects;
	}
	
	public void scan(Graph g, int n) {
		recursive_scan(null, g, n);
		handler.finish();
	}

	private void recursive_scan(Graph parent, Graph g, int n) {
		output(parent, g);
		if (g.getVertexCount() == n) return;
		PermutationGroup autG = getGroup(g);
		for (UpperObject orbitRep : getUpperObjects(g, autG)) {
			List<LowerObject> lowerObjects = invF(orbitRep);
			if (!lowerObjects.isEmpty()) {
				LowerObject y = lowerObjects.get(0);
				if (isCanonical(y)) {
					recursive_scan(g, y.getGraph(), n);
				}
			}
		}
	}

	public boolean isCanonical(LowerObject y) {
		Graph g = y.getGraph();
		int n = g.getVertexCount();
		GraphDiscretePartitionRefiner refiner = new GraphDiscretePartitionRefiner();
		PermutationGroup autG = refiner.getAutomorphismGroup(g);
		Permutation minPerm = refiner.getBest();
		if (minPerm.get(n - 1) == n - 1) {
			return true;
		} else {
			SortedSet<Integer> orbit = getOrbit(minPerm.get(n - 1), autG);
			return orbit.contains(n - 1);
		}
	}

	public List<LowerObject> invF(UpperObject orbitRep) {
		List<LowerObject> lowerObjects = new ArrayList<LowerObject>();
		// er, for now just make a simple extension
		Graph graph = new Graph(orbitRep.getGraph());
		int n = graph.getVertexCount();
		// special case
		if (orbitRep.getVertices().isEmpty()) {
//			graph.makeEdge(n, n);
			graph.makeIsolatedVertex();	// note that this only works if we only add to the end...
		}
		for (int connectionPoint : orbitRep.getVertices()) {
			graph.makeEdge(connectionPoint, n);
		}
		lowerObjects.add(new LowerObject(graph, n));
		return lowerObjects;
	}

}
