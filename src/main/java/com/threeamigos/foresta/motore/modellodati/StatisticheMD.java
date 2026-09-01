package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

public class StatisticheMD implements Serializzabile {

	private int punti;
	private Map<ClassePersonaggio, Integer> mostriUccisi = new EnumMap<>(ClassePersonaggio.class);

	public final int getPunti() {
		return punti;
	}

	public final void setPunti(int punti) {
		this.punti = punti;
	}

	public final Map<ClassePersonaggio, Integer> getMostriUccisi() {
		return mostriUccisi;
	}

	public final void setMostriUccisi(Map<ClassePersonaggio, Integer> mostriUccisi) {
		this.mostriUccisi = mostriUccisi;
	}

	////////////////////

	public final void reimposta() {
		mostriUccisi.clear();
		punti = 0;
	}

	public final void addPunti(int quantita) {
		punti += quantita;
	}

	public final void addMostroUcciso(ClassePersonaggio classe) {
		Integer uccisi = mostriUccisi.get(classe);
		if (uccisi == null) {
			mostriUccisi.put(classe,  1);
		} else {
			mostriUccisi.put(classe, uccisi + 1);
		}
	}

	public final int getMostriUccisi(ClassePersonaggio classe) {
		Integer uccisi = mostriUccisi.get(classe);
		if (uccisi == null) {
			return 0;
		}
		return uccisi.intValue();
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
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
		punti = Integer.parseInt(line);
		mostriUccisi.clear();
		ClassePersonaggio[] classi = ClassePersonaggio.values();
		line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		for (int i = 0; i < classi.length; i++) {
			mostriUccisi.put(classi[Integer.parseInt(st.nextToken())], Integer.parseInt(st.nextToken()));
		}
	}
}
