package br.ufrj.cos.mhoc.algorithm;

import br.ufrj.cos.mhoc.type.Solution;

public interface ILS extends Metaheuristc {
	public Solution disturb(Solution solution);
}
