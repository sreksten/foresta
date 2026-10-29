package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.Serializzabile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * La cassa del gruppo: le monete e le pietre preziose. Il gruppo non sa di avere una cassa: la governa Cassa.
 * <p>
 * Si salva su una riga: monete e preziosi.
 *
 * @author Stefano Reksten
 */
public class CassaMD implements Serializzabile {

	private int monete;
	private int preziosi;

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
	 * Svuota la cassa, come in una partita nuova (le monete con cui si parte le mette Cassa).
	 */
	public void reimposta() {
		monete = 0;
		preziosi = 0;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stampaRiga(stream, monete, preziosi);
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		LettoreCampi st = new LettoreCampi(stream.readLine());
		monete = st.intero();
		preziosi = st.intero();
	}
}
