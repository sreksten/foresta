package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkTextRectangle2x;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Il riquadro di testo in cui scorrono le frasi che raccontano la partita.
 */
public class DisplayableCanvasRiquadroTesto implements Finestra {

	// Pixel di scorrimento per ogni passo della rotella (uno scatto fa più passi: circa una riga)
	private static final int PASSO_SCORRIMENTO = 3;

	private final int topLeftX;
	private final int topLeftY;
	private final int width;
	private final int height;
	private final DoomdarkTextRectangle2x doomdarkTextRectangle;
	// L'immagine finita del riquadro (ombra del drago + testo sfumato), rifatta solo
	// quando cambia la versione del testo
	private BufferedImage immagineTesto;
	private int versioneDisegnata;
	// Quanto si è tornati indietro nel testo, ma al contrario: 0 è la riga più vecchia e il massimo la più recente (che
	// è dove si parte), così trascinare verso il basso mostra il testo più vecchio, come negli altri elenchi
	private final ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(System::nanoTime);
	// Un messaggio nuovo (o il testo svuotato) riporta alla riga più recente: lo si fa al prossimo disegno, sul suo thread
	private volatile boolean riportaInFondo = true;

	public DisplayableCanvasRiquadroTesto(int topLeftX, int topLeftY, int width, int height) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.width = width;
		this.height = height;
		doomdarkTextRectangle = new DoomdarkTextRectangle2x(width, height);
	}

	/**
	 * Cancella tutto il testo.
	 */
	public void clear() {
		doomdarkTextRectangle.clear();
		riportaInFondo = true;
	}
	
	/**
	 * Aggiunge una frase in fondo.
	 */
	public void addString(String messaggio) {
		doomdarkTextRectangle.addString(messaggio);
		riportaInFondo = true;
	}

	@Override
	public int passiPerScattoDellaRotella() {
		return PASSI_PER_SCATTO_DELLE_LISTE;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		int pixel = numeroRotazioni * PASSO_SCORRIMENTO;
		// Su, verso il testo più vecchio, è verso l'inizio dello scorrimento
		scorrimento.scorriDi(movimentoRotella == MovimentoRotella.SU ? -pixel : pixel);
	}

	/**
	 * Il gesto che comincia sul riquadro prosegue anche se il puntatore ne esce, finché non si lascia
	 */
	@Override
	public boolean catturaIlTrascinamento() {
		return true;
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.premi(y, true);
		}
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		scorrimento.trascina(y);
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.rilascia();
		}
	}

	/**
	 * Porta il testo dov'è lo scorrimento: aggiorna limite e movimento, e rimette il testo alla riga più recente se è
	 * arrivato qualcosa di nuovo.
	 */
	private void sincronizzaScorrimento() {
		int massimo = doomdarkTextRectangle.getScorrimentoMassimo();
		scorrimento.imposta(massimo > 0, massimo);
		if (riportaInFondo) {
			riportaInFondo = false;
			scorrimento.reimposta();
			scorrimento.scorriDi(massimo);
		}
		scorrimento.aggiorna();
		doomdarkTextRectangle.impostaOffset(massimo - (int) Math.round(scorrimento.getScorrimento()));
	}

	/**
	 * Disegna le ultime frasi che entrano nel riquadro.
	 */
	public void disegnaTesto(Graphics2D graphics) {
		sincronizzaScorrimento();
		int versione = doomdarkTextRectangle.getVersione();
		if (immagineTesto == null || versione != versioneDisegnata) {
			Image raster = Toolkit.getDefaultToolkit().createImage(doomdarkTextRectangle.getImageSource());
			immagineTesto = componi(raster);
			versioneDisegnata = versione;
		}
		graphics.drawImage(immagineTesto, topLeftX, topLeftY, null);

		final int SPACING = ImageCache.SPACING;
		if (doomdarkTextRectangle.isScorribileSu()) {
			Image frecciaSu = ImageCache.componenteScorrevoleFrecciaSu;
			graphics.drawImage(frecciaSu, topLeftX + width - frecciaSu.getWidth(null) - SPACING, topLeftY + SPACING, null);
		}
		if (doomdarkTextRectangle.isScorribileGiu()) {
			Image frecciaGiu = ImageCache.componenteScorrevoleFrecciaGiu;
			graphics.drawImage(frecciaGiu, topLeftX + width - frecciaGiu.getWidth(null) - SPACING,
					topLeftY + height - frecciaGiu.getHeight(null) - SPACING, null);
		}
	}

	/**
	 * Compone il raster del testo sull'ombra del drago, schiarendo le righe dall'alto
	 * verso il basso così che il testo più vecchio sfumi.
	 */
	private BufferedImage componi(Image image) {

		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);

		final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

		BufferedImage background = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.OPAQUE);
		Graphics2D backgroundG2d = background.createGraphics();
		float transparency = (float)0.3;
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
		backgroundG2d.setComposite(ac);
		backgroundG2d.drawImage(ImageCache.ombraDelDrago, imageWidth - ImageCache.ombraDelDrago.getWidth() >> 1, imageHeight - ImageCache.ombraDelDrago.getHeight() >> 1, null);

		backgroundG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

		BufferedImage copy = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.TRANSLUCENT);
		Graphics2D g2d = copy.createGraphics();		
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		int vertLimit = Math.min(imageHeight, 256);
		for (int y = 0; y < vertLimit; y++) {
			int mask = (y << 16) + (y << 8) + y;
			for (int x = 0; x < imageWidth; x++) {
				int argb = copy.getRGB(x, y);
				int alfa = argb >>> 24;
				if (alfa != 0 && (argb & 0x00FFFFFF) != 0) {
					// La sfumatura dall'alto scurisce il colore e lascia l'alfa del glifo, così i bordi restano sfumati
					copy.setRGB(x, y, (alfa << 24) | (argb & mask));
				} else {
					copy.setRGB(x, y, 0x00000000);
				}
			}
		}

		backgroundG2d.drawImage(copy, 0, 0, null);
		backgroundG2d.dispose();

		return background;
	}

}
