package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Un incarico preso in una città: un mandante chiede un servizio, il gruppo lo fa e torna in quella città a
 * riscuotere.
 * <ol>
 * <li>INCARICO, a inizio locazione, in una città: la missione se la ricorda e parte l'intermezzo del mandante;</li>
 * <li>ACCETTAZIONE, in locazione, nella città: la missione si attiva, dopo l'intermezzo;</li>
 * <li>i passi del compito, dal {@link #primoPassoDelCompito()}: l'ultimo va a {@link #RITORNO};</li>
 * <li>RITORNO, a inizio locazione, di nuovo nella città: l'intermezzo del ringraziamento;</li>
 * <li>CONSEGNA, in locazione, nella città, se il compito era procurarsi degli oggetti
 * ({@link #getOggettiDaConsegnare()}): il gruppo li consegna;</li>
 * <li>RICOMPENSA, in locazione, nella città: le monete, e la missione si completa.</li>
 * </ol>
 * Si riscuote in un'altra città se l'incarico è portare qualcosa a qualcuno ({@link #getCittaDelRitorno()}): allora
 * RITORNO, CONSEGNA e RICOMPENSA sono là.
 * Se la città in cui si riscuote viene distrutta dopo l'accettazione, la missione fallisce.
 * <p>
 * Un incarico può avere una città fissa ({@link #getCittaFissa()}): è la storia di quella città (il medaglione di
 * Fleena, le derrate di Ruuna, vedi IncaricoDiCombattimentoBase), e parte alla prima visita. Gli altri si prendono in
 * una città qualsiasi, ma solo a una visita tranquilla, in cui nessun'altra missione mostra un intermezzo entrando in
 * città: non alla prima visita, quando parte la missione della città, né quando si torna a concluderne una; fra due
 * incarichi pronti nella stessa visita parte il primo controllato, l'altro aspetta.
 * <p>
 * Gli incarichi senza città fissa sono ripetibili (vedi MissioneAPassi.isRipetibile): finiti, bene o male, ne
 * lasciano uno nuovo uguale, che si può prendere dopo una pausa.
 */
public abstract class IncaricoInCitta extends MissioneAPassi {

	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	protected static final String RITORNO = "RITORNO";
	private static final String CONSEGNA = "CONSEGNA";
	private static final String RICOMPENSA = "RICOMPENSA";
	private static final String CITTA = "CITTA";
	private static final String PREFISSO_CITTA = "CITTA_";

	protected IncaricoInCitta(ClasseMissione classe) {
		super(classe);
	}

	protected abstract ScenaInCitta scenaIncarico();

	protected abstract ScenaInCitta scenaRingraziamento();

	protected abstract String testoAccettazione();

	protected abstract String testoRicompensa();

	protected abstract int getRicompensa();

	protected abstract String primoPassoDelCompito();

	protected abstract Passo costruisciPassoDelCompito(String id);

	/**
	 * Gli oggetti che il gruppo consegna al ritorno, prima della ricompensa; null se non ce ne sono.
	 */
	protected OggettiDaRaccogliere getOggettiDaConsegnare() {
		return null;
	}

	protected String testoConsegna() {
		return "";
	}

	/**
	 * La città dell'incarico, se è sempre quella (la storia di una città): allora l'incarico parte alla prima visita,
	 * senza aspettare una visita tranquilla. Null per un incarico che si prende in una città qualsiasi.
	 */
	protected TipoLocazione getCittaFissa() {
		return null;
	}

	/**
	 * La città in cui si torna a riscuotere: quella dell'incarico, se non è un'altra (vedi IlCorriere).
	 */
	protected TipoLocazione getCittaDelRitorno() {
		return getCitta();
	}

	/**
	 * Se l'incarico si può offrire nella città in cui è il gruppo (per esempio se c'è un'altra città a cui portare
	 * qualcosa). Vale solo per gli incarichi senza città fissa.
	 */
	protected boolean isPossibileQui() {
		return true;
	}

	/**
	 * Gli incarichi che si prendono in una città qualsiasi si ripetono; le storie delle città no.
	 */
	@Override
	protected boolean isRipetibile() {
		return getCittaFissa() == null;
	}

	/**
	 * Quando l'incarico si offre, prima dell'intermezzo del mandante: il momento di pescare i nomi dei personaggi
	 * (vedi MissioneAPassi.parametro), che l'intermezzo userà.
	 */
	protected void allIncarico() {
	}

	protected String testoCittaDistrutta() {
		return nomeDellaCitta(getCittaDelRitorno()) + " è stata distrutta: nessuno potrà più pagare per l'incarico.";
	}

	/**
	 * La città dell'incarico, o null finché il gruppo non ne ha trovata una.
	 */
	public final TipoLocazione getCitta() {
		String citta = ottieniProprieta(CITTA);
		if (citta != null) {
			return TipoLocazione.valueOf(citta);
		}
		return getCittaFissa();
	}

	/**
	 * Il nome della città dell'incarico, "Ruuna"; vuoto finché non c'è.
	 */
	protected final String getNomeCitta() {
		return nomeDellaCitta(getCitta());
	}

	/**
	 * Il nome di una città, "Ruuna"; vuoto se non c'è.
	 */
	protected static String nomeDellaCitta(TipoLocazione citta) {
		if (citta == null) {
			return "";
		}
		String nome = citta.name().substring(PREFISSO_CITTA.length()).toLowerCase();
		return Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
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
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::isIncaricoDaOffrire)
						.esegui(() -> {
							aggiungiProprieta(CITTA, gruppo.getTipoLocazioneCorrente().name());
							allIncarico();
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaIncarico().getPagine())
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaCitta)
						.esegui(() -> {
							BusEventi.pubblica(new NotificaTestoParagrafo(testoAccettazione()));
							attivaMissione();
						})
						.poi(primoPassoDelCompito());
			case RITORNO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nellaCittaDelRitorno)
						.segnala(this::coordinateDellaCittaDelRitorno)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaRingraziamento().getPagine())
						.falliscoSe(this::isCittaDistrutta, this::testoCittaDistrutta)
						.poi(getOggettiDaConsegnare() != null ? CONSEGNA : RICOMPENSA);
			case CONSEGNA:
				return consegna(MomentoControllo.IN_LOCAZIONE, this::nellaCittaDelRitorno, getOggettiDaConsegnare(), this::testoConsegna)
						.segnala(this::coordinateDellaCittaDelRitorno)
						.falliscoSe(this::isCittaDistrutta, this::testoCittaDistrutta)
						.poi(RICOMPENSA);
			case RICOMPENSA:
				return ricompensa(MomentoControllo.IN_LOCAZIONE, getRicompensa(), this::testoRicompensa)
						.falliscoSe(this::isCittaDistrutta, this::testoCittaDistrutta)
						.poi(Passo.FINE);
			default:
				return costruisciPassoDelCompito(id).falliscoSe(this::isCittaDistrutta, this::testoCittaDistrutta);
		}
	}

	/**
	 * Dov'è la città in cui si riscuote, per il segnalino sulla mappa mentre ci si deve tornare; null se è stata
	 * distrutta.
	 */
	protected final CoordinateMD coordinateDellaCittaDelRitorno() {
		TipoLocazione citta = getCittaDelRitorno();
		return citta == null || LineaTemporale.isCittaDistrutta(citta) ? null : Foresta.getCoordinateLocazioneUnica(citta);
	}

	/**
	 * Se la città in cui si riscuote è stata distrutta.
	 */
	private boolean isCittaDistrutta() {
		TipoLocazione citta = getCittaDelRitorno();
		return citta != null && LineaTemporale.isCittaDistrutta(citta);
	}

	/**
	 * Se il gruppo è dove l'incarico si offre: nella sua città fissa, oppure in una città qualsiasi a una visita
	 * tranquilla, e passata la pausa dopo l'incarico precedente.
	 */
	private boolean isIncaricoDaOffrire() {
		if (getCittaFissa() != null) {
			return nellaCitta();
		}
		return isDisponibile() && inUnaCitta() && isPossibileQui() && isVisitaTranquilla();
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return getCittaFissa() == null;
	}

	private boolean inUnaCitta() {
		TipoLocazione tipo = GruppoGiocatore.getIstanza().getTipoLocazioneCorrente();
		return tipo != null && tipo.getCategoria() == CategoriaLocazione.CITTA
				&& !LineaTemporale.isCittaDistrutta(tipo);
	}

	protected final boolean nellaCitta() {
		return inCitta(getCitta());
	}

	/**
	 * Se il gruppo è nella città in cui si riscuote.
	 */
	protected final boolean nellaCittaDelRitorno() {
		return inCitta(getCittaDelRitorno());
	}

	private static boolean inCitta(TipoLocazione citta) {
		return citta != null && GruppoGiocatore.getIstanza().isInLocazioneUnica(citta) && !LineaTemporale.isCittaDistrutta(citta);
	}
}
