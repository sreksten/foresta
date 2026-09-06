package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.OggettoConArticoli;
import com.threeamigos.foresta.motore.modellodati.EffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.PersonaggioMD;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Artefatto;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface Personaggio extends OggettoConArticoli {

	Supplier<IllegalArgumentException> PERSONAGGIO_SENZA_NOME = () -> new IllegalArgumentException("Personaggio senza nome!");

	enum OpzioniGetNome {
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

	enum NotificaFerite {
		SI,
		NO
    }

	enum NotificaMorte {
		SI,
		NO
    }

	enum Sesso {
		MASCHIO,
		FEMMINA
    }

	/**
	 * Di + articolo determinativo singolare
	 */
    String getDeS();
	/**
	 * Di + articolo determinativo plurale
	 */
    String getDeP();
	/**
	 * Da (ucciso da)
	 */
    String getDa();

	/**
	 * L'immagine che rappresenta il personaggio
	 */
    BufferedImage getImmagine();
	/**
	 * L'icona che rappresenta il personaggio
	 */
    BufferedImage getIcona();
	/**
	 * Il nome singolare della razza del personaggio
	 */
    String getNomeSingolare();
	/**
	 * Il nome plurale dela razza del personaggio
	 */
    String getNomePlurale();
	/**
	 * Un pronome adatto al personaggio (egli, ella, esso)
	 */
    String getPronome();

	/**
	 * Usata da LocazioneBase per impostare il numero del personaggio che viene poi
	 * utilizzato duranre i combattimenti (il primo mostro non ce l'ha fatta...)
	 */
    void setOrdinale(int ordinale);
	/**
	 * Ritorna l'ordinale del personaggio all'interno del gruppo
	 */
    int getOrdinale();

	/**
	 * Il sesso del personaggio da ClassiPersonaggio
	 */
    Sesso getSesso();
	/**
	 * Indica se sia possibile o meno cercare di corrompere questo personaggio
	 */
    boolean isCorrompibile();
	/**
	 * Indica se sia possibile o meno cercare di fare amicizia con questo personaggio
	 */
    boolean isAmichevole();
	/**
	 * Indica se questo personaggio sia in grado di usare la magia
	 */
    boolean isMagico();

	/**
	 * Indica se questo personaggio sia un personaggio non giocante ossia
	 * controllato dal computer
	 */
    boolean isPNG();
	/**
	 * Indica se il personaggio sia immortale
	 */
    boolean isImmortale();
	/**
	 * Indica se il personaggio sia vivo
	 */
    boolean isVivo();
	/**
	 * Il personaggio muore a causa di qualcosa
	 */
    void muore(String causaTrapasso);
	/**
	 * Cosa ha provocato la morte del personaggio?
	 */
    String getCausaTrapasso();
	/**
	 * Il personaggio resuscita
	 */
    void resuscita();
	/**
	 * Aggiunge salute a un personaggio (non puo' superare la salute massima)
	 */
    void addSalute(int quantita);
	/**
	 * Sottrae salute (ferisce) a un personaggio
	 * @param avversario il personaggio responsabile del ferimento
	 * @param notificaFerite se true verra' richiesto all'UI di notificare che il
	 *        personaggio è stato ferito
	 * @param notificaMorte se true verra' richiesto all'UI (in caso di decesso) che
	 *        il personagio è morto
	 */
    void subSalute(int quantita, Personaggio avversario, NotificaFerite notificaFerite, NotificaMorte notificaMorte);
	/**
	 * Alcuni oggetti possono aumentare la salute massima di un personaggio
	 */
    void addSaluteMassima(int quantita, String note);
	/**
	 * Quanti danni fa normalmente il personaggio in combattimento
	 */
    int getDanniInCombattimento();
	/**
	 * Incrementa il livello di magia del personaggio; non puo' superare il livello massimo
	 */
    void addMagia(int quantita);
	/**
	 * Alcuni oggetti possono aumentare la salute massima di un personaggio
	 */
	void addMagiaMassima(int quantita, String note);
	/**
	 * Sottrae magia al personaggio
	 */
    void subMagia(int quantita);
	/**
	 *  Eventuali moltiplicatori ai danni derivanti da un incantesimo
	 */
    int getModificaDanniMagia(int danniBase);
	/**
	 * Aumenta il coraggio di un personaggio fino a 99
	 */
    void addCoraggio(int quantita);
	/**
	 * Diminuisce il coraggio di un personaggio fino a 0
	 */
    void subCoraggio(int quantita);
	/**
	 * Aumenta il valore di un personaggio fino a 99
	 */
    void addValore(int quantita);
	/**
	 * Diminuisce il valore di un personaggio fino a 0
	 */
    void subValore(int quantita);
	/**
	 * Aumenta la stanchezza di un personaggio fino a 9
	 */
    void addStanchezza(int quantita);
	/**
	 * Diminuisce la stanchezza di un personaggio fino a 0
	 */
    void subStanchezza(int quantita);
	/**
	 * Aumenta il carisma di un personaggio
	 */
    void addCarisma(int quantita);
	/**
	 * Diminuisce il carisma di un personaggio fino a 0
	 */
    void subCarisma(int quantita);
	/**
	 * Quanti avversari può impegnare un personaggio in combattimento
	 * (combatte con uno ma impedisce agli altri di formulare incantesimi, oppure cerca di colpire con un
	 * incantesimo quel numero di bersagli)
	 */
    int getBersagli();
	/**
	 * La descrizione di questo personaggio
	 */
    String getDescrizione();
	/**
	 * Il personaggio riposa per diminuire la stanchezza e aumentare la forza e la magia.
	 * Se il personaggio riposa al coperto (locanda o città) o davanti a un fuoco allora i valori aumentano.
	 */
    void riposa(int ore, TipoRiposo tipoRiposo);
	/**
	 * Il personaggio fugge
	 */
    void fugge();
	/**
	 * Il personaggio attacca un avversario; questa viene chiamata per i PNG
	 */
    void attacca(Personaggio bersaglio);
	/**
	 * Il personaggio attacca un gruppo avversario; questa viene chiamata per i PNG
	 */
    void attacca(Gruppo bersaglio);

	/**
	 * Alcuni PNG possono decidere di aggregarsi al gruppo di un giocatore per
	 * qualche turno.
	 * @param turni il numero di turni per il quale il PNG seguirà un giocatore
	 */
    void setTempo(int turni);

	/**
	 * Indica che un personaggio si ferma nel gruppo per un certo numero di turni.
	 */
    boolean isATempo();
	/**
	 * Diminuisce il numero di turni per i quali un PNG rimane con un giocatore.
	 * Quando la funzione restituisce 0 il PNG lascia il gruppo.
	 */
    int decrementaTempo();
	/**
	 * Ogni personaggio puo' fare differenti tipi di offerta, a seconda della sua classe.
	 */
    Offerta getOfferta(Comando azione);
	/**
	 * Un personaggio potrebbe essere immune a una certa classe di incantesimi (o più)
	 */
    boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo);

	PersonaggioMD getModelloDati();

	void setModelloDati(PersonaggioMD modelloDati);

	/*
	 * Nuove funzionalità per i personaggi
	 */

	// Statistiche del personaggio

	/**
	 * La classe del personaggio da ClassiPersonaggio
	 */
    ClassePersonaggio getClasse();

	/**
	 * Il nome proprio del personaggio (puo' non averlo)
	 */
    Optional<String> getNomeProprio(); // il nome del personaggio

	/**
	 * Un nome per il personaggio corrente. Se è disponibile il nome proprio, restituisce quello.
	 * Altrimenti restituisce il nome singolare del personaggio
	 */
    String getNome(OpzioniGetNome... opzioni);

	/**
	 * Ritorna la lettera da usare per un aggettivo relativo al personaggio
	 */
    String getLetteraFinaleAttributo();

	/**
	 * Il livello corrente del personaggio
	 */
    int getLivello();

	/**
	 * L'esperienza accumulata dal personaggio
	 */
    int getEsperienza();

	/**
	 * Aggiunge punti esperienza a un personaggio.
	 */
	void addPuntiEsperienza(int puntiEsperienza);

	/**
	 * Restituisce il numero di punti abilità attribuibili
	 */
	int getPuntiAbilitaDisponibili();

	/**
	 * La salute attuale di un personaggio
	 */
    int getSalute();

	/**
	 * La salute massima del personaggio
	 */
    int getSaluteMassima();

	/**
	 * La quantita' di salute che il personaggio riacquista riposando, somma del recupero base più
	 * dei modificatori di salute degli artefatti.
	 */
    int getRecuperoSalute();

	/**
	 * La magia attuale di un personaggio
	 */
    int getMagia();

	/**
	 * Il livello massimo di magia del personaggio, somma della magia massima base e dei modificatori di magia degli artefatti.
	 */
    int getMagiaMassima();

	/**
	 * La quantita' di magia che il personaggio riacquista riposando, somma del recupero base più
	 * dei modificatori di magia degli artefatti.
	 */
    int getRecuperoMagia();

	/**
	 * Il carico attuale di un personaggio
	 */
    double getCarico();

	/**
	 * Il carico che un personaggio può portare, somma del carico base e dei modificatori di carico degli artefatti.
	 */
    int getCaricoMassimo();

	/**
	 * La forza di un personaggio, somma della forza base e dei modificatori di forza degli artefatti.
	 */
    int getForza();

	/**
	 * La destrezza di un personaggio, somma della destrezza base e dei modificatori di destrezza degli artefatti.
	 */
    int getDestrezza();

	/**
	 * La costituzione di un personaggio, somma della costituzione base e dei modificatori di costituzione degli
	 * artefatti.
	 */
    int getCostituzione();

	/**
	 * L'intelligenza di un personaggio, somma della intelligenza base e dei modificatori di intelligenza degli
	 * artefatti.
	 */
    int getIntelligenza();

	/**
	 * La saggezza di un personaggio, somma della saggezza base e dei modificatori di saggezza degli
	 * artefatti.
	 */
    int getSaggezza();

	/**
	 * Il carisma di un personaggio, somma del carisma base e dei modificatori di carisma degli artefatti.
	 */
    int getCarisma();

	/**
	 * La fortuna di un personaggio, somma della fortuna base e dei modificatori di fortuna degli artefatti.
	 */
    int getFortuna();

	/**
	 * Il critico di un personaggio, somma del critico base e dei modificatori di critico degli artefatti.
	 */
    int getCritico();

	/**
	 * La precisione di un personaggio, somma della precisione base e dei modificatori di precisione degli artefatti.
	 */
    int getPrecisione();

	/**
	 * La velocità di un personaggio, somma della velocità base e dei modificatori di velocità degli artefatti.
	 */
    int getVelocita();

	/**
	 * La furtività di un personaggio, somma della furtività base e dei modificatori di furtività degli artefatti.
	 */
    int getFurtivita();

	/**
	 * La parata di un personaggio, somma della parata base e dei modificatori di parata degli artefatti.
	 */
    int getParata();

	/**
	 * La resistenza magica di un personaggio, somma della resistenza magica base e dei modificatori di resistenza
	 * magica degli artefatti.
	 */
    int getResistenzaMagica();

	/**
	 * La percezione di un personaggio, somma della percezione base e dei modificatori di percezione degli artefatti.
	 */
    int getPercezione();

	/**
	 * La soggezione che un personaggio esercita, somma della soggezione base e dei modificatori di soggezione degli
	 * artefatti.
	 */
    int getSoggezione();

	/**
	 * La furia di un personaggio, somma della furia base e dei modificatori di furia degli artefatti.
	 */
    int getFuria();

	/**
	 * Il coraggio di un personaggio, somma del coraggio base e dei modificatori di coraggio degli artefatti.
	 */
    int getCoraggio();

	/**
	 * Il valore di un personaggio, somma del valore base e dei modificatori di valore degli artefatti.
	 */
    int getValore();

	/**
	 * La stanchezza di un personaggio, somma della stanchezza base e dei modificatori di stanchezza degli artefatti.
	 */
    int getStanchezza();

	/**
	 * Punti esperienza derivanti dalla sconfitta
	 */
	int getPuntiEsperienza();

	// Modificatori di stato

	/**
	 * Effetti di stato attivi sul personaggio
	 */
    Collection<EffettoDiStato> getEffettiDiStato();

	/**
	 * Aggiunge un effetto di stato al personaggio
	 */
    void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int valore);

	/**
	 * Verifica se un personaggio ha un effetto di stato
	 */
    boolean hasEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato);

	/**
	 * Restituisce il valore di un effetto di stato
	 */
    int getQuantitaEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato);

	/**
	 * Rimuove un effetto di stato dal personaggio
	 */
    void removeEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato);

	// Artefatti

	/**
	 * Restituisce l'inventario del personaggio
	 */
    Collection<Artefatto> getInventario();

	/**
	 * Aggiunge un artefatto all'inventario del personaggio - questo si riflette in modo automatico sui modificatori
	 * di stato del personaggio
	 */
    void addArtefatto(Artefatto artefatto);

	/**
	 * Rimuove un artefatto dall'inventario del personaggio - questo si riflette in modo automatico sui modificatori
	 * di stato del personaggio
	 */
    void removeArtefatto(Artefatto artefatto);

	double getSaluteBase();

	double getLivellamentoSalute();

	double getMagiaBase();

	double getLivellamentoMagia();

	double getMoltiplicatoreCarico();

	String getNoteMoltiplicatoreCarico();

	double getMoltiplicatoreCritico();

	String getNoteMoltiplicatoreCritico();

	double getMoltiplicatorePrecisione();

	String getNoteMoltiplicatorePrecisione();

	double getMoltiplicatoreVelocita();

	String getNoteMoltiplicatoreVelocita();

	double getMoltiplicatoreFurtivita();

	String getNoteMoltiplicatoreFurtivita();

	double getMoltiplicatoreParata();

	String getNoteMoltiplicatoreParata();

	double getMoltiplicatoreResistenzaMagica();

	String getNoteMoltiplicatoreResistenzaMagica();

	double getMoltiplicatorePercezione();

	String getNoteMoltiplicatorePercezione();

	double getMoltiplicatoreSoggezione();

	String getNoteMoltiplicatoreSoggezione();

	double getMoltiplicatoreFuria();

	String getNoteMoltiplicatoreFuria();

	double getMoltiplicatoreCoraggio();

	String getNoteMoltiplicatoreCoraggio();

	double getMoltiplicatoreValore();

	String getNoteMoltiplicatoreValore();

	double getMoltiplicatoreNumeroBersagli();

	String getNoteMoltiplicatoreNumeroBersagli();

	double getMoltiplicatoreStanchezza();

	String getNoteMoltiplicatoreStanchezza();

	double getMoltiplicatoreDanniMagici();

	String getNoteMoltiplicatoreDanniMagici();

	double getMoltiplicatoreDanniFisici();

	String getNoteMoltiplicatoreDanniFisici();

	double getMoltiplicatoreRecuperoMagico();

	String getNoteMoltiplicatoreRecuperoMagico();

	double getMoltiplicatoreRecuperoFisico();

	String getNoteMoltiplicatoreRecuperoFisico();

}
