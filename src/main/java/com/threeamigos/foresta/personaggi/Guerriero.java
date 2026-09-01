package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Guerriero extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Guerriero"; }
	public String getNomePlurale() { return "Guerrieri"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getModificaDanniForza() { return Costanti.GUERRIERO_MODIFICATORE_DANNI_FORZA; }

	public Guerriero() {
		super(ClassePersonaggio.GUERRIERO);
	}

	public Guerriero(String nome) {
		super(nome, ClassePersonaggio.GUERRIERO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Guerriero.gif");
		setIcona("icone/Guerriero.gif");
		setSaluteMassima(Costanti.GUERRIERO_MAX_SALUTE);
		setMagiaMassima(Costanti.GUERRIERO_MAX_MAGIA);
		setValore(Costanti.GUERRIERO_MAX_VALORE);
		setCoraggio(Costanti.GUERRIERO_MAX_CORAGGIO);
		setCarisma(Costanti.GUERRIERO_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
