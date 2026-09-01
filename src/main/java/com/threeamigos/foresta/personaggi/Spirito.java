package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Spirito extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNO; }
	public String getADS() { return Misc.LO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELLO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UNO; }

	public String getNomeSingolare() { return "Spirito"; }
	public String getNomePlurale() { return "Spiriti"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Spirito() {
		super(ClassePersonaggio.SPIRITO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Spirito.gif");
		setSaluteMassima(Costanti.SPIRITO_MAX_SALUTE);
		setMagiaMassima(Costanti.SPIRITO_MAX_MAGIA);
		setValore(Costanti.SPIRITO_MAX_VALORE);
		setCoraggio(Costanti.SPIRITO_MAX_CORAGGIO);
		setCarisma(Costanti.SPIRITO_MAX_CARISMA);
		setQuantitaMassima(Costanti.SPIRITO_MAX_NUMERO);
		setAmichevole(true);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
