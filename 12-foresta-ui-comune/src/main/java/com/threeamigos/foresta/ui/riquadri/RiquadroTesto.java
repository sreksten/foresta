package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.testo.StoricoDelTesto;

/**
 * Il riquadro di testo in cui scorrono le frasi che raccontano la partita, senza disegno: lo storico delle righe, lo scorrimento con la rotella e col
 * trascinamento (inerzia ed elastico) e il ritorno alla riga più recente quando arriva un messaggio. Swing e libGDX lo estendono e ci mettono sopra il
 * loro disegno: prima di disegnare chiamano {@link #sincronizzaScorrimento()}, poi si fanno dire le righe da {@link StoricoDelTesto#perOgniRigaVisibile}.
 */
public class RiquadroTesto implements Finestra {

	/** La sfumatura in cima, al massimo: il testo più vecchio si spegne verso l'alto, dal nero al bianco pieno in 255 pixel */
	public static final int SFUMATURA_MASSIMA = 255;
	/** La sfumatura in cima non prende più di questa parte dell'altezza del riquadro: dove il riquadro è basso (il telefono) il testo resta leggibile */
	public static final double PARTE_DELLA_SFUMATURA = 0.10;

	// Pixel di scorrimento per ogni passo della rotella (uno scatto fa più passi: circa una riga)
	private static final int PASSO_SCORRIMENTO = 3;

	private final int topLeftX;
	private final int topLeftY;
	private final int width;
	private final int height;
	private final StoricoDelTesto storico;
	// Quanto si è tornati indietro nel testo, ma al contrario: 0 è la riga più vecchia e il massimo la più recente (che
	// è dove si parte), così trascinare verso il basso mostra il testo più vecchio, come negli altri elenchi
	private final ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(System::nanoTime);
	// Un messaggio nuovo (o il testo svuotato) riporta alla riga più recente: lo si fa al prossimo disegno, sul suo thread
	private volatile boolean riportaInFondo = true;

	public RiquadroTesto(int topLeftX, int topLeftY, int width, int height) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.width = width;
		this.height = height;
		storico = new StoricoDelTesto(width, height);
	}

	public int getTopLeftX() {
		return topLeftX;
	}

	public int getTopLeftY() {
		return topLeftY;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Quanti pixel, dalla cima del riquadro, il testo impiega a passare dal nero al bianco pieno: {@link #SFUMATURA_MASSIMA}, ma non più del
	 * {@link #PARTE_DELLA_SFUMATURA} dell'altezza del riquadro
	 */
	public int getAltezzaDellaSfumatura() {
		return Math.max(1, Math.min(SFUMATURA_MASSIMA, (int) Math.round(height * PARTE_DELLA_SFUMATURA)));
	}

	/**
	 * La luminosità del testo a quella quota dalla cima del riquadro: 0 in cima, 1 dalla fine della sfumatura in giù
	 */
	public float luminositaAllaQuota(int quota) {
		return Math.max(0f, Math.min(1f, quota / (float) getAltezzaDellaSfumatura()));
	}

	public StoricoDelTesto getStorico() {
		return storico;
	}

	/**
	 * Cancella tutto il testo.
	 */
	public void clear() {
		storico.clear();
		riportaInFondo = true;
	}

	/**
	 * Aggiunge una frase in fondo.
	 */
	public void addString(String messaggio) {
		storico.addString(messaggio);
		riportaInFondo = true;
	}

	@Override
	public int passiPerScattoDellaRotella() {
		return PASSI_PER_SCATTO_DELLE_LISTE;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		int pixel = numeroRotazioni * PASSO_SCORRIMENTO;
		// Su, verso il testo più vecchio, è verso l'inizio dello scorrimento
		scorrimento.scorriDi(movimentoRotella == MovimentoRotella.SU ? -pixel : pixel);
	}

	/**
	 * Il gesto che comincia sul riquadro prosegue anche se il puntatore ne esce, finché non si lascia
	 */
	@Override
	public boolean catturaIlTrascinamento() {
		return true;
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.premi(y, true);
		}
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		scorrimento.trascina(y);
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.rilascia();
		}
	}

	/**
	 * Porta il testo dov'è lo scorrimento: aggiorna limite e movimento, e rimette il testo alla riga più recente se è
	 * arrivato qualcosa di nuovo. Da chiamare prima di ogni disegno.
	 */
	protected void sincronizzaScorrimento() {
		int massimo = storico.getScorrimentoMassimo();
		scorrimento.imposta(massimo > 0, massimo);
		if (riportaInFondo) {
			riportaInFondo = false;
			scorrimento.reimposta();
			scorrimento.scorriDi(massimo);
		}
		scorrimento.aggiorna();
		storico.impostaOffset(massimo - (int) Math.round(scorrimento.getScorrimento()));
	}
}
