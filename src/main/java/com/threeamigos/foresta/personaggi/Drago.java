package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Drago extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Drago"; }
	public String getNomePlurale() { return "Draghi"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoMagia() { return Costanti.DRAGO_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.DRAGO_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.DRAGO_BERSAGLI_PER_INCANTESIMO; }

	public Drago() {
		super(ClassePersonaggio.DRAGO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Drago.gif");
		setSaluteMassima(Costanti.DRAGO_MAX_SALUTE);
		setMagiaMassima(Costanti.DRAGO_MAX_MAGIA);
		setValore(Costanti.DRAGO_MAX_VALORE);
		setCoraggio(Costanti.DRAGO_MAX_CORAGGIO);
		setCarisma(Costanti.DRAGO_MAX_CARISMA);
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.FUOCO;
	}
}
