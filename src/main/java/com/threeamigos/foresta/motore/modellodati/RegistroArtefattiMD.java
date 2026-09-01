package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.motore.Dado;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class RegistroArtefattiMD implements Serializzabile {

	private final List<ArtefattoMD> elencoIniziale = new ArrayList<>();

	private final Map<CoordinateMD, ArtefattoMD> artefattiInLocazione = new HashMap<>();

	public void reimposta() {
		elencoIniziale.clear();
		artefattiInLocazione.clear();
	}

	public void aggiungiArtefatto(ArtefattoMD artefattoMD) {
		elencoIniziale.add(artefattoMD);
	}

	public final ArtefattoMD getArtefattoDisponibile() {
		return Dado.selezionaCasualmente(elencoIniziale);
	}

	public final ArtefattoESuaUbicazione getArtefattoCasuale() {
		if (!artefattiInLocazione.isEmpty()) {
            ArrayList<CoordinateMD> elencoCoordinate = new ArrayList<>(artefattiInLocazione.keySet());
			int indice = Dado.tira(elencoCoordinate.size()) - 1;
			CoordinateMD coordinate = elencoCoordinate.get(indice);
			ArtefattoMD artefatto = artefattiInLocazione.get(coordinate);
			return new ArtefattoESuaUbicazione(artefatto, coordinate);
		}
		return null;
	}

	public final void addArtefattoInLocazione(ArtefattoMD artefatto, CoordinateMD coordinate) {
		artefattiInLocazione.put(coordinate, artefatto);
	}

	public final ArtefattoMD getArtefattoInLocazione(CoordinateMD coordinate) {
		return artefattiInLocazione.get(coordinate);
	}

	public final void rimuoviArtefattoInLocazione(CoordinateMD coordinate) {
		artefattiInLocazione.remove(coordinate);
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(artefattiInLocazione.size());
		for (Map.Entry<CoordinateMD, ArtefattoMD> entry : artefattiInLocazione.entrySet()) {
			stream.print(entry.getKey().getX());
			stream.print(PIPE);
			stream.println(entry.getKey().getY());
			entry.getValue().salva(stream);
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
			artefattiInLocazione.put(coordinate, artefatto);
		}
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
