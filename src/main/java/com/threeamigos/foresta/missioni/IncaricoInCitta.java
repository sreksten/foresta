package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.RegistroMissioni;

/**
 * Un incarico preso in una città qualsiasi: un mandante chiede un servizio, il gruppo lo fa e torna in quella città
 * a riscuotere. L'incarico si offre solo a una visita tranquilla, in cui nessun'altra missione mostra un intermezzo
 * entrando in città: non alla prima visita, quando parte la missione della città, né quando si torna a concluderne
 * una; fra due incarichi in città pronti nella stessa visita parte il primo controllato, l'altro aspetta.
 * <ol>
 * <li>INCARICO, a inizio locazione, in una città: la missione se la ricorda e parte l'intermezzo del mandante;</li>
 * <li>ACCETTAZIONE, in locazione, nella città: la missione si attiva, dopo l'intermezzo;</li>
 * <li>i passi del compito, dal {@link #primoPassoDelCompito()}: l'ultimo va a {@link #RITORNO};</li>
 * <li>RITORNO, a inizio locazione, di nuovo nella città: l'intermezzo del ringraziamento;</li>
 * <li>RICOMPENSA, in locazione, nella città: le monete, e la missione si completa.</li>
 * </ol>
 * Se la città viene distrutta dopo l'accettazione, la missione fallisce (la guardia è di questa classe: i passi
 * del compito non usano {@link Passo#falliscoSe}).
 */
public abstract class IncaricoInCitta extends MissioneAPassi {

	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	protected static final String RITORNO = "RITORNO";
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
	 * La città dell'incarico, o null finché il gruppo non ne ha trovata una.
	 */
	public final ClassiLocazione getCitta() {
		String citta = ottieniProprieta(CITTA);
		return citta == null ? null : ClassiLocazione.valueOf(citta);
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
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> inUnaCitta() && isVisitaTranquilla())
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
						.poi(RICOMPENSA);
			case RICOMPENSA:
				return ricompensa(MomentoControllo.IN_LOCAZIONE, getRicompensa(), this::testoRicompensa)
						.falliscoSe(this::isCittaDistrutta, this::testoCittaDistrutta)
						.poi(Passo.FINE);
			default:
				return costruisciPassoDelCompito(id).falliscoSe(this::isCittaDistrutta, this::testoCittaDistrutta);
		}
	}

	private String testoCittaDistrutta() {
		return getNomeCitta() + " è stata distrutta: nessuno potrà più pagare per l'incarico.";
	}

	private boolean isCittaDistrutta() {
		return getCitta() != null && LineaTemporale.isCittaDistrutta(getCitta());
	}

	/**
	 * Nessun'altra missione mostrerà un intermezzo entrando in questa locazione. Gli altri incarichi in città contano
	 * solo se il loro è già in attesa: se si guardassero l'un l'altro prima di partire, non partirebbe nessuno.
	 */
	private boolean isVisitaTranquilla() {
		for (Missione missione : RegistroMissioni.getTutteLeMissioni()) {
			if (missione == this || !(missione instanceof MissioneAPassi)) {
				continue;
			}
			MissioneAPassi altra = (MissioneAPassi) missione;
			boolean inArrivo = altra instanceof IncaricoInCitta
					? altra.getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE) != null
					: altra.haUnIntermezzoInArrivo(MomentoControllo.PRE_LOCAZIONE, MomentoIntermezzo.INIZIO_LOCAZIONE);
			if (inArrivo) {
				return false;
			}
		}
		return true;
	}

	private boolean inUnaCitta() {
		ClassiLocazione classe = GruppoGiocatore.getIstanza().getClasseLocazioneCorrente();
		return classe != null && classe.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA
				&& !LineaTemporale.isCittaDistrutta(classe);
	}

	private boolean nellaCitta() {
		return getCitta() != null && GruppoGiocatore.getIstanza().isInLocazioneUnica(getCitta()) && !isCittaDistrutta();
	}
}
