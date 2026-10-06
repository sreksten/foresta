package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.missioni.SetLeggendario;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tools.Misc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
	 * Dove sta un pezzo di un set, rispetto a un altro pezzo dello stesso set.
	 */
	public enum StatoPezzo {
		/**
		 * Lo indossa chi indossa l'altro pezzo.
		 */
		INDOSSATO,
		/**
		 * Ce l'ha il gruppo: nell'inventario, o addosso a un altro personaggio.
		 */
		DEL_GRUPPO,
		/**
		 * Il gruppo non ce l'ha.
		 */
		DA_TROVARE
	}

	/**
	 * Un pezzo di un set, per i testi: la chiave, il nome breve, il tipo e dove sta.
	 */
	public static final class Pezzo {
		private final String chiave;
		private final String nome;
		private final TipoArtefatto tipo;
		private final StatoPezzo stato;

		Pezzo(OggettoLeggendario leggendario, StatoPezzo stato) {
			this.chiave = leggendario.getChiave();
			this.nome = leggendario.getNomeBreve();
			this.tipo = leggendario.getTipo();
			this.stato = stato;
		}

		public String getChiave() {
			return chiave;
		}

		/**
		 * Il nome breve, con l'articolo.
		 */
		public String getNome() {
			return nome;
		}

		public TipoArtefatto getTipo() {
			return tipo;
		}

		public StatoPezzo getStato() {
			return stato;
		}
	}

	/**
	 * Per i testi, il set di cui l'artefatto è un pezzo: "Pezzo del Corredo di RomyJona (bonus x1,5)"; vuoto se non è
	 * un pezzo di un set.
	 */
	public static Optional<String> descrizioneSet(ArtefattoMD artefatto) {
		return set(artefatto).map(set -> "Pezzo " + Misc.conPreposizione("di", set.getNome()) + " (bonus x" + moltiplicatore(set) + ")");
	}

	/**
	 * Per i testi, i tipi dei pezzi del set dell'artefatto: "Spada, Elmo, Maschera, Schinieri"; vuoto se non è un pezzo
	 * di un set.
	 */
	public static Optional<String> tipiDelSet(ArtefattoMD artefatto) {
		return set(artefatto).map(set -> CatalogoLeggendari.getPezzi(set.getChiave()).stream()
				.map(pezzo -> leggendario(pezzo).getTipo().getDescrizione())
				.collect(Collectors.joining(", ")));
	}

	/**
	 * I pezzi del set dell'artefatto e dove stanno: indossati da chi indossa l'artefatto, del gruppo o da trovare.
	 * L'artefatto stesso è indossato se qualcuno lo indossa, altrimenti del gruppo se è nell'inventario, altrimenti
	 * (per esempio in un negozio) da trovare. Vuoto se non è un pezzo di un set.
	 *
	 * @param equipaggiamenti  gli artefatti indossati da ogni personaggio del gruppo
	 * @param inventarioGruppo gli artefatti del gruppo che nessuno indossa
	 */
	public static List<Pezzo> pezzi(ArtefattoMD artefatto, Collection<Collection<ArtefattoMD>> equipaggiamenti,
									Collection<ArtefattoMD> inventarioGruppo) {
		String chiave = artefatto.getSetLeggendario();
		if (chiave == null) {
			return Collections.emptyList();
		}
		Collection<ArtefattoMD> portatore = equipaggiamenti.stream()
				.filter(equipaggiamento -> equipaggiamento.contains(artefatto))
				.findFirst().orElse(Collections.emptyList());
		Set<String> indossati = pezziDelSet(portatore, chiave);
		Set<String> delGruppo = new HashSet<>(pezziDelSet(inventarioGruppo, chiave));
		equipaggiamenti.forEach(equipaggiamento -> delGruppo.addAll(pezziDelSet(equipaggiamento, chiave)));
		List<Pezzo> pezzi = new ArrayList<>();
		for (String pezzo : CatalogoLeggendari.getPezzi(chiave)) {
			StatoPezzo stato = indossati.contains(pezzo) ? StatoPezzo.INDOSSATO
					: delGruppo.contains(pezzo) ? StatoPezzo.DEL_GRUPPO : StatoPezzo.DA_TROVARE;
			pezzi.add(new Pezzo(leggendario(pezzo), stato));
		}
		return pezzi;
	}

	/**
	 * I set che chi indossa quegli artefatti ha completato.
	 */
	public static List<SetLeggendario> setCompleti(Collection<ArtefattoMD> indossati) {
		return indossati.stream()
				.map(ArtefattoMD::getSetLeggendario)
				.filter(Objects::nonNull)
				.distinct()
				.filter(set -> isCompleto(indossati, set))
				.map(CatalogoLeggendari::getSet)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	/**
	 * Per i testi, un set completo: "Set completo: Corredo di RomyJona (bonus x1,5)". Senza aggettivi da accordare,
	 * perché i set sono maschili e femminili.
	 */
	public static String descrizioneSetCompleto(SetLeggendario set) {
		return "Set completo: " + Misc.inizialeMaiuscola(senzaArticolo(set.getNome())) + " (bonus x" + moltiplicatore(set) + ")";
	}

	private static String senzaArticolo(String nome) {
		for (String articolo : new String[] {"il ", "lo ", "la ", "i ", "gli ", "le ", "l'"}) {
			if (nome.startsWith(articolo)) {
				return nome.substring(articolo.length());
			}
		}
		return nome;
	}

	private static OggettoLeggendario leggendario(String chiave) {
		return CatalogoLeggendari.getLeggendario(chiave).orElseThrow(() -> new IllegalStateException("Leggendario che non c'è: " + chiave));
	}

	private static Optional<SetLeggendario> set(ArtefattoMD artefatto) {
		String chiave = artefatto.getSetLeggendario();
		return chiave == null ? Optional.empty() : CatalogoLeggendari.getSet(chiave);
	}

	private static String moltiplicatore(SetLeggendario set) {
		return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ITALIAN)).format(set.getMoltiplicatore());
	}

	private static Set<String> pezziDelSet(Collection<ArtefattoMD> indossati, String set) {
		return indossati.stream()
				.filter(artefatto -> set.equals(artefatto.getSetLeggendario()))
				.map(ArtefattoMD::getPezzoLeggendario)
				.collect(Collectors.toSet());
	}
}
