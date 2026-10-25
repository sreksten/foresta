package com.threeamigos.foresta.tipi.tattiche;

import com.threeamigos.foresta.testi.Testi;


/**
 * I filtri di un selettore: restringono i candidati di una rosa (tattiche_di_combattimento_automatiche.md §11.5). Qui c'è solo il nome e il tipo del
 * parametro; chi risponde "sì" o "no" per un candidato è <code>tattiche.Filtri</code>.
 * <p>
 * I testi di ogni costante (DESCRIZIONE) non stanno qui ma nel file {@code tipo_filtro_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 */
public enum TipoFiltro {

	/** La salute attuale in punti */
	SALUTE(TipoParametro.CONFRONTO_E_NUMERO),
	/** La salute attuale sulla massima, in percentuale */
	SALUTE_PERCENTUALE(TipoParametro.CONFRONTO_E_NUMERO),
	/** La MAGIA attuale in punti */
	MAGIA(TipoParametro.CONFRONTO_E_NUMERO),
	/** Il livello */
	LIVELLO(TipoParametro.CONFRONTO_E_NUMERO),
	/** Il livello rispetto a quello di chi decide */
	LIVELLO_RISPETTO_AL_MIO(TipoParametro.CONFRONTO),
	/** La classe, intesa come ruolo: Mago e Maga sono la stessa cosa */
	CLASSE(TipoParametro.CLASSE),
	/** La categoria di creatura (basta una delle sue) */
	CATEGORIA(TipoParametro.CATEGORIA),
	/** Ha quell'effetto di stato */
	STATO(TipoParametro.STATO),
	/** Ha almeno un effetto di stato che è un male da togliere (non l'ispirazione, non il berserk) */
	STATO_NEGATIVO(TipoParametro.NESSUNO),
	/** Sa fare magie in questo momento (vedi tattiche.Filtri) */
	MAGICO(TipoParametro.NESSUNO),
	/** È debole a quel tipo di danno: il moltiplicatore di classe e di stati è maggiore di 1 */
	DEBOLE_A(TipoParametro.TIPO_DANNO),
	/** È debole al tipo di danno dell'azione che si sta valutando */
	DEBOLE_ALL_AZIONE(TipoParametro.NESSUNO),
	/** È immune a quel tipo di danno */
	IMMUNE_A(TipoParametro.TIPO_DANNO),
	/** È il capo del suo gruppo */
	IL_CAPO(TipoParametro.NESSUNO),
	/** È chi decide */
	ME_STESSO(TipoParametro.NESSUNO),
	/** È il bersaglio che chi decide ha scelto l'ultima volta */
	SCELTO_L_ULTIMA_VOLTA(TipoParametro.NESSUNO),
	/** È chi ha attaccato per ultimo chi decide */
	MI_HA_ATTACCATO_PER_ULTIMO(TipoParametro.NESSUNO),
	/** Ha in mano un'arma che dà quel tipo di danno (la principale o la secondaria) */
	ARMA_CHE_DA(TipoParametro.TIPO_DANNO);

	private final TipoParametro parametro;

	TipoFiltro(TipoParametro parametro) {
		this.parametro = parametro;
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	public TipoParametro getParametro() {
		return parametro;
	}
}
