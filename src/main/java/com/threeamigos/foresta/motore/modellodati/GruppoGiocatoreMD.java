package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

public class GruppoGiocatoreMD extends GruppoMD implements Serializzabile {

	private int monete;
	private int preziosi;
	private int[] incantesimi = new int[ClasseIncantesimo.values().length];
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

	public int[] getIncantesimi() {
		return incantesimi;
	}

	public void setIncantesimi(int[] incantesimi) {
		this.incantesimi = incantesimi;
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
		incantesimi = new int[ClasseIncantesimo.values().length];
		monete = 0;
		preziosi = 0;
		pozioniSalute = 0;
		pozioniSaluteGrande = 0;
		pozioniMagia = 0;
		pozioniMagiaGrande = 0;
		artefatti.clear();
	}

	public void setIncantesimi(ClasseIncantesimo classeIncantesimo, int quantita) {
		incantesimi[classeIncantesimo.ordinal()] = quantita;
	}

	public int getIncantesimi(ClasseIncantesimo classeIncantesimo) {
		return incantesimi[classeIncantesimo.ordinal()];
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		super.salva(stream);
		stream.print(monete);
		stream.print(PIPE);
		stream.print(preziosi);
		stream.print(PIPE);
        for (int i : incantesimi) {
            stream.print(i);
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
		StringTokenizer st = new StringTokenizer(line, PIPE);
		monete = Integer.parseInt(st.nextToken());
		preziosi = Integer.parseInt(st.nextToken());
		for (int i = 0; i < incantesimi.length; i++) {
			incantesimi[i] = Integer.parseInt(st.nextToken());
		}
		pozioniSalute = Integer.parseInt(st.nextToken());
		pozioniSaluteGrande = Integer.parseInt(st.nextToken());
		pozioniMagia = Integer.parseInt(st.nextToken());
		pozioniMagiaGrande = Integer.parseInt(st.nextToken());
		coordinate = new CoordinateMD(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		int numeroArtefatti = Integer.parseInt(st.nextToken());

		artefatti.clear();
		for (int i = 0; i < numeroArtefatti; i++) {
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefatti.add(artefatto);
		}
	}
}
