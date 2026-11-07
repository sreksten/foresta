package com.threeamigos.foresta.tutorial;

import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.RegoleEquipaggiamento;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.TruccatoreDiDadi;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Pilota la partita nei primi turni, a tutorial acceso, truccando i dadi perché il giocatore incontri le cose una alla volta (vedi
 * {@code tutorial.md} §4 ter): un mostro per turno, sempre uno solo. È il {@link TruccatoreDiDadi} sempre installato dal motore; se c'è
 * una finestra di prova i suoi trucchi hanno la precedenza. A tutorial spento, o finita la sequenza, non decide mai niente e i dadi
 * tornano al caso.
 * <p>
 * Il turno è quello della partita ({@link Statistiche#getTurniGiocati()}), quindi non c'è altro stato da salvare. Quando i suggerimenti
 * saranno realizzati, il passo dipenderà da quelli già visti e non dal numero del turno.
 */
public final class PilotaggioDelTutorial implements TruccatoreDiDadi {

	/** Il mostro di ogni turno, dal primo: l'arpia, la chimera drago, lo scheletro, il goblin e il folletto. */
	private static final TipoPersonaggio[] MOSTRI_DEI_PRIMI_TURNI = {
			TipoPersonaggio.ARPIA, TipoPersonaggio.CHIMERA_DRAGO, TipoPersonaggio.SCHELETRO, TipoPersonaggio.GOBLIN, TipoPersonaggio.FOLLETTO};

	private static final int TURNO_DELLA_CHIMERA_DRAGO = 1;
	private static final int TURNO_DEL_GOBLIN = 3;
	private static final int TURNO_DEL_FOLLETTO = 4;

	private final TruccatoreDiDadi prioritario;

	/**
	 * @param prioritario chi decide prima del tutorial (i trucchi della finestra di prova), o null
	 */
	public PilotaggioDelTutorial(TruccatoreDiDadi prioritario) {
		this.prioritario = prioritario;
	}

	/**
	 * Se in questo turno il tutorial sceglie l'incontro: acceso, e la sequenza dei primi turni non è finita.
	 */
	public static boolean pilotaQuestoTurno() {
		return mostroDelTurno().isPresent();
	}

	/**
	 * Il mostro che il tutorial vuole in questo turno, se lo pilota.
	 */
	public static Optional<TipoPersonaggio> mostroDelTurno() {
		int turno = Statistiche.getTurniGiocati();
		if (!RegistroSuggerimenti.isAttivo() || turno < 0 || turno >= MOSTRI_DEI_PRIMI_TURNI.length) {
			return Optional.empty();
		}
		return Optional.of(MOSTRI_DEI_PRIMI_TURNI[turno]);
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
	 * I turni in cui il giocatore non può perdere: il primo scontro (l'arpia) e quello con lo scheletro. Con la chimera drago non è protetto.
	 */
	private static boolean isTurnoProtetto(int turno) {
		return MOSTRI_DEI_PRIMI_TURNI[turno] == TipoPersonaggio.ARPIA || MOSTRI_DEI_PRIMI_TURNI[turno] == TipoPersonaggio.SCHELETRO;
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
		int turno = Statistiche.getTurniGiocati();
		switch (contesto) {
			// Un mostro solo, per spiegare una cosa alla volta
			case NUMERO_MOSTRI:
				return OptionalInt.of(1);
			// Il primo scontro (l'arpia) e quello con lo scheletro non si possono perdere: i mostri non colpiscono e non lanciano incantesimi
			// (un tiro di 100 non va mai a segno, e non fa valere nessuna voce con una probabilità sotto il 100)
			case COLPO_DEI_MOSTRI:
			case TATTICA_DEI_MOSTRI:
				return isTurnoProtetto(turno) ? OptionalInt.of(100) : OptionalInt.empty();
			// Davanti alla chimera drago conviene non farsi vedere: passare inosservati riesce sempre
			case PASSAGGIO_INOSSERVATO:
				return turno == TURNO_DELLA_CHIMERA_DRAGO ? OptionalInt.of(1) : OptionalInt.empty();
			// Il goblin si corrompe (il dado supera la soglia)
			case CORRUZIONE:
				return turno == TURNO_DEL_GOBLIN ? OptionalInt.of(max) : OptionalInt.empty();
			// Il folletto diventa amico (il dado è sotto il carisma) e non sfida a carta, forbici e sasso
			case AMICIZIA:
				return turno == TURNO_DEL_FOLLETTO ? OptionalInt.of(min) : OptionalInt.empty();
			case SFIDA_CARTA_FORBICI_SASSO:
				return turno == TURNO_DEL_FOLLETTO ? OptionalInt.of(max) : OptionalInt.empty();
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
