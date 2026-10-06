package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
 * {@link TipoIntermezzo#getIstanza()} crea una nuova istanza a ogni controllo e
 * {@link com.threeamigos.foresta.modellodati.IntermezziMD} è un semplice insieme
 * di stringhe senza vincoli di formato.
 */
public class IntermezzoLocandaPrimaVisita implements Intermezzo {

	private static final String REGEX_PER_SPEZZARE_FRASI = "(?<=[.!?][\"“”])(?!\\s*,)(?=(?:[^\"“”]*[\"“”][^\"“”]*[\"“”])*[^\"“”]*$)"
			+ "|(?<=[.!?])(?![.!?])(?![\"“”])(?=(?:[^\"“”]*[\"“”][^\"“”]*[\"“”])*[^\"“”]*$)";

	private static final double X_PARTENZA_PERSONAGGI = -0.15;
	private static final double X_TARGET_BASE = 0.5;
	private static final double DISTANZA_FRA_PERSONAGGI = 0.05;
	private static final double RITARDO_FRA_PARTENZE = 0.6;
	private static final double SECONDI_CAMMINATA = 2.0;
	private static final double MARGINE_DOPO_CAMMINATA = 0.3;

	private static final double X_LOCANDIERE = 0.65;
	private static final double Y_LOCANDIERE = 0.6;
	private static final double X_BARDO = 0.60;
	private static final double Y_BARDO = 0.6;

	// Centrato come lo sfondo, di cui ha le stesse dimensioni
	private static final double X_FOREGROUND = 0.5;
	private static final double Y_FOREGROUND = 0.5;

	@Override
	public String getId() {
		String identificativo = Locande.getIdentificativoLocandaCorrente();
		return TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA.name()
				+ (identificativo == null ? "" : "_" + identificativo);
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		if (!Locande.momentoCoerenteConLocazioneCorrente(momento)) {
			return false;
		}
		LocazioneMD locazioneMD = Locande.getLocazioneMDLocandaCorrente();
		return locazioneMD != null && locazioneMD.ottieniProprieta(Locanda.LOCANDA_VISITATA) == null;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		LocazioneMD locazioneMD = Locande.getLocazioneMDLocandaCorrente();
		String dialogo = locazioneMD.ottieniProprieta(Locanda.LOCANDA_DIALOGO);

		List<Personaggio> personaggiVivi = GruppoGiocatore.getIstanza().getPersonaggiVivi();

		PaginaIntermezzo pagina = new PaginaIntermezzo()
				.conSfondo(ImmagineIntermezzo.risorsa("fondi/InternoLocanda.gif"))
				.conRitaglioSuSfondo()
				.conElemento(ElementoIntermezzo.di("locandiere", ImmagineIntermezzo.risorsa("personaggi/Locandiere.gif"), X_LOCANDIERE, Y_LOCANDIERE)
						.conBocca(0.5, -0.15))
				.conElemento(ElementoIntermezzo.personaggio("bardo", TipoPersonaggio.BARDO, X_BARDO, Y_BARDO)
						.conBocca(0.5, -0.15));

		for (int i = 0; i < personaggiVivi.size(); i++) {
			TipoPersonaggio classe = personaggiVivi.get(i).getClasse();
			double targetX = X_TARGET_BASE - i * DISTANZA_FRA_PERSONAGGI;
			pagina.conElemento(ElementoIntermezzo.personaggio("personaggio" + i, classe, X_PARTENZA_PERSONAGGI, Y_BARDO)
					.orientaNelVersoDelMoto(VersoDiDefault.di(classe))
					// Ritardo scaglionato: ogni personaggio parte un secondo dopo il precedente
					.attendi(i * RITARDO_FRA_PARTENZE)
					.poi(Tappa.inSecondi(SECONDI_CAMMINATA).verso(targetX, Y_BARDO)));
		}

		//double tempoFineCammino = (personaggiVivi.size() - 1) * RITARDO_FRA_PARTENZE + SECONDI_CAMMINATA;
		//pagina.conBattuta(BattutaIntermezzo.di("bardo", "C'era una volta...").daSecondo(tempoFineCammino + MARGINE_DOPO_CAMMINATA));

		String fiaba = ProduttoreDiTestiCasuale.fiaba().get(0);
		Collection<String> frasiFiaba = Arrays.stream(fiaba.split(REGEX_PER_SPEZZARE_FRASI))
				.map(String::trim)
				.collect(Collectors.toList());

		frasiFiaba.forEach(frase -> pagina.conBattuta(BattutaIntermezzo.di("bardo", frase)));

		String reazioneLocandiere = ProduttoreDiTestiCasuale.reazioneLocandiere();
		Collection<String> frasiReazioneLocandiere = Arrays.stream(reazioneLocandiere.split(REGEX_PER_SPEZZARE_FRASI))
				.map(String::trim)
				.collect(Collectors.toList());

		frasiReazioneLocandiere.forEach(frase -> pagina.conBattuta(BattutaIntermezzo.di("locandiere", frase)));

		Collection<String> frasiLocandiere = Arrays.stream(dialogo.split(REGEX_PER_SPEZZARE_FRASI))
				.map(String::trim)
				.collect(Collectors.toList());

		frasiLocandiere.forEach(frase -> pagina.conBattuta(BattutaIntermezzo.di("locandiere", frase)));

		//pagina.conBattuta(BattutaIntermezzo.di("locandiere", dialogo).conLarghezza(0.8).centrataOrizzontalmente());

		// Aggiunto per ultimo così resta sopra a tutto il resto della scena
		pagina.conElemento(ElementoIntermezzo.di("foreground", ImmagineIntermezzo.risorsa("fondi/ForegroundLocanda.gif"), X_FOREGROUND, Y_FOREGROUND));

		// Evita che Locanda.descrivi(), chiamato subito dopo con la locanda ormai
		// costruita, ripeta il dialogo o anticipi la recensione appena mostrati qui
		locazioneMD.aggiungiProprieta(Locanda.LOCANDA_DIALOGO_LETTO, LocazioneMD.AFFERMATIVO);
		locazioneMD.aggiungiProprieta(Locanda.LOCANDA_RECENSIONE_LETTA, LocazioneMD.AFFERMATIVO);

		List<PaginaIntermezzo> pagineIntermezzo = new ArrayList<>();
		pagineIntermezzo.add(pagina);
		return pagineIntermezzo;
	}
}
