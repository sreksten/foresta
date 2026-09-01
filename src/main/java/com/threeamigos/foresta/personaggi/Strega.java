package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Strega extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Strega"; }
	public String getNomePlurale() { return "Streghe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return Costanti.STREGA_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.STREGA_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.STREGA_BERSAGLI_PER_INCANTESIMO; }

	public Strega() {
		super(ClassePersonaggio.STREGA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Strega.gif");
		setSaluteMassima(Costanti.STREGA_MAX_SALUTE);
		setMagiaMassima(Costanti.STREGA_MAX_MAGIA);
		setValore(Costanti.STREGA_MAX_VALORE);
		setCoraggio(Costanti.STREGA_MAX_CORAGGIO);
		setCarisma(Costanti.STREGA_MAX_CARISMA);
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.ACQUA ||
				classeIncantesimo == ClassiIncantesimo.ARIA ||
				classeIncantesimo == ClassiIncantesimo.FUOCO ||
				classeIncantesimo == ClassiIncantesimo.TERRA;
	}
}
