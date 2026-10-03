package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.tools.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Un ingrediente che chiede l'alchimista (vedi LAlchimista), letto da una riga di missioni.txt: genere, singolare,
 * plurale, dove si trova, la battuta del capo e la risposta dell'alchimista, separati da ";".
 * <pre>
 * F;radice di mandragola;radici di mandragola;RADURA BOSCO;E quando le tiriamo su non strillano?;Solo un po'.
 * </pre>
 */
public final class IngredienteAlchemico {

	private static final String SEPARATORE = ";";
	private static final int CAMPI = 6;

	private final String riga;
	private final boolean femminile;
	private final String singolare;
	private final String plurale;
	private final List<ClassiLocazione> luoghi;
	private final String battutaDelCapo;
	private final String rispostaDellAlchimista;

	private IngredienteAlchemico(String riga) {
		this.riga = Objects.requireNonNull(riga);
		String[] campi = riga.split(SEPARATORE, -1);
		if (campi.length != CAMPI) {
			throw new IllegalArgumentException("Un ingrediente alchemico ha " + CAMPI + " campi: " + riga);
		}
		if (!"F".equals(campi[0]) && !"M".equals(campi[0])) {
			throw new IllegalArgumentException("Il genere è F o M: " + riga);
		}
		femminile = "F".equals(campi[0]);
		singolare = campi[1].trim();
		plurale = campi[2].trim();
		List<ClassiLocazione> dove = new ArrayList<>();
		for (String luogo : campi[3].trim().split("\\s+")) {
			dove.add(ClassiLocazione.valueOf(luogo));
		}
		luoghi = Collections.unmodifiableList(dove);
		battutaDelCapo = campi[4].trim();
		rispostaDellAlchimista = campi[5].trim();
	}

	/**
	 * L'ingrediente di una riga di missioni.txt (o di una riga salvata con {@link #getRiga()}).
	 */
	public static IngredienteAlchemico da(String riga) {
		return new IngredienteAlchemico(riga);
	}

	/**
	 * La riga da cui è stato letto, da salvare con la missione.
	 */
	public String getRiga() {
		return riga;
	}

	public boolean isFemminile() {
		return femminile;
	}

	public NomeOggetto getNome() {
		return femminile ? NomeOggetto.femminile(singolare, plurale) : NomeOggetto.maschile(singolare, plurale);
	}

	public String getPlurale() {
		return plurale;
	}

	/**
	 * Il plurale con l'articolo: "le radici di mandragola", "i fiori di aconito".
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
	 * "Le mie" o "I miei".
	 */
	public String getPossessivo() {
		return femminile ? "Le mie" : "I miei";
	}

	public List<ClassiLocazione> getLuoghi() {
		return luoghi;
	}

	/**
	 * Dove si trova, per i testi: "nelle radure e nei boschi".
	 */
	public String getDoveSiTrova() {
		List<String> dove = new ArrayList<>();
		for (ClassiLocazione luogo : luoghi) {
			switch (luogo) {
				case RADURA:
					dove.add("nelle radure");
					break;
				case BOSCO:
					dove.add("nei boschi");
					break;
				case GROTTA:
					dove.add("nelle grotte");
					break;
				case ROVINE:
					dove.add("fra le rovine");
					break;
				default:
					dove.add(Misc.conPreposizione("in", luogo.name().toLowerCase()));
					break;
			}
		}
		if (dove.size() == 1) {
			return dove.get(0);
		}
		return String.join(", ", dove.subList(0, dove.size() - 1)) + " e " + dove.get(dove.size() - 1);
	}

	public String getBattutaDelCapo() {
		return battutaDelCapo;
	}

	public String getRispostaDellAlchimista() {
		return rispostaDellAlchimista;
	}
}
