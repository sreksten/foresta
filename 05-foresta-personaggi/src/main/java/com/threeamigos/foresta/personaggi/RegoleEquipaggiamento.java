package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoSlotArtefatto;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoEquipaggiamento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.*;

/**
 * Regole a slot per l'equipaggiamento di un personaggio (vedi artefatti_e_incantamenti.md, §2 "Equipaggiamento"):
 * al massimo un artefatto per testa, corpo e ciascuna mano, accessori senza limite, pergamene mai,
 * arma in mano secondaria solo per Ladro/Ladra ed Elfo/Elfa, armi a due mani solo con entrambe le mani libere.
 * Ogni classe giocabile usa solo le armi, lo scudo, il libro e l'armatura adatti a lei ({@link #puoUsare}): il Ladro, il Mago e il Sacerdote, per esempio, non portano armature, e nemmeno l'elmo.
 * Qui si controllano anche la FORZA minima per l'armatura, il peso e il livello dell'artefatto (vedi {@link #valuta}).
 * {@link PersonaggioBase#puoEquipaggiare} e CambioDArma (nel combattimento) chiedono qui; le classi che non hanno una tabella (i mostri,
 * l'Ombrafiamma, i non combattenti) usano tutto.
 */
public final class RegoleEquipaggiamento {

	// Armi, scudo, libro e armatura che ogni classe giocabile usa (vedi artefatti_e_incantamenti.md, §2,
	// “Equipaggiamento secondo la classe”). Vesti e accessori li portano tutti, e l'elmo tutti tranne il Ladro, il Mago e il Sacerdote. L'armatura la portano tutti
	// tranne il Ladro, che non la usa mai (agisce con la destrezza), il Mago (la magia non si fa in armatura) e il
	// Sacerdote (un sacerdote in armatura è il Chierico); chi
	// può portarla chiede una FORZA minima.
	// Le classi che non sono qui (i mostri, l'Ombrafiamma, i non combattenti) non hanno limiti (vedi ammessi).
	private static final Set<TipoArtefatto> GUERRIERO = EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.SPADONE, TipoArtefatto.FALCE,
			TipoArtefatto.MAZZA, TipoArtefatto.ASCIA, TipoArtefatto.LANCIA, TipoArtefatto.SCUDO, TipoArtefatto.ARMATURA);
	private static final Set<TipoArtefatto> LADRO = EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.MAZZA, TipoArtefatto.ASCIA);
	// L'elfo sa usare anche il libro magico, ma ne ha metà del bonus (vedi CostantiPersonaggi.LIBRO_MAGICO_FATTORE_ELFO)
	private static final Set<TipoArtefatto> ELFO = EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.MAZZA, TipoArtefatto.ASCIA,
			TipoArtefatto.LANCIA, TipoArtefatto.LIBRO_MAGICO, TipoArtefatto.ARMATURA);
	private static final Set<TipoArtefatto> BARDO = EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.SCUDO, TipoArtefatto.ARMATURA);
	private static final Set<TipoArtefatto> MAGO = EnumSet.of(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.LIBRO_MAGICO);
	private static final Set<TipoArtefatto> SACERDOTE = EnumSet.of(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.MAZZA, TipoArtefatto.LIBRO_MAGICO);
	// Il chierico è il sacerdote che combatte in prima linea: mazza (e spada), scudo e armatura, ma né bastone né libro
	private static final Set<TipoArtefatto> CHIERICO = EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.MAZZA, TipoArtefatto.SCUDO,
			TipoArtefatto.ARMATURA);
	// Il druido è l'incantatore della natura: bastone, lancia e libro, niente metallo (né scudo né armatura); non ci sono archi
	private static final Set<TipoArtefatto> DRUIDO = EnumSet.of(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.LANCIA, TipoArtefatto.LIBRO_MAGICO);

	/**
	 * Lo slot che l'artefatto occuperebbe, oppure il motivo per cui non si può prendere: uno dei due è sempre null. Con il motivo ci sono
	 * anche le due frasi già pronte: la spiegazione in terza persona (con il nome del personaggio davanti) e la frase del fumetto.
	 */
	public static final class EsitoControlloRichiestaEquipaggiamento {
		private final TipoSlotArtefatto slot;
		private final TipoMotivoRifiutoEquipaggiamento motivo;
		private final String descrizione;
		private final String fumetto;

		// artefattoMD è l'artefatto da nominare nelle frasi (quello nuovo o quello che occupa già il posto, secondo il motivo); null per i
		// motivi le cui frasi non nominano nessun artefatto
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

		public static EsitoControlloRichiestaEquipaggiamento rifiuto(Personaggio personaggio, TipoMotivoRifiutoEquipaggiamento motivo) {
			return new EsitoControlloRichiestaEquipaggiamento(personaggio, null, motivo, null);
		}

		static EsitoControlloRichiestaEquipaggiamento rifiuto(Personaggio personaggio, TipoMotivoRifiutoEquipaggiamento motivo, ArtefattoMD artefatto) {
			return new EsitoControlloRichiestaEquipaggiamento(personaggio, null, motivo, artefatto);
		}

		/**
		 * Lo slot in cui andrebbe l'artefatto; null se è un rifiuto.
		 */
		public TipoSlotArtefatto getSlot() {
			return slot;
		}

		/**
		 * Il motivo del rifiuto; null se l'artefatto si può prendere.
		 */
		public TipoMotivoRifiutoEquipaggiamento getMotivo() {
			return motivo;
		}

		/**
		 * La spiegazione del rifiuto in terza persona, con il nome del personaggio (“Il ladro non usa questo tipo di oggetti.”); null se
		 * non è un rifiuto.
		 */
		public String getDescrizione() {
			return descrizione;
		}

		/**
		 * La frase del rifiuto che il personaggio dice nel fumetto, in prima persona; null se non è un rifiuto.
		 */
		public String getFumetto() {
			return fumetto;
		}
	}

	private RegoleEquipaggiamento() {
	}

	/**
	 * Valuta se il personaggio può usare l'artefatto, nell'ordine: la classe usa quel genere di oggetti, la forza basta per un'armatura, il
	 * peso è sostenibile, non è una pergamena o un ingrediente (slot NUCLEO), il livello è sufficiente, lo slot è libero (con le regole di
	 * mani e seconda arma). Il primo controllo che fallisce dà il motivo. Non cambia niente: è
	 * chi chiama a equipaggiare, se l'esito lo permette.
	 *
	 * @return l'esito del controllo, con il motivo del rifiuto se non può
	 */
	public static EsitoControlloRichiestaEquipaggiamento valuta(Personaggio personaggio, ArtefattoMD artefatto) {
		TipoPersonaggio classe = personaggio.getClasse();
		int livelloPersonaggio = personaggio.getLivello();
		Collection<ArtefattoMD> equipaggiati = personaggio.getModelloDati().getArtefatti();

		TipoArtefatto tipo = artefatto.getTipo();

		// La classe prima di tutto: chi non usa quel genere di oggetti non lo prende, e non importa quanto sia forte o
		// quanto carico porti (diversamente il fumetto direbbe "non ho la forza" a un ladro che l'armatura non la usa mai)
		if (!puoUsare(classe, tipo)) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE);
		}

		if (artefatto.getTipo() == TipoArtefatto.ARMATURA && personaggio.get(TipoAttributo.FORZA) < CostantiPersonaggi.ARMATURA_FORZA_MINIMA) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.FORZA_INSUFFICIENTE, artefatto);
		}

		if (artefatto.getPeso() > personaggio.getCaricoMassimo() - personaggio.getCarico()) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.TROPPO_CARICO);
		}

		TipoSlotArtefatto slotDelTipo = tipo.getSlotArtefatto();
		if (slotDelTipo == TipoSlotArtefatto.NUCLEO) {
			return EsitoControlloRichiestaEquipaggiamento.rifiuto(personaggio, TipoMotivoRifiutoEquipaggiamento.PERGAMENA);
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

		} else {
			// MANO_SECONDARIA: gli altri slot sono già stati gestiti sopra
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

		}
	}

	/**
	 * Lo slot che un artefatto equipaggiato occupa: di solito quello del suo tipo, ma una seconda arma nella mano secondaria ha
	 * MANO_SECONDARIA anche se il suo tipo dice MANO_PRINCIPALE. Se slotEquipaggiamento manca (artefatto aggiunto senza passare dalle
	 * regole) si ripiega sullo slot del suo tipo.
	 */
	public static TipoSlotArtefatto slotOccupato(ArtefattoMD artefatto) {
		TipoSlotArtefatto slot = artefatto.getSlotEquipaggiamento();
		return slot != null ? slot : artefatto.getTipo().getSlotArtefatto();
	}

	/**
	 * Il primo artefatto, fra quelli equipaggiati, che occupa quello slot; vuoto se lo slot è libero.
	 */
	public static Optional<ArtefattoMD> occupato(Collection<ArtefattoMD> equipaggiati, TipoSlotArtefatto slot) {
		return equipaggiati.stream().filter(a -> slotOccupato(a) == slot).findFirst();
	}

	/**
	 * Se un'arma a una mano si può portare anche nella mano secondaria, come seconda arma: solo spada, mazza e ascia (lancia e bastone
	 * magico restano nella principale). Chi può farlo lo dice {@link #puoImpugnareDueArmi}.
	 */
	public static boolean isArmaDaManoSecondaria(TipoArtefatto tipo) {
		return tipo == TipoArtefatto.SPADA || tipo == TipoArtefatto.MAZZA || tipo == TipoArtefatto.ASCIA;
	}

	/**
	 * @return true se la classe sa usare quel tipo di artefatto. La tabella vale per armi, scudo, libro e armatura; l'elmo lo portano tutti tranne
	 * il Ladro, il Mago e il Sacerdote (le forme femminili comprese); il resto lo possono portare tutti.
	 */
	public static boolean puoUsare(TipoPersonaggio classe, TipoArtefatto tipo) {
		if (tipo == TipoArtefatto.ELMO) {
			return !isSenzaElmo(classe);
		}
		boolean daTabella = tipo.getCategoria() == CategoriaArtefatto.ARMA || tipo == TipoArtefatto.SCUDO
				|| tipo == TipoArtefatto.LIBRO_MAGICO || tipo == TipoArtefatto.ARMATURA;
		if (!daTabella) {
			return true;
		}
		Set<TipoArtefatto> ammessi = ammessi(classe);
		return ammessi == null || ammessi.contains(tipo);
	}

	/**
	 * Il Ladro, il Mago e il Sacerdote non portano l'elmo: non sta bene in testa a chi vive di destrezza, di magia e di preghiera.
	 */
	private static boolean isSenzaElmo(TipoPersonaggio classe) {
		// getRuolo mette insieme le forme maschile e femminile: la Ladra come il Ladro
		TipoPersonaggio ruolo = classe.getRuolo();
		return ruolo == TipoPersonaggio.LADRO || ruolo == TipoPersonaggio.MAGO || ruolo == TipoPersonaggio.SACERDOTE;
	}

	// Le armi, lo scudo, il libro e l'armatura che la classe sa usare; null per le classi senza limiti (i mostri, l'Ombrafiamma, i non
	// combattenti). Si guarda il ruolo (TipoPersonaggio.getRuolo): la forma femminile usa quel che usa la maschile
	private static Set<TipoArtefatto> ammessi(TipoPersonaggio classe) {
		switch (classe.getRuolo()) {
			case GUERRIERO:
			case CAPITANO_DELLE_GUARDIE:
			case VICHINGO:
				return GUERRIERO;
			case LADRO:
				return LADRO;
			case ELFO:
				return ELFO;
			case BARDO:
				return BARDO;
			case MAGO:
				return MAGO;
			case SACERDOTE:
				return SACERDOTE;
			case CHIERICO:
				return CHIERICO;
			case DRUIDO:
				return DRUIDO;
			default:
				return null;
		}
	}

	/**
	 * Se la classe combatte con due armi, una per mano: solo il Ladro e l'Elfo (e le loro forme femminili, che hanno lo stesso ruolo).
	 */
	public static boolean puoImpugnareDueArmi(TipoPersonaggio classe) {
		switch (classe.getRuolo()) {
			case LADRO:
			case ELFO:
				return true;
			default:
				return false;
		}
	}
}
