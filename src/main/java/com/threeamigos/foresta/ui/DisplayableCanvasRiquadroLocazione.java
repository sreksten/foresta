package com.threeamigos.foresta.ui;

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

class DisplayableCanvasRiquadroLocazione implements Finestra {

	// Per sparpagliare un po' gli avversari: non Dado, che segue il seme della partita e non va consumato dalla UI
	private final Random caso = new Random();

	private static final int SOLLEVAMENTO_ANCORA = 8;
	private static final int SCOSTAMENTO_SOVRAPPOSIZIONE = 20;
	/**
	 * Di quanto si sposta di lato e sale ogni personaggio rispetto al precedente nel suo gruppo
	 */
	private static final int PASSO_ORIZZONTALE = 20;
	/**
	 * Il gruppo del giocatore sta più largo degli avversari: ogni personaggio è più distante dal precedente
	 */
	private static final int PASSO_ORIZZONTALE_GRUPPO = 40;
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

	DisplayableCanvasRiquadroLocazione(int topLeftX, int topLeftY, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
	}

	void assegnaCoordinateAgliAvversari() {
		mappaCoordinate.clear();
		mappaImmagini.clear();
		effettiAttivi.clear();
		VistaGruppo gruppoAvversario = vistaPartita.getGruppoAvversario();
		int i = 0;
		for (VistaPersonaggio personaggioCorrente : gruppoAvversario.getPersonaggi()) {
			BufferedImage d = ClassePersonaggioImmagine.getImmagine(personaggioCorrente.getClasse());
			mappaImmagini.put(personaggioCorrente, d);
			CoordinateMD coordinate = new CoordinateMD(topLeftX + i++ * PASSO_ORIZZONTALE + 1 + caso.nextInt(10),
					ImageCache.SPACING + ImageCache.locazioni.get(TipoLocazione.BOSCO).getHeight() - i * PASSO_VERTICALE - d.getHeight());
			mappaCoordinate.put(personaggioCorrente, coordinate);
		}
		// Le mappe sono state svuotate: anche il gruppo si dispone da capo
		gruppoDisegnato = new ArrayList<>();
		aggiornaGruppo();
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
		List<Point> posizioni = disponiGruppo(topLeftX, locazione.getWidth(), locazione.getHeight(), immagini);
		for (int i = 0; i < personaggi.size(); i++) {
			mappaImmagini.put(personaggi.get(i), immagini.get(i));
			mappaCoordinate.put(personaggi.get(i), new CoordinateMD(posizioni.get(i).x, posizioni.get(i).y));
		}
		gruppoDisegnato = personaggi;
	}

	/**
	 * Dove disegnare il gruppo, a destra della locazione e come gli avversari a sinistra: il primo, il capo, sta più
	 * in basso e vicino al bordo, e ogni altro si sposta verso il centro e sale un poco, così il precedente lo copre.
	 *
	 * @param immagini le immagini dei personaggi, nell'ordine del gruppo
	 * @return la posizione dell'angolo in alto a sinistra di ognuna, nello stesso ordine
	 */
	static List<Point> disponiGruppo(int topLeftX, int larghezzaLocazione, int altezzaLocazione, List<BufferedImage> immagini) {
		List<Point> posizioni = new ArrayList<>();
		for (int i = 0; i < immagini.size(); i++) {
			BufferedImage immagine = immagini.get(i);
			posizioni.add(new Point(topLeftX + larghezzaLocazione - immagine.getWidth() - (i * PASSO_ORIZZONTALE_GRUPPO + 1),
					ImageCache.SPACING + altezzaLocazione - (i + 1) * PASSO_VERTICALE - immagine.getHeight()));
		}
		return posizioni;
	}

	void disegnaLocazione(Graphics2D graphics) {
		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		VistaGruppo gng = vistaPartita.getGruppoAvversario();
		TipoLocazione tipoLocazione = g.getTipoLocazioneCorrente();
		BufferedImage locazione = ImageCache.locazioni.get(tipoLocazione);
		int locXOffset = topLeftX;
		graphics.drawImage(locazione, locXOffset, topLeftY, null);

		// Lista invertita
		List<VistaPersonaggio> avversariDaDisegnare = new ArrayList<>();
		gng.getPersonaggiVivi().forEach(p -> avversariDaDisegnare.add(0, p));

		for (VistaPersonaggio personaggioCorrente : avversariDaDisegnare) {
			BufferedImage d = mappaImmagini.get(personaggioCorrente);
			CoordinateMD coordinate = mappaCoordinate.get(personaggioCorrente);
			graphics.drawImage(d, coordinate.getX(), coordinate.getY(), null);
		}

		// Il gruppo, dall'ultimo al primo: il capo sta più in basso e copre gli altri
		aggiornaGruppo();
		List<VistaPersonaggio> gruppoDaDisegnare = new ArrayList<>(gruppoDisegnato);
		for (int i = gruppoDaDisegnare.size() - 1; i >= 0; i--) {
			CoordinateMD coordinate = mappaCoordinate.get(gruppoDaDisegnare.get(i));
			BufferedImage d = mappaImmagini.get(gruppoDaDisegnare.get(i));
			if (coordinate != null && d != null) {
				graphics.drawImage(d, coordinate.getX(), coordinate.getY(), null);
			}
		}

		TipoOggetto tipoOggetto = g.getTipoOggettoInLocazione();
		if (tipoOggetto != null && tipoOggetto != TipoOggetto.ARTEFATTO) {
			BufferedImage d = ClassiOggettoImmagine.getImmagine(tipoOggetto);
			graphics.drawImage(d, xOggetto(locazione, d), yOggetto(locazione, d), null);
		}
	}
	
	SpriteInterface notificaMorte(VistaPersonaggio personaggio) {
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

	SpriteInterface variaSalute(VistaPersonaggio personaggio, int variazione) {
		return spriteATempo(personaggio, ImageCache.spriteCombattimento, variazione, "Salute modificata");
	}

	SpriteInterface variaMagia(VistaPersonaggio personaggio, int variazione) {
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
	static int primoPostoLibero(List<Point> occupati, int x, int y, int passo) {
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

	SpriteInterface raccogliOggetto() {
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
	 * L'oggetto che si trova in locazione sta al centro, in basso: i lati sono dei due gruppi
	 */
	private int xOggetto(BufferedImage locazione, BufferedImage oggetto) {
		return topLeftX + (locazione.getWidth() - oggetto.getWidth()) / 2;
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

	SpriteInterface aggiungiEffettoDiStato(VistaPersonaggio personaggio, TipoEffettoDiStato effettoDiStato, DoomdarkColorModel.Color colore) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}

		BufferedImage image = mappaImmagini.get(personaggio);
		int x = xEffetto(personaggio, coordinate, image);
		int yIniziale = coordinate.getY() + image.getHeight() / 3 - SOLLEVAMENTO_ANCORA;
		SpriteEffetto sprite = new SpriteEffetto(effettoDiStato.getDescrizione(), DoomdarkFontMedium.getInstance(),
				colore, x, yIniziale + calcolaOffsetVerticale(yIniziale));
		effettiAttivi.add(new EffettoAttivo(yIniziale, sprite));
		return sprite;
	}

	SpriteInterface aggiungiInterazione(VistaPersonaggio personaggio, TipoInterazioneConEffettiDiStato interazione) {
		CoordinateMD coordinate = mappaCoordinate.get(personaggio);
		if (coordinate == null) {
			return null;
		}
		BufferedImage image = mappaImmagini.get(personaggio);
		int x = xEffetto(personaggio, coordinate, image);
		int yIniziale = coordinate.getY() + image.getHeight() * 2 / 3 - SOLLEVAMENTO_ANCORA;
		SpriteEffetto sprite = new SpriteEffetto(interazione.getDescrizione(), DoomdarkFontMedium.getInstance(),
				DoomdarkColorModel.Color.GREEN, x, yIniziale + calcolaOffsetVerticale(yIniziale));
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
