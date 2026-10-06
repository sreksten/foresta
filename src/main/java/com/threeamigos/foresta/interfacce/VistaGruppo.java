package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.List;

/**
 * Un gruppo di personaggi visto dalla UI, in sola lettura (vedi VistaPartita). Lo implementa Gruppo.
 */
public interface VistaGruppo {

	int getNumeroPersonaggi();

	List<Personaggio> getPersonaggi();

	List<Personaggio> getPersonaggiVivi();

	/**
	 * @param numero da 0 a getNumeroPersonaggi() - 1
	 */
	Personaggio getPersonaggio(int numero);

	boolean contiene(Personaggio personaggio);

}
