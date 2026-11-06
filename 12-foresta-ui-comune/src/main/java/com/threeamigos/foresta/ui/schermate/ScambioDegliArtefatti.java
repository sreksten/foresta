package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoCommutazioneElenco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoScambioArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoArtefatto;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Finestra.Tasto;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.testo.TestiDellAiuto;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Lo scambio di artefatti fra due liste affiancate (inventario, commercianti, incantatore), senza disegno: le due liste come alberi da far scorrere
 * ({@link ElencoDegliArtefatti}), l'artefatto sotto un punto, le righe dell'aiuto, e i click e doppi click, che diventano comandi (commutare l'elenco dei
 * modificatori, spostare un artefatto da una parte all'altra). Reagisce alle notifiche di acquisto, vendita e rifiuto del proprio scambio con un fumetto e uno
 * sprite di monete. Le singole schermate (che cosa c'è al centro, i colori, l'aiuto della colonna centrale) lo personalizzano ({@link Personalizzazioni}).
 * <p>
 * Non è una finestra: lo tiene una schermata di scambio ({@link SchermataDiScambio}) a cui chiede le zone, il mouse e gli scorrimenti.
 */
public final class ScambioDegliArtefatti {

	/**
	 * Quel che cambia da una schermata di scambio all'altra
	 */
	public interface Personalizzazioni extends ElencoDegliArtefatti.Colori {
		/**
		 * Cosa fa il doppio click su un artefatto della parte sinistra (parte attiva), con le parole della schermata: per l'aiuto
		 */
		String aiutoDoppioClickSinistra();

		/**
		 * Cosa fa il doppio click su un artefatto della parte destra
		 */
		String aiutoDoppioClickDestra();

		/**
		 * L'aiuto per la colonna centrale, quando il mouse non è su un elenco di artefatti; di norma nessuno.
		 */
		default List<String> righeAiutoColonnaPersonaggio() {
			return new ArrayList<>();
		}

		/**
		 * Gestisce un click nella colonna centrale; dice se l'ha consumato.
		 */
		boolean processaClickPersonaggio(int x, int y, Tasto tasto);

		/**
		 * Gestisce un doppio click nella colonna centrale; dice se l'ha consumato.
		 */
		boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto);

		/**
		 * Che cosa dice chi ha appena comprato qualcosa dal gruppo.
		 */
		default String fraseDopoLaVendita() {
			return TestiDellaUI.get("ARTEFATTI_GRAZIE_AFFARI");
		}

		/**
		 * Aggiunge uno sprite che resta confinato nella schermata (le monete spese o ricavate)
		 */
		void aggiungiSpriteLocale(DescrizioneSpriteATempo sprite);
	}

	private final SchermataDiScambio schermata;
	private final VistaPartita vistaPartita;
	private final MisureDelleImmagini misure;
	private final Personalizzazioni personalizzazioni;

	// Lo scambio aperto, in sola lettura: gli spostamenti si chiedono con ComandoScambioArtefatto
	private VistaScambio scambio;

	// Quota (in coordinate della finestra) a cui inizia l'elenco delle caratteristiche del personaggio, aggiornata a ogni disegno e usata per l'hit-test
	// dei click.
	private int yAttributi = 0;

	public ScambioDegliArtefatti(SchermataDiScambio schermata, VistaPartita vistaPartita, MisureDelleImmagini misure, Personalizzazioni personalizzazioni) {
		this.schermata = schermata;
		this.vistaPartita = vistaPartita;
		this.misure = misure;
		this.personalizzazioni = personalizzazioni;
		BusEventi.iscriviti(NotificaApprovazioneAcquistoArtefatto.class, this::gestisciEventoApprovazioneAcquistoArtefatto);
		BusEventi.iscriviti(NotificaRifiutoAcquistoArtefatto.class, this::gestisciEventoRifiutoAcquistoArtefatto);
		BusEventi.iscriviti(NotificaApprovazioneVenditaArtefatto.class, this::gestisciEventoApprovazioneVenditaArtefatto);
	}

	/**
	 * Imposta lo scambio fra le due liste di artefatti.
	 */
	public void impostaScambio(VistaScambio scambio) {
		this.scambio = scambio;
	}

	public VistaScambio getScambio() {
		return scambio;
	}

	public int getYAttributi() {
		return yAttributi;
	}

	public void setYAttributi(int yAttributi) {
		this.yAttributi = yAttributi;
	}

	/**
	 * Ogni schermata di scambio (inventario, commerciante, incantatore...) è iscritta alle stesse notifiche: deve reagire solo a quelle dei comandi del proprio
	 * scambio, altrimenti un solo acquisto produce un fumetto e uno sprite per ciascuna schermata. La notifica porta lo scambio da cui veniva la richiesta,
	 * quindi basta confrontarlo per identità.
	 */
	private boolean riguardaQuestaSchermata(VistaScambio scambioDellaNotifica) {
		return scambio != null && scambioDellaNotifica == scambio;
	}

	private DescrizioneSpriteATempo spriteDelleMonete(int variazione, String descrizione) {
		String moneta = NomiDelleImmagini.SPRITE_MONETA;
		return DescrizioneSpriteATempo.conVariazione(moneta, misure.immagine(moneta), variazione, schermata.font, schermata.xMassimaZonaCentrale,
				schermata.yRigaMonete(), false, descrizione);
	}

	private void gestisciEventoApprovazioneAcquistoArtefatto(NotificaApprovazioneAcquistoArtefatto notifica) {
		if (!riguardaQuestaSchermata(notifica.getScambio())) {
			return;
		}
		BusEventi.pubblica(new InternoNotificaViaFumettoATempo(TestiDellaUI.get("ARTEFATTI_GRAZIE_ACQUISTO"), schermata.getCoordinateFumetto()));
		int costo = vistaPartita.prezzoAcquisto(notifica.getOggettoSpostato().getCostoAcquisto());
		personalizzazioni.aggiungiSpriteLocale(spriteDelleMonete(-costo, "Monete spese"));
	}

	private void gestisciEventoApprovazioneVenditaArtefatto(NotificaApprovazioneVenditaArtefatto notifica) {
		if (!riguardaQuestaSchermata(notifica.getScambio())) {
			return;
		}
		BusEventi.pubblica(new InternoNotificaViaFumettoATempo(personalizzazioni.fraseDopoLaVendita(), schermata.getCoordinateFumetto()));
		int costo = vistaPartita.prezzoVendita(notifica.getOggettoSpostato().getCostoAcquisto());
		personalizzazioni.aggiungiSpriteLocale(spriteDelleMonete(costo, "Monete acquisite"));
	}

	private void gestisciEventoRifiutoAcquistoArtefatto(NotificaRifiutoAcquistoArtefatto notifica) {
		if (!riguardaQuestaSchermata(notifica.getScambio())) {
			return;
		}
		BusEventi.pubblica(new InternoNotificaViaFumettoATempo(TestiDellaUI.get("ARTEFATTI_NON_ABBASTANZA_DENARO"), schermata.getCoordinateFumetto()));
	}

	/**
	 * Il prezzo da mostrare accanto agli artefatti di una parte, oppure null se lì non si mostra: sulla parte attiva è quanto si ricava vendendo, sulla
	 * remota quanto si paga comprando.
	 */
	private IntUnaryOperator prezzo(boolean parteAttiva) {
		if (parteAttiva) {
			return scambio.mostraCostoSuParteAttiva() ? vistaPartita::prezzoVendita : null;
		}
		return scambio.mostraCostoSuParteRemota() ? vistaPartita::prezzoAcquisto : null;
	}

	/**
	 * Gli artefatti della parte attiva (a sinistra), nell'ordine in cui li dà lo scambio
	 */
	public Collection<VistaArtefatto> getArtefattiDellaParteAttiva() {
		return new ArrayList<>(scambio.getInventarioParteAttiva());
	}

	public Collection<? extends VistaArtefatto> getArtefattiDellaParteRemota() {
		return scambio.getInventarioParteRemota();
	}

	/**
	 * L'albero degli artefatti di una parte, sempre con lo stesso valore accanto ai nomi (livello ed eventuale prezzo), così le righe cadono alla stessa quota.
	 * L'artefatto passato in evidenziato (se non null) è disegnato in bianco invece che in grigio chiaro.
	 */
	public ElencoScorrevole<VistaArtefatto> costruisciElenco(Collection<? extends VistaArtefatto> artefatti, VistaArtefatto evidenziato, boolean parteAttiva) {
		return ElencoDegliArtefatti.costruisci(vistaPartita, artefatti, evidenziato, parteAttiva, prezzo(parteAttiva), personalizzazioni,
				schermata.larghezzaDisponibileInRiquadroInventario(), misure);
	}

	/**
	 * L'artefatto sotto il mouse in quella parte (null se nessuno): serve a evidenziarlo
	 */
	public VistaArtefatto artefattoSottoIlMouse(Collection<? extends VistaArtefatto> artefatti, int boxX, int offset, boolean parteAttiva) {
		return trovaArtefatto(artefatti, boxX, offset, schermata.mouseX, schermata.mouseY, parteAttiva);
	}

	/**
	 * Riporta l'artefatto disegnato alla posizione (x, y) espressa in coordinate della finestra, oppure null se il punto non cade sull'elenco o non
	 * corrisponde al titolo di un artefatto (per esempio una riga di modificatore o di incantamento, o spazio vuoto).
	 */
	public VistaArtefatto trovaArtefatto(Collection<? extends VistaArtefatto> artefatti, int boxX, int offset, int x, int y, boolean parteAttiva) {
		int xInterno = x - (boxX + Finestra.SPACING);
		int yInterno = y - (SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + 2 * Finestra.SPACING);
		if (xInterno < 0 || xInterno >= schermata.larghezzaDisponibileInRiquadroInventario()
				|| yInterno < 0 || yInterno >= schermata.altezzaDisponibileInRiquadroInventario()) {
			return null;
		}
		return costruisciElenco(artefatti, null, parteAttiva).riferimentoTitoloAllaQuota(yInterno + offset);
	}

	/**
	 * Le righe di aiuto per l'elenco sotto il mouse: sul nome di un artefatto cosa fa il click (aprire o chiudere l'elenco dei modificatori) e cosa fa il
	 * doppio click da quella parte; ovunque nell'elenco, se si scorre, la rotella
	 */
	public List<String> righeAiuto() {
		List<String> righe = new ArrayList<>();
		Collection<? extends VistaArtefatto> elenco;
		boolean parteAttiva;
		int boxX;
		int offset;
		String doppioClick;
		if (schermata.dentroElenco(schermata.xMinimaZonaSinistra, schermata.mouseX, schermata.mouseY)) {
			elenco = getArtefattiDellaParteAttiva();
			parteAttiva = true;
			boxX = schermata.xMinimaZonaSinistra;
			offset = schermata.offsetYZonaSinistra();
			doppioClick = personalizzazioni.aiutoDoppioClickSinistra();
		} else if (schermata.dentroElenco(schermata.xMinimaZonaDestra, schermata.mouseX, schermata.mouseY)) {
			elenco = getArtefattiDellaParteRemota();
			parteAttiva = false;
			boxX = schermata.xMinimaZonaDestra;
			offset = schermata.offsetYZonaDestra();
			doppioClick = personalizzazioni.aiutoDoppioClickDestra();
		} else {
			return personalizzazioni.righeAiutoColonnaPersonaggio();
		}
		VistaArtefatto artefatto = trovaArtefatto(elenco, boxX, offset, schermata.mouseX, schermata.mouseY, parteAttiva);
		if (artefatto != null) {
			righe.add(TestiDellAiuto.click(artefatto.isFigliVisibili(), TestiDellaUI.get("COSA_ELENCO_MODIFICATORI")));
			righe.add(doppioClick);
		}
		if (costruisciElenco(elenco, null, parteAttiva).isScorrevole(schermata.altezzaDisponibileInRiquadroInventario())) {
			righe.add(TestiDellAiuto.rotella());
		}
		return righe;
	}

	/**
	 * Un click: nella colonna centrale lo gestisce la schermata, sul nome di un artefatto apre o chiude il suo elenco dei modificatori
	 */
	public void processaClick(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO || scambio == null || schermata.isGestoTrascinato()) {
			return;
		}
		if (personalizzazioni.processaClickPersonaggio(x, y, tasto)) {
			return;
		}
		VistaArtefatto artefatto = trovaArtefatto(getArtefattiDellaParteAttiva(), schermata.xMinimaZonaSinistra, schermata.offsetYZonaSinistra(), x, y, true);
		if (artefatto == null) {
			artefatto = trovaArtefatto(scambio.getInventarioParteRemota(), schermata.xMinimaZonaDestra, schermata.offsetYZonaDestra(), x, y, false);
		}
		if (artefatto == null) {
			return;
		}
		BusEventi.pubblica(ComandoCommutazioneElenco.artefatto(scambio.getId(), artefatto.getUuid()));
	}

	/**
	 * Un doppio click: sposta l'artefatto dall'altra parte
	 */
	public void processaDoppioClick(int x, int y, Tasto tasto) {
		// Come il click singolo, solo con il tasto sinistro: un doppio click destro o centrale non compra, non vende e non spende punti
		if (tasto != Tasto.SINISTRO || scambio == null || schermata.isGestoTrascinato()) {
			return;
		}
		if (personalizzazioni.processaDoppioClickPersonaggio(x, y, tasto)) {
			return;
		}
		VistaArtefatto artefatto = trovaArtefatto(getArtefattiDellaParteAttiva(), schermata.xMinimaZonaSinistra, schermata.offsetYZonaSinistra(), x, y, true);
		if (artefatto != null) {
			BusEventi.pubblica(new ComandoScambioArtefatto(scambio.getId(), ComandoScambioArtefatto.Destinazione.PARTE_REMOTA, artefatto.getUuid()));
			return;
		}
		artefatto = trovaArtefatto(scambio.getInventarioParteRemota(), schermata.xMinimaZonaDestra, schermata.offsetYZonaDestra(), x, y, false);
		if (artefatto != null) {
			BusEventi.pubblica(new ComandoScambioArtefatto(scambio.getId(), ComandoScambioArtefatto.Destinazione.PARTE_ATTIVA, artefatto.getUuid()));
		}
	}
}
