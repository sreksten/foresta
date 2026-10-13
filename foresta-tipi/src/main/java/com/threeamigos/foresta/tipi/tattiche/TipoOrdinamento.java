package com.threeamigos.foresta.tipi.tattiche;

/**
 * Come un selettore sceglie <i>uno</i> fra i candidati rimasti dopo i filtri; a parità vince il primo della rosa
 * (tattiche_di_combattimento_automatiche.md §11.6). La logica è in <code>tattiche.Ordinamenti</code>.
 */
public enum TipoOrdinamento {

	/** Il primo della rosa */
	PRIMO("Il primo", TipoParametro.NESSUNO),
	/** Uno a caso, tutti con la stessa probabilità */
	A_CASO("A caso", TipoParametro.NESSUNO),
	MENO_SALUTE("Con meno salute", TipoParametro.NESSUNO),
	PIU_SALUTE("Con più salute", TipoParametro.NESSUNO),
	LIVELLO_PIU_BASSO("Di livello più basso", TipoParametro.NESSUNO),
	LIVELLO_PIU_ALTO("Di livello più alto", TipoParametro.NESSUNO),
	/** Il fattore di danno atteso più alto per quel tipo di danno */
	PIU_DEBOLE_A("Più debole a", TipoParametro.TIPO_DANNO),
	/** Il fattore di danno atteso più basso per quel tipo di danno (un immune è il più resistente) */
	PIU_RESISTENTE_A("Più resistente a", TipoParametro.TIPO_DANNO),
	/**
	 * Chi ha attaccato per ultimo chi decide: solo lui, e se non c'è fra i candidati la voce non si può eseguire (e si passa alla successiva)
	 */
	ULTIMO_ATTACCANTE("Chi mi ha attaccato per ultimo", TipoParametro.NESSUNO),
	/**
	 * L'avversario o l'alleato che chi decide ha scelto l'ultima volta: solo lui, e se non c'è fra i candidati la voce non si può eseguire
	 */
	ULTIMO_SCELTO("L'ultimo scelto", TipoParametro.NESSUNO),
	PIU_MAGIA("Con più magia", TipoParametro.NESSUNO),
	MENO_MAGIA("Con meno magia", TipoParametro.NESSUNO);

	private final String descrizione;
	private final TipoParametro parametro;

	TipoOrdinamento(String descrizione, TipoParametro parametro) {
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
