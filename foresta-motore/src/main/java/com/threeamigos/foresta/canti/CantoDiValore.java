package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.combattimento.AnnuncioDellAzione;
import com.threeamigos.foresta.tipi.CostantiCanti;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoCanto;

/**
 * Il canto che rafforza il gruppo: tutti i personaggi vivi del gruppo diventano ISPIRATI, con coraggio e precisione
 * aumentati di una percentuale che cresce con il livello del cantore (e non con il suo carisma): CANTO_DI_VALORE_PERCENTUALE_BASE
 * più CANTO_DI_VALORE_PERCENTUALE_PER_LIVELLO per livello, fino a CANTO_DI_VALORE_PERCENTUALE_MASSIMA, per
 * CANTO_DI_VALORE_DURATA turni. Cantarlo di nuovo non raddoppia l'effetto: rinfresca la durata (vedi
 * Personaggio.addEffettoDiStato).
 */
public class CantoDiValore implements Canto {

	private final int livello;

	public CantoDiValore(Personaggio cantore) {
		this.livello = cantore.getLivello();
	}

	@Override
	public TipoCanto getTipo() {
		return TipoCanto.DI_VALORE;
	}

	/**
	 * La percentuale di coraggio e di precisione in più, per il livello del cantore
	 */
	public double getIntensita() {
		return Math.min(CostantiCanti.CANTO_DI_VALORE_PERCENTUALE_MASSIMA,
				CostantiCanti.CANTO_DI_VALORE_PERCENTUALE_BASE + CostantiCanti.CANTO_DI_VALORE_PERCENTUALE_PER_LIVELLO * livello);
	}

	@Override
	public boolean canta(Personaggio cantore, Gruppo gruppo, Gruppo gruppoAvversario) {
		AnnuncioDellAzione.azione(cantore, null, TipoAzione.CANTO_DI_VALORE);
		cantore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(cantore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " intona un canto di valore: il gruppo si sente più coraggioso e preciso."));
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			personaggio.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, CostantiCanti.CANTO_DI_VALORE_DURATA, 0, getIntensita());
		}
		return true;
	}
}
