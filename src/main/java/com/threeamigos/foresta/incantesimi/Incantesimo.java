package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un evento soprannaturale che il giocatore puo' scatenare contro un gruppo
 * avversario, contro un avversario singolo o su un proprio amico o se stesso,
 * o sulla locazione
 */

public interface Incantesimo {

	/**
	 * La classe dell'incantesimo
	 */
	public ClassiIncantesimo getClasse();

	/**
	 * Il nome abbreviato di un incantesimo (es. Aria)
	 */
	public String getNomeAbbreviato();

	/**
	 * Il nome completo di un incantesimo al singolare (es. Incantesimo dell'Aria)
	 */
	public String getNomeSingolare();

	/**
	 * Il nome completo di un incantesimo al plurale (es. Incantesimi dell'Aria)
	 */
	public String getNomePlurale();

	/**
	 * Formula un incantesimo su un bersaglio
	 * @param formulante il personaggio che formula l'incantesimo
	 * @param personaggioBersaglio il personaggio su cui viene formulato l'incantesimo,
	 *        puo' essere null
	 * @param gruppoBersaglio il gruppo su cui viene formulato l'incantesimo,
	 *        puo' essere null
	 */
	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio);

	/**
	 * Il tipo di questo incantesimo: BENEFICO, MALEFICO
	 */
	public TipoIncantesimo getTipo();
	
	/**
	 * La portata di questo incantesimo; GLOBALE, GRUPPO, SINGOLO_SOLO_VIVI, SINGOLO_QUALSIASI
	 */
	public PortataIncantesimo getPortata();

	/**
	 * Quante monete costa acquistarlo
	 */
	public int getCostoAcquisto();

	/**
	 * Quanta magia costa lanciarlo
	 * @return
	 */
	public int getCostoLancio();

}
