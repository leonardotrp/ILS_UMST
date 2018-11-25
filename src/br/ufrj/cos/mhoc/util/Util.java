package br.ufrj.cos.mhoc.util;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import br.ufrj.cos.mhoc.type.Solution;
import br.ufrj.cos.mhoc.type.graph.Edge;
import br.ufrj.cos.mhoc.type.graph.Vertex;

public class Util {
	private Util() {
		super();
	}

	public static List<File> listIntanceFiles(String path) {
		try {
			File directory = new File(new URI(Config.RESOURCES_ROOT + "instances/" + path));
			File[] files = directory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().indexOf(".in") > -1;
				}
			});
			if (files == null) {
				throw new RuntimeException("Files *.in not found in " + directory);
			}
			return Arrays.asList(files);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	public static String getFileNameWithoutExtension(File file) {
        String fileName = "";
        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                fileName = name.replaceFirst("[.][^.]+$", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
        }
        return fileName;
    }	

	/**
	 * --------------------------------------------------------------
	 * SOLUTIONS CACHE
	 * --------------------------------------------------------------
	 */
	private static List<Solution> solutionsCache = new ArrayList<>();
	public static boolean isCached(Solution solution) {
		return solutionsCache.contains(solution);
	}
	public static Solution getCached(Solution solution) {
		if (solutionsCache.contains(solution)) {
			int index = solutionsCache.indexOf(solution);
			return solutionsCache.get(index);
		}
		return solution;
	}
	public static void cache(Solution solution) {
		if (solutionsCache.contains(solution)) {
			int index = solutionsCache.indexOf(solution);
			solutionsCache.add(index, solution);
		}
		else
			solutionsCache.add(solution);
	}
	/**
	 * --------------------------------------------------------------
	 * PRIORITIES EDGES QUEUE CACHE
	 * --------------------------------------------------------------
	 */
	private static Map<Vertex, Queue<Edge>> prioritiesEdgesQueue = new HashMap<>();
	public static Queue<Edge> getPriorityEdgesQueue(Vertex vertex) {
		Queue<Edge> queue = prioritiesEdgesQueue.get(vertex);
		if (queue == null) {
			queue = new PriorityQueue<Edge>(vertex.getEdges());
			prioritiesEdgesQueue.put(vertex, queue);
		}
		return queue;
	}
	public static void clearPrioritiesQueue() {
		prioritiesEdgesQueue.clear();
	}
	
	/**
	 * --------------------------------------------------------------
	 * RANDOM HELPER METHODS
	 * --------------------------------------------------------------
	 */
	private static Random random = new Random();
	public static int random(int until) {
		return random.nextInt(until);
	}
	public static double randomInRange(double min, double max) {
		double range = max - min;
		double scaled = random.nextDouble() * range;
		return scaled + min;
	}
	
	/**
	 * --------------------------------------------------------------
	 * LOG
	 * --------------------------------------------------------------
	 */
	public static int evalCount = 0;
	public static void evalCalled() {
		evalCount++;
	}
	static long initialTime = Instant.now().toEpochMilli();
	public static long getTimeSpent() {
		 return Instant.now().toEpochMilli() - initialTime;
	}
	public static String formatTime(double time) {
		 double decimalTime = (double) time / (double) 1000;
		 return formatNumber(decimalTime, 2);
	}

	public static void clearAll() {
		solutionsCache.clear();
		prioritiesEdgesQueue.clear();
		evalCount = 0;
		initialTime = Instant.now().toEpochMilli();
	}
	
	/**
	 * --------------------------------------------------------------
	 * STATISTICS
	 * --------------------------------------------------------------
	 */
	public static void foundGlobal(Solution solution) {
		if (solution.getFes() != Integer.MAX_VALUE)
			solution.setFes(Util.evalCount);
		if (solution.getTime() != Long.MAX_VALUE)
			solution.setTime(Util.getTimeSpent());
	}
	public static boolean stoppingCriterion(Solution solution, int iteration) {
		// Noman, N., & Iba, H. (2008). Accelerating differential evolution using an adaptive local search. Evolutionary Computation, IEEE Transactions on, 12(1), 107-125
		// Maximum number of evaluations (@see Fitness function evaluations: A fair stopping condition? SIS 2014: 181-188)
		//boolean reachedMaxFES = evalCount >= Config.MAX_FES; 
		boolean reachedMaxExecutionTime = getTimeSpent() > Config.MAX_TIME; // Álvarez-Miranda (1800 s)
		boolean reachedMaxIterations = iteration > Config.MAX_ITER; // maximum number of iterations without improvement

		//if (reachedMaxFES)
		//	solution.setFes(Integer.MAX_VALUE);
		if (reachedMaxExecutionTime)
			solution.setTime(Long.MAX_VALUE);
		
		return /*reachedMaxFES || */reachedMaxExecutionTime || reachedMaxIterations;
	}

	public static String formatNumber(Object value, int scale) {
		String format = "%1$." + scale + "f";
		return String.format(Locale.US, format, value);
	}
	public static String formatNumber(Object value) {
		return formatNumber(value, 2);
	}
	
	public static boolean rateAcceptedRandomly(double rate) {
		double pin = Util.randomInRange(0.0, 1.0);
		return (rate < pin);
	}
}
