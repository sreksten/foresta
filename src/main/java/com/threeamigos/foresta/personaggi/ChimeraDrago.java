package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class ChimeraDrago extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Chimera-Drago"; }
	public String getNomePlurale() { return "Chimere-Drago"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public ChimeraDrago() {
		super(ClassePersonaggio.CHIMERA_DRAGO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/ChimeraDrago.gif");
		setSaluteMassima(Costanti.CHIMERADRAGO_MAX_SALUTE);
		setMagiaMassima(Costanti.CHIMERADRAGO_MAX_MAGIA);
		setValore(Costanti.CHIMERADRAGO_MAX_VALORE);
		setCoraggio(Costanti.CHIMERADRAGO_MAX_CORAGGIO);
		setCarisma(Costanti.CHIMERADRAGO_MAX_CARISMA);
		setQuantitaMassima(Costanti.CHIMERADRAGO_MAX_NUMERO);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.FUOCO;
	}
}
