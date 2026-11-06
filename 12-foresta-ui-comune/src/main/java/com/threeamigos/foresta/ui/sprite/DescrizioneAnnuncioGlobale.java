package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Che cosa mostra uno sprite di annuncio globale, senza immagini: un'etichetta (per esempio "Missione completata") e sotto un messaggio (il nome della
 * missione), nell'alfabeto grande, a capo entro metà dello schermo e centrati, che restano al centro dello schermo e si ingrandiscono mentre sfumano. Dice
 * dove sta ogni riga nell'immagine composta. Il movimento e la dissolvenza sono di {@link ModelloDelloSprite}.
 */
public final class DescrizioneAnnuncioGlobale {

	public static final float DURATA_IN_SECONDI = 2.0f;
	public static final float ATTACCO_DISSOLVENZA_DOPO_SECONDI = DURATA_IN_SECONDI / 2;
	public static final float SCALA_INIZIALE = 1.0f;
	public static final float SCALA_FINALE = 1.8f;
	public static final int SCOSTAMENTO_OMBRA = 2;
	/**
	 * L'ombra, ARGB: nero con un po' di trasparenza
	 */
	public static final int OMBRA_ARGB = (160 << 24);

	private static final int DIVARIO_RIGHE = 4;
	private static final int DIVARIO_GRUPPI = 12;

	/**
	 * Una riga dell'immagine composta: il testo e dove sta (angolo in alto a sinistra)
	 */
	public static final class Riga {
		private final String testo;
		private final int x;
		private final int y;

		private Riga(String testo, int x, int y) {
			this.testo = testo;
			this.x = x;
			this.y = y;
		}

		public String getTesto() {
			return testo;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

	private final String titolo;
	private final String descrizione;
	private final int larghezzaSchermo;
	private final int altezzaSchermo;
	private final boolean sostituibile;
	private final List<Riga> righe;
	private final int larghezza;
	private final int altezza;

	public DescrizioneAnnuncioGlobale(String titolo, String descrizione, int larghezzaSchermo, int altezzaSchermo, boolean sostituibile) {
		this.titolo = titolo;
		this.descrizione = descrizione;
		this.larghezzaSchermo = larghezzaSchermo;
		this.altezzaSchermo = altezzaSchermo;
		this.sostituibile = sostituibile;
		int larghezzaMassima = larghezzaSchermo / 2;
		List<String> righeEtichetta = LayoutDelTestoGrande.righe(titolo, larghezzaMassima);
		List<String> righeNome = LayoutDelTestoGrande.righe(descrizione, larghezzaMassima);
		int altezzaRiga = LayoutDelTestoGrande.ALTEZZA_GLIFO + DIVARIO_RIGHE;
		int larghezzaTesto = 0;
		for (String riga : righeEtichetta) {
			larghezzaTesto = Math.max(larghezzaTesto, LayoutDelTestoGrande.larghezza(riga));
		}
		for (String riga : righeNome) {
			larghezzaTesto = Math.max(larghezzaTesto, LayoutDelTestoGrande.larghezza(riga));
		}
		// L'ultima riga ha in più lo spazio di accenti e cediglie
		this.larghezza = larghezzaTesto;
		this.altezza = righeEtichetta.size() * altezzaRiga + DIVARIO_GRUPPI + righeNome.size() * altezzaRiga
				+ LayoutDelTestoGrande.altezzaConAccenti() - LayoutDelTestoGrande.ALTEZZA_GLIFO;
		List<Riga> composte = new ArrayList<>();
		int y = 0;
		for (String riga : righeEtichetta) {
			composte.add(new Riga(riga, (larghezzaTesto - LayoutDelTestoGrande.larghezza(riga)) >> 1, y));
			y += altezzaRiga;
		}
		y += DIVARIO_GRUPPI - DIVARIO_RIGHE;
		for (String riga : righeNome) {
			composte.add(new Riga(riga, (larghezzaTesto - LayoutDelTestoGrande.larghezza(riga)) >> 1, y));
			y += altezzaRiga;
		}
		this.righe = Collections.unmodifiableList(composte);
	}

	public String getTitolo() {
		return titolo;
	}

	public String getDescrizione() {
		return descrizione;
	}

	/**
	 * Un annuncio sostituibile, quando ne arriva un altro dello stesso genere, non aspetta il suo turno né finisce la sua animazione: il nuovo prende subito
	 * il suo posto
	 */
	public boolean isSostituibile() {
		return sostituibile;
	}

	public int getLarghezzaSchermo() {
		return larghezzaSchermo;
	}

	public int getAltezzaSchermo() {
		return altezzaSchermo;
	}

	/**
	 * Le righe dell'immagine composta, l'etichetta e poi il messaggio
	 */
	public List<Riga> getRighe() {
		return righe;
	}

	/**
	 * La larghezza dell'immagine composta (della sua riga più larga)
	 */
	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
	}

	/**
	 * Il testo composto in pixel, senza ombra: le righe nei loro posti
	 */
	public ImmagineDiPixel immagine() {
		int w = Math.max(1, larghezza);
		int h = Math.max(1, altezza);
		int[] pixel = new int[w * h];
		for (Riga riga : righe) {
			LayoutDelTestoGrande.scrivi(pixel, w, h, riga.getTesto(), riga.getX(), riga.getY());
		}
		return new ImmagineDiPixel(w, h, pixel);
	}

	/**
	 * Il testo con l'ombra attorno: una sagoma scura e un po' trasparente ({@link #OMBRA_ARGB}) spostata negli otto versi di {@link #SCOSTAMENTO_OMBRA}
	 * pixel, sotto il testo; l'immagine è più larga e alta di due scostamenti
	 */
	public ImmagineDiPixel immagineConOmbra() {
		ImmagineDiPixel testo = immagine();
		int s = SCOSTAMENTO_OMBRA;
		int w = testo.getLarghezza() + 2 * s;
		int h = testo.getAltezza() + 2 * s;
		int[] pixel = new int[w * h];
		int[] scostamenti = {-s, 0, s};
		for (int dx : scostamenti) {
			for (int dy : scostamenti) {
				if (dx == 0 && dy == 0) {
					continue;
				}
				for (int y = 0; y < testo.getAltezza(); y++) {
					for (int x = 0; x < testo.getLarghezza(); x++) {
						int alfa = testo.getArgb(x, y) >>> 24;
						if (alfa != 0) {
							int ombra = ((OMBRA_ARGB >>> 24) * alfa / 255) << 24;
							int i = (s + dy + y) * w + s + dx + x;
							pixel[i] = LayoutDelTestoGrande.sopra(ombra, pixel[i]);
						}
					}
				}
			}
		}
		for (int y = 0; y < testo.getAltezza(); y++) {
			for (int x = 0; x < testo.getLarghezza(); x++) {
				int argb = testo.getArgb(x, y);
				if ((argb >>> 24) != 0) {
					int i = (s + y) * w + s + x;
					pixel[i] = LayoutDelTestoGrande.sopra(argb, pixel[i]);
				}
			}
		}
		return new ImmagineDiPixel(w, h, pixel);
	}

	/**
	 * Dove sta, a quella scala, l'angolo in alto a sinistra dell'immagine composta: al centro dello schermo
	 */
	public int xAllaScala(float scala, int larghezzaConOmbra) {
		return (larghezzaSchermo - Math.round(larghezzaConOmbra * scala)) >> 1;
	}

	public int yAllaScala(float scala, int altezzaConOmbra) {
		return (altezzaSchermo - Math.round(altezzaConOmbra * scala)) >> 1;
	}
}
