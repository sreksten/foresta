package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Guerriera extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Guerriera"; }
	public String getNomePlurale() { return "Guerriere"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getModificaDanniForza() { return Costanti.GUERRIERA_MODIFICATORE_DANNI_FORZA; }

	public Guerriera() {
		super(ClassePersonaggio.GUERRIERA);
	}

	public Guerriera(String nome) {
		super(nome, ClassePersonaggio.GUERRIERA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Guerriera.gif");
		setIcona("icone/Guerriera.gif");
		setSaluteMassima(Costanti.GUERRIERA_MAX_SALUTE);
		setMagiaMassima(Costanti.GUERRIERA_MAX_MAGIA);
		setValore(Costanti.GUERRIERA_MAX_VALORE);
		setCoraggio(Costanti.GUERRIERA_MAX_CORAGGIO);
		setCarisma(Costanti.GUERRIERA_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
