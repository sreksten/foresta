package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.tools.Misc;

public class Idra extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public ClassiPersonaggio getClasse() { return ClassiPersonaggio.IDRA; }
	public String getNomeSingolare() { return "Idra"; }
	public String getNomePlurale() { return "Idre"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Idra() {
		super(ClassiPersonaggio.IDRA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Idra.gif");
		setForzaMassima(600);
		setMagiaMassima(0);
		setValore(70);
		setCoraggio(70);
		setCarisma(0);
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.TERRA || 
				classeIncantesimo == ClassiIncantesimo.ACQUA ||
				classeIncantesimo == ClassiIncantesimo.ARIA;
	}}
