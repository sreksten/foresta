package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPuliziaCacheDinamicaImmagini;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.strumenti.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ImageCache {

	static final int SPACING = 4;
	
	// Le risorse di cui serve la dimensione prima di caricare le immagini (vedi ForestaUI.calcolaDimensioniFinestra):
	// sono costanti, quindi usarle non inizializza ImageCache
	static final String RISORSA_LOGO_3AM = "Logo3AM.png";
	static final String RISORSA_CORNICE_GRANDE = "fondi/CorniceGrande.gif";
	static final String RISORSA_CORNICE_INCANTESIMI = "fondi/CorniceIncantesimi.gif";
	static final String RISORSA_CORNICE_MAPPA = "fondi/CorniceMappa.gif";
	static final String RISORSA_BOSCO = "locazioni/Foresta.gif";

	static BufferedImage logo3AM;
	static BufferedImage logoForesta;
	static BufferedImage corniceGrande;
	static BufferedImage corniceIncantesimi;
	static BufferedImage corniceMappa;
	static BufferedImage cornicePiccola;
	static BufferedImage corniceLarga;
	static BufferedImage corniceInventario;
	static BufferedImage sferaMagica;
	static BufferedImage armaiolo;
	static BufferedImage alchimista;
	static BufferedImage venditoreDiPergamene;
	static BufferedImage incantatore;
	static BufferedImage ombraDelDrago;
	// Lo sfondo della storia nell'intro: una foresta con templi e locande
	static BufferedImage sfondoStoria;
	static BufferedImage trionfo;
	static BufferedImage separatore;
	static BufferedImage separatoreArmi;
	static BufferedImage separatoreElmi;
	static BufferedImage separatoreMaschere;
	// I due capi del cartiglio con il nome delle caselle sulla mappa a tutto schermo
	static BufferedImage cartiglioSinistro;
	static BufferedImage cartiglioDestro;
	static BufferedImage separatoreArmature;
	static BufferedImage separatoreSchinieri;
	static BufferedImage separatoreScudi;
	static BufferedImage separatoreIncantesimi;
	static BufferedImage separatorePozioni;
	static BufferedImage separatoreLibriMagici;
	static BufferedImage separatoreIncantamenti;
	static BufferedImage separatoreNinnoli;
	static BufferedImage segnalino;
	static BufferedImage indicatore;
	static BufferedImage punto;
	static BufferedImage virgola;
	static BufferedImage apostrofo;
	static BufferedImage puntodd;
	static Map<TipoLocazione, BufferedImage> locazioni;
	static Map<TipoLocazione, BufferedImage> mappa;
	// Le immagini alternative del bosco sulla mappa (indice 0 = Foresta.gif), vedi
	// Bosco.VARIANTE_MAPPA e getImmagineMappaBosco: devono essere Bosco.NUMERO_VARIANTI_MAPPA (lo controlla un test)
	static final String[] RISORSE_VARIANTI_BOSCO = {
			"mappa/Foresta.gif", "mappa/Foresta2.gif", "mappa/Foresta3.gif", "mappa/Foresta4.gif"
	};
	static BufferedImage[] mappaVariantiBosco;
	static BufferedImage[] lettere;
	static BufferedImage[] cifre;

	static BufferedImage[] spriteIncantesimi;
	static BufferedImage spriteAmicizia;
	static BufferedImage spriteCombattimento;
	static BufferedImage spriteMagia;
	static BufferedImage spritePozioneSalute;
	static BufferedImage spritePozioneSaluteGrande;
	static BufferedImage spritePozioneMagia;
	static BufferedImage spritePozioneMagiaGrande;
	static BufferedImage spriteMappa;
	static BufferedImage spriteMoneta;
	static BufferedImage spritePietraPreziosa;
	static BufferedImage spriteTempo;
	static BufferedImage spriteAumentoLivello;
	static BufferedImage spriteGruppo;

	static BufferedImage missioneBirra;
	static BufferedImage missioneGallo;
	static BufferedImage componenteScorrevoleFrecciaSu;
	static BufferedImage componenteScorrevoleFrecciaGiu;


	private static final Map<String, BufferedImage> imageMap = new HashMap<>();

	// Utilizziamo una mappa thread-safe che ospita riferimenti deboli alle immagini,
	// in modo da tenere in cache le stringhe usate più di frequente ma potendo ripulire
	// la memoria da quelle non usate da più tempo, per velocizzare le operazioni di rendering.
	private static final Map<DoomdarkFont, Map<DoomdarkColorModel.Color, Map<String, WeakReference<Image>>>> cacheDinamica =
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
		logo3AM = BufferedImageBuilder.buildBufferedImage(RISORSA_LOGO_3AM);
		logoForesta = BufferedImageBuilder.buildBufferedImage("LogoForesta.png");

		corniceGrande = BufferedImageBuilder.buildBufferedImage(RISORSA_CORNICE_GRANDE);
		corniceIncantesimi = BufferedImageBuilder.buildBufferedImage(RISORSA_CORNICE_INCANTESIMI);
		corniceMappa = BufferedImageBuilder.buildBufferedImage(RISORSA_CORNICE_MAPPA);
		cornicePiccola = BufferedImageBuilder.buildBufferedImage("fondi/CornicePiccola.gif");
		corniceLarga = BufferedImageBuilder.buildBufferedImage("fondi/CorniceLarga.gif");
		corniceInventario = BufferedImageBuilder.buildBufferedImage("fondi/CorniceInventario.gif");
		sferaMagica = BufferedImageBuilder.buildBufferedImage("fondi/SferaMagica.gif");
		armaiolo = BufferedImageBuilder.buildBufferedImage("personaggi/Armaiolo.gif");
		alchimista = BufferedImageBuilder.buildBufferedImage("personaggi/Alchimista.gif");
		venditoreDiPergamene = BufferedImageBuilder.buildBufferedImage("personaggi/VenditoreDiPergamene.gif");
		incantatore = BufferedImageBuilder.buildBufferedImage("personaggi/Incantatore.gif");
		ombraDelDrago = BufferedImageBuilder.buildBufferedImage("fondi/OmbraDelDrago.gif");
		sfondoStoria = BufferedImageBuilder.buildBufferedImage("fondi/SfondoStoria.gif");
		trionfo = BufferedImageBuilder.buildBufferedImage("fondi/Trionfo.gif");
		separatore = BufferedImageBuilder.buildBufferedImage("fondi/Separatore.gif");
		separatoreArmi = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Armi.gif");
		separatoreElmi = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Elmi.gif");
		// Per ora una copia di quello degli elmi, da ridisegnare
		separatoreMaschere = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Maschere.gif");
		cartiglioSinistro = BufferedImageBuilder.buildBufferedImage("fondi/Cartiglio-sinistro.gif");
		cartiglioDestro = BufferedImageBuilder.buildBufferedImage("fondi/Cartiglio-destro.gif");
		separatoreArmature = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Armature.gif");
		separatoreSchinieri = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Schinieri.gif");
		separatoreScudi = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Scudi.gif");
		separatoreIncantesimi = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Incantesimi.gif");
		separatorePozioni = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Pozioni.gif");
		separatoreLibriMagici = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-LibriMagici.gif");
		separatoreIncantamenti = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Incantamenti.gif");
		separatoreNinnoli = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Ninnoli.gif");

		locazioni = new EnumMap<>(TipoLocazione.class);
		BufferedImage d;
		locazioni.put(TipoLocazione.BOSCO, BufferedImageBuilder.buildBufferedImage(RISORSA_BOSCO));
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Castello.gif");
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoria() == CategoriaLocazione.CASTELLO) {
				locazioni.put(tipoLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Citta.gif");
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoria() == CategoriaLocazione.CITTA) {
				locazioni.put(tipoLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Grotta.gif");
		locazioni.put(TipoLocazione.GROTTA, d);
		locazioni.put(TipoLocazione.LOCANDA, BufferedImageBuilder.buildBufferedImage("locazioni/Locanda.gif"));
		locazioni.put(TipoLocazione.PALUDE, BufferedImageBuilder.buildBufferedImage("locazioni/Palude.gif"));
		locazioni.put(TipoLocazione.RADURA, BufferedImageBuilder.buildBufferedImage("locazioni/Radura.gif"));
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Rovine.gif");
		locazioni.put(TipoLocazione.ROVINE, d);
		locazioni.put(TipoLocazione.TEMPIO, BufferedImageBuilder.buildBufferedImage("locazioni/Tempio.gif"));

		mappa = new EnumMap<>(TipoLocazione.class);
		mappaVariantiBosco = new BufferedImage[RISORSE_VARIANTI_BOSCO.length];
		for (int i = 0; i < RISORSE_VARIANTI_BOSCO.length; i++) {
			mappaVariantiBosco[i] = BufferedImageBuilder.buildBufferedImage(RISORSE_VARIANTI_BOSCO[i]);
		}
		mappa.put(TipoLocazione.BOSCO, mappaVariantiBosco[0]);
		d = BufferedImageBuilder.buildBufferedImage("mappa/Castello.gif");
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoria() == CategoriaLocazione.CASTELLO) {
				mappa.put(tipoLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("mappa/Citta.gif");
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoria() == CategoriaLocazione.CITTA) {
				mappa.put(tipoLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("mappa/Grotta.gif");
		mappa.put(TipoLocazione.GROTTA, d);
		mappa.put(TipoLocazione.LOCANDA, BufferedImageBuilder.buildBufferedImage("mappa/Locanda.gif"));
		mappa.put(TipoLocazione.PALUDE, BufferedImageBuilder.buildBufferedImage("mappa/Palude.gif"));
		mappa.put(TipoLocazione.RADURA, BufferedImageBuilder.buildBufferedImage("mappa/Radura.gif"));
		d = BufferedImageBuilder.buildBufferedImage("mappa/Rovine.gif");
		mappa.put(TipoLocazione.ROVINE, d);
		mappa.put(TipoLocazione.TEMPIO, BufferedImageBuilder.buildBufferedImage("mappa/Tempio.gif"));
		segnalino = BufferedImageBuilder.buildBufferedImage("mappa/Segnalino.gif");
		indicatore = BufferedImageBuilder.buildBufferedImage("mappa/Indicatore.gif");

		lettere = new BufferedImage[26];
		lettere[0] = BufferedImageBuilder.buildBufferedImage("alfabeto/A.gif");
		lettere[1] = BufferedImageBuilder.buildBufferedImage("alfabeto/B.gif");
		lettere[2] = BufferedImageBuilder.buildBufferedImage("alfabeto/C.gif");
		lettere[3] = BufferedImageBuilder.buildBufferedImage("alfabeto/D.gif");
		lettere[4] = BufferedImageBuilder.buildBufferedImage("alfabeto/E.gif");
		lettere[5] = BufferedImageBuilder.buildBufferedImage("alfabeto/F.gif");
		lettere[6] = BufferedImageBuilder.buildBufferedImage("alfabeto/G.gif");
		lettere[7] = BufferedImageBuilder.buildBufferedImage("alfabeto/H.gif");
		lettere[8] = BufferedImageBuilder.buildBufferedImage("alfabeto/I.gif");
		lettere[9] = BufferedImageBuilder.buildBufferedImage("alfabeto/J.gif");
		lettere[10] = BufferedImageBuilder.buildBufferedImage("alfabeto/K.gif");
		lettere[11] = BufferedImageBuilder.buildBufferedImage("alfabeto/L.gif");
		lettere[12] = BufferedImageBuilder.buildBufferedImage("alfabeto/M.gif");
		lettere[13] = BufferedImageBuilder.buildBufferedImage("alfabeto/N.gif");
		lettere[14] = BufferedImageBuilder.buildBufferedImage("alfabeto/O.gif");
		lettere[15] = BufferedImageBuilder.buildBufferedImage("alfabeto/P.gif");
		lettere[16] = BufferedImageBuilder.buildBufferedImage("alfabeto/Q.gif");
		lettere[17] = BufferedImageBuilder.buildBufferedImage("alfabeto/R.gif");
		lettere[18] = BufferedImageBuilder.buildBufferedImage("alfabeto/S.gif");
		lettere[19] = BufferedImageBuilder.buildBufferedImage("alfabeto/T.gif");
		lettere[20] = BufferedImageBuilder.buildBufferedImage("alfabeto/U.gif");
		lettere[21] = BufferedImageBuilder.buildBufferedImage("alfabeto/V.gif");
		lettere[22] = BufferedImageBuilder.buildBufferedImage("alfabeto/W.gif");
		lettere[23] = BufferedImageBuilder.buildBufferedImage("alfabeto/X.gif");
		lettere[24] = BufferedImageBuilder.buildBufferedImage("alfabeto/Y.gif");
		lettere[25] = BufferedImageBuilder.buildBufferedImage("alfabeto/Z.gif");

		cifre = new BufferedImage[10];
		cifre[0] = BufferedImageBuilder.buildBufferedImage("alfabeto/0.gif");
		cifre[1] = BufferedImageBuilder.buildBufferedImage("alfabeto/1.gif");
		cifre[2] = BufferedImageBuilder.buildBufferedImage("alfabeto/2.gif");
		cifre[3] = BufferedImageBuilder.buildBufferedImage("alfabeto/3.gif");
		cifre[4] = BufferedImageBuilder.buildBufferedImage("alfabeto/4.gif");
		cifre[5] = BufferedImageBuilder.buildBufferedImage("alfabeto/5.gif");
		cifre[6] = BufferedImageBuilder.buildBufferedImage("alfabeto/6.gif");
		cifre[7] = BufferedImageBuilder.buildBufferedImage("alfabeto/7.gif");
		cifre[8] = BufferedImageBuilder.buildBufferedImage("alfabeto/8.gif");
		cifre[9] = BufferedImageBuilder.buildBufferedImage("alfabeto/9.gif");

		punto = BufferedImageBuilder.buildBufferedImage("alfabeto/Punto.gif");
		virgola = BufferedImageBuilder.buildBufferedImage("alfabeto/Virgola.gif");
		puntodd = BufferedImageBuilder.buildBufferedImage("alfabeto/PuntoDiDomanda.gif");
		apostrofo = BufferedImageBuilder.buildBufferedImage("alfabeto/Apostrofo.gif");
		
		spriteIncantesimi = new BufferedImage[ClasseIncantesimo.values().length];
		spriteIncantesimi[ClasseIncantesimo.ARIA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Aria-nobordo-piccolo.gif");
		spriteIncantesimi[ClasseIncantesimo.ACQUA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Acqua-nobordo-piccolo.gif");
		spriteIncantesimi[ClasseIncantesimo.TERRA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Terra-nobordo-piccolo.gif");
		spriteIncantesimi[ClasseIncantesimo.FUOCO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Fuoco-nobordo-piccolo.gif");
		spriteIncantesimi[ClasseIncantesimo.FULMINE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Fulmine-nobordo-piccolo.gif");
		spriteIncantesimi[ClasseIncantesimo.GELO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Gelo-nobordo-piccolo.gif");
		spriteIncantesimi[ClasseIncantesimo.VELENO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Veleno-nobordo-piccolo.gif");
		spriteIncantesimi[ClasseIncantesimo.MORTE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Morte-nobordo-piccolo.gif");
		spriteIncantesimi[ClasseIncantesimo.RESURREZIONE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Resurrezione-nobordo-piccolo.gif");
		spriteIncantesimi[ClasseIncantesimo.ALBA_SACRA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/AlbaSacra-nobordo-piccolo.gif");

		spriteAmicizia = BufferedImageBuilder.buildBufferedImage("icone/Amicizia-nobordo-piccolo.gif");
		spriteCombattimento = BufferedImageBuilder.buildBufferedImage("icone/Combattimento-nobordo-piccolo.gif");
		spriteMagia = BufferedImageBuilder.buildBufferedImage("icone/Incantesimo-nobordo-piccolo.gif");
		spritePozioneSalute = BufferedImageBuilder.buildBufferedImage("icone/PozioneSalute-nobordo-piccolo.gif");
		spritePozioneSaluteGrande = BufferedImageBuilder.buildBufferedImage("icone/PozioneSaluteGrande-nobordo-piccolo.gif");
		spritePozioneMagia = BufferedImageBuilder.buildBufferedImage("icone/PozioneMagia-nobordo-piccolo.gif");
		spritePozioneMagiaGrande = BufferedImageBuilder.buildBufferedImage("icone/PozioneMagiaGrande-nobordo-piccolo.gif");
		spriteMappa = BufferedImageBuilder.buildBufferedImage("icone/Mappa-nobordo-piccolo.gif");
		spriteMoneta = BufferedImageBuilder.buildBufferedImage("icone/Moneta-nobordo-piccolo.gif");
		spritePietraPreziosa = BufferedImageBuilder.buildBufferedImage("icone/PietraPreziosa-nobordo-piccolo.gif");
		spriteTempo = BufferedImageBuilder.buildBufferedImage("icone/Tempo-nobordo-piccolo.gif");
		spriteAumentoLivello = BufferedImageBuilder.buildBufferedImage("icone/AumentoLivello-nobordo-piccolo.gif");
		spriteGruppo = BufferedImageBuilder.buildBufferedImage("icone/Gruppo-nobordo-piccolo.gif");

		missioneBirra = BufferedImageBuilder.buildBufferedImage("icone/Missione-birra-grande.gif");
		missioneGallo = BufferedImageBuilder.buildBufferedImage("icone/Missione-gallo.gif");

		componenteScorrevoleFrecciaSu = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaSu.gif");
		componenteScorrevoleFrecciaGiu = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaGiu.gif");

		// Avvia il reaper in background ogni minuto
		avviaReaper(1, TimeUnit.MINUTES);
		// Inietta un thread provvederà alla chiusura del reaper allo shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Logger.log("[SHUTDOWN] Intercettata chiusura del gioco. Arresto del Reaper.");
			spegniReaper();
		}, "ImageCache-Shutdown-Cleanup"));
	}

	/**
	 * Chiamare alla chiusura del gioco per spegnere il thread di pulizia della cache in modo pulito.
	 */
	public static void spegniReaper() {
		reaperExecutor.shutdown();
	}

	static void init() {
		if (!inited) {
			// Forzo il caricamento delle immagini dei personaggi, che stanno in ClassePersonaggioImmagine
			ClassePersonaggioImmagine.values();
			// ...e quelle degli oggetti, che il motore non carica (vedi ClassiOggettoImmagine)
			ClassiOggettoImmagine.values();
			// ...e le icone, che si caricano al primo uso (vedi ClasseIcona)
			ClasseIcona.precarica();
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

	public static void set(String nomeImmagine, BufferedImage displayable) {
		imageMap.put(nomeImmagine, displayable);
	}
	
	public static BufferedImage get(String nomeImmagine) {
		return imageMap.get(nomeImmagine);
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
