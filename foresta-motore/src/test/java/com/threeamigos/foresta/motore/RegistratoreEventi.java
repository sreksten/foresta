package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tiene gli eventi pubblicati sul bus dei tipi indicati, in ordine di arrivo, al posto della UI.
 * Il bus consegna per classe esatta: vanno indicati i tipi concreti che interessano.
 */
final class RegistratoreEventi {

	private final List<Object> eventi = Collections.synchronizedList(new ArrayList<>());

	void ascolta(Class<?>... tipi) {
		for (Class<?> tipo : tipi) {
			BusEventi.iscriviti(tipo, eventi::add);
		}
	}

	<T> List<T> tutti(Class<T> tipo) {
		synchronized (eventi) {
			return eventi.stream().filter(tipo::isInstance).map(tipo::cast).collect(Collectors.toList());
		}
	}

	/**
	 * Gli eventi di uno qualsiasi dei tipi indicati, nell'ordine in cui sono arrivati.
	 */
	List<Object> inOrdine(Class<?>... tipi) {
		synchronized (eventi) {
			return eventi.stream()
					.filter(e -> java.util.Arrays.stream(tipi).anyMatch(t -> t.isInstance(e)))
					.collect(Collectors.toList());
		}
	}

	<T> T ultimo(Class<T> tipo) {
		List<T> tutti = tutti(tipo);
		if (tutti.isEmpty()) {
			throw new AssertionError("Nessun evento " + tipo.getSimpleName() + " pubblicato");
		}
		return tutti.get(tutti.size() - 1);
	}

	boolean haRicevuto(Class<?> tipo) {
		return !tutti(tipo).isEmpty();
	}

	void svuota() {
		eventi.clear();
	}
}
