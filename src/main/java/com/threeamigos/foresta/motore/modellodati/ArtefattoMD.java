package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class ArtefattoMD implements Serializzabile {

	private String nome;
	private String descrizione;
	private String utilizzo;
	protected int costoAcquisto;
	protected int forza;
	private int magia;
	private int valore;
	private int coraggio;
	private int carisma;
	private int stanchezza;
	private int bersagli;
	private int protezione;

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

	public String getUtilizzo() {
		return utilizzo;
	}

	public void setUtilizzo(String utilizzo) {
		this.utilizzo = utilizzo;
	}

	public int getCostoAcquisto() {
		return costoAcquisto;
	}

	public void setCostoAcquisto(int costoAcquisto) {
		this.costoAcquisto = costoAcquisto;
	}

	public int getForza() {
		return forza;
	}

	public void setForza(int forza) {
		this.forza = forza;
	}

	public int getMagia() {
		return magia;
	}

	public void setMagia(int magia) {
		this.magia = magia;
	}

	public int getValore() {
		return valore;
	}

	public void setValore(int valore) {
		this.valore = valore;
	}

	public int getCoraggio() {
		return coraggio;
	}

	public void setCoraggio(int coraggio) {
		this.coraggio = coraggio;
	}

	public int getCarisma() {
		return carisma;
	}

	public void setCarisma(int carisma) {
		this.carisma = carisma;
	}

	public int getStanchezza() {
		return stanchezza;
	}

	public void setStanchezza(int stanchezza) {
		this.stanchezza = stanchezza;
	}

	public int getBersagli() {
		return bersagli;
	}

	public void setBersagli(int bersagli) {
		this.bersagli = bersagli;
	}

	public int getProtezione() {
		return protezione;
	}

	public void setProtezione(int protezione) {
		this.protezione = protezione;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(nome);
		stream.print(PIPE);
		stream.print(descrizione);
		stream.print(PIPE);
		stream.print(utilizzo);
		stream.print(PIPE);
		stream.print(costoAcquisto);
		stream.print(PIPE);
		stream.print(forza);
		stream.print(PIPE);
		stream.print(magia);
		stream.print(PIPE);
		stream.print(valore);
		stream.print(PIPE);
		stream.print(coraggio);
		stream.print(PIPE);
		stream.print(carisma);
		stream.print(PIPE);
		stream.print(stanchezza);
		stream.print(PIPE);
		stream.print(bersagli);
		stream.print(PIPE);
		stream.println(protezione);
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		nome = st.nextToken();
		descrizione = st.nextToken();
		utilizzo = st.nextToken();
		costoAcquisto = Integer.parseInt(st.nextToken());
		forza = Integer.parseInt(st.nextToken());
		magia = Integer.parseInt(st.nextToken());
		valore = Integer.parseInt(st.nextToken());
		coraggio = Integer.parseInt(st.nextToken());
		carisma= Integer.parseInt(st.nextToken());
		stanchezza = Integer.parseInt(st.nextToken());
		bersagli = Integer.parseInt(st.nextToken());
		protezione = Integer.parseInt(st.nextToken());
	}
}
