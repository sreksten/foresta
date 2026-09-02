package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Strega extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Strega"; }
	public String getNomePlurale() { return "Streghe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return Costanti.STREGA_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.STREGA_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.STREGA_BERSAGLI_PER_INCANTESIMO; }

	public Strega() {
		super(ClassePersonaggio.STREGA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Strega.gif");
		setSaluteMassima(Costanti.STREGA_MAX_SALUTE);
		setMagiaMassima(Costanti.STREGA_MAX_MAGIA);
		setValore(Costanti.STREGA_MAX_VALORE);
		setCoraggio(Costanti.STREGA_MAX_CORAGGIO);
		setCarisma(Costanti.STREGA_MAX_CARISMA);

		setForza(Dado.tira(Costanti.STREGA_FORZA_MIN, Costanti.STREGA_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.STREGA_DESTREZZA_MIN, Costanti.STREGA_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.STREGA_COSTITUZIONE_MIN, Costanti.STREGA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.STREGA_INTELLIGENZA_MIN, Costanti.STREGA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.STREGA_SAGGEZZA_MIN, Costanti.STREGA_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.STREGA_FORTUNA_MIN, Costanti.STREGA_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.STREGA_CRITICO_MIN, Costanti.STREGA_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.STREGA_PRECISIONE_MIN, Costanti.STREGA_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.STREGA_VELOCITA_MIN, Costanti.STREGA_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.STREGA_PARATA_MIN, Costanti.STREGA_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.STREGA_RESISTENZA_MAGICA_MIN, Costanti.STREGA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.STREGA_MAGIA_MIN, Costanti.STREGA_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.STREGA_FURIA_MIN, Costanti.STREGA_FURIA_MAX));

	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.ACQUA ||
				classeIncantesimo == ClassiIncantesimo.ARIA ||
				classeIncantesimo == ClassiIncantesimo.FUOCO ||
				classeIncantesimo == ClassiIncantesimo.TERRA;
	}
}
