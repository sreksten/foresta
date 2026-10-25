package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.tipi.TipoSlotArtefatto;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegoleEquipaggiamento;
import com.threeamigos.foresta.motore.RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoEquipaggiamento;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Il cambio d'arma durante uno scontro: quali armi escono dalle mani per far posto a quella nuova (si sceglie guardando quanto danno
 * fanno, quindi serve il calcolatore del combattimento), se il personaggio può ancora cambiare arma in questo scontro (lo ricorda lo
 * {@link StatoDelCombattimento}) e il cambio stesso sull'inventario del gruppo. Le regole a slot dell'equipaggiamento restano in
 * {@link RegoleEquipaggiamento}.
 */
public final class CambioDArma {

	private CambioDArma() {
	}

	/**
	 * Gli oggetti che escono dalle mani del personaggio per far posto a un'arma nuova (il cambio d'arma, vedi
	 * tattiche_di_combattimento_automatiche.md §5): per un'arma da mano principale quella che occupa lo slot (o l'arma a due mani
	 * che occupa entrambi); per un'arma a due mani tutto quello che ha in mano. Lo scudo (e ciò che non è un'arma) non esce mai.
	 *
	 * @return gli artefatti che uscirebbero (anche nessuno), oppure vuoto se non si può fare perché in mano c'è qualcosa che non è un'arma
	 */
	private static Optional<List<ArtefattoMD>> chiEsce(Personaggio personaggio, ArtefattoMD nuova) {
		Collection<ArtefattoMD> equipaggiati = personaggio.getModelloDati().getArtefatti();
		TipoSlotArtefatto slotNuova = nuova.getTipo().getSlotArtefatto();
		List<ArtefattoMD> uscenti = new ArrayList<>();
		if (slotNuova == TipoSlotArtefatto.MANO_PRINCIPALE) {
			Optional<ArtefattoMD> occupante = RegoleEquipaggiamento.occupato(equipaggiati, TipoSlotArtefatto.ENTRAMBE_LE_MANI);
			if (!occupante.isPresent()) {
				occupante = RegoleEquipaggiamento.occupato(equipaggiati, TipoSlotArtefatto.MANO_PRINCIPALE);
			}
			Optional<ArtefattoMD> secondaria = RegoleEquipaggiamento.occupato(equipaggiati, TipoSlotArtefatto.MANO_SECONDARIA);
			// Con la doppia arma esce la più debole delle due (a parità la secondaria), purché la nuova possa stare nella secondaria
			if (occupante.isPresent() && secondaria.isPresent() && occupante.get().getTipo().getCategoria() == CategoriaArtefatto.ARMA
					&& secondaria.get().getTipo().getCategoria() == CategoriaArtefatto.ARMA
					&& RegoleEquipaggiamento.isArmaDaManoSecondaria(nuova.getTipo()) && RegoleEquipaggiamento.puoImpugnareDueArmi(personaggio.getClasse())
					&& dannoAtteso(personaggio, secondaria.get()) <= dannoAtteso(personaggio, occupante.get())) {
				occupante = secondaria;
			}
			occupante.ifPresent(uscenti::add);
		} else {
			for (TipoSlotArtefatto slot : new TipoSlotArtefatto[] {TipoSlotArtefatto.ENTRAMBE_LE_MANI,
					TipoSlotArtefatto.MANO_PRINCIPALE, TipoSlotArtefatto.MANO_SECONDARIA}) {
				RegoleEquipaggiamento.occupato(equipaggiati, slot).ifPresent(uscenti::add);
			}
		}
		for (ArtefattoMD uscente : uscenti) {
			if (uscente.getTipo().getCategoria() != CategoriaArtefatto.ARMA) {
				return Optional.empty();
			}
		}
		return Optional.of(uscenti);
	}

	/**
	 * Il controllo preventivo del cambio d'arma: la nuova arma si potrebbe impugnare una volta usciti gli oggetti che le fanno posto? Guarda
	 * la situazione "senza l'arma che esce" (carico, mani libere), che {@link #valuta} da sola non vede perché guarda l'equipaggiamento
	 * com'è adesso. Non cambia niente: gli artefatti che escono si tolgono solo per un attimo dall'elenco del personaggio e si rimettono
	 * dov'erano, senza ricalcoli né notifiche (a differenza di removeArtefatto, che riduce salute e MAGIA se l'arma ne dava il massimo).
	 *
	 * @param nuova un'arma da mano principale o a due mani
	 * @return lo slot che occuperebbe, o il motivo per cui il cambio non si può fare
	 */
	public static EsitoControlloRichiestaEquipaggiamento valutaCambioArma(Personaggio personaggio, ArtefattoMD nuova) {
		TipoSlotArtefatto slotNuova = nuova.getTipo().getSlotArtefatto();
		if (slotNuova != TipoSlotArtefatto.MANO_PRINCIPALE && slotNuova != TipoSlotArtefatto.ENTRAMBE_LE_MANI) {
			throw new IllegalArgumentException("Non è un'arma da mano principale o a due mani: " + nuova.getTipo());
		}
		PersonaggioMD modello = personaggio.getModelloDati();
		Optional<List<ArtefattoMD>> uscenti = chiEsce(personaggio, nuova);
		if (!uscenti.isPresent()) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.MANI_OCCUPATE_PER_ARMA_A_DUE_MANI);
		}
		List<ArtefattoMD> prima = new ArrayList<>(modello.getArtefatti());
		try {
			for (ArtefattoMD uscente : uscenti.get()) {
				modello.togliArtefatto(uscente);
			}
			return RegoleEquipaggiamento.valuta(personaggio, nuova);
		} finally {
			modello.ripristinaArtefatti(prima);
		}
	}

	/**
	 * Gli artefatti che uscirebbero dalle mani del personaggio se prendesse quell'arma (vuoto se nessuno, o se il cambio non si può fare).
	 */
	public static List<ArtefattoMD> uscentiPerCambioArma(Personaggio personaggio, ArtefattoMD nuova) {
		return chiEsce(personaggio, nuova).orElse(new ArrayList<>());
	}


	private static double dannoAtteso(Personaggio personaggio, ArtefattoMD arma) {
		return CalcolatoreCombattimento.dannoAttesoDellArma(personaggio, (Arma) Artefatto.di(arma));
	}

	/**
	 * Il controllo preventivo del cambio d'arma, sulla situazione "senza l'arma che esce" (vedi {@link #valutaCambioArma});
	 * non cambia niente e non pubblica niente. Include il massimo dei cambi per combattimento.
	 *
	 * @param nuova un'arma dell'inventario del gruppo, da mano principale o a due mani
	 * @return vuoto se il cambio si può fare, altrimenti il motivo
	 */
	public static Optional<EsitoControlloRichiestaEquipaggiamento> controllaCambioArma(Personaggio personaggio, Artefatto nuova) {
		EsitoControlloRichiestaEquipaggiamento esito =
				valutaCambioArma(personaggio, nuova.getModelloDati());
		return esito.getMotivo() == null ? Optional.empty() : Optional.of(esito);
	}

	/**
	 * Se il personaggio può ancora cambiare arma in questo combattimento (CostantiPersonaggi.MAX_CAMBI_ARMA_PER_COMBATTIMENTO) e con quest'arma;
	 * ricorda l'esito della valutazione nello stato del combattimento, così non si rifà (vedi {@link StatoDelCombattimento}).
	 */
	public static boolean puoCambiareArma(GruppoGiocatore gruppo, Personaggio personaggio, Artefatto nuova) {
		StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
		if (stato.getCambiDArma(personaggio) >= CostantiPersonaggi.MAX_CAMBI_ARMA_PER_COMBATTIMENTO) {
			return false;
		}
		Boolean valutata = stato.getValutazioneArma(personaggio, nuova);
		if (valutata != null) {
			return valutata;
		}
		boolean utilizzabile = !controllaCambioArma(personaggio, nuova).isPresent();
		stato.registraArmaValutata(personaggio, nuova, utilizzabile);
		return utilizzabile;
	}

	/**
	 * Il cambio d'arma come azione sola: se si può (puoCambiareArma) toglie dalle mani le armi che fanno posto, le mette nell'inventario
	 * del gruppo e impugna la nuova; conta il cambio nello stato del combattimento. È atomico: il controllo è già fatto, quindi non teme
	 * rifiuti a metà. Non consuma il turno: lo fa chi lo chiama. Niente notifiche di inventario, la UI vede le variazioni dei personaggi.
	 *
	 * @return true se il cambio è avvenuto
	 */
	public static boolean cambiaArma(GruppoGiocatore gruppo, Personaggio personaggio, Artefatto nuova) {
		if (!puoCambiareArma(gruppo, personaggio, nuova)) {
			return false;
		}
		for (ArtefattoMD uscente : uscentiPerCambioArma(personaggio, nuova.getModelloDati())) {
			gruppo.stocca(personaggio, Artefatto.di(uscente));
		}
		gruppo.equipaggia(personaggio, nuova);
		StatoDelCombattimento.getIstanza().registraCambioArma(personaggio);
		return true;
	}

	/**
	 * Le armi dell'inventario del gruppo che il personaggio può prendere al posto di quelle che ha in mano e che danno quel tipo di
	 * danno (Arma.daTipoDanno); servono a una tattica come "un'arma sacra contro i non morti".
	 */
	public static List<Artefatto> armiPerCambio(GruppoGiocatore gruppo, Personaggio personaggio, TipoDanno tipoDanno) {
		List<Artefatto> trovate = new ArrayList<>();
		for (Artefatto artefatto : gruppo.getInventario()) {
			TipoSlotArtefatto slot = artefatto.getTipo().getSlotArtefatto();
			if (!(artefatto instanceof Arma) || (slot != TipoSlotArtefatto.MANO_PRINCIPALE && slot != TipoSlotArtefatto.ENTRAMBE_LE_MANI)) {
				continue;
			}
			if (((Arma) artefatto).daTipoDanno(tipoDanno) && puoCambiareArma(gruppo, personaggio, artefatto)) {
				trovate.add(artefatto);
			}
		}
		return trovate;
	}
}
