package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.SetLeggendario;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.tipi.TipoModificatore;
import com.threeamigos.foresta.tools.Misc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * I set leggendari (vedi CatalogoLeggendari): un personaggio che ne indossa tutti i pezzi ha i bonus di ognuno
 * moltiplicati per il moltiplicatore del set. Si moltiplicano i modificatori positivi e gli incantamenti scritti
 * sull'artefatto, anche quelli aggiunti dall'incantatore; non i malus, né quello che l'artefatto dà di suo (i danni
 * dell'arma, la parata di uno scudo). Il moltiplicatore si applica quando si fanno i conti, mai sull'artefatto:
 * togliendo un pezzo i bonus tornano quelli di prima.
 */
public final class RegoleSetLeggendari {

	private RegoleSetLeggendari() {
	}

	/**
	 * Quanti pezzi del set il personaggio indossa.
	 */
	public static int pezziIndossati(Collection<ArtefattoMD> indossati, String set) {
		return pezziDelSet(indossati, set).size();
	}

	/**
	 * Se il personaggio indossa tutti i pezzi del set.
	 */
	public static boolean isCompleto(Collection<ArtefattoMD> indossati, String set) {
		Set<String> pezzi = CatalogoLeggendari.getPezzi(set);
		return !pezzi.isEmpty() && pezziDelSet(indossati, set).containsAll(pezzi);
	}

	/**
	 * Per quanto si moltiplicano i bonus dell'artefatto addosso a chi indossa quegli artefatti: il moltiplicatore
	 * del suo set se il set è completo, altrimenti 1.
	 */
	public static double moltiplicatore(Collection<ArtefattoMD> indossati, ArtefattoMD artefatto) {
		String set = artefatto.getSetLeggendario();
		if (set == null || !isCompleto(indossati, set)) {
			return 1.0d;
		}
		return CatalogoLeggendari.getSet(set).map(SetLeggendario::getMoltiplicatore).orElse(1.0d);
	}

	/**
	 * Il modificatore con il moltiplicatore: solo se è un bonus, fisso o percentuale.
	 */
	public static ModificatoreAttributo applica(ModificatoreAttributo modificatore, double moltiplicatore) {
		boolean bonus = modificatore.getQuantita() > 0
				&& (modificatore.getTipoModificatoreAttributo() == TipoModificatore.AUMENTO_FISSO
				|| modificatore.getTipoModificatoreAttributo() == TipoModificatore.AUMENTO_PERCENTUALE);
		if (moltiplicatore == 1.0d || !bonus) {
			return modificatore;
		}
		return new ModificatoreAttributo(modificatore.getTipoAttributo(), modificatore.getTipoModificatoreAttributo(),
				modificatore.getQuantita() * moltiplicatore, modificatore.getNote());
	}

	/**
	 * Per i testi, il set di cui l'artefatto è un pezzo: "Pezzo del Corredo di RomyJona (4 pezzi, bonus x1,5)";
	 * vuoto se non è un pezzo di un set.
	 */
	public static Optional<String> descrizioneSet(ArtefattoMD artefatto) {
		String chiave = artefatto.getSetLeggendario();
		if (chiave == null) {
			return Optional.empty();
		}
		return CatalogoLeggendari.getSet(chiave).map(set -> "Pezzo " + Misc.conPreposizione("di", set.getNome()) + " ("
				+ CatalogoLeggendari.getPezzi(chiave).size() + " pezzi, bonus x"
				+ new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ITALIAN)).format(set.getMoltiplicatore()) + ")");
	}

	private static Set<String> pezziDelSet(Collection<ArtefattoMD> indossati, String set) {
		return indossati.stream()
				.filter(artefatto -> set.equals(artefatto.getSetLeggendario()))
				.map(ArtefattoMD::getPezzoLeggendario)
				.collect(Collectors.toSet());
	}
}
