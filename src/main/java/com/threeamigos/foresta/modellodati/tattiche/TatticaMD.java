package com.threeamigos.foresta.modellodati.tattiche;

import java.util.Objects;
import com.threeamigos.foresta.modellodati.Serializzabile;
import com.threeamigos.foresta.modellodati.LettoreCampi;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * La tattica di un personaggio: una lista ordinata di voci (nessun tetto), di cui si esegue la prima eseguibile. Immutabile.
 * Vedi tattiche_di_combattimento_automatiche.md §11.
 */
public final class TatticaMD {

	/**
	 * Nessuna voce: si attacca sempre il primo avversario in campo (il ripiego). È la tattica dei miei personaggi finché il giocatore non ne scrive una.
	 */
	public static final TatticaMD VUOTA = new TatticaMD(Collections.emptyList());

	private final List<VoceMD> voci;

	public TatticaMD(List<VoceMD> voci) {
		this.voci = Collections.unmodifiableList(new ArrayList<>(voci));
	}

	public static TatticaMD di(VoceMD... voci) {
		return new TatticaMD(java.util.Arrays.asList(voci));
	}

	public List<VoceMD> getVoci() {
		return voci;
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

	@Override
	public String toString() {
		return voci.toString();
	}

	/**
	 * Scrive la tattica e le sue voci (vedi FormatoTattiche)
	 */
	public void salva(PrintWriter stream) {
		stream.println("TATTICA" + Serializzabile.PIPE + voci.size());
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
			List<VoceMD> voci = new ArrayList<>();
			for (int i = 0; i < numeroVoci; i++) {
				voci.add(VoceMD.leggi(stream));
			}
			return new TatticaMD(voci);
		} catch (IllegalArgumentException e) {
			// Un nome di enumerato sconosciuto o un numero scritto male
			throw new IOException("Salvataggio delle tattiche incoerente: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean equals(Object altro) {
		return altro instanceof TatticaMD && voci.equals(((TatticaMD) altro).voci);
	}

	@Override
	public int hashCode() {
		return voci.hashCode();
	}
}
