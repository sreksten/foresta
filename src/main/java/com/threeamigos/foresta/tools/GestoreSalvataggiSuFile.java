package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaErroreCaricamento;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.LettoreCampi;
import com.threeamigos.foresta.motore.modellodati.Serializzabile;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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
					try (BufferedReader reader = apriInLettura(fileSalvataggio)) {
						salvataggi.add(leggiTestataSalvataggio(reader));
					}
				}
			} catch (Exception e) {
				BusEventi.pubblica(new InternoException("Durante lettura file di salvataggio " + id, e));
			}
		}
		return salvataggi;
	}

	@Override
	public boolean leggi(Comando id) {
		File directorySalvataggi = recuperaDirectory();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + id.name() + POSTFISSO_FILE);
		if (fileSalvataggio.exists() && fileSalvataggio.canRead()) {
			try (BufferedReader reader = apriInLettura(fileSalvataggio)) {
				// La prima riga è l'intestazione e la saltiamo
				reader.readLine();
				ModelloDati md = new ModelloDati();
				md.leggi(reader);
				ModelloDati.setIstanza(md);
				return true;
			} catch (Exception e) {
				BusEventi.pubblica(new NotificaErroreCaricamento(e));
			}
		} else {
			BusEventi.pubblica(new InternoMessaggio("Tentativo di lettura di file non esistente: " + id));
		}
		return false;
	}

	/**
	 * Scrive prima su un file temporaneo e lo sostituisce al salvataggio solo se è stato scritto per intero:
	 * un errore a metà lascia intatto il salvataggio che c'era.
	 */
	@Override
	public boolean salva(Comando id) {
		File directorySalvataggi = recuperaDirectory();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + id.name() + POSTFISSO_FILE);
		if (fileSalvataggio.exists() && !fileSalvataggio.canWrite()) {
			BusEventi.pubblica(new InternoMessaggio("Tentativo di scrittura su file non scrivibile: " + id));
			return false;
		}
		File fileTemporaneo = new File(fileSalvataggio.getPath() + ".tmp");
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileTemporaneo), StandardCharsets.UTF_8)))) {
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

			// PrintWriter non lancia eccezioni sugli errori di scrittura: vanno chiesti
			if (writer.checkError()) {
				throw new IOException("Errore di scrittura del salvataggio " + id);
			}
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			fileTemporaneo.delete();
			return false;
		}
		try {
			try {
				Files.move(fileTemporaneo.toPath(), fileSalvataggio.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(fileTemporaneo.toPath(), fileSalvataggio.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		} catch (IOException e) {
			BusEventi.pubblica(new InternoException(e));
			fileTemporaneo.delete();
			return false;
		}
	}

	/**
	 * I salvataggi sono sempre in UTF-8, qualunque sia il charset predefinito del sistema.
	 */
	private static BufferedReader apriInLettura(File file) throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
	}

	private TestataSalvataggio leggiTestataSalvataggio(BufferedReader reader) throws Exception {
		String line = reader.readLine();
		LettoreCampi st = new LettoreCampi(line);
		String id = st.testo();
		String descrizione = st.testo();
		GruppoGiocatoreMD md = new GruppoGiocatoreMD();
		md.leggi(reader);
		GruppoGiocatore gruppoGiocatore = GruppoGiocatore.of(md);
		return new TestataSalvataggio(Comando.valueOf(id), descrizione, gruppoGiocatore);
	}
}
