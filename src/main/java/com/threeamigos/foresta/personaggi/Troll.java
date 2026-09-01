package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Troll extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Troll"; }
	public String getNomePlurale() { return "Troll"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Troll() {
		super(ClassePersonaggio.TROLL);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Troll.gif");
		setSaluteMassima(Costanti.TROLL_MAX_SALUTE);
		setMagiaMassima(Costanti.TROLL_MAX_MAGIA);
		setValore(Costanti.TROLL_MAX_VALORE);
		setCoraggio(Costanti.TROLL_MAX_CORAGGIO);
		setCarisma(Costanti.TROLL_MAX_CARISMA);
		setQuantitaMassima(Costanti.TROLL_MAX_NUMERO);
		setCorrompibile(true);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.TERRA;
	}
}
