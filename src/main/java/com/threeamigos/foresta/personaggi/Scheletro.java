package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Scheletro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNO; }
	public String getADS() { return Misc.LO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELLO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UNO; }

	public String getNomeSingolare() { return "Scheletro"; }
	public String getNomePlurale() { return "Scheletri"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Scheletro() {
		super(ClassePersonaggio.SCHELETRO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Scheletro.gif");
		setSaluteMassima(Costanti.SCHELETRO_MAX_SALUTE);
		setMagiaMassima(Costanti.SCHELETRO_MAX_MAGIA);
		setValore(Costanti.SCHELETRO_MAX_VALORE);
		setCoraggio(Costanti.SCHELETRO_MAX_CORAGGIO);
		setCarisma(Costanti.SCHELETRO_MAX_CARISMA);
		setQuantitaMassima(Costanti.SCHELETRO_MAX_NUMERO);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
