package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringTokenizer;

public class StatisticheMD implements Serializzabile {

	private int livello = 1;
	private int puntiEsperienza = 0;
	private int punti = 0;
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

	public final int getPunti() {
		return punti;
	}

	////////////////////

	public final void reimposta() {
		mostriUccisi.clear();
		punti = 0;
		livello = 1;
		puntiEsperienza = 0;
	}

	public final void addPunti(int quantita) {
		punti += quantita;
	}

	public final void addPuntiEsperienza(int quantita) {
		puntiEsperienza += quantita;
	}

	public final void setLivello(int livello) {
		this.livello = livello;
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
		stream.println(punti);
		for (ClassePersonaggio classePersonaggio : ClassePersonaggio.values()) {
			stream.print(classePersonaggio.ordinal());
			stream.print(PIPE);
			stream.print(getMostriUccisi(classePersonaggio));
			stream.print(PIPE);
		}
		stream.println("");
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		livello = Integer.parseInt(st.nextToken());
		puntiEsperienza = Integer.parseInt(st.nextToken());
		punti = Integer.parseInt(st.nextToken());
		mostriUccisi.clear();
		ClassePersonaggio[] classi = ClassePersonaggio.values();
		line = stream.readLine();
		st = new StringTokenizer(line, PIPE);
		for (int i = 0; i < classi.length; i++) {
			mostriUccisi.put(classi[Integer.parseInt(st.nextToken())], Integer.parseInt(st.nextToken()));
		}
	}
}
