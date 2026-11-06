package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.barra.BarraIcone;
import com.threeamigos.foresta.ui.immagini.AtlanteDiImmagini;
import com.threeamigos.foresta.ui.immagini.ComposizioneDelleIcone;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.immagini.SfondoARiempimento;

import java.util.HashMap;
import java.util.Map;

/**
 * Le immagini delle icone della barra dei comandi in libGDX: la tabella ({@link TipoIcona}) e la composizione (sfondo, ombra, disegno:
 * {@link ComposizioneDelleIcone}) sono del modulo comune, qui si compongono i pixel degli atlanti e se ne fa una texture. Si compongono al primo uso. Sono a
 * metà risoluzione: chi disegna le ingrandisce dello zoom.
 */
public final class IconeGdx {

	private final ImmaginiGdx immagini;
	private final Map<String, TextureRegion> cache = new HashMap<>();

	public IconeGdx(ImmaginiGdx immagini) {
		this.immagini = immagini;
	}

	/**
	 * La larghezza (e l'altezza) di un'icona a metà risoluzione, quella dei suoi sfondi
	 */
	public int larghezza() {
		return immagini.larghezza(NomiDelleImmagini.SFONDO_ICONA_TRASPARENTE, 1f);
	}

	public int altezza() {
		return immagini.altezza(NomiDelleImmagini.SFONDO_ICONA_TRASPARENTE, 1f);
	}

	/**
	 * L'immagine di un'icona della barra: normale, del capo (sfondo rosso) o in ricarica
	 */
	public TextureRegion regione(BarraIcone.IconaVisibile icona) {
		TipoIcona tipo = icona.getTipo();
		if (icona.isInRicarica()) {
			return daPixel("ricarica|" + tipo + "|" + icona.getTurniRimasti() + "|" + icona.getDurata(), () -> {
				int rimasti = Math.min(icona.getTurniRimasti(), icona.getDurata());
				ImmagineDiPixel sfondo = SfondoARiempimento.crea(pixel(NomiDelleImmagini.SFONDO_ICONA_TRASPARENTE), icona.getDurata() - rimasti + 1,
						icona.getDurata());
				return ComposizioneDelleIcone.componi(sfondo, pixel(tipo.getNomeRisorsa()));
			});
		}
		TipoIcona.SfondoIcona sfondo = icona.isCapo() ? TipoIcona.SfondoIcona.ROSSO : tipo.getSfondo();
		return daPixel("icona|" + tipo + "|" + sfondo, () -> ComposizioneDelleIcone.componi(pixel(sfondo.getRisorsa()), pixel(tipo.getNomeRisorsa())));
	}

	private static ImmagineDiPixel pixel(String risorsa) {
		ImmagineDiPixel immagine = AtlanteDiImmagini.cerca(risorsa);
		if (immagine == null) {
			throw new IllegalArgumentException("Immagine non trovata negli atlanti: " + risorsa);
		}
		return immagine;
	}

	private TextureRegion daPixel(String chiave, java.util.function.Supplier<ImmagineDiPixel> costruttore) {
		TextureRegion regione = cache.get(chiave);
		if (regione == null) {
			regione = immagini.regioneDaPixel(costruttore.get(), false);
			cache.put(chiave, regione);
		}
		return regione;
	}
}
