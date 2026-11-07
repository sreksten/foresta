package com.threeamigos.foresta.tutorial;

import com.threeamigos.foresta.gruppi.GruppoGiocatore;
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

	/** Il mostro di ogni turno, dal primo: l'arpia, la chimera drago, lo scheletro, il goblin e il folletto. */
	private static final TipoPersonaggio[] MOSTRI_DEI_PRIMI_TURNI = {
			TipoPersonaggio.ARPIA, TipoPersonaggio.CHIMERA_DRAGO, TipoPersonaggio.SCHELETRO, TipoPersonaggio.GOBLIN, TipoPersonaggio.FOLLETTO};

	/**
	 * Il suggerimento che sblocca ogni passo dopo il primo: il passo n comincia quando si è visto il suggerimento n-1 di questa tabella (e tutti i
	 * precedenti). Visti tutti, la sequenza è finita.
	 */
	public static final TipoSuggerimento[] SUGGERIMENTI_CHE_SBLOCCANO = {TipoSuggerimento.COMBATTIMENTO, TipoSuggerimento.SCELTE_IN_COMBATTIMENTO,
			TipoSuggerimento.OGGETTO_TROVATO, TipoSuggerimento.CORRUZIONE, TipoSuggerimento.AMICIZIA};

	/** Dopo tanti turni giocati il tutorial non pilota più, qualunque cosa abbia visto il giocatore (non resta fermo a ripetere un passo per sempre) */
	public static final int TURNI_MASSIMI = 8;

	private static final int PASSO_DELLA_CHIMERA_DRAGO = 1;
	private static final int PASSO_DEL_GOBLIN = 3;
	private static final int PASSO_DEL_FOLLETTO = 4;
	private static final int PASSO_DA_CALCOLARE = -1;

	// Il passo della locazione in corso, fissato all'inizio della locazione (vedi iniziaLocazione)
	private static volatile int passoDellaLocazione = PASSO_DA_CALCOLARE;

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
	}

	/**
	 * Si ricomincia da capo (una partita nuova): il primo passo si ricalcola da quello che il giocatore ha visto.
	 */
	public static void ricomincia() {
		passoDellaLocazione = PASSO_DA_CALCOLARE;
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
	 * I passi in cui il giocatore non può perdere: il primo scontro (l'arpia) e quello con lo scheletro. Con la chimera drago non è protetto.
	 */
	private static boolean isPassoProtetto(int passo) {
		return MOSTRI_DEI_PRIMI_TURNI[passo] == TipoPersonaggio.ARPIA || MOSTRI_DEI_PRIMI_TURNI[passo] == TipoPersonaggio.SCHELETRO;
	}

	/**
	 * L'oggetto che il tutorial vuole far trovare in questo turno (quello dello scheletro), se lo pilota: un'arma che il capo del gruppo può
	 * impugnare, perché il suggerimento sull'inventario abbia qualcosa da equipaggiare. La mazza prima di tutto: contro lo scheletro, le ossa si
	 * spezzano a bastonate. Un mago o un druido trovano un bastone magico, un bardo una spada.
	 */
	public static Optional<TipoBottino> oggettoDelTurno() {
		if (!mostroDelTurno().filter(mostro -> mostro == TipoPersonaggio.SCHELETRO).isPresent()) {
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
			// Il primo scontro (l'arpia) e quello con lo scheletro non si possono perdere: i mostri non colpiscono e non lanciano incantesimi
			// (un tiro di 100 non va mai a segno, e non fa valere nessuna voce con una probabilità sotto il 100)
			case COLPO_DEI_MOSTRI:
			case TATTICA_DEI_MOSTRI:
				return isPassoProtetto(passo) ? OptionalInt.of(100) : OptionalInt.empty();
			// Davanti alla chimera drago conviene non farsi vedere: passare inosservati riesce sempre
			case PASSAGGIO_INOSSERVATO:
				return passo == PASSO_DELLA_CHIMERA_DRAGO ? OptionalInt.of(1) : OptionalInt.empty();
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
