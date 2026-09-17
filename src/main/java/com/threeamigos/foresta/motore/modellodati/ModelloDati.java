package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoErroreCaricamento;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Il modello dati del gioco. Comprende:
 * <ul>
 * <li>GruppoGiocatoreMD - il gruppo del giocatore con tutti i personaggi contenuti</li>
 * <li>StatisticheMD - statistiche sul gioco</li>
 * <li>LineaTemporaleMD - quel che è successo durante il gioco</li>
 * <li>ForestaMD - informazioni sulla Foresta</li>
 * <li>RegistroPersonaggiMD - i personaggi sparsi per la Foresta</li>
 * <li>RegistroArtefattiMD - gli artefatti sparsi per la Foresta</li>
 * <li>RegistroMissioniMD - le missioni del gioco</li>
 * </ul>
 */
public class ModelloDati implements Serializzabile {

	// L'istanza di modello dati su cui il gioco si basa
	private static ModelloDati istanza = new ModelloDati();

	// Dati sufficienti per mostrare una situazione di salvataggio
	private final GruppoGiocatoreMD gruppoGiocatoreMD;
	// Tutto il resto dei dati di gioco
	private final StatisticheMD statisticheMD;
	private final LineaTemporaleMD lineaTemporaleMD;
	private final ForestaMD forestaMD;
	private final RegistroPersonaggiMD registroPersonaggiMD;
	private final RegistroArtefattiMD registroArtefattiMD;
	private final RegistroMissioniMD registroMissioniMD;

	public ModelloDati() {
		gruppoGiocatoreMD = new GruppoGiocatoreMD();
		statisticheMD = new StatisticheMD();
		lineaTemporaleMD = new LineaTemporaleMD();
		forestaMD = new ForestaMD();
		registroPersonaggiMD = new RegistroPersonaggiMD();
		registroArtefattiMD = new RegistroArtefattiMD();
		registroMissioniMD = new RegistroMissioniMD();
	}

	public static ModelloDati getIstanza() {
		return istanza;
	}

	public final GruppoGiocatoreMD getGruppoGiocatoreMD() {
		return gruppoGiocatoreMD;
	}

	public final StatisticheMD getStatisticheMD() {
		return statisticheMD;
	}

	public final LineaTemporaleMD getLineaTemporaleMD() {
		return lineaTemporaleMD;
	}

	public final ForestaMD getForestaMD() {
		return forestaMD;
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
		gruppoGiocatoreMD.reimposta();
		statisticheMD.reimposta();
		lineaTemporaleMD.reimposta();
		forestaMD.reimposta(dimensioneX, dimensioneY);
		registroPersonaggiMD.reimposta();
		registroArtefattiMD.reimposta();
		registroMissioniMD.reimposta();
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		gruppoGiocatoreMD.salva(stream);
		statisticheMD.salva(stream);
		lineaTemporaleMD.salva(stream);
		forestaMD.salva(stream);
		registroPersonaggiMD.salva(stream);
		registroArtefattiMD.salva(stream);
		registroMissioniMD.salva(stream);
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		gruppoGiocatoreMD.reimposta();
		statisticheMD.reimposta();
		lineaTemporaleMD.reimposta();
		registroPersonaggiMD.reimposta();
		registroArtefattiMD.reimposta();
		registroMissioniMD.reimposta();

		gruppoGiocatoreMD.leggi(stream);
		statisticheMD.leggi(stream);
		lineaTemporaleMD.leggi(stream);
		forestaMD.leggi(stream);
		registroPersonaggiMD.leggi(stream);
		registroArtefattiMD.leggi(stream);
		registroMissioniMD.leggi(stream);
	}

	public boolean leggiTestata(BufferedReader stream) throws IOException {
		try {
			gruppoGiocatoreMD.leggi(stream);
			return true;
		} catch (Exception e) {
			BusEventi.pubblica(new EventoErroreCaricamento(e));
			return false;
		}
	}

	public static void sostituisciIstanza(ModelloDati modelloDati) {
		istanza = modelloDati;
	}
}
