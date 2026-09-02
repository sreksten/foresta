package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Chimera extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Chimera"; }
	public String getNomePlurale() { return "Chimere"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Chimera() {
		super(ClassePersonaggio.CHIMERA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Chimera.gif");
		setSaluteMassima(Costanti.CHIMERA_MAX_SALUTE);
		setMagiaMassima(Costanti.CHIMERA_MAX_MAGIA);
		setValore(Costanti.CHIMERA_MAX_VALORE);
		setCoraggio(Costanti.CHIMERA_MAX_CORAGGIO);
		setCarisma(Costanti.CHIMERA_MAX_CARISMA);
		setQuantitaMassima(Costanti.CHIMERA_MAX_NUMERO);

		setForza(Dado.tira(Costanti.CHIMERA_FORZA_MIN, Costanti.CHIMERA_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.CHIMERA_DESTREZZA_MIN, Costanti.CHIMERA_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.CHIMERA_COSTITUZIONE_MIN, Costanti.CHIMERA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.CHIMERA_INTELLIGENZA_MIN, Costanti.CHIMERA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.CHIMERA_SAGGEZZA_MIN, Costanti.CHIMERA_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.CHIMERA_FORTUNA_MIN, Costanti.CHIMERA_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.CHIMERA_CRITICO_MIN, Costanti.CHIMERA_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.CHIMERA_PRECISIONE_MIN, Costanti.CHIMERA_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.CHIMERA_VELOCITA_MIN, Costanti.CHIMERA_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.CHIMERA_PARATA_MIN, Costanti.CHIMERA_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.CHIMERA_RESISTENZA_MAGICA_MIN, Costanti.CHIMERA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.CHIMERA_MAGIA_MIN, Costanti.CHIMERA_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.CHIMERA_FURIA_MIN, Costanti.CHIMERA_FURIA_MAX));

	}
}
