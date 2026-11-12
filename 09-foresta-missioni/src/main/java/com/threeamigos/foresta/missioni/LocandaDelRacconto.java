package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.IntermezzoLocandaPrimaVisita;
import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Le locande nel bosco in cui qualcuno racconta una storia al gruppo:
 * <ul>
 * <li>Non sparate sul pianista parte alla <b>terza locanda</b>: nella prima locanda nel bosco in cui il gruppo entra dopo essere già entrato due volte
 * in una locanda, nel bosco o in città, in tutta la partita (contano tutte le visite, anche alla stessa). Se la locanda è nuova, la sua scena prende il
 * posto del benvenuto, che slitta alla visita dopo; in quella visita non scatta nessun'altra scena (vedi NonSparateSulPianista.bloccaIntermezzo);</li>
 * <li>la leggenda del locandiere e le missioni di recupero del sacerdote si offrono <b>dalla seconda visita</b> a una locanda nel bosco in poi
 * ({@link #isQui()}), dopo il benvenuto, al posto della recensione, che è un intermezzo di ripiego (IntermezzoLocandaVisitaSuccessiva) e quindi aspetta da sé.</li>
 * </ul>
 * Tutte e tre stanno nel sorteggio delle missioni (SceltaDegliIncarichi): ne parte una per visita, ma il pianista, quando è pronto, ha la precedenza.
 */
public final class LocandaDelRacconto {

	/**
	 * Le visite precedenti a questa locanda (si contano entrando, dopo i controlli delle missioni): dalla seconda visita.
	 */
	public static final int VISITE_PRECEDENTI = 1;

	/**
	 * Le visite a una locanda qualsiasi, in tutta la partita, prima di quella del pianista: parte alla terza.
	 */
	public static final int VISITE_PRECEDENTI_DEL_PIANISTA = 2;

	private LocandaDelRacconto() {
	}

	/**
	 * Se il gruppo è in una locanda nel bosco in cui è già entrato almeno una volta, e ne ha già visto il benvenuto: lì si offrono la leggenda del
	 * locandiere e le missioni del sacerdote. Se il benvenuto è slittato (alla prima visita c'era il pianista) scatta lui, e il racconto aspetta la
	 * visita dopo: una scena per visita.
	 */
	public static boolean isQui() {
		if (Foresta.getLocazioneDelGruppo() != TipoLocazione.LOCANDA) {
			return false;
		}
		String visite = Foresta.getLocazioneMD(Foresta.getPosizioneDelGruppo()).ottieniProprieta(Locande.LOCANDA_VISITE);
		return visite != null && Integer.parseInt(visite) >= VISITE_PRECEDENTI && !isBenvenutoRimandato();
	}

	/**
	 * Se nella locanda in cui si trova il gruppo il benvenuto è stato rimandato (alla prima visita c'era il pianista) e non è ancora scattato.
	 */
	private static boolean isBenvenutoRimandato() {
		return Foresta.getLocazioneMD(Foresta.getPosizioneDelGruppo()).ottieniProprieta(Locande.LOCANDA_BENVENUTO_RIMANDATO) != null
				&& !RegistroIntermezzi.isScattato(new IntermezzoLocandaPrimaVisita().getId());
	}

	/**
	 * Se il gruppo è in una locanda nel bosco, nuova o no, ed era già entrato almeno due volte in una locanda: lì il locandiere affida il bardo al
	 * gruppo.
	 */
	public static boolean isTerzaLocanda() {
		return Foresta.getLocazioneDelGruppo() == TipoLocazione.LOCANDA && visiteATutteLeLocande() >= VISITE_PRECEDENTI_DEL_PIANISTA;
	}

	/**
	 * Quante volte il gruppo è entrato in una locanda, nel bosco o in città, in tutta la partita.
	 */
	public static int visiteATutteLeLocande() {
		int visite = 0;
		for (int y = 0; y < Foresta.getDimensioneY(); y++) {
			for (int x = 0; x < Foresta.getDimensioneX(); x++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				TipoLocazione tipo = Foresta.getLocazione(coordinate);
				if (tipo == TipoLocazione.LOCANDA || tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
					String visiteQui = Foresta.getLocazioneMD(coordinate).ottieniProprieta(Locande.LOCANDA_VISITE);
					visite += visiteQui == null ? 0 : Integer.parseInt(visiteQui);
				}
			}
		}
		return visite;
	}
}
