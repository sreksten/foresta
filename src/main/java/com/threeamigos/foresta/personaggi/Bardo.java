package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Bardo extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Bardo"; }
	public String getNomePlurale() { return "Bardi"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Bardo() {
		super(ClassePersonaggio.BARDO);
	}

	public Bardo(String nome) {
		super(nome, ClassePersonaggio.BARDO);
	}
	
	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Bardo.gif");
		setIcona("icone/Bardo.gif");
		setSaluteMassima(Costanti.BARDO_MAX_SALUTE);
		setMagiaMassima(Costanti.BARDO_MAX_MAGIA);
		setValore(Costanti.BARDO_MAX_VALORE);
		setCoraggio(Costanti.BARDO_MAX_CORAGGIO);
		setCarisma(Costanti.BARDO_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tiraAncheSenzaRange(Costanti.BARDO_FORZA_MIN, Costanti.BARDO_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.BARDO_DESTREZZA_MIN, Costanti.BARDO_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.BARDO_COSTITUZIONE_MIN, Costanti.BARDO_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.BARDO_INTELLIGENZA_MIN, Costanti.BARDO_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.BARDO_SAGGEZZA_MIN, Costanti.BARDO_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.BARDO_FORTUNA_MIN, Costanti.BARDO_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.BARDO_CRITICO_MIN, Costanti.BARDO_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.BARDO_PRECISIONE_MIN, Costanti.BARDO_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.BARDO_VELOCITA_MIN, Costanti.BARDO_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.BARDO_PARATA_MIN, Costanti.BARDO_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.BARDO_RESISTENZA_MAGICA_MIN, Costanti.BARDO_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.BARDO_MAGIA_MIN, Costanti.BARDO_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.BARDO_FURIA_MIN, Costanti.BARDO_FURIA_MAX));

	}
}
