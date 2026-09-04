package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

public class Viverna extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Viverna"; }
	public String getNomePlurale() { return "Viverne"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return Costanti.VIVERNA_RECUPERO_MAGIA; }

	public Viverna() {
		super(ClassePersonaggio.VIVERNA);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setImmagine("personaggi/Viverna.gif");

		md.setSaluteMassima(Costanti.VIVERNA_MAX_SALUTE);
		md.setMagiaMassima(Costanti.VIVERNA_MAX_MAGIA);
		md.setForzaMassima(Costanti.VIVERNA_MAX_FORZA);
		md.setDestrezzaMassima(Costanti.VIVERNA_MAX_DESTREZZA);
		md.setCostituzioneMassima(Costanti.VIVERNA_MAX_COSTITUZIONE);
		md.setIntelligenzaMassima(Costanti.VIVERNA_MAX_INTELLIGENZA);
		md.setSaggezzaMassima(Costanti.VIVERNA_MAX_SAGGEZZA);
		md.setCarismaMassimo(Costanti.VIVERNA_MAX_CARISMA);
		md.setFortunaMassima(Costanti.VIVERNA_MAX_FORTUNA);

		setQuantitaMassima(Costanti.VIVERNA_MAX_NUMERO);

		super.impostaValoriDiPartenza(funzione);
	}

	@Override
	protected double getMoltiplicatoreCarico() {
		return Costanti.VIVERNA_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.VIVERNA_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCritico() {
		return Costanti.VIVERNA_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.VIVERNA_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	protected double getMoltiplicatorePrecisione() {
		return Costanti.VIVERNA_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.VIVERNA_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreVelocita() {
		return Costanti.VIVERNA_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.VIVERNA_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFurtivita() {
		return Costanti.VIVERNA_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.VIVERNA_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreParata() {
		return Costanti.VIVERNA_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.VIVERNA_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreResistenzaMagica() {
		return Costanti.VIVERNA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.VIVERNA_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	protected double getMoltiplicatorePercezione() {
		return Costanti.VIVERNA_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.VIVERNA_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreSoggezione() {
		return Costanti.VIVERNA_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.VIVERNA_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFuria() {
		return Costanti.VIVERNA_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.VIVERNA_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCoraggio() {
		return Costanti.VIVERNA_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.VIVERNA_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreValore() {
		return Costanti.VIVERNA_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.VIVERNA_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreNumeroBersagli() {
		return Costanti.VIVERNA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.VIVERNA_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	protected double getMoltiplicatoreStanchezza() {
		return Costanti.VIVERNA_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.VIVERNA_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.FUOCO;
	}
}
