package br.ufrj.cos.mhoc.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import br.ufrj.cos.mhoc.algorithm.ILS;
import br.ufrj.cos.mhoc.type.Solution;
import br.ufrj.cos.mhoc.type.graph.Graph;
import br.ufrj.cos.mhoc.type.graph.Vertex;

public class ILSUtil {
	private ILSUtil() {
		super();
	}

	public static Solution disturb(ILS ils, Solution solution) {
		Graph graph = ils.getGraph();

		// perturba��o da solu��o, retornando uma nova inst�ncia de solu��o
		Solution disturbed = solution.clone();
		double totalCost = solution.getCost();

		// lista de v�rtices com upgrade (yes)
		List<Vertex> yes = graph.getVertices().stream().filter(v -> v.isUpgraded()).collect(Collectors.toList());

		int numberOfDisturbances = (int) (yes.size() * Config.DISTURBANCES_RATE);
		
		if (totalCost == 0 || yes.size() == 0) // solu��o vazia (sem nenhum upgrade)
			throw new RuntimeException("disturb(): solution without update!");
		
		// monta lista de v�rtices sem upgrade (no)
		List<Vertex> no = graph.getVertices().stream().filter(v -> !v.isUpgraded()).collect(Collectors.toList());

		// DECONSTRUCTION
		int countDowngrades = 0;
		while (countDowngrades++ < numberOfDisturbances) {
			// seleciona aleatoriamente um v�rtice com upgrade
			Vertex toDown = yes.get(Util.random(yes.size()));

			// downgrade
			GraphUtil.downgrade(toDown);
			totalCost -= toDown.getCost(); // diminui o custo total
			disturbed.set(toDown.getIndex(), 0);
			
			yes.remove(toDown);
		}
		// devolte os v�rtices que n�o tiveram downgrade para na lista dos que poder�o ter upgrade
		yes.forEach(v -> { v.reset(); v.setPenality(Config.DOWNGRADE_PENALITY); });
		no.addAll(yes);

		// CONSTRUCTION
		while (!no.isEmpty() && totalCost <= graph.getBudget()) {
			
			// seleciona aleatoriamente um v�rtice sem upgrade
			Vertex toUp = no.get(Util.random(no.size()));
			
			boolean accepted = (toUp.getPenality() == 0 || Util.rateAcceptedRandomly(toUp.getPenality()));
			
			// construction: upgrade
			if (accepted && GraphUtil.canUpgrade(toUp, totalCost, graph.getBudget())) {
				GraphUtil.upgrade(toUp);
				totalCost += toUp.getCost(); // aumenta o custo total
				disturbed.set(toUp.getIndex(), 1);
			}
			no.remove(toUp);
		}
		disturbed.setCost(totalCost);
		
		if (!GraphUtil.upgrade(ils, disturbed))
			throw new RuntimeException("disturb(): valid solution did not upgrade in graph");
			
		return disturbed;
	}

	public static Solution acceptanceCriteria(Solution local, Solution modified, int iteration) {
		if (!local.equals(modified)) { // n�o ocorreu perturba��o
			// verifica o crit�rio entre this.best e improved... se this.best ser� substitu�do (return true) ou n�o (return false)
			if (modified.getFitness() < local.getFitness()) {
				local = modified; // substitui solu��o local
			}
			// verifica se aceita a piora da solu��o
			else {
				BigDecimal factor = new BigDecimal(iteration);
				factor = factor.divide(new BigDecimal(Config.MAX_ITER));
				BigDecimal rate = BigDecimal.valueOf(1).subtract(factor).setScale(4);
				if (Util.rateAcceptedRandomly(rate.doubleValue())) {
					local = modified; // aceita a pior solu��o como solu��o corrente
				}
			}
		}
		return local;
	}
}
