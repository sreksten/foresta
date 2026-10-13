package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Il riquadro della sfida a carta, forbici e sasso (vedi carta_forbici_sasso.md): la cornice larga
 * ({@link ImageCache#corniceLarga}) come sfondo e, sopra, le mani delle due mosse affiancate, a sinistra quella del
 * giocatore e a destra quella dell'avversario, e in alto il punteggio di ognuno, scritto con TestoGrande con l'alone.
 * <p>
 * Mentre il giocatore sceglie, le due mani (due sassi) si muovono come nel "bim bum bam": su e giù e avanti e indietro,
 * con due sinusoidi piccole attorno a una posizione spostata verso i bordi della cornice, ritagliate allo spazio dentro il bordo nero ({@link #BORDO_DELLA_CORNICE}).
 * Quando il giocatore ha scelto le mani si fermano al centro e mostrano le due mosse per
 * {@link #SECONDI_DI_PAUSA_DOPO_UNA_MANO}, poi riprendono a muoversi. Il riquadro sta al centro dell'area di contenuto e
 * occupa al massimo la metà della sua larghezza e la metà della sua altezza (vedi {@link #fattoreDiScala}): finita la
 * sfida diventa uno sprite che si dissolve (vedi {@link DisplayableCanvas#dissolviLaSfida}).
 */
class DisplayableCanvasRiquadroSfida implements Finestra {

	/**
	 * La parte massima dell'area di contenuto, in larghezza e in altezza, che il riquadro può occupare.
	 */
	static final double PARTE_MASSIMA_DELLO_SCHERMO = 0.5;
	/**
	 * Lo spessore del bordo interno nero della cornice larga, da tutti i lati: le mani stanno, e vengono ritagliate,
	 * dentro quello che resta.
	 */
	static final int BORDO_DELLA_CORNICE = 20;
	/**
	 * Quanto restano ferme le mani a mostrare le due mosse prima di riprendere a muoversi.
	 */
	static final double SECONDI_DI_PAUSA_DOPO_UNA_MANO = 0.5;
	// Quanto si spostano le mani al massimo, in parte dello spazio dentro la cornice: avanti e indietro (verso i lati) e su e giù
	static final double AMPIEZZA_ORIZZONTALE = 0.04;
	static final double AMPIEZZA_VERTICALE = 0.04;
	// Le due sinusoidi, in oscillazioni al secondo: su e giù va a velocità doppia rispetto ad avanti e indietro
	static final double FREQUENZA_ORIZZONTALE = 1.0;
	static final double FREQUENZA_VERTICALE = 2.0;
	// Mentre si muovono le mani stanno più vicine al bordo della cornice di quando sono ferme al centro: ognuna verso il
	// suo lato, fino al margine che c'è fra la mano e il bordo e un po' oltre (questa parte dello spazio dentro la cornice). Il ritaglio
	// nasconde quello che esce dal bordo nero, quindi sulla cornice non si vede niente
	static final double SPOSTAMENTO_VERSO_IL_BORDO = 0.02;
	// Il passaggio dal centro alla posizione verso il bordo, a inizio movimento, per non fare uno scatto
	static final double SECONDI_PER_ANDARE_VERSO_IL_BORDO = 0.8;
	private static final int RAGGIO_ALONE = 6;
	// Il punteggio sta in alto, a questa distanza dal bordo interno della cornice (a grandezza naturale)
	private static final int MARGINE_PUNTEGGIO = 8;

	private final int larghezzaContenuto;
	private final int altezzaContenuto;
	// Quello che si registra nel canvas per il mouse: vuoto finché non si disegna per la prima volta, perché le immagini
	// si caricano solo allora
	private final Rectangle rettangolo = new Rectangle();
	private final Map<MossaCartaForbiciSasso, BufferedImage> maniSinistre = new EnumMap<>(MossaCartaForbiciSasso.class);
	private final Map<MossaCartaForbiciSasso, BufferedImage> maniDestre = new EnumMap<>(MossaCartaForbiciSasso.class);
	private final Map<Integer, BufferedImage> punteggi = new HashMap<>();

	private boolean dimensionato;
	// Tutto qui sotto è alla grandezza di schermo, dentro il riquadro (l'origine è il suo angolo in alto a sinistra)
	private double scala;
	private BufferedImage cornice;
	private Rectangle spazio;
	private int xManoSinistra;
	private int xManoDestra;
	private int yMani;
	private int larghezzaManoSinistra;
	private int larghezzaManoDestra;
	private int ampiezzaX;
	private int ampiezzaY;
	private int spostamentoVersoIlBordo;

	private boolean visibile;
	private MossaCartaForbiciSasso mossaDelGiocatore = MossaCartaForbiciSasso.SASSO;
	private MossaCartaForbiciSasso mossaDellAvversario = MossaCartaForbiciSasso.SASSO;
	private int punteggioDelGiocatore;
	private int punteggioDellAvversario;
	// Quando le mani ripartono (il movimento comincia da ferme), e fino a quando mostrano le mosse scelte
	private long inizioMovimentoNanos;
	private long fermeFinoANanos;

	DisplayableCanvasRiquadroSfida(int larghezzaContenuto, int altezzaContenuto) {
		this.larghezzaContenuto = larghezzaContenuto;
		this.altezzaContenuto = altezzaContenuto;
	}

	/**
	 * Di quanto si riducono le immagini perché le due mani affiancate, grandi {@code larghezzaTotale} per
	 * {@code altezzaMassima}, non occupino più di metà dell'area di contenuto in larghezza e in altezza. Non si
	 * ingrandiscono mai.
	 */
	static double fattoreDiScala(int larghezzaContenuto, int altezzaContenuto, int larghezzaTotale, int altezzaMassima) {
		double perLaLarghezza = PARTE_MASSIMA_DELLO_SCHERMO * larghezzaContenuto / Math.max(1, larghezzaTotale);
		double perLAltezza = PARTE_MASSIMA_DELLO_SCHERMO * altezzaContenuto / Math.max(1, altezzaMassima);
		return Math.min(1.0, Math.min(perLaLarghezza, perLAltezza));
	}

	/**
	 * Lo spostamento orizzontale delle mani dopo quel tempo dall'inizio del movimento, da -ampiezza a +ampiezza: una
	 * sinusoide che parte da zero.
	 */
	static double spostamentoOrizzontale(double secondi, double ampiezza) {
		return ampiezza * Math.sin(2 * Math.PI * FREQUENZA_ORIZZONTALE * secondi);
	}

	/**
	 * Quanto delle mani è già andato dal centro alla posizione verso il bordo, da 0 a 1, dopo quel tempo dall'inizio del
	 * movimento: una salita dolce, perché le mani non scattino.
	 */
	static double avvicinamentoAlBordo(double secondi) {
		double t = Math.max(0, Math.min(1, secondi / SECONDI_PER_ANDARE_VERSO_IL_BORDO));
		return t * t * (3 - 2 * t);
	}

	/**
	 * Lo spostamento verticale delle mani, con la sua sinusoide.
	 */
	static double spostamentoVerticale(double secondi, double ampiezza) {
		return ampiezza * Math.sin(2 * Math.PI * FREQUENZA_VERTICALE * secondi);
	}

	Rectangle getRettangolo() {
		return rettangolo;
	}

	@Override
	public boolean isVisibile() {
		return visibile;
	}

	void setVisibile(boolean visibile) {
		this.visibile = visibile;
	}

	/**
	 * La sfida comincia ({@code inizio} vero: i due sassi si muovono) o si è giocata una mano (le mani si fermano a
	 * mostrare le due mosse, e dopo la pausa riprendono a muoversi con il punteggio nuovo).
	 */
	void aggiorna(MossaCartaForbiciSasso mossaDelGiocatore, MossaCartaForbiciSasso mossaDellAvversario,
				  int punteggioDelGiocatore, int punteggioDellAvversario, boolean inizio) {
		this.mossaDelGiocatore = mossaDelGiocatore;
		this.mossaDellAvversario = mossaDellAvversario;
		this.punteggioDelGiocatore = punteggioDelGiocatore;
		this.punteggioDellAvversario = punteggioDellAvversario;
		long ora = System.nanoTime();
		if (inizio) {
			fermeFinoANanos = ora;
		} else {
			fermeFinoANanos = ora + (long) (SECONDI_DI_PAUSA_DOPO_UNA_MANO * 1_000_000_000L);
		}
		inizioMovimentoNanos = fermeFinoANanos;
		this.visibile = true;
	}

	void disegna(Graphics2D graphics) {
		if (!visibile) {
			return;
		}
		dimensiona();
		long ora = System.nanoTime();
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.translate(rettangolo.x, rettangolo.y);
			g.drawImage(cornice, 0, 0, null);
			if (ora < fermeFinoANanos) {
				disegnaLeMani(g, mossaDelGiocatore, mossaDellAvversario, 0, 0);
			} else {
				double secondi = (ora - inizioMovimentoNanos) / 1_000_000_000.0;
				disegnaLeMani(g, MossaCartaForbiciSasso.SASSO, MossaCartaForbiciSasso.SASSO,
						spostamentoOrizzontale(secondi, ampiezzaX) - avvicinamentoAlBordo(secondi) * spostamentoVersoIlBordo,
						spostamentoVerticale(secondi, ampiezzaY));
			}
			scriviIPunteggi(g);
		} finally {
			g.dispose();
		}
	}

	/**
	 * Il riquadro fermo, con le mosse dell'ultima mano, in un'immagine sola: per lo sprite che lo dissolve.
	 */
	BufferedImage inUnImmagine() {
		dimensiona();
		BufferedImage immagine = new BufferedImage(rettangolo.width, rettangolo.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		try {
			g.drawImage(cornice, 0, 0, null);
			disegnaLeMani(g, mossaDelGiocatore, mossaDellAvversario, 0, 0);
			scriviIPunteggi(g);
		} finally {
			g.dispose();
		}
		return immagine;
	}

	/**
	 * Il punto in alto a sinistra del riquadro nell'area di contenuto.
	 */
	int getX() {
		dimensiona();
		return rettangolo.x;
	}

	int getY() {
		dimensiona();
		return rettangolo.y;
	}

	/**
	 * Le due mani, sopra la cornice, ritagliate dentro il bordo nero: la sinistra si sposta di {@code dx} (negativo verso
	 * il suo bordo) e la destra del contrario (si avvicinano e si allontanano), tutte e due di {@code dy} (su e giù
	 * insieme).
	 */
	private void disegnaLeMani(Graphics2D graphics, MossaCartaForbiciSasso sinistra, MossaCartaForbiciSasso destra,
							   double dx, double dy) {
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.clipRect(spazio.x, spazio.y, spazio.width, spazio.height);
			g.drawImage(maniSinistre.get(sinistra), (int) Math.round(xManoSinistra + dx), (int) Math.round(yMani + dy), null);
			g.drawImage(maniDestre.get(destra), (int) Math.round(xManoDestra - dx), (int) Math.round(yMani + dy), null);
		} finally {
			g.dispose();
		}
	}

	private void scriviIPunteggi(Graphics2D graphics) {
		scriviIlPunteggio(graphics, punteggioDelGiocatore, xManoSinistra, larghezzaManoSinistra);
		scriviIlPunteggio(graphics, punteggioDellAvversario, xManoDestra, larghezzaManoDestra);
	}

	private void scriviIlPunteggio(Graphics2D graphics, int punteggio, int xMano, int larghezzaMano) {
		BufferedImage numero = punteggi.computeIfAbsent(punteggio,
				p -> TestoGrande.conAlone(TestoGrande.immagine(String.valueOf(p), Math.max(1, larghezzaMano), true), RAGGIO_ALONE));
		int y = spazio.y + (int) Math.round(MARGINE_PUNTEGGIO * scala);
		graphics.drawImage(numero, xMano + (larghezzaMano - numero.getWidth()) / 2, y, null);
	}

	/**
	 * La prima volta si caricano la cornice e le sei mani, si calcola di quanto va ridotto tutto perché il riquadro stia
	 * entro metà schermo, si dispongono le mani al centro dello spazio dentro il bordo e si centra il riquadro.
	 */
	private void dimensiona() {
		if (dimensionato) {
			return;
		}
		BufferedImage corniceOriginale = ImageCache.corniceLarga;
		scala = fattoreDiScala(larghezzaContenuto, altezzaContenuto, corniceOriginale.getWidth(), corniceOriginale.getHeight());
		cornice = scalata(corniceOriginale, scala);
		int bordo = (int) Math.round(BORDO_DELLA_CORNICE * scala);
		spazio = new Rectangle(bordo, bordo, cornice.getWidth() - 2 * bordo, cornice.getHeight() - 2 * bordo);

		int sinistra = 0;
		int destra = 0;
		int alto = 0;
		for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
			BufferedImage sx = BufferedImageBuilder.buildBufferedImage("fondi/" + nomeDellaMano(mossa) + "-sx.gif", LivelloDiZoom.valore());
			BufferedImage dx = BufferedImageBuilder.buildBufferedImage("fondi/" + nomeDellaMano(mossa) + "-dx.gif", LivelloDiZoom.valore());
			maniSinistre.put(mossa, sx);
			maniDestre.put(mossa, dx);
			sinistra = Math.max(sinistra, sx.getWidth());
			destra = Math.max(destra, dx.getWidth());
			alto = Math.max(alto, Math.max(sx.getHeight(), dx.getHeight()));
		}
		// Le mani non si ingrandiscono, e se non stanno nello spazio dentro la cornice si riducono, insieme alla cornice
		double perLeMani = Math.min(scala, Math.min((double) spazio.width / (sinistra + destra), (double) spazio.height / alto));
		for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
			maniSinistre.put(mossa, scalata(maniSinistre.get(mossa), perLeMani));
			maniDestre.put(mossa, scalata(maniDestre.get(mossa), perLeMani));
		}
		larghezzaManoSinistra = (int) Math.round(sinistra * perLeMani);
		larghezzaManoDestra = (int) Math.round(destra * perLeMani);
		int altezzaMani = (int) Math.round(alto * perLeMani);
		xManoSinistra = spazio.x + (spazio.width - larghezzaManoSinistra - larghezzaManoDestra) / 2;
		xManoDestra = xManoSinistra + larghezzaManoSinistra;
		yMani = spazio.y + (spazio.height - altezzaMani) / 2;
		ampiezzaX = (int) Math.round(AMPIEZZA_ORIZZONTALE * spazio.width);
		ampiezzaY = (int) Math.round(AMPIEZZA_VERTICALE * spazio.height);
		// Fin dove arriva il margine fra la mano e il bordo, e un poco oltre
		spostamentoVersoIlBordo = xManoSinistra - spazio.x + (int) Math.round(SPOSTAMENTO_VERSO_IL_BORDO * spazio.width);

		rettangolo.setBounds((larghezzaContenuto - cornice.getWidth()) / 2, (altezzaContenuto - cornice.getHeight()) / 2,
				cornice.getWidth(), cornice.getHeight());
		dimensionato = true;
	}

	private static BufferedImage scalata(BufferedImage originale, double scala) {
		if (scala >= 1.0) {
			return originale;
		}
		int larghezza = Math.max(1, (int) Math.round(originale.getWidth() * scala));
		int altezza = Math.max(1, (int) Math.round(originale.getHeight() * scala));
		BufferedImage scalata = new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = scalata.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(originale.getScaledInstance(larghezza, altezza, Image.SCALE_SMOOTH), 0, 0, null);
		} finally {
			g.dispose();
		}
		return scalata;
	}

	/**
	 * Il nome della mossa nei file delle immagini: "Carta", "Forbici", "Sasso".
	 */
	static String nomeDellaMano(MossaCartaForbiciSasso mossa) {
		switch (mossa) {
			case CARTA:
				return "Carta";
			case FORBICE:
				return "Forbici";
			default:
				return "Sasso";
		}
	}
}
