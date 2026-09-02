package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

public class Maga extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Maga"; }
	public String getNomePlurale() { return "Maghe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoForza() { return Costanti.MAGA_RECUPERO_FORZA; }
	public int getRecuperoMagia() { return Costanti.MAGA_RECUPERO_MAGIA; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * Costanti.MAGA_MODIFICATORE_DANNI_MAGIA; }
	public int getBersagliPerIncantesimo() { return Costanti.MAGA_BERSAGLI_PER_INCANTESIMO_BONUS + super.getBersagliPerIncantesimo(); }

	public Maga() {
		super(ClassePersonaggio.MAGA);
	}

	public Maga(String nome) {
		super(nome, ClassePersonaggio.MAGA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Maga.gif");
		setIcona("icone/Maga.gif");
		setSaluteMassima(Costanti.MAGA_MAX_SALUTE);
		setMagiaMassima(Costanti.MAGA_MAX_MAGIA);
		setValore(Costanti.MAGA_MAX_VALORE);
		setCoraggio(Costanti.MAGA_MAX_CORAGGIO);
		setCarisma(Costanti.MAGA_MAX_CARISMA);
		setCorrompibile(true);
		setAmichevole(true);

		setForza(Dado.tira(Costanti.MAGA_FORZA_MIN, Costanti.MAGA_FORZA_MAX));
		setDestrezza(Dado.tira(Costanti.MAGA_DESTREZZA_MIN, Costanti.MAGA_DESTREZZA_MAX));
		setCostituzione(Dado.tira(Costanti.MAGA_COSTITUZIONE_MIN, Costanti.MAGA_COSTITUZIONE_MAX));
		setIntelligenza(Dado.tira(Costanti.MAGA_INTELLIGENZA_MIN, Costanti.MAGA_INTELLIGENZA_MAX));
		setSaggezza(Dado.tira(Costanti.MAGA_SAGGEZZA_MIN, Costanti.MAGA_SAGGEZZA_MAX));
		setFortuna(Dado.tira(Costanti.MAGA_FORTUNA_MIN, Costanti.MAGA_FORTUNA_MAX));
		setCritico(Dado.tira(Costanti.MAGA_CRITICO_MIN, Costanti.MAGA_CRITICO_MAX));
		setPrecisione(Dado.tira(Costanti.MAGA_PRECISIONE_MIN, Costanti.MAGA_PRECISIONE_MAX));
		setVelocita(Dado.tira(Costanti.MAGA_VELOCITA_MIN, Costanti.MAGA_VELOCITA_MAX));
		setParata(Dado.tira(Costanti.MAGA_PARATA_MIN, Costanti.MAGA_PARATA_MAX));
		setResistenzaMagica(Dado.tira(Costanti.MAGA_RESISTENZA_MAGICA_MIN, Costanti.MAGA_RESISTENZA_MAGICA_MAX));
		setMagia(Dado.tira(Costanti.MAGA_MAGIA_MIN, Costanti.MAGA_MAGIA_MAX));
		setFuria(Dado.tira(Costanti.MAGA_FURIA_MIN, Costanti.MAGA_FURIA_MAX));

	}
}
