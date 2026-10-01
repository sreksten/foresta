package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermezzo per i checkpoint {@link MomentoIntermezzo#INIZIO_LOCAZIONE} (locanda nel
 * bosco) e {@link MomentoIntermezzo#INGRESSO_LOCANDA_IN_CITTA} (locanda in città): scatta
 * alla prima visita del gruppo a una locanda non ancora visitata. Il gruppo entra in scena
 * camminando verso il centro (capo in testa), un bardo saluta con "C'era una volta...",
 * il locandiere lo zittisce e pronuncia il dialogo di benvenuto specifico di quella
 * locanda.
 * <p>
 * A differenza degli altri {@link Intermezzo}, che scattano una volta sola per l'intera
 * partita, questo deve scattare una volta per ogni locanda: {@link #getId()} include
 * quindi l'identificativo della locanda corrente, cosa possibile perché
 * {@link ClasseIntermezzo#getIstanza()} crea una nuova istanza a ogni controllo e
 * {@link com.threeamigos.foresta.motore.modellodati.IntermezziMD} è un semplice insieme
 * di stringhe senza vincoli di formato.
 */
public class IntermezzoLocandaPrimaVisita implements Intermezzo {

	private static final double X_PARTENZA_PERSONAGGI = -0.15;
	private static final double X_TARGET_BASE = 0.45;
	private static final double DISTANZA_FRA_PERSONAGGI = 0.08;
	private static final double RITARDO_FRA_PARTENZE = 1.0;
	private static final double SECONDI_CAMMINATA = 2.0;
	private static final double MARGINE_DOPO_CAMMINATA = 0.3;

	private static final double X_LOCANDIERE = 0.65;
	private static final double Y_LOCANDIERE = 0.6;
	private static final double X_BARDO = 0.60;
	private static final double Y_BARDO = 0.6;

	@Override
	public String getId() {
		String identificativo = getIdentificativoLocandaCorrente();
		return ClasseIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA.name()
				+ (identificativo == null ? "" : "_" + identificativo);
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		if (!momentoCoerenteConLocazioneCorrente(momento)) {
			return false;
		}
		LocazioneMD locazioneMD = getLocazioneMDLocandaCorrente();
		return locazioneMD != null && locazioneMD.ottieniProprieta(Locanda.LOCANDA_VISITATA) == null;
	}

	/**
	 * Il checkpoint di una locanda nel bosco (INIZIO_LOCAZIONE) scatta all'ingresso nella
	 * locazione stessa, ma lo stesso checkpoint scatterebbe anche entrando in una città
	 * (la cui locanda viene costruita subito, a prescindere da cosa si scelga in piazza):
	 * serve quindi distinguere i due casi, altrimenti l'intermezzo comparirebbe appena
	 * entrati in città, prima ancora di aver scelto "Locanda".
	 */
	private static boolean momentoCoerenteConLocazioneCorrente(MomentoIntermezzo momento) {
		ClassiLocazione classe = GruppoGiocatore.getIstanza().getClasseLocazioneCorrente();
		if (momento == MomentoIntermezzo.INIZIO_LOCAZIONE) {
			return classe == ClassiLocazione.LOCANDA;
		}
		if (momento == MomentoIntermezzo.INGRESSO_LOCANDA_IN_CITTA) {
			return classe.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA;
		}
		return false;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		LocazioneMD locazioneMD = getLocazioneMDLocandaCorrente();
		String dialogo = locazioneMD.ottieniProprieta(Locanda.LOCANDA_DIALOGO);

		List<Personaggio> personaggiVivi = GruppoGiocatore.getIstanza().getPersonaggiVivi();

		PaginaIntermezzo pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa("fondi/InternoLocanda.gif"))
				.conRitaglioSuSfondo()
				.conElemento(ElementoIntermezzo.di("locandiere", ImmagineIntermezzo.risorsa("personaggi/Locandiere.gif"), X_LOCANDIERE, Y_LOCANDIERE)
						.conBocca(0.5, -0.15))
				.conElemento(ElementoIntermezzo.personaggio("bardo", ClassePersonaggio.BARDO, X_BARDO, Y_BARDO)
						.conBocca(0.5, -0.15));

		for (int i = 0; i < personaggiVivi.size(); i++) {
			ClassePersonaggio classe = personaggiVivi.get(i).getClasse();
			double targetX = X_TARGET_BASE - i * DISTANZA_FRA_PERSONAGGI;
			pagina.conElemento(ElementoIntermezzo.personaggio("personaggio" + i, classe, X_PARTENZA_PERSONAGGI, Y_BARDO)
					.orientaNelVersoDelMoto(VersoDiDefault.di(classe))
					// Ritardo scaglionato: ogni personaggio parte un secondo dopo il precedente
					.attendi(i * RITARDO_FRA_PARTENZE)
					.poi(Tappa.inSecondi(SECONDI_CAMMINATA).verso(targetX, Y_BARDO)));
		}

		double tempoFineCammino = (personaggiVivi.size() - 1) * RITARDO_FRA_PARTENZE + SECONDI_CAMMINATA;
		pagina.conBattuta(BattutaIntermezzo.di("bardo", "C'era una volta...").daSecondo(tempoFineCammino + MARGINE_DOPO_CAMMINATA))
				.conBattuta(BattutaIntermezzo.di("locandiere", "Smettila con queste tue storielle!"))
				.conBattuta(BattutaIntermezzo.di("locandiere", dialogo));

		// Evita che Locanda.descrivi(), chiamato subito dopo con la locanda ormai
		// costruita, ripeta il dialogo o anticipi la recensione appena mostrati qui
		locazioneMD.aggiungiProprieta(Locanda.LOCANDA_DIALOGO_LETTO, LocazioneMD.AFFERMATIVO);
		locazioneMD.aggiungiProprieta(Locanda.LOCANDA_RECENSIONE_LETTA, LocazioneMD.AFFERMATIVO);

		List<PaginaIntermezzo> pagineIntermezzo = new ArrayList<>();
		pagineIntermezzo.add(pagina);
		return pagineIntermezzo;
	}

	private static LocazioneMD getLocazioneMDLocandaCorrente() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		ClassiLocazione classe = gruppo.getClasseLocazioneCorrente();
		if (classe != ClassiLocazione.LOCANDA && classe.getTipoLocazione() != ClassiLocazione.TipoLocazione.CITTA) {
			return null;
		}
		return Foresta.getLocazioneMD(gruppo.getCoordinate());
	}

	private static String getIdentificativoLocandaCorrente() {
		LocazioneMD locazioneMD = getLocazioneMDLocandaCorrente();
		return locazioneMD == null ? null : locazioneMD.ottieniProprieta(Locanda.LOCANDA_IDENTIFICATIVO);
	}
}
