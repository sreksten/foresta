package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInLocanda;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In una locanda, dalla terza visita in poi, un sacerdote o una sacerdotessa offre al gruppo una benedizione in cambio
 * di un favore (vedi {@link BenedizioneRichiesta}, da missioni.txt): il favore è una missione secondaria che la
 * benedizione affida (vedi {@link IlFavore} e {@link MissioneAPassi#affida}). Fatto il favore, un tempio compare
 * sulla mappa: lì il giocatore sceglie chi del gruppo riceve la benedizione, un modificatore permanente di un
 * attributo. La benedizione si ripete, anche per lo stesso personaggio.
 * <ol>
 * <li>INCONTRO, a inizio locazione, in una locanda già visitata due volte, a una visita tranquilla: l'intermezzo;</li>
 * <li>FAVORE, in locanda: la missione si attiva e affida il favore;</li>
 * <li>ATTESA, a fine locazione: il favore è finito;</li>
 * <li>TEMPIO, a fine locazione: se il favore è riuscito, il tempio compare sulla mappa (altrimenti FALLIMENTO);</li>
 * <li>ARRIVO, a inizio locazione, nel tempio: il tempio è sicuro, niente avversari né oggetti a caso (vedi
 * RegistroMissioni.sopprimiContenutoLocazione);</li>
 * <li>SCELTA, se nel gruppo c'è più di un personaggio in campo, o DIRETTA: la benedizione.</li>
 * </ol>
 */
public class LaBenedizione extends MissioneAPassi {

	public static final String BENEDIZIONE = "BENEDIZIONE";
	public static final String FAVORE = "FAVORE";
	public static final String SCELTA = "SCELTA";
	public static final int ORE_FRA_DUE_BENEDIZIONI = 48;
	private static final String INCONTRO = "INCONTRO";
	private static final String ATTESA = "ATTESA";
	private static final String TEMPIO = "TEMPIO";
	private static final String ARRIVO = "ARRIVO";
	private static final String DIRETTA = "DIRETTA";
	private static final String FALLIMENTO = "FALLIMENTO";
	private static final String LOCANDA = "LOCANDA";
	private static final String RICEVUTA_DA = "RICEVUTA_DA";
	// Le visite precedenti alla locanda sono già contate quando si entra: alle prime due c'è l'intermezzo della locanda
	private static final int VISITE_PRECEDENTI = 2;
	private static final int OPZIONI_MASSIME = 5;

	public LaBenedizione() {
		super(ClasseMissione.LA_BENEDIZIONE);
	}

	public BenedizioneRichiesta getBenedizione() {
		return BenedizioneRichiesta.da(parametro(BENEDIZIONE, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(BENEDIZIONE)));
	}

	/**
	 * Il tempio della benedizione, o null finché la missione non l'ha trovato (prima del tempio la missione non
	 * rivendica niente: il posto del favore è della missione secondaria).
	 */
	public CoordinateMD getTempio() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Il nome di chi ha ricevuto la benedizione, o null se non l'ha ancora ricevuta nessuno.
	 */
	public String getBenedetto() {
		return ottieniProprieta(RICEVUTA_DA);
	}

	@Override
	protected boolean isRipetibile() {
		return true;
	}

	@Override
	protected int getOreFraUnaMissioneELAltra() {
		return ORE_FRA_DUE_BENEDIZIONI;
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	private static boolean inUnaLocandaGiaVisitata() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.getTipoLocazioneCorrente() != TipoLocazione.LOCANDA) {
			return false;
		}
		String visite = Foresta.getLocazioneMD(gruppo.getCoordinate()).ottieniProprieta(Locanda.LOCANDA_VISITE);
		return visite != null && Integer.parseInt(visite) >= VISITE_PRECEDENTI;
	}

	private boolean nellaLocanda() {
		CoordinateMD qui = GruppoGiocatore.getIstanza().getCoordinate();
		return qui != null && (qui.getX() + "_" + qui.getY()).equals(ottieniProprieta(LOCANDA));
	}

	private boolean nelTempio() {
		return getTempio() != null && getTempio().equals(GruppoGiocatore.getIstanza().getCoordinate());
	}

	/**
	 * Chi può ricevere la benedizione: i personaggi in campo, al più quante sono le opzioni di una scelta.
	 */
	private static List<Personaggio> candidati() {
		List<Personaggio> vivi = GruppoGiocatore.getIstanza().getPersonaggiVivi();
		return vivi.size() > OPZIONI_MASSIME ? vivi.subList(0, OPZIONI_MASSIME) : vivi;
	}

	/**
	 * Il personaggio riceve la benedizione: il modificatore permanente, e il testo.
	 */
	private void benedici(Personaggio personaggio) {
		BenedizioneRichiesta benedizione = getBenedizione();
		personaggio.addModificatore(benedizione.nuovoModificatore());
		aggiungiProprieta(RICEVUTA_DA, personaggio.getNome());
		BusEventi.pubblica(new NotificaTestoParagrafo(benedizione.getBenedetto().replace(BenedizioneRichiesta.PERSONAGGIO, personaggio.getNome())
				+ " " + Misc.inizialeMaiuscola(benedizione.getNomeDellaBenedizione()) + " resterà con " + personaggio.getNome() + " per sempre."));
	}

	private List<PaginaIntermezzo> getPagineDellIncontro() {
		BenedizioneRichiesta benedizione = getBenedizione();
		return ScenaInLocanda.conSacerdote(benedizione.isSacerdotessa())
				.parlaIlLocandiere(benedizione.getRichiesta())
				.parlaIlCapo(benedizione.getBattutaDelCapo())
				.parlaIlLocandiere(benedizione.getRisposta())
				.parlaIlLocandiere("Fate questo per me, e vi aspetterò in un tempio per darvi " + benedizione.getNomeDellaBenedizione() + ".")
				.getPagine();
	}

	@Override
	public String getNome() {
		return Misc.inizialeMaiuscola(getBenedizione().getNomeDellaBenedizione());
	}

	@Override
	public String getDescrizione() {
		BenedizioneRichiesta benedizione = getBenedizione();
		String passo = getPassoCorrente();
		if (TEMPIO.equals(passo) || ARRIVO.equals(passo) || SCELTA.equals(passo) || DIRETTA.equals(passo)) {
			return "Il favore è fatto: " + benedizione.getMandante() + " ti aspetta nel tempio segnato sulla mappa, per "
					+ benedizione.getNomeDellaBenedizione() + ".";
		}
		return Misc.inizialeMaiuscola(benedizione.getMandante()) + " ti darà " + benedizione.getNomeDellaBenedizione()
				+ " in cambio di un favore: " + getMissioniAffidate(FAVORE).stream().findFirst().map(Missione::getNome)
				.orElse(benedizione.getFavore().getNome()) + ".";
	}

	@Override
	protected String passoIniziale() {
		return INCONTRO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		BenedizioneRichiesta benedizione = getBenedizione();
		switch (id) {
			case INCONTRO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> isDisponibile() && inUnaLocandaGiaVisitata() && isVisitaTranquilla())
						.esegui(() -> {
							CoordinateMD qui = GruppoGiocatore.getIstanza().getCoordinate();
							aggiungiProprieta(LOCANDA, qui.getX() + "_" + qui.getY());
							getBenedizione();
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDellIncontro)
						.poi(FAVORE);
			case FAVORE:
				return affida(FAVORE, MomentoControllo.IN_LOCAZIONE, this::nellaLocanda,
								() -> Collections.singletonList(IlFavore.per(benedizione)))
						.esegui(this::attivaMissione)
						.poi(ATTESA);
			case ATTESA:
				return attendiLeAffidate(FAVORE, MomentoControllo.POST_LOCAZIONE)
						.poi(() -> sonoRiusciteLeAffidate(FAVORE) ? TEMPIO : FALLIMENTO);
			case TEMPIO:
				return cercaLocazione(MomentoControllo.POST_LOCAZIONE, TipoLocazione.TEMPIO)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getTempio());
							BusEventi.pubblica(new NotificaTestoParagrafo(Misc.inizialeMaiuscola(benedizione.getMandante()) + " vi aspetta "
									+ Misc.conPreposizione("in", TestiDeiLuoghi.nome(getTempio())) + ", segnato sulla mappa."));
						})
						.poi(ARRIVO);
			case ARRIVO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::nelTempio)
						.aOgniControllo(() -> {
							if (nelTempio()) {
								RegistroMissioni.sopprimiContenutoLocazione(getTempio());
							}
						})
						.poi(() -> candidati().size() > 1 ? SCELTA : DIRETTA);
			case SCELTA:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> true)
						.chiediScelta("Chi riceve " + benedizione.getNomeDellaBenedizione() + "?",
								candidati().stream().map(Personaggio::getNome).collect(Collectors.toList()))
						.esegui(() -> benedici(candidati().get(Integer.parseInt(getRisposta(SCELTA)) - 1)))
						.poi(Passo.FINE);
			case DIRETTA:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> true)
						.esegui(() -> benedici(candidati().get(0)))
						.poi(Passo.FINE);
			case FALLIMENTO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> true)
						.esegui(this::fallisciMissione)
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
