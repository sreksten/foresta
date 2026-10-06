package com.threeamigos.foresta.interfacce;

import java.util.Optional;

/**
 * La bottega dell'incantatore vista dalla UI: lo scambio fra l'inventario del gruppo (parte attiva) e il banco di
 * lavoro (parte remota), con quel che serve per mostrare la fusione. Lo implementa AutomaIncantatore.
 */
public interface VistaBancoDiLavoro extends VistaScambio {

	/**
	 * L'artefatto sul banco, se c'è (gli altri sono ingredienti)
	 */
	Optional<? extends VistaArtefatto> getArtefattoSulBanco();

	/**
	 * Gli effetti degli ingredienti sul banco che passeranno sull'artefatto
	 */
	int getEffettiDaTrasferire();

	/**
	 * Quanto costa al gruppo la fusione di quel che sta sul banco
	 */
	int getCostoFusione();

}
