package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Minotauro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public ClassePersonaggio getClasse() { return ClassePersonaggio.MINOTAURO; }
	public String getNomeSingolare() { return "Minotauro"; }
	public String getNomePlurale() { return "Minotauri"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Minotauro() {
		super(ClassePersonaggio.MINOTAURO);
	}

	@Override
	public ClassiOfferta[] getOfferteCorruzione() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_MERCENARIO,
				ClassiOfferta.MAPPA_FORESTA,
		};
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Minotauro.gif");
		setIcona("icone/Minotauro.gif");
		setSaluteMassima(Costanti.MINOTAURO_MAX_SALUTE);
		setMagiaMassima(Costanti.MINOTAURO_MAX_MAGIA);
		setValore(Costanti.MINOTAURO_MAX_VALORE);
		setCoraggio(Costanti.MINOTAURO_MAX_CORAGGIO);
		setCarisma(Costanti.MINOTAURO_MAX_CARISMA);
		setQuantitaMassima(Costanti.MINOTAURO_MAX_NUMERO);
		setCorrompibile(true);

		setForza(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_FORZA_MIN, Costanti.MINOTAURO_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_DESTREZZA_MIN, Costanti.MINOTAURO_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_COSTITUZIONE_MIN, Costanti.MINOTAURO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_INTELLIGENZA_MIN, Costanti.MINOTAURO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_SAGGEZZA_MIN, Costanti.MINOTAURO_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_FORTUNA_MIN, Costanti.MINOTAURO_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_CRITICO_MIN, Costanti.MINOTAURO_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_PRECISIONE_MIN, Costanti.MINOTAURO_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_VELOCITA_MIN, Costanti.MINOTAURO_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_PARATA_MIN, Costanti.MINOTAURO_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_RESISTENZA_MAGICA_MIN, Costanti.MINOTAURO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_MAGIA_MIN, Costanti.MINOTAURO_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.MINOTAURO_FURIA_MIN, Costanti.MINOTAURO_FURIA_MAX));

	}
}
