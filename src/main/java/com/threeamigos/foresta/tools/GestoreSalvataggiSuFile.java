package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoException;
import com.threeamigos.foresta.eventi.EventoMessaggioInterno;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestoreSalvataggiSuFile extends GestoreSalvataggiBase {

	private static final String POSTFISSO_FILE = ".TXT";

	@Override
	public List<TestataSalvataggio> getSalvataggiDisponibili() {
		File directorySalvataggi = recuperaDirectory();
		List<TestataSalvataggio> salvataggi = new ArrayList<>();
		for (int i = 1; i <= NUMERO_MASSIMO; i++) {
			try {
				File salvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + i + POSTFISSO_FILE);
				if (salvataggio.exists() && salvataggio.canRead()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(salvataggio))) {
						String line = reader.readLine();
						SalvataggioImpl testataSalvataggio = new SalvataggioImpl();
						testataSalvataggio.setId(String.valueOf(i));
						testataSalvataggio.setNome(line);
						salvataggi.add(testataSalvataggio);
					}
				}
			} catch (Exception e) {
				BusEventi.pubblica(new EventoException("Durante lettura file di salvataggio " + i, e));
			}
		}
		return salvataggi;
	}

	@Override
	public Salvataggio recuperaSalvataggio(String id) {
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
				SalvataggioImpl salvataggio = new SalvataggioImpl();
				salvataggio.setId(fileSalvataggio.getPath());
				salvataggio.setNome(line);
				salvataggio.setContenuto(sb.toString());
				return salvataggio;
			} catch (Exception e) {
				BusEventi.pubblica(new EventoException(e));
				return null;
			}
		} else {
			BusEventi.pubblica(new EventoMessaggioInterno("Tentativo di lettura di file non esistente: " + id));
			return null;
		}
	}

	@Override
	public void salva(Salvataggio salvataggio) {
		File directorySalvataggi = recuperaDirectory();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + salvataggio.getId() + POSTFISSO_FILE);
		try (PrintWriter writer = new PrintWriter(new FileWriter(fileSalvataggio))) {
			writer.println(salvataggio.getDescrizione());
			writer.println(salvataggio.getContenuto());
			writer.flush();
		} catch (IOException e) {
			BusEventi.pubblica(new EventoException(e));
		}
	}
}
