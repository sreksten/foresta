package com.threeamigos.foresta.tipi.tattiche;

/**
 * I filtri di un selettore: restringono i candidati di una rosa (tattiche_di_combattimento_automatiche.md §11.5). Qui c'è solo il nome e il tipo del
 * parametro; chi risponde "sì" o "no" per un candidato è <code>tattiche.Filtri</code>.
 */
public enum TipoFiltro {

	/** La salute attuale in punti */
	SALUTE("Salute", TipoParametro.CONFRONTO_E_NUMERO),
	/** La salute attuale sulla massima, in percentuale */
	SALUTE_PERCENTUALE("Salute in percentuale", TipoParametro.CONFRONTO_E_NUMERO),
	/** La MAGIA attuale in punti */
	MAGIA("Magia", TipoParametro.CONFRONTO_E_NUMERO),
	/** Il livello */
	LIVELLO("Livello", TipoParametro.CONFRONTO_E_NUMERO),
	/** Il livello rispetto a quello di chi decide */
	LIVELLO_RISPETTO_AL_MIO("Livello rispetto al mio", TipoParametro.CONFRONTO),
	/** La classe, intesa come ruolo: Mago e Maga sono la stessa cosa */
	CLASSE("Classe", TipoParametro.CLASSE),
	/** La categoria di creatura (basta una delle sue) */
	CATEGORIA("Categoria", TipoParametro.CATEGORIA),
	/** Ha quell'effetto di stato */
	STATO("Stato", TipoParametro.STATO),
	/** Ha almeno un effetto di stato che è un male da togliere (non l'ispirazione, non il berserk) */
	STATO_NEGATIVO("Stato negativo", TipoParametro.NESSUNO),
	/** Sa fare magie in questo momento (vedi tattiche.Filtri) */
	MAGICO("Magie abilitate", TipoParametro.NESSUNO),
	/** È debole a quel tipo di danno: il moltiplicatore di classe e di stati è maggiore di 1 */
	DEBOLE_A("Debolezza verso", TipoParametro.TIPO_DANNO),
	/** È debole al tipo di danno dell'azione che si sta valutando */
	DEBOLE_ALL_AZIONE("Debolezza contro", TipoParametro.NESSUNO),
	/** È immune a quel tipo di danno */
	IMMUNE_A("Immunità da", TipoParametro.TIPO_DANNO),
	/** È il capo del suo gruppo */
	IL_CAPO("Il capo", TipoParametro.NESSUNO),
	/** È chi decide */
	ME_STESSO("Me stesso", TipoParametro.NESSUNO),
	/** È il bersaglio che chi decide ha scelto l'ultima volta */
	SCELTO_L_ULTIMA_VOLTA("Scelto l'ultima volta", TipoParametro.NESSUNO),
	/** È chi ha attaccato per ultimo chi decide */
	MI_HA_ATTACCATO_PER_ULTIMO("Mi ha attaccato per ultimo", TipoParametro.NESSUNO),
	/** Ha in mano un'arma che dà quel tipo di danno (la principale o la secondaria) */
	ARMA_CHE_DA("Arma che dà", TipoParametro.TIPO_DANNO);

	private final String descrizione;
	private final TipoParametro parametro;

	TipoFiltro(String descrizione, TipoParametro parametro) {
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
