package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Goblin extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Goblin"; }
	public String getNomePlurale() { return "Goblin"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Goblin() {
		super(ClassePersonaggio.GOBLIN);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Goblin.gif");
		setIcona("icone/Goblin.gif");
		setSaluteMassima(Costanti.GOBLIN_MAX_SALUTE);
		setMagiaMassima(Costanti.GOBLIN_MAX_MAGIA);
		setValore(Costanti.GOBLIN_MAX_VALORE);
		setCoraggio(Costanti.GOBLIN_MAX_CORAGGIO);
		setCarisma(Costanti.GOBLIN_MAX_CARISMA);
		setQuantitaMassima(Costanti.GOBLIN_MAX_NUMERO);
		setCorrompibile(true);

		setForza(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_FORZA_MIN, Costanti.GOBLIN_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_DESTREZZA_MIN, Costanti.GOBLIN_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_COSTITUZIONE_MIN, Costanti.GOBLIN_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_INTELLIGENZA_MIN, Costanti.GOBLIN_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_SAGGEZZA_MIN, Costanti.GOBLIN_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_FORTUNA_MIN, Costanti.GOBLIN_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_CRITICO_MIN, Costanti.GOBLIN_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_PRECISIONE_MIN, Costanti.GOBLIN_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_VELOCITA_MIN, Costanti.GOBLIN_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_PARATA_MIN, Costanti.GOBLIN_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_RESISTENZA_MAGICA_MIN, Costanti.GOBLIN_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_MAGIA_MIN, Costanti.GOBLIN_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.GOBLIN_FURIA_MIN, Costanti.GOBLIN_FURIA_MAX));

	}

	@Override
	public ClassiOfferta[] getOfferteCorruzione() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_MERCENARIO,
				ClassiOfferta.MAPPA_FORESTA,
		};
	}
}
