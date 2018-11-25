package br.ufrj.cos.mhoc.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufrj.cos.mhoc.util.BestValues.Where;
import br.ufrj.cos.mhoc.util.Util;

public class Solution implements Cloneable, Comparable<Solution> {
	
	private int[] values;
	private int size;
	private double cost;
	private double fitness = Double.MAX_VALUE;
	private long time;
	private int fes;
	private Double[] rootGap = new Double[Where.values().length];
	public Solution(int size) {
		this.values = new int[size];
		this.size = size;
	}
	public Solution(int[] values) {
		this.values = values;
		this.size = values.length;
	}
	public void set(int index, int value) {
		this.values[index] = value;
	}
	public int get(int index) {
		return this.values[index];
	}
	public boolean yes(int index) {
		return this.get(index) == 1;
	}
	public boolean no(int index) {
		return this.get(index) == 0;
	}
	public int getSize() {
		return size;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public double getFitness() {
		return fitness;
	}
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getFes() {
		return fes;
	}
	public void setFes(int fes) {
		this.fes = fes;
	}
	public Double[] getRootGap() {
		return rootGap;
	}
	public void setRootGap(Double[] rootGap) {
		this.rootGap = rootGap;
	}
	public List<Integer> getIndexes(boolean upgraded) {
		List<Integer> indexes = new ArrayList<>(this.size);
		for (int index = 0; index < this.size; index++) {
			if ((upgraded && this.yes(index)) || (!upgraded && this.no(index)))
				indexes.add(index);
		}
		return indexes;
	}
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.values);
	}
	@Override
	public boolean equals(Object obj) {
		Solution s = (Solution) obj;
		return Arrays.equals(this.values, s.values);
	}
	@Override
	public Solution clone() {
		Solution solution = new Solution(this.values.clone());
		solution.size = this.values.length;
		solution.cost = this.cost;
		solution.fitness = this.fitness;
		solution.time = this.time;
		solution.fes = this.fes;
		solution.rootGap = this.rootGap.clone();
		return solution;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("---------------------------------------\n");
		builder.append(String.format("Solution = %s\n", Arrays.toString(this.values)));
		builder.append(String.format("Upgrade Cost = %f\n", this.getCost()));
		builder.append(String.format("Delay Fitness = %f\n", this.getFitness()));
		builder.append(String.format("Evaluations count: %d\n", Util.evalCount));
		
		String timeSpent = String.format("Time spent = %s (sec.milisec)\n", Util.formatTime(this.time)); 
		builder.append(timeSpent);
		return builder.toString();
	}
	@Override
	public int compareTo(Solution o) {
		return Double.valueOf(this.getFitness()).compareTo(Double.valueOf(o.getFitness()));
	}
}
