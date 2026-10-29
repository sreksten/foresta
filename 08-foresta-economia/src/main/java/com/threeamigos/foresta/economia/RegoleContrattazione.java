package com.threeamigos.foresta.economia;

import com.threeamigos.foresta.tipi.CostantiEconomia;

/**
 * I prezzi nelle botteghe secondo la CONTRATTAZIONE di chi tratta. Lo sconto sugli acquisti (artefatti,
 * consumabili, fusioni) e la quota del costo che si ricava vendendo crescono con
 * bonus = contrattazione / (contrattazione + saturazione), che satura verso 1:
 * <ul>
 *     <li>acquisto: sconto = 0,24 &times; bonus, al massimo del 20%;</li>
 *     <li>vendita: quota = 0,50 + 0,30 &times; bonus, al massimo del 75%.</li>
 * </ul>
 * Il peggior acquisto (80%) resta sopra la miglior vendita (75%): comprare e rivendere non fa mai guadagnare.
 * Gli arrotondamenti vanno a favore del mercante.
 */
public final class RegoleContrattazione {

	private RegoleContrattazione() {
	}

	static double bonus(int contrattazione) {
		if (contrattazione <= 0) {
			return 0;
		}
		return (double) contrattazione / (contrattazione + CostantiEconomia.CONTRATTAZIONE_SATURAZIONE);
	}

	/**
	 * Lo sconto sugli acquisti, tra 0 e {@link CostantiEconomia#CONTRATTAZIONE_SCONTO_MASSIMO}.
	 */
	public static double sconto(int contrattazione) {
		return Math.min(CostantiEconomia.CONTRATTAZIONE_SCONTO_MASSIMO,
				CostantiEconomia.CONTRATTAZIONE_COEFFICIENTE_SCONTO * bonus(contrattazione));
	}

	/**
	 * La quota del costo che si ricava vendendo, tra {@link CostantiEconomia#CONTRATTAZIONE_QUOTA_VENDITA_BASE} e
	 * {@link CostantiEconomia#CONTRATTAZIONE_QUOTA_VENDITA_MASSIMA}.
	 */
	public static double quotaVendita(int contrattazione) {
		return Math.min(CostantiEconomia.CONTRATTAZIONE_QUOTA_VENDITA_MASSIMA,
				CostantiEconomia.CONTRATTAZIONE_QUOTA_VENDITA_BASE
						+ CostantiEconomia.CONTRATTAZIONE_COEFFICIENTE_VENDITA * bonus(contrattazione));
	}

	/**
	 * Quanto si paga, arrotondato per eccesso, un oggetto di quel costo.
	 */
	public static int prezzoAcquisto(int costo, int contrattazione) {
		return (int) Math.ceil(costo * (1 - sconto(contrattazione)) - 1e-9);
	}

	/**
	 * Quanto si ricava, arrotondato per difetto, vendendo un oggetto di quel costo.
	 */
	public static int prezzoVendita(int costo, int contrattazione) {
		return (int) Math.floor(costo * quotaVendita(contrattazione) + 1e-9);
	}
}
