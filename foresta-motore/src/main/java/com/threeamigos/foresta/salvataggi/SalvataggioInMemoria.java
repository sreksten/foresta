package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.interfacce.GestoreSalvataggi;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;

import java.io.*;

/**
 * Il salvataggio automatico in memoria: l'ultimo stato in cui la partita è arrivata a una fine di locazione, tenuto solo in
 * memoria (non si scrive mai su disco, e sparisce con l'app). Serve da rete di sicurezza: se il giocatore muore per errore, può
 * riprendere dalla locazione precedente. Ne esiste uno solo, che a ogni fine di locazione sostituisce il precedente.
 * <p>
 * Ha il formato dei salvataggi su file (intestazione più {@link ModelloDati}) e compare nell'elenco da cui si carica come lo
 * slot {@link GestoreSalvataggi#SLOT_IN_MEMORIA}, con la scritta "salvataggio automatico".
 */
public final class SalvataggioInMemoria {

	/** La scritta con cui compare nell'elenco dei salvataggi */
	static final String DESCRIZIONE = "salvataggio automatico";

	// L'intero salvataggio (intestazione compresa); null se non ce n'è uno
	private String testo;

	/**
	 * Se c'è un salvataggio da cui riprendere.
	 */
	public boolean isPresente() {
		return testo != null;
	}

	/**
	 * Registra lo stato della partita com'è adesso, al posto di quello di prima. Se la scrittura non riesce, quello di prima resta.
	 */
	public void salva() {
		StringWriter scritto = new StringWriter();
		try (PrintWriter writer = new PrintWriter(scritto)) {
			writer.println(GestoreSalvataggi.SLOT_IN_MEMORIA + "|" + DESCRIZIONE + " - giorno " + LineaTemporale.getGiorno() + ", ora "
					+ LineaTemporale.getOra());
			ModelloDati.getIstanza().salva(writer);
			if (writer.checkError()) {
				throw new IOException("Errore di scrittura del salvataggio automatico in memoria");
			}
		} catch (IOException | RuntimeException e) {
			BusEventi.pubblica(new InternoException(e));
			return;
		}
		testo = scritto.toString();
	}

	/**
	 * La testata da mostrare nell'elenco dei salvataggi (il gruppo di quel momento), o null se non c'è un salvataggio.
	 */
	public TestataSalvataggio getTestata() {
		if (testo == null) {
			return null;
		}
		try (BufferedReader reader = new BufferedReader(new StringReader(testo))) {
			LettoreCampi intestazione = new LettoreCampi(reader.readLine());
			intestazione.testo();
			String descrizione = intestazione.testo();
			GruppoGiocatoreMD gruppo = new GruppoGiocatoreMD();
			gruppo.leggi(reader);
			return new TestataSalvataggio(GestoreSalvataggi.SLOT_IN_MEMORIA, descrizione, GruppoGiocatore.of(gruppo));
		} catch (IOException e) {
			BusEventi.pubblica(new InternoException(e));
			return null;
		}
	}

	/**
	 * Installa il modello dati salvato (lo stato derivato lo ricostruisce chi chiama, con RiletturaPartita.ricostruisci); non lo
	 * toglie: si può riprendere da lì più volte.
	 *
	 * @return true se c'era un salvataggio e si è letto
	 */
	public boolean leggi() {
		if (testo == null) {
			return false;
		}
		try (BufferedReader reader = new BufferedReader(new StringReader(testo))) {
			// La prima riga è l'intestazione
			reader.readLine();
			ModelloDati modelloDati = new ModelloDati();
			modelloDati.leggi(reader);
			ModelloDati.setIstanza(modelloDati);
			return true;
		} catch (IOException e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
	}

	/**
	 * Dimentica il salvataggio.
	 */
	public void elimina() {
		testo = null;
	}
}
