package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.MoltiplicatoreDiClasse;
import com.threeamigos.foresta.tipi.NoteDeiPersonaggi;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * I moltiplicatori di una classe di personaggi (quelli che ogni classe dichiara in Costanti) con la nota che dice perché ha quel valore
 * (note_personaggi_it.properties). Li legge dal personaggio modello della classe: sono uguali per tutti i personaggi della classe.
 */
public final class MoltiplicatoriDiClasse {

	private MoltiplicatoriDiClasse() {
	}

	/**
	 * I moltiplicatori della classe, nell'ordine in cui i testi li elencano
	 */
	public static List<MoltiplicatoreDiClasse> di(TipoPersonaggio classe) {
		Personaggio modello = FabbricaPersonaggi.modello(classe);
		List<MoltiplicatoreDiClasse> moltiplicatori = new ArrayList<>();
		aggiungi(moltiplicatori, classe, "CARICO_MASSIMO", modello.getMoltiplicatoreCarico());
		aggiungi(moltiplicatori, classe, "CRITICO", modello.getMoltiplicatoreCritico());
		aggiungi(moltiplicatori, classe, "PRECISIONE", modello.getMoltiplicatorePrecisione());
		aggiungi(moltiplicatori, classe, "VELOCITA", modello.getMoltiplicatoreVelocita());
		aggiungi(moltiplicatori, classe, "FURTIVITA", modello.getMoltiplicatoreFurtivita());
		aggiungi(moltiplicatori, classe, "PARATA", modello.getMoltiplicatoreParata());
		aggiungi(moltiplicatori, classe, "RESISTENZA_MAGICA", modello.getMoltiplicatoreResistenzaMagica());
		aggiungi(moltiplicatori, classe, "PERCEZIONE", modello.getMoltiplicatorePercezione());
		aggiungi(moltiplicatori, classe, "SOGGEZIONE", modello.getMoltiplicatoreSoggezione());
		aggiungi(moltiplicatori, classe, "FURIA", modello.getMoltiplicatoreFuria());
		aggiungi(moltiplicatori, classe, "CORAGGIO", modello.getMoltiplicatoreCoraggio());
		aggiungi(moltiplicatori, classe, "VALORE", modello.getMoltiplicatoreValore());
		aggiungi(moltiplicatori, classe, "CONTRATTAZIONE", modello.getMoltiplicatoreContrattazione());
		aggiungi(moltiplicatori, classe, "NUMERO_BERSAGLI", modello.getMoltiplicatoreNumeroBersagli());
		aggiungi(moltiplicatori, classe, "STANCHEZZA", modello.getMoltiplicatoreStanchezza());
		aggiungi(moltiplicatori, classe, NoteDeiPersonaggi.DANNI_MAGICI, modello.getMoltiplicatoreDanniMagici());
		aggiungi(moltiplicatori, classe, NoteDeiPersonaggi.DANNI_FISICI, modello.getMoltiplicatoreDanniFisici());
		aggiungi(moltiplicatori, classe, "RIGENERAZIONE_SALUTE", modello.getMoltiplicatoreRecuperoFisico());
		aggiungi(moltiplicatori, classe, "RIGENERAZIONE_MAGIA", modello.getMoltiplicatoreRecuperoMagico());
		return Collections.unmodifiableList(moltiplicatori);
	}

	private static void aggiungi(List<MoltiplicatoreDiClasse> moltiplicatori, TipoPersonaggio classe, String chiave, double valore) {
		moltiplicatori.add(new MoltiplicatoreDiClasse(chiave, classe, valore));
	}
}
