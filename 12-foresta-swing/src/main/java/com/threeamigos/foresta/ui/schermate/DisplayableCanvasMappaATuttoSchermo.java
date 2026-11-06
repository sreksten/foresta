package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.barra.Cartiglio;
import com.threeamigos.foresta.ui.componenti.JoystickVirtuale;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sfx.GestoreNuvole;
import com.threeamigos.foresta.ui.sfx.NuvoleAwt;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.threeamigos.foresta.testi.Lingue;

/**
 * La mappa della foresta a tutto schermo: si trascina con il mouse, mostra il nome della casella sotto il cursore e, in alto, il notiziario.
 * Con il joystick virtuale (vedi {@link #abilitaJoystickVirtuale()}) c'è anche un disco in basso a destra che, premuto, fa
 * comparire una mano al centro della mappa: la mano si muove finché il disco è premuto, con la velocità e la direzione date
 * dalla posizione del puntatore rispetto al disco, e fa da puntatore (mostra il nome di ciò che ha sotto); al rilascio sparisce.
 */
public class DisplayableCanvasMappaATuttoSchermo extends DisegnatoreMappa implements Finestra {

	// Il notiziario si ferma a metà della larghezza della sfera magica: l'altra
	// metà è coperta dalla sfera stessa (disegnata in primo piano sull'angolo
	// inferiore sinistro di tutto il canvas, vedi DisplayableCanvas.disegnaSferaMagica),
	// ma essendo un'immagine con trasparenza le notizie continuano a intravedersi
	// dietro di essa fino al centro, dove l'immagine diventa opaca.
	private static final int LARGHEZZA_SFERA_MAGICA_COPERTA = ImageCache.sferaMagica.getWidth() / 2;

	private final int width;
	private final int height;
	private final Notiziario notiziario;
	private int mappaXOffset;
	private int mappaYOffset;

	private boolean stoTrascinando;
	private int ultimaXMouse;
	private int ultimaYMouse;
	// Dove sta il mouse sopra la mappa, per il nome della casella (vedi disegnaNomeSottoIlMouse)
	private boolean mouseSopra;
	private int xMouse;
	private int yMouse;

	// Il joystick virtuale, per chi non ha un mouse; null se non è abilitato
	private JoystickVirtuale joystick;
	// Dove sta la punta della mano, in pixel dello schermo, e quando si è disegnato l'ultima volta (0: non ancora)
	private double xMano;
	private double yMano;
	private long ultimoFotogrammaDellaMano;
	// Se la mano è già stata mossa durante questa visita della mappa: la volta dopo riparte da dove si era fermata
	private boolean manoMemorizzata;
	// Il tempo fra due fotogrammi non conta oltre questo, perché una pausa non faccia saltare la mano
	private static final long PASSO_MASSIMO_NANOS = 100_000_000L;
	// La punta del dito nell'immagine della mano
	private static final int PUNTA_DELLA_MANO_X = 14;
	private static final int PUNTA_DELLA_MANO_Y = 3;


	public DisplayableCanvasMappaATuttoSchermo(int width, int height, VistaPartita vistaPartita) {
		super(vistaPartita);
		this.width = width;
		this.height = height;
		this.notiziario = new Notiziario(width - LARGHEZZA_SFERA_MAGICA_COPERTA, ALTEZZA_ICONA, vistaPartita);
	}

	/**
	 * Abilita il joystick virtuale: un disco in basso a destra che, premuto, muove una mano-puntatore al centro della mappa.
	 */
	public void abilitaJoystickVirtuale() {
		joystick = new JoystickVirtuale(width, height);
	}

	// La fascia del notiziario occupa spazio solo quando ci sono effettivamente
	// notizie da mostrare: altrimenti la mappa può usare tutta l'altezza disponibile.
	private int altezzaMappa() {
		return vistaPartita.getUltimeNotizie().isEmpty() ? height : height - ALTEZZA_ICONA;
	}

	/**
	 * Sposta la mappa in modo che il gruppo sia al centro dello schermo (o, se la mappa è più piccola dello schermo, la centra).
	 */
	public void centraSuGiocatore() {
		// Ho una mappa che può essere più o meno grande rispetto a uno schermo.
		// Le dimensioni dello schermo sono width e height.
		// L'immagine che rappresenta la mappa ha dimensioni:
		int dimensioneMappaX = vistaPartita.getMappa().getDimensioneX() * LARGHEZZA_ICONA;
		int dimensioneMappaY = vistaPartita.getMappa().getDimensioneY() * ALTEZZA_ICONA;
		// Il giocatore rappresentato sulla mappa si trova in posizione:
		CoordinateMD coordinateGruppo = vistaPartita.getMappa().getPosizioneDelGruppo();
		int posizioneXGiocatoreSuMappa = coordinateGruppo.getX() * LARGHEZZA_ICONA +
				LARGHEZZA_ICONA / 2;
		int posizioneYGiocatoreSuMappa = coordinateGruppo.getY() * ALTEZZA_ICONA +
				ALTEZZA_ICONA / 2;
		// Vogliamo rappresentare la mappa a video inizialmente con il giocatore
		// posizionato al centro.
		if (dimensioneMappaX <= width) {
			// Se la larghezza della mappa è inferiore a quella dello schermo allora
			// l'offset x è centrato rispetto allo schermo.
			mappaXOffset = (width - dimensioneMappaX) / 2;
		} else {
			// 1. Calcoliamo la posizione teorica per mettere il giocatore al centro esatto della width dello schermo
			int offsetTeoricoX = (width / 2) - posizioneXGiocatoreSuMappa;

			// 2. Blocchiamo l'offset in modo che non superi lo 0 (bordo sinistro)
			// e non scenda sotto la differenza minima (bordo destro)
			mappaXOffset = Math.max(width - dimensioneMappaX, Math.min(0, offsetTeoricoX));
		}

		int altezzaMappa = altezzaMappa();
		if (dimensioneMappaY <= altezzaMappa) {
			// Se l'altezza della mappa è inferiore a quella dell'area disponibile allora
			// l'offset y è centrato rispetto ad essa.
			mappaYOffset = (altezzaMappa - dimensioneMappaY) / 2;
		} else {
			// 1. Calcoliamo la posizione teorica per mettere il giocatore al centro esatto dell'area disponibile
			int offsetTeoricoY = (altezzaMappa / 2) - posizioneYGiocatoreSuMappa;

			// 2. Blocchiamo l'offset in modo che non superi lo 0 (bordo superiore)
			// e non scenda sotto la differenza minima (bordo inferiore)
			mappaYOffset = Math.max(altezzaMappa - dimensioneMappaY, Math.min(0, offsetTeoricoY));
		}
	}

	/**
	 * Il segnalino del gruppo, acceso un secondo sì e uno no, sulla casella lasciata
	 * vuota nell'immagine della mappa.
	 */
	private void disegnaSegnalino(Graphics2D graphics) {
		CoordinateMD coordinateGruppo = vistaPartita.getMappa().getPosizioneDelGruppo();
		graphics.drawImage(ImageCache.segnalino,
				mappaXOffset + coordinateGruppo.getX() * LARGHEZZA_ICONA,
				mappaYOffset + coordinateGruppo.getY() * ALTEZZA_ICONA, null);
	}

	/**
	 * Le caselle segnalate (artefatti di cui si è saputo, bersagli di missione), lampeggianti
	 * come il segnalino del gruppo: vanno ridisegnate a ogni frame perché non fanno parte
	 * dell'immagine statica della mappa (vedi DisegnatoreMappa.ottieniMappaGenerale).
	 */
	private void disegnaIndicatori(Graphics2D graphics) {
		for (CoordinateMD coordinate : vistaPartita.getMappa().getCoordinateDaSegnalare()) {
			graphics.drawImage(ImageCache.indicatore,
					mappaXOffset + coordinate.getX() * LARGHEZZA_ICONA,
					mappaYOffset + coordinate.getY() * ALTEZZA_ICONA, null);
		}
	}

	/**
	 * Disegna la mappa con le caselle conosciute e il gruppo, e il notiziario.
	 */
	public void disegnaMappaATuttoSchermo(Graphics2D graphics) {

		int altezzaMappa = altezzaMappa();
		int dimensioneMappaX = vistaPartita.getMappa().getDimensioneX() * LARGHEZZA_ICONA;
		int dimensioneMappaY = vistaPartita.getMappa().getDimensioneY() * ALTEZZA_ICONA;

		// L'altezza disponibile può essere cambiata da un frame all'altro (es. è appena
		// arrivata la prima notizia, o si è svuotato il notiziario): l'offset va rivalidato
		// prima di disegnare, con lo stesso clamping usato durante il trascinamento.
		if (dimensioneMappaY <= altezzaMappa) {
			mappaYOffset = (altezzaMappa - dimensioneMappaY) / 2;
		} else {
			mappaYOffset = Math.max(altezzaMappa - dimensioneMappaY, Math.min(0, mappaYOffset));
		}

		// Salva lo stato originale della Clip e del Composite
		Shape originalClip = graphics.getClip();
		Composite originalComposite = graphics.getComposite();

		// Limita il disegno della mappa (immagine + nuvole) alla sola fascia sopra il
		// notiziario, così non può mai debordare nella fascia in basso
		graphics.clipRect(0, 0, width, altezzaMappa);

		graphics.drawImage(ottieniMappaGenerale(), mappaXOffset, mappaYOffset, null);

		if (isSegnaliniVisibili()) {
			disegnaSegnalino(graphics);
			disegnaIndicatori(graphics);
		}

		GestoreNuvole.assicuraGenerate(vistaPartita.getMappa(), width, height, LARGHEZZA_ICONA, ALTEZZA_ICONA);

		// Applica la clip sull'area occupata dalla mappa, in modo che le nuvole non
		// vengano disegnate al di fuori di essa quando la mappa è più piccola dello schermo
		graphics.clipRect(mappaXOffset, mappaYOffset, dimensioneMappaX, dimensioneMappaY);

		// Imposta la trasparenza e disegna le nuvole condivise con il riquadro mappa,
		// allineate rispetto alla cella (0,0) della foresta che qui corrisponde sempre
		// all'angolo in alto a sinistra dell'immagine disegnata a (mappaXOffset, mappaYOffset)
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
		NuvoleAwt.disegna(graphics, 0, 0, mappaXOffset, mappaYOffset, LARGHEZZA_ICONA, ALTEZZA_ICONA);

		// Ripristina la clip e il composite originali
		graphics.setComposite(originalComposite);
		graphics.setClip(originalClip);

		// Aggiorna la posizione delle nuvolette
		GestoreNuvole.aggiorna();

		if (altezzaMappa < height) {
			notiziario.disegna(graphics, LARGHEZZA_SFERA_MAGICA_COPERTA, altezzaMappa);
		}
		disegnaJoystickEMano(graphics, altezzaMappa);
		disegnaNomeSottoIlMouse(graphics, altezzaMappa);
	}

	/**
	 * Il disco del joystick e, finché è premuto, la mano: che nel frattempo si è mossa per il tempo passato dall'ultimo
	 * fotogramma. La mano fa da cursore, quindi la sua punta è dove si legge il nome sotto il mouse; resta dentro la mappa.
	 */
	private void disegnaJoystickEMano(Graphics2D graphics, int altezzaMappa) {
		if (joystick == null) {
			return;
		}
		if (joystick.isAttivo()) {
			long ora = System.nanoTime();
			if (ultimoFotogrammaDellaMano != 0) {
				double secondi = Math.min(ora - ultimoFotogrammaDellaMano, PASSO_MASSIMO_NANOS) / 1e9;
				xMano += joystick.getVelocitaX() * secondi;
				yMano += joystick.getVelocitaY() * secondi;
			}
			// La mano non esce dalla mappa: né dall'immagine, se è più piccola dello schermo, né dalla parte visibile, se è più grande
			int dimensioneMappaX = vistaPartita.getMappa().getDimensioneX() * LARGHEZZA_ICONA;
			int dimensioneMappaY = vistaPartita.getMappa().getDimensioneY() * ALTEZZA_ICONA;
			xMano = Math.max(Math.max(0, mappaXOffset), Math.min(Math.min(width, mappaXOffset + dimensioneMappaX) - 1, xMano));
			yMano = Math.max(Math.max(0, mappaYOffset), Math.min(Math.min(altezzaMappa, mappaYOffset + dimensioneMappaY) - 1, yMano));
			ultimoFotogrammaDellaMano = ora;
			xMouse = (int) xMano;
			yMouse = (int) yMano;
		}
		joystick.disegna(graphics);
		if (joystick.isAttivo()) {
			graphics.drawImage(ImageCache.manoPuntatore, xMouse - PUNTA_DELLA_MANO_X, yMouse - PUNTA_DELLA_MANO_Y, null);
		}
	}

	private boolean mouseSuCasellaValida() {
		return mouseSopra && !stoTrascinando && yMouse < altezzaMappa();
	}

	private CoordinateMD getCasellaSottoIlMouse() {
		return new CoordinateMD(Math.floorDiv(xMouse - mappaXOffset, LARGHEZZA_ICONA),
				Math.floorDiv(yMouse - mappaYOffset, ALTEZZA_ICONA));
	}

	/**
	 * Il nome della casella sotto il mouse, se il gruppo la conosce e ne ha uno (città, castelli, locande, templi;
	 * vedi VistaMappa.getNomeDaMostrare), o null. Mai mentre si trascina la mappa o sopra il notiziario.
	 */
	public String getNomeSottoIlMouse() {
		return mouseSuCasellaValida() ? vistaPartita.getMappa().getNomeDaMostrare(getCasellaSottoIlMouse()) : null;
	}

	/**
	 * I nomi delle missioni per cui la casella sotto il mouse lampeggia (vedi VistaMappa.getNomiMissioniDaMostrare),
	 * nessuno se non lampeggia. Mai mentre si trascina la mappa o sopra il notiziario.
	 */
	List<String> getNomiMissioniSottoIlMouse() {
		return mouseSuCasellaValida() ? vistaPartita.getMappa().getNomiMissioniDaMostrare(getCasellaSottoIlMouse())
				: Collections.emptyList();
	}

	/**
	 * Il nome della casella sotto il mouse su un cartiglio accanto al puntatore, tenuto dentro la mappa; se la casella
	 * lampeggia anche per delle missioni (vedi getNomiMissioniSottoIlMouse), una riga con il nome di ognuna sotto la
	 * prima.
	 */
	private void disegnaNomeSottoIlMouse(Graphics2D graphics, int altezzaMappa) {
		List<String> righe = new ArrayList<>();
		String nome = getNomeSottoIlMouse();
		if (nome != null) {
			righe.add(Lingue.corrente().inizialeMaiuscola(nome));
		}
		for (String nomeMissione : getNomiMissioniSottoIlMouse()) {
			righe.add(Lingue.corrente().inizialeMaiuscola(nomeMissione));
		}
		Cartiglio.disegnaAccantoAlMouse(graphics, righe, xMouse, yMouse, width, altezzaMappa);
	}


	@Override
	public void processaMovimento(int x, int y) {
		if (joystickAttivo()) {
			return;
		}
		mouseSopra = true;
		xMouse = x;
		yMouse = y;
	}

	@Override
	public void processaPressione(int x, int y, Finestra.Tasto tasto) {
		if (joystick != null && tasto == Tasto.SINISTRO && joystick.attiva(x, y)) {
			// La mano parte da dove si era fermata, o al primo uso dal centro della mappa
			if (!manoMemorizzata) {
				xMano = width / 2.0;
				yMano = altezzaMappa() / 2.0;
				manoMemorizzata = true;
			}
			xMouse = (int) xMano;
			yMouse = (int) yMano;
			ultimoFotogrammaDellaMano = 0;
			mouseSopra = true;
			return;
		}
		if (tasto == Tasto.SINISTRO && y < altezzaMappa()) {
			stoTrascinando = true;
			ultimaXMouse = x;
			ultimaYMouse = y;
		}
	}

	@Override
	public void processaRilascio(int x, int y, Finestra.Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			if (joystickAttivo()) {
				// La mano scompare, e con lei il nome che mostrava
				joystick.rilascia();
				mouseSopra = false;
			}
			stoTrascinando = false;
		}
	}

	@Override
	public void processaUscita(int x, int y) {
		if (joystickAttivo()) {
			return; // il rilascio arriva comunque (vedi catturaIlTrascinamento)
		}
		// Uscendo dal riquadro (sulla barra icone o fuori dal canvas) il rilascio del tasto potrebbe
		// arrivare a un'altra finestra o andare perso: si chiude il trascinamento come se fosse avvenuto qui
		stoTrascinando = false;
		mouseSopra = false;
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		if (joystickAttivo()) {
			joystick.sposta(x, y);
			return;
		}
		if (stoTrascinando && y >= altezzaMappa()) {
			// Sceso sul notiziario: si esce dalla zona della mappa, come per processaUscita
			stoTrascinando = false;
		}
		xMouse = x;
		yMouse = y;
		if (stoTrascinando) {
			// 1. Calcoliamo il delta (differenza rispetto alla posizione precedente)
			int deltaX = x - ultimaXMouse;
			int deltaY = y - ultimaYMouse;

			// Aggiorniamo la posizione precedente
			ultimaXMouse = x;
			ultimaYMouse = y;

			// 2. Applichiamo lo spostamento del mouse: il punto della mappa sotto il
			// cursore deve restare sotto il cursore, quindi l'offset segue il delta.
			mappaXOffset += deltaX;
			mappaYOffset += deltaY;

			// Ricalcoliamo al volo le dimensioni reali della mappa
			int dimensioneMappaX = vistaPartita.getMappa().getDimensioneX() * LARGHEZZA_ICONA;
			int dimensioneMappaY = vistaPartita.getMappa().getDimensioneY() * ALTEZZA_ICONA;

			// 3. APPLICAZIONE DEL CLAMPING SULL'ASSE X
			if (dimensioneMappaX <= width) {
				// Se la mappa è più piccola dello schermo, costringiamo l'offset al centro fisso
				mappaXOffset = (width - dimensioneMappaX) / 2;
			} else {
				// Se è più grande, impediamo di trascinare oltre il bordo sinistro (0) o destro (width - dimensioneMappaX)
				mappaXOffset = Math.max(width - dimensioneMappaX, Math.min(0, mappaXOffset));
			}

			// 4. APPLICAZIONE DEL CLAMPING SULL'ASSE Y
			int altezzaMappa = altezzaMappa();
			if (dimensioneMappaY <= altezzaMappa) {
				// Se la mappa è più bassa dell'area disponibile, la costringiamo al centro fisso
				mappaYOffset = (altezzaMappa - dimensioneMappaY) / 2;
			} else {
				// Se è più grande, impediamo di trascinare oltre il bordo superiore (0) o inferiore (altezzaMappa - dimensioneMappaY)
				mappaYOffset = Math.max(altezzaMappa - dimensioneMappaY, Math.min(0, mappaYOffset));
			}
		}
	}

	/**
	 * Si entra nella schermata della mappa: la mano, la prossima volta, parte dal centro.
	 */
	public void dimenticaLaMano() {
		manoMemorizzata = false;
	}

	private boolean joystickAttivo() {
		return joystick != null && joystick.isAttivo();
	}

	/**
	 * Con il joystick il gesto, fuori dal disco, resta suo finché il tasto non è rilasciato.
	 */
	@Override
	public boolean catturaIlTrascinamento() {
		return joystick != null;
	}

	@Override
	public boolean gestisceDoppioClick() {
		return true;
	}

	@Override
	public void processaDoppioClick(int x, int y, Finestra.Tasto tasto) {
		centraSuGiocatore();
	}
}
