package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoScambioArtefatto;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.oggetti.Artefatto;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
		Optional<AutomaScambiatoreArtefatti> scambio = trova(comando.getIdScambio());
		if (!scambio.isPresent()) {
			return;
		}
		// L'artefatto sta nella parte da cui parte lo spostamento
		boolean versoParteAttiva = comando.getDestinazione() == ComandoScambioArtefatto.Destinazione.PARTE_ATTIVA;
		Collection<Artefatto> partenza = versoParteAttiva
				? scambio.get().getInventarioParteRemota() : scambio.get().getInventarioParteAttiva();
		Optional<Artefatto> artefatto = cerca(partenza, comando.getUuidArtefatto());
		if (!artefatto.isPresent()) {
			return;
		}
		if (versoParteAttiva) {
			scambio.get().richiediSpostamentoSuParteAttiva(artefatto.get());
		} else {
			scambio.get().richiediSpostamentoSuParteRemota(artefatto.get());
		}
	}

	/**
	 * Gli scambi che la UI può nominare nei comandi, per identificativo. I riferimenti sono deboli: uno scambio
	 * dura finché una schermata lo tiene, poi non lo si può più chiamare.
	 */
	private static final Map<String, WeakReference<AutomaScambiatoreArtefatti>> SCAMBI = new ConcurrentHashMap<>();

	/**
	 * Lo scambio con quell'identificativo, se esiste ancora
	 */
	static Optional<AutomaScambiatoreArtefatti> trova(String id) {
		if (id == null) {
			return Optional.empty();
		}
		WeakReference<AutomaScambiatoreArtefatti> riferimento = SCAMBI.get(id);
		return Optional.ofNullable(riferimento == null ? null : riferimento.get());
	}

	private static Optional<Artefatto> cerca(Collection<Artefatto> inventario, String uuid) {
		if (uuid == null) {
			return Optional.empty();
		}
		return inventario.stream().filter(a -> uuid.equals(a.getUuid())).findFirst();
	}

	/**
	 * L'artefatto con quell'uuid in una delle due parti dello scambio, se c'è
	 */
	Optional<Artefatto> trovaArtefatto(String uuid) {
		Optional<Artefatto> artefatto = cerca(getInventarioParteAttiva(), uuid);
		return artefatto.isPresent() ? artefatto : cerca(getInventarioParteRemota(), uuid);
	}

	private final String id = UUID.randomUUID().toString();

	@Override
	public final String getId() {
		return id;
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
		SCAMBI.values().removeIf(riferimento -> riferimento.get() == null);
		SCAMBI.put(id, new WeakReference<>(this));
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
