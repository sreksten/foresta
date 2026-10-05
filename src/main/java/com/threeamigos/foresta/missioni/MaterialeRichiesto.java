package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tools.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Un materiale che un mandante chiede (vedi RichiestaDiMateriali), letto da una riga di missioni.txt: genere
 * ("F", "M", oppure "M/F" quando il singolare e il plurale ne hanno uno diverso, come orecchio e orecchie), singolare,
 * plurale, provenienza, quantità, prezzo per pezzo, la battuta del capo e la risposta del mandante,
 * separati da ";".
 * <pre>
 * F;radice di mandragola;radici di mandragola;LUOGHI RADURA BOSCO;3-5;5;E quando le tiriamo su non strillano?;Solo un po'.
 * F;scaglia di viverna;scaglie di viverna;NEMICI VIVERNA;2-3;12;Per farci cosa?;Uno scudo che non brucia.
 * </pre>
 * La provenienza è "LUOGHI" e dove si raccoglie, oppure "NEMICI" e i mostri a cui si prende sconfiggendoli.
 */
public final class MaterialeRichiesto {

	private static final String SEPARATORE = ";";
	private static final int CAMPI = 8;
	private static final String LUOGHI = "LUOGHI";
	private static final String NEMICI = "NEMICI";

	private final String riga;
	private final boolean singolareFemminile;
	// Il genere del plurale, che regge articoli, pronomi e accordi dei testi
	private final boolean femminile;
	private final String singolare;
	private final String plurale;
	private final List<TipoLocazione> luoghi;
	private final List<ClassePersonaggio> nemici;
	private final int quantitaMinima;
	private final int quantitaMassima;
	private final int prezzo;
	private final String battutaDelCapo;
	private final String rispostaDelMandante;

	private MaterialeRichiesto(String riga) {
		this.riga = Objects.requireNonNull(riga);
		String[] campi = riga.split(SEPARATORE, -1);
		if (campi.length != CAMPI) {
			throw new IllegalArgumentException("Un materiale richiesto ha " + CAMPI + " campi: " + riga);
		}
		String[] generi = campi[0].trim().split("/");
		for (String genere : generi) {
			if (!"F".equals(genere) && !"M".equals(genere)) {
				throw new IllegalArgumentException("Il genere è F, M o, singolare e plurale, M/F: " + riga);
			}
		}
		if (generi.length > 2) {
			throw new IllegalArgumentException("Al più due generi, singolare e plurale: " + riga);
		}
		singolareFemminile = "F".equals(generi[0]);
		femminile = "F".equals(generi[generi.length - 1]);
		singolare = campi[1].trim();
		plurale = campi[2].trim();
		String[] provenienza = campi[3].trim().split("\\s+");
		List<TipoLocazione> dove = new ArrayList<>();
		List<ClassePersonaggio> chi = new ArrayList<>();
		for (int i = 1; i < provenienza.length; i++) {
			if (LUOGHI.equals(provenienza[0])) {
				dove.add(TipoLocazione.valueOf(provenienza[i]));
			} else if (NEMICI.equals(provenienza[0])) {
				chi.add(ClassePersonaggio.valueOf(provenienza[i]));
			} else {
				throw new IllegalArgumentException("La provenienza è LUOGHI o NEMICI: " + riga);
			}
		}
		if (dove.isEmpty() && chi.isEmpty()) {
			throw new IllegalArgumentException("Manca da dove viene: " + riga);
		}
		luoghi = Collections.unmodifiableList(dove);
		nemici = Collections.unmodifiableList(chi);
		String[] quantita = campi[4].trim().split("-");
		quantitaMinima = Integer.parseInt(quantita[0]);
		quantitaMassima = Integer.parseInt(quantita[quantita.length - 1]);
		if (quantitaMinima < 1 || quantitaMassima < quantitaMinima) {
			throw new IllegalArgumentException("Quantità sbagliata: " + riga);
		}
		prezzo = Integer.parseInt(campi[5].trim());
		battutaDelCapo = campi[6].trim();
		rispostaDelMandante = campi[7].trim();
	}

	/**
	 * Il materiale di una riga di missioni.txt (o di una riga salvata con {@link #getRiga()}).
	 */
	public static MaterialeRichiesto da(String riga) {
		return new MaterialeRichiesto(riga);
	}

	/**
	 * Un ingrediente che non si paga a pezzo e non ha battute (quelli di un rito, vedi RitualeRichiesto): genere
	 * (F, M o M/F), singolare, plurale, provenienza ("LUOGHI BOSCO", "NEMICI SPETTRO") e quantità.
	 */
	static MaterialeRichiesto ingrediente(String genere, String singolare, String plurale, String provenienza, int quantita) {
		return new MaterialeRichiesto(String.join(SEPARATORE, genere, singolare, plurale, provenienza, quantita + "-" + quantita,
				"0", "", ""));
	}

	/**
	 * La riga da cui è stato letto, da salvare con la missione.
	 */
	public String getRiga() {
		return riga;
	}

	/**
	 * Gli oggetti da raccogliere, con quella chiave e in quella quantità: nei luoghi o, se è un trofeo, ai nemici.
	 */
	public OggettiDaRaccogliere daRaccogliere(String chiave, int quantita) {
		OggettiDaRaccogliere oggetti = OggettiDaRaccogliere.di(chiave, getNome(), quantita);
		if (isTrofeo()) {
			return oggetti.daiNemici(nemici.get(0), nemici.subList(1, nemici.size()).toArray(new ClassePersonaggio[0]))
					.alPiuPerLocazione(quantitaMassima);
		}
		return oggetti.in(luoghi.get(0), luoghi.subList(1, luoghi.size()).toArray(new TipoLocazione[0]))
				.conProbabilita(35)
				.alPiuPerLocazione(2);
	}

	public boolean isTrofeo() {
		return !nemici.isEmpty();
	}

	/**
	 * Se il plurale è femminile: è quello che usano i testi delle missioni, che parlano sempre di più pezzi.
	 */
	public boolean isFemminile() {
		return femminile;
	}

	/**
	 * Il nome, con gli articoli del genere del singolare e del plurale: "un orecchio di goblin", "le orecchie di
	 * goblin".
	 */
	public NomeOggetto getNome() {
		return new NomeOggetto(singolare, plurale,
				singolareFemminile ? Misc.UNA : Misc.UN, femminile ? Misc.ALCUNE : Misc.ALCUNI,
				singolareFemminile ? Misc.LA : Misc.IL, femminile ? Misc.LE : Misc.I);
	}

	public String getPlurale() {
		return plurale;
	}

	/**
	 * Il plurale con l'articolo: "le radici di mandragola", "i corni di minotauro".
	 */
	public String getPluraleConArticolo() {
		return (femminile ? Misc.LE : Misc.I) + plurale;
	}

	/**
	 * Quanti, in lettere, con il plurale: "quattro radici di mandragola".
	 */
	public String quanti(int quantita) {
		return (femminile ? Misc.getCardinaleF(quantita) : Misc.getCardinaleM(quantita)) + " " + plurale;
	}

	/**
	 * Il pronome per il plurale: "le" o "li".
	 */
	public String getPronome() {
		return femminile ? "le" : "li";
	}

	/**
	 * "tutte" o "tutti".
	 */
	public String getTutti() {
		return femminile ? "tutte" : "tutti";
	}

	/**
	 * "una per una" o "uno per uno".
	 */
	public String getUnoPerUno() {
		return femminile ? "una per una" : "uno per uno";
	}

	/**
	 * "Le mie" o "I miei".
	 */
	public String getPossessivo() {
		return femminile ? "Le mie" : "I miei";
	}

	public int getQuantitaMinima() {
		return quantitaMinima;
	}

	public int getQuantitaMassima() {
		return quantitaMassima;
	}

	public int getPrezzo() {
		return prezzo;
	}

	public List<TipoLocazione> getLuoghi() {
		return luoghi;
	}

	public List<ClassePersonaggio> getNemici() {
		return nemici;
	}

	/**
	 * Da dove viene, per i testi: "si trovano nelle radure e nei boschi", "si prendono sconfiggendo le Viverne".
	 */
	public String getDaDoveViene() {
		List<String> parti = new ArrayList<>();
		if (isTrofeo()) {
			for (ClassePersonaggio nemico : nemici) {
				Personaggio modello = nemico.getMoltiplicatoriDiClasse();
				parti.add(modello.getADP() + modello.getNomePlurale());
			}
			return "si prendono sconfiggendo " + elenco(parti);
		}
		for (TipoLocazione luogo : luoghi) {
			switch (luogo) {
				case RADURA:
					parti.add("nelle radure");
					break;
				case BOSCO:
					parti.add("nei boschi");
					break;
				case GROTTA:
					parti.add("nelle grotte");
					break;
				case ROVINE:
					parti.add("fra le rovine");
					break;
				case PALUDE:
					parti.add("nelle paludi");
					break;
				default:
					parti.add(Misc.conPreposizione("in", luogo.name().toLowerCase()));
					break;
			}
		}
		return "si trovano " + elenco(parti);
	}

	private static String elenco(List<String> parti) {
		if (parti.size() == 1) {
			return parti.get(0);
		}
		return String.join(", ", parti.subList(0, parti.size() - 1)) + " e " + parti.get(parti.size() - 1);
	}

	public String getBattutaDelCapo() {
		return battutaDelCapo;
	}

	public String getRispostaDelMandante() {
		return rispostaDelMandante;
	}
}
