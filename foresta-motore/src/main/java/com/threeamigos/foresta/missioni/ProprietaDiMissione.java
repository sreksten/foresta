package com.threeamigos.foresta.missioni;

/**
 * I nomi delle proprietà di una missione che qualcuno fuori dalla missione stessa deve poter leggere (con
 * {@link Missione#ottieniProprieta}), senza conoscerne la classe.
 */
public final class ProprietaDiMissione {

	/**
	 * La città dove va la moglie del bardo (vedi IlViaggioDellaMoglie); il valore è il nome di un TipoLocazione.
	 */
	public static final String CITTA_DI_ARRIVO = "CITTA_DI_ARRIVO";

	private ProprietaDiMissione() {
	}
}
