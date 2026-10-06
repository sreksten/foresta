package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.RegoleSetLeggendari;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.IncantamentoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.tools.Temporizzatore;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * La rivelazione di un artefatto trovato in un cofano o in un tempio: un cerchio luminoso con l'oggetto sopra,
 * raggi di luce che girano in senso antiorario allungandosi e ritraendosi, scintille, e sotto un pannello scuro
 * con nome, descrizione ed effetti. Più l'artefatto spicca (vedi {@link #splendore}), più il cerchio è grande e
 * i raggi sono numerosi e lunghi; se ha incantamenti, i raggi hanno i colori dei loro elementi.
 * <p>
 * Tempi: un punto di luce che si gonfia, il cerchio che si apre con un piccolo rimbalzo, la sosta, e l'uscita,
 * in cui cerchio e oggetto volano rimpicciolendo verso il riquadro del gruppo. Il gioco continua sotto.
 */
class SpriteRivelazioneArtefatto implements SpriteInterface {

	private static final float DURATA = 5.0f; // 3.4f;
	// Con altre rivelazioni in coda (un cofano nei castelli ne vale cinque) si accorcia la sosta
	private static final float DURATA_CON_CODA = 2.2f;
	private static final float ANTICIPAZIONE = 0.25f;
	private static final float APERTURA = 0.35f;
	private static final float USCITA = 0.5f;
	private static final float FOTOGRAMMA = 1f / Temporizzatore.FRAME_PER_SECONDO;

	// Per gradino di splendore (0-3): raggio del cerchio, numero di raggi, lunghezza massima in raggi, scintille
	private static final int[] RAGGIO = {64, 76, 88, 100};
	private static final int[] RAGGI = {8, 12, 16, 24};
	private static final float[] LUNGHEZZA = {1.8f, 2.1f, 2.5f, 3.0f};
	private static final int[] SCINTILLE = {0, 6, 12, 22};
	private static final float[] LAMPO = {0f, 0f, 0.35f, 0.55f};
	private static final Color[] COLORE = {
			new Color(255, 236, 190), new Color(255, 214, 92), new Color(255, 190, 70), new Color(255, 225, 120)
	};
	private static final Map<TipoDanno, Color> COLORI_ELEMENTI = new EnumMap<>(TipoDanno.class);

	static {
		COLORI_ELEMENTI.put(TipoDanno.ARIA, new Color(200, 235, 255));
		COLORI_ELEMENTI.put(TipoDanno.ACQUA, new Color(70, 140, 255));
		COLORI_ELEMENTI.put(TipoDanno.TERRA, new Color(190, 135, 70));
		COLORI_ELEMENTI.put(TipoDanno.FUOCO, new Color(255, 120, 30));
		COLORI_ELEMENTI.put(TipoDanno.FULMINE, new Color(255, 245, 90));
		COLORI_ELEMENTI.put(TipoDanno.GELO, new Color(150, 220, 255));
		COLORI_ELEMENTI.put(TipoDanno.ACIDO, new Color(160, 235, 40));
		COLORI_ELEMENTI.put(TipoDanno.SONICO, new Color(230, 200, 255));
		COLORI_ELEMENTI.put(TipoDanno.VELENO, new Color(90, 200, 70));
		COLORI_ELEMENTI.put(TipoDanno.NECROTICO, new Color(120, 170, 100));
		COLORI_ELEMENTI.put(TipoDanno.SACRO, new Color(255, 250, 215));
		COLORI_ELEMENTI.put(TipoDanno.PSICHICO, new Color(235, 110, 220));
		COLORI_ELEMENTI.put(TipoDanno.ARCANO, new Color(165, 115, 255));
		COLORI_ELEMENTI.put(TipoDanno.VUOTO, new Color(110, 80, 190));
		COLORI_ELEMENTI.put(TipoDanno.MALEDIZIONE, new Color(170, 50, 120));
	}

	private final int gradino;
	private final int raggio;
	private final Color colore;
	private final List<Color> coloriRaggi = new ArrayList<>();
	private final BufferedImage immagine;
	private final BufferedImage pannello;
	private final Rectangle area;
	private final int centroX;
	private final int centroY;
	private final int pannelloX;
	private final Point destinazione;
	private final float[][] scintille;

	private float durata = DURATA;
	private float secondi;
	private boolean attivo = true;

	/**
	 * @param area         dove sta la rivelazione, centrata: tutto lo schermo
	 * @param destinazione dove vola all'uscita (il riquadro del gruppo)
	 */
	SpriteRivelazioneArtefatto(Artefatto artefatto, int livelloMondo, Rectangle area, Point destinazione) {
		ArtefattoMD md = artefatto.getModelloDati();
		this.gradino = splendore(md, livelloMondo);
		this.raggio = RAGGIO[gradino];
		this.colore = COLORE[gradino];
		this.area = area;
		this.destinazione = destinazione;
		for (IncantamentoMD incantamento : md.getIncantamenti()) {
			coloriRaggi.add(COLORI_ELEMENTI.getOrDefault(incantamento.getTipoDannoElementale(), colore));
		}
		if (coloriRaggi.isEmpty()) {
			coloriRaggi.add(colore);
			if (gradino == 3) {
				coloriRaggi.add(Color.WHITE);
			}
		}
		this.immagine = immagine(md.getTipo());
		this.pannello = pannello(md, Math.min(area.width - 48, 900));
		int altezzaTotale = 2 * raggio + 12 + pannello.getHeight();
		int alto = area.y + Math.max(6, (area.height - altezzaTotale) / 2);
		this.centroX = (int) area.getCenterX();
		this.centroY = alto + raggio;
		// Il pannello si centra sotto il cerchio, senza uscire dall'area
		this.pannelloX = Math.max(area.x + 4, Math.min(area.x + area.width - 4 - pannello.getWidth(), centroX - pannello.getWidth() / 2));
		Random random = new Random(md.getNome().hashCode());
		scintille = new float[SCINTILLE[gradino]][];
		for (int i = 0; i < scintille.length; i++) {
			// angolo, distanza in raggi, fase, dimensione
			scintille[i] = new float[] {(float) (random.nextDouble() * Math.PI * 2), 1.15f + random.nextFloat() * 0.9f,
					(float) (random.nextDouble() * Math.PI * 2), 2.5f + random.nextFloat() * 3.5f};
		}
	}

	/**
	 * Quanto spicca l'artefatto, da 0 (spoglio) a 3: un punto per effetto, due se è raro e quattro se leggendario,
	 * più i livelli sopra quello del mondo.
	 */
	static int splendore(ArtefattoMD md, int livelloMondo) {
		int punti = md.getModificatori().size() + md.getIncantamenti().size();
		if (md.getRarita() == TipoRaritaArtefatto.RARO) {
			punti += 2;
		} else if (md.getRarita() == TipoRaritaArtefatto.LEGGENDARIO) {
			punti += 4;
		}
		punti += Math.max(0, md.getLivello() - livelloMondo);
		if (punti == 0) {
			return 0;
		}
		return punti <= 2 ? 1 : punti <= 4 ? 2 : 3;
	}

	/**
	 * Con altre rivelazioni in coda la sosta si accorcia, se non è già finita.
	 */
	void accelera() {
		if (secondi < DURATA_CON_CODA - USCITA) {
			durata = DURATA_CON_CODA;
		}
	}

	@Override
	public boolean isAttivo() {
		return attivo;
	}

	@Override
	public void anima(Graphics2D graphics) {
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			disegna(g, secondi);
		} finally {
			g.dispose();
		}
		secondi += FOTOGRAMMA;
		if (secondi >= durata) {
			attivo = false;
		}
	}

	void disegna(Graphics2D g, float t) {
		float inizioUscita = durata - USCITA;
		float uscita = t > inizioUscita ? Math.min(1f, (t - inizioUscita) / USCITA) : 0f;
		float voloUscita = uscita * uscita;

		// Scala del cerchio: punto che si gonfia, apertura con rimbalzo, poi rimpicciolisce uscendo
		float scala;
		if (t < ANTICIPAZIONE) {
			scala = 0.35f * uscitaMorbida(t / ANTICIPAZIONE);
		} else if (t < ANTICIPAZIONE + APERTURA) {
			scala = 0.35f + 0.65f * rimbalzo((t - ANTICIPAZIONE) / APERTURA);
		} else {
			scala = 1f;
		}
		scala *= 1f - 0.85f * voloUscita;
		float x = centroX + (destinazione.x - centroX) * voloUscita;
		float y = centroY + (destinazione.y - centroY) * voloUscita;
		float alfa = Math.min(1f, t / ANTICIPAZIONE) * (1f - 0.7f * uscita);
		// Finché non vola via resta nella sua area: i raggi lunghi non devono coprire il riquadro del testo
		if (uscita == 0f) {
			g.clip(area);
		}

		// Lo sfondo si scurisce un po', così l'artefatto spicca
		float velo = Math.min(1f, t / 0.3f) * (1f - uscita);
		riempi(g, area, new Color(0, 0, 0, (int) (110 * velo)));

		// Lampo all'apertura, solo per gli artefatti che spiccano di più
		float lampo = LAMPO[gradino];
		if (lampo > 0 && t >= ANTICIPAZIONE && t < ANTICIPAZIONE + 0.3f) {
			riempi(g, area, new Color(255, 255, 255, (int) (255 * lampo * (1f - (t - ANTICIPAZIONE) / 0.3f))));
		}

		float r = raggio * scala;
		if (r < 1) {
			return;
		}

		// Raggi: si allungano fino al massimo e si ritraggono nel corso della rivelazione
		float inviluppo = 0f;
		if (t > ANTICIPAZIONE && t < inizioUscita) {
			inviluppo = (float) Math.pow(Math.sin(Math.PI * (t - ANTICIPAZIONE) / (inizioUscita - ANTICIPAZIONE)), 0.8);
		}
		float lunghezzaMassima = raggio * LUNGHEZZA[gradino];
		if (inviluppo > 0) {
			// Il giro secondario, tenue e in senso orario, dà profondità
			if (gradino > 0) {
				disegnaRaggi(g, x, y, r, r + (lunghezzaMassima * 0.85f - r) * inviluppo, RAGGI[gradino] / 2,
						(float) (0.25 * t), Color.WHITE, 0.35f * alfa, t, false);
			}
			disegnaRaggi(g, x, y, r, r + (lunghezzaMassima - r) * inviluppo, RAGGI[gradino],
					(float) (-(0.5 + 0.15 * gradino) * t), null, 0.85f * alfa, t, gradino >= 2);
		}

		// Alone, cerchio pieno e bordo
		float alone = r * 1.9f;
		g.setPaint(new RadialGradientPaint(x, y, alone, new float[] {0f, 1f},
				new Color[] {conAlfa(colore, 0.65f * alfa), conAlfa(colore, 0f)}));
		g.fill(new Ellipse2D.Float(x - alone, y - alone, 2 * alone, 2 * alone));
		g.setPaint(new RadialGradientPaint(x - r * 0.25f, y - r * 0.25f, r * 1.3f, new float[] {0f, 0.55f, 1f},
				new Color[] {conAlfa(Color.WHITE, alfa), conAlfa(colore, alfa), conAlfa(colore.darker(), alfa)}));
		g.fill(new Ellipse2D.Float(x - r, y - r, 2 * r, 2 * r));
		g.setStroke(new BasicStroke(2f));
		g.setColor(conAlfa(Color.WHITE, 0.8f * alfa));
		g.draw(new Ellipse2D.Float(x - r, y - r, 2 * r, 2 * r));

		// Scintille che brillano attorno al cerchio
		for (float[] scintilla : scintille) {
			float brillio = (float) Math.max(0, Math.sin(scintilla[2] + t * 5)) * inviluppo * alfa;
			if (brillio > 0.05f) {
				double angolo = scintilla[0] - 0.3 * t;
				float sx = (float) (x + Math.cos(angolo) * r * scintilla[1]);
				float sy = (float) (y + Math.sin(angolo) * r * scintilla[1]);
				stella(g, sx, sy, scintilla[3] * (0.6f + 0.4f * brillio), conAlfa(Color.WHITE, brillio));
			}
		}

		// L'oggetto sul cerchio, al più alle sue dimensioni vere: ingrandito, il disegno si sgranerebbe
		if (immagine != null) {
			float lato = raggio * 1.6f;
			float s = Math.min(1f, Math.min(lato / immagine.getWidth(), lato / immagine.getHeight())) * scala;
			int w = Math.round(immagine.getWidth() * s);
			int h = Math.round(immagine.getHeight() * s);
			Composite composito = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alfa))));
			g.drawImage(immagine, Math.round(x - w / 2f), Math.round(y - h / 2f), w, h, null);
			g.setComposite(composito);
		}

		// Il pannello con nome ed effetti resta al suo posto e sfuma per primo
		float testo = Math.min(1f, Math.max(0f, (t - ANTICIPAZIONE - APERTURA + 0.1f) / 0.3f))
				* (1f - Math.min(1f, uscita * 2f));
		if (testo > 0) {
			Composite composito = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, testo));
			g.drawImage(pannello, pannelloX, centroY + raggio + 12, null);
			g.setComposite(composito);
		}
	}

	private void disegnaRaggi(Graphics2D g, float x, float y, float base, float lunghezza, int numero, float rotazione,
							  Color coloreUnico, float alfa, float t, boolean alterni) {
		double semiampiezza = Math.PI / numero * 0.45;
		for (int i = 0; i < numero; i++) {
			double angolo = rotazione + i * 2 * Math.PI / numero;
			float l = lunghezza * (alterni && i % 2 == 1 ? 0.65f : 1f) * (1f + 0.06f * (float) Math.sin(4 * t + i));
			if (l <= base) {
				continue;
			}
			Path2D.Float raggioDiLuce = new Path2D.Float();
			raggioDiLuce.moveTo(x + Math.cos(angolo - semiampiezza) * base * 0.9, y + Math.sin(angolo - semiampiezza) * base * 0.9);
			raggioDiLuce.lineTo(x + Math.cos(angolo) * l, y + Math.sin(angolo) * l);
			raggioDiLuce.lineTo(x + Math.cos(angolo + semiampiezza) * base * 0.9, y + Math.sin(angolo + semiampiezza) * base * 0.9);
			raggioDiLuce.closePath();
			Color c = coloreUnico != null ? coloreUnico : coloriRaggi.get(i % coloriRaggi.size());
			g.setPaint(new RadialGradientPaint(x, y, l, new float[] {Math.min(0.99f, base / l), 1f},
					new Color[] {conAlfa(c, alfa), conAlfa(c, 0f)}));
			g.fill(raggioDiLuce);
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
	 * L'immagine dell'oggetto, dove c'è: gli ingredienti hanno l'icona della pergamena, i tipi senza immagine
	 * mostrano solo il cerchio.
	 */
	private static BufferedImage immagine(TipoArtefatto tipo) {
		if (tipo.isIngrediente()) {
			return ClasseIcona.PERGAMENA.getIcona();
		}
		switch (tipo) {
			case SPADA:
				return ClassiOggettoImmagine.getImmagine(TipoOggetto.SPADA);
			case SPADONE:
				return ClassiOggettoImmagine.getImmagine(TipoOggetto.SPADONE);
			case SCUDO:
				return ClassiOggettoImmagine.getImmagine(TipoOggetto.SCUDO);
			case ELMO:
				return ClassiOggettoImmagine.getImmagine(TipoOggetto.ELMO);
			case MASCHERA:
				return ClassiOggettoImmagine.getImmagine(TipoOggetto.MASCHERA);
			case ARMATURA:
				return ClassiOggettoImmagine.getImmagine(TipoOggetto.ARMATURA);
			case SCHINIERI:
				return ClassiOggettoImmagine.getImmagine(TipoOggetto.SCHINIERI);
			case ANELLO:
				return ClassiOggettoImmagine.getImmagine(TipoOggetto.ANELLO);
			default:
				return null;
		}
	}

	/**
	 * Il pannello scuro sotto il cerchio: il nome proprio (o il nome), il nome comune se c'è un nome proprio, la
	 * descrizione, livello e rarità, il set leggendario e i tipi dei suoi pezzi, poi gli incantamenti e i
	 * modificatori, verdi i bonus e rossi i malus. Tutto con il font medio, che si legge meglio del piccolo.
	 */
	private BufferedImage pannello(ArtefattoMD md, int larghezzaMassima) {
		DoomdarkFont font = DoomdarkFontMedium.getInstance();
		List<Image> righe = new ArrayList<>();
		String nomeProprio = md.getNomeProprio();
		if (nomeProprio != null) {
			righe.add(ImageCache.get(nomeProprio, font, DoomdarkColorModel.Color.YELLOW, larghezzaMassima));
			righe.add(ImageCache.get(maiuscola(md.getNome()), font, DoomdarkColorModel.Color.WHITE, larghezzaMassima));
		} else {
			righe.add(ImageCache.get(maiuscola(md.getNome()), font, DoomdarkColorModel.Color.YELLOW, larghezzaMassima));
		}
		if (md.getDescrizione() != null) {
			righe.add(ImageCache.get(maiuscola(md.getDescrizione()), font, DoomdarkColorModel.Color.LIGHT_GRAY, larghezzaMassima));
		}
		StringBuilder dati = new StringBuilder("Livello ").append(md.getLivello());
		if (md.getRarita() != TipoRaritaArtefatto.COMUNE) {
			dati.append(", ").append(md.getRarita().getNome());
		}
		if (md.getDanni() > 0) {
			dati.append(", danni ").append(md.getDanni());
		}
		righe.add(ImageCache.get(dati.toString(), font, DoomdarkColorModel.Color.WHITE, larghezzaMassima));
		RegoleSetLeggendari.descrizioneSet(md).ifPresent(set ->
				righe.add(ImageCache.get(set, font, DoomdarkColorModel.Color.YELLOW, larghezzaMassima)));
		RegoleSetLeggendari.tipiDelSet(md).ifPresent(tipi ->
				righe.add(ImageCache.get(tipi, font, DoomdarkColorModel.Color.YELLOW, larghezzaMassima)));
		List<String> incantamenti = new ArrayList<>();
		for (IncantamentoMD incantamento : md.getIncantamenti()) {
			incantamenti.add(incantamento(incantamento));
		}
		List<String> bonus = new ArrayList<>();
		List<String> malus = new ArrayList<>();
		for (ModificatoreAttributo modificatore : md.getModificatori()) {
			(modificatore.getQuantita() >= 0 ? bonus : malus).add(modificatore(modificatore));
		}
		aggiungi(righe, incantamenti, font, DoomdarkColorModel.Color.YELLOW, larghezzaMassima);
		aggiungi(righe, bonus, font, DoomdarkColorModel.Color.GREEN, larghezzaMassima);
		aggiungi(righe, malus, font, DoomdarkColorModel.Color.RED, larghezzaMassima);

		int margine = 8;
		int divario = 3;
		int larghezza = 0;
		int altezza = 0;
		for (Image riga : righe) {
			larghezza = Math.max(larghezza, riga.getWidth(null));
			altezza += riga.getHeight(null) + divario;
		}
		BufferedImage immagine = new BufferedImage(larghezza + 2 * margine, altezza - divario + 2 * margine, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(0, 0, 0, 200));
			g.fillRoundRect(0, 0, immagine.getWidth(), immagine.getHeight(), 12, 12);
			g.setColor(conAlfa(colore, 0.6f));
			g.drawRoundRect(0, 0, immagine.getWidth() - 1, immagine.getHeight() - 1, 12, 12);
			int y = margine;
			for (Image riga : righe) {
				g.drawImage(riga, (immagine.getWidth() - riga.getWidth(null)) / 2, y, null);
				y += riga.getHeight(null) + divario;
			}
		} finally {
			g.dispose();
		}
		return immagine;
	}

	private static void aggiungi(List<Image> righe, List<String> voci, DoomdarkFont font, DoomdarkColorModel.Color colore, int larghezzaMassima) {
		if (!voci.isEmpty()) {
			righe.add(ImageCache.get(String.join(", ", voci), font, colore, larghezzaMassima));
		}
	}

	private static String modificatore(ModificatoreAttributo modificatore) {
		String valore = String.format("%+.0f", modificatore.getQuantita());
		if (modificatore.getTipoModificatoreAttributo() == TipoModificatore.AUMENTO_PERCENTUALE) {
			valore += "%";
		}
		return modificatore.getTipoAttributo().getNome() + ' ' + valore;
	}

	private static String incantamento(IncantamentoMD incantamento) {
		StringBuilder sb = new StringBuilder(maiuscola(incantamento.getNomeIncantamento()));
		List<String> parti = new ArrayList<>();
		if (incantamento.getDannoBonusFisso() != 0) {
			parti.add(String.format("%+d", incantamento.getDannoBonusFisso()));
		}
		if (incantamento.getCoefficienteScala() != 0) {
			parti.add(String.format("%+.0f%%", incantamento.getCoefficienteScala() * 100));
		}
		if (!parti.isEmpty()) {
			sb.append(' ').append(String.join(" e ", parti));
		}
		return sb.toString();
	}

	private static String maiuscola(String testo) {
		return testo.isEmpty() ? testo : Character.toUpperCase(testo.charAt(0)) + testo.substring(1);
	}

	private static Color conAlfa(Color c, float alfa) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, Math.round(255 * alfa))));
	}

	private static float uscitaMorbida(float u) {
		return 1f - (1f - u) * (1f - u);
	}

	/**
	 * Arriva un po' oltre 1 e torna indietro (easing "back")
	 */
	private static float rimbalzo(float u) {
		float c = 1.7f;
		float v = u - 1f;
		return 1f + (c + 1f) * v * v * v + c * v * v;
	}
}
