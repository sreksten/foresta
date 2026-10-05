package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IndagineRichiesta.Indizio;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tools.Misc;

import java.util.List;

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
		super(ClasseMissione.LA_DOCUMENTAZIONE);
	}

	public DocumentazioneRichiesta getDocumentazione() {
		return DocumentazioneRichiesta.da(parametro(DOCUMENTAZIONE, () -> ProduttoreDiTestiCasuale.rigaDiMissioni(DOCUMENTAZIONE)));
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
		return getPosto() != null && getPosto().equals(GruppoGiocatore.getIstanza().getCoordinate());
	}

	private String getMandanteDiCitta() {
		return getDocumentazione().getMandante() + " di " + getNomeCitta();
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
			descrizione.append("Hai documentato tutto: torna ").append(Misc.conPreposizione("da", documentazione.getMandante()))
					.append(" a ").append(getNomeCitta()).append(" a consegnare le note.");
		} else {
			descrizione.append(Misc.inizialeMaiuscola(getMandanteDiCitta())).append(" ti ha chiesto di documentare ")
					.append(Misc.getCardinaleM(reperti.size())).append(" posti: vai in quello segnato sulla mappa.");
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
						BusEventi.pubblica(new NotificaTestoParagrafo((numero == 1 ? "Si comincia " : "Si prosegue ")
								+ TestiDeiLuoghi.dentro(reperto.getLuogo()) + ": il posto è segnato sulla mappa."));
					})
					.poi(REPERTO + numero);
		}
		if (id.startsWith(REPERTO)) {
			int numero = Integer.parseInt(id.substring(REPERTO.length()));
			boolean ultimo = numero == reperti.size();
			return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nelPosto)
					.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(reperti.get(numero - 1).getTesto()
							+ (ultimo ? " Le note sono complete: " + documentazione.getMandante() + " aspetta a " + getNomeCitta() + "." : ""))))
					.poi(ultimo ? RITORNO : TRACCIA + (numero + 1));
		}
		throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		DocumentazioneRichiesta documentazione = getDocumentazione();
		return documentazione.getAspetto().nuovaScena()
				.parlaIlMandante(documentazione.getRichiesta())
				.parlaIlMandante("Vi segno sulla mappa da dove cominciare. Portatemi le note di tutti i posti, e avrete "
						+ getRicompensa() + " monete.")
				.parlaIlCapo(documentazione.getBattutaDelCapo())
				.parlaIlMandante(documentazione.getRisposta());
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return getDocumentazione().getAspetto().nuovaScena()
				.parlaIlMandante(getDocumentazione().getRingraziamento())
				.parlaIlMandante("Ecco le " + getRicompensa() + " monete promesse.");
	}

	@Override
	protected String testoAccettazione() {
		return Misc.inizialeMaiuscola(getMandanteDiCitta()) + " pagherà " + getRicompensa() + " monete per le note di "
				+ Misc.getCardinaleM(getDocumentazione().getReperti().size()) + " posti.";
	}

	@Override
	protected String testoRicompensa() {
		return Misc.inizialeMaiuscola(getDocumentazione().getMandante()) + " paga le " + getRicompensa() + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return getDocumentazione().getMonete();
	}
}
