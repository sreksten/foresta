package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoException;
import com.threeamigos.foresta.eventi.EventoMessaggioInterno;
import com.threeamigos.foresta.motore.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestoreSalvataggiSuFile extends GestoreSalvataggiBase {

	private static final String POSTFISSO_FILE = ".TXT";

	@Override
	public List<InterfacciaTestataSalvataggio> getSalvataggiDisponibili() {
		File directorySalvataggi = recuperaDirectory();
		List<InterfacciaTestataSalvataggio> salvataggi = new ArrayList<>();
		for (int i = 1; i <= NUMERO_MASSIMO; i++) {
			File salvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + i + POSTFISSO_FILE);
			if (salvataggio.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(salvataggio))) {
					String line = reader.readLine();
					Salvataggio testataSalvataggio = new Salvataggio();
					testataSalvataggio.setId(String.valueOf(i));
					testataSalvataggio.setNome(line);
					salvataggi.add(testataSalvataggio);
				} catch (Exception e) {
					BusEventi.pubblica(new EventoException(e));
				}
			}
		}
		return salvataggi;
	}

	@Override
	public InterfacciaGestoreSalvataggi.InterfacciaSalvataggio recuperaSalvataggio(String id) {
		File directorySalvataggi = recuperaDirectory();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + id + POSTFISSO_FILE);
		if (fileSalvataggio.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(fileSalvataggio))) {
				String line = reader.readLine();
				BusEventi.pubblica(new EventoMessaggioInterno("Lettura file salvataggio " + line));
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sb.append(line);
					sb.append('\n');
				}
				Salvataggio salvataggio = new Salvataggio();
				salvataggio.setId(fileSalvataggio.getPath());
				salvataggio.setNome(line);
				salvataggio.setContenuto(sb.toString());
				return salvataggio;
			} catch (Exception e) {
				BusEventi.pubblica(new EventoException(e));
				return null;
			}
		} else {
			Logger.log("Tentativo di lettura di file non esistente: " + id);
			return null;
		}
	}

	@Override
	public void salva(InterfacciaGestoreSalvataggi.InterfacciaSalvataggio salvataggio) {
		File directorySalvataggi = recuperaDirectory();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + salvataggio.getId() + POSTFISSO_FILE);
		try (PrintWriter writer = new PrintWriter(new FileWriter(fileSalvataggio))) {
			writer.println(salvataggio.getDescrizione());
			writer.println(salvataggio.getContenuto());
			writer.flush();
		} catch (IOException e) {
			Logger.log(e);
		}
	}
}
