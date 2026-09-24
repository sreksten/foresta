package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.oggetti.Incantamento;

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
	// Stato per la UI: se false, l'elenco modificatori/incantamenti resta chiuso.
	private boolean figliVisibili = true;
	private final Collection<ModificatoreAttributo> modificatori = new ArrayList<>();
	private final Collection<Incantamento> incantamenti = new ArrayList<>();

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

	public Collection<Incantamento> getIncantamenti() {
		return incantamenti;
	}

	public void addIncantamento(Incantamento incantamento) {
		incantamenti.add(incantamento);
	}

	public void addIncantamento(String nomeIncantamento, TipoDanno tipoDanno, int dannoBonusFisso, double coefficienteScala) {
		incantamenti.add(new Incantamento(nomeIncantamento, tipoDanno, dannoBonusFisso, coefficienteScala));
	}

	public boolean isFigliVisibili() {
		return figliVisibili;
	}

	public void setFigliVisibili(boolean figliVisibili) {
		this.figliVisibili = figliVisibili;
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
		stream.print(figliVisibili);
		stream.print(PIPE);
		stream.print(modificatori.size());
		stream.print(PIPE);
		stream.println(incantamenti.size());

		for (ModificatoreAttributo modificatore : modificatori) {
			stream.print(modificatore.getTipoAttributo().name());
			stream.print(PIPE);
			stream.print(modificatore.getTipoModificatoreAttributo().name());
			stream.print(PIPE);
			stream.print(modificatore.getQuantita());
			stream.print(PIPE);
			stream.println((modificatore.getNote() == null || modificatore.getNote().isEmpty()) ? "-" : modificatore.getNote());
		}

		for (Incantamento incantamento : incantamenti) {
			stream.print(incantamento.getNomeIncantamento());
			stream.print(PIPE);
			stream.print(incantamento.getTipoDannoElementale().name());
			stream.print(PIPE);
			stream.print(incantamento.getDannoBonusFisso());
			stream.print(PIPE);
			stream.println(incantamento.getCoefficienteScala());
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
		figliVisibili = Boolean.parseBoolean(st.nextToken());
		int numeroModificatori = Integer.parseInt(st.nextToken());
		int numeroIncantamenti = Integer.parseInt(st.nextToken());
		modificatori.clear();
		for (int i = 0; i < numeroModificatori; i++) {
			line = stream.readLine();
			st = new StringTokenizer(line, PIPE);
			while (st.hasMoreTokens()) {
				TipoAttributo tipoAttributo = TipoAttributo.valueOf(st.nextToken());
				TipoModificatore tipoModificatore = TipoModificatore.valueOf(st.nextToken());
				double quantita = Double.parseDouble(st.nextToken());
				String note = st.nextToken();
				// "-" sta per nota vuota (StringTokenizer salterebbe un campo vuoto). Si rilegge
				// come "", come la mette il costruttore a tre argomenti di ModificatoreAttributo:
				// con null il modificatore riletto non sarebbe più equals all'originale.
				if ("-".equals(note)) {
					note = "";
				}
				modificatori.add(new ModificatoreAttributo(tipoAttributo, tipoModificatore, quantita, note));
			}
		}
		incantamenti.clear();
		for (int i = 0; i < numeroIncantamenti; i++) {
			line = stream.readLine();
			st = new StringTokenizer(line, PIPE);
			String nomeIncantamento = st.nextToken();
			TipoDanno tipoDannoElementale = TipoDanno.valueOf(st.nextToken());
			int dannoBonusFisso = Integer.parseInt(st.nextToken());
			double coefficienteScala = Double.parseDouble(st.nextToken());
			incantamenti.add(new Incantamento(nomeIncantamento, tipoDannoElementale, dannoBonusFisso, coefficienteScala));
		}
	}
}
