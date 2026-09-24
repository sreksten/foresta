package com.threeamigos.foresta.motore.modellodati;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Legge i campi di una riga di salvataggio separati da {@link Serializzabile#PIPE}.
 * A differenza di StringTokenizer conserva i campi vuoti ("a||b" sono tre campi), così un testo
 * vuoto non sposta i campi che lo seguono. Una riga vuota non ha nessun campo: è il caso delle
 * liste vuote scritte su una sola riga.
 */
public final class LettoreCampi {

	private final String riga;
	private final String[] campi;
	private int indice;

	private static final Pattern SEPARATORE = Pattern.compile(Pattern.quote(Serializzabile.PIPE));

	public LettoreCampi(String riga) throws IOException {
		if (riga == null) {
			throw new IOException("Fine del salvataggio inattesa");
		}
		this.riga = riga;
		this.campi = riga.isEmpty() ? new String[0] : SEPARATORE.split(riga, -1);
	}

	public boolean haAltriCampi() {
		return indice < campi.length;
	}

	/**
	 * Il prossimo campo così com'è, anche vuoto.
	 */
	public String testo() throws IOException {
		if (!haAltriCampi()) {
			throw new IOException("Mancano campi nella riga: " + riga);
		}
		return campi[indice++];
	}

	/**
	 * Il prossimo campo, oppure null se è vuoto (vedi {@link Serializzabile#facoltativo(Object)}).
	 */
	public String testoFacoltativo() throws IOException {
		String testo = testo();
		return testo.isEmpty() ? null : testo;
	}

	public int intero() throws IOException {
		return Integer.parseInt(testo());
	}

	public double decimale() throws IOException {
		return Double.parseDouble(testo());
	}

	public boolean booleano() throws IOException {
		return Boolean.parseBoolean(testo());
	}

	public <E extends Enum<E>> E enumerato(Class<E> classe) throws IOException {
		return Enum.valueOf(classe, testo());
	}

	/**
	 * Il prossimo campo come valore dell'enum, oppure null se è vuoto.
	 */
	public <E extends Enum<E>> E enumeratoFacoltativo(Class<E> classe) throws IOException {
		String testo = testo();
		return testo.isEmpty() ? null : Enum.valueOf(classe, testo);
	}
}
