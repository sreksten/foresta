package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

public class Anello extends OggettoBase implements Oggetto {

	private final boolean anelloMagico;
	private final int tipo;
	private boolean notificato;

	public Anello() {
		super();
		int tiroDado = Dado.tira(6);
		Logger.log("Tiro del dado per anello magico: " + tiroDado);
		if (tiroDado == 6) {
			anelloMagico = true;
			tipo = Dado.tira(3);
			Logger.log("Anello magico di tipo " + tipo);
		} else {
			anelloMagico = false;
			tipo = -1;
		}
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
		if (anelloMagico) {
			if (!notificato) {
				StringBuilder sb = new StringBuilder("Questo è un anello magico, che fa aumentare ");
				if (tipo == 1)
					sb.append("il valore in combattimento");
				else if (tipo == 2)
					sb.append("il coraggio");
				else
					sb.append("il carisma");
				sb.append("!");
				UI.notifica(sb.toString());
				notificato = true;
			}
			if (gruppo.getNumeroPersonaggiVivi() > 1 && azione == null) {
				UI.notifica("Chi lo vuole indossare?");
				return false;
			} else {
				if (azione == null || azione == Comando.TIMER) {
					return false;
				}
				Personaggio p = gruppo.getPersonaggio(azione);
				UI.notifica(p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " indossa l'anello.");
				if (tipo == 1)
					p.addValore(Costanti.ANELLO_MAGICO_AGGIUNTA_VALORE);
				else if (tipo == 2)
					p.addCoraggio(Costanti.ANELLO_MAGICO_AGGIUNTA_CORAGGIO);
				else // CARISMA
					p.addCarisma(Costanti.ANELLO_MAGICO_AGGIUNTA_CARISMA);
				Statistiche.addPunti(Costanti.ANELLO_MAGICO_PUNTEGGIO);
			}
		}
		UI.primoPiano(InterfacciaUtente.Finestra.STATO);
		return super.prendi(gruppo, azione);
	}
}
