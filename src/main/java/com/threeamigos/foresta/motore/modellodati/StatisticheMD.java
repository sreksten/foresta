package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;

public class StatisticheMD implements Serializzabile {

	private int livello = 1;
	private int puntiEsperienza = 0;
	private int punti = 0;
	private int turniGiocati = 0;
	private final Map<ClassePersonaggio, Integer> mostriUccisi = new EnumMap<>(ClassePersonaggio.class);

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

	////////////////////

	public final void reimposta() {
		mostriUccisi.clear();
		livello = 1;
		puntiEsperienza = 0;
		punti = 0;
		turniGiocati = 0;
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

	public final void addMostroUcciso(ClassePersonaggio classe) {
        mostriUccisi.merge(classe, 1, Integer::sum);
	}

	public final int getMostriUccisi(ClassePersonaggio classe) {
		Integer uccisi = mostriUccisi.get(classe);
		if (uccisi == null) {
			return 0;
		}
		return uccisi;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(livello);
		stream.print(PIPE);
		stream.print(puntiEsperienza);
		stream.print(PIPE);
		stream.print(punti);
		stream.print(PIPE);
		stream.println(turniGiocati);
		for (ClassePersonaggio classePersonaggio : ClassePersonaggio.values()) {
			stream.print(classePersonaggio.name());
			stream.print(PIPE);
			stream.print(getMostriUccisi(classePersonaggio));
			stream.print(PIPE);
		}
		stream.println("");
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		LettoreCampi st = new LettoreCampi(line);
		livello = Integer.parseInt(st.testo());
		puntiEsperienza = Integer.parseInt(st.testo());
		punti = Integer.parseInt(st.testo());
		turniGiocati = Integer.parseInt(st.testo());
		mostriUccisi.clear();
		ClassePersonaggio[] classi = ClassePersonaggio.values();
		line = stream.readLine();
		st = new LettoreCampi(line);
		for (int i = 0; i < classi.length; i++) {
			mostriUccisi.put(ClassePersonaggio.valueOf(st.testo()), Integer.parseInt(st.testo()));
		}
	}
}
