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
public class SpriteAnnuncioGlobale implements SpriteInterface {

	private static final float DURATA_IN_SECONDI = 2.0f;
	private static final float DURATA_FADE_IN_SECONDI = DURATA_IN_SECONDI / 2;
	private static final float SCALA_INIZIALE = 1.0f;
	private static final float SCALA_FINALE = 1.8f;

	private static final int SPAZIATURA_CARATTERI = 1;
	private static final int LARGHEZZA_SPAZIO = 10;
	private static final int DIVARIO_RIGHE = 4;
	private static final int DIVARIO_GRUPPI = 12;
	private static final int SCOSTAMENTO_OMBRA = 2;
	private static final Color COLORE_OMBRA = new Color(0, 0, 0, 160);

	private final String titolo;
	private final String descrizione;
	private final BufferedImage immagine;
	private final int larghezzaSchermo;
	private final int altezzaSchermo;
	private float secondiTrascorsi;
	private boolean attivo;

	SpriteAnnuncioGlobale(String titolo, String descrizione, int larghezzaSchermo, int altezzaSchermo) {
		this.titolo = titolo;
		this.descrizione = descrizione;
		this.larghezzaSchermo = larghezzaSchermo;
		this.altezzaSchermo = altezzaSchermo;
		int larghezzaMassima = larghezzaSchermo / 2;
		// Il font non supporta i caratteri minuscoli
		List<String> righeEtichetta = spezzaInRighe(this.titolo.toUpperCase(), larghezzaMassima);
		List<String> righeNome = spezzaInRighe(descrizione.toUpperCase(), larghezzaMassima);
		immagine = costruisciImmagine(righeEtichetta, righeNome);
		attivo = true;
	}

	public String getTitolo() {
		return titolo;
	}

	public String getDescrizione() {
		return descrizione;
	}

	private static List<String> spezzaInRighe(String testo, int larghezzaMassima) {
		List<String> righe = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(testo, " ");
		StringBuilder riga = new StringBuilder();
		int larghezzaRiga = 0;
		while (st.hasMoreTokens()) {
			String parola = st.nextToken();
			int larghezzaParola = larghezzaParola(parola);
			if (larghezzaRiga > 0 && larghezzaRiga + LARGHEZZA_SPAZIO + larghezzaParola > larghezzaMassima) {
				righe.add(riga.toString());
				riga = new StringBuilder();
				larghezzaRiga = 0;
			}
			if (riga.length() > 0) {
				riga.append(" ");
				larghezzaRiga += LARGHEZZA_SPAZIO;
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
			larghezza += larghezzaCarattere(parola.charAt(i)) + SPAZIATURA_CARATTERI;
		}
		return larghezza;
	}

	private static int larghezzaCarattere(char c) {
		BufferedImage glifo = glifo(c);
		if (glifo != null) {
			return glifo.getWidth();
		}
		if (c == ' ') {
			return LARGHEZZA_SPAZIO;
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

		int altezzaRiga = altezzaMassimaGlifi(tutteLeRighe) + DIVARIO_RIGHE;

		int larghezzaTesto = 0;
		for (String riga : tutteLeRighe) {
			larghezzaTesto = Math.max(larghezzaTesto, larghezzaParola(riga.replace(" ", "")) + spaziInRiga(riga) * LARGHEZZA_SPAZIO);
		}
		int altezzaTesto = righeEtichetta.size() * altezzaRiga + DIVARIO_GRUPPI + righeNome.size() * altezzaRiga;

		int larghezzaTotale = larghezzaTesto + SCOSTAMENTO_OMBRA * 2;
		int altezzaTotale = altezzaTesto + SCOSTAMENTO_OMBRA * 2;

		BufferedImage testo = new BufferedImage(larghezzaTotale, altezzaTotale, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gTesto = testo.createGraphics();
		int y = SCOSTAMENTO_OMBRA;
		for (int i = 0; i < righeEtichetta.size(); i++) {
			disegnaRigaCentrata(gTesto, righeEtichetta.get(i), larghezzaTotale, y);
			y += altezzaRiga;
		}
		y += DIVARIO_GRUPPI - DIVARIO_RIGHE;
		for (int i = 0; i < righeNome.size(); i++) {
			disegnaRigaCentrata(gTesto, righeNome.get(i), larghezzaTotale, y);
			y += altezzaRiga;
		}
		gTesto.dispose();

		BufferedImage ombra = creaSagomaScura(testo);

		BufferedImage risultato = new BufferedImage(larghezzaTotale, altezzaTotale, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = risultato.createGraphics();
		int[] scostamenti = {-SCOSTAMENTO_OMBRA, 0, SCOSTAMENTO_OMBRA};
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
		int larghezzaRiga = larghezzaParola(riga.replace(" ", "")) + spaziInRiga(riga) * LARGHEZZA_SPAZIO;
		int x = (larghezzaTotale - larghezzaRiga) >> 1;
		for (int i = 0; i < riga.length(); i++) {
			char c = riga.charAt(i);
			BufferedImage glifo = glifo(c);
			if (glifo != null) {
				g.drawImage(glifo, x, y, null);
			}
			x += larghezzaCarattere(c) + SPAZIATURA_CARATTERI;
		}
	}

	private static BufferedImage creaSagomaScura(BufferedImage sorgente) {
		BufferedImage sagoma = new BufferedImage(sorgente.getWidth(), sorgente.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = sagoma.createGraphics();
		g.drawImage(sorgente, 0, 0, null);
		g.setComposite(AlphaComposite.SrcIn);
		g.setColor(COLORE_OMBRA);
		g.fillRect(0, 0, sagoma.getWidth(), sagoma.getHeight());
		g.dispose();
		return sagoma;
	}

	@Override
	public void anima(Graphics2D g) {
		if (!attivo) {
			return;
		}
		if (secondiTrascorsi >= DURATA_IN_SECONDI) {
			attivo = false;
			return;
		}

		float progresso = secondiTrascorsi / DURATA_IN_SECONDI;
		float scala = SCALA_INIZIALE + (SCALA_FINALE - SCALA_INIZIALE) * progresso;
		float alpha;
		if (secondiTrascorsi <= DURATA_FADE_IN_SECONDI) {
			alpha = 1.0f;
		} else {
			alpha = Math.max(0.0f, 1.0f - (secondiTrascorsi - DURATA_FADE_IN_SECONDI) / (DURATA_IN_SECONDI - DURATA_FADE_IN_SECONDI));
		}

		Composite compositeOriginale = g.getComposite();
		Object interpolazioneOriginale = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		int larghezzaScalata = Math.round(immagine.getWidth() * scala);
		int altezzaScalata = Math.round(immagine.getHeight() * scala);
		int x = (larghezzaSchermo - larghezzaScalata) >> 1;
		int y = (altezzaSchermo - altezzaScalata) >> 1;
		g.drawImage(immagine, x, y, larghezzaScalata, altezzaScalata, null);

		g.setComposite(compositeOriginale);
		if (interpolazioneOriginale != null) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolazioneOriginale);
		}

		secondiTrascorsi += 1f / 30;
	}

	@Override
	public boolean isAttivo() {
		return attivo;
	}
}
