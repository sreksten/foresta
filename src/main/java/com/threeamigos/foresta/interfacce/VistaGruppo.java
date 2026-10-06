package com.threeamigos.foresta.interfacce;

import java.util.List;

/**
 * Un gruppo di personaggi visto dalla UI, in sola lettura (vedi VistaPartita). Lo implementa Gruppo.
 */
public interface VistaGruppo {

	int getNumeroPersonaggi();

	List<? extends VistaPersonaggio> getPersonaggi();

	List<? extends VistaPersonaggio> getPersonaggiVivi();

	/**
	 * @param numero da 0 a getNumeroPersonaggi() - 1
	 */
	VistaPersonaggio getPersonaggio(int numero);

	boolean contiene(VistaPersonaggio personaggio);

}
