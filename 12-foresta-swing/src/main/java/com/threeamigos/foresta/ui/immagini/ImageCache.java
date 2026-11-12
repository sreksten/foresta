package com.threeamigos.foresta.ui.immagini;

import com.threeamigos.foresta.ui.DisposizioneDelloSchermo;
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
import com.threeamigos.foresta.ui.Finestra;
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

	public static final int SPACING = Finestra.SPACING;

	private static final int MASSIMO_COMPOSTE = 1024;
	private static final int MASSIMO_PORZIONI = 24;
	private static final Map<List<Object>, Image> composte = lru(MASSIMO_COMPOSTE);
	private static final Map<List<Object>, Image> porzioni = lru(MASSIMO_PORZIONI);
	
	// Le risorse dei loghi: sono costanti, quindi usarle non inizializza ImageCache. Quelle delle cornici di cui serve la dimensione
	// prima di caricare le immagini stanno in DisposizioneDelloSchermo, nel modulo comune
	public static final String RISORSA_LOGO_3AM = "Logo3AM.png";
	// Il logo iniziale: quello completo, con la pergamena e gli uccelli, si mostra; da quello senza ombra, con la sola scritta nera
	// esattamente nello stesso posto, si traccia il contorno (vedi TracciatoreLogo.Costruttore.immagineDaMostrare). Si usano senza zoom
	public static final String RISORSA_LOGO_3AM_COMPLETO = "3AM_logo_completo.png";
	public static final String RISORSA_LOGO_3AM_SENZA_OMBRA = "3AM_logo_senza_ombra.png";

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
	public static Map<TipoLocazione, BufferedImage> locazioni;
	public static Map<TipoLocazione, BufferedImage> mappa;
	// Le immagini alternative del bosco sulla mappa (indice 0 = Foresta.png), vedi
	// Foresta.VARIANTE_BOSCO e getImmagineMappaBosco: devono essere Foresta.NUMERO_VARIANTI_BOSCO (lo controlla un test)
	static final String[] RISORSE_VARIANTI_BOSCO = NomiDelleImmagini.VARIANTI_DEL_BOSCO_SULLA_MAPPA;
	static BufferedImage[] mappaVariantiBosco;
	/**
	 * I glifi dell'alfabeto grande (maiuscole, cifre e ' , . ?), già ingranditi, presi dall'atlante {@code img/font/alfabeto_grande.png}
	 */
	private static Map<Character, BufferedImage> alfabetoGrande;

	/**
	 * La mano che fa da puntatore col joystick virtuale, alla sua dimensione (senza zoom)
	 */
	public static BufferedImage manoPuntatore;
	public static BufferedImage[] spriteIncantesimi;
	public static BufferedImage spriteAmicizia;
	public static BufferedImage spriteCombattimento;
	public static BufferedImage spriteMagia;
	public static BufferedImage spritePozioneSalute;
	public static BufferedImage spritePozioneSaluteGrande;
	public static BufferedImage spritePozioneMagia;
	public static BufferedImage spritePozioneMagiaGrande;
	public static BufferedImage spritePozionePanacea;
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
	public static BufferedImage componenteScorrevoleFrecciaSinistra;
	public static BufferedImage componenteScorrevoleFrecciaDestra;
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
	private static final DoomdarkFont fontMedium = DoomdarkFont.medio();
	// Separa, nella chiave della cache, la larghezza massima dal testo: un carattere che nei testi non c'è
	private static final String SEPARATORE_LARGHEZZA = "\u0000";

	private static boolean inited = false;
	
	static {
		// Locazioni, personaggi, oggetti, mappa, alfabeto e parte dei fondi stanno nei file a metà risoluzione: si ingrandiscono qui
		double zoom = LivelloDiZoom.valore();
		logo3AM = BufferedImageBuilder.buildBufferedImage(RISORSA_LOGO_3AM, zoom);
		logoForesta = BufferedImageBuilder.buildBufferedImage("LogoForesta.png", zoom);

		corniceGrande = BufferedImageBuilder.buildBufferedImage(DisposizioneDelloSchermo.RISORSA_CORNICE_GRANDE, zoom);
		corniceMappa = BufferedImageBuilder.buildBufferedImage(DisposizioneDelloSchermo.RISORSA_CORNICE_MAPPA, zoom);
		cornicePiccola = BufferedImageBuilder.buildBufferedImage(DisposizioneDelloSchermo.RISORSA_CORNICE_PICCOLA, zoom);
		corniceCombattimento = BufferedImageBuilder.buildBufferedImage("fondi/CorniceCombattimento.png", zoom);
		corniceLarga = BufferedImageBuilder.buildBufferedImage("fondi/CorniceLarga.png", zoom);
		corniceInventario = BufferedImageBuilder.buildBufferedImage("fondi/CorniceInventario.png", zoom);
		sferaMagica = BufferedImageBuilder.buildBufferedImage("fondinon2x2/SferaMagica.png");
		ombraDelDrago = BufferedImageBuilder.buildBufferedImage("fondi/OmbraDelDrago.png", zoom);
		sfondoStoria = BufferedImageBuilder.buildBufferedImage("fondinon2x2/SfondoStoria.png");
		trionfo = BufferedImageBuilder.buildBufferedImage("fondi/Trionfo.png", zoom);
		separatore = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore.png");
		separatoreArmi = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Armi.png");
		separatoreElmi = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Elmi.png");
		// Per ora una copia di quello degli elmi, da ridisegnare
		separatoreMaschere = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Maschere.png", zoom);
		cartiglioSinistro = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Cartiglio-sinistro.png");
		cartiglioDestro = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Cartiglio-destro.png");
		separatoreArmature = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Armature.png");
		separatoreSchinieri = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Schinieri.png");
		separatoreScudi = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-Scudi.png");
		separatoreIncantesimi = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Incantesimi.png", zoom);
		separatorePozioni = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Pozioni.png", zoom);
		separatoreLibriMagici = BufferedImageBuilder.buildBufferedImage("fondinon2x2/Separatore-LibriMagici.png");
		separatoreIncantamenti = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Incantamenti.png", zoom);
		separatoreNinnoli = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Ninnoli.png", zoom);

		locazioni = new EnumMap<>(TipoLocazione.class);
		// I nomi degli sfondi sono nel modulo comune; quelli con lo stesso sfondo (i castelli, le città) condividono l'immagine
		Map<String, BufferedImage> sfondi = new HashMap<>();
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			String risorsa = NomiDelleImmagini.locazione(tipoLocazione);
			if (risorsa != null) {
				locazioni.put(tipoLocazione, sfondi.computeIfAbsent(risorsa, r -> BufferedImageBuilder.buildBufferedImage(r, zoom)));
			}
		}

		mappa = new EnumMap<>(TipoLocazione.class);
		mappaVariantiBosco = new BufferedImage[RISORSE_VARIANTI_BOSCO.length];
		for (int i = 0; i < RISORSE_VARIANTI_BOSCO.length; i++) {
			mappaVariantiBosco[i] = ImmaginiAwt.suFondoNero(BufferedImageBuilder.buildBufferedImage(RISORSE_VARIANTI_BOSCO[i], zoom));
		}
		// I nomi delle tessere sono nel modulo comune; quelle con la stessa tessera (i castelli, le città) condividono l'immagine
		Map<String, BufferedImage> tessere = new HashMap<>();
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			String risorsa = NomiDelleImmagini.tessera(tipoLocazione);
			if (tipoLocazione == TipoLocazione.BOSCO) {
				mappa.put(tipoLocazione, mappaVariantiBosco[0]);
			} else if (risorsa != null) {
				// Le tessere hanno parti trasparenti: sulla mappa stanno sul nero
				mappa.put(tipoLocazione, tessere.computeIfAbsent(risorsa, r -> ImmaginiAwt.suFondoNero(BufferedImageBuilder.buildBufferedImage(r, zoom))));
			}
		}
		segnalino = BufferedImageBuilder.buildBufferedImage("mappa/Segnalino.png", zoom);
		indicatore = BufferedImageBuilder.buildBufferedImage("mappa/Indicatore.png", zoom);

		alfabetoGrande = costruisciAlfabetoGrande(zoom);
		
		spriteIncantesimi = new BufferedImage[TipoIncantesimo.values().length];
		spriteIncantesimi[TipoIncantesimo.ARIA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Aria.png");
		spriteIncantesimi[TipoIncantesimo.ACQUA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Acqua.png");
		spriteIncantesimi[TipoIncantesimo.TERRA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Terra.png");
		spriteIncantesimi[TipoIncantesimo.FUOCO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Fuoco.png");
		spriteIncantesimi[TipoIncantesimo.FULMINE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Fulmine.png");
		spriteIncantesimi[TipoIncantesimo.GELO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Gelo.png");
		spriteIncantesimi[TipoIncantesimo.VELENO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Veleno.png");
		spriteIncantesimi[TipoIncantesimo.MORTE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Morte.png");
		spriteIncantesimi[TipoIncantesimo.RESURREZIONE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Resurrezione.png");
		spriteIncantesimi[TipoIncantesimo.SILENZIO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Silenzio.png");
		spriteIncantesimi[TipoIncantesimo.NEBBIA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Nebbia.png");
		spriteIncantesimi[TipoIncantesimo.NEGA_MAGIA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/NegaMagia.png");
		spriteIncantesimi[TipoIncantesimo.GRIDO_DI_BATTAGLIA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/GridoDiBattaglia.png");
		spriteIncantesimi[TipoIncantesimo.TERREMOTO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Terremoto.png");
		spriteIncantesimi[TipoIncantesimo.TEMPESTA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Tempesta.png");
		spriteIncantesimi[TipoIncantesimo.TERRORE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Terrore.png");

		spriteAmicizia = BufferedImageBuilder.buildBufferedImage("icone/Amicizia.png");
		spriteCombattimento = BufferedImageBuilder.buildBufferedImage("icone/Combattimento.png");
		spriteMagia = BufferedImageBuilder.buildBufferedImage("icone/Incantesimo.png");
		spritePozioneSalute = BufferedImageBuilder.buildBufferedImage("icone/PozioneSalute.png");
		spritePozioneSaluteGrande = BufferedImageBuilder.buildBufferedImage("icone/PozioneSaluteGrande.png");
		spritePozioneMagia = BufferedImageBuilder.buildBufferedImage("icone/PozioneMagia.png");
		spritePozioneMagiaGrande = BufferedImageBuilder.buildBufferedImage("icone/PozioneMagiaGrande.png");
		spritePozionePanacea = BufferedImageBuilder.buildBufferedImage("icone/PozionePanacea.png");
		spritePozioneAumentoLivello = BufferedImageBuilder.buildBufferedImage("icone/AumentoLivello.png");
		spriteMappa = BufferedImageBuilder.buildBufferedImage("icone/Mappa.png");
		spriteMoneta = BufferedImageBuilder.buildBufferedImage("icone/Moneta.png");
		spritePietraPreziosa = BufferedImageBuilder.buildBufferedImage("icone/PietraPreziosa.png");
		spriteTempo = BufferedImageBuilder.buildBufferedImage("icone/Tempo.png");
		// Lo stesso disegno della pozione: un solo caricamento, due nomi per due usi (la pozione e il livello guadagnato)
		spriteAumentoLivello = spritePozioneAumentoLivello;
		spriteGruppo = BufferedImageBuilder.buildBufferedImage("icone/Gruppo.png");
		manoPuntatore = BufferedImageBuilder.buildBufferedImage("icone/ManoPuntatore.png");

		missioneBirra = BufferedImageBuilder.buildBufferedImage("icone/Missione-birra-grande.png", zoom);
		missioneGallo = BufferedImageBuilder.buildBufferedImage("icone/Missione-gallo.png");

		componenteScorrevoleFrecciaSu = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaSu.png");
		componenteScorrevoleFrecciaGiu = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaGiu.png");
		componenteScorrevoleFrecciaSinistra = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaSinistra.png");
		componenteScorrevoleFrecciaDestra = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaDestra.png");
		frecciaGiuPersonaggioAttivo = BufferedImageBuilder.buildBufferedImage("icone/Giu.png");

		// Avvia il reaper in background ogni minuto: è un thread daemon, finisce da solo con la JVM
		avviaReaper(1, TimeUnit.MINUTES);
	}

	private static final String RISORSA_ALFABETO_GRANDE = "/com/threeamigos/foresta/img/font/alfabeto_grande.png";
	private static final int ALFABETO_GRANDE_LARGHEZZA_DELLO_SPAZIO = 5;

	/**
	 * I glifi dell'atlante dell'alfabeto grande, in maiuscolo (la riga delle minuscole è vuota), già ingranditi. Hanno
	 * l'altezza dell'intero riquadro: le righe in più in cima servono agli accenti, quelle in fondo alle cediglie.
	 */
	private static Map<Character, BufferedImage> costruisciAlfabetoGrande(double zoom) {
		ImmagineDelFontAColori atlante = ImmagineDelFontAColori.carica(RISORSA_ALFABETO_GRANDE, ALFABETO_GRANDE_LARGHEZZA_DELLO_SPAZIO);
		Map<Character, BufferedImage> glifi = new HashMap<>();
		for (Map.Entry<Character, GlifoAColori> voce : atlante.getGlifi().entrySet()) {
			if (voce.getKey() == ' ') {
				continue; // lo spazio lo misura chi scrive il testo
			}
			GlifoAColori glifo = voce.getValue();
			BufferedImage immagine = new BufferedImage(glifo.getLarghezza(), glifo.getAltezza(), BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < glifo.getAltezza(); y++) {
				for (int x = 0; x < glifo.getLarghezza(); x++) {
					immagine.setRGB(x, y, glifo.getPixel(x, y));
				}
			}
			glifi.put(voce.getKey(), BufferedImageBuilder.ingrandisci(immagine, zoom));
		}
		return glifi;
	}

	/**
	 * Il glifo dell'alfabeto grande (in maiuscolo: lettere, anche accentate, cifre e punteggiatura), o null se l'alfabeto
	 * non ha quel carattere. È alto quanto un riquadro dell'atlante, con lo spazio per accenti e cediglie.
	 */
	public static BufferedImage glifoGrande(char c) {
		return alfabetoGrande.get(c);
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
			IconeAwt.precarica();
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
	 * usando il font medio
	 */
	public static Image get(int valore, DoomdarkColorModel.Color colore) {
		return get(Integer.toString(valore), fontMedium, colore);
	}

	/**
	 * Metodo di utilità che costruisce e memorizza immagini utilizzate spessissimo (Ad esempio, nomi e statistiche)
	 * usando il font medio
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
