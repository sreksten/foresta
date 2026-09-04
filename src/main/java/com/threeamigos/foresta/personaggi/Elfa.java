package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

public class Elfa extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public String getNomeSingolare() { return "Elfa"; }
	public String getNomePlurale() { return "Elfe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return Costanti.ELFA_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.ELFA_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.ELFA_BERSAGLI_PER_INCANTESIMO_BONUS + super.getBersagliPerIncantesimo(); }

	public Elfa() {
		super(ClassePersonaggio.ELFA);
	}

	public Elfa(String nome) {
		super(nome, ClassePersonaggio.ELFA);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setImmagine("personaggi/Elfa.gif");
		setIcona("icone/Elfa.gif");
		setCorrompibile(true);
		setAmichevole(true);

		md.setSaluteMassima(Costanti.ELFA_MAX_SALUTE);
		md.setMagiaMassima(Costanti.ELFA_MAX_MAGIA);
		md.setForzaMassima(Costanti.ELFA_MAX_FORZA);
		md.setDestrezzaMassima(Costanti.ELFA_MAX_DESTREZZA);
		md.setCostituzioneMassima(Costanti.ELFA_MAX_COSTITUZIONE);
		md.setIntelligenzaMassima(Costanti.ELFA_MAX_INTELLIGENZA);
		md.setSaggezzaMassima(Costanti.ELFA_MAX_SAGGEZZA);
		md.setCarismaMassimo(Costanti.ELFA_MAX_CARISMA);
		md.setFortunaMassima(Costanti.ELFA_MAX_FORTUNA);

		setQuantitaMassima(Costanti.ELFA_MAX_NUMERO);

		super.impostaValoriDiPartenza(funzione);
	}

	@Override
	protected double getMoltiplicatoreCarico() {
		return Costanti.ELFA_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.ELFA_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCritico() {
		return Costanti.ELFA_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.ELFA_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	protected double getMoltiplicatorePrecisione() {
		return Costanti.ELFA_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.ELFA_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreVelocita() {
		return Costanti.ELFA_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.ELFA_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFurtivita() {
		return Costanti.ELFA_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.ELFA_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreParata() {
		return Costanti.ELFA_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.ELFA_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreResistenzaMagica() {
		return Costanti.ELFA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.ELFA_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	protected double getMoltiplicatorePercezione() {
		return Costanti.ELFA_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.ELFA_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreSoggezione() {
		return Costanti.ELFA_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.ELFA_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFuria() {
		return Costanti.ELFA_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.ELFA_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCoraggio() {
		return Costanti.ELFA_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.ELFA_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreValore() {
		return Costanti.ELFA_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.ELFA_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreNumeroBersagli() {
		return Costanti.ELFA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.ELFA_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	protected double getMoltiplicatoreStanchezza() {
		return Costanti.ELFA_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.ELFA_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}
}
