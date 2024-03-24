package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Goblin extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Goblin"; }
	public String getNomePlurale() { return "Goblin"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Goblin() {
		super(ClassiPersonaggio.GOBLIN);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Goblin.gif");
		setIcona("icone/Goblin.gif");
		setForzaMassima(40);
		setMagiaMassima(0);
		setValore(30);
		setCoraggio(50);
		setCarisma(0);
		setQuantitaMassima(5);
		setCorrompibile(true);
	}

	@Override
	public ClassiOfferta[] getOfferteCorruzione() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_MERCENARIO,
				ClassiOfferta.MAPPA_FORESTA,
		};
	}
}
