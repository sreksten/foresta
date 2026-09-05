package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

public class ArtefattoMD implements Serializzabile {

	private TipoArtefatto tipo;
	private String nome;
	private String descrizione;
	private int livello;
	private int danni;
	protected int costoAcquisto;
	private double peso;
	private final Collection<ModificatoreAttributo> modificatori = new ArrayList<>();

	public TipoArtefatto getTipo() {
		return tipo;
	}

	public void setTipo(TipoArtefatto tipo) {
		this.tipo = tipo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public int getLivello() {
		return livello;
	}

	public void setLivello(int livello) {
		this.livello = livello;
	}

	public int getDanni() {
		return danni;
	}

	public void setDanni(int danniBase) {
		this.danni = danniBase;
	}

	public int getCostoAcquisto() {
		return costoAcquisto;
	}

	public void setCostoAcquisto(int costoAcquisto) {
		this.costoAcquisto = costoAcquisto;
	}

	public double getPeso() {
		return peso;
	}

	public void setPeso(double peso) {
		this.peso = peso;
	}

	public Collection<ModificatoreAttributo> getModificatori() {
		return modificatori;
	}

	public void addModificatore(ModificatoreAttributo modificatore) {
		modificatori.add(modificatore);
	}

	public void addModificatore(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
								double quantita, String nota) {
		modificatori.add(new ModificatoreAttributo(tipoAttributo, tipoModificatore, quantita, nota));
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(tipo.name());
		stream.print(PIPE);
		stream.print(nome);
		stream.print(PIPE);
		stream.print(descrizione);
		stream.print(PIPE);
		stream.print(livello);
		stream.print(PIPE);
		stream.print(danni);
		stream.print(PIPE);
		stream.print(costoAcquisto);
		stream.print(PIPE);
		stream.print(peso);
		stream.print(PIPE);
		stream.println(modificatori.size());
		for (ModificatoreAttributo modificatore : modificatori) {
			stream.print(modificatore.getTipoAttributo().name());
			stream.print(PIPE);
			stream.print(modificatore.getTipoModificatoreAttributo().name());
			stream.print(PIPE);
			stream.print(modificatore.getQuantita());
			stream.print(PIPE);
			stream.println((modificatore.getNote() == null || modificatore.getNote().isEmpty()) ? "-" : modificatore.getNote());
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		tipo = TipoArtefatto.valueOf(st.nextToken());
		nome = st.nextToken();
		descrizione = st.nextToken();
		livello = Integer.parseInt(st.nextToken());
		danni = Integer.parseInt(st.nextToken());
		costoAcquisto = Integer.parseInt(st.nextToken());
		peso = Double.parseDouble(st.nextToken());
		int numeroModificatori = Integer.parseInt(st.nextToken());
		modificatori.clear();
		for (int i = 0; i < numeroModificatori; i++) {
			line = stream.readLine();
			st = new StringTokenizer(line, PIPE);
			while (st.hasMoreTokens()) {
				TipoAttributo tipoAttributo = TipoAttributo.valueOf(st.nextToken());
				TipoModificatore tipoModificatore = TipoModificatore.valueOf(st.nextToken());
				Double quantita = Double.parseDouble(st.nextToken());
				String note = st.nextToken();
				if ("-".equals(note)) {
					note = null;
				}
				modificatori.add(new ModificatoreAttributo(tipoAttributo, tipoModificatore, quantita, note));
			}
		}
	}
}
