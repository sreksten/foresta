package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ModelloDati implements Serializzabile {

	private static ModelloDati istanza = new ModelloDati();

	private ForestaMD forestaMD;
	private GruppoGiocatoreMD gruppoGiocatoreMD;
	private StatisticheMD statisticheMD;
	private LineaTemporaleMD lineaTemporaleMD;
	private RegistroPersonaggiMD registroPersonaggiMD;
	private RegistroArtefattiMD registroArtefattiMD;
	private RegistroMissioniMD registroMissioniMD;

	public ModelloDati() {
		forestaMD = new ForestaMD();
		gruppoGiocatoreMD = new GruppoGiocatoreMD();
		statisticheMD = new StatisticheMD();
		lineaTemporaleMD = new LineaTemporaleMD();
		registroPersonaggiMD = new RegistroPersonaggiMD();
		registroArtefattiMD = new RegistroArtefattiMD();
		registroMissioniMD = new RegistroMissioniMD();
	}

	public static final ModelloDati getIstanza() {
		return istanza;
	}

	public final ForestaMD getForestaMD() {
		return forestaMD;
	}

	public final GruppoGiocatoreMD getGruppoGiocatoreMD() {
		return gruppoGiocatoreMD;
	}

	public final StatisticheMD getStatisticheMD() {
		return statisticheMD;
	}

	public LineaTemporaleMD getLineaTemporaleMD() {
		return lineaTemporaleMD;
	}

	public RegistroPersonaggiMD getRegistroPersonaggiMD() {
		return registroPersonaggiMD;
	}

	public RegistroArtefattiMD getRegistroArtefattiMD() {
		return registroArtefattiMD;
	}

	public RegistroMissioniMD getRegistroMissioniMD() {
		return registroMissioniMD;
	}

	///////////////////////////////////

	public void reimposta(int dimensioneX, int dimensioneY) {
		forestaMD.reimposta(dimensioneX, dimensioneY);
		gruppoGiocatoreMD.reimposta();
		statisticheMD.reimposta();
		lineaTemporaleMD.reimposta();
		registroPersonaggiMD.reimposta();
		registroArtefattiMD.reimposta();
		registroMissioniMD.reimposta();
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		forestaMD.salva(stream);
		gruppoGiocatoreMD.salva(stream);
		statisticheMD.salva(stream);
		lineaTemporaleMD.salva(stream);
		registroPersonaggiMD.salva(stream);
		registroArtefattiMD.salva(stream);
		registroMissioniMD.salva(stream);
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		forestaMD.leggi(stream);
		gruppoGiocatoreMD.leggi(stream);
		statisticheMD.leggi(stream);
		lineaTemporaleMD.leggi(stream);
		registroPersonaggiMD.leggi(stream);
		registroArtefattiMD.leggi(stream);
		registroMissioniMD.leggi(stream);
	}
}
