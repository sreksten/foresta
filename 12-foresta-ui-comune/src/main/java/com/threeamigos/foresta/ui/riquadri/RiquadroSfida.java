package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;

/**
 * Il riquadro della sfida a carta, forbici e sasso (vedi carta_forbici_sasso.md), senza disegno: la cornice larga come sfondo e, sopra, le mani delle due mosse
 * affiancate, a sinistra quella del giocatore e a destra quella dell'avversario, e in alto il punteggio di ognuno.
 * <p>
 * Mentre il giocatore sceglie, le due mani (due sassi) si muovono come nel "bim bum bam": su e giù e avanti e indietro, con due sinusoidi piccole attorno a
 * una posizione spostata verso i bordi della cornice, ritagliate allo spazio dentro il bordo nero ({@link #BORDO_DELLA_CORNICE}). Quando il giocatore ha
 * scelto le mani si fermano al centro e mostrano le due mosse per {@link #SECONDI_DI_PAUSA_DOPO_UNA_MANO}, poi riprendono a muoversi. Il riquadro sta al
 * centro dell'area di contenuto e occupa al massimo la metà della sua larghezza e la metà della sua altezza (vedi {@link #fattoreDiScala}).
 * <p>
 * Chi disegna (Swing, libGDX) dice quanto sono grandi la cornice e le mani ({@link #misuraDellaCornice}, {@link #misuraDellaMano}), riceve da
 * {@link #disposizione()} dove stanno e di quanto ridurle, e da {@link #fotogramma(long)} che mani mostrare e di quanto spostarle in questo istante.
 */
public abstract class RiquadroSfida implements Finestra {

	/**
	 * La parte massima dell'area di contenuto, in larghezza e in altezza, che il riquadro può occupare.
	 */
	public static final double PARTE_MASSIMA_DELLO_SCHERMO = 0.5;
	/**
	 * Lo spessore del bordo interno nero della cornice larga, da tutti i lati: le mani stanno, e vengono ritagliate, dentro quello che resta.
	 */
	public static final int BORDO_DELLA_CORNICE = 20;
	/**
	 * Quanto restano ferme le mani a mostrare le due mosse prima di riprendere a muoversi.
	 */
	public static final double SECONDI_DI_PAUSA_DOPO_UNA_MANO = 0.5;
	// Quanto si spostano le mani al massimo, in parte dello spazio dentro la cornice: avanti e indietro (verso i lati) e su e giù
	static final double AMPIEZZA_ORIZZONTALE = 0.04;
	static final double AMPIEZZA_VERTICALE = 0.04;
	// Le due sinusoidi, in oscillazioni al secondo: su e giù va a velocità doppia rispetto ad avanti e indietro
	public static final double FREQUENZA_ORIZZONTALE = 1.0;
	static final double FREQUENZA_VERTICALE = 2.0;
	// Mentre si muovono le mani stanno più vicine al bordo della cornice di quando sono ferme al centro: ognuna verso il suo lato, fino al margine che c'è fra
	// la mano e il bordo e un po' oltre (questa parte dello spazio dentro la cornice). Il ritaglio nasconde quello che esce dal bordo nero
	static final double SPOSTAMENTO_VERSO_IL_BORDO = 0.02;
	// Il passaggio dal centro alla posizione verso il bordo, a inizio movimento, per non fare uno scatto
	public static final double SECONDI_PER_ANDARE_VERSO_IL_BORDO = 0.8;
	/**
	 * Il raggio dell'alone attorno ai punteggi
	 */
	public static final int RAGGIO_ALONE = 6;
	// Il punteggio sta in alto, a questa distanza dal bordo interno della cornice (a grandezza naturale)
	private static final int MARGINE_PUNTEGGIO = 8;

	/**
	 * Dove stanno la cornice e le mani, alla grandezza di schermo (l'origine è l'angolo in alto a sinistra del riquadro)
	 */
	public static final class Disposizione {
		private final double scala;
		private final double scalaDelleMani;
		private final Misura cornice;
		private final Area spazio;
		private final int xManoSinistra;
		private final int xManoDestra;
		private final int yMani;
		private final int larghezzaManoSinistra;
		private final int larghezzaManoDestra;
		private final int yDelPunteggio;
		private final int ampiezzaX;
		private final int ampiezzaY;
		private final int spostamentoVersoIlBordo;
		private final Area area;

		private Disposizione(double scala, double scalaDelleMani, Misura cornice, Area spazio, int xManoSinistra, int xManoDestra, int yMani,
							 int larghezzaManoSinistra, int larghezzaManoDestra, int yDelPunteggio, int ampiezzaX, int ampiezzaY,
							 int spostamentoVersoIlBordo, Area area) {
			this.scala = scala;
			this.scalaDelleMani = scalaDelleMani;
			this.cornice = cornice;
			this.spazio = spazio;
			this.xManoSinistra = xManoSinistra;
			this.xManoDestra = xManoDestra;
			this.yMani = yMani;
			this.larghezzaManoSinistra = larghezzaManoSinistra;
			this.larghezzaManoDestra = larghezzaManoDestra;
			this.yDelPunteggio = yDelPunteggio;
			this.ampiezzaX = ampiezzaX;
			this.ampiezzaY = ampiezzaY;
			this.spostamentoVersoIlBordo = spostamentoVersoIlBordo;
			this.area = area;
		}

		/**
		 * Di quanto si riducono la cornice (e di conseguenza il resto): al più 1, non si ingrandisce mai
		 */
		public double getScala() {
			return scala;
		}

		/**
		 * Di quanto si riducono le mani (non più della cornice, e quanto serve perché stiano nello spazio dentro il bordo)
		 */
		public double getScalaDelleMani() {
			return scalaDelleMani;
		}

		/**
		 * La cornice già ridotta
		 */
		public Misura getCornice() {
			return cornice;
		}

		/**
		 * Lo spazio dentro il bordo nero, in coordinate del riquadro: lì si ritagliano le mani
		 */
		public Area getSpazio() {
			return spazio;
		}

		public int getXManoSinistra() {
			return xManoSinistra;
		}

		public int getXManoDestra() {
			return xManoDestra;
		}

		public int getYMani() {
			return yMani;
		}

		public int getLarghezzaManoSinistra() {
			return larghezzaManoSinistra;
		}

		public int getLarghezzaManoDestra() {
			return larghezzaManoDestra;
		}

		/**
		 * La quota (nel riquadro) dell'alto dei punteggi
		 */
		public int getYDelPunteggio() {
			return yDelPunteggio;
		}

		/**
		 * Dove sta il riquadro nell'area di contenuto
		 */
		public Area getArea() {
			return area;
		}
	}

	/**
	 * Quel che si mostra in un istante: le due mani e di quanto sono spostate (la sinistra di dx, la destra di -dx, tutte e due di dy)
	 */
	public static final class Fotogramma {
		private final MossaCartaForbiciSasso sinistra;
		private final MossaCartaForbiciSasso destra;
		private final double dx;
		private final double dy;

		private Fotogramma(MossaCartaForbiciSasso sinistra, MossaCartaForbiciSasso destra, double dx, double dy) {
			this.sinistra = sinistra;
			this.destra = destra;
			this.dx = dx;
			this.dy = dy;
		}

		public MossaCartaForbiciSasso getSinistra() {
			return sinistra;
		}

		public MossaCartaForbiciSasso getDestra() {
			return destra;
		}

		public double getDx() {
			return dx;
		}

		public double getDy() {
			return dy;
		}
	}

	private final int larghezzaContenuto;
	private final int altezzaContenuto;
	private Disposizione disposizione;

	private boolean visibile;
	private MossaCartaForbiciSasso mossaDelGiocatore = MossaCartaForbiciSasso.SASSO;
	private MossaCartaForbiciSasso mossaDellAvversario = MossaCartaForbiciSasso.SASSO;
	private int punteggioDelGiocatore;
	private int punteggioDellAvversario;
	// Quando le mani ripartono (il movimento comincia da ferme), e fino a quando mostrano le mosse scelte
	private long inizioMovimentoNanos;
	private long fermeFinoANanos;

	protected RiquadroSfida(int larghezzaContenuto, int altezzaContenuto) {
		this.larghezzaContenuto = larghezzaContenuto;
		this.altezzaContenuto = altezzaContenuto;
	}

	/**
	 * Quanto è grande la cornice larga, come la disegna chi disegna (già con lo zoom), prima della riduzione
	 */
	protected abstract Misura misuraDellaCornice();

	/**
	 * Quanto è grande la mano di quella mossa (<code>sinistra</code> per quella del giocatore), già con lo zoom, prima della riduzione
	 */
	protected abstract Misura misuraDellaMano(MossaCartaForbiciSasso mossa, boolean sinistra);

	/**
	 * Di quanto si riducono le immagini perché le due mani affiancate, grandi {@code larghezzaTotale} per
	 * {@code altezzaMassima}, non occupino più di metà dell'area di contenuto in larghezza e in altezza. Non si
	 * ingrandiscono mai.
	 */
	public static double fattoreDiScala(int larghezzaContenuto, int altezzaContenuto, int larghezzaTotale, int altezzaMassima) {
		double perLaLarghezza = PARTE_MASSIMA_DELLO_SCHERMO * larghezzaContenuto / Math.max(1, larghezzaTotale);
		double perLAltezza = PARTE_MASSIMA_DELLO_SCHERMO * altezzaContenuto / Math.max(1, altezzaMassima);
		return Math.min(1.0, Math.min(perLaLarghezza, perLAltezza));
	}

	/**
	 * Lo spostamento orizzontale delle mani dopo quel tempo dall'inizio del movimento, da -ampiezza a +ampiezza: una
	 * sinusoide che parte da zero.
	 */
	public static double spostamentoOrizzontale(double secondi, double ampiezza) {
		return ampiezza * Math.sin(2 * Math.PI * FREQUENZA_ORIZZONTALE * secondi);
	}

	/**
	 * Quanto delle mani è già andato dal centro alla posizione verso il bordo, da 0 a 1, dopo quel tempo dall'inizio del
	 * movimento: una salita dolce, perché le mani non scattino.
	 */
	public static double avvicinamentoAlBordo(double secondi) {
		double t = Math.max(0, Math.min(1, secondi / SECONDI_PER_ANDARE_VERSO_IL_BORDO));
		return t * t * (3 - 2 * t);
	}

	/**
	 * Lo spostamento verticale delle mani, con la sua sinusoide.
	 */
	public static double spostamentoVerticale(double secondi, double ampiezza) {
		return ampiezza * Math.sin(2 * Math.PI * FREQUENZA_VERTICALE * secondi);
	}

	/**
	 * Il nome della mossa nei file delle immagini: "Carta", "Forbici", "Sasso".
	 */
	public static String nomeDellaMano(MossaCartaForbiciSasso mossa) {
		switch (mossa) {
			case CARTA:
				return "Carta";
			case FORBICE:
				return "Forbici";
			default:
				return "Sasso";
		}
	}

	@Override
	public boolean isVisibile() {
		return visibile;
	}

	public void setVisibile(boolean visibile) {
		this.visibile = visibile;
	}

	public int getPunteggioDelGiocatore() {
		return punteggioDelGiocatore;
	}

	public int getPunteggioDellAvversario() {
		return punteggioDellAvversario;
	}

	/**
	 * La sfida comincia ({@code inizio} vero: i due sassi si muovono) o si è giocata una mano (le mani si fermano a
	 * mostrare le due mosse, e dopo la pausa riprendono a muoversi con il punteggio nuovo).
	 */
	public void aggiorna(MossaCartaForbiciSasso mossaDelGiocatore, MossaCartaForbiciSasso mossaDellAvversario,
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

	/**
	 * Quel che si mostra all'istante dato (in nanosecondi): se le mani sono ferme a mostrare le mosse scelte o in movimento come due sassi
	 */
	public Fotogramma fotogramma(long oraNanos) {
		Disposizione d = disposizione();
		if (oraNanos < fermeFinoANanos) {
			return new Fotogramma(mossaDelGiocatore, mossaDellAvversario, 0, 0);
		}
		double secondi = (oraNanos - inizioMovimentoNanos) / 1_000_000_000.0;
		return new Fotogramma(MossaCartaForbiciSasso.SASSO, MossaCartaForbiciSasso.SASSO,
				spostamentoOrizzontale(secondi, d.ampiezzaX) - avvicinamentoAlBordo(secondi) * d.spostamentoVersoIlBordo,
				spostamentoVerticale(secondi, d.ampiezzaY));
	}

	/**
	 * Quel che si mostra adesso
	 */
	public Fotogramma fotogramma() {
		return fotogramma(System.nanoTime());
	}

	/**
	 * Il riquadro fermo, con le mosse dell'ultima mano (per lo sprite che lo dissolve)
	 */
	public Fotogramma fotogrammaFermo() {
		disposizione();
		return new Fotogramma(mossaDelGiocatore, mossaDellAvversario, 0, 0);
	}

	/**
	 * Dove stanno la cornice e le mani. La prima volta calcola di quanto va ridotto tutto perché il riquadro stia entro metà schermo, dispone le mani al
	 * centro dello spazio dentro il bordo e centra il riquadro.
	 */
	public Disposizione disposizione() {
		if (disposizione == null) {
			Misura corniceOriginale = misuraDellaCornice();
			double scala = fattoreDiScala(larghezzaContenuto, altezzaContenuto, corniceOriginale.larghezza, corniceOriginale.altezza);
			Misura cornice = scalata(corniceOriginale, scala);
			int bordo = (int) Math.round(BORDO_DELLA_CORNICE * scala);
			Area spazio = new Area(bordo, bordo, cornice.larghezza - 2 * bordo, cornice.altezza - 2 * bordo);

			int sinistra = 0;
			int destra = 0;
			int alto = 0;
			for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
				Misura sx = misuraDellaMano(mossa, true);
				Misura dx = misuraDellaMano(mossa, false);
				sinistra = Math.max(sinistra, sx.larghezza);
				destra = Math.max(destra, dx.larghezza);
				alto = Math.max(alto, Math.max(sx.altezza, dx.altezza));
			}
			// Le mani non si ingrandiscono, e se non stanno nello spazio dentro la cornice si riducono, insieme alla cornice
			double perLeMani = Math.min(scala, Math.min((double) spazio.getLarghezza() / (sinistra + destra), (double) spazio.getAltezza() / alto));
			int larghezzaManoSinistra = (int) Math.round(sinistra * perLeMani);
			int larghezzaManoDestra = (int) Math.round(destra * perLeMani);
			int altezzaMani = (int) Math.round(alto * perLeMani);
			int xManoSinistra = spazio.getX() + (spazio.getLarghezza() - larghezzaManoSinistra - larghezzaManoDestra) / 2;
			int xManoDestra = xManoSinistra + larghezzaManoSinistra;
			int yMani = spazio.getY() + (spazio.getAltezza() - altezzaMani) / 2;
			int ampiezzaX = (int) Math.round(AMPIEZZA_ORIZZONTALE * spazio.getLarghezza());
			int ampiezzaY = (int) Math.round(AMPIEZZA_VERTICALE * spazio.getAltezza());
			// Fin dove arriva il margine fra la mano e il bordo, e un poco oltre
			int spostamentoVersoIlBordo = xManoSinistra - spazio.getX() + (int) Math.round(SPOSTAMENTO_VERSO_IL_BORDO * spazio.getLarghezza());
			int yDelPunteggio = spazio.getY() + (int) Math.round(MARGINE_PUNTEGGIO * scala);
			Area area = new Area((larghezzaContenuto - cornice.larghezza) / 2, (altezzaContenuto - cornice.altezza) / 2, cornice.larghezza, cornice.altezza);
			disposizione = new Disposizione(scala, perLeMani, cornice, spazio, xManoSinistra, xManoDestra, yMani, larghezzaManoSinistra,
					larghezzaManoDestra, yDelPunteggio, ampiezzaX, ampiezzaY, spostamentoVersoIlBordo, area);
		}
		return disposizione;
	}

	/**
	 * Dove sta il riquadro nell'area di contenuto
	 */
	public Area getArea() {
		return disposizione().getArea();
	}

	private static Misura scalata(Misura originale, double scala) {
		if (scala >= 1.0) {
			return originale;
		}
		return new Misura(Math.max(1, (int) Math.round(originale.larghezza * scala)), Math.max(1, (int) Math.round(originale.altezza * scala)));
	}

	/**
	 * Il nome della risorsa della cornice
	 */
	protected static String risorsaDellaCornice() {
		return NomiDelleImmagini.CORNICE_LARGA;
	}
}
