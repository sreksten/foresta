package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.motore.modellodati.LettoreCampi;
import com.threeamigos.foresta.motore.modellodati.Serializzabile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class GestorePunteggiSuFile extends GestorePunteggiBase {

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
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(nomeFile())), StandardCharsets.UTF_8))) {
			for (int posizione = 0; posizione < getConteggio(); posizione++) {
				LettoreCampi campi = new LettoreCampi(reader.readLine());
				String nome = campi.testo();
				int punteggio = campi.intero();
				setPunteggio(posizione, nome, punteggio);
			}
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}

	public boolean salva() {
		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(nomeFile())), StandardCharsets.UTF_8))) {
			for (int posizione = 0; posizione < getConteggio(); posizione++) {
				Punteggio punteggio = getPunteggio(posizione);
				writer.print(punteggio.getNome());
				writer.print(Serializzabile.PIPE);
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
