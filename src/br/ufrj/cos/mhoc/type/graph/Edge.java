package br.ufrj.cos.mhoc.type.graph;

/**
 * @author Leonardo
 */
public class Edge implements Comparable<Edge> {
	private Weight weight;
	private Vertex source;
	private Vertex target;

	public Edge(Weight weight) {
		super();
		this.weight = weight;
	}
	
	public Weight getWeight() {
		return this.weight;
	}

	public void setPair(Vertex source, Vertex target) {
		if (!equals(source, target)) {
			this.source = source;
			this.target = target;
		}
	}
	
	public Vertex getSource() {
		return this.source;
	}
	
	public Vertex getTarget() {
		return this.target;
	}

	@Override
	public String toString() {
		return "\tEdge: { (" + this.source.getIndex() + "-" + this.target.getIndex() + "), delay: " + this.getWeight().getDelay() + " }\n";
	}

	@Override
	public int compareTo(Edge o) {
		return this.getWeight().getDelay().compareTo(o.getWeight().getDelay());
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = hash * 31 + this.source.hashCode();
		hash = hash * 31 + this.target.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		return equals(other.getSource(), other.getTarget());
	}

	private boolean equals(Vertex source, Vertex target) {
		return this.source != null && this.target != null && 
				source != null && target != null &&
				this.source.equals(source) && this.target.equals(target);
	}
}
