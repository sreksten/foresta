package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Un modificatore di un artefatto scritto in una riga di testo (MOD= di leggendari.txt):
 * <pre>
 * ATTRIBUTO VALORE [@BERSAGLIO] [DURATA]
 * FORZA +100%                     variazione percentuale, permanente, su chi lo porta
 * VALORE +2                       variazione fissa
 * FORTUNA =0 @BERSAGLIO 3TURNI    porta a 0 la fortuna di chi viene colpito per 3 turni
 * CORAGGIO -20% 3 LOCAZIONI       per 3 locazioni (lo spazio fra numero e unità è facoltativo)
 * </pre>
 * Il valore con "=" porta l'attributo a quel valore (QUANTITA_ASSOLUTA), con "%" è una variazione percentuale,
 * altrimenti è fissa. Le unità sono TURNI e LOCAZIONI (anche al singolare).
 */
public final class ModificatoreDelTesto {

	private static final String BERSAGLIO = "@BERSAGLIO";
	private static final Pattern DURATA = Pattern.compile("(\\d+)\\s*(TURNI|TURNO|LOCAZIONI|LOCAZIONE)");

	private ModificatoreDelTesto() {
	}

	/**
	 * Legge il modificatore scritto in una riga di testo (MOD= di leggendari.txt).
	 *
	 * @throws IllegalArgumentException se il testo non ha la forma attesa
	 */
	public static ModificatoreAttributoMD da(String testo) {
		String descrizione = testo.trim();
		String[] parti = descrizione.split("\\s+", 3);
		if (parti.length < 2) {
			throw new IllegalArgumentException("Un modificatore è attributo e valore, \"FORZA +100%\": " + testo);
		}
		TipoAttributo attributo = TipoAttributo.valueOf(parti[0]);

		String valoreTesto = parti[1];
		TipoModificatore tipo;
		if (valoreTesto.startsWith("=")) {
			tipo = TipoModificatore.QUANTITA_ASSOLUTA;
			valoreTesto = valoreTesto.substring(1);
		} else if (valoreTesto.endsWith("%")) {
			tipo = TipoModificatore.VARIAZIONE_PERCENTUALE;
			valoreTesto = valoreTesto.substring(0, valoreTesto.length() - 1);
		} else {
			tipo = TipoModificatore.VARIAZIONE_FISSA;
		}
		double valore = Double.parseDouble(valoreTesto);

		String resto = parti.length > 2 ? parti[2].trim().toUpperCase(Locale.ROOT) : "";
		BersaglioModificatore bersaglio = BersaglioModificatore.PORTATORE;
		if (resto.startsWith(BERSAGLIO)) {
			bersaglio = BersaglioModificatore.BERSAGLIO;
			resto = resto.substring(BERSAGLIO.length()).trim();
		}
		UnitaDurataModificatore unita = UnitaDurataModificatore.PERMANENTE;
		int durata = 0;
		if (!resto.isEmpty()) {
			Matcher matcher = DURATA.matcher(resto);
			if (!matcher.matches()) {
				throw new IllegalArgumentException("Dopo il valore ci possono essere \"@BERSAGLIO\" e la durata "
						+ "(\"3TURNI\", \"3 LOCAZIONI\"): " + testo);
			}
			durata = Integer.parseInt(matcher.group(1));
			unita = matcher.group(2).startsWith("TURN") ? UnitaDurataModificatore.TURNI : UnitaDurataModificatore.LOCAZIONI;
		}
		return new ModificatoreAttributoMD(attributo, tipo, valore, "", unita, durata, bersaglio);
	}
}
