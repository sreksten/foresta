package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.ui.UI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dieci locande sparse nella Foresta, quelle cittadine non contano. Tornare due volte
 * dallo stesso oste non conta nemmeno: le locande già visitate vengono annotate fra le
 * proprietà del modello dati, e ci restano anche dopo un salvataggio.
 */
public class NessunBoccaleLasciatoIndietro extends MissioneBase {

	private static final int LOCANDE_DA_VISITARE = 10;

	private static final String DESCRIZIONE_BASE = "Visita " + LOCANDE_DA_VISITARE + " locande nel mezzo della Foresta";
	private static final String LOCANDE_VISITATE = "LOCANDE_VISITATE";
	// Il valore di una proprietà non può contenere il PIPE, che separa le proprietà fra loro
	private static final String SEPARATORE = ",";

	public NessunBoccaleLasciatoIndietro() {
		super(ClasseMissione.NESSUN_BOCCALE_LASCIATO_INDIETRO);
	}

	@Override
	public String getNome() {
		return "Nessun boccale lasciato indietro";
	}

	@Override
	public String getDescrizione() {
		int visitate = getLocandeVisitate().size();
		if (isCompleta()) {
			return DESCRIZIONE_BASE + ". Tutte e dieci, e nessun ricordo particolarmente nitido.";
		}
		int mancanti = LOCANDE_DA_VISITARE - visitate;
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
		if (!isAttiva()) {
			UI.notifica("");
			UI.notifica("Da qualche parte fra le paludi e i castelli ci sono osti che non hanno ancora conosciuto la tua sete. " +
					DESCRIZIONE_BASE + ", e non lasciarne indietro nemmeno una.");
			attivaMissione();
		}
	}

	@Override
	public void controllaInLocazione() {
		// La bevuta si conta solo quando la locazione è conclusa
	}

	@Override
	public void controllaPostLocazione() {
		if (isCompleta()) {
			return;
		}
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		// Le locande cittadine sono dentro una CITTA e non contano; qui contano solo
		// quelle piantate in mezzo alla Foresta. Chi viene respinto sulla porta
		// dall'oste non ha bevuto niente.
		if (gruppo.getClasseLocazioneCorrente() != ClassiLocazione.LOCANDA || !Locanda.getIstanza().isEntrato()) {
			return;
		}
		String locanda = chiave(gruppo.getCoordinate());
		List<String> visitate = getLocandeVisitate();
		if (visitate.contains(locanda)) {
			// Tornare sui propri passi è comprensibile, ma non fa punteggio
			return;
		}
		visitate.add(locanda);
		aggiungiProprieta(LOCANDE_VISITATE, String.join(SEPARATORE, visitate));
		if (visitate.size() >= LOCANDE_DA_VISITARE) {
			completaMissione();
		} else {
			UI.notifica("Locanda numero " + visitate.size() + " debitamente censita.");
		}
	}

	@Override
	public void completaMissione() {
		super.completaMissione();
		UI.notifica("Dieci locande, dieci osti, un solo fegato. Nessun boccale è stato lasciato indietro, " +
				"e la Foresta ha un nuovo esperto di birre a cui nessuno ha chiesto un parere.");
	}

	private List<String> getLocandeVisitate() {
		String visitate = ottieniProprieta(LOCANDE_VISITATE);
		if (visitate == null || visitate.isEmpty()) {
			return new ArrayList<>();
		}
		return new ArrayList<>(Arrays.asList(visitate.split(SEPARATORE)));
	}

	private String chiave(CoordinateMD coordinate) {
		return coordinate.getX() + "x" + coordinate.getY();
	}
}
