package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Spirito extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNO; }
	public String getADS() { return Misc.LO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELLO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UNO; }

	public String getNomeSingolare() { return "Spirito"; }
	public String getNomePlurale() { return "Spiriti"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Spirito() {
		super(ClassePersonaggio.SPIRITO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Spirito.gif");
		setSaluteMassima(Costanti.SPIRITO_MAX_SALUTE);
		setMagiaMassima(Costanti.SPIRITO_MAX_MAGIA);
		setValore(Costanti.SPIRITO_MAX_VALORE);
		setCoraggio(Costanti.SPIRITO_MAX_CORAGGIO);
		setCarisma(Costanti.SPIRITO_MAX_CARISMA);
		setQuantitaMassima(Costanti.SPIRITO_MAX_NUMERO);
		setAmichevole(true);

		setForza(Dado.tira(Costanti.SPIRITO_FORZA_MIN, Costanti.SPIRITO_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.SPIRITO_DESTREZZA_MIN, Costanti.SPIRITO_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.SPIRITO_COSTITUZIONE_MIN, Costanti.SPIRITO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.SPIRITO_INTELLIGENZA_MIN, Costanti.SPIRITO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.SPIRITO_SAGGEZZA_MIN, Costanti.SPIRITO_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.SPIRITO_FORTUNA_MIN, Costanti.SPIRITO_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.SPIRITO_CRITICO_MIN, Costanti.SPIRITO_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.SPIRITO_PRECISIONE_MIN, Costanti.SPIRITO_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.SPIRITO_VELOCITA_MIN, Costanti.SPIRITO_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.SPIRITO_PARATA_MIN, Costanti.SPIRITO_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.SPIRITO_RESISTENZA_MAGICA_MIN, Costanti.SPIRITO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.SPIRITO_MAGIA_MIN, Costanti.SPIRITO_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.SPIRITO_FURIA_MIN, Costanti.SPIRITO_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
