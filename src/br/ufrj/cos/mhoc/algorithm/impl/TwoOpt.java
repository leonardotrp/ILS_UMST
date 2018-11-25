package br.ufrj.cos.mhoc.algorithm.impl;

import br.ufrj.cos.mhoc.algorithm.Algorithm;
import br.ufrj.cos.mhoc.algorithm.Metaheuristc;
import br.ufrj.cos.mhoc.type.Solution;
import br.ufrj.cos.mhoc.type.graph.Graph;
import br.ufrj.cos.mhoc.util.Config;
import br.ufrj.cos.mhoc.util.Config.TwoOptContinuity;
import br.ufrj.cos.mhoc.util.GraphUtil;

public class TwoOpt implements Algorithm {
	private Metaheuristc metaheuristc;
	private Solution solution;
	
	private static ThreadLocal<TwoOpt> instance = new ThreadLocal<>();
	public static TwoOpt newInstance(Metaheuristc metaheuristc, Solution solution) {
		if (instance.get() == null)
			instance.set(new TwoOpt(metaheuristc));
		instance.get().solution = solution.clone();
		return instance.get();
	}
	
	private TwoOpt(Metaheuristc metaheuristc) {
		super();
		this.metaheuristc = metaheuristc;
	}

	@Override
	public Solution getSolution() {
		return this.solution;
	}

	private Solution twoOptSwap(Solution solution, int from, int to) {
		Solution newSolution = solution.clone();

		// @see https://en.wikipedia.org/wiki/2-opt
		// route[0] to route[from-1]  
		int start = from;
		// route[from] to route[to] reversed
		for (int index = to; index >= from; index--) {
			newSolution.set(start++, solution.get(index));
		}
		//  route[to+1] to end
		
		return (!newSolution.equals(solution) && GraphUtil.upgrade(this.metaheuristc, newSolution)) ? newSolution : null;
	}
	
	private boolean improved(Solution swapSolution) {
		return (swapSolution != null && swapSolution.getFitness() < this.solution.getFitness());
	}
	
	private boolean stop(Solution swapSolution, int improvementsCount) {
		TwoOptContinuity twoOptContinuity = Config.TWO_OPT_CONTINUITY;

		boolean improved = improved(swapSolution);
		boolean byFirstImprove = improved && twoOptContinuity.equals(TwoOptContinuity.first_improve) && improvementsCount > 0;
		boolean byImprovementsCount = improved && twoOptContinuity.equals(TwoOptContinuity.improvements_count) && improvementsCount == Config.IMPROVEMENTS_COUNT;
		boolean byTolerance = improved && twoOptContinuity.equals(TwoOptContinuity.tolerance) && (this.solution.getFitness() - swapSolution.getFitness()) >= Config.TWO_OPT_TOLERANCE;
		
		// vai parar por UM e SOMENTE UM dos critérios acima
		return (byFirstImprove || byImprovementsCount || byTolerance);
	}
	
	@Override
	public double run() {
		Graph graph = this.metaheuristc.getGraph();
		graph.reset();
		Solution swapSolution, improvedSolution;
		int size = graph.countVertices();
		int improvementsCount;
		boolean improved, stop;
		do {
			improvedSolution = null;
			improvementsCount = 0;
			improved = false;
			stop = false;
			for (int from = 1; !stop && from < size - 2; from++) {
				for (int to = from + 2; !stop && to < size; to++) {
					swapSolution = twoOptSwap(this.solution, from, to);
					improved = improved(swapSolution);
					stop = stop(swapSolution, improvementsCount + 1);
					if (improved || stop) { 
						improvedSolution = swapSolution;
						improvementsCount++;
					}
				}
			}
			stop = stop || (improvementsCount == 0);
		}
		while (!stop);
		
		if (improvedSolution != null)
			this.solution = improvedSolution;
		GraphUtil.upgrade(this.metaheuristc, this.solution);
		
		return this.solution.getFitness();
	}
}
