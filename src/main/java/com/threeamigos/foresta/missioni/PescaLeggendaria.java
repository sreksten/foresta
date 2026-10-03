package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.CatalogoLeggendari;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.tools.Misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

/**
 * Quale leggendario racconta la prossima leggenda (vedi LaLeggenda), o mette in palio il prossimo torneo (vedi
 * IlTorneo), perché i set si possano completare: a caso, ma favorendo i pezzi mancanti dei set già cominciati.
 * <ul>
 * <li>Un set è cominciato se almeno un suo pezzo è già uscito in una leggenda della partita, e almeno uno manca.</li>
 * <li>Se c'è un set cominciato, una leggenda su due (vedi {@link #PROBABILITA_PEZZO_MANCANTE}) racconta un suo pezzo
 * mancante, del set più vicino a essere completo; le altre pescano a caso fra tutti i leggendari rimasti.</li>
 * <li>Se per {@link #LEGGENDE_SENZA_PEZZI_MASSIME} leggende di fila, con un set cominciato, non è uscito un pezzo
 * mancante, la successiva lo racconta per forza.</li>
 * </ul>
 */
public final class PescaLeggendaria {

	/**
	 * La probabilità, in percentuale, che con un set cominciato la leggenda racconti un suo pezzo mancante.
	 */
	public static final int PROBABILITA_PEZZO_MANCANTE = 50;
	/**
	 * Dopo quante leggende di fila senza pezzi mancanti, con un set cominciato, la successiva ne racconta uno per forza.
	 */
	public static final int LEGGENDE_SENZA_PEZZI_MASSIME = 3;

	private PescaLeggendaria() {
	}

	/**
	 * Le chiavi dei leggendari che le missioni della partita con un leggendario (vedi {@link ConLeggendario}), tranne
	 * quella, hanno già pescato: finite o no, bene o male.
	 */
	public static Set<String> giaPescati(Missione esclusa) {
		return RegistroMissioni.getTutteLeMissioni().stream()
				.filter(missione -> missione != esclusa && missione instanceof ConLeggendario)
				.map(missione -> ((ConLeggendario) missione).getLeggendario())
				.filter(leggendario -> leggendario != null)
				.map(OggettoLeggendario::getChiave)
				.collect(Collectors.toSet());
	}

	/**
	 * Se resta qualche leggendario che nessuno ha pescato.
	 */
	static boolean restaUnLeggendario(Set<String> giaPescati) {
		return !giaPescati.containsAll(CatalogoLeggendari.getTuttiILeggendari().keySet());
	}

	/**
	 * La riga del prossimo leggendario, fra quelli che nessuno ha già pescato: un pezzo mancante di un set cominciato,
	 * se tocca a lui (vedi {@link #pezzoDaRaccontare}), altrimenti uno a caso.
	 */
	static String pesca(Set<String> giaPescati, int leggendeSenzaPezzi) {
		Optional<String> pezzoMancante = pezzoDaRaccontare(giaPescati, leggendeSenzaPezzi, Dado::tiraAncheAUnaFaccia);
		if (pezzoMancante.isPresent()) {
			return CatalogoLeggendari.getLeggendario(pezzoMancante.get()).orElseThrow(IllegalStateException::new).getRiga();
		}
		return ProduttoreDiTestiCasuale.oggettoLeggendario(riga -> giaPescati.contains(OggettoLeggendario.da(riga).getChiave()))
				.orElseThrow(IllegalStateException::new);
	}

	/**
	 * Il pezzo mancante da raccontare, se questa volta tocca a un set cominciato; vuoto se si pesca a caso.
	 *
	 * @param giaPescati        le chiavi dei leggendari già usciti in una leggenda della partita
	 * @param leggendeSenzaPezzi quante leggende di fila, con un set cominciato, non hanno raccontato un pezzo mancante
	 * @param dado              un dado: dato N, un numero da 1 a N
	 */
	public static Optional<String> pezzoDaRaccontare(Set<String> giaPescati, int leggendeSenzaPezzi, IntUnaryOperator dado) {
		List<SetLeggendario> cominciati = setCominciati(giaPescati);
		if (cominciati.isEmpty()) {
			return Optional.empty();
		}
		if (leggendeSenzaPezzi < LEGGENDE_SENZA_PEZZI_MASSIME && dado.applyAsInt(100) > PROBABILITA_PEZZO_MANCANTE) {
			return Optional.empty();
		}
		List<String> mancanti = pezziMancanti(cominciati.get(0), giaPescati);
		return Optional.of(mancanti.get(dado.applyAsInt(mancanti.size()) - 1));
	}

	/**
	 * I set cominciati, dal più vicino a essere completo (la quota di pezzi già usciti più alta; a parità, quello a cui
	 * ne mancano meno, poi per chiave).
	 */
	public static List<SetLeggendario> setCominciati(Set<String> giaPescati) {
		return CatalogoLeggendari.getTuttiISet().values().stream()
				.filter(set -> {
					long usciti = usciti(set, giaPescati);
					return usciti > 0 && usciti < CatalogoLeggendari.getPezzi(set.getChiave()).size();
				})
				.sorted(Comparator.comparingDouble((SetLeggendario set) -> -quota(set, giaPescati))
						.thenComparingInt(set -> pezziMancanti(set, giaPescati).size())
						.thenComparing(SetLeggendario::getChiave))
				.collect(Collectors.toList());
	}

	/**
	 * Se il leggendario è un pezzo mancante di un set cominciato.
	 */
	public static boolean isPezzoMancante(String leggendario, Set<String> giaPescati) {
		return setCominciati(giaPescati).stream().anyMatch(set -> pezziMancanti(set, giaPescati).contains(leggendario));
	}

	/**
	 * Quello che dice il narratore, prima della leggenda, se il leggendario è un pezzo di un set di cui è già uscito
	 * qualche altro pezzo: "Vi interessa il Corredo di RomyJona? Allora ascoltate: questo è un altro dei suoi pezzi, e
	 * ne mancano ancora due." Vuoto se non è un pezzo di un set, o se è il primo che esce.
	 *
	 * @param giaPescati le chiavi dei leggendari usciti nelle altre leggende della partita
	 */
	public static Optional<String> battutaDelSet(OggettoLeggendario leggendario, Set<String> giaPescati) {
		if (leggendario.getSet() == null) {
			return Optional.empty();
		}
		return CatalogoLeggendari.getSet(leggendario.getSet()).flatMap(set -> {
			Set<String> pezzi = CatalogoLeggendari.getPezzi(set.getChiave());
			long usciti = pezzi.stream().filter(pezzo -> !pezzo.equals(leggendario.getChiave()) && giaPescati.contains(pezzo)).count();
			if (usciti == 0) {
				return Optional.empty();
			}
			long mancano = pezzi.size() - usciti - 1;
			String quantiMancano = mancano == 0 ? "ed è l'ultimo che manca."
					: mancano == 1 ? "e ne manca ancora uno." : "e ne mancano ancora " + Misc.getCardinaleM((int) mancano) + ".";
			return Optional.of("Vi interessa " + set.getNome() + "? Allora ascoltate: questo è un altro dei suoi pezzi, " + quantiMancano);
		});
	}

	private static List<String> pezziMancanti(SetLeggendario set, Set<String> giaPescati) {
		List<String> mancanti = new ArrayList<>(CatalogoLeggendari.getPezzi(set.getChiave()));
		mancanti.removeAll(giaPescati);
		return mancanti;
	}

	private static long usciti(SetLeggendario set, Set<String> giaPescati) {
		return CatalogoLeggendari.getPezzi(set.getChiave()).stream().filter(giaPescati::contains).count();
	}

	private static double quota(SetLeggendario set, Set<String> giaPescati) {
		return (double) usciti(set, giaPescati) / CatalogoLeggendari.getPezzi(set.getChiave()).size();
	}
}
