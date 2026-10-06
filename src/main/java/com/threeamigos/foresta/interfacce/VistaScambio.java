package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.oggetti.Artefatto;

import java.util.Collection;

/**
 * Uno scambio di artefatti aperto (inventario, armaiolo, venditore di pergamene, incantatore) visto dalla UI, in sola
 * lettura: a sinistra la parte attiva (un personaggio o il gruppo), a destra la parte remota (il gruppo, un negozio,
 * il banco di lavoro). La UI sposta gli artefatti con ComandoScambioArtefatto; lo implementa AutomaScambiatoreArtefatti.
 */
public interface VistaScambio {

	Collection<Artefatto> getInventarioParteAttiva();

	Collection<Artefatto> getInventarioParteRemota();

	/**
	 * Se accanto agli artefatti della parte attiva si mostra quanto si ricava vendendoli
	 */
	boolean mostraCostoSuParteAttiva();

	/**
	 * Se accanto agli artefatti della parte remota si mostra quanto si paga comprandoli
	 */
	boolean mostraCostoSuParteRemota();

}
