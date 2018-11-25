package br.ufrj.cos.mhoc;

import br.ufrj.cos.mhoc.algorithm.Metaheuristc;
import br.ufrj.cos.mhoc.util.Config;

public class Main {
	public static void main(String[] args) throws Exception {
		String className = Metaheuristc.class.getPackage().getName() + '.' + Config.METAHEURISTIC;
		Metaheuristc metaheuristic = (Metaheuristc) Class.forName(className).newInstance();
		metaheuristic.main();
	}
	
	public static void main2(String[] args) throws Exception {
		//Stream<File> instances = GraphUtil.printListFiles(".");
		//instances.forEach(System.out::println);
		//String instance = "H1004950.in";
		//Util.safePrintln("INSTANCE: " + instance);
		//ILSImpl ils = ILSImpl.newInstance(instance);
		//while (GraphUtil.hasBudgetRate(ils.getGraph())) {
		//	ils.run();
		//}
	}
}
