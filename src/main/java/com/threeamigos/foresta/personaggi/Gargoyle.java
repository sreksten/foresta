package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.tools.Misc;

public class Gargoyle extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Gargoyle"; }
	public String getNomePlurale() { return "Gargoyle"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoForza() { return 20; }
	public int getRecuperoMagia() { return 2; }

	public Gargoyle() {
		super(ClassiPersonaggio.GARGOYLE);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Gargoyle.gif");
		setForzaMassima(50);
		setMagiaMassima(20);
		setValore(70);
		setCoraggio(70);
		setCarisma(0);
		setQuantitaMassima(2);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return true;
	}}
