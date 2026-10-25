package com.threeamigos.foresta.artefatti;


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
	 * Gli effetti degli ingredienti sul banco che entreranno sull'artefatto
	 */
	int getEffettiDaTrasferire();

	/**
	 * Quanti effetti avrà l'artefatto sul banco dopo la fusione (quelli nuovi possono scalzarne di vecchi)
	 */
	int getEffettiDopoLaFusione();

	/**
	 * Il livello che avrà l'artefatto sul banco dopo la fusione: più alto del suo se sul banco ci sono degli aggiornamenti
	 * (oggetti che fanno salire di livello un artefatto); 0 se manca l'artefatto
	 */
	int getLivelloDopoLaFusione();

	/**
	 * Quanti effetti al massimo avrà l'artefatto sul banco dopo la fusione, con il livello nuovo; 0 se manca l'artefatto
	 */
	int getEffettiMassimiDopoLaFusione();

	/**
	 * Se l'effetto (un IncantamentoMD o un ModificatoreAttributoMD di un ingrediente sul banco) passerebbe
	 * sull'artefatto con la fusione
	 */
	boolean isEffettoInEntrata(Object effetto);

	/**
	 * Se l'effetto (un IncantamentoMD o un ModificatoreAttributoMD dell'artefatto sul banco) verrebbe scalzato
	 * dalla fusione
	 */
	boolean isEffettoInUscita(Object effetto);

	/**
	 * Quanto costa al gruppo la fusione di quel che sta sul banco
	 */
	int getCostoFusione();

}
