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
	private int peso;

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

	public int getPeso() {
		return peso;
	}

	public void setPeso(int peso) {
		this.peso = peso;
	}

	public void addModificatoreAttributo(TipoAttributo tipoModificatoreAttributo, int valore) {
		modificatori.add(new ModificatoreAttributo(tipoModificatoreAttributo, valore));
	}

	public int getModificatoreAttributo(TipoAttributo tipoAttributo) {
		int risultato = 0;
		for (ModificatoreAttributo modificatore : modificatori) {
			if (modificatore.getTipoModificatoreAttributo() == tipoAttributo) {
				risultato += modificatore.getValore();
			}
		}
		return risultato;
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
		for (ModificatoreAttributo modificatore : modificatori) {
			stream.print(modificatore.getTipoModificatoreAttributo().name());
			stream.print(PIPE);
			stream.print(modificatore.getValore());
			stream.print(PIPE);
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
		peso = Integer.parseInt(st.nextToken());
		while (st.hasMoreTokens()) {
			modificatori.add(new ModificatoreAttributo(TipoAttributo.valueOf(st.nextToken()), Integer.parseInt(st.nextToken())));
		}
	}
}
