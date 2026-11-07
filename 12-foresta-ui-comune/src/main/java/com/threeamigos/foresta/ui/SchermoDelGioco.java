package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaBancoDiLavoro;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.interni.InternoUiInattiva;
import com.threeamigos.foresta.eventi.interni.InternoUiOccupata;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.modellodati.MessaggioMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.tipi.StatoDelloSchermo;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoFinestra;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoNegozio;
import com.threeamigos.foresta.ui.barra.BarraIcone;
import com.threeamigos.foresta.ui.riquadri.*;
import com.threeamigos.foresta.ui.schermate.*;
import com.threeamigos.foresta.ui.sprite.*;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Lo schermo del gioco senza toolkit: tutto quello che il canvas di Swing (<code>DisplayableCanvas</code>) e lo schermo di libGDX
 * fanno senza disegnare. Sa quale schermata è quella corrente ({@link StatoDelloSchermo}) e fa da facciata per tutto ciò che la UI
 * deve mostrare (la classe radice, {@link ForestaUI}, traduce gli eventi del bus in chiamate sui suoi metodi).
 * <ul>
 *     <li>la pila dei riquadri della schermata di gioco: quale sta davanti, e dove stanno i tre riquadri impilati nella colonna a
 *     destra (gruppo, incantesimi e pozioni, missioni), ognuno più in basso del precedente;</li>
 *     <li>i cambi di schermata: aprire una bottega la prepara, tornare all'inventario dai trofei, dal bestiario o dalle tattiche no;
 *     la classifica appena aggiornata fa partire l'intro da lei; la vittoria e la sconfitta raccontano a passi;</li>
 *     <li>gli effetti che si mostrano uno alla volta: i fumetti (lo stesso fumetto riparte, uno diverso interrompe), gli annunci a
 *     tutto schermo (rimandati durante un intermezzo; quelli sostituibili scartano quelli dello stesso genere) e le rivelazioni degli
 *     artefatti (un annuncio aspetta la rivelazione, e viceversa);</li>
 *     <li>gli sprite in vita, e l'avviso all'automa quando sono finiti tutti ({@code InternoUiInattiva});</li>
 *     <li>gli eventi del bus che riguardano i riquadri;</li>
 *     <li>quel che il router degli input chiede allo schermo ({@link #contestoDegliInput()}).</li>
 * </ul>
 * Il toolkit dà i riquadri e le schermate (i modelli comuni che i suoi disegnatori estendono), crea gli sprite e disegna, nell'ordine
 * che preferisce, con i metodi per il disegno ({@link #getPila()}, {@link #perOgniSprite}, {@link #disegnaIlFumetto},
 * {@link #disegnaLaRivelazione}, {@link #disegnaLAnnuncio}, {@link #fineFotogramma()}).
 *
 * @param <S> lo sprite del toolkit
 * @param <A> l'annuncio a tutto schermo del toolkit
 * @param <F> il fumetto del toolkit
 * @param <R> la rivelazione di un artefatto del toolkit
 */
public abstract class SchermoDelGioco<S extends SpriteDelloSchermo, A extends AnnuncioDelloSchermo, F extends FumettoDelloSchermo,
		R extends CodaDelleRivelazioni.InCoda> {

	private static final float SECONDI_DI_ATTESA_DELLA_SFIDA = 1.5f;
	private static final float SECONDI_DI_DISSOLVENZA_DELLA_SFIDA = 1.0f;

	protected final DisposizioneDelloSchermo disposizione;
	protected final VistaPartita vistaPartita;

	// Letto anche dal thread di animazione di Swing (vedi deveRidisegnare)
	private volatile StatoDelloSchermo stato = StatoDelloSchermo.INTRO;
	private final List<TipoFinestra> pila = new ArrayList<>();
	// Quanto sfuma in basso un riquadro impilato prima che cominci il successivo
	private final int altezzaDissolvenzaPila = DoomdarkFont.medio().getHeight();

	private final List<S> sprite = new ArrayList<>();
	// Letta anche dal thread di animazione di Swing
	private final List<A> codaAnnunci = new CopyOnWriteArrayList<>();
	// Un annuncio richiesto mentre è ancora a schermo l'ultima pagina di un intermezzo (es. una missione attivata da un controllo
	// dell'Automa a cascata, prima che la UI torni al gioco) va rimandato: disegnarlo subito lo sovrapporrebbe all'intermezzo
	private final List<A> annunciRimandati = new ArrayList<>();
	private volatile A annuncioAttivo;
	private final List<F> codaFumetti = new ArrayList<>();
	private F fumettoAttivo;
	// Gli artefatti trovati nei cofani e nei templi, mostrati uno alla volta
	private final CodaDelleRivelazioni<R> rivelazioni = new CodaDelleRivelazioni<>();
	// Rileva il fronte "appena tornata inattiva" per notificare l'Automa con InternoUiInattiva
	private boolean uiOccupata;
	// La prossima intro deve partire dalla classifica invece che dai loghi (vedi mostraPunteggi)
	private boolean introDallaClassifica;

	protected SchermoDelGioco(DisposizioneDelloSchermo disposizione, VistaPartita vistaPartita) {
		this.disposizione = disposizione;
		this.vistaPartita = vistaPartita;
		// Gruppo, incantesimi e missioni sono impilati nell'ordine in cui compaiono qui. INTRO_OUTRO e MAPPA_A_TUTTO_SCHERMO non
		// appartengono alla pila: occupano da soli tutto lo schermo e sono scelti in base allo stato.
		pila.add(TipoFinestra.STATO);
		pila.add(TipoFinestra.INCANTESIMI_E_POZIONI);
		pila.add(TipoFinestra.MAPPA);
		pila.add(TipoFinestra.STATISTICHE);
		pila.add(TipoFinestra.GRAFICA);
		pila.add(TipoFinestra.TESTO);
		pila.add(TipoFinestra.MISSIONI);
		pila.add(TipoFinestra.INFO_COMBATTIMENTO);
		pila.add(TipoFinestra.SFIDA);
	}

	/**
	 * Da chiamare alla fine del costruttore del toolkit, quando riquadri e schermate ci sono: mette in posizione i riquadri
	 * impilati e si iscrive agli eventi del bus che riguardano i riquadri.
	 */
	protected final void prepara() {
		posizionaRiquadriImpilati();
		BusEventi.iscriviti(NotificaAumentoLivelloMondo.class, e -> notifica(TestiDellaUI.get("LEVEL_UP_MONDO", e.getLivello())));
		BusEventi.iscriviti(NotificaArtefattoTrovato.class, this::gestisciEventoArtefattoTrovato);
		BusEventi.iscriviti(InternoCreazioneSpriteATempo.class, e -> {
			if (stato == StatoDelloSchermo.IN_GIOCO) {
				aggiungiSprite(sprite(e.getDescrizione()));
			}
		});
		// Il riquadro del gruppo
		BusEventi.iscriviti(NotificaAumentoLivelloPersonaggio.class, this::gestisciEventoAumentoLivelloPersonaggio);
		BusEventi.iscriviti(NotificaVariazioneStatistichePersonaggio.class, this::gestisciEventoVariazioneStatistichePersonaggio);
		// Il riquadro di incantesimi e pozioni
		RiquadroIncantesimiEPozioni incantesimi = riquadroIncantesimiEPozioni();
		iscrivitiConIncantesimiDavanti(NotificaVariazioneDisponibilitaIncantesimi.class, incantesimi::gestisciEventoVariazioneIncantesimi);
		iscrivitiConIncantesimiDavanti(NotificaVariazioneDisponibilitaPozioniSalute.class, incantesimi::gestisciEventoVariazionePozioniSalute);
		iscrivitiConIncantesimiDavanti(NotificaVariazioneDisponibilitaPozioniSaluteGrandi.class, incantesimi::gestisciEventoVariazionePozioniSaluteGrandi);
		iscrivitiConIncantesimiDavanti(NotificaVariazioneDisponibilitaPozioniMagia.class, incantesimi::gestisciEventoVariazionePozioniMagia);
		iscrivitiConIncantesimiDavanti(NotificaVariazioneDisponibilitaPozioniMagiaGrandi.class, incantesimi::gestisciEventoVariazionePozioniMagiaGrandi);
		iscrivitiConIncantesimiDavanti(NotificaVariazioneDisponibilitaPozioniPanacea.class, incantesimi::gestisciEventoVariazionePozioniPanacea);
		iscrivitiConIncantesimiDavanti(NotificaVariazioneDisponibilitaPozioniAumentoLivello.class, incantesimi::gestisciEventoVariazionePozioniAumentoLivello);
		// Il riquadro delle missioni
		BusEventi.iscriviti(NotificaAggiornamentoStatoMissione.class, e -> {
			primoPiano(TipoFinestra.MISSIONI);
			notificaAnnuncioGlobale(e.getEtichetta(), e.getDescrizione());
		});
	}

	private <E extends com.threeamigos.foresta.eventi.EventoBase> void iscrivitiConIncantesimiDavanti(Class<E> classe, Consumer<E> gestore) {
		BusEventi.iscriviti(classe, evento -> {
			primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
			gestore.accept(evento);
		});
	}

	// ---------------------------------------------------------------------------------------------------------------------------
	// Quel che il toolkit dà: riquadri, schermate e sprite

	protected abstract RiquadroTesto riquadroTesto();

	protected abstract RiquadroGruppo riquadroGruppo();

	protected abstract RiquadroIncantesimiEPozioni riquadroIncantesimiEPozioni();

	protected abstract RiquadroMissioni riquadroMissioni();

	protected abstract RiquadroLocazione riquadroLocazione();

	protected abstract RiquadroCombattimento riquadroCombattimento();

	protected abstract RiquadroSfida riquadroSfida();

	protected abstract Finestra riquadroMappa();

	protected abstract Finestra riquadroStatistiche();

	protected abstract BarraIcone barraIcone();

	protected abstract SchermateIntroOutro introOutro();

	protected abstract SchermataIntermezzo intermezzo();

	protected abstract SchermataMappa mappaATuttoSchermo();

	protected abstract SchermataDiScambio schermataInventario();

	/** Armaiolo e venditore di pergamene: una sola schermata, che cambia secondo il negozio */
	protected abstract SchermataDiScambio schermataCommerciante();

	protected abstract SchermataDiScambio schermataAlchimista();

	protected abstract SchermataDiScambio schermataIncantatore();

	protected abstract SchermataTattiche schermataTattiche();

	protected abstract SchermataTrofei schermataTrofei();

	protected abstract SchermataBestiario schermataBestiario();

	protected abstract SchermataGrimorio schermataGrimorio();

	/** Imposta lo scambio e il personaggio che l'inventario mostra */
	protected abstract void scambioDellInventario(VistaScambio scambio, VistaPersonaggio personaggio);

	/** Imposta il negozio e lo scambio che la schermata del commerciante mostra */
	protected abstract void scambioDelCommerciante(TipoNegozio negozio, VistaScambio scambio);

	/** Imposta il banco di lavoro che la schermata dell'incantatore mostra */
	protected abstract void bancoDellIncantatore(VistaBancoDiLavoro banco);

	/** Imposta le offerte che la schermata dell'alchimista mostra */
	protected abstract void offerteDellAlchimista(List<? extends VistaOffertaConsumabile> offerte);

	/** Lo sprite di quella descrizione; null se la descrizione è null */
	protected abstract S sprite(DescrizioneSpriteATempo descrizione);

	/** Lo sprite di quella descrizione; null se la descrizione è null */
	protected abstract S sprite(DescrizioneSpriteEffetto descrizione);

	/** Lo sprite del personaggio morto, che si dissolve dove stava; null se non è in locazione */
	protected abstract S spriteDellaMorte(VistaPersonaggio personaggio);

	/** Il riquadro della sfida come uno sprite che resta intero un attimo e poi si dissolve */
	protected abstract S spriteDellaSfida(float secondiDiAttesa, float secondiDiDissolvenza);

	protected abstract A annuncio(DescrizioneAnnuncioGlobale descrizione);

	/** Un fumetto con quel testo (largo al massimo così) in quel punto, con la punta verso quell'altro */
	protected abstract F fumetto(String testo, int larghezzaMassima, int x, int y, int puntaVersoX, int puntaVersoY);

	/** La rivelazione dell'artefatto al centro dell'area, che all'uscita vola verso la destinazione */
	protected abstract R rivelazione(VistaArtefatto artefatto, int livelloMondo, Area area, Punto destinazione);

	/** Chiede di ridisegnare lo schermo appena si può (libGDX ridisegna a ogni fotogramma comunque) */
	public abstract void ridisegna();

	// ---------------------------------------------------------------------------------------------------------------------------
	// Lo stato e la pila

	public StatoDelloSchermo getStato() {
		return stato;
	}

	public DisposizioneDelloSchermo getDisposizione() {
		return disposizione;
	}

	public RiquadroSfida getRiquadroSfida() {
		return riquadroSfida();
	}

	public RiquadroCombattimento getRiquadroCombattimento() {
		return riquadroCombattimento();
	}

	/**
	 * Le finestre della pila, dal fondo al primo piano: l'ordine in cui si disegnano (il combattimento e la sfida, che ci sono anche
	 * loro, si disegnano dopo tutti gli altri, così nessuno li copre)
	 */
	public List<TipoFinestra> getPila() {
		return new ArrayList<>(pila);
	}

	/**
	 * Porta la finestra in cima alla pila: è quella che si disegna sopra le altre e che riceve per prima il mouse.
	 *
	 * @throws IllegalArgumentException se non è una finestra della pila
	 */
	public void primoPiano(TipoFinestra finestra) {
		if (!pila.remove(finestra)) {
			throw new IllegalArgumentException("Elemento grafico non valido: " + finestra);
		}
		pila.add(finestra);
		posizionaRiquadriImpilati();
	}

	private static boolean isRiquadroImpilato(TipoFinestra finestra) {
		return finestra == TipoFinestra.STATO || finestra == TipoFinestra.INCANTESIMI_E_POZIONI || finestra == TipoFinestra.MISSIONI;
	}

	/**
	 * I riquadri impilati nell'ordine della pila: il primo è il più in alto, l'ultimo il più in basso e davanti agli altri
	 */
	public List<TipoFinestra> riquadriImpilati() {
		List<TipoFinestra> impilati = new ArrayList<>(DisposizioneDelloSchermo.RIQUADRI_IMPILATI);
		for (TipoFinestra finestra : pila) {
			if (isRiquadroImpilato(finestra)) {
				impilati.add(finestra);
			}
		}
		return impilati;
	}

	/**
	 * Dà a ogni riquadro impilato la quota secondo la sua posizione nella pila, sia per disegnarlo sia per il mouse
	 */
	private void posizionaRiquadriImpilati() {
		List<TipoFinestra> impilati = riquadriImpilati();
		for (int posizione = 0; posizione < impilati.size(); posizione++) {
			int y = disposizione.quotaNellaPila(posizione);
			switch (impilati.get(posizione)) {
				case STATO:
					riquadroGruppo().spostaInVerticale(y);
					break;
				case INCANTESIMI_E_POZIONI:
					riquadroIncantesimiEPozioni().spostaInVerticale(y);
					break;
				default:
					riquadroMissioni().spostaInVerticale(y);
					break;
			}
		}
	}

	private int quotaDellImpilato(TipoFinestra finestra) {
		return disposizione.quotaNellaPila(riquadriImpilati().indexOf(finestra));
	}

	/**
	 * Un riquadro impilato che non è l'ultimo sfuma nel nero nelle ultime {@link #getAltezzaDissolvenzaPila()} righe prima del
	 * riquadro successivo, che comincia a questa quota.
	 *
	 * @return la quota del riquadro che lo copre, o -1 se non è impilato o è l'ultimo (non lo copre nessuno)
	 */
	public int quotaDelSuccessivo(TipoFinestra finestra) {
		List<TipoFinestra> impilati = riquadriImpilati();
		int posizione = impilati.indexOf(finestra);
		if (posizione < 0 || posizione == impilati.size() - 1) {
			return -1;
		}
		return disposizione.quotaNellaPila(posizione + 1);
	}

	public int getAltezzaDissolvenzaPila() {
		return altezzaDissolvenzaPila;
	}

	/**
	 * La finestra che sta in quel posto della pila, o una delle schermate a tutto schermo che hanno un tipo
	 */
	public Finestra finestra(TipoFinestra finestra) {
		switch (finestra) {
			case INTRO_OUTRO:
				return introOutro();
			case MAPPA:
				return riquadroMappa();
			case STATISTICHE:
				return riquadroStatistiche();
			case GRAFICA:
				return riquadroLocazione();
			case STATO:
				return riquadroGruppo();
			case INCANTESIMI_E_POZIONI:
				return riquadroIncantesimiEPozioni();
			case TESTO:
				return riquadroTesto();
			case MISSIONI:
				return riquadroMissioni();
			case INFO_COMBATTIMENTO:
				return riquadroCombattimento();
			case SFIDA:
				return riquadroSfida();
			case MAPPA_A_TUTTO_SCHERMO:
				return mappaATuttoSchermo();
			case INVENTARIO:
				return schermataInventario();
			default:
				throw new IllegalArgumentException("Elemento grafico non valido: " + finestra);
		}
	}

	/**
	 * Dove sta la finestra, in coordinate dello schermo: i riquadri impilati dove li mette la pila, il combattimento e la sfida dove
	 * si mettono loro, le schermate a tutto schermo sull'area di contenuto.
	 */
	public Area getArea(Finestra finestra) {
		if (finestra == barraIcone()) {
			return disposizione.getBarra();
		} else if (finestra == riquadroMappa()) {
			return disposizione.getMappa();
		} else if (finestra == riquadroStatistiche()) {
			return disposizione.getStatistiche();
		} else if (finestra == riquadroLocazione()) {
			return disposizione.getLocazione();
		} else if (finestra == riquadroTesto()) {
			return disposizione.getTesto();
		} else if (finestra == riquadroGruppo()) {
			return conQuota(disposizione.getGruppo(), quotaDellImpilato(TipoFinestra.STATO));
		} else if (finestra == riquadroIncantesimiEPozioni()) {
			return conQuota(disposizione.getIncantesimi(), quotaDellImpilato(TipoFinestra.INCANTESIMI_E_POZIONI));
		} else if (finestra == riquadroMissioni()) {
			return conQuota(disposizione.getMissioni(), quotaDellImpilato(TipoFinestra.MISSIONI));
		} else if (finestra == riquadroCombattimento()) {
			return riquadroCombattimento().getArea();
		} else if (finestra == riquadroSfida()) {
			return riquadroSfida().getArea();
		}
		return disposizione.getContenuto();
	}

	private static Area conQuota(Area area, int y) {
		return new Area(area.getX(), y, area.getLarghezza(), area.getAltezza());
	}

	/**
	 * L'unica finestra che occupa tutto lo schermo nello stato corrente, oppure null se lo schermo è composto dalla pila dei
	 * riquadri di gioco.
	 */
	public Finestra finestraATuttoSchermo() {
		switch (stato) {
			case IN_GIOCO:
				return null;
			case MAPPA:
				return mappaATuttoSchermo();
			case INVENTARIO:
				return schermataInventario();
			case TROFEI:
				return schermataTrofei();
			case BESTIARIO:
				return schermataBestiario();
			case GRIMORIO:
				return schermataGrimorio();
			case TATTICHE:
				return schermataTattiche();
			case COMMERCIANTE:
				return schermataCommerciante();
			case ALCHIMISTA:
				return schermataAlchimista();
			case INCANTATORE:
				return schermataIncantatore();
			case INTERMEZZO:
				return intermezzo();
			default:
				return introOutro();
		}
	}

	/**
	 * Cambia la schermata mostrata: lasciando quella delle tattiche, il nome di una voce che si stava scrivendo non si aspetta più;
	 * entrando in una bottega o nell'inventario la schermata si prepara (ma dalle pagine dei trofei, del bestiario, del grimorio e delle tattiche
	 * si torna all'inventario com'era).
	 */
	private void cambiaStato(StatoDelloSchermo nuovo) {
		if (stato == StatoDelloSchermo.TATTICHE && nuovo != StatoDelloSchermo.TATTICHE) {
			schermataTattiche().getEditor().annullaLaRichiestaDiTesto();
		}
		if (nuovo != stato) {
			SchermataDiScambio aperta = scambiatoreDelloStato(nuovo);
			boolean ritornoAllInventario = nuovo == StatoDelloSchermo.INVENTARIO
					&& (stato == StatoDelloSchermo.TROFEI || stato == StatoDelloSchermo.BESTIARIO || stato == StatoDelloSchermo.GRIMORIO
							|| stato == StatoDelloSchermo.TATTICHE);
			if (aperta != null && !ritornoAllInventario) {
				aperta.apri();
			}
		}
		stato = nuovo;
	}

	/**
	 * La schermata di scambio che quello stato mostra, o null se non ne mostra una.
	 */
	private SchermataDiScambio scambiatoreDelloStato(StatoDelloSchermo s) {
		switch (s) {
			case INVENTARIO:
				return schermataInventario();
			case COMMERCIANTE:
				return schermataCommerciante();
			case ALCHIMISTA:
				return schermataAlchimista();
			case INCANTATORE:
				return schermataIncantatore();
			default:
				return null;
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------
	// Le schermate

	/**
	 * Fa ripartire l'intro: dai loghi, oppure dalla classifica se è appena stato registrato un punteggio. Va chiamato anche quando
	 * lo schermo è già nell'intro (è lo stato iniziale, all'avvio del gioco).
	 */
	public void avviaIntro() {
		cambiaStato(StatoDelloSchermo.INTRO);
		if (introDallaClassifica) {
			introOutro().posizionaSuPunteggi();
			introDallaClassifica = false;
		} else {
			introOutro().resettaSequenza();
		}
		ridisegna();
	}

	/**
	 * A ogni battito del temporizzatore della UI: se non si è nell'intro, la si fa partire. L'intro poi va avanti da sola, a ogni
	 * fotogramma.
	 */
	public void avanzaIntro() {
		if (stato != StatoDelloSchermo.INTRO) {
			avviaIntro();
		}
	}

	/**
	 * Passa alla schermata di gioco; se si torna da un intermezzo ne libera le immagini e fa partire gli annunci che erano stati
	 * rimandati.
	 */
	public void iniziaGioco() {
		intermezzo().svuota();
		cambiaStato(StatoDelloSchermo.IN_GIOCO);
		fumettoAttivo = null;
		codaFumetti.clear();
		if (!annunciRimandati.isEmpty()) {
			codaAnnunci.addAll(annunciRimandati);
			annunciRimandati.clear();
			BusEventi.pubblica(new InternoUiOccupata());
		}
		ridisegna();
	}

	/**
	 * Passa alla mappa a tutto schermo, centrata sul gruppo.
	 */
	public void mappa() {
		cambiaStato(StatoDelloSchermo.MAPPA);
		mappaATuttoSchermo().getModello().centraSuGiocatore();
		mappaATuttoSchermo().getModello().dimenticaLaMano();
		ridisegna();
	}

	public void inventario() {
		cambiaStato(StatoDelloSchermo.INVENTARIO);
		ridisegna();
	}

	/**
	 * La pagina dei trofei, aperta dall'inventario.
	 */
	public void trofei() {
		schermataTrofei().apri();
		cambiaStato(StatoDelloSchermo.TROFEI);
		ridisegna();
	}

	/**
	 * Il bestiario, aperto dall'inventario.
	 */
	public void bestiario() {
		schermataBestiario().apri();
		cambiaStato(StatoDelloSchermo.BESTIARIO);
		ridisegna();
	}

	/**
	 * Il grimorio, aperto dall'inventario.
	 */
	public void grimorio() {
		schermataGrimorio().apri();
		cambiaStato(StatoDelloSchermo.GRIMORIO);
		ridisegna();
	}

	/**
	 * Chi sa chiedere un testo al giocatore per conto della UI (il campo di testo), per la schermata delle tattiche che rinomina le
	 * voci
	 */
	public void setRichiestaTesto(BiConsumer<String, Consumer<String>> richiestaTesto, Consumer<Consumer<String>> annullaRichiesta) {
		schermataTattiche().getEditor().setRichiestaTesto(richiestaTesto, annullaRichiesta);
	}

	/**
	 * La schermata delle tattiche, aperta dall'inventario; arriva di nuovo quando cambia il personaggio scelto o l'ordine del gruppo.
	 */
	public void tattiche(VistaPersonaggio personaggio, TatticaMD tattica, TatticaMD tatticaPredefinita, Set<TipoAzione> azioniPossibili) {
		schermataTattiche().getEditor().impostaTattica(personaggio, tattica, tatticaPredefinita, azioniPossibili);
		cambiaStato(StatoDelloSchermo.TATTICHE);
		ridisegna();
	}

	public void impostaScambioInventario(VistaScambio scambio, VistaPersonaggio personaggio) {
		scambioDellInventario(scambio, personaggio);
		ridisegna();
	}

	/**
	 * Passa alla schermata del commerciante, che accoglie il gruppo con un fumetto.
	 */
	public void commerciante() {
		cambiaStato(StatoDelloSchermo.COMMERCIANTE);
		notificaFumetto(TestiDellaUI.get("COMMERCIANTE_SALUTO"), schermataCommerciante().getCoordinateFumetto());
		ridisegna();
	}

	public void impostaScambioCommerciante(TipoNegozio negozio, VistaScambio scambio) {
		scambioDelCommerciante(negozio, scambio);
		ridisegna();
	}

	/**
	 * @param messaggio il fumetto con cui accoglie l'incantatore, o null per nessuno
	 */
	public void incantatore(String messaggio) {
		cambiaStato(StatoDelloSchermo.INCANTATORE);
		if (messaggio != null) {
			notificaFumetto(messaggio, schermataIncantatore().getCoordinateFumetto());
		}
		ridisegna();
	}

	public void impostaBancoIncantatore(VistaBancoDiLavoro banco) {
		bancoDellIncantatore(banco);
		ridisegna();
	}

	public void impostaOfferteAlchimista(List<? extends VistaOffertaConsumabile> offerte) {
		offerteDellAlchimista(offerte);
		ridisegna();
	}

	/**
	 * Apre la schermata dell'alchimista, che chiede che cosa serve (l'oroscopo del giorno lo dice nell'intermezzo d'ingresso).
	 */
	public void alchimista() {
		cambiaStato(StatoDelloSchermo.ALCHIMISTA);
		notificaFumetto(TestiDellaUI.get("ALCHIMISTA_SALUTO"), schermataAlchimista().getCoordinateFumetto());
		ridisegna();
	}

	/**
	 * Aggiorna la barra delle icone con i comandi possibili.
	 */
	public void impostaAzioniIcone() {
		barraIcone().impostaAzioni();
		ridisegna();
	}

	/**
	 * Mostra la schermata per scegliere lo slot da cui caricare una partita, con le testate di quelli disponibili.
	 */
	public void selezioneSlotSalvataggioDaCaricare(Collection<TestataSalvataggio> salvataggiDisponibili) {
		introOutro().setSalvataggiDisponibili(salvataggiDisponibili);
		cambiaStato(StatoDelloSchermo.SELEZIONE_SLOT_DA_CARICARE);
		ridisegna();
	}

	/**
	 * Le testate arrivano dall'Automa, lette una volta sola all'apertura della schermata: ognuna ricostruisce dal file il gruppo del
	 * salvataggio con i suoi personaggi, troppo per farlo a ogni fotogramma. Mentre la schermata è aperta i file non cambiano (si
	 * salva solo dopo averla chiusa).
	 */
	public void selezioneSlotSalvataggioDaSalvare(Collection<TestataSalvataggio> salvataggiDisponibili) {
		introOutro().setSalvataggiDisponibili(salvataggiDisponibili);
		cambiaStato(StatoDelloSchermo.SELEZIONE_SLOT_DA_SALVARE);
		ridisegna();
	}

	public void confermaUscita() {
		cambiaStato(StatoDelloSchermo.CONFERMA_USCITA);
		ridisegna();
	}

	/**
	 * Passa alla schermata della sconfitta; se c'è già, fa avanzare il suo testo.
	 */
	public void perso() {
		if (stato != StatoDelloSchermo.PERSO) {
			cambiaStato(StatoDelloSchermo.PERSO);
			introOutro().resettaSequenza();
		} else {
			introOutro().incrementaSequenza(vistaPartita.getFinaleDellaSconfitta().size());
		}
		ridisegna();
	}

	/**
	 * Passa alla schermata della vittoria; se c'è già, fa avanzare il suo testo.
	 */
	public void vinto() {
		if (stato != StatoDelloSchermo.VINTO) {
			cambiaStato(StatoDelloSchermo.VINTO);
			introOutro().resettaSequenza();
		} else {
			introOutro().incrementaSequenza(vistaPartita.getFinaleDellaVittoria().size());
		}
		ridisegna();
	}

	public void mostraStatistiche() {
		cambiaStato(StatoDelloSchermo.STATISTICHE);
		ridisegna();
	}

	/**
	 * Passa alla schermata dei punteggi migliori; l'introduzione che segue parte da quella classifica.
	 */
	public void mostraPunteggi() {
		cambiaStato(StatoDelloSchermo.PUNTEGGI);
		// Il motore torna subito all'intro: che parta dalla classifica appena aggiornata
		introDallaClassifica = true;
		ridisegna();
	}

	public void mostraPaginaIntermezzo(PaginaIntermezzo pagina) {
		cambiaStato(StatoDelloSchermo.INTERMEZZO);
		intermezzo().mostra(pagina);
		ridisegna();
	}

	/**
	 * La pagina dell'intermezzo riparte da quei secondi dalla sua comparsa: dal fumetto successivo.
	 */
	public void saltaNellaPaginaIntermezzo(double secondi) {
		intermezzo().saltaA(secondi);
		ridisegna();
	}

	/**
	 * Mostra un messaggio a tutto schermo, in caratteri grandi.
	 */
	public void scriviGrande(String messaggio) {
		cambiaStato(StatoDelloSchermo.MESSAGGIO);
		introOutro().impostaMessaggio(messaggio);
		ridisegna();
	}

	// ---------------------------------------------------------------------------------------------------------------------------
	// I riquadri della schermata di gioco

	/**
	 * Svuota il riquadro di testo, per una partita nuova.
	 */
	public void reinizializza() {
		riquadroTesto().clear();
	}

	/**
	 * Aggiunge una frase al riquadro di testo.
	 */
	public void notifica(String messaggio) {
		riquadroTesto().addString(messaggio);
		ridisegna();
	}

	/**
	 * Un nuovo paragrafo è separato dal testo precedente da una riga vuota.
	 */
	public void notificaParagrafo(String messaggio) {
		aggiungiParagrafo(messaggio);
		ridisegna();
	}

	private void aggiungiParagrafo(String messaggio) {
		riquadroTesto().addString("");
		riquadroTesto().addString(messaggio);
	}

	/**
	 * Ripopola il riquadro di testo dopo un caricamento, dal più vecchio al più recente e con la stessa impaginazione (paragrafi o
	 * continuazioni) con cui i messaggi erano stati mostrati.
	 *
	 * @param messaggiDalPiuRecenteAlPiuVecchio come li restituisce {@code Notizie.getUltimiMessaggi()}
	 */
	public void ripristinaMessaggi(List<MessaggioMD> messaggiDalPiuRecenteAlPiuVecchio) {
		riquadroTesto().clear();
		for (int i = messaggiDalPiuRecenteAlPiuVecchio.size() - 1; i >= 0; i--) {
			MessaggioMD messaggio = messaggiDalPiuRecenteAlPiuVecchio.get(i);
			if (messaggio.isParagrafo()) {
				aggiungiParagrafo(messaggio.getTesto());
			} else {
				riquadroTesto().addString(messaggio.getTesto());
			}
		}
		ridisegna();
	}

	public void preparaLocazione() {
		primoPiano(TipoFinestra.GRAFICA);
		riquadroLocazione().assegnaCoordinateAgliAvversari();
	}

	/**
	 * Gli avversari in locazione sono cambiati (è arrivata un'altra ondata): immagini e posizioni nuove.
	 */
	public void assegnaCoordinateAPersonaggi() {
		riquadroLocazione().assegnaCoordinateAgliAvversari();
		ridisegna();
	}

	/**
	 * Finita la sfida a carta, forbici e sasso il riquadro va via: diventa uno sprite che resta intero un attimo, per vedere l'ultima
	 * mano, e poi si dissolve lasciando leggere la mappa.
	 */
	public void dissolviLaSfida() {
		aggiungiSprite(spriteDellaSfida(SECONDI_DI_ATTESA_DELLA_SFIDA, SECONDI_DI_DISSOLVENZA_DELLA_SFIDA));
		riquadroSfida().setVisibile(false);
	}

	private void gestisciEventoAumentoLivelloPersonaggio(NotificaAumentoLivelloPersonaggio evento) {
		VistaPersonaggio personaggio = evento.getPersonaggio();
		notificaAnnuncioGlobale(TestiDellaUI.get("LEVEL_UP_TITOLO"), TestiDellaUI.get("LEVEL_UP_ANNUNCIO", personaggio.getNome(), personaggio.getLivello()));
		notifica(TestiDellaUI.get("LEVEL_UP_PERSONAGGIO", personaggio.getNome(), personaggio.getLivello()));
		// La notifica come iconcina è fatta dal riquadro del gruppo
		primoPiano(TipoFinestra.STATO);
		riquadroGruppo().gestisciEventoAumentoLivelloPersonaggio(evento);
	}

	private void gestisciEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
		// Il riquadro del gruppo viene davanti solo se cambia qualcosa dei miei: le statistiche di un mostro non lo riguardano
		if (vistaPartita.getGruppoGiocatore().contiene(evento.getPersonaggio())) {
			primoPiano(TipoFinestra.STATO);
		}
		riquadroGruppo().gestisciEventoVariazioneStatistichePersonaggio(evento);
	}

	/**
	 * La rivelazione dell'artefatto trovato è al centro dello schermo; all'uscita vola verso il riquadro del gruppo.
	 */
	private void gestisciEventoArtefattoTrovato(NotificaArtefattoTrovato evento) {
		Area gruppo = getArea(riquadroGruppo());
		Area schermo = new Area(0, 0, disposizione.getLarghezzaTotale(), disposizione.getAltezzaTotale());
		Punto destinazione = new Punto(gruppo.getX() + gruppo.getLarghezza() / 2, gruppo.getY() + gruppo.getAltezza() / 2);
		rivelazioni.aggiungi(rivelazione(evento.getArtefatto(), evento.getLivelloMondo(), schermo, destinazione));
		BusEventi.pubblica(new InternoUiOccupata());
	}

	// ---------------------------------------------------------------------------------------------------------------------------
	// Gli sprite sui personaggi

	/**
	 * Segnala con uno sprite che un personaggio è morto; se è del gruppo, il suo riquadro viene davanti.
	 */
	public void notificaVariazioneStatoVitale(VistaPersonaggio personaggio) {
		if (!personaggio.isVivo()) {
			aggiungiSprite(spriteDellaMorte(personaggio));
		}
		if (!personaggio.isPNG()) {
			primoPiano(TipoFinestra.STATO);
		}
	}

	/**
	 * Mostra con uno sprite la variazione di salute di un personaggio (anche del gruppo, che si vede in locazione), e porta in primo
	 * piano il riquadro del gruppo se è dei miei.
	 */
	public void variaSalute(VistaPersonaggio personaggio, int variazione) {
		if (!personaggio.isPNG()) {
			primoPiano(TipoFinestra.STATO);
		}
		aggiungiSprite(sprite(riquadroLocazione().spriteDellaSalute(personaggio, variazione)));
	}

	/**
	 * Come {@link #variaSalute}, per la magia.
	 */
	public void variaMagia(VistaPersonaggio personaggio, int variazione) {
		if (!personaggio.isPNG()) {
			primoPiano(TipoFinestra.STATO);
		}
		aggiungiSprite(sprite(riquadroLocazione().spriteDellaMagia(personaggio, variazione)));
	}

	/**
	 * Mostra con uno sprite che il gruppo ha raccolto un oggetto.
	 */
	public void raccogliOggetto() {
		aggiungiSprite(sprite(riquadroLocazione().spriteDelloOggettoRaccolto()));
	}

	/**
	 * Mostra con uno sprite l'effetto di stato appena subito (o finito) da un personaggio.
	 */
	public void aggiungiEffettoDiStato(VistaPersonaggio personaggio, TipoEffettoDiStato effettoDiStato, DoomdarkColorModel.Color colore) {
		aggiungiSprite(sprite(riquadroLocazione().spriteDellEffettoDiStato(personaggio, effettoDiStato.getDescrizione(), colore)));
	}

	/**
	 * Come l'effetto di stato, con la descrizione di un modificatore di attributo ("Furtività +5").
	 */
	public void aggiungiModificatore(VistaPersonaggio personaggio, String descrizione, DoomdarkColorModel.Color colore) {
		aggiungiSprite(sprite(riquadroLocazione().spriteDellEffettoDiStato(personaggio, descrizione, colore)));
	}

	/**
	 * Mostra con uno sprite, in verde, l'interazione fra tipo di danno e stato appena scattata su un personaggio.
	 */
	public void aggiungiInterazione(VistaPersonaggio personaggio, TipoInterazioneConEffettiDiStato interazione) {
		aggiungiSprite(sprite(riquadroLocazione().spriteDelTestoSulPersonaggio(personaggio, interazione.getDescrizione(),
				DoomdarkFont.medio(), DoomdarkColorModel.Color.GREEN)));
	}

	/**
	 * Mostra con uno sprite "Mancato", in grigio medio e in piccolo, il personaggio che il colpo non ha raggiunto.
	 */
	public void aggiungiColpoMancato(VistaPersonaggio personaggio) {
		aggiungiSprite(sprite(riquadroLocazione().spriteDelTestoSulPersonaggio(personaggio, TestiDellaUI.get("COLPO_MANCATO"),
				DoomdarkFont.piccolo(), DoomdarkColorModel.Color.MEDIUM_GRAY)));
	}

	/**
	 * Un testo che sale sopra il personaggio, di un colore ARGB costruito al volo (per esempio la reazione di classe a un danno).
	 */
	public void aggiungiTestoSulPersonaggio(VistaPersonaggio personaggio, String testo, int coloreArgb) {
		aggiungiSprite(sprite(riquadroLocazione().spriteDelTestoColoratoSulPersonaggio(personaggio, testo, coloreArgb)));
	}

	/**
	 * Aggiunge uno sprite a quelli in vita (null non aggiunge niente): la UI è occupata finché non finisce.
	 */
	protected final void aggiungiSprite(S nuovo) {
		if (nuovo != null) {
			sprite.add(nuovo);
			BusEventi.pubblica(new InternoUiOccupata());
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------
	// Fumetti e annunci

	public void notificaFumetto(String testo, CoordinateFumetto coordinate) {
		notificaFumetto(testo, coordinate.getX(), coordinate.getY(), coordinate.getPointToX(), coordinate.getPointToY());
	}

	/**
	 * Mostra un fumetto con quel testo in quel punto, con la punta verso quell'altro; se c'è già quello stesso fumetto lo fa
	 * ripartire, uno diverso lo interrompe (e scarta anche quelli in coda dietro di lui).
	 */
	public void notificaFumetto(String testo, int x, int y, int puntaVersoX, int puntaVersoY) {
		if (fumettoAttivo != null) {
			if (testo.equals(fumettoAttivo.getTesto())) {
				fumettoAttivo.resettaImpulsi();
				return;
			}
			fumettoAttivo = null;
			codaFumetti.clear();
		}
		accodaFumetto(fumetto(testo, disposizione.getLarghezzaContenuto() / 5, x, y, puntaVersoX, puntaVersoY));
	}

	/**
	 * Mette in coda un fumetto già fatto (null non aggiunge niente).
	 */
	protected final void accodaFumetto(F nuovo) {
		if (nuovo != null) {
			codaFumetti.add(nuovo);
		}
	}

	/**
	 * Mostra un annuncio al centro dello schermo (per esempio una missione disponibile o completata); in un intermezzo lo rimanda a
	 * dopo.
	 */
	public void notificaAnnuncioGlobale(String etichetta, String messaggio) {
		A nuovo = annuncio(new DescrizioneAnnuncioGlobale(etichetta, messaggio, disposizione.getLarghezzaContenuto(),
				disposizione.getAltezzaContenuto(), false));
		if (stato == StatoDelloSchermo.INTERMEZZO) {
			// Fatto subito, l'annuncio non scade: il suo tempo parte dal primo disegno, non dalla costruzione
			annunciRimandati.add(nuovo);
			return;
		}
		accodaAnnuncio(nuovo);
	}

	/**
	 * Come {@link #notificaAnnuncioGlobale}, ma per annunci che si ripetono a raffica (per esempio un punto abilità speso dopo
	 * l'altro): quello in corso e quelli in coda dello stesso genere vengono scartati e il nuovo parte subito, invece di aspettare
	 * che sfilino tutti. Gli annunci di altro genere (una missione completata) non si toccano.
	 */
	public void notificaAnnuncioGlobaleSostituibile(String etichetta, String messaggio) {
		A nuovo = annuncio(new DescrizioneAnnuncioGlobale(etichetta, messaggio, disposizione.getLarghezzaContenuto(),
				disposizione.getAltezzaContenuto(), true));
		if (stato == StatoDelloSchermo.INTERMEZZO) {
			annunciRimandati.removeIf(AnnuncioDelloSchermo::isSostituibile);
			annunciRimandati.add(nuovo);
			return;
		}
		codaAnnunci.removeIf(AnnuncioDelloSchermo::isSostituibile);
		A inCorso = annuncioAttivo;
		if (inCorso != null && inCorso.isSostituibile()) {
			annuncioAttivo = null;
		}
		accodaAnnuncio(nuovo);
	}

	/**
	 * Mette in coda un annuncio già fatto (null non aggiunge niente): la UI è occupata finché non finisce.
	 */
	protected final void accodaAnnuncio(A nuovo) {
		if (nuovo != null) {
			codaAnnunci.add(nuovo);
			BusEventi.pubblica(new InternoUiOccupata());
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------
	// Per il disegno, a ogni fotogramma

	/**
	 * Anima (disegna) ogni sprite in vita, e toglie quelli che hanno finito.
	 */
	public void perOgniSprite(Consumer<S> anima) {
		List<S> copia = new ArrayList<>(sprite);
		for (S corrente : copia) {
			anima.accept(corrente);
			if (!corrente.isAttivo()) {
				sprite.remove(corrente);
			}
		}
	}

	/**
	 * Anima (disegna) il fumetto corrente, se c'è (prendendo il primo in coda, se serve), e lo toglie se ha finito.
	 */
	public void disegnaIlFumetto(Consumer<F> anima) {
		if (fumettoAttivo == null && !codaFumetti.isEmpty()) {
			fumettoAttivo = codaFumetti.remove(0);
		}
		if (fumettoAttivo == null) {
			return;
		}
		F corrente = fumettoAttivo;
		anima.accept(corrente);
		if (!corrente.isAttivo() && fumettoAttivo == corrente) {
			fumettoAttivo = null;
		}
	}

	/**
	 * Anima (disegna) la rivelazione dell'artefatto corrente, se c'è. Solo nella schermata di gioco: altrove la rivelazione aspetta,
	 * perché il suo tempo avanza solo quando si disegna. Un effetto alla volta: con un annuncio a tutto schermo in corso, aspetta.
	 */
	public void disegnaLaRivelazione(Consumer<R> anima) {
		R attiva = rivelazioni.attiva(annuncioAttivo != null);
		if (attiva != null) {
			anima.accept(attiva);
		}
		rivelazioni.dopoIlDisegno();
	}

	/**
	 * Anima (disegna) l'annuncio corrente, se c'è. A differenza degli altri sprite, ancorati ai riquadri della schermata di gioco,
	 * l'annuncio è centrato sullo schermo e va disegnato sopra il contenuto in qualunque schermata. Un effetto alla volta: un
	 * annuncio (per esempio "MISSIONE COMPLETATA" per l'artefatto appena raccolto) aspetta che la rivelazione in corso o in coda sia
	 * finita.
	 */
	public void disegnaLAnnuncio(Consumer<A> anima) {
		if (annuncioAttivo == null && !codaAnnunci.isEmpty() && !rivelazioni.isOccupata()) {
			annuncioAttivo = codaAnnunci.remove(0);
		}
		A attivo = annuncioAttivo;
		if (attivo == null) {
			return;
		}
		anima.accept(attivo);
		if (!attivo.isAttivo() && annuncioAttivo == attivo) {
			annuncioAttivo = null;
		}
	}

	/**
	 * Alla fine di ogni fotogramma, in qualunque schermata: appena sprite, annunci e rivelazioni sono tutti finiti, l'Automa può
	 * mostrare il prossimo intermezzo in attesa (se ce n'è uno).
	 */
	public void fineFotogramma() {
		boolean occupataOra = !sprite.isEmpty() || annuncioAttivo != null || !codaAnnunci.isEmpty() || rivelazioni.isOccupata();
		if (uiOccupata && !occupataOra) {
			BusEventi.pubblica(new InternoUiInattiva());
		}
		uiOccupata = occupataOra;
	}

	/**
	 * Se c'è qualcosa che si muove e lo schermo va ridisegnato al prossimo fotogramma (Swing ridisegna solo allora; libGDX sempre).
	 */
	public boolean deveRidisegnare() {
		StatoDelloSchermo corrente = stato;
		return corrente == StatoDelloSchermo.IN_GIOCO || corrente == StatoDelloSchermo.MAPPA || corrente == StatoDelloSchermo.INTRO
				|| corrente == StatoDelloSchermo.INVENTARIO || corrente == StatoDelloSchermo.COMMERCIANTE
				|| corrente == StatoDelloSchermo.TATTICHE || corrente == StatoDelloSchermo.ALCHIMISTA
				|| corrente == StatoDelloSchermo.INCANTATORE || corrente == StatoDelloSchermo.INTERMEZZO
				|| annuncioAttivo != null || !codaAnnunci.isEmpty()
				|| barraIcone().inTransizione() || schermataTrofei().inTransizione() || schermataBestiario().inTransizione()
				|| schermataGrimorio().inTransizione();
	}

	/**
	 * La sfera magica (decorazione nell'angolo in basso a sinistra) si vede sulla mappa a tutto schermo, se ci sono notizie.
	 */
	public boolean isSferaMagicaVisibile() {
		return stato == StatoDelloSchermo.MAPPA && !vistaPartita.getUltimeNotizie().isEmpty();
	}

	/**
	 * Quel che il router degli input (nel modulo comune) chiede allo schermo.
	 */
	public RouterDegliInput.Contesto contestoDegliInput() {
		return new RouterDegliInput.Contesto() {
			@Override
			public Finestra getBarraIcone() {
				return barraIcone();
			}

			@Override
			public Finestra getFinestraATuttoSchermo() {
				return finestraATuttoSchermo();
			}

			@Override
			public List<Finestra> getFinestreImpilate() {
				List<Finestra> finestre = new ArrayList<>();
				for (TipoFinestra tipo : getPila()) {
					finestre.add(finestra(tipo));
				}
				return finestre;
			}

			@Override
			public Area getArea(Finestra finestra) {
				return SchermoDelGioco.this.getArea(finestra);
			}

			@Override
			public boolean contiene(Finestra finestra, int xLocale, int yLocale) {
				// La barra decide da sé quali punti sono suoi: le icone ingrandite escono dal suo rettangolo
				if (finestra == barraIcone()) {
					return barraIcone().contiene(xLocale, yLocale);
				}
				return RouterDegliInput.Contesto.super.contiene(finestra, xLocale, yLocale);
			}

			@Override
			public boolean portaInPrimoPianoSeImpilato(Finestra finestra) {
				List<TipoFinestra> impilati = riquadriImpilati();
				for (TipoFinestra tipo : impilati) {
					if (finestra(tipo) == finestra) {
						if (tipo == impilati.get(impilati.size() - 1)) {
							return false;
						}
						primoPiano(tipo);
						return true;
					}
				}
				return false;
			}

			@Override
			public Object getStato() {
				return stato;
			}

			@Override
			public void ridisegna() {
				SchermoDelGioco.this.ridisegna();
			}
		};
	}
}
