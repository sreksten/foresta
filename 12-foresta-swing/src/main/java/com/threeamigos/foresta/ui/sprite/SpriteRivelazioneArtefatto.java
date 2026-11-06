package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.immagini.IconeAwt;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.tipi.MappaBottinoImmagine;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Cerchio;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Fotogramma;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Pannello;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Raggio;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Riga;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Stella;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.function.LongSupplier;

/**
 * La rivelazione di un artefatto trovato in un cofano o in un tempio, in Swing: un cerchio luminoso con l'oggetto sopra, raggi di luce che girano, scintille,
 * e sotto un pannello scuro con nome, descrizione ed effetti. I tempi, la geometria a ogni istante e il testo del pannello sono di
 * {@link RivelazioneDellArtefatto}, nel modulo comune; qui c'è il disegno con le sfumature radiali di Java2D. Il gioco continua sotto.
 */
public class SpriteRivelazioneArtefatto implements SpriteInterface {

	// La veste, gli ingredienti magici, il talismano e il ninnolo non si trovano nelle locazioni, quindi non sono in MappaBottinoImmagine
	// Per ora la veste è quella del mago, anche per il ladro e il sacerdote
	private static final BufferedImage VESTE = BufferedImageBuilder.buildBufferedImage("oggetti/Veste.png", LivelloDiZoom.valore());
	private static final BufferedImage TALISMANO = BufferedImageBuilder.buildBufferedImage("oggetti/Talismano.png", LivelloDiZoom.valore());
	private static final BufferedImage NINNOLO = BufferedImageBuilder.buildBufferedImage("oggetti/Ninnolo.png", LivelloDiZoom.valore());
	private static final BufferedImage PERGAMENA = BufferedImageBuilder.buildBufferedImage("oggetti/Pergamena.png", LivelloDiZoom.valore());
	private static final BufferedImage GEMMA = BufferedImageBuilder.buildBufferedImage("oggetti/Gemma.png", LivelloDiZoom.valore());
	private static final BufferedImage MONILE = BufferedImageBuilder.buildBufferedImage("oggetti/Monile.png", LivelloDiZoom.valore());
	private static final BufferedImage GINGILLO = BufferedImageBuilder.buildBufferedImage("oggetti/Gingillo.png", LivelloDiZoom.valore());
	private static final BufferedImage SIGILLO = BufferedImageBuilder.buildBufferedImage("oggetti/Sigillo.png", LivelloDiZoom.valore());

	private final RivelazioneDellArtefatto modello;
	private final BufferedImage immagine;
	private final BufferedImage pannello;

	/**
	 * @param area         dove sta la rivelazione, centrata: tutto lo schermo
	 * @param destinazione dove vola all'uscita (il riquadro del gruppo)
	 */
	public SpriteRivelazioneArtefatto(VistaArtefatto artefatto, int livelloMondo, Rectangle area, Point destinazione) {
		this(artefatto, livelloMondo, area, destinazione, System::nanoTime);
	}

	/**
	 * @param orologioNanosecondi l'orologio con cui si misura il tempo che passa fra un disegno e l'altro: nel gioco System.nanoTime
	 */
	SpriteRivelazioneArtefatto(VistaArtefatto artefatto, int livelloMondo, Rectangle area, Point destinazione,
							   LongSupplier orologioNanosecondi) {
		this.immagine = artefatto.getAspetto() != null ? MappaBottinoImmagine.getImmagine(artefatto.getAspetto())
				: immagine(artefatto.getTipo());
		this.modello = new RivelazioneDellArtefatto(artefatto, livelloMondo, new Area(area.x, area.y, area.width, area.height),
				new Punto(destinazione.x, destinazione.y), immagine == null ? null : new Misura(immagine.getWidth(), immagine.getHeight()), orologioNanosecondi);
		this.pannello = costruisciIlPannello(modello.getPannello());
	}

	/**
	 * Quanto spicca l'artefatto, da 0 (spoglio) a 3: vedi {@link RivelazioneDellArtefatto#splendore}.
	 */
	static int splendore(VistaArtefatto artefatto, int livelloMondo) {
		return RivelazioneDellArtefatto.splendore(artefatto, livelloMondo);
	}

	/**
	 * Con altre rivelazioni in coda la sosta si accorcia, se non è già finita.
	 */
	public void accelera() {
		modello.accelera();
	}

	/**
	 * La chiude subito, senza l'uscita: quando chi guarda salta avanti.
	 */
	public void concludi() {
		modello.concludi();
	}

	@Override
	public boolean isAttivo() {
		return modello.isAttivo();
	}

	/**
	 * Da quanti secondi è cominciata, per i test.
	 */
	float getSecondi() {
		return modello.getSecondi();
	}

	@Override
	public void anima(Graphics2D graphics) {
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			disegna(g, modello.getSecondi());
		} finally {
			g.dispose();
		}
		modello.avanza();
	}

	/**
	 * Disegna la rivelazione all'istante {@code t} dall'inizio: il cerchio, i raggi, le scintille e il pannello.
	 */
	public void disegna(Graphics2D g, float t) {
		Fotogramma f = modello.fotogramma(t);
		if (f.ritaglio != null) {
			g.clip(new Rectangle(f.ritaglio.getX(), f.ritaglio.getY(), f.ritaglio.getLarghezza(), f.ritaglio.getAltezza()));
		}
		Rectangle area = new Rectangle(f.area.getX(), f.area.getY(), f.area.getLarghezza(), f.area.getAltezza());
		riempi(g, area, new Color(0, 0, 0, f.alfaDelVelo));
		riempi(g, area, new Color(255, 255, 255, Math.max(0, Math.min(255, f.alfaDelLampo))));
		if (!f.isCerchioVisibile()) {
			return;
		}

		for (Raggio raggio : f.raggi) {
			Path2D.Float raggioDiLuce = new Path2D.Float();
			raggioDiLuce.moveTo(raggio.vertici[0], raggio.vertici[1]);
			raggioDiLuce.lineTo(raggio.vertici[2], raggio.vertici[3]);
			raggioDiLuce.lineTo(raggio.vertici[4], raggio.vertici[5]);
			raggioDiLuce.closePath();
			Color c = new Color(raggio.rgb);
			g.setPaint(new RadialGradientPaint(raggio.centroX, raggio.centroY, raggio.lunghezza, new float[]{Math.min(0.99f, raggio.base / raggio.lunghezza), 1f},
					new Color[]{conAlfa(c, raggio.alfa), conAlfa(c, 0f)}));
			g.fill(raggioDiLuce);
		}

		// Alone, cerchio pieno e bordo
		Cerchio alone = f.alone;
		g.setPaint(new RadialGradientPaint(alone.fuocoX, alone.fuocoY, alone.raggioDellaSfumatura, new float[]{0f, 1f},
				new Color[]{conAlfa(new Color(alone.tinte[0]), alone.alfe[0]), conAlfa(new Color(alone.tinte[2]), alone.alfe[2])}));
		g.fill(new Ellipse2D.Float(alone.centroX - alone.raggio, alone.centroY - alone.raggio, 2 * alone.raggio, 2 * alone.raggio));
		Cerchio sfera = f.sfera;
		g.setPaint(new RadialGradientPaint(sfera.fuocoX, sfera.fuocoY, sfera.raggioDellaSfumatura, new float[]{0f, 0.55f, 1f},
				new Color[]{conAlfa(new Color(sfera.tinte[0]), sfera.alfe[0]), conAlfa(new Color(sfera.tinte[1]), sfera.alfe[1]),
						conAlfa(new Color(sfera.tinte[2]), sfera.alfe[2])}));
		g.fill(new Ellipse2D.Float(sfera.centroX - sfera.raggio, sfera.centroY - sfera.raggio, 2 * sfera.raggio, 2 * sfera.raggio));
		g.setStroke(new BasicStroke(2f));
		g.setColor(conAlfa(Color.WHITE, f.alfaDelBordo));
		g.draw(new Ellipse2D.Float(sfera.centroX - sfera.raggio, sfera.centroY - sfera.raggio, 2 * sfera.raggio, 2 * sfera.raggio));

		for (Stella stella : f.stelle) {
			stella(g, stella.x, stella.y, stella.d, conAlfa(Color.WHITE, stella.alfa));
		}

		// L'oggetto sul cerchio
		if (immagine != null && f.oggetto != null) {
			Composite composito = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f.oggetto.alfa));
			g.drawImage(immagine, f.oggetto.x, f.oggetto.y, f.oggetto.larghezza, f.oggetto.altezza, null);
			g.setComposite(composito);
		}

		// Il pannello con nome ed effetti resta al suo posto e sfuma per primo
		if (f.opacitaDelPannello > 0) {
			Composite composito = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f.opacitaDelPannello));
			g.drawImage(pannello, f.pannello.x, f.pannello.y, null);
			g.setComposite(composito);
		}
	}

	private static void stella(Graphics2D g, float x, float y, float d, Color c) {
		g.setColor(c);
		Path2D.Float p = new Path2D.Float();
		p.moveTo(x, y - d);
		p.lineTo(x + d * 0.25f, y);
		p.lineTo(x, y + d);
		p.lineTo(x - d * 0.25f, y);
		p.closePath();
		p.moveTo(x - d, y);
		p.lineTo(x, y + d * 0.25f);
		p.lineTo(x + d, y);
		p.lineTo(x, y - d * 0.25f);
		p.closePath();
		g.fill(p);
	}

	private static void riempi(Graphics2D g, Rectangle r, Color c) {
		if (c.getAlpha() > 0) {
			g.setColor(c);
			g.fillRect(r.x, r.y, r.width, r.height);
		}
	}

	/**
	 * L'immagine dell'oggetto, dove c'è: gli ingredienti magici hanno la loro, l'aggiornamento l'icona della pergamena, i
	 * tipi senza immagine mostrano solo il cerchio. Un artefatto con un aspetto suo (i libri tecnici) ha l'immagine di quello.
	 */
	private static BufferedImage immagine(TipoArtefatto tipo) {
		switch (tipo) {
			case PERGAMENA:
				return PERGAMENA;
			case GEMMA:
				return GEMMA;
			case MONILE:
				return MONILE;
			case GINGILLO:
				return GINGILLO;
			case SIGILLO:
				return SIGILLO;
			case SPADA:
				return MappaBottinoImmagine.getImmagine(TipoBottino.SPADA);
			case SPADONE:
				return MappaBottinoImmagine.getImmagine(TipoBottino.SPADONE);
			case FALCE:
				return MappaBottinoImmagine.getImmagine(TipoBottino.FALCE);
			case MAZZA:
				return MappaBottinoImmagine.getImmagine(TipoBottino.MAZZA);
			case ASCIA:
				return MappaBottinoImmagine.getImmagine(TipoBottino.ASCIA);
			case LANCIA:
				return MappaBottinoImmagine.getImmagine(TipoBottino.LANCIA);
			case BASTONE_MAGICO:
				return MappaBottinoImmagine.getImmagine(TipoBottino.BASTONE_MAGICO);
			case LIBRO_MAGICO:
				return MappaBottinoImmagine.getImmagine(TipoBottino.LIBRO_MAGICO);
			case SCUDO:
				return MappaBottinoImmagine.getImmagine(TipoBottino.SCUDO);
			case ELMO:
				return MappaBottinoImmagine.getImmagine(TipoBottino.ELMO);
			case MASCHERA:
				return MappaBottinoImmagine.getImmagine(TipoBottino.MASCHERA);
			case VESTE:
				return VESTE;
			case ARMATURA:
				return MappaBottinoImmagine.getImmagine(TipoBottino.ARMATURA);
			case SCHINIERI:
				return MappaBottinoImmagine.getImmagine(TipoBottino.SCHINIERI);
			case ANELLO:
				return MappaBottinoImmagine.getImmagine(TipoBottino.ANELLO);
			case TALISMANO:
				return TALISMANO;
			case NINNOLO:
				return NINNOLO;
			default:
				return tipo.isIngrediente() ? IconeAwt.icona(TipoIcona.PERGAMENA) : null;
		}
	}

	/**
	 * Il pannello scuro sotto il cerchio, in un'immagine sola: lo sfondo, il bordo e le righe che il modello ha disposto.
	 */
	private static BufferedImage costruisciIlPannello(Pannello pannello) {
		DoomdarkFont font = DoomdarkFont.medio();
		BufferedImage immagine = new BufferedImage(pannello.larghezza, pannello.altezza, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(0, 0, 0, 200));
			g.fillRoundRect(0, 0, immagine.getWidth(), immagine.getHeight(), 12, 12);
			g.setColor(conAlfa(new Color(pannello.rgbDelBordo), pannello.alfaDelBordo));
			g.drawRoundRect(0, 0, immagine.getWidth() - 1, immagine.getHeight() - 1, 12, 12);
			for (Riga riga : pannello.getRighe()) {
				g.drawImage(ImageCache.get(riga.getTesto(), font, riga.getColore(), riga.getLarghezzaMassima()), riga.getX(), riga.getY(), null);
			}
		} finally {
			g.dispose();
		}
		return immagine;
	}

	private static Color conAlfa(Color c, float alfa) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, Math.round(255 * alfa))));
	}
}
