package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.CostruttoreArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoBottino;

import java.util.Optional;

/**
 * Un anello: una volta su sei è magico, di uno di tre tipi, e allora porta con sé un artefatto con i suoi effetti; altrimenti non ha
 * effetto.
 */
public class Anello extends OggettoBase implements Bottino {

	private final boolean anelloMagico;
	private final int tipo;
	// Se è magico, l'artefatto nasce con l'anello: serve già per scegliere chi lo prende
	private final Artefatto artefatto;
	private boolean notificato;

	public Anello() {
		super();
		int tiroDado = Dado.tira(6);
		Logger.log(() -> "Tiro del dado per anello magico: " + tiroDado);
		if (tiroDado == 6) {
			anelloMagico = true;
			tipo = Dado.tira(3);
			Logger.log(() -> "Anello magico di tipo " + tipo);
			artefatto = creaAnelloMagico(tipo);
		} else {
			// Gli anelli non magici restano senza effetto. TODO più avanti si potranno vendere come gemme
			anelloMagico = false;
			tipo = -1;
			artefatto = null;
		}
	}

	private static Artefatto creaAnelloMagico(int tipo) {
		String nome;
		ModificatoreAttributoMD modificatore;
		String descrizione;
		if (tipo == 1) {
			String[] appartenenzePossibili = new String[]{ "della Valchiria", "del Grifone", "del Paladino", "del Centurione", "della Manticora", "del Minotauro", "della Viverna" };
			int indiceAppartenenza = Dado.tiraAncheAUnaFaccia(appartenenzePossibili.length) - 1;
			nome = "un Anello magico " + appartenenzePossibili[indiceAppartenenza];
			modificatore = new ModificatoreAttributoMD(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_FISSA, Costanti.ANELLO_MAGICO_AGGIUNTA_VALORE);
			descrizione = "che aumenta il Valore";
		} else if (tipo == 2) {
			String[] appartenenzePossibili = new String[]{ "del Berserker", "della Fenice", "del Pegaso", "della Salamandra", "del Gladiatore", "dell'Ippogrifo", "dell'Esploratore" };
			int indiceAppartenenza = Dado.tiraAncheAUnaFaccia(appartenenzePossibili.length) - 1;
			nome = "un Anello magico " + appartenenzePossibili[indiceAppartenenza];
			modificatore = new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, Costanti.ANELLO_MAGICO_AGGIUNTA_CORAGGIO);
			descrizione = "che aumenta il Coraggio";
		} else {
			String[] appartenenzePossibili = new String[]{ "della Sirena", "della Sfinge", "dell'Arcangelo", "della Gorgone", "del Dullahan", "della Lamia", "del Basilisco" };
			int indiceAppartenenza = Dado.tiraAncheAUnaFaccia(appartenenzePossibili.length) - 1;
			nome = "un Anello magico " + appartenenzePossibili[indiceAppartenenza];
			modificatore = new ModificatoreAttributoMD(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_PERCENTUALE, Costanti.ANELLO_MAGICO_AGGIUNTA_CARISMA);
			descrizione = "che aumenta il Carisma";
		}
		return CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ANELLO)
				.setNome(nome)
				.setDescrizione(descrizione)
				.setLivello(1)
				.setDanniBase(0)
				.setCostoAcquisto(20)
				.setPeso(0.1)
				.setModificatore(modificatore)
				.costruisci();
	}

	/**
	 * Se è un anello magico (uno su sei): allora porta un artefatto.
	 */
	public boolean isMagico() {
		return anelloMagico;
	}

	/**
	 * Di quale dei tre tipi è l'anello magico (1 aumenta il valore, 2 il coraggio, 3 il carisma); -1 se non è magico.
	 */
	public int getTipoDiMagia() {
		return tipo;
	}

	/**
	 * Se la magia dell'anello è già stata annunciata: un anello rifiutato e ripreso non si annuncia due volte.
	 */
	public boolean isMagiaAnnunciata() {
		return notificato;
	}

	/**
	 * Ricorda che la magia dell'anello è stata annunciata.
	 */
	public void segnaMagiaAnnunciata() {
		notificato = true;
	}

	@Override
	public Optional<Artefatto> getArtefatto() {
		return Optional.ofNullable(artefatto);
	}

	public String getAIS() {
		return Articoli.UN;
	}

	public String getAIP() {
		return Articoli.ALCUNI;
	}

	public String getADS() {
		return Articoli.L_APOSTROFO;
	}

	public String getADP() {
		return Articoli.GLI;
	}

	public String getNomeSingolare() {
		return "anello";
	}

	public String getNomePlurale() {
		return "anelli";
	}

	public TipoBottino getClasse() {
		return TipoBottino.ANELLO;
	}

}
