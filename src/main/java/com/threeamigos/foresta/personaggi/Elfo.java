package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
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

		setForza(Dado.tiraAncheSenzaRange(Costanti.ELFO_FORZA_MIN, Costanti.ELFO_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.ELFO_DESTREZZA_MIN, Costanti.ELFO_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.ELFO_COSTITUZIONE_MIN, Costanti.ELFO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.ELFO_INTELLIGENZA_MIN, Costanti.ELFO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.ELFO_SAGGEZZA_MIN, Costanti.ELFO_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.ELFO_FORTUNA_MIN, Costanti.ELFO_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.ELFO_CRITICO_MIN, Costanti.ELFO_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.ELFO_PRECISIONE_MIN, Costanti.ELFO_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.ELFO_VELOCITA_MIN, Costanti.ELFO_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.ELFO_PARATA_MIN, Costanti.ELFO_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.ELFO_RESISTENZA_MAGICA_MIN, Costanti.ELFO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.ELFO_MAGIA_MIN, Costanti.ELFO_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.ELFO_FURIA_MIN, Costanti.ELFO_FURIA_MAX));

	}
}
