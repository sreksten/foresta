package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.tipi.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Scheletro del generatore: nomi da piccole tabelle interne e valori da formule semplici,
 * tarate sugli artefatti dei templi (RegistroArtefatti). Tutto va bilanciato. Per i tipi che la
 * grammatica degli artefatti conosce (spada, armatura, veste, elmo, scudo e schinieri), una parte degli
 * artefatti prende da lì nome ed effetti (vedi {@link GrammaticaArtefatti}). Gli ingredienti magici vengono
 * tutti dalla loro grammatica, se si carica.
 */
public class GeneratoreArtefattiTabelle implements GeneratoreArtefatti {

	static final GeneratoreArtefatti ISTANZA = new GeneratoreArtefattiTabelle(Dado.sorgente(), GrammaticaArtefatti.caricaOppureNull(),
			GrammaticaArtefatti.caricaIngredientiOppureNull());

	private static final Map<TipoArtefatto, String> NOMI = new EnumMap<>(TipoArtefatto.class);
	private static final Map<TipoArtefatto, Double> PESI = new EnumMap<>(TipoArtefatto.class);

	static {
		nomeEPeso(TipoArtefatto.SPADA, "la spada", 1);
		nomeEPeso(TipoArtefatto.SPADONE, "lo spadone", 3);
		nomeEPeso(TipoArtefatto.MAZZA, "la mazza", 2);
		nomeEPeso(TipoArtefatto.ASCIA, "l'ascia", 2);
		nomeEPeso(TipoArtefatto.LANCIA, "la lancia", 2);
		nomeEPeso(TipoArtefatto.BASTONE_MAGICO, "il bastone", 1);
		nomeEPeso(TipoArtefatto.LIBRO_MAGICO, "il libro", 1);
		nomeEPeso(TipoArtefatto.SCUDO, "lo scudo", 2);
		nomeEPeso(TipoArtefatto.ELMO, "l'elmo", 1);
		nomeEPeso(TipoArtefatto.ARMATURA, "l'armatura", 3);
		nomeEPeso(TipoArtefatto.SCHINIERI, "gli schinieri", 2);
		nomeEPeso(TipoArtefatto.VESTE, "la veste", 1);
		nomeEPeso(TipoArtefatto.ANELLO, "l'anello", 0.1);
		nomeEPeso(TipoArtefatto.TALISMANO, "il talismano", 0.5);
		nomeEPeso(TipoArtefatto.NINNOLO, "il ninnolo", 0.2);
		nomeEPeso(TipoArtefatto.PERGAMENA, "la pergamena", 0.1);
		nomeEPeso(TipoArtefatto.GEMMA, "la gemma", 0.1);
		nomeEPeso(TipoArtefatto.MONILE, "il monile", 0.2);
		nomeEPeso(TipoArtefatto.GINGILLO, "il gingillo", 0.2);
		nomeEPeso(TipoArtefatto.SIGILLO, "il sigillo", 0.1);
	}

	private static final String[] COMPLEMENTI = {
			"di ferro", "d'acciaio", "d'argento", "di bronzo", "d'osso",
			"del viandante", "della guardia", "del cacciatore", "dell'eremita", "del mercenario"
	};

	private static final String[] NOMI_PROPRI = {
			"Diavolina", "Morsofreddo", "Ultimaparola", "Spaccaossa", "Lucente", "Brontolona", "Sussurro", "Vedova Nera"
	};

	private static final String COMBATTIMENTO = "il cui potere è nel combattimento";
	private static final String PROTEZIONE = "che protegge dagli attacchi avversari";
	private static final String PROTEZIONE_PAIO = "che proteggono dagli attacchi avversari";
	private static final String MAGIA = "che aumenta il potere magico";

	/**
	 * Accessori: l'attributo che migliorano e la descrizione che lo dice
	 */
	private static final Object[][] ACCESSORI = {
			{ TipoAttributo.CARISMA, "che aumenta il Carisma", "che aumentano il Carisma" },
			{ TipoAttributo.CORAGGIO, "che aumenta il Coraggio", "che aumentano il Coraggio" },
			{ TipoAttributo.VALORE, "che aumenta il Valore", "che aumentano il Valore" },
			{ TipoAttributo.FORTUNA, "che porta Fortuna", "che portano Fortuna" },
			{ TipoAttributo.PERCEZIONE, "che acuisce i sensi", "che acuiscono i sensi" }
	};

	/**
	 * Gli incantamenti sono elementali o magici: il danno fisico lo dà già l'arma
	 */
	private static final List<TipoDanno> TIPI_DANNO_INCANTAMENTO = Arrays.stream(TipoDanno.values())
			.filter(t -> t.getSuperTipo() != SupertipoDanno.FISICO)
			.collect(Collectors.toList());

	private static final List<TipoArtefatto> TIPI_CASUALI = Arrays.stream(TipoArtefatto.values())
			.filter(t -> !t.isIngrediente())
			.collect(Collectors.toList());

	private static final List<TipoArtefatto> INGREDIENTI = Arrays.stream(TipoArtefatto.values())
			.filter(TipoArtefatto::isIngrediente)
			.collect(Collectors.toList());

	/**
	 * La specialità di ogni ingrediente magico: gli attributi che migliora o i tipi di danno che dà
	 */
	private static final Map<TipoArtefatto, List<TipoAttributo>> ATTRIBUTI_DEGLI_INGREDIENTI = new EnumMap<>(TipoArtefatto.class);
	private static final Map<TipoArtefatto, List<TipoDanno>> DANNI_DEGLI_INGREDIENTI = new EnumMap<>(TipoArtefatto.class);

	static {
		// La pergamena le caratteristiche primarie
		ATTRIBUTI_DEGLI_INGREDIENTI.put(TipoArtefatto.PERGAMENA, Arrays.stream(TipoAttributo.values())
				.filter(TipoAttributo::isPrimario).collect(Collectors.toList()));
		// Il gingillo le secondarie che servono ad attaccare
		ATTRIBUTI_DEGLI_INGREDIENTI.put(TipoArtefatto.GINGILLO, Arrays.asList(TipoAttributo.CRITICO, TipoAttributo.PRECISIONE,
				TipoAttributo.VELOCITA, TipoAttributo.FURTIVITA, TipoAttributo.PERCEZIONE, TipoAttributo.FURIA, TipoAttributo.SOGGEZIONE));
		// Il sigillo quelle che servono a difendersi e a tenere duro; il POTERE_MAGICO si fonde solo su bastoni e libri
		ATTRIBUTI_DEGLI_INGREDIENTI.put(TipoArtefatto.SIGILLO, Arrays.asList(TipoAttributo.PARATA, TipoAttributo.RESISTENZA_MAGICA,
				TipoAttributo.CORAGGIO, TipoAttributo.VALORE, TipoAttributo.CARICO_MASSIMO, TipoAttributo.POTERE_MAGICO));
		// La gemma gli incantamenti elementali, il monile quelli magici
		DANNI_DEGLI_INGREDIENTI.put(TipoArtefatto.GEMMA, TIPI_DANNO_INCANTAMENTO.stream()
				.filter(t -> t.getSuperTipo() == SupertipoDanno.ELEMENTALE).collect(Collectors.toList()));
		DANNI_DEGLI_INGREDIENTI.put(TipoArtefatto.MONILE, TIPI_DANNO_INCANTAMENTO.stream()
				.filter(t -> t.getSuperTipo() == SupertipoDanno.MAGICO).collect(Collectors.toList()));
	}

	private final Random random;
	/**
	 * Null se non si usano: allora gli artefatti, o gli ingredienti, vengono tutti dalle tabelle
	 */
	private final GrammaticaArtefatti grammatica;
	private final GrammaticaArtefatti grammaticaIngredienti;

	/**
	 * Un generatore che usa solo le tabelle.
	 */
	public GeneratoreArtefattiTabelle(Random random) {
		this(random, null, null);
	}

	GeneratoreArtefattiTabelle(Random random, GrammaticaArtefatti grammatica, GrammaticaArtefatti grammaticaIngredienti) {
		this.random = random;
		this.grammatica = grammatica;
		this.grammaticaIngredienti = grammaticaIngredienti;
	}

	/**
	 * Gli attributi che un ingrediente può migliorare: vuoto se dà solo incantamenti.
	 */
	static List<TipoAttributo> attributiDi(TipoArtefatto ingrediente) {
		return ATTRIBUTI_DEGLI_INGREDIENTI.getOrDefault(ingrediente, Collections.emptyList());
	}

	/**
	 * I tipi di danno degli incantamenti che un ingrediente può dare: vuoto se dà solo modificatori.
	 */
	static List<TipoDanno> danniDi(TipoArtefatto ingrediente) {
		return DANNI_DEGLI_INGREDIENTI.getOrDefault(ingrediente, Collections.emptyList());
	}

	private static void nomeEPeso(TipoArtefatto tipo, String nome, double peso) {
		NOMI.put(tipo, nome);
		PESI.put(tipo, peso);
	}

	@Override
	public Artefatto generaArtefatto(TipoArtefatto tipo, int livello) {
		if (tipo.isIngrediente()) {
			return generaIngrediente(tipo, livello);
		}
		int livelloEffettivo = Math.max(1, livello);
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(tipo);
		md.setNome(NOMI.get(tipo) + ' ' + scegli(COMPLEMENTI));
		md.setLivello(livelloEffettivo);
		md.setPeso(PESI.get(tipo));
		md.setCostoAcquisto(5 + 5 * livelloEffettivo);
		if (grammatica != null && grammatica.supporta(tipo) && random.nextDouble() < Costanti.ARTEFATTO_PROBABILITA_DA_GRAMMATICA) {
			return generaDaGrammatica(md, livelloEffettivo);
		}
		if (random.nextDouble() < Costanti.ARTEFATTO_PROBABILITA_NOME_PROPRIO) {
			md.setNomeProprio(scegli(NOMI_PROPRI));
		}
		// La rarità conta solo per il numero di effetti, quindi solo per gli artefatti incantabili
		if (Artefatto.di(md).isIncantabile() && random.nextDouble() < Costanti.ARTEFATTO_PROBABILITA_RARO) {
			md.setRarita(TipoRaritaArtefatto.RARO);
		}
		switch (tipo.getSupertipo()) {
			case ARMA:
				completaArma(md, livelloEffettivo);
				break;
			case SCUDO:
			case ELMO:
			case ARMATURA:
			case SCHINIERI:
				completaProtezione(md, livelloEffettivo);
				break;
			case POTENZIAMENTO_POTERE_MAGICO:
				md.setDescrizione(MAGIA);
				md.addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 5.0 * livelloEffettivo, "");
				break;
			default:
				Object[] accessorio = ACCESSORI[random.nextInt(ACCESSORI.length)];
				if (tipo.getCardinalita() == TipoCardinalitaArtefatto.SINGOLO) {
					md.setDescrizione((String) accessorio[1]);
				} else {
					md.setDescrizione((String) accessorio[2]);
				}
				md.addModificatore((TipoAttributo) accessorio[0], TipoModificatore.AUMENTO_FISSO, livelloEffettivo, "");
				break;
		}
		Artefatto artefatto = Artefatto.di(md);
		incantaForse(artefatto, livelloEffettivo);
		return artefatto;
	}

	/**
	 * Ogni tanto un artefatto incantabile nasce già incantato, tanto più spesso quanto più è alto il livello
	 * (vedi {@link #probabilitaIncantato(int)}). Gli incantamenti sono del grado del livello, sulle armi danno
	 * danno aggiuntivo e sui pezzi difensivi resistenza. Sono al massimo 3 e lasciano sempre almeno un posto
	 * libero nel limite di effetti dell'artefatto (che conta anche i modificatori che ha già), così il giocatore
	 * ha sempre modo di migliorarlo con la fusione. Il prezzo cresce come quello delle pergamene.
	 */
	private void incantaForse(Artefatto artefatto, int livello) {
		ArtefattoMD md = artefatto.getModelloDati();
		int postiLiberi = artefatto.getEffettiMassimi() - md.getModificatori().size() - md.getIncantamenti().size();
		int massimo = Math.min(Costanti.ARTEFATTO_MASSIMO_INCANTAMENTI_IN_NASCITA, postiLiberi - 1);
		if (massimo <= 0 || random.nextDouble() >= probabilitaIncantato(livello)) {
			return;
		}
		GradoIncantamento grado = GradoIncantamento.perLivello(livello);
		int numero = 1 + random.nextInt(massimo);
		for (int i = 0; i < numero; i++) {
			Incantamento incantamento = generaIncantamento(grado);
			md.addIncantamento(incantamento);
			md.setCostoAcquisto(md.getCostoAcquisto() + (int) Math.round(ListinoPergamene.prezzo(incantamento)));
		}
	}

	/**
	 * Nome, soprannome, descrizione ed effetti vengono dalla grammatica, che ne sceglie tanti quanti ne ammette
	 * l'artefatto meno uno, lasciando come {@link #incantaForse} un posto libero per la fusione. Armi e pezzi
	 * difensivi hanno prima quello che danno loro le tabelle (danno, PARATA di base), che occupa i suoi posti.
	 * Gli effetti sono del grado del livello: un modificatore vale la sua intensità per il gradino, un
	 * incantamento ha sia la parte fissa sia la percentuale. Il prezzo sale per i modificatori positivi e gli incantamenti e scende per i
	 * modificatori negativi, ma non sotto la metà del prezzo base. Niente incantaForse: un incantamento a caso
	 * contraddirebbe il nome.
	 */
	private Artefatto generaDaGrammatica(ArtefattoMD md, int livello) {
		TipoArtefatto tipo = md.getTipo();
		if (Artefatto.di(md).isIncantabile() && random.nextDouble() < Costanti.ARTEFATTO_PROBABILITA_RARO) {
			md.setRarita(TipoRaritaArtefatto.RARO);
		}
		md.setNome(NOMI.get(tipo));
		switch (tipo.getSupertipo()) {
			case ARMA:
				completaArma(md, livello);
				break;
			case SCUDO:
			case ELMO:
			case ARMATURA:
			case SCHINIERI:
				completaProtezione(md, livello);
				break;
			default:
				break;
		}
		int effetti = Artefatto.di(md).getEffettiMassimi() - md.getModificatori().size() - md.getIncantamenti().size() - 1;
		GrammaticaArtefatti.Risultato risultato = grammatica.genera(tipo, effetti);
		md.setNome(risultato.getNome());
		md.setNomeProprio(risultato.getSoprannome());
		if (risultato.getDescrizione() != null) {
			md.setDescrizione(risultato.getDescrizione());
		}
		GradoIncantamento grado = GradoIncantamento.perLivello(livello);
		int costoBase = md.getCostoAcquisto();
		double costo = costoBase;
		for (GrammaticaArtefatti.Modificatore modificatore : risultato.getModificatori()) {
			ModificatoreAttributo aggiunto = new ModificatoreAttributo(modificatore.getAttributo(), TipoModificatore.AUMENTO_FISSO,
					modificatore.getIntensita() * grado.getGradino(), "");
			md.addModificatore(aggiunto);
			costo += Math.signum(modificatore.getIntensita()) * ListinoPergamene.prezzo(aggiunto);
		}
		for (TipoDanno tipoDanno : risultato.getDanni()) {
			Incantamento incantamento = new Incantamento(tipoDanno.getNome() + ' ' + grado.getNome(), tipoDanno,
					grado.getBonusFisso(), grado.getCoefficiente());
			md.addIncantamento(incantamento);
			costo += ListinoPergamene.prezzo(incantamento);
		}
		md.setCostoAcquisto((int) Math.round(Math.max(costoBase / 2.0, costo)));
		return Artefatto.di(md);
	}

	static double probabilitaIncantato(int livello) {
		return Math.min(Costanti.ARTEFATTO_PROBABILITA_INCANTATO_MASSIMA,
				Costanti.ARTEFATTO_PROBABILITA_INCANTATO_PER_LIVELLO * (livello - 1));
	}

	/**
	 * Scudo, elmo, armatura e schinieri: +5% di PARATA per livello, la veste invece di RESISTENZA_MAGICA.
	 */
	private void completaProtezione(ArtefattoMD md, int livello) {
		md.setDescrizione(md.getTipo().getCardinalita() == TipoCardinalitaArtefatto.PAIO ? PROTEZIONE_PAIO : PROTEZIONE);
		md.addModificatore(md.getTipo() == TipoArtefatto.VESTE ? TipoAttributo.RESISTENZA_MAGICA : TipoAttributo.PARATA,
				TipoModificatore.AUMENTO_PERCENTUALE, 5.0 * livello, "");
	}

	/**
	 * Danno base {@link GeneratoreArtefatti#danniMediArma}, con uno scarto di ±1. Lo spadone fa il 50% in più (e costa il 50% in più),
	 * il bastone magico la metà ma aumenta la magia.
	 */
	private void completaArma(ArtefattoMD md, int livello) {
		int danni = GeneratoreArtefatti.danniMediArma(livello) + random.nextInt(3) - 1;
		if (md.getTipo() == TipoArtefatto.SPADONE) {
			danni = (int) Math.round(danni * Costanti.ARTEFATTO_MOLTIPLICATORE_DUE_MANI);
			md.setCostoAcquisto((int) Math.round(md.getCostoAcquisto() * Costanti.ARTEFATTO_MOLTIPLICATORE_DUE_MANI));
		}
		if (md.getTipo() == TipoArtefatto.BASTONE_MAGICO) {
			danni = danni / 2;
			md.setDescrizione(MAGIA);
			md.addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.AUMENTO_PERCENTUALE, 5.0 * livello, "");
		} else {
			md.setDescrizione(COMBATTIMENTO);
		}
		md.setDanni(danni);
	}

	@Override
	public Artefatto generaArtefattoCasuale(int livello) {
		return generaArtefatto(TIPI_CASUALI.get(random.nextInt(TIPI_CASUALI.size())), livello);
	}

	@Override
	public Artefatto generaIngrediente(int livello) {
		return generaIngrediente(INGREDIENTI.get(random.nextInt(INGREDIENTI.size())), livello);
	}

	/**
	 * Un ingrediente magico ha il livello di riferimento e tanti effetti quanto il livello, fino a
	 * INGREDIENTE_EFFETTI_MASSIMI, tutti diversi e della sua specialità, del grado adatto al livello
	 * ({@link GradoIncantamento#perIngrediente}): "la gemma maggiore del Drago e della Vipera". Nome ed effetti
	 * vengono dalla grammatica degli ingredienti, se c'è; i valori dal grado. Un modificatore è fisso o
	 * percentuale, un incantamento può avere solo la parte fissa, solo la percentuale o entrambe. Il prezzo si
	 * calcola con {@link ListinoPergamene}.
	 */
	private Artefatto generaIngrediente(TipoArtefatto tipo, int livello) {
		int livelloEffettivo = Math.max(1, livello);
		GradoIncantamento grado = GradoIncantamento.perIngrediente(livelloEffettivo);
		int numeroEffetti = Math.min(Costanti.INGREDIENTE_EFFETTI_MASSIMI, livelloEffettivo);
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(tipo);
		md.setLivello(livelloEffettivo);
		md.setPeso(PESI.get(tipo));
		String nome;
		if (grammaticaIngredienti != null && grammaticaIngredienti.supporta(tipo)) {
			GrammaticaArtefatti.Risultato risultato = grammaticaIngredienti.genera(tipo, numeroEffetti);
			nome = risultato.getNome();
			for (GrammaticaArtefatti.Modificatore modificatore : risultato.getModificatori()) {
				aggiungiModificatore(md, modificatore.getAttributo(), grado);
			}
			for (TipoDanno tipoDanno : risultato.getDanni()) {
				md.addIncantamento(generaIncantamento(grado, tipoDanno));
			}
		} else {
			nome = NOMI.get(tipo);
			List<TipoAttributo> attributi = new ArrayList<>(attributiDi(tipo));
			List<TipoDanno> danni = new ArrayList<>(danniDi(tipo));
			Collections.shuffle(attributi, random);
			Collections.shuffle(danni, random);
			for (int i = 0; i < numeroEffetti; i++) {
				if (i < danni.size()) {
					md.addIncantamento(generaIncantamento(grado, danni.get(i)));
				} else {
					aggiungiModificatore(md, attributi.get(i), grado);
				}
			}
		}
		md.setNome(conGrado(nome, tipo, grado));
		int effetti = md.getModificatori().size() + md.getIncantamenti().size();
		md.setDescrizione(effetti == 1 ? "che trasmette un effetto" : "che trasmette " + effetti + " effetti");
		md.setCostoAcquisto(ListinoPergamene.prezzo(md));
		return Artefatto.di(md);
	}

	/**
	 * Il grado nel nome, subito dopo il nome comune: "la gemma minore del Drago", "la gemma del Drago",
	 * "la gemma maggiore del Drago".
	 */
	private static String conGrado(String nome, TipoArtefatto tipo, GradoIncantamento grado) {
		if (grado == GradoIncantamento.MEDIO) {
			return nome;
		}
		String nomeComune = NOMI.get(tipo);
		return nomeComune + ' ' + grado.getNome() + nome.substring(nomeComune.length());
	}

	private Incantamento generaIncantamento(GradoIncantamento grado) {
		return generaIncantamento(grado, TIPI_DANNO_INCANTAMENTO.get(random.nextInt(TIPI_DANNO_INCANTAMENTO.size())));
	}

	private Incantamento generaIncantamento(GradoIncantamento grado, TipoDanno tipoDanno) {
		int bonusFisso = grado.getBonusFisso();
		double coefficiente = grado.getCoefficiente();
		switch (random.nextInt(3)) {
			case 0:
				coefficiente = 0;
				break;
			case 1:
				bonusFisso = 0;
				break;
			default:
				break;
		}
		return new Incantamento(tipoDanno.getNome() + ' ' + grado.getNome(), tipoDanno, bonusFisso, coefficiente);
	}

	private void aggiungiModificatore(ArtefattoMD md, TipoAttributo attributo, GradoIncantamento grado) {
		if (random.nextBoolean()) {
			md.addModificatore(attributo, TipoModificatore.AUMENTO_FISSO, grado.getGradino(), "");
		} else {
			md.addModificatore(attributo, TipoModificatore.AUMENTO_PERCENTUALE, Math.round(grado.getCoefficiente() * 100), "");
		}
	}

	private String scegli(String[] valori) {
		return valori[random.nextInt(valori.length)];
	}
}
