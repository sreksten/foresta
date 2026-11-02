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
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
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
 * Se le icone non ci stanno tutte scorrono in modo fluido, in pixel: si disegnano con uno scostamento e si ritagliano
 * sulla lunghezza utile della barra. Si scorre trascinando (mouse o dito), con la rotella (di un'icona per scatto, con
 * una breve animazione) e, lasciando andare con una certa velocità, le icone proseguono per inerzia e rallentano;
 * oltre i capi si tirano con un po' di elastico e tornano. Due freccine blu (a sinistra e a destra, o in alto e in
 * basso) segnalano che da quella parte c'è altro, si sovrappongono alle icone e non si toccano: stanno in basso nella
 * barra orizzontale, a destra in quella verticale, e quella verso la fine si ferma prima della riserva a fine asse
 * (vedi setRiservaAFineAsse), lo spazio che ha preso il joystick virtuale.
 * Una pressione che non supera la soglia di trascinamento e viene lasciata sulla stessa icona è un tocco: esegue il
 * comando. Per ricevere il trascinamento anche fuori dalla barra se lo fa catturare (vedi
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

	/**
	 * Quanti pixel, a fine asse, la barra lascia ad altro (il joystick virtuale): le icone e la freccia verso la fine
	 * stanno prima
	 */
	private int riservaAFineAsse = 0;

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
	 * Lo scorrimento delle icone: di quanti pixel sono spostate verso l'inizio della barra (0 le mostra dalla prima),
	 * fino al massimo, con trascinamento, inerzia e animazione delle frecce
	 */
	private final ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(this::adesso);

	public final List<IconaVisibile> iconeVisibili = new ArrayList<>();

	int mouseX = -1;
	int mouseY = -1;
	boolean mousePremuto = false;

	// Il gesto in corso: se la pressione ha fermato le icone in movimento (e allora non è un tocco) e l'icona premuta
	private boolean fermatoInMovimento = false;
	private Comando comandoPremuto = null;

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

		DoomdarkFont fontSmall = DoomdarkFont.piccolo();
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
		scorrimento.reimposta();
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
		int iconePossibili = lunghezzaUtile() / (latoIcona + MARGINE_ICONA);
		scorrevole = quanteScelte > iconePossibili;
		scorrimento.imposta(scorrevole, scorrevole ? Math.max(0, quanteScelte * latoIcona - lunghezzaUtile()) : 0.0d);
		ricostruisciIconeVisibili();
	}

	/**
	 * La lunghezza della barra, lungo il suo asse, che è delle icone: tutta, meno la riserva a fine asse
	 */
	int lunghezzaUtile() {
		return Math.max(0, lunghezzaAsse - riservaAFineAsse);
	}

	/**
	 * Lascia in fondo alla barra (a destra in orizzontale, in basso in verticale) questi pixel: le icone si
	 * dispongono prima, e la freccia verso la fine si ferma prima. Serve a non finire sotto il joystick virtuale.
	 */
	public void setRiservaAFineAsse(int pixel) {
		riservaAFineAsse = Math.max(0, pixel);
		ricalcolaGeometria();
	}

	/**
	 * Le icone che si vedono, anche solo in parte, nell'ordine della barra (la prima e l'ultima possono uscire dalla
	 * barra a metà: le ritaglia il disegno).
	 */
	private void ricostruisciIconeVisibili() {
		iconeVisibili.clear();
		int quanteScelte = comandi.length;
		if (quanteScelte == 0) {
			return;
		}
		if (!scorrevole) {
			int posizione = (lunghezzaUtile() - quanteScelte * latoIcona) >> 1;
			for (int i = 0; i < quanteScelte; i++) {
				aggiungiIcona(posizione + i * latoIcona, comandi[i], icone[i]);
			}
			return;
		}
		int origine = -(int) Math.round(scorrimento.getScorrimento());
		for (int i = 0; i < quanteScelte; i++) {
			int posizione = origine + i * latoIcona;
			if (posizione + latoIcona > 0 && posizione < lunghezzaUtile()) {
				aggiungiIcona(posizione, comandi[i], icone[i]);
			}
		}
	}

	private void aggiungiIcona(int posizioneSuAsse, Comando comando, BufferedImage icona) {
		Rectangle rettangolo = orizzontale
				? new Rectangle(posizioneSuAsse, posizioneTrasversale, ICONA_WIDTH, ICONA_HEIGHT)
				: new Rectangle(posizioneTrasversale, posizioneSuAsse, ICONA_WIDTH, ICONA_HEIGHT);
		iconeVisibili.add(new IconaVisibile(rettangolo, comando, icona));
	}

	public double getScorrimento() {
		return scorrimento.getScorrimento();
	}

	public double getScorrimentoMassimo() {
		return scorrimento.getMassimo();
	}

	/**
	 * Se le icone scorrono (non ci stanno tutte)
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
	 * Fa avanzare l'inerzia o l'animazione di una freccia fino a questo istante: va chiamato a ogni disegno, prima di
	 * disegnare. Durante un trascinamento lo scorrimento lo decide il puntatore.
	 */
	protected void aggiornaScorrimento() {
		if (scorrimento.aggiorna()) {
			ricostruisciIconeVisibili();
		}
	}

	/**
	 * Prepara la geometria che serve a disegnare e a trovare l'icona sotto il puntatore: la barra classica non ha niente
	 * da fare, il Dock ricalcola i rettangoli ingranditi.
	 */
	void preparaGeometria() {
	}

	/**
	 * La fascia in cui le icone si vedono, in coordinate dello schermo, che si estende oltre la barra sull'altro asse
	 * perché le icone ingrandite del Dock possano uscirne.
	 */
	Rectangle fasciaDiRitaglio() {
		return orizzontale
				? new Rectangle(offsetX, offsetY - ICONA_HEIGHT, lunghezzaUtile(), altezza + ICONA_HEIGHT)
				: new Rectangle(offsetX - ICONA_WIDTH, offsetY, larghezza + ICONA_WIDTH, lunghezzaUtile());
	}

	private int asse(int x, int y) {
		return orizzontale ? x : y;
	}

	/**
	 * Se il punto (relativo alla barra) cade nella lunghezza utile della barra, quella in cui le icone si vedono e si
	 * toccano: se le icone scorrono, la parte che esce non si tocca
	 */
	boolean nellaFascia(int x, int y) {
		if (!scorrevole) {
			return true;
		}
		int posizione = asse(x, y);
		return posizione >= 0 && posizione < lunghezzaUtile();
	}

	/**
	 * L'indice, fra le icone visibili, di quella che copre il punto (relativo alla barra), o -1. Un'icona che esce
	 * dalla barra non si tocca nella parte che esce.
	 */
	int indiceSotto(int x, int y) {
		preparaGeometria();
		for (int i = 0; i < iconeVisibili.size(); i++) {
			if (rettangoloDisegnato(i).contains(x, y) && nellaFascia(x, y)) {
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
					disegnaIcona(ritagliato, i);
				}
			} finally {
				ritagliato.dispose();
			}
		} finally {
			icone.dispose();
		}
		disegnaIndicatoriDiScorrimento(graphics);
		copyright(graphics);
	}

	/**
	 * Le due freccine blu sopra le icone: quella verso l'inizio se le icone sono scorse, quella verso la fine se ce ne
	 * sono ancora. Stanno in basso in orizzontale e a destra in verticale, con un po' di spazio dal bordo (come le
	 * frecce degli altri elenchi scorrevoli); quella verso la fine si ferma prima della riserva a fine asse.
	 */
	private void disegnaIndicatoriDiScorrimento(Graphics2D graphics) {
		if (!scorrevole) {
			return;
		}
		boolean altroPrima = scorrimento.getScorrimento() > 0.5d;
		boolean altroDopo = scorrimento.getScorrimento() < scorrimento.getMassimo() - 0.5d;
		Image prima = orizzontale ? ImageCache.componenteScorrevoleFrecciaSinistra : ImageCache.componenteScorrevoleFrecciaSu;
		Image dopo = orizzontale ? ImageCache.componenteScorrevoleFrecciaDestra : ImageCache.componenteScorrevoleFrecciaGiu;
		int spazio = ImageCache.SPACING;
		if (altroPrima) {
			graphics.drawImage(prima, offsetX + (orizzontale ? spazio : larghezza - prima.getWidth(null) - spazio),
					offsetY + (orizzontale ? altezza - prima.getHeight(null) - spazio : spazio), null);
		}
		if (altroDopo) {
			graphics.drawImage(dopo,
					offsetX + (orizzontale ? lunghezzaUtile() - dopo.getWidth(null) - spazio : larghezza - dopo.getWidth(null) - spazio),
					offsetY + (orizzontale ? altezza - dopo.getHeight(null) - spazio : lunghezzaUtile() - dopo.getHeight(null) - spazio), null);
		}
	}

	private void disegnaIcona(Graphics2D graphics, int indice) {
		IconaVisibile iconaVisibile = iconeVisibili.get(indice);
		Rectangle r = rettangoloDisegnato(indice);
		int x = offsetX + r.x;
		int y = offsetY + r.y;
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
		return !scorrimento.isTrascinando() && mouseX >= 0 && disegnato.contains(mouseX, mouseY) && nellaFascia(mouseX, mouseY);
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
		int i = scorrimento.isTrascinando() ? -1 : indiceSotto(mouseX, mouseY);
		if (i < 0) {
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
		return scorrimento.isInMovimento();
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
		int sotto = indiceSotto(x, y);
		comandoPremuto = sotto >= 0 ? iconeVisibili.get(sotto).comando : null;
		// Toccare le icone mentre scorrono le ferma; e quel tocco non vale come tocco su un'icona.
		fermatoInMovimento = scorrimento.premi(asse(x, y), nellaFascia(x, y));
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		mouseX = x;
		mouseY = y;
		scorrimento.trascina(asse(x, y));
		if (scorrimento.isTrascinando()) {
			ricostruisciIconeVisibili();
		}
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
		boolean eraTrascinamento = scorrimento.isTrascinando();
		boolean eraPremuto = scorrimento.isPremuto();
		scorrimento.rilascia();
		if (eraTrascinamento || !eraPremuto || fermatoInMovimento || comandoPremuto == null) {
			return;
		}
		int sotto = indiceSotto(x, y);
		if (sotto >= 0 && iconeVisibili.get(sotto).comando == comandoPremuto) {
			esegui(comandoPremuto);
		}
	}

	/**
	 * La rotella fa scorrere le icone di una per scatto, con la stessa breve animazione delle frecce: su verso l'inizio
	 * della barra (a sinistra in orizzontale, in alto in verticale), giù verso la fine.
	 */
	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (!scorrevole) {
			return;
		}
		double spostamento = (double) numeroRotazioni * latoIcona;
		scorrimento.scorriFinoA(scorrimento.getPosizioneDiArrivo() + (movimentoRotella == MovimentoRotella.SU ? -spostamento : spostamento),
				latoIcona / 2.0d);
	}

	/**
	 * Il tocco si decide al rilascio (vedi processaRilascio): il click che Swing manda dopo non va contato due volte.
	 */
	@Override
	public void processaClick(int x, int y, Tasto tasto) {
	}

	/**
	 * Il click su un'icona: l'interruttore accende e spegne l'aiuto, le altre mandano il comando al gioco.
	 */
	public void esegui(Comando azione) {
		if (ComandiPossibili.getRicarica(azione) != null) {
			// In ricarica: l'icona si vede, ma non si può dare il comando
			return;
		}
		if (isInterruttoreAiuto(azione)) {
			commutaAiuto(azione);
		} else {
			BusEventi.pubblica(new ComandoDiGioco(azione));
		}
	}
}
