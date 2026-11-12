package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.TipoDiLayout;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;

/**
 * Dove stanno le parti dello schermo di gioco, senza toolkit: la barra delle icone (in fondo in orizzontale, a destra in
 * verticale), l'area di contenuto che resta, e in questa i riquadri della schermata di gioco. Le misure vengono dalle cornici
 * (con lo zoom): a destra la colonna con la mappa e le statistiche, allineate a destra, e sotto di loro i riquadri impilati
 * (gruppo, incantesimi e pozioni, missioni); a sinistra la locazione con il testo sotto. La usano Swing e libGDX.
 * <p>
 * Questo è il layout del {@link TipoDiLayout#TABLET}. In quello del {@link TipoDiLayout#TELEFONO} (vedi {@code layout_telefono.md} §4)
 * le statistiche non ci sono (la loro area è vuota) e la mappa è il quarto riquadro della pila, che comincia in cima alla colonna a destra;
 * la colonna a sinistra, con locazione e testo, prende tutta la larghezza che resta.
 */
public final class DisposizioneDelloSchermo {

	/** Spessore della fascia (o della colonna) della barra icone */
	public static final int SPESSORE_BARRA_ICONE = 72;
	/** Quanti riquadri sono impilati nella colonna a destra nel layout del tablet (gruppo, incantesimi e pozioni, missioni) */
	public static final int RIQUADRI_IMPILATI = 3;
	/** Quanti riquadri sono impilati nel layout del telefono: anche la mappa */
	public static final int RIQUADRI_IMPILATI_DEL_TELEFONO = 4;

	public static final String RISORSA_CORNICE_GRANDE = "fondi/CorniceGrande.png";
	public static final String RISORSA_CORNICE_MAPPA = "fondi/CorniceMappa.png";
	public static final String RISORSA_CORNICE_PICCOLA = "fondi/CornicePiccola.png";
	public static final String RISORSA_CORNICE_INCANTESIMI = "fondi/CorniceIncantesimi.png";
	public static final String RISORSA_BOSCO = "locazioni/Foresta.png";

	private static final int SP = Finestra.SPACING;

	private final TipoDiLayout tipo;
	private final Orientamento orientamento;
	// In verticale, la barra sta a sinistra invece che a destra: tutto il resto si disegna spostato a destra del suo spessore (getXDelContenuto)
	private final boolean barraASinistra;
	private final int larghezzaTotale;
	private final int altezzaTotale;
	private final int larghezzaContenuto;
	private final int altezzaContenuto;
	private final Area barra;
	private final Area mappa;
	private final Area locazione;
	private final Area statistiche;
	private final Area testo;
	private final Area gruppo;
	private final Area incantesimi;
	private final Area missioni;
	private final int colonnaDestraX;
	private final int yPreferitaDelCombattimento;
	private final int quotaPila;
	private final int passoPila;
	private final int riquadriImpilati;

	/**
	 * @param larghezza   la larghezza di tutto lo schermo, barra compresa
	 * @param altezza     l'altezza di tutto lo schermo, barra compresa
	 * @param misure      le misure delle immagini, senza zoom
	 * @param zoom        lo zoom con cui si disegnano le immagini
	 */
	public DisposizioneDelloSchermo(Orientamento orientamento, int larghezza, int altezza, MisureDelleImmagini misure, double zoom) {
		this(TipoDiLayout.TABLET, orientamento, larghezza, altezza, misure, zoom);
	}

	/**
	 * @param tipo        il layout (vedi SceltaDelLayout); quello del telefono vuole la barra a destra
	 * @param larghezza   la larghezza di tutto lo schermo, barra compresa
	 * @param altezza     l'altezza di tutto lo schermo, barra compresa
	 * @param misure      le misure delle immagini, senza zoom
	 * @param zoom        lo zoom con cui si disegnano le immagini
	 */
	public DisposizioneDelloSchermo(TipoDiLayout tipo, Orientamento orientamento, int larghezza, int altezza, MisureDelleImmagini misure, double zoom) {
		this(tipo, orientamento, false, larghezza, altezza, misure, zoom);
	}

	/**
	 * @param barraASinistra in verticale, la barra a sinistra invece che a destra: le aree del contenuto restano le stesse, nelle coordinate del
	 *                       contenuto (che comincia in x = 0), e chi disegna e chi riceve il mouse le sposta di {@link #getXDelContenuto()}; la barra
	 *                       sta in x = 0
	 */
	public DisposizioneDelloSchermo(TipoDiLayout tipo, Orientamento orientamento, boolean barraASinistra, int larghezza, int altezza,
			MisureDelleImmagini misure, double zoom) {
		this.barraASinistra = barraASinistra && orientamento == Orientamento.VERTICALE;
		if (tipo == TipoDiLayout.TELEFONO && orientamento != Orientamento.VERTICALE) {
			throw new IllegalArgumentException("Il layout del telefono ha la barra delle icone a destra");
		}
		this.tipo = tipo;
		this.orientamento = orientamento;
		larghezzaTotale = larghezza;
		altezzaTotale = altezza;
		if (orientamento == Orientamento.ORIZZONTALE) {
			larghezzaContenuto = larghezza;
			altezzaContenuto = altezza - SPESSORE_BARRA_ICONE;
			barra = new Area(0, altezzaContenuto, larghezza, SPESSORE_BARRA_ICONE);
		} else {
			larghezzaContenuto = larghezza - SPESSORE_BARRA_ICONE;
			altezzaContenuto = altezza;
			barra = new Area(this.barraASinistra ? 0 : larghezza - SPESSORE_BARRA_ICONE, 0, SPESSORE_BARRA_ICONE, altezza);
		}
		Misura corniceGrande = conZoom(misure, RISORSA_CORNICE_GRANDE, zoom);
		Misura corniceMappa = conZoom(misure, RISORSA_CORNICE_MAPPA, zoom);
		Misura cornicePiccola = conZoom(misure, RISORSA_CORNICE_PICCOLA, zoom);
		int altezzaLocazione = conZoom(misure, RISORSA_BOSCO, zoom).altezza;
		// Di norma sotto l'immagine della locazione, dove ci sono i personaggi
		yPreferitaDelCombattimento = SP + altezzaLocazione + SP;
		colonnaDestraX = larghezzaContenuto - SP - corniceGrande.larghezza;
		gruppo = new Area(colonnaDestraX, SP, corniceGrande.larghezza, corniceGrande.altezza);

		if (tipo == TipoDiLayout.TELEFONO) {
			// Una colonna a sinistra con locazione e testo, larga quanto resta, e a destra la pila di quattro riquadri grandi dalla cima
			int larghezzaSinistra = colonnaDestraX - 2 * SP;
			locazione = new Area(SP, SP, larghezzaSinistra, altezzaLocazione);
			testo = new Area(SP, SP + altezzaLocazione + SP, larghezzaSinistra, altezzaContenuto - SP - altezzaLocazione - SP);
			statistiche = new Area(0, 0, 0, 0);
			incantesimi = gruppo;
			missioni = gruppo;
			// La mappa ha la cornice grande degli altri riquadri impilati (vedi RiquadroMappa.nellaCorniceDellaPila)
			mappa = gruppo;
			riquadriImpilati = RIQUADRI_IMPILATI_DEL_TELEFONO;
			quotaPila = SP;
			passoPila = Math.max(0, (altezzaContenuto - quotaPila - corniceGrande.altezza) / riquadriImpilati);
			return;
		}

		// Fino all'altezza della locazione la colonna a destra è larga quanto la mappa, quindi lo spazio per la locazione è la
		// larghezza meno quella della mappa; il testo sotto, accanto ai riquadri impilati, è più stretto
		int larghezzaLocazioneDisponibile = larghezzaContenuto - 3 * SP - corniceMappa.larghezza;
		int larghezzaTesto = colonnaDestraX - 2 * SP;
		// Mappa e statistiche, più strette dei riquadri impilati, sono allineate a destra
		int mappaX = colonnaDestraX + corniceGrande.larghezza - corniceMappa.larghezza;
		int statisticheX = colonnaDestraX + corniceGrande.larghezza - cornicePiccola.larghezza;

		mappa = new Area(mappaX, SP, corniceMappa.larghezza, corniceMappa.altezza);
		locazione = new Area(SP, SP, larghezzaLocazioneDisponibile, altezzaLocazione);
		statistiche = new Area(statisticheX, SP + corniceMappa.altezza + SP, cornicePiccola.larghezza, cornicePiccola.altezza);
		testo = new Area(SP, SP + altezzaLocazione + SP, larghezzaTesto, altezzaContenuto - SP - altezzaLocazione - SP);
		incantesimi = new Area(colonnaDestraX, SP + corniceGrande.altezza + SP, corniceGrande.larghezza, corniceGrande.altezza);
		missioni = new Area(colonnaDestraX, SP + 2 * (corniceGrande.altezza + SP), corniceGrande.larghezza, corniceGrande.altezza);
		quotaPila = SP + corniceMappa.altezza + SP + cornicePiccola.altezza + SP;
		riquadriImpilati = RIQUADRI_IMPILATI;
		passoPila = Math.max(0, (altezzaContenuto - quotaPila - corniceGrande.altezza) / riquadriImpilati);
	}

	/**
	 * Le dimensioni dello schermo che servono per vedere tutto: quelle date dalle cornici dei riquadri e dalla barra delle icone.
	 * In orizzontale la barra sta in fondo, in verticale a destra: l'area di contenuto è la stessa, quindi lo schermo verticale è
	 * più largo della barra e più basso di lei.
	 *
	 * @return larghezza e altezza
	 */
	public static Misura dimensioniDelloSchermo(Orientamento orientamento, MisureDelleImmagini misure, double zoom) {
		Misura corniceMappa = conZoom(misure, RISORSA_CORNICE_MAPPA, zoom);
		Misura bosco = conZoom(misure, RISORSA_BOSCO, zoom);
		Misura corniceGrande = conZoom(misure, RISORSA_CORNICE_GRANDE, zoom);
		Misura corniceIncantesimi = conZoom(misure, RISORSA_CORNICE_INCANTESIMI, zoom);
		int larghezza = SP + corniceMappa.larghezza + SP + bosco.larghezza + SP + corniceGrande.larghezza + SP;
		int altezza = SP + corniceGrande.altezza + SP + corniceIncantesimi.altezza + SP + corniceGrande.altezza + SP
				+ altezzaMassimaDelleIcone(misure, zoom) + SP;
		if (orientamento != Orientamento.ORIZZONTALE) {
			larghezza += SPESSORE_BARRA_ICONE;
			altezza -= SPESSORE_BARRA_ICONE;
		}
		return new Misura(larghezza, altezza);
	}

	/**
	 * La misura dell'area di contenuto del layout di oggi (lo schermo meno la barra delle icone): è la scena su cui sono composte le pagine degli
	 * intermezzi (vedi ScenaDellIntermezzo), che su uno schermo di un'altra misura si centra e si ritaglia.
	 */
	public static Misura dimensioniDellaScena(MisureDelleImmagini misure, double zoom) {
		Misura schermo = dimensioniDelloSchermo(Orientamento.VERTICALE, misure, zoom);
		return new Misura(schermo.larghezza - SPESSORE_BARRA_ICONE, schermo.altezza);
	}

	/**
	 * L'altezza dell'icona più alta (con lo zoom), dalle misure degli sfondi delle icone.
	 */
	public static int altezzaMassimaDelleIcone(MisureDelleImmagini misure, double zoom) {
		int massima = 0;
		for (TipoIcona icona : TipoIcona.values()) {
			massima = Math.max(massima, conZoom(misure, icona.getSfondo().getRisorsa(), zoom).altezza);
		}
		return massima;
	}

	/**
	 * La misura dell'immagine con lo zoom, arrotondata come la ingrandiscono i toolkit
	 */
	public static Misura conZoom(MisureDelleImmagini misure, String risorsa, double zoom) {
		Misura misura = misure.immagine(risorsa);
		return new Misura((int) Math.round(misura.larghezza * zoom), (int) Math.round(misura.altezza * zoom));
	}

	public TipoDiLayout getTipo() {
		return tipo;
	}

	public boolean isTelefono() {
		return tipo == TipoDiLayout.TELEFONO;
	}

	/** Quanti riquadri stanno nella pila: tre sul tablet, quattro (con la mappa) sul telefono */
	public int getRiquadriImpilati() {
		return riquadriImpilati;
	}

	public Orientamento getOrientamento() {
		return orientamento;
	}

	/** Se la barra sta a sinistra (solo in verticale) */
	public boolean isBarraASinistra() {
		return barraASinistra;
	}

	/**
	 * Di quanto il contenuto (riquadri e schermate a tutto schermo, che hanno le loro coordinate a partire da 0) sta a destra sullo schermo: lo spessore
	 * della barra se sta a sinistra, altrimenti 0. Le aree di questa classe sono nelle coordinate del contenuto, tranne quella della barra, che è nelle
	 * coordinate dello schermo
	 */
	public int getXDelContenuto() {
		return barraASinistra ? SPESSORE_BARRA_ICONE : 0;
	}

	/** La larghezza di tutto lo schermo, barra compresa */
	public int getLarghezzaTotale() {
		return larghezzaTotale;
	}

	/** L'altezza di tutto lo schermo, barra compresa */
	public int getAltezzaTotale() {
		return altezzaTotale;
	}

	/** La larghezza dell'area in cui si disegnano riquadri e schermate a tutto schermo, esclusa la barra icone */
	public int getLarghezzaContenuto() {
		return larghezzaContenuto;
	}

	/** L'altezza dell'area in cui si disegnano riquadri e schermate a tutto schermo, esclusa la barra icone */
	public int getAltezzaContenuto() {
		return altezzaContenuto;
	}

	/** L'area di contenuto, a partire dall'angolo in alto a sinistra */
	public Area getContenuto() {
		return new Area(0, 0, larghezzaContenuto, altezzaContenuto);
	}

	public Area getBarra() {
		return barra;
	}

	/** Il riquadro della mappa: sul telefono, con la cornice grande, il suo posto nella pila nella prima posizione (si sposta in verticale) */
	public Area getMappa() {
		return mappa;
	}

	public Area getLocazione() {
		return locazione;
	}

	/** Il riquadro delle statistiche; vuoto sul telefono, dove non c'è */
	public Area getStatistiche() {
		return statistiche;
	}

	public Area getTesto() {
		return testo;
	}

	/** Il riquadro del gruppo nella sua prima posizione: impilato, si sposta in verticale */
	public Area getGruppo() {
		return gruppo;
	}

	/** Il riquadro di incantesimi e pozioni nella sua prima posizione: impilato, si sposta in verticale */
	public Area getIncantesimi() {
		return incantesimi;
	}

	/** Il riquadro delle missioni nella sua prima posizione: impilato, si sposta in verticale */
	public Area getMissioni() {
		return missioni;
	}

	/** Dove comincia la colonna a destra: il riquadro del combattimento si centra sullo spazio alla sua sinistra */
	public int getColonnaDestraX() {
		return colonnaDestraX;
	}

	/** Dove sta, di norma, il riquadro del combattimento: sotto l'immagine della locazione */
	public int getYPreferitaDelCombattimento() {
		return yPreferitaDelCombattimento;
	}

	/** La quota del primo dei riquadri impilati */
	public int getQuotaPila() {
		return quotaPila;
	}

	/** Di quanto ogni riquadro impilato sta più in basso del precedente */
	public int getPassoPila() {
		return passoPila;
	}

	/** La quota del riquadro impilato in quella posizione (0 il più in alto) */
	public int quotaNellaPila(int posizione) {
		return quotaPila + posizione * passoPila;
	}
}
