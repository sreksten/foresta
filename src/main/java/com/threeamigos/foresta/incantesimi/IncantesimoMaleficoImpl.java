package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.ui.UI;

import java.util.List;

/*
 * Incantesimi originali su ZX Spectrum:
 * Sonno
 * Lievitazione
 * Invisibilità
 */

public abstract class IncantesimoMaleficoImpl implements IncantesimoMalefico {

	public TipoIncantesimo getTipo() {
		return TipoIncantesimo.MALEFICO;
	}

	protected int totale;
	protected int bersagli;
	protected int feriti;
	protected int uccisi;

	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		if (getPortata() == PortataIncantesimo.GLOBALE) {
			formula(formulante);
		} else if (personaggioBersaglio != null) {
			formula(formulante, personaggioBersaglio);
		} else if (gruppoBersaglio != null) {
			formula(formulante, gruppoBersaglio);
		} else {
			throw new IllegalArgumentException("Non so come formulare questo incantesimo!");
		}
		if (!formulante.isPNG()) {
			UI.notifica(risultato(formulante));
		}
	}

	private void formula(Personaggio formulante, Gruppo gruppoBersaglio) {
		Logger.log("IncantesimoGenerico::formula(formulante,gruppo)");
		totale = gruppoBersaglio.getNumeroPersonaggi();
		List<Personaggio> personaggiVivi = gruppoBersaglio.getPersonaggiVivi(); 
		bersagli = formulante.getBersagli();
		if (bersagli > personaggiVivi.size()) {
			bersagli = personaggiVivi.size();
		}
		int danni = formulante.getModificaDanniMagia(getDanni());
		if (getPortata() != PortataIncantesimo.GRUPPO && bersagli > 1) {
			Logger.log("I danni vengono suddivisi tra i personaggi bersaglio");
			danni /= bersagli;
		}
		Logger.log("Bersagli base: " + formulante.getBersagli() + ", vivi: " + personaggiVivi.size() + " -> bersagli: " + bersagli + ", danni=" + danni);

		for (int i = 0; i < bersagli; i++) {
			Personaggio personaggioBersaglio = personaggiVivi.get(i);
			formulaImpl(formulante, personaggioBersaglio, danni, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
		}
		formulante.subMagia(getCostoLancio());
	}

	private void formula(Personaggio formulante, Personaggio personaggioBersaglio) {
		Logger.log("IncantesimoGenerico::formula(formulante,personaggio)");
		int danni = formulante.getModificaDanniMagia(getDanni());
		formulaImpl(formulante, personaggioBersaglio, danni, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
		formulante.subMagia(getCostoLancio());
	}
	
	private void formulaImpl(Personaggio formulante, Personaggio personaggioBersaglio, int danni, Personaggio.NotificaFerite notificaFerite, Personaggio.NotificaMorte notificaMorte) {
		if (!personaggioBersaglio.isImmuneAIncantesimo(getClasse())) {
			personaggioBersaglio.subSalute(danni, formulante, notificaFerite, notificaMorte);
			if (personaggioBersaglio.isVivo()) {
				feriti++;
			} else {
				uccisi++;
				if (GruppoGiocatore.getIstanza().contiene(formulante)) {
					Statistiche.addMostroUcciso(personaggioBersaglio.getClasse());
					Statistiche.addPunti(personaggioBersaglio.getSaluteMassima());
					GruppoGiocatore.getIstanza().addPuntiEsperienza(personaggioBersaglio.getSaluteMassima());
				}
			}
		} else {
			Logger.log("Mostro immune a incantesimo");
		}
		Logger.log("Feriti: " + feriti + ", uccisi: " + uccisi);
	}

	protected String risultato(Personaggio formulante) {
		Logger.log("Totale: " + totale + ", bersagli: " + bersagli + ", feriti: " + feriti + ", uccisi: " + uccisi);

		String s = formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);

		StringBuilder sb = new StringBuilder(s);
		if (uccisi == totale && totale > 1) {
			sb.append(" ha formulato l'incantesimo alla perfezione, eliminando tutti i suoi avversari.");
		} else {
			if (uccisi > 0) {
				sb.append(" ha eliminato ");
				if (uccisi == 1) {
					if (totale == 1) {
						sb.append("il suo avversario");
					} else {
						sb.append("un suo avversario");
					}
				} else {
					sb.append(Misc.getCardinaleM(uccisi)).append(" dei suoi avversari");
				}
				if (feriti > 0) {
					sb.append(" e ne ha ");
					if (feriti == 1) {
						sb.append("ferito uno");
					} else {
						sb.append("feriti ").append(Misc.getCardinaleM(feriti));
					}
				}
				sb.append('.');
			} else {
				sb.append(" ha ferito ");
				if (totale == 1) {
					sb.append("il suo avversario.");
				} else {
					if (feriti == totale) {
						sb.append(" tutti i suoi avversari.");
					} else {
						sb.append(Misc.getCardinaleM(feriti)).append(" dei suoi avversari.");
					}
				}
			}
		}
		return sb.toString();
	}

	protected void formula(Personaggio formulante) {
		Logger.log("Formula() implementazione base senza effetti!");
	}
}