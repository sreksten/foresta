package com.threeamigos.foresta.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Un'immagine che scorre in verticale dentro un'area, che ne mostra solo la parte che ci sta. Si usa in due modi:
 * <ul>
 * <li>automatico ({@link #avanza}): l'immagine sale a velocità costante, partendo da sotto l'area
 * ({@link #dalBasso}), finché esce in alto o si ferma a una quota ({@link #fermaA}); ai bordi dell'area, in una fascia
 * alta {@code fascia} pixel, sfuma, così compare e scompare in dissolvenza. Così scorrono la storia, la classifica e
 * i trofei dell'intro;</li>
 * <li>manuale ({@link #scorri}): l'immagine parte con la cima in cima all'area e si sposta di quanto chiede chi la usa
 * (la rotella del mouse, le frecce), senza uscire dall'area più del necessario. Le frecce ({@link #disegnaFrecce})
 * dicono se c'è altro sopra o sotto. Così scorre la pagina dei trofei.</li>
 * </ul>
 * L'immagine si disegna centrata in orizzontale nell'area.
 */
final class ScorrimentoVerticale {

	private static final int MARGINE_FRECCE = 4;

	private final BufferedImage contenuto;
	private final Rectangle area;
	private final int fascia;
	// Dov'è la cima dell'immagine rispetto alla cima dell'area: positiva sotto, negativa sopra
	private double y;
	private Integer quotaDiArrivo;
	// Riusata a ogni disegno: ci si compone l'immagine con le sue dissolvenze
	private BufferedImage tela;

	/**
	 * Lo scorrimento manuale: l'immagine parte con la cima in cima all'area, senza dissolvenze.
	 */
	ScorrimentoVerticale(BufferedImage contenuto, Rectangle area) {
		this(contenuto, area, 0, 0);
	}

	private ScorrimentoVerticale(BufferedImage contenuto, Rectangle area, int fascia, double y) {
		this.contenuto = contenuto;
		this.area = new Rectangle(area);
		this.fascia = fascia;
		this.y = y;
	}

	/**
	 * Lo scorrimento automatico: l'immagine parte appena sotto l'area e sale, sfumando nella fascia ai bordi.
	 */
	static ScorrimentoVerticale dalBasso(BufferedImage contenuto, Rectangle area, int fascia) {
		return new ScorrimentoVerticale(contenuto, area, fascia, area.height);
	}

	/**
	 * Salendo, l'immagine si ferma quando la sua cima arriva a quella quota dalla cima dell'area.
	 */
	ScorrimentoVerticale fermaA(int quota) {
		quotaDiArrivo = quota;
		return this;
	}

	/**
	 * Fa salire l'immagine per quei secondi, a quella velocità in pixel al secondo.
	 */
	void avanza(double secondi, double velocita) {
		y -= secondi * velocita;
		if (quotaDiArrivo != null && y < quotaDiArrivo) {
			y = quotaDiArrivo;
		}
	}

	/**
	 * Se l'immagine si è fermata alla sua quota.
	 */
	boolean isArrivato() {
		return quotaDiArrivo != null && y <= quotaDiArrivo;
	}

	/**
	 * Se l'immagine è uscita tutta dalla cima dell'area.
	 */
	boolean isUscito() {
		return y + contenuto.getHeight() <= 0;
	}

	/**
	 * Dove sta il fondo dell'immagine, nelle coordinate di chi disegna.
	 */
	int getFondo() {
		return area.y + (int) Math.round(y) + contenuto.getHeight();
	}

	/**
	 * Sposta l'immagine di quei pixel (positivi per vedere quello che c'è sotto), senza staccarne la cima dalla cima
	 * dell'area né il fondo dal fondo.
	 */
	void scorri(int pixel) {
		y = Math.max(Math.min(0, area.height - contenuto.getHeight()), Math.min(0, y - pixel));
	}

	/**
	 * Se sopra l'area c'è ancora un pezzo d'immagine.
	 */
	boolean haAltroSopra() {
		return y < 0;
	}

	/**
	 * Se sotto l'area c'è ancora un pezzo d'immagine.
	 */
	boolean haAltroSotto() {
		return y + contenuto.getHeight() > area.height;
	}

	/**
	 * Disegna la parte dell'immagine che sta nell'area, con le dissolvenze ai bordi e quell'opacità (da 0 a 1).
	 */
	void disegna(Graphics2D graphics, float opacita) {
		if (opacita <= 0f || area.width <= 0 || area.height <= 0) {
			return;
		}
		if (tela == null) {
			tela = new BufferedImage(area.width, area.height, BufferedImage.TYPE_INT_ARGB);
		}
		Graphics2D g = tela.createGraphics();
		try {
			g.setComposite(AlphaComposite.Clear);
			g.fillRect(0, 0, area.width, area.height);
			g.setComposite(AlphaComposite.SrcOver);
			g.drawImage(contenuto, (area.width - contenuto.getWidth()) / 2, (int) Math.round(y), null);
			if (fascia > 0) {
				// Si tiene dell'immagine solo quanto dice la maschera: trasparente al bordo, piena dentro
				g.setComposite(AlphaComposite.DstIn);
				g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0), 0, fascia, Color.BLACK));
				g.fillRect(0, 0, area.width, fascia);
				g.setPaint(new GradientPaint(0, area.height - fascia, Color.BLACK, 0, area.height, new Color(0, 0, 0, 0)));
				g.fillRect(0, area.height - fascia, area.width, fascia);
			}
		} finally {
			g.dispose();
		}
		Composite composito = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, opacita)));
		graphics.drawImage(tela, area.x, area.y, null);
		graphics.setComposite(composito);
	}

	/**
	 * Le frecce, a destra in alto e in basso nell'area, quando sopra o sotto c'è altro (come nel riquadro delle
	 * missioni).
	 */
	void disegnaFrecce(Graphics2D graphics) {
		if (haAltroSopra()) {
			Image freccia = ImageCache.componenteScorrevoleFrecciaSu;
			graphics.drawImage(freccia, xFrecce(freccia), area.y + MARGINE_FRECCE, null);
		}
		if (haAltroSotto()) {
			Image freccia = ImageCache.componenteScorrevoleFrecciaGiu;
			graphics.drawImage(freccia, xFrecce(freccia), area.y + area.height - freccia.getHeight(null) - MARGINE_FRECCE, null);
		}
	}

	/**
	 * Se il punto (nelle coordinate di chi disegna) è sulla freccia verso l'alto.
	 */
	boolean isSullaFrecciaSu(int x, int y) {
		Image freccia = ImageCache.componenteScorrevoleFrecciaSu;
		return haAltroSopra() && new Rectangle(xFrecce(freccia), area.y + MARGINE_FRECCE,
				freccia.getWidth(null), freccia.getHeight(null)).contains(x, y);
	}

	/**
	 * Se il punto (nelle coordinate di chi disegna) è sulla freccia verso il basso.
	 */
	boolean isSullaFrecciaGiu(int x, int y) {
		Image freccia = ImageCache.componenteScorrevoleFrecciaGiu;
		return haAltroSotto() && new Rectangle(xFrecce(freccia), area.y + area.height - freccia.getHeight(null) - MARGINE_FRECCE,
				freccia.getWidth(null), freccia.getHeight(null)).contains(x, y);
	}

	private int xFrecce(Image freccia) {
		return area.x + area.width - freccia.getWidth(null) - MARGINE_FRECCE;
	}
}
