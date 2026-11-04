package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.intermezzi.ScenaNelBosco;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Ricompensa;
import com.threeamigos.foresta.missioni.leggende.ConLeggendario;
import com.threeamigos.foresta.missioni.leggende.PescaLeggendaria;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Vichingo;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Il viaggio di Olaf: dopo sei o sette giorni di cammino, entrando in un bosco, il gruppo trova un vichingo smarrito, che la sera prima
 * ha bevuto troppo Sglord a una festa in maschera con il bardo delle locande. Olaf si unisce al gruppo come personaggio che combatte, a
 * tempo (vedi TipoMotivoNelGruppo.MISSIONE): il suo tempo lo fa scadere la missione, quando si completa. Vuole uccidere una viverna o
 * una chimera drago e poi essere portato in una città a comprare dei barili di Sglord. Fatto questo si separa dal gruppo e lascia come
 * ringraziamento un oggetto leggendario.
 * <ol>
 * <li>PARAMETRI, prima della prima locazione: si fissa il giorno dell'incontro;</li>
 * <li>INCONTRO, prima di una locazione di bosco, dal giorno fissato in poi, a una visita tranquilla e se si può fargli posto (vedi
 * GruppoGiocatore.faiPostoAUnoATempo): l'intermezzo dell'incontro;</li>
 * <li>ACCETTAZIONE, in locazione, nel bosco: Olaf si unisce al gruppo (a tempo, vedi sopra), si sceglie il leggendario e la missione si attiva;</li>
 * <li>CACCIA, dopo ogni locazione: sconfitta una viverna o una chimera drago, dovunque;</li>
 * <li>CITTA, prima di una locazione: il gruppo entra in una città qualsiasi, in piedi; Olaf si separa e l'intermezzo del commiato;</li>
 * <li>RICOMPENSA, in locazione: il leggendario.</li>
 * </ol>
 * <p>
 * Le battute di Olaf, e il titolo della missione, sono scritte di proposito in modo buffo, e così vanno tradotte in ogni lingua. In italiano
 * la o in fondo a una parola diventa ø (la o barrata norvegese) e ogni d diventa ð (la lettera eth); in inglese, dove le o in fondo sono poche,
 * l'ultima o di ogni parola diventa ø e l'ultima d diventa ð (e "mug" è "müg"). Le produzioni di missioni.txt con "_VICHINGO" nel nome sono
 * sue, e il titolo, e la regola la controlla CoerenzaDeiTestiDelleStorieTest. Si fa una volta sola per partita (non ridefinisce
 * MissioneAPassi.isRipetibile, che è falso).
 */
public class IlViaggioDiOlaf extends MissioneAPassi implements ConLeggendario {

	/**
	 * Il giorno dell'incontro si sceglie fra questi due (il primo giorno della partita è l'1): dopo il viandante, che arriva al terzo o al quarto.
	 */
	public static final int GIORNO_MINIMO = 6;
	public static final int GIORNO_MASSIMO = 7;
	/**
	 * Il leggendario costruito al volo, quando il catalogo non ne ha più, è di questi livelli più di quello del mondo (come per LaSfidaDeiCampioni).
	 */
	private static final int LIVELLI_IN_PIU_DEL_LEGGENDARIO_COSTRUITO = 2;
	private static final int LIVELLO_MASSIMO_ARTEFATTO = 10;

	private static final String PARAMETRI = "PARAMETRI";
	private static final String INCONTRO = "INCONTRO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String CACCIA = "CACCIA";
	private static final String CITTA = "CITTA";
	private static final String RICOMPENSA = "RICOMPENSA";

	private static final String GIORNO = "GIORNO";
	private static final String LEGGENDARIO = "LEGGENDARIO";
	private static final String VICHINGO = "VICHINGO";

	public IlViaggioDiOlaf() {
		super(MissionePredefinita.IL_VIAGGIO_DI_OLAF);
	}

	/**
	 * Il giorno, da quello fissato in poi, in cui Olaf si fa incontrare; 0 finché non è fissato.
	 */
	public int getGiornoDellIncontro() {
		String giorno = getParametro(GIORNO);
		return giorno == null ? 0 : Integer.parseInt(giorno);
	}

	/**
	 * Il leggendario che Olaf lascia, o null finché non è stato scelto (all'incontro).
	 */
	@Override
	public OggettoLeggendario getLeggendario() {
		String riga = ottieniProprieta(LEGGENDARIO);
		return riga == null ? null : OggettoLeggendario.da(riga);
	}

	private static String getNomeDelVichingo() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_NOME");
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_TITOLO", valori());
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		if (!isAttiva()) {
			return "IL_VIAGGIO_DI_OLAF_DESCRIZIONE_ATTESA";
		}
		return CACCIA.equals(passo) ? "IL_VIAGGIO_DI_OLAF_DESCRIZIONE_CACCIA" : "IL_VIAGGIO_DI_OLAF_DESCRIZIONE_CITTA";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Tutti i segnaposto dei testi della storia: il nome del vichingo e, quando è scelto, il leggendario che lascia. Senza "private" per i test.
	 */
	Object[] valori() {
		OggettoLeggendario leggendario = getLeggendario();
		return new Object[] {"PARAM_VICHINGO", getNomeDelVichingo(),
				"PARAM_PREMIO", leggendario != null ? leggendario.getNomeBreve() : ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_UN_LEGGENDARIO")};
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	/**
	 * Si fissa una volta sola, all'inizio: il giorno dell'incontro.
	 */
	private Map<String, Supplier<String>> parametri() {
		Map<String, Supplier<String>> parametri = new LinkedHashMap<>();
		parametri.put(GIORNO, () -> String.valueOf(Dado.tira(GIORNO_MINIMO, GIORNO_MASSIMO)));
		return parametri;
	}

	/**
	 * Se è l'ora dell'incontro: dal giorno fissato in poi, entrando in un bosco, quando nessun'altra missione mostra un intermezzo
	 * (o subito, in un bosco, se la partenza è stata forzata dalla finestra di prova).
	 */
	private boolean isIncontroPossibile() {
		if (!haPostoNelGruppo()) {
			return false;
		}
		if (isInLuogoDellaPartenzaForzata()) {
			return true;
		}
		return LineaTemporale.getGiorno() >= getGiornoDellIncontro()
				&& Foresta.getLocazioneDelGruppo() == TipoLocazione.BOSCO
				&& isVisitaTranquilla();
	}

	/**
	 * Se per Olaf c'è posto, o si può fare: Olaf entra anche se il gruppo ha già i suoi cinque personaggi, ma in tutto non si superano gli
	 * otto, quindi a gruppo pieno serve qualcuno a tempo che se ne vada.
	 */
	private static boolean haPostoNelGruppo() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		return gruppo.getNumeroPersonaggi() < CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE
				|| gruppo.getPersonaggi().stream().anyMatch(Personaggio::isATempo);
	}

	/**
	 * Olaf, se è nel gruppo.
	 */
	private Optional<Personaggio> getOlaf() {
		String uuid = ottieniProprieta(VICHINGO);
		if (uuid == null) {
			return Optional.empty();
		}
		return GruppoGiocatore.getIstanza().getPersonaggi().stream()
				.filter(p -> uuid.equals(p.getModelloDati().getUuid()))
				.findFirst();
	}

	/**
	 * Il tempo di Olaf scade: se ne va dal gruppo, vivo o morto.
	 */
	private void facciScadereIlTempoDiOlaf() {
		getOlaf().ifPresent(olaf -> {
			olaf.setTempo(0);
			GruppoGiocatore.getIstanza().rimuoviPersonaggio(olaf);
		});
		rimuoviProprieta(VICHINGO);
	}

	@Override
	public void fallisciMissione() {
		facciScadereIlTempoDiOlaf();
		super.fallisciMissione();
	}

	/**
	 * Se il gruppo sta entrando in una città ancora in piedi.
	 */
	private static boolean isInUnaCitta() {
		TipoLocazione qui = Foresta.getLocazioneDelGruppo();
		return qui != null && qui.getCategoriaLocazione() == CategoriaLocazione.CITTA && !Foresta.isCittaDistrutta(qui);
	}

	/**
	 * Un leggendario del catalogo che nessuno ha ancora pescato; se non ne restano, uno costruito al volo (vedi LaSfidaDeiCampioni).
	 */
	private OggettoLeggendario scegliIlLeggendario() {
		if (PescaLeggendaria.restaUnLeggendario(PescaLeggendaria.giaPescati(this))) {
			return OggettoLeggendario.da(PescaLeggendaria.pesca(PescaLeggendaria.giaPescati(this), 0));
		}
		return LaSfidaDeiCampioni.costruisciUnLeggendario(Math.min(LIVELLO_MASSIMO_ARTEFATTO,
				Statistiche.getLivello() + LIVELLI_IN_PIU_DEL_LEGGENDARIO_COSTRUITO));
	}

	private void accetta() {
		aggiungiProprieta(LEGGENDARIO, scegliIlLeggendario().getRiga());
		GruppoGiocatore.getIstanza().faiPostoAUnoATempo();
		// Olaf arriva al livello del mondo, con l'equipaggiamento di quel livello: un compagno qualsiasi arriva anche due livelli sotto, ma lui è a
		// tempo e non cresce, quindi sarebbe carne da macello (vedi RegistroPersonaggi.preparaCompagno)
		Personaggio olaf = new Vichingo(getNomeDelVichingo(), 1);
		olaf.impostaLivelloDopoReclutamento(Statistiche.getLivello());
		EquipaggiamentoIniziale.equipaggia(olaf, Statistiche.getLivello());
		olaf.setMotivoNelGruppo(TipoMotivoNelGruppo.MISSIONE);
		// Il tempo non scorre: lo fa scadere la missione. Non guadagna esperienza e non sale di livello
		olaf.setTempo(1);
		GruppoGiocatore.getIstanza().aggiungiPersonaggio(olaf);
		aggiungiProprieta(VICHINGO, olaf.getModelloDati().getUuid());
		BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_ACCETTATO", valori())));
		attivaMissione();
	}

	/**
	 * Se nel gruppo c'è il bardo delle locande, quello della festa in maschera (un ospite, mentre lo si riporta a casa).
	 */
	private static boolean ceIlBardo() {
		return GruppoGiocatore.getIstanza().getOspiti().stream()
				.anyMatch(ospite -> ospite.isVivo() && ospite.getClasse() == TipoPersonaggio.BARDO_DELLE_LOCANDE);
	}

	/**
	 * La scena nel bosco: Olaf è già lì, il gruppo arriva (vedi ScenaNelBosco). Se il bardo è nel gruppo Olaf lo riconosce; se il gruppo ha
	 * più di un personaggio la battuta su Sglord è di un compagno del capo, altrimenti del capo.
	 */
	private List<PaginaIntermezzo> getPagineDellIncontro() {
		ScenaNelBosco scena = ScenaNelBosco.con(TipoPersonaggio.VICHINGO)
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_CAPO_1"))
				.parlaChiIncontrato(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_VICHINGO_1", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_CAPO_2"))
				.parlaChiIncontrato(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_VICHINGO_2"));
		if (ceIlBardo()) {
			scena.parlaChiIncontrato(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_VICHINGO_BARDO"));
		}
		return scena
				.parlaUnCompagno(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_SGLORD"))
				.parlaChiIncontrato(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_VICHINGO_3"))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_CAPO_3"))
				.getPagine();
	}

	/**
	 * Il commiato in città: Olaf, che a questo punto non è più tra gli ospiti, saluta il gruppo.
	 */
	private List<PaginaIntermezzo> getPagineDelCommiato() {
		return ScenaInCitta.con(TipoPersonaggio.VICHINGO)
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_VICHINGO_COMMIATO"))
				.getPagine();
	}

	@Override
	protected String passoIniziale() {
		return PARAMETRI;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case PARAMETRI:
				return generaParametri(MomentoControllo.PRE_LOCAZIONE, parametri()).poi(INCONTRO);
			case INCONTRO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::isIncontroPossibile)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDellIncontro)
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true).esegui(this::accetta).poi(CACCIA);
			case CACCIA:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE,
								() -> getConteggioNelPassoCorrente(eventoSconfitto(TipoPersonaggio.VIVERNA)) >= 1
										|| getConteggioNelPassoCorrente(eventoSconfitto(TipoPersonaggio.CHIMERA_DRAGO)) >= 1)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_CACCIA_FINITA", valori()))))
						.poi(CITTA);
			case CITTA:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, IlViaggioDiOlaf::isInUnaCitta)
						// Prima della scena, così Olaf non compare due volte: tra chi saluta e tra chi arriva
						.esegui(this::facciScadereIlTempoDiOlaf)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDelCommiato)
						.poi(RICOMPENSA);
			case RICOMPENSA:
				return ricompensa(MomentoControllo.IN_LOCAZIONE,
								Ricompensa.nulla().conArtefatto(() -> getLeggendario().costruisci(Statistiche.getLivello())),
								() -> ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DI_OLAF_RICOMPENSA", valori()))
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
