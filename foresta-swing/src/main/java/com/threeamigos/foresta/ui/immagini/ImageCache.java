package com.threeamigos.foresta.ui.immagini;

import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.tipi.MappaBottinoImmagine;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPuliziaCacheDinamicaImmagini;
import com.threeamigos.foresta.tipi.Verso;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.testo.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * La raccolta delle immagini del gioco: le cornici, i fondali, le icone, le immagini delle locazioni, dei personaggi e degli oggetti, caricate una volta sola all'avvio con
 * {@link #init()}. Tiene anche una cache dinamica di immagini di testo ({@code get(testo, font, colore)}), costruite alla prima richiesta e rilasciate quando nessuno le usa più.
 */
public class ImageCache {

	public static final int SPACING = 4;

	private static final int MASSIMO_COMPOSTE = 1024;
	private static final int MASSIMO_PORZIONI = 24;
	private static final Map<List<Object>, Image> composte = lru(MASSIMO_COMPOSTE);
	private static final Map<List<Object>, Image> porzioni = lru(MASSIMO_PORZIONI);
	
	// Le risorse di cui serve la dimensione prima di caricare le immagini (vedi ForestaUI.calcolaDimensioniFinestra):
	// sono costanti, quindi usarle non inizializza ImageCache
	public static final String RISORSA_LOGO_3AM = "Logo3AM.png";
	// Il logo iniziale: quello completo, con la pergamena e gli uccelli, si mostra; da quello senza ombra, con la sola scritta nera
	// esattamente nello stesso posto, si traccia il contorno (vedi TracciatoreLogo.Costruttore.immagineDaMostrare). Si usano senza zoom
	public static final String RISORSA_LOGO_3AM_COMPLETO = "3AM_logo_completo.gif";
	public static final String RISORSA_LOGO_3AM_SENZA_OMBRA = "3AM_logo_senza_ombra.gif";
	public static final String RISORSA_CORNICE_GRANDE = "fondi/CorniceGrande.gif";
	public static final String RISORSA_CORNICE_MAPPA = "fondi/CorniceMappa.gif";
	// Non si disegna più: serve solo a ForestaUI per le dimensioni della finestra
	public static final String RISORSA_CORNICE_INCANTESIMI = "fondi/CorniceIncantesimi.gif";
	public static final String RISORSA_BOSCO = "locazioni/Foresta.gif";

	public static BufferedImage logo3AM;
	public static BufferedImage logoForesta;
	public static BufferedImage corniceGrande;
	public static BufferedImage corniceMappa;
	public static BufferedImage cornicePiccola;
	public static BufferedImage corniceCombattimento;
	public static BufferedImage corniceLarga;
	public static BufferedImage corniceInventario;
	public static BufferedImage sferaMagica;
	public static BufferedImage ombraDelDrago;
	// Lo sfondo della storia nell'intro: una foresta con templi e locande
	public static BufferedImage sfondoStoria;
	public static BufferedImage trionfo;
	public static BufferedImage separatore;
	public static BufferedImage separatoreArmi;
	public static BufferedImage separatoreElmi;
	public static BufferedImage separatoreMaschere;
	// I due capi del cartiglio con il nome delle caselle sulla mappa a tutto schermo
	public static BufferedImage cartiglioSinistro;
	public static BufferedImage cartiglioDestro;
	public static BufferedImage separatoreArmature;
	public static BufferedImage separatoreSchinieri;
	public static BufferedImage separatoreScudi;
	public static BufferedImage separatoreIncantesimi;
	public static BufferedImage separatorePozioni;
	public static BufferedImage separatoreLibriMagici;
	public static BufferedImage separatoreIncantamenti;
	public static BufferedImage separatoreNinnoli;
	public static BufferedImage segnalino;
	public static BufferedImage indicatore;
	public static BufferedImage punto;
	public static BufferedImage virgola;
	public static BufferedImage apostrofo;
	public static BufferedImage puntodd;
	public static Map<TipoLocazione, BufferedImage> locazioni;
	public static Map<TipoLocazione, BufferedImage> mappa;
	// Le immagini alternative del bosco sulla mappa (indice 0 = Foresta.gif), vedi
	// Foresta.VARIANTE_BOSCO e getImmagineMappaBosco: devono essere Foresta.NUMERO_VARIANTI_BOSCO (lo controlla un test)
	static final String[] RISORSE_VARIANTI_BOSCO = {
			"mappa/Foresta.gif", "mappa/Foresta2.gif", "mappa/Foresta3.gif", "mappa/Foresta4.gif"
	};
	static BufferedImage[] mappaVariantiBosco;
	public static BufferedImage[] lettere;
	public static BufferedImage[] cifre;

	public static BufferedImage[] spriteIncantesimi;
	public static BufferedImage spriteAmicizia;
	public static BufferedImage spriteCombattimento;
	public static BufferedImage spriteMagia;
	public static BufferedImage spritePozioneSalute;
	public static BufferedImage spritePozioneSaluteGrande;
	public static BufferedImage spritePozioneMagia;
	public static BufferedImage spritePozioneMagiaGrande;
	public static BufferedImage spritePozioneAumentoLivello;
	public static BufferedImage spriteMappa;
	public static BufferedImage spriteMoneta;
	public static BufferedImage spritePietraPreziosa;
	public static BufferedImage spriteTempo;
	public static BufferedImage spriteAumentoLivello;
	public static BufferedImage spriteGruppo;

	public static BufferedImage missioneBirra;
	public static BufferedImage missioneGallo;
	public static BufferedImage componenteScorrevoleFrecciaSu;
	public static BufferedImage componenteScorrevoleFrecciaGiu;
	public static BufferedImage frecciaGiuPersonaggioAttivo;

	// Utilizziamo una mappa thread-safe che ospita riferimenti deboli alle immagini,
	// in modo da tenere in cache le stringhe usate più di frequente ma potendo ripulire
	// la memoria da quelle non usate da più tempo, per velocizzare le operazioni di rendering.
	private static final Map<DoomdarkFont, Map<DoomdarkColorModel.Color, Map<String, WeakReference<Image>>>> cacheDinamica =
			new ConcurrentHashMap<>();
	// Le immagini dei personaggi rivolte da una certa parte (vedi MappaPersonaggioImmagine.getImmagine con il verso):
	// solo quelle che richiedono uno specchio, costruite alla prima richiesta. Riferimenti deboli come per le scritte:
	// se nessuno le usa più (il personaggio non è più in scena) il reaper ripulisce la mappa e la memoria si libera.
	private static final Map<TipoPersonaggio, Map<Verso, WeakReference<BufferedImage>>> immaginiPersonaggiPerVerso =
			new ConcurrentHashMap<>();
	// Il thread di background che gestisce il timer del reaper
	private static final ScheduledExecutorService reaperExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "ImageCache-Reaper");
		thread.setDaemon(true);
		return thread;
	});
	private static final DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
	// Separa, nella chiave della cache, la larghezza massima dal testo: un carattere che nei testi non c'è
	private static final String SEPARATORE_LARGHEZZA = "\u0000";

	private static boolean inited = false;
	
	static {
		// Locazioni, personaggi, oggetti, mappa, alfabeto e parte dei fondi stanno nei file a metà risoluzione: si ingrandiscono qui
		double zoom = LivelloDiZoom.valore();
		logo3AM = BufferedImageBuilder.buildBufferedImage(RISORSA_LOGO_3AM, zoom);
		logoForesta = BufferedImageBuilder.buildBufferedImage("LogoForesta.png", zoom);

		corniceGrande = BufferedImageBuilder.buildBufferedImage(RISORSA_CORNICE_GRANDE, zoom);
		corniceMappa = BufferedImageBuilder.buildBufferedImage(RISORSA_CORNICE_MAPPA, zoom);
		cornicePiccola = BufferedImageBuilder.buildBufferedImage("fondi/CornicePiccola.gif", zoom);
		corniceCombattimento = BufferedImageBuilder.buildBufferedImage("fondi/CorniceCombattimento.gif", zoom);
		corniceLarga = BufferedImageBuilder.buildBufferedImage("fondi/CorniceLarga.gif", zoom);
		corniceInventario = BufferedImageBuilder.buildBufferedImage("fondi/CorniceInventario.gif", zoom);
		sferaMagica = BufferedImageBuilder.buildBufferedImage("fondinon2x2/SferaMagica.gif");
		ombraDelDrago = BufferedImageBuilder.buildBufferedImage("fondi/OmbraDelDrago.gif", zoom);
		sfondoStoria = BufferedImageBuilder.buildBufferedImage("fondinon2x2/SfondoStoria.gif");
		trionfo = BufferedImageBuilder.buildBufferedImage("fondi/Trionfo.gif", zoom);
		separatore = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore.gif");
		separatoreArmi = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Armi.gif");
		separatoreElmi = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Elmi.gif");
		// Per ora una copia di quello degli elmi, da ridisegnare
		separatoreMaschere = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Maschere.gif", zoom);
		cartiglioSinistro = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Cartiglio-sinistro.gif");
		cartiglioDestro = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Cartiglio-destro.gif");
		separatoreArmature = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Armature.gif");
		separatoreSchinieri = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Schinieri.gif");
		separatoreScudi = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Scudi.gif");
		separatoreIncantesimi = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Incantesimi.gif", zoom);
		separatorePozioni = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Pozioni.gif", zoom);
		separatoreLibriMagici = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-LibriMagici.gif");
		separatoreIncantamenti = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Incantamenti.gif", zoom);
		separatoreNinnoli = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Ninnoli.gif", zoom);

		locazioni = new EnumMap<>(TipoLocazione.class);
		BufferedImage d;
		locazioni.put(TipoLocazione.BOSCO, BufferedImageBuilder.buildBufferedImage(RISORSA_BOSCO, zoom));
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Castello.gif", zoom);
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CASTELLO) {
				locazioni.put(tipoLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Citta.gif", zoom);
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
				locazioni.put(tipoLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Grotta.gif", zoom);
		locazioni.put(TipoLocazione.GROTTA, d);
		locazioni.put(TipoLocazione.LOCANDA, BufferedImageBuilder.buildBufferedImage("locazioni/Locanda.gif", zoom));
		locazioni.put(TipoLocazione.PALUDE, BufferedImageBuilder.buildBufferedImage("locazioni/Palude.gif", zoom));
		locazioni.put(TipoLocazione.RADURA, BufferedImageBuilder.buildBufferedImage("locazioni/Radura.gif", zoom));
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Rovine.gif", zoom);
		locazioni.put(TipoLocazione.ROVINE, d);
		locazioni.put(TipoLocazione.TEMPIO, BufferedImageBuilder.buildBufferedImage("locazioni/Tempio.gif", zoom));
		locazioni.put(TipoLocazione.ALCHIMISTA, BufferedImageBuilder.buildBufferedImage("locazioni/Alchimista.gif", zoom));
		locazioni.put(TipoLocazione.ARMAIOLO, BufferedImageBuilder.buildBufferedImage("locazioni/Armaiolo.gif", zoom));
		locazioni.put(TipoLocazione.VENDITORE_DI_PERGAMENE, BufferedImageBuilder.buildBufferedImage("locazioni/VenditoreDiPergamene.gif", zoom));
		locazioni.put(TipoLocazione.INCANTATORE, BufferedImageBuilder.buildBufferedImage("locazioni/Incantatore.gif", zoom));

		mappa = new EnumMap<>(TipoLocazione.class);
		mappaVariantiBosco = new BufferedImage[RISORSE_VARIANTI_BOSCO.length];
		for (int i = 0; i < RISORSE_VARIANTI_BOSCO.length; i++) {
			mappaVariantiBosco[i] = BufferedImageBuilder.buildBufferedImage(RISORSE_VARIANTI_BOSCO[i], zoom);
		}
		mappa.put(TipoLocazione.BOSCO, mappaVariantiBosco[0]);
		d = BufferedImageBuilder.buildBufferedImage("mappa/Castello.gif", zoom);
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CASTELLO) {
				mappa.put(tipoLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("mappa/Citta.gif", zoom);
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
				mappa.put(tipoLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("mappa/Grotta.gif", zoom);
		mappa.put(TipoLocazione.GROTTA, d);
		mappa.put(TipoLocazione.LOCANDA, BufferedImageBuilder.buildBufferedImage("mappa/Locanda.gif", zoom));
		mappa.put(TipoLocazione.PALUDE, BufferedImageBuilder.buildBufferedImage("mappa/Palude.gif", zoom));
		mappa.put(TipoLocazione.RADURA, BufferedImageBuilder.buildBufferedImage("mappa/Radura.gif", zoom));
		d = BufferedImageBuilder.buildBufferedImage("mappa/Rovine.gif", zoom);
		mappa.put(TipoLocazione.ROVINE, d);
		mappa.put(TipoLocazione.TEMPIO, BufferedImageBuilder.buildBufferedImage("mappa/Tempio.gif", zoom));
		mappa.put(TipoLocazione.ALCHIMISTA, BufferedImageBuilder.buildBufferedImage("mappa/Alchimista.gif", zoom));
		mappa.put(TipoLocazione.ARMAIOLO, BufferedImageBuilder.buildBufferedImage("mappa/Armaiolo.gif", zoom));
		mappa.put(TipoLocazione.VENDITORE_DI_PERGAMENE, BufferedImageBuilder.buildBufferedImage("mappa/VenditoreDiPergamene.gif", zoom));
		mappa.put(TipoLocazione.INCANTATORE, BufferedImageBuilder.buildBufferedImage("mappa/Incantatore.gif", zoom));
		segnalino = BufferedImageBuilder.buildBufferedImage("mappa/Segnalino.gif", zoom);
		indicatore = BufferedImageBuilder.buildBufferedImage("mappa/Indicatore.gif", zoom);

		lettere = new BufferedImage[26];
		lettere[0] = BufferedImageBuilder.buildBufferedImage("alfabeto/A.gif", zoom);
		lettere[1] = BufferedImageBuilder.buildBufferedImage("alfabeto/B.gif", zoom);
		lettere[2] = BufferedImageBuilder.buildBufferedImage("alfabeto/C.gif", zoom);
		lettere[3] = BufferedImageBuilder.buildBufferedImage("alfabeto/D.gif", zoom);
		lettere[4] = BufferedImageBuilder.buildBufferedImage("alfabeto/E.gif", zoom);
		lettere[5] = BufferedImageBuilder.buildBufferedImage("alfabeto/F.gif", zoom);
		lettere[6] = BufferedImageBuilder.buildBufferedImage("alfabeto/G.gif", zoom);
		lettere[7] = BufferedImageBuilder.buildBufferedImage("alfabeto/H.gif", zoom);
		lettere[8] = BufferedImageBuilder.buildBufferedImage("alfabeto/I.gif", zoom);
		lettere[9] = BufferedImageBuilder.buildBufferedImage("alfabeto/J.gif", zoom);
		lettere[10] = BufferedImageBuilder.buildBufferedImage("alfabeto/K.gif", zoom);
		lettere[11] = BufferedImageBuilder.buildBufferedImage("alfabeto/L.gif", zoom);
		lettere[12] = BufferedImageBuilder.buildBufferedImage("alfabeto/M.gif", zoom);
		lettere[13] = BufferedImageBuilder.buildBufferedImage("alfabeto/N.gif", zoom);
		lettere[14] = BufferedImageBuilder.buildBufferedImage("alfabeto/O.gif", zoom);
		lettere[15] = BufferedImageBuilder.buildBufferedImage("alfabeto/P.gif", zoom);
		lettere[16] = BufferedImageBuilder.buildBufferedImage("alfabeto/Q.gif", zoom);
		lettere[17] = BufferedImageBuilder.buildBufferedImage("alfabeto/R.gif", zoom);
		lettere[18] = BufferedImageBuilder.buildBufferedImage("alfabeto/S.gif", zoom);
		lettere[19] = BufferedImageBuilder.buildBufferedImage("alfabeto/T.gif", zoom);
		lettere[20] = BufferedImageBuilder.buildBufferedImage("alfabeto/U.gif", zoom);
		lettere[21] = BufferedImageBuilder.buildBufferedImage("alfabeto/V.gif", zoom);
		lettere[22] = BufferedImageBuilder.buildBufferedImage("alfabeto/W.gif", zoom);
		lettere[23] = BufferedImageBuilder.buildBufferedImage("alfabeto/X.gif", zoom);
		lettere[24] = BufferedImageBuilder.buildBufferedImage("alfabeto/Y.gif", zoom);
		lettere[25] = BufferedImageBuilder.buildBufferedImage("alfabeto/Z.gif", zoom);

		cifre = new BufferedImage[10];
		cifre[0] = BufferedImageBuilder.buildBufferedImage("alfabeto/0.gif", zoom);
		cifre[1] = BufferedImageBuilder.buildBufferedImage("alfabeto/1.gif", zoom);
		cifre[2] = BufferedImageBuilder.buildBufferedImage("alfabeto/2.gif", zoom);
		cifre[3] = BufferedImageBuilder.buildBufferedImage("alfabeto/3.gif", zoom);
		cifre[4] = BufferedImageBuilder.buildBufferedImage("alfabeto/4.gif", zoom);
		cifre[5] = BufferedImageBuilder.buildBufferedImage("alfabeto/5.gif", zoom);
		cifre[6] = BufferedImageBuilder.buildBufferedImage("alfabeto/6.gif", zoom);
		cifre[7] = BufferedImageBuilder.buildBufferedImage("alfabeto/7.gif", zoom);
		cifre[8] = BufferedImageBuilder.buildBufferedImage("alfabeto/8.gif", zoom);
		cifre[9] = BufferedImageBuilder.buildBufferedImage("alfabeto/9.gif", zoom);

		punto = BufferedImageBuilder.buildBufferedImage("alfabeto/Punto.gif", zoom);
		virgola = BufferedImageBuilder.buildBufferedImage("alfabeto/Virgola.gif", zoom);
		puntodd = BufferedImageBuilder.buildBufferedImage("alfabeto/PuntoDiDomanda.gif", zoom);
		apostrofo = BufferedImageBuilder.buildBufferedImage("alfabeto/Apostrofo.gif", zoom);
		
		spriteIncantesimi = new BufferedImage[TipoIncantesimo.values().length];
		spriteIncantesimi[TipoIncantesimo.ARIA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Aria.gif");
		spriteIncantesimi[TipoIncantesimo.ACQUA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Acqua.gif");
		spriteIncantesimi[TipoIncantesimo.TERRA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Terra.gif");
		spriteIncantesimi[TipoIncantesimo.FUOCO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Fuoco.gif");
		spriteIncantesimi[TipoIncantesimo.FULMINE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Fulmine.gif");
		spriteIncantesimi[TipoIncantesimo.GELO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Gelo.gif");
		spriteIncantesimi[TipoIncantesimo.VELENO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Veleno.gif");
		spriteIncantesimi[TipoIncantesimo.MORTE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Morte.gif");
		spriteIncantesimi[TipoIncantesimo.RESURREZIONE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Resurrezione.gif");
		spriteIncantesimi[TipoIncantesimo.SILENZIO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Silenzio.gif");

		spriteAmicizia = BufferedImageBuilder.buildBufferedImage("icone/Amicizia.gif");
		spriteCombattimento = BufferedImageBuilder.buildBufferedImage("icone/Combattimento.gif");
		spriteMagia = BufferedImageBuilder.buildBufferedImage("icone/Incantesimo.gif");
		spritePozioneSalute = BufferedImageBuilder.buildBufferedImage("icone/PozioneSalute.gif");
		spritePozioneSaluteGrande = BufferedImageBuilder.buildBufferedImage("icone/PozioneSaluteGrande.gif");
		spritePozioneMagia = BufferedImageBuilder.buildBufferedImage("icone/PozioneMagia.gif");
		spritePozioneMagiaGrande = BufferedImageBuilder.buildBufferedImage("icone/PozioneMagiaGrande.gif");
		spritePozioneAumentoLivello = BufferedImageBuilder.buildBufferedImage("icone/AumentoLivello.gif");
		spriteMappa = BufferedImageBuilder.buildBufferedImage("icone/Mappa.gif");
		spriteMoneta = BufferedImageBuilder.buildBufferedImage("icone/Moneta.gif");
		spritePietraPreziosa = BufferedImageBuilder.buildBufferedImage("icone/PietraPreziosa.gif");
		spriteTempo = BufferedImageBuilder.buildBufferedImage("icone/Tempo.gif");
		// Lo stesso disegno della pozione: un solo caricamento, due nomi per due usi (la pozione e il livello guadagnato)
		spriteAumentoLivello = spritePozioneAumentoLivello;
		spriteGruppo = BufferedImageBuilder.buildBufferedImage("icone/Gruppo.gif");

		missioneBirra = BufferedImageBuilder.buildBufferedImage("icone/Missione-birra-grande.gif", zoom);
		missioneGallo = BufferedImageBuilder.buildBufferedImage("icone/Missione-gallo.gif");

		componenteScorrevoleFrecciaSu = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaSu.gif");
		componenteScorrevoleFrecciaGiu = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaGiu.gif");
		frecciaGiuPersonaggioAttivo = BufferedImageBuilder.buildBufferedImage("icone/Giu.gif");

		// Avvia il reaper in background ogni minuto: è un thread daemon, finisce da solo con la JVM
		avviaReaper(1, TimeUnit.MINUTES);
	}

	/**
	 * Carica tutte le immagini, la prima volta; le volte successive non fa niente.
	 */
	public static void init() {
		if (!inited) {
			// Forzo il caricamento delle immagini dei personaggi, che stanno in MappaPersonaggioImmagine
			MappaPersonaggioImmagine.values();
			// ...e quelle degli oggetti, che il motore non carica (vedi MappaBottinoImmagine)
			MappaBottinoImmagine.values();
			// ...e le icone, che si caricano al primo uso (vedi TipoIcona)
			TipoIcona.precarica();
			inited = true;
		}
	}

	private static void avviaReaper(long periodo, TimeUnit unitaTempo) {
		reaperExecutor.scheduleAtFixedRate(() -> {
			try {
				int elementiPrima = 0;
				int elementiDopo = 0;

				// Rimuoviamo dalla mappa tutte le chiavi il cui valore interno (l'immagine)
				// è stato reclamato dal Garbage Collector perché non più referenziato nella UI
				for (DoomdarkFont font : cacheDinamica.keySet()) {
					Map<DoomdarkColorModel.Color, Map<String, WeakReference<Image>>> mapPerColore =
							cacheDinamica.get(font);
					for (DoomdarkColorModel.Color colore : mapPerColore.keySet()) {
						Map<String, WeakReference<Image>> mapPerString = mapPerColore.get(colore);
						elementiPrima += mapPerString.size();
						mapPerString.entrySet().removeIf(entry -> entry.getValue().get() == null);
						elementiDopo += mapPerString.size();
					}
				}
				for (Map<Verso, WeakReference<BufferedImage>> mapPerVerso : immaginiPersonaggiPerVerso.values()) {
					elementiPrima += mapPerVerso.size();
					mapPerVerso.entrySet().removeIf(entry -> entry.getValue().get() == null);
					elementiDopo += mapPerVerso.size();
				}
				int rimossi = elementiPrima - elementiDopo;
				if (rimossi > 0) {
					BusEventi.pubblica(new InternoPuliziaCacheDinamicaImmagini(elementiPrima, elementiDopo));
				}
			} catch (Exception e) {
				// Protezione anti-crash per evitare che un errore blocchi i tick futuri del reaper
				System.err.println("Errore durante l'esecuzione del reaper di ImageCache: " + e.getMessage());
			}
		}, periodo, periodo, unitaTempo);
	}


	/**
	 * L'immagine della variante di bosco indicata (1-based, vedi Bosco.getVarianteMappa),
	 * da usare al posto di {@code mappa.get(TipoLocazione.BOSCO)}.
	 */
	public static BufferedImage getImmagineMappaBosco(int variante) {
		return mappaVariantiBosco[variante - 1];
	}

	/**
	 * Metodo di utilità che costruisce e memorizza immagini utilizzate spessissimo (Ad esempio, nomi e statistiche)
	 * usando il font DoomdarkFontMedium
	 */
	public static Image get(int valore, DoomdarkColorModel.Color colore) {
		return get(Integer.toString(valore), fontMedium, colore);
	}

	/**
	 * Metodo di utilità che costruisce e memorizza immagini utilizzate spessissimo (Ad esempio, nomi e statistiche)
	 * usando il font DoomdarkFontMedium
	 */
	public static Image get(String testo, DoomdarkColorModel.Color colore) {
		return get(testo, fontMedium, colore);
	}

	/**
	 * Metodo di utilità che costruisce e memorizza immagini utilizzate spessissimo (Ad esempio, nomi e statistiche)
	 */
	public static Image get(int valore, DoomdarkFont font, DoomdarkColorModel.Color colore) {
		return get(Integer.toString(valore), font, colore);
	}

	/**
	 * Metodo di utilità che costruisce e memorizza immagini utilizzate spessissimo (Ad esempio, nomi e statistiche)
	 */
	public static Image get(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore) {
		return get(font, colore, testo, () -> DoomdarkTextProducer.getImage(testo, font, colore));
	}

	/**
	 * Come {@link #get(String, DoomdarkFont, DoomdarkColorModel.Color)}, ma il testo va a capo per non superare la
	 * larghezza massima.
	 */
	public static Image get(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore, int larghezzaMassima) {
		// Lo stesso testo a capo a larghezze diverse è un'altra immagine: la larghezza entra nella chiave
		return get(font, colore, larghezzaMassima + SEPARATORE_LARGHEZZA + testo,
				() -> DoomdarkTextProducer.getImage(testo, font, colore, larghezzaMassima));
	}

	/**
	 * L'immagine di un personaggio rivolta dal verso indicato, costruita con il costruttore la prima volta (o dopo che
	 * il garbage collector ha reclamato la precedente) e poi ricordata. Chi la usa ne tiene un riferimento finché serve.
	 */
	public static BufferedImage getImmaginePersonaggio(TipoPersonaggio tipo, Verso verso, Supplier<BufferedImage> costruttore) {
		Map<Verso, WeakReference<BufferedImage>> perVerso = immaginiPersonaggiPerVerso
				.computeIfAbsent(tipo, k -> new ConcurrentHashMap<>());
		WeakReference<BufferedImage> ref = perVerso.get(verso);
		BufferedImage immagine = (ref != null) ? ref.get() : null;
		if (immagine == null) {
			immagine = costruttore.get();
			perVerso.put(verso, new WeakReference<>(immagine));
		}
		return immagine;
	}

	/**
	 * La sagoma dell'immagine di quel colore: dove l'immagine è opaca, il colore pieno; il resto resta trasparente. Serve a ombre e silhouette
	 * (l'ombra dei fumetti e degli sprite, le voci del bestiario ancora chiuse).
	 */
	public static BufferedImage sagomaScura(BufferedImage sorgente, Color colore) {
		BufferedImage sagoma = BufferedImageBuilder.creaCompatibile(sorgente.getWidth(), sorgente.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D g = sagoma.createGraphics();
		g.drawImage(sorgente, 0, 0, null);
		g.setComposite(AlphaComposite.SrcIn);
		g.setColor(colore);
		g.fillRect(0, 0, sagoma.getWidth(), sagoma.getHeight());
		g.dispose();
		return sagoma;
	}

	/**
	 * Come {@link #sagomaScura(BufferedImage, Color)}, ricordata per la classe e il colore: per le silhouette che si ridisegnano a ogni fotogramma.
	 */
	public static Image getSagomaDelPersonaggio(TipoPersonaggio classe, Verso verso, Color colore, Supplier<BufferedImage> immagine) {
		return getComposta(Arrays.asList("sagomaDelPersonaggio", classe, verso, colore), () -> sagomaScura(immagine.get(), colore));
	}

	/**
	 * Un testo a capo entro la larghezza indicata, con l'interlinea indicata fra le righe: un'immagine larga esattamente
	 * {@code larghezza}, composta dalle righe di {@code get(testo, font, colore)}. A differenza di quella a capo con
	 * la sola larghezza massima, qui l'altezza di ogni riga è quella che serve a chi impagina (vedi ComponenteScorrevole).
	 */
	public static Image getTestoACapo(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore, int larghezza, int interlinea) {
		return getComposta(Arrays.asList("testoACapo", font, colore, testo, larghezza, interlinea), () -> {
			List<String> righe = FontTool.split(font, testo, larghezza);
			BufferedImage canvas = BufferedImageBuilder.creaCompatibile(larghezza, (font.getHeight() + interlinea) * righe.size(), Transparency.TRANSLUCENT);
			Graphics2D g2d = canvas.createGraphics();
			int y = 0;
			for (String riga : righe) {
				g2d.drawImage(get(riga, font, colore), 0, y, null);
				y += font.getHeight() + interlinea;
			}
			g2d.dispose();
			return canvas;
		});
	}

	/**
	 * Un'immagine composta da altre (per esempio livello e prezzo di un artefatto), costruita la prima volta che la
	 * chiave compare e poi riusata. La chiave dice di cosa si tratta: se contiene immagini valgono per identità, quindi
	 * le sorgenti devono venire da {@code get}, che dà sempre la stessa. A differenza della cache dinamica dei testi
	 * qui il riferimento è forte (le più vecchie si scartano oltre {@value #MASSIMO_COMPOSTE}), perché la composta
	 * sarebbe altrimenti ricostruita ogni volta che nessuno la tiene in mano, cioè a ogni fotogramma.
	 */
	public static Image getComposta(List<Object> chiave, Supplier<Image> costruttore) {
		return getInLru(composte, chiave, costruttore);
	}

	/**
	 * La porzione visibile di una lista scorrevole già disegnata. Tenuta a parte da {@link #getComposta} perché ogni
	 * porzione è grande (l'area di un riquadro), quindi se ne tengono poche: {@value #MASSIMO_PORZIONI}.
	 */
	public static Image getPorzione(List<Object> chiave, Supplier<Image> costruttore) {
		return getInLru(porzioni, chiave, costruttore);
	}

	private static Image getInLru(Map<List<Object>, Image> lru, List<Object> chiave, Supplier<Image> costruttore) {
		synchronized (lru) {
			Image inCache = lru.get(chiave);
			if (inCache != null) {
				return inCache;
			}
		}
		Image costruita = costruttore.get();
		synchronized (lru) {
			lru.put(chiave, costruita);
		}
		return costruita;
	}

	private static <K, V> Map<K, V> lru(int massimo) {
		return new LinkedHashMap<K, V>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > massimo;
			}
		};
	}

	private static Image get(DoomdarkFont font, DoomdarkColorModel.Color colore, String chiave, Supplier<Image> costruttore) {
		Map<String, WeakReference<Image>> stringToImageMap = cacheDinamica
				// Mappe concorrenti anche all'interno: il reaper le ripulisce dal suo thread mentre l'EDT le usa
				.computeIfAbsent(font, k -> new ConcurrentHashMap<>())
				.computeIfAbsent(colore, k -> new ConcurrentHashMap<>());
		WeakReference<Image> ref = stringToImageMap.get(chiave);
		Image img = (ref != null) ? ref.get() : null;
		if (img == null) {
			img = costruttore.get();
			stringToImageMap.put(chiave, new WeakReference<>(img));
		}
		return img;
	}
}
