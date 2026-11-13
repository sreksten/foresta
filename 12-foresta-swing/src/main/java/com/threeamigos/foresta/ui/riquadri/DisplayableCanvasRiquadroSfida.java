package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.ui.DisplayableCanvas;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.TestoGrande;

import java.awt.*;
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
public class DisplayableCanvasRiquadroSfida extends RiquadroSfida {

	// Quello che si registra nel canvas per il mouse: vuoto finché non si disegna per la prima volta, perché le immagini
	// si caricano solo allora
	private final Rectangle rettangolo = new Rectangle();
	private final Map<MossaCartaForbiciSasso, BufferedImage> maniSinistre = new EnumMap<>(MossaCartaForbiciSasso.class);
	private final Map<MossaCartaForbiciSasso, BufferedImage> maniDestre = new EnumMap<>(MossaCartaForbiciSasso.class);
	private final Map<Integer, BufferedImage> punteggi = new HashMap<>();
	// Di quanto l'ombra dei nomi è spostata in basso a destra
	private static final int OMBRA = 2;

	private boolean dimensionato;
	private BufferedImage cornice;

	public DisplayableCanvasRiquadroSfida(int larghezzaContenuto, int altezzaContenuto) {
		super(larghezzaContenuto, altezzaContenuto);
	}

	@Override
	protected Misura misuraDellaCornice() {
		return new Misura(ImageCache.corniceLarga.getWidth(), ImageCache.corniceLarga.getHeight());
	}

	@Override
	protected Misura misuraDellaMano(MossaCartaForbiciSasso mossa, boolean sinistra) {
		BufferedImage mano = (sinistra ? maniSinistre : maniDestre).computeIfAbsent(mossa,
				m -> BufferedImageBuilder.buildBufferedImage(NomiDelleImmagini.manoDellaSfida(m, sinistra), LivelloDiZoom.valore()));
		return new Misura(mano.getWidth(), mano.getHeight());
	}

	public Rectangle getRettangolo() {
		return rettangolo;
	}

	/**
	 * Disegna il riquadro della sfida a carta, forbici e sasso.
	 */
	public void disegna(Graphics2D graphics) {
		if (!isVisibile()) {
			return;
		}
		dimensiona();
		Fotogramma fotogramma = fotogramma();
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.translate(rettangolo.x, rettangolo.y);
			g.drawImage(cornice, 0, 0, null);
			disegnaLeMani(g, fotogramma);
			scriviIPunteggi(g);
			scriviINomi(g);
		} finally {
			g.dispose();
		}
	}

	/**
	 * Il riquadro fermo, con le mosse dell'ultima mano, in un'immagine sola: per lo sprite che lo dissolve.
	 */
	public BufferedImage inUnImmagine() {
		dimensiona();
		BufferedImage immagine = new BufferedImage(rettangolo.width, rettangolo.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		try {
			g.drawImage(cornice, 0, 0, null);
			disegnaLeMani(g, fotogrammaFermo());
			scriviIPunteggi(g);
			scriviINomi(g);
		} finally {
			g.dispose();
		}
		return immagine;
	}

	/**
	 * Il punto in alto a sinistra del riquadro nell'area di contenuto.
	 */
	public int getX() {
		dimensiona();
		return rettangolo.x;
	}

	public int getY() {
		dimensiona();
		return rettangolo.y;
	}

	/**
	 * Le due mani, sopra la cornice, ritagliate dentro il bordo nero: la sinistra si sposta di {@code dx} (negativo verso
	 * il suo bordo) e la destra del contrario (si avvicinano e si allontanano), tutte e due di {@code dy} (su e giù
	 * insieme).
	 */
	private void disegnaLeMani(Graphics2D graphics, Fotogramma fotogramma) {
		Disposizione d = disposizione();
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.clipRect(d.getSpazio().getX(), d.getSpazio().getY(), d.getSpazio().getLarghezza(), d.getSpazio().getAltezza());
			g.drawImage(maniSinistre.get(fotogramma.getSinistra()), (int) Math.round(d.getXManoSinistra() + fotogramma.getDx()),
					(int) Math.round(d.getYMani() + fotogramma.getDy()), null);
			g.drawImage(maniDestre.get(fotogramma.getDestra()), (int) Math.round(d.getXManoDestra() - fotogramma.getDx()),
					(int) Math.round(d.getYMani() + fotogramma.getDy()), null);
		} finally {
			g.dispose();
		}
	}

	private void scriviIPunteggi(Graphics2D graphics) {
		Disposizione d = disposizione();
		scriviIlPunteggio(graphics, getPunteggioDelGiocatore(), d.getXManoSinistra(), d.getLarghezzaManoSinistra(), d.getYDelPunteggio());
		scriviIlPunteggio(graphics, getPunteggioDellAvversario(), d.getXManoDestra(), d.getLarghezzaManoDestra(), d.getYDelPunteggio());
	}

	/**
	 * I nomi negli angoli, come i titoli delle cornici dell'inventario: grigio chiaro con l'ombra nera
	 */
	private void scriviINomi(Graphics2D graphics) {
		if (!getNomeDelGiocatore().isEmpty()) {
			scriviConLOmbra(graphics, getNomeDelGiocatore(), posizioneDelNomeDelGiocatore());
		}
		if (!getNomeDellAvversario().isEmpty()) {
			Image nome = ImageCache.get(getNomeDellAvversario(), DoomdarkColorModel.Color.LIGHT_GRAY);
			scriviConLOmbra(graphics, getNomeDellAvversario(),
					posizioneDelNomeDellAvversario(new Misura(nome.getWidth(null) + OMBRA, nome.getHeight(null) + OMBRA)));
		}
	}

	private static void scriviConLOmbra(Graphics2D graphics, String testo, Punto punto) {
		graphics.drawImage(ImageCache.get(testo, DoomdarkColorModel.Color.BLACK), punto.x + OMBRA, punto.y + OMBRA, null);
		graphics.drawImage(ImageCache.get(testo, DoomdarkColorModel.Color.LIGHT_GRAY), punto.x, punto.y, null);
	}

	private void scriviIlPunteggio(Graphics2D graphics, int punteggio, int xMano, int larghezzaMano, int y) {
		BufferedImage numero = punteggi.computeIfAbsent(punteggio,
				p -> TestoGrande.conAlone(TestoGrande.immagine(String.valueOf(p), Math.max(1, larghezzaMano), true), RAGGIO_ALONE));
		graphics.drawImage(numero, xMano + (larghezzaMano - numero.getWidth()) / 2, y, null);
	}

	/**
	 * La prima volta si caricano la cornice e le sei mani (e si riducono, se serve) e si centra il riquadro.
	 */
	private void dimensiona() {
		if (dimensionato) {
			return;
		}
		Disposizione d = disposizione();
		cornice = scalata(ImageCache.corniceLarga, d.getScala());
		for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
			maniSinistre.put(mossa, scalata(maniSinistre.get(mossa), d.getScalaDelleMani()));
			maniDestre.put(mossa, scalata(maniDestre.get(mossa), d.getScalaDelleMani()));
		}
		rettangolo.setBounds(d.getArea().getX(), d.getArea().getY(), d.getArea().getLarghezza(), d.getArea().getAltezza());
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
}
