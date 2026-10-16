package com.threeamigos.foresta.tipi;

import java.util.Locale;


/**
 * Perché un personaggio non può prendere un artefatto (vedi Personaggio.puoEquipaggiare e RegoleEquipaggiamento).
 * La spiegazione è una frase in terza persona da far seguire al nome del personaggio,
 * es. "Il guerriero" + " porta già troppo peso". I testi (spiegazione e fumetto di ciascun motivo) non stanno qui ma nel
 * file {@code rifiuti_equipaggiamento_<lingua>.properties}, in modo che il gioco si possa tradurre.
 */
public enum TipoMotivoRifiutoEquipaggiamento {

	/**
	 * Il carico del personaggio è già al massimo
	 */
	TROPPO_CARICO,
	/**
	 * Lo slot dell'artefatto è già occupato da un altro
	 */
	SLOT_OCCUPATO,
	/**
	 * Le pergamene (slot NUCLEO) restano sempre nell'inventario del gruppo
	 */
	PERGAMENA,
	/**
	 * Il livello dell'artefatto supera quello del personaggio
	 */
	LIVELLO_TROPPO_ALTO,
	/**
	 * Ogni classe usa solo certe armi, lo scudo, il libro e l'armatura (es. niente spadone per il mago, niente armatura per il
	 * ladro): non è che non li sappia usare, è che non li usa proprio
	 */
	NON_ADATTO_ALLA_CLASSE,
	/**
	 * L'armatura chiede una FORZA minima (Costanti.ARMATURA_FORZA_MINIMA) a chi la può portare
	 */
	FORZA_INSUFFICIENTE,
	/**
	 * Solo Ladro/Ladra ed Elfo/Elfa impugnano un'arma nella mano secondaria: non è che gli altri non sappiano, è che la
	 * loro classe non combatte così (come per NON_ADATTO_ALLA_CLASSE)
	 */
	SECONDA_ARMA_NON_CONSENTITA,
	/**
	 * Per un'arma a due mani servono entrambe le mani libere
	 */
	MANI_OCCUPATE_PER_ARMA_A_DUE_MANI,
	/**
	 * Arma che non può essere usata con la mano secondaria
	 */
	ARMA_NON_DA_MANO_SECONDARIA,
	/**
	 * Tentativo di impugnare una terza arma
	 */
	MANO_SECONDARIA_OCCUPATA,
	/**
	 * Chi impugna un'arma a due mani non può prendere scudo, libro o seconda arma
	 */
	ARMA_A_DUE_MANI_IMPUGNATA;

	private static final String FILE = "rifiuti_equipaggiamento";

	/**
	 * La frase in terza persona da far seguire al nome del personaggio, nella lingua dei testi (vedi {@link Testi}); con
	 * un %s al posto del nome dell'artefatto se il motivo lo nomina. Sta nel file
	 * {@code rifiuti_equipaggiamento_<lingua>.properties}, alla voce {@code MOTIVO.SPIEGAZIONE}.
	 */
	private String spiegazione() {
		return Testi.get(FILE, name() + ".SPIEGAZIONE").orElse(name());
	}

	/**
	 * La frase che il personaggio dice nel fumetto, in prima persona, con lo stesso %s di {@link #spiegazione()}; alla voce
	 * {@code MOTIVO.FUMETTO} dello stesso file.
	 */
	private String fumetto() {
		return Testi.get(FILE, name() + ".FUMETTO").orElse(name());
	}

	/**
	 * Frase completa dopo il nome del personaggio, es. "Il guerriero" + " porta già troppo peso."
	 */
	public String formattaSpiegazione(String nomePersonaggio) {
		return nomePersonaggio + ' ' + spiegazione() + '.';
	}

	/**
	 * Come sopra, con il nome dell'artefatto al posto del %s della spiegazione
	 */
	public String formattaSpiegazione(String nomePersonaggio, String nomeArtefatto) {
		return nomePersonaggio + ' ' + String.format(Locale.ROOT, spiegazione(), nomeArtefatto) + '.';
	}

	/**
	 * La frase che il personaggio dice nel fumetto, per i motivi che non nominano l'artefatto.
	 */
	public String formattaFumetto() {
		return fumetto() + '.';
	}

	/**
	 * La frase che il personaggio dice nel fumetto, con il nome dell'artefatto al posto del %s.
	 */
	public String formattaFumetto(String nomeArtefatto) {
		return String.format(Locale.ROOT, fumetto(), nomeArtefatto) + '.';
	}
}
