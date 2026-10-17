package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.OggettiDaRaccogliere;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
 * Un incarico si prende in una città qualsiasi, ma solo a una visita tranquilla, in cui nessun'altra missione mostra un
 * intermezzo entrando in città: non alla prima visita, quando parte la missione della città, né quando si torna a
 * concluderne una. Se nella stessa visita ne sono pronti più d'uno, ne parte uno solo, scelto a caso con più probabilità
 * fra quelli non ancora affidati (vedi SceltaDegliIncarichi); gli altri aspettano.
 * <p>
 * Gli incarichi sono ripetibili (vedi MissioneAPassi.isRipetibile): finiti, bene o male, ne lasciano uno nuovo uguale,
 * che si può prendere dopo una pausa.
 */
public abstract class IncaricoInCitta extends MissioneAPassi {

	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	protected static final String RITORNO = "RITORNO";
	private static final String CONSEGNA = "CONSEGNA";
	private static final String RICOMPENSA = "RICOMPENSA";
	private static final String CITTA = "CITTA";
	private static final String LIVELLO_DELL_INCARICO = "LIVELLO_DELL_INCARICO";
	private static final String PREFISSO_CITTA = "CITTA_";

	protected IncaricoInCitta(MissionePredefinita classe) {
		super(classe);
	}

	/**
	 * La scena in cui il mandante chiede l'incarico.
	 */
	protected abstract ScenaInCitta scenaIncarico();

	/**
	 * La scena in cui il mandante ringrazia, quando il gruppo torna a riscuotere.
	 */
	protected abstract ScenaInCitta scenaRingraziamento();

	/**
	 * Il testo che si legge quando il gruppo accetta l'incarico.
	 */
	protected abstract String testoAccettazione();

	/**
	 * Il testo che si legge quando il gruppo riceve la ricompensa.
	 */
	protected abstract String testoRicompensa();

	/**
	 * La paga base, per il primo livello del mondo; la paga vera la moltiplica per il livello (vedi {@link #getRicompensa()}).
	 */
	protected abstract int getRicompensaBase();

	/**
	 * Quanto paga l'incarico: la paga base (quella che la missione scrive, per il primo livello del mondo) per il
	 * livello del mondo di quando l'incarico si è offerto. Si fissa allora, così la cifra promessa nelle scene e
	 * quella pagata a fine lavoro sono la stessa anche se nel frattempo il livello sale; prima che l'incarico si offra
	 * vale quello di adesso.
	 */
	protected final int getRicompensa() {
		return getRicompensaBase() * getLivelloDellIncarico();
	}

	/**
	 * Il livello del mondo di quando l'incarico si è offerto; finché non si è offerto, quello di adesso.
	 */
	protected final int getLivelloDellIncarico() {
		String livello = ottieniProprieta(LIVELLO_DELL_INCARICO);
		return livello != null ? Integer.parseInt(livello) : Statistiche.getLivello();
	}

	/**
	 * L'identificativo del primo passo del compito, quello che viene dopo l'accettazione.
	 */
	protected abstract String primoPassoDelCompito();

	/**
	 * Costruisce uno dei passi del compito: l'ultimo deve proseguire con il ritorno nella città.
	 */
	protected abstract Passo costruisciPassoDelCompito(String id);

	/**
	 * Gli oggetti che il gruppo consegna al ritorno, prima della ricompensa; null se non ce ne sono.
	 */
	protected OggettiDaRaccogliere getOggettiDaConsegnare() {
		return null;
	}

	/**
	 * Il testo che si legge quando il gruppo consegna gli oggetti richiesti; vuoto se non ce ne sono.
	 */
	protected String testoConsegna() {
		return "";
	}

	/**
	 * La città in cui si torna a riscuotere: quella dell'incarico, se non è un'altra (vedi IlCorriere).
	 */
	protected TipoLocazione getCittaDelRitorno() {
		return getCitta();
	}

	/**
	 * Se l'incarico si può offrire nella città in cui è il gruppo (per esempio se c'è un'altra città a cui portare
	 * qualcosa). 
	 */
	protected boolean isPossibileQui() {
		return true;
	}

	@Override
	protected boolean isRipetibile() {
		return true;
	}

	/**
	 * Quando l'incarico si offre, prima dell'intermezzo del mandante: il momento di pescare i nomi dei personaggi
	 * (vedi MissioneAPassi.parametro), che l'intermezzo userà.
	 */
	protected void allIncarico() {
	}

	/**
	 * Il testo che si legge se la città in cui si riscuote viene distrutta, e l'incarico fallisce.
	 */
	protected String testoCittaDistrutta() {
		return nomeDellaCitta(getCittaDelRitorno()) + " è stata distrutta: nessuno potrà più pagare per l'incarico.";
	}

	/**
	 * La città dell'incarico, o null finché il gruppo non ne ha trovata una.
	 */
	public final TipoLocazione getCitta() {
		String citta = ottieniProprieta(CITTA);
		return citta == null ? null : TipoLocazione.valueOf(citta);
	}

	/**
	 * Il nome della città dell'incarico, "Ruuna"; vuoto finché non c'è.
	 */
	protected final String getNomeCitta() {
		return nomeDellaCitta(getCitta());
	}

	/**
	 * I mandanti che nel mondo hanno un nome proprio (vedi RegistroPersonaggi e tipi.PersonaggioNoto), come li scrivono le righe
	 * delle missioni. Il locandiere non c'è: il suo nome sta nella locanda.
	 */
	private static final Map<String, PersonaggioNoto> MANDANTI_CON_NOME = new HashMap<>();

	static {
		MANDANTI_CON_NOME.put("il capitano delle guardie", PersonaggioNoto.CAPITANO);
		MANDANTI_CON_NOME.put("l'alchimista", PersonaggioNoto.ALCHIMISTA);
		MANDANTI_CON_NOME.put("l'armaiolo", PersonaggioNoto.ARMAIOLO);
		MANDANTI_CON_NOME.put("il venditore di pergamene", PersonaggioNoto.VENDITORE_DI_PERGAMENE);
		MANDANTI_CON_NOME.put("l'incantatore", PersonaggioNoto.INCANTATORE);
		MANDANTI_CON_NOME.put("il borgomastro", PersonaggioNoto.BORGOMASTRO);
		MANDANTI_CON_NOME.put("la levatrice", PersonaggioNoto.LEVATRICE);
	}

	/**
	 * Il mandante come lo dice la riga della missione, ma se ha un nome proprio (il capitano delle guardie, l'alchimista,
	 * l'armaiolo, il borgomastro...) lo scrive con il nome, fra virgole: "il capitano delle guardie, Rodolfo,". Gli altri
	 * mandanti restano come sono.
	 */
	protected final String mandante(String mandante) {
		return conNome(mandante, mandante, getCitta(), true);
	}

	/**
	 * Come {@link #mandante}, ma senza la virgola finale: per quando il mandante è seguito da un segno di punteggiatura.
	 */
	protected final String mandanteSenzaVirgolaFinale(String mandante) {
		return conNome(mandante, mandante, getCitta(), false);
	}

	/**
	 * Il mandante e la città dell'incarico: "il fabbro di Ruuna", "l'armaiolo di Ruuna, Mastro Ferruccio,".
	 */
	protected final String mandanteDiCitta(String mandante) {
		return conNome(mandante, mandante + " di " + getNomeCitta(), getCitta(), true);
	}

	/**
	 * Come {@link #mandanteDiCitta}, ma senza la virgola finale: per quando il mandante è seguito da un segno di punteggiatura.
	 */
	protected final String mandanteDiCittaSenzaVirgolaFinale(String mandante) {
		return conNome(mandante, mandante + " di " + getNomeCitta(), getCitta(), false);
	}

	/**
	 * Come {@link #mandante}, ma di una città qualunque, per esempio quella a cui si porta qualcosa.
	 */
	protected final String mandanteIn(String mandante, TipoLocazione citta) {
		return conNome(mandante, mandante, citta, true);
	}

	/**
	 * Come {@link #mandanteIn}, ma senza la virgola finale.
	 */
	protected final String mandanteInSenzaVirgolaFinale(String mandante, TipoLocazione citta) {
		return conNome(mandante, mandante, citta, false);
	}

	private static String conNome(String mandante, String testo, TipoLocazione citta, boolean virgolaFinale) {
		String nome = nomeDelMandante(mandante, citta);
		if (nome == null) {
			return testo;
		}
		return testo + ", " + nome + (virgolaFinale ? "," : "");
	}

	/**
	 * Il nome proprio del mandante in quella città, o null se non ne ha uno nel registro (o la città non c'è).
	 */
	private static String nomeDelMandante(String mandante, TipoLocazione citta) {
		PersonaggioNoto noto = MANDANTI_CON_NOME.get(mandante);
		if (noto == null) {
			return null;
		}
		if (noto.getAmbito() == PersonaggioNoto.Ambito.PER_CITTA) {
			return citta == null ? null : RegistroPersonaggi.getResidente(noto, citta).getNome();
		}
		return RegistroPersonaggi.getResidente(noto).getNome();
	}

	/**
	 * Il nome proprio del mandante nella città dell'incarico, o null se non ne ha uno nel registro o la città non c'è.
	 */
	protected final String nomeDelMandante(String mandante) {
		return nomeDelMandante(mandante, getCitta());
	}

	/**
	 * Il nome di una città, "Ruuna"; vuoto se non c'è.
	 */
	protected static String nomeDellaCitta(TipoLocazione citta) {
		if (citta == null) {
			return "";
		}
		String nome = citta.name().substring(PREFISSO_CITTA.length()).toLowerCase(Locale.ROOT);
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
							SceltaDegliIncarichi.affidato(this);
							aggiungiProprieta(CITTA, gruppo.getTipoLocazioneCorrente().name());
							aggiungiProprieta(LIVELLO_DELL_INCARICO, String.valueOf(Statistiche.getLivello()));
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
	 * Se l'incarico si offre adesso: è pronto (vedi {@link #isProntoPerOffrirsi()}) e, se anche altri lo sono, è quello
	 * scelto (vedi SceltaDegliIncarichi).
	 */
	private boolean isIncaricoDaOffrire() {
		return isProntoPerOffrirsi() && SceltaDegliIncarichi.tocca(this);
	}

	/**
	 * Se il gruppo è dove l'incarico si offre: in una città qualsiasi a una visita tranquilla, e passata la pausa dopo
	 * l'incarico precedente.
	 */
	final boolean isProntoPerOffrirsi() {
		return isDisponibile() && inUnaCitta() && isPossibileQui() && isVisitaTranquilla();
	}

	/**
	 * Se l'incarico non si è ancora offerto: è al primo passo e non è finito.
	 */
	final boolean isInAttesaDiOffrirsi() {
		return !isAttiva() && !isCompleta() && !isFallita() && INCARICO.equals(getPassoCorrente());
	}

	/**
	 * Il giro in cui l'incarico è stato affidato (vedi SceltaDegliIncarichi), o -1 se non si è ancora offerto.
	 */
	final int getGiro() {
		String giro = ottieniProprieta(SceltaDegliIncarichi.GIRO);
		return giro == null ? -1 : Integer.parseInt(giro);
	}

	/**
	 * Se l'incarico, quando è pronto insieme ad altri, è scelto a caso con loro (vedi SceltaDegliIncarichi): sì, salvo
	 * gli incarichi che sono una storia sola legata a una città, che si offrono appena possono.
	 */
	protected boolean partecipaAlSorteggio() {
		return true;
	}

	/**
	 * Se l'incarico potrà ancora offrirsi in questa partita, in qualche città e prima o poi: un incarico che non potrà
	 * più farlo non tiene aperto il giro del sorteggio (vedi SceltaDegliIncarichi).
	 */
	protected boolean puoAncoraOffrirsi() {
		return true;
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	private boolean inUnaCitta() {
		TipoLocazione tipo = GruppoGiocatore.getIstanza().getTipoLocazioneCorrente();
		return tipo != null && tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA
				&& !LineaTemporale.isCittaDistrutta(tipo);
	}

	/**
	 * Dice se il gruppo è nella città in cui l'incarico si offre.
	 */
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
