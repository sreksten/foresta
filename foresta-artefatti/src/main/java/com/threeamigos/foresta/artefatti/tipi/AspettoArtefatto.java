package com.threeamigos.foresta.artefatti.tipi;

/**
 * L'aspetto speciale di un artefatto: l'immagine che ha nelle schermate quando non è quella del suo {@link TipoArtefatto}. Gli
 * artefatti normali non ne hanno uno (il loro aspetto è il loro tipo: una spada ha l'immagine di una spada); lo hanno i pochi
 * che ne hanno una tutta loro.
 * <p>
 * Non è il tipo di bottino che si trova per terra ({@code TipoBottino}): questi aspetti non si trovano in nessuna locazione.
 *
 * @author Stefano Reksten
 */
public enum AspettoArtefatto {

	/**
	 * Dà l'aspetto ai libri tecnici, libri magici leggendari che stanno solo nei cofani (vedi LibroTecnico).
	 */
	LIBRO_TECNICO,

	/**
	 * Dà l'aspetto alla spada normalmente nascosta, che il gruppo ottiene solo con un intermezzo (vedi SpadaNormalmenteNascosta).
	 */
	SPADA_NORMALMENTE_NASCOSTA

}
