package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Spettro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNO; }
	public String getADS() { return Misc.LO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELLO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UNO; }

	public String getNomeSingolare() { return "Spettro"; }
	public String getNomePlurale() { return "Spettri"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Spettro() {
		super(ClassePersonaggio.SPETTRO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Spettro.gif");
		setSaluteMassima(Costanti.SPETTRO_MAX_SALUTE);
		setMagiaMassima(Costanti.SPETTRO_MAX_MAGIA);
		setValore(Costanti.SPETTRO_MAX_VALORE);
		setCoraggio(Costanti.SPETTRO_MAX_CORAGGIO);
		setCarisma(Costanti.SPETTRO_MAX_CARISMA);
		setQuantitaMassima(Costanti.SPETTRO_MAX_NUMERO);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
