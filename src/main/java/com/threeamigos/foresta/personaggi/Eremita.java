package com.threeamigos.foresta.personaggi;

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
		super(ClassiPersonaggio.EREMITA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Eremita.gif");
		setIcona("icone/Eremita.gif");
		setForzaMassima(70);
		setMagiaMassima(0);
		setValore(50);
		setCoraggio(80);
		setCarisma(1);
		setCorrompibile(true);
		setAmichevole(true);
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
