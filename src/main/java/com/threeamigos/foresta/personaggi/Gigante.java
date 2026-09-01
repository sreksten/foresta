package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Gigante extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Gigante"; }
	public String getNomePlurale() { return "Giganti"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Gigante() {
		super(ClassePersonaggio.GIGANTE);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Gigante.gif");
		setIcona("icone/Gigante.gif");
		setSaluteMassima(Costanti.GIGANTE_MAX_SALUTE);
		setMagiaMassima(Costanti.GIGANTE_MAX_MAGIA);
		setValore(Costanti.GIGANTE_MAX_VALORE);
		setCoraggio(Costanti.GIGANTE_MAX_CORAGGIO);
		setCarisma(Costanti.GIGANTE_MAX_CARISMA);
		setQuantitaMassima(Costanti.GIGANTE_MAX_NUMERO);
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
}
