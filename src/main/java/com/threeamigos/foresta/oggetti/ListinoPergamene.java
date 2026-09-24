package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;

/**
 * Prezzo di una pergamena come somma dei prezzi dei suoi effetti (vedi artefatti_e_incantamenti.md, §8):
 * <ul>
 * <li>incantamento: 2 × bonus fisso + coefficiente in punti percentuali, +25% se il tipo di danno ha effetti di stato;</li>
 * <li>modificatore AUMENTO_FISSO di q: 2 × q; AUMENTO_PERCENTUALE di q%: q; QUANTITA_ASSOLUTA ("porta a q"): 5 × q.</li>
 * </ul>
 * I valori stanno in Costanti e vanno bilanciati.
 */
public final class ListinoPergamene {

	private ListinoPergamene() {
	}

	public static int prezzo(ArtefattoMD pergamena) {
		double prezzo = 0;
		for (Incantamento incantamento : pergamena.getIncantamenti()) {
			prezzo += prezzo(incantamento);
		}
		for (ModificatoreAttributo modificatore : pergamena.getModificatori()) {
			prezzo += prezzo(modificatore);
		}
		return (int) Math.round(prezzo);
	}

	static double prezzo(Incantamento incantamento) {
		double prezzo = Costanti.PERGAMENA_PREZZO_PER_PUNTO_FISSO * Math.abs(incantamento.getDannoBonusFisso())
				+ Costanti.PERGAMENA_PREZZO_PER_PUNTO_PERCENTUALE * Math.abs(incantamento.getCoefficienteScala()) * 100;
		if (incantamento.getTipoDannoElementale().hasEffettiDiStato()) {
			prezzo *= 1 + Costanti.PERGAMENA_MAGGIORAZIONE_EFFETTI_DI_STATO;
		}
		return prezzo;
	}

	static double prezzo(ModificatoreAttributo modificatore) {
		double quantita = Math.abs(modificatore.getQuantita());
		switch (modificatore.getTipoModificatoreAttributo()) {
			case AUMENTO_FISSO:
				return Costanti.PERGAMENA_PREZZO_PER_PUNTO_FISSO * quantita;
			case AUMENTO_PERCENTUALE:
				return Costanti.PERGAMENA_PREZZO_PER_PUNTO_PERCENTUALE * quantita;
			case QUANTITA_ASSOLUTA:
				return Costanti.PERGAMENA_PREZZO_PER_PUNTO_ASSOLUTO * quantita;
			default:
				throw new IllegalArgumentException("Tipo di modificatore non gestito: " + modificatore.getTipoModificatoreAttributo());
		}
	}
}
