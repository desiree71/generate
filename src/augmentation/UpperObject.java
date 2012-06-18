package augmentation;

import group.Permutation;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import model.Graph;

public class UpperObject {
	
	private Graph graph;
	
	private SortedSet<Integer> vertexSet;
	
	public UpperObject(Graph graph, int vertexIndex) {
		this.graph = graph;
		this.vertexSet = new TreeSet<Integer>();
		vertexSet.add(vertexIndex);
	}
	
	public UpperObject(Graph graph, SortedSet<Integer> vertexSet) {
		this.graph = graph;
		this.vertexSet = vertexSet;
	}
	
	public UpperObject(Graph graph, List<Integer> vertexSet) {
		this(graph, new TreeSet<Integer>(vertexSet));
	}
	
	public UpperObject permute(Permutation permutation) {
		int[] p = permutation.getValues();
		Graph permutedGraph = this.graph.getPermutedGraph(p);
		SortedSet<Integer> permutedVertices = new TreeSet<Integer>();
		for (int vertex : this.vertexSet) {
			permutedVertices.add(p[vertex]);
		}
		return new UpperObject(permutedGraph, permutedVertices);
	}
	
	public Graph getGraph() {
		return this.graph;
	}
	
	public SortedSet<Integer> getVertices() {
		return this.vertexSet;
	}

	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public boolean equals(Object other) {
		if (other instanceof UpperObject) {
			return ((UpperObject)other).toString().equals(this.toString());
		} else {
			return false;
		}
	}
	
	public String toString() {
		return "<" + graph.getSortedEdgeString() + ", " + vertexSet + ">";
	}

}
