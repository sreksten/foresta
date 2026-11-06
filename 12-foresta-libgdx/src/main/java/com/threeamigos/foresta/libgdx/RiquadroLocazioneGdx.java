package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.Verso;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.immagini.VersiDeiPersonaggi;
import com.threeamigos.foresta.ui.riquadri.RiquadroLocazione;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Il riquadro con la locazione corrente in libGDX: dove sta ogni personaggio, lo sfondo e l'oggetto sono del modulo comune ({@link RiquadroLocazione} e
 * {@code DisposizioneNellaLocazione}, gli stessi di Swing), qui c'è solo il disegno: lo sfondo, gli avversari e il gruppo (specchiati se l'immagine guarda
 * dalla parte sbagliata), l'oggetto, che li copre, e la freccia sopra chi agisce e il suo bersaglio. Gli sprite (danni, effetti, oggetti raccolti)
 * li descrive il modello comune ({@link RiquadroLocazione}) e li disegna {@link SpriteATempoGdx} e {@link SpriteEffettoGdx}. Le immagini dei file sono a metà risoluzione: si disegnano con lo zoom.
 */
public final class RiquadroLocazioneGdx extends RiquadroLocazione {

	private final ImmaginiGdx immagini;
	private final float zoom = (float) LivelloDiZoom.valore();
	// Le immagini dei personaggi rivolte da una certa parte (la specchiata si costruisce una volta)
	private final Map<String, TextureRegion> rivolte = new HashMap<>();

	public RiquadroLocazioneGdx(int x, int y, int larghezzaDisponibile, VistaPartita vistaPartita, ImmaginiGdx immagini) {
		super(x, y, larghezzaDisponibile, vistaPartita);
		this.immagini = immagini;
	}

	@Override
	protected Misura misuraDelPersonaggio(TipoPersonaggio classe) {
		TextureRegion regione = immagini.regione(NomiDelleImmagini.personaggio(classe));
		return new Misura(Math.round(regione.getRegionWidth() * zoom), Math.round(regione.getRegionHeight() * zoom));
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		// Le icone non hanno lo zoom
		return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
	}

	@Override
	protected Misura misuraDelBottino(TipoBottino tipo) {
		TextureRegion regione = immagini.regione(NomiDelleImmagini.oggetto(tipo));
		return new Misura(Math.round(regione.getRegionWidth() * zoom), Math.round(regione.getRegionHeight() * zoom));
	}

	@Override
	protected int altezzaDelloSfondo() {
		return Math.round(immagini.regione(NomiDelleImmagini.locazione(TipoLocazione.BOSCO)).getRegionHeight() * zoom);
	}

	@Override
	protected int larghezzaDelCentro() {
		return Math.round(immagini.regione(NomiDelleImmagini.oggetto(TipoBottino.COFANO)).getRegionWidth() * zoom);
	}

	/**
	 * L'immagine della classe rivolta verso il verso voluto: quella originale se già guarda da quella parte, altrimenti la sua specchiata
	 */
	private TextureRegion rivolta(TipoPersonaggio classe, Verso verso) {
		String nome = NomiDelleImmagini.personaggio(classe);
		if (!VersiDeiPersonaggi.serveSpecchiare(classe, verso)) {
			return immagini.regione(nome);
		}
		return rivolte.computeIfAbsent(nome + "|" + verso, chiave -> {
			TextureRegion specchiata = new TextureRegion(immagini.regione(nome));
			specchiata.flip(true, false);
			return specchiata;
		});
	}

	private void disegnaPersonaggio(Batch batch, VistaPersonaggio personaggio, Verso verso, VistaPersonaggio agente, VistaPersonaggio bersaglio) {
		Punto posizione = getPosizione(personaggio);
		if (posizione == null) {
			return;
		}
		TextureRegion regione = rivolta(personaggio.getClasse(), verso);
		float larghezza = regione.getRegionWidth() * zoom;
		batch.draw(regione, posizione.x, posizione.y, larghezza, regione.getRegionHeight() * zoom);
		if (personaggio.equals(agente) || personaggio.equals(bersaglio)) {
			TextureRegion freccia = immagini.regione("icone/Giu.png");
			batch.draw(freccia, posizione.x + ((int) larghezza - freccia.getRegionWidth() >> 1), posizione.y - freccia.getRegionHeight());
		}
	}

	/**
	 * @param agente    chi sta agendo (null se non c'è): gli appare una freccia sopra la testa
	 * @param bersaglio il suo bersaglio, se c'è: anche a lui
	 */
	public void disegna(Batch batch, VistaPersonaggio agente, VistaPersonaggio bersaglio) {
		TipoLocazione tipo = vistaPartita.getMappa().getLocazioneDelGruppo();
		TextureRegion sfondo = immagini.regione(NomiDelleImmagini.locazione(tipo));
		float larghezzaSfondo = sfondo.getRegionWidth() * zoom;
		// Lo sfondo sta al centro dello spazio a disposizione
		batch.draw(sfondo, xDelloSfondo((int) larghezzaSfondo), topLeftY, larghezzaSfondo, sfondo.getRegionHeight() * zoom);

		// Dal primo all'ultimo: chi sta più in basso copre chi sta più in alto
		for (VistaPersonaggio avversario : vistaPartita.getGruppoAvversario().getPersonaggiVivi()) {
			disegnaPersonaggio(batch, avversario, Verso.DESTRA, agente, bersaglio);
		}
		aggiornaGruppo();
		List<VistaPersonaggio> gruppo = getGruppoDisposto();
		for (VistaPersonaggio personaggio : gruppo) {
			disegnaPersonaggio(batch, personaggio, Verso.SINISTRA, agente, bersaglio);
		}

		// L'oggetto si disegna dopo i personaggi e li copre: una lancia, che è lunga, non li spinge verso i bordi
		TipoBottino tipoOggetto = vistaPartita.getTipoBottinoInLocazione();
		if (tipoOggetto != null && tipoOggetto != TipoBottino.ARTEFATTO) {
			TextureRegion oggetto = immagini.regione(NomiDelleImmagini.oggetto(tipoOggetto));
			float larghezza = oggetto.getRegionWidth() * zoom;
			float altezza = oggetto.getRegionHeight() * zoom;
			batch.draw(oggetto, xDelloOggetto((int) larghezza), yDelloOggetto((int) (sfondo.getRegionHeight() * zoom), (int) altezza), larghezza, altezza);
		}
	}
}
