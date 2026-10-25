package com.threeamigos.foresta.modellodati;


import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * Gli artefatti del mondo che non sono nell'inventario del gruppo: quelli ancora da distribuire, quelli smarriti nelle
 * locazioni (uno per casella, riconosciuti dalle coordinate) e le caselle in cui il gruppo sa che c'è un artefatto smarrito.
 * I magazzini dei negozi stanno nell'economia (MagazziniMD).
 * <p>
 * L'elenco di quelli da distribuire non si salva: serve solo quando si costruisce la foresta.
 *
 * @author Stefano Reksten
 */
public class RegistroArtefattiMD implements Serializzabile {

	private final List<ArtefattoMD> elencoIniziale = new ArrayList<>();

	private final Map<CoordinateMD, ArtefattoMD> artefattiSmarriti = new HashMap<>();
	// Locazioni di artefatti smarriti di cui il giocatore ha avuto notizia (Informazioni, in
	// locanda o per amicizia): vanno segnalate sulla mappa con l'indicatore finché
	// l'artefatto non viene recuperato.
	private final Set<CoordinateMD> localizzazioniConosciute = new HashSet<>();

	public Set<CoordinateMD> getLocalizzazioniConosciute() {
		return Collections.unmodifiableSet(localizzazioniConosciute);
	}

	/**
	 * Dimentica tutti gli artefatti e le localizzazioni conosciute.
	 */
	public void reimposta() {
		elencoIniziale.clear();
		artefattiSmarriti.clear();
		localizzazioniConosciute.clear();
	}

	/**
	 * Aggiunge un artefatto all'elenco di quelli ancora da distribuire.
	 */
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

	/**
	 * Mette l'artefatto smarrito nella casella indicata, al posto di quello che c'era.
	 */
	public final void addArtefattoInLocazione(ArtefattoMD artefatto, CoordinateMD coordinate) {
		artefattiSmarriti.put(coordinate, artefatto);
	}

	public final ArtefattoMD getArtefattoInLocazione(CoordinateMD coordinate) {
		return artefattiSmarriti.get(coordinate);
	}

	/**
	 * Toglie l'artefatto smarrito dalla casella, e con lui il fatto che il gruppo ne conoscesse la posizione.
	 */
	public final void rimuoviArtefattoInLocazione(CoordinateMD coordinate) {
		artefattiSmarriti.remove(coordinate);
		localizzazioniConosciute.remove(coordinate);
	}

	/**
	 * Ricorda che il gruppo sa che nella casella c'è un artefatto smarrito, così la mappa la segnala.
	 */
	public final void segnaLocalizzazioneConosciuta(CoordinateMD coordinate) {
		localizzazioniConosciute.add(coordinate);
	}

	public final boolean isLocalizzazioneConosciuta(CoordinateMD coordinate) {
		return localizzazioniConosciute.contains(coordinate);
	}

	/**
	 * Scrive gli artefatti smarriti (le coordinate e poi l'artefatto) e le localizzazioni conosciute,
	 * ogni gruppo preceduto dal numero di elementi.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(artefattiSmarriti.size());
		for (Map.Entry<CoordinateMD, ArtefattoMD> entry : artefattiSmarriti.entrySet()) {
			stampaRiga(stream,
					entry.getKey().getX(),
					entry.getKey().getY());
			entry.getValue().salva(stream);
		}
		stream.println(localizzazioniConosciute.size());
		for (CoordinateMD coordinate : localizzazioniConosciute) {
			stampaRiga(stream,
					coordinate.getX(),
					coordinate.getY());
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, al posto di quello che c'è già (artefatti smarriti e luoghi conosciuti).
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		artefattiSmarriti.clear();
		localizzazioniConosciute.clear();
		int numeroPersonaggi = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroPersonaggi; i++) {
			LettoreCampi st = new LettoreCampi(stream.readLine());
			CoordinateMD coordinate = new CoordinateMD(st.intero(), st.intero());
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefattiSmarriti.put(coordinate, artefatto);
		}
		int numeroLocalizzazioniConosciute = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroLocalizzazioniConosciute; i++) {
			LettoreCampi st = new LettoreCampi(stream.readLine());
			CoordinateMD coordinate = new CoordinateMD(st.intero(), st.intero());
			localizzazioniConosciute.add(coordinate);
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
