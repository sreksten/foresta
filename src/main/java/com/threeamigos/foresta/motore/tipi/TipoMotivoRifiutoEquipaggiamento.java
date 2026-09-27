package com.threeamigos.foresta.motore.tipi;

import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Perché un personaggio non può prendere un artefatto (vedi {@link Personaggio#puoEquipaggiare}).
 * La spiegazione è una frase in terza persona da far seguire al nome del personaggio,
 * es. "Il guerriero" + " porta già troppo peso".
 */
public enum TipoMotivoRifiutoEquipaggiamento {

	TROPPO_CARICO("è già al massimo ddel carico che può portare", "Sono già al massimo del carico che posso portare"),
	SLOT_OCCUPATO("non ha uno slot libero per usare %s", "Sto già usando %s"),
	/**
	 * Le pergamene (slot NUCLEO) restano sempre nell'inventario del gruppo
	 */
	PERGAMENA("le pergamene rimangono nell'inventario del gruppo", "Le pergamene rimangono nell'inventario del gruppo"),
	LIVELLO_TROPPO_ALTO("non ha ancora il Livello per usare %s", "Non ho ancora il Livello per usare %s"),
	/**
	 * Ogni classe usa solo certe armi, lo scudo e il libro (es. niente spadone per il mago)
	 */
	NON_ADATTO_ALLA_CLASSE("non sa usare questo genere di oggetti", "Non so usare questo genere di oggetti"),
	/**
	 * L'armatura chiede una FORZA minima (Costanti.ARMATURA_FORZA_MINIMA)
	 */
	FORZA_INSUFFICIENTE("non ha la forza necessaria per portare %s", "Non ho la forza per portare %s"),
	/**
	 * Solo Ladro/Ladra ed Elfo/Elfa possono impugnare un'arma nella mano secondaria
	 */
	SECONDA_ARMA_NON_CONSENTITA("non sa combattere con due armi", "Non so combattere con due armi"),
	/**
	 * Per un'arma a due mani servono entrambe le mani libere
	 */
	MANI_OCCUPATE_PER_ARMA_A_DUE_MANI("non ha entrambe le mani libere per un'arma a due mani", "Non ho entrambe le mani libere per un'arma a due mani"),
	/**
	 * Arma che non può essere usata con la mano secondaria
	 */
	ARMA_NON_DA_MANO_SECONDARIA("non può usare questa arma con la mano secondaria perché non è un'arma da mano secondaria", "%s non è un'arma da mano secondaria"),
	/**
	 * Tentativo di impugnare una terza arma
	 */
	MANO_SECONDARIA_OCCUPATA("sta già usando anche %s", "Sto già usando anche %s"),
	/**
	 * Chi impugna un'arma a due mani non può prendere scudo, libro o seconda arma
	 */
	ARMA_A_DUE_MANI_IMPUGNATA("impugna già un'arma a due mani", "Impugno già un'arma a due mani");

	private final String spiegazione;
	private final String fumetto;

	TipoMotivoRifiutoEquipaggiamento(String spiegazione, String fumetto) {
		this.spiegazione = spiegazione;
		this.fumetto = fumetto;
	}

	public String getSpiegazione(Personaggio personaggio) {
		return personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + ' ' + spiegazione + '.';
	}

	public String getFumetto() {
		return fumetto;
	}

	/**
	 * Frase completa, es. "Il guerriero porta già troppo peso."
	 */
	public String formattaSpiegazione(Personaggio personaggio) {
		return personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + ' ' + spiegazione + '.';
	}

	public String formattaSpiegazione(Personaggio personaggio, Artefatto artefatto) {
		return personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + ' ' + String.format(spiegazione, artefatto.getNome()) + '.';
	}

	public String formattaFumetto() {
		return fumetto + '.';
	}

	public String formattaFumetto(Artefatto artefatto) {
		return String.format(fumetto, artefatto.getNome()) + '.';
	}
}
