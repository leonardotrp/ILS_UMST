package br.ufrj.cos.mhoc.algorithm.impl;

import java.io.File;

import br.ufrj.cos.mhoc.algorithm.Metaheuristc;
import br.ufrj.cos.mhoc.util.Config;
import br.ufrj.cos.mhoc.util.GraphUtil;
import br.ufrj.cos.mhoc.util.Statistic;
import br.ufrj.cos.mhoc.util.Util;

public abstract class MetaheuristicImpl implements Metaheuristc {
	@Override
	public double run() {
		return 0;
	}
	
	public abstract void load(File file);
	
	private void initializeRun() {
		this.reset();
		Util.clearAll();
	}
	
	public abstract void run(Statistic statistic, int round);
	
	public void main() {
		for (String instanceFilesRoot : Config.INSTANCES_FILES_ROOT) { // run all instances present in specific subdirectories
			
			for (File instanceFile : Util.listIntanceFiles(instanceFilesRoot)) { // run all instances in current root directory

				//Runnable runnable = () -> { 
					this.load(instanceFile); // loads data from the main structure from the current file 'instanceFile'
					Statistic statistic = new Statistic(instanceFile);
					double budgetRate;

					while ((budgetRate = GraphUtil.changeBudgetRate(this.getGraph())) > 0) { // run for all budget rates
						statistic.setBudgetRate(budgetRate);
						statistic.start(); // initialize statistics
						for (int round = 0; round < Config.MAX_RUNS; round++) { // rounds
							this.initializeRun();
							this.run(statistic, round); // ils run
							statistic.addRoundSolution(this.getAllSolutions()); // save round statistics
						}
						statistic.end();
					}
					statistic.finish();
				//};
				//Thread thread = new Thread(runnable);
				//thread.start();
			}
		}
	}
}
