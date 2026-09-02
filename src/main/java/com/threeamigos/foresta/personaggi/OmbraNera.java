package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class OmbraNera extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public String getNomeSingolare() { return "Ombra Nera"; }
	public String getNomePlurale() { return "Ombre Nere"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return Costanti.OMBRANERA_RECUPERO_MAGIA; }

	public OmbraNera() {
		super(ClassePersonaggio.OMBRA_NERA);
	}
	
	@Override
	protected void impostaValori() {
		setImmagine("personaggi/OmbraNera.gif");
		setSaluteMassima(Costanti.OMBRANERA_MAX_SALUTE);
		setMagiaMassima(Costanti.OMBRANERA_MAX_MAGIA);
		setValore(Costanti.OMBRANERA_MAX_VALORE);
		setCoraggio(Costanti.OMBRANERA_MAX_CORAGGIO);
		setCarisma(Costanti.OMBRANERA_MAX_CARISMA);
		setQuantitaMassima(Costanti.OMBRANERA_MAX_NUMERO);

		setForza(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_FORZA_MIN, Costanti.OMBRANERA_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_DESTREZZA_MIN, Costanti.OMBRANERA_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_COSTITUZIONE_MIN, Costanti.OMBRANERA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_INTELLIGENZA_MIN, Costanti.OMBRANERA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_SAGGEZZA_MIN, Costanti.OMBRANERA_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_FORTUNA_MIN, Costanti.OMBRANERA_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_CRITICO_MIN, Costanti.OMBRANERA_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_PRECISIONE_MIN, Costanti.OMBRANERA_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_VELOCITA_MIN, Costanti.OMBRANERA_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_PARATA_MIN, Costanti.OMBRANERA_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_RESISTENZA_MAGICA_MIN, Costanti.OMBRANERA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_MAGIA_MIN, Costanti.OMBRANERA_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.OMBRANERA_FURIA_MIN, Costanti.OMBRANERA_FURIA_MAX));

	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
