package com.threeamigos.foresta.serializzazione;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Dichiara che un oggetto sa scriversi in un salvataggio e rileggersi da lì.
 * <p>
 * Un salvataggio è un file di testo, una riga per record, con i campi separati da {@link #PIPE}. Per esempio la riga
 * {@code GUERRIERO|Bruno|12|} ha quattro campi: un'enum (scritta con il suo nome), un testo, un numero e un campo vuoto
 * (un valore che manca, vedi {@link #facoltativo(Object)}). Chi implementa l'interfaccia scrive le sue righe in
 * {@link #salva(PrintWriter)} con {@link #stampaRiga(PrintWriter, Object...)} e le rilegge in
 * {@link #leggi(BufferedReader)} con {@link LettoreCampi}, <b>nello stesso ordine</b>: il formato non ha nomi di campo,
 * conta solo la posizione.
 * <p>
 * I metodi {@code static} qui sotto sono gli aiuti comuni per scrivere i campi (in un'interfaccia Java un metodo
 * {@code static} si chiama con il nome dell'interfaccia: {@code Serializzabile.campo(x)}).
 *
 * @author Stefano Reksten
 */
public interface Serializzabile {

	/**
	 * Il separatore dei campi di una riga del salvataggio.
	 */
	String PIPE = "|";

	/**
	 * Il nome di ciò a cui è stato dato un nome vuoto (artefatti, incantamenti, classifica)
	 */
	String NESSUN_NOME = "nessun nome";

	/**
	 * Pulisce in silenzio un testo che finirà in un campo di un salvataggio: toglie il separatore dei campi (un nome con
	 * dentro un "|" sposterebbe tutti i campi seguenti alla rilettura) e trasforma gli a capo in uno spazio (il
	 * salvataggio è a righe, un a capo spezzerebbe la riga).
	 */
	static String pulisciCampo(String testo) {
		return testo == null ? null : senzaACapo(testo.replace(PIPE, ""));
	}

	/**
	 * Trasforma gli a capo in uno spazio, e lascia il resto: per l'ultimo campo di una riga, che può contenere il "|".
	 */
	static String senzaACapo(String testo) {
		return testo == null ? null : testo.replaceAll("\\r\\n|\\r|\\n", " ");
	}

	/**
	 * Come scrivere un campo che può mancare: il campo vuoto, che {@link LettoreCampi} rilegge come null.
	 */
	static String facoltativo(Object valore) {
		return valore == null ? "" : campo(valore);
	}

	/**
	 * Come si scrive un valore in un campo: un enum con il suo nome, il resto come lo scriverebbe PrintWriter.print
	 * (null diventa "null": per un campo che può mancare c'è {@link #facoltativo(Object)}).
	 */
	static String campo(Object valore) {
		return valore instanceof Enum ? ((Enum<?>) valore).name() : String.valueOf(valore);
	}

	/**
	 * Scrive una riga del salvataggio: i valori, uno per campo, separati da {@link #PIPE}, e poi l'a capo (vedi
	 * {@link #campo(Object)}). Senza valori scrive una riga vuota. È quella che {@link LettoreCampi} rilegge campo per campo.
	 */
	static void stampaRiga(PrintWriter stream, Object... valori) {
		stampaRiga(stream, Arrays.asList(valori));
	}

	/**
	 * Come {@link #stampaRiga(PrintWriter, Object...)}, quando quanti sono i valori si sa solo scrivendo (per esempio
	 * una lista riempita in un ciclo).
	 */
	static void stampaRiga(PrintWriter stream, Collection<?> valori) {
		stream.println(valori.stream().map(Serializzabile::campo).collect(Collectors.joining(PIPE)));
	}

	/**
	 * Più valori in un solo campo, separati da {@value MappaProprieta#SEPARATORE} (per esempio "FORZA§12"): quello che
	 * {@link LettoreCampi#sottocampi(int, int)} rilegge.
	 */
	static String composto(Object... valori) {
		return Arrays.stream(valori).map(Serializzabile::campo).collect(Collectors.joining(MappaProprieta.SEPARATORE));
	}

	/**
	 * Scrive l'oggetto sul flusso, in un formato di testo a righe che {@link #leggi(BufferedReader)} sa rileggere.
	 */
	void salva(PrintWriter stream) throws IOException;
	
	/**
	 * Rilegge l'oggetto da ciò che ha scritto {@link #salva(PrintWriter)}, sostituendo il suo contenuto attuale.
	 */
	void leggi(BufferedReader stream) throws IOException;
	
}
