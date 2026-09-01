package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Elfa extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public String getNomeSingolare() { return "Elfa"; }
	public String getNomePlurale() { return "Elfe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return Costanti.ELFA_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.ELFA_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.ELFA_BERSAGLI_PER_INCANTESIMO_BONUS + super.getBersagliPerIncantesimo(); }

	public Elfa() {
		super(ClassePersonaggio.ELFA);
	}

	public Elfa(String nome) {
		super(nome, ClassePersonaggio.ELFA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Elfa.gif");
		setIcona("icone/Elfa.gif");
		setSaluteMassima(Costanti.ELFA_MAX_SALUTE);
		setMagiaMassima(Costanti.ELFA_MAX_MAGIA);
		setValore(Costanti.ELFA_MAX_VALORE);
		setCoraggio(Costanti.ELFA_MAX_CORAGGIO);
		setCarisma(Costanti.ELFA_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
