package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un evento soprannaturale che il giocatore può scatenare contro un gruppo
 * avversario, contro un avversario singolo o su un proprio amico o se stesso,
 * o sulla locazione
 */

public interface Incantesimo {

	/**
	 * La classe dell'incantesimo
	 */
    ClasseIncantesimo getClasse();

	/**
	 * Formula un incantesimo su un bersaglio
	 * @param formulante il personaggio che formula l'incantesimo
	 * @param personaggioBersaglio il personaggio su cui viene formulato l'incantesimo,
	 *        può essere null se la portata è GRUPPO o GLOBALE
	 * @param gruppoBersaglio il gruppo su cui viene formulato l'incantesimo,
	 *        può essere null se la portata è GLOBALE
	 */
    void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio);

	/**
	 * Il livello a cui viene formulato l'incantesimo
	 */
	int getLivello();

	/**
	 * Quanti punti di magia costa lanciarlo
	 */
    int getCostoLancio();
}
