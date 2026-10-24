package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

/**
 * Le abilità innate del Chierico e del Druido (e delle loro forme femminili), simili ai canti del Bardo: costano MAGIA, non
 * consumano pergamene, non si comprano né si trovano.
 * <p>
 * Come per {@link TipoCanto}, ogni abilità è associata a un {@link Comando} usato dalla UI per chiederne l'uso al motore.
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
	SCACCIARE_I_NON_MORTI(Comando.SCACCIARE_I_NON_MORTI),
	IMPOSIZIONE_DELLE_MANI(Comando.IMPOSIZIONE_DELLE_MANI),
	// Del druido
	RADICI(Comando.RADICI),
	SCIAME(Comando.SCIAME),
	PELLE_DI_CORTECCIA(Comando.PELLE_DI_CORTECCIA),
	RIGENERAZIONE(Comando.RIGENERAZIONE);

	/**
	 * @return l'abilità scelta con quel comando, o null se il comando non è di un'abilità
	 */
	public static TipoAbilita ofComando(Comando comando) {
		for (TipoAbilita tipo : values()) {
			if (tipo.comando == comando) {
				return tipo;
			}
		}
		return null;
	}

	private final Comando comando;

	TipoAbilita(Comando comando) {
		this.comando = comando;
	}

	/**
	 * Il comando con cui il giocatore sceglie questa abilità nel menu degli incantesimi
	 */
	public Comando getComando() {
		return comando;
	}

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	/**
	 * I punti di MAGIA che costa usarla.
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
			default:
				throw new IllegalStateException("Costo non previsto per " + this);
		}
	}
}
