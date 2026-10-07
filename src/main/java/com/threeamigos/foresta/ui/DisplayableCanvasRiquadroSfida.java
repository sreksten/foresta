package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Il riquadro della sfida a carta, forbici e sasso (vedi carta_forbici_sasso.md): le mani delle due mosse affiancate,
 * a sinistra quella del giocatore e a destra quella dell'avversario (all'inizio, per tutti e due, il sasso), e in
 * alto il punteggio di ognuno, scritto con TestoGrande con l'alone. Sta al centro dell'area di contenuto e occupa al
 * massimo la metà della sua larghezza e la metà della sua altezza, qualunque siano le dimensioni delle immagini (vedi
 * {@link #fattoreDiScala}): finita la sfida diventa uno sprite che si dissolve (vedi
 * {@link DisplayableCanvas#dissolviLaSfida}).
 */
class DisplayableCanvasRiquadroSfida implements Finestra {

	/**
	 * La parte massima dell'area di contenuto, in larghezza e in altezza, che il riquadro può occupare.
	 */
	static final double PARTE_MASSIMA_DELLO_SCHERMO = 0.5;
	private static final int RAGGIO_ALONE = 6;
	// Il punteggio sta in alto, a questa distanza dal bordo del riquadro
	private static final int MARGINE_PUNTEGGIO = 10;

	private final int larghezzaContenuto;
	private final int altezzaContenuto;
	// Quello che si registra nel canvas per il mouse: vuoto finché non si disegna per la prima volta, perché le immagini
	// si caricano solo allora
	private final Rectangle rettangolo = new Rectangle();
	private final Map<MossaCartaForbiciSasso, BufferedImage> maniSinistre = new EnumMap<>(MossaCartaForbiciSasso.class);
	private final Map<MossaCartaForbiciSasso, BufferedImage> maniDestre = new EnumMap<>(MossaCartaForbiciSasso.class);

	private boolean dimensionato;
	private int larghezzaSinistra;
	private int larghezzaDestra;
	private int altezza;
	private boolean visibile;
	private MossaCartaForbiciSasso mossaDelGiocatore = MossaCartaForbiciSasso.SASSO;
	private MossaCartaForbiciSasso mossaDellAvversario = MossaCartaForbiciSasso.SASSO;
	private int punteggioDelGiocatore;
	private int punteggioDellAvversario;

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
	 * La sfida comincia o si è giocata una mano.
	 */
	void aggiorna(MossaCartaForbiciSasso mossaDelGiocatore, MossaCartaForbiciSasso mossaDellAvversario,
				  int punteggioDelGiocatore, int punteggioDellAvversario) {
		this.mossaDelGiocatore = mossaDelGiocatore;
		this.mossaDellAvversario = mossaDellAvversario;
		this.punteggioDelGiocatore = punteggioDelGiocatore;
		this.punteggioDellAvversario = punteggioDellAvversario;
		this.visibile = true;
	}

	void disegna(Graphics2D graphics) {
		if (!visibile) {
			return;
		}
		dimensiona();
		graphics.drawImage(maniSinistre.get(mossaDelGiocatore), rettangolo.x, rettangolo.y, null);
		graphics.drawImage(maniDestre.get(mossaDellAvversario), rettangolo.x + larghezzaSinistra, rettangolo.y, null);
		scriviIlPunteggio(graphics, punteggioDelGiocatore, rettangolo.x, larghezzaSinistra);
		scriviIlPunteggio(graphics, punteggioDellAvversario, rettangolo.x + larghezzaSinistra, larghezzaDestra);
	}

	/**
	 * Il riquadro com'è adesso in un'immagine sola, e dove sta nell'area di contenuto: per lo sprite che lo dissolve.
	 */
	BufferedImage inUnImmagine() {
		dimensiona();
		BufferedImage immagine = new BufferedImage(rettangolo.width, rettangolo.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = immagine.createGraphics();
		try {
			graphics.translate(-rettangolo.x, -rettangolo.y);
			boolean eraVisibile = visibile;
			visibile = true;
			disegna(graphics);
			visibile = eraVisibile;
		} finally {
			graphics.dispose();
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
	 * La prima volta si caricano le sei immagini, si calcola la scala che le tiene entro metà schermo e si centra il
	 * riquadro.
	 */
	private void dimensiona() {
		if (dimensionato) {
			return;
		}
		int sinistra = 0;
		int destra = 0;
		int alto = 0;
		for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
			BufferedImage sx = BufferedImageBuilder.buildBufferedImage("fondi/" + nomeDellaMano(mossa) + "-sx.gif");
			BufferedImage dx = BufferedImageBuilder.buildBufferedImage("fondi/" + nomeDellaMano(mossa) + "-dx.gif");
			maniSinistre.put(mossa, sx);
			maniDestre.put(mossa, dx);
			sinistra = Math.max(sinistra, sx.getWidth());
			destra = Math.max(destra, dx.getWidth());
			alto = Math.max(alto, Math.max(sx.getHeight(), dx.getHeight()));
		}
		double scala = fattoreDiScala(larghezzaContenuto, altezzaContenuto, sinistra + destra, alto);
		for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
			maniSinistre.put(mossa, scalata(maniSinistre.get(mossa), scala));
			maniDestre.put(mossa, scalata(maniDestre.get(mossa), scala));
		}
		larghezzaSinistra = (int) Math.round(sinistra * scala);
		larghezzaDestra = (int) Math.round(destra * scala);
		altezza = (int) Math.round(alto * scala);
		int larghezza = larghezzaSinistra + larghezzaDestra;
		rettangolo.setBounds((larghezzaContenuto - larghezza) / 2, (altezzaContenuto - altezza) / 2, larghezza, altezza);
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

	private void scriviIlPunteggio(Graphics2D graphics, int punteggio, int xMano, int larghezzaMano) {
		BufferedImage numero = TestoGrande.conAlone(TestoGrande.immagine(String.valueOf(punteggio), Math.max(1, larghezzaMano), true), RAGGIO_ALONE);
		graphics.drawImage(numero, xMano + (larghezzaMano - numero.getWidth()) / 2, rettangolo.y + MARGINE_PUNTEGGIO, null);
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
