package com.threeamigos.foresta.missioni;

public class TrovaPersonaggio extends MissionePersonaggio {

	public TrovaPersonaggio() {
		super();
		StringBuilder sb = new StringBuilder();
		sb.append("Trova ").append(ottieniProprieta(PERSONAGGIO_BERSAGLIO));
		aggiungiProprieta(NOME, sb.toString());
		sb = new StringBuilder();
		sb.append("Trova ").append(ottieniProprieta(PERSONAGGIO_BERSAGLIO)).append(" per conto di ").append(ottieniProprieta(PERSONAGGIO_RICHIEDENTE));
		aggiungiProprieta(DESCRIZIONE, sb.toString());
	}

	@Override
	public String getNome() {
		return ottieniProprieta(NOME);
	}

	@Override
	public String getDescrizione() {
		return ottieniProprieta(DESCRIZIONE);
	}

	@Override
	public void controllaPreLocazione() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllaInLocazione() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllaPostLocazione() {
		// TODO Auto-generated method stub		
	}

}
