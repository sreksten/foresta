package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Titano extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Titano"; }
	public String getNomePlurale() { return "Titani"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Titano() {
		super(ClassePersonaggio.TITANO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Titano.gif");
		setIcona("icone/Titano.gif");
		setSaluteMassima(Costanti.TITANO_MAX_SALUTE);
		setMagiaMassima(Costanti.TITANO_MAX_MAGIA);
		setValore(Costanti.TITANO_MAX_VALORE);
		setCoraggio(Costanti.TITANO_MAX_CORAGGIO);
		setCarisma(Costanti.TITANO_MAX_CARISMA);
		setQuantitaMassima(Costanti.TITANO_MAX_NUMERO);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tira(Costanti.TITANO_FORZA_MIN, Costanti.TITANO_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.TITANO_DESTREZZA_MIN, Costanti.TITANO_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.TITANO_COSTITUZIONE_MIN, Costanti.TITANO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.TITANO_INTELLIGENZA_MIN, Costanti.TITANO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.TITANO_SAGGEZZA_MIN, Costanti.TITANO_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.TITANO_FORTUNA_MIN, Costanti.TITANO_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.TITANO_CRITICO_MIN, Costanti.TITANO_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.TITANO_PRECISIONE_MIN, Costanti.TITANO_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.TITANO_VELOCITA_MIN, Costanti.TITANO_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.TITANO_PARATA_MIN, Costanti.TITANO_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.TITANO_RESISTENZA_MAGICA_MIN, Costanti.TITANO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.TITANO_MAGIA_MIN, Costanti.TITANO_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.TITANO_FURIA_MIN, Costanti.TITANO_FURIA_MAX));

	}

	@Override
	public ClassiOfferta[] getOfferteAmicizia() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_GRATUITO,
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
