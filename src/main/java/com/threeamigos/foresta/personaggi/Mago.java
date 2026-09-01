package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Mago extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Mago"; }
	public String getNomePlurale() { return "Maghi"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoForza() { return Costanti.MAGO_RECUPERO_FORZA; }
	public int getRecuperoMagia() { return Costanti.MAGO_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.MAGO_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.MAGO_BERSAGLI_PER_INCANTESIMO_BONUS + super.getBersagliPerIncantesimo(); }

	public Mago() {
		super(ClassePersonaggio.MAGO);
	}

	public Mago(String nome) {
		super(nome, ClassePersonaggio.MAGO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Mago.gif");
		setIcona("icone/Mago.gif");
		setSaluteMassima(Costanti.MAGO_MAX_SALUTE);
		setMagiaMassima(Costanti.MAGO_MAX_MAGIA);
		setValore(Costanti.MAGO_MAX_VALORE);
		setCoraggio(Costanti.MAGO_MAX_CORAGGIO);
		setCarisma(Costanti.MAGO_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
