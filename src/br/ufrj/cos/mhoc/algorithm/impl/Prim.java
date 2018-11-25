package br.ufrj.cos.mhoc.algorithm.impl;

import java.util.Queue;

import br.ufrj.cos.mhoc.algorithm.Algorithm;
import br.ufrj.cos.mhoc.type.Solution;
import br.ufrj.cos.mhoc.type.graph.Edge;
import br.ufrj.cos.mhoc.type.graph.Graph;
import br.ufrj.cos.mhoc.type.graph.MSTree;
import br.ufrj.cos.mhoc.type.graph.Vertex;
import br.ufrj.cos.mhoc.util.Util;

/**
 * @author Leonardo
 */
public class Prim implements Algorithm {
	private Graph graph;
	private Graph msTree;
	private int countVertex;
	private double edgesDelay;

	public Prim(Graph graph, boolean generateMsTree) {
		super();
		this.graph = graph;
		this.countVertex = graph.getVertices().size();
		if (generateMsTree)
			this.msTree = new MSTree(this.countVertex); // Minimal Spanning Tree
	}
	
	private Vertex randomVertex() {
		int index = Util.random(this.countVertex);
		return this.graph.getVertex(index);
	}
	
	private void visit(Vertex vertex) {
		try {
			vertex.setVisited(true);
			if (this.msTree != null)
				msTree.addVertex(vertex.clone());
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	private void visit(Edge edge) {
		try {
			edge.getTarget().setVisited(true);
			if (this.msTree != null) {
				// adiciona source
				Vertex source = edge.getSource().clone();
				this.msTree.addVertex(source);
				// adiciona target
				Vertex target = edge.getTarget().clone();
				this.msTree.addVertex(target);
				this.msTree.addNeighbor(source, target, edge.getWeight().clone());
			}
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public double run() {
		this.graph.reset(true);
		int countEdgesVisited = 1;
		this.edgesDelay = 0;
		
		Vertex source = this.randomVertex(), target;
		this.visit(source);
		
		Queue<Edge> prioritiesEdges = Util.getPriorityEdgesQueue(source);

		while (!prioritiesEdges.isEmpty() && countEdgesVisited < this.countVertex) {
			Edge edge = prioritiesEdges.poll();
			
			target = edge.getTarget();
			if (target.isVisited())
				continue;

			prioritiesEdges.addAll(target.getEdges());
			
			this.visit(edge);
			this.edgesDelay += edge.getWeight().getDelay();

			countEdgesVisited++;
		}
		Util.evalCalled();
		return this.edgesDelay;
	}

	@Override
	public Solution getSolution() {
		throw new RuntimeException("PRIM only runs to return the minimum tree cost, not to return the upgrade solution");
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (this.msTree != null)
			builder.append(this.msTree.toString());
		builder.append("Edges Delay: " + this.edgesDelay);
		return builder.toString();
	}
}
