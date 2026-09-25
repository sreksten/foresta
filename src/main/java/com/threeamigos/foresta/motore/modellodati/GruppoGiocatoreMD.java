package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class GruppoGiocatoreMD extends GruppoMD implements Serializzabile {

	private int monete;
	private int preziosi;
	private final Map<ClasseIncantesimo, Integer> incantesimi = new EnumMap<>(ClasseIncantesimo.class);
	private int pozioniSalute;
	private int pozioniSaluteGrande;
	private int pozioniMagia;
	private int pozioniMagiaGrande;
	// Coordinate all'interno della Foresta
	private CoordinateMD coordinate;
	// Artefatti disponibili al gruppo ma non in uso da un personaggio specifico
	private final Collection<ArtefattoMD> artefatti = new ArrayList<>();

	public int getMonete() {
		return monete;
	}

	public void setMonete(int monete) {
		this.monete = monete;
	}

	public int getPreziosi() {
		return preziosi;
	}

	public void setPreziosi(int preziosi) {
		this.preziosi = preziosi;
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

	public CoordinateMD getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(CoordinateMD coordinate) {
		this.coordinate = coordinate;
	}

	public Collection<ArtefattoMD> getArtefatti() {
		return artefatti;
	}

	////////////////////////////////

	public void reimposta() {
		super.reimposta();
		incantesimi.clear();
		monete = 0;
		preziosi = 0;
		pozioniSalute = 0;
		pozioniSaluteGrande = 0;
		pozioniMagia = 0;
		pozioniMagiaGrande = 0;
		artefatti.clear();
	}

	public void setIncantesimi(ClasseIncantesimo classeIncantesimo, int quantita) {
		incantesimi.put(classeIncantesimo, quantita);
	}

	public int getIncantesimi(ClasseIncantesimo classeIncantesimo) {
		return incantesimi.getOrDefault(classeIncantesimo, 0);
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		super.salva(stream);
		stream.print(monete);
		stream.print(PIPE);
		stream.print(preziosi);
		stream.print(PIPE);
		stream.print(incantesimi.size());
		stream.print(PIPE);
		for (Map.Entry<ClasseIncantesimo, Integer> incantesimo : incantesimi.entrySet()) {
			stream.print(incantesimo.getKey().name());
			stream.print(PIPE);
			stream.print(incantesimo.getValue());
			stream.print(PIPE);
		}
		stream.print(pozioniSalute);
		stream.print(PIPE);
		stream.print(pozioniSaluteGrande);
		stream.print(PIPE);
		stream.print(pozioniMagia);
		stream.print(PIPE);
		stream.print(pozioniMagiaGrande);
		stream.print(PIPE);
		stream.print(coordinate.getX());
		stream.print(PIPE);
		stream.print(coordinate.getY());
		stream.print(PIPE);
		stream.println(artefatti.size());

		for (ArtefattoMD artefatto : artefatti) {
			artefatto.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		super.leggi(stream);
		String line = stream.readLine();
		LettoreCampi st = new LettoreCampi(line);
		monete = Integer.parseInt(st.testo());
		preziosi = Integer.parseInt(st.testo());
		incantesimi.clear();
		int numeroIncantesimi = st.intero();
		for (int i = 0; i < numeroIncantesimi; i++) {
			incantesimi.put(st.enumerato(ClasseIncantesimo.class), st.intero());
		}
		pozioniSalute = Integer.parseInt(st.testo());
		pozioniSaluteGrande = Integer.parseInt(st.testo());
		pozioniMagia = Integer.parseInt(st.testo());
		pozioniMagiaGrande = Integer.parseInt(st.testo());
		coordinate = new CoordinateMD(Integer.parseInt(st.testo()), Integer.parseInt(st.testo()));
		int numeroArtefatti = Integer.parseInt(st.testo());

		artefatti.clear();
		for (int i = 0; i < numeroArtefatti; i++) {
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefatti.add(artefatto);
		}
	}
}
