package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroArtefatti;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Qualcuno (l'armaiolo in città, il locandiere in una locanda: vedi le sottoclassi) racconta la leggenda di un
 * oggetto leggendario (vedi {@link OggettoLeggendario}, da leggendari.txt), e il gruppo lo va a prendere: la missione
 * fa sorgere su un bosco un tempio che lo custodisce, segnato sulla mappa, con dei guardiani. Raccolto l'oggetto, la
 * missione è completa. Il leggendario lo tiene il gruppo: è la caccia al tesoro della Foresta.
 * <ol>
 * <li>INCARICO, a inizio locazione, dove racconta il narratore, a una visita tranquilla: si pesca un leggendario che
 * nessun'altra leggenda della partita ha già pescato, e parte l'intermezzo con la leggenda;</li>
 * <li>ACCETTAZIONE, in locazione, nello stesso posto: sorge il tempio e la missione si attiva;</li>
 * <li>RECUPERO, a fine locazione, nel tempio, quando il leggendario non c'è più: la missione si completa.</li>
 * </ol>
 * Si ripete con altri leggendari finché ce ne sono; fra il racconto di una leggenda e quello della successiva, di
 * chiunque, passano almeno {@link #ORE_FRA_DUE_LEGGENDE} ore di gioco.
 */
public abstract class LaLeggenda extends MissioneAPassi {

	/**
	 * Quante ore di gioco passano almeno fra due leggende, chiunque le racconti.
	 */
	public static final int ORE_FRA_DUE_LEGGENDE = 36;
	public static final String LEGGENDARIO = "LEGGENDARIO";
	private static final String GUARDIANI = "GUARDIANI";
	private static final String POSTO = "POSTO";
	private static final String RACCONTATA_ALLE = "RACCONTATA_ALLE";
	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String RECUPERO = "RECUPERO";
	private static final String SEPARATORE = ",";

	/**
	 * Chi racconta la leggenda nell'intermezzo: le sue battute e quelle del capo.
	 */
	protected interface Racconto {
		Racconto narra(String testo);

		Racconto parlaIlCapo(String testo);

		List<PaginaIntermezzo> getPagine();
	}

	protected LaLeggenda(ClasseMissione classe) {
		super(classe);
	}

	/**
	 * Se il gruppo è dove il narratore racconta le sue leggende.
	 */
	protected abstract boolean isPostoDelRacconto();

	/**
	 * Il narratore, con l'articolo: "l'armaiolo".
	 */
	protected abstract String getNarratore();

	protected abstract Racconto nuovoRacconto();

	@Override
	protected boolean isRipetibile() {
		return true;
	}

	@Override
	protected int getOreFraUnaMissioneELAltra() {
		return ORE_FRA_DUE_LEGGENDE;
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	/**
	 * Il leggendario di questa leggenda, o null finché non l'ha pescato.
	 */
	public final OggettoLeggendario getLeggendario() {
		String riga = ottieniProprieta(LEGGENDARIO);
		return riga == null ? null : OggettoLeggendario.da(riga);
	}

	/**
	 * Chi custodisce il leggendario: quelli della sua riga, o quelli adatti al livello del gruppo quando la leggenda
	 * è stata raccontata.
	 */
	public final IncontroDiMissione getGuardiani() {
		String guardiani = ottieniProprieta(GUARDIANI);
		if (guardiani == null) {
			return getLeggendario().getGuardiani().orElseGet(() -> guardianiPerLivello(Statistiche.getLivello()));
		}
		String[] parti = guardiani.split(SEPARATORE);
		return IncontroDiMissione.di(ClassePersonaggio.valueOf(parti[0]), Integer.parseInt(parti[1]));
	}

	/**
	 * I guardiani per un gruppo di quel livello, quando la riga non li dice.
	 */
	static IncontroDiMissione guardianiPerLivello(int livello) {
		if (livello <= 2) {
			return IncontroDiMissione.di(ClassePersonaggio.HOBGOBLIN, 4);
		}
		if (livello <= 4) {
			return IncontroDiMissione.di(ClassePersonaggio.TROLL, 3);
		}
		if (livello <= 6) {
			return IncontroDiMissione.di(ClassePersonaggio.VIVERNA, 4);
		}
		return IncontroDiMissione.di(ClassePersonaggio.CHIMERA_DRAGO, 3);
	}

	/**
	 * I guardiani per i testi, con l'articolo: "le Viverne".
	 */
	private String getNomeDeiGuardiani() {
		Personaggio modello = getGuardiani().getClasse().getMoltiplicatoriDiClasse();
		return modello.getADP() + modello.getNomePlurale();
	}

	private CoordinateMD getTempio() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	@Override
	public String getNome() {
		OggettoLeggendario leggendario = getLeggendario();
		return leggendario == null ? "Una leggenda" : "Recupera " + leggendario.getNomeBreve();
	}

	@Override
	public String getDescrizione() {
		OggettoLeggendario leggendario = getLeggendario();
		if (leggendario == null) {
			return "";
		}
		String tempio = getTempio() == null ? "in un tempio" : Misc.conPreposizione("in", Tempio.getNome(getTempio()));
		return "Secondo la leggenda raccontata " + Misc.conPreposizione("da", getNarratore()) + ", " + getNomeDeiGuardiani()
				+ " custodiscono " + leggendario.getNomeBreve() + " " + tempio + ", segnato sulla mappa.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		OggettoLeggendario leggendario = getLeggendario();
		return leggendario == null ? null : "Qui " + getNomeDeiGuardiani() + " custodivano " + leggendario.getNomeBreve() + ".";
	}

	@Override
	protected String passoIniziale() {
		return INCARICO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case INCARICO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> isDisponibile() && isPostoDelRacconto()
								&& nessunaLeggendaRecente() && isVisitaTranquilla() && (getLeggendario() != null || pesca().isPresent()))
						.esegui(this::raccontaLaLeggenda)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDelRacconto)
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				// Il tempio sorge su un bosco che la missione rivendica; se non ce n'è nessuno libero se ne fa uno
				return Passo.quando(MomentoControllo.IN_LOCAZIONE,
								() -> nelPosto() && RegistroMissioni.cercaOCostruisci(ClassiLocazione.BOSCO, this).isPresent())
						.esegui(() -> {
							RegistroArtefatti.custodisciInUnTempioNuovo(getLeggendario().costruisci(), getTempio());
							BusEventi.pubblica(new NotificaTestoParagrafo(Misc.inizialeMaiuscola(getNarratore()) + " segna sulla mappa "
									+ Tempio.getNome(getTempio()) + ", dove " + getNomeDeiGuardiani() + " custodiscono "
									+ getLeggendario().getNomeBreve() + "."));
							attivaMissione();
						})
						.poi(RECUPERO);
			case RECUPERO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE,
								() -> getTempio().equals(GruppoGiocatore.getIstanza().getCoordinate())
										&& RegistroArtefatti.getArtefattoInLocazione(getTempio()) == null)
						.affronta(this::getTempio, getGuardiani())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("La leggenda era vera: "
								+ getLeggendario().getNomeBreve() + " è nelle vostre mani.")))
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	/**
	 * Quando la leggenda si racconta: il leggendario (se non è già fissato), i guardiani, il posto e l'ora.
	 */
	private void raccontaLaLeggenda() {
		if (getLeggendario() == null) {
			aggiungiProprieta(LEGGENDARIO, pesca().orElseThrow(IllegalStateException::new));
		}
		IncontroDiMissione guardiani = getGuardiani();
		aggiungiProprieta(GUARDIANI, guardiani.getClasse().name() + SEPARATORE + guardiani.getNumero());
		CoordinateMD qui = GruppoGiocatore.getIstanza().getCoordinate();
		aggiungiProprieta(POSTO, qui.getX() + SEPARATORE + qui.getY());
		aggiungiProprieta(RACCONTATA_ALLE, String.valueOf(oreDiGioco()));
	}

	private List<PaginaIntermezzo> getPagineDelRacconto() {
		OggettoLeggendario leggendario = getLeggendario();
		Racconto racconto = nuovoRacconto();
		leggendario.getLeggenda().forEach(racconto::narra);
		String breve = leggendario.getNomeBreve();
		boolean plurale = breve.startsWith("gli ") || breve.startsWith("i ") || breve.startsWith("le ");
		return racconto
				.narra("Oggi " + (plurale ? "riposano" : "riposa") + " in un tempio nella foresta. Ma attenti: a fare la guardia ci sono "
						+ getNomeDeiGuardiani() + ".")
				.parlaIlCapo("Con " + breve + " il Drago avrebbe di che preoccuparsi.")
				.narra("Allora andate. Vi segno il tempio sulla mappa.")
				.parlaIlCapo("Partiamo subito.")
				.getPagine();
	}

	private boolean nelPosto() {
		CoordinateMD qui = GruppoGiocatore.getIstanza().getCoordinate();
		return qui != null && (qui.getX() + SEPARATORE + qui.getY()).equals(ottieniProprieta(POSTO));
	}

	/**
	 * Le altre leggende della partita, di qualunque narratore, finite o no.
	 */
	private List<LaLeggenda> altreLeggende() {
		return RegistroMissioni.getTutteLeMissioni().stream()
				.filter(m -> m != this && m instanceof LaLeggenda)
				.map(LaLeggenda.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Se nessun'altra leggenda è stata raccontata nelle ultime {@link #ORE_FRA_DUE_LEGGENDE} ore.
	 */
	private boolean nessunaLeggendaRecente() {
		return altreLeggende().stream()
				.map(leggenda -> leggenda.ottieniProprieta(RACCONTATA_ALLE))
				.noneMatch(ora -> ora != null && oreDiGioco() - Long.parseLong(ora) < ORE_FRA_DUE_LEGGENDE);
	}

	/**
	 * Un leggendario che nessun'altra leggenda della partita ha già pescato, o vuoto se non ne restano.
	 */
	private Optional<String> pesca() {
		Set<String> giaPescati = altreLeggende().stream()
				.map(LaLeggenda::getLeggendario)
				.filter(leggendario -> leggendario != null)
				.map(OggettoLeggendario::getNomeBreve)
				.collect(Collectors.toSet());
		return ProduttoreDiTestiCasuale.oggettoLeggendario(riga -> giaPescati.contains(OggettoLeggendario.da(riga).getNomeBreve()));
	}
}
