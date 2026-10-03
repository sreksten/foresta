package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.tipi.SupertipoArtefatto;
import com.threeamigos.foresta.motore.tipi.TipoArtefatto;
import com.threeamigos.foresta.motore.tipi.TipoAttributo;
import com.threeamigos.foresta.motore.tipi.TipoDanno;
import com.threeamigos.foresta.motore.tipi.TipoModificatore;
import com.threeamigos.foresta.motore.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.tools.CostruttoreArtefatto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Un oggetto leggendario già pronto, con le sue proprietà e la sua leggenda, letto da una riga di leggendari.txt (che
 * ne descrive il formato): campi CHIAVE=valore separati da ";".
 * <pre>
 * TIPO=SCUDO;NOME=lo Scudo Fiscale;BREVE=lo Scudo Fiscale;DESCRIZIONE=che si fa fare sconti;LIVELLO=5;COSTO=60;PESO=3;
 * MOD=CONTRATTAZIONE +4;INCANTAMENTO=GELO 12 0.15 La battuta del cavolo;LEGGENDA=Conoscete lo Scudo Fiscale?;...
 * </pre>
 * Il nome breve lo identifica: due righe non hanno mai lo stesso.
 */
public final class OggettoLeggendario {

	private static final String SEPARATORE = ";";
	private static final String UGUALE = "=";
	private static final String PERCENTUALE = "%";
	private static final int LEGGENDE_MINIME = 2;

	private final String riga;
	private final TipoArtefatto tipo;
	private final String nome;
	private final String nomeBreve;
	private final String descrizione;
	private final int livello;
	private final Integer danni;
	private final int costo;
	private final double peso;
	private final List<String> modificatori;
	private final List<String> incantamenti;
	private final List<String> leggenda;
	private final String guardiani;

	private OggettoLeggendario(String riga) {
		this.riga = Objects.requireNonNull(riga);
		Map<String, String> campi = new HashMap<>();
		Map<String, List<String>> ripetuti = new HashMap<>();
		for (String campo : riga.split(SEPARATORE)) {
			if (campo.trim().isEmpty()) {
				continue;
			}
			int uguale = campo.indexOf(UGUALE);
			if (uguale < 0) {
				throw new IllegalArgumentException("Un campo è CHIAVE=valore: " + campo + " in " + riga);
			}
			String chiave = campo.substring(0, uguale).trim();
			String valore = campo.substring(uguale + 1).trim();
			switch (chiave) {
				case "MOD":
				case "INCANTAMENTO":
				case "LEGGENDA":
					ripetuti.computeIfAbsent(chiave, k -> new ArrayList<>()).add(valore);
					break;
				case "TIPO":
				case "NOME":
				case "BREVE":
				case "DESCRIZIONE":
				case "LIVELLO":
				case "DANNI":
				case "COSTO":
				case "PESO":
				case "GUARDIANI":
					if (campi.put(chiave, valore) != null) {
						throw new IllegalArgumentException("Campo ripetuto: " + chiave + " in " + riga);
					}
					break;
				default:
					throw new IllegalArgumentException("Campo sconosciuto: " + chiave + " in " + riga);
			}
		}
		tipo = TipoArtefatto.valueOf(obbligatorio(campi, "TIPO"));
		nome = obbligatorio(campi, "NOME");
		nomeBreve = obbligatorio(campi, "BREVE");
		descrizione = obbligatorio(campi, "DESCRIZIONE");
		livello = Integer.parseInt(obbligatorio(campi, "LIVELLO"));
		costo = Integer.parseInt(obbligatorio(campi, "COSTO"));
		peso = Double.parseDouble(obbligatorio(campi, "PESO"));
		boolean arma = tipo.getSupertipo() == SupertipoArtefatto.ARMA;
		if (arma != campi.containsKey("DANNI")) {
			throw new IllegalArgumentException("I danni ci sono per le armi, e solo per loro: " + riga);
		}
		danni = arma ? Integer.parseInt(campi.get("DANNI")) : null;
		modificatori = Collections.unmodifiableList(ripetuti.getOrDefault("MOD", Collections.emptyList()));
		incantamenti = Collections.unmodifiableList(ripetuti.getOrDefault("INCANTAMENTO", Collections.emptyList()));
		leggenda = Collections.unmodifiableList(ripetuti.getOrDefault("LEGGENDA", Collections.emptyList()));
		if (leggenda.size() < LEGGENDE_MINIME) {
			throw new IllegalArgumentException("La leggenda ha almeno " + LEGGENDE_MINIME + " battute: " + riga);
		}
		guardiani = campi.get("GUARDIANI");
		// Si costruisce una volta qui, così un modificatore, un incantamento o i guardiani sbagliati si scoprono subito
		costruisci();
		getGuardiani();
	}

	private static String obbligatorio(Map<String, String> campi, String chiave) {
		String valore = campi.get(chiave);
		if (valore == null || valore.isEmpty()) {
			throw new IllegalArgumentException("Manca il campo " + chiave);
		}
		return valore;
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
	 * Il nome con cui se ne parla, con l'articolo: "la Spada della Morte". Lo identifica.
	 */
	public String getNomeBreve() {
		return nomeBreve;
	}

	/**
	 * La leggenda, una battuta per elemento.
	 */
	public List<String> getLeggenda() {
		return leggenda;
	}

	/**
	 * Chi lo custodisce, se la riga lo dice; altrimenti lo decide la missione.
	 */
	public Optional<IncontroDiMissione> getGuardiani() {
		if (guardiani == null) {
			return Optional.empty();
		}
		String[] parti = guardiani.split("\\s+");
		if (parti.length != 2) {
			throw new IllegalArgumentException("I guardiani sono classe e numero, \"VIVERNA 5\": " + guardiani);
		}
		return Optional.of(IncontroDiMissione.di(ClassePersonaggio.valueOf(parti[0]), Integer.parseInt(parti[1])));
	}

	/**
	 * Un artefatto nuovo, leggendario, con tutte le proprietà della riga.
	 */
	public Artefatto costruisci() {
		CostruttoreArtefatto.StepDanniBase passo = CostruttoreArtefatto.istanza()
				.setTipo(tipo)
				.setNome(nome)
				.setDescrizione(descrizione)
				.setLivello(livello);
		CostruttoreArtefatto.StepModificatore costruttore = (danni != null ? passo.setDanniBase(danni).setCostoAcquisto(costo)
				: passo.setCostoAcquisto(costo)).setPeso(peso);
		for (String modificatore : modificatori) {
			String[] parti = modificatore.split("\\s+");
			if (parti.length != 2) {
				throw new IllegalArgumentException("Un modificatore è attributo e valore, \"FORZA +100%\": " + modificatore);
			}
			boolean percentuale = parti[1].endsWith(PERCENTUALE);
			double valore = Double.parseDouble(percentuale ? parti[1].substring(0, parti[1].length() - 1) : parti[1]);
			costruttore = costruttore.setModificatore(TipoAttributo.valueOf(parti[0]),
					percentuale ? TipoModificatore.AUMENTO_PERCENTUALE : TipoModificatore.AUMENTO_FISSO, valore);
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
		return artefatto;
	}
}
