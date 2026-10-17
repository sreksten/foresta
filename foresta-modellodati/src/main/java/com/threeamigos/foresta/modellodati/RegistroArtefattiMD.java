package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoNegozio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Predicate;

import static com.threeamigos.foresta.modellodati.Serializzabile.stampaRiga;

/**
 * Gli artefatti del mondo che non sono nell'inventario del gruppo: quelli ancora da distribuire, quelli smarriti nelle
 * locazioni (uno per casella, riconosciuti dalle coordinate), i magazzini dei negozi (per tipo di negozio e per città) e
 * le caselle in cui il gruppo sa che c'è un artefatto smarrito.
 * <p>
 * L'elenco di quelli da distribuire non si salva: serve solo quando si costruisce la foresta.
 *
 * @author Stefano Reksten
 */
public class RegistroArtefattiMD implements Serializzabile {

	private final List<ArtefattoMD> elencoIniziale = new ArrayList<>();

	private final Map<CoordinateMD, ArtefattoMD> artefattiSmarriti = new HashMap<>();
	// I magazzini dei negozi: più negozi della stessa città hanno la stessa coordinata
	private final Map<TipoNegozio, Map<CoordinateMD, List<ArtefattoMD>>> magazzini = new EnumMap<>(TipoNegozio.class);
	// Locazioni di artefatti smarriti di cui il giocatore ha avuto notizia (Informazioni, in
	// locanda o per amicizia): vanno segnalate sulla mappa con l'indicatore finché
	// l'artefatto non viene recuperato.
	private final Set<CoordinateMD> localizzazioniConosciute = new HashSet<>();

	/**
	 * Dimentica tutti gli artefatti, i magazzini e le localizzazioni conosciute.
	 */
	public void reimposta() {
		elencoIniziale.clear();
		artefattiSmarriti.clear();
		magazzini.clear();
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

	public Set<CoordinateMD> getLocalizzazioniConosciute() {
		return Collections.unmodifiableSet(localizzazioniConosciute);
	}

	/**
	 * Scrive gli artefatti smarriti (le coordinate e poi l'artefatto), i magazzini dei negozi e le localizzazioni conosciute,
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
		stream.println(localizzazioniConosciute.size());
		for (CoordinateMD coordinate : localizzazioniConosciute) {
			stampaRiga(stream,
					coordinate.getX(),
					coordinate.getY());
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, aggiungendolo a quello che c'è già.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		int numeroPersonaggi = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroPersonaggi; i++) {
			LettoreCampi st = new LettoreCampi(stream.readLine());
			CoordinateMD coordinate = new CoordinateMD(st.intero(), st.intero());
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefattiSmarriti.put(coordinate, artefatto);
		}
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
		int numeroLocalizzazioniConosciute = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroLocalizzazioniConosciute; i++) {
			LettoreCampi st = new LettoreCampi(stream.readLine());
			CoordinateMD coordinate = new CoordinateMD(st.intero(), st.intero());
			localizzazioniConosciute.add(coordinate);
		}
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
