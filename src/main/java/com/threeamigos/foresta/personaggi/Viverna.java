package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

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
	protected void impostaValori() {
		setImmagine("personaggi/Viverna.gif");
		setSaluteMassima(Costanti.VIVERNA_MAX_SALUTE);
		setMagiaMassima(Costanti.VIVERNA_MAX_MAGIA);
		setValore(Costanti.VIVERNA_MAX_VALORE);
		setCoraggio(Costanti.VIVERNA_MAX_CORAGGIO);
		setCarisma(Costanti.VIVERNA_MAX_CARISMA);
		setQuantitaMassima(Costanti.VIVERNA_MAX_NUMERO);

		setForza(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_FORZA_MIN, Costanti.VIVERNA_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_DESTREZZA_MIN, Costanti.VIVERNA_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_COSTITUZIONE_MIN, Costanti.VIVERNA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_INTELLIGENZA_MIN, Costanti.VIVERNA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_SAGGEZZA_MIN, Costanti.VIVERNA_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_FORTUNA_MIN, Costanti.VIVERNA_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_CRITICO_MIN, Costanti.VIVERNA_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_PRECISIONE_MIN, Costanti.VIVERNA_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_VELOCITA_MIN, Costanti.VIVERNA_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_PARATA_MIN, Costanti.VIVERNA_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_RESISTENZA_MAGICA_MIN, Costanti.VIVERNA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_MAGIA_MIN, Costanti.VIVERNA_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.VIVERNA_FURIA_MIN, Costanti.VIVERNA_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.FUOCO;
	}
}
