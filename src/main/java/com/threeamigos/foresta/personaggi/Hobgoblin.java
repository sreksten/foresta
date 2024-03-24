package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Hobgoblin extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Hobgoblin"; }
	public String getNomePlurale() { return "Hobgoblin"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoForza() { return 8; }

	public Hobgoblin() {
		super(ClassiPersonaggio.HOBGOBLIN);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Hobgoblin.gif");
		setIcona("icone/Hobgoblin.gif");
		setForzaMassima(65);
		setMagiaMassima(10);
		setValore(40);
		setCoraggio(60);
		setCarisma(0);
		setQuantitaMassima(3);
		setCorrompibile(true);
	}

	@Override
	public ClassiOfferta[] getOfferteCorruzione() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_MERCENARIO,
				ClassiOfferta.INCANTESIMI,
				ClassiOfferta.MAPPA_FORESTA,
		};
	}
}
