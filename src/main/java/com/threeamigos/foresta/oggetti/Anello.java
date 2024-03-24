package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;
import com.threeamigos.foresta.ui.InterfacciaUtente;

public class Anello extends OggettoBase implements Oggetto {

	private static final int VALORE = 0;
	private static final int CORAGGIO = 1;
	private static final int CARISMA = 2;
	
	private int tipo;
	private boolean notificato;

	public Anello() {
		super();
		tipo = Random.getInt(3);
	}

	public String getAIS() {
		return Misc.UN;
	}

	public String getAIP() {
		return Misc.ALCUNI;
	}

	public String getADS() {
		return Misc.L_APOSTROFO;
	}

	public String getADP() {
		return Misc.GLI;
	}

	public String getNomeSingolare() {
		return "anello";
	}

	public String getNomePlurale() {
		return "anelli";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.ANELLO;
	}

	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		if (tipo <= CARISMA) {
			if (!notificato) {
				StringBuilder sb = new StringBuilder("Questo e' un anello magico, che fa aumentare ");
				if (tipo == 0)
					sb.append("il valore in combattimento");
				else if (tipo == 1)
					sb.append("il coraggio");
				else
					sb.append("il carisma");
				sb.append("!");
				UI.notifica(sb.toString());
				notificato = true;
			}
			if (gruppo.getNumeroPersonaggi() > 1 && azione == null) {
				UI.notifica("Chi lo vuole indossare?");
				return false;
			} else {
				if (azione == null || azione == Comando.TIMER) {
					return false;
				}
				Personaggio p = gruppo.getPersonaggio(azione);
				UI.notifica(p.getNome() + " indossa l'anello.");
				if (tipo == VALORE)
					p.addValore(5);
				else if (tipo == CORAGGIO)
					p.addCoraggio(5);
				else // CARISMA
					p.addCarisma(1);
				Statistiche.addPunti(50);
			}
		}
		UI.primoPiano(InterfacciaUtente.Finestra.STATO);
		return super.prendi(gruppo, azione);
	}
}
