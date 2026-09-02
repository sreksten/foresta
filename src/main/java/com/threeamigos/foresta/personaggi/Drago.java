package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Drago extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Drago"; }
	public String getNomePlurale() { return "Draghi"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoMagia() { return Costanti.DRAGO_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.DRAGO_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.DRAGO_BERSAGLI_PER_INCANTESIMO; }

	public Drago() {
		super(ClassePersonaggio.DRAGO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Drago.gif");
		setSaluteMassima(Costanti.DRAGO_MAX_SALUTE);
		setMagiaMassima(Costanti.DRAGO_MAX_MAGIA);
		setValore(Costanti.DRAGO_MAX_VALORE);
		setCoraggio(Costanti.DRAGO_MAX_CORAGGIO);
		setCarisma(Costanti.DRAGO_MAX_CARISMA);

		setForza(Dado.tira(Costanti.DRAGO_FORZA_MIN, Costanti.DRAGO_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.DRAGO_DESTREZZA_MIN, Costanti.DRAGO_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.DRAGO_COSTITUZIONE_MIN, Costanti.DRAGO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.DRAGO_INTELLIGENZA_MIN, Costanti.DRAGO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.DRAGO_SAGGEZZA_MIN, Costanti.DRAGO_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.DRAGO_FORTUNA_MIN, Costanti.DRAGO_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.DRAGO_CRITICO_MIN, Costanti.DRAGO_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.DRAGO_PRECISIONE_MIN, Costanti.DRAGO_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.DRAGO_VELOCITA_MIN, Costanti.DRAGO_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.DRAGO_PARATA_MIN, Costanti.DRAGO_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.DRAGO_RESISTENZA_MAGICA_MIN, Costanti.DRAGO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.DRAGO_MAGIA_MIN, Costanti.DRAGO_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.DRAGO_FURIA_MIN, Costanti.DRAGO_FURIA_MAX));

	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.FUOCO;
	}
}
