package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tattiche.TatticheDeiPersonaggi;
import com.threeamigos.foresta.canti.CantiImparati;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.notizie.Notizie;
import com.threeamigos.foresta.economia.MagazziniDeiNegozi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.mondo.RegistroArtefatti;
import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.statistiche.Statistiche;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Il modello dati del gioco. Comprende:
 * <ul>
 * <li>GruppoGiocatoreMD - il gruppo del giocatore con tutti i personaggi contenuti</li>
 * <li>CassaMD - le monete e i preziosi del gruppo</li>
 * <li>TatticheMD - le tattiche dei personaggi</li>
 * <li>CantiImparatiMD - i canti che il gruppo ha imparato strada facendo</li>
 * <li>StatisticheMD - statistiche sul gioco</li>
 * <li>LineaTemporaleMD - quel che è successo durante il gioco</li>
 * <li>ForestaMD - informazioni sulla Foresta</li>
 * <li>RegistroPersonaggiMD - i personaggi sparsi per la Foresta</li>
 * <li>RegistroArtefattiMD - gli artefatti sparsi per la Foresta</li>
 * <li>MagazziniMD - i magazzini dei negozi</li>
 * <li>RegistroMissioniMD - le missioni del gioco</li>
 * <li>NotizieMD - ultimi messaggi mostrati al giocatore e ultime notizie per la mappa</li>
 * <li>IntermezziMD - gli intermezzi già mostrati</li>
 * </ul>
 * e se l'aiuto della barra delle icone è acceso (vedi DisplayableCanvasBarraIcone).
 *
 * @author Stefano Reksten
 */
public class ModelloDati implements Serializzabile {

	// L'istanza di modello dati su cui il gioco si basa (volatile: la imposta chi carica o azzera, e la leggono tutti, anche da altri thread)
	private static volatile ModelloDati istanza = new ModelloDati();

	/**
	 * Dice a ogni parte del gioco dove sono i suoi dati: da qui in poi li prende sempre dal modello dati in corso. Le parti non
	 * conoscono il modello dati, quindi finché nessuno lo chiama chi le usa riceve un IllegalStateException. Va chiamato una volta,
	 * all'avvio (da {@code Main}, dai test di una partita da {@code PartitaDiTest}); richiamarlo non fa danni.
	 */
	public static void installa() {
		Foresta.setFornitoreDiDati(() -> getIstanza().getForestaMD());
		GruppoGiocatore.setFornitoreDiDati(() -> getIstanza().getGruppoGiocatoreMD());
		TatticheDeiPersonaggi.setFornitoreDiDati(() -> getIstanza().getTatticheMD());
		CantiImparati.setFornitoreDiDati(() -> getIstanza().getCantiImparatiMD());
		RegistroIntermezzi.setFornitoreDiDati(() -> getIstanza().getIntermezziMD());
		Statistiche.setFornitoreDiDati(() -> getIstanza().getStatisticheMD());
		LineaTemporale.setFornitoreDiDati(() -> getIstanza().getLineaTemporaleMD());
		RegistroPersonaggi.setFornitoreDiDati(() -> getIstanza().getRegistroPersonaggiMD());
		RegistroArtefatti.setFornitoreDiDati(() -> getIstanza().getRegistroArtefattiMD());
		RegistroMissioni.setFornitoreDiDati(() -> getIstanza().getRegistroMissioniMD());
		MagazziniDeiNegozi.setFornitoreDiDati(() -> getIstanza().getMagazziniMD());
		Cassa.setFornitoreDiDati(() -> getIstanza().getCassaMD());
		Notizie.setFornitoreDiDati(() -> getIstanza().getNotizieMD());
	}

	/**
	 * L'istanza del modello dati su cui si basa la partita in corso.
	 */
	public static ModelloDati getIstanza() {
		return istanza;
	}

	/**
	 * Sostituisce l'istanza del modello dati: succede quando si carica un salvataggio.
	 */
	public static void setIstanza(ModelloDati modelloDati) {
		istanza = modelloDati;
	}

	// Dati sufficienti per mostrare una situazione di salvataggio
	private final GruppoGiocatoreMD gruppoGiocatoreMD;
	private final CassaMD cassaMD;
	private final TatticheMD tatticheMD;
	// Tutto il resto dei dati di gioco
	private final CantiImparatiMD cantiImparatiMD;
	private final StatisticheMD statisticheMD;
	private final LineaTemporaleMD lineaTemporaleMD;
	private final ForestaMD forestaMD;
	private final RegistroPersonaggiMD registroPersonaggiMD;
	private final RegistroArtefattiMD registroArtefattiMD;
	private final MagazziniMD magazziniMD;
	private final RegistroMissioniMD registroMissioniMD;
	private final NotizieMD notizieMD;
	private final IntermezziMD intermezziMD;
	// I cartigli con la descrizione dei comandi sopra la barra delle icone: accesi in una partita nuova
	private boolean aiutoAbilitato = true;

	public ModelloDati() {
		gruppoGiocatoreMD = new GruppoGiocatoreMD();
		cassaMD = new CassaMD();
		tatticheMD = new TatticheMD();
		cantiImparatiMD = new CantiImparatiMD();
		statisticheMD = new StatisticheMD();
		lineaTemporaleMD = new LineaTemporaleMD();
		forestaMD = new ForestaMD();
		registroPersonaggiMD = new RegistroPersonaggiMD();
		registroArtefattiMD = new RegistroArtefattiMD();
		magazziniMD = new MagazziniMD();
		registroMissioniMD = new RegistroMissioniMD();
		notizieMD = new NotizieMD();
		intermezziMD = new IntermezziMD();
	}

	public final GruppoGiocatoreMD getGruppoGiocatoreMD() {
		return gruppoGiocatoreMD;
	}

	public final CassaMD getCassaMD() {
		return cassaMD;
	}

	public final TatticheMD getTatticheMD() {
		return tatticheMD;
	}

	public final CantiImparatiMD getCantiImparatiMD() {
		return cantiImparatiMD;
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

	public MagazziniMD getMagazziniMD() {
		return magazziniMD;
	}

	public RegistroMissioniMD getRegistroMissioniMD() {
		return registroMissioniMD;
	}

	public NotizieMD getNotizieMD() {
		return notizieMD;
	}

	public IntermezziMD getIntermezziMD() {
		return intermezziMD;
	}

	public boolean isAiutoAbilitato() {
		return aiutoAbilitato;
	}

	public void setAiutoAbilitato(boolean aiutoAbilitato) {
		this.aiutoAbilitato = aiutoAbilitato;
	}

	///////////////////////////////////

	/**
	 * Azzera tutte le parti per una partita nuova, con una mappa della Foresta delle dimensioni indicate.
	 */
	public void reimposta(int dimensioneX, int dimensioneY) {
		gruppoGiocatoreMD.reimposta();
		cassaMD.reimposta();
		tatticheMD.reimposta();
		cantiImparatiMD.reimposta();
		statisticheMD.reimposta();
		lineaTemporaleMD.reimposta();
		forestaMD.reimposta(dimensioneX, dimensioneY);
		registroPersonaggiMD.reimposta();
		registroArtefattiMD.reimposta();
		magazziniMD.reimposta();
		registroMissioniMD.reimposta();
		notizieMD.reimposta();
		intermezziMD.reimposta();
		aiutoAbilitato = true;
	}

	/**
	 * Scrive tutte le parti del modello dati, una dopo l'altra e nell'ordine in cui le ha {@link #leggi(BufferedReader)},
	 * e infine se l'aiuto della barra delle icone è acceso.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		gruppoGiocatoreMD.salva(stream);
		cassaMD.salva(stream);
		tatticheMD.salva(stream);
		cantiImparatiMD.salva(stream);
		statisticheMD.salva(stream);
		lineaTemporaleMD.salva(stream);
		forestaMD.salva(stream);
		registroPersonaggiMD.salva(stream);
		registroArtefattiMD.salva(stream);
		magazziniMD.salva(stream);
		registroMissioniMD.salva(stream);
		notizieMD.salva(stream);
		intermezziMD.salva(stream);
		stream.println(aiutoAbilitato);
	}

	/**
	 * Azzera le parti e le rilegge da ciò che scrive {@link #salva(PrintWriter)}.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		gruppoGiocatoreMD.reimposta();
		cassaMD.reimposta();
		tatticheMD.reimposta();
		cantiImparatiMD.reimposta();
		statisticheMD.reimposta();
		lineaTemporaleMD.reimposta();
		registroPersonaggiMD.reimposta();
		registroArtefattiMD.reimposta();
		magazziniMD.reimposta();
		registroMissioniMD.reimposta();
		notizieMD.reimposta();
		intermezziMD.reimposta();

		gruppoGiocatoreMD.leggi(stream);
		cassaMD.leggi(stream);
		tatticheMD.leggi(stream);
		cantiImparatiMD.leggi(stream);
		statisticheMD.leggi(stream);
		lineaTemporaleMD.leggi(stream);
		forestaMD.leggi(stream);
		registroPersonaggiMD.leggi(stream);
		registroArtefattiMD.leggi(stream);
		magazziniMD.leggi(stream);
		registroMissioniMD.leggi(stream);
		notizieMD.leggi(stream);
		intermezziMD.leggi(stream);
		aiutoAbilitato = LettoreCampi.leggiBooleano(stream);
	}

}
