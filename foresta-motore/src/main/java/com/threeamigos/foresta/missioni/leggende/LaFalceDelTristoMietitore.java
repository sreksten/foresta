package com.threeamigos.foresta.missioni.leggende;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.*;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Arrays;
import java.util.List;

/**
 * La falce del Tristo Mietitore. In una città qualsiasi (a una visita tranquilla) un personaggio a caso, spaventatissimo, giura di aver
 * visto la Morte in persona nella foresta, da lontano, con la sua falce e circondata da scheletri; chiede il gruppo, incuriosito, e lui
 * indica il posto, che finisce sulla mappa. In realtà era un necromante: nelle rovine il gruppo trova lui, con quattro scheletri (due
 * da una parte e due dall'altra) e, finché il necromante è vivo, ne arrivano altri (vedi LocazioneBase). Sconfitto il necromante, la sua
 * falce, la Falce del Tristo Mietitore (un leggendario di bottino di leggendari.txt, di cui il racconto riprende la leggenda), passa al gruppo.
 * Si offre una volta sola per partita.
 * <ol>
 * <li>INCARICO, a inizio locazione, in una città: si pescano il narratore e il nome del necromante e parte l'intermezzo;</li>
 * <li>ACCETTAZIONE, in locazione, nella stessa città: si rivendicano le rovine, si segnano sulla mappa e la missione si attiva;</li>
 * <li>SCONTRO, a fine locazione, nelle rovine, quando il necromante è sconfitto: la falce passa al gruppo.</li>
 * </ol>
 */
public class LaFalceDelTristoMietitore extends MissioneAPassi {

	/**
	 * La chiave della Falce in leggendari.txt
	 */
	public static final String CHIAVE_DELLA_FALCE = "FALCE_DEL_TRISTO_MIETITORE";
	/**
	 * Gli scheletri con cui il necromante comincia: due a sinistra e due a destra.
	 */
	public static final int SCHELETRI_INIZIALI = 4;

	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String SCONTRO = "SCONTRO";
	private static final String ASPETTO = "ASPETTO";
	private static final String NECROMANTE = "NECROMANTE";
	private static final String POSTO = "POSTO";
	private static final String SEPARATORE = ",";

	// Chi può raccontare: chi si incontra in una città
	private static final List<AspettoDelMandante> NARRATORI = Arrays.asList(AspettoDelMandante.QUALUNQUE, AspettoDelMandante.DONNA,
			AspettoDelMandante.ARMAIOLO, AspettoDelMandante.ALCHIMISTA, AspettoDelMandante.LOCANDIERE,
			AspettoDelMandante.VENDITORE_DI_PERGAMENE, AspettoDelMandante.INCANTATORE);

	public LaFalceDelTristoMietitore() {
		super(MissionePredefinita.LA_FALCE_DEL_TRISTO_MIETITORE);
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	@Override
	public String getNome() {
		return "La falce del Tristo Mietitore";
	}

	@Override
	public String getDescrizione() {
		return Articoli.inizialeMaiuscola(getNarratore()) + " giura di aver visto la Morte in persona, con la sua falce e circondata da scheletri, "
				+ "nelle rovine segnate sulla mappa.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "Qui il necromante " + getNomeDelNecromante() + " si finse la Morte, con la sua falce e i suoi scheletri.";
	}

	/**
	 * Il necromante, con il nome che gli ha dato la missione.
	 */
	public String getNomeDelNecromante() {
		return parametro(NECROMANTE, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni("NOME_NECROMANTE"));
	}

	/**
	 * Le rovine dove il necromante aspetta il gruppo, o null finché non sono state trovate.
	 */
	public CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	private AspettoDelMandante getAspetto() {
		return AspettoDelMandante.valueOf(parametro(ASPETTO, () -> NARRATORI.get(Dado.tira(0, NARRATORI.size() - 1)).name()));
	}

	/**
	 * Chi ha raccontato, con l'articolo: "l'armaiolo", "una donna".
	 */
	private String getNarratore() {
		switch (getAspetto()) {
			case DONNA:
				return "una donna";
			case ARMAIOLO:
				return "l'armaiolo";
			case ALCHIMISTA:
				return "l'alchimista";
			case LOCANDIERE:
				return "il locandiere";
			case VENDITORE_DI_PERGAMENE:
				return "il venditore di pergamene";
			case INCANTATORE:
				return "l'incantatore";
			default:
				return "un uomo";
		}
	}

	/**
	 * Il necromante e i suoi scheletri: il necromante al centro, due scheletri per parte.
	 */
	public IncontroDiMissione getIncontro() {
		return IncontroDiMissione.di(TipoPersonaggio.SCHELETRO, SCHELETRI_INIZIALI).conCapoAlCentro(getNomeDelNecromante(), TipoPersonaggio.NECROMANTE);
	}

	/**
	 * Nelle rovine del necromante non c'è nessun oggetto: la Falce è già un bel premio.
	 */
	@Override
	public boolean isLocazioneSenzaOggetti(CoordinateMD coordinate) {
		return SCONTRO.equals(getPassoCorrente()) && coordinate.equals(getCovo());
	}

	@Override
	protected String passoIniziale() {
		return INCARICO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case INCARICO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> isDisponibile() && isInUnaCitta() && isVisitaTranquilla())
						.esegui(() -> {
							getAspetto();
							getNomeDelNecromante();
							CoordinateMD qui = GruppoGiocatore.getIstanza().getCoordinate();
							aggiungiProprieta(POSTO, qui.getX() + SEPARATORE + qui.getY());
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDelRacconto)
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE,
								() -> nelPosto() && RegistroMissioni.cercaOCostruisci(TipoLocazione.ROVINE, this).isPresent())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getCovo());
							BusEventi.pubblica(new NotificaTestoParagrafo(Articoli.inizialeMaiuscola(getNarratore())
									+ " segna sulla mappa le rovine dove ha visto la Morte."));
							attivaMissione();
						})
						.poi(SCONTRO);
			case SCONTRO:
				return combattiIlCapo(this::getCovo, getIncontro())
						.esegui(this::consegnaLaFalce)
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	private void consegnaLaFalce() {
		OggettoLeggendario falce = CatalogoLeggendari.getLeggendario(CHIAVE_DELLA_FALCE).orElseThrow(IllegalStateException::new);
		Artefatto artefatto = falce.costruisci(Statistiche.getLivello());
		GruppoGiocatore.getIstanza().addArtefatto(artefatto);
		BusEventi.pubblica(new NotificaTestoParagrafo(getNomeDelNecromante() + " è sconfitto: la Morte non era che un necromante. "
				+ "Fra le ossa dei suoi scheletri resta " + falce.getNomeBreve() + ", e il gruppo la prende."));
		BusEventi.pubblica(new NotificaArtefattoTrovato(artefatto, Statistiche.getLivello()));
	}

	/**
	 * Il racconto: lo spavento, la leggenda della falce (le battute di leggendari.txt), il posto.
	 */
	private List<PaginaIntermezzo> getPagineDelRacconto() {
		OggettoLeggendario falce = CatalogoLeggendari.getLeggendario(CHIAVE_DELLA_FALCE).orElseThrow(IllegalStateException::new);
		ScenaInCitta scena = getAspetto().nuovaScena()
				.parlaIlMandante("Aiuto! Sono ancora tutto un tremito... Ho visto la Morte! La Morte in persona, nella foresta, da lontano!")
				.parlaIlCapo("La Morte? Calmatevi. Che cosa avete visto, esattamente?")
				.parlaIlMandante("Impugnava la sua falce, ed era circondata da scheletri. Una fila di scheletri, come una guardia d'onore.");
		for (String battuta : falce.getLeggenda()) {
			scena.parlaIlMandante(battuta);
		}
		return scena
				.parlaIlCapo("E dove l'avete vista?")
				.parlaIlMandante("Nelle rovine, qui vicino. Ve le segno sulla mappa... ma io non ci tornerei, al vostro posto.")
				.parlaIlCapo("Andiamo a vedere.")
				.getPagine();
	}

	private boolean isInUnaCitta() {
		TipoLocazione tipo = GruppoGiocatore.getIstanza().getTipoLocazioneCorrente();
		return tipo != null && tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA && !LineaTemporale.isCittaDistrutta(tipo);
	}

	private boolean nelPosto() {
		CoordinateMD qui = GruppoGiocatore.getIstanza().getCoordinate();
		return qui != null && (qui.getX() + SEPARATORE + qui.getY()).equals(ottieniProprieta(POSTO));
	}
}
