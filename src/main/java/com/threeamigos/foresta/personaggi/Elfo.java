package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Elfo extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DALL_APOSTROFO; }

	public String getNomeSingolare() { return "Elfo"; }
	public String getNomePlurale() { return "Elfi"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoMagia() { return Costanti.ELFO_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.ELFO_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.ELFO_BERSAGLI_PER_INCANTESIMO_BONUS + super.getBersagliPerIncantesimo(); }

	public Elfo() {
		super(ClassePersonaggio.ELFO);
	}

	public Elfo(String nome) {
		super(nome, ClassePersonaggio.ELFO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Elfo.gif");
		setIcona("icone/Elfo.gif");
		setSaluteMassima(Costanti.ELFO_MAX_SALUTE);
		setMagiaMassima(Costanti.ELFO_MAX_MAGIA);
		setValore(Costanti.ELFO_MAX_VALORE);
		setCoraggio(Costanti.ELFO_MAX_CORAGGIO);
		setCarisma(Costanti.ELFO_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
