package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.artefatti.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.artefatti.tipi.TipoMotivoRifiutoIncantatura;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Le regole della fusione (vedi artefatti_e_incantamenti.md, §2 "Fusione"): sul banco un solo artefatto
 * incantabile e almeno un ingrediente magico (pergamena, gemma, monile, gingillo, sigillo). Tra gli effetti
 * che l'artefatto ha già e quelli degli ingredienti che vi hanno senso (incantamenti e modificatori) restano
 * sull'artefatto i più preziosi (secondo {@link ListinoPergamene}), senza superare il numero massimo di effetti
 * dell'artefatto: un effetto nuovo migliore ne scalza uno vecchio (uno solo, anche se ce ne sono di uguali),
 * e gli effetti che non entrano vanno persi. L'incantatore lo dice prima. Su un libro magico non passano gli incantamenti elementali, che
 * agiscono solo sulle armi; il POTERE_MAGICO passa solo su bastoni e libri magici. Costo 10 monete più 5 per
 * effetto trasferito. Gli ingredienti usati vengono distrutti.
 * <p>
 * Con il libro che riprogramma sul banco (vedi TipoArtefatto.RIPROGRAMMAZIONE, che si trova solo nei cofani) la fusione è un'altra
 * cosa: l'artefatto perde tutti gli incantamenti e per ognuno nasce un ingrediente "del Core Dump" (vedi
 * {@link #riprogramma}). Sul banco stanno comunque tutti gli ingredienti che si vuole: non vengono usati, e tornano nel gruppo con
 * il libro. Costa come una fusione con tanti effetti quanti gli incantamenti.
 * <p>
 * Un aggiornamento (vedi TipoArtefatto.AGGIORNAMENTO, che si trova solo nei cofani) non dà effetti: fa salire di un livello
 * l'artefatto, ognuno di uno, fino al livello del mondo. Il livello nuovo vale già per la fusione stessa: più posti per gli
 * effetti. Costa come ogni fusione, e da solo basta a farla.
 */
public final class RegoleIncantatura {

	private RegoleIncantatura() {
	}

	/**
	 * Se è un ingrediente da fondere (pergamena, gemma, monile, gingillo, sigillo, aggiornamento), e non l'artefatto da incantare.
	 */
	public static boolean isIngrediente(Artefatto artefatto) {
		return artefatto.getTipo().isIngrediente();
	}

	/**
	 * Se è il libro che riprogramma (vedi TipoArtefatto.RIPROGRAMMAZIONE).
	 */
	public static boolean isRiprogrammazione(Artefatto artefatto) {
		return artefatto.getTipo().isRiprogrammazione();
	}

	/**
	 * Se sul banco c'è il libro che riprogramma: allora la fusione è una riprogrammazione.
	 */
	public static boolean conRiprogrammazione(Collection<Artefatto> banco) {
		return banco.stream().anyMatch(RegoleIncantatura::isRiprogrammazione);
	}

	/**
	 * Numero di effetti (incantamenti più modificatori) di un artefatto o di un ingrediente.
	 */
	public static int effetti(Artefatto artefatto) {
		return artefatto.getNumeroEffetti();
	}

	/**
	 * Gli effetti degli ingredienti sul banco che entreranno sull'artefatto (tutti, finché l'artefatto manca).
	 */
	public static int effettiDaTrasferire(Collection<Artefatto> banco) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		if (conRiprogrammazione(banco)) {
			// Si paga per ogni incantamento da riprogrammare
			return artefatto.map(a -> a.getIncantamenti().size()).orElse(0);
		}
		if (!artefatto.isPresent()) {
			return ingredienti(banco).stream().mapToInt(RegoleIncantatura::effetti).sum();
		}
		return piano(artefatto.get(), banco).daAggiungere.size();
	}

	/**
	 * Che cosa farebbe la fusione di quel che c'è sul banco: quali effetti entrano e quali escono. Vuoto se manca
	 * l'artefatto. Il calcolo non è gratis: chi lo chiede spesso (la UI) lo tiene da parte.
	 */
	public static Piano pianoDellaFusione(Collection<Artefatto> banco) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		return artefatto.isPresent() ? piano(artefatto.get(), banco) : Piano.VUOTO;
	}

	/**
	 * Costo base della fusione di quel numero di effetti: 10 monete più 5 per ogni effetto. Lo sconto della contrattazione di chi tratta
	 * non è una regola dell'incantesimo: lo applica chi chiama.
	 */
	public static int costo(int effettiDaTrasferire) {
		return CostantiArtefatti.FUSIONE_COSTO_BASE + CostantiArtefatti.FUSIONE_COSTO_PER_EFFETTO * effettiDaTrasferire;
	}

	/**
	 * Quanti effetti avrà l'artefatto dopo la fusione: i suoi, meno quelli scalzati, più quelli nuovi. Zero se
	 * sul banco manca l'artefatto.
	 */
	public static int effettiDopoLaFusione(Collection<Artefatto> banco) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		if (!artefatto.isPresent()) {
			return 0;
		}
		Piano piano = piano(artefatto.get(), banco);
		return effetti(artefatto.get()) - piano.daTogliere.size() + piano.daAggiungere.size();
	}

	/**
	 * Che cosa farà perdere la fusione, se qualcosa: effetti degli ingredienti sul banco che non passano, o dell'artefatto che
	 * un effetto migliore scalza (vedi {@link AvvisoDiFusione}). L'incantatore lo dice, ma la fusione si può fare. Vuoto se passa tutto
	 * senza scalzare niente (o se manca l'artefatto, o se si riprogramma).
	 */
	public static Optional<AvvisoDiFusione> avvisoEffettiPersi(Collection<Artefatto> banco) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		if (!artefatto.isPresent() || conRiprogrammazione(banco)) {
			return Optional.empty();
		}
		List<Artefatto> ingredienti = ingredienti(banco);
		int totale = ingredienti.stream().mapToInt(RegoleIncantatura::effetti).sum();
		int sensati = ingredienti.stream().mapToInt(i -> trasferibili(artefatto.get(), i).size()).sum();
		Piano piano = piano(artefatto.get(), banco);
		int passano = piano.daAggiungere.size();
		int scalzati = piano.daTogliere.size();
		if (passano == 0 && sensati > 0 && aggiornamenti(banco) == 0) {
			return Optional.of(AvvisoDiFusione.nessunMiglioramento());
		}
		if (passano == totale && scalzati == 0) {
			return Optional.empty();
		}
		AvvisoDiFusione.Perdita perdita = null;
		if (sensati > passano) {
			perdita = AvvisoDiFusione.Perdita.LIMITE_DI_EFFETTI;
		} else if (totale > passano) {
			if (!isArma(artefatto.get()) && haModificatoriSulBersaglio(ingredienti)) {
				perdita = AvvisoDiFusione.Perdita.MODIFICATORI_SUL_BERSAGLIO_SOLO_SULLE_ARMI;
			} else {
				perdita = isLibro(artefatto.get())
						? AvvisoDiFusione.Perdita.INCANTAMENTI_ELEMENTALI_NON_SUL_LIBRO
						: AvvisoDiFusione.Perdita.POTERE_MAGICO_SOLO_SU_BASTONI_E_LIBRI;
			}
		}
		return Optional.of(AvvisoDiFusione.di(perdita, passano, scalzati));
	}

	/**
	 * Se si può aggiungere l'oggetto al banco: un solo artefatto, incantabile, con almeno un posto libero, e
	 * nessun ingrediente che sull'artefatto non avrebbe effetto, e senza far salire l'artefatto oltre il livello del mondo con gli
	 * aggiornamenti: oltre il livello massimo non si va. Vuoto se si può.
	 */
	public static Optional<TipoMotivoRifiutoIncantatura> puoMettereSulBanco(Collection<Artefatto> banco, Artefatto nuovo, int livelloMassimo) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		List<Artefatto> ingredienti = ingredienti(banco);
		int aggiornamenti = aggiornamenti(banco);
		boolean riprogramma = conRiprogrammazione(banco) || isRiprogrammazione(nuovo);
		if (isAggiornamento(nuovo)) {
			aggiornamenti++;
		} else if (isIngrediente(nuovo)) {
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
			// Con il libro che riprogramma gli ingredienti non si usano: ce ne può stare qualunque
			if (riprogramma) {
				return Optional.empty();
			}
			if (aggiornamenti > 0 && artefatto.get().getLivello() + aggiornamenti > livelloMassimo) {
				return Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO);
			}
			return problemi(artefatto.get(), ingredienti);
		}
		return Optional.empty();
	}

	/**
	 * Se si può fare la fusione con quel che c'è sul banco, le monete del gruppo e il costo che il gruppo deve pagare (quello di
	 * {@link #costo(Collection)}, scontato come vuole chi chiama). Gli aggiornamenti non portano l'artefatto oltre il livello massimo.
	 * Vuoto se si può.
	 */
	public static Optional<TipoMotivoRifiutoIncantatura> verifica(Collection<Artefatto> banco, int monete, int costoDaPagare, int livelloMassimo) {
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
		if (conRiprogrammazione(banco)) {
			if (artefatto.getIncantamenti().isEmpty()) {
				return Optional.of(TipoMotivoRifiutoIncantatura.NIENTE_DA_RIPROGRAMMARE);
			}
			return monete < costoDaPagare ? Optional.of(TipoMotivoRifiutoIncantatura.MONETE_INSUFFICIENTI) : Optional.empty();
		}
		if (ingredienti.isEmpty() && aggiornamenti(banco) == 0) {
			return Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_INGREDIENTE);
		}
		if (aggiornamenti(banco) > 0 && artefatto.getLivello() + aggiornamenti(banco) > livelloMassimo) {
			return Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO);
		}
		Optional<TipoMotivoRifiutoIncantatura> problema = problemi(artefatto, ingredienti);
		if (problema.isPresent()) {
			return problema;
		}
		if (piano(artefatto, banco).daAggiungere.isEmpty() && aggiornamenti(banco) == 0) {
			return Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_MIGLIORAMENTO);
		}
		if (monete < costoDaPagare) {
			return Optional.of(TipoMotivoRifiutoIncantatura.MONETE_INSUFFICIENTI);
		}
		return Optional.empty();
	}

	/**
	 * Costo base della fusione: 10 monete più 5 per ogni effetto degli ingredienti sul banco che passa sull'artefatto.
	 */
	public static int costo(Collection<Artefatto> banco) {
		return costo(effettiDaTrasferire(banco));
	}

	/**
	 * L'artefatto sul banco, se c'è.
	 */
	public static Optional<Artefatto> artefattoSulBanco(Collection<Artefatto> banco) {
		return banco.stream().filter(a -> !isIngrediente(a)).findFirst();
	}

	/**
	 * Fa la fusione, che va prima verificata ({@link #verifica}): copia sull'artefatto gli effetti degli
	 * ingredienti che entrano (togliendo quelli che scalzano), gli dà il nome proprio (normalizzato; vuoto vuol dire nessun nome) e svuota il
	 * banco. Gli ingredienti spariscono. Restituisce l'artefatto incantato; le monete le toglie chi chiama.
	 */
	public static Artefatto fondi(BancoDiLavoro banco, String nomeProprio) {
		Collection<Artefatto> sulBanco = banco.getInventario();
		Artefatto artefatto = artefattoSulBanco(sulBanco).orElseThrow(IllegalStateException::new);
		ArtefattoMD md = artefatto.getModelloDati();
		Piano piano = piano(artefatto, sulBanco);
		// Il livello sale per primo: il piano ha già contato i posti del livello nuovo
		md.setLivello(livelloDopoLaFusione(sulBanco));
		for (Effetto effetto : piano.daTogliere) {
			if (effetto.incantamento != null) {
				md.removeIncantamento(effetto.incantamento);
			} else {
				md.removeModificatore(effetto.modificatore);
			}
		}
		for (Effetto effetto : piano.daAggiungere) {
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

	/**
	 * La riprogrammazione, che va prima verificata ({@link #verifica}): toglie all'artefatto tutti gli incantamenti e per ognuno
	 * crea un ingrediente "del Core Dump" (vedi IngredienteDelCoreDump) che lo custodisce. Il banco si svuota: l'artefatto,
	 * il libro, gli altri ingredienti (che non servono a niente) e gli ingredienti nuovi tornano tutti a chi chiama, che li
	 * rimette nel gruppo. Le monete le toglie chi chiama.
	 */
	public static List<Artefatto> riprogramma(BancoDiLavoro banco) {
		Collection<Artefatto> sulBanco = banco.getInventario();
		Artefatto artefatto = artefattoSulBanco(sulBanco).orElseThrow(IllegalStateException::new);
		ArtefattoMD md = artefatto.getModelloDati();
		List<Artefatto> daRestituire = new ArrayList<>(sulBanco);
		for (IncantamentoMD incantamento : new ArrayList<>(md.getIncantamenti())) {
			md.removeIncantamento(incantamento);
			daRestituire.add(IngredienteDelCoreDump.da(incantamento, artefatto.getLivello()));
		}
		for (Artefatto oggetto : sulBanco) {
			banco.removeArtefatto(oggetto);
		}
		return daRestituire;
	}

	/**
	 * Gli ingredienti sul banco che danno effetti: gli aggiornamenti no.
	 */
	private static List<Artefatto> ingredienti(Collection<Artefatto> banco) {
		return banco.stream().filter(a -> isIngrediente(a) && !isAggiornamento(a)).collect(Collectors.toList());
	}

	/**
	 * Se è un aggiornamento (vedi TipoArtefatto.AGGIORNAMENTO): fa salire di un livello l'artefatto.
	 */
	public static boolean isAggiornamento(Artefatto artefatto) {
		return artefatto.getTipo().isAggiornamento();
	}

	/**
	 * Quanti aggiornamenti ci sono sul banco: tanti livelli in più per l'artefatto.
	 */
	public static int aggiornamenti(Collection<Artefatto> banco) {
		return (int) banco.stream().filter(RegoleIncantatura::isAggiornamento).count();
	}

	/**
	 * Il livello dell'artefatto sul banco dopo la fusione, con gli aggiornamenti messi; zero se manca l'artefatto.
	 */
	public static int livelloDopoLaFusione(Collection<Artefatto> banco) {
		// Una riprogrammazione non fa salire di livello: gli aggiornamenti sul banco non si usano
		return artefattoSulBanco(banco).map(a -> a.getLivello() + (conRiprogrammazione(banco) ? 0 : aggiornamenti(banco))).orElse(0);
	}

	/**
	 * Quanti effetti al massimo avrà l'artefatto sul banco dopo la fusione, con il suo livello nuovo; zero se manca.
	 */
	public static int effettiMassimiDopoLaFusione(Collection<Artefatto> banco) {
		return artefattoSulBanco(banco).filter(Artefatto::isIncantabile)
				.map(a -> a.getRarita().getEffettiMassimi(livelloDopoLaFusione(banco))).orElse(0);
	}

	/**
	 * Perché l'artefatto non può ricevere niente da quegli ingredienti, se è così: un ingrediente non vi avrebbe
	 * nessun effetto. Se gli effetti nuovi siano migliori di quelli presenti lo dice {@link #verifica}.
	 */
	private static Optional<TipoMotivoRifiutoIncantatura> problemi(Artefatto artefatto, Collection<Artefatto> ingredienti) {
		if (ingredienti.stream().anyMatch(i -> trasferibili(artefatto, i).isEmpty())) {
			return Optional.of(isLibro(artefatto)
					? TipoMotivoRifiutoIncantatura.INCANTAMENTO_SU_LIBRO
					: TipoMotivoRifiutoIncantatura.POTERE_MAGICO_FUORI_POSTO);
		}
		return Optional.empty();
	}

	/**
	 * Quali effetti restano sull'artefatto dopo la fusione: fra i suoi e quelli degli ingredienti che hanno senso,
	 * i più preziosi, fino al numero massimo di effetti (o a quanti ne ha già, se ne ha di più, come certi
	 * leggendari: non ne guadagna e non ne perde per questo). A parità di prezzo vince l'effetto già presente, poi
	 * l'ordine sul banco: così non si scalza niente per niente.
	 */
	private static Piano piano(Artefatto artefatto, Collection<Artefatto> banco) {
		if (conRiprogrammazione(banco)) {
			// Niente entra, e tutti gli incantamenti escono: ognuno diventa un ingrediente
			Piano riprogrammazione = new Piano();
			for (IncantamentoMD incantamento : artefatto.getIncantamenti()) {
				riprogrammazione.togli(new Effetto(incantamento, null, ListinoPergamene.prezzo(incantamento, artefatto.getLivello())));
			}
			return riprogrammazione;
		}
		List<Artefatto> ingredienti = ingredienti(banco);
		int livello = artefatto.getLivello() + aggiornamenti(banco);
		List<Effetto> candidati = new ArrayList<>();
		for (IncantamentoMD incantamento : artefatto.getIncantamenti()) {
			candidati.add(new Effetto(incantamento, null, ListinoPergamene.prezzo(incantamento, livello)));
		}
		for (ModificatoreAttributoMD modificatore : artefatto.getModificatori()) {
			candidati.add(new Effetto(null, modificatore, ListinoPergamene.prezzo(modificatore)));
		}
		int presenti = candidati.size();
		ingredienti.forEach(i -> candidati.addAll(trasferibili(artefatto, i, livello)));
		int posti = Math.max(artefatto.isIncantabile() ? artefatto.getRarita().getEffettiMassimi(livello) : 0, presenti);
		// Il sorted è stabile: a parità di prezzo restano nell'ordine in cui sono (prima i presenti)
		List<Effetto> ordinati = candidati.stream()
				.sorted(Comparator.comparingDouble((Effetto e) -> e.prezzo).reversed())
				.collect(Collectors.toList());
		Piano piano = new Piano();
		for (int i = 0; i < ordinati.size(); i++) {
			Effetto effetto = ordinati.get(i);
			boolean presente = candidati.indexOf(effetto) < presenti;
			if (i < posti) {
				if (!presente) {
					piano.aggiungi(effetto);
				}
			} else if (presente) {
				piano.togli(effetto);
			}
		}
		return piano;
	}

	/**
	 * Gli effetti di un ingrediente che sull'artefatto hanno senso.
	 */
	private static List<Effetto> trasferibili(Artefatto artefatto, Artefatto ingrediente) {
		return trasferibili(artefatto, ingrediente, artefatto.getLivello());
	}

	private static List<Effetto> trasferibili(Artefatto artefatto, Artefatto ingrediente, int livello) {
		List<Effetto> effetti = new ArrayList<>();
		if (!isLibro(artefatto)) {
			for (IncantamentoMD incantamento : ingrediente.getIncantamenti()) {
				effetti.add(new Effetto(incantamento, null, ListinoPergamene.prezzo(incantamento, livello)));
			}
		}
		for (ModificatoreAttributoMD modificatore : ingrediente.getModificatori()) {
			if (modificatore.getBersaglio() == BersaglioModificatore.BERSAGLIO && !isArma(artefatto)) {
				continue;
			}
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

	/**
	 * Un modificatore sul bersaglio lo mette un'arma quando colpisce: non ha senso su altro
	 */
	private static boolean isArma(Artefatto artefatto) {
		return artefatto.getTipo().getCategoria() == CategoriaArtefatto.ARMA;
	}

	private static boolean haModificatoriSulBersaglio(List<Artefatto> ingredienti) {
		return ingredienti.stream().flatMap(i -> i.getModificatori().stream())
				.anyMatch(m -> m.getBersaglio() == BersaglioModificatore.BERSAGLIO);
	}

	private static boolean isLibro(Artefatto artefatto) {
		return artefatto.getTipo().getCategoria() == CategoriaArtefatto.POTENZIAMENTO_POTERE_MAGICO;
	}

	/**
	 * Un effetto di un ingrediente: un incantamento oppure un modificatore, con il suo prezzo
	 */
	private static final class Effetto {

		private final IncantamentoMD incantamento;
		private final ModificatoreAttributoMD modificatore;
		private final double prezzo;

		private Effetto(IncantamentoMD incantamento, ModificatoreAttributoMD modificatore, double prezzo) {
			this.incantamento = incantamento;
			this.modificatore = modificatore;
			this.prezzo = prezzo;
		}
	}

	/**
	 * Gli effetti nuovi che entrano sull'artefatto e quelli suoi che vengono scalzati
	 */
	public static final class Piano {

		private static final Piano VUOTO = new Piano();

		private final List<Effetto> daAggiungere = new ArrayList<>();
		private final List<Effetto> daTogliere = new ArrayList<>();

		/**
		 * Un piano senza effetti: li aggiunge chi lo costruisce.
		 */
		private Piano() {
		}
		// Gli effetti stessi (gli oggetti dei modelli dati, per identità: due +5 Forza uguali sono due effetti)
		private final Set<Object> inEntrata = Collections.newSetFromMap(new IdentityHashMap<>());
		private final Set<Object> inUscita = Collections.newSetFromMap(new IdentityHashMap<>());

		private void aggiungi(Effetto effetto) {
			daAggiungere.add(effetto);
			inEntrata.add(effetto.incantamento != null ? effetto.incantamento : effetto.modificatore);
		}

		private void togli(Effetto effetto) {
			daTogliere.add(effetto);
			inUscita.add(effetto.incantamento != null ? effetto.incantamento : effetto.modificatore);
		}

		/**
		 * Se l'effetto (un IncantamentoMD o un ModificatoreAttributoMD di un ingrediente) passerà sull'artefatto
		 */
		public boolean entra(Object effetto) {
			return inEntrata.contains(effetto);
		}

		/**
		 * Se l'effetto (un IncantamentoMD o un ModificatoreAttributoMD dell'artefatto) verrà scalzato
		 */
		public boolean esce(Object effetto) {
			return inUscita.contains(effetto);
		}

		/**
		 * Quanti effetti degli ingredienti entrano sull'artefatto.
		 */
		public int numeroDaAggiungere() {
			return daAggiungere.size();
		}

		/**
		 * Quanti effetti dell'artefatto escono perché ne entrano di migliori.
		 */
		public int numeroDaTogliere() {
			return daTogliere.size();
		}
	}
}
