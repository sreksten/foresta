package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Lich extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Lich"; }
	public String getNomePlurale() { return "Lich"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoMagia() { return Costanti.LICH_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.LICH_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.LICH_BERSAGLI_PER_INCANTESIMO; }

	public Lich() {
		super(ClassePersonaggio.LICH);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Lich.gif");
		setSaluteMassima(Costanti.LICH_MAX_SALUTE);
		setMagiaMassima(Costanti.LICH_MAX_MAGIA);
		setValore(Costanti.LICH_MAX_VALORE);
		setCoraggio(Costanti.LICH_MAX_CORAGGIO);
		setCarisma(Costanti.LICH_MAX_CARISMA);

		setForza(Dado.tira(Costanti.LICH_FORZA_MIN, Costanti.LICH_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.LICH_DESTREZZA_MIN, Costanti.LICH_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.LICH_COSTITUZIONE_MIN, Costanti.LICH_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.LICH_INTELLIGENZA_MIN, Costanti.LICH_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.LICH_SAGGEZZA_MIN, Costanti.LICH_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.LICH_FORTUNA_MIN, Costanti.LICH_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.LICH_CRITICO_MIN, Costanti.LICH_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.LICH_PRECISIONE_MIN, Costanti.LICH_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.LICH_VELOCITA_MIN, Costanti.LICH_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.LICH_PARATA_MIN, Costanti.LICH_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.LICH_RESISTENZA_MAGICA_MIN, Costanti.LICH_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.LICH_MAGIA_MIN, Costanti.LICH_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.LICH_FURIA_MIN, Costanti.LICH_FURIA_MAX));

	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}}
