package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
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

	public ClassePersonaggio getClasse() { return ClassePersonaggio.MINOTAURO; }
	public String getNomeSingolare() { return "Minotauro"; }
	public String getNomePlurale() { return "Minotauri"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Minotauro() {
		super(ClassePersonaggio.MINOTAURO);
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
		setSaluteMassima(Costanti.MINOTAURO_MAX_SALUTE);
		setMagiaMassima(Costanti.MINOTAURO_MAX_MAGIA);
		setValore(Costanti.MINOTAURO_MAX_VALORE);
		setCoraggio(Costanti.MINOTAURO_MAX_CORAGGIO);
		setCarisma(Costanti.MINOTAURO_MAX_CARISMA);
		setQuantitaMassima(Costanti.MINOTAURO_MAX_NUMERO);
		setCorrompibile(true);
	}
}
