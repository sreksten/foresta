package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.awt.image.BufferedImage;

public enum ClasseIcona {

	MASCHIO(Comando.MASCHIO, "icone/Maschio.gif"),
	FEMMINA(Comando.FEMMINA,"icone/Femmina.gif"),

	CENTAURO(TipoPersonaggio.CENTAURO, "icone/Centauro.gif"),
	EREMITA(TipoPersonaggio.EREMITA, "icone/Eremita.gif"),
	GIGANTE(TipoPersonaggio.GIGANTE, "icone/Gigante.gif"),
	GOBLIN(TipoPersonaggio.GOBLIN, "icone/Goblin.gif"),
	HOBGOBLIN(TipoPersonaggio.HOBGOBLIN, "icone/Hobgoblin.gif"),
	MINOTAURO(TipoPersonaggio.MINOTAURO, "icone/Minotauro.gif"),
	SCHELETRO(TipoPersonaggio.SCHELETRO, "icone/Scheletro.gif"),
	TITANO(TipoPersonaggio.TITANO, "icone/Titano.gif"),
	GUERRIERA(Comando.GUERRIERA, TipoPersonaggio.GUERRIERA, "icone/Guerriera.gif"),
	GUERRIERO(Comando.GUERRIERO, TipoPersonaggio.GUERRIERO, "icone/Guerriero.gif"),
	LADRA(Comando.LADRA, TipoPersonaggio.LADRA, "icone/Ladra.gif"),
	LADRO(Comando.LADRO, TipoPersonaggio.LADRO, "icone/Ladro.gif"),
	BARDO(Comando.BARDO, TipoPersonaggio.BARDO, "icone/Bardo.gif"),
	CANTASTORIE(Comando.CANTASTORIE, TipoPersonaggio.CANTASTORIE, "icone/Cantastorie.gif"),
	ELFA(Comando.ELFA, TipoPersonaggio.ELFA, "icone/Elfa.gif"),
	ELFO(Comando.ELFO, TipoPersonaggio.ELFO, "icone/Elfo.gif"),
	MAGA(Comando.MAGA, TipoPersonaggio.MAGA, "icone/Maga.gif"),
	MAGO(Comando.MAGO, TipoPersonaggio.MAGO, "icone/Mago.gif"),
	OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA, "icone/OmbraFiamma.gif"),
	VIANDANTE(TipoPersonaggio.VIANDANTE, "icone/Viandante.gif"),

	SINGOLO_ATTACCO(Comando.SINGOLO_ATTACCO,"icone/SingoloAttacco.gif"),
	COMBATTIMENTO(Comando.COMBATTIMENTO,"icone/Combattimento.gif"),
	INTERRUZIONE_COMBATTIMENTO(Comando.INTERRUZIONE_COMBATTIMENTO, "icone/InterruzioneCombattimento.gif"),
	INCANTESIMO(Comando.INCANTESIMO, "icone/Incantesimo.gif"),
	CORRUZIONE(Comando.CORRUZIONE, "icone/Corruzione.gif"),
	AMICIZIA(Comando.AMICIZIA,"icone/Amicizia.gif"),
	FUGA(Comando.FUGA, "icone/Fuga.gif"),
	PASSA_INOSSERVATO(Comando.PASSA_INOSSERVATO, "icone/PassaInosservato.gif"),

	DARDO_ARCANO(Comando.DARDO_ARCANO, "icone/DardoArcano.gif"),
	ARIA(Comando.ARIA,"icone/Aria.gif"),
	ACQUA(Comando.ACQUA, "icone/Acqua.gif"),
	TERRA(Comando.TERRA,"icone/Terra.gif"),
	FUOCO(Comando.FUOCO, "icone/Fuoco.gif"),
	FULMINE(Comando.FULMINE, "icone/Fulmine.gif"),
	GELO(Comando.GELO, "icone/Gelo.gif"),
	VELENO(Comando.VELENO, "icone/Veleno.gif"),
	MORTE(Comando.MORTE, "icone/Morte.gif"),
	RESURREZIONE(Comando.RESURREZIONE, "icone/Resurrezione.gif"),
	ALBA_SACRA(Comando.ALBA_SACRA, "icone/AlbaSacra.gif"),
	NO_INCANTESIMO(Comando.NO_INCANTESIMO, "icone/NoIncantesimo.gif"),

	NORD(Comando.NORD, "icone/Nord.gif"),
	EST(Comando.EST, "icone/Est.gif"),
	SUD(Comando.SUD, "icone/Sud.gif"),
	OVEST(Comando.OVEST, "icone/Ovest.gif"),

	ACCAMPAMENTO(Comando.ACCAMPAMENTO, "icone/Accampamento.gif"),
	POZIONE_SALUTE(Comando.POZIONE_SALUTE, "icone/PozioneSalute.gif"),
	POZIONE_SALUTE_GRANDE(Comando.POZIONE_SALUTE_GRANDE, "icone/PozioneSaluteGrande.gif"),
	POZIONE_MAGIA(Comando.POZIONE_MAGIA, "icone/PozioneMagia.gif"),
	POZIONE_MAGIA_GRANDE(Comando.POZIONE_MAGIA_GRANDE, "icone/PozioneMagiaGrande.gif"),
	MAPPA(Comando.MAPPA, "icone/Mappa.gif"),
	INVENTARIO(Comando.INVENTARIO, "icone/Inventario.gif"),
	FLOPPY_CARICA(Comando.FLOPPY_CARICA, "icone/Floppy.gif"),
	FLOPPY_SALVA(Comando.FLOPPY_SALVA, "icone/Floppy.gif"),

	NUMERO_1(Comando.NUMERO_1, "icone/1.gif"),
	NUMERO_2(Comando.NUMERO_2, "icone/2.gif"),
	NUMERO_3(Comando.NUMERO_3, "icone/3.gif"),
	NUMERO_4(Comando.NUMERO_4, "icone/4.gif"),
	NUMERO_5(Comando.NUMERO_5, "icone/5.gif"),

	LOCANDA(Comando.LOCANDA, "icone/Locanda.gif"),
	ALCHIMISTA(Comando.ALCHIMISTA, "icone/Alchimista.gif"),
	ARMAIOLO(Comando.ARMAIOLO, "icone/Armaiolo.gif"),
	VENDITORE_DI_PERGAMENE(Comando.VENDITORE_DI_PERGAMENE, "icone/VenditoreDiPergamene.gif"),
	INCANTATORE(Comando.INCANTATORE, "icone/Incantatore.gif"),
	FUSIONE(Comando.FUSIONE, "icone/Fusione.gif"),
	ESCI_DA_CITTA(Comando.ESCI_DA_CITTA, "icone/EsciDaCitta.gif"),

	GRUPPO(Comando.GRUPPO, "icone/Gruppo.gif"),
	SINGOLO(Comando.SINGOLO, "icone/Singolo.gif"),

	SI(Comando.SI, "icone/Si.gif"),
	NO(Comando.NO, "icone/No.gif"),
	
	ANNULLA(Comando.ANNULLA, "icone/Annulla.gif"),

	AIUTO(Comando.AIUTO, "icone/Aiuto.gif"),
	NO_AIUTO(Comando.NO_AIUTO, "icone/NoAiuto.gif"),
	MOSTRA_TROFEI(Comando.MOSTRA_TROFEI, "icone/Trofei.gif"),
	PERGAMENA(Comando.PERGAMENA, "icone/Pergamena.gif"),

	SU(Comando.SU, "icone/Su.gif"),
	GIU(Comando.GIU, "icone/Giu.gif"),
	DESTRA(Comando.DESTRA, "icone/Destra.gif"),
	SINISTRA(Comando.SINISTRA, "icone/Sinistra.gif"),

	CARTA(Comando.CARTA, "icone/Carta.gif"),
	FORBICE(Comando.FORBICE, "icone/Forbice.gif"),
	SASSO(Comando.SASSO, "icone/Sasso.gif"),

	RUTTOLOMEO(Comando.RUTTOLOMEO, "icone/Ruttolomeo.gif"),
	STORPSGORBLIN(Comando.STORPSGORBLIN, "icone/Storpsgorblin.gif");

	private final Comando comando;
	private final TipoPersonaggio classePersonaggio;
	private final String nomeRisorsa;
	// Caricata al primo uso, o da precarica(): cosi' la dimensione della finestra si calcola senza caricarle tutte
	private volatile BufferedImage icona;
	private static int altezzaMassima = -1;

	ClasseIcona(Comando comando, String nomeRisorsa) {
		this.comando = comando;
		this.classePersonaggio = null;
		this.nomeRisorsa = nomeRisorsa;
	}

	ClasseIcona(TipoPersonaggio classePersonaggio, String nomeRisorsa) {
		this.comando = null;
		this.classePersonaggio = classePersonaggio;
		this.nomeRisorsa = nomeRisorsa;
	}

	ClasseIcona(Comando comando, TipoPersonaggio classePersonaggio, String nomeRisorsa) {
		this.comando = comando;
		this.classePersonaggio = classePersonaggio;
		this.nomeRisorsa = nomeRisorsa;
	}

	public BufferedImage getIcona() {
		BufferedImage caricata = icona;
		if (caricata == null) {
			synchronized (this) {
				caricata = icona;
				if (caricata == null) {
					caricata = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
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

	public static int getAltezzaMassima() {
		if (altezzaMassima == -1) {
			for (ClasseIcona corrente : values()) {
				// Dall'intestazione del file: non serve caricare le icone per saperlo
				altezzaMassima = Math.max(altezzaMassima, DimensioniRisorsa.di(corrente.nomeRisorsa).height);
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

}
