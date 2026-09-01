package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Chimera extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Chimera"; }
	public String getNomePlurale() { return "Chimere"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Chimera() {
		super(ClassePersonaggio.CHIMERA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Chimera.gif");
		setSaluteMassima(Costanti.CHIMERA_MAX_SALUTE);
		setMagiaMassima(Costanti.CHIMERA_MAX_MAGIA);
		setValore(Costanti.CHIMERA_MAX_VALORE);
		setCoraggio(Costanti.CHIMERA_MAX_CORAGGIO);
		setCarisma(Costanti.CHIMERA_MAX_CARISMA);
		setQuantitaMassima(Costanti.CHIMERA_MAX_NUMERO);
	}
}
