package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
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
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}}
