package com.threeamigos.foresta.ui;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.personaggi.ClassiPersonaggio;

public class ImageCache {

	static final int SPACING = 4;
	
	static BufferedImage logo3AM;
	static BufferedImage logoForesta;
	static BufferedImage corniceGrande;
	static BufferedImage corniceIncantesimi;
	static BufferedImage corniceMappa;
	static BufferedImage cornicePiccola;
	static BufferedImage ombraDelDrago;
	static BufferedImage trionfo;
	static BufferedImage segnalino;
	static BufferedImage punto;
	static BufferedImage virgola;
	static BufferedImage apostrofo;
	static BufferedImage puntodd;
	static Map<ClassiLocazione, BufferedImage> locazioni;
	static Map<ClassiLocazione, BufferedImage> mappa;
	static BufferedImage[] icone;
	static BufferedImage[] lettere;
	static BufferedImage[] cifre;

	static BufferedImage[] spriteIncantesimi;
	static BufferedImage spriteAmicizia;
	static BufferedImage spriteCombattimento;
	static BufferedImage spriteMagia;
	static BufferedImage spritePozioneForza;
	static BufferedImage spritePozioneGrandeForza;
	static BufferedImage spritePozioneMagia;
	static BufferedImage spriteMappa;
	static BufferedImage spriteMoneta;
	static BufferedImage spriteGemma;
	static BufferedImage spriteTempo;

	private static Map<String, BufferedImage> imageMap = new HashMap<>();
	
	private static boolean inited = false;
	
	static {
		logo3AM = BufferedImageBuilder.buildBufferedImage("Logo3AM.png");
		logoForesta = BufferedImageBuilder.buildBufferedImage("LogoForesta.png");

		corniceGrande = BufferedImageBuilder.buildBufferedImage("fondi/CorniceGrande.gif");
		corniceIncantesimi = BufferedImageBuilder.buildBufferedImage("fondi/CorniceIncantesimi.gif");
		corniceMappa = BufferedImageBuilder.buildBufferedImage("fondi/CorniceMappa.gif");
		cornicePiccola = BufferedImageBuilder.buildBufferedImage("fondi/CornicePiccola.gif");
		ombraDelDrago = BufferedImageBuilder.buildBufferedImage("fondi/OmbraDelDrago.gif");
		trionfo = BufferedImageBuilder.buildBufferedImage("fondi/Trionfo.gif");

		icone = new BufferedImage[ClassiIcona.NUMERO_ICONE];
		icone[ClassiIcona.MASCHIO] = BufferedImageBuilder.buildBufferedImage("icone/Maschio.gif");
		icone[ClassiIcona.FEMMINA] = BufferedImageBuilder.buildBufferedImage("icone/Femmina.gif");
		icone[ClassiIcona.CENTAURO] = BufferedImageBuilder.buildBufferedImage("icone/Centauro.gif");
		icone[ClassiIcona.EREMITA] = BufferedImageBuilder.buildBufferedImage("icone/Eremita.gif");
		icone[ClassiIcona.GIGANTE] = BufferedImageBuilder.buildBufferedImage("icone/Gigante.gif");
		icone[ClassiIcona.GOBLIN] = BufferedImageBuilder.buildBufferedImage("icone/Goblin.gif");
		icone[ClassiIcona.HOBGOBLIN] = BufferedImageBuilder.buildBufferedImage("icone/Hobgoblin.gif");
		icone[ClassiIcona.MINOTAURO] = BufferedImageBuilder.buildBufferedImage("icone/Minotauro.gif");
		icone[ClassiIcona.TITANO] = BufferedImageBuilder.buildBufferedImage("icone/Titano.gif");
		icone[ClassiIcona.GUERRIERA] = BufferedImageBuilder.buildBufferedImage("icone/Guerriera.gif");
		icone[ClassiIcona.GUERRIERO] = BufferedImageBuilder.buildBufferedImage("icone/Guerriero.gif");
		icone[ClassiIcona.LADRA] = BufferedImageBuilder.buildBufferedImage("icone/Ladra.gif");
		icone[ClassiIcona.LADRO] = BufferedImageBuilder.buildBufferedImage("icone/Ladro.gif");
		icone[ClassiIcona.BARDO] = BufferedImageBuilder.buildBufferedImage("icone/Bardo.gif");
		icone[ClassiIcona.CANTASTORIE] = BufferedImageBuilder.buildBufferedImage("icone/Cantastorie.gif");
		icone[ClassiIcona.ELFA] = BufferedImageBuilder.buildBufferedImage("icone/Elfa.gif");
		icone[ClassiIcona.ELFO] = BufferedImageBuilder.buildBufferedImage("icone/Elfo.gif");
		icone[ClassiIcona.MAGA] = BufferedImageBuilder.buildBufferedImage("icone/Maga.gif");
		icone[ClassiIcona.MAGO] = BufferedImageBuilder.buildBufferedImage("icone/Mago.gif");
		icone[ClassiIcona.OMBRAFIAMMA] = BufferedImageBuilder.buildBufferedImage("icone/OmbraFiamma.gif");
		icone[ClassiIcona.COMBATTIMENTO] = BufferedImageBuilder.buildBufferedImage("icone/Combattimento.gif");
		icone[ClassiIcona.INCANTESIMO] = BufferedImageBuilder.buildBufferedImage("icone/Incantesimo.gif");
		icone[ClassiIcona.CORRUZIONE] = BufferedImageBuilder.buildBufferedImage("icone/Corruzione.gif");
		icone[ClassiIcona.AMICIZIA] = BufferedImageBuilder.buildBufferedImage("icone/Amicizia.gif");
		icone[ClassiIcona.FUGA] = BufferedImageBuilder.buildBufferedImage("icone/Fuga.gif");
		icone[ClassiIcona.ARIA] = BufferedImageBuilder.buildBufferedImage("icone/Aria.gif");
		icone[ClassiIcona.ACQUA] = BufferedImageBuilder.buildBufferedImage("icone/Acqua.gif");
		icone[ClassiIcona.TERRA] = BufferedImageBuilder.buildBufferedImage("icone/Terra.gif");
		icone[ClassiIcona.FUOCO] = BufferedImageBuilder.buildBufferedImage("icone/Fuoco.gif");
		icone[ClassiIcona.FULMINE] = BufferedImageBuilder.buildBufferedImage("icone/Fulmine.gif");
		icone[ClassiIcona.MORTE] = BufferedImageBuilder.buildBufferedImage("icone/Morte.gif");
		icone[ClassiIcona.RESURREZIONE] = BufferedImageBuilder.buildBufferedImage("icone/Resurrezione.gif");
		icone[ClassiIcona.NO_INCANTESIMO] = BufferedImageBuilder.buildBufferedImage("icone/NoIncantesimo.gif");
		icone[ClassiIcona.NORD] = BufferedImageBuilder.buildBufferedImage("icone/Nord.gif");
		icone[ClassiIcona.EST] = BufferedImageBuilder.buildBufferedImage("icone/Est.gif");
		icone[ClassiIcona.SUD] = BufferedImageBuilder.buildBufferedImage("icone/Sud.gif");
		icone[ClassiIcona.OVEST] = BufferedImageBuilder.buildBufferedImage("icone/Ovest.gif");
		icone[ClassiIcona.ACCAMPAMENTO] = BufferedImageBuilder.buildBufferedImage("icone/Accampamento.gif");
		icone[ClassiIcona.FORZA] = BufferedImageBuilder.buildBufferedImage("icone/Forza.gif");
		icone[ClassiIcona.GRANDE_FORZA] = BufferedImageBuilder.buildBufferedImage("icone/GrandeForza.gif");
		icone[ClassiIcona.MAGIA] = BufferedImageBuilder.buildBufferedImage("icone/Magia.gif");
		icone[ClassiIcona.MAPPA] = BufferedImageBuilder.buildBufferedImage("icone/Mappa.gif");
		icone[ClassiIcona.FLOPPY] = BufferedImageBuilder.buildBufferedImage("icone/Floppy.gif");
		icone[ClassiIcona.NUMERO_1] = BufferedImageBuilder.buildBufferedImage("icone/1.gif");
		icone[ClassiIcona.NUMERO_2] = BufferedImageBuilder.buildBufferedImage("icone/2.gif");
		icone[ClassiIcona.NUMERO_3] = BufferedImageBuilder.buildBufferedImage("icone/3.gif");
		icone[ClassiIcona.NUMERO_4] = BufferedImageBuilder.buildBufferedImage("icone/4.gif");
		icone[ClassiIcona.NUMERO_5] = BufferedImageBuilder.buildBufferedImage("icone/5.gif");
		icone[ClassiIcona.LOCANDA] = BufferedImageBuilder.buildBufferedImage("icone/Locanda.gif");
		icone[ClassiIcona.ALCHIMISTA] = BufferedImageBuilder.buildBufferedImage("icone/Alchimista.gif");
		icone[ClassiIcona.ESCI_DA_CITTA] = BufferedImageBuilder.buildBufferedImage("icone/EsciDaCitta.gif");
		icone[ClassiIcona.GRUPPO] = BufferedImageBuilder.buildBufferedImage("icone/Gruppo.gif");
		icone[ClassiIcona.SINGOLO] = BufferedImageBuilder.buildBufferedImage("icone/Singolo.gif");
		icone[ClassiIcona.SI] = BufferedImageBuilder.buildBufferedImage("icone/Si.gif");
		icone[ClassiIcona.NO] = BufferedImageBuilder.buildBufferedImage("icone/No.gif");
		icone[ClassiIcona.ANNULLA] = BufferedImageBuilder.buildBufferedImage("icone/Annulla.gif");
		icone[ClassiIcona.AIUTO] = BufferedImageBuilder.buildBufferedImage("icone/Aiuto.gif");
		icone[ClassiIcona.PERGAMENA] = BufferedImageBuilder.buildBufferedImage("icone/Pergamena.gif");
		icone[ClassiIcona.SU] = BufferedImageBuilder.buildBufferedImage("icone/Su.gif");
		icone[ClassiIcona.GIU] = BufferedImageBuilder.buildBufferedImage("icone/Giu.gif");
		icone[ClassiIcona.DESTRA] = BufferedImageBuilder.buildBufferedImage("icone/Destra.gif");
		icone[ClassiIcona.SINISTRA] = BufferedImageBuilder.buildBufferedImage("icone/Sinistra.gif");
		icone[ClassiIcona.RUTTOLOMEO] = BufferedImageBuilder.buildBufferedImage("icone/Ruttolomeo.gif");
		icone[ClassiIcona.STORPSGORBLIN] = BufferedImageBuilder.buildBufferedImage("icone/Storpsgorblin.gif");

		locazioni = new EnumMap<>(ClassiLocazione.class);
		BufferedImage d;
		locazioni.put(ClassiLocazione.BOSCO, BufferedImageBuilder.buildBufferedImage("locazioni/Foresta.gif"));
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Castello.gif");
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CASTELLO) {
				locazioni.put(classeLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Citta.gif");
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA) {
				locazioni.put(classeLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Grotta.gif");
		locazioni.put(ClassiLocazione.GROTTA, d);
		locazioni.put(ClassiLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE, d);
		locazioni.put(ClassiLocazione.LOCANDA, BufferedImageBuilder.buildBufferedImage("locazioni/Locanda.gif"));
		locazioni.put(ClassiLocazione.PALUDE, BufferedImageBuilder.buildBufferedImage("locazioni/Palude.gif"));
		locazioni.put(ClassiLocazione.RADURA, BufferedImageBuilder.buildBufferedImage("locazioni/Radura.gif"));
		d = BufferedImageBuilder.buildBufferedImage("locazioni/Rovine.gif");
		locazioni.put(ClassiLocazione.ROVINE, d);
		locazioni.put(ClassiLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI, d);
		locazioni.put(ClassiLocazione.TEMPIO, BufferedImageBuilder.buildBufferedImage("locazioni/Tempio.gif"));

		mappa = new EnumMap<>(ClassiLocazione.class);
		mappa.put(ClassiLocazione.BOSCO, BufferedImageBuilder.buildBufferedImage("mappa/Foresta.gif"));
		d = BufferedImageBuilder.buildBufferedImage("mappa/Castello.gif");
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CASTELLO) {
				mappa.put(classeLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("mappa/Citta.gif");
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA) {
				mappa.put(classeLocazione, d);
			}
		}
		d = BufferedImageBuilder.buildBufferedImage("mappa/Grotta.gif");
		mappa.put(ClassiLocazione.GROTTA, d);
		mappa.put(ClassiLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE, d);
		mappa.put(ClassiLocazione.LOCANDA, BufferedImageBuilder.buildBufferedImage("mappa/Locanda.gif"));
		mappa.put(ClassiLocazione.PALUDE, BufferedImageBuilder.buildBufferedImage("mappa/Palude.gif"));
		mappa.put(ClassiLocazione.RADURA, BufferedImageBuilder.buildBufferedImage("mappa/Radura.gif"));
		d = BufferedImageBuilder.buildBufferedImage("mappa/Rovine.gif");
		mappa.put(ClassiLocazione.ROVINE, d);
		mappa.put(ClassiLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI, d);
		mappa.put(ClassiLocazione.TEMPIO, BufferedImageBuilder.buildBufferedImage("mappa/Tempio.gif"));
		segnalino = BufferedImageBuilder.buildBufferedImage("mappa/Segnalino.gif");

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
		puntodd = BufferedImageBuilder.buildBufferedImage("alfabeto/Puntodd.gif");
		apostrofo = BufferedImageBuilder.buildBufferedImage("alfabeto/Apostrofo.gif");
		
		spriteIncantesimi = new BufferedImage[ClassiIncantesimo.values().length];
		spriteIncantesimi[ClassiIncantesimo.ARIA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Aria-nobordo-piccolo.gif");
		spriteIncantesimi[ClassiIncantesimo.ACQUA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Acqua-nobordo-piccolo.gif");
		spriteIncantesimi[ClassiIncantesimo.TERRA.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Terra-nobordo-piccolo.gif");
		spriteIncantesimi[ClassiIncantesimo.FUOCO.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Fuoco-nobordo-piccolo.gif");
		spriteIncantesimi[ClassiIncantesimo.FULMINE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Fulmine-nobordo-piccolo.gif");
		spriteIncantesimi[ClassiIncantesimo.MORTE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Morte-nobordo-piccolo.gif");
		spriteIncantesimi[ClassiIncantesimo.RESURREZIONE.ordinal()] = BufferedImageBuilder.buildBufferedImage("icone/Resurrezione-nobordo-piccolo.gif");

		spriteAmicizia = BufferedImageBuilder.buildBufferedImage("icone/Amicizia-nobordo-piccolo.gif");
		spriteCombattimento = BufferedImageBuilder.buildBufferedImage("icone/Combattimento-nobordo-piccolo.gif");
		spriteMagia = BufferedImageBuilder.buildBufferedImage("icone/Incantesimo-nobordo-piccolo.gif");
		spritePozioneForza = BufferedImageBuilder.buildBufferedImage("icone/Forza-nobordo-piccolo.gif");
		spritePozioneGrandeForza = BufferedImageBuilder.buildBufferedImage("icone/GrandeForza-nobordo-piccolo.gif");
		spritePozioneMagia = BufferedImageBuilder.buildBufferedImage("icone/Magia-nobordo-piccolo.gif");
		spriteMappa = BufferedImageBuilder.buildBufferedImage("icone/Mappa-nobordo-piccolo.gif");
		spriteMoneta = BufferedImageBuilder.buildBufferedImage("icone/Moneta-nobordo-piccolo.gif");
		spriteGemma = BufferedImageBuilder.buildBufferedImage("icone/Gemma-nobordo-piccolo.gif");
		spriteTempo = BufferedImageBuilder.buildBufferedImage("icone/Tempo-nobordo-piccolo.gif");

	}
	
	static void init() {
		if (!inited) {
			for (ClassiPersonaggio classePersonaggio : ClassiPersonaggio.values()) {
				classePersonaggio.getIstanza();
			}
			inited = true;
		}
	}
	
	public static void set(String nomeImmagine, BufferedImage displayable) {
		imageMap.put(nomeImmagine, displayable);
	}
	
	public static BufferedImage get(String nomeImmagine) {
		return imageMap.get(nomeImmagine);
	}
}

