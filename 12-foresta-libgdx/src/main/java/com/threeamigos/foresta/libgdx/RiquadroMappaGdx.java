package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.riquadri.RiquadroMappa;
import com.threeamigos.foresta.ui.sfx.GestoreNuvole;

/**
 * La minimappa della schermata di gioco in libGDX: la finestra di 7 per 7 caselle, le tessere conosciute, il segnalino e gli indicatori sono del modulo
 * comune ({@link RiquadroMappa}, gli stessi di Swing), qui c'è il disegno: la cornice, le tessere (scurite se il gruppo ci è già stato), il segnalino che
 * lampeggia e le nuvole condivise ({@link GestoreNuvole}), ritagliate al quadrato della mappa e al 45% di opacità. Le tessere si disegnano una a una, non
 * con un'immagine di tutta la foresta come in Swing.
 */
public final class RiquadroMappaGdx extends RiquadroMappa {

	private static final String CORNICE = "fondi/CorniceMappa.png";
	// Quanto si scurisce una tessera già visitata: il 50% di nero
	private static final float OSCURAMENTO_DELLE_VISITATE = 0.5f;
	private static final float OPACITA_DELLE_NUVOLE = 0.45f;

	private final ImmaginiGdx immagini;
	private final NuvoleGdx nuvole;
	private final float zoom = (float) LivelloDiZoom.valore();

	public RiquadroMappaGdx(int x, int y, VistaPartita vistaPartita, ImmaginiGdx immagini, NuvoleGdx nuvole) {
		super(x, y, immagini.larghezza(NomiDelleImmagini.tessera(TipoLocazione.BOSCO), (float) LivelloDiZoom.valore()),
				immagini.altezza(NomiDelleImmagini.tessera(TipoLocazione.BOSCO), (float) LivelloDiZoom.valore()), vistaPartita);
		this.immagini = immagini;
		this.nuvole = nuvole;
	}

	/**
	 * @param larghezzaDelloSchermo la larghezza dello schermo di gioco (le nuvole nascono sulla mappa intera, centrata nello schermo)
	 * @param altezzaDelloSchermo   la sua altezza
	 */
	public void disegna(Batch batch, Camera camera, Viewport viewport, int larghezzaDelloSchermo, int altezzaDelloSchermo) {
		immagini.disegna(batch, CORNICE, topLeftX, topLeftY, zoom);
		Veduta veduta = veduta();
		for (Tessera tessera : veduta.getTessere()) {
			String risorsa = tessera.getTipo() == TipoLocazione.BOSCO ? NomiDelleImmagini.tesseraDelBosco(tessera.getVariante())
					: NomiDelleImmagini.tessera(tessera.getTipo());
			immagini.disegna(batch, risorsa, tessera.getX(), tessera.getY(), zoom);
			if (tessera.isVisitata()) {
				immagini.riempi(batch, tessera.getX(), tessera.getY(), larghezzaIcona, altezzaIcona, new Color(0f, 0f, 0f, OSCURAMENTO_DELLE_VISITATE));
			}
		}
		if (veduta.isSegnaliniVisibili()) {
			Punto segnalino = veduta.getSegnalino();
			immagini.disegna(batch, NomiDelleImmagini.SEGNALINO, segnalino.x, segnalino.y, zoom);
			for (Punto indicatore : veduta.getIndicatori()) {
				immagini.disegna(batch, NomiDelleImmagini.INDICATORE, indicatore.x, indicatore.y, zoom);
			}
		}

		// Le nuvole, sul solo quadrato della mappa
		Area area = veduta.getAreaDelleNuvole();
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
		Color precedente = new Color(batch.getColor());
		batch.setColor(1f, 1f, 1f, OPACITA_DELLE_NUVOLE);
		GestoreNuvole.assicuraGenerate(vistaPartita.getMappa(), larghezzaDelloSchermo, altezzaDelloSchermo, larghezzaIcona, altezzaIcona);
		nuvole.disegna(batch, veduta.getDaX(), veduta.getDaY(), area.getX(), area.getY(), larghezzaIcona, altezzaIcona);
		batch.setColor(precedente);
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}
		GestoreNuvole.aggiorna();
	}
}
