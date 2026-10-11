package com.threeamigos.foresta.tipi.tattiche;

/**
 * Le condizioni di una voce di tattica che non sono un controllo su un personaggio (quelli sono filtri di un selettore):
 * tattiche_di_combattimento_automatiche.md §11.8. Qui c'è solo il nome e il tipo del parametro; chi risponde è <code>tattiche.Predicati</code>.
 */
public enum TipoPredicato {

	/** Quanti candidati ci sono in una rosa dopo i filtri: esistenza e conteggi */
	QUANTI("Quanti", TipoParametro.SELEZIONE_CONFRONTO_E_NUMERO),
	/** Il numero del round dello scontro */
	ROUND("Round", TipoParametro.CONFRONTO_E_NUMERO),
	/** L'astuzia di chi decide */
	ASTUZIA("Astuzia", TipoParametro.CONFRONTO_E_NUMERO),
	/** Vero nel n% dei casi */
	PROBABILITA("Probabilità", TipoParametro.NUMERO),
	/** Quante volte chi decide ha usato questa voce nello scontro */
	VOCE_USATA("Voce usata", TipoParametro.CONFRONTO_E_NUMERO),
	/** I round passati dall'ultimo uso di questa voce (se non l'ha mai usata, moltissimi) */
	ROUND_DALL_ULTIMO_USO("Round dall'ultimo uso", TipoParametro.CONFRONTO_E_NUMERO);

	private final String descrizione;
	private final TipoParametro parametro;

	TipoPredicato(String descrizione, TipoParametro parametro) {
		this.descrizione = descrizione;
		this.parametro = parametro;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public TipoParametro getParametro() {
		return parametro;
	}
}
