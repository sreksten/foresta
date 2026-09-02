package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Idra extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public ClassePersonaggio getClasse() { return ClassePersonaggio.IDRA; }
	public String getNomeSingolare() { return "Idra"; }
	public String getNomePlurale() { return "Idre"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Idra() {
		super(ClassePersonaggio.IDRA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Idra.gif");
		setSaluteMassima(Costanti.IDRA_MAX_SALUTE);
		setMagiaMassima(Costanti.IDRA_MAX_MAGIA);
		setValore(Costanti.IDRA_MAX_VALORE);
		setCoraggio(Costanti.IDRA_MAX_CORAGGIO);
		setCarisma(Costanti.IDRA_MAX_CARISMA);

		setForza(Dado.tiraAncheSenzaRange(Costanti.IDRA_FORZA_MIN, Costanti.IDRA_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.IDRA_DESTREZZA_MIN, Costanti.IDRA_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.IDRA_COSTITUZIONE_MIN, Costanti.IDRA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.IDRA_INTELLIGENZA_MIN, Costanti.IDRA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.IDRA_SAGGEZZA_MIN, Costanti.IDRA_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.IDRA_FORTUNA_MIN, Costanti.IDRA_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.IDRA_CRITICO_MIN, Costanti.IDRA_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.IDRA_PRECISIONE_MIN, Costanti.IDRA_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.IDRA_VELOCITA_MIN, Costanti.IDRA_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.IDRA_PARATA_MIN, Costanti.IDRA_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.IDRA_RESISTENZA_MAGICA_MIN, Costanti.IDRA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.IDRA_MAGIA_MIN, Costanti.IDRA_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.IDRA_FURIA_MIN, Costanti.IDRA_FURIA_MAX));

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
