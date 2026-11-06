package com.threeamigos.foresta.ui;

import java.util.function.LongSupplier;

/**
 * Dai tocchi e dai movimenti grezzi di un puntatore (mouse o dito, un solo dito) ai gesti che il {@link RouterDegliInput} capisce: premuto, trascinato,
 * rilasciato, click e doppio click. I toolkit non li danno tutti (libGDX non ha gli eventi di click), e col dito mancano anche il passaggio del cursore e
 * i tasti: qui stanno le regole, uguali per ogni toolkit.
 * <ul>
 * <li><b>Click</b>: un tocco rilasciato quasi dove è cominciato (entro {@link #SOGLIA_DI_SPOSTAMENTO} pixel virtuali). Se ne segue un altro vicino
 * (entro {@link #DISTANZA_FRA_I_CLICK}) nell'intervallo del router ({@link EventiDelPuntatore#getIntervalloDoppioClick()}) è il secondo di un doppio
 * click: lo stesso intervallo con cui il router rimanda il click singolo, così singolo e doppio non scattano mai insieme.</li>
 * <li><b>Col dito</b> ({@code tocco} vero) la pressione si consegna al router solo quando si sa che non è una pressione prolungata, cioè al rilascio
 * o al primo movimento oltre la soglia. Sono i gesti del dito che il mouse non ha:
 * <ul>
 * <li>la <b>pressione prolungata</b> ({@link #PRESSIONE_PROLUNGATA_MS} senza muoversi) fa comparire le descrizioni come se il cursore passasse sul punto
 * ({@code mosso}), e non esegue niente: il rilascio la chiude (il cursore «esce») senza nessun click e senza che le finestre abbiano mai visto la
 * pressione, perché alcune, come la barra delle icone, eseguono il comando al rilascio. Spostando il dito le descrizioni lo seguono;</li>
 * <li>un movimento sotto la soglia è tremolio del dito: non è un trascinamento.</li>
 * </ul></li>
 * </ul>
 * Col mouse ({@code tocco} falso) ogni evento passa com'è e non c'è pressione prolungata: il cursore sta già sul punto.
 */
public final class RiconoscitoreDiGesti {

	/** Di quanti pixel virtuali si può spostare un tocco restando un click (e non un trascinamento) */
	public static final int SOGLIA_DI_SPOSTAMENTO = 8;
	/** A che distanza dal primo deve cadere il secondo click di un doppio click */
	public static final int DISTANZA_FRA_I_CLICK = 16;
	/** Quanto va tenuto premuto, senza muoversi, perché sia una pressione prolungata */
	public static final int PRESSIONE_PROLUNGATA_MS = 500;

	private final EventiDelPuntatore destinazione;
	private final Pianificatore pianificatore;
	private final boolean tocco;
	private final LongSupplier orologio;

	// Il tocco in corso
	private int xPressione;
	private int yPressione;
	private Finestra.Tasto tastoPremuto = Finestra.Tasto.SINISTRO;
	private boolean spostato;
	private boolean pressioneConsegnata;
	private boolean prolungata;
	private Pianificatore.Pianificata attesaDellaPressioneProlungata;
	// L'ultimo click, per riconoscere il doppio
	private long istanteDelClick;
	private int xClick;
	private int yClick;
	private Finestra.Tasto tastoDelClick;
	private int conteggioDelClick;

	/**
	 * @param tocco vero per un dito (pressione consegnata in ritardo, pressione prolungata), falso per un mouse
	 */
	public RiconoscitoreDiGesti(EventiDelPuntatore destinazione, Pianificatore pianificatore, boolean tocco) {
		this(destinazione, pianificatore, tocco, System::currentTimeMillis);
	}

	/**
	 * @param orologio i millisecondi correnti; si cambia nei test
	 */
	public RiconoscitoreDiGesti(EventiDelPuntatore destinazione, Pianificatore pianificatore, boolean tocco, LongSupplier orologio) {
		this.destinazione = destinazione;
		this.pianificatore = pianificatore;
		this.tocco = tocco;
		this.orologio = orologio;
	}

	/** Il puntatore si muove senza premere (solo col mouse) */
	public void mosso(int x, int y) {
		destinazione.mosso(x, y);
	}

	/** La rotella (solo col mouse): non c'e' niente da riconoscere, passa al router */
	public void rotella(int x, int y, int rotazioni) {
		destinazione.rotella(x, y, rotazioni);
	}

	public void premuto(int x, int y, Finestra.Tasto tasto) {
		xPressione = x;
		yPressione = y;
		tastoPremuto = tasto;
		spostato = false;
		prolungata = false;
		pressioneConsegnata = false;
		annullaLAttesa();
		if (tocco && tasto == Finestra.Tasto.SINISTRO) {
			attesaDellaPressioneProlungata = pianificatore.dopo(PRESSIONE_PROLUNGATA_MS, this::scattaLaPressioneProlungata);
		} else {
			consegnaLaPressione();
		}
	}

	public void trascinato(int x, int y) {
		if (prolungata) {
			// Le descrizioni seguono il dito
			destinazione.mosso(x, y);
			return;
		}
		if (Math.abs(x - xPressione) > SOGLIA_DI_SPOSTAMENTO || Math.abs(y - yPressione) > SOGLIA_DI_SPOSTAMENTO) {
			spostato = true;
			annullaLAttesa();
			// Il trascinamento parte dal punto in cui il dito si è posato
			consegnaLaPressione();
		}
		if (!tocco || spostato) {
			destinazione.trascinato(x, y);
		}
	}

	public void rilasciato(int x, int y, Finestra.Tasto tasto) {
		annullaLAttesa();
		if (prolungata) {
			// Solo chiude le descrizioni: niente rilascio, niente click
			prolungata = false;
			destinazione.mouseUscito(x, y);
			return;
		}
		consegnaLaPressione();
		destinazione.rilasciato(x, y, tasto);
		if (spostato) {
			return;
		}
		long ora = orologio.getAsLong();
		boolean secondo = conteggioDelClick == 1 && tasto == tastoDelClick && ora - istanteDelClick <= destinazione.getIntervalloDoppioClick()
				&& Math.abs(x - xClick) <= DISTANZA_FRA_I_CLICK && Math.abs(y - yClick) <= DISTANZA_FRA_I_CLICK;
		conteggioDelClick = secondo ? 2 : 1;
		istanteDelClick = ora;
		xClick = x;
		yClick = y;
		tastoDelClick = tasto;
		destinazione.clic(x, y, tasto, conteggioDelClick);
	}

	/** Vero mentre le descrizioni di una pressione prolungata sono sullo schermo */
	public boolean isPressioneProlungata() {
		return prolungata;
	}

	private void consegnaLaPressione() {
		if (!pressioneConsegnata) {
			pressioneConsegnata = true;
			destinazione.premuto(xPressione, yPressione, tastoPremuto);
		}
	}

	private void scattaLaPressioneProlungata() {
		attesaDellaPressioneProlungata = null;
		prolungata = true;
		destinazione.mosso(xPressione, yPressione);
	}

	private void annullaLAttesa() {
		if (attesaDellaPressioneProlungata != null) {
			attesaDellaPressioneProlungata.annulla();
			attesaDellaPressioneProlungata = null;
		}
	}
}
