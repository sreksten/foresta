package com.threeamigos.foresta.ui.testo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Un font Doomdark scritto in un'immagine (GIF o PNG): i glifi di tutti i caratteri di {@link CaratteriDelFont} in tre
 * righe, bianchi o in tonalità di grigio su nero, ognuno in un riquadro, tutti della stessa misura. Il livello di un
 * pixel è il suo grigio (se l'immagine ha un canale alfa, come un PNG, il grigio per l'alfa): 0, cioè il nero puro, è
 * trasparente, 255 è il colore pieno del testo e i grigi sfumano il colore sullo sfondo. La prima riga e la prima colonna dell'immagine sono rosse, e ogni riquadro ha a destra e in basso la sua linea
 * rossa: l'immagine non si cerca la griglia, la calcola. Dall'altezza tolta la linea in alto e divisa per le righe
 * vengono la misura di una riga di riquadri e quella dei riquadri; allo stesso modo dalla larghezza e dalle colonne.
 * <p>
 * Un riquadro con un pixel rosso (255, 0, 0) è un glifo che manca (un segnaposto da ridisegnare): il carattere non è
 * nella mappa, e chi lo chiede ottiene l'eccezione di sempre ({@link DoomdarkFont.UnsupportedCharacterException}). Un
 * riquadro con un pixel di un colore che non è né rosso né una tonalità di grigio è un errore dell'immagine. La
 * larghezza di un glifo è la posizione del suo pixel acceso (livello maggiore di zero) più a destra, più uno; lo spazio,
 * che non ha pixel accesi, ha la larghezza che si dice a chi carica l'immagine.
 */
public final class ImmagineDelFont {

	private static final int ROSSO = 0xFF0000;

	private final int altezza;
	private final Map<Character, GlifoDelFont> glifi;
	private final List<Character> mancanti;

	private ImmagineDelFont(int altezza, Map<Character, GlifoDelFont> glifi, List<Character> mancanti) {
		this.altezza = altezza;
		this.glifi = Collections.unmodifiableMap(glifi);
		this.mancanti = Collections.unmodifiableList(mancanti);
	}

	/**
	 * Legge l'immagine del font da una risorsa, per esempio {@code /com/threeamigos/foresta/img/font/doomdark_small.gif}.
	 *
	 * @param larghezzaDelloSpazio la larghezza dello spazio, che non si può misurare sui pixel
	 */
	public static ImmagineDelFont carica(String risorsa, int larghezzaDelloSpazio) {
		try (InputStream flusso = ImmagineDelFont.class.getResourceAsStream(risorsa)) {
			if (flusso == null) {
				throw new IllegalStateException("Risorsa del font non trovata: " + risorsa);
			}
			return daImmagine(ImageIO.read(flusso), larghezzaDelloSpazio);
		} catch (IOException e) {
			throw new IllegalStateException("Risorsa del font non leggibile: " + risorsa, e);
		}
	}

	/**
	 * Legge i glifi da un'immagine già in memoria, con la stessa griglia di carica.
	 */
	public static ImmagineDelFont daImmagine(BufferedImage immagine, int larghezzaDelloSpazio) {
		int righe = CaratteriDelFont.RIGHE.length;
		int colonne = CaratteriDelFont.colonne();
		if ((immagine.getHeight() - 1) % righe != 0 || (immagine.getWidth() - 1) % colonne != 0) {
			throw new IllegalStateException("L'immagine del font, " + immagine.getWidth() + "x" + immagine.getHeight()
					+ ", non è una griglia di " + colonne + " colonne e " + righe + " righe con una linea sola attorno a ogni riquadro");
		}
		int passoX = (immagine.getWidth() - 1) / colonne;
		int passoY = (immagine.getHeight() - 1) / righe;
		// L'ultima colonna e l'ultima riga di ogni riquadro sono la linea rossa di separazione
		int larghezzaRiquadro = passoX - 1;
		int altezzaRiquadro = passoY - 1;

		Map<Character, GlifoDelFont> glifi = new LinkedHashMap<>();
		List<Character> mancanti = new ArrayList<>();
		for (int r = 0; r < righe; r++) {
			String riga = CaratteriDelFont.RIGHE[r];
			for (int c = 0; c < riga.length(); c++) {
				char carattere = riga.charAt(c);
				GlifoDelFont glifo = leggiRiquadro(immagine, carattere, 1 + c * passoX, 1 + r * passoY,
						larghezzaRiquadro, altezzaRiquadro, larghezzaDelloSpazio);
				if (glifo == null) {
					mancanti.add(carattere);
				} else {
					glifi.put(carattere, glifo);
				}
			}
		}
		return new ImmagineDelFont(altezzaRiquadro, glifi, mancanti);
	}

	/**
	 * @return il glifo, o null se manca: ha un pixel rosso, oppure non ha pixel accesi e non è lo spazio
	 */
	private static GlifoDelFont leggiRiquadro(BufferedImage immagine, char carattere, int x0, int y0,
											  int larghezza, int altezza, int larghezzaDelloSpazio) {
		byte[] livelli = new byte[larghezza * altezza];
		int ultimaColonna = -1;
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				int argb = immagine.getRGB(x0 + x, y0 + y);
				int trasparenza = argb >>> 24;
				if (trasparenza == 0) {
					continue; // trasparente: il colore non conta
				}
				int rgb = argb & 0xFFFFFF;
				if (rgb == ROSSO) {
					return null;
				}
				int rosso = rgb >> 16;
				if (rosso != ((rgb >> 8) & 0xFF) || rosso != (rgb & 0xFF)) {
					throw new IllegalStateException("Pixel di colore non previsto (#" + Integer.toHexString(rgb | 0x1000000).substring(1)
							+ ") nel riquadro di '" + carattere + "' alla posizione " + x + "," + y + ": ci vogliono grigi o rosso");
				}
				// Un'immagine senza canale alfa ha trasparenza 255: il livello è il grigio
				int livello = rosso * trasparenza / 255;
				livelli[y * larghezza + x] = (byte) livello;
				if (livello > 0) {
					ultimaColonna = Math.max(ultimaColonna, x);
				}
			}
		}
		int larghezzaDelGlifo = ultimaColonna + 1;
		if (larghezzaDelGlifo == 0) {
			if (carattere != ' ') {
				return null;
			}
			return new GlifoDelFont(larghezzaDelloSpazio, altezza, new byte[larghezzaDelloSpazio * altezza]);
		}
		byte[] tagliati = new byte[larghezzaDelGlifo * altezza];
		for (int y = 0; y < altezza; y++) {
			System.arraycopy(livelli, y * larghezza, tagliati, y * larghezzaDelGlifo, larghezzaDelGlifo);
		}
		return new GlifoDelFont(larghezzaDelGlifo, altezza, tagliati);
	}

	/**
	 * L'altezza di tutti i glifi, cioè quella di un riquadro
	 */
	public int getAltezza() {
		return altezza;
	}

	/**
	 * I glifi presenti, per carattere. Quelli che mancano non ci sono.
	 */
	public Map<Character, GlifoDelFont> getGlifi() {
		return glifi;
	}

	/**
	 * I caratteri che nell'immagine sono ancora segnaposto (o riquadri vuoti)
	 */
	public List<Character> getCaratteriMancanti() {
		return mancanti;
	}

	public boolean supportaIlCarattere(char c) {
		return glifi.containsKey(c);
	}

	/**
	 * @throws DoomdarkFont.UnsupportedCharacterException se il carattere non c'è, come per i font di oggi
	 */
	public GlifoDelFont getGlifo(char c) {
		GlifoDelFont glifo = glifi.get(c);
		if (glifo == null) {
			throw new DoomdarkFont.UnsupportedCharacterException(c);
		}
		return glifo;
	}

	public int getLarghezzaDelGlifo(char c) {
		return getGlifo(c).getLarghezza();
	}
}
