package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Guerriero extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Guerriero"; }
	public String getNomePlurale() { return "Guerrieri"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getModificaDanniForza() { return Costanti.GUERRIERO_MODIFICATORE_DANNI_FORZA; }

	public Guerriero() {
		super(ClassePersonaggio.GUERRIERO);
	}

	public Guerriero(String nome) {
		super(nome, ClassePersonaggio.GUERRIERO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Guerriero.gif");
		setIcona("icone/Guerriero.gif");
		setSaluteMassima(Costanti.GUERRIERO_MAX_SALUTE);
		setMagiaMassima(Costanti.GUERRIERO_MAX_MAGIA);
		setValore(Costanti.GUERRIERO_MAX_VALORE);
		setCoraggio(Costanti.GUERRIERO_MAX_CORAGGIO);
		setCarisma(Costanti.GUERRIERO_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_FORZA_MIN, Costanti.GUERRIERO_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_DESTREZZA_MIN, Costanti.GUERRIERO_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_COSTITUZIONE_MIN, Costanti.GUERRIERO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_INTELLIGENZA_MIN, Costanti.GUERRIERO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_SAGGEZZA_MIN, Costanti.GUERRIERO_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_FORTUNA_MIN, Costanti.GUERRIERO_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_CRITICO_MIN, Costanti.GUERRIERO_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_PRECISIONE_MIN, Costanti.GUERRIERO_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_VELOCITA_MIN, Costanti.GUERRIERO_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_PARATA_MIN, Costanti.GUERRIERO_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_RESISTENZA_MAGICA_MIN, Costanti.GUERRIERO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_MAGIA_MIN, Costanti.GUERRIERO_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.GUERRIERO_FURIA_MIN, Costanti.GUERRIERO_FURIA_MAX));

	}
}
