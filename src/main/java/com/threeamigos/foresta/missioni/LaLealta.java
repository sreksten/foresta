package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaFraCompagni;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Il problema personale di un compagno (vedi {@link LealtaRichiesta}, da missioni.txt): una sera, intorno al fuoco
 * dell'accampamento, un compagno che non è il capo del gruppo confida qualcosa e chiede un favore, che è una missione
 * secondaria che la lealtà affida (vedi {@link IlFavore} e {@link MissioneAPassi#affida}). Fatto il favore, alla fine
 * della locazione il compagno ringrazia e riceve un modificatore permanente, con la nota {@link LealtaRichiesta#NOTA}:
 * chi ce l'ha non chiede più niente. Si ripete, con un altro compagno. Se il compagno muore o lascia il gruppo, la
 * lealtà fallisce, e con lei il favore.
 * <ol>
 * <li>INCONTRO, all'accampamento, se c'è un compagno che non ha ancora chiesto niente: l'intermezzo della confidenza;</li>
 * <li>FAVORE, subito: la missione si attiva e affida il favore;</li>
 * <li>ATTESA, a fine locazione: il favore è finito;</li>
 * <li>RINGRAZIAMENTO, subito, se il favore è riuscito (altrimenti FALLIMENTO): l'intermezzo del ringraziamento e il
 * modificatore.</li>
 * </ol>
 */
public class LaLealta extends MissioneAPassi {

	public static final String LEALTA = "LEALTA";
	public static final String FAVORE = "FAVORE";
	public static final int ORE_FRA_DUE_LEALTA = 72;
	private static final String INCONTRO = "INCONTRO";
	private static final String ATTESA = "ATTESA";
	private static final String RINGRAZIAMENTO = "RINGRAZIAMENTO";
	private static final String FALLIMENTO = "FALLIMENTO";
	private static final String COMPAGNO = "COMPAGNO";
	private static final String NOME_DEL_COMPAGNO = "NOME_DEL_COMPAGNO";

	public LaLealta() {
		super(ClasseMissione.LA_LEALTA);
	}

	public LealtaRichiesta getLealta() {
		return LealtaRichiesta.da(parametro(LEALTA, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(LEALTA)));
	}

	@Override
	protected boolean isRipetibile() {
		return true;
	}

	@Override
	protected int getOreFraUnaMissioneELAltra() {
		return ORE_FRA_DUE_LEALTA;
	}

	/**
	 * Chi può confidarsi: i personaggi vivi del gruppo, non il capo, non quelli a tempo o di passaggio, e non chi è già
	 * leale.
	 */
	private static List<Personaggio> candidati() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		return gruppo.getPersonaggiVivi().stream()
				.filter(p -> p != gruppo.getCapo() && !p.isATempo() && !p.isOspiteDiLocazione())
				.filter(p -> p.getModelloDati().getModificatori().stream().noneMatch(m -> LealtaRichiesta.NOTA.equals(m.getNote())))
				.collect(Collectors.toList());
	}

	/**
	 * Il compagno che ha chiesto il favore, se è ancora vivo nel gruppo.
	 */
	public Optional<Personaggio> getCompagno() {
		String uuid = ottieniProprieta(COMPAGNO);
		return GruppoGiocatore.getIstanza().getPersonaggi().stream()
				.filter(p -> p.isVivo() && p.getModelloDati().getUuid().equals(uuid))
				.findFirst();
	}

	/**
	 * Il nome del compagno, o null finché non si è confidato.
	 */
	public String getNomeDelCompagno() {
		return ottieniProprieta(NOME_DEL_COMPAGNO);
	}

	private void scegliIlCompagno() {
		List<Personaggio> candidati = candidati();
		Personaggio compagno = candidati.get(Dado.tiraAncheAUnaFaccia(candidati.size()) - 1);
		aggiungiProprieta(COMPAGNO, compagno.getModelloDati().getUuid());
		aggiungiProprieta(NOME_DEL_COMPAGNO, compagno.getNome());
		getLealta();
	}

	private String testo(String testo) {
		return testo.replace(LealtaRichiesta.PERSONAGGIO, getNomeDelCompagno());
	}

	/**
	 * Il compagno per primo, poi il capo, poi gli altri: parlano i primi due.
	 */
	private List<Personaggio> inScena() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		Personaggio compagno = getCompagno().orElseThrow(IllegalStateException::new);
		List<Personaggio> personaggi = new ArrayList<>();
		personaggi.add(compagno);
		personaggi.add(gruppo.getCapo());
		gruppo.getPersonaggiVivi().stream().filter(p -> !personaggi.contains(p)).forEach(personaggi::add);
		return personaggi;
	}

	private List<PaginaIntermezzo> getPagineDellaConfidenza() {
		LealtaRichiesta lealta = getLealta();
		return ScenaFraCompagni.allAccampamento(getNomeDelCompagno() + " guarda il fuoco a lungo, poi si decide a parlare.", inScena())
				.parlaIlPrimo(testo(lealta.getConfidenza()))
				.parlaIlSecondo(testo(lealta.getBattutaDelCapo()))
				.parlaIlPrimo(testo(lealta.getRisposta()))
				.getPagine();
	}

	private List<PaginaIntermezzo> getPagineDelRingraziamento() {
		return ScenaFraCompagni.in(GruppoGiocatore.getIstanza().getClasseLocazioneCorrente(),
						getNomeDelCompagno() + " ha qualcosa da dire.", inScena())
				.parlaIlPrimo(testo(getLealta().getRingraziamento()))
				.getPagine();
	}

	private void premia() {
		Personaggio compagno = getCompagno().orElseThrow(IllegalStateException::new);
		compagno.addModificatore(getLealta().nuovoModificatore());
		BusEventi.pubblica(new NotificaTestoParagrafo(testo(getLealta().getLeale())));
	}

	/**
	 * Fallita la lealtà, il favore non serve più.
	 */
	@Override
	public void fallisciMissione() {
		getMissioniAffidate(FAVORE).stream().filter(m -> !m.isCompleta() && !m.isFallita()).forEach(Missione::fallisciMissione);
		super.fallisciMissione();
	}

	private boolean isCompagnoPerso() {
		return !getCompagno().isPresent();
	}

	private String testoSeIlCompagnoEPerso() {
		return getNomeDelCompagno() + " non è più con il gruppo: il favore non serve più.";
	}

	@Override
	public String getNome() {
		String compagno = getNomeDelCompagno();
		return compagno == null ? "La lealtà" : "La lealtà di " + compagno;
	}

	@Override
	public String getDescrizione() {
		if (getNomeDelCompagno() == null) {
			return "Prima o poi, intorno al fuoco, qualcuno del gruppo avrà qualcosa da chiedere.";
		}
		return getNomeDelCompagno() + " ti ha chiesto un favore: " + getMissioniAffidate(FAVORE).stream().findFirst()
				.map(Missione::getNome).orElse(getLealta().getFavore().getNome()) + ".";
	}

	@Override
	protected String passoIniziale() {
		return INCONTRO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		LealtaRichiesta lealta = getLealta();
		switch (id) {
			case INCONTRO:
				return Passo.quando(MomentoControllo.ACCAMPAMENTO, () -> isDisponibile() && !candidati().isEmpty())
						.esegui(this::scegliIlCompagno)
						.conIntermezzo(MomentoIntermezzo.ACCAMPAMENTO, this::getPagineDellaConfidenza)
						.poi(FAVORE);
			case FAVORE:
				return affida(FAVORE, MomentoControllo.ACCAMPAMENTO, () -> true,
								() -> Collections.singletonList(IlFavore.per(lealta, getNomeDelCompagno())))
						.esegui(this::attivaMissione)
						.poi(ATTESA);
			case ATTESA:
				return attendiLeAffidate(FAVORE, MomentoControllo.POST_LOCAZIONE)
						.falliscoSe(this::isCompagnoPerso, this::testoSeIlCompagnoEPerso)
						.poi(() -> sonoRiusciteLeAffidate(FAVORE) ? RINGRAZIAMENTO : FALLIMENTO);
			case RINGRAZIAMENTO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, () -> true)
						.falliscoSe(this::isCompagnoPerso, this::testoSeIlCompagnoEPerso)
						.esegui(this::premia)
						.conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA, this::getPagineDelRingraziamento)
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
