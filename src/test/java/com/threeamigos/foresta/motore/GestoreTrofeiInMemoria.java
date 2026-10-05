package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.interfacce.GestoreTrofei;
import com.threeamigos.foresta.motore.modellodati.TrofeiMD;
import com.threeamigos.foresta.tipi.TipoTrofeo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * I trofei dei test, in memoria: il "file" è il testo che verrebbe scritto su disco.
 */
final class GestoreTrofeiInMemoria implements GestoreTrofei {

	private String contenuto = "";
	private int salvataggi;

	@Override
	public boolean carica(TrofeiMD trofeiMD) {
		try {
			trofeiMD.leggi(new BufferedReader(new StringReader(contenuto)));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean salva(TrofeiMD trofeiMD) {
		StringWriter scrittura = new StringWriter();
		try (PrintWriter writer = new PrintWriter(scrittura)) {
			trofeiMD.salva(writer);
		} catch (IOException e) {
			return false;
		}
		contenuto = scrittura.toString();
		salvataggi++;
		return true;
	}

	/**
	 * Scrive nel "file" i trofei indicati come vinti, come se lo fossero in partite precedenti.
	 * Va seguito da RegistroTrofei.impostaGestoreTrofei perché il registro li rilegga.
	 */
	void conVinti(TipoTrofeo... vinti) {
		TrofeiMD trofeiMD = new TrofeiMD();
		for (TipoTrofeo trofeo : vinti) {
			trofeiMD.aggiungiVinto(trofeo);
		}
		salva(trofeiMD);
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
