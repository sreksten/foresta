package com.threeamigos.foresta.tools;

public class Salvataggio implements InterfacciaGestoreSalvataggi.InterfacciaSalvataggio {

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
	public String getContenuto() {
		return contenuto;
	}

	@Override
	public void setContenuto(String contenuto) {
		this.contenuto = contenuto;
	}

}
