package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.threeamigos.foresta.locazioni.ClassiLocazione;

public class LineaTemporaleMD implements Serializzabile {

	public static final int PRIMA_ORA_DEL_MATTINO = 8;

	private int ora;
	private int giorno;
	private List<ClassiLocazione> cittaDistrutte = new ArrayList<>();
	private boolean giocoFinito;
	
	public int getOra() {
		return ora;
	}

	public void setOra(int ora) {
		this.ora = ora;
	}

	public int getGiorno() {
		return giorno;
	}

	public void setGiorno(int giorno) {
		this.giorno = giorno;
	}

	public List<ClassiLocazione> getCittaDistrutte() {
		return cittaDistrutte;
	}

	public void setCittaDistrutte(List<ClassiLocazione> cittaDistrutte) {
		this.cittaDistrutte = cittaDistrutte;
	}

	public boolean isGiocoFinito() {
		return giocoFinito;
	}

	public void setGiocoFinito(boolean giocoFinito) {
		this.giocoFinito = giocoFinito;
	}

	/////////////////////////

	public void reimposta() {
		ora = 8;
		giorno = 1;
		giocoFinito = false;
		cittaDistrutte.clear();
	}

	public void addCittaDistrutta(ClassiLocazione citta) {
		cittaDistrutte.add(citta);
	}

	public boolean isCittaDistrutta(ClassiLocazione citta) {
		return cittaDistrutte.contains(citta);
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(ora);
		stream.print(PIPE);
		stream.print(giorno);
		for (ClassiLocazione cittaDistrutta : cittaDistrutte) {
			stream.print(PIPE);
			stream.print(cittaDistrutta.ordinal());
		}
		stream.println("");
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		ora = Integer.parseInt(st.nextToken());
		giorno = Integer.parseInt(st.nextToken());
		cittaDistrutte.clear();
		while (st.hasMoreTokens()) {
			cittaDistrutte.add(ClassiLocazione.values()[Integer.parseInt(st.nextToken())]);
		}
	}
}
