package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import com.threeamigos.foresta.tipi.SupertipoArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoEquipaggiamento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoSlotArtefatto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;

/**
 * Regole a slot per l'equipaggiamento di un personaggio (vedi artefatti_e_incantamenti.md, §2 "Equipaggiamento"):
 * al massimo un artefatto per testa, corpo e ciascuna mano, accessori senza limite, pergamene mai,
 * arma in mano secondaria solo per Ladro/Ladra ed Elfo/Elfa, armi a due mani solo con entrambe le mani libere.
 * Ogni classe giocabile usa solo le armi, lo scudo e il libro adatti a lei ({@link #puoUsare}).
 * Peso e FORZA per l'armatura non sono controllati qui, ma in {@link PersonaggioBase#puoEquipaggiare}.
 */
public final class RegoleEquipaggiamento {

	// Armi, scudo e libro che ogni classe giocabile sa usare (vedi artefatti_e_incantamenti.md, §2,
	// "Equipaggiamento secondo la classe"). Elmo, armature, vesti e accessori li portano tutti; l'armatura
	// chiede una FORZA minima. Le classi che non sono qui (i mostri, l'Ombrafiamma) non hanno limiti.
	private static final Set<TipoArtefatto> GUERRIERO = EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.SPADONE,
			TipoArtefatto.MAZZA, TipoArtefatto.ASCIA, TipoArtefatto.LANCIA, TipoArtefatto.SCUDO);
	private static final Set<TipoArtefatto> LADRO = EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.MAZZA, TipoArtefatto.ASCIA);
	// L'elfo sa usare anche il libro magico, ma ne ha metà del bonus (vedi Costanti.LIBRO_MAGICO_FATTORE_ELFO)
	private static final Set<TipoArtefatto> ELFO = EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.MAZZA, TipoArtefatto.ASCIA,
			TipoArtefatto.LANCIA, TipoArtefatto.LIBRO_MAGICO);
	private static final Set<TipoArtefatto> BARDO = EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.SCUDO);
	private static final Set<TipoArtefatto> MAGO = EnumSet.of(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.LIBRO_MAGICO);
	private static final Set<TipoArtefatto> SACERDOTE = EnumSet.of(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.MAZZA, TipoArtefatto.LIBRO_MAGICO);

	/**
	 * Lo slot che l'artefatto occuperebbe, oppure il motivo per cui non si può prendere.
	 */
	public static final class EsitoControlloRichiestaEquipaggiamento {
		private final TipoSlotArtefatto slot;
		private final TipoMotivoRifiutoEquipaggiamento motivo;
		private final String descrizione;
		private final String fumetto;

		private EsitoControlloRichiestaEquipaggiamento(Personaggio personaggio, TipoSlotArtefatto slot,
													   TipoMotivoRifiutoEquipaggiamento motivo, ArtefattoMD artefattoMD) {
			this.slot = slot;
			this.motivo = motivo;
			if (motivo != null) {
				String nomePersonaggio = personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
						Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
				if (artefattoMD != null) {
					String nomeArtefatto = Artefatto.di(artefattoMD).getNome();
					this.descrizione = motivo.formattaSpiegazione(nomePersonaggio, nomeArtefatto);
					this.fumetto = motivo.formattaFumetto(nomeArtefatto);
				} else {
					this.descrizione = motivo.formattaSpiegazione(nomePersonaggio);
					this.fumetto = motivo.formattaFumetto();
				}
			} else {
				this.descrizione = null;
				this.fumetto = null;
			}
		}

		static EsitoControlloRichiestaEquipaggiamento slot(Personaggio personaggio, TipoSlotArtefatto slot) {
			return new EsitoControlloRichiestaEquipaggiamento(personaggio, slot, null, null);
		}

		static EsitoControlloRichiestaEquipaggiamento rifiuto(Personaggio personaggio, TipoMotivoRifiutoEquipaggiamento motivo) {
			return new EsitoControlloRichiestaEquipaggiamento(personaggio, null, motivo, null);
		}

		static EsitoControlloRichiestaEquipaggiamento rifiuto(Personaggio personaggio, TipoMotivoRifiutoEquipaggiamento motivo, ArtefattoMD artefatto) {
			return new EsitoControlloRichiestaEquipaggiamento(personaggio, null, motivo, artefatto);
		}

		public TipoSlotArtefatto getSlot() {
			return slot;
		}

		public TipoMotivoRifiutoEquipaggiamento getMotivo() {
			return motivo;
		}

		public String getDescrizione() {
			return descrizione;
		}

		public String getFumetto() {
			return fumetto;
		}
	}

	private RegoleEquipaggiamento() {
	}

	public static EsitoControlloRichiestaEquipaggiamento valuta(Personaggio personaggio, ArtefattoMD artefatto) {
		TipoPersonaggio classe = personaggio.getClasse();
		int livelloPersonaggio = personaggio.getLivello();
		Collection<ArtefattoMD> equipaggiati = personaggio.getModelloDati().getArtefatti();

		TipoArtefatto tipo = artefatto.getTipo();

		if (artefatto.getTipo() == TipoArtefatto.ARMATURA && personaggio.getForza() < Costanti.ARMATURA_FORZA_MINIMA) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.FORZA_INSUFFICIENTE, artefatto);
		}

		if (artefatto.getPeso() > personaggio.getCaricoMassimo() - personaggio.getCarico()) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.TROPPO_CARICO);
		}

		TipoSlotArtefatto slotDelTipo = tipo.getSlotArtefatto();
		if (slotDelTipo == TipoSlotArtefatto.NUCLEO) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.PERGAMENA);
		}
		if (!puoUsare(classe, tipo)) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE);
		}
		if (artefatto.getLivello() > livelloPersonaggio) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.LIVELLO_TROPPO_ALTO, artefatto);
		}

		if (slotDelTipo == TipoSlotArtefatto.ACCESSORIO) {
			return EsitoControlloRichiestaEquipaggiamento.slot(personaggio, TipoSlotArtefatto.ACCESSORIO);

		} else if (slotDelTipo == TipoSlotArtefatto.TESTA || slotDelTipo == TipoSlotArtefatto.VOLTO
				|| slotDelTipo == TipoSlotArtefatto.CORPO || slotDelTipo == TipoSlotArtefatto.GAMBE) {
			Optional<ArtefattoMD> occupante = occupato(equipaggiati, slotDelTipo);
			if (occupante.isPresent()) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.SLOT_OCCUPATO, occupante.get());
			} else {
				return EsitoControlloRichiestaEquipaggiamento.slot(personaggio, slotDelTipo);
			}

		} else if (slotDelTipo == TipoSlotArtefatto.ENTRAMBE_LE_MANI) {
			Optional<ArtefattoMD> occupanteADueMani = occupato(equipaggiati, TipoSlotArtefatto.ENTRAMBE_LE_MANI);
			if (occupanteADueMani.isPresent()) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.ARMA_A_DUE_MANI_IMPUGNATA, occupanteADueMani.get());
			}
			Optional<ArtefattoMD> occupanteInManoPrincipale = occupato(equipaggiati, TipoSlotArtefatto.MANO_PRINCIPALE);
			if (occupanteInManoPrincipale.isPresent()) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.MANI_OCCUPATE_PER_ARMA_A_DUE_MANI, occupanteInManoPrincipale.get());
			}
			Optional<ArtefattoMD> occupanteInManoSecondaria = occupato(equipaggiati, TipoSlotArtefatto.MANO_SECONDARIA);
			if (occupanteInManoSecondaria.isPresent()) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.MANI_OCCUPATE_PER_ARMA_A_DUE_MANI, occupanteInManoSecondaria.get());
			}
			return EsitoControlloRichiestaEquipaggiamento.slot(personaggio, TipoSlotArtefatto.ENTRAMBE_LE_MANI);

		} else if (slotDelTipo == TipoSlotArtefatto.MANO_PRINCIPALE) {
			Optional<ArtefattoMD> armaADueManiOccupante = occupato(equipaggiati, TipoSlotArtefatto.ENTRAMBE_LE_MANI);
			if (armaADueManiOccupante.isPresent()) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.ARMA_A_DUE_MANI_IMPUGNATA, armaADueManiOccupante.get());
			}
			Optional<ArtefattoMD> armaInManoPrincipale = occupato(equipaggiati, TipoSlotArtefatto.MANO_PRINCIPALE);
			if (!armaInManoPrincipale.isPresent()) {
				return EsitoControlloRichiestaEquipaggiamento.slot(personaggio, TipoSlotArtefatto.MANO_PRINCIPALE);
			}
			// Mano principale occupata: una seconda arma può andare nella secondaria
			if (!isArmaDaManoSecondaria(tipo)) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.ARMA_NON_DA_MANO_SECONDARIA, artefatto);
			}
			Optional<ArtefattoMD> armaInManoSecondaria = occupato(equipaggiati, TipoSlotArtefatto.MANO_SECONDARIA);
			if (armaInManoSecondaria.isPresent()) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.MANO_SECONDARIA_OCCUPATA, armaInManoSecondaria.get());
			}
			if (!puoImpugnareDueArmi(classe)) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.SECONDA_ARMA_NON_CONSENTITA);
			}
			return EsitoControlloRichiestaEquipaggiamento.slot(personaggio, TipoSlotArtefatto.MANO_SECONDARIA);

		} else if (slotDelTipo == TipoSlotArtefatto.MANO_SECONDARIA) {
			Optional<ArtefattoMD> occupanteADueMani = occupato(equipaggiati, TipoSlotArtefatto.ENTRAMBE_LE_MANI);
			if (occupanteADueMani.isPresent()) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.ARMA_A_DUE_MANI_IMPUGNATA);
			}
			Optional<ArtefattoMD> occupanteManoSecondaria = occupato(equipaggiati, TipoSlotArtefatto.MANO_SECONDARIA);
			if (occupanteManoSecondaria.isPresent()) {
				return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.SLOT_OCCUPATO, occupanteManoSecondaria.get());
			} else {
				return EsitoControlloRichiestaEquipaggiamento.slot(personaggio, TipoSlotArtefatto.MANO_SECONDARIA);
			}

		} else {
			throw new IllegalArgumentException("Slot non gestito: " + slotDelTipo);
		}
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
			Optional<ArtefattoMD> occupante = occupato(equipaggiati, TipoSlotArtefatto.ENTRAMBE_LE_MANI);
			if (!occupante.isPresent()) {
				occupante = occupato(equipaggiati, TipoSlotArtefatto.MANO_PRINCIPALE);
			}
			Optional<ArtefattoMD> secondaria = occupato(equipaggiati, TipoSlotArtefatto.MANO_SECONDARIA);
			// Con la doppia arma esce la più debole delle due (a parità la secondaria), purché la nuova possa stare nella secondaria
			if (occupante.isPresent() && secondaria.isPresent() && occupante.get().getTipo().getSupertipo() == SupertipoArtefatto.ARMA
					&& secondaria.get().getTipo().getSupertipo() == SupertipoArtefatto.ARMA
					&& isArmaDaManoSecondaria(nuova.getTipo()) && puoImpugnareDueArmi(personaggio.getClasse())
					&& dannoAtteso(personaggio, secondaria.get()) <= dannoAtteso(personaggio, occupante.get())) {
				occupante = secondaria;
			}
			occupante.ifPresent(uscenti::add);
		} else {
			for (TipoSlotArtefatto slot : new TipoSlotArtefatto[] {TipoSlotArtefatto.ENTRAMBE_LE_MANI,
					TipoSlotArtefatto.MANO_PRINCIPALE, TipoSlotArtefatto.MANO_SECONDARIA}) {
				occupato(equipaggiati, slot).ifPresent(uscenti::add);
			}
		}
		for (ArtefattoMD uscente : uscenti) {
			if (uscente.getTipo().getSupertipo() != SupertipoArtefatto.ARMA) {
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
		Collection<ArtefattoMD> equipaggiati = personaggio.getModelloDati().getArtefatti();
		Optional<List<ArtefattoMD>> uscenti = chiEsce(personaggio, nuova);
		if (!uscenti.isPresent()) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.MANI_OCCUPATE_PER_ARMA_A_DUE_MANI);
		}
		List<ArtefattoMD> prima = new ArrayList<>(equipaggiati);
		try {
			equipaggiati.removeAll(uscenti.get());
			return valuta(personaggio, nuova);
		} finally {
			equipaggiati.clear();
			equipaggiati.addAll(prima);
		}
	}

	/**
	 * Gli artefatti che uscirebbero dalle mani del personaggio se prendesse quell'arma (vuoto se nessuno, o se il cambio non si può fare).
	 */
	public static List<ArtefattoMD> uscentiPerCambioArma(Personaggio personaggio, ArtefattoMD nuova) {
		return chiEsce(personaggio, nuova).orElse(new ArrayList<>());
	}

	/**
	 * Lo slot che un artefatto equipaggiato occupa. Se slotEquipaggiamento manca (artefatto aggiunto
	 * senza passare dalle regole) si ripiega sullo slot del suo tipo.
	 */
	public static TipoSlotArtefatto slotOccupato(ArtefattoMD artefatto) {
		TipoSlotArtefatto slot = artefatto.getSlotEquipaggiamento();
		return slot != null ? slot : artefatto.getTipo().getSlotArtefatto();
	}

	private static double dannoAtteso(Personaggio personaggio, ArtefattoMD arma) {
		return CalcolatoreCombattimento.dannoAttesoDellArma(personaggio, (Arma) Artefatto.di(arma));
	}

	private static Optional<ArtefattoMD> occupato(Collection<ArtefattoMD> equipaggiati, TipoSlotArtefatto slot) {
		return equipaggiati.stream().filter(a -> slotOccupato(a) == slot).findFirst();
	}

	/**
	 * Lancia e bastone magico restano solo nella mano principale
	 */
	private static boolean isArmaDaManoSecondaria(TipoArtefatto tipo) {
		return tipo == TipoArtefatto.SPADA || tipo == TipoArtefatto.MAZZA || tipo == TipoArtefatto.ASCIA;
	}

	/**
	 * @return true se la classe sa usare quel tipo di artefatto. La tabella vale per armi, scudo e libro:
	 * tutto il resto lo possono portare tutti.
	 */
	static boolean puoUsare(TipoPersonaggio classe, TipoArtefatto tipo) {
		boolean daTabella = tipo.getSupertipo() == SupertipoArtefatto.ARMA || tipo == TipoArtefatto.SCUDO
				|| tipo == TipoArtefatto.LIBRO_MAGICO;
		if (!daTabella) {
			return true;
		}
		Set<TipoArtefatto> ammessi = ammessi(classe);
		return ammessi == null || ammessi.contains(tipo);
	}

	private static Set<TipoArtefatto> ammessi(TipoPersonaggio classe) {
		switch (classe) {
			case GUERRIERO:
			case GUERRIERA:
			case CAPITANO_DELLE_GUARDIE:
				return GUERRIERO;
			case LADRO:
			case LADRA:
				return LADRO;
			case ELFO:
			case ELFA:
				return ELFO;
			case BARDO:
			case CANTASTORIE:
				return BARDO;
			case MAGO:
			case MAGA:
				return MAGO;
			case SACERDOTE:
			case SACERDOTESSA:
				return SACERDOTE;
			default:
				return null;
		}
	}

	static boolean puoImpugnareDueArmi(TipoPersonaggio classe) {
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
