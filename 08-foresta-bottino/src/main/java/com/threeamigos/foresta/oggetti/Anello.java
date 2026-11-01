package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.CostantiBottino;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.CostruttoreArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.TipoBottino;

import com.threeamigos.foresta.testi.Testi;

import java.util.List;
import java.util.Optional;

/**
 * Un anello: una volta su sei è magico, di uno di tre tipi, e allora porta con sé un artefatto con i suoi effetti; altrimenti non ha
 * effetto, e preso vale come una gemma (un prezioso per ogni anello).
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
			// Gli anelli non magici non hanno effetto: chi li prende li ha come una gemma (vedi RaccoltaBottino)
			anelloMagico = false;
			tipo = -1;
			artefatto = null;
		}
	}

	// I nomi e le descrizioni stanno in anelli_<lingua>.properties
	private static final String FILE_DEI_TESTI = "anelli";

	private static String nomeACaso(int tipo) {
		List<String> nomi = Testi.serie(FILE_DEI_TESTI, "ANELLO." + tipo + ".NOME");
		return nomi.get(Dado.tiraAncheAUnaFaccia(nomi.size()) - 1);
	}

	private static Artefatto creaAnelloMagico(int tipo) {
		String nome;
		ModificatoreAttributoMD modificatore;
		String descrizione;
		if (tipo == 1) {
			nome = nomeACaso(1);
			modificatore = new ModificatoreAttributoMD(TipoAttributo.VALORE, TipoModificatore.VARIAZIONE_FISSA, CostantiBottino.ANELLO_MAGICO_AGGIUNTA_VALORE);
			descrizione = Testi.get(FILE_DEI_TESTI, "ANELLO.1.DESCRIZIONE").orElse("");
		} else if (tipo == 2) {
			nome = nomeACaso(2);
			modificatore = new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.VARIAZIONE_PERCENTUALE, CostantiBottino.ANELLO_MAGICO_AGGIUNTA_CORAGGIO);
			descrizione = Testi.get(FILE_DEI_TESTI, "ANELLO.2.DESCRIZIONE").orElse("");
		} else {
			nome = nomeACaso(3);
			modificatore = new ModificatoreAttributoMD(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_PERCENTUALE, CostantiBottino.ANELLO_MAGICO_AGGIUNTA_CARISMA);
			descrizione = Testi.get(FILE_DEI_TESTI, "ANELLO.3.DESCRIZIONE").orElse("");
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

	public TipoBottino getClasse() {
		return TipoBottino.ANELLO;
	}

}
