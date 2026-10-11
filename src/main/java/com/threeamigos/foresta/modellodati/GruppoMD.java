package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GruppoMD implements Serializzabile {

	protected List<PersonaggioMD> personaggiMD = new ArrayList<>();
	// Le scorte del gruppo: le pergamene degli incantesimi e le pozioni
	protected final Map<ClasseIncantesimo, Integer> incantesimi = new EnumMap<>(ClasseIncantesimo.class);
	protected int pozioniSalute;
	protected int pozioniSaluteGrande;
	protected int pozioniMagia;
	protected int pozioniMagiaGrande;

	public void setIncantesimi(ClasseIncantesimo classeIncantesimo, int quantita) {
		incantesimi.put(classeIncantesimo, quantita);
	}

	public int getIncantesimi(ClasseIncantesimo classeIncantesimo) {
		return incantesimi.getOrDefault(classeIncantesimo, 0);
	}

	public int getPozioniSalute() {
		return pozioniSalute;
	}

	public void setPozioniSalute(int pozioniSalute) {
		this.pozioniSalute = pozioniSalute;
	}

	public int getPozioniSaluteGrande() {
		return pozioniSaluteGrande;
	}

	public void setPozioniSaluteGrande(int pozioniSaluteGrande) {
		this.pozioniSaluteGrande = pozioniSaluteGrande;
	}

	public int getPozioniMagia() {
		return pozioniMagia;
	}

	public void setPozioniMagia(int pozioniMagia) {
		this.pozioniMagia = pozioniMagia;
	}

	public int getPozioniMagiaGrande() {
		return pozioniMagiaGrande;
	}

	public void setPozioniMagiaGrande(int pozioniMagiaGrande) {
		this.pozioniMagiaGrande = pozioniMagiaGrande;
	}

	public void addPersonaggioMD(PersonaggioMD personaggioMD) {
		personaggiMD.add(personaggioMD);
	}

	public List<PersonaggioMD> getPersonaggiMD() {
		return personaggiMD;
	}

	public void reimposta() {
		personaggiMD.clear();
		incantesimi.clear();
		pozioniSalute = 0;
		pozioniSaluteGrande = 0;
		pozioniMagia = 0;
		pozioniMagiaGrande = 0;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(personaggiMD.size());
		for (PersonaggioMD personaggio : personaggiMD) {
			personaggio.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		int numeroPersonaggi = Integer.parseInt(line);
		for (int i = 0; i < numeroPersonaggi; i++) {
			PersonaggioMD personaggioMD = new PersonaggioMD();
			personaggioMD.leggi(stream);
			personaggiMD.add(personaggioMD);
		}
	}
}
