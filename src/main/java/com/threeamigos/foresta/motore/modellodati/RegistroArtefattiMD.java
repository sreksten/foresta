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
	private final Map<CoordinateMD, Collection<ArtefattoMD>> artefattiPerLocazione = new HashMap<>();

	public void reimposta() {
		elencoIniziale.clear();
		artefattiSmarriti.clear();
		artefattiPerLocazione.clear();
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
		stream.println(artefattiPerLocazione.size());
		for (Map.Entry<CoordinateMD, Collection<ArtefattoMD>> entry : artefattiPerLocazione.entrySet()) {
			CoordinateMD coordinate = entry.getKey();
			stream.print(coordinate.getX());
			stream.print(PIPE);
			stream.print(coordinate.getY());
			stream.println(entry.getValue().size());
			for (ArtefattoMD artefatto : entry.getValue()) {
				artefatto.salva(stream);
			}
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		int numeroPersonaggi = Integer.parseInt(line);
		for (int i = 0; i < numeroPersonaggi; i++) {
			line = stream.readLine();
			StringTokenizer st = new StringTokenizer(line, PIPE);
			CoordinateMD coordinate = new CoordinateMD(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefattiSmarriti.put(coordinate, artefatto);
		}
		line = stream.readLine();
		int locazioni = Integer.parseInt(line);
		for (int i = 0; i < locazioni; i++) {
			line = stream.readLine();
			StringTokenizer st = new StringTokenizer(line, PIPE);
			int x = Integer.parseInt(st.nextToken());
			int y = Integer.parseInt(st.nextToken());
			int totale = Integer.parseInt(st.nextToken());
			CoordinateMD coordinate = new CoordinateMD(x, y);
			for (int j = 0; j < totale; j++) {
				ArtefattoMD artefatto = new ArtefattoMD();
				artefatto.leggi(stream);
				artefattiPerLocazione.computeIfAbsent(coordinate, k-> new ArrayList<>()).add(artefatto);
			}
		}
	}

	public ScambiatoreArtefatti getScambiatorePerLocazione(CoordinateMD coordinate) {
		return new ScambiatoreArtefatti() {
			@Override
			public Collection<Artefatto> getInventario() {
				return artefattiPerLocazione
						.computeIfAbsent(coordinate, k-> new ArrayList<>())
						.stream()
						.map(Artefatto::di)
						.collect(Collectors.toList());
			}
			@Override
			public void addArtefatto(Artefatto artefatto) {
				artefattiPerLocazione
						.computeIfAbsent(coordinate, k-> new ArrayList<>())
						.add(artefatto.getModelloDati());
			}
			@Override
			public void removeArtefatto(Artefatto artefatto) {
				artefattiPerLocazione
						.computeIfAbsent(coordinate, k-> new ArrayList<>())
						.remove(artefatto.getModelloDati());
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
