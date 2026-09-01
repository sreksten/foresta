package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
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
	}
}
