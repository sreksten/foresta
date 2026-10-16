package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.ClasseMissione;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * I dati di una missione: identificativo, classe, nome, descrizione e se la descrizione è già visibile al giocatore.
 * <p>
 * Lo stato in corso d'opera sta nelle <b>proprietà</b>, coppie nome-valore che la missione scrive e rilegge (con la
 * convenzione che una proprietà presente senza valore importante vale per vera, come in {@link LocazioneMD}).
 * Una missione può avere delle <b>missioni secondarie</b>, a loro volta con le proprie: formano un albero, che il
 * salvataggio scrive e rilegge ricorsivamente.
 * <p>
 * Le regole che fanno avanzare le missioni stanno nel motore (vedi gestione_missioni.md); qui ci sono solo i dati.
 *
 * @author Stefano Reksten
 */
public class MissioneMD implements Serializzabile {

	private String id = UUID.randomUUID().toString();
	private ClasseMissione classe = ClasseMissione.MISSIONE_SECONDARIA;
	private String nome;
	private String descrizione;
	private boolean descrizioneVisibile = true;
	private final Map<String, String> proprieta = new HashMap<>();
	private final List<MissioneMD> missioniSecondarie = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ClasseMissione getClasse() {
		return classe;
	}

	public void setClasse(ClasseMissione classe) {
		this.classe = classe;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = Serializzabile.pulisciCampo(nome);
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = Serializzabile.pulisciCampo(descrizione);
	}

	public boolean isDescrizioneVisibile() {
		return descrizioneVisibile;
	}

	public void setDescrizioneVisibile(boolean descrizioneVisibile) {
		this.descrizioneVisibile = descrizioneVisibile;
	}

	/**
	 * Toglie le proprietà e le missioni secondarie. Identificativo, classe, nome e descrizione restano.
	 */
	public void reimposta() {
		proprieta.clear();
		missioniSecondarie.clear();
	}

	/**
	 * Imposta una proprietà (o ne sostituisce il valore). Il carattere "|" si toglie dal valore.
	 */
	public void aggiungiProprieta(String nome, String valore) {
		proprieta.put(nome, Serializzabile.pulisciCampo(valore));
	}

	/**
	 * @return il valore della proprietà, o null se la missione non ce l'ha
	 */
	public String ottieniProprieta(String nome) {
		return proprieta.get(nome);
	}

	/**
	 * Toglie la proprietà, se c'è.
	 */
	public void rimuoviProprieta(String nome) {
		proprieta.remove(nome);
	}

	/**
	 * Aggiunge una missione secondaria.
	 *
	 * @return false, senza aggiungere niente, se la missione è questa stessa (creerebbe un ciclo) o se ne esiste già una
	 * con lo stesso identificativo
	 */
	public boolean aggiungiMissioneMD(MissioneMD missioneMD) {
		if (missioneMD == this) {
			// Un ciclo renderebbe infinite le ricorsioni di salva() e di ricostruzione
			return false;
		}
		if (missioniSecondarie.stream().noneMatch(m -> m.getId().equals(missioneMD.getId()))) {
			missioniSecondarie.add(missioneMD);
			return true;
		}
		return false;
	}

	/**
	 * Toglie la missione secondaria con lo stesso identificativo, se c'è.
	 */
	public void rimuoviMissioneMD(MissioneMD missioneMD) {
		missioniSecondarie.removeIf(m -> m.getId().equals(missioneMD.getId()));
	}

	/**
	 * Le missioni secondarie, nell'ordine in cui sono state aggiunte.
	 */
	public Collection<MissioneMD> getMissioniMD() {
		return missioniSecondarie;
	}

	/**
	 * Scrive una riga con identificativo, classe, nome, descrizione, visibilità e numero di missioni secondarie, poi una
	 * riga con le proprietà e infine, una dopo l'altra, le missioni secondarie.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(id);
		stream.print(PIPE);
		stream.print(classe.name());
		stream.print(PIPE);
		stream.print(nome == null ? "" : nome);
		stream.print(PIPE);
		stream.print(descrizione == null ? "" : descrizione);
		stream.print(PIPE);
		stream.print(descrizioneVisibile);
		stream.print(PIPE);
		stream.println(missioniSecondarie.size());
		stream.println(MappaProprieta.salva(proprieta));
		for (MissioneMD missioneMD : missioniSecondarie) {
			missioneMD.salva(stream);
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo proprietà e missioni secondarie.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = new LettoreCampi(stream.readLine());
		id = campi.testo();
		classe = campi.enumerato(ClasseMissione.class);
		nome = campi.testo();
		descrizione = campi.testo();
		descrizioneVisibile = campi.booleano();
		int dimensioneElencoMissioniSecondarie = campi.intero();
		MappaProprieta.leggi(new LettoreCampi(stream.readLine()), proprieta);
		missioniSecondarie.clear();
		for (int i = 0; i < dimensioneElencoMissioniSecondarie; i++) {
			MissioneMD missioneSecondaria = new MissioneMD();
			missioneSecondaria.leggi(stream);
			missioniSecondarie.add(missioneSecondaria);
		}
	}
}
