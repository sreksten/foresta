package com.threeamigos.foresta.tipi;

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
	 * @return true se il negozio compra e vende artefatti di quel tipo: gli oggetti che fanno salire di livello un artefatto
	 * non li tratta nessuno, perché si trovano solo nei cofani
	 */
	public boolean tratta(TipoArtefatto tipo) {
		if (tipo.isAggiornamento()) {
			return false;
		}
		return (this == VENDITORE_DI_PERGAMENE) == tipo.isIngrediente();
	}
}
