package com.threeamigos.foresta.missioni.principali;

import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.missioni.MissioneBase;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

/**
 * Dieci locande sparse nella Foresta, quelle cittadine non contano. Tornare due volte
 * dallo stesso oste non conta nemmeno: la bevuta viene annotata sulla casella della
 * locanda, che ha un modello dati suo e se la ricorda anche dopo un salvataggio.
 */
public class NessunBoccaleLasciatoIndietro extends MissioneBase {

	private static final int LOCANDE_DA_VISITARE = 10;

	private static String descrizioneBase() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("NESSUN_BOCCALE_LASCIATO_INDIETRO_DESCRIZIONE_BASE", "PARAM_NUMERO", Lingue.corrente().cardinale(LOCANDE_DA_VISITARE, Genere.MASCHILE));
	}
	// Quante tappe sono già state annunciate: le caselle sanno quali locande
	// sono state visitate, questo serve solo a non ripetere l'annuncio
	private static final String LOCANDE_CENSITE = "LOCANDE_CENSITE";

	public NessunBoccaleLasciatoIndietro() {
		super(MissionePredefinita.NESSUN_BOCCALE_LASCIATO_INDIETRO);
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("NESSUN_BOCCALE_LASCIATO_INDIETRO_TITOLO");
	}

	@Override
	public String getDescrizione() {
		if (isCompleta()) {
			return ProduttoreDiMissioniCasuale.testoDellaMissione("NESSUN_BOCCALE_LASCIATO_INDIETRO_DESCRIZIONE_INIZIO", "PARAM_DESCRIZIONE", descrizioneBase());
		}
		int mancanti = LOCANDE_DA_VISITARE - contaLocandeVisitate();
		if (mancanti == 1) {
			return ProduttoreDiMissioniCasuale.testoDellaMissione("NESSUN_BOCCALE_LASCIATO_INDIETRO_DESCRIZIONE_ULTIMA", "PARAM_DESCRIZIONE", descrizioneBase());
		}
		if (mancanti == LOCANDE_DA_VISITARE) {
			return descrizioneBase() + ".";
		} else {
			return ProduttoreDiMissioniCasuale.testoDellaMissione("NESSUN_BOCCALE_LASCIATO_INDIETRO_DESCRIZIONE_MANCANO", "PARAM_DESCRIZIONE", descrizioneBase(), "PARAM_MANCANTI", Lingue.corrente().cardinale(mancanti, Genere.MASCHILE));
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
		// La missione comincia quando si completa la prima locanda; quella locanda conta già
		if (!isCompleta() && !isAttiva()) {
			if (contaLocandeVisitate() == 0) {
				return;
			}
			BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("NESSUN_BOCCALE_LASCIATO_INDIETRO_INIZIO", "PARAM_DESCRIZIONE", descrizioneBase())));
			attivaMissione();
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
			BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("NESSUN_BOCCALE_LASCIATO_INDIETRO_FINE")));
		} else {
			BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("NESSUN_BOCCALE_LASCIATO_INDIETRO_CENSITA", "PARAM_NUMERO", Lingue.corrente().cardinale(visitate, Genere.MASCHILE))));
		}
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
				if (Foresta.getLocazione(coordinate) == TipoLocazione.LOCANDA &&
						Foresta.getLocazioneMD(coordinate).ottieniProprieta(Locande.LOCANDA_VISITATA) != null) {
					visitate++;
				}
			}
		}
		return visitate;
	}
}
