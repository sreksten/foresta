package com.threeamigos.foresta.testi;

import java.util.List;

/**
 * Unisce gli elementi di un elenco con i separatori di una lingua (vedi {@link RegoleDellaLingua#elenco}).
 */
final class Elenchi {

	private Elenchi() {
	}

	/**
	 * @param separatore       fra gli elementi
	 * @param ultimoDiDue      prima dell'ultimo elemento di un elenco di due (" e ", " and ")
	 * @param ultimoDiPiu      prima dell'ultimo elemento di un elenco più lungo (" e " in italiano, ", and " in inglese)
	 */
	static String unisci(List<String> elementi, String separatore, String ultimoDiDue, String ultimoDiPiu) {
		int n = elementi.size();
		if (n == 0) {
			return "";
		}
		if (n == 1) {
			return elementi.get(0);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			if (i > 0) {
				// Prima di ogni elemento tranne il primo: la virgola, e prima dell'ultimo la congiunzione (che in inglese cambia se
				// gli elementi sono più di due)
				sb.append(i < n - 1 ? separatore : n == 2 ? ultimoDiDue : ultimoDiPiu);
			}
			sb.append(elementi.get(i));
		}
		return sb.toString();
	}
}
