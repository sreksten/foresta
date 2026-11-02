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
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;

import java.util.List;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

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
		DocumentazioneRichiesta documentazione = getDocumentazione();
		List<Indizio> reperti = documentazione.getReperti();
		int trovati = getRepertiTrovati();
		StringBuilder descrizione = new StringBuilder();
		if (RITORNO.equals(getPassoCorrente())) {
			descrizione.append(Testi.formatta("missioni", "LaDocumentazione.s1")).append(Lingue.corrente().conPreposizione(Preposizione.DA, mandante(documentazione.getMandante())))
					.append(" a ").append(getNomeCitta()).append(Testi.formatta("missioni", "LaDocumentazione.s2"));
		} else {
			descrizione.append(Lingue.corrente().inizialeMaiuscola(getMandanteDiCitta())).append(Testi.formatta("missioni", "LaDocumentazione.s3"))
					.append(Lingue.corrente().cardinale(reperti.size(), Genere.MASCHILE)).append(Testi.formatta("missioni", "LaDocumentazione.s4"));
		}
		for (int i = 0; i < trovati; i++) {
			descrizione.append(" ").append(i + 1).append(": ").append(reperti.get(i).getTesto());
		}
		return descrizione.toString();
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
						BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", numero == 1 ? "LaDocumentazione.s5.comincia" : "LaDocumentazione.s5.prosegue", TestiDeiLuoghi.dentro(reperto.getLuogo()))));
					})
					.poi(REPERTO + numero);
		}
		if (id.startsWith(REPERTO)) {
			int numero = Integer.parseInt(id.substring(REPERTO.length()));
			boolean ultimo = numero == reperti.size();
			return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nelPosto)
					.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(reperti.get(numero - 1).getTesto()
							+ (ultimo ? Testi.formatta("missioni", "LaDocumentazione.s6", mandante(documentazione.getMandante()), getNomeCitta()) : ""))))
					.poi(ultimo ? RITORNO : TRACCIA + (numero + 1));
		}
		throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		DocumentazioneRichiesta documentazione = getDocumentazione();
		return documentazione.getAspetto().nuovaScena()
				.parlaIlMandante(documentazione.getRichiesta())
				.parlaIlMandante(Testi.formatta("missioni", "LaDocumentazione.1", getRicompensa()))
				.parlaIlCapo(documentazione.getBattutaDelCapo())
				.parlaIlMandante(documentazione.getRisposta());
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getDocumentazione().getAspetto().nuovaScena()
				.parlaIlMandante(getDocumentazione().getRingraziamento())
				.parlaIlMandante(Testi.formatta("missioni", "LaDocumentazione.2", getRicompensa()));
	}

	@Override
	protected String testoAccettazione() {
		return Testi.formatta("missioni", "LaDocumentazione.t1", Lingue.corrente().inizialeMaiuscola(getMandanteDiCitta()), getRicompensa(), Lingue.corrente().cardinale(getDocumentazione().getReperti().size(), Genere.MASCHILE));
	}

	@Override
	protected String testoRicompensa() {
		return Testi.formatta("missioni", "LaDocumentazione.t2", Lingue.corrente().inizialeMaiuscola(mandante(getDocumentazione().getMandante())), getRicompensa());
	}

	@Override
	protected int getRicompensaBase() {
		return getDocumentazione().getMonete();
	}
}
