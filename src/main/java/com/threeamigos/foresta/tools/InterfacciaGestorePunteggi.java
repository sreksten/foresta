package com.threeamigos.foresta.tools;

public interface InterfacciaGestorePunteggi {

	public interface Record {

		public String getNome();

		public int getPunteggio();

	}

	public boolean carica();

	public int getCardinalita();

	public Record getRecord(int posizione);

	public boolean isPunteggioInClassifica(int punteggio);

	public void addRecord(String nome, int punteggio);

	public boolean salva();

}
