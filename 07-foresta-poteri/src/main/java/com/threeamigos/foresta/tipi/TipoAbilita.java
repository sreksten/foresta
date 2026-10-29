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
	SCACCIARE_I_NON_MORTI,
	IMPOSIZIONE_DELLE_MANI,
	// Del druido
	RADICI,
	SCIAME,
	PELLE_DI_CORTECCIA,
	RIGENERAZIONE,
	// Del sacerdote
	ALBA_SACRA,
	LUCE_DIVINA,
	// Del mago e dell'elfo
	DARDO_ARCANO;

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
		switch (this) {
			case SCACCIARE_I_NON_MORTI:
				return CostantiAbilitaInnate.ABILITA_SCACCIARE_I_NON_MORTI_COSTO_LANCIO;
			case IMPOSIZIONE_DELLE_MANI:
				return CostantiAbilitaInnate.ABILITA_IMPOSIZIONE_DELLE_MANI_COSTO_LANCIO;
			case RADICI:
				return CostantiAbilitaInnate.ABILITA_RADICI_COSTO_LANCIO;
			case SCIAME:
				return CostantiAbilitaInnate.ABILITA_SCIAME_COSTO_LANCIO;
			case PELLE_DI_CORTECCIA:
				return CostantiAbilitaInnate.ABILITA_PELLE_DI_CORTECCIA_COSTO_LANCIO;
			case RIGENERAZIONE:
				return CostantiAbilitaInnate.ABILITA_RIGENERAZIONE_COSTO_LANCIO;
			case ALBA_SACRA:
				return CostantiAbilitaInnate.ABILITA_ALBA_SACRA_COSTO_LANCIO;
			case LUCE_DIVINA:
				return CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_COSTO_LANCIO;
			case DARDO_ARCANO:
				return CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO;
			default:
				throw new IllegalStateException("Costo non previsto per " + this);
		}
	}
}
