package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

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
	protected void impostaValori() {
		setImmagine("personaggi/OmbraFiamma.gif");
		setIcona("icone/OmbraFiamma.gif");
		setSaluteMassima(Costanti.OMBRAFIAMMA_MAX_SALUTE);
		setMagiaMassima(Costanti.OMBRAFIAMMA_MAX_MAGIA);
		setValore(Costanti.OMBRAFIAMMA_MAX_VALORE);
		setCoraggio(Costanti.OMBRAFIAMMA_MAX_CORAGGIO);
		setCarisma(Costanti.OMBRAFIAMMA_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tira(Costanti.OMBRAFIAMMA_FORZA_MIN, Costanti.OMBRAFIAMMA_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.OMBRAFIAMMA_DESTREZZA_MIN, Costanti.OMBRAFIAMMA_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.OMBRAFIAMMA_COSTITUZIONE_MIN, Costanti.OMBRAFIAMMA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.OMBRAFIAMMA_INTELLIGENZA_MIN, Costanti.OMBRAFIAMMA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.OMBRAFIAMMA_SAGGEZZA_MIN, Costanti.OMBRAFIAMMA_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.OMBRAFIAMMA_FORTUNA_MIN, Costanti.OMBRAFIAMMA_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.OMBRAFIAMMA_CRITICO_MIN, Costanti.OMBRAFIAMMA_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.OMBRAFIAMMA_PRECISIONE_MIN, Costanti.OMBRAFIAMMA_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.OMBRAFIAMMA_VELOCITA_MIN, Costanti.OMBRAFIAMMA_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.OMBRAFIAMMA_PARATA_MIN, Costanti.OMBRAFIAMMA_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.OMBRAFIAMMA_RESISTENZA_MAGICA_MIN, Costanti.OMBRAFIAMMA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.OMBRAFIAMMA_MAGIA_MIN, Costanti.OMBRAFIAMMA_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.OMBRAFIAMMA_FURIA_MIN, Costanti.OMBRAFIAMMA_FURIA_MAX));

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
