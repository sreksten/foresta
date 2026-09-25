package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Incantamento;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Le regole della fusione (vedi artefatti_e_incantamenti.md, §2 "Fusione"): sul banco un solo artefatto
 * incantabile e almeno una pergamena; si trasferiscono tutti gli effetti delle pergamene (incantamenti e
 * modificatori), fino ai posti dell'artefatto, che contano anche gli effetti che ha già; costo 10 monete
 * più 5 per effetto trasferito. Le pergamene usate vengono distrutte.
 */
public final class RegoleIncantatura {

	private RegoleIncantatura() {
	}

	public static boolean isPergamena(Artefatto artefatto) {
		return artefatto.getTipo() == TipoArtefatto.INCANTAMENTO;
	}

	/**
	 * Numero di effetti (incantamenti più modificatori) di un artefatto o di una pergamena.
	 */
	public static int effetti(Artefatto artefatto) {
		return artefatto.getIncantamenti().size() + artefatto.getModificatori().size();
	}

	/**
	 * Se si può aggiungere l'oggetto al banco: un solo artefatto, incantabile, e mai più effetti di quanti
	 * l'artefatto ne possa ricevere. Vuoto se si può.
	 */
	public static Optional<MotivoRifiutoIncantatura> puoMettereSulBanco(Collection<Artefatto> banco, Artefatto nuovo) {
		Optional<Artefatto> artefatto = banco.stream().filter(a -> !isPergamena(a)).findFirst();
		List<Artefatto> pergamene = banco.stream().filter(RegoleIncantatura::isPergamena).collect(Collectors.toList());
		if (isPergamena(nuovo)) {
			pergamene.add(nuovo);
		} else {
			if (artefatto.isPresent()) {
				return Optional.of(MotivoRifiutoIncantatura.PIU_ARTEFATTI);
			}
			if (!nuovo.isIncantabile()) {
				return Optional.of(MotivoRifiutoIncantatura.NON_INCANTABILE);
			}
			artefatto = Optional.of(nuovo);
		}
		// Finché manca l'artefatto non si sa quanti posti ha: il limite si controlla quando arriva
		if (artefatto.isPresent() && !ciStanno(artefatto.get(), pergamene)) {
			return Optional.of(MotivoRifiutoIncantatura.LIMITE_SUPERATO);
		}
		return Optional.empty();
	}

	/**
	 * Se si può fare la fusione con quel che c'è sul banco e le monete del gruppo. Vuoto se si può.
	 */
	public static Optional<MotivoRifiutoIncantatura> verifica(Collection<Artefatto> banco, int monete) {
		return verifica(banco, monete, 0);
	}

	/**
	 * Come {@link #verifica(Collection, int)}, con il costo scontato secondo la CONTRATTAZIONE di chi tratta.
	 */
	public static Optional<MotivoRifiutoIncantatura> verifica(Collection<Artefatto> banco, int monete, int contrattazione) {
		List<Artefatto> artefatti = banco.stream().filter(a -> !isPergamena(a)).collect(Collectors.toList());
		List<Artefatto> pergamene = banco.stream().filter(RegoleIncantatura::isPergamena).collect(Collectors.toList());
		if (artefatti.isEmpty()) {
			return Optional.of(MotivoRifiutoIncantatura.NESSUN_ARTEFATTO);
		}
		if (artefatti.size() > 1) {
			return Optional.of(MotivoRifiutoIncantatura.PIU_ARTEFATTI);
		}
		Artefatto artefatto = artefatti.get(0);
		if (!artefatto.isIncantabile()) {
			return Optional.of(MotivoRifiutoIncantatura.NON_INCANTABILE);
		}
		if (pergamene.isEmpty()) {
			return Optional.of(MotivoRifiutoIncantatura.NESSUNA_PERGAMENA);
		}
		if (!ciStanno(artefatto, pergamene)) {
			return Optional.of(MotivoRifiutoIncantatura.LIMITE_SUPERATO);
		}
		if (monete < costo(banco, contrattazione)) {
			return Optional.of(MotivoRifiutoIncantatura.MONETE_INSUFFICIENTI);
		}
		return Optional.empty();
	}

	/**
	 * Costo della fusione: 10 monete più 5 per ogni effetto delle pergamene sul banco.
	 */
	public static int costo(Collection<Artefatto> banco) {
		int effettiTrasferiti = banco.stream().filter(RegoleIncantatura::isPergamena).mapToInt(RegoleIncantatura::effetti).sum();
		return Costanti.FUSIONE_COSTO_BASE + Costanti.FUSIONE_COSTO_PER_EFFETTO * effettiTrasferiti;
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
		return banco.stream().filter(a -> !isPergamena(a)).findFirst();
	}

	/**
	 * Fa la fusione, che va prima verificata ({@link #verifica}): copia sull'artefatto gli effetti delle
	 * pergamene, gli dà il nome proprio (normalizzato; vuoto vuol dire nessun nome) e svuota il banco.
	 * Le pergamene spariscono. Restituisce l'artefatto incantato; le monete le toglie chi chiama.
	 */
	static Artefatto fondi(BancoDiLavoro banco, String nomeProprio) {
		Collection<Artefatto> sulBanco = banco.getInventario();
		Artefatto artefatto = artefattoSulBanco(sulBanco).orElseThrow(IllegalStateException::new);
		ArtefattoMD md = artefatto.getModelloDati();
		for (Artefatto oggetto : sulBanco) {
			if (isPergamena(oggetto)) {
				for (Incantamento incantamento : oggetto.getIncantamenti()) {
					md.addIncantamento(incantamento);
				}
				for (ModificatoreAttributo modificatore : oggetto.getModificatori()) {
					md.addModificatore(modificatore);
				}
			}
			// Il banco si svuota: le pergamene spariscono, l'artefatto lo rimette nel gruppo chi chiama
			banco.removeArtefatto(oggetto);
		}
		md.setNomeProprio(ArtefattoMD.normalizzaNomeProprio(nomeProprio));
		return artefatto;
	}

	private static boolean ciStanno(Artefatto artefatto, Collection<Artefatto> pergamene) {
		int daAggiungere = pergamene.stream().mapToInt(RegoleIncantatura::effetti).sum();
		return effetti(artefatto) + daAggiungere <= artefatto.getEffettiMassimi();
	}
}
