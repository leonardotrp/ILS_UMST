package br.ufrj.cos.mhoc.algorithm;

import java.util.List;

import br.ufrj.cos.mhoc.type.Solution;
import br.ufrj.cos.mhoc.type.graph.Graph;
import br.ufrj.cos.mhoc.util.Statistic;

public interface Metaheuristc extends Algorithm {
	Graph getGraph();
	void fitness(Solution solution);
	void reset();
	
	List<Solution> getAllSolutions();
	
	Solution generateInitialSolution();
	Solution localSearch(Solution solution);
	void run(Statistic statistic, int round);
	void main();
}
