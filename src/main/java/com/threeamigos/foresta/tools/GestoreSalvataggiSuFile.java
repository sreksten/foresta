package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoException;
import com.threeamigos.foresta.eventi.EventoMessaggioInterno;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.Serializzabile;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class GestoreSalvataggiSuFile extends GestoreSuFile implements InterfacciaGestoreSalvataggi {

	private static final String POSTFISSO_FILE = ".TXT";

	@Override
	public List<TestataSalvataggio> getSalvataggiDisponibili() {
		File directorySalvataggi = recuperaDirectory();
		List<TestataSalvataggio> salvataggi = new ArrayList<>();
        Comando[] salvataggiDisponibili = { Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3, Comando.NUMERO_4, Comando.NUMERO_5 };
		for (Comando id: salvataggiDisponibili) {
			try {
				File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + id.name() + POSTFISSO_FILE);
				if (fileSalvataggio.exists() && fileSalvataggio.canRead()) {
					TestataSalvataggio testata = leggiTestataSalvataggio(new BufferedReader(new FileReader(fileSalvataggio)));
					salvataggi.add(testata);
				}
			} catch (Exception e) {
				BusEventi.pubblica(new EventoException("Durante lettura file di salvataggio " + id, e));
			}
		}
		return salvataggi;
	}

	@Override
	public boolean leggi(Comando id) {
		File directorySalvataggi = recuperaDirectory();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + id.name() + POSTFISSO_FILE);
		if (fileSalvataggio.exists() && fileSalvataggio.canRead()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(fileSalvataggio))) {
				// La prima riga è l'intestazione
				String line = reader.readLine();
				ModelloDati md = new ModelloDati();
				md.leggi(reader);
				GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
				gruppo.setModelloDati(md.getGruppoGiocatoreMD());
				ModelloDati.setIstanza(md);
				gruppo.setLocazioneCorrente(Foresta.costruisciIstanza(gruppo.getCoordinate()));
				return true;
			} catch (Exception e) {
				BusEventi.pubblica(new EventoException(e));
			}
		} else {
			BusEventi.pubblica(new EventoMessaggioInterno("Tentativo di lettura di file non esistente: " + id));
		}
		return false;
	}

	@Override
	public void salva(Comando id) {
		File directorySalvataggi = recuperaDirectory();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + id.name() + POSTFISSO_FILE);
		if (fileSalvataggio.exists() && !fileSalvataggio.canWrite()) {
			BusEventi.pubblica(new EventoMessaggioInterno("Tentativo di scrittura su file non scrivibile: " + id));
		}
		try (PrintWriter writer = new PrintWriter(new FileWriter(fileSalvataggio))) {
			// Intestazione
			GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
            String sb = id +
                    "|" +
                    gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
                    " - giorno " +
                    LineaTemporale.getGiorno() +
                    ", ora " +
                    LineaTemporale.getOra();
			writer.println(sb);

			ModelloDati.getIstanza().salva(writer);

		} catch (IOException e) {
			BusEventi.pubblica(new EventoException(e));
		}
	}

	private TestataSalvataggio leggiTestataSalvataggio(BufferedReader reader) throws Exception {
		String line = reader.readLine();
		StringTokenizer st = new StringTokenizer(line, Serializzabile.PIPE);
		String id = st.nextToken();
		String descrizione = st.nextToken();
		GruppoGiocatoreMD md = new GruppoGiocatoreMD();
		md.leggi(reader);
		GruppoGiocatore gruppoGiocatore = GruppoGiocatore.of(md);
		return new TestataSalvataggio(Comando.valueOf(id), descrizione, gruppoGiocatore);
	}
}
