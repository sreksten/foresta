package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.immagini.LetturaPng;

/**
 * Il logo che si vede all'apertura del gioco, nello stato LOGO_INIZIALE, uguale per ogni toolkit: il logo 3AM completo, di cui {@link AnimazioneDelLogo}
 * traccia una volta la scritta (da un'immagine senza ombra, con la scritta nello stesso punto), centrato sulla finestra, su fondo nero. Le scie svaniscono
 * mentre il logo compare, poi il logo resta da solo per due secondi e svanisce. Qui stanno le immagini e i parametri; il disegno e' di
 * <code>PannelloLogoIniziale</code> in Swing e di <code>LogoInizialeGdx</code> in libGDX. Le immagini si usano senza zoom.
 */
public final class LogoIniziale {

	/** Il logo completo, con la pergamena e gli uccelli: e' quello che si mostra */
	public static final String RISORSA_COMPLETO = "/com/threeamigos/foresta/img/3AM_logo_completo.png";
	/** Il logo con la sola scritta nera, esattamente nello stesso posto: e' quello da cui si traccia il contorno */
	public static final String RISORSA_SENZA_OMBRA = "/com/threeamigos/foresta/img/3AM_logo_senza_ombra.png";

	/** Quanto resta il logo da solo */
	public static final int PAUSA_MS = 2_000;
	/** In quanti fotogrammi svanisce (come la dissolvenza con cui compare) */
	public static final int FOTOGRAMMI_SCOMPARSA = 40;
	/** Il raggio della torcia: 33 pixel, il 66% dei 50 di prima */
	public static final int RAGGIO_TORCIA = 33;
	/** Il giro completo dura 6 secondi (il predefinito e' 4) */
	public static final int DURATA_GIRO_MS = 6000;

	private LogoIniziale() {
	}

	/**
	 * L'animazione del logo iniziale, letta dalle risorse: il disegno si mostra intero, ma i contorni si cercano sulla sola scritta.
	 */
	public static AnimazioneDelLogo animazione() {
		return animazione(LetturaPng.leggiRisorsa(RISORSA_SENZA_OMBRA), LetturaPng.leggiRisorsa(RISORSA_COMPLETO));
	}

	/**
	 * Come {@link #animazione()}, con le immagini già lette
	 */
	public static AnimazioneDelLogo animazione(ImmagineDiPixel scritta, ImmagineDiPixel completo) {
		return AnimazioneDelLogo.costruttore(scritta)
				.immagineDaMostrare(completo)
				.raggioTorcia(RAGGIO_TORCIA)
				.durataGiroMs(DURATA_GIRO_MS)
				.sfumaScie(true)
				.fotogrammiPausa(PAUSA_MS / AnimazioneDelLogo.INTERVALLO_FOTOGRAMMA_MS)
				.fotogrammiScomparsa(FOTOGRAMMI_SCOMPARSA)
				.costruisci();
	}
}
