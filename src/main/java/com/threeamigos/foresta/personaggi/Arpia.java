package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Arpia extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public String getNomeSingolare() { return "Arpia"; }
	public String getNomePlurale() { return "Arpie"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Arpia() {
		super(ClassePersonaggio.ARPIA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Arpia.gif");
		setSaluteMassima(Costanti.ARPIA_MAX_SALUTE);
		setMagiaMassima(Costanti.ARPIA_MAX_MAGIA);
		setValore(Costanti.ARPIA_MAX_VALORE);
		setCoraggio(Costanti.ARPIA_MAX_CORAGGIO);
		setCarisma(Costanti.ARPIA_MAX_CARISMA);
		setQuantitaMassima(Costanti.ARPIA_MAX_NUMERO);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.ARIA;
	}
}
