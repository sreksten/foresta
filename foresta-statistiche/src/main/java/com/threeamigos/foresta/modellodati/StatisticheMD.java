package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * Le statistiche della partita: livello del gioco, punti esperienza, punteggio, turni giocati, mostri uccisi per
 * classe e l'identificativo della partita (che resta lo stesso attraverso salvataggi e caricamenti, così la classifica
 * non registra due volte la stessa partita).
 *
 * @author Stefano Reksten
 */
public class StatisticheMD implements Serializzabile {

	private int livello = 1;
	private int puntiEsperienza = 0;
	private int punti = 0;
	private int turniGiocati = 0;
	// Identifica la partita, anche attraverso salvataggi e caricamenti: la classifica lo usa per non registrare
	// più volte la stessa partita (vedi GestorePunteggiBase)
	private String idPartita = UUID.randomUUID().toString();
	private final Map<TipoPersonaggio, Integer> mostriUccisi = new EnumMap<>(TipoPersonaggio.class);

	/**
	 * Restituisce il livello corrente del gioco
	 */
	public int getLivello() {
		return livello;
	}

	public final void setLivello(int livello) {
		this.livello = livello;
	}

	/**
	 * Restituisce i punti esperienza totali accumulati
	 */
	public int getPuntiEsperienza() {
		return puntiEsperienza;
	}

	public final void setPuntiEsperienza(int puntiEsperienza) {
		this.puntiEsperienza = puntiEsperienza;
	}

	/**
	 * Restituisce il punteggio ottenuto
	 */
	public final int getPunti() {
		return punti;
	}

	public final void setPunti(int punti) {
		this.punti = punti;
	}

	/**
	 * Restituisce il numero di turni giocati (NON il tempo passato, quello è in LineaTemporale)
	 */
	public int getTurniGiocati() {
		return turniGiocati;
	}

	public void setTurniGiocati(int turniGiocati) {
		this.turniGiocati = turniGiocati;
	}

	/**
	 * L'identificativo unico della partita, che non cambia fra un salvataggio e il successivo.
	 */
	public String getIdPartita() {
		return idPartita;
	}

	/**
	 * Quanti mostri di quella classe sono stati uccisi; zero se nessuno.
	 */
	public final int getMostriUccisi(TipoPersonaggio classe) {
		Integer uccisi = mostriUccisi.get(classe);
		if (uccisi == null) {
			return 0;
		}
		return uccisi;
	}

	////////////////////

	/**
	 * Azzera tutto per una partita nuova, compreso un identificativo di partita nuovo.
	 */
	public final void reimposta() {
		mostriUccisi.clear();
		livello = 1;
		puntiEsperienza = 0;
		punti = 0;
		turniGiocati = 0;
		idPartita = UUID.randomUUID().toString();
	}

	/**
	 * Conta un mostro ucciso in più per quella classe.
	 */
	public final void addMostroUcciso(TipoPersonaggio classe) {
        mostriUccisi.merge(classe, 1, Integer::sum);
	}

	/**
	 * Scrive una riga con livello, punti esperienza, punteggio, turni e identificativo della partita, poi una riga
	 * con i mostri uccisi, nella forma nome della classe e numero, solo per le classi con almeno un ucciso.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stampaRiga(stream,
				livello,
				puntiEsperienza,
				punti,
				turniGiocati,
				idPartita);
		// Solo le classi con almeno un ucciso, col nome dell'enum: una classe nuova non cambia il formato dei salvataggi
		List<Object> mostri = new ArrayList<>();
		for (Map.Entry<TipoPersonaggio, Integer> uccisi : mostriUccisi.entrySet()) {
			if (uccisi.getValue() > 0) {
				mostri.add(uccisi.getKey());
				mostri.add(uccisi.getValue());
			}
		}
		stampaRiga(stream, mostri);
	}

	/**
	 * Rilegge le due righe scritte da {@link #salva(PrintWriter)}.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi st = new LettoreCampi(stream.readLine());
		livello = st.intero();
		puntiEsperienza = st.intero();
		punti = st.intero();
		turniGiocati = st.intero();
		idPartita = st.testo();
		mostriUccisi.clear();
		st = new LettoreCampi(stream.readLine());
		while (st.haAltriCampi()) {
			String nome = st.testo();
			int uccisi = st.intero();
			// Una classe che non esiste più (salvataggio di un'altra versione) si salta: le statistiche non valgono un salvataggio perso
			try {
				mostriUccisi.put(TipoPersonaggio.valueOf(nome), uccisi);
			} catch (IllegalArgumentException e) {
				// saltata
			}
		}
	}

}
