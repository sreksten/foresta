package com.threeamigos.foresta.libgdx.sviluppo;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.threeamigos.foresta.libgdx.ImmaginiGdx;
import com.threeamigos.foresta.libgdx.TestoGdx;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

/**
 * La pagina di prova per chi sviluppa: il testo nei due font e in tutti i colori, a capo entro una larghezza, e le immagini chieste per
 * nome (a metà risoluzione, con lo zoom). Non è parte del gioco: i testi sono campioni, non si traducono.
 */
public final class PaginaDiProva {

	private static final int MARGINE = 20;
	private static final float ZOOM = 2f;

	private PaginaDiProva() {
	}

	public static void disegna(SpriteBatch batch, ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio) {
		float y = MARGINE;
		for (DoomdarkColorModel.Color colore : DoomdarkColorModel.Color.values()) {
			if (colore == DoomdarkColorModel.Color.BLACK || colore == DoomdarkColorModel.Color.VERY_DARK_GRAY) {
				continue; // sul fondo scuro non si leggono
			}
			medio.disegnaConOmbra(batch, "La Foresta - " + colore + " - àèìòù ÀÈÌÒÙ 0123456789 ?!", MARGINE, y, colore, 1);
			y += medio.getFont().getHeight() + 2;
		}
		piccolo.disegna(batch, "Font piccolo: Il goblin colpisce il Mago per 12 danni. àèìòù ÀÈÌÒÙ", MARGINE, y + 8, DoomdarkColorModel.Color.WHITE);
		y += 8 + piccolo.getFont().getHeight() + 8;
		medio.disegnaSuPiuRighe(batch, "Un testo lungo va a capo fra le parole entro la larghezza data, come nei riquadri del gioco: "
				+ "questa è la prova su tre o quattro righe.", MARGINE, y, DoomdarkColorModel.Color.LIGHT_GRAY, 420);

		String[] icone = {"icone/Guerriero.png", "icone/Mago.png", "icone/Fuoco.png", "icone/Nord.png", "icone/Si.png", "icone/Pozione"
				+ "Salute.png", "icone/Moneta.png", "oggetti/Spada.png", "oggetti/Scudo.png", "mappa/Foresta.png", "mappa/Castello.png"};
		float x = 560;
		float yIcone = MARGINE;
		for (String icona : icone) {
			immagini.disegna(batch, icona, x, yIcone, ZOOM);
			x += immagini.regione(icona).getRegionWidth() * ZOOM + 12;
		}
		immagini.disegna(batch, "personaggi/Drago.png", 560, 100, ZOOM);
		immagini.disegna(batch, "locazioni/Foresta.png", 560 + 227 * ZOOM + 12, 100, ZOOM);
	}


	/**
	 * Qualche decina di messaggi per provare il riquadro del testo (lunghi, corti, con gli accenti)
	 */
	public static String[] messaggiDiEsempio() {
		String[] messaggi = new String[60];
		for (int i = 0; i < messaggi.length; i++) {
			messaggi[i] = i % 4 == 3
					? "Messaggio " + (i + 1) + ": un testo molto più lungo del solito, che deve andare a capo fra le parole entro la larghezza del riquadro."
					: "Messaggio " + (i + 1) + ": il goblin colpisce il Mago, perché è già più debole.";
		}
		return messaggi;
	}
}
