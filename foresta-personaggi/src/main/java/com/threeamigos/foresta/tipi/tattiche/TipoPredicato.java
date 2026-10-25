package com.threeamigos.foresta.tipi.tattiche;

import com.threeamigos.foresta.testi.Testi;


/**
 * Le condizioni di una voce di tattica che non sono un controllo su un personaggio (quelli sono filtri di un selettore):
 * tattiche_di_combattimento_automatiche.md §11.8. Qui c'è solo il nome e il tipo del parametro; chi risponde è <code>tattiche.Predicati</code>.
 * <p>
 * I testi di ogni costante (DESCRIZIONE) non stanno qui ma nel file {@code tipo_predicato_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 */
public enum TipoPredicato {

	/** Quanti candidati ci sono in una rosa dopo i filtri: esistenza e conteggi */
	QUANTI(TipoParametro.SELEZIONE_CONFRONTO_E_NUMERO),
	/** Il numero del round dello scontro */
	ROUND(TipoParametro.CONFRONTO_E_NUMERO),
	/** L'astuzia di chi decide */
	ASTUZIA(TipoParametro.CONFRONTO_E_NUMERO),
	/** L'INTELLIGENZA attuale di chi decide (non l'astuzia: le soglie di oggi dei mostri, 5 e 7, sono sull'INTELLIGENZA) */
	INTELLIGENZA(TipoParametro.CONFRONTO_E_NUMERO),
	/** Vero nel n% dei casi */
	PROBABILITA(TipoParametro.NUMERO),
	/** Quante volte chi decide ha usato questa voce nello scontro */
	VOCE_USATA(TipoParametro.CONFRONTO_E_NUMERO),
	/** I round passati dall'ultimo uso di questa voce (se non l'ha mai usata, moltissimi) */
	ROUND_DALL_ULTIMO_USO(TipoParametro.CONFRONTO_E_NUMERO);

	private final TipoParametro parametro;

	TipoPredicato(TipoParametro parametro) {
		this.parametro = parametro;
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	public TipoParametro getParametro() {
		return parametro;
	}
}
