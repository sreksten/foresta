package com.threeamigos.foresta.ui.immagini;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Le misure delle immagini lette dai file, senza caricarle e senza toolkit: dal descrittore dell'atlante se l'immagine ci sta
 * ({@link AtlanteDiImmagini#misura}), altrimenti dall'intestazione del PNG. Servono prima che ci sia una scheda grafica (libGDX)
 * o una finestra, per esempio per le dimensioni dello schermo (vedi <code>DisposizioneDelloSchermo</code>).
 */
public final class MisureDaiFile implements MisureDelleImmagini {

	private static final String CARTELLA = "/com/threeamigos/foresta/img/";
	// La firma del PNG (8 byte), poi la lunghezza (4) e il tipo (4) del primo blocco, IHDR, che comincia con larghezza e altezza
	private static final int BYTE_PRIMA_DELLE_MISURE = 16;
	// Ogni file si legge una volta sola: le misure non cambiano
	private static final Map<String, Misura> LETTE = new ConcurrentHashMap<>();

	/**
	 * Con lo zoom, come la misura delle immagini dei personaggi che danno i toolkit.
	 */
	@Override
	public Misura personaggio(TipoPersonaggio classe) {
		Misura misura = immagine(NomiDelleImmagini.personaggio(classe));
		double zoom = LivelloDiZoom.valore();
		return new Misura((int) Math.round(misura.larghezza * zoom), (int) Math.round(misura.altezza * zoom));
	}

	/**
	 * Senza zoom.
	 *
	 * @throws IllegalArgumentException se la risorsa non c'è o non è un PNG
	 */
	@Override
	public Misura immagine(String risorsa) {
		return LETTE.computeIfAbsent(risorsa, MisureDaiFile::leggi);
	}

	private static Misura leggi(String risorsa) {
		int[] dallAtlante = AtlanteDiImmagini.misura(risorsa);
		if (dallAtlante != null) {
			return new Misura(dallAtlante[0], dallAtlante[1]);
		}
		try (InputStream flusso = MisureDaiFile.class.getResourceAsStream(CARTELLA + risorsa)) {
			if (flusso == null) {
				throw new IllegalArgumentException("Non trovo il file " + risorsa);
			}
			DataInputStream dati = new DataInputStream(flusso);
			byte[] intestazione = new byte[BYTE_PRIMA_DELLE_MISURE];
			dati.readFully(intestazione);
			if ((intestazione[0] & 0xFF) != 0x89 || intestazione[1] != 'P' || intestazione[2] != 'N' || intestazione[3] != 'G'
					|| intestazione[12] != 'I' || intestazione[13] != 'H' || intestazione[14] != 'D' || intestazione[15] != 'R') {
				throw new IllegalArgumentException("Non è un PNG: " + risorsa);
			}
			return new Misura(dati.readInt(), dati.readInt());
		} catch (IOException e) {
			throw new IllegalArgumentException("Non riesco a leggere " + risorsa, e);
		}
	}
}
