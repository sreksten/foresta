package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.interfacce.GestoreBestiario;
import com.threeamigos.foresta.modellodati.BestiarioMD;

import java.io.*;
import java.util.Arrays;

/**
 * Il bestiario dei test, in memoria: il "file" è il testo che verrebbe scritto su disco.
 */
final class GestoreBestiarioInMemoria implements GestoreBestiario {

	private String contenuto = "";
	private int salvataggi;

	@Override
	public boolean carica(BestiarioMD bestiarioMD) {
		try {
			bestiarioMD.leggi(new BufferedReader(new StringReader(contenuto)));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean salva(BestiarioMD bestiarioMD) {
		StringWriter scrittura = new StringWriter();
		try (PrintWriter writer = new PrintWriter(scrittura)) {
			bestiarioMD.salva(writer);
		} catch (IOException e) {
			return false;
		}
		contenuto = scrittura.toString();
		salvataggi++;
		return true;
	}

	/**
	 * Se il "file" contiene esattamente quella riga.
	 */
	boolean contieneRiga(String riga) {
		return Arrays.asList(contenuto.split("\\R")).contains(riga);
	}

	int getSalvataggi() {
		return salvataggi;
	}
}
