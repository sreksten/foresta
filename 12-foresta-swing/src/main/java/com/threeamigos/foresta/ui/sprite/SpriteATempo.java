package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Un'icona, con eventualmente una variazione scritta a fianco (per esempio "+5" in verde o "-3" in rosso), che sale e sfuma in poco più di un secondo: per segnalare un cambiamento di
 * salute, magia, monete e simili sopra l'elemento che lo riguarda. Che cosa mostra e dove sta ogni parte lo dice {@link DescrizioneSpriteATempo}, nel modulo comune: qui c'è il disegno.
 */
public class SpriteATempo extends SpriteBase {

	private final DescrizioneSpriteATempo dati;
	// L'icona come immagine: la descrizione la dà come BufferedImage oppure come nome della risorsa (se viene dai riquadri del modulo comune)
	private final BufferedImage icona;

	/**
	 * Uno sprite che mostra quel che dice la descrizione, la cui icona è un <code>BufferedImage</code>
	 */
	public SpriteATempo(DescrizioneSpriteATempo dati) {
		this.dati = dati;
		this.icona = immagineDellIcona(dati.getIcona(), dati.getScalaDellIcona());
		completaCostruzione();
		Area ritaglio = dati.getRitaglio();
		if (ritaglio != null) {
			setRitaglio(new Rectangle(ritaglio.getX(), ritaglio.getY(), ritaglio.getLarghezza(), ritaglio.getAltezza()));
		}
	}

	public SpriteATempo(BufferedImage icona, int xIniziale, int yIniziale, String descrizione) {
		this(DescrizioneSpriteATempo.immagine(icona, misura(icona), xIniziale, yIniziale, descrizione));
	}

	public SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, int xIniziale, int yIniziale, String descrizione) {
		this(icona, variazione, font, xIniziale, yIniziale, false, descrizione);
	}

	/**
	 * @param ancoraSinistra se {@code true}, {@code xIniziale} è il bordo sinistro dell'icona
	 *                       (l'immagine composta icona+testo si estende verso destra); se
	 *                       {@code false} (comportamento storico) è il bordo destro del testo
	 *                       (l'immagine composta si estende verso sinistra).
	 */
	public SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, int xIniziale, int yIniziale,
				 boolean ancoraSinistra, String descrizione) {
		this(DescrizioneSpriteATempo.conVariazione(icona, misura(icona), variazione, font, xIniziale, yIniziale, ancoraSinistra, descrizione));
	}

	public SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, DoomdarkColorModel.Color color,
				 int xIniziale, int yIniziale, String descrizione) {
		this(icona, variazione, font, color, xIniziale, yIniziale, false, descrizione);
	}

	public SpriteATempo(BufferedImage icona, int variazione, DoomdarkFont font, DoomdarkColorModel.Color color,
				 int xIniziale, int yIniziale, boolean ancoraSinistra, String descrizione) {
		this(DescrizioneSpriteATempo.conVariazione(icona, misura(icona), variazione, font, color, xIniziale, yIniziale, ancoraSinistra, descrizione));
	}

	/**
	 * La descrizione di uno sprite che è solo quell'immagine (vedi i costruttori con gli stessi argomenti)
	 */
	public static DescrizioneSpriteATempo descrizione(BufferedImage icona, int xIniziale, int yIniziale, String descrizione) {
		return DescrizioneSpriteATempo.immagine(icona, misura(icona), xIniziale, yIniziale, descrizione);
	}

	/**
	 * La descrizione di uno sprite con la variazione scritta, col colore verde o rosso secondo il segno
	 */
	public static DescrizioneSpriteATempo descrizione(BufferedImage icona, int variazione, DoomdarkFont font, int xIniziale, int yIniziale, String descrizione) {
		return DescrizioneSpriteATempo.conVariazione(icona, misura(icona), variazione, font, xIniziale, yIniziale, false, descrizione);
	}

	/**
	 * La descrizione di uno sprite con la variazione scritta di quel colore
	 */
	public static DescrizioneSpriteATempo descrizione(BufferedImage icona, int variazione, DoomdarkFont font, DoomdarkColorModel.Color color,
													  int xIniziale, int yIniziale, String descrizione) {
		return DescrizioneSpriteATempo.conVariazione(icona, misura(icona), variazione, font, color, xIniziale, yIniziale, false, descrizione);
	}

	private static BufferedImage immagineDellIcona(Object icona, float scala) {
		if (icona instanceof String) {
			return BufferedImageBuilder.buildBufferedImage((String) icona, scala);
		}
		return (BufferedImage) icona;
	}

	private static Misura misura(BufferedImage immagine) {
		return immagine != null ? new Misura(immagine.getWidth(), immagine.getHeight()) : null;
	}

	private void completaCostruzione() {
		BufferedImage immagineCostruita = buildImage();
		inizializza(immagineCostruita,
				DescrizioneSpriteATempo.DURATA_IN_SECONDI, DescrizioneSpriteATempo.ATTACCO_DISSOLVENZA_DOPO_SECONDI, Dissolvenza.IPERBOLICA_CON_SOGLIA,
				null, 0,
				dati.getXIniziale(), dati.getYIniziale(),
				dati.getXIniziale(), dati.getYIniziale() - DescrizioneSpriteATempo.DISTANZA_TOTALE_MOVIMENTO,
				1f, 1f);
	}

	public String getDescrizione() {
		return dati.getDescrizione();
	}

	/**
	 * Centra lo sprite (icona e testo insieme) su quel punto, spostandolo quanto serve perché non esca dai limiti.
	 */
	public void centraEntro(float xCentro, float limiteSinistro, float limiteDestro) {
		float larghezza = immagine.getWidth();
		impostaX(DescrizioneSpriteEffetto.centroEntro(xCentro, larghezza, limiteSinistro, limiteDestro) - larghezza / 2.0f);
	}

	@Override
	protected BufferedImage buildImage() {
		if (dati.isSoloImmagine()) {
			return icona;
		}

		Image doomdark = ImageCache.get(dati.getTestoVariazione(), dati.getFont(), dati.getColore());
		Image blackDoomdark = ImageCache.get(dati.getTestoVariazione(), dati.getFont(), DoomdarkColorModel.Color.BLACK);

		final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

		BufferedImage image = gc.createCompatibleImage(dati.getLarghezza(), dati.getAltezza(), Transparency.TRANSLUCENT);
		Graphics2D g2d = image.createGraphics();
		if (icona != null) {
			g2d.drawImage(icona, 0, dati.getYIcona(), null);
		}
		for (int[] scostamento : DescrizioneSpriteATempo.getContorno()) {
			g2d.drawImage(blackDoomdark, dati.getXTesto() + scostamento[0], dati.getYTesto() + scostamento[1], null);
		}
		g2d.drawImage(doomdark, dati.getXTesto() + 1, dati.getYTesto() + 1, null);
		g2d.dispose();
		return image;
	}

	@Override
	public void disegna(Graphics2D g, float x, float y, float scala) {
		g.drawImage(immagine, Math.round(x), Math.round(y), null);
	}
}
