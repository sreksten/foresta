package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.intermezzi.Verso;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoOggetto;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.immagini.ClassePersonaggioImmagine;
import com.threeamigos.foresta.ui.immagini.ClassiOggettoImmagine;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;
import com.threeamigos.foresta.ui.sprite.SpriteEffetto;
import com.threeamigos.foresta.ui.sprite.SpriteInDissolvenza;
import com.threeamigos.foresta.ui.sprite.SpriteInterface;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;

/**
 * Il riquadro con la locazione corrente: sfondo, avversari e personaggi che incontrano il gruppo; ospita gli sprite che mostrano cosa accade (danni, effetti, oggetti raccolti).
 */
public class DisplayableCanvasRiquadroLocazione implements Finestra {

	// Per sparpagliare un po' gli avversari: non Dado, che segue il seme della partita e non va consumato dalla UI
	private final Random caso = new Random();

	private static final int SOLLEVAMENTO_ANCORA = 8;
	private static final int SCOSTAMENTO_SOVRAPPOSIZIONE = 20;
	/**
	 * Lo spazio fra un personaggio e il successivo nel suo gruppo, perché non si sovrappongano
	 */
	public static final int DISTANZA_FRA_PERSONAGGI = 10;
	/**
	 * Lo stacco fra lo spazio dell'oggetto al centro e il primo personaggio di ognuno dei due gruppi
	 */
	public static final int STACCO_DALL_OGGETTO = 10;
	/**
	 * Di quanto ogni personaggio è più in alto del successivo nel suo gruppo: più si va verso il bordo, più si scende
	 */
	private static final int PASSO_VERTICALE = 6;
	/**
	 * Due sprite a tempo (salute, magia, livello) più vicini di così in orizzontale si sovrappongono, e se lo sono
	 * anche in verticale il secondo parte più in basso (vedi primoPostoLibero)
	 */
	private static final int DISTANZA_ORIZZONTALE_SPRITE_A_TEMPO = 110;
	/**
	 * Di quanto scende uno sprite a tempo rispetto a quello sopra, in quarti dell'altezza della sua icona: l'icona ha
	 * dei bordi trasparenti, e uno spazio pari a tutta la sua altezza era troppo
	 */
	private static final int QUARTI_DI_ICONA_FRA_SPRITE_A_TEMPO = 3;
	private static final int TENTATIVI_SPRITE_A_TEMPO = 30;

	private final int topLeftX;
	private final int topLeftY;
	// La larghezza dello spazio a disposizione: lo sfondo e l'oggetto stanno al centro, gli avversari a sinistra
	// dell'oggetto e il gruppo a destra, via via verso i bordi
	private final int larghezzaDisponibile;
	// La larghezza dell'oggetto che c'era in locazione all'ultima disposizione dei personaggi (0 se non ce n'era):
	// i personaggi non si spostano se l'oggetto viene raccolto
	private int larghezzaOggetto;

	// Avversari e gruppo del giocatore: lette dall'EDT che disegna e dal thread che notifica gli eventi
	private final Map<VistaPersonaggio, CoordinateMD> mappaCoordinate = new ConcurrentHashMap<>();
	private final Map<VistaPersonaggio, BufferedImage> mappaImmagini = new ConcurrentHashMap<>();
	// I personaggi del gruppo che si disegnano adesso (i vivi, e gli ospiti vivi), nell'ordine: se cambiano, si ridispongono
	private List<VistaPersonaggio> gruppoDisegnato = new ArrayList<>();
	private final List<EffettoAttivo> effettiAttivi = new ArrayList<>();
	private final List<SpriteATempoAttivo> spriteATempoAttivi = new CopyOnWriteArrayList<>();

	private static final class SpriteATempoAttivo {
		final Point posizione;
		final SpriteInterface sprite;
		SpriteATempoAttivo(Point posizione, SpriteInterface sprite) {
			this.posizione = posizione;
			this.sprite = sprite;
		}
	}

	private static final class EffettoAttivo {
		final int yIniziale;
		final SpriteEffetto sprite;
		EffettoAttivo(int yIniziale, SpriteEffetto sprite) {
			this.yIniziale = yIniziale;
			this.sprite = sprite;
		}
	}

	private final VistaPartita vistaPartita;

	public DisplayableCanvasRiquadroLocazione(int topLeftX, int topLeftY, int larghezzaDisponibile, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.larghezzaDisponibile = larghezzaDisponibile;
	}

	/**
	 * Dispone gli avversari nel riquadro.
	 */
	public void assegnaCoordinateAgliAvversari() {
		mappaCoordinate.clear();
		mappaImmagini.clear();
		effettiAttivi.clear();
		larghezzaOggetto = larghezzaOggettoInLocazione();
		VistaGruppo gruppoAvversario = vistaPartita.getGruppoAvversario();
		List<VistaPersonaggio> avversari = new ArrayList<>(gruppoAvversario.getPersonaggi());
		List<BufferedImage> immagini = new ArrayList<>();
		for (VistaPersonaggio avversario : avversari) {
			// Gli avversari stanno a sinistra e guardano a destra, verso il gruppo
			immagini.add(ClassePersonaggioImmagine.getImmagine(avversario.getClasse(), Verso.DESTRA));
		}
		List<Point> posizioni = disponiAvversari(topLeftX, larghezzaDisponibile, larghezzaOggetto,
				ImageCache.locazioni.get(TipoLocazione.BOSCO).getHeight(), immagini);
		for (int i = 0; i < avversari.size(); i++) {
			mappaImmagini.put(avversari.get(i), immagini.get(i));
			// Sparpagliati un poco verso il bordo, meno della distanza fra due personaggi: non si toccano
			mappaCoordinate.put(avversari.get(i),
					new CoordinateMD(posizioni.get(i).x - caso.nextInt(DISTANZA_FRA_PERSONAGGI), posizioni.get(i).y));
		}
		// Le mappe sono state svuotate: anche il gruppo si dispone da capo
		gruppoDisegnato = new ArrayList<>();
		aggiornaGruppo();
	}

	/**
	 * La larghezza dell'immagine dell'oggetto che c'è adesso in locazione, 0 se non c'è (o è un artefatto, che non si mostra)
	 */
	private int larghezzaOggettoInLocazione() {
		TipoOggetto tipoOggetto = vistaPartita.getGruppoGiocatore().getTipoOggettoInLocazione();
		if (tipoOggetto == null || tipoOggetto == TipoOggetto.ARTEFATTO) {
			return 0;
		}
		return ClassiOggettoImmagine.getImmagine(tipoOggetto).getWidth();
	}

	/**
	 * I personaggi del gruppo da disegnare: i vivi e, dopo di loro, gli ospiti vivi
	 */
	private List<VistaPersonaggio> personaggiDelGruppoDaDisegnare() {
		VistaGruppoGiocatore gruppo = vistaPartita.getGruppoGiocatore();
		List<VistaPersonaggio> personaggi = new ArrayList<>(gruppo.getPersonaggiVivi());
		for (VistaPersonaggio ospite : gruppo.getOspiti()) {
			if (ospite.isVivo()) {
				personaggi.add(ospite);
			}
		}
		return personaggi;
	}

	/**
	 * Se chi è nel gruppo e vivo è cambiato (qualcuno è morto, è risorto, è arrivato o se n'è andato) dispone da capo i
	 * personaggi: gli altri scalano per riempire i posti liberi. Si chiama a ogni disegno, ed è subito dopo che i
	 * personaggi cambiano che gli sprite (per esempio la dissolvenza di chi è appena morto) trovano ancora le vecchie
	 * posizioni.
	 */
	private void aggiornaGruppo() {
		List<VistaPersonaggio> personaggi = personaggiDelGruppoDaDisegnare();
		if (personaggi.equals(gruppoDisegnato)) {
			return;
		}
		for (VistaPersonaggio vecchio : gruppoDisegnato) {
			mappaCoordinate.remove(vecchio);
			mappaImmagini.remove(vecchio);
		}
		List<BufferedImage> immagini = new ArrayList<>();
		for (VistaPersonaggio personaggio : personaggi) {
			// Il gruppo guarda a sinistra, verso i mostri
			immagini.add(ClassePersonaggioImmagine.getImmagine(personaggio.getClasse(), Verso.SINISTRA));
		}
		BufferedImage locazione = ImageCache.locazioni.get(TipoLocazione.BOSCO);
		List<Point> posizioni = disponiGruppo(topLeftX, larghezzaDisponibile, larghezzaOggetto, locazione.getHeight(), immagini);
		for (int i = 0; i < personaggi.size(); i++) {
			mappaImmagini.put(personaggi.get(i), immagini.get(i));
			mappaCoordinate.put(personaggi.get(i), new CoordinateMD(posizioni.get(i).x, posizioni.get(i).y));
		}
		gruppoDisegnato = personaggi;
	}

	/**
	 * Di quanto è distante dal punto di partenza ogni personaggio di un gruppo, nell'ordine: il primo sta sul punto di
	 * partenza, ogni altro subito dopo il precedente, a una certa distanza. Se l'ultimo uscirebbe dallo spazio
	 * disponibile le distanze si accorciano e i personaggi si sovrappongono un poco.
	 *
	 * @param spazio quanto si può andare lontano dal punto di partenza, fino al bordo dello spazio a disposizione
	 */
	static int[] distanzeDalPuntoDiPartenza(List<BufferedImage> immagini, int spazio) {
		int[] distanze = new int[immagini.size()];
		for (int i = 1; i < distanze.length; i++) {
			distanze[i] = distanze[i - 1] + immagini.get(i - 1).getWidth() + DISTANZA_FRA_PERSONAGGI;
		}
		if (distanze.length > 1) {
			int ultimo = distanze.length - 1;
			int massimo = spazio - immagini.get(ultimo).getWidth();
			if (distanze[ultimo] > massimo) {
				double fattore = Math.max(0, massimo) / (double) distanze[ultimo];
				for (int i = 1; i < distanze.length; i++) {
					distanze[i] = (int) (distanze[i] * fattore);
				}
			}
		}
		return distanze;
	}

	/**
	 * Il fondo di un personaggio di un gruppo di quel numero: il primo, accanto all'oggetto, sta più in alto, ogni altro
	 * scende di un passo, l'ultimo di un solo passo dal fondo della locazione.
	 */
	private static int yDelPersonaggio(int altezzaLocazione, int numeroPersonaggi, int indice, BufferedImage immagine) {
		return ImageCache.SPACING + altezzaLocazione - (numeroPersonaggi - indice) * PASSO_VERTICALE - immagine.getHeight();
	}

	/**
	 * Dove disegnare il gruppo, a destra dell'oggetto al centro dello spazio a disposizione: il primo, il capo, a poca
	 * distanza dall'oggetto e più in alto, ogni altro più a destra e un po' più in basso.
	 *
	 * @param larghezzaDisponibile la larghezza dello spazio, che comincia in topLeftX
	 * @param larghezzaOggetto lo spazio che l'oggetto occupa al centro
	 * @param immagini le immagini dei personaggi, nell'ordine del gruppo
	 * @return la posizione dell'angolo in alto a sinistra di ognuna, nello stesso ordine
	 */
	public static List<Point> disponiGruppo(int topLeftX, int larghezzaDisponibile, int larghezzaOggetto, int altezzaLocazione,
			List<BufferedImage> immagini) {
		int partenza = topLeftX + (larghezzaDisponibile + larghezzaOggetto) / 2 + STACCO_DALL_OGGETTO;
		int[] distanze = distanzeDalPuntoDiPartenza(immagini, topLeftX + larghezzaDisponibile - 1 - partenza);
		List<Point> posizioni = new ArrayList<>();
		for (int i = 0; i < immagini.size(); i++) {
			posizioni.add(new Point(partenza + distanze[i],
					yDelPersonaggio(altezzaLocazione, immagini.size(), i, immagini.get(i))));
		}
		return posizioni;
	}

	/**
	 * Come {@link #disponiGruppo}, a sinistra dell'oggetto: il primo accanto all'oggetto, ogni altro più a sinistra.
	 */
	public static List<Point> disponiAvversari(int topLeftX, int larghezzaDisponibile, int larghezzaOggetto, int altezzaLocazione,
			List<BufferedImage> immagini) {
		// Il bordo destro del primo: ogni altro arretra verso sinistra
		int partenza = topLeftX + (larghezzaDisponibile - larghezzaOggetto) / 2 - STACCO_DALL_OGGETTO;
		int[] distanze = distanzeDalPuntoDiPartenza(immagini, partenza - topLeftX - 1);
		List<Point> posizioni = new ArrayList<>();
		for (int i = 0; i < immagini.size(); i++) {
			BufferedImage immagine = immagini.get(i);
			posizioni.add(new Point(partenza - immagine.getWidth() - distanze[i],
					yDelPersonaggio(altezzaLocazione, immagini.size(), i, immagine)));
		}
		return posizioni;
	}

	/**
	 * @param agente chi sta agendo (null se non c'è): gli appare una freccia sopra la testa
	 * @param bersaglio il suo bersaglio, se c'è: anche a lui
	 */
	public void disegnaLocazione(Graphics2D graphics, VistaPersonaggio agente, VistaPersonaggio bersaglio) {
		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		VistaGruppo gng = vistaPartita.getGruppoAvversario();
		TipoLocazione tipoLocazione = g.getTipoLocazioneCorrente();
		BufferedImage locazione = ImageCache.locazioni.get(tipoLocazione);
		// Lo sfondo sta al centro dello spazio a disposizione
		graphics.drawImage(locazione, topLeftX + ((larghezzaDisponibile - locazione.getWidth()) >> 1), topLeftY, null);

		// L'oggetto si disegna per primo: i personaggi lo coprono
		TipoOggetto tipoOggetto = g.getTipoOggettoInLocazione();
		if (tipoOggetto != null && tipoOggetto != TipoOggetto.ARTEFATTO) {
			BufferedImage d = ClassiOggettoImmagine.getImmagine(tipoOggetto);
			graphics.drawImage(d, xOggetto(locazione, d), yOggetto(locazione, d), null);
		}

		// Dal primo all'ultimo: chi sta più in basso copre chi sta più in alto
		for (VistaPersonaggio personaggioCorrente : gng.getPersonaggiVivi()) {
			BufferedImage d = mappaImmagini.get(personaggioCorrente);
			CoordinateMD coordinate = mappaCoordinate.get(personaggioCorrente);
			disegnaPersonaggio(graphics, personaggioCorrente, d, coordinate.getX(), coordinate.getY(), agente, bersaglio);
		}

		// Il gruppo, dal primo all'ultimo, come gli avversari
		aggiornaGruppo();
		List<VistaPersonaggio> gruppoDaDisegnare = new ArrayList<>(gruppoDisegnato);
		for (int i = 0; i < gruppoDaDisegnare.size(); i++) {
			CoordinateMD coordinate = mappaCoordinate.get(gruppoDaDisegnare.get(i));
			BufferedImage d = mappaImmagini.get(gruppoDaDisegnare.get(i));
			if (coordinate != null && d != null) {
				disegnaPersonaggio(graphics, gruppoDaDisegnare.get(i), d, coordinate.getX(), coordinate.getY(), agente, bersaglio);
			}
		}
	}
	
	private void disegnaPersonaggio(Graphics2D graphics, VistaPersonaggio personaggio, BufferedImage immagine, int x, int y,
			VistaPersonaggio agente, VistaPersonaggio bersaglio) {
		graphics.drawImage(immagine, x, y, null);
		if (personaggio.equals(agente) || personaggio.equals(bersaglio)) {
			BufferedImage freccia = ImageCache.frecciaGiuPersonaggioAttivo;
			graphics.drawImage(freccia, x + ((immagine.getWidth() - freccia.getWidth()) >> 1), y - freccia.getHeight(), null);
		}
	}

	/**
	 * Crea lo sprite che segnala la morte del personaggio.
	 */
	public SpriteInterface notificaMorte(VistaPersonaggio personaggio) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		return new SpriteInDissolvenza(personaggio.getNome(), mappaImmagini.get(personaggio),
				coordinate.getX(), coordinate.getY());
	}

	SpriteInterface variaLivello(VistaPersonaggio personaggio, int variazione) {
		return spriteATempo(personaggio, ImageCache.spriteAumentoLivello, variazione, "Livello aumentato");
	}

	/**
	 * Crea lo sprite che mostra la variazione di salute del personaggio.
	 */
	public SpriteInterface variaSalute(VistaPersonaggio personaggio, int variazione) {
		return spriteATempo(personaggio, ImageCache.spriteCombattimento, variazione, "Salute modificata");
	}

	/**
	 * Crea lo sprite che mostra la variazione di magia del personaggio.
	 */
	public SpriteInterface variaMagia(VistaPersonaggio personaggio, int variazione) {
		return spriteATempo(personaggio, ImageCache.spriteMagia, variazione, "Magia modificata");
	}

	/**
	 * Lo sprite a tempo di una variazione sopra il personaggio, a destra della sua immagine (e a sinistra del testo);
	 * se ce n'è già uno vicino ancora attivo parte più in basso, così non si sovrappongono. Nessuno sprite per chi
	 * non è in locazione, o se non è cambiato niente.
	 */
	private SpriteInterface spriteATempo(VistaPersonaggio personaggio, BufferedImage icona, int variazione, String descrizione) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		BufferedImage immagine = mappaImmagini.get(personaggio);
		if (coordinate == null || immagine == null || variazione == 0) {
			return null;
		}
		spriteATempoAttivi.removeIf(attivo -> !attivo.sprite.isAttivo());
		List<Point> occupati = new ArrayList<>();
		for (SpriteATempoAttivo attivo : spriteATempoAttivi) {
			occupati.add(attivo.posizione);
		}
		int x = coordinate.getX() + immagine.getWidth();
		int y = primoPostoLibero(occupati, x, coordinate.getY(), icona.getHeight() * QUARTI_DI_ICONA_FRA_SPRITE_A_TEMPO / 4);
		SpriteInterface sprite = new SpriteATempo(icona, variazione, DoomdarkFontMedium.getInstance(), x, y, descrizione);
		spriteATempoAttivi.add(new SpriteATempoAttivo(new Point(x, y), sprite));
		return sprite;
	}

	/**
	 * La prima altezza, a partire da y e scendendo di un passo alla volta, dove uno sprite ancorato in x non si
	 * sovrappone a quelli già attivi (posizioni di partenza di quelli che occupano un posto).
	 */
	public static int primoPostoLibero(List<Point> occupati, int x, int y, int passo) {
		int candidato = y;
		for (int tentativo = 0; tentativo < TENTATIVI_SPRITE_A_TEMPO; tentativo++) {
			boolean libero = true;
			for (Point occupato : occupati) {
				if (Math.abs(occupato.x - x) < DISTANZA_ORIZZONTALE_SPRITE_A_TEMPO && Math.abs(occupato.y - candidato) < passo) {
					candidato = occupato.y + passo;
					libero = false;
					break;
				}
			}
			if (libero) {
				return candidato;
			}
		}
		return candidato;
	}

	/**
	 * Crea lo sprite che mostra il gruppo che raccoglie un oggetto.
	 */
	public SpriteInterface raccogliOggetto() {
		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		BufferedImage locazione = ImageCache.locazioni.get(g.getTipoLocazioneCorrente());
		TipoOggetto tipoOggetto = g.getTipoOggettoInLocazione();
		if (tipoOggetto != null && tipoOggetto != TipoOggetto.ARTEFATTO) {
			BufferedImage d = ClassiOggettoImmagine.getImmagine(tipoOggetto);
			return new SpriteATempo(d, xOggetto(locazione, d), yOggetto(locazione, d),
					"Raccolto oggetto " + tipoOggetto);
		}
		return null;
	}

	/**
	 * L'oggetto che si trova in locazione sta al centro dello spazio a disposizione, in basso: i lati sono dei due gruppi
	 */
	private int xOggetto(BufferedImage locazione, BufferedImage oggetto) {
		return topLeftX + (larghezzaDisponibile - oggetto.getWidth()) / 2;
	}

	private int yOggetto(BufferedImage locazione, BufferedImage oggetto) {
		return ImageCache.SPACING + locazione.getHeight() - oggetto.getHeight() - 5;
	}

	/**
	 * Dove ancorare gli effetti sopra un personaggio: a destra di un avversario, che sta a sinistra della locazione, e
	 * a sinistra di un personaggio del gruppo, che sta a destra: così non escono dal riquadro.
	 */
	private int xEffetto(VistaPersonaggio personaggio, CoordinateMD coordinate, BufferedImage immagine) {
		return personaggio.isPNG() ? coordinate.getX() + immagine.getWidth() : coordinate.getX();
	}

	/**
	 * Crea lo sprite con il nome dell'effetto di stato subito dal personaggio.
	 */
	public SpriteInterface aggiungiEffettoDiStato(VistaPersonaggio personaggio, TipoEffettoDiStato effettoDiStato, DoomdarkColorModel.Color colore) {
		return aggiungiScrittaSulPersonaggio(personaggio, effettoDiStato.getDescrizione(), colore);
	}

	/**
	 * Come l'effetto di stato, ma con la descrizione di un modificatore di attributo ("Furtività +5")
	 */
	public SpriteInterface aggiungiModificatore(VistaPersonaggio personaggio, String descrizione, DoomdarkColorModel.Color colore) {
		return aggiungiScrittaSulPersonaggio(personaggio, descrizione, colore);
	}

	private SpriteInterface aggiungiScrittaSulPersonaggio(VistaPersonaggio personaggio, String descrizione, DoomdarkColorModel.Color colore) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}

		BufferedImage image = mappaImmagini.get(personaggio);
		int x = xEffetto(personaggio, coordinate, image);
		int yIniziale = coordinate.getY() + image.getHeight() / 3 - SOLLEVAMENTO_ANCORA;
		SpriteEffetto sprite = new SpriteEffetto(descrizione, DoomdarkFontMedium.getInstance(),
				colore, x, yIniziale + calcolaOffsetVerticale(yIniziale));
		effettiAttivi.add(new EffettoAttivo(yIniziale, sprite));
		return sprite;
	}

	/**
	 * Crea lo sprite con la descrizione dell'interazione scattata sul personaggio.
	 */
	public SpriteInterface aggiungiInterazione(VistaPersonaggio personaggio, TipoInterazioneConEffettiDiStato interazione) {
		return aggiungiTestoSulPersonaggio(personaggio, interazione.getDescrizione(), DoomdarkColorModel.Color.GREEN);
	}

	/**
	 * Crea lo sprite con quel testo, di quel colore, sul personaggio.
	 */
	public SpriteInterface aggiungiTestoSulPersonaggio(VistaPersonaggio personaggio, String testo, DoomdarkColorModel.Color colore) {
		return aggiungiTestoSulPersonaggio(personaggio, testo, (x, y) ->
				new SpriteEffetto(testo, DoomdarkFontMedium.getInstance(), colore, x, y));
	}

	/**
	 * Come sopra, con un colore costruito al volo (non uno dei colori fissi di DoomdarkColorModel)
	 */
	public SpriteInterface aggiungiTestoSulPersonaggio(VistaPersonaggio personaggio, String testo, java.awt.Color colore) {
		BufferedImage pattern = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		pattern.setRGB(0, 0, colore.getRGB());
		return aggiungiTestoSulPersonaggio(personaggio, testo, (x, y) ->
				new SpriteEffetto(testo, DoomdarkFontMedium.getInstance(), pattern, x, y));
	}

	private SpriteInterface aggiungiTestoSulPersonaggio(VistaPersonaggio personaggio, String testo,
														java.util.function.BiFunction<Integer, Integer, SpriteEffetto> costruttore) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		BufferedImage image = mappaImmagini.get(personaggio);
		int x = xEffetto(personaggio, coordinate, image);
		int yIniziale = coordinate.getY() + image.getHeight() * 2 / 3 - SOLLEVAMENTO_ANCORA;
		SpriteEffetto sprite = costruttore.apply(x, yIniziale + calcolaOffsetVerticale(yIniziale));
		effettiAttivi.add(new EffettoAttivo(yIniziale, sprite));
		return sprite;
	}

	private int calcolaOffsetVerticale(int yIniziale) {
		effettiAttivi.removeIf(e -> !e.sprite.isAttivo());
		long occupati = effettiAttivi.stream()
				.filter(e -> Math.abs(e.yIniziale - yIniziale) < SCOSTAMENTO_SOVRAPPOSIZIONE)
				.count();
		if (occupati == 0) {
			return 0;
		}
		int passo = (int) ((occupati + 1) / 2);
		int segno = (occupati % 2 == 1) ? 1 : -1;
		return segno * passo * SCOSTAMENTO_SOVRAPPOSIZIONE;
	}
}
