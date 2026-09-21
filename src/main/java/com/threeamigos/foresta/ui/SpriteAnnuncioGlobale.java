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
public class SpriteAnnuncioGlobale extends SpriteBase {

	private static final float DURATA_IN_SECONDI = 2.0f;
	private static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = DURATA_IN_SECONDI / 2;
	private static final Color COLORE_OMBRA = new Color(0, 0, 0, 160);
	private static final int SCOSTAMENTO_OMBRA = 2;
	private static final float SCALA_INIZIALE = 1.0f;
	private static final float SCALA_FINALE = 1.8f;

	private static final int SPAZIATURA_CARATTERI = 1;
	private static final int LARGHEZZA_SPAZIO = 10;
	private static final int DIVARIO_RIGHE = 4;
	private static final int DIVARIO_GRUPPI = 12;

	private final String titolo;
	private final String descrizione;
	private final int larghezzaSchermo;
	private final int altezzaSchermo;

	SpriteAnnuncioGlobale(String titolo, String descrizione, int larghezzaSchermo, int altezzaSchermo) {
		this.titolo = titolo;
		this.descrizione = descrizione;
		this.larghezzaSchermo = larghezzaSchermo;
		this.altezzaSchermo = altezzaSchermo;
		inizializza(buildImage(),
				DURATA_IN_SECONDI, ATTACCO_DISSOLVENZA_DOPO_SECONDI,
				COLORE_OMBRA, SCOSTAMENTO_OMBRA,
				0, 0,
				0, 0,
				SCALA_INIZIALE, SCALA_FINALE);
	}

	public String getTitolo() {
		return titolo;
	}

	public String getDescrizione() {
		return descrizione;
	}

	@Override
	protected BufferedImage buildImage() {
		return costruisciImmagine(righe(titolo), righe(descrizione));
	}

	private List<String> righe(String testo) {
		int larghezzaMassima = larghezzaSchermo / 2;
		// Il font non supporta i caratteri minuscoli
		return spezzaInRighe(testo.toUpperCase(), larghezzaMassima);
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

		BufferedImage testo = new BufferedImage(larghezzaTesto, altezzaTesto, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gTesto = testo.createGraphics();
		int y = 0;
		for (int i = 0; i < righeEtichetta.size(); i++) {
			disegnaRigaCentrata(gTesto, righeEtichetta.get(i), larghezzaTesto, y);
			y += altezzaRiga;
		}
		y += DIVARIO_GRUPPI - DIVARIO_RIGHE;
		for (int i = 0; i < righeNome.size(); i++) {
			disegnaRigaCentrata(gTesto, righeNome.get(i), larghezzaTesto, y);
			y += altezzaRiga;
		}
		gTesto.dispose();

		return testo;
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

	@Override
	protected float calcolaAlpha(float secondiTrascorsi) {
		return dissolvenzaLineareConSoglia(secondiTrascorsi, momentoInizioFade, durataInSecondi);
	}

	@Override
	protected void disegna(Graphics2D g, float x, float y, float scala) {
		int larghezzaScalata = Math.round(immagine.getWidth() * scala);
		int altezzaScalata = Math.round(immagine.getHeight() * scala);
		int disegnaX = (larghezzaSchermo - larghezzaScalata) >> 1;
		int disegnaY = (altezzaSchermo - altezzaScalata) >> 1;
		g.drawImage(immagine, disegnaX, disegnaY, larghezzaScalata, altezzaScalata, null);
	}
}
