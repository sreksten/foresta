package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.tipi.SupertipoArtefatto;
import com.threeamigos.foresta.motore.tipi.TipoArtefatto;
import com.threeamigos.foresta.motore.tipi.TipoAttributo;
import com.threeamigos.foresta.motore.tipi.TipoMotivoRifiutoIncantatura;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Incantamento;
import com.threeamigos.foresta.oggetti.ListinoPergamene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Le regole della fusione (vedi artefatti_e_incantamenti.md, §2 "Fusione"): sul banco un solo artefatto
 * incantabile e almeno un ingrediente magico (pergamena, gemma, monile, gingillo, sigillo). Degli effetti
 * degli ingredienti (incantamenti e modificatori) passano sull'artefatto quelli che vi hanno senso, i più
 * preziosi per primi (secondo {@link ListinoPergamene}), finché l'artefatto ha posti liberi; gli altri vanno
 * persi, e l'incantatore lo dice prima. Su un libro magico non passano gli incantamenti elementali, che
 * agiscono solo sulle armi; il POTERE_MAGICO passa solo su bastoni e libri magici. Costo 10 monete più 5 per
 * effetto trasferito. Gli ingredienti usati vengono distrutti.
 */
public final class RegoleIncantatura {

	private RegoleIncantatura() {
	}

	public static boolean isIngrediente(Artefatto artefatto) {
		return artefatto.getTipo().isIngrediente();
	}

	/**
	 * Numero di effetti (incantamenti più modificatori) di un artefatto o di un ingrediente.
	 */
	public static int effetti(Artefatto artefatto) {
		return artefatto.getIncantamenti().size() + artefatto.getModificatori().size();
	}

	/**
	 * I posti che restano all'artefatto per gli effetti nuovi.
	 */
	public static int postiLiberi(Artefatto artefatto) {
		return Math.max(0, artefatto.getEffettiMassimi() - effetti(artefatto));
	}

	/**
	 * Gli effetti degli ingredienti sul banco che passeranno sull'artefatto (tutti, finché l'artefatto manca).
	 */
	public static int effettiDaTrasferire(Collection<Artefatto> banco) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		if (!artefatto.isPresent()) {
			return ingredienti(banco).stream().mapToInt(RegoleIncantatura::effetti).sum();
		}
		return effettiChePassano(artefatto.get(), ingredienti(banco)).size();
	}

	/**
	 * Se la fusione farà perdere qualche effetto degli ingredienti sul banco, e perché: l'incantatore lo dice,
	 * ma la fusione si può fare. Vuoto se passa tutto (o se manca l'artefatto).
	 */
	public static Optional<String> avvisoEffettiPersi(Collection<Artefatto> banco) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		if (!artefatto.isPresent()) {
			return Optional.empty();
		}
		List<Artefatto> ingredienti = ingredienti(banco);
		int totale = ingredienti.stream().mapToInt(RegoleIncantatura::effetti).sum();
		int sensati = ingredienti.stream().mapToInt(i -> trasferibili(artefatto.get(), i).size()).sum();
		int passano = effettiChePassano(artefatto.get(), ingredienti).size();
		if (passano == totale) {
			return Optional.empty();
		}
		if (sensati > passano) {
			return Optional.of(passano == 1
					? "L'artefatto ha posto per un solo effetto: passerà il migliore, gli altri andranno persi."
					: "L'artefatto ha posto per " + passano + " effetti: passeranno i migliori, gli altri andranno persi.");
		}
		return Optional.of(isLibro(artefatto.get())
				? "Sul libro gli incantamenti elementali non hanno effetto: passeranno solo gli altri effetti."
				: "Il potere magico si fonde solo su bastoni e libri magici: passeranno solo gli altri effetti.");
	}

	/**
	 * Se si può aggiungere l'oggetto al banco: un solo artefatto, incantabile, con almeno un posto libero, e
	 * nessun ingrediente che sull'artefatto non avrebbe effetto. Vuoto se si può.
	 */
	public static Optional<TipoMotivoRifiutoIncantatura> puoMettereSulBanco(Collection<Artefatto> banco, Artefatto nuovo) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		List<Artefatto> ingredienti = ingredienti(banco);
		if (isIngrediente(nuovo)) {
			ingredienti.add(nuovo);
		} else {
			if (artefatto.isPresent()) {
				return Optional.of(TipoMotivoRifiutoIncantatura.PIU_ARTEFATTI);
			}
			if (!nuovo.isIncantabile()) {
				return Optional.of(TipoMotivoRifiutoIncantatura.NON_INCANTABILE);
			}
			artefatto = Optional.of(nuovo);
		}
		// Finché manca l'artefatto non si sa quanti posti ha né che cosa sia: si controlla quando arriva
		if (artefatto.isPresent()) {
			return problemi(artefatto.get(), ingredienti);
		}
		return Optional.empty();
	}

	/**
	 * Se si può fare la fusione con quel che c'è sul banco e le monete del gruppo. Vuoto se si può.
	 */
	public static Optional<TipoMotivoRifiutoIncantatura> verifica(Collection<Artefatto> banco, int monete) {
		return verifica(banco, monete, 0);
	}

	/**
	 * Come {@link #verifica(Collection, int)}, con il costo scontato secondo la CONTRATTAZIONE di chi tratta.
	 */
	public static Optional<TipoMotivoRifiutoIncantatura> verifica(Collection<Artefatto> banco, int monete, int contrattazione) {
		List<Artefatto> artefatti = banco.stream().filter(a -> !isIngrediente(a)).collect(Collectors.toList());
		List<Artefatto> ingredienti = ingredienti(banco);
		if (artefatti.isEmpty()) {
			return Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_ARTEFATTO);
		}
		if (artefatti.size() > 1) {
			return Optional.of(TipoMotivoRifiutoIncantatura.PIU_ARTEFATTI);
		}
		Artefatto artefatto = artefatti.get(0);
		if (!artefatto.isIncantabile()) {
			return Optional.of(TipoMotivoRifiutoIncantatura.NON_INCANTABILE);
		}
		if (ingredienti.isEmpty()) {
			return Optional.of(TipoMotivoRifiutoIncantatura.NESSUNA_PERGAMENA);
		}
		Optional<TipoMotivoRifiutoIncantatura> problema = problemi(artefatto, ingredienti);
		if (problema.isPresent()) {
			return problema;
		}
		if (monete < costo(banco, contrattazione)) {
			return Optional.of(TipoMotivoRifiutoIncantatura.MONETE_INSUFFICIENTI);
		}
		return Optional.empty();
	}

	/**
	 * Costo della fusione: 10 monete più 5 per ogni effetto degli ingredienti sul banco che passa sull'artefatto.
	 */
	public static int costo(Collection<Artefatto> banco) {
		return Costanti.FUSIONE_COSTO_BASE + Costanti.FUSIONE_COSTO_PER_EFFETTO * effettiDaTrasferire(banco);
	}

	/**
	 * Il costo della fusione scontato secondo la CONTRATTAZIONE di chi tratta (vedi RegoleContrattazione).
	 */
	public static int costo(Collection<Artefatto> banco, int contrattazione) {
		return RegoleContrattazione.prezzoAcquisto(costo(banco), contrattazione);
	}

	/**
	 * L'artefatto sul banco, se c'è.
	 */
	public static Optional<Artefatto> artefattoSulBanco(Collection<Artefatto> banco) {
		return banco.stream().filter(a -> !isIngrediente(a)).findFirst();
	}

	/**
	 * Fa la fusione, che va prima verificata ({@link #verifica}): copia sull'artefatto gli effetti degli
	 * ingredienti che passano, gli dà il nome proprio (normalizzato; vuoto vuol dire nessun nome) e svuota il
	 * banco. Gli ingredienti spariscono. Restituisce l'artefatto incantato; le monete le toglie chi chiama.
	 */
	static Artefatto fondi(BancoDiLavoro banco, String nomeProprio) {
		Collection<Artefatto> sulBanco = banco.getInventario();
		Artefatto artefatto = artefattoSulBanco(sulBanco).orElseThrow(IllegalStateException::new);
		ArtefattoMD md = artefatto.getModelloDati();
		for (Effetto effetto : effettiChePassano(artefatto, ingredienti(sulBanco))) {
			if (effetto.incantamento != null) {
				md.addIncantamento(effetto.incantamento);
			} else {
				md.addModificatore(effetto.modificatore);
			}
		}
		// Il banco si svuota: gli ingredienti spariscono, l'artefatto lo rimette nel gruppo chi chiama
		for (Artefatto oggetto : sulBanco) {
			banco.removeArtefatto(oggetto);
		}
		md.setNomeProprio(ArtefattoMD.normalizzaNomeProprio(nomeProprio));
		return artefatto;
	}

	private static List<Artefatto> ingredienti(Collection<Artefatto> banco) {
		return banco.stream().filter(RegoleIncantatura::isIngrediente).collect(Collectors.toList());
	}

	/**
	 * Perché l'artefatto non può ricevere niente da quegli ingredienti, se è così: non ha posti, oppure un
	 * ingrediente non vi avrebbe nessun effetto.
	 */
	private static Optional<TipoMotivoRifiutoIncantatura> problemi(Artefatto artefatto, Collection<Artefatto> ingredienti) {
		if (postiLiberi(artefatto) == 0) {
			return Optional.of(TipoMotivoRifiutoIncantatura.LIMITE_SUPERATO);
		}
		if (ingredienti.stream().anyMatch(i -> trasferibili(artefatto, i).isEmpty())) {
			return Optional.of(isLibro(artefatto)
					? TipoMotivoRifiutoIncantatura.INCANTAMENTO_SU_LIBRO
					: TipoMotivoRifiutoIncantatura.POTERE_MAGICO_FUORI_POSTO);
		}
		return Optional.empty();
	}

	/**
	 * Gli effetti degli ingredienti che passano: quelli che sull'artefatto hanno senso, dal più prezioso, fino ai
	 * posti liberi. A parità di prezzo vale l'ordine sul banco.
	 */
	private static List<Effetto> effettiChePassano(Artefatto artefatto, Collection<Artefatto> ingredienti) {
		return ingredienti.stream()
				.flatMap(i -> trasferibili(artefatto, i).stream())
				.sorted(Comparator.comparingDouble((Effetto e) -> e.prezzo).reversed())
				.limit(postiLiberi(artefatto))
				.collect(Collectors.toList());
	}

	/**
	 * Gli effetti di un ingrediente che sull'artefatto hanno senso.
	 */
	private static List<Effetto> trasferibili(Artefatto artefatto, Artefatto ingrediente) {
		List<Effetto> effetti = new ArrayList<>();
		if (!isLibro(artefatto)) {
			for (Incantamento incantamento : ingrediente.getIncantamenti()) {
				effetti.add(new Effetto(incantamento, null, ListinoPergamene.prezzo(incantamento)));
			}
		}
		for (ModificatoreAttributo modificatore : ingrediente.getModificatori()) {
			if (modificatore.getTipoAttributo() != TipoAttributo.POTERE_MAGICO || accettaPotereMagico(artefatto)) {
				effetti.add(new Effetto(null, modificatore, ListinoPergamene.prezzo(modificatore)));
			}
		}
		return effetti;
	}

	/**
	 * Il POTERE_MAGICO rende più forti gli incantesimi di chi porta l'artefatto: va solo su bastoni e libri magici,
	 * non su una spada che poi impugnerebbe un elfo o un bardo.
	 */
	private static boolean accettaPotereMagico(Artefatto artefatto) {
		TipoArtefatto tipo = artefatto.getTipo();
		return tipo == TipoArtefatto.BASTONE_MAGICO || tipo == TipoArtefatto.LIBRO_MAGICO;
	}

	private static boolean isLibro(Artefatto artefatto) {
		return artefatto.getTipo().getSupertipo() == SupertipoArtefatto.POTENZIAMENTO_POTERE_MAGICO;
	}

	/**
	 * Un effetto di un ingrediente: un incantamento oppure un modificatore, con il suo prezzo
	 */
	private static final class Effetto {

		private final Incantamento incantamento;
		private final ModificatoreAttributo modificatore;
		private final double prezzo;

		private Effetto(Incantamento incantamento, ModificatoreAttributo modificatore, double prezzo) {
			this.incantamento = incantamento;
			this.modificatore = modificatore;
			this.prezzo = prezzo;
		}
	}
}
