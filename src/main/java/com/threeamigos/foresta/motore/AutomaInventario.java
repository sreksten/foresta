package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Collection;

/**
 * Gestisce lo scambio di artefatti tra l'inventario di un personaggio e un pool generico
 * di artefatti disponibili (tipicamente quello del gruppo, in futuro anche quello di un PNG),
 * appoggiandosi in entrambi i casi al contratto di ScambiatoreArtefatti.
 */
public class AutomaInventario {

	private final Personaggio personaggio;
	private final ScambiatoreArtefatti pool;

	public AutomaInventario(Personaggio personaggio, ScambiatoreArtefatti pool) {
		this.personaggio = personaggio;
		this.pool = pool;
	}

	public Personaggio getPersonaggio() {
		return personaggio;
	}

	public Collection<Artefatto> getArtefattiDisponibili() {
		return pool.getInventario();
	}

	public void spostaNelPersonaggio(Artefatto artefatto) {
		pool.removeArtefatto(artefatto);
		personaggio.addArtefatto(artefatto);
	}

	public void spostaNelPool(Artefatto artefatto) {
		personaggio.removeArtefatto(artefatto);
		pool.addArtefatto(artefatto);
	}
}
