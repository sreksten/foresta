package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.DimensioniRisorsa;
import com.threeamigos.foresta.ui.immagini.SfondoARiempimento;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Le icone della barra dei comandi: ognuna lega un comando (e, per i personaggi, una classe) a un'immagine e a uno sfondo, nell'ordine in cui compaiono nelle varie fasi di gioco: caricare una partita, scegliere
 * il sesso e la classe, i comandi in locazione, in combattimento, in città e così via.
 */
public enum TipoIcona {

	/* Inizio gioco */
	FLOPPY_CARICA(Comando.FLOPPY_CARICA, "icone/Floppy.png", Sfondo.VERDE),

	/* Scelta della lingua, se la piattaforma la offre: le bandiere sono segnaposto da rifare */
	LINGUA_ITALIANO(Comando.LINGUA_ITALIANO, "icone/LinguaItaliano.png", Sfondo.VERDE),
	LINGUA_INGLESE_USA(Comando.LINGUA_INGLESE_USA, "icone/LinguaIngleseUsa.png", Sfondo.VERDE),
	LINGUA_INGLESE_GB(Comando.LINGUA_INGLESE_GB, "icone/LinguaIngleseGb.png", Sfondo.VERDE),

	/* Scelta sesso del personaggio a inizio gioco */
	MASCHIO(Comando.MASCHIO, "icone/Maschio.png", Sfondo.VERDE),
	FEMMINA(Comando.FEMMINA,"icone/Femmina.png", Sfondo.VERDE),

	/* Personaggi normalmente giocabili */
	GUERRIERA(Comando.GUERRIERA, TipoPersonaggio.GUERRIERA, "icone/Guerriera.png", Sfondo.VERDE),
	GUERRIERO(Comando.GUERRIERO, TipoPersonaggio.GUERRIERO, "icone/Guerriero.png", Sfondo.VERDE),
	LADRA(Comando.LADRA, TipoPersonaggio.LADRA, "icone/Ladra.png", Sfondo.VERDE),
	LADRO(Comando.LADRO, TipoPersonaggio.LADRO, "icone/Ladro.png", Sfondo.VERDE),
	BARDO(Comando.BARDO, TipoPersonaggio.BARDO, "icone/Bardo.png", Sfondo.VERDE),
	CANTASTORIE(Comando.CANTASTORIE, TipoPersonaggio.CANTASTORIE, "icone/Cantastorie.png", Sfondo.VERDE),
	ELFA(Comando.ELFA, TipoPersonaggio.ELFA, "icone/Elfa.png", Sfondo.VERDE),
	ELFO(Comando.ELFO, TipoPersonaggio.ELFO, "icone/Elfo.png", Sfondo.VERDE),
	MAGA(Comando.MAGA, TipoPersonaggio.MAGA, "icone/Maga.png", Sfondo.VERDE),
	MAGO(Comando.MAGO, TipoPersonaggio.MAGO, "icone/Mago.png", Sfondo.VERDE),
	SACERDOTESSA(Comando.SACERDOTESSA, TipoPersonaggio.SACERDOTESSA, "icone/Sacerdotessa.png", Sfondo.VERDE),
	SACERDOTE(Comando.SACERDOTE, TipoPersonaggio.SACERDOTE, "icone/Sacerdote.png", Sfondo.VERDE),
	/* Solo in modalità di prova (la grafica è una copia di quella di guerriero e bardo) */
	CHIERICA(Comando.CHIERICA, TipoPersonaggio.CHIERICA, "icone/Chierica.png", Sfondo.VERDE),
	CHIERICO(Comando.CHIERICO, TipoPersonaggio.CHIERICO, "icone/Chierico.png", Sfondo.VERDE),
	DRUIDA(Comando.DRUIDA, TipoPersonaggio.DRUIDA, "icone/Druida.png", Sfondo.VERDE),
	DRUIDO(Comando.DRUIDO, TipoPersonaggio.DRUIDO, "icone/Druido.png", Sfondo.VERDE),
	/* Solo via trucco */
	OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA, "icone/OmbraFiamma.png", Sfondo.VERDE),
	/* Personaggi che possono essere ospitati/giocati */
	CENTAURO(TipoPersonaggio.CENTAURO, "icone/Centauro.png", Sfondo.VERDE),
	EREMITA(TipoPersonaggio.EREMITA, "icone/Eremita.png", Sfondo.VERDE),
	GIGANTE(TipoPersonaggio.GIGANTE, "icone/Gigante.png", Sfondo.VERDE),
	GOBLIN(TipoPersonaggio.GOBLIN, "icone/Goblin.png", Sfondo.VERDE),
	HOBGOBLIN(TipoPersonaggio.HOBGOBLIN, "icone/Hobgoblin.png", Sfondo.VERDE),
	MINOTAURO(TipoPersonaggio.MINOTAURO, "icone/Minotauro.png", Sfondo.VERDE),
	SCHELETRO(TipoPersonaggio.SCHELETRO, "icone/Scheletro.png", Sfondo.VERDE),
	TITANO(TipoPersonaggio.TITANO, "icone/Titano.png", Sfondo.VERDE),
	LOCANDIERE(TipoPersonaggio.LOCANDIERE, "icone/Locandiere.png", Sfondo.VERDE),
	ARMAIOLO(TipoPersonaggio.ARMAIOLO, "icone/Armaiolo.png", Sfondo.VERDE),
	ALCHIMISTA(TipoPersonaggio.ALCHIMISTA, "icone/Alchimista.png", Sfondo.VERDE),
	VENDITORE_DI_PERGAMENE(TipoPersonaggio.VENDITORE_DI_PERGAMENE, "icone/VenditoreDiPergamene.png", Sfondo.VERDE),
	INCANTATORE(TipoPersonaggio.INCANTATORE, "icone/Incantatore.png", Sfondo.VERDE),
	MOGLIE_DEL_BARDO(TipoPersonaggio.MOGLIE_DEL_BARDO, "icone/MoglieDelBardo.png", Sfondo.VERDE),
	VIANDANTE(TipoPersonaggio.VIANDANTE, "icone/Viandante.png", Sfondo.VERDE),
	BARDO_LOCANDA(TipoPersonaggio.BARDO_DELLE_LOCANDE, "icone/BardoLocanda.png", Sfondo.VERDE),
	VICHINGO(TipoPersonaggio.VICHINGO, "icone/Vichingo.png", Sfondo.VERDE),
	CAPITANO_DELLE_GUARDIE(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, "icone/CapitanoDelleGuardie.png", Sfondo.VERDE),
	UOMO(TipoPersonaggio.UOMO, "icone/Uomo.png", Sfondo.VERDE),
	DONNA(TipoPersonaggio.DONNA, "icone/Donna.png", Sfondo.VERDE),

	/* Pannello principale */
	SINGOLO_ATTACCO(Comando.SINGOLO_ATTACCO,"icone/SingoloAttacco.png", Sfondo.VERDE),
	COMBATTIMENTO(Comando.COMBATTIMENTO,"icone/Combattimento.png", Sfondo.VERDE),
	INTERRUZIONE_COMBATTIMENTO(Comando.INTERRUZIONE_COMBATTIMENTO, "icone/Combattimento.png", Sfondo.ROSSO),
	INCANTESIMO(Comando.INCANTESIMO, "icone/Incantesimo.png", Sfondo.VERDE),
	CORRUZIONE(Comando.CORRUZIONE, "icone/Corruzione.png", Sfondo.VERDE),
	AMICIZIA(Comando.AMICIZIA,"icone/Amicizia.png", Sfondo.VERDE),
	ACCAMPAMENTO(Comando.ACCAMPAMENTO, "icone/Accampamento.png", Sfondo.MARRONE),
	MAPPA(Comando.MAPPA, "icone/Mappa.png", Sfondo.VERDE),
	POZIONE_SALUTE(Comando.POZIONE_SALUTE, "icone/PozioneSalute.png", Sfondo.VERDE),
	POZIONE_SALUTE_GRANDE(Comando.POZIONE_SALUTE_GRANDE, "icone/PozioneSaluteGrande.png", Sfondo.VERDE),
	POZIONE_MAGIA(Comando.POZIONE_MAGIA, "icone/PozioneMagia.png", Sfondo.VERDE),
	POZIONE_MAGIA_GRANDE(Comando.POZIONE_MAGIA_GRANDE, "icone/PozioneMagiaGrande.png", Sfondo.VERDE),
	POZIONE_PANACEA(Comando.POZIONE_PANACEA, "icone/PozionePanacea.png", Sfondo.VERDE),
	POZIONE_AUMENTO_LIVELLO(Comando.POZIONE_AUMENTO_LIVELLO, "icone/AumentoLivello.png", Sfondo.VERDE),
	INVENTARIO(Comando.INVENTARIO, "icone/Inventario.png", Sfondo.VERDE),
	FUGA(Comando.FUGA, "icone/Fuga.png", Sfondo.ROSSO),
	PASSA_INOSSERVATO(Comando.PASSA_INOSSERVATO, "icone/PassaInosservato.png", Sfondo.VERDE),
	FLOPPY_SALVA(Comando.FLOPPY_SALVA, "icone/Floppy.png", Sfondo.VERDE),

	/* Incantesimo tipico di mago/a o elfo/a che non richiedono pergamene per essere lanciati */
	DARDO_ARCANO(Comando.DARDO_ARCANO, "icone/DardoArcano.png", Sfondo.VERDE),
	/* I canti del bardo e della cantastorie, che non richiedono pergamene */
	CANTO_CURATIVO(Comando.CANTO_CURATIVO, "icone/CantoCurativo.png", Sfondo.VERDE),
	CANTO_IPNOTICO(Comando.CANTO_IPNOTICO, "icone/CantoIpnotico.png", Sfondo.VERDE),
	CANTO_DI_VALORE(Comando.CANTO_DI_VALORE, "icone/CantoDiValore.png", Sfondo.VERDE),
	URLO_DELLA_BANSHEE(Comando.URLO_DELLA_BANSHEE, "icone/UrloDellaBanshee.png", Sfondo.VERDE),
	/* Le abilità innate di chierico e druido, che non richiedono pergamene (per ora tutte con la grafica della pergamena: da disegnare, vedi todo.md) */
	SCACCIARE_I_NON_MORTI(Comando.SCACCIARE_I_NON_MORTI, "icone/ScacciareINonMorti.png", Sfondo.VERDE),
	IMPOSIZIONE_DELLE_MANI(Comando.IMPOSIZIONE_DELLE_MANI, "icone/ImposizioneDelleMani.png", Sfondo.VERDE),
	RADICI(Comando.RADICI, "icone/Radici.png", Sfondo.VERDE),
	SCIAME(Comando.SCIAME, "icone/Sciame.png", Sfondo.VERDE),
	PELLE_DI_CORTECCIA(Comando.PELLE_DI_CORTECCIA, "icone/PelleDiCorteccia.png", Sfondo.VERDE),
	RIGENERAZIONE(Comando.RIGENERAZIONE, "icone/Rigenerazione.png", Sfondo.VERDE),
	/* Incantesimi */
	ARIA(Comando.ARIA,"icone/Aria.png", Sfondo.VERDE),
	ACQUA(Comando.ACQUA, "icone/Acqua.png", Sfondo.VERDE),
	TERRA(Comando.TERRA,"icone/Terra.png", Sfondo.VERDE),
	FUOCO(Comando.FUOCO, "icone/Fuoco.png", Sfondo.VERDE),
	FULMINE(Comando.FULMINE, "icone/Fulmine.png", Sfondo.VERDE),
	GELO(Comando.GELO, "icone/Gelo.png", Sfondo.VERDE),
	VELENO(Comando.VELENO, "icone/Veleno.png", Sfondo.VERDE),
	MORTE(Comando.MORTE, "icone/Morte.png", Sfondo.VERDE),
	RESURREZIONE(Comando.RESURREZIONE, "icone/Resurrezione.png", Sfondo.VERDE),
	ALBA_SACRA(Comando.ALBA_SACRA, "icone/AlbaSacra.png", Sfondo.VERDE),
	MARTELLO_SACRO(Comando.MARTELLO_SACRO, "icone/MartelloSacro.png", Sfondo.VERDE),
	ATTACCO_ALLE_SPALLE(Comando.ATTACCO_ALLE_SPALLE, "icone/AttaccoAlleSpalle.png", Sfondo.VERDE),
	LUCE_DIVINA(Comando.LUCE_DIVINA, "icone/LuceDivina.png", Sfondo.VERDE),
	SILENZIO(Comando.SILENZIO, "icone/Silenzio.png", Sfondo.VERDE),
	NEBBIA(Comando.NEBBIA, "icone/Nebbia.png", Sfondo.VERDE),
	NEGA_MAGIA(Comando.NEGA_MAGIA, "icone/NegaMagia.png", Sfondo.VERDE),
	GRIDO_DI_BATTAGLIA(Comando.GRIDO_DI_BATTAGLIA, "icone/GridoDiBattaglia.png", Sfondo.VERDE),
	TERREMOTO(Comando.TERREMOTO, "icone/Terremoto.png", Sfondo.VERDE),
	TEMPESTA(Comando.TEMPESTA, "icone/Tempesta.png", Sfondo.VERDE),
	TERRORE(Comando.TERRORE, "icone/Terrore.png", Sfondo.VERDE),

	/* Direzione verso la quale muoversi a fine locazione */
	NORD(Comando.NORD, "icone/Nord.png", Sfondo.VERDE),
	EST(Comando.EST, "icone/Est.png", Sfondo.VERDE),
	SUD(Comando.SUD, "icone/Sud.png", Sfondo.VERDE),
	OVEST(Comando.OVEST, "icone/Ovest.png", Sfondo.VERDE),

	/* Numero di passi / slot di salvataggio / slot di caricamento */
	NUMERO_1(Comando.NUMERO_1, "icone/1.png", Sfondo.VERDE),
	NUMERO_2(Comando.NUMERO_2, "icone/2.png", Sfondo.VERDE),
	NUMERO_3(Comando.NUMERO_3, "icone/3.png", Sfondo.VERDE),
	NUMERO_4(Comando.NUMERO_4, "icone/4.png", Sfondo.VERDE),
	NUMERO_5(Comando.NUMERO_5, "icone/5.png", Sfondo.VERDE),

	/* Comandi in città */
	LOCANDA(Comando.LOCANDA, "icone/Locanda.png", Sfondo.VERDE),
	NEGOZIO_ALCHIMISTA(Comando.NEGOZIO_ALCHIMISTA, "icone/NegozioAlchimista.png", Sfondo.VERDE),
	NEGOZIO_ARMAIOLO(Comando.NEGOZIO_ARMAIOLO, "icone/NegozioArmaiolo.png", Sfondo.VERDE),
	NEGOZIO_VENDITORE_DI_PERGAMENE(Comando.NEGOZIO_VENDITORE_DI_PERGAMENE, "icone/NegozioVenditoreDiPergamene.png", Sfondo.VERDE),
	NEGOZIO_INCANTATORE(Comando.NEGOZIO_INCANTATORE, "icone/NegozioIncantatore.png", Sfondo.VERDE),
	ESCI_DA_CITTA(Comando.ESCI_DA_CITTA, "icone/EsciDaCitta.png", Sfondo.VERDE),
	/* In negozio incantatore */
	FUSIONE(Comando.FUSIONE, "icone/Fusione.png", Sfondo.VERDE),

	/* Azione su singolo personaggio / su gruppo */
	/* Per ora disattivato, aumentava la magia dall'alchimista */
	GRUPPO(Comando.GRUPPO, "icone/Gruppo.png", Sfondo.VERDE),
	SINGOLO(Comando.SINGOLO, "icone/Singolo.png", Sfondo.VERDE),

	SI(Comando.SI, "icone/Si.png", Sfondo.VERDE),
	NO(Comando.NO, "icone/No.png", Sfondo.ROSSO),
	ANNULLA(Comando.ANNULLA, "icone/Annulla.png", Sfondo.VERDE),

	AIUTO(Comando.AIUTO, "icone/Aiuto.png", Sfondo.VERDE),
	NO_AIUTO(Comando.NO_AIUTO, "icone/Aiuto.png", Sfondo.ROSSO),
	MOSTRA_TROFEI(Comando.MOSTRA_TROFEI, "icone/Trofei.png", Sfondo.VERDE),
	// Per ora la copia dell'icona del goblin (vedi todo.md)
	MOSTRA_BESTIARIO(Comando.MOSTRA_BESTIARIO, "icone/Bestiario.png", Sfondo.VERDE),
	MOSTRA_TATTICHE(Comando.MOSTRA_TATTICHE, "icone/TatticheAttacco.png", Sfondo.VERDE),
	PERGAMENA(Comando.PERGAMENA, "icone/Pergamena.png", Sfondo.GRIGIO),

	SU(Comando.SU, "icone/Su.png", Sfondo.VERDE),
	GIU(Comando.GIU, "icone/Giu.png", Sfondo.VERDE),
	DESTRA(Comando.DESTRA, "icone/Destra.png", Sfondo.VERDE),
	SINISTRA(Comando.SINISTRA, "icone/Sinistra.png", Sfondo.VERDE),

	CARTA(Comando.CARTA, "icone/Carta.png", Sfondo.VERDE),
	FORBICE(Comando.FORBICE, "icone/Forbice.png", Sfondo.VERDE),
	SASSO(Comando.SASSO, "icone/Sasso.png", Sfondo.VERDE),

	RUTTOLOMEO(Comando.RUTTOLOMEO, "icone/Ruttolomeo.png", Sfondo.VERDE),
	STORPSGORBLIN(Comando.STORPSGORBLIN, "icone/Storpsgorblin.png", Sfondo.VERDE);

	private final Comando comando;
	private final TipoPersonaggio classePersonaggio;
	private final String nomeRisorsa;
	private final Sfondo sfondo;
	// Caricata al primo uso, o da precarica(): cosi' la dimensione della finestra si calcola senza caricarle tutte
	private volatile BufferedImage icona;
	private volatile BufferedImage iconaCapo;
	// Le icone in ricarica, per turni rimasti e durata (vedi getIconaInRicarica): poche, e si compongono la prima volta che servono, o da precarica()
	private final Map<Long, BufferedImage> iconeInRicarica = new ConcurrentHashMap<>();
	private static volatile BufferedImage corniceInRicarica;
	private static int altezzaMassima = -1;

	TipoIcona(Comando comando, String nomeRisorsa, Sfondo sfondo) {
		this.comando = comando;
		this.classePersonaggio = null;
		this.nomeRisorsa = nomeRisorsa;
		this.sfondo = sfondo;
	}

	TipoIcona(TipoPersonaggio classePersonaggio, String nomeRisorsa, Sfondo sfondo) {
		this.comando = null;
		this.classePersonaggio = classePersonaggio;
		this.nomeRisorsa = nomeRisorsa;
		this.sfondo = sfondo;
	}

	TipoIcona(Comando comando, TipoPersonaggio classePersonaggio, String nomeRisorsa, Sfondo sfondo) {
		this.comando = comando;
		this.classePersonaggio = classePersonaggio;
		this.nomeRisorsa = nomeRisorsa;
		this.sfondo = sfondo;
	}

	/**
	 * L'icona come si disegna nel pannello, a metà risoluzione e prima dello zoom: lo sfondo del colore voluto, la sagoma
	 * nera del disegno spostata di un pixel in basso e a destra (l'ombra) e poi il disegno. Il disegno sta in alto a
	 * sinistra; la dimensione è quella dello sfondo. Dove il disegno è trasparente non c'è ombra.
	 */
	public static BufferedImage componi(BufferedImage sfondo, BufferedImage disegno) {
		BufferedImage composta = new BufferedImage(sfondo.getWidth(), sfondo.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = composta.createGraphics();
		g2d.drawImage(sfondo, 0, 0, null);
		g2d.dispose();
		for (int y = 0; y < disegno.getHeight(); y++) {
			for (int x = 0; x < disegno.getWidth(); x++) {
				boolean ombraDentro = x + 1 < composta.getWidth() && y + 1 < composta.getHeight();
				if (ombraDentro && (disegno.getRGB(x, y) >>> 24) != 0) {
					composta.setRGB(x + 1, y + 1, 0xFF000000);
				}
			}
		}
		g2d = composta.createGraphics();
		g2d.drawImage(disegno, 0, 0, null);
		g2d.dispose();
		return composta;
	}

	public BufferedImage getIcona() {
		BufferedImage caricata = icona;
		if (caricata == null) {
			synchronized (this) {
				caricata = icona;
				if (caricata == null) {
					BufferedImage composta = componi(sfondo.getImmagine(), BufferedImageBuilder.buildBufferedImage(nomeRisorsa));
					caricata = BufferedImageBuilder.ingrandisci(composta, LivelloDiZoom.valore());
					icona = caricata;
				}
			}
		}
		return caricata;
	}

	/**
	 * L'icona del personaggio che è il capo del gruppo: la stessa, con lo sfondo rosso.
	 */
	public BufferedImage getIconaCapo() {
		BufferedImage caricata = iconaCapo;
		if (caricata == null) {
			synchronized (this) {
				caricata = iconaCapo;
				if (caricata == null) {
					BufferedImage composta = componi(Sfondo.ROSSO.getImmagine(), BufferedImageBuilder.buildBufferedImage(nomeRisorsa));
					caricata = BufferedImageBuilder.ingrandisci(composta, LivelloDiZoom.valore());
					iconaCapo = caricata;
				}
			}
		}
		return caricata;
	}

	/**
	 * L'icona di un comando in ricarica: lo sfondo, che si riempie dal basso con un colore che va dal rosso scuro al verde scuro man mano che passano i
	 * turni, e sopra il disegno. Non si può cliccare. Per una ricarica di {@code durata} turni, con {@code turniRimasti} ancora da aspettare, lo sfondo è
	 * pieno per (durata - turniRimasti + 1)/(durata + 1): subito dopo l'uso è un po' riempito, e a zero turni torna l'icona normale (vedi {@link #getIcona()}).
	 */
	public BufferedImage getIconaInRicarica(int turniRimasti, int durata) {
		if (turniRimasti <= 0 || durata <= 0) {
			return getIcona();
		}
		int rimasti = Math.min(turniRimasti, durata);
		return iconeInRicarica.computeIfAbsent((long) durata << 32 | rimasti, chiave -> {
			BufferedImage sfondo = SfondoARiempimento.crea(getCorniceInRicarica(), durata - rimasti + 1, durata);
			BufferedImage composta = componi(sfondo, BufferedImageBuilder.buildBufferedImage(nomeRisorsa));
			return BufferedImageBuilder.ingrandisci(composta, LivelloDiZoom.valore());
		});
	}

	private static BufferedImage getCorniceInRicarica() {
		BufferedImage caricata = corniceInRicarica;
		if (caricata == null) {
			synchronized (TipoIcona.class) {
				caricata = corniceInRicarica;
				if (caricata == null) {
					caricata = BufferedImageBuilder.buildBufferedImage("icone/Sfondo-icona-trasparente.png");
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
		for (TipoIcona corrente : values()) {
			corrente.getIcona();
		}
		for (TipoAbilita abilita : TipoAbilita.values()) {
			precaricaInRicarica(ComandiAssociati.di(abilita), abilita.getRicarica());
		}
		for (TipoCanto canto : TipoCanto.values()) {
			precaricaInRicarica(ComandiAssociati.di(canto), canto.getRicarica());
		}
	}

	private static void precaricaInRicarica(Comando comando, int durata) {
		TipoIcona icona = ofComando(comando);
		for (int rimasti = 1; rimasti <= durata; rimasti++) {
			icona.getIconaInRicarica(rimasti, durata);
		}
	}

	/**
	 * La larghezza delle icone dopo lo zoom (quella degli sfondi, anche questa dall'intestazione del file).
	 */
	public static int getLarghezzaMassima() {
		int larghezza = 0;
		for (Sfondo corrente : Sfondo.values()) {
			larghezza = Math.max(larghezza, DimensioniRisorsa.di(corrente.risorsa, LivelloDiZoom.valore()).width);
		}
		return larghezza;
	}

	public static int getAltezzaMassima() {
		if (altezzaMassima == -1) {
			for (TipoIcona corrente : values()) {
				// Dall'intestazione del file: non serve caricare le icone per saperlo
				altezzaMassima = Math.max(altezzaMassima, DimensioniRisorsa.di(corrente.sfondo.risorsa, LivelloDiZoom.valore()).height);
			}
		}
		return altezzaMassima;
	}

	/**
	 * L'icona della classe di personaggio.
	 *
	 * @throws IllegalArgumentException se quella classe non ha un'icona
	 */
	public static TipoIcona ofClasse(TipoPersonaggio classePersonaggio) {
		for (TipoIcona corrente : values()) {
			if (corrente.classePersonaggio == classePersonaggio) {
				return corrente;
			}
		}
		throw new IllegalArgumentException("TipoIcona non trovata via classe personaggio: " + classePersonaggio);
	}

	/**
	 * L'icona del comando.
	 *
	 * @throws IllegalArgumentException se quel comando non ha un'icona
	 */
	public static TipoIcona ofComando(Comando comando) {
		for (TipoIcona corrente : values()) {
			if (corrente.comando == comando) {
				return corrente;
			}
		}
		throw new IllegalArgumentException("TipoIcona non trovata via comando: " + comando);
	}

	/**
	 * Il colore di sfondo di un'icona: il file icone/Sfondo-icona-colore.png, caricato una volta sola.
	 */
	private enum Sfondo {
		VERDE, ROSSO, MARRONE, GRIGIO;

		private final String risorsa = "icone/Sfondo-icona-" + name().toLowerCase(Locale.ROOT) + ".png";
		private volatile BufferedImage immagine;

		BufferedImage getImmagine() {
			BufferedImage caricata = immagine;
			if (caricata == null) {
				synchronized (this) {
					caricata = immagine;
					if (caricata == null) {
						caricata = BufferedImageBuilder.buildBufferedImage(risorsa);
						immagine = caricata;
					}
				}
			}
			return caricata;
		}
	}

}
