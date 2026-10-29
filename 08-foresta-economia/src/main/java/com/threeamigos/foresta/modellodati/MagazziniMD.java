package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.tipi.TipoNegozio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * I magazzini dei negozi: gli artefatti in vendita, per tipo di negozio e per città (o bottega della Foresta).
 *
 * @author Stefano Reksten
 */
public class MagazziniMD implements Serializzabile {

	// Più negozi della stessa città hanno la stessa coordinata
	private final Map<TipoNegozio, Map<CoordinateMD, List<ArtefattoMD>>> magazzini = new EnumMap<>(TipoNegozio.class);

	/**
	 * Svuota tutti i magazzini, come in una partita nuova.
	 */
	public void reimposta() {
		magazzini.clear();
	}

	/**
	 * Le città in cui un negozio ha un magazzino.
	 */
	public Set<CoordinateMD> getCoordinateMagazzini(TipoNegozio negozio) {
		Map<CoordinateMD, List<ArtefattoMD>> delNegozio = magazzini.get(negozio);
		return delNegozio == null ? new HashSet<>() : new HashSet<>(delNegozio.keySet());
	}

	/**
	 * Mette l'artefatto nel magazzino del negozio in quella città, che nasce vuoto se ancora non c'è.
	 */
	public void aggiungiAMagazzino(CoordinateMD coordinate, TipoNegozio negozio, ArtefattoMD artefatto) {
		magazzinoModificabile(coordinate, negozio).add(artefatto);
	}

	/**
	 * Toglie l'artefatto dal magazzino, se c'è ancora.
	 *
	 * @return true se c'era
	 */
	public boolean rimuoviDaMagazzino(CoordinateMD coordinate, TipoNegozio negozio, ArtefattoMD artefatto) {
		List<ArtefattoMD> magazzino = magazzinoEsistente(coordinate, negozio);
		return magazzino != null && magazzino.remove(artefatto);
	}

	/**
	 * Toglie dal magazzino gli artefatti che non soddisfano la condizione.
	 */
	public void tieniInMagazzino(CoordinateMD coordinate, TipoNegozio negozio, Predicate<ArtefattoMD> daTenere) {
		List<ArtefattoMD> magazzino = magazzinoEsistente(coordinate, negozio);
		if (magazzino != null) {
			magazzino.removeIf(daTenere.negate());
		}
	}

	/**
	 * Il magazzino del negozio in quella città, di sola lettura e vuoto se non ce n'è uno (leggerlo non lo crea). Per
	 * cambiarlo si usano {@link #aggiungiAMagazzino}, {@link #rimuoviDaMagazzino} e {@link #tieniInMagazzino}.
	 */
	public List<ArtefattoMD> getMagazzino(CoordinateMD coordinate, TipoNegozio negozio) {
		List<ArtefattoMD> magazzino = magazzinoEsistente(coordinate, negozio);
		return magazzino == null ? Collections.emptyList() : Collections.unmodifiableList(magazzino);
	}

	// Null se il negozio non ha un magazzino in quella città
	private List<ArtefattoMD> magazzinoEsistente(CoordinateMD coordinate, TipoNegozio negozio) {
		Map<CoordinateMD, List<ArtefattoMD>> delNegozio = magazzini.get(negozio);
		return delNegozio == null ? null : delNegozio.get(coordinate);
	}

	private List<ArtefattoMD> magazzinoModificabile(CoordinateMD coordinate, TipoNegozio negozio) {
		return magazzini
				.computeIfAbsent(negozio, k -> new HashMap<>())
				.computeIfAbsent(coordinate, k -> new ArrayList<>());
	}

	/**
	 * Scrive i magazzini: il loro numero e poi, per ognuno, una riga con le coordinate, il negozio e quanti artefatti ha, seguita
	 * dagli artefatti.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(magazzini.values().stream().mapToInt(Map::size).sum());
		for (Map.Entry<TipoNegozio, Map<CoordinateMD, List<ArtefattoMD>>> negozio : magazzini.entrySet()) {
			for (Map.Entry<CoordinateMD, List<ArtefattoMD>> entry : negozio.getValue().entrySet()) {
				CoordinateMD coordinate = entry.getKey();
				stampaRiga(stream,
						coordinate.getX(),
						coordinate.getY(),
						negozio.getKey(),
						entry.getValue().size());
				for (ArtefattoMD artefatto : entry.getValue()) {
					artefatto.salva(stream);
				}
			}
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, al posto dei magazzini che ci sono già.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		magazzini.clear();
		int numeroMagazzini = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroMagazzini; i++) {
			LettoreCampi st = new LettoreCampi(stream.readLine());
			int x = st.intero();
			int y = st.intero();
			TipoNegozio negozio = st.enumerato(TipoNegozio.class);
			int totale = st.intero();
			List<ArtefattoMD> magazzino = magazzinoModificabile(new CoordinateMD(x, y), negozio);
			for (int j = 0; j < totale; j++) {
				ArtefattoMD artefatto = new ArtefattoMD();
				artefatto.leggi(stream);
				magazzino.add(artefatto);
			}
		}
	}
}
