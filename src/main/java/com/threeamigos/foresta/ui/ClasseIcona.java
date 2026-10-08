package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Locale;

public enum ClasseIcona {

	MASCHIO(Comando.MASCHIO, "icone/Maschio-nobordo.gif", Sfondo.VERDE),
	FEMMINA(Comando.FEMMINA,"icone/Femmina-nobordo.gif", Sfondo.VERDE),

	CENTAURO(TipoPersonaggio.CENTAURO, "icone/Centauro-nobordo.gif", Sfondo.VERDE),
	EREMITA(TipoPersonaggio.EREMITA, "icone/Eremita-nobordo.gif", Sfondo.VERDE),
	GIGANTE(TipoPersonaggio.GIGANTE, "icone/Gigante-nobordo.gif", Sfondo.VERDE),
	GOBLIN(TipoPersonaggio.GOBLIN, "icone/Goblin-nobordo.gif", Sfondo.VERDE),
	HOBGOBLIN(TipoPersonaggio.HOBGOBLIN, "icone/Hobgoblin-nobordo.gif", Sfondo.VERDE),
	MINOTAURO(TipoPersonaggio.MINOTAURO, "icone/Minotauro-nobordo.gif", Sfondo.VERDE),
	SCHELETRO(TipoPersonaggio.SCHELETRO, "icone/Scheletro-nobordo.gif", Sfondo.VERDE),
	TITANO(TipoPersonaggio.TITANO, "icone/Titano-nobordo.gif", Sfondo.VERDE),
	GUERRIERA(Comando.GUERRIERA, TipoPersonaggio.GUERRIERA, "icone/Guerriera-nobordo.gif", Sfondo.VERDE),
	GUERRIERO(Comando.GUERRIERO, TipoPersonaggio.GUERRIERO, "icone/Guerriero-nobordo.gif", Sfondo.VERDE),
	LADRA(Comando.LADRA, TipoPersonaggio.LADRA, "icone/Ladra-nobordo.gif", Sfondo.VERDE),
	LADRO(Comando.LADRO, TipoPersonaggio.LADRO, "icone/Ladro-nobordo.gif", Sfondo.VERDE),
	BARDO(Comando.BARDO, TipoPersonaggio.BARDO, "icone/Bardo-nobordo.gif", Sfondo.VERDE),
	CANTASTORIE(Comando.CANTASTORIE, TipoPersonaggio.CANTASTORIE, "icone/Cantastorie-nobordo.gif", Sfondo.VERDE),
	ELFA(Comando.ELFA, TipoPersonaggio.ELFA, "icone/Elfa-nobordo.gif", Sfondo.VERDE),
	ELFO(Comando.ELFO, TipoPersonaggio.ELFO, "icone/Elfo-nobordo.gif", Sfondo.VERDE),
	MAGA(Comando.MAGA, TipoPersonaggio.MAGA, "icone/Maga-nobordo.gif", Sfondo.VERDE),
	MAGO(Comando.MAGO, TipoPersonaggio.MAGO, "icone/Mago-nobordo.gif", Sfondo.VERDE),
	OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA, "icone/OmbraFiamma-nobordo.gif", Sfondo.VERDE),
	VIANDANTE(TipoPersonaggio.VIANDANTE, "icone/Viandante-nobordo.gif", Sfondo.VERDE),

	SINGOLO_ATTACCO(Comando.SINGOLO_ATTACCO,"icone/SingoloAttacco-nobordo.gif", Sfondo.VERDE),
	COMBATTIMENTO(Comando.COMBATTIMENTO,"icone/Combattimento-nobordo.gif", Sfondo.VERDE),
	INTERRUZIONE_COMBATTIMENTO(Comando.INTERRUZIONE_COMBATTIMENTO, "icone/Combattimento-nobordo.gif", Sfondo.ROSSO),
	INCANTESIMO(Comando.INCANTESIMO, "icone/Incantesimo-nobordo.gif", Sfondo.VERDE),
	CORRUZIONE(Comando.CORRUZIONE, "icone/Corruzione-nobordo.gif", Sfondo.VERDE),
	AMICIZIA(Comando.AMICIZIA,"icone/Amicizia-nobordo.gif", Sfondo.VERDE),
	FUGA(Comando.FUGA, "icone/Fuga-nobordo.gif", Sfondo.ROSSO),
	PASSA_INOSSERVATO(Comando.PASSA_INOSSERVATO, "icone/PassaInosservato-nobordo.gif", Sfondo.VERDE),

	DARDO_ARCANO(Comando.DARDO_ARCANO, "icone/DardoArcano-nobordo.gif", Sfondo.VERDE),
	ARIA(Comando.ARIA,"icone/Aria-nobordo.gif", Sfondo.VERDE),
	ACQUA(Comando.ACQUA, "icone/Acqua-nobordo.gif", Sfondo.VERDE),
	TERRA(Comando.TERRA,"icone/Terra-nobordo.gif", Sfondo.VERDE),
	FUOCO(Comando.FUOCO, "icone/Fuoco-nobordo.gif", Sfondo.VERDE),
	FULMINE(Comando.FULMINE, "icone/Fulmine-nobordo.gif", Sfondo.VERDE),
	GELO(Comando.GELO, "icone/Gelo-nobordo.gif", Sfondo.VERDE),
	VELENO(Comando.VELENO, "icone/Veleno-nobordo.gif", Sfondo.VERDE),
	MORTE(Comando.MORTE, "icone/Morte-nobordo.gif", Sfondo.VERDE),
	RESURREZIONE(Comando.RESURREZIONE, "icone/Resurrezione-nobordo.gif", Sfondo.VERDE),
	ALBA_SACRA(Comando.ALBA_SACRA, "icone/AlbaSacra-nobordo.gif", Sfondo.VERDE),
	NO_INCANTESIMO(Comando.NO_INCANTESIMO, "icone/NoIncantesimo-nobordo.gif", Sfondo.VERDE),

	NORD(Comando.NORD, "icone/Nord-nobordo.gif", Sfondo.VERDE),
	EST(Comando.EST, "icone/Est-nobordo.gif", Sfondo.VERDE),
	SUD(Comando.SUD, "icone/Sud-nobordo.gif", Sfondo.VERDE),
	OVEST(Comando.OVEST, "icone/Ovest-nobordo.gif", Sfondo.VERDE),

	ACCAMPAMENTO(Comando.ACCAMPAMENTO, "icone/Accampamento-nobordo.gif", Sfondo.MARRONE),
	POZIONE_SALUTE(Comando.POZIONE_SALUTE, "icone/PozioneSalute-nobordo.gif", Sfondo.VERDE),
	POZIONE_SALUTE_GRANDE(Comando.POZIONE_SALUTE_GRANDE, "icone/PozioneSaluteGrande-nobordo.gif", Sfondo.VERDE),
	POZIONE_MAGIA(Comando.POZIONE_MAGIA, "icone/PozioneMagia-nobordo.gif", Sfondo.VERDE),
	POZIONE_MAGIA_GRANDE(Comando.POZIONE_MAGIA_GRANDE, "icone/PozioneMagiaGrande-nobordo.gif", Sfondo.VERDE),
	MAPPA(Comando.MAPPA, "icone/Mappa-nobordo.gif", Sfondo.VERDE),
	INVENTARIO(Comando.INVENTARIO, "icone/Inventario-nobordo.gif", Sfondo.VERDE),
	FLOPPY_CARICA(Comando.FLOPPY_CARICA, "icone/Floppy-nobordo.gif", Sfondo.VERDE),
	FLOPPY_SALVA(Comando.FLOPPY_SALVA, "icone/Floppy-nobordo.gif", Sfondo.VERDE),

	NUMERO_1(Comando.NUMERO_1, "icone/1-nobordo.gif", Sfondo.VERDE),
	NUMERO_2(Comando.NUMERO_2, "icone/2-nobordo.gif", Sfondo.VERDE),
	NUMERO_3(Comando.NUMERO_3, "icone/3-nobordo.gif", Sfondo.VERDE),
	NUMERO_4(Comando.NUMERO_4, "icone/4-nobordo.gif", Sfondo.VERDE),
	NUMERO_5(Comando.NUMERO_5, "icone/5-nobordo.gif", Sfondo.VERDE),

	LOCANDA(Comando.LOCANDA, "icone/Locanda-nobordo.gif", Sfondo.VERDE),
	ALCHIMISTA(Comando.ALCHIMISTA, "icone/Alchimista-nobordo.gif", Sfondo.VERDE),
	ARMAIOLO(Comando.ARMAIOLO, "icone/Armaiolo-nobordo.gif", Sfondo.VERDE),
	VENDITORE_DI_PERGAMENE(Comando.VENDITORE_DI_PERGAMENE, "icone/VenditoreDiPergamene-nobordo.gif", Sfondo.VERDE),
	INCANTATORE(Comando.INCANTATORE, "icone/Incantatore-nobordo.gif", Sfondo.VERDE),
	FUSIONE(Comando.FUSIONE, "icone/Fusione-nobordo.gif", Sfondo.VERDE),
	ESCI_DA_CITTA(Comando.ESCI_DA_CITTA, "icone/EsciDaCitta-nobordo.gif", Sfondo.VERDE),

	GRUPPO(Comando.GRUPPO, "icone/Gruppo-nobordo.gif", Sfondo.VERDE),
	SINGOLO(Comando.SINGOLO, "icone/Singolo-nobordo.gif", Sfondo.VERDE),

	SI(Comando.SI, "icone/Si-nobordo.gif", Sfondo.VERDE),
	NO(Comando.NO, "icone/No-nobordo.gif", Sfondo.ROSSO),
	
	ANNULLA(Comando.ANNULLA, "icone/Annulla-nobordo.gif", Sfondo.VERDE),

	AIUTO(Comando.AIUTO, "icone/Aiuto-nobordo.gif", Sfondo.VERDE),
	NO_AIUTO(Comando.NO_AIUTO, "icone/Aiuto-nobordo.gif", Sfondo.ROSSO),
	MOSTRA_TROFEI(Comando.MOSTRA_TROFEI, "icone/Trofei-nobordo.gif", Sfondo.VERDE),
	PERGAMENA(Comando.PERGAMENA, "icone/Pergamena-nobordo.gif", Sfondo.GRIGIO),

	SU(Comando.SU, "icone/Su-nobordo.gif", Sfondo.VERDE),
	GIU(Comando.GIU, "icone/Giu-nobordo.gif", Sfondo.VERDE),
	DESTRA(Comando.DESTRA, "icone/Destra-nobordo.gif", Sfondo.VERDE),
	SINISTRA(Comando.SINISTRA, "icone/Sinistra-nobordo.gif", Sfondo.VERDE),

	CARTA(Comando.CARTA, "icone/Carta-nobordo.gif", Sfondo.VERDE),
	FORBICE(Comando.FORBICE, "icone/Forbice-nobordo.gif", Sfondo.VERDE),
	SASSO(Comando.SASSO, "icone/Sasso-nobordo.gif", Sfondo.VERDE),

	RUTTOLOMEO(Comando.RUTTOLOMEO, "icone/Ruttolomeo-nobordo.gif", Sfondo.VERDE),
	STORPSGORBLIN(Comando.STORPSGORBLIN, "icone/Storpsgorblin-nobordo.gif", Sfondo.VERDE);

	private final Comando comando;
	private final TipoPersonaggio classePersonaggio;
	private final String nomeRisorsa;
	private final Sfondo sfondo;
	// Caricata al primo uso, o da precarica(): cosi' la dimensione della finestra si calcola senza caricarle tutte
	private volatile BufferedImage icona;
	private static int altezzaMassima = -1;

	ClasseIcona(Comando comando, String nomeRisorsa, Sfondo sfondo) {
		this.comando = comando;
		this.classePersonaggio = null;
		this.nomeRisorsa = nomeRisorsa;
		this.sfondo = sfondo;
	}

	ClasseIcona(TipoPersonaggio classePersonaggio, String nomeRisorsa, Sfondo sfondo) {
		this.comando = null;
		this.classePersonaggio = classePersonaggio;
		this.nomeRisorsa = nomeRisorsa;
		this.sfondo = sfondo;
	}

	ClasseIcona(Comando comando, TipoPersonaggio classePersonaggio, String nomeRisorsa, Sfondo sfondo) {
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
	static BufferedImage componi(BufferedImage sfondo, BufferedImage disegno) {
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
	 * Carica subito tutte le icone (durante il logo iniziale, in background).
	 */
	static void precarica() {
		for (ClasseIcona corrente : values()) {
			corrente.getIcona();
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
			for (ClasseIcona corrente : values()) {
				// Dall'intestazione del file: non serve caricare le icone per saperlo
				altezzaMassima = Math.max(altezzaMassima, DimensioniRisorsa.di(corrente.sfondo.risorsa, LivelloDiZoom.valore()).height);
			}
		}
		return altezzaMassima;
	}

	public static ClasseIcona ofClasse(TipoPersonaggio classePersonaggio) {
		for (ClasseIcona corrente : values()) {
			if (corrente.classePersonaggio == classePersonaggio) {
				return corrente;
			}
		}
		throw new IllegalArgumentException("ClasseIcona non trovata via classe personaggio: " + classePersonaggio);
	}

	public static ClasseIcona ofComando(Comando comando) {
		for (ClasseIcona corrente : values()) {
			if (corrente.comando == comando) {
				return corrente;
			}
		}
		throw new IllegalArgumentException("ClasseIcona non trovata via comando: " + comando);
	}

	/**
	 * Il colore di sfondo di un'icona: il file icone/Sfondo-icona-colore.gif, caricato una volta sola.
	 */
	private enum Sfondo {
		VERDE, ROSSO, MARRONE, GRIGIO;

		private final String risorsa = "icone/Sfondo-icona-" + name().toLowerCase(Locale.ROOT) + ".gif";
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
