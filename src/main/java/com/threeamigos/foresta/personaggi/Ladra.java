package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Ladra extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Ladra"; }
	public String getNomePlurale() { return "Ladre"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Ladra() {
		super(ClassePersonaggio.LADRA);
	}

	public Ladra(String nome) {
		super(nome, ClassePersonaggio.LADRA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Ladra.gif");
		setIcona("icone/Ladra.gif");
		setSaluteMassima(Costanti.LADRA_MAX_SALUTE);
		setMagiaMassima(Costanti.LADRA_MAX_MAGIA);
		setValore(Costanti.LADRA_MAX_VALORE);
		setCoraggio(Costanti.LADRA_MAX_CORAGGIO);
		setCarisma(Costanti.LADRA_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tira(Costanti.LADRA_FORZA_MIN, Costanti.LADRA_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.LADRA_DESTREZZA_MIN, Costanti.LADRA_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.LADRA_COSTITUZIONE_MIN, Costanti.LADRA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.LADRA_INTELLIGENZA_MIN, Costanti.LADRA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.LADRA_SAGGEZZA_MIN, Costanti.LADRA_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.LADRA_FORTUNA_MIN, Costanti.LADRA_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.LADRA_CRITICO_MIN, Costanti.LADRA_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.LADRA_PRECISIONE_MIN, Costanti.LADRA_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.LADRA_VELOCITA_MIN, Costanti.LADRA_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.LADRA_PARATA_MIN, Costanti.LADRA_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.LADRA_RESISTENZA_MAGICA_MIN, Costanti.LADRA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.LADRA_MAGIA_MIN, Costanti.LADRA_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.LADRA_FURIA_MIN, Costanti.LADRA_FURIA_MAX));

	}
}
