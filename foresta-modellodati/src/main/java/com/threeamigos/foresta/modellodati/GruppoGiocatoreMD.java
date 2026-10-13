package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.ClasseIncantesimo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Il gruppo del giocatore. Oltre a quanto tiene {@link GruppoMD} (personaggi, capo, pergamene e pozioni) porta con sé
 * le monete, le pietre preziose, la posizione sulla mappa, gli artefatti dell'inventario generale (quelli che nessun
 * personaggio sta usando), gli ospiti e il conto delle sfide a carta, forbici e sasso vinte.
 * <p>
 * Gli <b>ospiti</b> viaggiano con il gruppo senza farne parte; fra loro, solo quelli registrati come vulnerabili
 * possono essere attaccati dagli avversari.
 * <p>
 * È anche la parte del salvataggio che basta a mostrare la situazione di un salvataggio nell'elenco degli slot,
 * quindi si può rileggere da sola ({@link #leggi(BufferedReader)}).
 *
 * @author Stefano Reksten
 */
public class GruppoGiocatoreMD extends GruppoMD implements Serializzabile {

	private int monete;
	private int preziosi;
	// Coordinate all'interno della Foresta
	private CoordinateMD coordinate;
	// Artefatti disponibili al gruppo ma non in uso da un personaggio specifico
	private final Collection<ArtefattoMD> artefatti = new ArrayList<>();
	// Chi viaggia con il gruppo senza farne parte (vedi GruppoGiocatore.aggiungiOspite)
	private final List<PersonaggioMD> ospiti = new ArrayList<>();
	// Gli ospiti che gli avversari possono attaccare, per uuid
	private final Set<String> ospitiVulnerabili = new HashSet<>();
	// Le sfide a carta, forbici e sasso vinte in tutta la partita (vedi carta_forbici_sasso.md)
	private int sfideVinte;

	/**
	 * Le sfide a carta, forbici e sasso vinte in tutta la partita.
	 */
	public int getSfideVinte() {
		return sfideVinte;
	}

	public void setSfideVinte(int sfideVinte) {
		this.sfideVinte = sfideVinte;
	}

	/**
	 * Gli ospiti del gruppo: chi viaggia con i personaggi senza farne parte.
	 */
	public List<PersonaggioMD> getOspitiMD() {
		return ospiti;
	}

	/**
	 * Gli identificativi (uuid) degli ospiti che gli avversari possono attaccare.
	 */
	public Set<String> getOspitiVulnerabili() {
		return ospitiVulnerabili;
	}

	public int getMonete() {
		return monete;
	}

	public void setMonete(int monete) {
		this.monete = monete;
	}

	public int getPreziosi() {
		return preziosi;
	}

	public void setPreziosi(int preziosi) {
		this.preziosi = preziosi;
	}

	/**
	 * La posizione del gruppo sulla mappa della Foresta.
	 */
	public CoordinateMD getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(CoordinateMD coordinate) {
		this.coordinate = coordinate;
	}

	/**
	 * Gli artefatti del gruppo che nessun personaggio sta usando (l'inventario generale).
	 */
	public Collection<ArtefattoMD> getArtefatti() {
		return artefatti;
	}

	////////////////////////////////

	/**
	 * Azzera il gruppo per una partita nuova: oltre a quanto azzera {@link GruppoMD#reimposta()}, monete, pietre preziose,
	 * artefatti, ospiti e ospiti vulnerabili, e il conto delle sfide vinte.
	 */
	public void reimposta() {
		super.reimposta();
		monete = 0;
		preziosi = 0;
		artefatti.clear();
		ospiti.clear();
		ospitiVulnerabili.clear();
		sfideVinte = 0;
	}

	/**
	 * Scrive il gruppo (vedi {@link GruppoMD#salva(PrintWriter)}), poi una riga con monete, preziosi, pergamene, pozioni,
	 * posizione e sfide vinte, e infine gli artefatti, gli ospiti e gli identificativi degli ospiti vulnerabili, ciascun
	 * elenco preceduto dal suo numero di elementi.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		super.salva(stream);
		stream.print(monete);
		stream.print(PIPE);
		stream.print(preziosi);
		stream.print(PIPE);
		stream.print(incantesimi.size());
		stream.print(PIPE);
		for (Map.Entry<ClasseIncantesimo, Integer> incantesimo : incantesimi.entrySet()) {
			stream.print(incantesimo.getKey().name());
			stream.print(PIPE);
			stream.print(incantesimo.getValue());
			stream.print(PIPE);
		}
		stream.print(pozioniSalute);
		stream.print(PIPE);
		stream.print(pozioniSaluteGrande);
		stream.print(PIPE);
		stream.print(pozioniMagia);
		stream.print(PIPE);
		stream.print(pozioniMagiaGrande);
		stream.print(PIPE);
		stream.print(coordinate.getX());
		stream.print(PIPE);
		stream.print(coordinate.getY());
		stream.print(PIPE);
		stream.print(sfideVinte);
		stream.print(PIPE);
		stream.println(artefatti.size());

		for (ArtefattoMD artefatto : artefatti) {
			artefatto.salva(stream);
		}
		stream.println(ospiti.size());
		for (PersonaggioMD ospite : ospiti) {
			ospite.salva(stream);
		}
		stream.println(ospitiVulnerabili.size());
		for (String uuid : ospitiVulnerabili) {
			stream.println(uuid);
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo il contenuto attuale del gruppo.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		super.leggi(stream);
		String line = stream.readLine();
		LettoreCampi st = new LettoreCampi(line);
		monete = Integer.parseInt(st.testo());
		preziosi = Integer.parseInt(st.testo());
		incantesimi.clear();
		int numeroIncantesimi = st.intero();
		for (int i = 0; i < numeroIncantesimi; i++) {
			incantesimi.put(st.enumerato(ClasseIncantesimo.class), st.intero());
		}
		pozioniSalute = Integer.parseInt(st.testo());
		pozioniSaluteGrande = Integer.parseInt(st.testo());
		pozioniMagia = Integer.parseInt(st.testo());
		pozioniMagiaGrande = Integer.parseInt(st.testo());
		coordinate = new CoordinateMD(Integer.parseInt(st.testo()), Integer.parseInt(st.testo()));
		sfideVinte = Integer.parseInt(st.testo());
		int numeroArtefatti = Integer.parseInt(st.testo());

		artefatti.clear();
		for (int i = 0; i < numeroArtefatti; i++) {
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefatti.add(artefatto);
		}
		ospiti.clear();
		int numeroOspiti = Integer.parseInt(stream.readLine());
		for (int i = 0; i < numeroOspiti; i++) {
			PersonaggioMD ospite = new PersonaggioMD();
			ospite.leggi(stream);
			ospiti.add(ospite);
		}
		ospitiVulnerabili.clear();
		int numeroVulnerabili = Integer.parseInt(stream.readLine());
		for (int i = 0; i < numeroVulnerabili; i++) {
			ospitiVulnerabili.add(stream.readLine());
		}
	}
}
