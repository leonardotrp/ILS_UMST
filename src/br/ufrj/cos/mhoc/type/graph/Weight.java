package br.ufrj.cos.mhoc.type.graph;

import java.util.Arrays;

public class Weight implements Cloneable {
	private Double[] delayArcs;
	private short delayLevel;

	/**
	 * d0 > d1 > d2 (diminuição do delay)
	 * @param delayArcs
	 * @return boolean
	 */
	private static boolean validDelayArcs(Double... delayArcs) {
		boolean valid = delayArcs != null && delayArcs.length > 0;
		for (int level = 1; valid && level < delayArcs.length; level++) {
			valid = valid && delayArcs[level] <= delayArcs[level - 1];
		}
		return valid;
	}
	
	public Weight(Double... delayArcs) {
		super();

		if (!validDelayArcs(delayArcs))
			throw new RuntimeException("Invalid delay values. The condition is 'delay0 > delay1 > delay2 > ...'");

		this.delayArcs = delayArcs;
		this.reset();
	}

	public void reset() {
		this.delayLevel = 0;
	}
	
	public Double[] getDelayArcs() {
		return this.delayArcs;
	}

	public Double getDelay() {
		return this.delayArcs[this.delayLevel];
	}

	public boolean canImprove() {
		return this.delayLevel + 1 < this.delayArcs.length;
	}

	public boolean canGetWorse() {
		return this.delayLevel - 1 >= 0;
	}

	public double getImprovedDelay() { // delay variation
		return this.canImprove() ? (this.delayArcs[this.delayLevel] - this.delayArcs[this.delayLevel + 1]) : 0;
	}

	/**
	 * decrease delay - improvement
	 */
	public boolean improve() {
		if (this.canImprove()) {
			this.delayLevel++;
			return true;
		}
		return false;
	}

	/**
	 * increase delay - worsen
	 */
	public void worsen() {
		if (this.canGetWorse())
			this.delayLevel--;
	}

	@Override
	public Weight clone() throws CloneNotSupportedException {
		Weight weight = (Weight) super.clone();
		return weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		result = prime * result + Arrays.hashCode(delayArcs);
		result = prime * result + delayLevel;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Weight other = (Weight) obj;
		if (!Arrays.equals(delayArcs, other.delayArcs))
			return false;
		if (delayLevel != other.delayLevel)
			return false;
		return true;
	}
}
