package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroArtefatti;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.Comparator;
import java.util.List;
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
 * Si ripete con altri leggendari finché ce ne sono, favorendo i pezzi mancanti dei set già cominciati (vedi
 * PescaLeggendaria); fra il racconto di una leggenda e quello della successiva, di chiunque, passano almeno
 * {@link #ORE_FRA_DUE_LEGGENDE} ore di gioco.
 */
public abstract class LaLeggenda extends MissioneAPassi implements ConLeggendario {

	/**
	 * Quante ore di gioco passano almeno fra due leggende, chiunque le racconti.
	 */
	public static final int ORE_FRA_DUE_LEGGENDE = 36;
	public static final String LEGGENDARIO = "LEGGENDARIO";
	private static final String GUARDIANI = "GUARDIANI";
	private static final String POSTO = "POSTO";
	private static final String RACCONTATA_ALLE = "RACCONTATA_ALLE";
	// La leggenda è stata raccontata con un set cominciato, ma senza un suo pezzo mancante (vedi PescaLeggendaria)
	private static final String SENZA_PEZZI = "SENZA_PEZZI";
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
	@Override
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
		return IncontroDiMissione.di(TipoPersonaggio.valueOf(parti[0]), Integer.parseInt(parti[1]));
	}

	/**
	 * I guardiani per un gruppo di quel livello, quando la riga non li dice.
	 */
	static IncontroDiMissione guardianiPerLivello(int livello) {
		if (livello <= 2) {
			return IncontroDiMissione.di(TipoPersonaggio.HOBGOBLIN, 4);
		}
		if (livello <= 4) {
			return IncontroDiMissione.di(TipoPersonaggio.TROLL, 3);
		}
		if (livello <= 6) {
			return IncontroDiMissione.di(TipoPersonaggio.VIVERNA, 4);
		}
		return IncontroDiMissione.di(TipoPersonaggio.CHIMERA_DRAGO, 3);
	}

	/**
	 * I guardiani per i testi, con l'articolo: "le Viverne".
	 */
	private String getNomeDeiGuardiani() {
		Personaggio modello = FabbricaPersonaggi.modello(getGuardiani().getClasse());
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
								&& nessunaLeggendaRecente() && isVisitaTranquilla() && (getLeggendario() != null || restaUnLeggendario()))
						.esegui(this::raccontaLaLeggenda)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDelRacconto)
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				// Il tempio sorge su un bosco che la missione rivendica; se non ce n'è nessuno libero se ne fa uno
				return Passo.quando(MomentoControllo.IN_LOCAZIONE,
								() -> nelPosto() && RegistroMissioni.cercaOCostruisci(TipoLocazione.BOSCO, this).isPresent())
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
	 * Quando la leggenda si racconta: il leggendario (se non è già fissato), se ha lasciato in attesa un set
	 * cominciato, i guardiani, il posto e l'ora.
	 */
	private void raccontaLaLeggenda() {
		Set<String> giaPescati = giaPescati();
		if (getLeggendario() == null) {
			aggiungiProprieta(LEGGENDARIO, PescaLeggendaria.pesca(giaPescati, leggendeSenzaPezzi()));
		}
		if (!PescaLeggendaria.setCominciati(giaPescati).isEmpty()
				&& !PescaLeggendaria.isPezzoMancante(getLeggendario().getChiave(), giaPescati)) {
			aggiungiProprieta(SENZA_PEZZI, AFFERMATIVO);
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
		// Se è un altro pezzo di un set già cominciato, il narratore lo dice subito
		PescaLeggendaria.battutaDelSet(leggendario, giaPescati()).ifPresent(racconto::narra);
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
	 * Le chiavi dei leggendari che le altre missioni della partita, leggende o tornei, hanno già pescato.
	 */
	private Set<String> giaPescati() {
		return PescaLeggendaria.giaPescati(this);
	}

	private boolean restaUnLeggendario() {
		return PescaLeggendaria.restaUnLeggendario(giaPescati());
	}

	/**
	 * Quante delle ultime leggende raccontate, di fila, hanno lasciato in attesa un set cominciato.
	 */
	private int leggendeSenzaPezzi() {
		List<LaLeggenda> raccontate = altreLeggende().stream()
				.filter(leggenda -> leggenda.ottieniProprieta(RACCONTATA_ALLE) != null)
				.sorted(Comparator.comparingLong((LaLeggenda leggenda) -> Long.parseLong(leggenda.ottieniProprieta(RACCONTATA_ALLE))).reversed())
				.collect(Collectors.toList());
		int senzaPezzi = 0;
		for (LaLeggenda leggenda : raccontate) {
			if (leggenda.ottieniProprieta(SENZA_PEZZI) == null) {
				break;
			}
			senzaPezzi++;
		}
		return senzaPezzi;
	}
}
