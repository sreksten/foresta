package com.threeamigos.foresta.tipi;

/**
 * I negozi che comprano e vendono artefatti. Più negozi della stessa città stanno sulla stessa casella:
 * il loro magazzino si riconosce da coordinate e tipo di negozio.
 * <p>
 * I testi di ogni costante (NOME) non stanno qui ma nel file {@code tipo_negozio_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 */
public enum TipoNegozio {

	/**
	 * Tratta tutti gli artefatti tranne le pergamene
	 */
	ARMAIOLO,
	/**
	 * Tratta solo le pergamene
	 */
	VENDITORE_DI_PERGAMENE;


	TipoNegozio() {
	}

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
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
