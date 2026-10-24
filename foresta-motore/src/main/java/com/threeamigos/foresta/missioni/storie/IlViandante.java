package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.BattutaIntermezzo;
import com.threeamigos.foresta.intermezzi.ElementoIntermezzo;
import com.threeamigos.foresta.intermezzi.ImmagineIntermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.Tappa;
import com.threeamigos.foresta.intermezzi.Verso;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Ricompensa;
import com.threeamigos.foresta.missioni.richieste.ViandanteRichiesto;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Dopo due o tre giorni di cammino, entrando in un bosco, il gruppo incontra per caso un viandante che chiede un favore: andare
 * in una certa città, sconfiggere dei mostri che le danno noia (vedi {@link ViandanteRichiesto}, da missioni.txt) e tornare da lui,
 * nel bosco dove si sono incontrati. Il giorno dell'incontro, la richiesta, quanti mostri e la città si fissano subito, all'inizio
 * della partita, e restano quelli anche dopo un salvataggio.
 * <ol>
 * <li>PARAMETRI, prima della prima locazione: si fissano giorno, richiesta, quantità e città (vedi
 * {@link #generaParametri});</li>
 * <li>INCONTRO, prima di una locazione di bosco, dal giorno fissato in poi e a una visita tranquilla: l'intermezzo del viandante;</li>
 * <li>ACCETTAZIONE, in locazione, nel bosco: la missione si attiva e si ricorda il punto di partenza;</li>
 * <li>ANDATA, prima di una locazione: il gruppo arriva nella città (se nel frattempo viene distrutta la missione fallisce);</li>
 * <li>CACCIA, dopo ogni locazione: sconfitti i mostri, dovunque, da quando il gruppo è in città;</li>
 * <li>RITORNO, prima di una locazione: il gruppo torna al punto di partenza, e l'intermezzo del ringraziamento;</li>
 * <li>RICOMPENSA, in locazione: le monete, tante per ogni mostro sconfitto e per ogni livello del mondo di quando si è
 * incontrato il viandante.</li>
 * </ol>
 */
public class IlViandante extends MissioneAPassi {

	/**
	 * Il giorno dell'incontro si sceglie fra questi due (il primo giorno della partita è l'1): dopo due o tre giorni di cammino.
	 */
	public static final int GIORNO_MINIMO = 3;
	public static final int GIORNO_MASSIMO = 4;

	private static final String PARAMETRI = "PARAMETRI";
	private static final String INCONTRO = "INCONTRO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String ANDATA = "ANDATA";
	private static final String CACCIA = "CACCIA";
	private static final String RITORNO = "RITORNO";
	private static final String RICOMPENSA = "RICOMPENSA";

	private static final String GIORNO = "GIORNO";
	private static final String RIGA = "RIGA";
	private static final String QUANTITA = "QUANTITA";
	private static final String CITTA = "CITTA";
	private static final String LIVELLO_DELL_INCONTRO = "LIVELLO_DELL_INCONTRO";

	private static final String ID_EROE = "eroe";
	private static final String ID_VIANDANTE = "viandante";

	public IlViandante() {
		super(MissionePredefinita.IL_VIANDANTE);
	}

	/**
	 * Il giorno, da quello fissato in poi, in cui il viandante si fa incontrare; 0 finché non è fissato.
	 */
	public int getGiornoDellIncontro() {
		String giorno = getParametro(GIORNO);
		return giorno == null ? 0 : Integer.parseInt(giorno);
	}

	/**
	 * Che cosa chiede il viandante, o null finché non è fissato.
	 */
	public ViandanteRichiesto getRichiesta() {
		String riga = getParametro(RIGA);
		return riga == null ? null : ViandanteRichiesto.da(riga);
	}

	/**
	 * Quanti mostri chiede di sconfiggere; 0 finché non è fissato.
	 */
	public int getQuantita() {
		String quantita = getParametro(QUANTITA);
		return quantita == null ? 0 : Integer.parseInt(quantita);
	}

	/**
	 * La città in cui il gruppo deve andare, o null finché non è fissata.
	 */
	public TipoLocazione getCitta() {
		String citta = getParametro(CITTA);
		return citta == null ? null : TipoLocazione.valueOf(citta);
	}

	/**
	 * Quante monete paga il viandante alla fine: quelle di ogni mostro, per ogni mostro, per il livello del mondo di quando si è
	 * incontrato il viandante (finché non si è incontrato, quello di adesso).
	 */
	public int getMonete() {
		ViandanteRichiesto richiesta = getRichiesta();
		return richiesta == null ? 0 : richiesta.getMonete() * getQuantita() * getLivelloDellIncontro();
	}

	private int getLivelloDellIncontro() {
		String livello = ottieniProprieta(LIVELLO_DELL_INCONTRO);
		return livello != null ? Integer.parseInt(livello) : Statistiche.getLivello();
	}

	private String nomeDellaCitta() {
		TipoLocazione citta = getCitta();
		return citta == null ? "" : citta.getNomeDellaCitta();
	}

	@Override
	public String getNome() {
		return "Il viandante nel bosco";
	}

	@Override
	public String getDescrizione() {
		ViandanteRichiesto richiesta = getRichiesta();
		if (richiesta == null || !isAttiva()) {
			return "Prima o poi, nei boschi, si incontra qualcuno che ha bisogno di aiuto.";
		}
		String mostri = Numeri.getCardinaleM(getQuantita()) + " " + richiesta.getPlurale();
		switch (getPassoCorrente()) {
			case CACCIA:
				return "Sei a " + nomeDellaCitta() + ": sconfiggi " + mostri + ", dovunque. Finora: "
						+ getConteggioNelPassoCorrente(eventoSconfitto(richiesta.getNemico())) + ".";
			case RITORNO:
				return "I mostri sono sconfitti: torna dal viandante, nel bosco dove l'hai incontrato.";
			default:
				return "Un viandante incontrato nel bosco ti ha chiesto di andare a " + nomeDellaCitta() + ", di sconfiggere "
						+ mostri + " e di tornare da lui, dove l'hai incontrato.";
		}
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	/**
	 * Si fissa una volta sola, all'inizio: il giorno dell'incontro, la richiesta, quanti mostri e una città in piedi.
	 */
	private Map<String, Supplier<String>> parametri() {
		Map<String, Supplier<String>> parametri = new LinkedHashMap<>();
		parametri.put(GIORNO, () -> String.valueOf(Dado.tira(GIORNO_MINIMO, GIORNO_MASSIMO)));
		parametri.put(RIGA, () -> ProduttoreDiTestiCasuale.rigaDiMissioni("VIANDANTE"));
		// La riga è già stata fissata: generaParametri le scrive nell'ordine in cui le trova
		parametri.put(QUANTITA, () -> {
			ViandanteRichiesto richiesta = getRichiesta();
			return String.valueOf(Dado.tira(richiesta.getQuantitaMinima(), richiesta.getQuantitaMassima()));
		});
		parametri.put(CITTA, () -> sceglieUnaCitta().name());
		return parametri;
	}

	/**
	 * Una città a caso fra quelle ancora in piedi.
	 */
	private static TipoLocazione sceglieUnaCitta() {
		List<TipoLocazione> citta = new ArrayList<>();
		for (TipoLocazione tipo : TipoLocazione.values()) {
			if (tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA && Foresta.getCoordinateLocazioneUnica(tipo) != null
					&& !LineaTemporale.isCittaDistrutta(tipo)) {
				citta.add(tipo);
			}
		}
		return citta.get(Dado.tiraAncheAUnaFaccia(citta.size()) - 1);
	}

	/**
	 * Se è l'ora dell'incontro: dal giorno fissato in poi, entrando in un bosco, quando nessun'altra missione mostra un intermezzo
	 * (o subito, in un bosco, se la partenza è stata forzata dalla finestra di prova).
	 */
	private boolean isIncontroPossibile() {
		// Dalla finestra di prova si può forzare: allora basta essere in un bosco
		if (isInLuogoDellaPartenzaForzata()) {
			return true;
		}
		return LineaTemporale.getGiorno() >= getGiornoDellIncontro()
				&& GruppoGiocatore.getIstanza().getTipoLocazioneCorrente() == TipoLocazione.BOSCO
				&& isVisitaTranquilla();
	}

	private boolean isCittaDistrutta() {
		TipoLocazione citta = getCitta();
		return citta != null && LineaTemporale.isCittaDistrutta(citta);
	}

	private void accetta() {
		aggiungiProprieta(LIVELLO_DELL_INCONTRO, String.valueOf(Statistiche.getLivello()));
		ViandanteRichiesto richiesta = getRichiesta();
		BusEventi.pubblica(new NotificaTestoParagrafo("Il viandante vi chiede di andare a " + nomeDellaCitta() + ", di sconfiggere "
				+ Numeri.getCardinaleM(getQuantita()) + " " + richiesta.getPlurale() + " e di tornare da lui. Vi pagherà "
				+ getMonete() + " monete."));
		attivaMissione();
	}

	/**
	 * La scena nel bosco: l'eroe a sinistra, il viandante che arriva da destra e si ferma davanti a lui. Il testo in alto usa
	 * l'alfabeto grande, senza accenti.
	 */
	private PaginaIntermezzo scenaNelBosco(String testo) {
		return new PaginaIntermezzo(testo)
				.conSfondo(ImmagineIntermezzo.locazione(TipoLocazione.BOSCO))
				.conElemento(ElementoIntermezzo.personaggio(ID_VIANDANTE, TipoPersonaggio.VIANDANTE, 1.1, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.SINISTRA)
						.poi(Tappa.inSecondi(2).verso(0.62, 0.61)))
				.conElemento(ElementoIntermezzo.personaggio(ID_EROE, GruppoGiocatore.getIstanza().getCapo().getClasse(), 0.38, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.DESTRA));
	}

	private List<PaginaIntermezzo> getPagineDellIncontro() {
		ViandanteRichiesto richiesta = getRichiesta();
		return Collections.singletonList(scenaNelBosco("Un viandante nel bosco.")
				.conBattuta(BattutaIntermezzo.di(ID_VIANDANTE, richiesta.getRichiesta(nomeDellaCitta())).daSecondo(2))
				.conBattuta(BattutaIntermezzo.di(ID_EROE, richiesta.getBattutaDelCapo()))
				.conBattuta(BattutaIntermezzo.di(ID_VIANDANTE, richiesta.getRisposta())));
	}

	private List<PaginaIntermezzo> getPagineDelRitorno() {
		return Collections.singletonList(scenaNelBosco("Il viandante aspettava.")
				.conBattuta(BattutaIntermezzo.di(ID_VIANDANTE, getRichiesta().getRitorno()).daSecondo(2))
				.conBattuta(BattutaIntermezzo.di(ID_EROE, "Tutto a posto. Buona strada.")));
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
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true).esegui(this::accetta).poi(ANDATA);
			case ANDATA:
				return vai(MomentoControllo.PRE_LOCAZIONE, getCitta())
						.falliscoSe(this::isCittaDistrutta, () -> nomeDellaCitta() + " è stata distrutta: il viandante non rivedrà mai più nessuno di laggiù.")
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(getRichiesta().getArrivo(nomeDellaCitta()))))
						.poi(CACCIA);
			case CACCIA:
				return sconfiggi(MomentoControllo.POST_LOCAZIONE, getRichiesta().getNemico(), getQuantita())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Fatto: ora il viandante aspetta, nel bosco dove vi ha fermato.")))
						.poi(RITORNO);
			case RITORNO:
				return tornaAlPuntoDiPartenza(MomentoControllo.PRE_LOCAZIONE)
						.segnala(this::getPuntoDiPartenza)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDelRitorno)
						.poi(RICOMPENSA);
			case RICOMPENSA:
				return ricompensa(MomentoControllo.IN_LOCAZIONE, Ricompensa.inMonete(getMonete()),
						() -> "Il viandante vi paga le " + getMonete() + " monete promesse.")
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
