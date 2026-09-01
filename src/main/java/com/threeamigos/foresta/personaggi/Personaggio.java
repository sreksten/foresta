package com.threeamigos.foresta.personaggi;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.OggettoConArticoli;
import com.threeamigos.foresta.motore.modellodati.EffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.PersonaggioMD;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Artefatto;

public interface Personaggio extends OggettoConArticoli {

	public static final Supplier<IllegalArgumentException> PERSONAGGIO_SENZA_NOME = () -> new IllegalArgumentException("Personaggio senza nome!");

	public enum OpzioniGetNome {
		/**
		 * Es. il, la
		 */
		INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
		/**
		 * Es. un, una
		 */
		INCLUDI_ARTICOLO_INDETERMINATIVO_SINGOLARE,
		/**
		 * Es. del, della
		 */
		INCLUDI_PREPOSIZIONE_ARTICOLATA,
		/**
		 * Riporta in maiuscolo la prima lettera del risultato (non necessariamente del nome se preceduto da
		 * preposizione o articolo)
		 */
		INIZIALE_MAIUSCOLA
	}

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
		SALUTE,
		SALUTE_MASSIMA,
		MAGIA,
		MAGIA_MASSIMA,
		CORAGGIO,
		VALORE,
		STANCHEZZA,
		CARISMA
	}

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
	 * Un pronome adatto al personaggio (egli, ella, esso)
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
	 * Indica se questo personaggio sia un personaggio non giocante ossia
	 * controllato dal computer
	 */
	public boolean isPNG();
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
	 * Aggiunge salute a un personaggio (non puo' superare la salute massima)
	 */
	public void addSalute(int quantita);
	/**
	 * Sottrae salute (ferisce) a un personaggio
	 * @param avversario il personaggio responsabile del ferimento
	 * @param notificaFerite se true verra' richiesto all'UI di notificare che il
	 *        personaggio è stato ferito
	 * @param notificaMorte se true verra' richiesto all'UI (in caso di decesso) che
	 *        il personagio è morto
	 */
	public void subSalute(int quantita, Personaggio avversario, NotificaFerite notificaFerite, NotificaMorte notificaMorte);
	/**
	 * Alcuni oggetti possono aumentare la salute massima di un personaggio
	 */
	public void addSaluteMassima(int quantita);
	/**
	 * Quanti danni fa normalmente il personaggio in combattimento
	 */
	public int getDanniInCombattimento();
	/**
	 * Eventuali moltiplicatori ai danni del combattimento, normalmente 1
	 */
	public int getModificaDanniForza();
	/**
	 * Incrementa il livello di magia del personaggio; non puo' superare il livello massimo
	 */
	public void addMagia(int quantita);
	/**
	 * Sottrae magia al personaggio
	 */
	public void subMagia(int quantita);
	/**
	 * Quanti avversari puo' bersagliare il personaggio con un singolo incantesimo
	 */
	public int getBersagliPerIncantesimo();
	/**
	 *  Eventuali moltiplicatori ai danni derivanti da un incantesimo
	 */
	public int getModificaDanniMagia(int danniBase);
	/**
	 * Aumenta il coraggio di un personaggio fino a 99
	 */
	public void addCoraggio(int quantita);
	/**
	 * Diminuisce il coraggio di un personaggio fino a 0
	 */
	public void subCoraggio(int quantita);
	/**
	 * Aumenta il valore di un personaggio fino a 99
	 */
	public void addValore(int quantita);
	/**
	 * Diminuisce il valore di un personaggio fino a 0
	 */
	public void subValore(int quantita);
	/**
	 * Aumenta la stanchezza di un personaggio fino a 9
	 */
	public void addStanchezza(int quantita);
	/**
	 * Diminuisce la stanchezza di un personaggio fino a 0
	 */
	public void subStanchezza(int quantita);
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
	 * Il personaggio riposa per diminuire la stanchezza e aumentare la forza e la magia.
	 * Se il personaggio riposa al coperto (locanda o citta') allora i valori aumentano.
	 */
	public void riposa(int ore, boolean alCoperto);
	/**
	 * Il personaggio fugge
	 */
	public void fugge();
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
	 * @param turni il numero di turni per il quale il PNG seguirà un giocatore
	 */
	public void setTempo(int turni);
	/**
	 * Costante che indica che un PNG è entrato definitivamente nel gruppo di un giocatore
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

	/*
	 * Nuove funzionalità per i personaggi
	 */

	// Statistiche del personaggio

	/**
	 * La classe del personaggio da ClassiPersonaggio
	 */
	public ClassePersonaggio getClasse();

	/**
	 * Il nome proprio del personaggio (puo' non averlo)
	 */
	public Optional<String> getNomeProprio(); // il nome del personaggio

	/**
	 * Un nome per il personaggio corrente. Se è disponibile il nome proprio, restituisce quello.
	 * Altrimenti restituisce il nome singolare del personaggio
	 */
	public String getNome(OpzioniGetNome... opzioni);

	/**
	 * Ritorna la lettera da usare per un aggettivo relativo al personaggio
	 */
	public String getLetteraFinaleAttributo();

	/**
	 * Il livello corrente del personaggio
	 */
	public int getLivello();

	/**
	 * L'esperienza accumulata dal personaggio
	 */
	public int getEsperienza();

	/**
	 * La salute attuale di un personaggio
	 */
	public int getSalute();

	/**
	 * La salute massima del personaggio
	 */
	public int getSaluteMassima();

	/**
	 * La quantita' di salute che il personaggio riacquista riposando, somma del recupero base più
	 * dei modificatori di salute degli artefatti.
	 */
	public int getRecuperoSalute();

	/**
	 * La magia attuale di un personaggio
	 */
	public int getMagia();

	/**
	 * Il livello massimo di magia del personaggio, somma della magia massima base e dei modificatori di magia degli artefatti.
	 */
	public int getMagiaMassima();

	/**
	 * La quantita' di magia che il personaggio riacquista riposando, somma del recupero base più
	 * dei modificatori di magia degli artefatti.
	 */
	public int getRecuperoMagia();

	/**
	 * Il carico attuale di un personaggio
	 */
	public int getCarico();

	/**
	 * Il carico che un personaggio può portare, somma del carico base e dei modificatori di carico degli artefatti.
	 */
	public int getCaricoMassimo();

	/**
	 * La forza di un personaggio, somma della forza base e dei modificatori di forza degli artefatti.
	 */
	public int getForza();

	/**
	 * La destrezza di un personaggio, somma della destrezza base e dei modificatori di destrezza degli artefatti.
	 */
	public int getDestrezza();

	/**
	 * La costituzione di un personaggio, somma della costituzione base e dei modificatori di costituzione degli
	 * artefatti.
	 */
	public int getCostituzione();

	/**
	 * L'intelligenza di un personaggio, somma della intelligenza base e dei modificatori di intelligenza degli
	 * artefatti.
	 */
	public int getIntelligenza();

	/**
	 * La saggezza di un personaggio, somma della saggezza base e dei modificatori di saggezza degli
	 * artefatti.
	 */
	public int getSaggezza();

	/**
	 * Il carisma di un personaggio, somma del carisma base e dei modificatori di carisma degli artefatti.
	 * Da 0 in poi
	 */
	public int getCarisma();

	/**
	 * La fortuna di un personaggio, somma della fortuna base e dei modificatori di fortuna degli artefatti.
	 */
	public int getFortuna();

	/**
	 * Il critico di un personaggio, somma del critico base e dei modificatori di critico degli artefatti.
	 */
	public int getCritico();

	/**
	 * La precisione di un personaggio, somma della precisione base e dei modificatori di precisione degli artefatti.
	 */
	public int getPrecisione();

	/**
	 * La velocità di un personaggio, somma della velocità base e dei modificatori di velocità degli artefatti.
	 */
	public int getVelocita();

	/**
	 * La furtività di un personaggio, somma della furtività base e dei modificatori di furtività degli artefatti.
	 */
	public int getFurtivita();

	/**
	 * La parata di un personaggio, somma della parata base e dei modificatori di parata degli artefatti.
	 */
	public int getParata();

	/**
	 * La resistenza magica di un personaggio, somma della resistenza magica base e dei modificatori di resistenza
	 * magica degli artefatti.
	 */
	public int getResistenzaMagica();

	/**
	 * La percezione di un personaggio, somma della percezione base e dei modificatori di percezione degli artefatti.
	 */
	public int getPercezione();

	/**
	 * La soggezione che un personaggio esercita, somma della soggezione base e dei modificatori di soggezione degli
	 * artefatti.
	 */
	public int getSoggezione();

	/**
	 * La furia di un personaggio, somma della furia base e dei modificatori di furia degli artefatti.
	 */
	public int getFuria();

	/**
	 * Il coraggio di un personaggio, somma del coraggio base e dei modificatori di coraggio degli artefatti.
	 * Da 0 a 99
	 */
	public int getCoraggio();

	/**
	 * Il valore di un personaggio, somma del valore base e dei modificatori di valore degli artefatti.
	 * Da 0 a 99
	 */
	public int getValoreEffettoDiStato();

	/**
	 * La stanchezza di un personaggio, somma della stanchezza base e dei modificatori di stanchezza degli artefatti.
	 * Da 0 a 9
	 */
	public int getStanchezza();

	// Modificatori di stato

	/**
	 * Effetti di stato attivi sul personaggio
	 */
	public Collection<EffettoDiStato> getEffettiDiStato();

	/**
	 * Aggiunge un effetto di stato al personaggio
	 */
	public void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int valore);

	/**
	 * Verifica se un personaggio ha un effetto di stato
	 */
	public boolean hasEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato);

	/**
	 * Restituisce il valore di un effetto di stato
	 */
	public int getValoreEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato);

	/**
	 * Rimuove un effetto di stato dal personaggio
	 */
	public void removeEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato);

	// Artefatti

	/**
	 * Restituisce l'inventario del personaggio
	 */
	public Collection<Artefatto> getInventario();

	/**
	 * Aggiunge un artefatto all'inventario del personaggio - questo si riflette in modo automatico sui modificatori
	 * di stato del personaggio
	 */
	public void addArtefatto(Artefatto artefatto);

	/**
	 * Rimuove un artefatto dall'inventario del personaggio - questo si riflette in modo automatico sui modificatori
	 * di stato del personaggio
	 */
	public void removeArtefatto(Artefatto artefatto);

	// Vari metodi

}
