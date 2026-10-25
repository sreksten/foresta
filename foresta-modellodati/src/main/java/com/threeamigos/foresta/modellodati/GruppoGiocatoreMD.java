package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.artefatti.ArtefattoMD;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * Il gruppo del giocatore. Oltre a quanto tiene {@link GruppoMD} (personaggi, capo, pergamene e pozioni) porta con sé
 * le monete, le pietre preziose, la posizione sulla mappa, gli artefatti dell'inventario generale (quelli che nessun
 * personaggio sta usando), gli ospiti, il conto delle sfide a carta, forbici e sasso vinte e i canti imparati strada facendo.
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
	private final List<ArtefattoMD> artefatti = new ArrayList<>();
	// Chi viaggia con il gruppo senza farne parte (vedi GruppoGiocatore.aggiungiOspite)
	private final List<PersonaggioMD> ospiti = new ArrayList<>();
	// Gli ospiti che gli avversari possono attaccare, per uuid
	private final Set<String> ospitiVulnerabili = new HashSet<>();
	// Le sfide a carta, forbici e sasso vinte in tutta la partita (vedi carta_forbici_sasso.md)
	private int sfideVinte;
	// Vale per tutta la partita e si salva con il gruppo. Diventa vero quando un sacerdote o una sacerdotessa entra nel gruppo
	// accettando l'offerta di una missione (La Benedizione, Il Giro delle Sette Chiese): da allora nessuna missione lo ripropone,
	// così se ne ottiene al più uno. Un rifiuto, o un gruppo già al completo, non lo alzano: l'altra missione può ancora offrirlo.
	// Chi lo legge e lo scrive: OffertaDelSacerdote. Si azzera con una partita nuova (reimposta).
	private boolean sacerdoteAccolto;
	// Quel che il gruppo ha imparato strada facendo e non sapeva all'inizio (per ora i canti: l'Urlo della Banshee, vedi
	// IntermezzoUrloDellaBanshee). Si salva con il gruppo e si azzera con una partita nuova.
	private final Set<TipoCanto> cantiAppresi = EnumSet.noneOf(TipoCanto.class);

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
	 * Gli artefatti del gruppo che nessun personaggio sta usando (l'inventario generale). Di sola lettura: si cambia
	 * con {@link #aggiungiArtefatto} e {@link #togliArtefatto}.
	 */
	public Collection<ArtefattoMD> getArtefatti() {
		return Collections.unmodifiableList(artefatti);
	}

	/**
	 * Gli ospiti del gruppo: chi viaggia con i personaggi senza farne parte. Di sola lettura: si cambia con
	 * {@link #aggiungiOspite} e {@link #togliOspite}.
	 */
	public List<PersonaggioMD> getOspitiMD() {
		return Collections.unmodifiableList(ospiti);
	}

	/**
	 * Gli identificativi (uuid) degli ospiti che gli avversari possono attaccare. Di sola lettura: si cambia con
	 * {@link #aggiungiOspiteVulnerabile} e {@link #togliOspiteVulnerabile}.
	 */
	public Set<String> getOspitiVulnerabili() {
		return Collections.unmodifiableSet(ospitiVulnerabili);
	}

	/**
	 * I canti che il gruppo ha imparato strada facendo. Di sola lettura: si cambia con {@link #addCantoAppreso}.
	 */
	public Set<TipoCanto> getCantiAppresi() {
		return Collections.unmodifiableSet(cantiAppresi);
	}

	/**
	 * Il gruppo ha imparato quel canto.
	 */
	public void addCantoAppreso(TipoCanto canto) {
		cantiAppresi.add(canto);
	}

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
	 * Se un sacerdote offerto da una missione (la benedizione, il giro delle sette chiese) è già entrato nel gruppo.
	 * Finché è falso le missioni possono offrirne uno (se il gruppo ha posto); quando è vero non ne offrono più.
	 * Vedi il commento del campo.
	 */
	public boolean isSacerdoteAccolto() {
		return sacerdoteAccolto;
	}

	public void setSacerdoteAccolto(boolean sacerdoteAccolto) {
		this.sacerdoteAccolto = sacerdoteAccolto;
	}

	public void aggiungiOspite(PersonaggioMD ospite) {
		ospiti.add(ospite);
	}

	public void togliOspite(PersonaggioMD ospite) {
		ospiti.remove(ospite);
	}

	public void aggiungiOspiteVulnerabile(String uuid) {
		ospitiVulnerabili.add(uuid);
	}

	public void togliOspiteVulnerabile(String uuid) {
		ospitiVulnerabili.remove(uuid);
	}

	public void aggiungiArtefatto(ArtefattoMD artefatto) {
		artefatti.add(artefatto);
	}

	public void togliArtefatto(ArtefattoMD artefatto) {
		artefatti.remove(artefatto);
	}

	////////////////////////////////

	/**
	 * Azzera il gruppo per una partita nuova: oltre a quanto azzera {@link GruppoMD#reimposta()}, reimposta
	 * monete, pietre preziose, artefatti, ospiti e ospiti vulnerabili, e il conto delle sfide vinte.
	 */
	public void reimposta() {
		super.reimposta();
		monete = 0;
		preziosi = 0;
		artefatti.clear();
		ospiti.clear();
		ospitiVulnerabili.clear();
		sfideVinte = 0;
		sacerdoteAccolto = false;
		cantiAppresi.clear();
	}

	/**
	 * Scrive il gruppo (vedi {@link GruppoMD#salva(PrintWriter)}), poi una riga con monete, preziosi, pergamene, pozioni,
	 * posizione e sfide vinte, e infine gli artefatti, gli ospiti e gli identificativi degli ospiti vulnerabili, ciascun
	 * elenco preceduto dal suo numero di elementi.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		super.salva(stream);
		List<Object> campi = new ArrayList<>(Arrays.asList(
				monete,
				preziosi,
				incantesimi.size()));
		for (Map.Entry<TipoIncantesimo, Integer> incantesimo : incantesimi.entrySet()) {
			campi.add(incantesimo.getKey());
			campi.add(incantesimo.getValue());
		}
		campi.addAll(Arrays.asList(
				pozioniSalute,
				pozioniSaluteGrande,
				pozioniMagia,
				pozioniMagiaGrande,
				pozioniAumentoLivello,
				coordinate.getX(),
				coordinate.getY(),
				sfideVinte,
				sacerdoteAccolto,
				artefatti.size()));
		stampaRiga(stream, campi);

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
		stream.println(cantiAppresi.size());
		for (TipoCanto canto : cantiAppresi) {
			stream.println(canto.name());
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo il contenuto attuale del gruppo.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		super.leggi(stream);
		LettoreCampi st = new LettoreCampi(stream.readLine());
		monete = st.intero();
		preziosi = st.intero();
		incantesimi.clear();
		int numeroIncantesimi = st.intero();
		for (int i = 0; i < numeroIncantesimi; i++) {
			incantesimi.put(st.enumerato(TipoIncantesimo.class), st.intero());
		}
		pozioniSalute = st.intero();
		pozioniSaluteGrande = st.intero();
		pozioniMagia = st.intero();
		pozioniMagiaGrande = st.intero();
		pozioniAumentoLivello = st.intero();
		coordinate = new CoordinateMD(st.intero(), st.intero());
		sfideVinte = st.intero();
		sacerdoteAccolto = st.booleano();
		int numeroArtefatti = st.intero();

		artefatti.clear();
		for (int i = 0; i < numeroArtefatti; i++) {
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefatti.add(artefatto);
		}
		ospiti.clear();
		int numeroOspiti = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroOspiti; i++) {
			PersonaggioMD ospite = new PersonaggioMD();
			ospite.leggi(stream);
			ospiti.add(ospite);
		}
		ospitiVulnerabili.clear();
		int numeroVulnerabili = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroVulnerabili; i++) {
			ospitiVulnerabili.add(LettoreCampi.leggiTesto(stream));
		}
		cantiAppresi.clear();
		int numeroCanti = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < numeroCanti; i++) {
			cantiAppresi.add(TipoCanto.valueOf(LettoreCampi.leggiTesto(stream)));
		}
	}

}
