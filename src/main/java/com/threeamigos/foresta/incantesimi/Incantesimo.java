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
    ClassiIncantesimo getClasse();

	/**
	 * Il nome abbreviato di un incantesimo (es. Aria)
	 */
    String getNomeAbbreviato();

	/**
	 * Il nome completo di un incantesimo al singolare (es. Incantesimo dell'Aria)
	 */
    String getNomeSingolare();

	/**
	 * Il nome completo di un incantesimo al plurale (es. Incantesimi dell'Aria)
	 */
    String getNomePlurale();

	/**
	 * Formula un incantesimo su un bersaglio
	 * @param formulante il personaggio che formula l'incantesimo
	 * @param personaggioBersaglio il personaggio su cui viene formulato l'incantesimo,
	 *        può essere null
	 * @param gruppoBersaglio il gruppo su cui viene formulato l'incantesimo,
	 *        può essere null
	 */
    void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio);

	/**
	 * Il tipo di questo incantesimo: BENEFICO, MALEFICO
	 */
    TipoIncantesimo getTipo();
	
	/**
	 * La portata di questo incantesimo; GLOBALE, GRUPPO, SINGOLO_SOLO_VIVI, SINGOLO_QUALSIASI
	 */
    PortataIncantesimo getPortata();

	/**
	 * Quante monete costa acquistarlo
	 */
    int getCostoAcquisto();

	/**
	 * Quanti punti di magia costa lanciarlo
	 */
    int getCostoLancio();

}
