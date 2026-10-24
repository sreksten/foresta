package com.threeamigos.foresta.tipi;

/**
 * I negozi che comprano e vendono artefatti. Più negozi della stessa città stanno sulla stessa casella:
 * il loro magazzino si riconosce da coordinate e tipo di negozio.
 * <p>
 * Ci sono solo l'armaiolo e il venditore di pergamene perché sono le sole botteghe con un magazzino di artefatti da
 * comprare e vendere. Le altre due non ne hanno: l'alchimista vende pozioni, incantesimi, aumenti di magia e mappe da
 * un listino (OfferteAlchimista), che non sono artefatti e non si rivendono; l'incantatore non compra né vende, fonde
 * sul banco gli artefatti che il gruppo gli porta (AutomaIncantatore).
 * <p>
 * I testi di ogni costante (NOME) non stanno qui ma nel file {@code tipo_negozio_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoNegozio {

	/**
	 * Tratta tutti gli artefatti tranne gli ingredienti magici
	 */
	ARMAIOLO,
	/**
	 * Tratta solo gli ingredienti magici: pergamene, gemme, monili, gingilli e sigilli
	 */
	VENDITORE_DI_PERGAMENE;


	TipoNegozio() {
	}

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	/**
	 * @return true se il negozio compra e vende artefatti di quel tipo: gli oggetti che fanno salire di livello un artefatto
	 * e il libro che li riprogramma non li tratta nessuno, perché si trovano solo nei cofani
	 */
	public boolean tratta(TipoArtefatto tipo) {
		if (tipo.isAggiornamento() || tipo.isRiprogrammazione()) {
			return false;
		}
		return (this == VENDITORE_DI_PERGAMENE) == tipo.isIngrediente();
	}
}
