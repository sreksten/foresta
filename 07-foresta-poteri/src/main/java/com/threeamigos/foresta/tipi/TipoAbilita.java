package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

/**
 * Le abilità innate del Chierico, del Druido, del Sacerdote, del Mago e dell'Elfo (e delle loro forme femminili), simili ai canti del Bardo: costano MAGIA, non
 * consumano pergamene, non si comprano né si trovano.
 * <p>
 * Il comando con cui la UI ne chiede l'uso al motore lo dà {@link ComandiAssociati}.
 * <p>
 * Il nome e la descrizione di ogni abilità non stanno qui ma nel file {@code tipo_abilita_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 * <p>
 * Vedi anche {@code abilita.AbilitaInnate}, che dice chi conosce quale.
 *
 * @author Stefano Reksten
 */
public enum TipoAbilita {

	// Del chierico
	SCACCIARE_I_NON_MORTI(CostantiAbilitaInnate.ABILITA_SCACCIARE_I_NON_MORTI_COSTO_LANCIO, CostantiAbilitaInnate.ABILITA_SCACCIARE_I_NON_MORTI_RICARICA),
	IMPOSIZIONE_DELLE_MANI(CostantiAbilitaInnate.ABILITA_IMPOSIZIONE_DELLE_MANI_COSTO_LANCIO, CostantiAbilitaInnate.ABILITA_IMPOSIZIONE_DELLE_MANI_RICARICA),
	// Del druido
	RADICI(CostantiAbilitaInnate.ABILITA_RADICI_COSTO_LANCIO, CostantiAbilitaInnate.ABILITA_RADICI_RICARICA),
	SCIAME(CostantiAbilitaInnate.ABILITA_SCIAME_COSTO_LANCIO, CostantiAbilitaInnate.ABILITA_SCIAME_RICARICA),
	PELLE_DI_CORTECCIA(CostantiAbilitaInnate.ABILITA_PELLE_DI_CORTECCIA_COSTO_LANCIO, CostantiAbilitaInnate.ABILITA_PELLE_DI_CORTECCIA_RICARICA),
	RIGENERAZIONE(CostantiAbilitaInnate.ABILITA_RIGENERAZIONE_COSTO_LANCIO, CostantiAbilitaInnate.ABILITA_RIGENERAZIONE_RICARICA),
	// Del sacerdote
	ALBA_SACRA(CostantiAbilitaInnate.ABILITA_ALBA_SACRA_COSTO_LANCIO, CostantiAbilitaInnate.ABILITA_ALBA_SACRA_RICARICA),
	LUCE_DIVINA(CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_COSTO_LANCIO, CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_RICARICA),
	// Del mago e dell'elfo
	DARDO_ARCANO(CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO, CostantiAbilitaInnate.DARDO_ARCANO_RICARICA);

	private final int costoLancio;
	private final int ricarica;

	TipoAbilita(int costoLancio, int ricarica) {
		this.costoLancio = costoLancio;
		this.ricarica = ricarica;
	}

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	/**
	 * L'azione che usa questa abilità innata: ha lo stesso nome.
	 */
	public TipoAzione getTipoAzione() {
		return TipoAzione.valueOf(name());
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	/**
	 * I punti di MAGIA che costa usarla, per chi la usa con il costo base (quello del Mago per il dardo arcano).
	 */
	public int getCostoLancio() {
		return costoLancio;
	}

	/**
	 * Quanti turni passano prima di poterla usare di nuovo.
	 */
	public int getRicarica() {
		return ricarica;
	}
}
