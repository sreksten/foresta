package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.ImageCache;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Un'immagine che scorre in verticale dentro un'area, che ne mostra solo la parte che ci sta, nella versione Swing: dove sta l'immagine, quando arriva e
 * quando esce, il trascinamento, l'inerzia e le frecce sono di {@link ColonnaScorrevole} (nel modulo comune, vedi lì i due modi: automatico, che sfuma ai
 * bordi, e manuale); qui resta il disegno. L'immagine si disegna centrata in orizzontale nell'area.
 */
public final class ScorrimentoVerticale {

	private final BufferedImage contenuto;
	private final Rectangle area;
	private final int fascia;
	private final ColonnaScorrevole colonna;
	// Riusata a ogni disegno: ci si compone l'immagine con le sue dissolvenze
	private BufferedImage tela;

	/**
	 * Lo scorrimento manuale: l'immagine parte con la cima in cima all'area, senza dissolvenze.
	 */
	public ScorrimentoVerticale(BufferedImage contenuto, Rectangle area) {
		this(contenuto, area, 0, new ColonnaScorrevole(comune(area), contenuto.getHeight()));
	}

	/**
	 * Lo scorrimento manuale di una colonna già fatta (di chi la tiene, come la pagina dei trofei): l'immagine è il suo contenuto
	 */
	public ScorrimentoVerticale(BufferedImage contenuto, ColonnaScorrevole colonna) {
		this(contenuto, colonna, 0);
	}

	/**
	 * Come sopra, con le dissolvenze ai bordi alte quella fascia (lo scorrimento automatico dell'intro)
	 */
	public ScorrimentoVerticale(BufferedImage contenuto, ColonnaScorrevole colonna, int fascia) {
		this(contenuto, new Rectangle(colonna.getArea().getX(), colonna.getArea().getY(), colonna.getArea().getLarghezza(), colonna.getArea().getAltezza()),
				fascia, colonna);
	}

	private ScorrimentoVerticale(BufferedImage contenuto, Rectangle area, int fascia, ColonnaScorrevole colonna) {
		this.contenuto = contenuto;
		this.area = new Rectangle(area);
		this.fascia = fascia;
		this.colonna = colonna;
	}

	private static Area comune(Rectangle area) {
		return new Area(area.x, area.y, area.width, area.height);
	}

	/**
	 * Lo scorrimento automatico: l'immagine parte appena sotto l'area e sale, sfumando nella fascia ai bordi.
	 */
	public static ScorrimentoVerticale dalBasso(BufferedImage contenuto, Rectangle area, int fascia) {
		return new ScorrimentoVerticale(contenuto, area, fascia, ColonnaScorrevole.dalBasso(comune(area), contenuto.getHeight(), fascia));
	}

	/**
	 * Salendo, l'immagine si ferma quando la sua cima arriva a quella quota dalla cima dell'area.
	 */
	public ScorrimentoVerticale fermaA(int quota) {
		colonna.fermaA(quota);
		return this;
	}

	public void avanza(double secondi, double velocita) {
		colonna.avanza(secondi, velocita);
	}

	public boolean isArrivato() {
		return colonna.isArrivato();
	}

	public boolean isUscito() {
		return colonna.isUscito();
	}

	public int getFondo() {
		return colonna.getFondo();
	}

	public void scorri(int pixel) {
		colonna.scorri(pixel);
	}

	public void premi(int quota) {
		colonna.premi(quota);
	}

	public void trascina(int quota) {
		colonna.trascina(quota);
	}

	public void rilascia() {
		colonna.rilascia();
	}

	public boolean isGestoTrascinato() {
		return colonna.isGestoTrascinato();
	}

	public boolean isInMovimento() {
		return colonna.isInMovimento();
	}

	public int getScorrimento() {
		return colonna.getScorrimento();
	}

	public boolean contiene(int x, int y) {
		return colonna.contiene(x, y);
	}

	public int getCima() {
		return colonna.getCima();
	}

	boolean haAltroSopra() {
		return colonna.haAltroSopra();
	}

	boolean haAltroSotto() {
		return colonna.haAltroSotto();
	}

	/**
	 * Disegna la parte dell'immagine che sta nell'area, con le dissolvenze ai bordi e quell'opacità (da 0 a 1).
	 */
	public void disegna(Graphics2D graphics, float opacita) {
		colonna.aggiorna();
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
			g.drawImage(contenuto, (area.width - contenuto.getWidth()) / 2, (int) Math.round(colonna.getY()), null);
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

	private static Misura misura(Image freccia) {
		return new Misura(freccia.getWidth(null), freccia.getHeight(null));
	}

	/**
	 * Le frecce, a destra in alto e in basso nell'area, quando sopra o sotto c'è altro (come nel riquadro delle missioni).
	 */
	public void disegnaFrecce(Graphics2D graphics) {
		if (haAltroSopra()) {
			Image freccia = ImageCache.componenteScorrevoleFrecciaSu;
			Area a = colonna.areaDellaFrecciaSu(misura(freccia));
			graphics.drawImage(freccia, a.getX(), a.getY(), null);
		}
		if (haAltroSotto()) {
			Image freccia = ImageCache.componenteScorrevoleFrecciaGiu;
			Area a = colonna.areaDellaFrecciaGiu(misura(freccia));
			graphics.drawImage(freccia, a.getX(), a.getY(), null);
		}
	}

	/**
	 * Se il punto (nelle coordinate di chi disegna) è sulla freccia verso l'alto.
	 */
	public boolean isSullaFrecciaSu(int x, int y) {
		return colonna.isSullaFrecciaSu(x, y, misura(ImageCache.componenteScorrevoleFrecciaSu));
	}

	/**
	 * Se il punto (nelle coordinate di chi disegna) è sulla freccia verso il basso.
	 */
	public boolean isSullaFrecciaGiu(int x, int y) {
		return colonna.isSullaFrecciaGiu(x, y, misura(ImageCache.componenteScorrevoleFrecciaGiu));
	}
}
