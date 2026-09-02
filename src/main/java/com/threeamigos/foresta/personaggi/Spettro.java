package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Spettro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNO; }
	public String getADS() { return Misc.LO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELLO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UNO; }

	public String getNomeSingolare() { return "Spettro"; }
	public String getNomePlurale() { return "Spettri"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Spettro() {
		super(ClassePersonaggio.SPETTRO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Spettro.gif");
		setSaluteMassima(Costanti.SPETTRO_MAX_SALUTE);
		setMagiaMassima(Costanti.SPETTRO_MAX_MAGIA);
		setValore(Costanti.SPETTRO_MAX_VALORE);
		setCoraggio(Costanti.SPETTRO_MAX_CORAGGIO);
		setCarisma(Costanti.SPETTRO_MAX_CARISMA);
		setQuantitaMassima(Costanti.SPETTRO_MAX_NUMERO);

		setForza(Dado.tira(Costanti.SPETTRO_FORZA_MIN, Costanti.SPETTRO_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.SPETTRO_DESTREZZA_MIN, Costanti.SPETTRO_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.SPETTRO_COSTITUZIONE_MIN, Costanti.SPETTRO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.SPETTRO_INTELLIGENZA_MIN, Costanti.SPETTRO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.SPETTRO_SAGGEZZA_MIN, Costanti.SPETTRO_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.SPETTRO_FORTUNA_MIN, Costanti.SPETTRO_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.SPETTRO_CRITICO_MIN, Costanti.SPETTRO_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.SPETTRO_PRECISIONE_MIN, Costanti.SPETTRO_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.SPETTRO_VELOCITA_MIN, Costanti.SPETTRO_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.SPETTRO_PARATA_MIN, Costanti.SPETTRO_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.SPETTRO_RESISTENZA_MAGICA_MIN, Costanti.SPETTRO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.SPETTRO_MAGIA_MIN, Costanti.SPETTRO_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.SPETTRO_FURIA_MIN, Costanti.SPETTRO_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
