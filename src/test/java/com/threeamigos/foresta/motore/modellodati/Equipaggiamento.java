package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import com.threeamigos.foresta.oggetti.GradoIncantamento;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.MotivoRifiutoEquipaggiamento;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Un equipaggiamento da dare al PG nel simulatore (vedi CombatSimulatorMatrix e piano_montecarlo_matrix.md, §11):
 * armi, scudo, elmo, armatura, anche incantati. I pezzi si costruiscono al livello del PG con i valori medi di
 * GeneratoreArtefattiTabelle, senza la parte casuale, così i confronti fra equipaggiamenti sono puliti.
 * Passano dalle regole vere (Personaggio.puoEquipaggiare): una classe che non può portare un equipaggiamento
 * (es. il Guerriero con due spade, o chi è troppo carico) lo rifiuta.
 */
public final class Equipaggiamento {

	// Pesi come in GeneratoreArtefattiTabelle
	private static final Map<TipoArtefatto, Double> PESI = new EnumMap<>(TipoArtefatto.class);

	static {
		PESI.put(TipoArtefatto.SPADA, 1.0);
		PESI.put(TipoArtefatto.SPADONE, 3.0);
		PESI.put(TipoArtefatto.MAZZA, 2.0);
		PESI.put(TipoArtefatto.ASCIA, 2.0);
		PESI.put(TipoArtefatto.LANCIA, 2.0);
		PESI.put(TipoArtefatto.BASTONE_MAGICO, 1.0);
		PESI.put(TipoArtefatto.LIBRO_MAGICO, 1.0);
		PESI.put(TipoArtefatto.SCUDO, 2.0);
		PESI.put(TipoArtefatto.ELMO, 1.0);
		PESI.put(TipoArtefatto.ARMATURA, 3.0);
		PESI.put(TipoArtefatto.VESTE, 1.0);
	}

	/**
	 * Nessun artefatto: il PG combatte con l'arma naturale, come nella prima versione del simulatore
	 */
	public static final Equipaggiamento NESSUNO = di("NESSUNO");
	public static final Equipaggiamento SPADA = di("SPADA", Pezzo.di(TipoArtefatto.SPADA));
	public static final Equipaggiamento SPADA_E_SCUDO = di("SPADA_E_SCUDO",
			Pezzo.di(TipoArtefatto.SPADA), Pezzo.di(TipoArtefatto.SCUDO));
	/**
	 * Lo scudo raro dà più RESISTENZA_MAGICA di quello comune
	 */
	public static final Equipaggiamento SPADA_E_SCUDO_RARO = di("SPADA_E_SCUDO_RARO",
			Pezzo.di(TipoArtefatto.SPADA), Pezzo.di(TipoArtefatto.SCUDO).raro());
	/**
	 * Solo Ladro/Ladra ed Elfo/Elfa
	 */
	public static final Equipaggiamento DUE_SPADE = di("DUE_SPADE",
			Pezzo.di(TipoArtefatto.SPADA), Pezzo.di(TipoArtefatto.SPADA));
	public static final Equipaggiamento SPADONE = di("SPADONE", Pezzo.di(TipoArtefatto.SPADONE));
	public static final Equipaggiamento SPADA_DI_FUOCO = di("SPADA_DI_FUOCO",
			Pezzo.di(TipoArtefatto.SPADA).incantato(TipoDanno.FUOCO));
	public static final Equipaggiamento CORAZZATO = di("CORAZZATO",
			Pezzo.di(TipoArtefatto.SPADA), Pezzo.di(TipoArtefatto.SCUDO),
			Pezzo.di(TipoArtefatto.ELMO), Pezzo.di(TipoArtefatto.ARMATURA));
	/**
	 * Come CORAZZATO, con l'armatura incantata contro il veleno (il morso di Drago, Viverna e Chimera Drago)
	 */
	public static final Equipaggiamento CORAZZATO_CONTRO_VELENO = di("CORAZZATO_CONTRO_VELENO",
			Pezzo.di(TipoArtefatto.SPADA), Pezzo.di(TipoArtefatto.SCUDO),
			Pezzo.di(TipoArtefatto.ELMO), Pezzo.di(TipoArtefatto.ARMATURA).incantato(TipoDanno.VELENO));

	// Le dotazioni tipiche delle classi (vedi artefatti_e_incantamenti.md, §2, "Equipaggiamento secondo la
	// classe"): quel che ogni classe userebbe davvero, con elmo e armatura o veste
	public static final Equipaggiamento CAVALIERE = di("CAVALIERE",
			Pezzo.di(TipoArtefatto.SPADA), Pezzo.di(TipoArtefatto.SCUDO),
			Pezzo.di(TipoArtefatto.ELMO), Pezzo.di(TipoArtefatto.ARMATURA));
	public static final Equipaggiamento SPADONE_E_ARMATURA = di("SPADONE_E_ARMATURA",
			Pezzo.di(TipoArtefatto.SPADONE), Pezzo.di(TipoArtefatto.ELMO), Pezzo.di(TipoArtefatto.ARMATURA));
	public static final Equipaggiamento DUE_SPADE_E_VESTE = di("DUE_SPADE_E_VESTE",
			Pezzo.di(TipoArtefatto.SPADA), Pezzo.di(TipoArtefatto.SPADA),
			Pezzo.di(TipoArtefatto.ELMO), Pezzo.di(TipoArtefatto.VESTE));
	public static final Equipaggiamento LANCIA_E_VESTE = di("LANCIA_E_VESTE",
			Pezzo.di(TipoArtefatto.LANCIA), Pezzo.di(TipoArtefatto.ELMO), Pezzo.di(TipoArtefatto.VESTE));
	public static final Equipaggiamento SPADA_SCUDO_E_VESTE = di("SPADA_SCUDO_E_VESTE",
			Pezzo.di(TipoArtefatto.SPADA), Pezzo.di(TipoArtefatto.SCUDO),
			Pezzo.di(TipoArtefatto.ELMO), Pezzo.di(TipoArtefatto.VESTE));
	public static final Equipaggiamento BASTONE_LIBRO_E_VESTE = di("BASTONE_LIBRO_E_VESTE",
			Pezzo.di(TipoArtefatto.BASTONE_MAGICO), Pezzo.di(TipoArtefatto.LIBRO_MAGICO), Pezzo.di(TipoArtefatto.VESTE));

	/**
	 * Le dotazioni tipiche di una classe giocabile (le versioni femminili come le maschili), o NESSUNO
	 * per le altre
	 */
	public static List<Equipaggiamento> tipiciPer(ClassePersonaggio classe) {
		switch (classe) {
			case GUERRIERO:
			case GUERRIERA:
				return Arrays.asList(CAVALIERE, SPADONE_E_ARMATURA);
			case LADRO:
			case LADRA:
				return Collections.singletonList(DUE_SPADE_E_VESTE);
			case ELFO:
			case ELFA:
				return Arrays.asList(DUE_SPADE_E_VESTE, LANCIA_E_VESTE);
			case BARDO:
			case CANTASTORIE:
				return Collections.singletonList(SPADA_SCUDO_E_VESTE);
			case MAGO:
			case MAGA:
				return Collections.singletonList(BASTONE_LIBRO_E_VESTE);
			default:
				return Collections.singletonList(NESSUNO);
		}
	}

	/**
	 * Tutti gli equipaggiamenti già pronti, nell'ordine in cui compaiono nei report
	 */
	public static final List<Equipaggiamento> TUTTI = Collections.unmodifiableList(Arrays.asList(
			NESSUNO, SPADA, SPADA_E_SCUDO, SPADA_E_SCUDO_RARO, DUE_SPADE, SPADONE, SPADA_DI_FUOCO, CORAZZATO, CORAZZATO_CONTRO_VELENO));

	private final String nome;
	private final List<Pezzo> pezzi;

	private Equipaggiamento(String nome, List<Pezzo> pezzi) {
		this.nome = nome;
		this.pezzi = pezzi;
	}

	/**
	 * Un equipaggiamento nuovo, per le prove che i preset non coprono
	 */
	public static Equipaggiamento di(String nome, Pezzo... pezzi) {
		return new Equipaggiamento(nome, Collections.unmodifiableList(Arrays.asList(pezzi)));
	}

	public String getNome() {
		return nome;
	}

	public List<Pezzo> getPezzi() {
		return pezzi;
	}

	/**
	 * Costruisce i pezzi al livello del PG e glieli fa prendere uno alla volta, con le regole vere.
	 *
	 * @return il motivo per cui il PG non può portare l'equipaggiamento (e allora non ne prende nessun pezzo),
	 * oppure vuoto se l'ha preso tutto
	 */
	public Optional<String> equipaggia(Personaggio pg) {
		List<Artefatto> presi = new ArrayList<>();
		for (Pezzo pezzo : pezzi) {
			Artefatto artefatto = pezzo.costruisci(pg.getLivello());
			Optional<MotivoRifiutoEquipaggiamento> motivo = pg.puoEquipaggiare(artefatto);
			if (motivo.isPresent()) {
				presi.forEach(pg::removeArtefatto);
				return Optional.of(pezzo.getTipo() + ": " + motivo.get());
			}
			pg.addArtefatto(artefatto);
			presi.add(artefatto);
		}
		return Optional.empty();
	}

	/**
	 * @return il motivo per cui la classe, a quel livello, non può portare l'equipaggiamento, o vuoto se può
	 */
	public Optional<String> motivoRifiuto(ClassePersonaggio classe, int livello) {
		return equipaggia(classe.getIstanza(livello));
	}

	@Override
	public String toString() {
		return nome;
	}

	/**
	 * Un pezzo dell'equipaggiamento: un tipo di artefatto, facoltativamente con un incantamento del grado
	 * adatto al livello (danno aggiuntivo sulle armi, resistenza su elmo, scudo e armatura) e raro.
	 */
	public static final class Pezzo {

		private final TipoArtefatto tipo;
		private final TipoDanno incantamento;
		private final RaritaArtefatto rarita;

		private Pezzo(TipoArtefatto tipo, TipoDanno incantamento, RaritaArtefatto rarita) {
			this.tipo = tipo;
			this.incantamento = incantamento;
			this.rarita = rarita;
		}

		public static Pezzo di(TipoArtefatto tipo) {
			if (!PESI.containsKey(tipo)) {
				throw new IllegalArgumentException("Nel simulatore non si equipaggia " + tipo);
			}
			return new Pezzo(tipo, null, RaritaArtefatto.COMUNE);
		}

		public Pezzo incantato(TipoDanno tipoDanno) {
			return new Pezzo(tipo, tipoDanno, rarita);
		}

		public Pezzo raro() {
			return new Pezzo(tipo, incantamento, RaritaArtefatto.RARO);
		}

		public TipoArtefatto getTipo() {
			return tipo;
		}

		/**
		 * I valori medi di GeneratoreArtefattiTabelle: armi con danno GeneratoreArtefatti.danniMediArma (lo spadone +50%,
		 * il bastone metà e +5% di MAGIA per livello), pezzi difensivi con +5% di PARATA per livello
		 * (la veste di RESISTENZA_MAGICA), libro con +5% di MAGIA per livello.
		 */
		Artefatto costruisci(int livello) {
			ArtefattoMD md = new ArtefattoMD();
			md.setTipo(tipo);
			md.setNome("l'artefatto del simulatore");
			md.setDescrizione(tipo.name());
			md.setLivello(livello);
			md.setPeso(PESI.get(tipo));
			md.setRarita(rarita);
			switch (tipo.getSupertipo()) {
				case ARMA:
					int danni = GeneratoreArtefatti.danniMediArma(livello);
					if (tipo == TipoArtefatto.SPADONE) {
						danni = (int) Math.round(danni * Costanti.ARTEFATTO_MOLTIPLICATORE_DUE_MANI);
					} else if (tipo == TipoArtefatto.BASTONE_MAGICO) {
						danni = danni / 2;
						md.addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 5.0 * livello, "");
					}
					md.setDanni(danni);
					break;
				case SCUDO:
				case ELMO:
				case ARMATURA:
					md.addModificatore(tipo == TipoArtefatto.VESTE ? TipoAttributo.RESISTENZA_MAGICA : TipoAttributo.PARATA,
							TipoModificatore.AUMENTO_PERCENTUALE, 5.0 * livello, "");
					break;
				default:
					md.addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 5.0 * livello, "");
					break;
			}
			if (incantamento != null) {
				GradoIncantamento grado = GradoIncantamento.perLivello(livello);
				md.addIncantamento("Incantamento " + grado.getNome(), incantamento, grado.getBonusFisso(), grado.getCoefficiente());
			}
			return Artefatto.di(md);
		}
	}
}
