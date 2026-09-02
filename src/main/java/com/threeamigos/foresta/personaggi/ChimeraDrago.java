package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class ChimeraDrago extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Chimera-Drago"; }
	public String getNomePlurale() { return "Chimere-Drago"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public ChimeraDrago() {
		super(ClassePersonaggio.CHIMERA_DRAGO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/ChimeraDrago.gif");
		setSaluteMassima(Costanti.CHIMERADRAGO_MAX_SALUTE);
		setMagiaMassima(Costanti.CHIMERADRAGO_MAX_MAGIA);
		setValore(Costanti.CHIMERADRAGO_MAX_VALORE);
		setCoraggio(Costanti.CHIMERADRAGO_MAX_CORAGGIO);
		setCarisma(Costanti.CHIMERADRAGO_MAX_CARISMA);
		setQuantitaMassima(Costanti.CHIMERADRAGO_MAX_NUMERO);

		setForza(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_FORZA_MIN, Costanti.CHIMERADRAGO_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_DESTREZZA_MIN, Costanti.CHIMERADRAGO_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_COSTITUZIONE_MIN, Costanti.CHIMERADRAGO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_INTELLIGENZA_MIN, Costanti.CHIMERADRAGO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_SAGGEZZA_MIN, Costanti.CHIMERADRAGO_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_FORTUNA_MIN, Costanti.CHIMERADRAGO_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_CRITICO_MIN, Costanti.CHIMERADRAGO_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_PRECISIONE_MIN, Costanti.CHIMERADRAGO_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_VELOCITA_MIN, Costanti.CHIMERADRAGO_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_PARATA_MIN, Costanti.CHIMERADRAGO_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_RESISTENZA_MAGICA_MIN, Costanti.CHIMERADRAGO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_MAGIA_MIN, Costanti.CHIMERADRAGO_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.CHIMERADRAGO_FURIA_MIN, Costanti.CHIMERADRAGO_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.FUOCO;
	}
}
