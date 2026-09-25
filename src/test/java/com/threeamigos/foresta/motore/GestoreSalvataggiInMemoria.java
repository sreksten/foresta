package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.motore.modellodati.LettoreCampi;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.InterfacciaGestoreSalvataggi;
import com.threeamigos.foresta.tools.TestataSalvataggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * I salvataggi dei test: nello stesso formato di GestoreSalvataggiSuFile (intestazione più ModelloDati), ma in
 * memoria, così un test può salvare e rileggere senza toccare il disco.
 */
final class GestoreSalvataggiInMemoria implements InterfacciaGestoreSalvataggi {

	private final Map<Comando, String> salvataggi = new EnumMap<>(Comando.class);

	@Override
	public List<TestataSalvataggio> getSalvataggiDisponibili() {
		List<TestataSalvataggio> testate = new ArrayList<>();
		for (Map.Entry<Comando, String> salvataggio : salvataggi.entrySet()) {
			try (BufferedReader reader = new BufferedReader(new StringReader(salvataggio.getValue()))) {
				LettoreCampi intestazione = new LettoreCampi(reader.readLine());
				intestazione.testo();
				String descrizione = intestazione.testo();
				GruppoGiocatoreMD md = new GruppoGiocatoreMD();
				md.leggi(reader);
				testate.add(new TestataSalvataggio(salvataggio.getKey(), descrizione, GruppoGiocatore.of(md)));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return testate;
	}

	@Override
	public boolean leggi(Comando id) {
		String salvataggio = salvataggi.get(id);
		if (salvataggio == null) {
			return false;
		}
		try (BufferedReader reader = new BufferedReader(new StringReader(salvataggio))) {
			reader.readLine();
			ModelloDati md = new ModelloDati();
			md.leggi(reader);
			ModelloDati.setIstanza(md);
			return true;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void salva(Comando id) {
		StringWriter testo = new StringWriter();
		try (PrintWriter writer = new PrintWriter(testo)) {
			String capo = GruppoGiocatore.getIstanza().getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
			writer.println(id + "|" + capo + " - giorno " + LineaTemporale.getGiorno() + ", ora " + LineaTemporale.getOra());
			ModelloDati.getIstanza().salva(writer);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		salvataggi.put(id, testo.toString());
	}

	boolean contiene(Comando id) {
		return salvataggi.containsKey(id);
	}
}
