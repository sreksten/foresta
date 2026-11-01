package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostazioneAiuto;
import com.threeamigos.foresta.eventi.richieste.RicaricaDelComando;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.ui.DisplayableCanvas;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.ImageBasedDoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontSmall;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * La barra delle icone di comando: sostituisce le vecchie PannelloIcone/ImageButton,
 * veri componenti Swing che convivevano male con l'animazione continua di
 * DisplayableCanvas (vedi DisplayableCanvas.run). In orizzontale sta in fondo allo schermo, in verticale a destra:
 * l'asse lungo la barra è la larghezza nel primo caso, l'altezza nel secondo.
 * <p>
 * Se le icone non ci stanno tutte, le due frecce (SINISTRA e DESTRA, o SU e GIU) restano ancorate ai due estremi e le
 * altre scorrono fra loro in modo fluido, in pixel: si disegnano con uno scostamento e si ritagliano sulla fascia
 * di mezzo. Si scorre trascinando (mouse o dito), e lasciando andare con una certa velocità le icone proseguono per
 * inerzia e rallentano; le frecce fanno scorrere di una pagina, con una breve animazione, e si spengono ai capi.
 * Una pressione che non supera SOGLIA_TRASCINAMENTO e viene lasciata sulla stessa icona è un tocco: esegue il
 * comando. Per ricevere il trascinamento anche fuori dalla fascia la barra se lo fa catturare (vedi
 * catturaIlTrascinamento).
 * <p>
 * In fondo ai comandi la barra aggiunge da sola l'interruttore dell'aiuto, che il gioco non vede: AIUTO quando
 * l'aiuto è spento, NO_AIUTO quando è acceso (l'icona mostra cosa fa il click). La barra lo legge da VistaPartita e
 * lo cambia con ComandoImpostazioneAiuto: l'Automa lo scrive in ModelloDati, che lo salva con la partita. Ad aiuto acceso, passando sopra un'icona compare un cartiglio con la descrizione del comando
 * (Comando.getDescrizione), sempre dentro lo schermo (vedi disegnaAiuto).
 */
public class DisplayableCanvasBarraIcone implements Finestra {

	/**
	 * La misura delle icone dopo lo zoom (vedi TipoIcona.componi): 64 x 64 con lo zoom predefinito
	 */
	static final int ICONA_WIDTH = TipoIcona.getLarghezzaMassima();
	static final int ICONA_HEIGHT = TipoIcona.getAltezzaMassima();
	/**
	 * Quanto spazio, oltre alla loro misura, deve avere ogni icona della barra per essere contata fra quelle che ci
	 * stanno
	 */
	private static final int MARGINE_ICONA = 4;
	/**
	 * La distanza fra il cartiglio dell'aiuto e l'icona a cui si riferisce
	 */
	private static final int DISTANZA_CARTIGLIO = 4;
	/**
	 * Di quanti pixel va spostato il puntatore, premuto, perché il gesto sia un trascinamento e non un tocco
	 */
	static final int SOGLIA_TRASCINAMENTO = 8;
	/**
	 * Lasciando la barra a meno di questa velocità (pixel al secondo) le icone si fermano; a più, proseguono per inerzia
	 */
	static final double VELOCITA_MINIMA_INERZIA = 200.0d;
	/**
	 * Sotto questa velocità l'inerzia è finita
	 */
	private static final double VELOCITA_DI_ARRESTO = 20.0d;
	/**
	 * Il tempo, in secondi, in cui la velocità dell'inerzia si riduce di un fattore e (circa 37%)
	 */
	private static final double COSTANTE_DI_ATTRITO = 0.35d;
	/**
	 * Come COSTANTE_DI_ATTRITO, per l'animazione che segue il click su una freccia
	 */
	private static final double COSTANTE_DELLE_FRECCE = 0.08d;
	/**
	 * Se il puntatore è fermo da più di così quando si lascia la barra, non c'è inerzia
	 */
	private static final long ETA_MASSIMA_DEL_CAMPIONE_NANOS = 100_000_000L;
	/**
	 * Un fotogramma in ritardo (il sistema era sospeso) non fa scattare lo scorrimento: il tempo trascorso si limita a questo
	 */
	private static final double PASSO_MASSIMO_SECONDI = 0.05d;
	/**
	 * Quanto è opaca una freccia spenta (a fine corsa)
	 */
	private static final float OPACITA_FRECCIA_SPENTA = 0.3f;

	public static class IconaVisibile {
		public final Rectangle rettangolo;
		public final Comando comando;
		public final BufferedImage icona;

		IconaVisibile(Rectangle rettangolo, Comando comando, BufferedImage icona) {
			this.rettangolo = rettangolo;
			this.comando = comando;
			this.icona = icona;
		}
	}

	private final int orientamento;
	private final boolean orizzontale;
	final int offsetX;
	final int offsetY;
	final int larghezza;
	final int altezza;
	/**
	 * Le misure lungo l'asse della barra: la lunghezza dell'asse, e quella dell'icona (le frecce occupano un'icona a ciascun capo)
	 */
	private final int lunghezzaAsse;
	private final int latoIcona;
	/**
	 * Dove sta l'icona sull'altro asse: centrata nella barra
	 */
	private final int posizioneTrasversale;

	private final Comando comandoPrecedente;
	private final BufferedImage iconaPrecedente;
	private final Comando comandoSuccessivo;
	private final BufferedImage iconaSuccessivo;

	private final Image[] copyrightImages = new Image[3];
	private final int[] copyrightImagesXOffset = new int[3];
	private final int[] copyrightImagesYOffset = new int[3];

	private Comando[] comandi = new Comando[0];
	private BufferedImage[] icone = new BufferedImage[0];

	/**
	 * Se le icone non ci stanno: allora le frecce stanno ai capi e le altre scorrono nella fascia di mezzo
	 */
	private boolean scorrevole = false;
	/**
	 * Di quanti pixel le icone sono spostate verso l'inizio della barra (0 le mostra dalla prima), fino a scorrimentoMassimo
	 */
	private double scorrimento = 0.0d;
	private double scorrimentoMassimo = 0.0d;

	public final List<IconaVisibile> iconeVisibili = new ArrayList<>();

	int mouseX = -1;
	int mouseY = -1;
	boolean mousePremuto = false;

	// Il gesto in corso: da dove è iniziato, se ha superato la soglia e la velocità con cui il puntatore si muove
	private boolean premutoValido = false;
	private boolean puoTrascinare = false;
	private boolean trascinando = false;
	private boolean fermatoInMovimento = false;
	private Comando comandoPremuto = null;
	private int premutoAsse;
	private double scorrimentoAllaPressione;
	private int ultimoAsse;
	private long istanteUltimoCampione;
	private double velocitaStimata;

	// Il movimento che prosegue da solo: l'inerzia, oppure lo scorrimento verso la destinazione di una freccia
	private boolean inerzia = false;
	private double velocita;
	private boolean haDestinazione = false;
	private double destinazione;
	private long istanteAggiornamento;
	// Letto dal thread di animazione (vedi inTransizione)
	private volatile boolean inMovimento = false;

	private final VistaPartita vistaPartita;

	public DisplayableCanvasBarraIcone(int orientamento, int offsetX, int offsetY, int larghezza, int altezza, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.orientamento = orientamento;
		this.orizzontale = orientamento == DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.larghezza = larghezza;
		this.altezza = altezza;
		this.lunghezzaAsse = orizzontale ? larghezza : altezza;
		this.latoIcona = orizzontale ? ICONA_WIDTH : ICONA_HEIGHT;
		this.posizioneTrasversale = orizzontale ? (altezza - ICONA_HEIGHT) >> 1 : (larghezza - ICONA_WIDTH) >> 1;

		if (orizzontale) {
			comandoPrecedente = Comando.SINISTRA;
			iconaPrecedente = TipoIcona.SINISTRA.getIcona();
			comandoSuccessivo = Comando.DESTRA;
			iconaSuccessivo = TipoIcona.DESTRA.getIcona();
		} else {
			comandoPrecedente = Comando.SU;
			iconaPrecedente = TipoIcona.SU.getIcona();
			comandoSuccessivo = Comando.GIU;
			iconaSuccessivo = TipoIcona.GIU.getIcona();
		}

		DoomdarkFont fontSmall = ImageBasedDoomdarkFont.piccolo();
		DoomdarkColorModel.Color color = DoomdarkColorModel.Color.VERY_DARK_GRAY;
		copyrightImages[0] = ImageCache.get("La Foresta", fontSmall, color);
		copyrightImages[1] = ImageCache.get("copyright 1984-2026", fontSmall, color);
		copyrightImages[2] = ImageCache.get("Stefano Reksten", fontSmall, color);
		for (int i = 0; i < 3; i++) {
			copyrightImagesXOffset[i] = larghezza - copyrightImages[i].getWidth(null) - 1;
			copyrightImagesYOffset[i] = altezza - fontSmall.getHeight() * (3 - i) - 1;
		}
	}

	/**
	 * Dispone le icone dei comandi possibili e, in ogni schermata che ha dei comandi, aggiunge l'interruttore dell'aiuto.
	 */
	public void impostaAzioni() {
		List<Comando> possibiliAzioni = new ArrayList<>(ComandiPossibili.getComandiVisibili());
		// L'interruttore dell'aiuto, in ogni schermata che ha dei comandi
		if (!possibiliAzioni.isEmpty()) {
			possibiliAzioni.add(comandoAiuto());
		}
		int quanteScelte = possibiliAzioni.size();
		comandi = new Comando[quanteScelte];
		icone = new BufferedImage[quanteScelte];
		for (int i = 0; i < quanteScelte; i++) {
			Comando comando = possibiliAzioni.get(i);
			if (comando == null) {
				throw new IllegalArgumentException("Comando nullo");
			}
			comandi[i] = comando;
			icone[i] = getIcona(comando);
		}
		scorrimento = 0.0d;
		ferma();
		ricalcolaGeometria();
	}

	/**
	 * L'icona dell'interruttore mostra cosa fa il click: accendere l'aiuto se è spento, spegnerlo se è acceso
	 */
	private Comando comandoAiuto() {
		return comandoAiuto(vistaPartita.isAiutoAbilitato());
	}

	private static Comando comandoAiuto(boolean aiutoAbilitato) {
		return aiutoAbilitato ? Comando.NO_AIUTO : Comando.AIUTO;
	}

	private static boolean isInterruttoreAiuto(Comando comando) {
		return comando == Comando.AIUTO || comando == Comando.NO_AIUTO;
	}

	/**
	 * Accende o spegne l'aiuto e cambia l'icona dell'interruttore, senza cambiare pagina di scelte. L'icona nuova
	 * segue la scelta, senza aspettare che l'Automa la scriva nel modello dati.
	 */
	private void commutaAiuto(Comando azione) {
		boolean aiutoAbilitato = azione == Comando.AIUTO;
		BusEventi.pubblica(new ComandoImpostazioneAiuto(aiutoAbilitato));
		for (int i = 0; i < comandi.length; i++) {
			if (isInterruttoreAiuto(comandi[i])) {
				comandi[i] = comandoAiuto(aiutoAbilitato);
				icone[i] = getIcona(comandi[i]);
			}
		}
		ricalcolaGeometria();
	}

	private BufferedImage getIcona(Comando comando) {
		if (comando.isPersonaggio()) {
			int indice = comando.ordinal() - Comando.PERSONAGGIO_1.ordinal();
			TipoIcona icona = getIconaPersonaggio(indice);
			// Il capo si riconosce dallo sfondo rosso
			VistaPersonaggio capo = vistaPartita.getGruppoGiocatore().getCapo();
			boolean isCapo = capo != null && capo.getUuid().equals(vistaPartita.getGruppoGiocatore().getPersonaggio(indice).getUuid());
			return isCapo ? icona.getIconaCapo() : icona.getIcona();
		}
		RicaricaDelComando ricarica = ComandiPossibili.getRicarica(comando);
		if (ricarica != null) {
			return TipoIcona.ofComando(comando).getIconaInRicarica(ricarica.getTurniRimasti(), ricarica.getDurata());
		}
		return TipoIcona.ofComando(comando).getIcona();
	}

	private TipoIcona getIconaPersonaggio(int indice) {
		VistaPersonaggio personaggio = vistaPartita.getGruppoGiocatore().getPersonaggio(indice);
		if (personaggio == null) {
			throw new IllegalStateException("Personaggio non trovato con indice " + indice);
		}
		return TipoIcona.ofClasse(personaggio.getClasse());
	}

	/**
	 * Ricalcola quante icone ci stanno e di quanto si può scorrere, tiene lo scorrimento (e la destinazione) dentro i
	 * limiti e ricostruisce le icone visibili. Se le icone ci stanno tutte si mostrano centrate e le frecce non ci sono.
	 */
	private void ricalcolaGeometria() {
		int quanteScelte = comandi.length;
		int iconePossibili = lunghezzaAsse / (latoIcona + MARGINE_ICONA);
		scorrevole = quanteScelte > iconePossibili;
		scorrimentoMassimo = scorrevole ? Math.max(0, quanteScelte * latoIcona - (lunghezzaAsse - 2 * latoIcona)) : 0.0d;
		scorrimento = Math.max(0.0d, Math.min(scorrimento, scorrimentoMassimo));
		if (haDestinazione) {
			destinazione = Math.max(0.0d, Math.min(destinazione, scorrimentoMassimo));
		}
		ricostruisciIconeVisibili();
	}

	/**
	 * Le icone che si vedono, anche solo in parte, nell'ordine della barra: la freccia precedente, le icone della
	 * fascia di mezzo (la prima e l'ultima possono uscirne a metà: le ritaglia il disegno) e la freccia successiva.
	 */
	private void ricostruisciIconeVisibili() {
		iconeVisibili.clear();
		int quanteScelte = comandi.length;
		if (quanteScelte == 0) {
			return;
		}
		if (!scorrevole) {
			int posizione = (lunghezzaAsse - quanteScelte * latoIcona) >> 1;
			for (int i = 0; i < quanteScelte; i++) {
				aggiungiIcona(posizione + i * latoIcona, comandi[i], icone[i]);
			}
			return;
		}
		aggiungiIcona(0, comandoPrecedente, iconaPrecedente);
		int origine = latoIcona - (int) Math.round(scorrimento);
		for (int i = 0; i < quanteScelte; i++) {
			int posizione = origine + i * latoIcona;
			if (posizione + latoIcona > latoIcona && posizione < lunghezzaAsse - latoIcona) {
				aggiungiIcona(posizione, comandi[i], icone[i]);
			}
		}
		aggiungiIcona(lunghezzaAsse - latoIcona, comandoSuccessivo, iconaSuccessivo);
	}

	private void aggiungiIcona(int posizioneSuAsse, Comando comando, BufferedImage icona) {
		Rectangle rettangolo = orizzontale
				? new Rectangle(posizioneSuAsse, posizioneTrasversale, ICONA_WIDTH, ICONA_HEIGHT)
				: new Rectangle(posizioneTrasversale, posizioneSuAsse, ICONA_WIDTH, ICONA_HEIGHT);
		iconeVisibili.add(new IconaVisibile(rettangolo, comando, icona));
	}

	/**
	 * Quanto, in pixel, scorrono le frecce a ogni click: le icone che stanno nella fascia di mezzo meno una, così
	 * l'ultima vista resta in vista come riferimento.
	 */
	private int pagina() {
		int nellaFascia = (lunghezzaAsse - 2 * latoIcona) / latoIcona;
		return Math.max(1, nellaFascia - 1) * latoIcona;
	}

	public double getScorrimento() {
		return scorrimento;
	}

	public double getScorrimentoMassimo() {
		return scorrimentoMassimo;
	}

	/**
	 * Se le icone scorrono (non ci stanno tutte) e quindi le frecce stanno ai capi
	 */
	public boolean isScorrevole() {
		return scorrevole;
	}

	/**
	 * L'istante, in nanosecondi, da cui si misurano inerzia e animazioni: ridefinibile per provarle senza aspettare
	 */
	protected long adesso() {
		return System.nanoTime();
	}

	/**
	 * Ferma qualunque movimento in corso, trascinamento compreso
	 */
	private void ferma() {
		trascinando = false;
		premutoValido = false;
		inerzia = false;
		velocita = 0.0d;
		haDestinazione = false;
		inMovimento = false;
	}

	/**
	 * Fa avanzare l'inerzia o l'animazione di una freccia fino a questo istante: va chiamato a ogni disegno, prima di
	 * disegnare. Durante un trascinamento lo scorrimento lo decide il puntatore.
	 */
	protected void aggiornaScorrimento() {
		long ora = adesso();
		double passo = Math.min((ora - istanteAggiornamento) / 1_000_000_000.0d, PASSO_MASSIMO_SECONDI);
		istanteAggiornamento = ora;
		if (trascinando || !scorrevole || passo <= 0.0d) {
			return;
		}
		if (inerzia) {
			scorrimento += velocita * passo;
			velocita *= Math.exp(-passo / COSTANTE_DI_ATTRITO);
			if (scorrimento < 0.0d || scorrimento > scorrimentoMassimo) {
				// Arrivata a un capo l'inerzia finisce: niente rimbalzo
				scorrimento = Math.max(0.0d, Math.min(scorrimento, scorrimentoMassimo));
				inerzia = false;
				velocita = 0.0d;
			} else if (Math.abs(velocita) < VELOCITA_DI_ARRESTO) {
				inerzia = false;
				velocita = 0.0d;
			}
			ricostruisciIconeVisibili();
		} else if (haDestinazione) {
			double distanza = destinazione - scorrimento;
			if (Math.abs(distanza) < 0.5d) {
				scorrimento = destinazione;
				haDestinazione = false;
			} else {
				scorrimento += distanza * (1.0d - Math.exp(-passo / COSTANTE_DELLE_FRECCE));
			}
			ricostruisciIconeVisibili();
		}
		inMovimento = inerzia || haDestinazione;
	}

	/**
	 * Prepara la geometria che serve a disegnare e a trovare l'icona sotto il puntatore: la barra classica non ha niente
	 * da fare, il Dock ricalcola i rettangoli ingranditi.
	 */
	void preparaGeometria() {
	}

	/**
	 * La fascia in cui le icone scorrono, in coordinate dello schermo, che si estende oltre la barra sull'altro asse
	 * perché le icone ingrandite del Dock possano uscirne.
	 */
	Rectangle fasciaDiRitaglio() {
		return orizzontale
				? new Rectangle(offsetX + latoIcona, offsetY - ICONA_HEIGHT, lunghezzaAsse - 2 * latoIcona, altezza + ICONA_HEIGHT)
				: new Rectangle(offsetX - ICONA_WIDTH, offsetY + latoIcona, larghezza + ICONA_WIDTH, lunghezzaAsse - 2 * latoIcona);
	}

	/**
	 * Se l'icona con questo indice, fra le visibili, è una delle due frecce
	 */
	boolean isFreccia(int indice) {
		return scorrevole && (indice == 0 || indice == iconeVisibili.size() - 1);
	}

	/**
	 * Una freccia a fine corsa: si vede, ma spenta, e non fa niente
	 */
	private boolean isFrecciaSpenta(int indice) {
		if (!isFreccia(indice)) {
			return false;
		}
		return indice == 0 ? scorrimento <= 0.5d : scorrimento >= scorrimentoMassimo - 0.5d;
	}

	private int asse(int x, int y) {
		return orizzontale ? x : y;
	}

	/**
	 * Se il punto (relativo alla barra) cade nella fascia di mezzo, quella in cui le icone scorrono e si ritagliano
	 */
	boolean nellaFascia(int x, int y) {
		if (!scorrevole) {
			return true;
		}
		int posizione = asse(x, y);
		return posizione >= latoIcona && posizione < lunghezzaAsse - latoIcona;
	}

	/**
	 * L'indice, fra le icone visibili, di quella che copre il punto (relativo alla barra), o -1. Un'icona che esce
	 * dalla fascia di mezzo non si tocca nella parte che esce.
	 */
	int indiceSotto(int x, int y) {
		preparaGeometria();
		for (int i = 0; i < iconeVisibili.size(); i++) {
			if (rettangoloDisegnato(i).contains(x, y) && (isFreccia(i) || nellaFascia(x, y))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Disegna la barra: lo sfondo e le icone visibili, con l'aiuto del comando sotto il cursore.
	 */
	public void disegna(Graphics2D graphics) {
		aggiornaScorrimento();
		preparaGeometria();
		graphics.setColor(Color.black);
		graphics.fillRect(offsetX, offsetY, larghezza, altezza);
		// Una copia del contesto: su un Graphics2D nuovo l'hint di interpolazione vale null e non si potrebbe ripristinare
		Graphics2D icone = (Graphics2D) graphics.create();
		try {
			// Pixel netti: niente sfocatura sulle icone ingrandite
			icone.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			Graphics2D ritagliato = (Graphics2D) icone.create();
			try {
				if (scorrevole) {
					Rectangle fascia = fasciaDiRitaglio();
					ritagliato.clipRect(fascia.x, fascia.y, fascia.width, fascia.height);
				}
				for (int i = 0; i < iconeVisibili.size(); i++) {
					if (!isFreccia(i)) {
						disegnaIcona(ritagliato, i);
					}
				}
			} finally {
				ritagliato.dispose();
			}
			// Le frecce stanno ai capi e sopra le icone che scorrono
			for (int i = 0; i < iconeVisibili.size(); i++) {
				if (isFreccia(i)) {
					disegnaIcona(icone, i);
				}
			}
		} finally {
			icone.dispose();
		}
		copyright(graphics);
	}

	private void disegnaIcona(Graphics2D graphics, int indice) {
		IconaVisibile iconaVisibile = iconeVisibili.get(indice);
		Rectangle r = rettangoloDisegnato(indice);
		int x = offsetX + r.x;
		int y = offsetY + r.y;
		if (isFrecciaSpenta(indice)) {
			Composite precedente = graphics.getComposite();
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITA_FRECCIA_SPENTA));
			graphics.drawImage(iconaVisibile.icona, x, y, r.width, r.height, null);
			graphics.setComposite(precedente);
			return;
		}
		graphics.drawImage(iconaVisibile.icona, x, y, r.width, r.height, null);
		// Un comando in ricarica non si può dare: sotto il cursore non si evidenzia
		if (ComandiPossibili.getRicarica(iconaVisibile.comando) == null && isSottoIlCursore(indice, r)) {
			graphics.setColor(mousePremuto ? Color.white : Color.lightGray);
			graphics.drawRect(x, y, r.width - 1, r.height - 1);
		}
	}

	/**
	 * Se il cursore sta su questa icona (durante un trascinamento non sta su nessuna)
	 */
	private boolean isSottoIlCursore(int indice, Rectangle disegnato) {
		return !trascinando && mouseX >= 0 && disegnato.contains(mouseX, mouseY) && (isFreccia(indice) || nellaFascia(mouseX, mouseY));
	}

	/**
	 * Il rettangolo, relativo alla barra, in cui è disegnata adesso l'icona di iconeVisibili con quell'indice
	 */
	Rectangle rettangoloDisegnato(int indice) {
		return iconeVisibili.get(indice).rettangolo;
	}

	/**
	 * Ad aiuto acceso, il cartiglio con la descrizione del comando sotto il mouse, sopra tutto il resto: va chiamato
	 * dopo aver disegnato lo schermo intero, grande larghezzaSchermo x altezzaSchermo.
	 */
	public void disegnaAiuto(Graphics2D graphics, int larghezzaSchermo, int altezzaSchermo) {
		if (!vistaPartita.isAiutoAbilitato() || mouseX < 0) {
			return;
		}
		int i = trascinando ? -1 : indiceSotto(mouseX, mouseY);
		if (i < 0 || isFrecciaSpenta(i)) {
			return;
		}
		Rectangle r = rettangoloDisegnato(i);
		String descrizione = descrizioneAiuto(iconeVisibili.get(i).comando);
		if (descrizione != null) {
			Image testo = Cartiglio.testo(descrizione);
			Point posizione = posizioneCartiglio(orientamento, new Rectangle(offsetX + r.x, offsetY + r.y, r.width, r.height),
					Cartiglio.larghezza(testo), Cartiglio.altezza(), larghezzaSchermo, altezzaSchermo);
			Cartiglio.disegna(graphics, testo, posizione.x, posizione.y);
		}
	}

	/**
	 * Il testo del cartiglio: la descrizione del comando, tranne per l'icona di un personaggio, che porta il suo nome
	 */
	private String descrizioneAiuto(Comando comando) {
		if (comando.isPersonaggio()) {
			VistaPersonaggio personaggio = vistaPartita.getGruppoGiocatore().getPersonaggio(comando.ordinal() - Comando.PERSONAGGIO_1.ordinal());
			if (personaggio != null && personaggio.getNomeProprio().isPresent()) {
				return personaggio.getNomeProprio().get();
			}
		}
		RicaricaDelComando ricarica = ComandiPossibili.getRicarica(comando);
		if (ricarica != null) {
			return comando.getDescrizione() + " " + (ricarica.getTurniRimasti() == 1
					? TestiDellaUI.get("RICARICA_MANCA_UN_TURNO") : TestiDellaUI.get("RICARICA_MANCANO_TURNI", ricarica.getTurniRimasti()));
		}
		return comando.getDescrizione();
	}

	/**
	 * Dove mettere un cartiglio largo e alto così per l'icona in quel rettangolo (coordinate dello schermo): con la
	 * barra in basso sopra l'icona e centrato su di lei, con la barra a destra alla sua sinistra e centrato in
	 * altezza; in ogni caso spostato quanto serve per restare dentro lo schermo, al peggio allineato a un suo bordo.
	 */
	public static Point posizioneCartiglio(int orientamento, Rectangle icona, int larghezza, int altezza,
									int larghezzaSchermo, int altezzaSchermo) {
		int x;
		int y;
		if (orientamento == DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE) {
			x = icona.x + (icona.width - larghezza) / 2;
			y = icona.y - altezza - DISTANZA_CARTIGLIO;
		} else {
			x = icona.x - larghezza - DISTANZA_CARTIGLIO;
			y = icona.y + (icona.height - altezza) / 2;
		}
		x = Math.max(0, Math.min(x, larghezzaSchermo - larghezza));
		y = Math.max(0, Math.min(y, altezzaSchermo - altezza));
		return new Point(x, y);
	}

	void copyright(Graphics2D graphics) {
		// In verticale la barra è stretta e in fondo ha la freccia: il copyright non ci sta
		if (!orizzontale) {
			return;
		}
		for (int i = 0; i < 3; i++) {
			graphics.drawImage(copyrightImages[i], offsetX + copyrightImagesXOffset[i], offsetY + copyrightImagesYOffset[i], null);
		}
	}

	/**
	 * Se il punto (relativo all'angolo della barra) spetta alla barra: qui il suo rettangolo, ma una barra le cui
	 * icone escono dal rettangolo (vedi DisplayableCanvasBarraIconeDock) può reclamare anche i punti fuori.
	 */
	public boolean contiene(int x, int y) {
		return x >= 0 && y >= 0 && x < larghezza && y < altezza;
	}

	/**
	 * Se la barra sta animando qualcosa da sola, anche a mouse fermo (l'inerzia dopo un trascinamento, lo scorrimento
	 * di una freccia): allora DisplayableCanvas la ridisegna a ogni fotogramma.
	 */
	public boolean inTransizione() {
		return inMovimento;
	}

	/**
	 * La barra si fa trascinare: il gesto che comincia su di lei prosegue anche se il puntatore ne esce.
	 */
	@Override
	public boolean catturaIlTrascinamento() {
		return true;
	}

	@Override
	public void processaMovimento(int x, int y) {
		mouseX = x;
		mouseY = y;
	}

	@Override
	public void processaUscita(int x, int y) {
		mouseX = -1;
		mouseY = -1;
		mousePremuto = false;
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO) {
			return;
		}
		mouseX = x;
		mouseY = y;
		mousePremuto = true;
		// Toccare le icone mentre scorrono le ferma; e quel tocco non vale come tocco su un'icona
		fermatoInMovimento = inerzia || haDestinazione;
		inerzia = false;
		velocita = 0.0d;
		haDestinazione = false;
		inMovimento = false;
		premutoValido = true;
		trascinando = false;
		int sotto = indiceSotto(x, y);
		comandoPremuto = sotto >= 0 ? iconeVisibili.get(sotto).comando : null;
		// Solo da un punto della fascia di mezzo si trascina: le frecce non si trascinano
		puoTrascinare = scorrevole && scorrimentoMassimo > 0.0d && nellaFascia(x, y) && (sotto < 0 || !isFreccia(sotto));
		premutoAsse = asse(x, y);
		scorrimentoAllaPressione = scorrimento;
		ultimoAsse = premutoAsse;
		istanteUltimoCampione = adesso();
		velocitaStimata = 0.0d;
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		mouseX = x;
		mouseY = y;
		if (!premutoValido) {
			return;
		}
		int posizione = asse(x, y);
		if (!trascinando) {
			if (!puoTrascinare || Math.abs(posizione - premutoAsse) < SOGLIA_TRASCINAMENTO) {
				return;
			}
			// Da qui le icone seguono il puntatore: si parte da dove è adesso, senza lo scatto della soglia
			trascinando = true;
			premutoAsse = posizione;
			scorrimentoAllaPressione = scorrimento;
			ultimoAsse = posizione;
			istanteUltimoCampione = adesso();
			velocitaStimata = 0.0d;
			return;
		}
		long ora = adesso();
		double secondi = (ora - istanteUltimoCampione) / 1_000_000_000.0d;
		if (secondi > 0.001d) {
			// Il puntatore va verso l'inizio della barra quando le icone scorrono verso la fine
			double istantanea = -(posizione - ultimoAsse) / secondi;
			velocitaStimata = 0.5d * velocitaStimata + 0.5d * istantanea;
			ultimoAsse = posizione;
			istanteUltimoCampione = ora;
		}
		scorrimento = Math.max(0.0d, Math.min(scorrimentoAllaPressione - (posizione - premutoAsse), scorrimentoMassimo));
		ricostruisciIconeVisibili();
	}

	/**
	 * Lasciando il tasto: dopo un trascinamento le icone proseguono per inerzia se il puntatore si muoveva abbastanza
	 * in fretta; se invece non ha superato la soglia è un tocco, e se cade sull'icona premuta la esegue.
	 */
	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO) {
			return;
		}
		mousePremuto = false;
		mouseX = x;
		mouseY = y;
		boolean eraTrascinamento = trascinando;
		boolean eraPremuto = premutoValido;
		trascinando = false;
		premutoValido = false;
		if (eraTrascinamento) {
			boolean fermo = adesso() - istanteUltimoCampione > ETA_MASSIMA_DEL_CAMPIONE_NANOS;
			if (!fermo && Math.abs(velocitaStimata) >= VELOCITA_MINIMA_INERZIA) {
				velocita = velocitaStimata;
				inerzia = true;
				istanteAggiornamento = adesso();
				inMovimento = true;
			}
			return;
		}
		if (!eraPremuto || fermatoInMovimento || comandoPremuto == null) {
			return;
		}
		int sotto = indiceSotto(x, y);
		if (sotto >= 0 && iconeVisibili.get(sotto).comando == comandoPremuto) {
			esegui(comandoPremuto);
		}
	}

	/**
	 * Il tocco si decide al rilascio (vedi processaRilascio): il click che Swing manda dopo non va contato due volte.
	 */
	@Override
	public void processaClick(int x, int y, Tasto tasto) {
	}

	/**
	 * Fa scorrere le icone fino a questa posizione (in pixel, dentro i limiti) con una breve animazione.
	 */
	private void scorriFinoA(double posizione) {
		double limitata = Math.max(0.0d, Math.min(posizione, scorrimentoMassimo));
		// Vicino a un capo si arriva fino al capo: niente avanzi di mezza icona
		if (limitata < latoIcona / 2.0d) {
			limitata = 0.0d;
		} else if (limitata > scorrimentoMassimo - latoIcona / 2.0d) {
			limitata = scorrimentoMassimo;
		}
		if (Math.abs(limitata - scorrimento) < 0.5d) {
			return;
		}
		inerzia = false;
		velocita = 0.0d;
		destinazione = limitata;
		haDestinazione = true;
		istanteAggiornamento = adesso();
		inMovimento = true;
	}

	/**
	 * Il click su un'icona: le frecce fanno scorrere le scelte di una pagina, l'interruttore accende e spegne l'aiuto,
	 * le altre mandano il comando al gioco.
	 */
	public void esegui(Comando azione) {
		if (ComandiPossibili.getRicarica(azione) != null) {
			// In ricarica: l'icona si vede, ma non si può dare il comando
			return;
		}
		if (isInterruttoreAiuto(azione)) {
			commutaAiuto(azione);
		} else if (scorrevole && azione == comandoPrecedente) {
			scorriFinoA((haDestinazione ? destinazione : scorrimento) - pagina());
		} else if (scorrevole && azione == comandoSuccessivo) {
			scorriFinoA((haDestinazione ? destinazione : scorrimento) + pagina());
		} else {
			BusEventi.pubblica(new ComandoDiGioco(azione));
		}
	}
}
