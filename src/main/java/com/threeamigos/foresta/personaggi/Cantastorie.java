package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Cantastorie extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Cantastorie"; }
	public String getNomePlurale() { return "Cantastorie"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Cantastorie() {
		super(ClassePersonaggio.CANTASTORIE);
	}

	public Cantastorie(String nome) {
		super(nome, ClassePersonaggio.CANTASTORIE);
	}
	
	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Cantastorie.gif");
		setIcona("icone/Cantastorie.gif");
		setSaluteMassima(Costanti.CANTASTORIE_MAX_SALUTE);
		setMagiaMassima(Costanti.CANTASTORIE_MAX_MAGIA);
		setValore(Costanti.CANTASTORIE_MAX_VALORE);
		setCoraggio(Costanti.CANTASTORIE_MAX_CORAGGIO);
		setCarisma(Costanti.CANTASTORIE_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_FORZA_MIN, Costanti.CANTASTORIE_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_DESTREZZA_MIN, Costanti.CANTASTORIE_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_COSTITUZIONE_MIN, Costanti.CANTASTORIE_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_INTELLIGENZA_MIN, Costanti.CANTASTORIE_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_SAGGEZZA_MIN, Costanti.CANTASTORIE_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_FORTUNA_MIN, Costanti.CANTASTORIE_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_CRITICO_MIN, Costanti.CANTASTORIE_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_PRECISIONE_MIN, Costanti.CANTASTORIE_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_VELOCITA_MIN, Costanti.CANTASTORIE_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_PARATA_MIN, Costanti.CANTASTORIE_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_RESISTENZA_MAGICA_MIN, Costanti.CANTASTORIE_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_MAGIA_MIN, Costanti.CANTASTORIE_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.CANTASTORIE_FURIA_MIN, Costanti.CANTASTORIE_FURIA_MAX));

	}
}
