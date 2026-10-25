package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.CostruttoreArtefatto;
import com.threeamigos.foresta.artefatti.ModificatoreDelTesto;
import com.threeamigos.foresta.artefatti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * Quel che il motore dà al gruppo in più quando si gioca in modalità di prova (vedi {@link ModalitaDiProva}): scorte abbondanti, la Falce del
 * Tristo Mietitore, due spade che mettono un modificatore sul bersaglio e la mappa scoperta per intero. Il gruppo non sa che si sta provando.
 */
final class DotazioneDiProva {

	// Il leggendario che la modalità di prova mette nell'inventario del gruppo (vedi leggendari.txt)
	private static final String CHIAVE_FALCE_DI_PROVA = "FALCE_DEL_TRISTO_MIETITORE";

	private DotazioneDiProva() {
	}

	/**
	 * Se si sta provando, dà al gruppo la dotazione di prova; altrimenti non fa niente.
	 */
	static void applica(GruppoGiocatore gruppo) {
		if (!ModalitaDiProva.isAttiva()) {
			return;
		}
		GruppoGiocatoreMD md = (GruppoGiocatoreMD) gruppo.getGruppoMD();
		md.setMonete(9999);
		for (TipoIncantesimo tipoIncantesimo : TipoIncantesimo.values()) {
			md.setIncantesimi(tipoIncantesimo, 99);
		}
		md.setPozioniSalute(99);
		md.setPozioniSaluteGrande(99);
		md.setPozioniMagia(99);
		md.setPozioniMagiaGrande(99);
		md.setPozioniAumentoLivello(99);
		// La Falce del Tristo Mietitore, per provare la Mietitura
		CatalogoLeggendari.getLeggendario(CHIAVE_FALCE_DI_PROVA).ifPresent(falce -> gruppo.addArtefatto(falce.costruisci(Statistiche.getLivello())));
		// Due spade di livello 1 per provare i modificatori temporanei sul bersaglio (vedi modificatori_temporanei.md)
		gruppo.addArtefatto(spadaDiProva("la Spada dell'Inciampo", "che fa inciampare chi colpisce",
				ModificatoreDelTesto.da("FORTUNA =0 @BERSAGLIO 2TURNI")));
		gruppo.addArtefatto(spadaDiProva("la Spada della Debolezza", "che toglie forza a chi colpisce",
				ModificatoreDelTesto.da("FORZA -2 @BERSAGLIO 3TURNI")));
		Foresta.ottieniMappa();
	}

	private static Artefatto spadaDiProva(String nome, String descrizione, ModificatoreAttributoMD modificatore) {
		return CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome(nome)
				.setDescrizione(descrizione)
				.setLivello(1)
				.setDanniBase(10)
				.setCostoAcquisto(30)
				.setPeso(2)
				.setModificatore(modificatore)
				.costruisci();
	}
}
