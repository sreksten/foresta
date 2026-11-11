package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

/**
 * Che cosa mostra uno sprite a tempo, senza immagini: un'icona (o un'immagine intera già pronta), con a fianco, se c'è, una variazione scritta
 * ("+5" in verde, "-3" in rosso) col contorno nero, che sale e sfuma in poco più di un secondo. Dice dove sta ogni parte dell'immagine composta e da dove
 * parte lo sprite; il movimento e la dissolvenza sono di {@link ModelloDelloSprite}. L'icona è un oggetto di chi disegna (un <code>BufferedImage</code> in
 * Swing, il nome di una regione in libGDX), con la sua misura.
 */
public final class DescrizioneSpriteATempo {

	public static final float DURATA_IN_SECONDI = 1.6f;
	public static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = DURATA_IN_SECONDI / 2;
	public static final float DISTANZA_TOTALE_MOVIMENTO = 10f * DURATA_IN_SECONDI;

	/**
	 * Gli scostamenti, rispetto all'angolo del testo, a cui si disegna il testo nero che fa il contorno: i colorati vanno in (1, 1)
	 */
	private static final int[][] CONTORNO = {{0, 0}, {1, 0}, {2, 0}, {0, 1}, {2, 1}, {0, 2}, {1, 2}, {2, 2}};

	private final String descrizione;
	private final Object icona;
	private final Misura misuraIcona;
	private final String testoVariazione;
	private final DoomdarkFont font;
	private final DoomdarkColorModel.Color colore;
	private int larghezza;
	private int altezza;
	private int yIcona;
	private int xTesto;
	private int yTesto;
	private float xIniziale;
	private float yIniziale;
	private Area ritaglio;
	private float scalaDellIcona = 1f;

	private DescrizioneSpriteATempo(String descrizione, Object icona, Misura misuraIcona, String testoVariazione, DoomdarkFont font,
									DoomdarkColorModel.Color colore) {
		this.descrizione = descrizione;
		this.icona = icona;
		this.misuraIcona = misuraIcona;
		this.testoVariazione = testoVariazione;
		this.font = font;
		this.colore = colore;
	}

	/**
	 * Uno sprite che è solo un'immagine già pronta, ferma in quel punto (l'angolo in alto a sinistra) e poi su
	 */
	public static DescrizioneSpriteATempo immagine(Object immagine, Misura misura, int x, int y, String descrizione) {
		DescrizioneSpriteATempo d = new DescrizioneSpriteATempo(descrizione, immagine, misura, null, null, null);
		d.larghezza = misura.larghezza;
		d.altezza = misura.altezza;
		d.xIniziale = x;
		d.yIniziale = y;
		return d;
	}

	/**
	 * Uno sprite con la variazione scritta, e se c'è l'icona a sinistra; verde se la variazione non è negativa, rosso se lo è
	 *
	 * @param icona          l'icona di chi disegna, o null
	 * @param misuraIcona    la misura dell'icona (null se non c'è)
	 * @param ancoraSinistra se vero (x, y) è l'angolo in alto a sinistra dell'icona e l'immagine si estende verso destra; se falso è il bordo destro del testo
	 */
	public static DescrizioneSpriteATempo conVariazione(Object icona, Misura misuraIcona, int variazione, DoomdarkFont font, int x, int y,
														boolean ancoraSinistra, String descrizione) {
		return conVariazione(icona, misuraIcona, variazione, font, coloreDellaVariazione(variazione), x, y, ancoraSinistra, descrizione);
	}

	public static DescrizioneSpriteATempo conVariazione(Object icona, Misura misuraIcona, int variazione, DoomdarkFont font, DoomdarkColorModel.Color colore,
														int x, int y, boolean ancoraSinistra, String descrizione) {
		DescrizioneSpriteATempo d = new DescrizioneSpriteATempo(descrizione, icona, misuraIcona, testoDellaVariazione(variazione), font, colore);
		d.dispone(x, y, ancoraSinistra);
		return d;
	}

	public static DoomdarkColorModel.Color coloreDellaVariazione(int variazione) {
		return variazione >= 0 ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
	}

	/**
	 * La variazione scritta, col segno più se non è negativa
	 */
	public static String testoDellaVariazione(int variazione) {
		return variazione >= 0 ? "+" + variazione : String.valueOf(variazione);
	}

	private void dispone(int x, int y, boolean ancoraSinistra) {
		// Il testo è alto quanto il font più due righe di contorno, ed è largo quanto la sua scritta (almeno un pixel) più due colonne di contorno
		int larghezzaTesto = Math.max(1, FontTool.getWidth(font, testoVariazione));
		int altezzaTesto = font.getHeight() + 2;
		int yDelTesto;
		if (misuraIcona != null) {
			larghezza = misuraIcona.larghezza + larghezzaTesto + 3;
			xTesto = misuraIcona.larghezza + 1;
			altezza = Math.max(misuraIcona.altezza, altezzaTesto);
			if (misuraIcona.altezza < altezzaTesto) {
				yIcona = (altezzaTesto - misuraIcona.altezza) >> 1;
				yDelTesto = 0;
			} else {
				yIcona = 0;
				yDelTesto = (misuraIcona.altezza - altezzaTesto) >> 1;
			}
		} else {
			larghezza = larghezzaTesto + 2;
			altezza = altezzaTesto;
			xTesto = 0;
			yDelTesto = 0;
			yIcona = 0;
		}
		yTesto = yDelTesto;
		xIniziale = ancoraSinistra ? x : x - larghezza;
		yIniziale = y - yDelTesto;
	}

	public String getDescrizione() {
		return descrizione;
	}

	/**
	 * L'icona, o l'immagine intera se lo sprite è solo quella (null se non c'è)
	 */
	public Object getIcona() {
		return icona;
	}

	/**
	 * Se lo sprite è solo un'immagine già pronta, senza testo
	 */
	public boolean isSoloImmagine() {
		return testoVariazione == null;
	}

	public String getTestoVariazione() {
		return testoVariazione;
	}

	public DoomdarkFont getFont() {
		return font;
	}

	public DoomdarkColorModel.Color getColore() {
		return colore;
	}

	/**
	 * La larghezza dell'immagine composta
	 */
	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
	}

	/**
	 * Dove sta l'icona nell'immagine composta (a sinistra, in alto a quella quota)
	 */
	public int getYIcona() {
		return yIcona;
	}

	public int getXTesto() {
		return xTesto;
	}

	public int getYTesto() {
		return yTesto;
	}

	/**
	 * Gli scostamenti, rispetto a (xTesto, yTesto), a cui va il testo nero che fa il contorno, e che si disegna sotto quello colorato; questo va in (1, 1)
	 */
	public static int[][] getContorno() {
		return CONTORNO;
	}

	/**
	 * Dove parte l'angolo in alto a sinistra dell'immagine composta (sale di {@link #DISTANZA_TOTALE_MOVIMENTO} da lì)
	 */
	public float getXIniziale() {
		return xIniziale;
	}

	public float getYIniziale() {
		return yIniziale;
	}

	/**
	 * Limita il disegno a quest'area, in coordinate dello schermo: lo sprite parte dalla quota giusta anche se non si vede (per esempio perché la lista in cui
	 * sta è scorsa) e si vede solo la parte dentro
	 */
	public DescrizioneSpriteATempo conRitaglio(Area ritaglio) {
		this.ritaglio = ritaglio;
		return this;
	}

	/**
	 * Sposta lo sprite (e il suo ritaglio) di tanti pixel a destra: nasce in un riquadro che chi disegna ha spostato senza che lui lo sappia (la pila
	 * del telefono scorsa di lato)
	 */
	public DescrizioneSpriteATempo spostataInOrizzontale(int dx) {
		xIniziale += dx;
		if (ritaglio != null) {
			ritaglio = new Area(ritaglio.getX() + dx, ritaglio.getY(), ritaglio.getLarghezza(), ritaglio.getAltezza());
		}
		return this;
	}

	/**
	 * Centra lo sprite (icona e testo insieme) su quel punto, spostandolo quanto serve perché non esca dai limiti
	 */
	public DescrizioneSpriteATempo centraEntro(float xCentro, float limiteSinistro, float limiteDestro) {
		xIniziale = DescrizioneSpriteEffetto.centroEntro(xCentro, larghezza, limiteSinistro, limiteDestro) - larghezza / 2.0f;
		return this;
	}

	/**
	 * Di quanto chi disegna ingrandisce l'icona (la sua misura nella descrizione deve già essere quella ingrandita): 1 per le icone a dimensione originale,
	 * lo zoom per le immagini a metà risoluzione
	 */
	public DescrizioneSpriteATempo conScalaDellIcona(float scala) {
		this.scalaDellIcona = scala;
		return this;
	}

	public float getScalaDellIcona() {
		return scalaDellIcona;
	}

	public Area getRitaglio() {
		return ritaglio;
	}
}
