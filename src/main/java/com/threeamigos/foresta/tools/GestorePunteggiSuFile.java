package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public final class GestorePunteggiSuFile extends GestorePunteggiBase {

	private static final String SEPARATORE = "#";
	private String nomeFile;

	public GestorePunteggiSuFile() { 
		super();
	}

	private String nomeFile() {
		if (nomeFile == null)
			nomeFile = recuperaDirectory().getPath() + File.separatorChar + "forestaHS";
		return nomeFile;
	}

	public boolean carica() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(nomeFile()))))) {
			String line;
			StringTokenizer st;
			for (int posizione = 0; posizione < getConteggio(); posizione++) {
				line = reader.readLine();
				st = new StringTokenizer(line, SEPARATORE);
				String nome = st.nextToken();
				int punteggio = Integer.parseInt(st.nextToken());
				setPunteggio(posizione, nome, punteggio);
			}
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}

	public boolean salva() {
		try (PrintWriter writer = new PrintWriter(Files.newOutputStream(Paths.get(nomeFile())))) {
			for (int posizione = 0; posizione < getConteggio(); posizione++) {
				Punteggio punteggio = getPunteggio(posizione);
				writer.print(punteggio.getNome());
				writer.print(SEPARATORE);
				writer.println(punteggio.getPunteggio());
			}
			writer.flush();
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}
}
