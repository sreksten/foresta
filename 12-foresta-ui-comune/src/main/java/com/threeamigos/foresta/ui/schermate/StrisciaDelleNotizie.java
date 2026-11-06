package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Il notiziario in fondo alla mappa a tutto schermo, senza disegno: le {@link VistaPartita#getUltimeNotizie()} come un'unica striscia (il titolo in giallo e
 * il corpo in bianco nel font medio, separate da un "-" con margini di {@value #LARGHEZZA_MARGINE_SEPARATORE_NOTIZIE} pixel, incluso dopo l'ultima per un
 * giro continuo). Chi disegna la ripete affiancata (tiling), così una notizia comincia a comparire da destra mentre la precedente sta ancora uscendo da
 * sinistra, senza mai fermarsi. Lo scorrimento avanza del tempo reale passato fra una chiamata di {@link #avanza(long)} e la successiva, non di un passo fisso:
 * chi disegna può essere chiamato anche per altri motivi (il mouse che si muove), quindi non a un ritmo garantito.
 */
public final class StrisciaDelleNotizie {

	public static final int VELOCITA_SCORRIMENTO_PX_PER_SECONDO = 90;
	public static final int LARGHEZZA_ZONA_DI_DISSOLVENZA = 60;
	private static final int LARGHEZZA_MARGINE_SEPARATORE_NOTIZIE = 16;
	/** Il colore dello sfondo della fascia (RGB) */
	public static final int SFONDO = 0x142878;
	private static final String SEPARATORE_TITOLO = " - ";
	private static final String TRATTINO = "-";

	/**
	 * Un pezzo di testo della striscia, con il suo colore e dove comincia
	 */
	public static final class Pezzo {
		private final String testo;
		private final DoomdarkColorModel.Color colore;
		private final int x;

		private Pezzo(String testo, DoomdarkColorModel.Color colore, int x) {
			this.testo = testo;
			this.colore = colore;
			this.x = x;
		}

		public String getTesto() {
			return testo;
		}

		public DoomdarkColorModel.Color getColore() {
			return colore;
		}

		/**
		 * Dove comincia, nella striscia
		 */
		public int getX() {
			return x;
		}
	}

	private final int larghezza;
	private final VistaPartita vistaPartita;
	private final DoomdarkFont font = DoomdarkFont.medio();

	private List<NotiziaMD> notizieCostruite = new ArrayList<>();
	private List<Pezzo> pezzi = new ArrayList<>();
	private int larghezzaTotale;
	private float x;
	// -1 = nessuna chiamata precedente (o il notiziario era vuoto): la prossima avanza() non fa scorrere, registra solo l'istante.
	private long ultimoAggiornamentoNanos = -1;

	/**
	 * @param larghezza la larghezza della fascia in cui scorre
	 */
	public StrisciaDelleNotizie(int larghezza, VistaPartita vistaPartita) {
		this.larghezza = larghezza;
		this.vistaPartita = vistaPartita;
	}

	public int getLarghezza() {
		return larghezza;
	}

	/**
	 * Se non ci sono notizie da mostrare: la fascia non si vede.
	 */
	public boolean isVuota() {
		return vistaPartita.getUltimeNotizie().isEmpty();
	}

	/**
	 * Aggiorna la striscia (se le notizie sono cambiate la ricostruisce e la fa ripartire da destra) e la fa scorrere del tempo passato dall'ultima volta.
	 * Senza notizie non fa niente. Da chiamare a ogni disegno.
	 *
	 * @param ora un istante in nanosecondi (di solito <code>System.nanoTime()</code>)
	 */
	public void avanza(long ora) {
		List<NotiziaMD> notizieAttuali = vistaPartita.getUltimeNotizie();
		if (notizieAttuali.isEmpty()) {
			ultimoAggiornamentoNanos = -1;
			return;
		}
		if (pezzi.isEmpty() || !notizieAttuali.equals(notizieCostruite)) {
			costruisci(notizieAttuali);
		}
		if (larghezzaTotale == 0) {
			return;
		}
		if (ultimoAggiornamentoNanos >= 0) {
			x -= VELOCITA_SCORRIMENTO_PX_PER_SECONDO * ((ora - ultimoAggiornamentoNanos) / 1_000_000_000f);
		}
		ultimoAggiornamentoNanos = ora;
		while (x <= -larghezzaTotale) {
			x += larghezzaTotale;
		}
	}

	/**
	 * Dove comincia la prima copia della striscia, relativa alla fascia: negativa quando è già scorsa in parte.
	 */
	public int getX() {
		return Math.round(x);
	}

	/**
	 * La larghezza di una copia della striscia (tutte le notizie e i separatori): per ripeterla affiancata finché riempie la fascia.
	 */
	public int getLarghezzaTotale() {
		return larghezzaTotale;
	}

	/**
	 * I pezzi di testo di una copia della striscia.
	 */
	public List<Pezzo> getPezzi() {
		return pezzi;
	}

	public DoomdarkFont getFont() {
		return font;
	}

	private void costruisci(List<NotiziaMD> notizieAttuali) {
		List<Pezzo> nuovi = new ArrayList<>();
		int xCorrente = 0;
		// Il "-" con i suoi margini non è il testo " - ": gli spazi del font Doomdark sono larghi solo pochi pixel, troppo poco per separare visibilmente due
		// notizie concatenate
		int larghezzaSeparatore = LARGHEZZA_MARGINE_SEPARATORE_NOTIZIE * 2 + Math.max(1, FontTool.getWidth(font, TRATTINO));
		for (NotiziaMD notizia : notizieAttuali) {
			try {
				String corpo = notizia.getCorpo();
				int posizioneSeparatore = corpo.indexOf(SEPARATORE_TITOLO);
				List<Pezzo> daAggiungere = new ArrayList<>();
				int x0 = xCorrente;
				String resto = corpo;
				if (posizioneSeparatore > 0) {
					String titolo = corpo.substring(0, posizioneSeparatore);
					resto = corpo.substring(posizioneSeparatore);
					daAggiungere.add(new Pezzo(titolo, DoomdarkColorModel.Color.YELLOW, x0));
					x0 += Math.max(1, FontTool.getWidth(font, titolo));
				}
				daAggiungere.add(new Pezzo(resto, DoomdarkColorModel.Color.WHITE, x0));
				x0 += Math.max(1, FontTool.getWidth(font, resto));
				daAggiungere.add(new Pezzo(TRATTINO, DoomdarkColorModel.Color.WHITE, x0 + LARGHEZZA_MARGINE_SEPARATORE_NOTIZIE));
				nuovi.addAll(daAggiungere);
				xCorrente = x0 + larghezzaSeparatore;
			} catch (DoomdarkFont.UnsupportedCharacterException e) {
				Logger.log(e);
			}
		}
		// Snapshot per valore: VistaPartita.getUltimeNotizie() restituisce una vista di sola lettura della lista viva, che cambia quando arrivano notizie
		// nuove: va copiata per poterla confrontare più avanti con il suo stato futuro.
		notizieCostruite = new ArrayList<>(notizieAttuali);
		pezzi = nuovi;
		larghezzaTotale = xCorrente;
		// La notizia più recente è sempre la prima della striscia (Notizie.aggiungiNotizia inserisce in testa): facendo ripartire lo scorrimento da destra a
		// ogni ricostruzione, si vede subito, senza aspettare che il giro precedente finisca.
		x = larghezza;
	}
}
