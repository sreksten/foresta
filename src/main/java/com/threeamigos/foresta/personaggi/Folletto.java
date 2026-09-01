package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Folletto extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Folletto"; }
	public String getNomePlurale() { return "Folletti"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Folletto() {
		super(ClassePersonaggio.FOLLETTO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Folletto.gif");
		setSaluteMassima(Costanti.FOLLETTO_MAX_SALUTE);
		setMagiaMassima(Costanti.FOLLETTO_MAX_MAGIA);
		setValore(Costanti.FOLLETTO_MAX_VALORE);
		setCoraggio(Costanti.FOLLETTO_MAX_CORAGGIO);
		setCarisma(Costanti.FOLLETTO_MAX_CARISMA);
		setQuantitaMassima(Costanti.FOLLETTO_MAX_NUMERO);
		setCorrompibile(true);
		setAmichevole(true);
	}

	@Override
	public ClassiOfferta[] getOfferteAmicizia() {
		return new ClassiOfferta[] {
				ClassiOfferta.INCANTESIMI,
				ClassiOfferta.INFORMAZIONI,
				ClassiOfferta.PASTO
		};
	}
}
