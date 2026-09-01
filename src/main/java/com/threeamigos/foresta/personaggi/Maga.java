package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

public class Maga extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Maga"; }
	public String getNomePlurale() { return "Maghe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoForza() { return Costanti.MAGA_RECUPERO_FORZA; }
	public int getRecuperoMagia() { return Costanti.MAGA_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.MAGA_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.MAGA_BERSAGLI_PER_INCANTESIMO_BONUS + super.getBersagliPerIncantesimo(); }

	public Maga() {
		super(ClassePersonaggio.MAGA);
	}

	public Maga(String nome) {
		super(nome, ClassePersonaggio.MAGA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Maga.gif");
		setIcona("icone/Maga.gif");
		setSaluteMassima(Costanti.MAGA_MAX_SALUTE);
		setMagiaMassima(Costanti.MAGA_MAX_MAGIA);
		setValore(Costanti.MAGA_MAX_VALORE);
		setCoraggio(Costanti.MAGA_MAX_CORAGGIO);
		setCarisma(Costanti.MAGA_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
