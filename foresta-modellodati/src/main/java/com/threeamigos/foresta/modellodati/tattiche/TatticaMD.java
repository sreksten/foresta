package com.threeamigos.foresta.modellodati.tattiche;

import java.util.Objects;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.threeamigos.foresta.serializzazione.Serializzabile.facoltativo;
import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * La tattica di un personaggio: una lista ordinata di voci (nessun tetto), di cui si esegue la prima eseguibile.
 * È immutabile.
 * Può essere <b>disabilitata</b>: succede a una tattica letta da un salvataggio che ha errori (voci incoerenti),
 * che non si esegue ma si conserva perché il giocatore la corregga nella schermata delle tattiche;
 * chi la esegue la tratta come se non ci fosse.
 * Vedi tattiche_di_combattimento_automatiche.md §11.
 *
 * @author Stefano Reksten
 */
public final class TatticaMD {

	/**
	 * Nessuna voce: si attacca sempre il primo avversario in campo (il ripiego). È la tattica dei miei personaggi finché il giocatore non ne scrive una.
	 */
	public static final TatticaMD VUOTA = new TatticaMD(Collections.emptyList());

	/**
	 * Una tattica con quelle voci, nell'ordine dato.
	 */
	public static TatticaMD di(VoceMD... voci) {
		return new TatticaMD(java.util.Arrays.asList(voci));
	}

	private final List<VoceMD> voci;
	private final boolean disabilitata;

	public TatticaMD(List<VoceMD> voci) {
		this(voci, false);
	}

	private TatticaMD(List<VoceMD> voci, boolean disabilitata) {
		this.voci = Collections.unmodifiableList(new ArrayList<>(voci));
		this.disabilitata = disabilitata;
	}

	/**
	 * L'elenco delle voci che compongono la tattica
	 */
	public List<VoceMD> getVoci() {
		return voci;
	}

	public boolean isDisabilitata() {
		return disabilitata;
	}

	/**
	 * La stessa tattica, disabilitata: non si esegue finché non è corretta e confermata di nuovo
	 */
	public TatticaMD disabilitata() {
		return disabilitata ? this : new TatticaMD(voci, true);
	}

	/**
	 * Gli errori di tutte le voci per quel lato, con il numero della voce (da 1); vuota se la tattica è a posto
	 */
	public List<String> problemi(boolean attoreDelGiocatore) {
		List<String> problemi = new ArrayList<>();
		for (int i = 0; i < voci.size(); i++) {
			for (String problema : voci.get(i).problemi(attoreDelGiocatore)) {
				problemi.add("voce " + (i + 1) + ": " + problema);
			}
		}
		return problemi;
	}

	/**
	 * Gli errori che la tattica ha per tutti e due i lati: vuota se per almeno uno (i miei personaggi o i mostri) è a posto. La rilettura non sa di chi è
	 * la tattica, e una rosa che vale solo per i mostri non è un errore per un mostro.
	 */
	public List<String> problemiPerQualsiasiLato() {
		List<String> perIMiei = problemi(true);
		return perIMiei.isEmpty() || problemi(false).isEmpty() ? Collections.emptyList() : perIMiei;
	}

	/**
	 * Scrive la tattica e le sue voci (vedi FormatoTattiche)
	 */
	public void salva(PrintWriter stream) {
		stampaRiga(stream,
				"TATTICA",
				voci.size(),
				disabilitata ? 1 : 0);
		for (VoceMD voce : voci) {
			voce.salva(stream);
		}
	}

	/**
	 * Rilegge una tattica scritta da {@link #salva}
	 */
	public static TatticaMD leggi(BufferedReader stream) throws IOException {
		return leggi(stream.readLine(), stream);
	}

	/**
	 * Come {@link #leggi(BufferedReader)}, con la prima riga (l'intestazione) già letta da chi doveva sapere se la tattica c'è
	 */
	public static TatticaMD leggi(String intestazione, BufferedReader stream) throws IOException {
		try {
			LettoreCampi campi = FormatoTattiche.controlla(new LettoreCampi(intestazione), "TATTICA");
			int numeroVoci = campi.intero();
			boolean disabilitata = campi.intero() != 0;
			List<VoceMD> voci = new ArrayList<>();
			for (int i = 0; i < numeroVoci; i++) {
				voci.add(VoceMD.leggi(stream));
			}
			TatticaMD letta = new TatticaMD(voci, disabilitata);
			// Una tattica incoerente non fa fallire il caricamento della partita: si disabilita, e il giocatore la corregge nella schermata delle tattiche
			return letta.problemiPerQualsiasiLato().isEmpty() ? letta : letta.disabilitata();
		} catch (IllegalArgumentException e) {
			// Un nome di enumerato sconosciuto o un numero scritto male
			throw new IOException("Salvataggio delle tattiche incoerente: " + e.getMessage(), e);
		}
	}

	/**
	 * Una descrizione leggibile, per i messaggi di log e di prova.
	 */
	@Override
	public String toString() {
		return (disabilitata ? "(disabilitata) " : "") + voci.toString();
	}

	/**
	 * Due tattiche sono uguali se hanno gli stessi dati.
	 */
	@Override
	public boolean equals(Object altro) {
		return altro instanceof TatticaMD && voci.equals(((TatticaMD) altro).voci) && disabilitata == ((TatticaMD) altro).disabilitata;
	}

	/**
	 * Coerente con {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return voci.hashCode() * 2 + (disabilitata ? 1 : 0);
	}

}
