package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

public class Spettro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNO; }
	public String getADS() { return Misc.LO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELLO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UNO; }

	public String getNomeSingolare() { return "Spettro"; }
	public String getNomePlurale() { return "Spettri"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Spettro() {
		super(ClassePersonaggio.SPETTRO);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setImmagine("personaggi/Spettro.gif");

		md.setSaluteMassima(Costanti.SPETTRO_MAX_SALUTE);
		md.setMagiaMassima(Costanti.SPETTRO_MAX_MAGIA);
		md.setForzaMassima(Costanti.SPETTRO_MAX_FORZA);
		md.setDestrezzaMassima(Costanti.SPETTRO_MAX_DESTREZZA);
		md.setCostituzioneMassima(Costanti.SPETTRO_MAX_COSTITUZIONE);
		md.setIntelligenzaMassima(Costanti.SPETTRO_MAX_INTELLIGENZA);
		md.setSaggezzaMassima(Costanti.SPETTRO_MAX_SAGGEZZA);
		md.setCarismaMassimo(Costanti.SPETTRO_MAX_CARISMA);
		md.setFortunaMassima(Costanti.SPETTRO_MAX_FORTUNA);

		setQuantitaMassima(Costanti.SPETTRO_MAX_NUMERO);

		super.impostaValoriDiPartenza(funzione);
	}

	@Override
	protected double getMoltiplicatoreCarico() {
		return Costanti.SPETTRO_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.SPETTRO_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCritico() {
		return Costanti.SPETTRO_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.SPETTRO_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	protected double getMoltiplicatorePrecisione() {
		return Costanti.SPETTRO_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.SPETTRO_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreVelocita() {
		return Costanti.SPETTRO_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.SPETTRO_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFurtivita() {
		return Costanti.SPETTRO_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.SPETTRO_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreParata() {
		return Costanti.SPETTRO_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.SPETTRO_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreResistenzaMagica() {
		return Costanti.SPETTRO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.SPETTRO_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	protected double getMoltiplicatorePercezione() {
		return Costanti.SPETTRO_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.SPETTRO_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreSoggezione() {
		return Costanti.SPETTRO_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.SPETTRO_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFuria() {
		return Costanti.SPETTRO_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.SPETTRO_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCoraggio() {
		return Costanti.SPETTRO_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.SPETTRO_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreValore() {
		return Costanti.SPETTRO_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.SPETTRO_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreNumeroBersagli() {
		return Costanti.SPETTRO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.SPETTRO_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	protected double getMoltiplicatoreStanchezza() {
		return Costanti.SPETTRO_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.SPETTRO_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
