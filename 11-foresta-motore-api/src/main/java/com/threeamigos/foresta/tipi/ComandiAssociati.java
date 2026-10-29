package com.threeamigos.foresta.tipi;

import java.util.EnumMap;
import java.util.Map;

/**
 * Il comando con cui la UI sceglie un incantesimo, un canto, un'abilità o una mossa di carta, forbici e sasso, e viceversa. Sta qui e
 * non nelle enum perché i poteri non devono sapere come li chiede la UI: è il comando che li conosce.
 *
 * @author Stefano Reksten
 */
public final class ComandiAssociati {

	private static final Map<TipoIncantesimo, Comando> INCANTESIMI = new EnumMap<>(TipoIncantesimo.class);
	private static final Map<TipoCanto, Comando> CANTI = new EnumMap<>(TipoCanto.class);
	private static final Map<TipoAbilita, Comando> ABILITA = new EnumMap<>(TipoAbilita.class);
	private static final Map<MossaCartaForbiciSasso, Comando> MOSSE = new EnumMap<>(MossaCartaForbiciSasso.class);

	static {
		for (TipoIncantesimo tipo : TipoIncantesimo.values()) {
			INCANTESIMI.put(tipo, Comando.valueOf(tipo.name()));
		}
		CANTI.put(TipoCanto.CURATIVO, Comando.CANTO_CURATIVO);
		CANTI.put(TipoCanto.IPNOTICO, Comando.CANTO_IPNOTICO);
		CANTI.put(TipoCanto.DI_VALORE, Comando.CANTO_DI_VALORE);
		CANTI.put(TipoCanto.URLO_DELLA_BANSHEE, Comando.URLO_DELLA_BANSHEE);
		for (TipoAbilita tipo : TipoAbilita.values()) {
			ABILITA.put(tipo, Comando.valueOf(tipo.name()));
		}
		for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
			MOSSE.put(mossa, Comando.valueOf(mossa.name()));
		}
	}

	private ComandiAssociati() {
	}

	/**
	 * Il comando che attiva quell'incantesimo.
	 */
	public static Comando di(TipoIncantesimo tipo) {
		return INCANTESIMI.get(tipo);
	}

	/**
	 * Il comando con cui il giocatore sceglie quel canto nel menu degli incantesimi.
	 */
	public static Comando di(TipoCanto tipo) {
		return CANTI.get(tipo);
	}

	/**
	 * Il comando con cui il giocatore sceglie quell'abilità nel menu degli incantesimi.
	 */
	public static Comando di(TipoAbilita tipo) {
		return ABILITA.get(tipo);
	}

	/**
	 * Il comando con cui il giocatore sceglie quella mossa.
	 */
	public static Comando di(MossaCartaForbiciSasso mossa) {
		return MOSSE.get(mossa);
	}

	/**
	 * L'incantesimo che il comando attiva.
	 *
	 * @throws IllegalArgumentException se il comando non attiva nessun incantesimo
	 */
	public static TipoIncantesimo incantesimo(Comando comando) {
		for (Map.Entry<TipoIncantesimo, Comando> voce : INCANTESIMI.entrySet()) {
			if (voce.getValue() == comando) {
				return voce.getKey();
			}
		}
		throw new IllegalArgumentException("Comando invalido per incantesimo: " + comando);
	}

	/**
	 * Il canto scelto con quel comando, o null se il comando non è di un canto.
	 */
	public static TipoCanto canto(Comando comando) {
		for (Map.Entry<TipoCanto, Comando> voce : CANTI.entrySet()) {
			if (voce.getValue() == comando) {
				return voce.getKey();
			}
		}
		return null;
	}

	/**
	 * L'abilità scelta con quel comando, o null se il comando non è di un'abilità.
	 */
	public static TipoAbilita abilita(Comando comando) {
		for (Map.Entry<TipoAbilita, Comando> voce : ABILITA.entrySet()) {
			if (voce.getValue() == comando) {
				return voce.getKey();
			}
		}
		return null;
	}

	/**
	 * La mossa che il giocatore ha scelto con quel comando, o null se il comando non è una delle tre.
	 */
	public static MossaCartaForbiciSasso mossa(Comando comando) {
		for (Map.Entry<MossaCartaForbiciSasso, Comando> voce : MOSSE.entrySet()) {
			if (voce.getValue() == comando) {
				return voce.getKey();
			}
		}
		return null;
	}
}
