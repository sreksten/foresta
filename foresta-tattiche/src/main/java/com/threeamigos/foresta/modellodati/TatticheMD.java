package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;

import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Le tattiche dei personaggi della partita, ognuna con l'identificativo del personaggio a cui appartiene (il suo {@code uuid}): il
 * personaggio non sa di avere una tattica, la sa chi le governa. Chi non ne ha una (oggi i mostri) non compare.
 * <p>
 * Si salva così: una riga con il numero di tattiche e, per ognuna, una riga con l'identificativo del personaggio e la tattica con le
 * sue voci (vedi {@link TatticaMD#salva}).
 *
 * @author Stefano Reksten
 */
public final class TatticheMD implements Serializzabile {

	private final Map<String, TatticaMD> perPersonaggio = new LinkedHashMap<>();

	/**
	 * La tattica del personaggio con quell'identificativo, o null se non ne ha una.
	 */
	public TatticaMD get(String uuidPersonaggio) {
		return perPersonaggio.get(uuidPersonaggio);
	}

	/**
	 * Dà una tattica al personaggio con quell'identificativo; null gliela toglie.
	 */
	public void imposta(String uuidPersonaggio, TatticaMD tattica) {
		if (tattica == null) {
			perPersonaggio.remove(uuidPersonaggio);
		} else {
			perPersonaggio.put(uuidPersonaggio, tattica);
		}
	}

	/**
	 * Le tattiche per identificativo del personaggio, in sola lettura.
	 */
	public Map<String, TatticaMD> getTattiche() {
		return Collections.unmodifiableMap(perPersonaggio);
	}

	/**
	 * Toglie tutte le tattiche, come in una partita nuova.
	 */
	public void reimposta() {
		perPersonaggio.clear();
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(perPersonaggio.size());
		for (Map.Entry<String, TatticaMD> voce : perPersonaggio.entrySet()) {
			stampaRiga(stream, voce.getKey());
			voce.getValue().salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		perPersonaggio.clear();
		int quante = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < quante; i++) {
			String uuid = LettoreCampi.leggiTesto(stream);
			perPersonaggio.put(uuid, TatticaMD.leggi(stream));
		}
	}
}
