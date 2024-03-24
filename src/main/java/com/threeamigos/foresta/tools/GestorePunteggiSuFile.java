package com.threeamigos.foresta.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.threeamigos.foresta.motore.Logger;

public final class GestorePunteggiSuFile extends GestorePunteggiBase {

	private static final String SEPARATORE = "#";
	private String nomeFile;

	public GestorePunteggiSuFile() { 
		super();
	}

	private final String nomeFile() {
		if (nomeFile == null)
			nomeFile = new StringBuilder(System.getProperty("user.home"))
			.append(File.separatorChar).append(".forestaHS").toString();
		return nomeFile;
	}

	public boolean carica() {
		Logger.log("Leggo i punteggi dal file " + nomeFile);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(nomeFile())))) {
			String line;
			StringTokenizer st;
			int posizione = 0;
			int cardinalita = getCardinalita();
			while (posizione < cardinalita) {
				line = reader.readLine();
				st = new StringTokenizer(line, SEPARATORE);
				String nome = st.nextToken();
				int punteggio = Integer.parseInt(st.nextToken());
				setRecord(posizione, nome, punteggio);
				posizione++;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean salva() {
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(nomeFile()))) {
			int posizione = 0;
			int cardinalita = getCardinalita();
			while (posizione < cardinalita) {
				InterfacciaGestorePunteggi.Record record = getRecord(posizione);
				writer.print(record.getNome());
				writer.print(SEPARATORE);
				writer.println(record.getPunteggio());
				posizione++;
			}
			writer.flush();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
