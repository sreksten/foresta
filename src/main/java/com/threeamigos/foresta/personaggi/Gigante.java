package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
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

		setForza(Dado.tira(Costanti.GIGANTE_FORZA_MIN, Costanti.GIGANTE_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.GIGANTE_DESTREZZA_MIN, Costanti.GIGANTE_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.GIGANTE_COSTITUZIONE_MIN, Costanti.GIGANTE_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.GIGANTE_INTELLIGENZA_MIN, Costanti.GIGANTE_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.GIGANTE_SAGGEZZA_MIN, Costanti.GIGANTE_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.GIGANTE_FORTUNA_MIN, Costanti.GIGANTE_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.GIGANTE_CRITICO_MIN, Costanti.GIGANTE_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.GIGANTE_PRECISIONE_MIN, Costanti.GIGANTE_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.GIGANTE_VELOCITA_MIN, Costanti.GIGANTE_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.GIGANTE_PARATA_MIN, Costanti.GIGANTE_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.GIGANTE_RESISTENZA_MAGICA_MIN, Costanti.GIGANTE_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.GIGANTE_MAGIA_MIN, Costanti.GIGANTE_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.GIGANTE_FURIA_MIN, Costanti.GIGANTE_FURIA_MAX));

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
