package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Guerriera extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Guerriera"; }
	public String getNomePlurale() { return "Guerriere"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getModificaDanniForza() { return Costanti.GUERRIERA_MODIFICATORE_DANNI_FORZA; }

	public Guerriera() {
		super(ClassePersonaggio.GUERRIERA);
	}

	public Guerriera(String nome) {
		super(nome, ClassePersonaggio.GUERRIERA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Guerriera.gif");
		setIcona("icone/Guerriera.gif");
		setSaluteMassima(Costanti.GUERRIERA_MAX_SALUTE);
		setMagiaMassima(Costanti.GUERRIERA_MAX_MAGIA);
		setValore(Costanti.GUERRIERA_MAX_VALORE);
		setCoraggio(Costanti.GUERRIERA_MAX_CORAGGIO);
		setCarisma(Costanti.GUERRIERA_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tira(Costanti.GUERRIERA_FORZA_MIN, Costanti.GUERRIERA_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.GUERRIERA_DESTREZZA_MIN, Costanti.GUERRIERA_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.GUERRIERA_COSTITUZIONE_MIN, Costanti.GUERRIERA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.GUERRIERA_INTELLIGENZA_MIN, Costanti.GUERRIERA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.GUERRIERA_SAGGEZZA_MIN, Costanti.GUERRIERA_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.GUERRIERA_FORTUNA_MIN, Costanti.GUERRIERA_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.GUERRIERA_CRITICO_MIN, Costanti.GUERRIERA_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.GUERRIERA_PRECISIONE_MIN, Costanti.GUERRIERA_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.GUERRIERA_VELOCITA_MIN, Costanti.GUERRIERA_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.GUERRIERA_PARATA_MIN, Costanti.GUERRIERA_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.GUERRIERA_RESISTENZA_MAGICA_MIN, Costanti.GUERRIERA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.GUERRIERA_MAGIA_MIN, Costanti.GUERRIERA_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.GUERRIERA_FURIA_MIN, Costanti.GUERRIERA_FURIA_MAX));

	}
}
