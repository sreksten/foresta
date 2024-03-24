package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.threeamigos.foresta.personaggi.ClassiPersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

public class PersonaggioMD implements Serializzabile {

	private ClassiPersonaggio classe;
	private String nome;
	private boolean vivo;
	private int forza;
	private int forzaMassima;
	private int magia;
	private int magiaMassima;
	private int coraggio;
	private int valore;
	private int stanchezza;
	private int carisma;
	private String causaTrapasso;
	private int tempo = Personaggio.NO_TEMPO;
	private List<ArtefattoMD> artefatti = new ArrayList<>();

	public ClassiPersonaggio getClasse() {
		return classe;
	}

	public void setClasse(ClassiPersonaggio classe) {
		this.classe = classe;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public boolean isVivo() {
		return vivo;
	}

	public void setVivo(boolean vivo) {
		this.vivo = vivo;
	}

	public int getForza() {
		return forza;
	}

	public void setForza(int forza) {
		this.forza = forza;
	}

	public int getForzaMassima() {
		return forzaMassima;
	}

	public void setForzaMassima(int forzaMassima) {
		this.forzaMassima = forzaMassima;
	}

	public int getMagia() {
		return magia;
	}

	public void setMagia(int magia) {
		this.magia = magia;
	}

	public int getMagiaMassima() {
		return magiaMassima;
	}

	public void setMagiaMassima(int magiaMassima) {
		this.magiaMassima = magiaMassima;
	}

	public int getCoraggio() {
		return coraggio;
	}

	public void setCoraggio(int coraggio) {
		this.coraggio = coraggio;
	}

	public int getValore() {
		return valore;
	}

	public void setValore(int valore) {
		this.valore = valore;
	}

	public int getStanchezza() {
		return stanchezza;
	}

	public void setStanchezza(int stanchezza) {
		this.stanchezza = stanchezza;
	}

	public int getCarisma() {
		return carisma;
	}

	public void setCarisma(int carisma) {
		this.carisma = carisma;
	}

	public String getCausaTrapasso() {
		return causaTrapasso;
	}

	public void setCausaTrapasso(String causaTrapasso) {
		this.causaTrapasso = causaTrapasso;
	}

	public int getTempo() {
		return tempo;
	}

	public void setTempo(int tempo) {
		this.tempo = tempo;
	}

	public List<ArtefattoMD> getArtefatti() {
		return artefatti;
	}

	public void setArtefatti(List<ArtefattoMD> artefatti) {
		this.artefatti = artefatti;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(classe.ordinal());
		stream.print(PIPE);
		stream.print(nome);
		stream.print(PIPE);
		stream.print(vivo ? "vivo" : causaTrapasso);
		stream.print(PIPE);
		stream.print(forza);
		stream.print(PIPE);
		stream.print(forzaMassima);
		stream.print(PIPE);
		stream.print(magia);
		stream.print(PIPE);
		stream.print(magiaMassima);
		stream.print(PIPE);
		stream.print(coraggio);
		stream.print(PIPE);
		stream.print(valore);
		stream.print(PIPE);
		stream.print(stanchezza);
		stream.print(PIPE);
		stream.print(carisma);
		stream.print(PIPE);
		stream.print(tempo);
		stream.print(PIPE);
		stream.println(artefatti.size());
		for (ArtefattoMD artefatto : artefatti) {
			artefatto.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException{
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		classe = ClassiPersonaggio.values()[Integer.parseInt(st.nextToken())];
		nome = st.nextToken();
		if ("null".equals(nome)) {
			nome = null;
		}
		String vivoOMorto = st.nextToken();
		if ("vivo".equals(vivoOMorto)) {
			vivo = true;
			causaTrapasso = null;
		} else {
			vivo = false;
			causaTrapasso = vivoOMorto;
		}
		forza = Integer.parseInt(st.nextToken());
		forzaMassima = Integer.parseInt(st.nextToken());
		magia = Integer.parseInt(st.nextToken());
		magiaMassima = Integer.parseInt(st.nextToken());
		coraggio = Integer.parseInt(st.nextToken());
		valore = Integer.parseInt(st.nextToken());
		stanchezza = Integer.parseInt(st.nextToken());
		carisma = Integer.parseInt(st.nextToken());
		tempo = Integer.parseInt(st.nextToken());
		int numeroArtefatti = Integer.parseInt(st.nextToken());
		artefatti.clear();
		for (int i = 0; i < numeroArtefatti; i++) {
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefatti.add(artefatto);
		}
	}
}
