package br.ufrj.cos.mhoc.type.graph;

/**
 * Minimal Spanning Tree
 * @author Leonardo
 */
public class MSTree extends Graph {
	static boolean DIRECTIONAL = true, INIT_VERTICES = false;

	public MSTree(int countVertex) {
		super(countVertex, countVertex - 1, DIRECTIONAL, INIT_VERTICES);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("|");
		this.getVertices().forEach(v -> builder.append(v.getUpgraded() + "|"));
		builder.append("\n");
		this.getEdges().forEach(builder::append);
		return builder.toString();
	}
}
