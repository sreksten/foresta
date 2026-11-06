package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.barra.Cartiglio;
import com.threeamigos.foresta.ui.componenti.JoystickVirtualeAwt;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.riquadri.RiquadroMappa.Tessera;
import com.threeamigos.foresta.ui.sfx.GestoreNuvole;
import com.threeamigos.foresta.ui.sfx.NuvoleAwt;

import java.awt.*;
import java.util.List;

/**
 * La mappa della foresta a tutto schermo in Swing: si trascina con il mouse, mostra il nome della casella sotto il cursore e, in basso, il notiziario.
 * Con il joystick virtuale (vedi {@link #abilitaJoystickVirtuale()}) c'è anche un disco in basso a destra che, premuto, fa comparire una mano al centro
 * della mappa: la mano si muove finché il disco è premuto, con la velocità e la direzione date dalla posizione del puntatore rispetto al disco, e fa da
 * puntatore (mostra il nome di ciò che ha sotto); al rilascio sparisce. Dove sta la mappa, il nome sotto il mouse e la mano sono di
 * {@link MappaATuttoSchermo} e il notiziario di {@link StrisciaDelleNotizie}, nel modulo comune; qui c'è il disegno.
 */
public class DisplayableCanvasMappaATuttoSchermo extends DisegnatoreMappa implements Finestra {

	// Il notiziario si ferma a metà della larghezza della sfera magica: l'altra metà è coperta dalla sfera stessa (disegnata in primo piano sull'angolo
	// inferiore sinistro di tutto il canvas, vedi DisplayableCanvas.disegnaSferaMagica), ma essendo un'immagine con trasparenza le notizie continuano a
	// intravedersi dietro di essa fino al centro, dove l'immagine diventa opaca.
	private static final int LARGHEZZA_SFERA_MAGICA_COPERTA = ImageCache.sferaMagica.getWidth() / 2;

	private final int width;
	private final int height;
	private final MappaATuttoSchermo modello;
	private final Notiziario notiziario;

	public DisplayableCanvasMappaATuttoSchermo(int width, int height, VistaPartita vistaPartita) {
		super(vistaPartita);
		this.width = width;
		this.height = height;
		this.modello = new MappaATuttoSchermo(width, height, vistaPartita, LARGHEZZA_ICONA, ALTEZZA_ICONA);
		this.notiziario = new Notiziario(new StrisciaDelleNotizie(width - LARGHEZZA_SFERA_MAGICA_COPERTA, vistaPartita), ALTEZZA_ICONA);
	}

	public MappaATuttoSchermo getModello() {
		return modello;
	}

	/**
	 * Abilita il joystick virtuale: un disco in basso a destra che, premuto, muove una mano-puntatore al centro della mappa.
	 */
	public void abilitaJoystickVirtuale() {
		modello.abilitaJoystickVirtuale();
	}

	/**
	 * Sposta la mappa in modo che il gruppo sia al centro dello schermo (o, se la mappa è più piccola dello schermo, la centra).
	 */
	public void centraSuGiocatore() {
		modello.centraSuGiocatore();
	}

	/**
	 * Si entra nella schermata della mappa: la mano, la prossima volta, parte dal centro.
	 */
	public void dimenticaLaMano() {
		modello.dimenticaLaMano();
	}

	/**
	 * Il nome della casella sotto il mouse, se il gruppo la conosce e ne ha uno; vedi {@link MappaATuttoSchermo#getNomeSottoIlMouse()}.
	 */
	public String getNomeSottoIlMouse() {
		return modello.getNomeSottoIlMouse();
	}

	List<String> getNomiMissioniSottoIlMouse() {
		return modello.getNomiMissioniSottoIlMouse();
	}

	/**
	 * Disegna la mappa con le caselle conosciute e il gruppo, e il notiziario.
	 */
	public void disegnaMappaATuttoSchermo(Graphics2D graphics) {
		modello.rivalidaLOffset();
		int altezzaMappa = modello.altezzaMappa();
		int xOffset = modello.getMappaXOffset();
		int yOffset = modello.getMappaYOffset();
		int dimensioneMappaX = vistaPartita.getMappa().getDimensioneX() * LARGHEZZA_ICONA;
		int dimensioneMappaY = vistaPartita.getMappa().getDimensioneY() * ALTEZZA_ICONA;

		// Salva lo stato originale della Clip e del Composite
		Shape originalClip = graphics.getClip();
		Composite originalComposite = graphics.getComposite();

		// Limita il disegno della mappa (immagine + nuvole) alla sola fascia sopra il notiziario, così non può mai debordare nella fascia in basso
		graphics.clipRect(0, 0, width, altezzaMappa);

		graphics.drawImage(ottieniMappaGenerale(), xOffset, yOffset, null);

		if (modello.isSegnaliniVisibili()) {
			Punto segnalino = modello.getSegnalino();
			graphics.drawImage(ImageCache.segnalino, segnalino.x, segnalino.y, null);
			// Le caselle segnalate lampeggiano come il segnalino: vanno ridisegnate a ogni frame perché non fanno parte dell'immagine statica della mappa
			for (Punto indicatore : modello.getIndicatori()) {
				graphics.drawImage(ImageCache.indicatore, indicatore.x, indicatore.y, null);
			}
		}

		GestoreNuvole.assicuraGenerate(vistaPartita.getMappa(), width, height, LARGHEZZA_ICONA, ALTEZZA_ICONA);

		// Le nuvole non vengono disegnate al di fuori della mappa quando questa è più piccola dello schermo
		graphics.clipRect(xOffset, yOffset, dimensioneMappaX, dimensioneMappaY);

		// Le nuvole condivise con il riquadro mappa, allineate rispetto alla cella (0,0) della foresta che qui corrisponde sempre all'angolo in alto a
		// sinistra dell'immagine disegnata a (xOffset, yOffset)
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
		NuvoleAwt.disegna(graphics, 0, 0, xOffset, yOffset, LARGHEZZA_ICONA, ALTEZZA_ICONA);

		graphics.setComposite(originalComposite);
		graphics.setClip(originalClip);

		// Aggiorna la posizione delle nuvolette
		GestoreNuvole.aggiorna();

		if (modello.isNotiziarioVisibile()) {
			notiziario.disegna(graphics, LARGHEZZA_SFERA_MAGICA_COPERTA, altezzaMappa);
		}
		disegnaJoystickEMano(graphics);
		Cartiglio.disegnaAccantoAlMouse(graphics, modello.righeDelCartiglio(), modello.getXMouse(), modello.getYMouse(), width, altezzaMappa);
	}

	private void disegnaJoystickEMano(Graphics2D graphics) {
		if (modello.getJoystick() == null) {
			return;
		}
		modello.aggiornaLaMano(System.nanoTime());
		JoystickVirtualeAwt.disegna(graphics, modello.getJoystick());
		if (modello.isManoVisibile()) {
			graphics.drawImage(ImageCache.manoPuntatore, modello.getXMouse() - MappaATuttoSchermo.PUNTA_DELLA_MANO_X,
					modello.getYMouse() - MappaATuttoSchermo.PUNTA_DELLA_MANO_Y, null);
		}
	}

	@Override
	public void processaMovimento(int x, int y) {
		modello.processaMovimento(x, y);
	}

	@Override
	public void processaPressione(int x, int y, Finestra.Tasto tasto) {
		modello.processaPressione(x, y, tasto);
	}

	@Override
	public void processaRilascio(int x, int y, Finestra.Tasto tasto) {
		modello.processaRilascio(x, y, tasto);
	}

	@Override
	public void processaUscita(int x, int y) {
		modello.processaUscita(x, y);
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		modello.processaTrascinamento(x, y);
	}

	@Override
	public boolean catturaIlTrascinamento() {
		return modello.catturaIlTrascinamento();
	}

	@Override
	public boolean gestisceDoppioClick() {
		return modello.gestisceDoppioClick();
	}

	@Override
	public void processaDoppioClick(int x, int y, Finestra.Tasto tasto) {
		modello.processaDoppioClick(x, y, tasto);
	}
}
