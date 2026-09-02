package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class MinotauroGigante extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public ClassePersonaggio getClasse() { return ClassePersonaggio.MINOTAURO_GIGANTE; }
	public String getNomeSingolare() { return "Minotauro Gigante"; }
	public String getNomePlurale() { return "Minotauri Giganti"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public MinotauroGigante() {
		super(ClassePersonaggio.MINOTAURO_GIGANTE);
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/MinotauroGigante.gif");
		setSaluteMassima(Costanti.MINOTAUROGIGANTE_MAX_SALUTE);
		setMagiaMassima(Costanti.MINOTAUROGIGANTE_MAX_MAGIA);
		setValore(Costanti.MINOTAUROGIGANTE_MAX_VALORE);
		setCoraggio(Costanti.MINOTAUROGIGANTE_MAX_CORAGGIO);
		setCarisma(Costanti.MINOTAUROGIGANTE_MAX_CARISMA);

		setForza(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_FORZA_MIN, Costanti.MINOTAUROGIGANTE_FORZA_MAX));
		setDestrezza(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_DESTREZZA_MIN, Costanti.MINOTAUROGIGANTE_DESTREZZA_MAX));
		setCostituzione(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_COSTITUZIONE_MIN, Costanti.MINOTAUROGIGANTE_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_INTELLIGENZA_MIN, Costanti.MINOTAUROGIGANTE_INTELLIGENZA_MAX));
		setSaggezza(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_SAGGEZZA_MIN, Costanti.MINOTAUROGIGANTE_SAGGEZZA_MAX));
		setFortuna(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_FORTUNA_MIN, Costanti.MINOTAUROGIGANTE_FORTUNA_MAX));
		setCritico(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_CRITICO_MIN, Costanti.MINOTAUROGIGANTE_CRITICO_MAX));
		setPrecisione(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_PRECISIONE_MIN, Costanti.MINOTAUROGIGANTE_PRECISIONE_MAX));
		setVelocita(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_VELOCITA_MIN, Costanti.MINOTAUROGIGANTE_VELOCITA_MAX));
		setParata(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_PARATA_MIN, Costanti.MINOTAUROGIGANTE_PARATA_MAX));
		setResistenzaMagica(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_RESISTENZA_MAGICA_MIN, Costanti.MINOTAUROGIGANTE_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_MAGIA_MIN, Costanti.MINOTAUROGIGANTE_MAGIA_MAX));
		setFuria(Dado.tiraAncheSenzaRange(Costanti.MINOTAUROGIGANTE_FURIA_MIN, Costanti.MINOTAUROGIGANTE_FURIA_MAX));

	}
}
