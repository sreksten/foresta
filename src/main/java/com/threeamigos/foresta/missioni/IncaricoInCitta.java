package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaAggiornamentoStatoMissione;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.RegistroMissioni;

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
 * Se la città viene distrutta dopo l'accettazione, la missione fallisce.
 * <p>
 * Un incarico può avere una città fissa ({@link #getCittaFissa()}): è la storia di quella città (il medaglione di
 * Fleena, le derrate di Ruuna, vedi MissioneRecuperaBersaglio), e parte alla prima visita. Gli altri si prendono in
 * una città qualsiasi, ma solo a una visita tranquilla, in cui nessun'altra missione mostra un intermezzo entrando in
 * città: non alla prima visita, quando parte la missione della città, né quando si torna a concluderne una; fra due
 * incarichi pronti nella stessa visita parte il primo controllato, l'altro aspetta.
 * <p>
 * Un incarico ripetibile ({@link #isRipetibile()}), finito (bene o male), ne lascia uno nuovo uguale, che si può
 * prendere dopo {@link #ORE_FRA_UN_INCARICO_E_L_ALTRO} ore di gioco.
 */
public abstract class IncaricoInCitta extends MissioneAPassi {

	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	protected static final String RITORNO = "RITORNO";
	private static final String CONSEGNA = "CONSEGNA";
	private static final String RICOMPENSA = "RICOMPENSA";
	private static final String CITTA = "CITTA";
	private static final String PREFISSO_CITTA = "CITTA_";
	private static final String DISPONIBILE_DALLE = "DISPONIBILE_DALLE";
	private static final String GIA_RIPETUTO = "GIA_RIPETUTO";

	/**
	 * Quante ore di gioco passano fra la fine di un incarico ripetibile e quello nuovo che lascia.
	 */
	public static final int ORE_FRA_UN_INCARICO_E_L_ALTRO = 48;

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
	protected ClassiLocazione getCittaFissa() {
		return null;
	}

	/**
	 * Se l'incarico, finito, ne lascia uno nuovo uguale: sì per quelli che si prendono in una città qualsiasi.
	 */
	protected boolean isRipetibile() {
		return getCittaFissa() == null;
	}

	protected String testoCittaDistrutta() {
		return getNomeCitta() + " è stata distrutta: nessuno potrà più pagare per l'incarico.";
	}

	/**
	 * La città dell'incarico, o null finché il gruppo non ne ha trovata una.
	 */
	public final ClassiLocazione getCitta() {
		String citta = ottieniProprieta(CITTA);
		if (citta != null) {
			return ClassiLocazione.valueOf(citta);
		}
		return getCittaFissa();
	}

	/**
	 * Il nome della città dell'incarico, "Ruuna"; vuoto finché non c'è.
	 */
	protected final String getNomeCitta() {
		ClassiLocazione citta = getCitta();
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
						.esegui(() -> aggiungiProprieta(CITTA, gruppo.getClasseLocazioneCorrente().name()))
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
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nellaCitta)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaRingraziamento().getPagine())
						.falliscoSe(this::isCittaDistrutta, this::testoCittaDistrutta)
						.poi(getOggettiDaConsegnare() != null ? CONSEGNA : RICOMPENSA);
			case CONSEGNA:
				return consegna(MomentoControllo.IN_LOCAZIONE, this::nellaCitta, getOggettiDaConsegnare(), this::testoConsegna)
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

	private boolean isCittaDistrutta() {
		return getCitta() != null && LineaTemporale.isCittaDistrutta(getCitta());
	}

	/**
	 * Se il gruppo è dove l'incarico si offre: nella sua città fissa, oppure in una città qualsiasi a una visita
	 * tranquilla, e passata la pausa dopo l'incarico precedente.
	 */
	private boolean isIncaricoDaOffrire() {
		if (getCittaFissa() != null) {
			return nellaCitta();
		}
		String disponibileDalle = ottieniProprieta(DISPONIBILE_DALLE);
		if (disponibileDalle != null && oreDiGioco() < Long.parseLong(disponibileDalle)) {
			return false;
		}
		return inUnaCitta() && isVisitaTranquilla();
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return getCittaFissa() == null;
	}

	@Override
	public void completaMissione() {
		super.completaMissione();
		lasciaUnIncaricoNuovo();
	}

	@Override
	public void fallisciMissione() {
		boolean eraInCorso = !isCompleta() && !isFallita();
		super.fallisciMissione();
		if (eraInCorso && !RegistroMissioni.TipoMissionePredefinita.contieneMissione(getId())) {
			// Per le missioni predefinite l'avviso lo dà già MissioneBase
			BusEventi.pubblica(new NotificaAggiornamentoStatoMissione(this, "MISSIONE FALLITA", getNome()));
		}
		lasciaUnIncaricoNuovo();
	}

	/**
	 * Un incarico ripetibile, finito, ne lascia uno nuovo della stessa classe, che si potrà prendere dopo la pausa.
	 * Una volta sola, anche se la fine si controllasse due volte.
	 */
	private void lasciaUnIncaricoNuovo() {
		if (!isRipetibile() || ottieniProprieta(GIA_RIPETUTO) != null) {
			return;
		}
		aggiungiProprieta(GIA_RIPETUTO, AFFERMATIVO);
		Missione nuovo = getModelloDati().getClasse().getIstanza();
		nuovo.aggiungiProprieta(DISPONIBILE_DALLE, String.valueOf(oreDiGioco() + ORE_FRA_UN_INCARICO_E_L_ALTRO));
		RegistroMissioni.aggiungiMissioneSecondaria(nuovo);
	}

	private boolean inUnaCitta() {
		ClassiLocazione classe = GruppoGiocatore.getIstanza().getClasseLocazioneCorrente();
		return classe != null && classe.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA
				&& !LineaTemporale.isCittaDistrutta(classe);
	}

	protected final boolean nellaCitta() {
		return getCitta() != null && GruppoGiocatore.getIstanza().isInLocazioneUnica(getCitta()) && !isCittaDistrutta();
	}
}
