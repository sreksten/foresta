package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Ladro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Ladro"; }
	public String getNomePlurale() { return "Ladri"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Ladro() {
		super(ClassePersonaggio.LADRO);
	}

	public Ladro(String nome) {
		super(nome, ClassePersonaggio.LADRO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Ladro.gif");
		setIcona("icone/Ladro.gif");
		setSaluteMassima(Costanti.LADRO_MAX_SALUTE);
		setMagiaMassima(Costanti.LADRO_MAX_MAGIA);
		setValore(Costanti.LADRO_MAX_VALORE);
		setCoraggio(Costanti.LADRO_MAX_CORAGGIO);
		setCarisma(Costanti.LADRO_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
