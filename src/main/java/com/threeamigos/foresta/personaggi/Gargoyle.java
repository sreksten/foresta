package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

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
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setImmagine("personaggi/Gargoyle.gif");

		md.setSaluteMassima(Costanti.GARGOYLE_MAX_SALUTE);
		md.setMagiaMassima(Costanti.GARGOYLE_MAX_MAGIA);
		md.setForzaMassima(Costanti.GARGOYLE_MAX_FORZA);
		md.setDestrezzaMassima(Costanti.GARGOYLE_MAX_DESTREZZA);
		md.setCostituzioneMassima(Costanti.GARGOYLE_MAX_COSTITUZIONE);
		md.setIntelligenzaMassima(Costanti.GARGOYLE_MAX_INTELLIGENZA);
		md.setSaggezzaMassima(Costanti.GARGOYLE_MAX_SAGGEZZA);
		md.setCarismaMassimo(Costanti.GARGOYLE_MAX_CARISMA);
		md.setFortunaMassima(Costanti.GARGOYLE_MAX_FORTUNA);

		setQuantitaMassima(Costanti.GARGOYLE_MAX_NUMERO);

		super.impostaValoriDiPartenza(funzione);
	}

	@Override
	protected double getMoltiplicatoreCarico() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCritico() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	protected double getMoltiplicatorePrecisione() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreVelocita() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFurtivita() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreParata() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreResistenzaMagica() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	protected double getMoltiplicatorePercezione() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreSoggezione() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFuria() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCoraggio() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreValore() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreNumeroBersagli() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	protected double getMoltiplicatoreStanchezza() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return true;
	}}
