package com.threeamigos.foresta.tutorial;

import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.TruccatoreDiDadi;
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

	@Override
	public OptionalInt lancio(ContestoLancio contesto, int min, int max) {
		if (prioritario != null) {
			OptionalInt voluto = prioritario.lancio(contesto, min, max);
			if (voluto.isPresent()) {
				return voluto;
			}
		}
		// Un mostro solo, per spiegare una cosa alla volta
		if (contesto == ContestoLancio.NUMERO_MOSTRI && pilotaQuestoTurno()) {
			return OptionalInt.of(1);
		}
		return OptionalInt.empty();
	}

	@Override
	public <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi) {
		if (prioritario != null) {
			Optional<T> voluto = prioritario.scelta(contesto, ammessi);
			if (voluto.isPresent()) {
				return voluto;
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
