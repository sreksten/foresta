package com.threeamigos.foresta.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static com.threeamigos.foresta.modellodati.Serializzabile.stampaRiga;

/**
 * I personaggi del mondo che non stanno nel gruppo del giocatore: quelli ancora da distribuire, quelli che si trovano
 * nelle locazioni (uno per casella, riconosciuti dalle coordinate) e i residenti, che hanno un nome nel mondo senza
 * stare in una locazione (vedi {@link ResidenteMD} e nomi.md).
 * <p>
 * L'elenco dei personaggi da distribuire non si salva: serve solo quando si costruisce la foresta.
 *
 * @author Stefano Reksten
 */
public class RegistroPersonaggiMD implements Serializzabile {

	private final List<PersonaggioMD> elencoIniziale = new ArrayList<>();

	private final Map<CoordinateMD, PersonaggioMD> personaggiInLocazione = new HashMap<>();

	// Chi ha un nome nel mondo senza stare in una locazione (vedi tipi.PersonaggioNoto), per chiave
	private final Map<String, ResidenteMD> residenti = new LinkedHashMap<>();

	/**
	 * I residenti per chiave, nell'ordine in cui sono stati aggiunti; la mappa non si può modificare.
	 */
	public final Map<String, ResidenteMD> getResidenti() {
		return Collections.unmodifiableMap(residenti);
	}

	/**
	 * Dimentica tutti i personaggi e i residenti.
	 */
	public void reimposta() {
		elencoIniziale.clear();
		personaggiInLocazione.clear();
		residenti.clear();
	}

	/**
	 * @return il residente con quella chiave, o null se non c'è
	 */
	public final ResidenteMD getResidente(String chiave) {
		return residenti.get(Serializzabile.pulisciCampo(chiave));
	}

	/**
	 * Aggiunge un residente con la sua chiave (vedi tipi.PersonaggioNoto); un residente con la stessa chiave viene sostituito.
	 * La chiave si pulisce come ogni campo di un salvataggio (senza "|" né a capo): le chiavi dei campioni portano il loro nome, che il
	 * residente ha già pulito, e una chiave sporca spezzerebbe la riga salvata.
	 */
	public final void aggiungiResidente(String chiave, ResidenteMD residente) {
		residenti.put(Serializzabile.pulisciCampo(chiave), residente);
	}

	/**
	 * Aggiunge un personaggio all'elenco di quelli ancora da distribuire.
	 */
	public void aggiungiPersonaggio(PersonaggioMD personaggioMD) {
		elencoIniziale.add(personaggioMD);
	}

	/**
	 * Toglie dall'elenco iniziale il personaggio in quella posizione (la sceglie a caso il motore).
	 */
	public final PersonaggioMD rimuoviDisponibile(int indice) {
		return elencoIniziale.remove(indice);
	}

	/**
	 * Quanti personaggi restano da distribuire.
	 */
	public final int getNumeroDisponibili() {
		return elencoIniziale.size();
	}

	/**
	 * Le coordinate delle caselle in cui si trova un personaggio; l'insieme non si può modificare.
	 */
	public final Set<CoordinateMD> getUbicazioniPersonaggi() {
		return Collections.unmodifiableSet(personaggiInLocazione.keySet());
	}

	/**
	 * Mette il personaggio nella casella indicata, al posto di quello che c'era.
	 */
	public final void addPersonaggioInLocazione(PersonaggioMD personaggio, CoordinateMD coordinate) {
		personaggiInLocazione.put(coordinate, personaggio);
	}

	/**
	 * @return il personaggio nella casella, o null se non ce n'è
	 */
	public final PersonaggioMD getPersonaggioInLocazione(CoordinateMD coordinate) {
		return personaggiInLocazione.get(coordinate);
	}

	/**
	 * Toglie il personaggio dalla casella, se c'è.
	 */
	public final void rimuoviPersonaggioInLocazione(CoordinateMD coordinate) {
		personaggiInLocazione.remove(coordinate);
	}

	/**
	 * Scrive i personaggi nelle locazioni (le coordinate su una riga e poi il personaggio) e i residenti (una riga ciascuno),
	 * ogni gruppo preceduto dal numero di elementi.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(personaggiInLocazione.size());
		for (Map.Entry<CoordinateMD, PersonaggioMD> entry : personaggiInLocazione.entrySet()) {
			stampaRiga(stream,
					entry.getKey().getX(),
					entry.getKey().getY());
			entry.getValue().salva(stream);
		}
		stream.println(residenti.size());
		for (Map.Entry<String, ResidenteMD> entry : residenti.entrySet()) {
			entry.getValue().salva(stream, entry.getKey());
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo i residenti ricordati finora.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		int numeroPersonaggi = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroPersonaggi; i++) {
			LettoreCampi st = new LettoreCampi(stream.readLine());
			CoordinateMD coordinate = new CoordinateMD(st.intero(), st.intero());
			PersonaggioMD personaggio = new PersonaggioMD();
			personaggio.leggi(stream);
			personaggiInLocazione.put(coordinate, personaggio);
		}
		residenti.clear();
		int numeroResidenti = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroResidenti; i++) {
			LettoreCampi campi = new LettoreCampi(stream.readLine());
			String chiave = campi.testo();
			residenti.put(chiave, ResidenteMD.leggi(campi));
		}
	}

}
