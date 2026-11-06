package com.threeamigos.foresta.ui.immagini;

import com.threeamigos.foresta.ui.DisposizioneDelloSchermo;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.tipi.ComandiAssociati;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.ui.LivelloDiZoom;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Le immagini delle icone della barra dei comandi per Swing: la tabella ({@link TipoIcona}) e la composizione (sfondo, ombra, disegno:
 * {@link ComposizioneDelleIcone}) sono del modulo comune, qui si caricano i file, si compongono e si ingrandiscono dello zoom. Si compongono al primo uso, o
 * da {@link #precarica()}: così la dimensione della finestra si calcola senza caricarle tutte.
 */
public final class IconeAwt {

	private static final Map<TipoIcona, BufferedImage> ICONE = new ConcurrentHashMap<>();
	private static final Map<TipoIcona, BufferedImage> ICONE_CAPO = new ConcurrentHashMap<>();
	// Le icone in ricarica, per icona, turni rimasti e durata (vedi iconaInRicarica): poche, e si compongono la prima volta che servono, o da precarica()
	private static final Map<TipoIcona, Map<Long, BufferedImage>> ICONE_IN_RICARICA = new EnumMap<>(TipoIcona.class);
	private static final Map<TipoIcona.SfondoIcona, ImmagineDiPixel> SFONDI = new ConcurrentHashMap<>();
	private static volatile ImmagineDiPixel corniceInRicarica;
	private static int altezzaMassima = -1;

	private IconeAwt() {
	}

	/**
	 * L'icona come si disegna nel pannello, già con lo zoom
	 */
	public static BufferedImage icona(TipoIcona tipo) {
		return ICONE.computeIfAbsent(tipo, t -> compone(sfondo(t.getSfondo()), t));
	}

	/**
	 * L'icona del personaggio che è il capo del gruppo: la stessa, con lo sfondo rosso.
	 */
	public static BufferedImage iconaCapo(TipoIcona tipo) {
		return ICONE_CAPO.computeIfAbsent(tipo, t -> compone(sfondo(TipoIcona.SfondoIcona.ROSSO), t));
	}

	/**
	 * L'icona di un comando in ricarica: lo sfondo, che si riempie dal basso con un colore che va dal rosso scuro al verde scuro man mano che passano i
	 * turni, e sopra il disegno. Non si può cliccare. Per una ricarica di {@code durata} turni, con {@code turniRimasti} ancora da aspettare, lo sfondo è
	 * pieno per (durata - turniRimasti + 1)/(durata + 1): subito dopo l'uso è un po' riempito, e a zero turni torna l'icona normale (vedi {@link #icona}).
	 */
	public static BufferedImage iconaInRicarica(TipoIcona tipo, int turniRimasti, int durata) {
		if (turniRimasti <= 0 || durata <= 0) {
			return icona(tipo);
		}
		int rimasti = Math.min(turniRimasti, durata);
		Map<Long, BufferedImage> mappa;
		synchronized (ICONE_IN_RICARICA) {
			mappa = ICONE_IN_RICARICA.computeIfAbsent(tipo, t -> new ConcurrentHashMap<>());
		}
		return mappa.computeIfAbsent((long) durata << 32 | rimasti,
				chiave -> compone(SfondoARiempimento.crea(corniceInRicarica(), durata - rimasti + 1, durata), tipo));
	}

	private static BufferedImage compone(ImmagineDiPixel sfondo, TipoIcona tipo) {
		ImmagineDiPixel disegno = ImmaginiAwt.pixel(BufferedImageBuilder.buildBufferedImage(tipo.getNomeRisorsa()));
		BufferedImage composta = ImmaginiAwt.buffered(ComposizioneDelleIcone.componi(sfondo, disegno));
		return BufferedImageBuilder.ingrandisci(composta, LivelloDiZoom.valore());
	}

	private static ImmagineDiPixel sfondo(TipoIcona.SfondoIcona sfondo) {
		return SFONDI.computeIfAbsent(sfondo, s -> ImmaginiAwt.pixel(BufferedImageBuilder.buildBufferedImage(s.getRisorsa())));
	}

	private static ImmagineDiPixel corniceInRicarica() {
		ImmagineDiPixel caricata = corniceInRicarica;
		if (caricata == null) {
			synchronized (IconeAwt.class) {
				caricata = corniceInRicarica;
				if (caricata == null) {
					caricata = ImmaginiAwt.pixel(BufferedImageBuilder.buildBufferedImage("icone/Sfondo-icona-trasparente.png"));
					corniceInRicarica = caricata;
				}
			}
		}
		return caricata;
	}

	/**
	 * Carica subito tutte le icone (durante il logo iniziale, in background), comprese quelle in ricarica di abilità e canti.
	 */
	public static void precarica() {
		for (TipoIcona corrente : TipoIcona.values()) {
			icona(corrente);
		}
		for (TipoAbilita abilita : TipoAbilita.values()) {
			precaricaInRicarica(ComandiAssociati.di(abilita), abilita.getRicarica());
		}
		for (TipoCanto canto : TipoCanto.values()) {
			precaricaInRicarica(ComandiAssociati.di(canto), canto.getRicarica());
		}
	}

	private static void precaricaInRicarica(Comando comando, int durata) {
		TipoIcona icona = TipoIcona.ofComando(comando);
		for (int rimasti = 1; rimasti <= durata; rimasti++) {
			iconaInRicarica(icona, rimasti, durata);
		}
	}

	/**
	 * La larghezza delle icone dopo lo zoom (quella degli sfondi, anche questa dall'intestazione del file).
	 */
	public static int getLarghezzaMassima() {
		int larghezza = 0;
		for (TipoIcona.SfondoIcona corrente : TipoIcona.SfondoIcona.values()) {
			larghezza = Math.max(larghezza, DisposizioneDelloSchermo.conZoom(new MisureDaiFile(), corrente.getRisorsa(), LivelloDiZoom.valore()).larghezza);
		}
		return larghezza;
	}

	public static int getAltezzaMassima() {
		if (altezzaMassima == -1) {
			// Dall'intestazione del file: non serve caricare le icone per saperlo
			altezzaMassima = DisposizioneDelloSchermo.altezzaMassimaDelleIcone(new MisureDaiFile(), LivelloDiZoom.valore());
		}
		return altezzaMassima;
	}
}
