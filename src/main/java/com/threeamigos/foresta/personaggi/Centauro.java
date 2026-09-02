package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Centauro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Centauro"; }
	public String getNomePlurale() { return "Centauri"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Centauro() {
		super(ClassePersonaggio.CENTAURO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Centauro.gif");
		setIcona("icone/Centauro.gif");
		setSaluteMassima(Costanti.CENTAURO_MAX_SALUTE);
		setMagiaMassima(Costanti.CENTAURO_MAX_MAGIA);
		setValore(Costanti.CENTAURO_MAX_VALORE);
		setCoraggio(Costanti.CENTAURO_MAX_CORAGGIO);
		setCarisma(Costanti.CENTAURO_MAX_CARISMA);
		setQuantitaMassima(Costanti.CENTAURO_MAX_NUMERO);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tira(Costanti.CENTAURO_FORZA_MIN, Costanti.CENTAURO_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.CENTAURO_DESTREZZA_MIN, Costanti.CENTAURO_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.CENTAURO_COSTITUZIONE_MIN, Costanti.CENTAURO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.CENTAURO_INTELLIGENZA_MIN, Costanti.CENTAURO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.CENTAURO_SAGGEZZA_MIN, Costanti.CENTAURO_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.CENTAURO_FORTUNA_MIN, Costanti.CENTAURO_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.CENTAURO_CRITICO_MIN, Costanti.CENTAURO_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.CENTAURO_PRECISIONE_MIN, Costanti.CENTAURO_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.CENTAURO_VELOCITA_MIN, Costanti.CENTAURO_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.CENTAURO_PARATA_MIN, Costanti.CENTAURO_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.CENTAURO_RESISTENZA_MAGICA_MIN, Costanti.CENTAURO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.CENTAURO_MAGIA_MIN, Costanti.CENTAURO_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.CENTAURO_FURIA_MIN, Costanti.CENTAURO_FURIA_MAX));

	}

	@Override
	public ClassiOfferta[] getOfferteAmicizia() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_GRATUITO,
				ClassiOfferta.AIUTO_MERCENARIO,
				ClassiOfferta.INCANTESIMI,
				ClassiOfferta.INFORMAZIONI,
				ClassiOfferta.MAPPA_FORESTA,
				ClassiOfferta.MAPPA_ZONA,
				ClassiOfferta.PASTO
		};
	}

	@Override
	public ClassiOfferta[] getOfferteCorruzione() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_GRATUITO,
				ClassiOfferta.AIUTO_MERCENARIO,
				ClassiOfferta.INCANTESIMI,
				ClassiOfferta.INFORMAZIONI,
				ClassiOfferta.MAPPA_FORESTA,
				ClassiOfferta.MAPPA_ZONA,
				ClassiOfferta.PASTO
		};
	}
}
