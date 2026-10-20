package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Legge i campi di una riga di salvataggio separati da {@link Serializzabile#PIPE}.
 * A differenza di StringTokenizer conserva i campi vuoti ("a||b" sono tre campi), così un testo
 * vuoto non sposta i campi che lo seguono. Una riga vuota non ha nessun campo: è il caso delle
 * liste vuote scritte su una sola riga.
 *
 * @author Stefano Reksten
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

	private LettoreCampi(String riga, String[] campi) {
		this.riga = riga;
		this.campi = campi;
	}

	/**
	 * La prossima riga del salvataggio, che contiene solo un numero intero (per esempio quanti elementi seguono).
	 */
	public static int leggiIntero(BufferedReader stream) throws IOException {
		return new LettoreCampi(stream.readLine()).intero();
	}

	/**
	 * La prossima riga del salvataggio, che contiene solo un testo (per esempio un uuid); un file finito prima o una riga vuota
	 * sono un errore, non un null.
	 */
	public static String leggiTesto(BufferedReader stream) throws IOException {
		return new LettoreCampi(stream.readLine()).testo();
	}

	/**
	 * La prossima riga del salvataggio, che contiene solo un valore vero o falso.
	 */
	public static boolean leggiBooleano(BufferedReader stream) throws IOException {
		return new LettoreCampi(stream.readLine()).booleano();
	}

	/**
	 * Dice se ci sono ancora campi da leggere.
	 */
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

	/**
	 * Il prossimo campo come numero intero.
	 *
	 * @throws IOException anche se il campo non è un numero
	 */
	public int intero() throws IOException {
		String testo = testo();
		try {
			return Integer.parseInt(testo);
		} catch (NumberFormatException e) {
			throw new IOException("\"" + testo + "\" non è un numero intero nella riga: " + riga, e);
		}
	}

	/**
	 * Il prossimo campo come numero decimale.
	 *
	 * @throws IOException anche se il campo non è un numero
	 */
	public double decimale() throws IOException {
		String testo = testo();
		try {
			return Double.parseDouble(testo);
		} catch (NumberFormatException e) {
			throw new IOException("\"" + testo + "\" non è un numero nella riga: " + riga, e);
		}
	}

	/**
	 * Il prossimo campo come valore vero o falso.
	 */
	public boolean booleano() throws IOException {
		return Boolean.parseBoolean(testo());
	}

	/**
	 * Il prossimo campo come valore dell'enum, in base al suo nome; un campo vuoto non è valido (per quelli che possono mancare vedi {@link #enumeratoFacoltativo}).
	 */
	public <E extends Enum<E>> E enumerato(Class<E> classe) throws IOException {
		return valoreDi(classe, testo());
	}

	/**
	 * Il valore dell'enum con quel nome.
	 *
	 * @throws IOException se non esiste: un salvataggio rovinato, o scritto da una versione diversa
	 */
	private <E extends Enum<E>> E valoreDi(Class<E> classe, String nome) throws IOException {
		try {
			return Enum.valueOf(classe, nome);
		} catch (IllegalArgumentException e) {
			throw new IOException("\"" + nome + "\" non è un valore di " + classe.getSimpleName() + " nella riga: " + riga, e);
		}
	}

	/**
	 * Il prossimo campo diviso nei suoi sottocampi, separati da {@value MappaProprieta#SEPARATORE} (per esempio
	 * "FORZA§12" o i campi di un modificatore), da leggere a loro volta con questi metodi. Con {@code massimo}
	 * sottocampi l'ultimo tiene tutto il resto, anche se contiene il separatore (è il caso di una nota). Meno di
	 * {@code minimo} sottocampi è un salvataggio rovinato.
	 *
	 * @throws IOException se il campo ha meno di {@code minimo} sottocampi
	 */
	public LettoreCampi sottocampi(int minimo, int massimo) throws IOException {
		String campo = testo();
		String[] sottocampi = campo.split(MappaProprieta.SEPARATORE, massimo);
		if (sottocampi.length < minimo) {
			throw new IOException("Mancano sottocampi nel campo \"" + campo + "\" della riga: " + riga);
		}
		return new LettoreCampi(campo, sottocampi);
	}

	/**
	 * Tutto il resto della riga, anche con dentro il separatore dei campi: per l'ultimo campo di una riga che può
	 * contenere il "|" (vedi {@link Serializzabile#senzaACapo(String)}). Vuoto se i campi sono finiti.
	 */
	public String resto() {
		String resto = String.join(Serializzabile.PIPE, Arrays.asList(campi).subList(indice, campi.length));
		indice = campi.length;
		return resto;
	}

	/**
	 * Il prossimo campo come valore dell'enum, oppure null se è vuoto.
	 */
	public <E extends Enum<E>> E enumeratoFacoltativo(Class<E> classe) throws IOException {
		String testo = testo();
		return testo.isEmpty() ? null : valoreDi(classe, testo);
	}
}
