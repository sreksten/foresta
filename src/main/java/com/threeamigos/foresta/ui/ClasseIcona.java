package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.awt.image.BufferedImage;

public enum ClasseIcona {

	MASCHIO(Comando.MASCHIO, "icone/Maschio.gif"),
	FEMMINA(Comando.FEMMINA,"icone/Femmina.gif"),

	CENTAURO(ClassePersonaggio.CENTAURO, "icone/Centauro.gif"),
	EREMITA(ClassePersonaggio.EREMITA, "icone/Eremita.gif"),
	GIGANTE(ClassePersonaggio.GIGANTE, "icone/Gigante.gif"),
	GOBLIN(ClassePersonaggio.GOBLIN, "icone/Goblin.gif"),
	HOBGOBLIN(ClassePersonaggio.HOBGOBLIN, "icone/Hobgoblin.gif"),
	MINOTAURO(ClassePersonaggio.MINOTAURO, "icone/Minotauro.gif"),
	TITANO(ClassePersonaggio.TITANO, "icone/Titano.gif"),
	GUERRIERA(Comando.GUERRIERA, ClassePersonaggio.GUERRIERA, "icone/Guerriera.gif"),
	GUERRIERO(Comando.GUERRIERO, ClassePersonaggio.GUERRIERO, "icone/Guerriero.gif"),
	LADRA(Comando.LADRA, ClassePersonaggio.LADRA, "icone/Ladra.gif"),
	LADRO(Comando.LADRO, ClassePersonaggio.LADRO, "icone/Ladro.gif"),
	BARDO(Comando.BARDO, ClassePersonaggio.BARDO, "icone/Bardo.gif"),
	CANTASTORIE(Comando.CANTASTORIE, ClassePersonaggio.CANTASTORIE, "icone/Cantastorie.gif"),
	ELFA(Comando.ELFA, ClassePersonaggio.ELFA, "icone/Elfa.gif"),
	ELFO(Comando.ELFO, ClassePersonaggio.ELFO, "icone/Elfo.gif"),
	MAGA(Comando.MAGA, ClassePersonaggio.MAGA, "icone/Maga.gif"),
	MAGO(Comando.MAGO, ClassePersonaggio.MAGO, "icone/Mago.gif"),
	OMBRAFIAMMA(ClassePersonaggio.OMBRAFIAMMA, "icone/OmbraFiamma.gif"),

	COMBATTIMENTO(Comando.COMBATTIMENTO,"icone/Combattimento.gif"),
	INTERRUZIONE_COMBATTIMENTO(Comando.INTERRUZIONE_COMBATTIMENTO, "icone/InterruzioneCombattimento.gif"),
	INCANTESIMO(Comando.INCANTESIMO, "icone/Incantesimo.gif"),
	CORRUZIONE(Comando.CORRUZIONE, "icone/Corruzione.gif"),
	AMICIZIA(Comando.AMICIZIA,"icone/Amicizia.gif"),
	FUGA(Comando.FUGA, "icone/Fuga.gif"),

	ARIA(Comando.ARIA,"icone/Aria.gif"),
	ACQUA(Comando.ACQUA, "icone/Acqua.gif"),
	TERRA(Comando.TERRA,"icone/Terra.gif"),
	FUOCO(Comando.FUOCO, "icone/Fuoco.gif"),
	FULMINE(Comando.FULMINE, "icone/Fulmine.gif"),
	GELO(Comando.GELO, "icone/Gelo.gif"),
	VELENO(Comando.VELENO, "icone/Veleno.gif"),
	MORTE(Comando.MORTE, "icone/Morte.gif"),
	RESURREZIONE(Comando.RESURREZIONE, "icone/Resurrezione.gif"),
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
	FLOPPY(Comando.FLOPPY, "icone/Floppy.gif"),

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
	private final ClassePersonaggio classePersonaggio;
	private final BufferedImage icona;
	private static int altezzaMassima = -1;

	ClasseIcona(Comando comando, String nomeRisorsa) {
		this.comando = comando;
		this.classePersonaggio = null;
		icona = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
	}

	ClasseIcona(ClassePersonaggio classePersonaggio, String nomeRisorsa) {
		this.comando = null;
		this.classePersonaggio = classePersonaggio;
		icona = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
	}

	ClasseIcona(Comando comando, ClassePersonaggio classePersonaggio, String nomeRisorsa) {
		this.comando = comando;
		this.classePersonaggio = classePersonaggio;
		icona = BufferedImageBuilder.buildBufferedImage(nomeRisorsa);
	}

	public BufferedImage getIcona() {
		return icona;
	}

	public static int getAltezzaMassima() {
		if (altezzaMassima == -1) {
			for (ClasseIcona corrente : values()) {
				altezzaMassima = Math.max(altezzaMassima, corrente.icona.getHeight());
			}
		}
		return altezzaMassima;
	}

	public static ClasseIcona ofClasse(ClassePersonaggio classePersonaggio) {
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
