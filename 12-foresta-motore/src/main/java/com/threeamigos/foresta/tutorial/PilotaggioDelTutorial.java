package com.threeamigos.foresta.tutorial;

import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.RegoleEquipaggiamento;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.TruccatoreDiDadi;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoSuggerimento;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Pilota la partita nei primi turni, a tutorial acceso, truccando i dadi perché il giocatore incontri le cose una alla volta (vedi
 * {@code tutorial.md} §4 ter): un mostro per turno, sempre uno solo. È il {@link TruccatoreDiDadi} sempre installato dal motore; se c'è
 * una finestra di prova i suoi trucchi hanno la precedenza. A tutorial spento, o finita la sequenza, non decide mai niente e i dadi
 * tornano al caso.
 * <p>
 * <b>Il passo</b> (quale mostro, che cosa truccare) dipende da quello che il giocatore ha già letto, non dal numero del turno: ogni passo comincia
 * quando è stato visto il suggerimento che lo sblocca (vedi {@code SUGGERIMENTI_CHE_SBLOCCANO}; conta solo il precedente, perché un mostro non può
 * aspettare la spiegazione di quel che porta). Il passo si fissa all'inizio di ogni locazione ({@link #iniziaLocazione}) e non cambia finché quella
 * non è finita: un suggerimento letto in mezzo a uno scontro non deve togliere la protezione al giocatore. Se i suggerimenti non si vedono (l'interfaccia
 * non li mostra: {@link RegistroSuggerimenti#isMostrabile}) o il passo non avanza, dopo {@link #TURNI_MASSIMI} turni il tutorial smette comunque di
 * pilotare.
 */
public final class PilotaggioDelTutorial implements TruccatoreDiDadi {

	/** Il mostro di ogni turno, dal primo: l'arpia, il goblin e il folletto. */
	private static final TipoPersonaggio[] MOSTRI_DEI_PRIMI_TURNI = {TipoPersonaggio.ARPIA, TipoPersonaggio.GOBLIN, TipoPersonaggio.FOLLETTO};

	/**
	 * Il suggerimento che sblocca ogni passo dopo il primo: il passo n comincia quando si è visto il suggerimento n-1 di questa tabella (e tutti i
	 * precedenti). Visti tutti, la sequenza è finita.
	 */
	public static final TipoSuggerimento[] SUGGERIMENTI_CHE_SBLOCCANO = {TipoSuggerimento.COMBATTIMENTO, TipoSuggerimento.CORRUZIONE, TipoSuggerimento.AMICIZIA};

	/** Dopo tanti turni giocati il tutorial non pilota più, qualunque cosa abbia visto il giocatore (non resta fermo a ripetere un passo per sempre) */
	public static final int TURNI_MASSIMI = 8;

	private static final int PASSO_DEL_GOBLIN = 1;
	private static final int PASSO_DEL_FOLLETTO = 2;
	private static final int PASSO_DA_CALCOLARE = -1;

	/**
	 * Quanti attacchi il giocatore fa al massimo nel primo scontro senza il combattimento automatico, se l'effetto di stato non arriva (un'arma che non ne
	 * lascia): poi l'icona torna comunque.
	 */
	public static final int ATTACCHI_PRIMA_DEL_COMBATTIMENTO_AUTOMATICO = 4;
	private static final double SALUTE_MINIMA_PERCHE_L_ARPIA_COLPISCA = 0.75;

	// Il passo della locazione in corso, fissato all'inizio della locazione (vedi iniziaLocazione)
	private static volatile int passoDellaLocazione = PASSO_DA_CALCOLARE;

	// Il primo scontro, un attacco alla volta: quanti colpi con un'arma che può lasciare un effetto di stato sono stati tirati (il primo non ne lascia, il
	// secondo sì), se l'effetto c'è stato e quanti attacchi ha fatto il giocatore. Si azzerano a ogni locazione
	private static volatile int colpiConEffettoTirati;
	private static volatile boolean effettoDiStatoLasciato;
	private static volatile int attacchiDelGiocatore;
	// Quanti colpi dei mostri sono stati tirati nel primo scontro: uno va a vuoto, uno colpisce (vedi lancio)
	private static volatile int colpiDeiMostriTirati;

	private final TruccatoreDiDadi prioritario;

	/**
	 * @param prioritario chi decide prima del tutorial (i trucchi della finestra di prova), o null
	 */
	public PilotaggioDelTutorial(TruccatoreDiDadi prioritario) {
		this.prioritario = prioritario;
	}

	/**
	 * Comincia una locazione: si guarda a che punto del tutorial è il giocatore (quali suggerimenti ha visto) e quel passo vale fino alla locazione
	 * dopo; si avanza di un passo per volta. Lo chiama {@code LocazioneBase.crea}.
	 */
	public static void iniziaLocazione() {
		// Si avanza di un passo per locazione, al massimo: un suggerimento visto prima del suo momento (l'inventario, per un oggetto trovato per caso) non
		// deve far saltare un passo intero
		int precedente = passoDellaLocazione == PASSO_DA_CALCOLARE ? 0 : passoDellaLocazione;
		passoDellaLocazione = Math.min(calcolaIlPasso(), precedente + 1);
		azzeraIlPrimoScontro();
	}

	/**
	 * Si ricomincia da capo (una partita nuova): il primo passo si ricalcola da quello che il giocatore ha visto.
	 */
	public static void ricomincia() {
		passoDellaLocazione = PASSO_DA_CALCOLARE;
		azzeraIlPrimoScontro();
	}

	private static void azzeraIlPrimoScontro() {
		colpiConEffettoTirati = 0;
		effettoDiStatoLasciato = false;
		attacchiDelGiocatore = 0;
		colpiDeiMostriTirati = 0;
	}

	/**
	 * Il giocatore ha scelto un attacco singolo (lo chiama {@code LocazioneBase}): serve a non lasciarlo senza il combattimento automatico per sempre.
	 */
	/** Se il capo ha ancora almeno i tre quarti della salute: finché è così, nel primo scontro l'arpia lo può colpire */
	private static boolean isCapoInSalute() {
		Personaggio capo = GruppoGiocatore.getIstanza().getCapo();
		return capo.getSalute() >= SALUTE_MINIMA_PERCHE_L_ARPIA_COLPISCA * capo.getSaluteMassima();
	}

	public static void registraUnAttaccoDelGiocatore() {
		attacchiDelGiocatore++;
	}

	/**
	 * Se nel primo scontro l'icona del combattimento automatico non c'è ancora: il giocatore attacca un colpo alla volta, il primo senza effetti di stato e il
	 * secondo con uno, che il tutorial spiega (vedi {@code tutorial.md} §4 ter) e interrompe lo scontro; l'icona compare quando l'effetto è arrivato, e il
	 * suggerimento che la spiega subito dopo aver letto quello. Se l'effetto non arriva l'icona torna dopo
	 * {@value #ATTACCHI_PRIMA_DEL_COMBATTIMENTO_AUTOMATICO} attacchi.
	 */
	public static boolean isCombattimentoAutomaticoVietato() {
		return isPrimoScontro() && !effettoDiStatoLasciato && !RegistroSuggerimenti.isVisto(TipoSuggerimento.EFFETTI_DI_STATO)
				&& attacchiDelGiocatore < ATTACCHI_PRIMA_DEL_COMBATTIMENTO_AUTOMATICO;
	}

	/**
	 * Il passo di adesso: quanti suggerimenti che sbloccano, in ordine, il giocatore ha già visto.
	 */
	private static int calcolaIlPasso() {
		int passo = 0;
		while (passo < SUGGERIMENTI_CHE_SBLOCCANO.length && RegistroSuggerimenti.isVisto(SUGGERIMENTI_CHE_SBLOCCANO[passo])) {
			passo++;
		}
		return passo;
	}

	private static int passo() {
		int passo = passoDellaLocazione;
		return passo == PASSO_DA_CALCOLARE ? calcolaIlPasso() : passo;
	}

	/**
	 * Se in questa locazione il tutorial pilota: è acceso, l'interfaccia mostra i suggerimenti, la sequenza non è finita e non si sono giocati troppi turni.
	 */
	public static boolean pilotaQuestoTurno() {
		return mostroDelTurno().isPresent();
	}

	/**
	 * Se questo è il primo scontro del tutorial (l'arpia): niente fuga né passaggio inosservato.
	 */
	public static boolean isPrimoScontro() {
		return pilotaQuestoTurno() && passo() == 0;
	}

	/**
	 * Il mostro che il tutorial vuole in questa locazione, se lo pilota.
	 */
	public static Optional<TipoPersonaggio> mostroDelTurno() {
		int passo = passo();
		if (!RegistroSuggerimenti.isAttivo() || !RegistroSuggerimenti.isMostrabile() || Statistiche.getTurniGiocati() >= TURNI_MASSIMI
				|| passo >= MOSTRI_DEI_PRIMI_TURNI.length) {
			return Optional.empty();
		}
		return Optional.of(MOSTRI_DEI_PRIMI_TURNI[passo]);
	}

	/**
	 * Gli incontri ammessi in una locazione, con in più il mostro del turno se il tutorial lo pilota: la locazione (e l'inizio morbido, che
	 * esclude la chimera drago) non possono impedire di incontrare il mostro che il tutorial vuole. La scelta resta al dado.
	 */
	public static List<TipoPersonaggio> conIlMostroDelTurno(List<TipoPersonaggio> ammessi) {
		mostroDelTurno().ifPresent(mostro -> {
			if (!ammessi.contains(mostro)) {
				ammessi.add(mostro);
			}
		});
		return ammessi;
	}

	/**
	 * L'oggetto che il tutorial vuole far trovare, se lo pilota: il primo che il giocatore trova è un'arma che il capo del gruppo può impugnare, perché
	 * il suggerimento sull'inventario abbia qualcosa da equipaggiare (e non una gemma a caso). Vale finché quel suggerimento non è stato visto. La
	 * mazza prima di tutto: contro lo scheletro, le ossa si spezzano a bastonate. Un mago o un druido trovano un bastone magico, un bardo una spada.
	 */
	public static Optional<TipoBottino> oggettoDelTurno() {
		if (!pilotaQuestoTurno() || RegistroSuggerimenti.isVisto(TipoSuggerimento.OGGETTO_TROVATO)) {
			return Optional.empty();
		}
		TipoPersonaggio classe = GruppoGiocatore.getIstanza().getCapo().getClasse();
		if (RegoleEquipaggiamento.puoUsare(classe, TipoArtefatto.MAZZA)) {
			return Optional.of(TipoBottino.MAZZA);
		}
		if (RegoleEquipaggiamento.puoUsare(classe, TipoArtefatto.BASTONE_MAGICO)) {
			return Optional.of(TipoBottino.BASTONE_MAGICO);
		}
		return Optional.of(TipoBottino.SPADA);
	}

	/**
	 * Gli oggetti che una locazione può avere, con in più quello del turno se il tutorial lo pilota: la locazione (una radura non ne ha) non può
	 * impedire di trovarlo. La scelta resta al dado.
	 */
	public static List<TipoBottino> conLOggettoDelTurno(List<TipoBottino> possibili) {
		oggettoDelTurno().ifPresent(oggetto -> {
			if (!possibili.contains(oggetto)) {
				possibili.add(oggetto);
			}
		});
		return possibili;
	}

	@Override
	public OptionalInt lancio(ContestoLancio contesto, int min, int max) {
		if (prioritario != null) {
			OptionalInt voluto = prioritario.lancio(contesto, min, max);
			if (voluto.isPresent()) {
				return voluto;
			}
		}
		if (!pilotaQuestoTurno()) {
			return OptionalInt.empty();
		}
		int passo = passo();
		switch (contesto) {
			// Un mostro solo, per spiegare una cosa alla volta
			case NUMERO_MOSTRI:
				return OptionalInt.of(1);
			// L'arpia, nel primo scontro, non lancia incantesimi (un tiro di 100 non fa valere nessuna voce con una probabilità sotto il 100)
			case TATTICA_DEI_MOSTRI:
				return isPrimoScontro() ? OptionalInt.of(max) : OptionalInt.empty();
			// ...e colpisce ogni tanto, ma piano: un colpo sì e uno no, finché il capo ha almeno il 75% della salute; sotto, non colpisce più. Così si vede
			// che si può restare feriti, ma il primo scontro non si perde
			case COLPO_DEI_MOSTRI:
				if (!isPrimoScontro()) {
					return OptionalInt.empty();
				}
				return colpiDeiMostriTirati++ % 2 == 1 && isCapoInSalute() ? OptionalInt.of(min) : OptionalInt.of(max);
			// Il primo scontro, un attacco alla volta: il primo colpo (con un'arma che può lasciare un effetto di stato) non ne lascia, il secondo sì, perché
			// il tutorial lo spieghi mentre lo scontro è fermo; dopo averlo letto torna il caso
			case EFFETTO_DI_STATO_DELL_ARMA:
				if (!isPrimoScontro() || RegistroSuggerimenti.isVisto(TipoSuggerimento.EFFETTI_DI_STATO)) {
					return OptionalInt.empty();
				}
				if (colpiConEffettoTirati++ == 0) {
					return OptionalInt.of(max);
				}
				effettoDiStatoLasciato = true;
				return OptionalInt.of(min);
			// Il goblin si corrompe (il dado supera la soglia)
			case CORRUZIONE:
				return passo == PASSO_DEL_GOBLIN ? OptionalInt.of(max) : OptionalInt.empty();
			// Il folletto diventa amico (il dado è sotto il carisma) e non sfida a carta, forbici e sasso
			case AMICIZIA:
				return passo == PASSO_DEL_FOLLETTO ? OptionalInt.of(min) : OptionalInt.empty();
			case SFIDA_CARTA_FORBICI_SASSO:
				return passo == PASSO_DEL_FOLLETTO ? OptionalInt.of(max) : OptionalInt.empty();
			default:
				return OptionalInt.empty();
		}
	}

	@Override
	public <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi) {
		if (prioritario != null) {
			Optional<T> voluto = prioritario.scelta(contesto, ammessi);
			if (voluto.isPresent()) {
				return voluto;
			}
		}
		if (contesto == ContestoLancio.TIPO_OGGETTO_TROVATO) {
			Optional<TipoBottino> oggetto = oggettoDelTurno();
			if (oggetto.isPresent() && ammessi.contains(oggetto.get())) {
				@SuppressWarnings("unchecked")
				T scelto = (T) oggetto.get();
				return Optional.of(scelto);
			}
		}
		if (contesto == ContestoLancio.TIPO_MOSTRO) {
			Optional<TipoPersonaggio> mostro = mostroDelTurno();
			if (mostro.isPresent() && ammessi.contains(mostro.get())) {
				@SuppressWarnings("unchecked")
				T scelto = (T) mostro.get();
				return Optional.of(scelto);
			}
		}
		return Optional.empty();
	}
}
