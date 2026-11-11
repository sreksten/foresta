package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.strumenti.temporizzatore.Temporizzatore;

import java.util.ArrayList;
import java.util.List;

/**
 * Il router del mouse (e del tocco) fra le finestre dello schermo, senza toolkit: le coordinate arrivano in quelle dello schermo di gioco e
 * ogni finestra le riceve in quelle sue ({@link Finestra}). Lo usano Swing (con gli eventi di AWT) e libGDX (con quelli di
 * <code>InputProcessor</code>). Quello che il router sa fare:
 * <ol>
 *     <li>trovare a chi spetta un evento: la barra delle icone per prima (è sempre sopra a tutto), poi l'eventuale finestra a tutto schermo,
 *     altrimenti la più in primo piano della pila che contiene il punto;</li>
 *     <li>sintetizzare entrata e uscita di ogni finestra (i toolkit le danno solo per l'intero schermo);</li>
 *     <li>far catturare il trascinamento a chi lo chiede ({@link Finestra#catturaIlTrascinamento()}): finché il tasto sinistro resta premuto,
 *     trascinamento e rilascio vanno a lei, anche fuori dal suo rettangolo;</li>
 *     <li>portare davanti un riquadro impilato cliccato e non ultimo, senza altro;</li>
 *     <li>sul telefono, far scorrere di lato la pila dei riquadri ({@link PilaScorrevole}): un trascinamento che comincia su un riquadro della pila e
 *     diventa orizzontale prima che verticale passa alla pila, e il riquadro lo lascia; un click sulla freccia della pila la fa scorrere;</li>
 *     <li>ritardare il click singolo (175 ms col mouse, di più col dito: {@link #getIntervalloDoppioClick()}) per chi distingue il doppio click
 *     ({@link Finestra#gestisceDoppioClick()}), scartandolo se arriva il secondo e se nel frattempo la schermata è cambiata;</li>
 *     <li>dividere la rotella in passi consegnati a uno o due per fotogramma ({@link Finestra#passiPerScattoDellaRotella()}), perché lo scorrimento
 *     a pixel risulti fluido.</li>
 * </ol>
 * Limite noto: l'intervallo del doppio click è quello dato al costruttore e non quello del sistema, quindi un doppio click più lento fa scattare
 * sia il click singolo sia il doppio. Chi riconosce i gesti ({@link RiconoscitoreDiGesti}) conta il secondo click solo entro lo stesso intervallo, così
 * il doppio click e il singolo non scattano mai insieme.
 */
public final class RouterDegliInput implements EventiDelPuntatore {

	/**
	 * Quanto aspetta il click singolo prima di scattare, nell'attesa di un secondo click
	 */
	public static final int INTERVALLO_DOPPIO_CLICK = 175;

	/**
	 * Quanto aspetta il click singolo col dito: i doppi tocchi sono piu' lenti dei doppi click del mouse
	 */
	public static final int INTERVALLO_DOPPIO_TOCCO = 300;

	/**
	 * Quel che il router chiede a chi lo ospita
	 */
	public interface Contesto {
		/**
		 * La barra delle icone, sempre in primo piano sopra a tutto; null se non c'è
		 */
		Finestra getBarraIcone();

		/**
		 * La finestra che occupa tutto lo schermo (intro, inventario…), o null se si sta nella schermata di gioco a riquadri
		 */
		Finestra getFinestraATuttoSchermo();

		/**
		 * I riquadri impilati, dal fondo al primo piano, nell'ordine in cui si disegnano
		 */
		List<Finestra> getFinestreImpilate();

		/**
		 * Dove sta la finestra, in coordinate dello schermo
		 */
		Area getArea(Finestra finestra);

		/**
		 * Se il punto (in coordinate della finestra) è suo: di norma sta nella sua area, ma la barra decide da sé (le icone ingrandite escono dal suo
		 * rettangolo)
		 */
		default boolean contiene(Finestra finestra, int xLocale, int yLocale) {
			return xLocale >= 0 && yLocale >= 0 && xLocale < getArea(finestra).getLarghezza() && yLocale < getArea(finestra).getAltezza();
		}

		/**
		 * Porta davanti la finestra se è un riquadro impilato che non è l'ultimo; restituisce true se l'ha fatto
		 */
		boolean portaInPrimoPianoSeImpilato(Finestra finestra);

		/**
		 * La pila che scorre di lato (sul telefono), o null se non c'è
		 */
		default PilaScorrevole getPilaScorrevole() {
			return null;
		}

		/**
		 * Se la finestra è uno dei riquadri che scorrono con la pila
		 */
		default boolean scorreConLaPila(Finestra finestra) {
			return false;
		}

		/**
		 * Se il punto, in coordinate dello schermo, è sulla freccia che fa scorrere la pila
		 */
		default boolean isSullaFrecciaDellaPila(int x, int y) {
			return false;
		}

		/**
		 * Che schermata si sta mostrando: se cambia mentre un click aspetta, il click non vale più
		 */
		Object getStato();

		/**
		 * Chiede di ridisegnare lo schermo
		 */
		void ridisegna();
	}

	private static final class Risultato {
		final Finestra finestra;
		final int x;
		final int y;

		Risultato(Finestra finestra, int x, int y) {
			this.finestra = finestra;
			this.x = x;
			this.y = y;
		}
	}

	private final Contesto contesto;
	private final Pianificatore pianificatore;

	// Su quale finestra si trovava il cursore, per sintetizzare entrata e uscita
	private Finestra finestraSottoIlCursore;
	// La finestra su cui si è premuto il tasto sinistro, se si fa trascinare
	private Finestra finestraCheCatturaIlTrascinamento;
	// Dove si è premuto su un riquadro della pila che scorre, finché il gesto può ancora diventare uno scorrimento della pila (null altrimenti)
	private Punto pressioneSullaPila;
	// Se il gesto in corso fa scorrere la pila
	private boolean pilaPresa;
	// Il click singolo in attesa di un secondo click
	private Pianificatore.Pianificata clickSingoloInAttesa;

	// Uno scatto della rotella si divide in più passi, consegnati a uno o due per fotogramma
	private Finestra finestraRotella;
	private int xRotella;
	private int yRotella;
	private int rotazioniInSospeso;
	private Pianificatore.Pianificata passiDellaRotella;

	private final int intervalloDoppioClick;

	public RouterDegliInput(Contesto contesto, Pianificatore pianificatore) {
		this(contesto, pianificatore, INTERVALLO_DOPPIO_CLICK);
	}

	/**
	 * @param intervalloDoppioClick quanto aspetta il click singolo prima di scattare, nell'attesa di un secondo click: {@link #INTERVALLO_DOPPIO_CLICK}
	 *                              col mouse, {@link #INTERVALLO_DOPPIO_TOCCO} col dito
	 */
	public RouterDegliInput(Contesto contesto, Pianificatore pianificatore, int intervalloDoppioClick) {
		this.contesto = contesto;
		this.pianificatore = pianificatore;
		this.intervalloDoppioClick = intervalloDoppioClick;
	}

	@Override
	public boolean vuoleLaPressioneSubito(int x, int y) {
		Risultato risultato = trova(x, y);
		return risultato != null && risultato.finestra.vuoleLaPressioneSubito(risultato.x, risultato.y);
	}

	@Override
	public int getIntervalloDoppioClick() {
		return intervalloDoppioClick;
	}

	// ---- chi riceve un evento ----

	private Risultato trova(int x, int y) {
		Risultato barra = crea(contesto.getBarraIcone(), x, y);
		if (barra != null) {
			return barra;
		}
		// Fuori dal gioco lo schermo è occupato da un unico elemento: non c'è nessuna pila da percorrere
		Finestra aTuttoSchermo = contesto.getFinestraATuttoSchermo();
		if (aTuttoSchermo != null) {
			return crea(aTuttoSchermo, x, y);
		}
		List<Finestra> pila = new ArrayList<>(contesto.getFinestreImpilate());
		// La pila si disegna dal fondo al primo piano, quindi va percorsa al contrario: l'evento spetta alla più in primo piano
		for (int indice = pila.size() - 1; indice >= 0; indice--) {
			Risultato risultato = crea(pila.get(indice), x, y);
			if (risultato != null) {
				return risultato;
			}
		}
		return null;
	}

	private Risultato crea(Finestra finestra, int x, int y) {
		if (finestra == null || !finestra.isVisibile()) {
			return null;
		}
		Area area = contesto.getArea(finestra);
		int xLocale = x - area.getX();
		int yLocale = y - area.getY();
		return contesto.contiene(finestra, xLocale, yLocale) ? new Risultato(finestra, xLocale, yLocale) : null;
	}

	/**
	 * Notifica uscita ed entrata quando il cursore passa da una finestra a un'altra. Le coordinate assolute servono per l'uscita, che va
	 * comunicata alla finestra precedente e cade quindi fuori dal suo rettangolo.
	 */
	private void aggiornaFinestraSottoIlCursore(Risultato risultato, int x, int y) {
		Finestra nuova = risultato == null ? null : risultato.finestra;
		if (nuova == finestraSottoIlCursore) {
			return;
		}
		if (finestraSottoIlCursore != null) {
			Area area = contesto.getArea(finestraSottoIlCursore);
			finestraSottoIlCursore.processaUscita(x - area.getX(), y - area.getY());
		}
		finestraSottoIlCursore = nuova;
		if (risultato != null) {
			nuova.processaEntrata(risultato.x, risultato.y);
		}
	}

	// ---- gli eventi ----

	/**
	 * Il cursore entra nello schermo: si stabilisce su quale finestra si trova
	 */
	public void mouseEntrato(int x, int y) {
		aggiornaFinestraSottoIlCursore(trova(x, y), x, y);
		contesto.ridisegna();
	}

	/**
	 * Il cursore esce dallo schermo: si esce anche dalla finestra su cui si era
	 */
	@Override
	public void mouseUscito(int x, int y) {
		aggiornaFinestraSottoIlCursore(null, x, y);
		contesto.ridisegna();
	}

	@Override
	public void mosso(int x, int y) {
		Risultato risultato = trova(x, y);
		aggiornaFinestraSottoIlCursore(risultato, x, y);
		if (risultato != null) {
			risultato.finestra.processaMovimento(risultato.x, risultato.y);
		}
		contesto.ridisegna();
	}

	@Override
	public void premuto(int x, int y, Finestra.Tasto tasto) {
		Risultato risultato = trova(x, y);
		if (risultato == null) {
			return;
		}
		if (tasto == Finestra.Tasto.SINISTRO) {
			finestraCheCatturaIlTrascinamento = risultato.finestra.catturaIlTrascinamento() ? risultato.finestra : null;
			pilaPresa = false;
			pressioneSullaPila = contesto.getPilaScorrevole() != null && contesto.scorreConLaPila(risultato.finestra) ? new Punto(x, y) : null;
		}
		risultato.finestra.processaPressione(risultato.x, risultato.y, tasto);
		contesto.ridisegna();
	}

	@Override
	public void trascinato(int x, int y) {
		if (prendeLaPila(x, y)) {
			contesto.getPilaScorrevole().trascina(x);
			contesto.ridisegna();
			return;
		}
		if (finestraCheCatturaIlTrascinamento != null) {
			Area area = contesto.getArea(finestraCheCatturaIlTrascinamento);
			finestraCheCatturaIlTrascinamento.processaTrascinamento(x - area.getX(), y - area.getY());
			contesto.ridisegna();
			return;
		}
		Risultato risultato = trova(x, y);
		// Anche trascinando si può passare da una finestra all'altra: chi viene lasciata riceve processaUscita
		aggiornaFinestraSottoIlCursore(risultato, x, y);
		if (risultato != null) {
			risultato.finestra.processaTrascinamento(risultato.x, risultato.y);
		}
		contesto.ridisegna();
	}

	/**
	 * Se il trascinamento fa scorrere la pila: lo fa già, o è cominciato su un suo riquadro e si è spostato in orizzontale oltre la soglia del click prima
	 * che in verticale (allora il riquadro, che magari faceva scorrere la sua lista, lo lascia). Un trascinamento verticale resta del riquadro.
	 */
	private boolean prendeLaPila(int x, int y) {
		if (pilaPresa) {
			return true;
		}
		if (pressioneSullaPila == null) {
			return false;
		}
		int dx = Math.abs(x - pressioneSullaPila.x);
		int dy = Math.abs(y - pressioneSullaPila.y);
		if (dx > RiconoscitoreDiGesti.SOGLIA_DI_SPOSTAMENTO && dx > dy) {
			pilaPresa = true;
			if (finestraCheCatturaIlTrascinamento != null) {
				Finestra finestra = finestraCheCatturaIlTrascinamento;
				finestraCheCatturaIlTrascinamento = null;
				Area area = contesto.getArea(finestra);
				finestra.processaRilascio(pressioneSullaPila.x - area.getX(), pressioneSullaPila.y - area.getY(), Finestra.Tasto.SINISTRO);
			}
			contesto.getPilaScorrevole().prendi(pressioneSullaPila.x);
			pressioneSullaPila = null;
			return true;
		}
		if (dy > RiconoscitoreDiGesti.SOGLIA_DI_SPOSTAMENTO) {
			pressioneSullaPila = null;
		}
		return false;
	}

	@Override
	public void rilasciato(int x, int y, Finestra.Tasto tasto) {
		if (tasto == Finestra.Tasto.SINISTRO) {
			pressioneSullaPila = null;
			if (pilaPresa) {
				pilaPresa = false;
				contesto.getPilaScorrevole().lascia();
				aggiornaFinestraSottoIlCursore(trova(x, y), x, y);
				contesto.ridisegna();
				return;
			}
		}
		if (finestraCheCatturaIlTrascinamento != null && tasto == Finestra.Tasto.SINISTRO) {
			Finestra finestra = finestraCheCatturaIlTrascinamento;
			finestraCheCatturaIlTrascinamento = null;
			Area area = contesto.getArea(finestra);
			finestra.processaRilascio(x - area.getX(), y - area.getY(), Finestra.Tasto.SINISTRO);
			// Durante il trascinamento nessuno ha seguito il cursore: adesso si stabilisce dove sta
			aggiornaFinestraSottoIlCursore(trova(x, y), x, y);
			contesto.ridisegna();
			return;
		}
		Risultato risultato = trova(x, y);
		if (risultato == null) {
			return;
		}
		risultato.finestra.processaRilascio(risultato.x, risultato.y, tasto);
		contesto.ridisegna();
	}

	/**
	 * Un click (premuto e rilasciato nello stesso punto)
	 *
	 * @param conteggio 1 per il primo click, 2 per il secondo di un doppio click
	 */
	@Override
	public void clic(int x, int y, Finestra.Tasto tasto, int conteggio) {
		// La freccia della pila sta sopra i riquadri: la fa scorrere e basta
		if (tasto == Finestra.Tasto.SINISTRO && contesto.getPilaScorrevole() != null && contesto.isSullaFrecciaDellaPila(x, y)) {
			contesto.getPilaScorrevole().commuta();
			contesto.ridisegna();
			return;
		}
		Risultato risultato = trova(x, y);
		if (risultato == null) {
			return;
		}
		Finestra finestra = risultato.finestra;
		// Il click su un riquadro impilato che non è l'ultimo lo porta davanti e basta
		if (contesto.portaInPrimoPianoSeImpilato(finestra)) {
			contesto.ridisegna();
			return;
		}
		if (!finestra.gestisceDoppioClick()) {
			// Nessuna attesa: questa finestra non distingue il doppio click dal singolo
			finestra.processaClick(risultato.x, risultato.y, tasto);
			contesto.ridisegna();
			return;
		}
		// Il click singolo si rimanda, per poterlo scartare se arriva il secondo (altrimenti processaClick e processaDoppioClick scatterebbero
		// entrambi per un solo doppio click)
		if (clickSingoloInAttesa != null) {
			clickSingoloInAttesa.annulla();
			clickSingoloInAttesa = null;
		}
		if (conteggio >= 2) {
			finestra.processaDoppioClick(risultato.x, risultato.y, tasto);
			contesto.ridisegna();
			return;
		}
		Object statoAlClick = contesto.getStato();
		clickSingoloInAttesa = pianificatore.dopo(intervalloDoppioClick, () -> {
			// Se nell'attesa la schermata è cambiata il click non vale più: la finestra cliccata non si vede
			if (contesto.getStato() != statoAlClick) {
				return;
			}
			finestra.processaClick(risultato.x, risultato.y, tasto);
			contesto.ridisegna();
		});
	}

	/**
	 * La rotella: positiva verso il basso (verso chi guarda), negativa verso l'alto
	 */
	@Override
	public void rotella(int x, int y, int rotazioni) {
		Risultato risultato = trova(x, y);
		if (risultato == null) {
			return;
		}
		Finestra finestra = risultato.finestra;
		int passi = finestra.passiPerScattoDellaRotella();
		if (passi <= 1) {
			consegnaRotella(finestra, risultato.x, risultato.y, rotazioni);
			contesto.ridisegna();
			return;
		}
		// Se il cursore è passato a un'altra finestra, quel che restava per la precedente si consegna subito
		if (finestraRotella != null && finestraRotella != finestra) {
			consegnaRotellaInSospeso();
		}
		finestraRotella = finestra;
		xRotella = risultato.x;
		yRotella = risultato.y;
		rotazioniInSospeso += rotazioni * passi;
		if (passiDellaRotella == null || !passiDellaRotella.isAttiva()) {
			passiDellaRotella = pianificatore.ogni(1000 / Temporizzatore.FRAME_PER_SECONDO, this::consegnaUnPassoDiRotella);
		}
	}

	private void consegnaUnPassoDiRotella() {
		if (rotazioniInSospeso == 0) {
			passiDellaRotella.annulla();
			return;
		}
		// Più la coda è lunga più passi per fotogramma, così una rotella girata in fretta non resta indietro
		int passi = Math.max(1, (Math.abs(rotazioniInSospeso) + 3) / 4);
		int consegnati = Math.min(passi, Math.abs(rotazioniInSospeso)) * Integer.signum(rotazioniInSospeso);
		rotazioniInSospeso -= consegnati;
		consegnaRotella(finestraRotella, xRotella, yRotella, consegnati);
		contesto.ridisegna();
		if (rotazioniInSospeso == 0) {
			passiDellaRotella.annulla();
		}
	}

	private void consegnaRotellaInSospeso() {
		int daConsegnare = rotazioniInSospeso;
		rotazioniInSospeso = 0;
		consegnaRotella(finestraRotella, xRotella, yRotella, daConsegnare);
	}

	private static void consegnaRotella(Finestra finestra, int x, int y, int rotazioni) {
		if (rotazioni < 0) {
			finestra.processaRotella(x, y, -rotazioni, Finestra.MovimentoRotella.SU);
		} else if (rotazioni > 0) {
			finestra.processaRotella(x, y, rotazioni, Finestra.MovimentoRotella.GIU);
		}
	}
}
