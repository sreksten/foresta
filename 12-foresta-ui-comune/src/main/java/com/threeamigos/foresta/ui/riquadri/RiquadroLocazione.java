package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.Verso;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteEffetto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Il riquadro con la locazione corrente, senza disegno: dove sta ogni personaggio (gli avversari a sinistra, che guardano a destra; il gruppo del giocatore
 * a destra, che guarda a sinistra), con la disposizione di {@link DisposizioneNellaLocazione}, e dove stanno lo sfondo e l'oggetto in locazione. Lo sfondo e
 * l'oggetto stanno al centro dello spazio a disposizione, gli avversari a sinistra dell'oggetto e il gruppo a destra, via via verso i bordi. Chi disegna
 * (Swing, libGDX) dice quanto è grande l'immagine di ogni classe (già con lo zoom) e disegna dove la disposizione dice.
 */
public abstract class RiquadroLocazione implements Finestra {

	/**
	 * Di quanto, al più, un avversario si scosta a caso verso il bordo, perché la fila non sembri tirata col righello
	 */
	private static final int SCOSTAMENTO_CASUALE = 10;
	// L'oggetto sta 5 pixel sopra il fondo dello sfondo
	private static final int SOLLEVAMENTO_DELL_OGGETTO = 5;

	protected final int topLeftX;
	protected final int topLeftY;
	// La larghezza dello spazio a disposizione: lo sfondo e l'oggetto stanno al centro
	protected final int larghezzaDisponibile;
	protected final VistaPartita vistaPartita;

	private static final int SOLLEVAMENTO_ANCORA = 8;
	private static final int SCOSTAMENTO_SOVRAPPOSIZIONE = 20;
	/**
	 * Di quanto scende uno sprite a tempo rispetto a quello sopra, in quarti dell'altezza della sua icona: l'icona ha
	 * dei bordi trasparenti, e uno spazio pari a tutta la sua altezza era troppo
	 */
	private static final int QUARTI_DI_ICONA_FRA_SPRITE_A_TEMPO = 3;

	/**
	 * Uno sprite che ha preso un posto sopra un personaggio: finché dura, il successivo parte più in basso (o più in alto) per non sovrapporsi
	 */
	private static final class SpriteInVita {
		final int x;
		final int y;
		final long fineInNanosecondi;

		SpriteInVita(int x, int y, float durataInSecondi) {
			this.x = x;
			this.y = y;
			this.fineInNanosecondi = System.nanoTime() + (long) (durataInSecondi * 1_000_000_000L);
		}

		boolean isInVita() {
			return System.nanoTime() < fineInNanosecondi;
		}
	}

	private final List<SpriteInVita> spriteATempoInVita = new CopyOnWriteArrayList<>();
	private final List<SpriteInVita> effettiInVita = new ArrayList<>();

	// Per sparpagliare un po' gli avversari: non Dado, che segue il seme della partita e non va consumato dalla UI
	private final Random caso = new Random();
	// Avversari e gruppo del giocatore: lette da chi disegna e dal thread che notifica gli eventi
	private final Map<VistaPersonaggio, Punto> posizioni = new ConcurrentHashMap<>();
	// I personaggi del gruppo che si disegnano adesso (i vivi, e gli ospiti vivi), nell'ordine: se cambiano, si ridispongono
	private List<VistaPersonaggio> gruppoDisegnato = new ArrayList<>();

	protected RiquadroLocazione(int topLeftX, int topLeftY, int larghezzaDisponibile, VistaPartita vistaPartita) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.larghezzaDisponibile = larghezzaDisponibile;
		this.vistaPartita = vistaPartita;
	}

	/**
	 * Quanto è grande l'immagine di quella classe di personaggio, già con lo zoom (specchiata ha la stessa misura)
	 */
	protected abstract Misura misuraDelPersonaggio(TipoPersonaggio classe);

	/**
	 * Quanto è grande l'icona di uno sprite (<code>icone/Combattimento.png</code>…), come la tiene chi disegna, a dimensione originale
	 */
	protected abstract Misura misuraDellIcona(String risorsa);

	/**
	 * Quanto è grande l'immagine di un oggetto che si trova in locazione, già con lo zoom
	 */
	protected abstract Misura misuraDelBottino(TipoBottino tipo);

	/**
	 * L'altezza dello sfondo di una locazione (sono tutti uguali), già con lo zoom
	 */
	protected abstract int altezzaDelloSfondo();

	/**
	 * La larghezza dello spazio libero al centro, fra i due gruppi: quella di un cofano, qualunque oggetto ci sia (l'oggetto copre i personaggi, quindi uno
	 * più largo non li sposta)
	 */
	protected abstract int larghezzaDelCentro();

	/**
	 * Le disposizioni sono state azzerate (cambia lo scontro): chi tiene dati per personaggio li butta
	 */
	protected void disposizioneAzzerata() {
	}

	/**
	 * Un personaggio ha il suo posto, rivolto a quel verso (gli avversari guardano a destra, il gruppo a sinistra): chi disegna prepara la sua immagine
	 */
	protected void personaggioDisposto(VistaPersonaggio personaggio, Verso verso) {
	}

	/**
	 * Un personaggio non è più disposto (è caduto, se n'è andato)
	 */
	protected void personaggioRimosso(VistaPersonaggio personaggio) {
	}

	/**
	 * Dove sta il personaggio (angolo in alto a sinistra della sua immagine), o null se non è in locazione
	 */
	protected Punto getPosizione(VistaPersonaggio personaggio) {
		return posizioni.get(personaggio);
	}

	/**
	 * I personaggi del gruppo disposti adesso, nell'ordine in cui si disegnano
	 */
	protected List<VistaPersonaggio> getGruppoDisposto() {
		return new ArrayList<>(gruppoDisegnato);
	}

	/**
	 * Dispone gli avversari nel riquadro (e, da capo, il gruppo).
	 */
	public void assegnaCoordinateAgliAvversari() {
		posizioni.clear();
		disposizioneAzzerata();
		VistaGruppo gruppoAvversario = vistaPartita.getGruppoAvversario();
		List<VistaPersonaggio> avversari = new ArrayList<>(gruppoAvversario.getPersonaggi());
		List<Punto> dove = DisposizioneNellaLocazione.disponiAvversari(topLeftX, larghezzaDisponibile, larghezzaDelCentro(), altezzaDelloSfondo(),
				misure(avversari));
		for (int i = 0; i < avversari.size(); i++) {
			personaggioDisposto(avversari.get(i), Verso.DESTRA);
			// Sparpagliati un poco verso il bordo, senza uscire dal riquadro
			posizioni.put(avversari.get(i), new Punto(Math.max(topLeftX, dove.get(i).x - caso.nextInt(SCOSTAMENTO_CASUALE)), dove.get(i).y));
		}
		// Le disposizioni sono state svuotate: anche il gruppo si dispone da capo
		gruppoDisegnato = new ArrayList<>();
		aggiornaGruppo();
	}

	private List<Misura> misure(List<VistaPersonaggio> personaggi) {
		List<Misura> misure = new ArrayList<>();
		for (VistaPersonaggio personaggio : personaggi) {
			misure.add(misuraDelPersonaggio(personaggio.getClasse()));
		}
		return misure;
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
	 * Se chi è nel gruppo e vivo è cambiato (qualcuno è morto, è risorto, è arrivato o se n'è andato) dispone da capo i personaggi: gli altri scalano per
	 * riempire i posti liberi. Si chiama a ogni disegno, ed è subito dopo che i personaggi cambiano che gli sprite (per esempio la dissolvenza di chi è
	 * appena morto) trovano ancora le vecchie posizioni.
	 */
	protected void aggiornaGruppo() {
		List<VistaPersonaggio> personaggi = personaggiDelGruppoDaDisegnare();
		if (personaggi.equals(gruppoDisegnato)) {
			return;
		}
		for (VistaPersonaggio vecchio : gruppoDisegnato) {
			posizioni.remove(vecchio);
			personaggioRimosso(vecchio);
		}
		List<Punto> dove = DisposizioneNellaLocazione.disponiGruppo(topLeftX, larghezzaDisponibile, larghezzaDelCentro(), altezzaDelloSfondo(),
				misure(personaggi));
		for (int i = 0; i < personaggi.size(); i++) {
			personaggioDisposto(personaggi.get(i), Verso.SINISTRA);
			posizioni.put(personaggi.get(i), dove.get(i));
		}
		gruppoDisegnato = personaggi;
	}

	/**
	 * La x dello sfondo (largo <code>larghezzaSfondo</code>): al centro dello spazio a disposizione
	 */
	protected int xDelloSfondo(int larghezzaSfondo) {
		return topLeftX + ((larghezzaDisponibile - larghezzaSfondo) >> 1);
	}

	/**
	 * L'oggetto che si trova in locazione sta al centro dello spazio a disposizione, in basso: i lati sono dei due gruppi
	 */
	protected int xDelloOggetto(int larghezzaOggetto) {
		return topLeftX + (larghezzaDisponibile - larghezzaOggetto) / 2;
	}

	protected int yDelloOggetto(int altezzaSfondo, int altezzaOggetto) {
		return Finestra.SPACING + altezzaSfondo - altezzaOggetto - SOLLEVAMENTO_DELL_OGGETTO;
	}

	/**
	 * Lo sprite che mostra la variazione di salute di un personaggio (nessuno sprite, cioè null, per chi non è in locazione o se non è cambiato niente)
	 */
	public DescrizioneSpriteATempo spriteDellaSalute(VistaPersonaggio personaggio, int variazione) {
		return spriteATempo(personaggio, NomiDelleImmagini.SPRITE_COMBATTIMENTO, variazione, "Salute modificata");
	}

	/**
	 * Lo sprite che mostra la variazione di magia di un personaggio
	 */
	public DescrizioneSpriteATempo spriteDellaMagia(VistaPersonaggio personaggio, int variazione) {
		return spriteATempo(personaggio, NomiDelleImmagini.SPRITE_MAGIA, variazione, "Magia modificata");
	}

	/**
	 * Lo sprite a tempo di una variazione sopra il personaggio, centrato sulla sua immagine (spostato solo se uscirebbe dal riquadro); se ce n'è già uno
	 * vicino ancora in vita parte più in basso, così non si sovrappongono.
	 */
	private DescrizioneSpriteATempo spriteATempo(VistaPersonaggio personaggio, String icona, int variazione, String descrizione) {
		Punto coordinate = getPosizione(personaggio);
		if (coordinate == null || variazione == 0) {
			return null;
		}
		spriteATempoInVita.removeIf(sprite -> !sprite.isInVita());
		List<Punto> occupati = new ArrayList<>();
		for (SpriteInVita sprite : spriteATempoInVita) {
			occupati.add(new Punto(sprite.x, sprite.y));
		}
		Misura misuraIcona = misuraDellIcona(icona);
		int xCentro = coordinate.x + misuraDelPersonaggio(personaggio.getClasse()).larghezza / 2;
		int y = DisposizioneNellaLocazione.primoPostoLibero(occupati, xCentro, coordinate.y, misuraIcona.altezza * QUARTI_DI_ICONA_FRA_SPRITE_A_TEMPO / 4);
		spriteATempoInVita.add(new SpriteInVita(xCentro, y, DescrizioneSpriteATempo.DURATA_IN_SECONDI));
		return DescrizioneSpriteATempo.conVariazione(icona, misuraIcona, variazione, DoomdarkFont.medio(), xCentro, y, true, descrizione)
				.centraEntro(xCentro, topLeftX, topLeftX + larghezzaDisponibile);
	}

	/**
	 * Lo sprite che mostra il gruppo che raccoglie un oggetto: l'oggetto, al suo posto in locazione, che poi sale (null se non c'è un oggetto da mostrare)
	 */
	public DescrizioneSpriteATempo spriteDelloOggettoRaccolto() {
		TipoBottino tipoOggetto = vistaPartita.getTipoBottinoInLocazione();
		if (tipoOggetto == null || tipoOggetto == TipoBottino.ARTEFATTO) {
			return null;
		}
		Misura misura = misuraDelBottino(tipoOggetto);
		return DescrizioneSpriteATempo.immagine(NomiDelleImmagini.oggetto(tipoOggetto), misura, xDelloOggetto(misura.larghezza),
				yDelloOggetto(altezzaDelloSfondo(), misura.altezza), "Raccolto oggetto " + tipoOggetto)
				.conScalaDellIcona((float) LivelloDiZoom.valore());
	}

	/**
	 * Il nome dell'effetto di stato (o di un modificatore) subito dal personaggio, sopra di lui
	 */
	public DescrizioneSpriteEffetto spriteDellEffettoDiStato(VistaPersonaggio personaggio, String descrizione, DoomdarkColorModel.Color colore) {
		return scrittaSulPersonaggio(personaggio, descrizione, DoomdarkFont.medio(), colore, null, true);
	}

	/**
	 * Un testo di un colore del font sul personaggio (un'interazione, "Mancato"…)
	 */
	public DescrizioneSpriteEffetto spriteDelTestoSulPersonaggio(VistaPersonaggio personaggio, String testo, DoomdarkFont font, DoomdarkColorModel.Color colore) {
		return scrittaSulPersonaggio(personaggio, testo, font, colore, null, false);
	}

	/**
	 * Un testo di un colore qualunque (ARGB) sul personaggio
	 */
	public DescrizioneSpriteEffetto spriteDelTestoColoratoSulPersonaggio(VistaPersonaggio personaggio, String testo, int coloreArgb) {
		return scrittaSulPersonaggio(personaggio, testo, DoomdarkFont.medio(), null, coloreArgb, false);
	}

	/**
	 * @param daEffettoDiStato vero per l'effetto di stato (che sta a un terzo dell'altezza del personaggio), falso per gli altri testi (a due terzi)
	 */
	private DescrizioneSpriteEffetto scrittaSulPersonaggio(VistaPersonaggio personaggio, String testo, DoomdarkFont font, DoomdarkColorModel.Color colore,
														   Integer coloreArgb, boolean daEffettoDiStato) {
		Punto coordinate = getPosizione(personaggio);
		if (coordinate == null) {
			return null;
		}
		Misura immagine = misuraDelPersonaggio(personaggio.getClasse());
		int yIniziale = daEffettoDiStato ? coordinate.y + immagine.altezza / 3 - SOLLEVAMENTO_ANCORA : coordinate.y + immagine.altezza * 2 / 3 - SOLLEVAMENTO_ANCORA;
		int y = yIniziale + scostamentoVerticaleDegliEffetti(yIniziale);
		effettiInVita.add(new SpriteInVita(coordinate.x, yIniziale, DescrizioneSpriteEffetto.DURATA_IN_SECONDI));
		DescrizioneSpriteEffetto sprite = colore != null
				? DescrizioneSpriteEffetto.colorato(testo, font, colore, coordinate.x, y)
				: DescrizioneSpriteEffetto.conColoreRgb(testo, font, coloreArgb, coordinate.x, y);
		return sprite.centraEntro(coordinate.x + immagine.larghezza / 2.0f, topLeftX, topLeftX + larghezzaDisponibile);
	}

	/**
	 * Di quanto sposta in verticale un effetto che parte a quella quota, se altri ancora in vita sono vicini: alternativamente sotto e sopra, a passi
	 */
	private int scostamentoVerticaleDegliEffetti(int yIniziale) {
		effettiInVita.removeIf(e -> !e.isInVita());
		long occupati = effettiInVita.stream().filter(e -> Math.abs(e.y - yIniziale) < SCOSTAMENTO_SOVRAPPOSIZIONE).count();
		if (occupati == 0) {
			return 0;
		}
		int passo = (int) ((occupati + 1) / 2);
		int segno = (occupati % 2 == 1) ? 1 : -1;
		return segno * passo * SCOSTAMENTO_SOVRAPPOSIZIONE;
	}
}
