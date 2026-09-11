package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.oggetti.Artefatto;

import java.util.Collection;

/**
 * Gestisce lo scambio di artefatti tra l'inventario di un personaggio e un pool generico
 * di artefatti disponibili (tipicamente quello del gruppo, in futuro anche quello di un PNG),
 * appoggiandosi in entrambi i casi al contratto di ScambiatoreArtefatti.
 */
public class AutomaInventario {

	/**
	 * L'oggetto che attivamente decide di dare via o prelevare artefatti
	 * (ad esempio, il giocatore che mette gli inventari nel gruppo generale, oppure
	 * il gruppo generale che interagisce con un venditore)
	 */
	private final ScambiatoreArtefatti parteAttiva;
	/**
	 * L'oggetto che passivamente riceve o invia artefatti (può essere un venditore, un magazzino...)
	 */
	private final ScambiatoreArtefatti parteRemota;

	public AutomaInventario(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota) {
		this.parteAttiva = parteAttiva;
		this.parteRemota = parteRemota;
	}

	public ScambiatoreArtefatti getParteAttiva() {
		return parteAttiva;
	}

	public Collection<Artefatto> getArtefattiDisponibili() {
		return parteRemota.getInventario();
	}

	public void spostaSuParteAttiva(Artefatto artefatto) {
		parteRemota.removeArtefatto(artefatto);
		parteAttiva.addArtefatto(artefatto);
	}

	public void spostaSuParteRemota(Artefatto artefatto) {
		parteAttiva.removeArtefatto(artefatto);
		parteRemota.addArtefatto(artefatto);
	}
}
