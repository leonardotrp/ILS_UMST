package br.ufrj.cos.mhoc.algorithm.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.ufrj.cos.mhoc.algorithm.Algorithm;
import br.ufrj.cos.mhoc.algorithm.ILS;
import br.ufrj.cos.mhoc.type.Solution;
import br.ufrj.cos.mhoc.type.graph.Graph;
import br.ufrj.cos.mhoc.util.GraphUtil;
import br.ufrj.cos.mhoc.util.ILSUtil;
import br.ufrj.cos.mhoc.util.Statistic;
import br.ufrj.cos.mhoc.util.Util;

/**
 * ILS (Iterated Local Search): Method for solving discrete optimization problems
 * @author leonardo
 *
 */
public class ILSImpl extends MetaheuristicImpl implements ILS {
	private Graph graph;
	private Algorithm fitness;
	private Solution global;
	private List<Solution> all;
	
	private Statistic statistic;
	private int round;

	public ILSImpl() {
		super();
	}

	@Override
	public void load(File file) {
		this.graph = GraphUtil.newInstanceGraph(file);
		this.fitness = new Prim(this.graph, false);
		this.all = new ArrayList<>();
	}
	
	@Override
	public void reset() {
		this.all = new ArrayList<>();
		this.global = null;
		this.graph.reset();
	}
	
	@Override
	public Graph getGraph() {
		return this.graph;
	}
	
	@Override
	public Solution getSolution() {
		return this.global;
	}
	
	@Override
	public List<Solution> getAllSolutions() {
		return this.all;
	}
	
	private boolean setGlobal(Solution local) {
		boolean improve = false;
		int returnCompare = this.global == null ? -1 : Double.compare(local.getFitness(), this.global.getFitness());
		if (returnCompare < 0) {
			this.global = local.clone();
			this.all.add(this.global);
			this.statistic.checkSolutionInTime(this.round, this.global);
			improve = true;
		}
		return improve;
	}
	
	@Override
	public Solution generateInitialSolution() {
		Solution initial = GraphUtil.initialSolution(this /*greedy*/);
		this.setGlobal(initial);
		return initial;
	}

	@Override
	public Solution localSearch(Solution solution) { // 2-Opt
		Algorithm localSearch = TwoOpt.newInstance(this, solution);
		localSearch.run();
		return localSearch.getSolution();
	}
	
	@Override
	public Solution disturb(Solution solution) { // LSMC - simulated annealing style
		return ILSUtil.disturb(this, solution);
	}
	
	@Override
	public void fitness(Solution solution) {
		solution.setFitness(this.fitness.run());
		solution.setRootGap(this.statistic.getRootGaps(solution));
		this.statistic.functionEvaluationCalled(this.round, this.global);
	}

	private void setStatistic(Statistic statistic, int round) {
		this.statistic = statistic;
		this.round = round;
	}
	
	@Override
	public void run(Statistic statistic, int round) {
		//executorService.scheduleAtFixedRate(ILSImpl::check, 0, 1, TimeUnit.SECONDS);
	    this.setStatistic(statistic, round);
		
		Solution initial = this.generateInitialSolution() /* S0 (initial solution) */;
		Solution local = this.localSearch(initial) /* S* (local optimum solution) */, modified;
		this.setGlobal(local); /* S* (global optimum solution) */
		int iteration = 0;
		do {
			modified = this.disturb(local); // S'
			if (!modified.equals(local)) { // a disturbance occurred
				modified = this.localSearch(modified); // S*' (local optimum)
				local = ILSUtil.acceptanceCriteria(local, modified, iteration);
				iteration = this.setGlobal(local) ? 0 : iteration + 1; // substitui a solução global ou não
			}
			else { // there was no disturbance in the solution
				iteration++;
			}
		}
		while (!Util.stoppingCriterion(this.global, iteration));
		Util.foundGlobal(this.global);
	}
}
