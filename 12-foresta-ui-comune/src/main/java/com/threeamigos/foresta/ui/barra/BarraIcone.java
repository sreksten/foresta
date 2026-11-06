package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoImpostazioneAiuto;
import com.threeamigos.foresta.eventi.richieste.RicaricaDelComando;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.List;

/**
 * La barra delle icone di comando, senza disegno. In orizzontale sta in fondo allo schermo, in verticale a destra: l'asse lungo la barra è la larghezza nel
 * primo caso, l'altezza nel secondo.
 * <p>
 * Se le icone non ci stanno tutte scorrono in modo fluido, in pixel: si dispongono con uno scostamento e si ritagliano sulla lunghezza utile della barra. Si
 * scorre trascinando (mouse o dito), con la rotella (di un'icona per scatto, con una breve animazione) e, lasciando andare con una certa velocità, le icone
 * proseguono per inerzia e rallentano; oltre i capi si tirano con un po' di elastico e tornano. Due freccine (a sinistra e a destra, o in alto e in basso)
 * segnalano che da quella parte c'è altro, si sovrappongono alle icone e non si toccano: stanno in basso nella barra orizzontale, a destra in quella
 * verticale, e quella verso la fine si ferma prima della riserva a fine asse ({@link #setRiservaAFineAsse}), lo spazio che ha preso il joystick virtuale.
 * Una pressione che non supera la soglia di trascinamento e viene lasciata sulla stessa icona è un tocco: esegue il comando. Per ricevere il trascinamento
 * anche fuori dalla barra se lo fa catturare ({@link #catturaIlTrascinamento}).
 * <p>
 * In fondo ai comandi la barra aggiunge da sola l'interruttore dell'aiuto, che il gioco non vede: AIUTO quando l'aiuto è spento, NO_AIUTO quando è acceso
 * (l'icona mostra cosa fa il click). La barra lo legge da VistaPartita e lo cambia con ComandoImpostazioneAiuto. Ad aiuto acceso, passando sopra un'icona compare
 * un cartiglio con la descrizione del comando ({@link #descrizioneAiuto}), sempre dentro lo schermo ({@link #posizioneCartiglio}).
 * <p>
 * Le coordinate dei punti e dei rettangoli sono relative all'angolo in alto a sinistra della barra. Quale immagine disegnare per ogni icona lo dice
 * {@link IconaVisibile} (il tipo, se è del capo, se è in ricarica): le immagini le tiene chi disegna (Swing, libGDX).
 */
public class BarraIcone implements Finestra {

	/**
	 * Quanto spazio, oltre alla loro misura, deve avere ogni icona della barra per essere contata fra quelle che ci stanno
	 */
	private static final int MARGINE_ICONA = 4;
	/**
	 * La distanza fra il cartiglio dell'aiuto e l'icona a cui si riferisce
	 */
	private static final int DISTANZA_CARTIGLIO = 4;

	/**
	 * Un'icona che si vede, anche solo in parte, con il rettangolo in cui sta (relativo alla barra) e che cosa disegnarci
	 */
	public static class IconaVisibile {
		public final Area rettangolo;
		public final Comando comando;
		private final Descrizione descrizione;

		IconaVisibile(Area rettangolo, Comando comando, Descrizione descrizione) {
			this.rettangolo = rettangolo;
			this.comando = comando;
			this.descrizione = descrizione;
		}

		/**
		 * L'icona (sfondo e disegno) da cui si ricava l'immagine
		 */
		public TipoIcona getTipo() {
			return descrizione.tipo;
		}

		/**
		 * Se è l'icona del personaggio che è il capo del gruppo: ha lo sfondo rosso
		 */
		public boolean isCapo() {
			return descrizione.capo;
		}

		/**
		 * Se il comando è in ricarica: lo sfondo si riempie dal basso, e non si può dare
		 */
		public boolean isInRicarica() {
			return descrizione.durata > 0;
		}

		public int getTurniRimasti() {
			return descrizione.turniRimasti;
		}

		public int getDurata() {
			return descrizione.durata;
		}
	}

	/**
	 * Che immagine ha un'icona: il tipo, se è del capo, e se è in ricarica quanti turni restano e quanto dura
	 */
	private static final class Descrizione {
		final TipoIcona tipo;
		final boolean capo;
		final int turniRimasti;
		final int durata;

		Descrizione(TipoIcona tipo, boolean capo, int turniRimasti, int durata) {
			this.tipo = tipo;
			this.capo = capo;
			this.turniRimasti = turniRimasti;
			this.durata = durata;
		}
	}

	protected final Orientamento orientamento;
	protected final boolean orizzontale;
	protected final int offsetX;
	protected final int offsetY;
	protected final int larghezza;
	protected final int altezza;
	/**
	 * La misura delle icone dopo lo zoom: 64 x 64 con lo zoom predefinito
	 */
	protected final int larghezzaIcona;
	protected final int altezzaIcona;
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
	 * Quanti pixel, a fine asse, la barra lascia ad altro (il joystick virtuale): le icone e la freccia verso la fine stanno prima
	 */
	private int riservaAFineAsse = 0;

	private Comando[] comandi = new Comando[0];
	private Descrizione[] descrizioni = new Descrizione[0];

	/**
	 * Se le icone non ci stanno: allora le frecce stanno ai capi e le altre scorrono nella fascia di mezzo
	 */
	private boolean scorrevole = false;
	/**
	 * Lo scorrimento delle icone: di quanti pixel sono spostate verso l'inizio della barra (0 le mostra dalla prima), fino al massimo, con trascinamento,
	 * inerzia e animazione delle frecce
	 */
	private final ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(this::adesso);

	public final List<IconaVisibile> iconeVisibili = new ArrayList<>();

	protected int mouseX = -1;
	protected int mouseY = -1;
	protected boolean mousePremuto = false;

	// Il gesto in corso: se la pressione ha fermato le icone in movimento (e allora non è un tocco) e l'icona premuta
	private boolean fermatoInMovimento = false;
	private Comando comandoPremuto = null;

	protected final VistaPartita vistaPartita;

	/**
	 * @param larghezzaIcona la larghezza di un'icona, già con lo zoom (quella dello sfondo)
	 * @param altezzaIcona   la sua altezza
	 */
	public BarraIcone(Orientamento orientamento, int offsetX, int offsetY, int larghezza, int altezza, int larghezzaIcona, int altezzaIcona,
					  VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.orientamento = orientamento;
		this.orizzontale = orientamento == Orientamento.ORIZZONTALE;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.larghezza = larghezza;
		this.altezza = altezza;
		this.larghezzaIcona = larghezzaIcona;
		this.altezzaIcona = altezzaIcona;
		this.lunghezzaAsse = orizzontale ? larghezza : altezza;
		this.latoIcona = orizzontale ? larghezzaIcona : altezzaIcona;
		this.posizioneTrasversale = orizzontale ? (altezza - altezzaIcona) >> 1 : (larghezza - larghezzaIcona) >> 1;
	}

	public Orientamento getOrientamento() {
		return orientamento;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
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
		descrizioni = new Descrizione[quanteScelte];
		for (int i = 0; i < quanteScelte; i++) {
			Comando comando = possibiliAzioni.get(i);
			if (comando == null) {
				throw new IllegalArgumentException("Comando nullo");
			}
			comandi[i] = comando;
			descrizioni[i] = descrivi(comando);
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
	 * Accende o spegne l'aiuto e cambia l'icona dell'interruttore, senza cambiare pagina di scelte. L'icona nuova segue la scelta, senza aspettare che
	 * l'Automa la scriva nel modello dati.
	 */
	private void commutaAiuto(Comando azione) {
		boolean aiutoAbilitato = azione == Comando.AIUTO;
		BusEventi.pubblica(new ComandoImpostazioneAiuto(aiutoAbilitato));
		for (int i = 0; i < comandi.length; i++) {
			if (isInterruttoreAiuto(comandi[i])) {
				comandi[i] = comandoAiuto(aiutoAbilitato);
				descrizioni[i] = descrivi(comandi[i]);
			}
		}
		ricalcolaGeometria();
	}

	private Descrizione descrivi(Comando comando) {
		if (comando.isPersonaggio()) {
			int indice = comando.ordinal() - Comando.PERSONAGGIO_1.ordinal();
			TipoIcona icona = getIconaPersonaggio(indice);
			// Il capo si riconosce dallo sfondo rosso
			VistaPersonaggio capo = vistaPartita.getGruppoGiocatore().getCapo();
			boolean isCapo = capo != null && capo.getUuid().equals(vistaPartita.getGruppoGiocatore().getPersonaggio(indice).getUuid());
			return new Descrizione(icona, isCapo, 0, 0);
		}
		RicaricaDelComando ricarica = ComandiPossibili.getRicarica(comando);
		if (ricarica != null && ricarica.getTurniRimasti() > 0 && ricarica.getDurata() > 0) {
			return new Descrizione(TipoIcona.ofComando(comando), false, ricarica.getTurniRimasti(), ricarica.getDurata());
		}
		return new Descrizione(TipoIcona.ofComando(comando), false, 0, 0);
	}

	private TipoIcona getIconaPersonaggio(int indice) {
		VistaPersonaggio personaggio = vistaPartita.getGruppoGiocatore().getPersonaggio(indice);
		if (personaggio == null) {
			throw new IllegalStateException("Personaggio non trovato con indice " + indice);
		}
		return TipoIcona.ofClasse(personaggio.getClasse());
	}

	/**
	 * Ricalcola quante icone ci stanno e di quanto si può scorrere, tiene lo scorrimento (e la destinazione) dentro i limiti e ricostruisce le icone
	 * visibili. Se le icone ci stanno tutte si mostrano centrate e le frecce non ci sono.
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
	protected int lunghezzaUtile() {
		return Math.max(0, lunghezzaAsse - riservaAFineAsse);
	}

	/**
	 * Lascia in fondo alla barra (a destra in orizzontale, in basso in verticale) questi pixel: le icone si dispongono prima, e la freccia verso la fine si
	 * ferma prima. Serve a non finire sotto il joystick virtuale.
	 */
	public void setRiservaAFineAsse(int pixel) {
		riservaAFineAsse = Math.max(0, pixel);
		ricalcolaGeometria();
	}

	/**
	 * Le icone che si vedono, anche solo in parte, nell'ordine della barra (la prima e l'ultima possono uscire dalla barra a metà: le ritaglia il disegno).
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
				aggiungiIcona(posizione + i * latoIcona, comandi[i], descrizioni[i]);
			}
			return;
		}
		int origine = -(int) Math.round(scorrimento.getScorrimento());
		for (int i = 0; i < quanteScelte; i++) {
			int posizione = origine + i * latoIcona;
			if (posizione + latoIcona > 0 && posizione < lunghezzaUtile()) {
				aggiungiIcona(posizione, comandi[i], descrizioni[i]);
			}
		}
	}

	private void aggiungiIcona(int posizioneSuAsse, Comando comando, Descrizione descrizione) {
		Area rettangolo = orizzontale
				? new Area(posizioneSuAsse, posizioneTrasversale, larghezzaIcona, altezzaIcona)
				: new Area(posizioneTrasversale, posizioneSuAsse, larghezzaIcona, altezzaIcona);
		iconeVisibili.add(new IconaVisibile(rettangolo, comando, descrizione));
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
	 * Fa avanzare l'inerzia o l'animazione di una freccia fino a questo istante: va chiamato a ogni disegno, prima di disegnare. Durante un trascinamento lo
	 * scorrimento lo decide il puntatore.
	 */
	protected void aggiornaScorrimento() {
		if (scorrimento.aggiorna()) {
			ricostruisciIconeVisibili();
		}
	}

	/**
	 * Prepara la geometria che serve a disegnare e a trovare l'icona sotto il puntatore: la barra classica non ha niente da fare, il Dock ricalcola i
	 * rettangoli ingranditi. Si chiama a ogni fotogramma, dopo {@link #aggiornaScorrimento()}, prima di disegnare.
	 */
	protected void preparaGeometria() {
	}

	/**
	 * La fascia in cui le icone si vedono, relativa alla barra, che si estende oltre la barra sull'altro asse perché le icone ingrandite del Dock possano
	 * uscirne
	 */
	public Area fasciaDiRitaglio() {
		return orizzontale
				? new Area(0, -altezzaIcona, lunghezzaUtile(), altezza + altezzaIcona)
				: new Area(-larghezzaIcona, 0, larghezza + larghezzaIcona, lunghezzaUtile());
	}

	private int asse(int x, int y) {
		return orizzontale ? x : y;
	}

	/**
	 * Se il punto (relativo alla barra) cade nella lunghezza utile della barra, quella in cui le icone si vedono e si toccano: se le icone scorrono, la parte
	 * che esce non si tocca
	 */
	protected boolean nellaFascia(int x, int y) {
		if (!scorrevole) {
			return true;
		}
		int posizione = asse(x, y);
		return posizione >= 0 && posizione < lunghezzaUtile();
	}

	/**
	 * L'indice, fra le icone visibili, di quella che copre il punto (relativo alla barra), o -1. Un'icona che esce dalla barra non si tocca nella parte che
	 * esce.
	 */
	protected int indiceSotto(int x, int y) {
		preparaGeometria();
		for (int i = 0; i < iconeVisibili.size(); i++) {
			if (rettangoloDisegnato(i).contiene(x, y) && nellaFascia(x, y)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Se c'è altro prima delle icone che si vedono (le icone sono scorse): la freccia verso l'inizio
	 */
	public boolean isAltroPrima() {
		return scorrevole && scorrimento.getScorrimento() > 0.5d;
	}

	/**
	 * Se c'è altro dopo: la freccia verso la fine
	 */
	public boolean isAltroDopo() {
		return scorrevole && scorrimento.getScorrimento() < scorrimento.getMassimo() - 0.5d;
	}

	/**
	 * Dove sta, relativa alla barra, una freccia di scorrimento di quella misura. Stanno in basso in orizzontale e a destra in verticale, con un po' di
	 * spazio dal bordo (come le frecce degli altri elenchi scorrevoli); quella verso la fine si ferma prima della riserva a fine asse.
	 *
	 * @param verso la freccia verso l'inizio (false: verso la fine)
	 */
	public Punto posizioneDellaFreccia(boolean verso, Misura freccia) {
		int spazio = Finestra.SPACING;
		if (verso) {
			return new Punto(orizzontale ? spazio : larghezza - freccia.larghezza - spazio, orizzontale ? altezza - freccia.altezza - spazio : spazio);
		}
		return new Punto(orizzontale ? lunghezzaUtile() - freccia.larghezza - spazio : larghezza - freccia.larghezza - spazio,
				orizzontale ? altezza - freccia.altezza - spazio : lunghezzaUtile() - freccia.altezza - spazio);
	}

	/**
	 * Il rettangolo, relativo alla barra, in cui è disegnata adesso l'icona di iconeVisibili con quell'indice
	 */
	public Area rettangoloDisegnato(int indice) {
		return iconeVisibili.get(indice).rettangolo;
	}

	/**
	 * Se il cursore sta su questa icona (durante un trascinamento non sta su nessuna) e il suo comando si può dare: quella sotto il cursore si evidenzia
	 */
	public boolean isDaEvidenziare(int indice) {
		IconaVisibile icona = iconeVisibili.get(indice);
		return !icona.isInRicarica() && ComandiPossibili.getRicarica(icona.comando) == null && isSottoIlCursore(rettangoloDisegnato(indice));
	}

	private boolean isSottoIlCursore(Area disegnato) {
		return !scorrimento.isTrascinando() && mouseX >= 0 && disegnato.contiene(mouseX, mouseY) && nellaFascia(mouseX, mouseY);
	}

	/**
	 * Se il tasto è premuto (l'icona sotto il cursore si evidenzia di più)
	 */
	public boolean isMousePremuto() {
		return mousePremuto;
	}

	/**
	 * Ad aiuto acceso, l'indice (fra le icone visibili) dell'icona sotto il mouse di cui mostrare il cartiglio; -1 se non c'è, se l'aiuto è spento o se si
	 * sta trascinando
	 */
	public int getIndiceDelCartiglio() {
		if (!vistaPartita.isAiutoAbilitato() || mouseX < 0) {
			return -1;
		}
		return scorrimento.isTrascinando() ? -1 : indiceSotto(mouseX, mouseY);
	}

	/**
	 * Da chiamare a ogni fotogramma prima di disegnare: fa avanzare l'inerzia e le animazioni e prepara la geometria delle icone
	 */
	public void preparaPerDisegno() {
		aggiornaScorrimento();
		preparaGeometria();
	}

	/**
	 * Il testo del cartiglio: la descrizione del comando, tranne per l'icona di un personaggio, che porta il suo nome
	 */
	public String descrizioneAiuto(Comando comando) {
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
	 * Dove mettere un cartiglio largo e alto così per l'icona in quel rettangolo (coordinate dello schermo): con la barra in basso sopra l'icona e centrato
	 * su di lei, con la barra a destra alla sua sinistra e centrato in altezza; in ogni caso spostato quanto serve per restare dentro lo schermo, al peggio
	 * allineato a un suo bordo.
	 */
	public static Punto posizioneCartiglio(Orientamento orientamento, Area icona, int larghezza, int altezza, int larghezzaSchermo, int altezzaSchermo) {
		int x;
		int y;
		if (orientamento == Orientamento.ORIZZONTALE) {
			x = icona.getX() + (icona.getLarghezza() - larghezza) / 2;
			y = icona.getY() - altezza - DISTANZA_CARTIGLIO;
		} else {
			x = icona.getX() - larghezza - DISTANZA_CARTIGLIO;
			y = icona.getY() + (icona.getAltezza() - altezza) / 2;
		}
		x = Math.max(0, Math.min(x, larghezzaSchermo - larghezza));
		y = Math.max(0, Math.min(y, altezzaSchermo - altezza));
		return new Punto(x, y);
	}

	/**
	 * Se il punto (relativo all'angolo della barra) spetta alla barra: qui il suo rettangolo, ma una barra le cui icone escono dal rettangolo (vedi
	 * {@link BarraIconeDock}) può reclamare anche i punti fuori.
	 */
	public boolean contiene(int x, int y) {
		return x >= 0 && y >= 0 && x < larghezza && y < altezza;
	}

	/**
	 * Se la barra sta animando qualcosa da sola, anche a mouse fermo (l'inerzia dopo un trascinamento, lo scorrimento di una freccia): allora chi disegna la
	 * ridisegna a ogni fotogramma.
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
	 * Lasciando il tasto: dopo un trascinamento le icone proseguono per inerzia se il puntatore si muoveva abbastanza in fretta; se invece non ha superato la
	 * soglia è un tocco, e se cade sull'icona premuta la esegue.
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
	 * La rotella fa scorrere le icone di una per scatto, con la stessa breve animazione delle frecce: su verso l'inizio della barra (a sinistra in
	 * orizzontale, in alto in verticale), giù verso la fine.
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
	 * Il tocco si decide al rilascio (vedi processaRilascio): il click che il toolkit manda dopo non va contato due volte.
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
