package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Annuncia a schermo, con le lettere di {@link ImageCache}, che una missione è
 * disponibile o è stata completata. A differenza degli altri sprite non si sposta:
 * resta centrato sullo schermo e si ingrandisce mentre sfuma.
 */
class SpriteMissione implements SpriteInterface {

	//private static final long DURATA_MS = 3500L;
	private static final long DURATA_MS = 2000L;
	private static final long DURATA_FADE_MS = DURATA_MS / 2;
	private static final float SCALA_INIZIALE = 1.0f;
	private static final float SCALA_FINALE = 1.8f;

	private static final int CHAR_SPACING = 1;
	private static final int SPACE_WIDTH = 10;
	private static final int RIGHE_GAP = 4;
	private static final int GRUPPI_GAP = 12;
	private static final int OMBRA_SCOSTAMENTO = 2;
	private static final Color OMBRA_COLORE = new Color(0, 0, 0, 160);

	private final BufferedImage image;
	private final int larghezzaSchermo;
	private final int altezzaSchermo;
	private long inizio = -1;
	private boolean active;

	SpriteMissione(String etichetta, String nomeMissione, int larghezzaSchermo, int altezzaSchermo) {
		this.larghezzaSchermo = larghezzaSchermo;
		this.altezzaSchermo = altezzaSchermo;
		int larghezzaMassima = larghezzaSchermo / 2;
		List<String> righeEtichetta = spezzaInRighe(etichetta.toUpperCase(), larghezzaMassima);
		List<String> righeNome = spezzaInRighe(nomeMissione.toUpperCase(), larghezzaMassima);
		image = costruisciImmagine(righeEtichetta, righeNome);
		active = true;
	}

	private static List<String> spezzaInRighe(String testo, int larghezzaMassima) {
		List<String> righe = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(testo, " ");
		StringBuilder riga = new StringBuilder();
		int larghezzaRiga = 0;
		while (st.hasMoreTokens()) {
			String parola = st.nextToken();
			int larghezzaParola = larghezzaParola(parola);
			if (larghezzaRiga > 0 && larghezzaRiga + SPACE_WIDTH + larghezzaParola > larghezzaMassima) {
				righe.add(riga.toString());
				riga = new StringBuilder();
				larghezzaRiga = 0;
			}
			if (riga.length() > 0) {
				riga.append(" ");
				larghezzaRiga += SPACE_WIDTH;
			}
			riga.append(parola);
			larghezzaRiga += larghezzaParola;
		}
		if (riga.length() > 0) {
			righe.add(riga.toString());
		}
		return righe;
	}

	private static int larghezzaParola(String parola) {
		int larghezza = 0;
		for (int i = 0; i < parola.length(); i++) {
			larghezza += larghezzaCarattere(parola.charAt(i)) + CHAR_SPACING;
		}
		return larghezza;
	}

	private static int larghezzaCarattere(char c) {
		BufferedImage glifo = glifo(c);
		if (glifo != null) {
			return glifo.getWidth();
		}
		if (c == ' ') {
			return SPACE_WIDTH;
		}
		return 1;
	}

	private static BufferedImage glifo(char c) {
		if (c >= 'A' && c <= 'Z') {
			return ImageCache.lettere[c - 'A'];
		}
		if (c >= '0' && c <= '9') {
			return ImageCache.cifre[c - '0'];
		}
		if (c == '\'') {
			return ImageCache.apostrofo;
		}
		if (c == ',') {
			return ImageCache.virgola;
		}
		if (c == '.') {
			return ImageCache.punto;
		}
		if (c == '?') {
			return ImageCache.puntodd;
		}
		return null;
	}

	private static BufferedImage costruisciImmagine(List<String> righeEtichetta, List<String> righeNome) {
		List<String> tutteLeRighe = new ArrayList<>(righeEtichetta.size() + righeNome.size());
		tutteLeRighe.addAll(righeEtichetta);
		tutteLeRighe.addAll(righeNome);

		int altezzaRiga = altezzaMassimaGlifi(tutteLeRighe) + RIGHE_GAP;

		int larghezzaTesto = 0;
		for (String riga : tutteLeRighe) {
			larghezzaTesto = Math.max(larghezzaTesto, larghezzaParola(riga.replace(" ", "")) + spaziInRiga(riga) * SPACE_WIDTH);
		}
		int altezzaTesto = righeEtichetta.size() * altezzaRiga + GRUPPI_GAP + righeNome.size() * altezzaRiga;

		int larghezzaTotale = larghezzaTesto + OMBRA_SCOSTAMENTO * 2;
		int altezzaTotale = altezzaTesto + OMBRA_SCOSTAMENTO * 2;

		BufferedImage testo = new BufferedImage(larghezzaTotale, altezzaTotale, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gTesto = testo.createGraphics();
		int y = OMBRA_SCOSTAMENTO;
		for (int i = 0; i < righeEtichetta.size(); i++) {
			disegnaRigaCentrata(gTesto, righeEtichetta.get(i), larghezzaTotale, y);
			y += altezzaRiga;
		}
		y += GRUPPI_GAP - RIGHE_GAP;
		for (int i = 0; i < righeNome.size(); i++) {
			disegnaRigaCentrata(gTesto, righeNome.get(i), larghezzaTotale, y);
			y += altezzaRiga;
		}
		gTesto.dispose();

		BufferedImage ombra = creaSagomaScura(testo);

		BufferedImage risultato = new BufferedImage(larghezzaTotale, altezzaTotale, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = risultato.createGraphics();
		int[] scostamenti = {-OMBRA_SCOSTAMENTO, 0, OMBRA_SCOSTAMENTO};
		for (int dx : scostamenti) {
			for (int dy : scostamenti) {
				if (dx != 0 || dy != 0) {
					g.drawImage(ombra, dx, dy, null);
				}
			}
		}
		g.drawImage(testo, 0, 0, null);
		g.dispose();

		return risultato;
	}

	private static int spaziInRiga(String riga) {
		int spazi = 0;
		for (int i = 0; i < riga.length(); i++) {
			if (riga.charAt(i) == ' ') {
				spazi++;
			}
		}
		return spazi;
	}

	private static int altezzaMassimaGlifi(List<String> righe) {
		int altezza = 0;
		for (String riga : righe) {
			for (int i = 0; i < riga.length(); i++) {
				BufferedImage glifo = glifo(riga.charAt(i));
				if (glifo != null) {
					altezza = Math.max(altezza, glifo.getHeight());
				}
			}
		}
		return altezza;
	}

	private static void disegnaRigaCentrata(Graphics2D g, String riga, int larghezzaTotale, int y) {
		int larghezzaRiga = larghezzaParola(riga.replace(" ", "")) + spaziInRiga(riga) * SPACE_WIDTH;
		int x = (larghezzaTotale - larghezzaRiga) >> 1;
		for (int i = 0; i < riga.length(); i++) {
			char c = riga.charAt(i);
			BufferedImage glifo = glifo(c);
			if (glifo != null) {
				g.drawImage(glifo, x, y, null);
			}
			x += larghezzaCarattere(c) + CHAR_SPACING;
		}
	}

	private static BufferedImage creaSagomaScura(BufferedImage sorgente) {
		BufferedImage sagoma = new BufferedImage(sorgente.getWidth(), sorgente.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = sagoma.createGraphics();
		g.drawImage(sorgente, 0, 0, null);
		g.setComposite(AlphaComposite.SrcIn);
		g.setColor(OMBRA_COLORE);
		g.fillRect(0, 0, sagoma.getWidth(), sagoma.getHeight());
		g.dispose();
		return sagoma;
	}

	@Override
	public void animate(Graphics2D g) {
		if (!active) {
			return;
		}
		long adesso = System.currentTimeMillis();
		if (inizio < 0) {
			inizio = adesso;
		}
		long trascorsi = adesso - inizio;
		if (trascorsi >= DURATA_MS) {
			active = false;
			return;
		}

		float progresso = trascorsi / (float) DURATA_MS;
		float scala = SCALA_INIZIALE + (SCALA_FINALE - SCALA_INIZIALE) * progresso;
		float alpha;
		if (trascorsi <= DURATA_FADE_MS) {
			alpha = 1.0f;
		} else {
			alpha = Math.max(0.0f, 1.0f - (trascorsi - DURATA_FADE_MS) / (float) (DURATA_MS - DURATA_FADE_MS));
		}

		Composite compositeOriginale = g.getComposite();
		Object interpolazioneOriginale = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		int larghezzaScalata = Math.round(image.getWidth() * scala);
		int altezzaScalata = Math.round(image.getHeight() * scala);
		int x = (larghezzaSchermo - larghezzaScalata) >> 1;
		int y = (altezzaSchermo - altezzaScalata) >> 1;
		g.drawImage(image, x, y, larghezzaScalata, altezzaScalata, null);

		g.setComposite(compositeOriginale);
		if (interpolazioneOriginale != null) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolazioneOriginale);
		}
	}

	@Override
	public boolean isActive() {
		return active;
	}
}
