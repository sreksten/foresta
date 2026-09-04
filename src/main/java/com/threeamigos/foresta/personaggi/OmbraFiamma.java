package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

public class OmbraFiamma extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DALL_APOSTROFO; }

	public String getNomeSingolare() { return "OmbraFiamma"; }
	public String getNomePlurale() { return "OmbreFiamma"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return Costanti.OMBRAFIAMMA_RECUPERO_MAGIA; }
	public int getModificaDanniForza() { return Costanti.OMBRAFIAMMA_MODIFICATORE_DANNI_FORZA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.OMBRAFIAMMA_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.OMBRAFIAMMA_BERSAGLI_PER_INCANTESIMO_BONUS + super.getBersagliPerIncantesimo(); }

	public OmbraFiamma() {
		super(ClassePersonaggio.OMBRAFIAMMA);
	}

	public OmbraFiamma(String nome) {
		super(nome, ClassePersonaggio.OMBRAFIAMMA);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setImmagine("personaggi/OmbraFiamma.gif");
		setIcona("icone/OmbraFiamma.gif");
		setCorrompibile(true);
		setAmichevole(true);

		md.setSaluteMassima(Costanti.OMBRAFIAMMA_MAX_SALUTE);
		md.setMagiaMassima(Costanti.OMBRAFIAMMA_MAX_MAGIA);
		md.setForzaMassima(Costanti.OMBRAFIAMMA_MAX_FORZA);
		md.setDestrezzaMassima(Costanti.OMBRAFIAMMA_MAX_DESTREZZA);
		md.setCostituzioneMassima(Costanti.OMBRAFIAMMA_MAX_COSTITUZIONE);
		md.setIntelligenzaMassima(Costanti.OMBRAFIAMMA_MAX_INTELLIGENZA);
		md.setSaggezzaMassima(Costanti.OMBRAFIAMMA_MAX_SAGGEZZA);
		md.setCarismaMassimo(Costanti.OMBRAFIAMMA_MAX_CARISMA);
		md.setFortunaMassima(Costanti.OMBRAFIAMMA_MAX_FORTUNA);

		setQuantitaMassima(Costanti.OMBRAFIAMMA_MAX_NUMERO);

		super.impostaValoriDiPartenza(funzione);
	}

	@Override
	protected double getMoltiplicatoreCarico() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCritico() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	protected double getMoltiplicatorePrecisione() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreVelocita() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFurtivita() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreParata() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreResistenzaMagica() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	protected double getMoltiplicatorePercezione() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreSoggezione() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreFuria() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	protected double getMoltiplicatoreCoraggio() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	protected double getMoltiplicatoreValore() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	protected double getMoltiplicatoreNumeroBersagli() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	protected double getMoltiplicatoreStanchezza() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.OMBRAFIAMMA_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmortale() {
		return true;
	}

	@Override
	public int getDanniInCombattimento() {
		return 100;
	}
}
