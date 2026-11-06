package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.TestoDelNodo;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.util.List;
import java.util.function.Consumer;

/**
 * Il menu a discesa della schermata delle tattiche, senza disegno: un popup che si apre sotto (o sopra, se sotto non c'è posto) il punto cliccato, con le scelte
 * nel font Doomdark in un albero da far scorrere ({@link ElencoScorrevole}), che se sono troppe si scorre con la rotella, con le frecce o trascinandolo.
 * <p>
 * Finché è aperto cattura tutti i click: uno su una scelta la applica e lo chiude, uno fuori lo chiude senza cambiare niente. Chi lo usa (la schermata) gli passa i
 * click prima di tutto il resto e lo disegna per ultimo: il riquadro (da {@link #getX()}, con {@link #SFONDO} e {@link #BORDO}) e l'elenco dentro di lui.
 *
 * @param <T> il tipo del valore di ogni scelta
 */
public final class MenuADiscesa<T> {

	/**
	 * Una scelta del menu: il testo e il valore che dà
	 */
	public static final class Scelta<T> {
		public final String testo;
		public final T valore;
		/** Che cosa vuol dire questa scelta, per il riquadro di spiegazione; null se non c'è */
		public final String spiegazione;

		public Scelta(String testo, T valore) {
			this(testo, valore, null);
		}

		public Scelta(String testo, T valore, String spiegazione) {
			this.testo = testo;
			this.valore = valore;
			this.spiegazione = spiegazione;
		}
	}

	public static final int MARGINE = 4;
	private static final int INTERLINEA = 1;
	/** Di quanti pixel scorre un click su una freccia */
	private static final int PASSO_FRECCIA = 40;
	/** Di quanti pixel scorre uno scatto della rotella */
	private static final int PASSO_ROTELLA = 12;
	/** Il colore dello sfondo e del bordo (RGB) */
	public static final int SFONDO = 0x20160C;
	public static final int BORDO = 0xC8C8C8;

	private final List<Scelta<T>> scelte;
	private final T corrente;
	private final Consumer<T> alloScegliere;
	private final DoomdarkFont font = DoomdarkFont.medio();
	private final int x;
	private final int y;
	private final int larghezza;
	private final int altezza;
	private final int larghezzaDellaFreccia;
	// Quanto la lista è scorsa: con la rotella, le frecce e trascinandola (anche col dito, con inerzia)
	private final ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(System::nanoTime);
	private boolean aperto = true;

	/**
	 * @param ancoraX     la x a cui allineare il lato sinistro del menu
	 * @param ancoraSotto la quota del bordo alto della riga che lo apre, e sotto di essa si apre
	 * @param ancoraSopra la quota del bordo basso della riga che lo apre, e sopra di essa si apre se sotto non c'è posto
	 */
	public MenuADiscesa(List<Scelta<T>> scelte, T corrente, int ancoraX, int ancoraSotto, int ancoraSopra, int larghezzaSchermo, int altezzaSchermo,
						MisureDelleImmagini misure, Consumer<T> alloScegliere) {
		this.scelte = scelte;
		this.corrente = corrente;
		this.alloScegliere = alloScegliere;
		int larghezzaTesto = 0;
		for (Scelta<T> scelta : scelte) {
			larghezzaTesto = Math.max(larghezzaTesto, Math.max(1, FontTool.getWidth(font, scelta.testo)));
		}
		this.larghezzaDellaFreccia = misure.immagine(NomiDelleImmagini.FRECCIA_SU).larghezza;
		int larghezzaFrecce = larghezzaDellaFreccia + Finestra.SPACING;
		this.larghezza = Math.min(larghezzaSchermo - 2 * MARGINE, larghezzaTesto + 2 * MARGINE + larghezzaFrecce);
		int altezzaContenuto = altezzaDelContenuto();
		int massima = Math.max(font.getHeight() * 3, altezzaSchermo - 2 * MARGINE);
		int sotto = altezzaSchermo - ancoraSotto - MARGINE;
		int sopra = ancoraSopra - MARGINE;
		int altezzaScelta;
		int yScelta;
		if (altezzaContenuto + 2 * MARGINE <= sotto || sotto >= sopra) {
			altezzaScelta = Math.min(altezzaContenuto + 2 * MARGINE, Math.min(massima, sotto));
			yScelta = ancoraSotto;
		} else {
			altezzaScelta = Math.min(altezzaContenuto + 2 * MARGINE, Math.min(massima, sopra));
			yScelta = ancoraSopra - altezzaScelta;
		}
		this.altezza = altezzaScelta;
		this.y = yScelta;
		this.x = Math.max(MARGINE, Math.min(ancoraX, larghezzaSchermo - larghezza - MARGINE));
		// Se l'elenco scorre, si apre con la scelta corrente a metà altezza invece che in cima
		int altezzaDiUnaScelta = font.getHeight() + 2 * INTERLINEA;
		for (int i = 0; i < scelte.size(); i++) {
			if (scelte.get(i).valore.equals(corrente)) {
				sincronizzaLimite();
				scorrimento.scorriDi(i * altezzaDiUnaScelta - (altezzaInterna() - altezzaDiUnaScelta) / 2);
				break;
			}
		}
	}

	public boolean isAperto() {
		return aperto;
	}

	public void chiudi() {
		aperto = false;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
	}

	/** Dove sta l'elenco: dentro il margine, a (x + MARGINE, y + MARGINE) */
	public int getLarghezzaInterna() {
		return larghezza - 2 * MARGINE;
	}

	public int getAltezzaInterna() {
		return altezza - 2 * MARGINE;
	}

	private int altezzaInterna() {
		return getAltezzaInterna();
	}

	private int altezzaDelContenuto() {
		// Ogni nodo è alto quanto il testo più l'interlinea del testo e l'interlinea fra i nodi
		return scelte.size() * (font.getHeight() + 2 * INTERLINEA);
	}

	/**
	 * Lo scorrimento della lista, per chi disegna: lo aggiorna con {@link #aggiorna()} e ne legge la quota
	 */
	public ScorrimentoSuUnAsse getScorrimento() {
		return scorrimento;
	}

	/**
	 * Da chiamare a ogni disegno: aggiorna lo scorrimento (inerzia, elastico) tenendolo nei limiti della lista
	 */
	public void aggiorna() {
		sincronizzaLimite();
		scorrimento.aggiorna();
	}

	/**
	 * L'albero delle scelte, con quella evidenziata (se non null) in bianco, quella corrente in giallo e le altre in grigio chiaro: si ricostruisce a ogni
	 * disegno e a ogni click.
	 */
	public ElencoScorrevole<T> elenco(T evidenziata) {
		ElencoScorrevole<T> elenco = new ElencoScorrevole<>(Math.max(1, larghezza - 2 * MARGINE), 0, INTERLINEA);
		for (Scelta<T> scelta : scelte) {
			DoomdarkColorModel.Color colore = scelta.valore.equals(evidenziata) ? DoomdarkColorModel.Color.WHITE
					: scelta.valore.equals(corrente) ? DoomdarkColorModel.Color.YELLOW : DoomdarkColorModel.Color.LIGHT_GRAY;
			elenco.creaNodo(new TestoDelNodo(scelta.testo, font, colore), null, null, null, null, scelta.valore);
		}
		return elenco;
	}

	/**
	 * Il valore della scelta alla posizione (px, py) in coordinate dello schermo, o null
	 */
	public T sceltaAlla(int px, int py) {
		int xi = px - (x + MARGINE);
		int yi = py - (y + MARGINE);
		if (xi < 0 || xi >= getLarghezzaInterna() || yi < 0 || yi >= altezzaInterna()) {
			return null;
		}
		return elenco(null).riferimentoTitoloAllaQuota(yi + (int) Math.round(scorrimento.getScorrimento()));
	}

	/**
	 * La scelta sotto il mouse, o null
	 */
	public Scelta<T> sceltaSotto(int px, int py) {
		T valore = sceltaAlla(px, py);
		if (valore == null) {
			return null;
		}
		for (Scelta<T> scelta : scelte) {
			if (scelta.valore.equals(valore)) {
				return scelta;
			}
		}
		return null;
	}

	private boolean dentro(int px, int py) {
		return px >= x && px < x + larghezza && py >= y && py < y + altezza;
	}

	/**
	 * Un click: sulle frecce scorre, su una scelta la applica e chiude, fuori chiude. Sempre consumato finché è aperto.
	 */
	public void click(int px, int py) {
		if (!aperto) {
			return;
		}
		if (!dentro(px, py)) {
			aperto = false;
			return;
		}
		if (elenco(null).isScorrevole(altezzaInterna())) {
			boolean suColonnaFrecce = px >= x + larghezza - MARGINE - larghezzaDellaFreccia - 2 * Finestra.SPACING;
			if (suColonnaFrecce) {
				int meta = y + altezza / 2;
				scorri(py < meta ? -PASSO_FRECCIA : PASSO_FRECCIA);
				return;
			}
		}
		T scelta = sceltaAlla(px, py);
		if (scelta != null) {
			aperto = false;
			alloScegliere.accept(scelta);
		}
	}

	public void rotella(int scatti, boolean versoSu) {
		scorri((versoSu ? -1 : 1) * scatti * PASSO_ROTELLA);
	}

	private void scorri(int pixel) {
		sincronizzaLimite();
		scorrimento.scorriDi(pixel);
	}

	/**
	 * Dice allo scorrimento di quanto la lista può scorrere nello spazio del menu
	 */
	private void sincronizzaLimite() {
		int massimo = elenco(null).getMassimoScorrimento(altezzaInterna());
		scorrimento.imposta(massimo > 0, massimo);
	}

	/**
	 * Il tasto si preme: da un punto dentro il menu comincia un possibile trascinamento della lista
	 */
	public void premi(int px, int py) {
		if (aperto) {
			sincronizzaLimite();
			scorrimento.premi(py, dentro(px, py));
		}
	}

	public void trascina(int py) {
		scorrimento.trascina(py);
	}

	public void rilascia() {
		scorrimento.rilascia();
	}

	/**
	 * Se l'ultimo gesto è stato un trascinamento: il click che segue il rilascio non vale
	 */
	public boolean isGestoTrascinato() {
		return scorrimento.isGestoTrascinato();
	}
}
