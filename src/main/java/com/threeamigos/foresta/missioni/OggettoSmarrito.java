package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tools.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Un oggetto smarrito nella foresta da ritrovare (vedi LOggettoSmarrito), letto da una riga di missioni.txt: genere
 * ("F" o "M"), oggetto, chi l'ha perso, i posti vicino a cui può averlo perso, monete, il racconto di chi l'ha perso,
 * la battuta del capo, la sua risposta e il ringraziamento, separati da ";".
 * <pre>
 * F;fede nuziale;il mugnaio;TEMPIO ROVINE;15;Al ritorno la fede non c'era più.;E se se ne accorge?;Meglio di no.;Eccola!
 * </pre>
 * L'oggetto è senza articolo, chi l'ha perso con l'articolo. I posti sono fra {@link #POSTI}.
 */
public final class OggettoSmarrito {

	/**
	 * I posti vicino a cui si può perdere qualcosa: hanno un nome o almeno un aspetto che si riconosce sulla mappa.
	 */
	public static final Set<TipoLocazione> POSTI = Collections.unmodifiableSet(EnumSet.of(
			TipoLocazione.TEMPIO, TipoLocazione.ROVINE, TipoLocazione.LOCANDA, TipoLocazione.GROTTA));

	private static final String SEPARATORE = ";";
	private static final int CAMPI = 9;

	private final String riga;
	private final boolean femminile;
	private final String oggetto;
	private final String proprietario;
	private final List<TipoLocazione> posti;
	private final int monete;
	private final String racconto;
	private final String battutaDelCapo;
	private final String risposta;
	private final String ringraziamento;

	private OggettoSmarrito(String riga) {
		this.riga = Objects.requireNonNull(riga);
		String[] campi = riga.split(SEPARATORE, -1);
		if (campi.length != CAMPI) {
			throw new IllegalArgumentException("Un oggetto smarrito ha " + CAMPI + " campi: " + riga);
		}
		String genere = campi[0].trim();
		if (!"F".equals(genere) && !"M".equals(genere)) {
			throw new IllegalArgumentException("Il genere è F o M: " + riga);
		}
		femminile = "F".equals(genere);
		oggetto = campi[1].trim();
		proprietario = campi[2].trim();
		List<TipoLocazione> dove = new ArrayList<>();
		for (String posto : campi[3].trim().split("\\s+")) {
			TipoLocazione tipo = TipoLocazione.valueOf(posto);
			if (!POSTI.contains(tipo)) {
				throw new IllegalArgumentException("I posti sono fra " + POSTI + ": " + riga);
			}
			dove.add(tipo);
		}
		posti = Collections.unmodifiableList(dove);
		monete = Integer.parseInt(campi[4].trim());
		if (monete < 1) {
			throw new IllegalArgumentException("Monete sbagliate: " + riga);
		}
		racconto = campi[5].trim();
		battutaDelCapo = campi[6].trim();
		risposta = campi[7].trim();
		ringraziamento = campi[8].trim();
	}

	/**
	 * L'oggetto smarrito di una riga di missioni.txt (o di una riga salvata con {@link #getRiga()}).
	 */
	public static OggettoSmarrito da(String riga) {
		return new OggettoSmarrito(riga);
	}

	/**
	 * La riga da cui è stato letto, da salvare con la missione.
	 */
	public String getRiga() {
		return riga;
	}

	public boolean isFemminile() {
		return femminile;
	}

	/**
	 * L'oggetto, senza articolo: "fede nuziale".
	 */
	public String getOggetto() {
		return oggetto;
	}

	/**
	 * L'oggetto con l'articolo: "la fede nuziale".
	 */
	public String getOggettoConArticolo() {
		return (femminile ? Misc.LA : Misc.IL) + oggetto;
	}

	/**
	 * Il pronome per l'oggetto: "la" o "lo".
	 */
	public String getPronome() {
		return femminile ? "la" : "lo";
	}

	/**
	 * "persa" o "perso".
	 */
	public String getPerso() {
		return femminile ? "persa" : "perso";
	}

	/**
	 * Il nome dell'oggetto di missione da ritrovare (uno solo).
	 */
	public NomeOggetto getNome() {
		return femminile ? NomeOggetto.femminile(oggetto, oggetto) : NomeOggetto.maschile(oggetto, oggetto);
	}

	/**
	 * Chi l'ha perso, con l'articolo: "il mugnaio".
	 */
	public String getProprietario() {
		return proprietario;
	}

	/**
	 * I posti vicino a cui può averlo perso: la missione ne sceglie uno.
	 */
	public List<TipoLocazione> getPosti() {
		return posti;
	}

	public int getMonete() {
		return monete;
	}

	/**
	 * Come l'ha perso: la prima battuta di chi l'ha perso nell'intermezzo.
	 */
	public String getRacconto() {
		return racconto;
	}

	public String getBattutaDelCapo() {
		return battutaDelCapo;
	}

	public String getRisposta() {
		return risposta;
	}

	/**
	 * Che cosa dice chi l'ha perso quando lo riavrà.
	 */
	public String getRingraziamento() {
		return ringraziamento;
	}
}
