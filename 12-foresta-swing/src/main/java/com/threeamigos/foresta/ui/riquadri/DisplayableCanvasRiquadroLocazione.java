package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.Verso;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.tipi.MappaBottinoImmagine;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteEffetto;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;
import com.threeamigos.foresta.ui.sprite.SpriteEffetto;
import com.threeamigos.foresta.ui.sprite.SpriteInDissolvenza;
import com.threeamigos.foresta.ui.sprite.SpriteInterface;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Il riquadro con la locazione corrente: sfondo, avversari e personaggi che incontrano il gruppo; ospita gli sprite che mostrano cosa accade (danni, effetti, oggetti raccolti).
 */
public class DisplayableCanvasRiquadroLocazione extends RiquadroLocazione {

	// Per sparpagliare un po' gli avversari: non Dado, che segue il seme della partita e non va consumato dalla UI

	// Avversari e gruppo del giocatore: lette dall'EDT che disegna e dal thread che notifica gli eventi
	private final Map<VistaPersonaggio, BufferedImage> mappaImmagini = new ConcurrentHashMap<>();

	public DisplayableCanvasRiquadroLocazione(int topLeftX, int topLeftY, int larghezzaDisponibile, VistaPartita vistaPartita) {
		super(topLeftX, topLeftY, larghezzaDisponibile, vistaPartita);
	}

	@Override
	protected Misura misuraDelPersonaggio(TipoPersonaggio classe) {
		BufferedImage immagine = MappaPersonaggioImmagine.getImmagine(classe);
		return new Misura(immagine.getWidth(), immagine.getHeight());
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		BufferedImage icona = BufferedImageBuilder.buildBufferedImage(risorsa);
		return new Misura(icona.getWidth(), icona.getHeight());
	}

	@Override
	protected Misura misuraDelBottino(TipoBottino tipo) {
		BufferedImage immagine = MappaBottinoImmagine.getImmagine(tipo);
		return new Misura(immagine.getWidth(), immagine.getHeight());
	}

	@Override
	protected int altezzaDelloSfondo() {
		return ImageCache.locazioni.get(TipoLocazione.BOSCO).getHeight();
	}

	@Override
	protected int larghezzaDelCentro() {
		return MappaBottinoImmagine.getImmagine(TipoBottino.COFANO).getWidth();
	}

	@Override
	protected void disposizioneAzzerata() {
		mappaImmagini.clear();
	}

	@Override
	protected void personaggioDisposto(VistaPersonaggio personaggio, Verso verso) {
		mappaImmagini.put(personaggio, MappaPersonaggioImmagine.getImmagine(personaggio.getClasse(), verso));
	}

	@Override
	protected void personaggioRimosso(VistaPersonaggio personaggio) {
		mappaImmagini.remove(personaggio);
	}

	/**
	 * @param agente chi sta agendo (null se non c'è): gli appare una freccia sopra la testa
	 * @param bersaglio il suo bersaglio, se c'è: anche a lui
	 */
	public void disegnaLocazione(Graphics2D graphics, VistaPersonaggio agente, VistaPersonaggio bersaglio) {
		VistaGruppo gng = vistaPartita.getGruppoAvversario();
		TipoLocazione tipoLocazione = vistaPartita.getMappa().getLocazioneDelGruppo();
		BufferedImage locazione = ImageCache.locazioni.get(tipoLocazione);
		// Lo sfondo sta al centro dello spazio a disposizione
		graphics.drawImage(locazione, xDelloSfondo(locazione.getWidth()), topLeftY, null);

		// Dal primo all'ultimo: chi sta più in basso copre chi sta più in alto
		for (VistaPersonaggio personaggioCorrente : gng.getPersonaggiVivi()) {
			BufferedImage d = mappaImmagini.get(personaggioCorrente);
			Punto coordinate = getPosizione(personaggioCorrente);
			if (coordinate != null && d != null) {
				disegnaPersonaggio(graphics, personaggioCorrente, d, coordinate.x, coordinate.y, agente, bersaglio);
			}
		}

		// Il gruppo, dal primo all'ultimo, come gli avversari
		aggiornaGruppo();
		List<VistaPersonaggio> gruppoDaDisegnare = getGruppoDisposto();
		for (int i = 0; i < gruppoDaDisegnare.size(); i++) {
			Punto coordinate = getPosizione(gruppoDaDisegnare.get(i));
			BufferedImage d = mappaImmagini.get(gruppoDaDisegnare.get(i));
			if (coordinate != null && d != null) {
				disegnaPersonaggio(graphics, gruppoDaDisegnare.get(i), d, coordinate.x, coordinate.y, agente, bersaglio);
			}
		}

		// L'oggetto si disegna dopo i personaggi e li copre: una lancia, che è lunga, non li spinge verso i bordi. Gli sprite
		// vengono dopo, sopra tutto (DisplayableCanvas.inGioco)
		TipoBottino tipoOggetto = vistaPartita.getTipoBottinoInLocazione();
		if (tipoOggetto != null && tipoOggetto != TipoBottino.ARTEFATTO) {
			BufferedImage d = MappaBottinoImmagine.getImmagine(tipoOggetto);
			graphics.drawImage(d, xOggetto(d), yOggetto(locazione, d), null);
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
		Punto coordinate = getPosizione(personaggio);
		if (coordinate == null) {
			return null;
		}
		return new SpriteInDissolvenza(personaggio.getNome(), mappaImmagini.get(personaggio),
				coordinate.x, coordinate.y);
	}

	/**
	 * Crea lo sprite che mostra la variazione di salute del personaggio.
	 */
	public SpriteInterface variaSalute(VistaPersonaggio personaggio, int variazione) {
		return sprite(spriteDellaSalute(personaggio, variazione));
	}

	/**
	 * Crea lo sprite che mostra la variazione di magia del personaggio.
	 */
	public SpriteInterface variaMagia(VistaPersonaggio personaggio, int variazione) {
		return sprite(spriteDellaMagia(personaggio, variazione));
	}

	/**
	 * Crea lo sprite che mostra il gruppo che raccoglie un oggetto.
	 */
	public SpriteInterface raccogliOggetto() {
		return sprite(spriteDelloOggettoRaccolto());
	}

	/**
	 * Crea lo sprite con il nome dell'effetto di stato subito dal personaggio.
	 */
	public SpriteInterface aggiungiEffettoDiStato(VistaPersonaggio personaggio, TipoEffettoDiStato effettoDiStato, DoomdarkColorModel.Color colore) {
		return sprite(spriteDellEffettoDiStato(personaggio, effettoDiStato.getDescrizione(), colore));
	}

	/**
	 * Come l'effetto di stato, ma con la descrizione di un modificatore di attributo ("Furtività +5")
	 */
	public SpriteInterface aggiungiModificatore(VistaPersonaggio personaggio, String descrizione, DoomdarkColorModel.Color colore) {
		return sprite(spriteDellEffettoDiStato(personaggio, descrizione, colore));
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
		return sprite(spriteDelTestoSulPersonaggio(personaggio, testo, DoomdarkFont.medio(), colore));
	}

	/**
	 * Crea lo sprite "Mancato", in grigio medio e in piccolo, sul personaggio che il colpo non ha raggiunto.
	 */
	public SpriteInterface aggiungiColpoMancato(VistaPersonaggio personaggio) {
		return sprite(spriteDelTestoSulPersonaggio(personaggio, TestiDellaUI.get("COLPO_MANCATO"), DoomdarkFont.piccolo(),
				DoomdarkColorModel.Color.MEDIUM_GRAY));
	}

	/**
	 * Come sopra, con un colore costruito al volo (non uno dei colori fissi di DoomdarkColorModel)
	 */
	public SpriteInterface aggiungiTestoSulPersonaggio(VistaPersonaggio personaggio, String testo, java.awt.Color colore) {
		return sprite(spriteDelTestoColoratoSulPersonaggio(personaggio, testo, colore.getRGB()));
	}

	/**
	 * L'oggetto che si trova in locazione sta al centro dello spazio a disposizione, in basso: i lati sono dei due gruppi
	 */
	private int xOggetto(BufferedImage oggetto) {
		return xDelloOggetto(oggetto.getWidth());
	}

	private int yOggetto(BufferedImage locazione, BufferedImage oggetto) {
		return yDelloOggetto(locazione.getHeight(), oggetto.getHeight());
	}

	private static SpriteInterface sprite(DescrizioneSpriteATempo descrizione) {
		return descrizione == null ? null : new SpriteATempo(descrizione);
	}

	private static SpriteInterface sprite(DescrizioneSpriteEffetto descrizione) {
		return descrizione == null ? null : new SpriteEffetto(descrizione);
	}
}
