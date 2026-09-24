package com.threeamigos.foresta.motore.modellodati;

/**
 * I negozi che comprano e vendono artefatti. Più negozi della stessa città stanno sulla stessa casella:
 * il loro magazzino si riconosce da coordinate e tipo di negozio.
 */
public enum TipoNegozio {

	/**
	 * Tratta tutti gli artefatti tranne le pergamene
	 */
	ARMAIOLO("Armaiolo"),
	/**
	 * Tratta solo le pergamene
	 */
	VENDITORE_DI_PERGAMENE("Venditore di pergamene");

	private final String nome;

	TipoNegozio(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

	/**
	 * @return true se il negozio compra e vende artefatti di quel tipo
	 */
	public boolean tratta(TipoArtefatto tipo) {
		boolean pergamena = tipo == TipoArtefatto.INCANTAMENTO;
		return this == VENDITORE_DI_PERGAMENE ? pergamena : !pergamena;
	}
}
