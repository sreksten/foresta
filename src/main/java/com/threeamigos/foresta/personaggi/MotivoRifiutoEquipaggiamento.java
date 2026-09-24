package com.threeamigos.foresta.personaggi;

/**
 * Perché un personaggio non può prendere un artefatto (vedi {@link Personaggio#puoEquipaggiare}).
 * La spiegazione è una frase in terza persona da far seguire al nome del personaggio,
 * es. "Il guerriero" + " porta già troppo peso".
 */
public enum MotivoRifiutoEquipaggiamento {

	TROPPO_CARICO("porta già troppo peso"),
	SLOT_OCCUPATO("ha già qualcosa in quel posto"),
	/**
	 * Le pergamene (slot NUCLEO) restano sempre nell'inventario del gruppo
	 */
	PERGAMENA("non può portare le pergamene, che restano nel gruppo"),
	LIVELLO_TROPPO_ALTO("non ha ancora il livello per usare questo oggetto"),
	/**
	 * Solo Ladro/Ladra ed Elfo/Elfa possono impugnare un'arma nella mano secondaria
	 */
	SECONDA_ARMA_NON_CONSENTITA("non sa combattere con due armi"),
	/**
	 * Per un'arma a due mani servono entrambe le mani libere
	 */
	MANI_OCCUPATE("non ha entrambe le mani libere per un'arma a due mani"),
	/**
	 * Chi impugna un'arma a due mani non può prendere scudo, libro o seconda arma
	 */
	ARMA_A_DUE_MANI_IMPUGNATA("impugna già un'arma a due mani");

	private final String spiegazione;

	MotivoRifiutoEquipaggiamento(String spiegazione) {
		this.spiegazione = spiegazione;
	}

	public String getSpiegazione() {
		return spiegazione;
	}

	/**
	 * Frase completa, es. "Il guerriero porta già troppo peso."
	 */
	public String getFrase(Personaggio personaggio) {
		return personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + ' ' + spiegazione + '.';
	}
}
