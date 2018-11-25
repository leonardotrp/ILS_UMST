package br.ufrj.cos.mhoc.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufrj.cos.mhoc.type.Solution;
import br.ufrj.cos.mhoc.util.BestValues.Budget;
import br.ufrj.cos.mhoc.util.BestValues.Where;

public class Statistic {
	static String ROUND_LINE_FORMAT = "%-15s, %-15s, %-15s, %-15s, %-15s, %-15s, %-15s, %-15s, %-15s, %-15s, %-15s, %-15s, %-15s\n";
	static boolean writeFiles = Config.STAT_FILES;

	private File fileInstance;
	private double budgetRate;
	private Writer fileAllBest;
	private Writer fileRoundMetrics;
	
	private List<Solution> bestSolutions;

	public Statistic(File file) {
		super();
		this.fileInstance = file;
		this.fileAllBest = createRoundFile("best", Util.getFileNameWithoutExtension(file));
		this.writeHeadStatistics(true);
	}
	
	private String getInstanceFileName() {
		return Util.getFileNameWithoutExtension(fileInstance);
	}
	
	private String getDefaultFileName() {
		return getInstanceFileName() + '_' + this.budgetRate;
	}
	
	private int indexBudget;
	private Budget getBudget() {
		return Budget.values()[this.indexBudget];
	}

	public void setBudgetRate(double budgetRate) {
		if (budgetRate == 0.1 || budgetRate == 0.2 || budgetRate == 0.3)
			this.indexBudget = (int) (budgetRate * 10) - 1;
		else if (budgetRate == 0.5)
			this.indexBudget = (int) (budgetRate * 10) - 4;
		else if (budgetRate == 0.9)
			this.indexBudget = (int) (budgetRate * 10) - 7;
		this.budgetRate = budgetRate;
	}

	private static String getFullFileName(String name) {
		String pathResult = Config.RESOURCES_ROOT + "results/";
		pathResult = pathResult.substring("file:/".length()).replace("/../../", "/").replaceAll("%20", " ");
		pathResult = pathResult.replace("/target/classes/", "/");
		File file = new File(pathResult);
		if (!file.exists())
			file.mkdirs();
		
		return pathResult + name;
	}
	
	private Writer createRoundFile(String prefix, String fileName) {
		Writer file = null;
		try {
			if (fileName == null)
				fileName = this.getDefaultFileName();
			fileName = getFullFileName(prefix + '_' + fileName + ".csv");
			if (writeFiles)
				file = new BufferedWriter(new FileWriter(fileName));
			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void start() {
		this.fileRoundMetrics = createRoundFile("best", null);
		
		this.writeHeadStatistics(false);
		
		this.bestSolutions = new ArrayList<Solution>(Config.MAX_RUNS);
	}
	
	public void functionEvaluationCalled(int round, Solution solution) {
	}
	
	public void checkSolutionInTime(int round, Solution solution) {
	}

	private void writeHeadStatistics(boolean all){
		if (!writeFiles) return;
		try {
			String head = String.format(ROUND_LINE_FORMAT , "BEST", "WORST", "MEAN", "Q1/4", "Q1/2", "Q3/4", "STD", "FES", "TIME(s)", "RG MIRA", "RG LAGR", "RG BB", "RG OPTIMUM");
			if (all)
				this.fileAllBest.write(head);
			else
				this.fileRoundMetrics.write(head);
			System.out.print(head);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Double rootGap(double fitness, Where where) {
		Double value = BestValues.get(getInstanceFileName(), getBudget(), where);
		if (value != null) {
			BigDecimal optimum = new BigDecimal(value);
			if (value.doubleValue() > 0.0) {
				BigDecimal rootGap = new BigDecimal(100.0).multiply(new BigDecimal(fitness).subtract(optimum).divide(optimum, 6, RoundingMode.HALF_UP));
				value = rootGap.doubleValue();
			}
		}
		return value;
	}
	public Double[] getRootGaps(Solution solution) {
		Double[] rootGaps = new Double[] {
				this.rootGap(solution.getFitness(), Where.optimum),
				this.rootGap(solution.getFitness(), Where.mira),
				this.rootGap(solution.getFitness(), Where.lagr),
				this.rootGap(solution.getFitness(), Where.bb)
		};
		return rootGaps;
	}
	
	private void writeStatisticLine(List<Solution> solutions, boolean end) {
		if (!writeFiles) return;
		
		Solution bestSolution = solutions.get(0);
		if (!end)
			this.bestSolutions.add(bestSolution);
		
		Double[] means = calculateMeans(solutions);
		double meanFitness = means[0];
		double meanFES = means[1];
		double meanTime = means[2];
		
		double medianNumber = quartile(2, solutions);
		double sdNumber = calculateStandardDeviation(solutions, medianNumber);
		
		String best = Util.formatNumber(bestSolution.getFitness());
		String worst = Util.formatNumber(solutions.get(solutions.size() - 1).getFitness());
		String mean = Util.formatNumber(meanFitness);
		String q1 = Util.formatNumber(quartile(1, solutions));
		String median = Util.formatNumber(medianNumber);
		String q3 = Util.formatNumber(quartile(3, solutions));
		String standardDeviation = Util.formatNumber(sdNumber);
		String fes = bestSolution.getFes() == Integer.MAX_VALUE ? "FL" : Util.formatNumber(meanFES); 
		String time = bestSolution.getTime() == Long.MAX_VALUE ? "TL" :  Util.formatTime(meanTime);
		
		String rootGapOptimum = Util.formatNumber(means[3]);
		String rootGapMira = Util.formatNumber(means[4]);
		String rootGapLagr = Util.formatNumber(means[5]);
		String rootGapBb = Util.formatNumber(means[6]);
		
		String line = String.format(ROUND_LINE_FORMAT, best, worst, mean, q1, median, q3, standardDeviation, fes, time, rootGapMira, rootGapLagr, rootGapBb, rootGapOptimum);
		try {
			if (end)
				this.fileAllBest.write(line);
			else
				this.fileRoundMetrics.write(line);
			System.out.println(line);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void addRoundSolution(List<Solution> solutions) {
		Collections.sort(solutions);
		this.writeStatisticLine(solutions, false);
	}

	public void end() {
		if (!writeFiles) return;
		Collections.sort(this.bestSolutions);
		try {
			this.writeStatisticLine(this.bestSolutions, true);
			this.fileRoundMetrics.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void finish() {
		if (!writeFiles) return;
		try {
			this.fileAllBest.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static Double[] calculateMeans(List<Solution> solutions) {
		BigDecimal meanFitness = new BigDecimal(0.0);
		BigDecimal meanFES = new BigDecimal(0.0);
		BigDecimal meanTime = new BigDecimal(0.0);
		BigDecimal meanRootGapOptimum = new BigDecimal(0.0);
		BigDecimal meanRootGapMira = new BigDecimal(0.0);
		BigDecimal meanRootGapLagr = new BigDecimal(0.0);
		BigDecimal meanRootGapBb = new BigDecimal(0.0);
		
		double bbCount = 0;
		for (Solution solution : solutions) {
			meanFitness = meanFitness.add(new BigDecimal(solution.getFitness()));
			meanFES = meanFES.add(new BigDecimal(solution.getFes()));
			meanTime = meanTime.add(new BigDecimal(solution.getTime()));
			if (solution.getRootGap()[Where.optimum.ordinal()] != null)
				meanRootGapOptimum = meanRootGapOptimum.add(new BigDecimal(solution.getRootGap()[Where.optimum.ordinal()]));
			if (solution.getRootGap()[Where.mira.ordinal()] != null)
				meanRootGapMira = meanRootGapMira.add(new BigDecimal(solution.getRootGap()[Where.mira.ordinal()]));
			if (solution.getRootGap()[Where.lagr.ordinal()] != null)
				meanRootGapLagr = meanRootGapLagr.add(new BigDecimal(solution.getRootGap()[Where.lagr.ordinal()]));
			if (solution.getRootGap()[Where.bb.ordinal()] != null) {
				meanRootGapBb = meanRootGapBb.add(new BigDecimal(solution.getRootGap()[Where.bb.ordinal()]));
				bbCount++;
			}
		}

		meanFitness = meanFitness.divide(new BigDecimal(solutions.size()), 6, RoundingMode.HALF_UP);
		meanFES = meanFES.divide(new BigDecimal(solutions.size()), 6, RoundingMode.HALF_UP);
		meanTime = meanTime.divide(new BigDecimal(solutions.size()), 6, RoundingMode.HALF_UP);
		meanRootGapOptimum = meanRootGapOptimum.divide(new BigDecimal(solutions.size()), 6, RoundingMode.HALF_UP);
		meanRootGapMira = meanRootGapMira.divide(new BigDecimal(solutions.size()), 6, RoundingMode.HALF_UP);
		meanRootGapLagr = meanRootGapLagr.divide(new BigDecimal(solutions.size()), 6, RoundingMode.HALF_UP);
		if (bbCount > 0)
			meanRootGapBb = meanRootGapBb.divide(new BigDecimal(bbCount), 6, RoundingMode.HALF_UP);

		return new Double[] { meanFitness.doubleValue(), meanFES.doubleValue(), meanTime.doubleValue(), meanRootGapOptimum.doubleValue(), meanRootGapMira.doubleValue(), meanRootGapLagr.doubleValue(), meanRootGapBb.doubleValue() };
	}
	
	/**
	 * n is 1-based
	 * Median: is the value that mediates the values present in a set ordered numerically
	 * @param solutions
	 * @return double
	 * @see https://pt.scribd.com/doc/20352517/A-Mediana-Formula
	 */
	static double calculateMedian(List<Solution> solutions) {
		Collections.sort(solutions);
		BigDecimal median;
		if (solutions.size() == 1) { // solução única
			median = new BigDecimal(solutions.get(0).getFitness());
		}
		else if (solutions.size() % 2 == 0) { // par
			int middleIndex = (solutions.size() / 2) /*(n/2)*/ - 1;
			BigDecimal termOne = new BigDecimal(solutions.get(middleIndex).getFitness()); // [ (n/2) ]
			BigDecimal termTwo = new BigDecimal(solutions.get(middleIndex + 1).getFitness()); // [(n/2) + 1]
			median = termOne.add(termTwo).divide(new BigDecimal(2)); // ( [(n/2)] + [(n/2) + 1] ) / 2
		}
		else { // ímpar
			int middleIndex = ((solutions.size() + 1) / 2) /*(n+1) / 2*/ - 1;
			median = new BigDecimal(solutions.get(middleIndex).getFitness());
		}
		return median.doubleValue();
	}
	
	static double quartile(double j, List<Solution> solutions) {
		if (solutions.size() == 0)
			return solutions.get(0).getFitness(); // Xk
		else {
			double doubleK = j * (double) (solutions.size() + 1) / 4;
			int intK = (int) doubleK;
			int indexOne = intK == 0 ? 0 : intK - 1;

			BigDecimal termOne = new BigDecimal(solutions.get(indexOne).getFitness()); // Xk
			BigDecimal toMultiply = new BigDecimal((double) (doubleK - intK));
			if (indexOne + 1 < solutions.size()) {
				BigDecimal termTwo = new BigDecimal(solutions.get(indexOne + 1).getFitness()).subtract(termOne);
				BigDecimal result = termOne.add(toMultiply.multiply(termTwo));
				return result.doubleValue();
			}
			else
				return termOne.doubleValue();
		}
	}
	
	/**
	 * Standard Deviation: The standard deviation is a measure that expresses the degree of dispersion of a data set, that is, how much a set of data is uniform. 
	 * 		The closer to 0 is the standard deviation, the more homogeneous the data are
	 * @param solutions
	 * @param meanOfFitness
	 * @return double
	 */
	static double calculateStandardDeviation(List<Solution> solutions, double meanOfFitness) {
		BigDecimal mean = new BigDecimal(meanOfFitness);
		BigDecimal standardDeviation = new BigDecimal(0.0);
		for (Solution solution : solutions) {

			BigDecimal bdFitness = new BigDecimal(solution.getFitness());
			bdFitness = bdFitness.subtract(mean);
			bdFitness = bdFitness.pow(2);
			
			standardDeviation = mean.add(bdFitness);
		}
		return Math.sqrt(standardDeviation.doubleValue() / solutions.size());
	}
}
