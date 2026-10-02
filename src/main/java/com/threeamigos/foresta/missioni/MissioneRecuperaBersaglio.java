package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;

/**
 * Una missione di recupero a passi (vedi gestione_missioni.md, §5): in una città un mandante chiede di recuperare
 * qualcosa da un covo, il gruppo lo recupera e lo riporta in città per la ricompensa.
 * <ol>
 * <li>INCARICO, a inizio locazione, nella città: l'intermezzo in cui il mandante chiede aiuto;</li>
 * <li>ACCETTAZIONE, in locazione, nella città: la missione si attiva e compare il covo. È un passo a parte perché
 * l'avviso di nuova missione arrivi dopo l'intermezzo e non prima;</li>
 * <li>RECUPERO, a fine locazione, nel covo completato;</li>
 * <li>RITORNO, a inizio locazione, di nuovo nella città: l'intermezzo del ringraziamento;</li>
 * <li>RICOMPENSA, in locazione, nella città: le monete, e la missione si completa, anche qui dopo l'intermezzo.</li>
 * </ol>
 * Se la città viene distrutta prima della consegna, la missione fallisce.
 */
public abstract class MissioneRecuperaBersaglio extends MissioneAPassi {

	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String RECUPERO = "RECUPERO";
	private static final String RITORNO = "RITORNO";
	private static final String RICOMPENSA = "RICOMPENSA";

	protected static final int AMMONTARE_RICOMPENSA = 20;

	protected MissioneRecuperaBersaglio(ClasseMissione classe) {
		super(classe);
	}

	/**
	 * La città del mandante, dove si prende l'incarico e si torna per la ricompensa.
	 */
	protected abstract ClassiLocazione getCitta();

	/**
	 * La locazione unica in cui si trova ciò che va recuperato.
	 */
	protected abstract ClassiLocazione getCovo();

	protected abstract ScenaInCitta scenaIncarico();

	protected abstract ScenaInCitta scenaRingraziamento();

	/**
	 * Il riassunto dell'incarico appena accettato, per il riquadro del testo.
	 */
	protected abstract String testoAccettazione(GruppoGiocatore gruppo);

	protected abstract String testoRecupero(GruppoGiocatore gruppo);

	protected abstract String testoRicompensa(GruppoGiocatore gruppo);

	protected abstract String testoCittaDistrutta();

	/**
	 * Se il bersaglio è già stato recuperato, cioè la missione è al ritorno in città o oltre.
	 */
	protected final boolean isBersaglioRecuperato() {
		String passo = getPassoCorrente();
		return RITORNO.equals(passo) || RICOMPENSA.equals(passo) || Passo.FINE.equals(passo);
	}

	@Override
	public void controllaPreLocazione() {
		// Se la città della consegna è stata distrutta, la missione non si può più concludere
		if (isAttiva() && !isCompleta() && !isFallita() && LineaTemporale.isCittaDistrutta(getCitta())) {
			BusEventi.pubblica(new NotificaTestoParagrafo(testoCittaDistrutta()));
			fallisciMissione();
		}
		super.controllaPreLocazione();
	}

	@Override
	protected String passoIniziale() {
		return INCARICO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		switch (id) {
			case INCARICO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nellaCitta)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaIncarico().getPagine())
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaCitta)
						.esegui(() -> {
							BusEventi.pubblica(new NotificaTestoParagrafo(testoAccettazione(gruppo)));
							attivaMissione();
							Foresta.costruisciLocazioneUnica(getCovo(), true);
						})
						.poi(RECUPERO);
			case RECUPERO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE,
								() -> gruppo.isInLocazioneUnica(getCovo()) && gruppo.getLocazioneCorrente().isCompleta())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testoRecupero(gruppo))))
						.poi(RITORNO);
			case RITORNO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nellaCitta)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaRingraziamento().getPagine())
						.poi(RICOMPENSA);
			case RICOMPENSA:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaCitta)
						.esegui(() -> {
							gruppo.addMonete(AMMONTARE_RICOMPENSA);
							BusEventi.pubblica(new NotificaTestoParagrafo(testoRicompensa(gruppo)));
						})
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	private boolean nellaCitta() {
		return GruppoGiocatore.getIstanza().isInLocazioneUnica(getCitta()) && !LineaTemporale.isCittaDistrutta(getCitta());
	}
}
