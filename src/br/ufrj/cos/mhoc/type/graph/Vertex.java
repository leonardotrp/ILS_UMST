package br.ufrj.cos.mhoc.type.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Vertex implements Cloneable {
	private int index;
	private double cost;
	private double density;
	private double penality;
	private boolean visited;
	private int upgraded; // {0,1}
	private List<Edge> edges;

	public Vertex(int index) {
		super();
		this.index = index;
		if (this.edges == null) {
			this.edges = new ArrayList<Edge>();
		}
	}

	public void reset() {
		this.density = 0;
		this.setPenality(0);
		this.visited = false;
		this.upgraded = 0;
	}

	public int getIndex() {
		return index;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	public double getPenality() {
		return penality;
	}

	public void setPenality(double penality) {
		this.penality = penality;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public int getUpgraded() {
		return upgraded;
	}

	public boolean isUpgraded() {
		return upgraded == 1;
	}

	public void setUpgraded(int upgraded) {
		this.upgraded = upgraded;
	}

	protected void addEdge(Vertex target, Edge edge) {
		edge.setPair(this, target);
		this.edges.add(edge);
	}

	public Collection<Edge> getEdges() {
		return this.edges;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Vertex: { index: " + this.getIndex() + ", cost: " + this.getCost() + ", upgraded: " + this.upgraded + ", density: " + this.getDensity() + " }\n");
		for (Edge edge : this.edges) {
			buffer.append(edge.toString());
		}
		return buffer.toString();
	}
	
	@Override
	public int hashCode() {
		return this.index;
	}

	@Override
	public boolean equals(Object obj) {
		return this.index == ((Vertex) obj).index;
	}

	@Override
	public Vertex clone() throws CloneNotSupportedException {
		Vertex vertex = (Vertex) super.clone();
		vertex.edges = new ArrayList<>();
		return vertex;
	}
}
