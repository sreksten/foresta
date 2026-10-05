package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.PersonaggioMD;
import com.threeamigos.foresta.personaggi.PersonaggioBase;

/**
 * Dopo che un GestoreSalvataggi ha letto una partita e installato il nuovo ModelloDati (via ModelloDati.setIstanza),
 * ricostruisce lo stato derivato che non si salva: gli attributi secondari dei personaggi, il gruppo collegato al
 * nuovo modello con la sua locazione corrente, il registro delle missioni. Va eseguito dopo l'installazione,
 * altrimenti le classi di dominio leggerebbero ancora la vecchia istanza tramite ModelloDati.getIstanza().
 */
public final class RiletturaPartita {

	private RiletturaPartita() {
	}

	public static void ricostruisci() {
		for (PersonaggioMD personaggioMD : ModelloDati.getIstanza().getGruppoGiocatoreMD().getPersonaggiMD()) {
			PersonaggioBase.ricalcolaAttributiSecondari(personaggioMD, personaggioMD.getClasse().getMoltiplicatoriDiClasse());
		}
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		gruppo.setModelloDati(ModelloDati.getIstanza().getGruppoGiocatoreMD());
		gruppo.setLocazioneCorrente(Foresta.costruisciIstanza(gruppo.getCoordinate()));
		RegistroMissioni.aggiornaDopoRilettura();
	}
}
