package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {

	static final int SPACING = 4;
	
	static BufferedImage logo3AM;
	static BufferedImage logoForesta;
	static BufferedImage corniceGrande;
	static BufferedImage corniceIncantesimi;
	static BufferedImage corniceMappa;
	static BufferedImage cornicePiccola;
	static BufferedImage corniceInventario;
	static BufferedImage ombraDelDrago;
	static BufferedImage trionfo;
	static BufferedImage separatore;
	static BufferedImage separatoreArmi;
	static BufferedImage separatoreArmature;
	static BufferedImage separatoreScudi;
	static BufferedImage separatoreNinnoli;
	static BufferedImage segnalino;
	static BufferedImage punto;
	static BufferedImage virgola;
	static BufferedImage apostrofo;
	static BufferedImage puntodd;
	static Map<ClassiLocazione, BufferedImage> locazioni;
	static Map<ClassiLocazione, BufferedImage> mappa;
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
	static BufferedImage spriteGemma;
	static BufferedImage spriteTempo;
	static BufferedImage spriteAumentoLivello;

	static BufferedImage missioneBirra;
	static BufferedImage missioneGallo;
	static BufferedImage componenteScorrevoleFrecciaSu;
	static BufferedImage componenteScorrevoleFrecciaGiu;


	private static final Map<String, BufferedImage> imageMap = new HashMap<>();

	private static final Map<DoomdarkFont, Map<DoomdarkColorModel.Color, Map<String, Image>>> cache = new HashMap<>();
	private static final DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();

	private static boolean inited = false;
	
	static {
		logo3AM = BufferedImageBuilder.buildBufferedImage("Logo3AM.png");
		logoForesta = BufferedImageBuilder.buildBufferedImage("LogoForesta.png");

		corniceGrande = BufferedImageBuilder.buildBufferedImage("fondi/CorniceGrande.gif");
		corniceIncantesimi = BufferedImageBuilder.buildBufferedImage("fondi/CorniceIncantesimi.gif");
		corniceMappa = BufferedImageBuilder.buildBufferedImage("fondi/CorniceMappa.gif");
		cornicePiccola = BufferedImageBuilder.buildBufferedImage("fondi/CornicePiccola.gif");
		corniceInventario = BufferedImageBuilder.buildBufferedImage("fondi/CorniceInventario.gif");
		ombraDelDrago = BufferedImageBuilder.buildBufferedImage("fondi/OmbraDelDrago.gif");
		trionfo = BufferedImageBuilder.buildBufferedImage("fondi/Trionfo.gif");
		separatore = BufferedImageBuilder.buildBufferedImage("fondi/Separatore.gif");
		separatoreArmi = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Armi.gif");
		separatoreArmature = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Armature.gif");
		separatoreScudi = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Scudi.gif");
		separatoreNinnoli = BufferedImageBuilder.buildBufferedImage("fondi/Separatore-Ninnoli.gif");

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

		spriteAmicizia = BufferedImageBuilder.buildBufferedImage("icone/Amicizia-nobordo-piccolo.gif");
		spriteCombattimento = BufferedImageBuilder.buildBufferedImage("icone/Combattimento-nobordo-piccolo.gif");
		spriteMagia = BufferedImageBuilder.buildBufferedImage("icone/Incantesimo-nobordo-piccolo.gif");
		spritePozioneSalute = BufferedImageBuilder.buildBufferedImage("icone/PozioneSalute-nobordo-piccolo.gif");
		spritePozioneSaluteGrande = BufferedImageBuilder.buildBufferedImage("icone/PozioneSaluteGrande-nobordo-piccolo.gif");
		spritePozioneMagia = BufferedImageBuilder.buildBufferedImage("icone/PozioneMagia-nobordo-piccolo.gif");
		spritePozioneMagiaGrande = BufferedImageBuilder.buildBufferedImage("icone/PozioneMagiaGrande-nobordo-piccolo.gif");
		spriteMappa = BufferedImageBuilder.buildBufferedImage("icone/Mappa-nobordo-piccolo.gif");
		spriteMoneta = BufferedImageBuilder.buildBufferedImage("icone/Moneta-nobordo-piccolo.gif");
		spriteGemma = BufferedImageBuilder.buildBufferedImage("icone/Gemma-nobordo-piccolo.gif");
		spriteTempo = BufferedImageBuilder.buildBufferedImage("icone/Tempo-nobordo-piccolo.gif");
		spriteAumentoLivello = BufferedImageBuilder.buildBufferedImage("icone/AumentoLivello-nobordo-piccolo.gif");

		missioneBirra = BufferedImageBuilder.buildBufferedImage("icone/Missione-birra-grande.gif");
		missioneGallo = BufferedImageBuilder.buildBufferedImage("icone/Missione-gallo.gif");

		componenteScorrevoleFrecciaSu = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaSu.gif");
		componenteScorrevoleFrecciaGiu = BufferedImageBuilder.buildBufferedImage("icone/ComponenteScorrevole-FrecciaGiu.gif");
	}
	
	static void init() {
		if (!inited) {
			for (ClassePersonaggio classePersonaggio : ClassePersonaggio.values()) {
				classePersonaggio.getIstanza(1);
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
		return cache
				.computeIfAbsent(font, k -> new HashMap<>())
				.computeIfAbsent(colore, k -> new HashMap<>())
				.computeIfAbsent(testo, k -> DoomdarkTextProducer.getImage(k, font, colore));
	}
}

