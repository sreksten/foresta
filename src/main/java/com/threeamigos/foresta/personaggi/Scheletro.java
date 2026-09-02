package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Scheletro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNO; }
	public String getADS() { return Misc.LO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELLO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UNO; }

	public String getNomeSingolare() { return "Scheletro"; }
	public String getNomePlurale() { return "Scheletri"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Scheletro() {
		super(ClassePersonaggio.SCHELETRO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Scheletro.gif");
		setSaluteMassima(Costanti.SCHELETRO_MAX_SALUTE);
		setMagiaMassima(Costanti.SCHELETRO_MAX_MAGIA);
		setValore(Costanti.SCHELETRO_MAX_VALORE);
		setCoraggio(Costanti.SCHELETRO_MAX_CORAGGIO);
		setCarisma(Costanti.SCHELETRO_MAX_CARISMA);
		setQuantitaMassima(Costanti.SCHELETRO_MAX_NUMERO);

		setForza(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_FORZA_MIN, Costanti.SCHELETRO_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_DESTREZZA_MIN, Costanti.SCHELETRO_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_COSTITUZIONE_MIN, Costanti.SCHELETRO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_INTELLIGENZA_MIN, Costanti.SCHELETRO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_SAGGEZZA_MIN, Costanti.SCHELETRO_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_FORTUNA_MIN, Costanti.SCHELETRO_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_CRITICO_MIN, Costanti.SCHELETRO_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_PRECISIONE_MIN, Costanti.SCHELETRO_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_VELOCITA_MIN, Costanti.SCHELETRO_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_PARATA_MIN, Costanti.SCHELETRO_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_RESISTENZA_MAGICA_MIN, Costanti.SCHELETRO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_MAGIA_MIN, Costanti.SCHELETRO_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.SCHELETRO_FURIA_MIN, Costanti.SCHELETRO_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
