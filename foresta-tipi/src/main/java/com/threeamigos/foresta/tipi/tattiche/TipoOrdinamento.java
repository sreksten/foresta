package com.threeamigos.foresta.tipi.tattiche;

import com.threeamigos.foresta.testi.Testi;


/**
 * Come un selettore sceglie <i>uno</i> fra i candidati rimasti dopo i filtri; a parità vince il primo della rosa
 * (tattiche_di_combattimento_automatiche.md §11.6). La logica è in <code>tattiche.Ordinamenti</code>.
 * <p>
 * I testi di ogni costante (DESCRIZIONE) non stanno qui ma nel file {@code tipo_ordinamento_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 */
public enum TipoOrdinamento {

	/** Il primo della rosa */
	PRIMO(TipoParametro.NESSUNO),
	/** Uno a caso, tutti con la stessa probabilità */
	A_CASO(TipoParametro.NESSUNO),
	MENO_SALUTE(TipoParametro.NESSUNO),
	PIU_SALUTE(TipoParametro.NESSUNO),
	LIVELLO_PIU_BASSO(TipoParametro.NESSUNO),
	LIVELLO_PIU_ALTO(TipoParametro.NESSUNO),
	/** Il fattore di danno atteso più alto per quel tipo di danno */
	PIU_DEBOLE_A(TipoParametro.TIPO_DANNO),
	/** Il fattore di danno atteso più basso per quel tipo di danno (un immune è il più resistente) */
	PIU_RESISTENTE_A(TipoParametro.TIPO_DANNO),
	/**
	 * Chi ha attaccato per ultimo chi decide: solo lui, e se non c'è fra i candidati la voce non si può eseguire (e si passa alla successiva)
	 */
	ULTIMO_ATTACCANTE(TipoParametro.NESSUNO),
	/**
	 * L'avversario o l'alleato che chi decide ha scelto l'ultima volta: solo lui, e se non c'è fra i candidati la voce non si può eseguire
	 */
	ULTIMO_SCELTO(TipoParametro.NESSUNO),
	PIU_MAGIA(TipoParametro.NESSUNO),
	MENO_MAGIA(TipoParametro.NESSUNO);

	private final TipoParametro parametro;

	TipoOrdinamento(TipoParametro parametro) {
		this.parametro = parametro;
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	public TipoParametro getParametro() {
		return parametro;
	}
}
