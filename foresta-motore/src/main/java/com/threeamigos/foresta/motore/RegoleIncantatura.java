package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.ListinoPergamene;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.SupertipoArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.List;
import java.util.Optional;
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
 * Un aggiornamento (vedi TipoArtefatto.AGGIORNAMENTO, che si trova solo nei cofani) non dà effetti: fa salire di un livello
 * l'artefatto, ognuno di uno, fino al livello del mondo. Il livello nuovo vale già per la fusione stessa: più posti per gli
 * effetti. Costa come ogni fusione, e da solo basta a farla.
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
		return artefatto.getNumeroEffetti();
	}

	/**
	 * Gli effetti degli ingredienti sul banco che entreranno sull'artefatto (tutti, finché l'artefatto manca).
	 */
	public static int effettiDaTrasferire(Collection<Artefatto> banco) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
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
	 * Costo della fusione di quel numero di effetti, scontato secondo la CONTRATTAZIONE di chi tratta.
	 */
	public static int costo(int effettiDaTrasferire, int contrattazione) {
		return RegoleContrattazione.prezzoAcquisto(
				Costanti.FUSIONE_COSTO_BASE + Costanti.FUSIONE_COSTO_PER_EFFETTO * effettiDaTrasferire, contrattazione);
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
	 * Se la fusione farà perdere qualche effetto degli ingredienti sul banco o scalzarne qualcuno dell'artefatto, e
	 * perché: l'incantatore lo dice, ma la fusione si può fare. Vuoto se passa tutto senza scalzare niente (o se
	 * manca l'artefatto).
	 */
	public static Optional<String> avvisoEffettiPersi(Collection<Artefatto> banco) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		if (!artefatto.isPresent()) {
			return Optional.empty();
		}
		List<Artefatto> ingredienti = ingredienti(banco);
		int totale = ingredienti.stream().mapToInt(RegoleIncantatura::effetti).sum();
		int sensati = ingredienti.stream().mapToInt(i -> trasferibili(artefatto.get(), i).size()).sum();
		Piano piano = piano(artefatto.get(), banco);
		int passano = piano.daAggiungere.size();
		int scalzati = piano.daTogliere.size();
		if (passano == 0 && sensati > 0 && aggiornamenti(banco) == 0) {
			return Optional.of("Nessun effetto è migliore di quelli dell'artefatto: la fusione non cambierebbe niente.");
		}
		if (passano == totale && scalzati == 0) {
			return Optional.empty();
		}
		List<String> frasi = new ArrayList<>();
		if (sensati > passano) {
			frasi.add(passano == 1
					? "L'artefatto tiene i migliori effetti: passerà un solo effetto, gli altri andranno persi."
					: "L'artefatto tiene i migliori effetti: passeranno " + passano + " effetti, gli altri andranno persi.");
		} else if (totale > passano) {
			if (!isArma(artefatto.get()) && haModificatoriSulBersaglio(ingredienti)) {
				frasi.add("I modificatori sul bersaglio si fondono solo sulle armi: passeranno solo gli altri effetti.");
			} else {
				frasi.add(isLibro(artefatto.get())
						? "Sul libro gli incantamenti elementali non hanno effetto: passeranno solo gli altri effetti."
						: "Il potere magico si fonde solo su bastoni e libri magici: passeranno solo gli altri effetti.");
			}
		}
		if (scalzati > 0) {
			frasi.add(scalzati == 1
					? "Un effetto dell'artefatto sarà scalzato da uno migliore."
					: scalzati + " effetti dell'artefatto saranno scalzati da altri migliori.");
		}
		return Optional.of(String.join(" ", frasi));
	}

	/**
	 * Se si può aggiungere l'oggetto al banco: un solo artefatto, incantabile, con almeno un posto libero, e
	 * nessun ingrediente che sull'artefatto non avrebbe effetto, e senza far salire l'artefatto oltre il livello del mondo con gli
	 * aggiornamenti. Vuoto se si può.
	 */
	public static Optional<TipoMotivoRifiutoIncantatura> puoMettereSulBanco(Collection<Artefatto> banco, Artefatto nuovo) {
		Optional<Artefatto> artefatto = artefattoSulBanco(banco);
		List<Artefatto> ingredienti = ingredienti(banco);
		int aggiornamenti = aggiornamenti(banco);
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
			if (aggiornamenti > 0 && artefatto.get().getLivello() + aggiornamenti > livelloMassimoPerAggiornamenti()) {
				return Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO);
			}
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
		if (ingredienti.isEmpty() && aggiornamenti(banco) == 0) {
			return Optional.of(TipoMotivoRifiutoIncantatura.NESSUNA_PERGAMENA);
		}
		if (aggiornamenti(banco) > 0 && artefatto.getLivello() + aggiornamenti(banco) > livelloMassimoPerAggiornamenti()) {
			return Optional.of(TipoMotivoRifiutoIncantatura.LIVELLO_DEL_MONDO_RAGGIUNTO);
		}
		Optional<TipoMotivoRifiutoIncantatura> problema = problemi(artefatto, ingredienti);
		if (problema.isPresent()) {
			return problema;
		}
		if (piano(artefatto, banco).daAggiungere.isEmpty() && aggiornamenti(banco) == 0) {
			return Optional.of(TipoMotivoRifiutoIncantatura.NESSUN_MIGLIORAMENTO);
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
	 * ingredienti che entrano (togliendo quelli che scalzano), gli dà il nome proprio (normalizzato; vuoto vuol dire nessun nome) e svuota il
	 * banco. Gli ingredienti spariscono. Restituisce l'artefatto incantato; le monete le toglie chi chiama.
	 */
	static Artefatto fondi(BancoDiLavoro banco, String nomeProprio) {
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
	 * Gli ingredienti sul banco che danno effetti: gli aggiornamenti no.
	 */
	private static List<Artefatto> ingredienti(Collection<Artefatto> banco) {
		return banco.stream().filter(a -> isIngrediente(a) && !isAggiornamento(a)).collect(Collectors.toList());
	}

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
		return artefattoSulBanco(banco).map(a -> a.getLivello() + aggiornamenti(banco)).orElse(0);
	}

	/**
	 * Quanti effetti al massimo avrà l'artefatto sul banco dopo la fusione, con il suo livello nuovo; zero se manca.
	 */
	public static int effettiMassimiDopoLaFusione(Collection<Artefatto> banco) {
		return artefattoSulBanco(banco).filter(Artefatto::isIncantabile)
				.map(a -> a.getRarita().getEffettiMassimi(livelloDopoLaFusione(banco))).orElse(0);
	}

	/**
	 * Fin dove si può far salire un artefatto con gli aggiornamenti: il livello del mondo.
	 */
	private static int livelloMassimoPerAggiornamenti() {
		return Statistiche.getLivello();
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
		return artefatto.getTipo().getSupertipo() == SupertipoArtefatto.ARMA;
	}

	private static boolean haModificatoriSulBersaglio(List<Artefatto> ingredienti) {
		return ingredienti.stream().flatMap(i -> i.getModificatori().stream())
				.anyMatch(m -> m.getBersaglio() == BersaglioModificatore.BERSAGLIO);
	}

	private static boolean isLibro(Artefatto artefatto) {
		return artefatto.getTipo().getSupertipo() == SupertipoArtefatto.POTENZIAMENTO_POTERE_MAGICO;
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

		public int numeroDaAggiungere() {
			return daAggiungere.size();
		}

		public int numeroDaTogliere() {
			return daTogliere.size();
		}
	}
}
