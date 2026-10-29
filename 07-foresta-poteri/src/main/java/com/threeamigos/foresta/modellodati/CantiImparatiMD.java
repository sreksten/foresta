package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;
import com.threeamigos.foresta.tipi.TipoCanto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * I canti che il gruppo ha imparato strada facendo e non sapeva all'inizio (per ora l'Urlo della Banshee, vedi IntermezzoUrloDellaBanshee).
 * Il gruppo non sa di averli imparati: lo sanno i canti (vedi CantiImparati).
 * <p>
 * Si salva così: una riga con il numero di canti e, per ognuno, una riga con il suo nome.
 *
 * @author Stefano Reksten
 */
public final class CantiImparatiMD implements Serializzabile {

	private final Set<TipoCanto> canti = EnumSet.noneOf(TipoCanto.class);

	/**
	 * I canti imparati, in sola lettura: si cambia con {@link #aggiungi}.
	 */
	public Set<TipoCanto> getCanti() {
		return Collections.unmodifiableSet(canti);
	}

	/**
	 * Il gruppo ha imparato quel canto.
	 */
	public void aggiungi(TipoCanto canto) {
		canti.add(canto);
	}

	/**
	 * Dimentica tutti i canti, come in una partita nuova.
	 */
	public void reimposta() {
		canti.clear();
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.println(canti.size());
		for (TipoCanto canto : canti) {
			stream.println(canto.name());
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		canti.clear();
		int quanti = LettoreCampi.leggiIntero(stream);
		for (int i = 0; i < quanti; i++) {
			canti.add(TipoCanto.valueOf(LettoreCampi.leggiTesto(stream)));
		}
	}
}
