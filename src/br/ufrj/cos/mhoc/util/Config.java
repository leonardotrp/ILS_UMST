package br.ufrj.cos.mhoc.util;

import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * @author Leonardo
 */
public class Config {
	static ResourceBundle bundle = ResourceBundle.getBundle("ils");
	
	/* BUDGET RATE */
	static Double[] BUDGET_RATE;
	static {
		String rates = bundle.getString("BUDGET_RATES");
		String[] budgetRates = rates.substring(1, rates.length()-1).split("\\|");
		BUDGET_RATE = Arrays.stream(budgetRates).map(Double::valueOf).toArray(Double[]::new);
	}
	static int budgetRateIndex = -1;
	static double changeBudgetRate() {
		if (budgetRateIndex + 1 < Config.BUDGET_RATE.length) {
			double rate = Config.BUDGET_RATE[++budgetRateIndex];
			return rate;
		}
		return 0;
	}
	static double getCurrentBudgetRate() {
		return Config.BUDGET_RATE[budgetRateIndex == -1 ? 0 : budgetRateIndex];
	}
	public static void reset() {
		budgetRateIndex = -1;
	}
	
	public static int MAX_ITER = Integer.parseInt(bundle.getString("MAX_ITER"));
	public static double DISTURBANCES_RATE = Double.parseDouble(bundle.getString("DISTURBANCES_RATE"));
	public static double DOWNGRADE_PENALITY = Double.parseDouble(bundle.getString("DOWNGRADE_PENALITY"));
	public static int MAX_TIME = Integer.parseInt(bundle.getString("MAX_EXECUTION_TIME")) * 1000;
	public static int MAX_RUNS = Integer.parseInt(bundle.getString("MAX_RUNS"));
	public static String METAHEURISTIC = "impl.ILSImpl";
	public static String[] INSTANCES_FILES_ROOT;
	static {
		String rates = bundle.getString("INSTANCES_FILES_ROOT");
		String[] roots = rates.substring(1, rates.length()-1).split("\\|");
		INSTANCES_FILES_ROOT = Arrays.stream(roots).map(String::valueOf).toArray(String[]::new);
	}
	public static final boolean INIT_GREEDY = Boolean.parseBoolean(bundle.getString("INIT_GREEDY"));
	public static final boolean STAT_FILES = Boolean.parseBoolean(bundle.getString("STAT_FILES"));
	public static String RESOURCES_ROOT = Config.class.getResource("/") + bundle.getString("RESOURCES_ROOT");
	
	
	/* 2-OPT CONFIG */
	static ResourceBundle bundle2opt = ResourceBundle.getBundle("2opt");
	public static enum TwoOptContinuity {
		first_improve, improvements_count, tolerance
	}
	public static TwoOptContinuity TWO_OPT_CONTINUITY = TwoOptContinuity.valueOf(bundle2opt.getString("CONTINUITY"));
	public static int IMPROVEMENTS_COUNT = Integer.parseInt(bundle2opt.getString("IMPROVEMENTS_COUNT"));
	public static double TWO_OPT_TOLERANCE = Double.parseDouble(bundle2opt.getString("TOLERANCE"));

}