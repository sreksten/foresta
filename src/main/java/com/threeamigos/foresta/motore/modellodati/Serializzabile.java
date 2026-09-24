package com.threeamigos.foresta.motore.modellodati;

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
	 * Toglie in silenzio il separatore dei campi da un testo che finirà in un salvataggio:
	 * un nome con dentro un "|" sposterebbe tutti i campi seguenti alla rilettura.
	 */
	static String senzaPipe(String testo) {
		return testo == null ? null : testo.replace(PIPE, "");
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

	void salva(PrintWriter stream) throws IOException;
	
	void leggi(BufferedReader stream) throws IOException;
	
}
