package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoFineLogoIniziale;
import com.threeamigos.foresta.eventi.interni.InternoInterfacciaUtentePronta;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.strumenti.temporizzatore.Temporizzatore;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.tipi.StileDellaBarra;
import com.threeamigos.foresta.tipi.TipoDiLayout;
import com.threeamigos.foresta.ui.barra.ComandiPossibili;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.schermate.DisegnoDiContenutoAwt;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmaginiAwt;
import com.threeamigos.foresta.ui.sfx.PannelloLogoIniziale;
import com.threeamigos.foresta.ui.sfx.TracciatoreLogo;

import javax.swing.*;
import java.awt.*;

/**
 * La classe radice dell'interfaccia in Swing: crea la finestra (sul thread di Swing), il canvas e il prompt, mostra il logo iniziale
 * mentre carica le risorse e poi lascia il posto alla schermata di gioco. Gli eventi del bus li traduce la parte comune
 * ({@link ForestaUI}). Quando la finestra è aperta pubblica {@code InternoInterfacciaUtentePronta}, e da lì l'automa parte. Vedi
 * motore_grafico.md.
 */
public class ForestaSwing extends ForestaUI {

	// Lo schermo simulato con SCHERMO=, o null per lo schermo di sempre
	private final MisuraDelloSchermo schermoDiProva;
	// Dove sta la barra e, per lo schermo simulato, il suo layout (vedi scegliLaDisposizione): cambiano se il giocatore sposta la barra dalle opzioni
	private Orientamento orientamento;
	private boolean barraASinistra;
	private SceltaDelLayout scelta;
	private final boolean tuttoSchermo;
	private final boolean saltaLogoIniziale;
	private boolean barraDock;
	private final boolean joystickVirtuale;
	private final GestorePunteggi gestorePunteggi;

	private JFrame jframe;
	private Prompt prompt;
	private DisplayableCanvas displayableCanvas;
	// Come togliere le iscrizioni al bus che ha fatto il canvas: servono quando lo si rifà, con la barra da un'altra parte
	private java.util.List<Runnable> iscrizioniDelCanvas = java.util.Collections.emptyList();
	private int larghezza;
	private int altezza;
	// Solo durante lo stato LOGO_INIZIALE; poi il posto passa al displayableCanvas
	private PannelloLogoIniziale pannelloLogoIniziale;
	private volatile boolean interfacciaCompleta;
	private boolean logoInizialeConcluso;

	/**
	 * @param saltaLogoIniziale vero per non mostrare il logo iniziale (per esempio nelle partite di prova): si
	 *                          aspetta solo il caricamento delle risorse e si passa subito all'INTRO
	 * @param barraDock vero per la barra delle icone che si ingrandisce sotto il cursore, come il Dock di macOS
	 *                  (vedi DisplayableCanvasBarraIconeDock; solo in orientamento orizzontale)
	 * @param joystickVirtuale vero per il joystick a schermo sulla mappa (vedi JoystickVirtuale), anche dove c'è il mouse
	 * @param gestorePunteggi la classifica da mostrare nell'intro (la stessa che riceve l'Automa)
	 * @param vistaPartita lo stato della partita da mostrare, in sola lettura
	 */
	/**
	 * @param posizione      dove sta la barra (dagli argomenti o dalle opzioni), o null per lasciarlo decidere: in basso sul desktop, e per lo
	 *                       schermo simulato quel che sceglie SceltaDelLayout
	 * @param schermoDiProva lo schermo simulato con SCHERMO=, o null per lo schermo di sempre: Swing non rimpicciolisce, e la finestra è grande quanto
	 *                       l'area virtuale (vedi layout_telefono.md §3.2)
	 */
	public ForestaSwing(PosizioneDellaBarra posizione, MisuraDelloSchermo schermoDiProva, boolean tuttoSchermo, boolean saltaLogoIniziale,
						boolean barraDock, boolean joystickVirtuale, GestorePunteggi gestorePunteggi, VistaPartita vistaPartita,
						Temporizzatore temporizzatore) {
		super(vistaPartita, temporizzatore);
		this.schermoDiProva = schermoDiProva;
		this.barraDock = barraDock;
		this.joystickVirtuale = joystickVirtuale;
		this.gestorePunteggi = gestorePunteggi;
		this.tuttoSchermo = tuttoSchermo;
		scegliLaDisposizione(posizione);
		this.saltaLogoIniziale = saltaLogoIniziale;
		// Per ultimo: l'interfaccia, appena pronta, pubblica eventi a cui il motore risponde, e le iscrizioni devono esserci già tutte
		SwingUtilities.invokeLater(this::creaEMostraInterfacciaUtente);
	}

	/**
	 * Dove sta la barra: per lo schermo simulato lo sceglie SceltaDelLayout (in basso sul tablet se ci sta, a destra sul telefono, se il giocatore non
	 * ha deciso altro); sul desktop in basso, a destra o a sinistra come dice la posizione (in basso, se non dice niente)
	 */
	private void scegliLaDisposizione(PosizioneDellaBarra posizione) {
		if (schermoDiProva != null) {
			scelta = SceltaDelLayout.per(schermoDiProva, new MisureDelleImmaginiAwt(), LivelloDiZoom.valore(), posizione);
			orientamento = scelta.getOrientamento();
			barraASinistra = scelta.isBarraASinistra();
		} else {
			scelta = null;
			orientamento = posizione == null || posizione == PosizioneDellaBarra.BASSO ? Orientamento.ORIZZONTALE : Orientamento.VERTICALE;
			barraASinistra = posizione == PosizioneDellaBarra.SINISTRA;
		}
	}

	/**
	 * Le dimensioni della finestra di gioco: lo schermo intero, oppure quelle date dalle cornici dei riquadri (vedi
	 * {@link DisposizioneDelloSchermo#dimensioniDelloSchermo}), senza superare lo schermo. Le legge dalle intestazioni dei file,
	 * senza caricare le immagini: la finestra si apre subito, col logo iniziale.
	 */
	static Dimension calcolaDimensioniFinestra(Orientamento orientamento, boolean tuttoSchermo) {
		Dimension schermo = Toolkit.getDefaultToolkit().getScreenSize();
		if (tuttoSchermo) {
			return new Dimension(schermo.width, schermo.height);
		}
		Misura dimensioni = DisposizioneDelloSchermo.dimensioniDelloSchermo(orientamento, new MisureDelleImmaginiAwt(), LivelloDiZoom.valore());
		return new Dimension(Math.min(dimensioni.larghezza, schermo.width), Math.min(dimensioni.altezza, schermo.height));
	}

	/**
	 * Apre subito la finestra con il solo logo iniziale: prompt e DisplayableCanvas, e le immagini di cui hanno
	 * bisogno, arrivano durante l'animazione (vedi avviaLogoIniziale).
	 */
	private void creaEMostraInterfacciaUtente() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		jframe = new JFrame("La Foresta");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension dimensioniFinestra = dimensioniFinestra();
		larghezza = dimensioniFinestra.width;
		altezza = dimensioniFinestra.height;

		jframe.setLayout(null);
		Container c = jframe.getContentPane();
		c.setBackground(Color.BLACK);
		c.setPreferredSize(new Dimension(larghezza, altezza));

		if (!saltaLogoIniziale) {
			pannelloLogoIniziale = new PannelloLogoIniziale(larghezza, altezza);
			jframe.add(pannelloLogoIniziale);
			pannelloLogoIniziale.setLocation(0, 0);
		}

		jframe.pack();
		jframe.setResizable(false);
		jframe.setLocation((screenDimension.width - jframe.getSize().width) / 2, (screenDimension.height - jframe.getSize().height) / 2);
		jframe.setVisible(true);

		BusEventi.pubblica(new InternoInterfacciaUtentePronta());
	}

	/**
	 * Fase LOGO_INIZIALE: fa partire l'animazione e, in un altro thread, il caricamento delle immagini; poi, di
	 * nuovo sull'EDT, costruisce prompt e DisplayableCanvas (vedi completaInterfacciaUtente).
	 */
	@Override
	protected void avviaLogoIniziale() {
		temporizzatore.inizia(TracciatoreLogo.INTERVALLO_FOTOGRAMMA_MS);
		Thread caricamento = new Thread(() -> {
			ImageCache.init();
			// I loghi dell'intro, con l'alone, si preparano ora: all'intro non devono costare uno scatto
			DisegnoDiContenutoAwt.precaricaILoghi();
			SwingUtilities.invokeLater(this::completaInterfacciaUtente);
		}, "Precaricamento immagini");
		caricamento.setDaemon(true);
		caricamento.start();
	}

	private void completaInterfacciaUtente() {
		prompt = new Prompt();
		prompt.setLocation((larghezza - prompt.getSize().width) / 2, (altezza - prompt.getSize().height) / 2);

		creaIlCanvas();
		interfacciaCompleta = true;
	}

	/**
	 * La finestra: quella dello schermo simulato (la sua area virtuale), o quella che serve sul desktop per la barra dove sta
	 */
	private Dimension dimensioniFinestra() {
		return scelta != null
				? new Dimension(scelta.getAreaVirtuale().larghezza, scelta.getAreaVirtuale().altezza)
				: calcolaDimensioniFinestra(orientamento, tuttoSchermo);
	}

	/**
	 * Il canvas con la barra dove sta adesso, ricordando come togliere le iscrizioni al bus che fa: se la barra si sposta, il canvas si rifà
	 */
	private void creaIlCanvas() {
		Logger.log(() -> "Orientamento: " + orientamento + (barraASinistra ? ", barra a sinistra" : ""));
		iscrizioniDelCanvas = BusEventi.iscrizioniDurante(() -> displayableCanvas = new DisplayableCanvas(
				scelta != null ? scelta.getTipo() : TipoDiLayout.TABLET, orientamento, barraASinistra, larghezza, altezza, barraDock,
				gestorePunteggi, vistaPartita));
		displayableCanvas.setJoystickVirtuale(joystickVirtuale);
		interfacciaCompleta(displayableCanvas.getSchermo(), prompt.getCampo());
	}

	/**
	 * Dalle opzioni (all'intro, senza una partita in corso): il canvas si rifà con la barra dove l'ha voluta il giocatore, e la finestra cambia misura
	 * se la barra passa dal basso a un lato o viceversa. Si resta nella schermata delle opzioni.
	 */
	@Override
	protected void cambiaPosizioneDellaBarra(PosizioneDellaBarra posizione) {
		Orientamento prima = orientamento;
		boolean primaASinistra = barraASinistra;
		scegliLaDisposizione(posizione);
		if (displayableCanvas == null || prima == orientamento && primaASinistra == barraASinistra) {
			return;
		}
		rifaiIlCanvas();
	}

	/**
	 * Dalle opzioni (all'intro, senza una partita in corso): il canvas si rifà con la barra normale o con il Dock. Si resta nella schermata delle opzioni.
	 */
	@Override
	protected void cambiaStileDellaBarra(StileDellaBarra stile) {
		boolean dock = stile != StileDellaBarra.NORMALE;
		if (displayableCanvas == null || dock == barraDock) {
			barraDock = dock;
			return;
		}
		barraDock = dock;
		rifaiIlCanvas();
	}

	/**
	 * Butta il canvas, con le sue iscrizioni al bus, e ne fa uno nuovo con la disposizione e la barra di adesso; la finestra prende la sua misura. Si
	 * torna alla schermata delle opzioni, con le scelte che il motore aveva messo nella barra.
	 */
	private void rifaiIlCanvas() {
		Dimension dimensioniFinestra = dimensioniFinestra();
		larghezza = dimensioniFinestra.width;
		altezza = dimensioniFinestra.height;
		iscrizioniDelCanvas.forEach(Runnable::run);
		// Tolto dalla finestra, il canvas ferma il suo thread dell'animazione
		jframe.remove(displayableCanvas);
		creaIlCanvas();
		jframe.getContentPane().setPreferredSize(new Dimension(larghezza, altezza));
		jframe.add(displayableCanvas);
		displayableCanvas.setLocation(0, 0);
		prompt.setLocation((larghezza - prompt.getSize().width) / 2, (altezza - prompt.getSize().height) / 2);
		jframe.pack();
		jframe.revalidate();
		jframe.repaint();
		// La schermata delle opzioni, con le scelte che il motore aveva messo nella barra
		getSchermo().opzioni(ComandiPossibili.getComandi());
		getSchermo().impostaAzioniIcone();
	}

	@Override
	protected boolean barraInBassoPossibile() {
		return schermoDiProva == null || SceltaDelLayout.barraInBassoPossibile(schermoDiProva, new MisureDelleImmaginiAwt(), LivelloDiZoom.valore());
	}

	/**
	 * Un fotogramma del logo iniziale. Quando l'animazione e' finita (subito, se il logo va saltato) e l'interfaccia
	 * e' completa, il logo lascia il posto al DisplayableCanvas e l'Automa viene avvisato.
	 */
	@Override
	protected void avanzaLogoIniziale() {
		if (logoInizialeConcluso) {
			return;
		}
		if (pannelloLogoIniziale != null) {
			pannelloLogoIniziale.avanza();
		}
		boolean animazioneFinita = pannelloLogoIniziale == null || pannelloLogoIniziale.isFinito();
		if (animazioneFinita && interfacciaCompleta) {
			logoInizialeConcluso = true;
			temporizzatore.termina();
			if (pannelloLogoIniziale != null) {
				jframe.remove(pannelloLogoIniziale);
				pannelloLogoIniziale = null;
			}
			// Nel layered pane, non nel content pane: da fratello del canvas la sua cornice
			// verrebbe coperta a ogni repaint() del canvas (es. al movimento del mouse), perché
			// il content pane presume che i figli non si sovrappongano e ridisegna solo il canvas.
			jframe.getLayeredPane().add(prompt, JLayeredPane.PALETTE_LAYER);
			jframe.add(displayableCanvas);
			displayableCanvas.setLocation(0, 0);
			jframe.revalidate();
			jframe.repaint();
			BusEventi.pubblica(new InternoFineLogoIniziale());
		}
	}

	@Override
	protected void rinfresca() {
		jframe.invalidate();
		jframe.repaint();
	}
}
