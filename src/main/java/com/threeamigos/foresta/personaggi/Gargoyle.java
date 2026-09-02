package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Gargoyle extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Gargoyle"; }
	public String getNomePlurale() { return "Gargoyle"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoForza() { return Costanti.GARGOYLE_RECUPERO_FORZA; }
	public int getRecuperoMagia() { return Costanti.GARGOYLE_RECUPERO_MAGIA; }

	public Gargoyle() {
		super(ClassePersonaggio.GARGOYLE);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Gargoyle.gif");
		setSaluteMassima(Costanti.GARGOYLE_MAX_SALUTE);
		setMagiaMassima(Costanti.GARGOYLE_MAX_MAGIA);
		setValore(Costanti.GARGOYLE_MAX_VALORE);
		setCoraggio(Costanti.GARGOYLE_MAX_CORAGGIO);
		setCarisma(Costanti.GARGOYLE_MAX_CARISMA);
		setQuantitaMassima(Costanti.GARGOYLE_MAX_NUMERO);

		setForza(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_FORZA_MIN, Costanti.GARGOYLE_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_DESTREZZA_MIN, Costanti.GARGOYLE_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_COSTITUZIONE_MIN, Costanti.GARGOYLE_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_INTELLIGENZA_MIN, Costanti.GARGOYLE_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_SAGGEZZA_MIN, Costanti.GARGOYLE_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_FORTUNA_MIN, Costanti.GARGOYLE_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_CRITICO_MIN, Costanti.GARGOYLE_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_PRECISIONE_MIN, Costanti.GARGOYLE_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_VELOCITA_MIN, Costanti.GARGOYLE_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_PARATA_MIN, Costanti.GARGOYLE_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_RESISTENZA_MAGICA_MIN, Costanti.GARGOYLE_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_MAGIA_MIN, Costanti.GARGOYLE_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.GARGOYLE_FURIA_MIN, Costanti.GARGOYLE_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return true;
	}}
