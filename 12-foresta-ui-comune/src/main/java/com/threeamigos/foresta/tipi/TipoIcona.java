package com.threeamigos.foresta.tipi;

import java.util.Locale;

/**
 * Le icone della barra dei comandi: ognuna lega un comando (e, per i personaggi, una classe) a un'immagine e a uno sfondo, nell'ordine in cui compaiono nelle varie fasi di gioco: caricare una partita, scegliere
 * il sesso e la classe, i comandi in locazione, in combattimento, in città e così via. Qui c'è solo la tabella: come si compone l'icona (sfondo, ombra, disegno) lo dice
 * {@link com.threeamigos.foresta.ui.immagini.ComposizioneDelleIcone}, e chi disegna (Swing, libGDX) ne tiene le immagini.
 */
public enum TipoIcona {

	/* Inizio gioco */
	FLOPPY_CARICA(Comando.FLOPPY_CARICA, "icone/Floppy.png", SfondoIcona.VERDE),

	/* Scelta della lingua, se la piattaforma la offre: le bandiere sono segnaposto da rifare */
	LINGUA_ITALIANO(Comando.LINGUA_ITALIANO, "icone/LinguaItaliano.png", SfondoIcona.VERDE),
	LINGUA_INGLESE_USA(Comando.LINGUA_INGLESE_USA, "icone/LinguaIngleseUsa.png", SfondoIcona.VERDE),
	LINGUA_INGLESE_GB(Comando.LINGUA_INGLESE_GB, "icone/LinguaIngleseGb.png", SfondoIcona.VERDE),

	/* Scelta sesso del personaggio a inizio gioco */
	MASCHIO(Comando.MASCHIO, "icone/Maschio.png", SfondoIcona.VERDE),
	FEMMINA(Comando.FEMMINA,"icone/Femmina.png", SfondoIcona.VERDE),

	/* Personaggi normalmente giocabili */
	GUERRIERA(Comando.GUERRIERA, TipoPersonaggio.GUERRIERA, "icone/Guerriera.png", SfondoIcona.VERDE),
	GUERRIERO(Comando.GUERRIERO, TipoPersonaggio.GUERRIERO, "icone/Guerriero.png", SfondoIcona.VERDE),
	LADRA(Comando.LADRA, TipoPersonaggio.LADRA, "icone/Ladra.png", SfondoIcona.VERDE),
	LADRO(Comando.LADRO, TipoPersonaggio.LADRO, "icone/Ladro.png", SfondoIcona.VERDE),
	BARDO(Comando.BARDO, TipoPersonaggio.BARDO, "icone/Bardo.png", SfondoIcona.VERDE),
	CANTASTORIE(Comando.CANTASTORIE, TipoPersonaggio.CANTASTORIE, "icone/Cantastorie.png", SfondoIcona.VERDE),
	ELFA(Comando.ELFA, TipoPersonaggio.ELFA, "icone/Elfa.png", SfondoIcona.VERDE),
	ELFO(Comando.ELFO, TipoPersonaggio.ELFO, "icone/Elfo.png", SfondoIcona.VERDE),
	MAGA(Comando.MAGA, TipoPersonaggio.MAGA, "icone/Maga.png", SfondoIcona.VERDE),
	MAGO(Comando.MAGO, TipoPersonaggio.MAGO, "icone/Mago.png", SfondoIcona.VERDE),
	SACERDOTESSA(Comando.SACERDOTESSA, TipoPersonaggio.SACERDOTESSA, "icone/Sacerdotessa.png", SfondoIcona.VERDE),
	SACERDOTE(Comando.SACERDOTE, TipoPersonaggio.SACERDOTE, "icone/Sacerdote.png", SfondoIcona.VERDE),
	/* Solo in modalità di prova (la grafica è una copia di quella di guerriero e bardo) */
	CHIERICA(Comando.CHIERICA, TipoPersonaggio.CHIERICA, "icone/Chierica.png", SfondoIcona.VERDE),
	CHIERICO(Comando.CHIERICO, TipoPersonaggio.CHIERICO, "icone/Chierico.png", SfondoIcona.VERDE),
	DRUIDA(Comando.DRUIDA, TipoPersonaggio.DRUIDA, "icone/Druida.png", SfondoIcona.VERDE),
	DRUIDO(Comando.DRUIDO, TipoPersonaggio.DRUIDO, "icone/Druido.png", SfondoIcona.VERDE),
	/* Solo via trucco */
	OMBRAFIAMMA(TipoPersonaggio.OMBRAFIAMMA, "icone/OmbraFiamma.png", SfondoIcona.VERDE),
	/* Personaggi che possono essere ospitati/giocati */
	CENTAURO(TipoPersonaggio.CENTAURO, "icone/Centauro.png", SfondoIcona.VERDE),
	EREMITA(TipoPersonaggio.EREMITA, "icone/Eremita.png", SfondoIcona.VERDE),
	GIGANTE(TipoPersonaggio.GIGANTE, "icone/Gigante.png", SfondoIcona.VERDE),
	GOBLIN(TipoPersonaggio.GOBLIN, "icone/Goblin.png", SfondoIcona.VERDE),
	HOBGOBLIN(TipoPersonaggio.HOBGOBLIN, "icone/Hobgoblin.png", SfondoIcona.VERDE),
	MINOTAURO(TipoPersonaggio.MINOTAURO, "icone/Minotauro.png", SfondoIcona.VERDE),
	SCHELETRO(TipoPersonaggio.SCHELETRO, "icone/Scheletro.png", SfondoIcona.VERDE),
	TITANO(TipoPersonaggio.TITANO, "icone/Titano.png", SfondoIcona.VERDE),
	LOCANDIERE(TipoPersonaggio.LOCANDIERE, "icone/Locandiere.png", SfondoIcona.VERDE),
	ARMAIOLO(TipoPersonaggio.ARMAIOLO, "icone/Armaiolo.png", SfondoIcona.VERDE),
	ALCHIMISTA(TipoPersonaggio.ALCHIMISTA, "icone/Alchimista.png", SfondoIcona.VERDE),
	VENDITORE_DI_PERGAMENE(TipoPersonaggio.VENDITORE_DI_PERGAMENE, "icone/VenditoreDiPergamene.png", SfondoIcona.VERDE),
	INCANTATORE(TipoPersonaggio.INCANTATORE, "icone/Incantatore.png", SfondoIcona.VERDE),
	MOGLIE_DEL_BARDO(TipoPersonaggio.MOGLIE_DEL_BARDO, "icone/MoglieDelBardo.png", SfondoIcona.VERDE),
	VIANDANTE(TipoPersonaggio.VIANDANTE, "icone/Viandante.png", SfondoIcona.VERDE),
	BARDO_LOCANDA(TipoPersonaggio.BARDO_DELLE_LOCANDE, "icone/BardoLocanda.png", SfondoIcona.VERDE),
	VICHINGO(TipoPersonaggio.VICHINGO, "icone/Vichingo.png", SfondoIcona.VERDE),
	CAPITANO_DELLE_GUARDIE(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, "icone/CapitanoDelleGuardie.png", SfondoIcona.VERDE),
	UOMO(TipoPersonaggio.UOMO, "icone/Uomo.png", SfondoIcona.VERDE),
	DONNA(TipoPersonaggio.DONNA, "icone/Donna.png", SfondoIcona.VERDE),

	/* Pannello principale */
	SINGOLO_ATTACCO(Comando.SINGOLO_ATTACCO,"icone/SingoloAttacco.png", SfondoIcona.VERDE),
	COMBATTIMENTO(Comando.COMBATTIMENTO,"icone/Combattimento.png", SfondoIcona.VERDE),
	INTERRUZIONE_COMBATTIMENTO(Comando.INTERRUZIONE_COMBATTIMENTO, "icone/Combattimento.png", SfondoIcona.ROSSO),
	INCANTESIMO(Comando.INCANTESIMO, "icone/Incantesimo.png", SfondoIcona.VERDE),
	CORRUZIONE(Comando.CORRUZIONE, "icone/Corruzione.png", SfondoIcona.VERDE),
	AMICIZIA(Comando.AMICIZIA,"icone/Amicizia.png", SfondoIcona.VERDE),
	ACCAMPAMENTO(Comando.ACCAMPAMENTO, "icone/Accampamento.png", SfondoIcona.MARRONE),
	MAPPA(Comando.MAPPA, "icone/Mappa.png", SfondoIcona.VERDE),
	POZIONE_SALUTE(Comando.POZIONE_SALUTE, "icone/PozioneSalute.png", SfondoIcona.VERDE),
	POZIONE_SALUTE_GRANDE(Comando.POZIONE_SALUTE_GRANDE, "icone/PozioneSaluteGrande.png", SfondoIcona.VERDE),
	POZIONE_MAGIA(Comando.POZIONE_MAGIA, "icone/PozioneMagia.png", SfondoIcona.VERDE),
	POZIONE_MAGIA_GRANDE(Comando.POZIONE_MAGIA_GRANDE, "icone/PozioneMagiaGrande.png", SfondoIcona.VERDE),
	POZIONE_PANACEA(Comando.POZIONE_PANACEA, "icone/PozionePanacea.png", SfondoIcona.VERDE),
	POZIONE_AUMENTO_LIVELLO(Comando.POZIONE_AUMENTO_LIVELLO, "icone/AumentoLivello.png", SfondoIcona.VERDE),
	INVENTARIO(Comando.INVENTARIO, "icone/Inventario.png", SfondoIcona.VERDE),
	FUGA(Comando.FUGA, "icone/Fuga.png", SfondoIcona.ROSSO),
	PASSA_INOSSERVATO(Comando.PASSA_INOSSERVATO, "icone/PassaInosservato.png", SfondoIcona.VERDE),
	FLOPPY_SALVA(Comando.FLOPPY_SALVA, "icone/Floppy.png", SfondoIcona.VERDE),

	/* Incantesimo tipico di mago/a o elfo/a che non richiedono pergamene per essere lanciati */
	DARDO_ARCANO(Comando.DARDO_ARCANO, "icone/DardoArcano.png", SfondoIcona.VERDE),
	/* I canti del bardo e della cantastorie, che non richiedono pergamene */
	CANTO_CURATIVO(Comando.CANTO_CURATIVO, "icone/CantoCurativo.png", SfondoIcona.VERDE),
	CANTO_IPNOTICO(Comando.CANTO_IPNOTICO, "icone/CantoIpnotico.png", SfondoIcona.VERDE),
	CANTO_DI_VALORE(Comando.CANTO_DI_VALORE, "icone/CantoDiValore.png", SfondoIcona.VERDE),
	URLO_DELLA_BANSHEE(Comando.URLO_DELLA_BANSHEE, "icone/UrloDellaBanshee.png", SfondoIcona.VERDE),
	/* Le abilità innate di chierico e druido, che non richiedono pergamene (per ora tutte con la grafica della pergamena: da disegnare, vedi todo.md) */
	SCACCIARE_I_NON_MORTI(Comando.SCACCIARE_I_NON_MORTI, "icone/ScacciareINonMorti.png", SfondoIcona.VERDE),
	IMPOSIZIONE_DELLE_MANI(Comando.IMPOSIZIONE_DELLE_MANI, "icone/ImposizioneDelleMani.png", SfondoIcona.VERDE),
	RADICI(Comando.RADICI, "icone/Radici.png", SfondoIcona.VERDE),
	SCIAME(Comando.SCIAME, "icone/Sciame.png", SfondoIcona.VERDE),
	PELLE_DI_CORTECCIA(Comando.PELLE_DI_CORTECCIA, "icone/PelleDiCorteccia.png", SfondoIcona.VERDE),
	RIGENERAZIONE(Comando.RIGENERAZIONE, "icone/Rigenerazione.png", SfondoIcona.VERDE),
	/* Incantesimi */
	ARIA(Comando.ARIA,"icone/Aria.png", SfondoIcona.VERDE),
	ACQUA(Comando.ACQUA, "icone/Acqua.png", SfondoIcona.VERDE),
	TERRA(Comando.TERRA,"icone/Terra.png", SfondoIcona.VERDE),
	FUOCO(Comando.FUOCO, "icone/Fuoco.png", SfondoIcona.VERDE),
	FULMINE(Comando.FULMINE, "icone/Fulmine.png", SfondoIcona.VERDE),
	GELO(Comando.GELO, "icone/Gelo.png", SfondoIcona.VERDE),
	VELENO(Comando.VELENO, "icone/Veleno.png", SfondoIcona.VERDE),
	MORTE(Comando.MORTE, "icone/Morte.png", SfondoIcona.VERDE),
	RESURREZIONE(Comando.RESURREZIONE, "icone/Resurrezione.png", SfondoIcona.VERDE),
	ALBA_SACRA(Comando.ALBA_SACRA, "icone/AlbaSacra.png", SfondoIcona.VERDE),
	MARTELLO_SACRO(Comando.MARTELLO_SACRO, "icone/MartelloSacro.png", SfondoIcona.VERDE),
	ATTACCO_ALLE_SPALLE(Comando.ATTACCO_ALLE_SPALLE, "icone/AttaccoAlleSpalle.png", SfondoIcona.VERDE),
	LUCE_DIVINA(Comando.LUCE_DIVINA, "icone/LuceDivina.png", SfondoIcona.VERDE),
	SILENZIO(Comando.SILENZIO, "icone/Silenzio.png", SfondoIcona.VERDE),
	NEBBIA(Comando.NEBBIA, "icone/Nebbia.png", SfondoIcona.VERDE),
	NEGA_MAGIA(Comando.NEGA_MAGIA, "icone/NegaMagia.png", SfondoIcona.VERDE),
	GRIDO_DI_BATTAGLIA(Comando.GRIDO_DI_BATTAGLIA, "icone/GridoDiBattaglia.png", SfondoIcona.VERDE),
	TERREMOTO(Comando.TERREMOTO, "icone/Terremoto.png", SfondoIcona.VERDE),
	TEMPESTA(Comando.TEMPESTA, "icone/Tempesta.png", SfondoIcona.VERDE),
	TERRORE(Comando.TERRORE, "icone/Terrore.png", SfondoIcona.VERDE),

	/* Direzione verso la quale muoversi a fine locazione */
	NORD(Comando.NORD, "icone/Nord.png", SfondoIcona.VERDE),
	EST(Comando.EST, "icone/Est.png", SfondoIcona.VERDE),
	SUD(Comando.SUD, "icone/Sud.png", SfondoIcona.VERDE),
	OVEST(Comando.OVEST, "icone/Ovest.png", SfondoIcona.VERDE),

	/* Numero di passi / slot di salvataggio / slot di caricamento */
	NUMERO_1(Comando.NUMERO_1, "icone/1.png", SfondoIcona.VERDE),
	NUMERO_2(Comando.NUMERO_2, "icone/2.png", SfondoIcona.VERDE),
	NUMERO_3(Comando.NUMERO_3, "icone/3.png", SfondoIcona.VERDE),
	NUMERO_4(Comando.NUMERO_4, "icone/4.png", SfondoIcona.VERDE),
	NUMERO_5(Comando.NUMERO_5, "icone/5.png", SfondoIcona.VERDE),

	/* Comandi in città */
	LOCANDA(Comando.LOCANDA, "icone/Locanda.png", SfondoIcona.VERDE),
	NEGOZIO_ALCHIMISTA(Comando.NEGOZIO_ALCHIMISTA, "icone/NegozioAlchimista.png", SfondoIcona.VERDE),
	NEGOZIO_ARMAIOLO(Comando.NEGOZIO_ARMAIOLO, "icone/NegozioArmaiolo.png", SfondoIcona.VERDE),
	NEGOZIO_VENDITORE_DI_PERGAMENE(Comando.NEGOZIO_VENDITORE_DI_PERGAMENE, "icone/NegozioVenditoreDiPergamene.png", SfondoIcona.VERDE),
	NEGOZIO_INCANTATORE(Comando.NEGOZIO_INCANTATORE, "icone/NegozioIncantatore.png", SfondoIcona.VERDE),
	ESCI_DA_CITTA(Comando.ESCI_DA_CITTA, "icone/EsciDaCitta.png", SfondoIcona.VERDE),
	/* In negozio incantatore */
	FUSIONE(Comando.FUSIONE, "icone/Fusione.png", SfondoIcona.VERDE),

	/* Azione su singolo personaggio / su gruppo */
	/* Per ora disattivato, aumentava la magia dall'alchimista */
	GRUPPO(Comando.GRUPPO, "icone/Gruppo.png", SfondoIcona.VERDE),
	SINGOLO(Comando.SINGOLO, "icone/Singolo.png", SfondoIcona.VERDE),

	SI(Comando.SI, "icone/Si.png", SfondoIcona.VERDE),
	NO(Comando.NO, "icone/No.png", SfondoIcona.ROSSO),
	ANNULLA(Comando.ANNULLA, "icone/Annulla.png", SfondoIcona.VERDE),

	AIUTO(Comando.AIUTO, "icone/Aiuto.png", SfondoIcona.VERDE),
	NO_AIUTO(Comando.NO_AIUTO, "icone/Aiuto.png", SfondoIcona.ROSSO),
	MOSTRA_TROFEI(Comando.MOSTRA_TROFEI, "icone/Trofei.png", SfondoIcona.VERDE),
	// Per ora la copia dell'icona del goblin (vedi todo.md)
	MOSTRA_BESTIARIO(Comando.MOSTRA_BESTIARIO, "icone/Bestiario.png", SfondoIcona.VERDE),
	MOSTRA_TATTICHE(Comando.MOSTRA_TATTICHE, "icone/TatticheAttacco.png", SfondoIcona.VERDE),
	PERGAMENA(Comando.PERGAMENA, "icone/Pergamena.png", SfondoIcona.GRIGIO),

	SU(Comando.SU, "icone/Su.png", SfondoIcona.VERDE),
	GIU(Comando.GIU, "icone/Giu.png", SfondoIcona.VERDE),
	DESTRA(Comando.DESTRA, "icone/Destra.png", SfondoIcona.VERDE),
	SINISTRA(Comando.SINISTRA, "icone/Sinistra.png", SfondoIcona.VERDE),

	CARTA(Comando.CARTA, "icone/Carta.png", SfondoIcona.VERDE),
	FORBICE(Comando.FORBICE, "icone/Forbice.png", SfondoIcona.VERDE),
	SASSO(Comando.SASSO, "icone/Sasso.png", SfondoIcona.VERDE),

	RUTTOLOMEO(Comando.RUTTOLOMEO, "icone/Ruttolomeo.png", SfondoIcona.VERDE),
	STORPSGORBLIN(Comando.STORPSGORBLIN, "icone/Storpsgorblin.png", SfondoIcona.VERDE);

	private final Comando comando;
	private final TipoPersonaggio classePersonaggio;
	private final String nomeRisorsa;
	private final SfondoIcona sfondo;

	TipoIcona(Comando comando, String nomeRisorsa, SfondoIcona sfondo) {
		this.comando = comando;
		this.classePersonaggio = null;
		this.nomeRisorsa = nomeRisorsa;
		this.sfondo = sfondo;
	}

	TipoIcona(TipoPersonaggio classePersonaggio, String nomeRisorsa, SfondoIcona sfondo) {
		this.comando = null;
		this.classePersonaggio = classePersonaggio;
		this.nomeRisorsa = nomeRisorsa;
		this.sfondo = sfondo;
	}

	TipoIcona(Comando comando, TipoPersonaggio classePersonaggio, String nomeRisorsa, SfondoIcona sfondo) {
		this.comando = comando;
		this.classePersonaggio = classePersonaggio;
		this.nomeRisorsa = nomeRisorsa;
		this.sfondo = sfondo;
	}

	/**
	 * Il nome della risorsa del disegno dell'icona (per esempio <code>icone/Floppy.png</code>)
	 */
	public String getNomeRisorsa() {
		return nomeRisorsa;
	}

	/**
	 * Lo sfondo normale dell'icona
	 */
	public SfondoIcona getSfondo() {
		return sfondo;
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
	 * Il colore di sfondo di un'icona: il file icone/Sfondo-icona-colore.png.
	 */
	public enum SfondoIcona {
		VERDE, ROSSO, MARRONE, GRIGIO;

		private final String risorsa = "icone/Sfondo-icona-" + name().toLowerCase(Locale.ROOT) + ".png";

		public String getRisorsa() {
			return risorsa;
		}
	}

}
