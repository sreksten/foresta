package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Minotauro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public ClassiPersonaggio getClasse() { return ClassiPersonaggio.MINOTAURO; }
	public String getNomeSingolare() { return "Minotauro"; }
	public String getNomePlurale() { return "Minotauri"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Minotauro() {
		super(ClassiPersonaggio.MINOTAURO);
	}

	@Override
	public ClassiOfferta[] getOfferteCorruzione() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_MERCENARIO,
				ClassiOfferta.MAPPA_FORESTA,
		};
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Minotauro.gif");
		setIcona("icone/Minotauro.gif");
		setForzaMassima(70);
		setMagiaMassima(0);
		setValore(50);
		setCoraggio(50);
		setCarisma(0);
		setQuantitaMassima(3);
		setCorrompibile(true);
	}
}
