package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Fantasma extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Fantasma"; }
	public String getNomePlurale() { return "Fantasmi"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Fantasma() {
		super(ClassePersonaggio.FANTASMA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Fantasma.gif");
		setSaluteMassima(Costanti.FANTASMA_MAX_SALUTE);
		setMagiaMassima(Costanti.FANTASMA_MAX_MAGIA);
		setValore(Costanti.FANTASMA_MAX_VALORE);
		setCoraggio(Costanti.FANTASMA_MAX_CORAGGIO);
		setCarisma(Costanti.FANTASMA_MAX_CARISMA);
		setQuantitaMassima(Costanti.FANTASMA_MAX_NUMERO);

		setForza(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_FORZA_MIN, Costanti.FANTASMA_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_DESTREZZA_MIN, Costanti.FANTASMA_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_COSTITUZIONE_MIN, Costanti.FANTASMA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_INTELLIGENZA_MIN, Costanti.FANTASMA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_SAGGEZZA_MIN, Costanti.FANTASMA_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_FORTUNA_MIN, Costanti.FANTASMA_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_CRITICO_MIN, Costanti.FANTASMA_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_PRECISIONE_MIN, Costanti.FANTASMA_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_VELOCITA_MIN, Costanti.FANTASMA_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_PARATA_MIN, Costanti.FANTASMA_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_RESISTENZA_MAGICA_MIN, Costanti.FANTASMA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_MAGIA_MIN, Costanti.FANTASMA_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.FANTASMA_FURIA_MIN, Costanti.FANTASMA_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}}
