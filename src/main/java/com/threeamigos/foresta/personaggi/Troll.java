package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Troll extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Troll"; }
	public String getNomePlurale() { return "Troll"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Troll() {
		super(ClassePersonaggio.TROLL);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Troll.gif");
		setSaluteMassima(Costanti.TROLL_MAX_SALUTE);
		setMagiaMassima(Costanti.TROLL_MAX_MAGIA);
		setValore(Costanti.TROLL_MAX_VALORE);
		setCoraggio(Costanti.TROLL_MAX_CORAGGIO);
		setCarisma(Costanti.TROLL_MAX_CARISMA);
		setQuantitaMassima(Costanti.TROLL_MAX_NUMERO);
		setCorrompibile(true);

		setForza(Dado.tira(Costanti.TROLL_FORZA_MIN, Costanti.TROLL_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.TROLL_DESTREZZA_MIN, Costanti.TROLL_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.TROLL_COSTITUZIONE_MIN, Costanti.TROLL_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.TROLL_INTELLIGENZA_MIN, Costanti.TROLL_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.TROLL_SAGGEZZA_MIN, Costanti.TROLL_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.TROLL_FORTUNA_MIN, Costanti.TROLL_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.TROLL_CRITICO_MIN, Costanti.TROLL_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.TROLL_PRECISIONE_MIN, Costanti.TROLL_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.TROLL_VELOCITA_MIN, Costanti.TROLL_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.TROLL_PARATA_MIN, Costanti.TROLL_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.TROLL_RESISTENZA_MAGICA_MIN, Costanti.TROLL_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.TROLL_MAGIA_MIN, Costanti.TROLL_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.TROLL_FURIA_MIN, Costanti.TROLL_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.TERRA;
	}
}
