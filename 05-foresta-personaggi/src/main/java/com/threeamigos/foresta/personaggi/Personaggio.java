package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoApproccio;
import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.interfacce.OggettoConArticoli;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.tipi.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Un personaggio del gioco, giocante o mostro, con tutto quel che il motore e i poteri gli possono chiedere o fare. Le letture
 * stanno in VistaPersonaggio, che è quel che ne vede la UI (in sola lettura); qui si aggiungono le operazioni che lo cambiano
 * (ferite, cure, stati, modificatori, esperienza, inventario) e i valori della sua classe (i moltiplicatori in fondo, che ogni
 * classe prende da CostantiPersonaggi). L'implementazione è PersonaggioBase.
 */
public interface Personaggio extends VistaPersonaggio, OggettoConArticoli, ScambiatoreArtefatti {

	@Override
	Collection<Artefatto> getInventario();

	/**
	 * Se, subendo un danno, il personaggio deve notificare le ferite.
	 */
	enum NotificaFerite {
		SI,
		NO
    }

	/**
	 * Se, morendo, il personaggio deve notificare la morte.
	 */
	enum NotificaMorte {
		SI,
		NO
    }

	/**
	 * Il sesso del personaggio, da cui dipendono gli articoli e i pronomi dei testi.
	 */
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
	 * Il nome singolare della classe del personaggio
	 */
    String getNomeSingolare();
	/**
	 * Il nome plurale della classe del personaggio
	 */
    String getNomePlurale();
	/**
	 * Un pronome adatto al personaggio (egli, ella, esso)
	 */
    String getPronome();

	/**
	 * Usata da LocazioneBase per impostare il numero del personaggio che viene poi
	 * utilizzato durante i combattimenti (il primo mostro non ce l'ha fatta...)
	 */
    void setOrdinale(int ordinale);
	/**
	 * Ritorna l'ordinale del personaggio all'interno del gruppo
	 */
    @Override
    int getOrdinale();

	/**
	 * Il sesso del personaggio: lo dice la sua classe (la Maga, il Mago)
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
     * Gli incantesimi malefici che il personaggio sa lanciare per natura quando attacca (vedi PersonaggioBase): non servono
     * pergamene, li paga con la Magia. Vale per chi è magico (isMagico).
     */
    Set<TipoIncantesimo> getRepertorioIncantesimi();

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
	 * Se adesso il personaggio non può morire: un danno che lo ucciderebbe lo lascia con un punto ferita, e lui continua a combattere. Vale
	 * per chi si è unito al gruppo durante il tutorial finché ci resta (vedi PersonaggioMD.isSalvoFinoAllaScadenza) e per chi il motore
	 * protegge (il capo, mentre il tutorial pilota: vedi PersonaggioBase.impostaProtezioneDallaMorte).
	 */
    boolean isSalvoDallaMorte();
	/**
	 * Se il personaggio si può curare: false per un non morto entrato nel gruppo del giocatore (lo Scheletro della
	 * Mietitura), che le cure non guariscono. Non deve mai essere un bersaglio possibile di pozioni di salute,
	 * canto curativo e Resurrezione: né per le tattiche né per chi sceglie a mano. Un non morto avversario resta
	 * un bersaglio come gli altri (un gruppo di soli non morti ha comunque un "più ferito").
	 */
    boolean isCurabile();

	/**
	 * Se per un effetto di stato (ATTERRATO, CONGELATO, STORDITO) il personaggio salta il turno: non attacca, non formula, non canta, non beve.
	 */
    default boolean isIncapaceDiAgire() {
		return hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) || hasEffettoDiStato(TipoEffettoDiStato.CONGELATO)
				|| hasEffettoDiStato(TipoEffettoDiStato.STORDITO);
	}
	/**
	 * Indica se il personaggio sia vivo
	 */
    boolean isVivo();
	/**
	 * Se il personaggio è in panchina: vivo, ma fuori dal combattimento di questa locazione (perché si è arreso, o
	 * perché in un duello combatte un altro). Chi è in panchina non conta fra i vivi del suo gruppo (vedi
	 * Gruppo.getPersonaggiVivi). La panchina si svuota a ogni nuova locazione.
	 */
    boolean isInPanchina();
    void setInPanchina(boolean inPanchina);
    @Override
    default String getUuid() {
        return getModelloDati().getUuid();
    }

	/**
	 * Se il personaggio non può combattere: è morto o in panchina.
	 */
    default boolean isFuoriCombattimento() {
        return !isVivo() || isInPanchina();
    }
	/**
	 * Se il personaggio combatte fino alla resa e non all'ultimo sangue (uno sfidante in un duello): quando i suoi
	 * punti ferita finirebbero si arrende e va in panchina, invece di morire. Contro di lui si arrendono anche i
	 * personaggi del gruppo.
	 */
    boolean isFinoAllaResa();
    void setFinoAllaResa(boolean finoAllaResa);
	/**
	 * Se il personaggio sfida a duello: si combatte uno contro uno, e chi del gruppo non lo affronta va in
	 * panchina (vedi LocazioneBase).
	 */
    boolean isSfidante();
    void setSfidante(boolean sfidante);
	/**
	 * Se il personaggio sfida il capo del gruppo a carta, forbici e sasso appena il gruppo entra in locazione: è uno
	 * dei campioni de La sfida dei campioni (vedi LocazioneBase e carta_forbici_sasso.md).
	 */
    boolean isSfidanteACartaForbiciSasso();
    void setSfidanteACartaForbiciSasso(boolean sfidante);
	/**
	 * Se il personaggio va affrontato per forza: lo vuole una missione (vedi IncontroDiMissione). Allora non si può
	 * passare inosservati, né corromperlo, né farci amicizia (vedi LocazioneBase).
	 */
    boolean isDaAffrontare();
    void setDaAffrontare(boolean daAffrontare);
	/**
	 * Un modificatore di un attributo: permanente (una benedizione, per esempio), che resta finché non lo si toglie,
	 * oppure a tempo. Uno a tempo uguale a uno già presente non si somma: ne rinfresca la durata, se è più lunga.
	 * Va dato con bersaglio PORTATORE: quelli sul bersaglio sono delle armi, e la copia che l'arma mette sul
	 * bersaglio colpito è suPortatore().
	 */
    void addModificatore(ModificatoreAttributoMD modificatore);

	/**
	 * Fa scorrere di un'unità il tempo dei modificatori misurati in quell'unità: a zero se ne vanno
	 */
	void riduciModificatori(UnitaDurataModificatore unitaDurata);

	/**
	 * Toglie i modificatori che scadono, benefici o no, tranne quelli non rimovibili come l'ignominia della fuga (vedi AlbaSacra)
	 */
	void rimuoviIModificatoriTemporaneiRimovibili();
	/**
	 * Il personaggio muore a causa di qualcosa
	 */
    void muore(String causaTrapasso);

    /**
     * Se sta dalla parte del giocatore: nel suo gruppo o fra i suoi ospiti.
     */
    boolean appartieneAlGruppoDelGiocatore();

    /**
     * I boss: partono con salute e magia piene e non possono essere mietuti.
     */
    boolean isParteConValoriMassimi();
	/**
	 * Cosa ha provocato la morte del personaggio?
	 */
    String getCausaTrapasso();
	/**
	 * Il personaggio resuscita
	 */
    void resuscita();
	/**
	 * Aggiunge salute a un personaggio (non può superare la salute massima)
	 */
    void addSalute(int quantita);
	/**
	 * Sottrae salute (ferisce) a un personaggio
	 * @param avversario il personaggio responsabile del ferimento
	 * @param notificaFerite con SI si pubblicano le notifiche delle ferite (la UI le mostra)
	 * @param notificaMorte con SI, se muore, si pubblica la notifica della morte
	 */
    void subSalute(int quantita, Personaggio avversario, NotificaFerite notificaFerite, NotificaMorte notificaMorte);
	/**
	 * Alcuni oggetti possono aumentare la salute massima di un personaggio
	 */
    void addSaluteMassima(int quantita, String note);
	/**
	 * Incrementa il livello di magia del personaggio; non puo' superare il livello massimo
	 */
    void addMagia(int quantita);
	/**
	 * Alcuni oggetti possono aumentare la magia massima di un personaggio (la pozione della magia grande)
	 */
	void addMagiaMassima(int quantita, String note);
	/**
	 * Sottrae magia al personaggio
	 */
    void subMagia(int quantita);
	/**
	 * Il POTERE_MAGICO come percentuale: 100 senza modificatori, di più con bastoni e libri magici
	 * (es. un bastone +20% lo porta a 120). Non dipende dalla riserva di Magia.
	 */
    double getPotereMagico();
	/**
	 * I danni di un incantesimo modificati dal POTERE_MAGICO di chi lo lancia (bastoni, libri)
	 */
    double getModificaDanniMagia(double danni);
	/**
	 * Aumenta la stanchezza di un personaggio fino a CostantiPersonaggi.MAX_STANCHEZZA
	 */
    void addStanchezza(int quantita);
	/**
	 * Diminuisce la stanchezza di un personaggio fino a 0
	 */
    void subStanchezza(int quantita);
	/**
	 * Aumenta (o, con una quantità negativa, diminuisce) un attributo primario del personaggio, e avvisa chi ascolta.
	 */
    void add(TipoAttributo tipoAttributo, int quantita);
	/**
	 * Quanti bersagli colpisce al massimo un suo potere a più bersagli: un incantesimo MULTIPLO, un canto, un'abilità di danno.
	 * Almeno uno.
	 */
    int getBersagli();
	/**
	 * La descrizione di questo personaggio
	 */
    String getDescrizione();
	/**
	 * Il personaggio riposa: cala la stanchezza, tornano salute e magia (vedi CalcolatoreRiposo). Quanto, dipende dalle ore e da
	 * dove riposa (vedi TipoRiposo: all'aperto senza fuoco la metà, al coperto una volta e mezza).
	 */
    void riposa(int ore, TipoRiposo tipoRiposo);
	/**
	 * Il personaggio fugge
	 */
    void fugge();

	/**
	 * Alcuni PNG possono decidere di aggregarsi al gruppo di un giocatore per
	 * qualche turno: il tempo conta solo per chi è a tempo (vedi {@link #isATempo()}).
	 * @param turni il numero di turni per il quale il PNG seguirà un giocatore
	 */
    void setTempo(int turni);

	/**
	 * Perché il personaggio si trova nel gruppo del giocatore (vedi TipoMotivoNelGruppo).
	 */
    void setMotivoNelGruppo(TipoMotivoNelGruppo motivo);

    TipoMotivoNelGruppo getMotivoNelGruppo();

	/**
	 * Indica che un personaggio si ferma nel gruppo per un certo numero di turni (aiuto, corrotto, scheletro della mietitura,
	 * compagno di una missione: vedi TipoMotivoNelGruppo.isATempo).
	 */
    boolean isATempo();
	/**
	 * Diminuisce il numero di turni per i quali un PNG rimane con un giocatore.
	 * Quando la funzione restituisce 0 (o meno) il PNG lascia il gruppo.
	 */
    int decrementaTempo();
	/**
	 * Ogni personaggio puo' fare differenti tipi di offerta, a seconda della sua classe: questo è il tipo di quella che fa a chi gli fa
	 * amicizia o lo corrompe ({@code azione}), scelto a caso fra i suoi (a gruppo pieno nessuno si può unire, quindi senza gli aiuti), o
	 * null se non ne ha. A creare l'offerta ci pensa chi la usa (vedi FabbricaOfferte).
	 */
    TipoOfferta getOfferta(TipoApproccio approccio);
	/**
	 * Un personaggio potrebbe essere immune a una certa classe di incantesimi (o più)
	 */
    boolean isImmuneAIncantesimo(TipoIncantesimo tipoIncantesimo);
	/**
	 * Un personaggio potrebbe essere immune a certi tipi di attacco
	 */
	boolean isImmuneATipoDanno(TipoDanno tipoDanno);
	/**
	 * Un personaggio potrebbe essere immune a un effetto di stato
	 */
	boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato);
	/**
	 * Per quanto si moltiplica il danno di quel tipo contro il personaggio: 1 se non cambia, 0 se è immune, tra 0 e 1 se
	 * resiste, più di 1 se è debole (i non morti subiscono il doppio dal SACRO). Lo dicono la sua classe e le sue categorie
	 * (vedi TipoPersonaggio.getMoltiplicatoreDannoSubito).
	 */
	double getMoltiplicatoreDannoSubito(TipoDanno tipoDanno);
	/**
	 * Restituisce il modello dati del personaggio.
	 */
	PersonaggioMD getModelloDati();

	void setModelloDati(PersonaggioMD modelloDati);

	/**
	 * La classe del personaggio
	 */
    TipoPersonaggio getClasse();

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
	 * La lettera finale di un aggettivo italiano riferito al personaggio: "o" per un maschio, "a" per una femmina (la usano le
	 * notizie, vedi RegistroNotizie)
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
	 * Porta al livello dato un personaggio che entra nel gruppo, di norma un compagno reclutato (vedi
	 * RegistroPersonaggi.preparaCompagno): imposta il livello, l'esperienza minima di quel livello e aggiunge ai punti
	 * abilità da spendere uno per ogni livello guadagnato, come se li avesse conquistati sul campo.
	 * <p>
	 * Esiste perché addPuntiEsperienza non è adatta: a ogni salita di livello pubblica NotificaAumentoLivelloPersonaggio,
	 * da cui la UI ricava il "LEVEL UP!" a tutto schermo, il messaggio nel notiziario e lo sprite nel riquadro del
	 * gruppo. Un compagno che arriva già al livello 4 non ha appena fatto un'impresa, e quegli annunci al momento
	 * del reclutamento sarebbero fuori luogo. Questo metodo quindi non pubblica nessuna notifica di livello: chi
	 * lo chiama lo fa prima di aggiungere il personaggio al gruppo.
	 * <p>
	 * Non abbassa mai il livello: se il personaggio è già a quel livello o oltre, non fa nulla. L'esperienza
	 * non scende sotto quella già accumulata.
	 *
	 * @param livello il livello da raggiungere
	 */
	void impostaLivelloDopoReclutamento(int livello);

	/**
	 * Restituisce il numero di punti abilità attribuibili
	 */
	int getPuntiAbilitaDisponibili();

	/**
	 * Se il tipo di attributo è primario e il personaggio dispone di punti abilità,
	 * ne aumenta il valore di uno e sottrae un punto abilità disponibile. Non ha
	 * effetto altrimenti.
	 */
	void spendiPuntoAbilita(TipoAttributo tipoAttributo);

	/**
	 * La salute attuale di un personaggio
	 */
    int getSalute();

	/**
	 * La salute massima del personaggio
	 */
    int getSaluteMassima();

	/**
	 * La salute che il personaggio recupera a fine locazione: una base (5 più una quota della salute massima) con i modificatori,
	 * aumentata dalla COSTITUZIONE e moltiplicata per il recupero fisico della classe (0 per chi non guarisce, come i non morti).
	 */
    int getRigenerazioneSalute();

	/**
	 * La magia attuale di un personaggio
	 */
    int getMagia();

	/**
	 * Il livello massimo di magia del personaggio, somma della magia massima base e dei modificatori di magia degli artefatti.
	 */
    int getMagiaMassima();

	/**
	 * La magia che il personaggio recupera a fine locazione: una base (5 più una quota della magia massima) con i modificatori,
	 * moltiplicata per il recupero magico della classe.
	 */
    int getRigenerazioneMagia();

	/**
	 * Il carico attuale di un personaggio
	 */
    double getCarico();

	/**
	 * Il carico che un personaggio può portare, somma del carico base e dei modificatori di carico degli artefatti.
	 */
    int getCaricoMassimo();

	/**
	 * Se il personaggio può prendere l'artefatto nel suo equipaggiamento: vuoto se può, altrimenti il motivo
	 * del rifiuto (troppo carico, slot occupato, pergamena, livello troppo alto, seconda arma non consentita,
	 * mani occupate o arma a due mani impugnata).
	 */
	Optional<RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento> puoEquipaggiare(Artefatto artefatto);

	/**
	 * Un Personaggio può formulare un incantesimo se ha abbastanza magia per farlo e se non è silenziato
	 * @param tipoIncantesimo il tipo di incantesimo che si vorrebbe lanciare
	 */
	boolean puoFormulare(TipoIncantesimo tipoIncantesimo);

	/**
	 * Punti esperienza derivanti dalla sconfitta
	 */
	int getPuntiEsperienza();

	/**
	 * Effetti di stato attivi sul personaggio
	 */
    Collection<EffettoDiStatoMD> getEffettiDiStato();

	/**
	 * Aggiunge un effetto di stato al personaggio
	 */
    void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int durata, int danniNelTempo);

	/**
	 * Come sopra, per gli stati che hanno una forza (vedi EffettoDiStatoMD.getIntensita). Se c'è già lo stesso effetto
	 * non si somma: si tengono la durata e l'intensità più alte.
	 */
    void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int durata, int danniNelTempo, double intensita);

	/**
	 * Verifica se un personaggio ha un effetto di stato
	 */
    boolean hasEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato);

	/**
	 * Applica danni da effetti di stato tipo sanguinamento
	 */
	void applicaDanniDaEffettiDiStato();

	/**
	 * Riduce la durata di un effetto di stato
	 */
	void riduciEffettiDiStato();

	/**
	 * I turni che mancano a riavere un'abilità innata o un canto appena usato (per nome, il {@code name()} del suo tipo); zero se è pronto.
	 */
	int getRicarica(String nome);

	/**
	 * Le abilità innate e i canti ancora in ricarica, per nome, con i turni che mancano.
	 */
	Map<String, Integer> getRicariche();

	/**
	 * Mette in ricarica un'abilità innata o un canto appena usato: per quanti turni non si può riusare (con zero è subito pronto).
	 */
	void setRicarica(String nome, int turni);

	/**
	 * Fa passare un turno al personaggio: gli effetti di stato fanno i loro danni e scadono un poco, i modificatori a turni e le ricariche calano di uno.
	 */
	default void trascorriUnTurno() {
		applicaDanniDaEffettiDiStato();
		riduciEffettiDiStato();
		riduciModificatori(UnitaDurataModificatore.TURNI);
		riduciRicariche();
	}

	/**
	 * Fa passare un turno per tutte le abilità e i canti in ricarica: quelli a zero tornano pronti.
	 */
	void riduciRicariche();

	/**
	 * Restituisce il valore di un effetto di stato
	 */
    int getQuantitaEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato);

	/**
	 * Rimuove tutti gli effetti di stato da un personaggio
	 */
	void rimuoviTuttiGliEffettiDiStato();
	/**
	 * Toglie gli effetti di stato che sono un male e lascia quelli benefici (vedi TipoEffettoDiStato.isBenefico):
	 * l'ispirazione del Bardo e il berserk del Guerriero. È quello che fa l'Alba sacra.
	 */
	void rimuoviGliEffettiDiStatoNegativi();

	/**
	 * Rimuove un effetto di stato dal personaggio
	 */
    void rimuoviEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato);

	/**
	 * L'arma della mano principale (o a due mani); senza, l'arma naturale della classe (artigli, morso, tocco gelido...)
	 */
	Arma getArmaEquipaggiata();

	/**
	 * L'arma della mano secondaria, per chi combatte con due armi (Ladro, Elfo)
	 */
	Optional<Arma> getArmaSecondaria();

	// I valori della classe, uguali per tutti i suoi personaggi: PersonaggioBase li legge dalla tabella CostantiPersonaggi.valoriDi (vedi
	// ValoriDiClasse). I moltiplicatori valgono 1 nella media, di più per un punto di forza, di meno per un punto debole.

	double getSaluteBase();

	double getMagiaBase();

	double getLivellamentoMagia();

	double getMoltiplicatoreCarico();

	double getMoltiplicatoreCritico();

	double getMoltiplicatorePrecisione();

	double getMoltiplicatoreVelocita();

	double getMoltiplicatoreFurtivita();

	double getMoltiplicatoreParata();

	double getMoltiplicatoreResistenzaMagica();

	double getMoltiplicatorePercezione();

	double getMoltiplicatoreSoggezione();

	double getMoltiplicatoreFuria();

	double getMoltiplicatoreCoraggio();

	double getMoltiplicatoreValore();

	double getMoltiplicatoreContrattazione();

	double getMoltiplicatoreNumeroBersagli();

	double getMoltiplicatoreStanchezza();

	double getMoltiplicatoreDanniMagici();

	double getMoltiplicatoreDanniFisici();

	double getMoltiplicatoreRecuperoMagico();

	double getMoltiplicatoreRecuperoFisico();

	/**
	 * L'intelligenza massima della classe (CostantiPersonaggi.<classe>_MAX_INTELLIGENZA): non quella vera del personaggio, che
	 * distribuisce LanciatoreDeiDadi, ma il riferimento della classe per l'astuzia delle sue tattiche (Astuzia).
	 */
	double getMaxIntelligenza();

}
