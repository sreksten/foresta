package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

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

	/**
	 * Restituisce i punti esperienza totali accumulati
	 */
	public int getPuntiEsperienza() {
		return puntiEsperienza;
	}

	/**
	 * Restituisce il punteggio ottenuto
	 */
	public final int getPunti() {
		return punti;
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

	public final void setLivello(int livello) {
		this.livello = livello;
	}

	public final void setPunti(int punti) {
		this.punti = punti;
	}

	public final void setPuntiEsperienza(int puntiEsperienza) {
		this.puntiEsperienza = puntiEsperienza;
	}

	/**
	 * Conta un mostro ucciso in più per quella classe.
	 */
	public final void addMostroUcciso(TipoPersonaggio classe) {
        mostriUccisi.merge(classe, 1, Integer::sum);
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

	/**
	 * Scrive una riga con livello, punti esperienza, punteggio, turni e identificativo della partita, poi una riga
	 * con i mostri uccisi di ogni classe, nella forma classe e numero.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(livello);
		stream.print(PIPE);
		stream.print(puntiEsperienza);
		stream.print(PIPE);
		stream.print(punti);
		stream.print(PIPE);
		stream.print(turniGiocati);
		stream.print(PIPE);
		stream.println(idPartita);
		for (TipoPersonaggio classePersonaggio : TipoPersonaggio.values()) {
			stream.print(classePersonaggio.name());
			stream.print(PIPE);
			stream.print(getMostriUccisi(classePersonaggio));
			stream.print(PIPE);
		}
		stream.println("");
	}

	/**
	 * Rilegge le due righe scritte da {@link #salva(PrintWriter)}.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		LettoreCampi st = new LettoreCampi(line);
		livello = Integer.parseInt(st.testo());
		puntiEsperienza = Integer.parseInt(st.testo());
		punti = Integer.parseInt(st.testo());
		turniGiocati = Integer.parseInt(st.testo());
		idPartita = st.testo();
		mostriUccisi.clear();
		TipoPersonaggio[] classi = TipoPersonaggio.values();
		line = stream.readLine();
		st = new LettoreCampi(line);
		for (int i = 0; i < classi.length; i++) {
			mostriUccisi.put(TipoPersonaggio.valueOf(st.testo()), Integer.parseInt(st.testo()));
		}
	}
}
