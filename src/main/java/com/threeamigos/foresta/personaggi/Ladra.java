package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Ladra extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Ladra"; }
	public String getNomePlurale() { return "Ladre"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Ladra() {
		super(ClassePersonaggio.LADRA);
	}

	public Ladra(String nome) {
		super(nome, ClassePersonaggio.LADRA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Ladra.gif");
		setIcona("icone/Ladra.gif");
		setSaluteMassima(Costanti.LADRA_MAX_SALUTE);
		setMagiaMassima(Costanti.LADRA_MAX_MAGIA);
		setValore(Costanti.LADRA_MAX_VALORE);
		setCoraggio(Costanti.LADRA_MAX_CORAGGIO);
		setCarisma(Costanti.LADRA_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
