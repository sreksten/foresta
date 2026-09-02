package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Arpia extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public String getNomeSingolare() { return "Arpia"; }
	public String getNomePlurale() { return "Arpie"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Arpia() {
		super(ClassePersonaggio.ARPIA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Arpia.gif");
		setSaluteMassima(Costanti.ARPIA_MAX_SALUTE);
		setMagiaMassima(Costanti.ARPIA_MAX_MAGIA);
		setValore(Costanti.ARPIA_MAX_VALORE);
		setCoraggio(Costanti.ARPIA_MAX_CORAGGIO);
		setCarisma(Costanti.ARPIA_MAX_CARISMA);
		setQuantitaMassima(Costanti.ARPIA_MAX_NUMERO);

		setForza(Dado.tira(Costanti.ARPIA_FORZA_MIN, Costanti.ARPIA_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.ARPIA_DESTREZZA_MIN, Costanti.ARPIA_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.ARPIA_COSTITUZIONE_MIN, Costanti.ARPIA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.ARPIA_INTELLIGENZA_MIN, Costanti.ARPIA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.ARPIA_SAGGEZZA_MIN, Costanti.ARPIA_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.ARPIA_FORTUNA_MIN, Costanti.ARPIA_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.ARPIA_CRITICO_MIN, Costanti.ARPIA_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.ARPIA_PRECISIONE_MIN, Costanti.ARPIA_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.ARPIA_VELOCITA_MIN, Costanti.ARPIA_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.ARPIA_PARATA_MIN, Costanti.ARPIA_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.ARPIA_RESISTENZA_MAGICA_MIN, Costanti.ARPIA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.ARPIA_MAGIA_MIN, Costanti.ARPIA_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.ARPIA_FURIA_MIN, Costanti.ARPIA_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.ARIA;
	}
}
