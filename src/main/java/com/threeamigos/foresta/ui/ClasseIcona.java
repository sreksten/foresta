package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Locale;

public enum ClasseIcona {

	MASCHIO(Comando.MASCHIO, "icone/Maschio.gif", Sfondo.VERDE),
	FEMMINA(Comando.FEMMINA,"icone/Femmina.gif", Sfondo.VERDE),

	CENTAURO(TipoPersonaggio.CENTAURO, "icone/Centauro.gif", Sfondo.VERDE),
	EREMITA(TipoPersonaggio.EREMITA, "icone/Eremita.gif", Sfondo.VERDE),
	GIGANTE(TipoPersonaggio.GIGANTE, "icone/Gigante.gif", Sfondo.VERDE),
	GOBLIN(TipoPersonaggio.GOBLIN, "icone/Goblin.gif", Sfondo.VERDE),
	HOBGOBLIN(TipoPersonaggio.HOBGOBLIN, "icone/Hobgoblin.gif", Sfondo.VERDE),
	MINOTAURO(TipoPersonaggio.MINOTAURO, "icone/Minotauro.gif", Sfondo.VERDE),
	SCHELETRO(TipoPersonaggio.SCHELETRO, "icone/Scheletro.gif", Sfondo.VERDE),
	TITANO(TipoPersonaggio.TITANO, "icone/Titano.gif", Sfondo.VERDE),
	GUERRIERA(Comando.GUERRIERA, TipoPersonaggio.GUERRIERA, "icone/Guerriera.gif", Sfondo.VERDE),
	GUERRIERO(Comando.GUERRIERO, TipoPersonaggio.GUERRIERO, "icone/Guerriero.gif", Sfondo.VERDE),
	LADRA(Comando.LADRA, TipoPersonaggio.LADRA, "icone/Ladra.gif", Sfondo.VERDE),
	LADRO(Comando.LADRO, TipoPersonaggio.LADRO, "icone/Ladro.gif", Sfondo.VERDE),
	BARDO(Comando.BARDO, TipoPersonaggio.BARDO, "icone/Bardo.gif", Sfondo.VERDE),
	CANTASTORIE(Comando.CANTASTORIE, TipoPersonaggio.CANTASTORIE, "icone/Cantastorie.gif", Sfondo.VERDE),
	ELFA(Comando.ELFA, TipoPersonaggio.ELFA, "icone/Elfa.gif", Sfondo.VERDE),
	ELFO(Comando.ELFO, TipoPersonaggio.ELFO, "icone/Elfo.gif", Sfondo.VERDE),
	MAGA(Comando.MAGA, TipoPersonaggio.MAGA, "icone/Maga.gif", Sfondo.VERDE),
	MAGO(Comando.MAGO, TipoPersonaggio.MAGO, "icone/Mago.gif", Sfondo.VERDE),
	OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA, "icone/OmbraFiamma.gif", Sfondo.VERDE),
	VIANDANTE(TipoPersonaggio.VIANDANTE, "icone/Viandante.gif", Sfondo.VERDE),

	SINGOLO_ATTACCO(Comando.SINGOLO_ATTACCO,"icone/SingoloAttacco.gif", Sfondo.VERDE),
	COMBATTIMENTO(Comando.COMBATTIMENTO,"icone/Combattimento.gif", Sfondo.VERDE),
	INTERRUZIONE_COMBATTIMENTO(Comando.INTERRUZIONE_COMBATTIMENTO, "icone/Combattimento.gif", Sfondo.ROSSO),
	INCANTESIMO(Comando.INCANTESIMO, "icone/Incantesimo.gif", Sfondo.VERDE),
	CORRUZIONE(Comando.CORRUZIONE, "icone/Corruzione.gif", Sfondo.VERDE),
	AMICIZIA(Comando.AMICIZIA,"icone/Amicizia.gif", Sfondo.VERDE),
	FUGA(Comando.FUGA, "icone/Fuga.gif", Sfondo.ROSSO),
	PASSA_INOSSERVATO(Comando.PASSA_INOSSERVATO, "icone/PassaInosservato.gif", Sfondo.VERDE),

	DARDO_ARCANO(Comando.DARDO_ARCANO, "icone/DardoArcano.gif", Sfondo.VERDE),
	ARIA(Comando.ARIA,"icone/Aria.gif", Sfondo.VERDE),
	ACQUA(Comando.ACQUA, "icone/Acqua.gif", Sfondo.VERDE),
	TERRA(Comando.TERRA,"icone/Terra.gif", Sfondo.VERDE),
	FUOCO(Comando.FUOCO, "icone/Fuoco.gif", Sfondo.VERDE),
	FULMINE(Comando.FULMINE, "icone/Fulmine.gif", Sfondo.VERDE),
	GELO(Comando.GELO, "icone/Gelo.gif", Sfondo.VERDE),
	VELENO(Comando.VELENO, "icone/Veleno.gif", Sfondo.VERDE),
	MORTE(Comando.MORTE, "icone/Morte.gif", Sfondo.VERDE),
	RESURREZIONE(Comando.RESURREZIONE, "icone/Resurrezione.gif", Sfondo.VERDE),
	ALBA_SACRA(Comando.ALBA_SACRA, "icone/AlbaSacra.gif", Sfondo.VERDE),
	NO_INCANTESIMO(Comando.NO_INCANTESIMO, "icone/NoIncantesimo.gif", Sfondo.VERDE),

	NORD(Comando.NORD, "icone/Nord.gif", Sfondo.VERDE),
	EST(Comando.EST, "icone/Est.gif", Sfondo.VERDE),
	SUD(Comando.SUD, "icone/Sud.gif", Sfondo.VERDE),
	OVEST(Comando.OVEST, "icone/Ovest.gif", Sfondo.VERDE),

	ACCAMPAMENTO(Comando.ACCAMPAMENTO, "icone/Accampamento.gif", Sfondo.MARRONE),
	POZIONE_SALUTE(Comando.POZIONE_SALUTE, "icone/PozioneSalute.gif", Sfondo.VERDE),
	POZIONE_SALUTE_GRANDE(Comando.POZIONE_SALUTE_GRANDE, "icone/PozioneSaluteGrande.gif", Sfondo.VERDE),
	POZIONE_MAGIA(Comando.POZIONE_MAGIA, "icone/PozioneMagia.gif", Sfondo.VERDE),
	POZIONE_MAGIA_GRANDE(Comando.POZIONE_MAGIA_GRANDE, "icone/PozioneMagiaGrande.gif", Sfondo.VERDE),
	MAPPA(Comando.MAPPA, "icone/Mappa.gif", Sfondo.VERDE),
	INVENTARIO(Comando.INVENTARIO, "icone/Inventario.gif", Sfondo.VERDE),
	FLOPPY_CARICA(Comando.FLOPPY_CARICA, "icone/Floppy.gif", Sfondo.VERDE),
	FLOPPY_SALVA(Comando.FLOPPY_SALVA, "icone/Floppy.gif", Sfondo.VERDE),

	NUMERO_1(Comando.NUMERO_1, "icone/1.gif", Sfondo.VERDE),
	NUMERO_2(Comando.NUMERO_2, "icone/2.gif", Sfondo.VERDE),
	NUMERO_3(Comando.NUMERO_3, "icone/3.gif", Sfondo.VERDE),
	NUMERO_4(Comando.NUMERO_4, "icone/4.gif", Sfondo.VERDE),
	NUMERO_5(Comando.NUMERO_5, "icone/5.gif", Sfondo.VERDE),

	LOCANDA(Comando.LOCANDA, "icone/Locanda.gif", Sfondo.VERDE),
	NEGOZIO_ALCHIMISTA(Comando.NEGOZIO_ALCHIMISTA, "icone/NegozioAlchimista.gif", Sfondo.VERDE),
	NEGOZIO_ARMAIOLO(Comando.NEGOZIO_ARMAIOLO, "icone/NegozioArmaiolo.gif", Sfondo.VERDE),
	NEGOZIO_VENDITORE_DI_PERGAMENE(Comando.NEGOZIO_VENDITORE_DI_PERGAMENE, "icone/NegozioVenditoreDiPergamene.gif", Sfondo.VERDE),
	NEGOZIO_INCANTATORE(Comando.NEGOZIO_INCANTATORE, "icone/NegozioIncantatore.gif", Sfondo.VERDE),
	FUSIONE(Comando.FUSIONE, "icone/Fusione.gif", Sfondo.VERDE),
	ESCI_DA_CITTA(Comando.ESCI_DA_CITTA, "icone/EsciDaCitta.gif", Sfondo.VERDE),

	GRUPPO(Comando.GRUPPO, "icone/Gruppo.gif", Sfondo.VERDE),
	SINGOLO(Comando.SINGOLO, "icone/Singolo.gif", Sfondo.VERDE),

	SI(Comando.SI, "icone/Si.gif", Sfondo.VERDE),
	NO(Comando.NO, "icone/No.gif", Sfondo.ROSSO),
	
	ANNULLA(Comando.ANNULLA, "icone/Annulla.gif", Sfondo.VERDE),

	AIUTO(Comando.AIUTO, "icone/Aiuto.gif", Sfondo.VERDE),
	NO_AIUTO(Comando.NO_AIUTO, "icone/Aiuto.gif", Sfondo.ROSSO),
	MOSTRA_TROFEI(Comando.MOSTRA_TROFEI, "icone/Trofei.gif", Sfondo.VERDE),
	PERGAMENA(Comando.PERGAMENA, "icone/Pergamena.gif", Sfondo.GRIGIO),

	SU(Comando.SU, "icone/Su.gif", Sfondo.VERDE),
	GIU(Comando.GIU, "icone/Giu.gif", Sfondo.VERDE),
	DESTRA(Comando.DESTRA, "icone/Destra.gif", Sfondo.VERDE),
	SINISTRA(Comando.SINISTRA, "icone/Sinistra.gif", Sfondo.VERDE),

	CARTA(Comando.CARTA, "icone/Carta.gif", Sfondo.VERDE),
	FORBICE(Comando.FORBICE, "icone/Forbice.gif", Sfondo.VERDE),
	SASSO(Comando.SASSO, "icone/Sasso.gif", Sfondo.VERDE),

	RUTTOLOMEO(Comando.RUTTOLOMEO, "icone/Ruttolomeo.gif", Sfondo.VERDE),
	STORPSGORBLIN(Comando.STORPSGORBLIN, "icone/Storpsgorblin.gif", Sfondo.VERDE);

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
