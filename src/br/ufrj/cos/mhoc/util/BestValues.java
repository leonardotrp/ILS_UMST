package br.ufrj.cos.mhoc.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BestValues {
	private static String getFilename(String name) {
		String pathResult = Config.RESOURCES_ROOT;
		pathResult = pathResult.substring("file:/".length()).replace("/../../", "/").replaceAll("%20", " ");
		pathResult = pathResult.replace("/target/classes/", "/");
		return pathResult + name;
	}
	private static Map<String, Double[]> getMapFromCSV(final String filePath) {
		Stream<String> lines = null;
        try {
			lines = Files.lines(Paths.get(filePath)).skip(1);
			Map<String, Double[]> resultMap = lines.map(line -> line.split(",")).
					collect(Collectors.toMap(
							key -> key[0], 
							values -> Arrays.stream(values, 1, values.length).
								map(Double::valueOf).
								toArray(Double[]::new) ) );
			return resultMap;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			lines.close();
		}
    }
	private static Map<String, Double[]> MAP = getMapFromCSV(getFilename("optimum_and_best.csv"));

	// INST => {Z1,Z2,Z3,MIRA1,MIRA1T,MIRA2,MIRA2T,MIRA3,MIRA3T,LAGR1,LAGR1T,LAGR2,LAGR2T,LAGR3,LAGR3T,BB1,BB1T,BB2,BB2T,BB3,BB3T}
	public static enum Budget { one, two, three };
	public static enum Where { optimum, mira, lagr, bb };
	
	private static final int BEST = 0;
	private static final int MIRA = 3;
	private static final int LAGRAN = 9;
	private static final int BB = 15;

	public static Double optimum(String instanceKey, Budget budget) {
		Double[] values = MAP.get(instanceKey);
		return values == null ? null : values[BEST + budget.ordinal()];
	}
	private static Double value(String instanceKey, Budget budget, int from) {
		Double[] values = MAP.get(instanceKey);
		if (values == null || from >= values.length)
			return null;
		return values[from + 2 * budget.ordinal()];
	}
	public static Double mira(String instanceKey, Budget budget, boolean time) {
		return time ? value(instanceKey, budget, MIRA + 1) : value(instanceKey, budget, MIRA);
	}
	public static Double lagran(String instanceKey, Budget budget, boolean time) {
		return time ? value(instanceKey, budget, LAGRAN + 1) : value(instanceKey, budget, LAGRAN);
	}
	public static Double bb(String instanceKey, Budget budget, boolean time) {
		Double[] values = MAP.get(instanceKey);
		if (values == null || BB >= MAP.get(instanceKey).length)
			return null;
		return time ? value(instanceKey, budget, BB + 1) : value(instanceKey, budget, BB);
	}
	
	public static Double get(String instanceKey, Budget budget, Where where) {
		if (Where.optimum.equals(where))
			return optimum(instanceKey, budget);
		else {
			int index = MIRA + (6 * (where.ordinal() - 1));
			return value(instanceKey, budget, index);
		}
	}
	public static Double getTime(String instanceKey, Budget budget, Where where) {
		int index = MIRA + (6 * (where.ordinal() - 1));
		return value(instanceKey, budget, index + 1);
	}

	public static void main(String[] args) {
		System.out.println(BestValues.getTime("c17", Budget.one, Where.mira));
	}
}
