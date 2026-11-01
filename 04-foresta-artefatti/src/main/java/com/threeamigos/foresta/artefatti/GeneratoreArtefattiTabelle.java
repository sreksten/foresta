package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.tipi.*;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.CategoriaDanno;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Nome;
import com.threeamigos.foresta.testi.Testi;

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

	private static final String FILE_DEI_TESTI = "generatore_artefatti";
	private static final String FILE_DEI_NOMI = "nomi_artefatti";
	private static final Map<TipoArtefatto, Double> PESI = new EnumMap<>(TipoArtefatto.class);

	static {
		peso(TipoArtefatto.SPADA, 1);
		peso(TipoArtefatto.SPADONE, 3);
		peso(TipoArtefatto.FALCE, 3);
		peso(TipoArtefatto.MAZZA, 2);
		peso(TipoArtefatto.ASCIA, 2);
		peso(TipoArtefatto.LANCIA, 2);
		peso(TipoArtefatto.BASTONE_MAGICO, 1);
		peso(TipoArtefatto.LIBRO_MAGICO, 1);
		peso(TipoArtefatto.SCUDO, 2);
		peso(TipoArtefatto.ELMO, 1);
		peso(TipoArtefatto.ARMATURA, 3);
		peso(TipoArtefatto.SCHINIERI, 2);
		peso(TipoArtefatto.VESTE, 1);
		peso(TipoArtefatto.MASCHERA, 0.5);
		peso(TipoArtefatto.ANELLO, 0.1);
		peso(TipoArtefatto.TALISMANO, 0.5);
		peso(TipoArtefatto.NINNOLO, 0.2);
		peso(TipoArtefatto.PERGAMENA, 0.1);
		peso(TipoArtefatto.GEMMA, 0.1);
		peso(TipoArtefatto.MONILE, 0.2);
		peso(TipoArtefatto.GINGILLO, 0.2);
		peso(TipoArtefatto.SIGILLO, 0.1);
		peso(TipoArtefatto.AGGIORNAMENTO, 0.1);
		peso(TipoArtefatto.RIPROGRAMMAZIONE, 0.5);
	}

	/**
	 * Gli attributi che un accessorio può migliorare, con la descrizione che lo dice (voce {@code DESCRIZIONE.ACCESSORIO.<ATTRIBUTO>}
	 * dei testi del generatore)
	 */
	private static final TipoAttributo[] ACCESSORI = {
			TipoAttributo.CARISMA, TipoAttributo.CORAGGIO, TipoAttributo.VALORE, TipoAttributo.FORTUNA, TipoAttributo.PERCEZIONE
	};

	/**
	 * Gli incantamenti sono elementali o magici: il danno fisico lo dà già l'arma
	 */
	private static final List<TipoDanno> TIPI_DANNO_INCANTAMENTO = Arrays.stream(TipoDanno.values())
			.filter(t -> t.getCategoriaDanno() != CategoriaDanno.FISICO)
			.collect(Collectors.toList());

	private static final List<TipoArtefatto> TIPI_CASUALI = Arrays.stream(TipoArtefatto.values())
			.filter(t -> !t.isIngrediente() && t.isGenerabileACaso())
			.collect(Collectors.toList());

	private static final List<TipoArtefatto> INGREDIENTI = Arrays.stream(TipoArtefatto.values())
			.filter(t -> t.isIngrediente() && !t.isAggiornamento() && !t.isRiprogrammazione())
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
				.filter(t -> t.getCategoriaDanno() == CategoriaDanno.ELEMENTALE).collect(Collectors.toList()));
		DANNI_DEGLI_INGREDIENTI.put(TipoArtefatto.MONILE, TIPI_DANNO_INCANTAMENTO.stream()
				.filter(t -> t.getCategoriaDanno() == CategoriaDanno.MAGICO).collect(Collectors.toList()));
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

	/**
	 * Il peso di un tipo di artefatto.
	 */
	static double peso(TipoArtefatto tipo) {
		return PESI.get(tipo);
	}

	private static void peso(TipoArtefatto tipo, double peso) {
		PESI.put(tipo, peso);
	}

	// I testi del generatore stanno in generatore_artefatti_<lingua>.properties
	private static String testo(String chiave, Object... valori) {
		return Testi.formatta(FILE_DEI_TESTI, chiave, valori);
	}

	/**
	 * Il nome comune del tipo con l'articolo determinativo, nella lingua in uso: "la spada", "l'ascia", "gli schinieri".
	 */
	static String nomeComune(TipoArtefatto tipo) {
		Nome nome = Nome.di(FILE_DEI_NOMI, tipo);
		return Lingue.corrente().articoloDeterminativo(nome, false) + nome.getSingolare();
	}

	// Il nome comune, con l'articolo e un complemento: "la spada di ferro", "the iron sword"
	private String nomeConComplemento(TipoArtefatto tipo) {
		Nome nome = Nome.di(FILE_DEI_NOMI, tipo);
		List<String> complementi = Testi.serie(FILE_DEI_TESTI, "COMPLEMENTO");
		return testo("NOME_CON_COMPLEMENTO", Lingue.corrente().articoloDeterminativo(nome, false), nome.getSingolare(),
				complementi.get(random.nextInt(complementi.size())));
	}

	@Override
	public Artefatto generaArtefatto(TipoArtefatto tipo, int livello) {
		if (tipo.isAggiornamento()) {
			return generaAggiornamento(livello);
		}
		if (tipo.isRiprogrammazione()) {
			return generaRiprogrammazione(livello);
		}
		if (tipo.isIngrediente()) {
			return generaIngrediente(tipo, livello);
		}
		int livelloEffettivo = Math.max(1, livello);
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(tipo);
		md.setNome(nomeConComplemento(tipo));
		md.setLivello(livelloEffettivo);
		md.setPeso(PESI.get(tipo));
		md.setCostoAcquisto(5 + 5 * livelloEffettivo);
		if (grammatica != null && grammatica.supporta(tipo) && random.nextDouble() < CostantiArtefatti.ARTEFATTO_PROBABILITA_DA_GRAMMATICA) {
			return generaDaGrammatica(md, livelloEffettivo);
		}
		if (random.nextDouble() < CostantiArtefatti.ARTEFATTO_PROBABILITA_NOME_PROPRIO) {
			md.setNomeProprio(scegli(Testi.serie(FILE_DEI_TESTI, "NOME_PROPRIO")));
		}
		// La rarità conta solo per il numero di effetti, quindi solo per gli artefatti incantabili
		if (Artefatto.di(md).isIncantabile() && random.nextDouble() < CostantiArtefatti.ARTEFATTO_PROBABILITA_RARO) {
			md.setRarita(TipoRaritaArtefatto.RARO);
		}
		switch (tipo.getCategoria()) {
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
				md.setDescrizione(testo("DESCRIZIONE.MAGIA"));
				md.addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 5.0 * livelloEffettivo, "");
				break;
			default:
				TipoAttributo accessorio = ACCESSORI[random.nextInt(ACCESSORI.length)];
				md.setDescrizione(testo("DESCRIZIONE.ACCESSORIO." + accessorio.name()));
				md.addModificatore(accessorio, TipoModificatore.VARIAZIONE_FISSA, livelloEffettivo, "");
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
		int massimo = Math.min(CostantiArtefatti.ARTEFATTO_MASSIMO_INCANTAMENTI_IN_NASCITA, postiLiberi - 1);
		if (massimo <= 0 || random.nextDouble() >= probabilitaIncantato(livello)) {
			return;
		}
		GradoIncantamento grado = GradoIncantamento.perLivello(livello);
		int numero = 1 + random.nextInt(massimo);
		for (int i = 0; i < numero; i++) {
			IncantamentoMD incantamento = generaIncantamento(grado);
			md.addIncantamento(incantamento);
			md.setCostoAcquisto(md.getCostoAcquisto() + (int) Math.round(ListinoPergamene.prezzo(incantamento, livello)));
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
		// La grammatica conosce solo armi e pezzi difensivi, che sono tutti incantabili
		if (random.nextDouble() < CostantiArtefatti.ARTEFATTO_PROBABILITA_RARO) {
			md.setRarita(TipoRaritaArtefatto.RARO);
		}
		md.setNome(nomeComune(tipo));
		if (tipo.getCategoria() == CategoriaArtefatto.ARMA) {
			completaArma(md, livello);
		} else {
			completaProtezione(md, livello);
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
			ModificatoreAttributoMD aggiunto = new ModificatoreAttributoMD(modificatore.getAttributo(), TipoModificatore.VARIAZIONE_FISSA,
					modificatore.getIntensita() * grado.getGradino(), "");
			md.addModificatore(aggiunto);
			costo += Math.signum(modificatore.getIntensita()) * ListinoPergamene.prezzo(aggiunto);
		}
		for (TipoDanno tipoDanno : risultato.getDanni()) {
			IncantamentoMD incantamento = new IncantamentoMD(tipoDanno.getNome() + ' ' + grado.getNome(), tipoDanno,
					grado.getBonusFisso(), grado.getCoefficiente());
			md.addIncantamento(incantamento);
			costo += ListinoPergamene.prezzo(incantamento, livello);
		}
		md.setCostoAcquisto((int) Math.round(Math.max(costoBase / 2.0, costo)));
		return Artefatto.di(md);
	}

	static double probabilitaIncantato(int livello) {
		return Math.min(CostantiArtefatti.ARTEFATTO_PROBABILITA_INCANTATO_MASSIMA,
				CostantiArtefatti.ARTEFATTO_PROBABILITA_INCANTATO_PER_LIVELLO * (livello - 1));
	}

	/**
	 * Scudo, elmo, maschera, armatura e schinieri: +5% di PARATA per livello, la veste invece di RESISTENZA_MAGICA.
	 */
	private void completaProtezione(ArtefattoMD md, int livello) {
		md.setDescrizione(testo(md.getTipo().getCardinalita() == TipoCardinalitaArtefatto.PAIO ? "DESCRIZIONE.PROTEZIONE_PAIO" : "DESCRIZIONE.PROTEZIONE"));
		md.addModificatore(md.getTipo() == TipoArtefatto.VESTE ? TipoAttributo.RESISTENZA_MAGICA : TipoAttributo.PARATA,
				TipoModificatore.VARIAZIONE_PERCENTUALE, 5.0 * livello, "");
	}

	/**
	 * Danno base {@link GeneratoreArtefatti#danniMediArma}, con uno scarto di ±1. Lo spadone fa il 50% in più (e costa il 50% in più),
	 * il bastone magico la metà ma aumenta la magia.
	 */
	private void completaArma(ArtefattoMD md, int livello) {
		int danni = GeneratoreArtefatti.danniMediArma(livello) + random.nextInt(3) - 1;
		if (md.getTipo() == TipoArtefatto.SPADONE) {
			danni = (int) Math.round(danni * CostantiArtefatti.ARTEFATTO_MOLTIPLICATORE_DUE_MANI);
			md.setCostoAcquisto((int) Math.round(md.getCostoAcquisto() * CostantiArtefatti.ARTEFATTO_MOLTIPLICATORE_DUE_MANI));
		}
		if (md.getTipo() == TipoArtefatto.BASTONE_MAGICO) {
			danni = danni / 2;
			md.setDescrizione(testo("DESCRIZIONE.MAGIA"));
			md.addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 5.0 * livello, "");
		} else {
			md.setDescrizione(testo("DESCRIZIONE.COMBATTIMENTO"));
		}
		md.setDanni(danni);
	}

	/**
	 * Un libro che riprogramma gli artefatti (vedi TipoArtefatto.RIPROGRAMMAZIONE): senza effetti, e non si compra né si vende. Il
	 * nome e la descrizione sono di ripiego: li cambia chi lo costruisce davvero (vedi LibroTecnico).
	 */
	private Artefatto generaRiprogrammazione(int livello) {
		int livelloEffettivo = Math.max(1, livello);
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(TipoArtefatto.RIPROGRAMMAZIONE);
		md.setNome(nomeComune(TipoArtefatto.RIPROGRAMMAZIONE));
		md.setDescrizione(testo("DESCRIZIONE.RIPROGRAMMAZIONE"));
		md.setLivello(livelloEffettivo);
		md.setPeso(PESI.get(TipoArtefatto.RIPROGRAMMAZIONE));
		md.setCostoAcquisto(CostantiArtefatti.RIPROGRAMMAZIONE_VALORE_PER_LIVELLO * livelloEffettivo);
		return Artefatto.di(md);
	}

	@Override
	public Artefatto generaArtefattoCasuale(int livello) {
		return generaArtefatto(TIPI_CASUALI.get(random.nextInt(TIPI_CASUALI.size())), livello);
	}

	@Override
	public Artefatto generaAggiornamento(int livello) {
		int livelloEffettivo = Math.max(1, livello);
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(TipoArtefatto.AGGIORNAMENTO);
		md.setNome(ProduttoreDiArtefattiCasuale.nomeAggiornamento());
		md.setDescrizione(testo("DESCRIZIONE.AGGIORNAMENTO"));
		md.setLivello(livelloEffettivo);
		md.setPeso(PESI.get(TipoArtefatto.AGGIORNAMENTO));
		// Non si compra né si vende: il costo dice solo quanto vale, per i punteggi e per l'inventario
		md.setCostoAcquisto(CostantiArtefatti.AGGIORNAMENTO_VALORE_PER_LIVELLO * livelloEffettivo);
		return Artefatto.di(md);
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
		int numeroEffetti = Math.min(CostantiArtefatti.INGREDIENTE_EFFETTI_MASSIMI, livelloEffettivo);
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
			nome = nomeComune(tipo);
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
		md.setDescrizione(effetti == 1 ? testo("DESCRIZIONE.INGREDIENTE_UNO") : testo("DESCRIZIONE.INGREDIENTE_N", effetti));
		md.setCostoAcquisto(ListinoPergamene.prezzo(md));
		return Artefatto.di(md);
	}

	/**
	 * Il grado nel nome, subito dopo il nome comune: "la gemma minore del Drago", "la gemma del Drago",
	 * "la gemma maggiore del Drago". Se il nome (uscito dalla grammatica) non comincia con il nome comune, il grado
	 * va in fondo: "la pietra del Drago minore".
	 */
	static String conGrado(String nome, TipoArtefatto tipo, GradoIncantamento grado) {
		if (grado == GradoIncantamento.MEDIO) {
			return nome;
		}
		Nome nomeDelTipo = Nome.di(FILE_DEI_NOMI, tipo);
		String articolo = Lingue.corrente().articoloDeterminativo(nomeDelTipo, false);
		String nomeComune = articolo + nomeDelTipo.getSingolare();
		if (!nome.startsWith(nomeComune)) {
			return testo("NOME_CON_GRADO_IN_FONDO", nome, grado.getNome());
		}
		return testo("NOME_CON_GRADO", articolo, nomeDelTipo.getSingolare(), grado.getNome(), nome.substring(nomeComune.length()));
	}

	private IncantamentoMD generaIncantamento(GradoIncantamento grado) {
		return generaIncantamento(grado, TIPI_DANNO_INCANTAMENTO.get(random.nextInt(TIPI_DANNO_INCANTAMENTO.size())));
	}

	private IncantamentoMD generaIncantamento(GradoIncantamento grado, TipoDanno tipoDanno) {
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
		return new IncantamentoMD(tipoDanno.getNome() + ' ' + grado.getNome(), tipoDanno, bonusFisso, coefficiente);
	}

	private void aggiungiModificatore(ArtefattoMD md, TipoAttributo attributo, GradoIncantamento grado) {
		if (random.nextBoolean()) {
			md.addModificatore(attributo, TipoModificatore.VARIAZIONE_FISSA, grado.getGradino(), "");
		} else {
			md.addModificatore(attributo, TipoModificatore.VARIAZIONE_PERCENTUALE, Math.round(grado.getCoefficiente() * 100), "");
		}
	}

	private String scegli(List<String> valori) {
		return valori.get(random.nextInt(valori.size()));
	}
}
