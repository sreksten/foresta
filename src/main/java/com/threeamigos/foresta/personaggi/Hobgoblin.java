package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
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
	public int getRecuperoForza() { return Costanti.HOBGOBLIN_RECUPERO_FORZA; }

	public Hobgoblin() {
		super(ClassePersonaggio.HOBGOBLIN);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Hobgoblin.gif");
		setIcona("icone/Hobgoblin.gif");
		setSaluteMassima(Costanti.HOBGOBLIN_MAX_SALUTE);
		setMagiaMassima(Costanti.HOBGOBLIN_MAX_MAGIA);
		setValore(Costanti.HOBGOBLIN_MAX_VALORE);
		setCoraggio(Costanti.HOBGOBLIN_MAX_CORAGGIO);
		setCarisma(Costanti.HOBGOBLIN_MAX_CARISMA);
		setQuantitaMassima(Costanti.HOBGOBLIN_MAX_NUMERO);
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
