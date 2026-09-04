package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

public class Arpia extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public String getNomeSingolare() { return "Arpia"; }
	public String getNomePlurale() { return "Arpie"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Arpia() {
		super(ClassePersonaggio.ARPIA);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setImmagine("personaggi/Arpia.gif");

		md.setSaluteMassima(Costanti.ARPIA_MAX_SALUTE);
		md.setMagiaMassima(Costanti.ARPIA_MAX_MAGIA);
		md.setForzaMassima(Costanti.ARPIA_MAX_FORZA);
		md.setDestrezzaMassima(Costanti.ARPIA_MAX_DESTREZZA);
		md.setCostituzioneMassima(Costanti.ARPIA_MAX_COSTITUZIONE);
		md.setIntelligenzaMassima(Costanti.ARPIA_MAX_INTELLIGENZA);
		md.setSaggezzaMassima(Costanti.ARPIA_MAX_SAGGEZZA);
		md.setCarismaMassimo(Costanti.ARPIA_MAX_CARISMA);
		md.setFortunaMassima(Costanti.ARPIA_MAX_FORTUNA);

		setQuantitaMassima(Costanti.ARPIA_MAX_NUMERO);

		super.impostaValoriDiPartenza(funzione);
	}

	@Override
	protected double getMoltiplicatoreCarico() {
		return Costanti.ARPIA_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.ARPIA_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCritico() {
		return Costanti.ARPIA_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.ARPIA_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	protected double getMoltiplicatorePrecisione() {
		return Costanti.ARPIA_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.ARPIA_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreVelocita() {
		return Costanti.ARPIA_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.ARPIA_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFurtivita() {
		return Costanti.ARPIA_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.ARPIA_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreParata() {
		return Costanti.ARPIA_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.ARPIA_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreResistenzaMagica() {
		return Costanti.ARPIA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.ARPIA_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	protected double getMoltiplicatorePercezione() {
		return Costanti.ARPIA_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.ARPIA_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreSoggezione() {
		return Costanti.ARPIA_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.ARPIA_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFuria() {
		return Costanti.ARPIA_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.ARPIA_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCoraggio() {
		return Costanti.ARPIA_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.ARPIA_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreValore() {
		return Costanti.ARPIA_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.ARPIA_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreNumeroBersagli() {
		return Costanti.ARPIA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.ARPIA_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	protected double getMoltiplicatoreStanchezza() {
		return Costanti.ARPIA_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.ARPIA_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.ARIA;
	}
}
