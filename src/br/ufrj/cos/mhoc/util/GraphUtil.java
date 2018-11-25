package br.ufrj.cos.mhoc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import br.ufrj.cos.mhoc.algorithm.Metaheuristc;
import br.ufrj.cos.mhoc.type.Solution;
import br.ufrj.cos.mhoc.type.graph.Graph;
import br.ufrj.cos.mhoc.type.graph.Vertex;
import br.ufrj.cos.mhoc.type.graph.Weight;

public class GraphUtil {
	public static double changeBudgetRate(Graph graph) {
		double rate = Config.changeBudgetRate();
		double newBudget = graph.getCost() * rate;
		graph.setBudget(newBudget);
		System.out.println("------------------------------------------------");
		System.out.println(String.format("@Budget (%.1f) = %.1f", rate, newBudget));
		return rate;
	}
	
	/**
	 * Methos createGraph -> load instance file and create a graph
	 * 
	 * (1) linha 1: número de vértices (n) ' ' número de arestas (m)
	 * (2) linha 2 a m+1: vértice i ' ' vértice j ' ' dij(0) ' ' dij(1) ' ' dij(2)
	 * (3) m+2 a n+m+1: custo(i) - upgrade
	 * 
	 * @param graphInstanceFile
	 * @throws URISyntaxException
	 * @throws FileNotFoundException
	 */
	public static Graph newInstanceGraph(File graphInstanceFile ) {
		Graph graph = null;
		Scanner instanceScanner = null;
		Config.reset();
		try {
			instanceScanner = new Scanner(graphInstanceFile);

			int indexLine = 0;
			String line;
			String[] parsedLine;

			int countVertex = 0, countEdge = 0;
			int index = 0, indexI, indexJ;
			Double[] delayArcs;

			while (instanceScanner.hasNextLine()) {
				line = instanceScanner.nextLine();
				parsedLine = line.split(" ");

				// first line { countVertex ' ' countEdge }
				if (indexLine == 0) {
					countVertex = Integer.parseInt(parsedLine[0]);
					countEdge = Integer.parseInt(parsedLine[1]);
					graph = new Graph(countVertex, countEdge);
				}
				// edge data { vértice i ' ' vértice j ' ' dij(0) ' ' dij(1) ' ' dij(2) }
				else if (indexLine > 0 && indexLine <= countEdge) { // edges data
					indexI = Integer.parseInt(parsedLine[0]);
					indexJ = Integer.parseInt(parsedLine[1]);

					delayArcs = new Double[] { Double.parseDouble(parsedLine[2]), Double.parseDouble(parsedLine[3]), Double.parseDouble(parsedLine[4]) };

					graph.addNeighbor(indexI, indexJ, new Weight(delayArcs));
				}
				// costs
				else if (indexLine > countEdge && indexLine <= (countVertex + countEdge)) {
					graph.setVertexCost(index++, Double.parseDouble(parsedLine[0]));
				} else {
					throw new RuntimeException("File with invalid number of rows");
				}
				indexLine++;
			}
			
			System.out.println("@File: " + graphInstanceFile.getName());
			System.out.println("@Number of vertices: " + countVertex);
			System.out.println("@Number of edges: " + countEdge);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (instanceScanner != null)
				instanceScanner.close();
		}
		return graph;
	}
	
	private static void calculateDensity(Graph graph) {
		graph.getVertices().forEach(v -> v.setDensity(calculateDensity(v)));
	}

	private static double calculateDensity(Vertex vertex) {
		double sum = vertex.getEdges().stream().mapToDouble(edge -> edge.getWeight().getImprovedDelay()).sum();
		return sum == 0 ? vertex.getCost() : vertex.getCost() / sum;
	}
	
	public static boolean canUpgrade(Vertex vertex, double cost, double budget) {
		return (cost + vertex.getCost() <= budget);/* {
			for (Edge edge : vertex.getEdges()) {
				if (edge.getWeight().canImprove())
					return true;
			}
		}
		return false;*/
	}

	/**
	 * GREEDY SOLUTION
	 * (1) calculate densities
	 * (2) sort vertices by density - descending order
	 * (3) upgrade vertices in accordance with the budget constraint
	 * @param graph
	 * @param greedy
	 * @return double (total cost)
	 */
	public static Solution initialSolution(Metaheuristc metaheuristic) {
		Graph graph = metaheuristic.getGraph();
		List<Vertex> vertices = new ArrayList<>(graph.getVertices());
		if (Config.INIT_GREEDY) {
			calculateDensity(graph);
			vertices = graph.getVertices().stream().sorted(Comparator.comparing(Vertex::getDensity)).collect(Collectors.toList());
		}
		return generateSolution(metaheuristic, vertices);
	}
	
	private static Solution generateSolution(Metaheuristc metaheuristic, List<Vertex> vertices) {
		Graph graph = metaheuristic.getGraph();
		Solution solution = new Solution(vertices.size());
		double cost = 0;
		while (!vertices.isEmpty()) {
			int index = Config.INIT_GREEDY ? 0 : Util.random(vertices.size());
			Vertex vertex = vertices.remove(index);
			if (canUpgrade(vertex, cost, graph.getBudget())) {
				upgrade(vertex);
				//calculateDensity(graph);
				solution.set(vertex.getIndex(), 1);
				cost += vertex.getCost();
			}
		}
		solution.setCost(cost);
		metaheuristic.fitness(solution);
		Util.cache(solution);
		return solution;
	}
	
	public static void upgrade(Vertex vertex) {
		vertex.getEdges().forEach(e -> e.getWeight().improve());
		vertex.setUpgraded(1);
	}

	private static void upgrade(Graph graph, Solution solution) {
		int cost = 0;
		for (Vertex vertex : graph.getVertices()) {
			if (solution.yes(vertex.getIndex())) {
				upgrade(vertex);
				cost += vertex.getCost();
			}
		}
		solution.setCost(cost);
	}
	
	public static boolean canUpgrade(Graph graph, Solution solution) {
		double cost = 0;
		for (Vertex vertex : graph.getVertices()) {
			if (canUpgrade(vertex, cost, graph.getBudget())) {
				if (solution.yes(vertex.getIndex()))
					cost += vertex.getCost();
			}
		}
		return cost <= graph.getBudget();
	}
	
	/**
	 * Se a solução for válida, realiza o upgrade no grafo indicado pela solução
	 * @param metaheuristic
	 * @param solution
	 * @return Solution
	 */
	public static boolean upgrade(Metaheuristc metaheuristic, Solution solution) {
		// zera as variáveis de controle do grafo (upgrade dos vértices e delays das arestas)
		metaheuristic.getGraph().reset();
		boolean success = false;
		// havendo solução válida no cahce, apenas aplica os upgrades em todos os vértices indicados pela solução
		if (Util.isCached(solution)) {
			solution = Util.getCached(solution);
			upgrade(metaheuristic.getGraph(), solution);
			success = true;
		}
		// caso contrário se a solução for válida...	
		else if (canUpgrade(metaheuristic.getGraph(), solution)) {
			// aplica os upgrades em todos os vértices indicados pela solução
			upgrade(metaheuristic.getGraph(), solution);
			// limpa o cache das filas de prioridades para cada vértice
			Util.clearPrioritiesQueue();
			// calcula o fitness
			metaheuristic.fitness(solution);
			// guarda solução válida no cache
			Util.cache(solution);
			success = true;
		}
		else
			System.out.println("OH OH...");
		return success;
	}
	
	public static void downgrade(Vertex vertex) {
		vertex.getEdges().forEach(e -> e.getWeight().worsen());
		vertex.setUpgraded(0);
	}
}
