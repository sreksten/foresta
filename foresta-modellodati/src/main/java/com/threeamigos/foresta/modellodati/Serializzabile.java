package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Dichiara che un certo oggetto può essere scritto e riletto da un flusso di input/output
 *
 * @author Stefano Reksten
 */
public interface Serializzabile {

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
		if (valore == null) {
			return "";
		}
		return valore instanceof Enum ? ((Enum<?>) valore).name() : valore.toString();
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
