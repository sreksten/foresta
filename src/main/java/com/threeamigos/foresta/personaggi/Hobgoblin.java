package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

public class Hobgoblin extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Hobgoblin"; }
	public String getNomePlurale() { return "Hobgoblin"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoForza() { return Costanti.HOBGOBLIN_RECUPERO_FORZA; }

	public Hobgoblin() {
		super(ClassePersonaggio.HOBGOBLIN);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Hobgoblin.gif");
		setIcona("icone/Hobgoblin.gif");
		setSaluteMassima(Costanti.HOBGOBLIN_MAX_SALUTE);
		setMagiaMassima(Costanti.HOBGOBLIN_MAX_MAGIA);
		setValore(Costanti.HOBGOBLIN_MAX_VALORE);
		setCoraggio(Costanti.HOBGOBLIN_MAX_CORAGGIO);
		setCarisma(Costanti.HOBGOBLIN_MAX_CARISMA);
		setQuantitaMassima(Costanti.HOBGOBLIN_MAX_NUMERO);
		setCorrompibile(true);

		setForza(Dado.tira(Costanti.HOBGOBLIN_FORZA_MIN, Costanti.HOBGOBLIN_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.HOBGOBLIN_DESTREZZA_MIN, Costanti.HOBGOBLIN_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.HOBGOBLIN_COSTITUZIONE_MIN, Costanti.HOBGOBLIN_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.HOBGOBLIN_INTELLIGENZA_MIN, Costanti.HOBGOBLIN_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.HOBGOBLIN_SAGGEZZA_MIN, Costanti.HOBGOBLIN_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.HOBGOBLIN_FORTUNA_MIN, Costanti.HOBGOBLIN_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.HOBGOBLIN_CRITICO_MIN, Costanti.HOBGOBLIN_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.HOBGOBLIN_PRECISIONE_MIN, Costanti.HOBGOBLIN_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.HOBGOBLIN_VELOCITA_MIN, Costanti.HOBGOBLIN_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.HOBGOBLIN_PARATA_MIN, Costanti.HOBGOBLIN_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.HOBGOBLIN_RESISTENZA_MAGICA_MIN, Costanti.HOBGOBLIN_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.HOBGOBLIN_MAGIA_MIN, Costanti.HOBGOBLIN_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.HOBGOBLIN_FURIA_MIN, Costanti.HOBGOBLIN_FURIA_MAX));

	}

	@Override
	public ClassiOfferta[] getOfferteCorruzione() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_MERCENARIO,
				ClassiOfferta.INCANTESIMI,
				ClassiOfferta.MAPPA_FORESTA,
		};
	}
}
