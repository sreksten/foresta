package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.oggetti.Artefatto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class RegistroArtefattiMD implements Serializzabile {

	private final List<ArtefattoMD> elencoIniziale = new ArrayList<>();

	private final Map<CoordinateMD, ArtefattoMD> artefattiSmarriti = new HashMap<>();
	// I magazzini dei negozi: più negozi della stessa città hanno la stessa coordinata
	private final Map<TipoNegozio, Map<CoordinateMD, Collection<ArtefattoMD>>> magazzini = new EnumMap<>(TipoNegozio.class);

	public void reimposta() {
		elencoIniziale.clear();
		artefattiSmarriti.clear();
		magazzini.clear();
	}

	public void aggiungiArtefatto(ArtefattoMD artefattoMD) {
		elencoIniziale.add(artefattoMD);
	}

	public final ArtefattoMD getArtefattoDisponibile() {
		return Dado.selezionaCasualmente(elencoIniziale);
	}

	public final ArtefattoESuaUbicazione getArtefattoCasuale() {
		if (!artefattiSmarriti.isEmpty()) {
            ArrayList<CoordinateMD> elencoCoordinate = new ArrayList<>(artefattiSmarriti.keySet());
			int indice = Dado.tiraAncheAUnaFaccia(elencoCoordinate.size()) - 1;
			CoordinateMD coordinate = elencoCoordinate.get(indice);
			ArtefattoMD artefatto = artefattiSmarriti.get(coordinate);
			return new ArtefattoESuaUbicazione(artefatto, coordinate);
		}
		return null;
	}

	public final void addArtefattoInLocazione(ArtefattoMD artefatto, CoordinateMD coordinate) {
		artefattiSmarriti.put(coordinate, artefatto);
	}

	public final ArtefattoMD getArtefattoInLocazione(CoordinateMD coordinate) {
		return artefattiSmarriti.get(coordinate);
	}

	public final void rimuoviArtefattoInLocazione(CoordinateMD coordinate) {
		artefattiSmarriti.remove(coordinate);
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
	}

	private Collection<ArtefattoMD> getMagazzino(CoordinateMD coordinate, TipoNegozio negozio) {
		return magazzini
				.computeIfAbsent(negozio, k -> new HashMap<>())
				.computeIfAbsent(coordinate, k -> new ArrayList<>());
	}

	public ScambiatoreArtefatti getScambiatorePerNegozio(CoordinateMD coordinate, TipoNegozio negozio) {
		return new ScambiatoreArtefatti() {
			@Override
			public Collection<Artefatto> getInventario() {
				return getMagazzino(coordinate, negozio)
						.stream()
						.map(Artefatto::di)
						.collect(Collectors.toList());
			}
			@Override
			public void addArtefatto(Artefatto artefatto) {
				getMagazzino(coordinate, negozio).add(artefatto.getModelloDati());
			}
			@Override
			public void removeArtefatto(Artefatto artefatto) {
				getMagazzino(coordinate, negozio).remove(artefatto.getModelloDati());
			}
			@Override
			public boolean tratta(Artefatto artefatto) {
				return negozio.tratta(artefatto.getTipo());
			}
		};
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
