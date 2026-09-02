package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
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

		setForza(Dado.tiraAncheSenzaRange(Costanti.ELFA_FORZA_MIN, Costanti.ELFA_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.ELFA_DESTREZZA_MIN, Costanti.ELFA_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.ELFA_COSTITUZIONE_MIN, Costanti.ELFA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.ELFA_INTELLIGENZA_MIN, Costanti.ELFA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.ELFA_SAGGEZZA_MIN, Costanti.ELFA_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.ELFA_FORTUNA_MIN, Costanti.ELFA_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.ELFA_CRITICO_MIN, Costanti.ELFA_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.ELFA_PRECISIONE_MIN, Costanti.ELFA_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.ELFA_VELOCITA_MIN, Costanti.ELFA_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.ELFA_PARATA_MIN, Costanti.ELFA_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.ELFA_RESISTENZA_MAGICA_MIN, Costanti.ELFA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.ELFA_MAGIA_MIN, Costanti.ELFA_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.ELFA_FURIA_MIN, Costanti.ELFA_FURIA_MAX));

	}
}
