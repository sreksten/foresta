package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Eremita extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Eremita"; }
	public String getNomePlurale() { return "Eremiti"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Eremita() {
		super(ClassePersonaggio.EREMITA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Eremita.gif");
		setIcona("icone/Eremita.gif");
		setSaluteMassima(Costanti.EREMITA_MAX_SALUTE);
		setMagiaMassima(Costanti.EREMITA_MAX_MAGIA);
		setValore(Costanti.EREMITA_MAX_VALORE);
		setCoraggio(Costanti.EREMITA_MAX_CORAGGIO);
		setCarisma(Costanti.EREMITA_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tira(Costanti.EREMITA_FORZA_MIN, Costanti.EREMITA_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.EREMITA_DESTREZZA_MIN, Costanti.EREMITA_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.EREMITA_COSTITUZIONE_MIN, Costanti.EREMITA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.EREMITA_INTELLIGENZA_MIN, Costanti.EREMITA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.EREMITA_SAGGEZZA_MIN, Costanti.EREMITA_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.EREMITA_FORTUNA_MIN, Costanti.EREMITA_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.EREMITA_CRITICO_MIN, Costanti.EREMITA_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.EREMITA_PRECISIONE_MIN, Costanti.EREMITA_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.EREMITA_VELOCITA_MIN, Costanti.EREMITA_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.EREMITA_PARATA_MIN, Costanti.EREMITA_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.EREMITA_RESISTENZA_MAGICA_MIN, Costanti.EREMITA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.EREMITA_MAGIA_MIN, Costanti.EREMITA_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.EREMITA_FURIA_MIN, Costanti.EREMITA_FURIA_MAX));

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
				ClassiOfferta.INFORMAZIONI,
		};
	}}
