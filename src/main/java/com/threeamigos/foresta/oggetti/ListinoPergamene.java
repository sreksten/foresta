package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.modellodati.IncantamentoMD;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tipi.TipoAttributo;

import java.util.EnumMap;
import java.util.Map;

/**
 * Prezzo di un ingrediente magico come somma dei prezzi dei suoi effetti, secondo quanto valgono in gioco (vedi
 * artefatti_e_incantamenti.md, §6):
 * <ul>
 * <li>incantamento: il danno grezzo che aggiunge a un colpo, cioè bonus fisso × livello dell'oggetto più
 * coefficiente × Intelligenza tipica, per PERGAMENA_PREZZO_PER_PUNTO_DI_DANNO; +25% se il tipo di danno ha
 * effetti di stato;</li>
 * <li>modificatore: di quanto aumenta l'attributo rispetto al suo valore tipico, per
 * PERGAMENA_PREZZO_PER_PERCENTO_DI_ATTRIBUTO ogni 1%. Un AUMENTO_PERCENTUALE di q% vale q%; un AUMENTO_FISSO di q
 * vale q diviso il valore tipico, quindi +1 di Precisione (che vale circa 3) costa molto più di +1 di Forza
 * (circa 11). Una QUANTITA_ASSOLUTA ("porta a q") vale 5 × q.</li>
 * </ul>
 * I valori stanno in Costanti e vanno bilanciati.
 */
public final class ListinoPergamene {

	/**
	 * Il valore tipico degli attributi di un personaggio giocante: la media delle classi in
	 * PERSONAGGI_VALORI_MEDI.csv; il POTERE_MAGICO parte da 100. Per gli altri VALORE_TIPICO_PREDEFINITO.
	 */
	private static final Map<TipoAttributo, Double> VALORI_TIPICI = new EnumMap<>(TipoAttributo.class);
	private static final double VALORE_TIPICO_PREDEFINITO = 10.0;

	static {
		VALORI_TIPICI.put(TipoAttributo.FORZA, 11.5);
		VALORI_TIPICI.put(TipoAttributo.DESTREZZA, 10.3);
		VALORI_TIPICI.put(TipoAttributo.COSTITUZIONE, 8.9);
		VALORI_TIPICI.put(TipoAttributo.INTELLIGENZA, 9.7);
		VALORI_TIPICI.put(TipoAttributo.SAGGEZZA, 8.7);
		VALORI_TIPICI.put(TipoAttributo.CARISMA, 9.1);
		VALORI_TIPICI.put(TipoAttributo.FORTUNA, 8.3);
		VALORI_TIPICI.put(TipoAttributo.CRITICO, 9.5);
		VALORI_TIPICI.put(TipoAttributo.CARICO_MASSIMO, 57.0);
		VALORI_TIPICI.put(TipoAttributo.PRECISIONE, 3.2);
		VALORI_TIPICI.put(TipoAttributo.VELOCITA, 3.3);
		VALORI_TIPICI.put(TipoAttributo.FURTIVITA, 3.2);
		VALORI_TIPICI.put(TipoAttributo.PARATA, 3.0);
		VALORI_TIPICI.put(TipoAttributo.RESISTENZA_MAGICA, 3.0);
		VALORI_TIPICI.put(TipoAttributo.PERCEZIONE, 3.0);
		VALORI_TIPICI.put(TipoAttributo.SOGGEZIONE, 2.8);
		VALORI_TIPICI.put(TipoAttributo.FURIA, 3.2);
		VALORI_TIPICI.put(TipoAttributo.CORAGGIO, 2.7);
		VALORI_TIPICI.put(TipoAttributo.VALORE, 2.8);
		VALORI_TIPICI.put(TipoAttributo.CONTRATTAZIONE, 2.9);
		VALORI_TIPICI.put(TipoAttributo.POTERE_MAGICO, 100.0);
	}

	private ListinoPergamene() {
	}

	/**
	 * Il prezzo di un ingrediente, almeno PERGAMENA_PREZZO_MINIMO: i suoi incantamenti si prezzano al suo livello.
	 */
	public static int prezzo(ArtefattoMD ingrediente) {
		double prezzo = 0;
		for (IncantamentoMD incantamento : ingrediente.getIncantamenti()) {
			prezzo += prezzo(incantamento, ingrediente.getLivello());
		}
		for (ModificatoreAttributo modificatore : ingrediente.getModificatori()) {
			prezzo += prezzo(modificatore);
		}
		return Math.max(Costanti.PERGAMENA_PREZZO_MINIMO, (int) Math.round(prezzo));
	}

	/**
	 * Il prezzo di un incantamento su un oggetto di quel livello, perché la parte fissa si moltiplica per il
	 * livello dell'oggetto (vedi CalcolatoreCombattimento).
	 */
	public static double prezzo(IncantamentoMD incantamento, int livello) {
		double danno = Math.abs(incantamento.getDannoBonusFisso()) * livello
				+ Math.abs(incantamento.getCoefficienteScala()) * valoreTipico(TipoAttributo.INTELLIGENZA);
		double prezzo = Costanti.PERGAMENA_PREZZO_PER_PUNTO_DI_DANNO * danno;
		if (incantamento.getTipoDannoElementale().hasEffettiDiStato()) {
			prezzo *= 1 + Costanti.PERGAMENA_MAGGIORAZIONE_EFFETTI_DI_STATO;
		}
		return prezzo;
	}

	public static double prezzo(ModificatoreAttributo modificatore) {
		double quantita = Math.abs(modificatore.getQuantita());
		switch (modificatore.getTipoModificatoreAttributo()) {
			case AUMENTO_FISSO:
				return Costanti.PERGAMENA_PREZZO_PER_PERCENTO_DI_ATTRIBUTO * 100 * quantita / valoreTipico(modificatore.getTipoAttributo());
			case AUMENTO_PERCENTUALE:
				return Costanti.PERGAMENA_PREZZO_PER_PERCENTO_DI_ATTRIBUTO * quantita;
			case QUANTITA_ASSOLUTA:
				return Costanti.PERGAMENA_PREZZO_PER_PUNTO_ASSOLUTO * quantita;
			default:
				throw new IllegalArgumentException("Tipo di modificatore non gestito: " + modificatore.getTipoModificatoreAttributo());
		}
	}

	static double valoreTipico(TipoAttributo attributo) {
		return VALORI_TIPICI.getOrDefault(attributo, VALORE_TIPICO_PREDEFINITO);
	}
}
