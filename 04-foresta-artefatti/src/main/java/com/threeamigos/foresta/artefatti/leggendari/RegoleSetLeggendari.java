package com.threeamigos.foresta.artefatti.leggendari;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.artefatti.VistaPezzoDelSet;
import com.threeamigos.foresta.tipi.StatoPezzoDelSet;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoModificatore;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Testi;

/**
 * I set leggendari (vedi CatalogoLeggendari): un personaggio che ne indossa tutti i pezzi (o quanti ne bastano, per un set
 * con PEZZI=) ha i bonus di ognuno moltiplicati per il moltiplicatore del set. Si moltiplicano i modificatori positivi e gli incantamenti scritti
 * sull'artefatto, anche quelli aggiunti dall'incantatore; non i malus, né quello che l'artefatto dà di suo (i danni
 * dell'arma, la parata di uno scudo). Il moltiplicatore si applica quando si fanno i conti, mai sull'artefatto:
 * togliendo un pezzo i bonus tornano quelli di prima.
 */
public final class RegoleSetLeggendari {

	// I testi che parlano dei set stanno in set_leggendari_<lingua>.properties
	private static final String FILE_DEI_TESTI = "set_leggendari";

	private RegoleSetLeggendari() {
	}

	/**
	 * Quanti pezzi del set il personaggio indossa.
	 */
	public static int pezziIndossati(Collection<ArtefattoMD> indossati, String set) {
		return pezziDelSet(indossati, set).size();
	}

	/**
	 * Se il personaggio ha completato il set: ne indossa tutti i pezzi, o quanti ne bastano per un set che si completa con una parte
	 * dei suoi pezzi (vedi CatalogoLeggendari.getPezziRichiesti).
	 */
	public static boolean isCompleto(Collection<ArtefattoMD> indossati, String set) {
		int richiesti = CatalogoLeggendari.getPezziRichiesti(set);
		return richiesti > 0 && pezziDelSet(indossati, set).size() >= richiesti;
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
	public static ModificatoreAttributoMD applica(ModificatoreAttributoMD modificatore, double moltiplicatore) {
		boolean bonus = modificatore.getQuantita() > 0
				&& modificatore.getBersaglio() == BersaglioModificatore.PORTATORE
				&& (modificatore.getTipoModificatoreAttributo() == TipoModificatore.VARIAZIONE_FISSA
				|| modificatore.getTipoModificatoreAttributo() == TipoModificatore.VARIAZIONE_PERCENTUALE);
		if (moltiplicatore == 1.0d || !bonus) {
			return modificatore;
		}
		return modificatore.conQuantita(modificatore.getQuantita() * moltiplicatore);
	}

	/**
	 * Un pezzo di un set, per i testi: la chiave, il nome breve, il tipo e dove sta.
	 */
	public static final class Pezzo implements VistaPezzoDelSet {
		private final String chiave;
		private final String nome;
		private final TipoArtefatto tipo;
		private final StatoPezzoDelSet stato;

		Pezzo(OggettoLeggendario leggendario, StatoPezzoDelSet stato) {
			this.chiave = leggendario.getChiave();
			this.nome = leggendario.getNomeBreve();
			this.tipo = leggendario.getTipo();
			this.stato = stato;
		}

		@Override
		public String getChiave() {
			return chiave;
		}

		/**
		 * Il nome breve, con l'articolo.
		 */
		@Override
		public String getNome() {
			return nome;
		}

		@Override
		public TipoArtefatto getTipo() {
			return tipo;
		}

		@Override
		public StatoPezzoDelSet getStato() {
			return stato;
		}
	}

	/**
	 * Per i testi, il set di cui l'artefatto è un pezzo: "Pezzo del Corredo di RomyJona (bonus x1,5)"; vuoto se non è
	 * un pezzo di un set.
	 */
	public static Optional<String> descrizioneSet(ArtefattoMD artefatto) {
		return set(artefatto).map(set -> Testi.formatta(FILE_DEI_TESTI, set.isCompletabileInParte() ? "PEZZO_PARZIALE" : "PEZZO",
				Lingue.corrente().conPreposizione(Preposizione.DI, set.getNome()), moltiplicatore(set),
				CatalogoLeggendari.getPezziRichiesti(set.getChiave())));
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
			StatoPezzoDelSet stato = indossati.contains(pezzo) ? StatoPezzoDelSet.INDOSSATO
					: delGruppo.contains(pezzo) ? StatoPezzoDelSet.DEL_GRUPPO : StatoPezzoDelSet.DA_TROVARE;
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
		return Testi.formatta(FILE_DEI_TESTI, "SET_COMPLETO", Lingue.corrente().inizialeMaiuscola(Lingue.corrente().senzaArticolo(set.getNome())),
				moltiplicatore(set));
	}

	private static OggettoLeggendario leggendario(String chiave) {
		// Le chiavi dei pezzi di un set vengono dal catalogo, quindi ci sono sempre: fra i leggendari o fra quelli di bottino
		return CatalogoLeggendari.getLeggendario(chiave)
				.orElseThrow(() -> new IllegalStateException("Pezzo di un set che non è nel catalogo: " + chiave));
	}

	private static Optional<SetLeggendario> set(ArtefattoMD artefatto) {
		String chiave = artefatto.getSetLeggendario();
		return chiave == null ? Optional.empty() : CatalogoLeggendari.getSet(chiave);
	}

	/**
	 * Il moltiplicatore come si scrive nella lingua in uso: "1,5" in italiano, "1.5" in inglese
	 */
	private static String moltiplicatore(SetLeggendario set) {
		return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(set.getMoltiplicatore())
				.replace(".", Testi.get(FILE_DEI_TESTI, "SEPARATORE_DECIMALE").orElse("."));
	}

	private static Set<String> pezziDelSet(Collection<ArtefattoMD> indossati, String set) {
		return indossati.stream()
				.filter(artefatto -> set.equals(artefatto.getSetLeggendario()))
				.map(ArtefattoMD::getPezzoLeggendario)
				.collect(Collectors.toSet());
	}
}
