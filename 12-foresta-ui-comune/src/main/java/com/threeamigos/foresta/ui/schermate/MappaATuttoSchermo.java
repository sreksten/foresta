package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.mappa.VistaMappa;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.componenti.JoystickVirtuale;
import com.threeamigos.foresta.ui.riquadri.RiquadroMappa;
import com.threeamigos.foresta.ui.riquadri.RiquadroMappa.Tessera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * La mappa della foresta a tutto schermo, senza disegno: dove sta la mappa sullo schermo (si trascina, si centra sul gruppo), le tessere conosciute, il
 * segnalino e gli indicatori, il nome della casella sotto il mouse (con le missioni per cui lampeggia) e, per chi non ha un mouse, il joystick virtuale
 * con la mano-puntatore. In basso c'è la fascia del notiziario, che occupa spazio solo quando ci sono notizie. Chi disegna (Swing, libGDX) mostra quel che
 * gli dice questa classe; le tessere sono nelle coordinate della mappa intera (senza lo spostamento), il resto nelle coordinate dello schermo.
 */
public final class MappaATuttoSchermo implements Finestra {

	// Il tempo fra due fotogrammi non conta oltre questo, perché una pausa non faccia saltare la mano
	private static final long PASSO_MASSIMO_NANOS = 100_000_000L;
	/** La punta del dito nell'immagine della mano */
	public static final int PUNTA_DELLA_MANO_X = 14;
	public static final int PUNTA_DELLA_MANO_Y = 3;

	private final int width;
	private final int height;
	private final VistaPartita vistaPartita;
	private final int larghezzaIcona;
	private final int altezzaIcona;
	private int mappaXOffset;
	private int mappaYOffset;

	private boolean stoTrascinando;
	private int ultimaXMouse;
	private int ultimaYMouse;
	// Dove sta il mouse sopra la mappa, per il nome della casella
	private boolean mouseSopra;
	private int xMouse;
	private int yMouse;

	// Il joystick virtuale, per chi non ha un mouse; null se non è abilitato
	private JoystickVirtuale joystick;
	// Dove sta la punta della mano, in pixel dello schermo, e quando si è calcolato l'ultima volta (0: non ancora)
	private double xMano;
	private double yMano;
	private long ultimoFotogrammaDellaMano;
	// Se la mano è già stata mossa durante questa visita della mappa: la volta dopo riparte da dove si era fermata
	private boolean manoMemorizzata;

	// Le caselle conosciute di tutta la foresta, ricostruite solo quando la mappa cambia
	private List<Tessera> tessere = Collections.emptyList();
	private int versioneDelleTessere = -1;

	/**
	 * @param larghezzaIcona larghezza di una tessera della mappa, già con lo zoom; l'altezza della fascia del notiziario è quella di una tessera
	 */
	public MappaATuttoSchermo(int width, int height, VistaPartita vistaPartita, int larghezzaIcona, int altezzaIcona) {
		this.width = width;
		this.height = height;
		this.vistaPartita = vistaPartita;
		this.larghezzaIcona = larghezzaIcona;
		this.altezzaIcona = altezzaIcona;
	}

	/**
	 * Abilita il joystick virtuale: un disco in basso a destra che, premuto, muove una mano-puntatore al centro della mappa.
	 */
	public void abilitaJoystickVirtuale() {
		joystick = new JoystickVirtuale(width, height);
	}

	public JoystickVirtuale getJoystick() {
		return joystick;
	}

	public int getLarghezzaIcona() {
		return larghezzaIcona;
	}

	public int getAltezzaIcona() {
		return altezzaIcona;
	}

	public int getMappaXOffset() {
		return mappaXOffset;
	}

	public int getMappaYOffset() {
		return mappaYOffset;
	}

	/**
	 * L'altezza della fascia in cui si vede la mappa: la fascia del notiziario occupa spazio solo quando ci sono effettivamente notizie da mostrare, altrimenti
	 * la mappa può usare tutta l'altezza disponibile.
	 */
	public int altezzaMappa() {
		return vistaPartita.getUltimeNotizie().isEmpty() ? height : height - altezzaIcona;
	}

	/**
	 * Se la fascia del notiziario si vede.
	 */
	public boolean isNotiziarioVisibile() {
		return altezzaMappa() < height;
	}

	private int dimensioneMappaX() {
		return vistaPartita.getMappa().getDimensioneX() * larghezzaIcona;
	}

	private int dimensioneMappaY() {
		return vistaPartita.getMappa().getDimensioneY() * altezzaIcona;
	}

	/**
	 * Sposta la mappa in modo che il gruppo sia al centro dello schermo (o, se la mappa è più piccola dello schermo, la centra).
	 */
	public void centraSuGiocatore() {
		int dimensioneMappaX = dimensioneMappaX();
		int dimensioneMappaY = dimensioneMappaY();
		CoordinateMD coordinateGruppo = vistaPartita.getMappa().getPosizioneDelGruppo();
		int posizioneXGiocatoreSuMappa = coordinateGruppo.getX() * larghezzaIcona + larghezzaIcona / 2;
		int posizioneYGiocatoreSuMappa = coordinateGruppo.getY() * altezzaIcona + altezzaIcona / 2;
		if (dimensioneMappaX <= width) {
			mappaXOffset = (width - dimensioneMappaX) / 2;
		} else {
			// Il giocatore al centro, ma senza oltrepassare il bordo sinistro (0) né il destro (width - dimensioneMappaX)
			mappaXOffset = Math.max(width - dimensioneMappaX, Math.min(0, (width / 2) - posizioneXGiocatoreSuMappa));
		}
		int altezzaMappa = altezzaMappa();
		if (dimensioneMappaY <= altezzaMappa) {
			mappaYOffset = (altezzaMappa - dimensioneMappaY) / 2;
		} else {
			mappaYOffset = Math.max(altezzaMappa - dimensioneMappaY, Math.min(0, (altezzaMappa / 2) - posizioneYGiocatoreSuMappa));
		}
	}

	/**
	 * L'altezza disponibile può essere cambiata da un fotogramma all'altro (è appena arrivata la prima notizia, o si è svuotato il notiziario): l'offset va
	 * rivalidato prima di disegnare, con lo stesso clamping usato durante il trascinamento. Da chiamare a ogni disegno.
	 */
	public void rivalidaLOffset() {
		int altezzaMappa = altezzaMappa();
		int dimensioneMappaY = dimensioneMappaY();
		if (dimensioneMappaY <= altezzaMappa) {
			mappaYOffset = (altezzaMappa - dimensioneMappaY) / 2;
		} else {
			mappaYOffset = Math.max(altezzaMappa - dimensioneMappaY, Math.min(0, mappaYOffset));
		}
	}

	/**
	 * Le caselle conosciute di tutta la foresta, nelle coordinate della mappa intera (senza lo spostamento): quella del gruppo manca, c'è il segnalino. Si
	 * ricostruiscono solo quando la mappa è cambiata dall'ultima volta.
	 */
	public List<Tessera> getTessere() {
		VistaMappa mappa = vistaPartita.getMappa();
		int versione = mappa.getVersioneMappa();
		if (versione != versioneDelleTessere) {
			CoordinateMD gruppo = mappa.getPosizioneDelGruppo();
			List<Tessera> nuove = new ArrayList<>();
			for (int x = 0; x < mappa.getDimensioneX(); x++) {
				for (int y = 0; y < mappa.getDimensioneY(); y++) {
					CoordinateMD coordinate = new CoordinateMD(x, y);
					if (coordinate.equals(gruppo) || !mappa.isLocazioneConosciuta(coordinate)) {
						continue;
					}
					TipoLocazione tipo = mappa.getLocazione(coordinate);
					int variante = tipo == TipoLocazione.BOSCO ? mappa.getVarianteBosco(coordinate) : 0;
					nuove.add(new Tessera(x * larghezzaIcona, y * altezzaIcona, tipo, variante, mappa.isLocazioneVisitata(coordinate)));
				}
			}
			tessere = nuove;
			versioneDelleTessere = versione;
		}
		return tessere;
	}

	/**
	 * Se i segnalini sono visibili in questo secondo: lampeggiano.
	 */
	public boolean isSegnaliniVisibili() {
		return RiquadroMappa.segnaliniVisibili();
	}

	/**
	 * Dove sta il segnalino del gruppo, in coordinate dello schermo: sulla casella lasciata vuota nell'immagine della mappa.
	 */
	public Punto getSegnalino() {
		CoordinateMD gruppo = vistaPartita.getMappa().getPosizioneDelGruppo();
		return new Punto(mappaXOffset + gruppo.getX() * larghezzaIcona, mappaYOffset + gruppo.getY() * altezzaIcona);
	}

	/**
	 * Le caselle segnalate (artefatti di cui si è saputo, bersagli di missione), in coordinate dello schermo: lampeggiano come il segnalino.
	 */
	public List<Punto> getIndicatori() {
		List<Punto> indicatori = new ArrayList<>();
		for (CoordinateMD coordinate : vistaPartita.getMappa().getCoordinateDaSegnalare()) {
			indicatori.add(new Punto(mappaXOffset + coordinate.getX() * larghezzaIcona, mappaYOffset + coordinate.getY() * altezzaIcona));
		}
		return indicatori;
	}

	/**
	 * Fa avanzare la mano del tempo passato dall'ultimo fotogramma, se il joystick è premuto. La mano fa da cursore, quindi la sua punta è dove si legge il
	 * nome sotto il mouse; resta dentro la mappa: né dall'immagine, se è più piccola dello schermo, né dalla parte visibile, se è più grande. Da chiamare a
	 * ogni disegno.
	 *
	 * @param ora un istante in nanosecondi (di solito <code>System.nanoTime()</code>)
	 */
	public void aggiornaLaMano(long ora) {
		if (!isManoVisibile()) {
			return;
		}
		if (ultimoFotogrammaDellaMano != 0) {
			double secondi = Math.min(ora - ultimoFotogrammaDellaMano, PASSO_MASSIMO_NANOS) / 1e9;
			xMano += joystick.getVelocitaX() * secondi;
			yMano += joystick.getVelocitaY() * secondi;
		}
		xMano = Math.max(Math.max(0, mappaXOffset), Math.min(Math.min(width, mappaXOffset + dimensioneMappaX()) - 1, xMano));
		yMano = Math.max(Math.max(0, mappaYOffset), Math.min(Math.min(altezzaMappa(), mappaYOffset + dimensioneMappaY()) - 1, yMano));
		ultimoFotogrammaDellaMano = ora;
		xMouse = (int) xMano;
		yMouse = (int) yMano;
	}

	/**
	 * Se si vede la mano: finché il joystick è premuto.
	 */
	public boolean isManoVisibile() {
		return joystick != null && joystick.isAttivo();
	}

	/**
	 * Dove sta il puntatore (il mouse, o la punta della mano).
	 */
	public int getXMouse() {
		return xMouse;
	}

	public int getYMouse() {
		return yMouse;
	}

	private boolean mouseSuCasellaValida() {
		return mouseSopra && !stoTrascinando && yMouse < altezzaMappa();
	}

	private CoordinateMD getCasellaSottoIlMouse() {
		return new CoordinateMD(Math.floorDiv(xMouse - mappaXOffset, larghezzaIcona), Math.floorDiv(yMouse - mappaYOffset, altezzaIcona));
	}

	/**
	 * Il nome della casella sotto il mouse, se il gruppo la conosce e ne ha uno (città, castelli, locande, templi; vedi VistaMappa.getNomeDaMostrare), o
	 * null. Mai mentre si trascina la mappa o sopra il notiziario.
	 */
	public String getNomeSottoIlMouse() {
		return mouseSuCasellaValida() ? vistaPartita.getMappa().getNomeDaMostrare(getCasellaSottoIlMouse()) : null;
	}

	/**
	 * I nomi delle missioni per cui la casella sotto il mouse lampeggia (vedi VistaMappa.getNomiMissioniDaMostrare), nessuno se non lampeggia. Mai mentre si
	 * trascina la mappa o sopra il notiziario.
	 */
	public List<String> getNomiMissioniSottoIlMouse() {
		return mouseSuCasellaValida() ? vistaPartita.getMappa().getNomiMissioniDaMostrare(getCasellaSottoIlMouse()) : Collections.emptyList();
	}

	/**
	 * Le righe del cartiglio accanto al puntatore: il nome della casella sotto il mouse e, se la casella lampeggia anche per delle missioni, una riga con il
	 * nome di ognuna sotto la prima.
	 */
	public List<String> righeDelCartiglio() {
		List<String> righe = new ArrayList<>();
		String nome = getNomeSottoIlMouse();
		if (nome != null) {
			righe.add(Lingue.corrente().inizialeMaiuscola(nome));
		}
		for (String nomeMissione : getNomiMissioniSottoIlMouse()) {
			righe.add(Lingue.corrente().inizialeMaiuscola(nomeMissione));
		}
		return righe;
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
		// Uscendo dal riquadro (sulla barra icone o fuori dal canvas) il rilascio del tasto potrebbe arrivare a un'altra finestra o andare perso: si chiude il
		// trascinamento come se fosse avvenuto qui
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
			// Il punto della mappa sotto il cursore deve restare sotto il cursore, quindi l'offset segue lo spostamento
			mappaXOffset += x - ultimaXMouse;
			mappaYOffset += y - ultimaYMouse;
			ultimaXMouse = x;
			ultimaYMouse = y;

			int dimensioneMappaX = dimensioneMappaX();
			int dimensioneMappaY = dimensioneMappaY();
			if (dimensioneMappaX <= width) {
				// Se la mappa è più piccola dello schermo, l'offset resta al centro
				mappaXOffset = (width - dimensioneMappaX) / 2;
			} else {
				// Altrimenti non si trascina oltre il bordo sinistro (0) o destro (width - dimensioneMappaX)
				mappaXOffset = Math.max(width - dimensioneMappaX, Math.min(0, mappaXOffset));
			}
			int altezzaMappa = altezzaMappa();
			if (dimensioneMappaY <= altezzaMappa) {
				mappaYOffset = (altezzaMappa - dimensioneMappaY) / 2;
			} else {
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
