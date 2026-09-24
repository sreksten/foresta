package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.SlotArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;

import java.util.Collection;

/**
 * Regole a slot per l'equipaggiamento di un personaggio (vedi artefatti_e_incantamenti.md, §2 "Equipaggiamento"):
 * al massimo un artefatto per testa, corpo e ciascuna mano, accessori senza limite, pergamene mai,
 * arma in mano secondaria solo per Ladro/Ladra ed Elfo/Elfa, armi a due mani solo con entrambe le mani libere.
 * Il peso non è controllato qui, ma in {@link PersonaggioBase#puoEquipaggiare}.
 */
final class RegoleEquipaggiamento {

	/**
	 * Lo slot che l'artefatto occuperebbe, oppure il motivo per cui non si può prendere.
	 */
	static final class Esito {
		private final SlotArtefatto slot;
		private final MotivoRifiutoEquipaggiamento motivo;

		private Esito(SlotArtefatto slot, MotivoRifiutoEquipaggiamento motivo) {
			this.slot = slot;
			this.motivo = motivo;
		}

		static Esito slot(SlotArtefatto slot) {
			return new Esito(slot, null);
		}

		static Esito rifiuto(MotivoRifiutoEquipaggiamento motivo) {
			return new Esito(null, motivo);
		}

		SlotArtefatto getSlot() {
			return slot;
		}

		MotivoRifiutoEquipaggiamento getMotivo() {
			return motivo;
		}
	}

	private RegoleEquipaggiamento() {
	}

	static Esito valuta(ClassePersonaggio classe, int livelloPersonaggio,
						Collection<ArtefattoMD> equipaggiati, ArtefattoMD artefatto) {
		TipoArtefatto tipo = artefatto.getTipo();
		SlotArtefatto slotDelTipo = tipo.getSlotArtefatto();
		if (slotDelTipo == SlotArtefatto.NUCLEO) {
			return Esito.rifiuto(MotivoRifiutoEquipaggiamento.PERGAMENA);
		}
		if (artefatto.getLivello() > livelloPersonaggio) {
			return Esito.rifiuto(MotivoRifiutoEquipaggiamento.LIVELLO_TROPPO_ALTO);
		}
		switch (slotDelTipo) {
			case ACCESSORIO:
				return Esito.slot(SlotArtefatto.ACCESSORIO);
			case TESTA:
			case CORPO:
				return occupato(equipaggiati, slotDelTipo)
						? Esito.rifiuto(MotivoRifiutoEquipaggiamento.SLOT_OCCUPATO)
						: Esito.slot(slotDelTipo);
			case ENTRAMBE_LE_MANI:
				if (occupato(equipaggiati, SlotArtefatto.ENTRAMBE_LE_MANI)) {
					return Esito.rifiuto(MotivoRifiutoEquipaggiamento.ARMA_A_DUE_MANI_IMPUGNATA);
				}
				if (occupato(equipaggiati, SlotArtefatto.MANO_PRINCIPALE)
						|| occupato(equipaggiati, SlotArtefatto.MANO_SECONDARIA)) {
					return Esito.rifiuto(MotivoRifiutoEquipaggiamento.MANI_OCCUPATE);
				}
				return Esito.slot(SlotArtefatto.ENTRAMBE_LE_MANI);
			case MANO_PRINCIPALE:
				if (occupato(equipaggiati, SlotArtefatto.ENTRAMBE_LE_MANI)) {
					return Esito.rifiuto(MotivoRifiutoEquipaggiamento.ARMA_A_DUE_MANI_IMPUGNATA);
				}
				if (!occupato(equipaggiati, SlotArtefatto.MANO_PRINCIPALE)) {
					return Esito.slot(SlotArtefatto.MANO_PRINCIPALE);
				}
				// Mano principale occupata: una seconda arma può andare nella secondaria
				if (!isArmaDaManoSecondaria(tipo) || occupato(equipaggiati, SlotArtefatto.MANO_SECONDARIA)) {
					return Esito.rifiuto(MotivoRifiutoEquipaggiamento.SLOT_OCCUPATO);
				}
				if (!puoImpugnareDueArmi(classe)) {
					return Esito.rifiuto(MotivoRifiutoEquipaggiamento.SECONDA_ARMA_NON_CONSENTITA);
				}
				return Esito.slot(SlotArtefatto.MANO_SECONDARIA);
			case MANO_SECONDARIA:
				if (occupato(equipaggiati, SlotArtefatto.ENTRAMBE_LE_MANI)) {
					return Esito.rifiuto(MotivoRifiutoEquipaggiamento.ARMA_A_DUE_MANI_IMPUGNATA);
				}
				return occupato(equipaggiati, SlotArtefatto.MANO_SECONDARIA)
						? Esito.rifiuto(MotivoRifiutoEquipaggiamento.SLOT_OCCUPATO)
						: Esito.slot(SlotArtefatto.MANO_SECONDARIA);
			default:
				throw new IllegalArgumentException("Slot non gestito: " + slotDelTipo);
		}
	}

	/**
	 * Lo slot che un artefatto equipaggiato occupa. Se slotEquipaggiamento manca (artefatto aggiunto
	 * senza passare dalle regole) si ripiega sullo slot del suo tipo.
	 */
	static SlotArtefatto slotOccupato(ArtefattoMD artefatto) {
		SlotArtefatto slot = artefatto.getSlotEquipaggiamento();
		return slot != null ? slot : artefatto.getTipo().getSlotArtefatto();
	}

	private static boolean occupato(Collection<ArtefattoMD> equipaggiati, SlotArtefatto slot) {
		return equipaggiati.stream().anyMatch(a -> slotOccupato(a) == slot);
	}

	/**
	 * Lancia e bastone magico restano solo nella mano principale
	 */
	private static boolean isArmaDaManoSecondaria(TipoArtefatto tipo) {
		return tipo == TipoArtefatto.SPADA || tipo == TipoArtefatto.MAZZA || tipo == TipoArtefatto.ASCIA;
	}

	static boolean puoImpugnareDueArmi(ClassePersonaggio classe) {
		switch (classe) {
			case LADRO:
			case LADRA:
			case ELFO:
			case ELFA:
				return true;
			default:
				return false;
		}
	}
}
