package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.RaritaArtefatto;
import com.threeamigos.foresta.motore.modellodati.SupertipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.SupertipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Scheletro del generatore: nomi da piccole tabelle interne e valori da formule semplici,
 * tarate sugli artefatti dei templi (RegistroArtefatti). Tutto va bilanciato. Per i tipi che la
 * grammatica degli artefatti conosce (per ora la spada), una parte degli artefatti prende da lì
 * nome ed effetti (vedi {@link GrammaticaArtefatti}).
 */
public class GeneratoreArtefattiTabelle implements GeneratoreArtefatti {

	static final GeneratoreArtefatti ISTANZA = new GeneratoreArtefattiTabelle(new Random(), GrammaticaArtefatti.caricaOppureNull());

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
		nomeEPeso(TipoArtefatto.VESTE, "la veste", 1);
		nomeEPeso(TipoArtefatto.ANELLO, "l'anello", 0.1);
		nomeEPeso(TipoArtefatto.TALISMANO, "il talismano", 0.5);
		nomeEPeso(TipoArtefatto.NINNOLO, "il ninnolo", 0.2);
		nomeEPeso(TipoArtefatto.INCANTAMENTO, "la pergamena", 0.1);
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
	private static final String MAGIA = "che aumenta il potere magico";

	/**
	 * Accessori: l'attributo che migliorano e la descrizione che lo dice
	 */
	private static final Object[][] ACCESSORI = {
			{ TipoAttributo.CARISMA, "che aumenta il Carisma" },
			{ TipoAttributo.CORAGGIO, "che aumenta il Coraggio" },
			{ TipoAttributo.VALORE, "che aumenta il Valore" },
			{ TipoAttributo.FORTUNA, "che porta fortuna" },
			{ TipoAttributo.PERCEZIONE, "che acuisce i sensi" }
	};

	/**
	 * Attributi che una pergamena può migliorare
	 */
	private static final TipoAttributo[] ATTRIBUTI_PERGAMENA = {
			TipoAttributo.FORZA, TipoAttributo.DESTREZZA, TipoAttributo.COSTITUZIONE, TipoAttributo.INTELLIGENZA,
			TipoAttributo.SAGGEZZA, TipoAttributo.CARISMA, TipoAttributo.FORTUNA, TipoAttributo.PARATA,
			TipoAttributo.RESISTENZA_MAGICA, TipoAttributo.PRECISIONE, TipoAttributo.VELOCITA,
			TipoAttributo.CORAGGIO, TipoAttributo.VALORE
	};

	/**
	 * Gli incantamenti sono elementali o magici: il danno fisico lo dà già l'arma
	 */
	private static final List<TipoDanno> TIPI_DANNO_INCANTAMENTO = Arrays.stream(TipoDanno.values())
			.filter(t -> t.getSuperTipo() != SupertipoDanno.FISICO)
			.collect(Collectors.toList());

	private static final List<TipoArtefatto> TIPI_CASUALI = Arrays.stream(TipoArtefatto.values())
			.filter(t -> t != TipoArtefatto.INCANTAMENTO)
			.collect(Collectors.toList());

	private final Random random;
	/**
	 * Null se non si usa: allora tutti gli artefatti vengono dalle tabelle
	 */
	private final GrammaticaArtefatti grammatica;

	/**
	 * Un generatore che usa solo le tabelle.
	 */
	public GeneratoreArtefattiTabelle(Random random) {
		this(random, null);
	}

	GeneratoreArtefattiTabelle(Random random, GrammaticaArtefatti grammatica) {
		this.random = random;
		this.grammatica = grammatica;
	}

	private static void nomeEPeso(TipoArtefatto tipo, String nome, double peso) {
		NOMI.put(tipo, nome);
		PESI.put(tipo, peso);
	}

	@Override
	public Artefatto generaArtefatto(TipoArtefatto tipo, int livello) {
		if (tipo == TipoArtefatto.INCANTAMENTO) {
			return generaPergamena(livello);
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
			md.setRarita(RaritaArtefatto.RARO);
		}
		switch (tipo.getSupertipo()) {
			case ARMA:
				completaArma(md, livelloEffettivo);
				break;
			case SCUDO:
			case ELMO:
			case ARMATURA:
				md.setDescrizione(PROTEZIONE);
				md.addModificatore(tipo == TipoArtefatto.VESTE ? TipoAttributo.RESISTENZA_MAGICA : TipoAttributo.PARATA,
						TipoModificatore.AUMENTO_PERCENTUALE, 5.0 * livelloEffettivo, "");
				break;
			default:
				if (tipo == TipoArtefatto.LIBRO_MAGICO) {
					md.setDescrizione(MAGIA);
					md.addModificatore(TipoAttributo.MAGIA, TipoModificatore.AUMENTO_PERCENTUALE, 5.0 * livelloEffettivo, "");
				} else {
					Object[] accessorio = ACCESSORI[random.nextInt(ACCESSORI.length)];
					md.setDescrizione((String) accessorio[1]);
					md.addModificatore((TipoAttributo) accessorio[0], TipoModificatore.AUMENTO_FISSO, livelloEffettivo, "");
				}
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
	 * l'artefatto meno uno, lasciando come {@link #incantaForse} un posto libero per la fusione. Gli effetti sono
	 * del grado del livello: un modificatore vale la sua intensità per il gradino, un incantamento ha sia la parte
	 * fissa sia la percentuale. Il prezzo sale per i modificatori positivi e gli incantamenti e scende per i
	 * modificatori negativi, ma non sotto la metà del prezzo base. Niente incantaForse: un incantamento a caso
	 * contraddirebbe il nome.
	 */
	private Artefatto generaDaGrammatica(ArtefattoMD md, int livello) {
		TipoArtefatto tipo = md.getTipo();
		if (Artefatto.di(md).isIncantabile() && random.nextDouble() < Costanti.ARTEFATTO_PROBABILITA_RARO) {
			md.setRarita(RaritaArtefatto.RARO);
		}
		md.setNome(NOMI.get(tipo));
		if (tipo.getSupertipo() == SupertipoArtefatto.ARMA) {
			completaArma(md, livello);
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
			md.addModificatore(TipoAttributo.MAGIA, TipoModificatore.AUMENTO_PERCENTUALE, 5.0 * livello, "");
		} else {
			md.setDescrizione(COMBATTIMENTO);
		}
		md.setDanni(danni);
	}

	@Override
	public Artefatto generaArtefattoCasuale(int livello) {
		return generaArtefatto(TIPI_CASUALI.get(random.nextInt(TIPI_CASUALI.size())), livello);
	}

	/**
	 * La pergamena ha un livello da 1 a 3 (il livello di riferimento, limitato a 3) e tanti effetti quanto
	 * il suo livello, ciascuno a caso un incantamento o un modificatore di attributo, tutti del grado adatto
	 * al livello di riferimento. Un incantamento può avere solo la parte fissa, solo la percentuale o entrambe.
	 * Il prezzo si calcola con {@link ListinoPergamene}.
	 */
	@Override
	public Artefatto generaPergamena(int livello) {
		int livelloEffettivo = Math.max(1, livello);
		GradoIncantamento grado = GradoIncantamento.perLivello(livelloEffettivo);
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(TipoArtefatto.INCANTAMENTO);
		md.setNome(NOMI.get(TipoArtefatto.INCANTAMENTO) + ' ' + grado.getNome());
		int livelloPergamena = Math.min(Costanti.PERGAMENA_LIVELLO_MASSIMO, livelloEffettivo);
		md.setLivello(livelloPergamena);
		md.setPeso(PESI.get(TipoArtefatto.INCANTAMENTO));
		int numeroEffetti = livelloPergamena;
		for (int i = 0; i < numeroEffetti; i++) {
			if (random.nextBoolean()) {
				md.addIncantamento(generaIncantamento(grado));
			} else {
				aggiungiModificatore(md, grado);
			}
		}
		md.setDescrizione(numeroEffetti == 1 ? "che trasmette un effetto" : "che trasmette " + numeroEffetti + " effetti");
		md.setCostoAcquisto(ListinoPergamene.prezzo(md));
		return Artefatto.di(md);
	}

	private Incantamento generaIncantamento(GradoIncantamento grado) {
		TipoDanno tipoDanno = TIPI_DANNO_INCANTAMENTO.get(random.nextInt(TIPI_DANNO_INCANTAMENTO.size()));
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

	private void aggiungiModificatore(ArtefattoMD md, GradoIncantamento grado) {
		TipoAttributo attributo = ATTRIBUTI_PERGAMENA[random.nextInt(ATTRIBUTI_PERGAMENA.length)];
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
