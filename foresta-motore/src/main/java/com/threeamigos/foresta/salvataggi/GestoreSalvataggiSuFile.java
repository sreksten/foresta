package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaErroreCaricamento;
import com.threeamigos.foresta.interfacce.GestoreSalvataggi;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.tipi.Comando;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * I salvataggi su file: cinque slot, un file di testo ciascuno nella cartella {@code .foresta}, sempre in UTF-8. La prima riga è l'intestazione (slot, capo
 * e momento della partita), il resto è il {@code ModelloDati}. Si scrive prima su un file temporaneo, che sostituisce il salvataggio solo se è stato scritto per intero.
 */
public class GestoreSalvataggiSuFile extends GestoreSuFile implements GestoreSalvataggi {

	private static final String POSTFISSO_FILE = ".TXT";

	/**
	 * Scrive nella cartella {@code .foresta} della cartella dell'utente.
	 */
	public GestoreSalvataggiSuFile() {
		super();
	}

	/**
	 * Scrive nella cartella indicata (vedi {@link GestoreSuFile}).
	 */
	public GestoreSalvataggiSuFile(File directory) {
		super(directory);
	}

	@Override
	public List<TestataSalvataggio> getSalvataggiDisponibili() {
		File directorySalvataggi = recuperaDirectory();
		List<TestataSalvataggio> salvataggi = new ArrayList<>();
		for (Comando id : SLOT_MANUALI) {
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
	 * Scrive prima su un file temporaneo e lo sostituisce al salvataggio solo se è stato scritto per intero
	 * (vedi {@link #scriviSostituendo}): un errore a metà lascia intatto il salvataggio che c'era.
	 */
	@Override
	public boolean salva(Comando id) {
		File directorySalvataggi = recuperaDirectory();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + id.name() + POSTFISSO_FILE);
		if (fileSalvataggio.exists() && !fileSalvataggio.canWrite()) {
			BusEventi.pubblica(new InternoMessaggio("Tentativo di scrittura su file non scrivibile: " + id));
			return false;
		}
		try {
			scriviSostituendo(fileSalvataggio, writer -> {
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
			});
			return true;
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
	}

	@Override
	public void elimina(Comando id) {
		File fileSalvataggio = new File(recuperaDirectory().getPath() + File.separatorChar + id.name() + POSTFISSO_FILE);
		if (fileSalvataggio.exists() && !fileSalvataggio.delete()) {
			BusEventi.pubblica(new InternoMessaggio("Impossibile cancellare il salvataggio " + id));
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
