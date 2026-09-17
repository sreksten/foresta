package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.GruppoGiocatore;

public class SalvataggioImpl implements InterfacciaGestoreSalvataggi.Salvataggio {

	private String id;
	private String nome;
	private String contenuto;
	
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String getId() {
		return id;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	@Override
	public String getDescrizione() {
		return nome;
	}

	@Override
	public GruppoGiocatore getGruppoGiocatore() {
		return null;
	}

	@Override
	public String getContenuto() {
		return contenuto;
	}

	@Override
	public void setContenuto(String contenuto) {
		this.contenuto = contenuto;
	}

}
