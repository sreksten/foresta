package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaBancoDiLavoro;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.modellodati.MessaggioMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoNegozio;
import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.strumenti.Temporizzatore;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import com.threeamigos.foresta.ui.barra.DisplayableCanvasBarraIcone;
import com.threeamigos.foresta.ui.barra.DisplayableCanvasBarraIconeDock;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroCombattimento;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroGruppo;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroIncantesimiEPozioni;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroLocazione;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroMappa;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroMissioni;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroSfida;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroStatistiche;
import com.threeamigos.foresta.ui.riquadri.DisplayableCanvasRiquadroTesto;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasCommerciante;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasIncantatore;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasIntermezzo;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasIntroOutro;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasInventario;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasMappaATuttoSchermo;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasScambiatoreConsumabili;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasTrofei;
import com.threeamigos.foresta.ui.sprite.CoordinateFumetto;
import com.threeamigos.foresta.ui.sprite.SpriteAnnuncioGlobale;
import com.threeamigos.foresta.ui.sprite.SpriteFumetto;
import com.threeamigos.foresta.ui.sprite.SpriteInDissolvenza;
import com.threeamigos.foresta.ui.sprite.SpriteInterface;
import com.threeamigos.foresta.ui.sprite.SpriteRivelazioneArtefatto;
import com.threeamigos.foresta.ui.tattiche.DisplayableCanvasTattiche;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;

/**
 * Il canvas grafico del gioco: sa quale schermata è quella corrente (in gioco, mappa, inventario, negozi, intermezzo, intro e fine partita...) e la disegna, e fa da
 * facciata per tutto ciò che la UI deve mostrare. {@link ForestaUI} traduce gli eventi del bus in chiamate sui suoi metodi.
 * <p>
 * Nella schermata di gioco disegna i riquadri (locazione, gruppo, combattimento, mappa, missioni, testo, statistiche, incantesimi e pozioni) e la barra delle icone, in
 * una pila di finestre che si possono portare in primo piano, e sopra a tutto gli sprite (numeri che salgono, fumetti, annunci). Le animazioni girano nel metodo
 * {@link #run()}, a {@code Temporizzatore.FRAME_PER_SECONDO} fotogrammi al secondo; il disegno vero avviene sul thread di Swing.
 */
public class DisplayableCanvas extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	public static final int ORIENTAMENTO_ORIZZONTALE = 0;
	public static final int ORIENTAMENTO_VERTICALE = 1;

	private enum StatoDisplayableCanvas {
		STATO_INTRO,
		STATO_SELEZIONE_SLOT_DA_CARICARE,
		STATO_MESSAGGIO,
		STATO_IN_GIOCO,
		STATO_MAPPA,
		STATO_SELEZIONE_SLOT_DA_SALVARE,
		STATO_CONFERMA_USCITA,
		STATO_PERSO,
		STATO_VINTO,
		STATO_STATISTICHE,
		STATO_PUNTEGGI,
		STATO_INVENTARIO,
		STATO_TROFEI,
		STATO_TATTICHE,
		STATO_COMMERCIANTE,
		STATO_ALCHIMISTA,
		STATO_INCANTATORE,
		STATO_INTERMEZZO
	}

	private StatoDisplayableCanvas stato;
	private final ArrayList<TipoFinestra> stackElementiGrafici;

	private final Map<Finestra, Rectangle> mappaCoordinateElementiGrafici = new HashMap<>();

	// Gruppo, incantesimi e missioni sono impilati nella colonna a destra, sotto mappa e statistiche, ognuno
	// più in basso del precedente di passoPila: l'ultimo dello stack è il più in basso e sta sopra gli altri
	private static final int RIQUADRI_IMPILATI = 3;
	private final int quotaPila;
	private final int passoPila;
	// Quanto sfuma in basso un riquadro prima che cominci il successivo
	private final int altezzaDissolvenzaPila = DoomdarkFontMedium.getInstance().getHeight();
	
	private final transient DisplayableCanvasIntroOutro riquadroIntroOutro;
	private final transient DisplayableCanvasRiquadroMappa riquadroMappa;
	private final transient DisplayableCanvasRiquadroLocazione riquadroLocazione;
	private final transient DisplayableCanvasRiquadroStatistiche riquadroStatistiche;
	private final transient DisplayableCanvasRiquadroCombattimento riquadroCombattimento;
	private final transient DisplayableCanvasRiquadroSfida riquadroSfida;
	private static final float SECONDI_DI_ATTESA_DELLA_SFIDA = 1.5f;
	private static final float SECONDI_DI_DISSOLVENZA_DELLA_SFIDA = 1.0f;
	private final transient DisplayableCanvasRiquadroTesto riquadroTesto;
	private final transient DisplayableCanvasRiquadroGruppo riquadroGruppo;
	private final transient DisplayableCanvasRiquadroIncantesimiEPozioni riquadroIncantesimiEPozioni;
	private final transient DisplayableCanvasRiquadroMissioni riquadroMissioni;
	private final transient DisplayableCanvasMappaATuttoSchermo mappaATuttoSchermo;
	private final transient DisplayableCanvasInventario inventario;
	private final transient DisplayableCanvasTrofei paginaTrofei;
	private final transient DisplayableCanvasTattiche tattiche;
	// Armaiolo e venditore di pergamene: una sola schermata, che cambia secondo il negozio
	private final transient DisplayableCanvasCommerciante commerciante;
	private final transient DisplayableCanvasScambiatoreConsumabili alchimista;
	private final transient DisplayableCanvasIncantatore incantatore;
	private final transient DisplayableCanvasBarraIcone barraIcone;
	private final transient DisplayableCanvasIntermezzo intermezzo;

	private final ArrayList<SpriteInterface> sprites;
	private final List<SpriteAnnuncioGlobale> codaAnnunciGlobali = new ArrayList<>();
	// Un annuncio richiesto mentre è ancora a schermo l'ultima pagina di un intermezzo
	// (es. una missione attivata da un controllo dell'Automa a cascata, prima che la UI
	// torni al gioco) va rimandato: disegnarlo subito lo sovrapporrebbe all'intermezzo.
	private final List<SpriteAnnuncioGlobale> annunciGlobaliRimandati = new ArrayList<>();
	private SpriteAnnuncioGlobale annuncioGlobaleAttivo;
	private final List<SpriteFumetto> codaFumetti = new ArrayList<>();
	// Gli artefatti trovati nei cofani e nei templi, mostrati uno alla volta (vedi SpriteRivelazioneArtefatto)
	private final List<SpriteRivelazioneArtefatto> codaRivelazioni = new ArrayList<>();
	private SpriteRivelazioneArtefatto rivelazioneAttiva;
	private SpriteFumetto fumettoAttivo;
	// Rileva il fronte "appena tornata inattiva" per notificare l'Automa con InternoUiInattiva
	private boolean uiOccupata;

	private final transient VistaPartita vistaPartita;

	private final int larghezzaSchermo;
	private final int altezzaSchermo;
	private final int altezzaTotaleSchermo;

	private transient Thread animatore;
	private boolean animatoreInAzione = false;
	// La prossima intro deve partire dalla classifica invece che dai loghi (vedi mostraPunteggi)
	private boolean introDallaClassifica = false;

	/**
	 * La parte della finestra in cui si disegnano riquadri e schermate a tutto schermo,
	 * esclusa la barra icone.
	 */
	static Dimension calcolaAreaDiContenuto(int width, int height, int orientamento, int dimensioneBarraIcone) {
		if (orientamento == ORIENTAMENTO_ORIZZONTALE) {
			return new Dimension(width, height - dimensioneBarraIcone);
		}
		return new Dimension(640, height);
	}

	/**
	 * @param barraDock vero per la barra delle icone che si ingrandisce sotto il cursore (DisplayableCanvasBarraIconeDock),
	 *                  solo in orientamento orizzontale; altrimenti la barra classica
	 * @param vistaPartita lo stato della partita da mostrare, in sola lettura: lo ricevono tutti i riquadri
	 */
	public DisplayableCanvas(int width, int height, int orientamento, int dimensioneBarraIcone, boolean barraDock,
							 GestorePunteggi gestorePunteggi, VistaPartita vistaPartita) {
		super();
		this.vistaPartita = vistaPartita;
		altezzaTotaleSchermo = height;
		Dimension areaDiContenuto = calcolaAreaDiContenuto(width, height, orientamento, dimensioneBarraIcone);
		int larghezzaContenuto = areaDiContenuto.width;
		int altezzaContenuto = areaDiContenuto.height;
		larghezzaSchermo = larghezzaContenuto;
		altezzaSchermo = altezzaContenuto;
		stackElementiGrafici = new ArrayList<>();
		// Gruppo, incantesimi e missioni sono impilati nell'ordine in cui compaiono qui
		stackElementiGrafici.add(TipoFinestra.STATO);
		stackElementiGrafici.add(TipoFinestra.INCANTESIMI_E_POZIONI);
		stackElementiGrafici.add(TipoFinestra.MAPPA);
		stackElementiGrafici.add(TipoFinestra.STATISTICHE);
		stackElementiGrafici.add(TipoFinestra.GRAFICA);
		stackElementiGrafici.add(TipoFinestra.TESTO);
		stackElementiGrafici.add(TipoFinestra.MISSIONI);
		stackElementiGrafici.add(TipoFinestra.INFO_COMBATTIMENTO);
		stackElementiGrafici.add(TipoFinestra.SFIDA);
		// INTRO_OUTRO e MAPPA_A_TUTTO_SCHERMO non appartengono allo stack: occupano
		// da soli tutto lo schermo e sono scelti in base allo stato del canvas.
		cambiaStato(StatoDisplayableCanvas.STATO_INTRO);
		setSize(width, height);
		setBackground(Color.black);
		BufferedImage immagineLocazione = ImageCache.locazioni.get(TipoLocazione.BOSCO);

		riquadroIntroOutro = new DisplayableCanvasIntroOutro(larghezzaContenuto, altezzaContenuto, gestorePunteggi, vistaPartita);

		Rectangle riquadroIntroOutroRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(riquadroIntroOutro, riquadroIntroOutroRect);

		intermezzo = new DisplayableCanvasIntermezzo(larghezzaContenuto, altezzaContenuto, riquadroIntroOutro);
		mappaCoordinateElementiGrafici.put(intermezzo, new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto));

		// A destra la colonna con mappa, statistiche e i riquadri impilati; a sinistra la locazione con il testo sotto.
		// Fino all'altezza della locazione la colonna è larga quanto la mappa, quindi lo spazio per la locazione è la
		// larghezza della finestra meno quella della mappa; il testo sotto, accanto ai riquadri impilati, è più stretto
		int colonnaDestraX = larghezzaContenuto - ImageCache.SPACING - ImageCache.corniceGrande.getWidth();
		int larghezzaLocazioneDisponibile = larghezzaContenuto - 3 * ImageCache.SPACING - ImageCache.corniceMappa.getWidth();
		int larghezzaTesto = colonnaDestraX - 2 * ImageCache.SPACING;
		// Mappa e statistiche, più strette dei riquadri impilati, sono allineate a destra
		int mappaX = colonnaDestraX + ImageCache.corniceGrande.getWidth() - ImageCache.corniceMappa.getWidth();
		int statisticheX = colonnaDestraX + ImageCache.corniceGrande.getWidth() - ImageCache.cornicePiccola.getWidth();

		int elementoX = mappaX;
		int elementoY = ImageCache.SPACING;
		int larghezzaElemento = ImageCache.corniceMappa.getWidth();
		int altezzaElemento = ImageCache.corniceMappa.getHeight();

		riquadroMappa = new DisplayableCanvasRiquadroMappa(
				mappaX,
				ImageCache.SPACING,
				larghezzaContenuto,
				altezzaContenuto,
				vistaPartita);

		Rectangle riquadroMappaRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroMappa, riquadroMappaRect);

		elementoX = ImageCache.SPACING;
		elementoY = ImageCache.SPACING;
		larghezzaElemento = larghezzaLocazioneDisponibile;
		altezzaElemento = immagineLocazione.getHeight();

		riquadroLocazione = new DisplayableCanvasRiquadroLocazione(elementoX, elementoY, larghezzaElemento, vistaPartita);

		Rectangle riquadroLocazioneRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroLocazione, riquadroLocazioneRect);

		elementoX = statisticheX;
		elementoY = ImageCache.SPACING + ImageCache.corniceMappa.getHeight() + ImageCache.SPACING;
		larghezzaElemento = ImageCache.cornicePiccola.getWidth();
		altezzaElemento = ImageCache.cornicePiccola.getHeight();

		riquadroStatistiche = new DisplayableCanvasRiquadroStatistiche(elementoX, elementoY, vistaPartita);

		Rectangle riquadroStatisticheRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroStatistiche, riquadroStatisticheRect);

		// Di norma sotto l'immagine della locazione, dove ci sono i personaggi
		// Centrato sul testo sotto la locazione, non sull'intero schermo
		riquadroCombattimento = new DisplayableCanvasRiquadroCombattimento(colonnaDestraX, altezzaContenuto,
				ImageCache.SPACING + immagineLocazione.getHeight() + ImageCache.SPACING, vistaPartita);

		// Lo calcola da solo perché è un elemento flottante a differenza degli altri che sono fissi
		mappaCoordinateElementiGrafici.put(riquadroCombattimento, riquadroCombattimento.getRettangolo());

		riquadroSfida = new DisplayableCanvasRiquadroSfida(larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(riquadroSfida, riquadroSfida.getRettangolo());

		elementoX = ImageCache.SPACING;
		elementoY = ImageCache.SPACING + immagineLocazione.getHeight() + ImageCache.SPACING;
		larghezzaElemento = larghezzaTesto;
		altezzaElemento = altezzaContenuto - ImageCache.SPACING - immagineLocazione.getHeight() - ImageCache.SPACING;

		riquadroTesto = new DisplayableCanvasRiquadroTesto(elementoX, elementoY, larghezzaElemento, altezzaElemento);

		Rectangle riquadroTestoRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroTesto, riquadroTestoRect);

		elementoX = colonnaDestraX;
		elementoY = ImageCache.SPACING;
		larghezzaElemento = ImageCache.corniceGrande.getWidth();
		altezzaElemento = ImageCache.corniceGrande.getHeight();

		riquadroGruppo = new DisplayableCanvasRiquadroGruppo(elementoX, elementoY, vistaPartita);

		Rectangle riquadroGruppoRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroGruppo, riquadroGruppoRect);

		elementoX = colonnaDestraX;
		elementoY = ImageCache.SPACING + ImageCache.corniceGrande.getHeight() +	ImageCache.SPACING;
		larghezzaElemento = ImageCache.corniceGrande.getWidth();
		altezzaElemento = ImageCache.corniceGrande.getHeight();

		riquadroIncantesimiEPozioni = new DisplayableCanvasRiquadroIncantesimiEPozioni(elementoX, elementoY, vistaPartita);

		Rectangle riquadroIncantesimiRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroIncantesimiEPozioni, riquadroIncantesimiRect);

		elementoX = colonnaDestraX;
		elementoY = ImageCache.SPACING + ImageCache.corniceGrande.getHeight() +
				ImageCache.SPACING + ImageCache.corniceGrande.getHeight() + ImageCache.SPACING;
		larghezzaElemento = ImageCache.corniceGrande.getWidth();
		altezzaElemento = ImageCache.corniceGrande.getHeight();

		riquadroMissioni = new DisplayableCanvasRiquadroMissioni(elementoX, elementoY, vistaPartita);

		Rectangle riquadroMissioniRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroMissioni, riquadroMissioniRect);

		quotaPila = ImageCache.SPACING + ImageCache.corniceMappa.getHeight() + ImageCache.SPACING
				+ ImageCache.cornicePiccola.getHeight() + ImageCache.SPACING;
		passoPila = Math.max(0, (altezzaContenuto - quotaPila - ImageCache.corniceGrande.getHeight()) / RIQUADRI_IMPILATI);
		posizionaRiquadriImpilati();

		mappaATuttoSchermo = new DisplayableCanvasMappaATuttoSchermo(larghezzaContenuto, altezzaContenuto, vistaPartita);

		Rectangle mappaATuttoSchermoRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(mappaATuttoSchermo, mappaATuttoSchermoRect);

		inventario = new DisplayableCanvasInventario(larghezzaContenuto, altezzaContenuto, vistaPartita);

		Rectangle inventarioRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(inventario, inventarioRect);

		paginaTrofei = new DisplayableCanvasTrofei(larghezzaContenuto, altezzaContenuto, vistaPartita);
		mappaCoordinateElementiGrafici.put(paginaTrofei, new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto));

		tattiche = new DisplayableCanvasTattiche(larghezzaContenuto, altezzaContenuto, vistaPartita);
		mappaCoordinateElementiGrafici.put(tattiche, new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto));

		commerciante = new DisplayableCanvasCommerciante(larghezzaContenuto, altezzaContenuto, vistaPartita);

		Rectangle commercianteRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(commerciante, commercianteRect);

		alchimista = new DisplayableCanvasScambiatoreConsumabili(larghezzaContenuto, altezzaContenuto, vistaPartita);

		Rectangle alchimistaRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(alchimista, alchimistaRect);

		incantatore = new DisplayableCanvasIncantatore(larghezzaContenuto, altezzaContenuto, vistaPartita);

		Rectangle incantatoreRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(incantatore, incantatoreRect);

		int barraLarghezza;
		int barraAltezza;
		int barraX;
		int barraY;
		if (orientamento == ORIENTAMENTO_ORIZZONTALE) {
			barraLarghezza = width;
			barraAltezza = dimensioneBarraIcone;
			barraX = 0;
			barraY = altezzaContenuto;
		} else {
			barraLarghezza = dimensioneBarraIcone;
			barraAltezza = height;
			barraX = width - dimensioneBarraIcone;
			barraY = 0;
		}
		barraIcone = barraDock && orientamento == ORIENTAMENTO_ORIZZONTALE
				? new DisplayableCanvasBarraIconeDock(barraX, barraY, barraLarghezza, barraAltezza, vistaPartita)
				: new DisplayableCanvasBarraIcone(orientamento, barraX, barraY, barraLarghezza, barraAltezza, vistaPartita);

		Rectangle barraIconeRect = new Rectangle(barraX, barraY, barraLarghezza, barraAltezza);
		mappaCoordinateElementiGrafici.put(barraIcone, barraIconeRect);

		sprites = new ArrayList<>();

		GestoreMouse gestoreMouse = new GestoreMouse();
		addMouseListener(gestoreMouse);
		addMouseMotionListener(gestoreMouse);
		addMouseWheelListener(gestoreMouse);

		registratiAEventi();
	}

	private void registratiAEventi() {
		// Eventi globali
		BusEventi.iscriviti(NotificaAumentoLivelloMondo.class, this::gestisciEventoAumentoLivelloMondo);
		BusEventi.iscriviti(NotificaArtefattoTrovato.class, this::gestisciEventoArtefattoTrovato);
		// Eventi interni del motore grafico - i sottopannelli potrebbero richiedere la creazione di sprite da gestire qui
		BusEventi.iscriviti(InternoCreazioneSpriteAnnuncioGlobale.class, this::gestisciEventoCreazioneSpriteAnnuncioGlobale);
		BusEventi.iscriviti(InternoCreazioneSpriteATempo.class, this::gestisciEventoCreazioneSpriteATempo);
		BusEventi.iscriviti(InternoCreazioneSpriteEffettoDiStato.class, this::gestisciEventoCreazioneSpriteEffetto);
		BusEventi.iscriviti(InternoCreazioneSpriteFumettoATempo.class, this::gestisciEventoCreazioneSpriteFumetto);
		BusEventi.iscriviti(InternoCreazioneSpriteInDissolvenza.class, this::gestisciEventoCreazioneSpriteInDissolvenza);
		// Eventi del riquadro gruppo
		BusEventi.iscriviti(NotificaAumentoLivelloPersonaggio.class, this::gestisciEventoAumentoLivelloPersonaggio);
		BusEventi.iscriviti(NotificaVariazioneStatistichePersonaggio.class, this::gestisciEventoVariazioneStatistichePersonaggio);
		// Eventi del riquadro incantesimi
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaIncantesimi.class, this::gestisciEventoVariazioneIncantesimi);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniSalute.class, this::gestisciEventoVariazionePozioniSalute);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniSaluteGrandi.class, this::gestisciEventoVariazionePozioniSaluteGrandi);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniMagia.class, this::gestisciEventoVariazionePozioniMagia);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniMagiaGrandi.class, this::gestisciEventoVariazionePozioniMagiaGrandi);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniAumentoLivello.class, this::gestisciEventoVariazionePozioniAumentoLivello);
		// Eventi del riquadro missioni
		BusEventi.iscriviti(NotificaAggiornamentoStatoMissione.class, this::gestisciEventoAggiornamentoStatoMissione);

	}

	// Eventi globali

	private void gestisciEventoAumentoLivelloMondo(NotificaAumentoLivelloMondo evento) {
		notifica("LEVEL UP! Ora il mondo è al livello " + evento.getLivello() + "!");
	}

	private void gestisciEventoCreazioneSpriteAnnuncioGlobale(InternoCreazioneSpriteAnnuncioGlobale evento) {
		if (evento.getSprite() != null) {
			codaAnnunciGlobali.add(evento.getSprite());
		}
	}

	private void gestisciEventoCreazioneSpriteATempo(InternoCreazioneSpriteATempo evento) {
		if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO) {
			aggiungiSprite(evento.getSprite());
		}
	}

	private void gestisciEventoCreazioneSpriteEffetto(InternoCreazioneSpriteEffettoDiStato evento) {
		aggiungiSprite(evento.getSprite());
	}

	private void gestisciEventoCreazioneSpriteFumetto(InternoCreazioneSpriteFumettoATempo evento) {
		if (evento.getSprite() != null) {
			codaFumetti.add(evento.getSprite());
		}
	}

	private void gestisciEventoCreazioneSpriteInDissolvenza(InternoCreazioneSpriteInDissolvenza evento) {
		aggiungiSprite(evento.getSprite());
	}

	// Eventi del riquadro del gruppo

	private void gestisciEventoAumentoLivelloPersonaggio(NotificaAumentoLivelloPersonaggio evento) {
		VistaPersonaggio personaggio = evento.getPersonaggio();
		notificaAnnuncioGlobale("LEVEL UP!", personaggio.getNome() + " A LIVELLO " + personaggio.getLivello() + "!");
		notifica("LEVEL UP! Ora " + personaggio.getNome() + " è al livello " + personaggio.getLivello() + "!");
		// La notifica come iconcina è fatta dal riquadro del gruppo
		primoPiano(TipoFinestra.STATO);
		riquadroGruppo.gestisciEventoAumentoLivelloPersonaggio(evento);
	}

	private void gestisciEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
		// Il riquadro del gruppo viene davanti solo se cambia qualcosa dei miei: le statistiche di un mostro non lo riguardano
		if (vistaPartita.getGruppoGiocatore().contiene(evento.getPersonaggio())) {
			primoPiano(TipoFinestra.STATO);
		}
		riquadroGruppo.gestisciEventoVariazioneStatistichePersonaggio(evento);
	}

	// Eventi del riquadro incantesimi

	private void gestisciEventoVariazioneIncantesimi(NotificaVariazioneDisponibilitaIncantesimi evento) {
		primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazioneIncantesimi(evento);
	}

	private void gestisciEventoVariazionePozioniSalute(NotificaVariazioneDisponibilitaPozioniSalute evento) {
		primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazionePozioniSalute(evento);
	}

	private void gestisciEventoVariazionePozioniSaluteGrandi(NotificaVariazioneDisponibilitaPozioniSaluteGrandi evento) {
		primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazionePozioniSaluteGrandi(evento);
	}

	private void gestisciEventoVariazionePozioniMagia(NotificaVariazioneDisponibilitaPozioniMagia evento) {
		primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazionePozioniMagia(evento);
	}

	private void gestisciEventoVariazionePozioniMagiaGrandi(NotificaVariazioneDisponibilitaPozioniMagiaGrandi evento) {
		primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazionePozioniMagiaGrandi(evento);
	}

	private void gestisciEventoVariazionePozioniAumentoLivello(NotificaVariazioneDisponibilitaPozioniAumentoLivello evento) {
		primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazionePozioniAumentoLivello(evento);
	}

	private void gestisciEventoAggiornamentoStatoMissione(NotificaAggiornamentoStatoMissione evento) {
		primoPiano(TipoFinestra.MISSIONI);
		notificaAnnuncioGlobale(evento.getEtichetta(), evento.getDescrizione());
	}

	/**
	 * Mostra la schermata per scegliere lo slot da cui caricare una partita, con le testate di quelli disponibili.
	 */
	public void selezioneSlotSalvataggioDaCaricare(Collection<TestataSalvataggio> salvataggiDisponibili) {
		riquadroIntroOutro.setSalvataggiDisponibili(salvataggiDisponibili);
		cambiaStato(StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_CARICARE);
		repaint();
	}

	@Override
	public void addNotify() {
		super.addNotify();
		avviaThreadAnimazione();
	}
	
	private void avviaThreadAnimazione() {
		if (animatore == null || !animatoreInAzione) {
			animatore = new Thread(this);
			animatore.setDaemon(true);
			animatore.start();
		}
	}

	/**
	 * Ferma il thread che ridisegna a ogni fotogramma.
	 */
	public void fermaThreadAnimazione() {
		animatoreInAzione = false;
	}

	/**
	 * Il ciclo di animazione: a cadenza fissa fa avanzare gli sprite e chiede il ridisegno, senza contenere logica di gioco.
	 */
	public void run() {
		animatoreInAzione = true;
		// Cadenza fissa: la scadenza del fotogramma successivo si calcola dalla precedente,
		// non da quando finisce il sonno, altrimenti i ritardi di sleep() si accumulano e
		// il ritmo effettivo scende sotto FRAME_PER_SECONDO (e diventa irregolare).
		long periodoNanos = 1_000_000_000L / Temporizzatore.FRAME_PER_SECONDO;
		long prossimoFotogramma = System.nanoTime();
		while (animatoreInAzione) {
			if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO || stato == StatoDisplayableCanvas.STATO_MAPPA
					|| stato == StatoDisplayableCanvas.STATO_INTRO
					|| stato == StatoDisplayableCanvas.STATO_INVENTARIO || stato == StatoDisplayableCanvas.STATO_COMMERCIANTE
					|| stato == StatoDisplayableCanvas.STATO_TATTICHE
					|| stato == StatoDisplayableCanvas.STATO_ALCHIMISTA
					|| stato == StatoDisplayableCanvas.STATO_INCANTATORE
					|| stato == StatoDisplayableCanvas.STATO_INTERMEZZO
					|| annuncioGlobaleAttivo != null || !codaAnnunciGlobali.isEmpty()
					|| barraIcone.inTransizione()) {
				repaint();
			}
			prossimoFotogramma += periodoNanos;
			long attesaNanos = prossimoFotogramma - System.nanoTime();
			if (attesaNanos <= 0) {
				// In ritardo di oltre un fotogramma (es. il sistema era sospeso): si riparte
				// da adesso invece di tentare di recuperare con una raffica di repaint().
				prossimoFotogramma = System.nanoTime();
				continue;
			}
			try {
				Thread.sleep(attesaNanos / 1_000_000L, (int) (attesaNanos % 1_000_000L));
			} catch (InterruptedException e) {
			}
		}
	}
	
	/**
	 * Finita la sfida a carta, forbici e sasso il riquadro va via: diventa uno sprite che resta intero un attimo, per
	 * vedere l'ultima mano, e poi si dissolve lasciando leggere la mappa.
	 */
	void dissolviLaSfida() {
		aggiungiSprite(new SpriteInDissolvenza("Sfida a carta, forbici e sasso", riquadroSfida.inUnImmagine(),
				riquadroSfida.getX(), riquadroSfida.getY(), SECONDI_DI_ATTESA_DELLA_SFIDA, SECONDI_DI_DISSOLVENZA_DELLA_SFIDA));
		riquadroSfida.setVisibile(false);
	}

	DisplayableCanvasRiquadroSfida getRiquadroSfida() {
		return riquadroSfida;
	}

	DisplayableCanvasRiquadroCombattimento getRiquadroCombattimento() {
		return riquadroCombattimento;
	}

	/**
	 * Svuota il riquadro di testo, per una partita nuova.
	 */
	public void reinizializza() {
		riquadroTesto.clear();
	}

	/**
	 * Porta la finestra in cima alla pila: è quella che si disegna sopra le altre e che riceve per prima il mouse.
	 *
	 * @throws IllegalArgumentException se non è una finestra della pila
	 */
	public void primoPiano(TipoFinestra finestra) {
		if (!stackElementiGrafici.remove(finestra)) {
			throw new IllegalArgumentException("Elemento grafico non valido: " + finestra);
		}
		stackElementiGrafici.add(finestra);
		posizionaRiquadriImpilati();
	}

	private static boolean isRiquadroImpilato(TipoFinestra finestra) {
		return finestra == TipoFinestra.STATO || finestra == TipoFinestra.INCANTESIMI_E_POZIONI
				|| finestra == TipoFinestra.MISSIONI;
	}

	/**
	 * I riquadri impilati nell'ordine dello stack: il primo è il più in alto, l'ultimo il più in basso e davanti agli altri
	 */
	List<TipoFinestra> riquadriImpilati() {
		List<TipoFinestra> impilati = new ArrayList<>(RIQUADRI_IMPILATI);
		for (TipoFinestra finestra : new ArrayList<>(stackElementiGrafici)) {
			if (isRiquadroImpilato(finestra)) {
				impilati.add(finestra);
			}
		}
		return impilati;
	}

	/**
	 * Dà a ogni riquadro impilato la quota secondo la sua posizione nello stack, sia per disegnarlo sia per il mouse
	 */
	private void posizionaRiquadriImpilati() {
		List<TipoFinestra> impilati = riquadriImpilati();
		for (int posizione = 0; posizione < impilati.size(); posizione++) {
			TipoFinestra tipo = impilati.get(posizione);
			int y = quotaPila + posizione * passoPila;
			switch (tipo) {
				case STATO:
					riquadroGruppo.spostaInVerticale(y);
					break;
				case INCANTESIMI_E_POZIONI:
					riquadroIncantesimiEPozioni.spostaInVerticale(y);
					break;
				default:
					riquadroMissioni.spostaInVerticale(y);
					break;
			}
			mappaCoordinateElementiGrafici.get(recuperaFinestra(tipo)).y = y;
		}
	}

	/**
	 * Dopo un riquadro impilato che non è l'ultimo, lo fa sfumare nel nero nelle ultime righe prima del riquadro
	 * successivo, che comincia subito dopo
	 */
	private void dissolviSeCoperto(Graphics2D graphics, TipoFinestra finestra) {
		List<TipoFinestra> impilati = riquadriImpilati();
		int posizione = impilati.indexOf(finestra);
		if (posizione < 0 || posizione == impilati.size() - 1) {
			return;
		}
		Rectangle rettangolo = mappaCoordinateElementiGrafici.get(recuperaFinestra(finestra));
		int quotaSuccessivo = quotaPila + (posizione + 1) * passoPila;
		for (int riga = 0; riga < altezzaDissolvenzaPila; riga++) {
			int alfa = (riga + 1) * 255 / altezzaDissolvenzaPila;
			graphics.setColor(new Color(0, 0, 0, alfa));
			graphics.fillRect(rettangolo.x, quotaSuccessivo - altezzaDissolvenzaPila + riga, rettangolo.width, 1);
		}
	}

	private void inGioco(Graphics gfx) {
		Graphics2D graphics = (Graphics2D)gfx;

		ArrayList<TipoFinestra> copiaStack = new ArrayList<>(stackElementiGrafici.size());
		copiaStack.addAll(stackElementiGrafici);

		for (TipoFinestra finestra : copiaStack) {
			switch (finestra) {
			case MAPPA:
				riquadroMappa.disegnaMappa(graphics);
				break;
			case STATISTICHE:
				riquadroStatistiche.disegnaStatistiche(graphics);
				break;
			case GRAFICA:
				boolean inCombattimento = riquadroCombattimento.isVisibile();
				riquadroLocazione.disegnaLocazione(graphics,
						inCombattimento ? riquadroCombattimento.getCombattente() : null,
						inCombattimento ? riquadroCombattimento.getAvversario() : null);
				break;
			case STATO:
				riquadroGruppo.disegnaStatus(graphics);
				dissolviSeCoperto(graphics, finestra);
				break;
			case INCANTESIMI_E_POZIONI:
				riquadroIncantesimiEPozioni.disegnaIncantesimi(graphics);
				dissolviSeCoperto(graphics, finestra);
				break;
			case TESTO:
				riquadroTesto.disegnaTesto(graphics);
				break;
			case MISSIONI:
				riquadroMissioni.disegnaMissioni(graphics);
				dissolviSeCoperto(graphics, finestra);
				break;
			case INFO_COMBATTIMENTO:
			case SFIDA:
				// Si disegnano dopo tutti gli altri, così nessuno li copre
				break;
			default:
				throw new IllegalArgumentException();
			}
		}
		riquadroCombattimento.disegnaInfoCombattimento(graphics);
		riquadroSfida.disegna(graphics);

//		gfx.setColor(Color.RED);
//		for (Rectangle rectangle : mappaCoordinateElementiGrafici.values()) {
//			gfx.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
//		}

		List<SpriteInterface> copiaSprites = new ArrayList<>(sprites);
		List<SpriteInterface> inactiveSprites = new ArrayList<>();
		for (SpriteInterface sprite : copiaSprites) {
			sprite.anima(graphics);
			if (!sprite.isAttivo()) {
				inactiveSprites.add(sprite);
			}
		}
		for (SpriteInterface inactiveSprite : inactiveSprites) {
			sprites.remove(inactiveSprite);
		}
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		aggiornaSchermo((Graphics2D)graphics);
	}


	private void disegnaFumetto(Graphics2D graphics) {
		if (fumettoAttivo == null && !codaFumetti.isEmpty()) {
			fumettoAttivo = codaFumetti.remove(0);
		}
		if (fumettoAttivo == null) {
			return;
		}
		fumettoAttivo.anima(graphics);
		if (!fumettoAttivo.isAttivo()) {
			fumettoAttivo = null;
		}
	}

	/**
	 * La rivelazione dell'artefatto trovato è al centro dello schermo; all'uscita vola verso il riquadro del gruppo.
	 */
	private void gestisciEventoArtefattoTrovato(NotificaArtefattoTrovato evento) {
		Rectangle gruppo = mappaCoordinateElementiGrafici.get(riquadroGruppo);
		Rectangle area = new Rectangle(0, 0, getWidth(), getHeight());
		Point destinazione = new Point((int) gruppo.getCenterX(), (int) gruppo.getCenterY());
		codaRivelazioni.add(new SpriteRivelazioneArtefatto(evento.getArtefatto(), evento.getLivelloMondo(), area, destinazione));
		BusEventi.pubblica(new InternoUiOccupata());
	}

	/**
	 * Solo nella schermata di gioco: altrove la rivelazione aspetta, perché il suo tempo avanza solo quando si disegna.
	 */
	private void disegnaRivelazione(Graphics2D graphics) {
		// Un effetto alla volta: con un annuncio a tutto schermo in corso, la rivelazione aspetta
		if (rivelazioneAttiva == null && !codaRivelazioni.isEmpty() && annuncioGlobaleAttivo == null) {
			rivelazioneAttiva = codaRivelazioni.remove(0);
		}
		if (rivelazioneAttiva == null) {
			return;
		}
		if (!codaRivelazioni.isEmpty()) {
			rivelazioneAttiva.accelera();
		}
		rivelazioneAttiva.anima(graphics);
		if (!rivelazioneAttiva.isAttivo()) {
			rivelazioneAttiva = null;
		}
	}

	/**
	 * A differenza degli altri sprite, ancorati a coordinate di riquadri validi solo in
	 * STATO_IN_GIOCO, l'annuncio globale è centrato sull'intero schermo e va quindi disegnato
	 * sopra il contenuto corrente indipendentemente dallo stato del canvas.
	 */
	private void disegnaAnnuncioGlobale(Graphics2D graphics) {
		// Un effetto alla volta: un annuncio (per esempio "MISSIONE COMPLETATA" per l'artefatto appena raccolto)
		// aspetta che la rivelazione in corso o in coda sia finita
		if (annuncioGlobaleAttivo == null && !codaAnnunciGlobali.isEmpty() && rivelazioneAttiva == null && codaRivelazioni.isEmpty()) {
			annuncioGlobaleAttivo = codaAnnunciGlobali.remove(0);
		}
		if (annuncioGlobaleAttivo == null) {
			return;
		}
		annuncioGlobaleAttivo.anima(graphics);
		if (!annuncioGlobaleAttivo.isAttivo()) {
			annuncioGlobaleAttivo = null;
		}
	}

	void preparaLocazione() {
		primoPiano(TipoFinestra.GRAFICA);
		riquadroLocazione.assegnaCoordinateAgliAvversari();
	}

	/**
	 * Gli avversari in locazione sono cambiati (è arrivata un'altra ondata): immagini e posizioni nuove.
	 */
	void assegnaCoordinateAPersonaggi() {
		riquadroLocazione.assegnaCoordinateAgliAvversari();
		repaint();
	}

	// Sprite decorativo statico, visibile solo mentre è mostrata la mappa a
	// tutto schermo, ancorato all'angolo inferiore sinistro.
	private void disegnaSferaMagica(Graphics2D graphics) {
		if (stato == StatoDisplayableCanvas.STATO_MAPPA && !vistaPartita.getUltimeNotizie().isEmpty()) {
			graphics.drawImage(ImageCache.sferaMagica, 0, altezzaTotaleSchermo - ImageCache.sferaMagica.getHeight(), null);
		}
	}

	private void aggiornaSchermo(Graphics2D graphics) {

		if (stato == StatoDisplayableCanvas.STATO_INTRO) {
			riquadroIntroOutro.intro(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_CARICARE) {
			riquadroIntroOutro.selezioneSlotDaCaricare(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_MESSAGGIO) {
			riquadroIntroOutro.scrivi(graphics, true);
		} else if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO) {
			inGioco(graphics);
			disegnaRivelazione(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_MAPPA) {
			mappaATuttoSchermo.disegnaMappaATuttoSchermo(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_SALVARE) {
			riquadroIntroOutro.selezioneSlotDaSalvare(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_CONFERMA_USCITA) {
			riquadroIntroOutro.confermaUscita(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_PERSO) {
			riquadroIntroOutro.perso(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_VINTO) {
			riquadroIntroOutro.vinto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_STATISTICHE) {
			riquadroIntroOutro.statistiche(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_PUNTEGGI) {
			riquadroIntroOutro.hiscore(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_INVENTARIO) {
			inventario.disegnaInventario(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_TROFEI) {
			paginaTrofei.disegna(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_TATTICHE) {
			tattiche.disegna(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_COMMERCIANTE) {
			commerciante.disegnaInventario(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_ALCHIMISTA) {
			alchimista.disegnaInventario(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_INCANTATORE) {
			incantatore.disegnaInventario(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_INTERMEZZO) {
			intermezzo.disegna(graphics);
			disegnaFumetto(graphics);
		}
		disegnaAnnuncioGlobale(graphics);
		barraIcone.disegna(graphics);
		disegnaSferaMagica(graphics);
		// I cartigli dell'aiuto stanno sopra tutto, anche sopra la sfera magica
		if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO) {
			riquadroGruppo.disegnaAiuto(graphics, getWidth(), getHeight());
			riquadroMissioni.disegnaAiuto(graphics, getWidth(), getHeight());
		}
		barraIcone.disegnaAiuto(graphics, getWidth(), getHeight());
		segnalaSeUiDiventataInattiva();
	}

	/**
	 * Rilevato ad ogni fotogramma, indipendentemente dallo stato del canvas: appena sprite
	 * transitori e annunci globali sono tutti esauriti, l'Automa può mostrare il prossimo
	 * intermezzo in attesa (se ce n'è uno).
	 */
	private void segnalaSeUiDiventataInattiva() {
		boolean occupataOra = !sprites.isEmpty() || annuncioGlobaleAttivo != null || !codaAnnunciGlobali.isEmpty()
				|| rivelazioneAttiva != null || !codaRivelazioni.isEmpty();
		if (uiOccupata && !occupataOra) {
			BusEventi.pubblica(new InternoUiInattiva());
		}
		uiOccupata = occupataOra;
	}

	/**
	 * Riporta l'unico elemento grafico che occupa tutto lo schermo nello stato corrente,
	 * oppure null se lo schermo è composto dallo stack degli elementi di gioco.
	 * Rispecchia le scelte di aggiornaSchermo().
	 */
	private Finestra finestraATuttoSchermo() {
		if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO) {
			return null;
		}
		if (stato == StatoDisplayableCanvas.STATO_MAPPA) {
			return mappaATuttoSchermo;
		}
		if (stato == StatoDisplayableCanvas.STATO_INVENTARIO) {
			return inventario;
		}
		if (stato == StatoDisplayableCanvas.STATO_TROFEI) {
			return paginaTrofei;
		}
		if (stato == StatoDisplayableCanvas.STATO_TATTICHE) {
			return tattiche;
		}
		if (stato == StatoDisplayableCanvas.STATO_COMMERCIANTE) {
			return commerciante;
		}
		if (stato == StatoDisplayableCanvas.STATO_ALCHIMISTA) {
			return alchimista;
		}
		if (stato == StatoDisplayableCanvas.STATO_INCANTATORE) {
			return incantatore;
		}
		if (stato == StatoDisplayableCanvas.STATO_INTERMEZZO) {
			return intermezzo;
		}
		return riquadroIntroOutro;
	}

	// Parte della interfaccia UI
	/**
	 * Fa ripartire l'intro: dai loghi, oppure dalla classifica se è appena stato
	 * registrato un punteggio. Va chiamato anche quando il canvas è già in
	 * STATO_INTRO (è lo stato iniziale del canvas, all'avvio del gioco).
	 */
	public void avviaIntro() {
		cambiaStato(StatoDisplayableCanvas.STATO_INTRO);
		if (introDallaClassifica) {
			riquadroIntroOutro.posizionaSuPunteggi();
			introDallaClassifica = false;
		} else {
			riquadroIntroOutro.resettaSequenza();
		}
		repaint();
	}

	/**
	 * A ogni battito del temporizzatore della UI: se non si è nell'intro, la si fa partire. L'intro poi va avanti da
	 * sola, a ogni fotogramma (vedi DisplayableCanvasIntroOutro.intro).
	 */
	public void avanzaIntro() {
		if (stato != StatoDisplayableCanvas.STATO_INTRO) {
			avviaIntro();
		}
	}

	/**
	 * Passa alla schermata di gioco; se si torna da un intermezzo ne libera le immagini e fa partire gli annunci che erano stati rimandati.
	 */
	public void iniziaGioco() {
		// Se si torna al gioco da un intermezzo, le sue immagini non servono più
		intermezzo.svuota();
		cambiaStato(StatoDisplayableCanvas.STATO_IN_GIOCO);
		abortisciFumetto();
		if (!annunciGlobaliRimandati.isEmpty()) {
			codaAnnunciGlobali.addAll(annunciGlobaliRimandati);
			annunciGlobaliRimandati.clear();
			BusEventi.pubblica(new InternoUiOccupata());
		}
		repaint();
	}

	/**
	 * Passa alla mappa a tutto schermo, centrata sul gruppo.
	 */
	public void mappa() {
		cambiaStato(StatoDisplayableCanvas.STATO_MAPPA);
		mappaATuttoSchermo.centraSuGiocatore();
		repaint();
	}

	/**
	 * Passa all'inventario.
	 */
	public void inventario() {
		cambiaStato(StatoDisplayableCanvas.STATO_INVENTARIO);
		repaint();
	}

	/**
	 * La pagina dei trofei, aperta dall'inventario.
	 */
	public void trofei() {
		paginaTrofei.apri();
		cambiaStato(StatoDisplayableCanvas.STATO_TROFEI);
		repaint();
	}

	/**
	 * Chi sa chiedere un testo al giocatore per conto della UI (il Prompt), per la schermata delle tattiche che rinomina le voci
	 */
	public void setRichiestaTesto(java.util.function.BiConsumer<String, java.util.function.Consumer<String>> richiestaTesto,
								  java.util.function.Consumer<java.util.function.Consumer<String>> annullaRichiesta) {
		tattiche.setRichiestaTesto(richiestaTesto, annullaRichiesta);
	}

	/**
	 * Cambia la schermata mostrata: lasciando quella delle tattiche, il nome di una voce che si stava scrivendo non si aspetta più.
	 */
	private void cambiaStato(StatoDisplayableCanvas nuovo) {
		if (stato == StatoDisplayableCanvas.STATO_TATTICHE && nuovo != StatoDisplayableCanvas.STATO_TATTICHE) {
			tattiche.annullaLaRichiestaDiTesto();
		}
		stato = nuovo;
	}

	/**
	 * La schermata delle tattiche, aperta dall'inventario; arriva di nuovo quando cambia il personaggio scelto o l'ordine del gruppo.
	 */
	public void tattiche(VistaPersonaggio personaggio, TatticaMD tattica, TatticaMD tatticaPredefinita,
						 java.util.Set<com.threeamigos.foresta.tipi.tattiche.TipoAzione> azioniPossibili) {
		tattiche.impostaTattica(personaggio, tattica, tatticaPredefinita, azioniPossibili);
		cambiaStato(StatoDisplayableCanvas.STATO_TATTICHE);
		repaint();
	}

	/**
	 * Imposta lo scambio e il personaggio che l'inventario mostra.
	 */
	public void impostaScambioInventario(VistaScambio scambio, VistaPersonaggio personaggio) {
		inventario.impostaScambio(scambio, personaggio);
		repaint();
	}

	/**
	 * Passa alla schermata del commerciante, che accoglie il gruppo con un fumetto.
	 */
	public void commerciante() {
		cambiaStato(StatoDisplayableCanvas.STATO_COMMERCIANTE);
		notificaFumetto("Come posso aiutarvi?", commerciante.getCoordinateFumetto());
		repaint();
	}

	/**
	 * Imposta il negozio e lo scambio che la schermata del commerciante mostra.
	 */
	public void impostaScambioCommerciante(TipoNegozio negozio, VistaScambio scambio) {
		commerciante.impostaNegozio(negozio);
		commerciante.impostaScambio(scambio);
		repaint();
	}

	/**
	 * @param messaggio il fumetto con cui accoglie l'incantatore, o null per nessuno
	 */
	public void incantatore(String messaggio) {
		cambiaStato(StatoDisplayableCanvas.STATO_INCANTATORE);
		if (messaggio != null) {
			notificaFumetto(messaggio, incantatore.getCoordinateFumetto());
		}
		repaint();
	}

	/**
	 * Imposta il banco di lavoro che la schermata dell'incantatore mostra.
	 */
	public void impostaBancoIncantatore(VistaBancoDiLavoro banco) {
		incantatore.impostaBanco(banco);
		repaint();
	}

	/**
	 * Imposta le offerte che la schermata dell'alchimista mostra.
	 */
	public void impostaOfferteAlchimista(List<? extends VistaOffertaConsumabile> offerte) {
		alchimista.impostaOfferte(offerte);
		repaint();
	}

	/**
	 * Apre la schermata dell'alchimista, che chiede che cosa serve (l'oroscopo del giorno lo dice nell'intermezzo
	 * d'ingresso).
	 */
	public void alchimista() {
		cambiaStato(StatoDisplayableCanvas.STATO_ALCHIMISTA);
		notificaFumetto("Cosa posso fare per voi?", alchimista.getCoordinateFumetto());
		repaint();
	}

	/**
	 * Aggiorna la barra delle icone con i comandi possibili.
	 */
	public void impostaAzioniIcone() {
		barraIcone.impostaAzioni();
		repaint();
	}

	/**
	 * Le testate arrivano dall'Automa (RichiestaSelezioneSlotPerSalvataggio), lette una volta sola all'apertura della
	 * schermata: ognuna ricostruisce dal file il gruppo del salvataggio con i suoi personaggi, troppo per farlo a ogni
	 * frame. Mentre la schermata è aperta i file non cambiano (si salva solo dopo averla chiusa).
	 */
	public void selezioneSlotSalvataggioDaSalvare(Collection<TestataSalvataggio> salvataggiDisponibili) {
		riquadroIntroOutro.setSalvataggiDisponibili(salvataggiDisponibili);
		cambiaStato(StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_SALVARE);
		repaint();
	}
	
	/**
	 * Passa alla schermata che chiede conferma per uscire dal gioco.
	 */
	public void confermaUscita() {
		cambiaStato(StatoDisplayableCanvas.STATO_CONFERMA_USCITA);
		repaint();
	}
	
	/**
	 * Passa alla schermata della sconfitta; se c'è già, fa avanzare il suo testo.
	 */
	public void perso() {
		if (stato != StatoDisplayableCanvas.STATO_PERSO) {
			cambiaStato(StatoDisplayableCanvas.STATO_PERSO);
			riquadroIntroOutro.resettaSequenza();
		} else {
			riquadroIntroOutro.incrementaSequenza(Misc.PERSO.length);
		}
		repaint();
	}

	/**
	 * Passa alla schermata della vittoria; se c'è già, fa avanzare il suo testo.
	 */
	public void vinto() {
		if (stato != StatoDisplayableCanvas.STATO_VINTO) {
			cambiaStato(StatoDisplayableCanvas.STATO_VINTO);
			riquadroIntroOutro.resettaSequenza();
		} else {
			riquadroIntroOutro.incrementaSequenza(Misc.VINTO.length);
		}
		repaint();
	}

	/**
	 * Passa alla schermata delle statistiche di fine partita.
	 */
	public void mostraStatistiche() {
		cambiaStato(StatoDisplayableCanvas.STATO_STATISTICHE);
		repaint();
	}

	/**
	 * Passa alla schermata dei punteggi migliori; l'introduzione che segue parte da quella classifica.
	 */
	public void mostraPunteggi() {
		cambiaStato(StatoDisplayableCanvas.STATO_PUNTEGGI);
		// Il motore torna subito all'intro: che parta dalla classifica appena aggiornata
		introDallaClassifica = true;
		repaint();
	}

	/**
	 * Passa all'intermezzo e ne mostra la pagina.
	 */
	public void mostraPaginaIntermezzo(PaginaIntermezzo pagina) {
		cambiaStato(StatoDisplayableCanvas.STATO_INTERMEZZO);
		intermezzo.mostra(pagina);
		repaint();
	}

	/**
	 * La pagina dell'intermezzo riparte da quei secondi dalla sua comparsa: dal fumetto successivo.
	 */
	public void saltaNellaPaginaIntermezzo(double secondi) {
		intermezzo.saltaA(secondi);
		repaint();
	}

	/**
	 * Mostra un messaggio a tutto schermo, in caratteri grandi.
	 */
	public void scriviGrande(String messaggio) {
		cambiaStato(StatoDisplayableCanvas.STATO_MESSAGGIO);
		riquadroIntroOutro.impostaMessaggio(messaggio);
		repaint();
	}

	/**
	 * Aggiunge una frase al riquadro di testo.
	 */
	public void notifica(String messaggio) {
		riquadroTesto.addString(messaggio);
		repaint();
	}

	/**
	 * Un nuovo paragrafo è separato dal testo precedente da una riga vuota.
	 */
	public void notificaParagrafo(String messaggio) {
		aggiungiParagrafo(messaggio);
		repaint();
	}

	private void aggiungiParagrafo(String messaggio) {
		riquadroTesto.addString("");
		riquadroTesto.addString(messaggio);
	}

	/**
	 * Ripopola il pannello di testo dopo un caricamento, dal più vecchio al più recente
	 * e con la stessa impaginazione (paragrafi o continuazioni) con cui i messaggi erano
	 * stati mostrati.
	 *
	 * @param messaggiDalPiuRecenteAlPiuVecchio come li restituisce {@code Notizie.getUltimiMessaggi()}
	 */
	public void ripristinaMessaggi(List<MessaggioMD> messaggiDalPiuRecenteAlPiuVecchio) {
		riquadroTesto.clear();
		for (int i = messaggiDalPiuRecenteAlPiuVecchio.size() - 1; i >= 0; i--) {
			MessaggioMD messaggioMD = messaggiDalPiuRecenteAlPiuVecchio.get(i);
			if (messaggioMD.isParagrafo()) {
				aggiungiParagrafo(messaggioMD.getTesto());
			} else {
				riquadroTesto.addString(messaggioMD.getTesto());
			}
		}
		repaint();
	}

	/**
	 * Segnala con uno sprite che un personaggio è morto o è tornato in vita.
	 */
	public void notificaVariazioneStatoVitale(VistaPersonaggio personaggio) {
		if (!personaggio.isVivo()) {
			aggiungiSprite(riquadroLocazione.notificaMorte(personaggio));
		}
		if (!personaggio.isPNG()) {
			primoPiano(TipoFinestra.STATO);
		}
	}

	/**
	 * Mostra con uno sprite la variazione di salute di un personaggio, e porta in primo piano la finestra dello stato se è del gruppo.
	 */
	public void variaSalute(VistaPersonaggio personaggio, int variazione) {
		if (!personaggio.isPNG()) {
			primoPiano(TipoFinestra.STATO);
		}
		// Anche i personaggi del gruppo, che ora si vedono in locazione
		aggiungiSprite(riquadroLocazione.variaSalute(personaggio, variazione));
	}

	/**
	 * Mostra con uno sprite la variazione di magia di un personaggio, e porta in primo piano la finestra dello stato se è del gruppo.
	 */
	public void variaMagia(VistaPersonaggio personaggio, int variazione) {
		if (!personaggio.isPNG()) {
			primoPiano(TipoFinestra.STATO);
		}
		aggiungiSprite(riquadroLocazione.variaMagia(personaggio, variazione));
	}

	/**
	 * Mostra con uno sprite che il gruppo ha raccolto un oggetto.
	 */
	public void raccogliOggetto() {
		aggiungiSprite(riquadroLocazione.raccogliOggetto());
	}

	void notificaFumetto(String testo, CoordinateFumetto coordinateFumetto) {
		notificaFumetto(testo, coordinateFumetto.getX(), coordinateFumetto.getY(), coordinateFumetto.getPointToX(), coordinateFumetto.getPointToY());
	}

	/**
	 * Mostra un fumetto con quel testo in quel punto, con la punta verso quell'altro; se c'è già quello stesso fumetto lo fa ripartire, uno diverso lo interrompe.
	 */
	public void notificaFumetto(String testo, int x, int y, int pointToX, int pointToY) {
		if (fumettoAttivo != null) {
			if (testo.equals(fumettoAttivo.getTesto())) {
				fumettoAttivo.resettaImpulsi();
				return;
			}
			// Un fumetto diverso interrompe immediatamente quello in corso, scartando
			// anche eventuali fumetti già in coda dietro di lui
			fumettoAttivo = null;
			codaFumetti.clear();
		}
		codaFumetti.add(new SpriteFumetto(testo, larghezzaSchermo / 5, x, y, DoomdarkFontMedium.getInstance(), DoomdarkColorModel.Color.BLACK, pointToX, pointToY));
	}

	private void abortisciFumetto() {
		fumettoAttivo = null;
		codaFumetti.clear();
	}

	/**
	 * Mostra un annuncio al centro dello schermo (per esempio una missione disponibile o completata); in un intermezzo lo rimanda a dopo.
	 */
	public void notificaAnnuncioGlobale(String etichetta, String messaggio) {
		SpriteAnnuncioGlobale annuncio = new SpriteAnnuncioGlobale(etichetta, messaggio, larghezzaSchermo, altezzaSchermo);
		if (stato == StatoDisplayableCanvas.STATO_INTERMEZZO) {
			// Costruire lo sprite subito non lo fa scadere: il suo tempo trascorso parte
			// dalla prima anima(), non dalla costruzione, quindi può aspettare in coda.
			annunciGlobaliRimandati.add(annuncio);
			return;
		}
		codaAnnunciGlobali.add(annuncio);
		BusEventi.pubblica(new InternoUiOccupata());
	}

	/**
	 * Mostra con uno sprite l'effetto di stato appena subito da un personaggio.
	 */
	public void aggiungiEffettoDiStato(VistaPersonaggio personaggio, TipoEffettoDiStato effettoDiStato, DoomdarkColorModel.Color colore) {
		aggiungiSprite(riquadroLocazione.aggiungiEffettoDiStato(personaggio, effettoDiStato, colore));
	}

	/**
	 * Mostra con uno sprite il modificatore appena applicato a un personaggio.
	 */
	public void aggiungiModificatore(VistaPersonaggio personaggio, String descrizione, DoomdarkColorModel.Color colore) {
		aggiungiSprite(riquadroLocazione.aggiungiModificatore(personaggio, descrizione, colore));
	}

	/**
	 * Mostra con uno sprite l'interazione fra tipo di danno e stato appena scattata su un personaggio.
	 */
	public void aggiungiInterazione(VistaPersonaggio personaggio, TipoInterazioneConEffettiDiStato interazione) {
		aggiungiSprite(riquadroLocazione.aggiungiInterazione(personaggio, interazione));
	}

	/**
	 * Un testo che sale sopra il personaggio, come il nome di un'interazione
	 */
	public void aggiungiTestoSulPersonaggio(VistaPersonaggio personaggio, String testo, java.awt.Color colore) {
		aggiungiSprite(riquadroLocazione.aggiungiTestoSulPersonaggio(personaggio, testo, colore));
	}

	private void aggiungiSprite(SpriteInterface sprite) {
		if (sprite != null) {
			sprites.add(sprite);
			BusEventi.pubblica(new InternoUiOccupata());
		}
	}

	private Finestra recuperaFinestra(TipoFinestra finestra) {
		switch (finestra) {
			case INTRO_OUTRO:
				return riquadroIntroOutro;
			case MAPPA:
				return riquadroMappa;
			case STATISTICHE:
				return riquadroStatistiche;
			case GRAFICA:
				return riquadroLocazione;
			case STATO:
				return riquadroGruppo;
			case INCANTESIMI_E_POZIONI:
				return riquadroIncantesimiEPozioni;
			case TESTO:
				return riquadroTesto;
			case MISSIONI:
				return riquadroMissioni;
			case INFO_COMBATTIMENTO:
				return riquadroCombattimento;
			case SFIDA:
				return riquadroSfida;
			case MAPPA_A_TUTTO_SCHERMO:
				return mappaATuttoSchermo;
			case INVENTARIO:
				return inventario;
			default:
				throw new IllegalArgumentException("Elemento grafico non valido: " + finestra);
		}
	}

	private class GestoreMouse implements MouseListener, MouseMotionListener, MouseWheelListener {

		// AWT segnala entrata e uscita solo per il canvas nel suo complesso: per averle
		// per singolo riquadro va ricordato su quale si trovava il cursore.
		private Finestra finestraSottoIlCursore;

		// mouseClicked scatta una volta per ogni click: su un doppio click arrivano quindi
		// sia un evento con clickCount 1 sia uno con clickCount 2. Il primo va quindi rimandato,
		// per poterlo scartare se nel frattempo arriva il secondo (altrimenti processaClick e
		// processaDoppioClick scatterebbero entrambi per un solo doppio click).
		//
		// Limiti noti, lasciati così perché oggi innocui:
		// - il timer ricorda la finestra cliccata e nessuno lo ferma quando la schermata cambia: se entro
		//   l'intervallo la schermata si chiude, processaClick scatta lo stesso sulla finestra che non si vede
		//   più (oggi apre o chiude al massimo i dettagli di un oggetto). Per correggerlo basterebbe controllare
		//   nel timer che la finestra sia ancora quella mostrata, o fermare il timer a ogni cambio di schermata;
		// - l'intervallo è fisso a 175 ms, mentre Swing riconosce il doppio click con quello del sistema (di
		//   solito 400-500 ms): un doppio click più lento di 175 ms fa scattare sia il click singolo sia il
		//   doppio. Con l'intervallo del sistema il click singolo risponderebbe più lentamente.
		private final int intervalloDoppioClick = intervalloDoppioClick();
		private Timer timerClickSingolo;

		private int intervalloDoppioClick() {
			return 175;
//			Object valore = Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
//			return valore instanceof Integer ? (Integer) valore : 500;
		}

		private class RisultatoRicerca {
			final Finestra finestra;
			final int xRelativoAFinestra;
			final int yRelativoAFinestra;
			RisultatoRicerca(Finestra finestra, int xRelativoAFinestra, int yRelativoAFinestra) {
				this.finestra = finestra;
				this.xRelativoAFinestra = xRelativoAFinestra;
				this.yRelativoAFinestra = yRelativoAFinestra;
			}
		}

		private RisultatoRicerca trovaFinestra(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			// La barra delle icone è sempre disegnata in primo piano sopra a tutto
			// il resto (vedi aggiornaSchermo), quindi va controllata per prima.
			RisultatoRicerca risultatoBarraIcone = creaRisultato(barraIcone, x, y);
			if (risultatoBarraIcone != null) {
				return risultatoBarraIcone;
			}
			// Fuori dal gioco lo schermo è occupato da un unico elemento grafico:
			// non c'è nessuno stack da percorrere.
			Finestra finestraATuttoSchermo = finestraATuttoSchermo();
			if (finestraATuttoSchermo != null) {
				return creaRisultato(finestraATuttoSchermo, x, y);
			}
			ArrayList<TipoFinestra> copiaStack = new ArrayList<>(stackElementiGrafici.size());
			copiaStack.addAll(stackElementiGrafici);
			// Lo stack viene disegnato dal fondo (indice 0) verso il primo piano (ultimo
			// indice), quindi va percorso al contrario: l'evento spetta all'elemento
			// più in primo piano fra quelli che contengono il punto.
			for (int indice = copiaStack.size() - 1; indice >= 0; indice--) {
				RisultatoRicerca risultato = creaRisultato(recuperaFinestra(copiaStack.get(indice)), x, y);
				if (risultato != null) {
					return risultato;
				}
			}
			return null;
		}

		/**
		 * @return true se la finestra è un riquadro impilato che non era l'ultimo, ed è stato portato davanti
		 */
		private boolean portaInPrimoPianoSeImpilato(Finestra finestra) {
			List<TipoFinestra> impilati = riquadriImpilati();
			for (TipoFinestra tipo : impilati) {
				if (recuperaFinestra(tipo) == finestra) {
					if (tipo == impilati.get(impilati.size() - 1)) {
						return false;
					}
					primoPiano(tipo);
					return true;
				}
			}
			return false;
		}

		private RisultatoRicerca creaRisultato(Finestra finestra, int x, int y) {
			if (!finestra.isVisibile()) {
				return null;
			}
			Rectangle rettangolo = mappaCoordinateElementiGrafici.get(finestra);
			// La barra decide da sé quali punti sono suoi: le icone ingrandite escono dal suo rettangolo
			boolean dentro = finestra == barraIcone
					? barraIcone.contiene(x - rettangolo.x, y - rettangolo.y)
					: rettangolo.contains(x, y);
			if (!dentro) {
				return null;
			}
			return new RisultatoRicerca(finestra, x - rettangolo.x, y - rettangolo.y);
		}

		/**
		 * Notifica uscita ed entrata quando il cursore passa da un riquadro a un altro.
		 * Le coordinate assolute servono per l'uscita, che va comunicata al riquadro
		 * precedente e cade quindi fuori dal suo rettangolo.
		 */
		private void aggiornaFinestraSottoIlCursore(RisultatoRicerca risultatoRicerca, int x, int y) {
			Finestra nuovaFinestra = risultatoRicerca == null ? null : risultatoRicerca.finestra;
			if (nuovaFinestra == finestraSottoIlCursore) {
				return;
			}
			if (finestraSottoIlCursore != null) {
				Rectangle rettangolo = mappaCoordinateElementiGrafici.get(finestraSottoIlCursore);
				finestraSottoIlCursore.processaUscita(x - rettangolo.x, y - rettangolo.y);
			}
			finestraSottoIlCursore = nuovaFinestra;
			if (risultatoRicerca != null) {
				nuovaFinestra.processaEntrata(risultatoRicerca.xRelativoAFinestra, risultatoRicerca.yRelativoAFinestra);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			// Il click su un riquadro impilato che non è l'ultimo lo porta davanti e basta
			if (portaInPrimoPianoSeImpilato(finestra)) {
				repaint();
				return;
			}

			Finestra.Tasto tasto;
			if (SwingUtilities.isLeftMouseButton(e)) {
				tasto = Finestra.Tasto.SINISTRO;
			} else if (SwingUtilities.isRightMouseButton(e)) {
				tasto = Finestra.Tasto.DESTRO;
			} else if (SwingUtilities.isMiddleMouseButton(e)) {
				tasto = Finestra.Tasto.CENTRALE;
			} else {
				return;
			}

			if (!finestra.gestisceDoppioClick()) {
				// Nessuna attesa: questa finestra non distingue il doppio click dal singolo,
				// quindi ogni click va processato subito.
				finestra.processaClick(x, y, tasto);
				repaint();
				return;
			}

			if (timerClickSingolo != null) {
				timerClickSingolo.stop();
				timerClickSingolo = null;
			}
			if (e.getClickCount() >= 2) {
				finestra.processaDoppioClick(x, y, tasto);
				repaint();
			} else {
				timerClickSingolo = new Timer(intervalloDoppioClick, evento -> {
					finestra.processaClick(x, y, tasto);
					repaint();
				});
				timerClickSingolo.setRepeats(false);
				timerClickSingolo.start();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			if (SwingUtilities.isLeftMouseButton(e)) {
				finestra.processaPressione(x, y, Finestra.Tasto.SINISTRO);
			} else if (SwingUtilities.isRightMouseButton(e)) {
				finestra.processaPressione(x, y, Finestra.Tasto.DESTRO);
			} else if (SwingUtilities.isMiddleMouseButton(e)) {
				finestra.processaPressione(x, y, Finestra.Tasto.CENTRALE);
			}
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			if (SwingUtilities.isLeftMouseButton(e)) {
				finestra.processaRilascio(x, y, Finestra.Tasto.SINISTRO);
			} else if (SwingUtilities.isRightMouseButton(e)) {
				finestra.processaRilascio(x, y, Finestra.Tasto.DESTRO);
			} else if (SwingUtilities.isMiddleMouseButton(e)) {
				finestra.processaRilascio(x, y, Finestra.Tasto.CENTRALE);
			}
			repaint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// Entrando nel canvas si stabilisce su quale riquadro si trova il cursore
			aggiornaFinestraSottoIlCursore(trovaFinestra(e), e.getX(), e.getY());
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// Uscendo dal canvas si esce anche dal riquadro su cui si era
			aggiornaFinestraSottoIlCursore(null, e.getX(), e.getY());
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			// Anche trascinando si può passare da un riquadro all'altro: chi viene lasciato riceve processaUscita
			aggiornaFinestraSottoIlCursore(risultatoRicerca, e.getX(), e.getY());
			if (risultatoRicerca == null) {
				repaint();
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			finestra.processaTrascinamento(x, y);
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			aggiornaFinestraSottoIlCursore(risultatoRicerca, e.getX(), e.getY());
			if (risultatoRicerca == null) {
				repaint();
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			finestra.processaMovimento(x, y);
			repaint();
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			int rotazioni = e.getWheelRotation();
			if (rotazioni < 0) {
				finestra.processaRotella(x, y, -rotazioni, Finestra.MovimentoRotella.SU);
			} else {
				finestra.processaRotella(x, y, rotazioni, Finestra.MovimentoRotella.GIU);
			}
			repaint();
		}
	}
}
