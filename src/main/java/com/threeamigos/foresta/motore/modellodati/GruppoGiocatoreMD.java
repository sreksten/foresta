package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class GruppoGiocatoreMD extends GruppoMD implements Serializzabile {

	private GruppoMD gruppo = new GruppoMD();
	private int monete;
	private int preziosi;
	private int[] incantesimi = new int[ClassiIncantesimo.values().length];
	private int pozioniSalute;
	private int pozioniSaluteGrande;
	private int pozioniMagia;
	// Coordinate all'interno della Foresta
	private CoordinateMD coordinate;

	public GruppoMD getGruppoMD() {
		return gruppo;
	}

	public void setGruppoMD(GruppoMD gruppo) {
		this.gruppo = gruppo;
	}

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

	public CoordinateMD getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(CoordinateMD coordinate) {
		this.coordinate = coordinate;
	}

	////////////////////////////////

	public void reimposta() {
		gruppo.getPersonaggiMD().clear();
		incantesimi = new int[ClassiIncantesimo.values().length];
	}

	public void setIncantesimi(ClassiIncantesimo classeIncantesimo, int quantita) {
		incantesimi[classeIncantesimo.ordinal()] = quantita;
	}

	public int getIncantesimi(ClassiIncantesimo classeIncantesimo) {
		return incantesimi[classeIncantesimo.ordinal()];
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		super.salva(stream);
		stream.print(monete);
		stream.print(PIPE);
		stream.print(preziosi);
		stream.print(PIPE);
		for (int i = 0; i < incantesimi.length; i++) {
			stream.print(incantesimi[i]);
			stream.print(PIPE);
		}
		stream.print(pozioniSalute);
		stream.print(PIPE);
		stream.print(pozioniSaluteGrande);
		stream.print(PIPE);
		stream.print(pozioniMagia);
		stream.print(PIPE);
		stream.print(coordinate.getX());
		stream.print(PIPE);
		stream.println(coordinate.getY());
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
		coordinate = new CoordinateMD(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
	}
}
