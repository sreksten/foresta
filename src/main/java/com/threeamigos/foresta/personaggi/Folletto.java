package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
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

		setForza(Dado.tira(Costanti.FOLLETTO_FORZA_MIN, Costanti.FOLLETTO_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.FOLLETTO_DESTREZZA_MIN, Costanti.FOLLETTO_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.FOLLETTO_COSTITUZIONE_MIN, Costanti.FOLLETTO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.FOLLETTO_INTELLIGENZA_MIN, Costanti.FOLLETTO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.FOLLETTO_SAGGEZZA_MIN, Costanti.FOLLETTO_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.FOLLETTO_FORTUNA_MIN, Costanti.FOLLETTO_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.FOLLETTO_CRITICO_MIN, Costanti.FOLLETTO_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.FOLLETTO_PRECISIONE_MIN, Costanti.FOLLETTO_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.FOLLETTO_VELOCITA_MIN, Costanti.FOLLETTO_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.FOLLETTO_PARATA_MIN, Costanti.FOLLETTO_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.FOLLETTO_RESISTENZA_MAGICA_MIN, Costanti.FOLLETTO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.FOLLETTO_MAGIA_MIN, Costanti.FOLLETTO_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.FOLLETTO_FURIA_MIN, Costanti.FOLLETTO_FURIA_MAX));

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
