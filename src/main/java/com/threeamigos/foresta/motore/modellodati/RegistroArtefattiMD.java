package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.tipi.TipoNegozio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Predicate;

public class RegistroArtefattiMD implements Serializzabile {

	private final List<ArtefattoMD> elencoIniziale = new ArrayList<>();

	private final Map<CoordinateMD, ArtefattoMD> artefattiSmarriti = new HashMap<>();
	// I magazzini dei negozi: più negozi della stessa città hanno la stessa coordinata
	private final Map<TipoNegozio, Map<CoordinateMD, Collection<ArtefattoMD>>> magazzini = new EnumMap<>(TipoNegozio.class);
	// Locazioni di artefatti smarriti di cui il giocatore ha avuto notizia (Informazioni, in
	// locanda o per amicizia): vanno segnalate sulla mappa con Indicatore.gif finché
	// l'artefatto non viene recuperato.
	private final Set<CoordinateMD> localizzazioniConosciute = new HashSet<>();

	public void reimposta() {
		elencoIniziale.clear();
		artefattiSmarriti.clear();
		magazzini.clear();
		localizzazioniConosciute.clear();
	}

	public void aggiungiArtefatto(ArtefattoMD artefattoMD) {
		elencoIniziale.add(artefattoMD);
	}

	public final int getNumeroDisponibili() {
		return elencoIniziale.size();
	}

	/**
	 * Toglie dall'elenco iniziale l'artefatto in quella posizione (la sceglie a caso il motore).
	 */
	public final ArtefattoMD rimuoviDisponibile(int indice) {
		return elencoIniziale.remove(indice);
	}

	public final Set<CoordinateMD> getUbicazioniArtefattiSmarriti() {
		return Collections.unmodifiableSet(artefattiSmarriti.keySet());
	}

	public final void addArtefattoInLocazione(ArtefattoMD artefatto, CoordinateMD coordinate) {
		artefattiSmarriti.put(coordinate, artefatto);
	}

	public final ArtefattoMD getArtefattoInLocazione(CoordinateMD coordinate) {
		return artefattiSmarriti.get(coordinate);
	}

	public final void rimuoviArtefattoInLocazione(CoordinateMD coordinate) {
		artefattiSmarriti.remove(coordinate);
		localizzazioniConosciute.remove(coordinate);
	}

	public final void segnaLocalizzazioneConosciuta(CoordinateMD coordinate) {
		localizzazioniConosciute.add(coordinate);
	}

	public final boolean isLocalizzazioneConosciuta(CoordinateMD coordinate) {
		return localizzazioniConosciute.contains(coordinate);
	}

	public Set<CoordinateMD> getLocalizzazioniConosciute() {
		return Collections.unmodifiableSet(localizzazioniConosciute);
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(artefattiSmarriti.size());
		for (Map.Entry<CoordinateMD, ArtefattoMD> entry : artefattiSmarriti.entrySet()) {
			stream.print(entry.getKey().getX());
			stream.print(PIPE);
			stream.println(entry.getKey().getY());
			entry.getValue().salva(stream);
		}
		stream.println(magazzini.values().stream().mapToInt(Map::size).sum());
		for (Map.Entry<TipoNegozio, Map<CoordinateMD, Collection<ArtefattoMD>>> negozio : magazzini.entrySet()) {
			for (Map.Entry<CoordinateMD, Collection<ArtefattoMD>> entry : negozio.getValue().entrySet()) {
				CoordinateMD coordinate = entry.getKey();
				stream.print(coordinate.getX());
				stream.print(PIPE);
				stream.print(coordinate.getY());
				stream.print(PIPE);
				stream.print(negozio.getKey().name());
				stream.print(PIPE);
				stream.println(entry.getValue().size());
				for (ArtefattoMD artefatto : entry.getValue()) {
					artefatto.salva(stream);
				}
			}
		}
		stream.println(localizzazioniConosciute.size());
		for (CoordinateMD coordinate : localizzazioniConosciute) {
			stream.print(coordinate.getX());
			stream.print(PIPE);
			stream.println(coordinate.getY());
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		int numeroPersonaggi = Integer.parseInt(line);
		for (int i = 0; i < numeroPersonaggi; i++) {
			line = stream.readLine();
			LettoreCampi st = new LettoreCampi(line);
			CoordinateMD coordinate = new CoordinateMD(Integer.parseInt(st.testo()), Integer.parseInt(st.testo()));
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefattiSmarriti.put(coordinate, artefatto);
		}
		line = stream.readLine();
		int numeroMagazzini = Integer.parseInt(line);
		for (int i = 0; i < numeroMagazzini; i++) {
			line = stream.readLine();
			LettoreCampi st = new LettoreCampi(line);
			int x = Integer.parseInt(st.testo());
			int y = Integer.parseInt(st.testo());
			TipoNegozio negozio = TipoNegozio.valueOf(st.testo());
			int totale = Integer.parseInt(st.testo());
			Collection<ArtefattoMD> magazzino = getMagazzino(new CoordinateMD(x, y), negozio);
			for (int j = 0; j < totale; j++) {
				ArtefattoMD artefatto = new ArtefattoMD();
				artefatto.leggi(stream);
				magazzino.add(artefatto);
			}
		}
		line = stream.readLine();
		int numeroLocalizzazioniConosciute = Integer.parseInt(line);
		for (int i = 0; i < numeroLocalizzazioniConosciute; i++) {
			line = stream.readLine();
			LettoreCampi st = new LettoreCampi(line);
			CoordinateMD coordinate = new CoordinateMD(Integer.parseInt(st.testo()), Integer.parseInt(st.testo()));
			localizzazioniConosciute.add(coordinate);
		}
	}

	/**
	 * Le città in cui un negozio ha un magazzino.
	 */
	public Set<CoordinateMD> getCoordinateMagazzini(TipoNegozio negozio) {
		return new HashSet<>(magazzini.computeIfAbsent(negozio, k -> new HashMap<>()).keySet());
	}

	/**
	 * Toglie l'artefatto dal magazzino, se c'è ancora.
	 *
	 * @return true se c'era
	 */
	public boolean rimuoviDaMagazzino(CoordinateMD coordinate, TipoNegozio negozio, ArtefattoMD artefatto) {
		return getMagazzino(coordinate, negozio).remove(artefatto);
	}

	/**
	 * Toglie dal magazzino gli artefatti che non soddisfano la condizione.
	 */
	public void tieniInMagazzino(CoordinateMD coordinate, TipoNegozio negozio, Predicate<ArtefattoMD> daTenere) {
		getMagazzino(coordinate, negozio).removeIf(daTenere.negate());
	}

	/**
	 * Il magazzino del negozio in quella città, modificabile (lo crea vuoto se ancora non c'è).
	 */
	public Collection<ArtefattoMD> getMagazzino(CoordinateMD coordinate, TipoNegozio negozio) {
		return magazzini
				.computeIfAbsent(negozio, k -> new HashMap<>())
				.computeIfAbsent(coordinate, k -> new ArrayList<>());
	}

	public static class ArtefattoESuaUbicazione {

		private final ArtefattoMD artefattoMD;
		private final CoordinateMD coordinate;

		public ArtefattoESuaUbicazione(ArtefattoMD artefattoMD, CoordinateMD coordinate) {
			this.artefattoMD = artefattoMD;
			this.coordinate = coordinate;
		}

		public ArtefattoMD getArtefattoMD() {
			return artefattoMD;
		}

		public CoordinateMD getCoordinate() {
			return coordinate;
		}
	}
}
