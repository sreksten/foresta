package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il berserk di chi ce l'ha (vedi TipoEffettoDiStato.BERSERK): quando scatta, scendendo sotto una certa parte della salute massima, e
 * quanto aumenta il danno fisico per ogni parte di salute persa. Il guerriero, la guerriera e il capitano delle guardie scattano a un terzo
 * della salute e aggiungono metà della salute persa al danno; il vichingo scatta a metà della salute e ne aggiunge il doppio.
 */
public final class Berserk {

	private Berserk() {
	}

	/**
	 * Se la classe ha il berserk (e quindi lo scatta se ha FURIA).
	 */
	public static boolean haIlBerserk(TipoPersonaggio classe) {
		return classe == TipoPersonaggio.GUERRIERO || classe == TipoPersonaggio.GUERRIERA
				|| classe == TipoPersonaggio.CAPITANO_DELLE_GUARDIE || classe == TipoPersonaggio.VICHINGO;
	}

	/**
	 * La parte della salute massima sotto la quale scatta il berserk.
	 */
	public static double soglia(TipoPersonaggio classe) {
		return classe == TipoPersonaggio.VICHINGO ? CostantiPersonaggi.VICHINGO_BERSERK_SOGLIA : CostantiPersonaggi.BERSERK_SOGLIA;
	}

	/**
	 * Di quanto cresce il danno fisico per ogni parte di salute persa (0.5: con metà salute persa il danno è del 25% maggiore).
	 */
	public static double bonus(TipoPersonaggio classe) {
		return classe == TipoPersonaggio.VICHINGO ? CostantiPersonaggi.VICHINGO_BERSERK_BONUS : CostantiPersonaggi.BERSERK_BONUS;
	}
}
