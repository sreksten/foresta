package com.threeamigos.foresta.ui.immagini;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Legge un PNG in una {@link ImmagineDiPixel}, usando solo il JDK senza <code>javax.imageio</code> (che su Android non c'è): serve a
 * chi deve guardare i pixel di un'immagine del gioco senza dipendere dal toolkit grafico. Capisce quel che c'è nelle immagini del gioco:
 * grigi, RGB, palette (anche a 1, 2 e 4 bit), grigi e RGB con alfa, trasparenza per colore o per palette (<code>tRNS</code>), a 8 bit
 * per canale e non interlacciate; il resto lo rifiuta con un messaggio.
 */
public final class LetturaPng {

	private static final byte[] FIRMA = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

	private LetturaPng() {
	}

	/**
	 * Legge una risorsa, per esempio {@code /com/threeamigos/foresta/img/font/doomdark_small.png}
	 *
	 * @throws IllegalStateException se la risorsa non c'è o non è un PNG leggibile
	 */
	public static ImmagineDiPixel leggiRisorsa(String risorsa) {
		try (InputStream flusso = LetturaPng.class.getResourceAsStream(risorsa)) {
			if (flusso == null) {
				throw new IllegalStateException("Risorsa non trovata: " + risorsa);
			}
			return leggi(flusso);
		} catch (IOException e) {
			throw new IllegalStateException("Risorsa non leggibile: " + risorsa, e);
		}
	}

	/**
	 * Legge un PNG da un flusso
	 */
	public static ImmagineDiPixel leggi(InputStream flusso) throws IOException {
		DataInputStream in = new DataInputStream(flusso);
		byte[] firma = new byte[FIRMA.length];
		in.readFully(firma);
		for (int i = 0; i < FIRMA.length; i++) {
			if (firma[i] != FIRMA[i]) {
				throw new IOException("Non è un PNG");
			}
		}
		int larghezza = 0;
		int altezza = 0;
		int profondita = 0;
		int tipo = 0;
		byte[] palette = null;
		byte[] alfaPalette = null;
		int[] coloreTrasparente = null;
		ByteArrayOutputStream compressi = new ByteArrayOutputStream();
		boolean fine = false;
		while (!fine) {
			int lunghezza = in.readInt();
			byte[] nome = new byte[4];
			in.readFully(nome);
			byte[] dati = new byte[lunghezza];
			in.readFully(dati);
			in.readInt(); // il controllo d'errore: i file del gioco sono nostri
			switch (new String(nome, "US-ASCII")) {
				case "IHDR":
					larghezza = intero(dati, 0);
					altezza = intero(dati, 4);
					profondita = dati[8] & 0xFF;
					tipo = dati[9] & 0xFF;
					if (dati[12] != 0) {
						throw new IOException("PNG interlacciato: non previsto");
					}
					break;
				case "PLTE":
					palette = dati;
					break;
				case "tRNS":
					if (tipo == 3) {
						alfaPalette = dati;
					} else if (tipo == 0) {
						coloreTrasparente = new int[]{((dati[0] & 0xFF) << 8) | (dati[1] & 0xFF)};
					} else if (tipo == 2) {
						coloreTrasparente = new int[]{((dati[0] & 0xFF) << 8) | (dati[1] & 0xFF),
								((dati[2] & 0xFF) << 8) | (dati[3] & 0xFF), ((dati[4] & 0xFF) << 8) | (dati[5] & 0xFF)};
					}
					break;
				case "IDAT":
					compressi.write(dati);
					break;
				case "IEND":
					fine = true;
					break;
				default:
					break;
			}
		}
		if (profondita > 8) {
			throw new IOException("PNG a " + profondita + " bit per canale: non previsto");
		}
		int canali = canali(tipo);
		int bitPerPixel = canali * profondita;
		int byteRiga = (larghezza * bitPerPixel + 7) / 8;
		int passo = Math.max(1, bitPerPixel / 8);
		byte[] grezzi = decomprimi(compressi.toByteArray(), (byteRiga + 1) * altezza);
		byte[] righe = deFiltra(grezzi, larghezza, altezza, byteRiga, passo);
		int[] argb = new int[larghezza * altezza];
		int massimo = (1 << profondita) - 1;
		for (int y = 0; y < altezza; y++) {
			int base = y * byteRiga;
			for (int x = 0; x < larghezza; x++) {
				argb[y * larghezza + x] = pixel(righe, base, x, tipo, profondita, massimo, palette, alfaPalette, coloreTrasparente);
			}
		}
		return new ImmagineDiPixel(larghezza, altezza, argb);
	}

	private static int canali(int tipo) throws IOException {
		switch (tipo) {
			case 0:
			case 3:
				return 1;
			case 2:
				return 3;
			case 4:
				return 2;
			case 6:
				return 4;
			default:
				throw new IOException("Tipo di colore PNG sconosciuto: " + tipo);
		}
	}

	private static int pixel(byte[] righe, int base, int x, int tipo, int profondita, int massimo, byte[] palette, byte[] alfaPalette,
							 int[] coloreTrasparente) {
		switch (tipo) {
			case 0: {
				int g = campione(righe, base, x, profondita);
				int livello = g * 255 / massimo;
				int alfa = coloreTrasparente != null && coloreTrasparente[0] == g ? 0 : 255;
				return (alfa << 24) | (livello << 16) | (livello << 8) | livello;
			}
			case 2: {
				int r = righe[base + x * 3] & 0xFF;
				int g = righe[base + x * 3 + 1] & 0xFF;
				int b = righe[base + x * 3 + 2] & 0xFF;
				boolean trasparente = coloreTrasparente != null && coloreTrasparente[0] == r && coloreTrasparente[1] == g
						&& coloreTrasparente[2] == b;
				return ((trasparente ? 0 : 255) << 24) | (r << 16) | (g << 8) | b;
			}
			case 3: {
				int indice = campione(righe, base, x, profondita);
				int r = palette[indice * 3] & 0xFF;
				int g = palette[indice * 3 + 1] & 0xFF;
				int b = palette[indice * 3 + 2] & 0xFF;
				int alfa = alfaPalette != null && indice < alfaPalette.length ? alfaPalette[indice] & 0xFF : 255;
				return (alfa << 24) | (r << 16) | (g << 8) | b;
			}
			case 4: {
				int livello = righe[base + x * 2] & 0xFF;
				int alfa = righe[base + x * 2 + 1] & 0xFF;
				return (alfa << 24) | (livello << 16) | (livello << 8) | livello;
			}
			default: {
				int r = righe[base + x * 4] & 0xFF;
				int g = righe[base + x * 4 + 1] & 0xFF;
				int b = righe[base + x * 4 + 2] & 0xFF;
				int a = righe[base + x * 4 + 3] & 0xFF;
				return (a << 24) | (r << 16) | (g << 8) | b;
			}
		}
	}

	/**
	 * Il campione di un canale solo, a 1, 2, 4 o 8 bit, nel punto <code>x</code> della riga
	 */
	private static int campione(byte[] righe, int base, int x, int profondita) {
		if (profondita == 8) {
			return righe[base + x] & 0xFF;
		}
		int bit = x * profondita;
		int valore = righe[base + bit / 8] & 0xFF;
		int scarto = 8 - profondita - (bit % 8);
		return (valore >> scarto) & ((1 << profondita) - 1);
	}

	private static byte[] decomprimi(byte[] compressi, int attesi) throws IOException {
		Inflater inflater = new Inflater();
		inflater.setInput(compressi);
		byte[] risultato = new byte[attesi];
		try {
			int letti = 0;
			while (letti < attesi && !inflater.finished()) {
				int n = inflater.inflate(risultato, letti, attesi - letti);
				if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
					break;
				}
				letti += n;
			}
			if (letti < attesi) {
				throw new IOException("PNG troncato: " + letti + " byte su " + attesi);
			}
		} catch (DataFormatException e) {
			throw new IOException("PNG con dati compressi non validi", e);
		} finally {
			inflater.end();
		}
		return risultato;
	}

	/**
	 * Toglie il filtro a ogni riga (gli stessi cinque del formato PNG) e restituisce le righe una dopo l'altra, senza il byte del filtro
	 */
	private static byte[] deFiltra(byte[] grezzi, int larghezza, int altezza, int byteRiga, int passo) throws IOException {
		byte[] righe = new byte[byteRiga * altezza];
		for (int y = 0; y < altezza; y++) {
			int filtro = grezzi[y * (byteRiga + 1)] & 0xFF;
			int origine = y * (byteRiga + 1) + 1;
			int destinazione = y * byteRiga;
			for (int i = 0; i < byteRiga; i++) {
				int x = grezzi[origine + i] & 0xFF;
				int a = i >= passo ? righe[destinazione + i - passo] & 0xFF : 0;
				int b = y > 0 ? righe[destinazione - byteRiga + i] & 0xFF : 0;
				int c = y > 0 && i >= passo ? righe[destinazione - byteRiga + i - passo] & 0xFF : 0;
				int valore;
				switch (filtro) {
					case 0:
						valore = x;
						break;
					case 1:
						valore = x + a;
						break;
					case 2:
						valore = x + b;
						break;
					case 3:
						valore = x + ((a + b) >> 1);
						break;
					case 4:
						valore = x + paeth(a, b, c);
						break;
					default:
						throw new IOException("Filtro PNG sconosciuto: " + filtro);
				}
				righe[destinazione + i] = (byte) valore;
			}
		}
		return righe;
	}

	private static int paeth(int a, int b, int c) {
		int p = a + b - c;
		int pa = Math.abs(p - a);
		int pb = Math.abs(p - b);
		int pc = Math.abs(p - c);
		if (pa <= pb && pa <= pc) {
			return a;
		}
		return pb <= pc ? b : c;
	}

	private static int intero(byte[] dati, int posizione) {
		return ((dati[posizione] & 0xFF) << 24) | ((dati[posizione + 1] & 0xFF) << 16) | ((dati[posizione + 2] & 0xFF) << 8)
				| (dati[posizione + 3] & 0xFF);
	}
}
