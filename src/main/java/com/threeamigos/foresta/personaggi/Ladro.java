package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Ladro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Ladro"; }
	public String getNomePlurale() { return "Ladri"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Ladro() {
		super(ClassePersonaggio.LADRO);
	}

	public Ladro(String nome) {
		super(nome, ClassePersonaggio.LADRO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Ladro.gif");
		setIcona("icone/Ladro.gif");
		setSaluteMassima(Costanti.LADRO_MAX_SALUTE);
		setMagiaMassima(Costanti.LADRO_MAX_MAGIA);
		setValore(Costanti.LADRO_MAX_VALORE);
		setCoraggio(Costanti.LADRO_MAX_CORAGGIO);
		setCarisma(Costanti.LADRO_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tira(Costanti.LADRO_FORZA_MIN, Costanti.LADRO_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.LADRO_DESTREZZA_MIN, Costanti.LADRO_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.LADRO_COSTITUZIONE_MIN, Costanti.LADRO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.LADRO_INTELLIGENZA_MIN, Costanti.LADRO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.LADRO_SAGGEZZA_MIN, Costanti.LADRO_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.LADRO_FORTUNA_MIN, Costanti.LADRO_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.LADRO_CRITICO_MIN, Costanti.LADRO_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.LADRO_PRECISIONE_MIN, Costanti.LADRO_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.LADRO_VELOCITA_MIN, Costanti.LADRO_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.LADRO_PARATA_MIN, Costanti.LADRO_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.LADRO_RESISTENZA_MAGICA_MIN, Costanti.LADRO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.LADRO_MAGIA_MIN, Costanti.LADRO_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.LADRO_FURIA_MIN, Costanti.LADRO_FURIA_MAX));

	}
}
