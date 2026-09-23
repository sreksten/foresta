package com.threeamigos.foresta.intermezzi;

import java.util.function.Supplier;

/**
 * Elenca le classi concrete di intermezzo, come ClasseMissione per le missioni. L'ordine
 * conta: se più intermezzi scattano nello stesso momento vengono mostrati in quest'ordine.
 */
public enum ClasseIntermezzo {

	//FIXME va levato dopo le prove
	INTERMEZZO_DI_PROVA(IntermezzoDiProva::new);

	private final Supplier<Intermezzo> supplier;

	ClasseIntermezzo(Supplier<Intermezzo> supplier) {
		this.supplier = supplier;
	}

	public Intermezzo getIstanza() {
		return supplier.get();
	}
}
