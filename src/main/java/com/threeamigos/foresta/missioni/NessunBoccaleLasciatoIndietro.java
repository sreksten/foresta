package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.ui.UI;

/**
 * Dieci locande sparse nella Foresta, quelle cittadine non contano. Tornare due volte
 * dallo stesso oste non conta nemmeno: la bevuta viene annotata sulla casella della
 * locanda, che ha un modello dati suo e se la ricorda anche dopo un salvataggio.
 */
public class NessunBoccaleLasciatoIndietro extends MissioneBase {

	private static final int LOCANDE_DA_VISITARE = 10;

	private static final String DESCRIZIONE_BASE = "Visita " + LOCANDE_DA_VISITARE + " locande nel mezzo della Foresta";
	// Quante tappe sono già state annunciate: le caselle sanno quali locande
	// sono state visitate, questo serve solo a non ripetere l'annuncio
	private static final String LOCANDE_CENSITE = "LOCANDE_CENSITE";

	public NessunBoccaleLasciatoIndietro() {
		super(ClasseMissione.NESSUN_BOCCALE_LASCIATO_INDIETRO);
	}

	@Override
	public String getNome() {
		return "Nessun boccale lasciato indietro";
	}

	@Override
	public String getDescrizione() {
		if (isCompleta()) {
			return DESCRIZIONE_BASE + ". Tutte e dieci, e nessun ricordo particolarmente nitido.";
		}
		int mancanti = LOCANDE_DA_VISITARE - contaLocandeVisitate();
		if (mancanti == 1) {
			return DESCRIZIONE_BASE + ". Ne manca una, e il fegato lo sa.";
		}
		if (mancanti == LOCANDE_DA_VISITARE) {
			return DESCRIZIONE_BASE + ".";
		} else {
			return DESCRIZIONE_BASE + ". Ne mancano " + mancanti + '.';
		}
	}

	@Override
	public void controllaPreLocazione() {
	}

	@Override
	public void controllaInLocazione() {
		// La bevuta si conta solo quando la locazione è conclusa
	}

	@Override
	public void controllaPostLocazione() {
		// Il giocatore inizia sempre nel bosco, non può essere in una locanda alla fine del primo turno
		if (!isCompleta() && !isAttiva()) {
			UI.notifica("");
			UI.notifica("Da qualche parte fra le paludi e i castelli ci sono osti che non hanno ancora conosciuto la tua sete. " +
					DESCRIZIONE_BASE + ", e non lasciarne indietro nemmeno una.");
			attivaMissione();
			return;
		}
		if (isCompleta()) {
			return;
		}
		int visitate = contaLocandeVisitate();
		if (visitate == locandeCensite()) {
			// Tornare sui propri passi è comprensibile, ma non fa punteggio
			return;
		}
		aggiungiProprieta(LOCANDE_CENSITE, String.valueOf(visitate));
		if (visitate >= LOCANDE_DA_VISITARE) {
			completaMissione();
		} else {
			UI.notifica("");
			UI.notifica("Locanda numero " + visitate + " debitamente censita.");
		}
	}

	@Override
	public void completaMissione() {
		super.completaMissione();
		UI.notifica("Dieci locande, dieci osti, un solo fegato. Nessun boccale è stato lasciato indietro, " +
				"e la Foresta ha un nuovo esperto di birre a cui nessuno ha chiesto un parere.");
	}

	/**
	 * Le locande cittadine sono dentro una CITTA e non contano: qui contano solo
	 * quelle piantate in mezzo alla Foresta.
	 */
	private int locandeCensite() {
		String censite = ottieniProprieta(LOCANDE_CENSITE);
		return censite == null ? 0 : Integer.parseInt(censite);
	}

	private int contaLocandeVisitate() {
		int visitate = 0;
		for (int y = 0; y < Foresta.getDimensioneY(); y++) {
			for (int x = 0; x < Foresta.getDimensioneX(); x++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				if (Foresta.getLocazione(coordinate) == ClassiLocazione.LOCANDA &&
						Foresta.getLocazioneMD(coordinate).ottieniProprieta(Locanda.LOCANDA_VISITATA) != null) {
					visitate++;
				}
			}
		}
		return visitate;
	}
}
