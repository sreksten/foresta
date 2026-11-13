package com.threeamigos.foresta.artefatti.leggendari;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.CostruttoreArtefatto;
import com.threeamigos.foresta.artefatti.ModificatoreDelTesto;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.tipi.TipoDanno;

import java.util.*;

/**
 * Un oggetto leggendario già pronto, con le sue proprietà e la sua leggenda, letto da una riga di leggendari.txt (che
 * ne descrive il formato): campi CHIAVE=valore separati da ";".
 * <pre>
 * TIPO=SCUDO;NOME=lo Scudo Fiscale;BREVE=lo Scudo Fiscale;DESCRIZIONE=che si fa fare sconti;LIVELLO=5;COSTO=60;PESO=3;
 * MOD=CONTRATTAZIONE +4;INCANTAMENTO=GELO 12 0.15 La battuta del cavolo;LEGGENDA=Conoscete lo Scudo Fiscale?;...
 * </pre>
 * La chiave lo identifica (nei set, nelle partite salvate, nei test): due righe non hanno mai la stessa, e non
 * cambia. Nomi e descrizioni invece sono solo testo, e si possono ritoccare.
 */
public final class OggettoLeggendario {

	// I campi della riga di leggendari.txt (vedi RigaDiCampi): quelli che compaiono una volta e quelli che si ripetono
	private static final List<String> CAMPI_SINGOLI = Arrays.asList("CHIAVE", "TIPO", "NOME", "BREVE", "DESCRIZIONE", "LIVELLO",
			"DANNI", "COSTO", "PESO", "SET", "PLURALE");
	private static final List<String> CAMPI_RIPETUTI = Arrays.asList("MOD", "INCANTAMENTO", "LEGGENDA");
	private static final int LEGGENDE_MINIME = 2;

	private final String riga;
	private final String chiave;
	private final TipoArtefatto tipo;
	private final String nome;
	private final String nomeBreve;
	private final boolean plurale;
	private final String descrizione;
	private final int livello;
	private final Integer danni;
	private final int costo;
	private final double peso;
	private final List<String> modificatori;
	private final List<String> incantamenti;
	private final List<String> leggenda;
	private final String set;

	private OggettoLeggendario(String riga) {
		RigaDiCampi campi = new RigaDiCampi(riga, CAMPI_SINGOLI, CAMPI_RIPETUTI);
		this.riga = riga;
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato(TipoArtefatto.class, "TIPO", campi.obbligatorio("TIPO"));
		nome = campi.obbligatorio("NOME");
		nomeBreve = campi.obbligatorio("BREVE");
		descrizione = campi.obbligatorio("DESCRIZIONE");
		livello = campi.intero("LIVELLO", campi.obbligatorio("LIVELLO"));
		costo = campi.intero("COSTO", campi.obbligatorio("COSTO"));
		peso = campi.decimale("PESO", campi.obbligatorio("PESO"));
		boolean arma = tipo.getCategoria() == CategoriaArtefatto.ARMA;
		if (arma != campi.ha("DANNI")) {
			throw new IllegalArgumentException(campi.conLaRiga("I danni ci sono per le armi, e solo per loro"));
		}
		danni = arma ? campi.intero("DANNI", campi.facoltativo("DANNI")) : null;
		modificatori = campi.ripetuti("MOD");
		incantamenti = campi.ripetuti("INCANTAMENTO");
		leggenda = campi.ripetuti("LEGGENDA");
		if (leggenda.size() < LEGGENDE_MINIME) {
			throw new IllegalArgumentException(campi.conLaRiga("La leggenda ha almeno " + LEGGENDE_MINIME + " battute"));
		}
		set = campi.facoltativo("SET");
		plurale = campi.ha("PLURALE");
		// Si costruisce una volta qui, così un modificatore o un incantamento sbagliato si scoprono subito (con la riga)
		try {
			costruisci(livello);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(campi.conLaRiga(e.getMessage()), e);
		}
	}

	/**
	 * L'oggetto leggendario di una riga di leggendari.txt (o di una riga salvata con {@link #getRiga()}).
	 */
	public static OggettoLeggendario da(String riga) {
		return new OggettoLeggendario(riga);
	}

	/**
	 * La riga da cui è stato letto, da salvare con la missione.
	 */
	public String getRiga() {
		return riga;
	}

	/**
	 * L'identificativo del leggendario: "SPADA_DI_ROMY_JONA".
	 */
	public String getChiave() {
		return chiave;
	}

	/**
	 * Il tipo di artefatto che il leggendario è.
	 */
	public TipoArtefatto getTipo() {
		return tipo;
	}

	/**
	 * Il nome completo, con l'articolo: "la Spada della Morte alata con rinterzo laterale".
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Il nome con cui se ne parla, con l'articolo: "la Spada della Morte".
	 */
	public String getNomeBreve() {
		return nomeBreve;
	}

	/**
	 * Se il nome è plurale ("gli Stivali del Fango", "the Boots of Mud"): lo dice il campo PLURALE della riga, perché in ogni
	 * lingua l'articolo è diverso e a volte non c'è, e da questo dipende il verbo di chi ne parla.
	 */
	public boolean isPlurale() {
		return plurale;
	}

	/**
	 * La leggenda, una battuta per elemento.
	 */
	public List<String> getLeggenda() {
		return leggenda;
	}

	/**
	 * Se la prima battuta della leggenda nomina già l'oggetto ("Conoscete la storia dello Scudo Fiscale?"), e quindi non
	 * serve un'introduzione. L'articolo del nome si toglie con le regole della lingua in uso (vedi Lingue).
	 */
	public boolean laLeggendaNominaLOggetto() {
		String nomeSenzaArticolo = Lingue.corrente().senzaArticolo(nomeBreve);
		return leggenda.get(0).toLowerCase(Locale.ROOT).contains(nomeSenzaArticolo.toLowerCase(Locale.ROOT));
	}

	/**
	 * La chiave del set di cui è un pezzo (vedi SetLeggendario), o null.
	 */
	public String getSet() {
		return set;
	}

	/**
	 * Il livello che un leggendario ha quando nasce: quello della riga, o {@code livelloDiRiferimento} se è più alto, così non
	 * resta sottodimensionato col procedere della partita (e non diventa più debole se lo si ottiene presto).
	 */
	private int livelloAllaNascita(int livelloDiRiferimento) {
		return Math.max(livello, livelloDiRiferimento);
	}

	/**
	 * Porta un leggendario già costruito al livello di riferimento, se questo è più alto del suo: per quelli che stanno in un tempio,
	 * costruiti quando la leggenda è stata raccontata, e che il gruppo ottiene molto dopo. Gli altri artefatti non si toccano.
	 *
	 * @param livelloDiRiferimento il livello a cui un artefatto deve essere oggi (il livello del mondo, per chi lo sa)
	 */
	public static void alLivello(Artefatto artefatto, int livelloDiRiferimento) {
		if (artefatto.getRarita() == TipoRaritaArtefatto.LEGGENDARIO && artefatto.getLivello() < livelloDiRiferimento) {
			artefatto.getModelloDati().setLivello(livelloDiRiferimento);
		}
	}

	/**
	 * Un artefatto nuovo, leggendario, con tutte le proprietà della riga, al {@code livelloDiRiferimento} se più alto di quello della
	 * riga (vedi {@link #livelloAllaNascita}).
	 *
	 * @param livelloDiRiferimento il livello a cui deve nascere, se più alto di quello della riga
	 */
	public Artefatto costruisci(int livelloDiRiferimento) {
		CostruttoreArtefatto.StepDanniBase passo = CostruttoreArtefatto.istanza()
				.setTipo(tipo)
				.setNome(nome)
				.setDescrizione(descrizione)
				.setLivello(livelloAllaNascita(livelloDiRiferimento));
		// Un'arma passa dai danni base, il resto va diretto al costo (il costruttore a passi lo permette solo così)
		CostruttoreArtefatto.StepModificatore costruttore = (danni != null ? passo.setDanniBase(danni).setCostoAcquisto(costo)
				: passo.setCostoAcquisto(costo)).setPeso(peso);
		for (String modificatore : modificatori) {
			costruttore = costruttore.setModificatore(ModificatoreDelTesto.da(modificatore));
		}
		Artefatto artefatto;
		if (incantamenti.isEmpty()) {
			artefatto = costruttore.costruisci();
		} else {
			CostruttoreArtefatto.StepIncantamento incantato = null;
			for (String incantamento : incantamenti) {
				String[] parti = incantamento.split("\\s+", 4);
				if (parti.length != 4) {
					throw new IllegalArgumentException("Un incantamento è tipo di danno, danno fisso, coefficiente e nome: " + incantamento);
				}
				TipoDanno tipoDanno = TipoDanno.valueOf(parti[0]);
				int fisso = Integer.parseInt(parti[1]);
				double coefficiente = Double.parseDouble(parti[2]);
				incantato = incantato == null ? costruttore.setIncantamento(parti[3], tipoDanno, fisso, coefficiente)
						: incantato.setIncantamento(parti[3], tipoDanno, fisso, coefficiente);
			}
			artefatto = incantato.costruisci();
		}
		artefatto.getModelloDati().setRarita(TipoRaritaArtefatto.LEGGENDARIO);
		if (set != null) {
			artefatto.getModelloDati().setPezzoDiSetLeggendario(set, chiave);
		}
		return artefatto;
	}
}
