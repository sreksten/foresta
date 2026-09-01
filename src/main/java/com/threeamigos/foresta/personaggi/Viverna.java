package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Viverna extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Viverna"; }
	public String getNomePlurale() { return "Viverne"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return Costanti.VIVERNA_RECUPERO_MAGIA; }

	public Viverna() {
		super(ClassePersonaggio.VIVERNA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Viverna.gif");
		setSaluteMassima(Costanti.VIVERNA_MAX_SALUTE);
		setMagiaMassima(Costanti.VIVERNA_MAX_MAGIA);
		setValore(Costanti.VIVERNA_MAX_VALORE);
		setCoraggio(Costanti.VIVERNA_MAX_CORAGGIO);
		setCarisma(Costanti.VIVERNA_MAX_CARISMA);
		setQuantitaMassima(Costanti.VIVERNA_MAX_NUMERO);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.FUOCO;
	}
}
