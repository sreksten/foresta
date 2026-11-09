package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoFineLogoIniziale;
import com.threeamigos.foresta.eventi.interni.InternoInterfacciaUtentePronta;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.strumenti.temporizzatore.Temporizzatore;
import com.threeamigos.foresta.tipi.Orientamento;
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

	private final Orientamento orientamento;
	private final boolean tuttoSchermo;
	private final boolean saltaLogoIniziale;
	private final boolean barraDock;
	private final boolean joystickVirtuale;
	private final GestorePunteggi gestorePunteggi;

	private JFrame jframe;
	private Prompt prompt;
	private DisplayableCanvas displayableCanvas;
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
	public ForestaSwing(Orientamento orientamento, boolean tuttoSchermo, boolean saltaLogoIniziale, boolean barraDock,
						boolean joystickVirtuale, GestorePunteggi gestorePunteggi, VistaPartita vistaPartita, Temporizzatore temporizzatore) {
		super(vistaPartita, temporizzatore);
		this.barraDock = barraDock;
		this.joystickVirtuale = joystickVirtuale;
		this.gestorePunteggi = gestorePunteggi;
		this.orientamento = orientamento;
		this.tuttoSchermo = tuttoSchermo;
		this.saltaLogoIniziale = saltaLogoIniziale;
		// Per ultimo: l'interfaccia, appena pronta, pubblica eventi a cui il motore risponde, e le iscrizioni devono esserci già tutte
		SwingUtilities.invokeLater(this::creaEMostraInterfacciaUtente);
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
		Dimension dimensioniFinestra = calcolaDimensioniFinestra(orientamento, tuttoSchermo);
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

		Logger.log(() -> "Orientamento: " + orientamento);
		displayableCanvas = new DisplayableCanvas(orientamento, larghezza, altezza, barraDock, gestorePunteggi, vistaPartita);
		displayableCanvas.setJoystickVirtuale(joystickVirtuale);
		interfacciaCompleta(displayableCanvas.getSchermo(), prompt.getCampo());
		interfacciaCompleta = true;
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
