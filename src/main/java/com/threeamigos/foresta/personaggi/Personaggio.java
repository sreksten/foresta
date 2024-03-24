package com.threeamigos.foresta.personaggi;

import java.awt.image.BufferedImage;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.modellodati.PersonaggioMD;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Artefatto;

public interface Personaggio {

	public enum NotificaFerite {
		SI,
		NO;
	}
	
	public enum NotificaMorte {
		SI,
		NO;
	}
	
	public enum Sesso {
		MASCHIO,
		FEMMINA;
	}
	
	public enum Caratteristica {
		FORZA,
		FORZA_MASSIMA,
		MAGIA,
		MAGIA_MASSIMA,
		CORAGGIO,
		VALORE,
		STANCHEZZA,
		CARISMA
	}

	/**
	 * Articolo indeterminativo singolare
	 */
	public String getAIS();
	/**
	 * Una sorta di "articolo indeterminativo plurale" (alcuni, alcune)
	 */
	public String getAIP();
	/**
	 * Articolo determinativo singolare
	 */
	public String getADS();
	/**
	 * Articolo determinativo plurale
	 */
	public String getADP();
	/**
	 * Di + articolo determinativo singolare
	 */
	public String getDeS();
	/**
	 * Di + articolo determinativo plurale
	 */
	public String getDeP();
	/**
	 * Da (ucciso da)
	 */
	public String getDa();

	/**
	 * La classe del personaggio da ClassiPersonaggio
	 */
	public ClassiPersonaggio getClasse();
	/**
	 * L'immagine che rappresenta il personaggio
	 */
	public BufferedImage getImmagine();
	/**
	 * L'icona che rappresenta il personaggio
	 */
	public BufferedImage getIcona();
	/**
	 * Il nome singolare della razza del personaggio
	 */
	public String getNomeSingolare();
	/**
	 * Il nome plurale dela razza del personaggio
	 */
	public String getNomePlurale();
	/**
	 * Un pronome adatto al personaggio  (egli, ella, esso)
	 */
	public String getPronome();

	/**
	 * Usata da LocazioneBase per impostare il numero del personaggio che viene poi
	 * utilizzato duranre i combattimenti (il primo mostro non ce l'ha fatta...)
	 */
	public void setOrdinale(int ordinale);
	/**
	 * Ritorna l'ordinale del personaggio all'interno del gruppo
	 */
	public int getOrdinale();

	/**
	 * Il sesso del personaggio da ClassiPersonaggio
	 */
	public Sesso getSesso();
	/**
	 * Indica se sia possibile o meno cercare di corrompere questo personaggio
	 */
	public boolean isCorrompibile();
	/**
	 * Indica se sia possibile o meno cercare di fare amicizia con questo personaggio
	 */
	public boolean isAmichevole();
	/**
	 * Indica se questo personaggio sia in grado di usare la magia
	 */
	public boolean isMagico();
	/**
	 * La quantita' di forza che il personaggio riacquista riposando
	 */
	public int getRecuperoForza();
	/**
	 * La quantita' di magia che questo personaggio riacquista riposando
	 */
	public int getRecuperoMagia();

	/**
	 * Indica se questo personaggio sia un personaggio non giocante ossia
	 * controllato dal computer
	 */
	public boolean isPNG();
	/**
	 * Il nome proprio del personaggio (puo' essere null)
	 */
	public String getNome(); // il nome del personaggio
	/**
	 * Indica se il personaggio sia immortale
	 */
	public boolean isImmortale();
	/**
	 * Indica se il personaggio sia vivo
	 */
	public boolean isVivo();
	/**
	 * Il personaggio muore a causa di qualcosa
	 */
	public void muore(String causaTrapasso);
	/**
	 * Cosa ha provocato la morte del personaggio?
	 */
	public String getCausaTrapasso();
	/**
	 * Il personaggio resuscita
	 */
	public void resuscita();
	/**
	 * La forza attuale del personaggio
	 */
	public int getForza();
	/**
	 * Aggiunge forza ad un personaggio (non puo' superare la forza massima)
	 */
	public void addForza(int quantita);
	/**
	 * Sottrae forza (ferisce) ad un personaggio
	 * @param avversario il personaggio responsabile del ferimento
	 * @param notificaFerite se true verra' richiesto all'UI di notificare che il
	 *        personaggio e' stato ferito
	 * @param notificaMorte se true verra' richiesto all'UI (in caso di decesso) che
	 *        il personagio e' morto
	 */
	public void subForza(int quantita, Personaggio avversario, NotificaFerite notificaFerite, NotificaMorte notificaMorte);
	/**
	 * La forza massima del personaggio
	 */
	public int getForzaMassima();
	/**
	 * Alcuni oggetti possono aumentare la forza massima di un personaggio
	 */
	public void addForzaMassima(int quantita);
	/**
	 * Quanti danni fa normalmente il personaggio in combattimento
	 */
	public int getDanniInCombattimento();
	/**
	 * Eventuali moltiplicatori ai danni del combattimento, normalmente 1
	 */
	public int getModificaDanniForza();
	/**
	 * La magia attuale del personaggio
	 */
	public int getMagia();
	/**
	 * Incrementa il livello di magia del personaggio; non puo' superare il livello massimo
	 */
	public void addMagia(int quantita);
	/**
	 * Sottrae magia al personaggio
	 */
	public void subMagia(int quantita);
	/**
	 * Il livello massimo di magia del personaggio
	 */
	public int getMagiaMassima();
	/**
	 * Alcuni oggetti possono aumentare la magia massima di un personaggio
	 */
	public void addMagiaMassima(int quantita);
	/**
	 * Quanti avversari puo' bersagliare il personaggio con un singolo incantesimo
	 */
	public int getBersagliPerIncantesimo();
	/**
	 *  Eventuali moltiplicatori ai danni derivanti da un incantesimo
	 */
	public int getModificaDanniMagia(int danniBase);
	/**
	 * Il coraggio di un personaggio da 0 a 99
	 */
	public int getCoraggio();
	/**
	 * Aumenta il coraggio di un personaggio fino a 99
	 */
	public void addCoraggio(int quantita);
	/**
	 * Diminuisce il coraggio di un personaggio fino a 0
	 */
	public void subCoraggio(int quantita);
	/**
	 * Il valore in combattimento di un personaggio da 0 a 99
	 */
	public int getValore();
	/**
	 * Aumenta il valore di un personaggio fino a 99
	 */
	public void addValore(int quantita);
	/**
	 * Diminuisce il valore di un personaggio fino a 0
	 */
	public void subValore(int quantita);
	/**
	 * La stanchezza di un personaggio da 0 a 9
	 */
	public int getStanchezza();
	/**
	 * Aumenta la stanchezza di un personaggio fino a 9
	 */
	public void addStanchezza(int quantita);
	/**
	 * Diminuisce la stanchezza di un personaggio fino a 0
	 */
	public void subStanchezza(int quantita);
	/**
	 * Il carisma di un personaggio da 0 in poi
	 */
	public int getCarisma();
	/**
	 * Aumenta il carisma di un personaggio
	 */
	public void addCarisma(int quantita);
	/**
	 * Diminuisce il carisma di un personaggio fino a 0
	 */
	public void subCarisma(int quantita);
	/**
	 * Quanti avversari puo' impegnare un personaggio in combattimento
	 * (combatte con uno ma impedisce agli altri di formulare incantesimi)
	 */
	public int getBersagli();
	/**
	 * La descrizione di questo personaggio
	 */
	public String getDescrizione();
	/**
	 * Il personaggio riposa per diminuire la stanchezza ed aumentare la forza e la magia.
	 * Se il personaggio riposa al coperto (locanda o citta') allora i valori aumentano.
	 */
	public void riposa(int ore, boolean alCoperto);
	/**
	 * Il personaggio fugge
	 */
	public void fugge();
	/**
	 * Il personaggio si impossessa di un artefatto
	 */
	public void addArtefatto(Artefatto a);
	/**
	 * Il personaggio attacca un avversario; questa viene chiamata per i PNG
	 */
	public void attacca(Personaggio bersaglio);
	/**
	 * Il personaggio attacca un gruppo avversario; questa viene chiamata per i PNG
	 */
	public void attacca(Gruppo bersaglio);

	/**
	 * Alcuni PNG possono decidere di aggregarsi al gruppo di un giocatore per
	 * qualche turno.
	 * @param turni il numero di turni per il quale il PNG seguira' un giocatore
	 */
	public void setTempo(int turni);
	/**
	 * Costante che indica che un PNG e' entrato definitivamente nel gruppo di un giocatore
	 */
	public int NO_TEMPO = -1;
	/**
	 * Diminuisce il numero di turni per i quali un PNG rimane con un giocatore.
	 * Quando la funzione restituisce 0 il PNG lascia il gruppo.
	 */
	public int decrementaTempo();
	/**
	 * Ogni personaggio puo' fare differenti tipi di offerta, a seconda della sua classe.
	 */
	public Offerta getOfferta(Comando azione);
	/**
	 * Un personaggio potrebbe essere immune a una certa classe di incantesimi (o più)
	 */
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo);
	
	public PersonaggioMD getModelloDati();
	
	public void setModelloDati(PersonaggioMD modelloDati);
}
