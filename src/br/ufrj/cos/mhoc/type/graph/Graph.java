package br.ufrj.cos.mhoc.type.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leonardo
 */
public class Graph {
	private boolean directional;
	private Map<Integer, Vertex> vertices;
	private List<Edge> edges; // for immediate access to edges
	private double budget;
	private double cost = 0;

	public Graph(int countVertex, int countEdges, boolean directional, boolean initVertices) {
		super();
		this.directional = directional;
		this.init(countVertex, countEdges, initVertices);
	}

	public Graph(int countVertex, int countEdges) {
		this(countVertex, countEdges, false, true);
	}
	
	private void init(int countVertex, int countEdges, boolean initVertices) {
		// inicia a lista de vértices e arestas
		this.vertices = new HashMap<>(countVertex);
		this.edges = new ArrayList<>(countEdges);

		if (initVertices) {
			for (int index = 0; index < countVertex; index++)
				this.addVertex(new Vertex(index));
		}
	}

	/**
	 * reset: reset das variáveis de controle do grafo (upgrade dos vértices e delays das arestas)
	 */
	public void reset() {
		this.vertices.values().forEach(v -> v.reset());
		this.edges.forEach(e -> e.getWeight().reset());
	}
	
	public void reset(boolean visited) {
		this.vertices.values().forEach(v -> {
			if (visited)
				v.setVisited(false);
		});
	}

	public Collection<Vertex> getVertices() {
		return this.vertices.values();
	}
	
	public int countVertices() {
		return this.vertices.size();
	}

	public void addNeighbor(int indexI, int indexJ, Weight edgeWeight) {
		Vertex vertexI = this.getVertex(indexI);
		Vertex vertexJ = this.getVertex(indexJ);
		this.addNeighbor(vertexI, vertexJ, edgeWeight);
	}

	public void addNeighbor(Vertex vertexI, Vertex vertexJ, Weight edgeWeight) {
		Edge edge = new Edge(edgeWeight);
		vertexI.addEdge(vertexJ, edge);
		this.edges.add(edge);

		if (!this.directional) {
			edge = new Edge(edgeWeight);
			vertexJ.addEdge(vertexI, edge);
			this.edges.add(edge);
		}
	}
	
	public List<Edge> getEdges() {
		return this.edges;
	}

	public double getBudget() {
		return budget;
	}

	public void setBudget(double budget) {
		this.budget = budget;
	}

	public double getCost() {
		return cost;
	}
	
	public Vertex getVertex(int index) {
		return this.vertices.get(index);
	}

	public void addVertex(Vertex vertex) {
		this.vertices.put(vertex.getIndex(), vertex);
	}

	public void setVertexCost(int index, double cost) {
		Vertex vertex = this.getVertex(index);
		vertex.setCost(cost);
		this.cost += cost;
		this.vertices.put(vertex.getIndex(), vertex);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		this.getVertices().forEach(builder::append);
		return builder.toString();
	}
}
