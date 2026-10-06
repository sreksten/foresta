package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoScambioArtefatto;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.oggetti.Artefatto;

import java.util.Collection;

/**
 * Gestisce lo scambio di artefatti tra l'inventario di un personaggio e un pool generico
 * di artefatti disponibili (tipicamente quello del gruppo, in futuro anche quello di un PNG),
 * appoggiandosi in entrambi i casi al contratto di ScambiatoreArtefatti.
 * <p>
 * La UI lo vede solo come VistaScambio, che riceve con l'evento di apertura della schermata, e gli chiede di spostare
 * un artefatto con ComandoScambioArtefatto (vedi esegui).
 */
public abstract class AutomaScambiatoreArtefatti implements VistaScambio {

	/**
	 * Lo spostamento chiesto dalla UI, fatto dallo scambio da cui viene (l'Automa ci si iscrive). Lo scambio è uno di
	 * questi automi: la UI ne conosce solo la vista.
	 */
	static void esegui(ComandoScambioArtefatto comando) {
		AutomaScambiatoreArtefatti scambio = (AutomaScambiatoreArtefatti) comando.getScambio();
		Artefatto artefatto = Artefatto.da(comando.getArtefatto());
		if (comando.getDestinazione() == ComandoScambioArtefatto.Destinazione.PARTE_ATTIVA) {
			scambio.richiediSpostamentoSuParteAttiva(artefatto);
		} else {
			scambio.richiediSpostamentoSuParteRemota(artefatto);
		}
	}

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

	public AutomaScambiatoreArtefatti(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota) {
		this.parteAttiva = parteAttiva;
		this.parteRemota = parteRemota;
	}

	public ScambiatoreArtefatti getParteAttiva() {
		return parteAttiva;
	}

	@Override
	public Collection<Artefatto> getInventarioParteAttiva() {
		return parteAttiva.getInventario();
	}

	@Override
	public abstract boolean mostraCostoSuParteAttiva();

	public ScambiatoreArtefatti getParteRemota() {
		return parteRemota;
	}

	@Override
	public Collection<Artefatto> getInventarioParteRemota() {
		return parteRemota.getInventario();
	}

	@Override
	public abstract boolean mostraCostoSuParteRemota();

	public abstract void richiediSpostamentoSuParteAttiva(Artefatto artefatto);

	public void spostaSuParteAttiva(Artefatto artefatto) {
		parteRemota.removeArtefatto(artefatto);
		parteAttiva.addArtefatto(artefatto);
	}

	public abstract void richiediSpostamentoSuParteRemota(Artefatto artefatto);

	public void spostaSuParteRemota(Artefatto artefatto) {
		parteAttiva.removeArtefatto(artefatto);
		parteRemota.addArtefatto(artefatto);
	}
}
