package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaBancoDiLavoro;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.strumenti.temporizzatore.Temporizzatore;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.StatoDelloSchermo;
import com.threeamigos.foresta.tipi.TipoFinestra;
import com.threeamigos.foresta.tipi.TipoNegozio;
import com.threeamigos.foresta.ui.barra.BarraIcone;
import com.threeamigos.foresta.ui.barra.BarraIconeDisegnabile;
import com.threeamigos.foresta.ui.tutorial.DisplayableCanvasTutorial;
import com.threeamigos.foresta.ui.barra.DisplayableCanvasBarraIcone;
import com.threeamigos.foresta.ui.barra.DisplayableCanvasBarraIconeDock;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.riquadri.*;
import com.threeamigos.foresta.ui.schermate.*;
import com.threeamigos.foresta.ui.sprite.*;
import com.threeamigos.foresta.ui.tattiche.DisplayableCanvasTattiche;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Locale;

/**
 * Il canvas grafico del gioco in Swing: disegna lo schermo del gioco ({@link SchermoDelGioco}, nel modulo comune, che sa quale
 * schermata è quella corrente e fa da facciata per tutto ciò che la UI deve mostrare: lo dà {@link #getSchermo()}), passa il mouse
 * al router degli input e fa girare le animazioni.
 * <p>
 * Nella schermata di gioco disegna i riquadri (locazione, gruppo, combattimento, mappa, missioni, testo, statistiche, incantesimi e
 * pozioni) nell'ordine della pila e la barra delle icone, e sopra a tutto gli sprite (numeri che salgono, fumetti, annunci). Le
 * animazioni girano nel metodo {@link #run()}, a {@code Temporizzatore.FRAME_PER_SECONDO} fotogrammi al secondo; il disegno vero
 * avviene sul thread di Swing.
 */
public class DisplayableCanvas extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	public static final int ORIENTAMENTO_ORIZZONTALE = 0;
	public static final int ORIENTAMENTO_VERTICALE = 1;

	private final transient DisplayableCanvasIntroOutro riquadroIntroOutro;
	private final transient DisplayableCanvasRiquadroMappa riquadroMappa;
	private final transient DisplayableCanvasRiquadroLocazione riquadroLocazione;
	private final transient DisplayableCanvasRiquadroStatistiche riquadroStatistiche;
	private final transient DisplayableCanvasRiquadroCombattimento riquadroCombattimento;
	private final transient DisplayableCanvasRiquadroSfida riquadroSfida;
	private final transient DisplayableCanvasRiquadroTesto riquadroTesto;
	private final transient DisplayableCanvasRiquadroGruppo riquadroGruppo;
	private final transient DisplayableCanvasRiquadroIncantesimiEPozioni riquadroIncantesimiEPozioni;
	private final transient DisplayableCanvasRiquadroMissioni riquadroMissioni;
	private final transient DisplayableCanvasMappaATuttoSchermo mappaATuttoSchermo;
	private final transient DisplayableCanvasInventario inventario;
	private final transient DisplayableCanvasTrofei paginaTrofei;
	private final transient DisplayableCanvasBestiario bestiario;
	private final transient DisplayableCanvasGrimorio grimorio;
	private final transient DisplayableCanvasTattiche tattiche;
	// Armaiolo e venditore di pergamene: una sola schermata, che cambia secondo il negozio
	private final transient DisplayableCanvasCommerciante commerciante;
	private final transient DisplayableCanvasScambiatoreConsumabili alchimista;
	private final transient DisplayableCanvasIncantatore incantatore;
	private final transient BarraIconeDisegnabile barraIcone;
	private final transient DisplayableCanvasTutorial tutorial;
	private final transient DisplayableCanvasIntermezzo intermezzo;

	private final transient Schermo schermo;

	private transient volatile Thread animatore;
	private volatile boolean animatoreInAzione = false;

	/**
	 * @param larghezza  la larghezza della finestra, barra delle icone compresa
	 * @param altezza    l'altezza della finestra, barra delle icone compresa
	 * @param barraDock  vero per la barra delle icone che si ingrandisce sotto il cursore (DisplayableCanvasBarraIconeDock), solo in
	 *                   orientamento orizzontale; altrimenti la barra classica
	 * @param vistaPartita lo stato della partita da mostrare, in sola lettura: lo ricevono tutti i riquadri
	 */
	public DisplayableCanvas(Orientamento orientamento, int larghezza, int altezza, boolean barraDock, GestorePunteggi gestorePunteggi,
							 VistaPartita vistaPartita) {
		super();
		DisposizioneDelloSchermo disposizione = new DisposizioneDelloSchermo(orientamento, larghezza, altezza, new MisureDelleImmaginiAwt(),
				LivelloDiZoom.valore());
		int larghezzaContenuto = disposizione.getLarghezzaContenuto();
		int altezzaContenuto = disposizione.getAltezzaContenuto();
		setSize(larghezza, altezza);
		setBackground(Color.black);

		riquadroIntroOutro = new DisplayableCanvasIntroOutro(larghezzaContenuto, altezzaContenuto, gestorePunteggi, vistaPartita);
		intermezzo = new DisplayableCanvasIntermezzo(larghezzaContenuto, altezzaContenuto, riquadroIntroOutro);
		Area mappa = disposizione.getMappa();
		riquadroMappa = new DisplayableCanvasRiquadroMappa(mappa.getX(), mappa.getY(), larghezzaContenuto, altezzaContenuto, vistaPartita);
		Area locazione = disposizione.getLocazione();
		riquadroLocazione = new DisplayableCanvasRiquadroLocazione(locazione.getX(), locazione.getY(), locazione.getLarghezza(), vistaPartita);
		Area statistiche = disposizione.getStatistiche();
		riquadroStatistiche = new DisplayableCanvasRiquadroStatistiche(statistiche.getX(), statistiche.getY(), vistaPartita);
		// Centrato sul testo sotto la locazione, non sull'intero schermo
		riquadroCombattimento = new DisplayableCanvasRiquadroCombattimento(disposizione.getColonnaDestraX(), altezzaContenuto,
				disposizione.getYPreferitaDelCombattimento(), vistaPartita);
		riquadroSfida = new DisplayableCanvasRiquadroSfida(larghezzaContenuto, altezzaContenuto);
		Area testo = disposizione.getTesto();
		riquadroTesto = new DisplayableCanvasRiquadroTesto(testo.getX(), testo.getY(), testo.getLarghezza(), testo.getAltezza());
		riquadroGruppo = new DisplayableCanvasRiquadroGruppo(disposizione.getGruppo().getX(), disposizione.getGruppo().getY(), vistaPartita);
		riquadroIncantesimiEPozioni = new DisplayableCanvasRiquadroIncantesimiEPozioni(disposizione.getIncantesimi().getX(),
				disposizione.getIncantesimi().getY(), vistaPartita);
		riquadroMissioni = new DisplayableCanvasRiquadroMissioni(disposizione.getMissioni().getX(), disposizione.getMissioni().getY(), vistaPartita);
		mappaATuttoSchermo = new DisplayableCanvasMappaATuttoSchermo(larghezzaContenuto, altezzaContenuto, vistaPartita);
		inventario = new DisplayableCanvasInventario(larghezzaContenuto, altezzaContenuto, vistaPartita);
		paginaTrofei = new DisplayableCanvasTrofei(larghezzaContenuto, altezzaContenuto, vistaPartita);
		bestiario = new DisplayableCanvasBestiario(larghezzaContenuto, altezzaContenuto, vistaPartita);
		grimorio = new DisplayableCanvasGrimorio(larghezzaContenuto, altezzaContenuto);
		tattiche = new DisplayableCanvasTattiche(larghezzaContenuto, altezzaContenuto, vistaPartita);
		commerciante = new DisplayableCanvasCommerciante(larghezzaContenuto, altezzaContenuto, vistaPartita);
		alchimista = new DisplayableCanvasScambiatoreConsumabili(larghezzaContenuto, altezzaContenuto, vistaPartita);
		incantatore = new DisplayableCanvasIncantatore(larghezzaContenuto, altezzaContenuto, vistaPartita);
		Area barra = disposizione.getBarra();
		barraIcone = barraDock && orientamento == Orientamento.ORIZZONTALE
				? new DisplayableCanvasBarraIconeDock(barra.getX(), barra.getY(), barra.getLarghezza(), barra.getAltezza(), vistaPartita)
				: new DisplayableCanvasBarraIcone(orientamento == Orientamento.ORIZZONTALE ? ORIENTAMENTO_ORIZZONTALE : ORIENTAMENTO_VERTICALE,
				barra.getX(), barra.getY(), barra.getLarghezza(), barra.getAltezza(), vistaPartita);

		schermo = new Schermo(disposizione, vistaPartita);
		// Il fumetto del tutorial lo sa disegnare e chiudere questo toolkit: da qui i suggerimenti si aprono
		tutorial = new DisplayableCanvasTutorial(schermo);
		schermo.getTutor().setAbilitato(true);

		GestoreMouse gestoreMouse = new GestoreMouse();
		addMouseListener(gestoreMouse);
		addMouseMotionListener(gestoreMouse);
		addMouseWheelListener(gestoreMouse);
	}

	/**
	 * Lo schermo del gioco: la facciata a cui si chiede che cosa mostrare.
	 */
	public SchermoDelGioco<SpriteInterface, SpriteAnnuncioGlobale, SpriteFumetto, SpriteRivelazioneArtefatto> getSchermo() {
		return schermo;
	}

	/**
	 * Abilita sulla mappa a tutto schermo il joystick virtuale (vedi {@link MappaATuttoSchermo#abilitaJoystickVirtuale()}).
	 */
	public void setJoystickVirtuale(boolean abilitato) {
		if (abilitato) {
			mappaATuttoSchermo.abilitaJoystickVirtuale();
		}
	}

	/**
	 * Lo schermo del gioco con i riquadri, le schermate e gli sprite di Swing.
	 */
	private final class Schermo extends SchermoDelGioco<SpriteInterface, SpriteAnnuncioGlobale, SpriteFumetto, SpriteRivelazioneArtefatto> {

		Schermo(DisposizioneDelloSchermo disposizione, VistaPartita vistaPartita) {
			super(disposizione, vistaPartita);
			prepara();
			// Gli sprite che i riquadri di Swing fanno da sé
			BusEventi.iscriviti(InternoCreazioneSpriteAnnuncioGlobale.class, e -> accodaAnnuncio(e.getSprite()));
			BusEventi.iscriviti(InternoCreazioneSpriteEffettoDiStato.class, e -> aggiungiSprite(e.getSprite()));
			BusEventi.iscriviti(InternoCreazioneSpriteFumettoATempo.class, e -> accodaFumetto(e.getSprite()));
			BusEventi.iscriviti(InternoCreazioneSpriteInDissolvenza.class, e -> aggiungiSprite(e.getSprite()));
		}

		@Override
		protected RiquadroTesto riquadroTesto() {
			return riquadroTesto;
		}

		@Override
		protected RiquadroGruppo riquadroGruppo() {
			return riquadroGruppo;
		}

		@Override
		protected RiquadroIncantesimiEPozioni riquadroIncantesimiEPozioni() {
			return riquadroIncantesimiEPozioni;
		}

		@Override
		protected RiquadroMissioni riquadroMissioni() {
			return riquadroMissioni;
		}

		@Override
		protected RiquadroLocazione riquadroLocazione() {
			return riquadroLocazione;
		}

		@Override
		protected RiquadroCombattimento riquadroCombattimento() {
			return riquadroCombattimento;
		}

		@Override
		protected RiquadroSfida riquadroSfida() {
			return riquadroSfida;
		}

		@Override
		protected Finestra riquadroMappa() {
			return riquadroMappa;
		}

		@Override
		protected Finestra riquadroStatistiche() {
			return riquadroStatistiche;
		}

		@Override
		protected BarraIcone barraIcone() {
			return (BarraIcone) barraIcone;
		}

		@Override
		protected SchermateIntroOutro introOutro() {
			return riquadroIntroOutro;
		}

		@Override
		protected SchermataIntermezzo intermezzo() {
			return intermezzo;
		}

		@Override
		protected SchermataMappa mappaATuttoSchermo() {
			return mappaATuttoSchermo;
		}

		@Override
		protected SchermataDiScambio schermataInventario() {
			return inventario;
		}

		@Override
		protected SchermataDiScambio schermataCommerciante() {
			return commerciante;
		}

		@Override
		protected SchermataDiScambio schermataAlchimista() {
			return alchimista;
		}

		@Override
		protected SchermataDiScambio schermataIncantatore() {
			return incantatore;
		}

		@Override
		protected SchermataTattiche schermataTattiche() {
			return tattiche;
		}

		@Override
		protected SchermataTrofei schermataTrofei() {
			return paginaTrofei;
		}

		@Override
		protected SchermataBestiario schermataBestiario() {
			return bestiario;
		}

		@Override
		protected SchermataGrimorio schermataGrimorio() {
			return grimorio;
		}

		@Override
		protected void scambioDellInventario(VistaScambio scambio, VistaPersonaggio personaggio) {
			inventario.impostaScambio(scambio, personaggio);
		}

		@Override
		protected void scambioDelCommerciante(TipoNegozio negozio, VistaScambio scambio) {
			commerciante.impostaNegozio(negozio);
			commerciante.impostaScambio(scambio);
		}

		@Override
		protected void bancoDellIncantatore(VistaBancoDiLavoro banco) {
			incantatore.impostaBanco(banco);
		}

		@Override
		protected void offerteDellAlchimista(List<? extends VistaOffertaConsumabile> offerte) {
			alchimista.impostaOfferte(offerte);
		}

		@Override
		protected SpriteInterface sprite(DescrizioneSpriteATempo descrizione) {
			return descrizione == null ? null : new SpriteATempo(descrizione);
		}

		@Override
		protected SpriteInterface sprite(DescrizioneSpriteEffetto descrizione) {
			return descrizione == null ? null : new SpriteEffetto(descrizione);
		}

		@Override
		protected SpriteInterface spriteDellaMorte(VistaPersonaggio personaggio) {
			return riquadroLocazione.notificaMorte(personaggio);
		}

		@Override
		protected SpriteInterface spriteDellaSfida(float secondiDiAttesa, float secondiDiDissolvenza) {
			return new SpriteInDissolvenza(TestiDellaUI.get("SFIDA_TITOLO"), riquadroSfida.inUnImmagine(), riquadroSfida.getX(),
					riquadroSfida.getY(), secondiDiAttesa, secondiDiDissolvenza);
		}

		@Override
		protected SpriteAnnuncioGlobale annuncio(DescrizioneAnnuncioGlobale descrizione) {
			return new SpriteAnnuncioGlobale(descrizione);
		}

		@Override
		protected SpriteFumetto fumetto(String testo, int larghezzaMassima, int x, int y, int puntaVersoX, int puntaVersoY) {
			return new SpriteFumetto(testo, larghezzaMassima, x, y, DoomdarkFont.medio(), DoomdarkColorModel.Color.BLACK, puntaVersoX, puntaVersoY);
		}

		@Override
		protected SpriteRivelazioneArtefatto rivelazione(VistaArtefatto artefatto, int livelloMondo, Area area, Punto destinazione) {
			return new SpriteRivelazioneArtefatto(artefatto, livelloMondo,
					new Rectangle(area.getX(), area.getY(), area.getLarghezza(), area.getAltezza()), new Point(destinazione.x, destinazione.y));
		}

		@Override
		public void ridisegna() {
			repaint();
		}
	}

	@Override
	public void addNotify() {
		super.addNotify();
		avviaThreadAnimazione();
	}

	@Override
	public void removeNotify() {
		fermaThreadAnimazione();
		super.removeNotify();
	}

	private void avviaThreadAnimazione() {
		if (animatore == null || !animatoreInAzione) {
			// Il flag si alza qui e non nel nuovo thread: due addNotify ravvicinati non ne fanno partire due
			animatoreInAzione = true;
			animatore = new Thread(this);
			animatore.setDaemon(true);
			animatore.start();
		}
	}

	/**
	 * Ferma il thread che ridisegna a ogni fotogramma.
	 */
	private void fermaThreadAnimazione() {
		animatoreInAzione = false;
	}

	/**
	 * Il ciclo di animazione: a cadenza fissa chiede il ridisegno se qualcosa si muove, senza contenere logica di gioco.
	 */
	public void run() {
		// Cadenza fissa: la scadenza del fotogramma successivo si calcola dalla precedente, non da quando finisce il sonno,
		// altrimenti i ritardi di sleep() si accumulano e il ritmo effettivo scende sotto FRAME_PER_SECONDO (e diventa irregolare).
		long periodoNanos = 1_000_000_000L / Temporizzatore.FRAME_PER_SECONDO;
		long prossimoFotogramma = System.nanoTime();
		// Se il canvas viene tolto e rimesso prima che questo thread si accorga dello stop, quello nuovo non deve trovarlo ancora in corsa
		while (animatoreInAzione && animatore == Thread.currentThread()) {
			if (schermo.deveRidisegnare()) {
				repaint();
			}
			prossimoFotogramma += periodoNanos;
			long attesaNanos = prossimoFotogramma - System.nanoTime();
			if (attesaNanos <= 0) {
				// In ritardo di oltre un fotogramma (es. il sistema era sospeso): si riparte da adesso invece di tentare di
				// recuperare con una raffica di repaint().
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
	 * Dopo un riquadro impilato che non è l'ultimo, lo fa sfumare nel nero nelle ultime righe prima del riquadro successivo, che
	 * comincia subito dopo
	 */
	private void dissolviSeCoperto(Graphics2D graphics, TipoFinestra finestra) {
		int quotaSuccessivo = schermo.quotaDelSuccessivo(finestra);
		if (quotaSuccessivo < 0) {
			return;
		}
		Area area = schermo.getArea(schermo.finestra(finestra));
		int altezzaDissolvenza = schermo.getAltezzaDissolvenzaPila();
		for (int riga = 0; riga < altezzaDissolvenza; riga++) {
			int alfa = (riga + 1) * 255 / altezzaDissolvenza;
			graphics.setColor(new Color(0, 0, 0, alfa));
			graphics.fillRect(area.getX(), quotaSuccessivo - altezzaDissolvenza + riga, area.getLarghezza(), 1);
		}
	}

	private void inGioco(Graphics2D graphics) {
		for (TipoFinestra finestra : schermo.getPila()) {
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
		schermo.perOgniSprite(sprite -> sprite.anima(graphics));
	}

	@Override
	public void paintComponent(Graphics graphics) {
		if (!PROFILO) {
			super.paintComponent(graphics);
			aggiornaSchermo((Graphics2D)graphics);
			return;
		}
		long inizio = System.nanoTime();
		super.paintComponent(graphics);
		long dopoSuper = System.nanoTime();
		aggiornaSchermo((Graphics2D)graphics);
		long fine = System.nanoTime();
		profiloPaint(fine - inizio, dopoSuper - inizio, inizio);
	}

	// PROFILO TEMPORANEO (-Dforesta.profilo=true): tempi di paintComponent per schermata, ogni due secondi
	private static final boolean PROFILO = Boolean.getBoolean("foresta.profilo");
	private long profiloInizioFinestra = 0;
	private long profiloTotale = 0, profiloMax = 0, profiloSuper = 0;
	private int profiloConta = 0;

	private void profiloPaint(long durata, long durataSuper, long inizio) {
		if (profiloInizioFinestra == 0) {
			profiloInizioFinestra = inizio;
		}
		profiloTotale += durata;
		profiloSuper += durataSuper;
		profiloMax = Math.max(profiloMax, durata);
		profiloConta++;
		long trascorso = System.nanoTime() - profiloInizioFinestra;
		if (trascorso >= 2_000_000_000L) {
			System.out.println(String.format(Locale.ROOT, "PROFILO stato=%s %dx%d paint=%d in %.1fs (%.1f fps) medio=%.2f ms (super %.2f) max=%.2f ms",
					schermo.getStato(), getWidth(), getHeight(), profiloConta, trascorso / 1e9, profiloConta * 1e9 / trascorso,
					profiloTotale / 1e6 / profiloConta, profiloSuper / 1e6 / profiloConta, profiloMax / 1e6));
			profiloInizioFinestra = 0;
			profiloTotale = profiloMax = profiloSuper = 0;
			profiloConta = 0;
		}
	}

	private void disegnaFumetto(Graphics2D graphics) {
		schermo.disegnaIlFumetto(fumetto -> fumetto.anima(graphics));
	}

	private void aggiornaSchermo(Graphics2D graphics) {
		StatoDelloSchermo stato = schermo.getStato();
		switch (stato) {
			case INTRO:
				riquadroIntroOutro.intro(graphics);
				break;
			case SELEZIONE_SLOT_DA_CARICARE:
				riquadroIntroOutro.selezioneSlotDaCaricare(graphics);
				break;
			case MESSAGGIO:
				riquadroIntroOutro.scrivi(graphics, true);
				break;
			case IN_GIOCO:
				inGioco(graphics);
				schermo.disegnaLaRivelazione(rivelazione -> rivelazione.anima(graphics));
				disegnaFumetto(graphics);
				break;
			case MAPPA:
				mappaATuttoSchermo.disegnaMappaATuttoSchermo(graphics);
				disegnaFumetto(graphics);
				break;
			case SELEZIONE_SLOT_DA_SALVARE:
				riquadroIntroOutro.selezioneSlotDaSalvare(graphics);
				break;
			case CONFERMA_USCITA:
				riquadroIntroOutro.confermaUscita(graphics);
				break;
			case PERSO:
				riquadroIntroOutro.perso(graphics);
				break;
			case VINTO:
				riquadroIntroOutro.vinto(graphics);
				break;
			case STATISTICHE:
				riquadroIntroOutro.statistiche(graphics);
				break;
			case PUNTEGGI:
				riquadroIntroOutro.hiscore(graphics);
				break;
			case INVENTARIO:
				inventario.disegnaInventario(graphics);
				disegnaFumetto(graphics);
				break;
			case TROFEI:
				paginaTrofei.disegna(graphics);
				break;
			case BESTIARIO:
				bestiario.disegna(graphics);
				break;
			case GRIMORIO:
				grimorio.disegna(graphics);
				break;
			case TATTICHE:
				tattiche.disegna(graphics);
				break;
			case COMMERCIANTE:
				commerciante.disegnaInventario(graphics);
				disegnaFumetto(graphics);
				break;
			case ALCHIMISTA:
				alchimista.disegnaInventario(graphics);
				disegnaFumetto(graphics);
				break;
			case INCANTATORE:
				incantatore.disegnaInventario(graphics);
				disegnaFumetto(graphics);
				break;
			case INTERMEZZO:
				intermezzo.disegna(graphics);
				disegnaFumetto(graphics);
				break;
			default:
				break;
		}
		schermo.disegnaLAnnuncio(annuncio -> annuncio.anima(graphics));
		barraIcone.disegna(graphics);
		// Il suggerimento del tutorial sta sopra la barra delle icone, ma sotto le sfere e i cartigli dell'aiuto
		if (stato == StatoDelloSchermo.IN_GIOCO) {
			tutorial.disegna(graphics, getWidth(), getHeight());
		}
		// Decorazione nell'angolo in basso a sinistra della mappa a tutto schermo
		if (schermo.isSferaMagicaVisibile()) {
			graphics.drawImage(ImageCache.sferaMagica, 0, getHeight() - ImageCache.sferaMagica.getHeight(), null);
		}
		// I cartigli dell'aiuto stanno sopra tutto, anche sopra la sfera magica
		if (stato == StatoDelloSchermo.IN_GIOCO) {
			riquadroGruppo.disegnaAiuto(graphics, getWidth(), getHeight());
			riquadroMissioni.disegnaAiuto(graphics, getWidth(), getHeight());
		}
		barraIcone.disegnaAiuto(graphics, getWidth(), getHeight());
		schermo.fineFotogramma();
	}

	/**
	 * Gli eventi del mouse di AWT, tradotti per il router degli input (che li instrada alle finestre: vedi {@link RouterDegliInput})
	 */
	private class GestoreMouse implements MouseListener, MouseMotionListener, MouseWheelListener {

		private final RouterDegliInput router = new RouterDegliInput(schermo.contestoDegliInput(), new PianificatoreSwing());

		private Finestra.Tasto tasto(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				return Finestra.Tasto.SINISTRO;
			} else if (SwingUtilities.isRightMouseButton(e)) {
				return Finestra.Tasto.DESTRO;
			} else if (SwingUtilities.isMiddleMouseButton(e)) {
				return Finestra.Tasto.CENTRALE;
			}
			return null;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (tutorial.clic(e.getX(), e.getY())) {
				return;
			}
			Finestra.Tasto tasto = tasto(e);
			if (tasto != null) {
				router.clic(e.getX(), e.getY(), tasto, e.getClickCount());
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (tutorial.suiPulsanti(e.getX(), e.getY())) {
				return;
			}
			Finestra.Tasto tasto = tasto(e);
			if (tasto != null) {
				router.premuto(e.getX(), e.getY(), tasto);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (tutorial.suiPulsanti(e.getX(), e.getY())) {
				return;
			}
			Finestra.Tasto tasto = tasto(e);
			if (tasto != null) {
				router.rilasciato(e.getX(), e.getY(), tasto);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			router.mouseEntrato(e.getX(), e.getY());
		}

		@Override
		public void mouseExited(MouseEvent e) {
			router.mouseUscito(e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			router.trascinato(e.getX(), e.getY());
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			router.mosso(e.getX(), e.getY());
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			router.rotella(e.getX(), e.getY(), e.getWheelRotation());
		}
	}
}
