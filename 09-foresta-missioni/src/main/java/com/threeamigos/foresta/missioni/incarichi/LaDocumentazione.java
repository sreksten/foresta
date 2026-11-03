package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.TestiDeiLuoghi;
import com.threeamigos.foresta.missioni.richieste.DocumentazioneRichiesta;
import com.threeamigos.foresta.missioni.richieste.IndagineRichiesta.Indizio;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.testi.RegoleDellaLingua;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;

import java.util.List;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Genere;

/**
 * In città uno studioso chiede di documentare qualcosa nella foresta: copiare le iscrizioni di più rovine, descrivere
 * gli affreschi di un tempio, catalogare dei reperti (vedi {@link DocumentazioneRichiesta}, da missioni.txt). I posti,
 * due o tre, si segnano sulla mappa uno alla volta, come gli indizi di un'indagine (vedi LIndagine); in ognuno si
 * scrive che cosa si è trovato, e resta nella descrizione della missione. Finito il giro, si torna dallo studioso a
 * consegnare le note e a riscuotere (vedi IncaricoInCitta). La documentazione si pesca quando si offre: la missione si
 * ripete con altre.
 * <ol>
 * <li>TRACCIA_n, in locazione: il posto n compare sulla mappa (il primo nella città, gli altri nel posto prima);</li>
 * <li>REPERTO_n, in locazione, nel posto: che cosa si è trovato.</li>
 * </ol>
 */
public class LaDocumentazione extends IncaricoInCitta {

	public static final String DOCUMENTAZIONE = "DOCUMENTAZIONE";
	private static final String TRACCIA = "TRACCIA_";
	private static final String REPERTO = "REPERTO_";

	public LaDocumentazione() {
		super(MissionePredefinita.LA_DOCUMENTAZIONE);
	}

	public DocumentazioneRichiesta getDocumentazione() {
		return DocumentazioneRichiesta.da(parametro(DOCUMENTAZIONE, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni(DOCUMENTAZIONE)));
	}

	/**
	 * Il posto da documentare, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getPosto() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Quanti posti il gruppo ha già documentato: nessuno all'incarico, tutti al ritorno.
	 */
	public int getRepertiTrovati() {
		String passo = getPassoCorrente();
		if (passo.startsWith(TRACCIA)) {
			return Integer.parseInt(passo.substring(TRACCIA.length())) - 1;
		}
		if (passo.startsWith(REPERTO)) {
			return Integer.parseInt(passo.substring(REPERTO.length())) - 1;
		}
		return isAttiva() ? getDocumentazione().getReperti().size() : 0;
	}

	private boolean nelPosto() {
		return getPosto() != null && getPosto().equals(Foresta.getPosizioneDelGruppo());
	}

	private String getMandanteDiCitta() {
		return mandanteDiCitta(getDocumentazione().getMandante());
	}

	@Override
	protected void allIncarico() {
		getDocumentazione();
	}

	@Override
	public String getNome() {
		return getDocumentazione().getTitolo();
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return RITORNO.equals(passo) ? "LA_DOCUMENTAZIONE_DESCRIZIONE_RITORNO" : "LA_DOCUMENTAZIONE_DESCRIZIONE";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Le note dei reperti già trovati, in fila (" 1: nota 2: nota"): l'unico valore che cambia mentre la missione dura.
	 */
	@Override
	protected Object[] valoriDellaDescrizione() {
		List<Indizio> reperti = getDocumentazione().getReperti();
		StringBuilder trovati = new StringBuilder();
		for (int i = 0; i < getRepertiTrovati(); i++) {
			trovati.append(" ").append(i + 1).append(": ").append(reperti.get(i).getTesto());
		}
		return new Object[] {"PARAM_REPERTI", trovati.toString()};
	}

	/**
	 * Tutti i segnaposto dei testi della missione che non cambiano mentre dura: il mandante nelle sue forme, il numero dei posti, la
	 * città e le monete.
	 */
	private Object[] valori() {
		DocumentazioneRichiesta documentazione = getDocumentazione();
		RegoleDellaLingua lingua = Lingue.corrente();
		String mandante = mandante(documentazione.getMandante());
		return new Object[] {
				"PARAM_NUMERO", lingua.cardinale(documentazione.getReperti().size(), Genere.MASCHILE),
				"PARAM_CITTA", getNomeCitta(),
				"PARAM_MONETE", getRicompensa(),
				"PARAM_MANDANTE", mandante,
				"PARAM_MANDANTE_MAIUSC", lingua.inizialeMaiuscola(mandante),
				"PARAM_MANDANTE_DI_CITTA_MAIUSC", lingua.inizialeMaiuscola(getMandanteDiCitta()),
				"PARAM_DA_MANDANTE", lingua.conPreposizione(Preposizione.DA, mandante)};
	}

	@Override
	protected String primoPassoDelCompito() {
		return TRACCIA + 1;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		DocumentazioneRichiesta documentazione = getDocumentazione();
		List<Indizio> reperti = documentazione.getReperti();
		if (id.startsWith(TRACCIA)) {
			int numero = Integer.parseInt(id.substring(TRACCIA.length()));
			Indizio reperto = reperti.get(numero - 1);
			return cercaLocazione(MomentoControllo.IN_LOCAZIONE, reperto.getLuogo())
					.esegui(() -> {
						Foresta.setLocazioneConosciuta(getPosto());
						BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione(numero == 1 ? "LA_DOCUMENTAZIONE_COMINCIA" : "LA_DOCUMENTAZIONE_PROSEGUE", "PARAM_DENTRO_LUOGO", TestiDeiLuoghi.dentro(reperto.getLuogo()))));
					})
					.poi(REPERTO + numero);
		}
		if (id.startsWith(REPERTO)) {
			int numero = Integer.parseInt(id.substring(REPERTO.length()));
			boolean ultimo = numero == reperti.size();
			return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nelPosto)
					.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(reperti.get(numero - 1).getTesto()
							+ (ultimo ? " " + ProduttoreDiMissioniCasuale.testoDellaMissione("LA_DOCUMENTAZIONE_NOTE_COMPLETE", valori()) : ""))))
					.poi(ultimo ? RITORNO : TRACCIA + (numero + 1));
		}
		throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		DocumentazioneRichiesta documentazione = getDocumentazione();
		return documentazione.getAspetto().nuovaScena()
				.parlaIlMandante(documentazione.getRichiesta())
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_DOCUMENTAZIONE_INCARICO_1", valori()))
				.parlaIlCapo(documentazione.getBattutaDelCapo())
				.parlaIlMandante(documentazione.getRisposta());
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getDocumentazione().getAspetto().nuovaScena()
				.parlaIlMandante(getDocumentazione().getRingraziamento())
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_DOCUMENTAZIONE_RINGRAZIAMENTO_2", valori()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_DOCUMENTAZIONE_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_DOCUMENTAZIONE_RICOMPENSA", valori());
	}

	@Override
	protected int getRicompensaBase() {
		return getDocumentazione().getMonete();
	}
}
