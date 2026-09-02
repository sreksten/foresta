package com.threeamigos.foresta.motore.modellodati;

/**
 * Rappresenta le statistiche e le caratteristiche modificabili di personaggi ed equipaggiamento.
 * Definisce come ogni attributo influenza il comportamento, le azioni e le dinamiche
 * di combattimento sia in fase offensiva che difensiva.
 *
 * @author Stefano Reksten
 */
public enum TipoAttributo {

    /**
     * Salute fisica e resistenza complessiva. Quando la salute corrente scende a zero, il personaggio muore.
     */
    SALUTE("Salute fisica e resistenza complessiva."),

    /**
     * Forza fisica bruta e potenza muscolare.
     * <p><b>In attacco:</b> Incrementa direttamente il danno dei colpi in mischia (Contundenti/Taglienti) e determina
     * la capacità di sfondare le parate nemiche.</p>
     * <p><b>In difesa:</b> Permette di resistere agli effetti di spostamento fisico (es. respingimenti da Aria o Sonico)
     * riduce la fatica quando si bloccano attacchi pesanti.</p>
     */
    FORZA("Forza fisica e potenza nei colpi."),

    /**
     * Agilità, coordinazione e riflessi.
     * <p><b>In attacco:</b> Aumenta il danno delle armi di precisione (Perforanti) e la velocità di esecuzione delle
     * combo o dei colpi a distanza.</p>
     * <p><b>In difesa:</b> Incrementa il valore di Schivata, permettendo di evitare completamente gli attacchi ad area
     * (es. Palle di fuoco) o i proiettili.</p>
     */
    DESTREZZA("Agilità nei movimenti e coordinazione."),

    /**
     * Resistenza fisica, tempra e salute complessiva.
     * <p><b>In attacco:</b> Non influisce direttamente sul danno, ma permette di mantenere la concentrazione sugli
     * incantesimi anche se si viene colpiti.</p>
     * <p><b>In difesa:</b> Aumenta i Punti Vita (HP) massimi e mitiga i danni nel tempo fisici (es. riduce la durata
     * o il danno del Sanguinamento).</p>
     */
    COSTITUZIONE("Tempra biologica e riserva di salute."),

    /**
     * Potenza intellettuale, logica e controllo dei flussi magici grezzi.
     * <p><b>In attacco:</b> Amplifica l'efficacia dei danni magici complessi (Arcano/Vuoto) e la riserva di
     * Punti Magia (MP).</p>
     * <p><b>In difesa:</b> Permette di identificare le illusioni nemiche e contrasta gli effetti di contro-incantesimo
     * o feedback arcano.</p>
     */
    INTELLIGENZA("Capacità logica e potenza magica elementale."),

    /**
     * Consapevolezza spirituale, intuito e connessione con il divino.
     * <p><b>In attacco:</b> Potenzia direttamente i danni di tipo Sacro e l'efficacia degli incantesimi di cura
     * lanciati sugli alleati.</p>
     * <p><b>In difesa:</b> Aumenta la resistenza mentale e riduce la durata delle Maledizioni e dei debuff spirituali
     * subiti, rendendo il personaggio più resistente agli effetti di controllo mentale (es. Confuso o Spaventato).</p>
     */
    SAGGEZZA("Fede, intuito e resistenza spirituale."),

    /**
     * Forza della personalità, magnetismo e forza di volontà.
     * <p><b>In attacco:</b> Potenzia le abilità di comando che buffano gli alleati e aumenta l'efficacia dei danni
     * Psichici/Mentali basati sull'ego.</p>
     * <p><b>In difesa:</b> Rende più difficile per estranei o nemici manipolare, corrompere o intimidire socialmente
     * il personaggio, mantenendo la fedeltà al gruppo anche sotto pressione.</p>
     */
    CARISMA("Forza della personalità e presenza scenica."),

    /**
     * Probabilità che gli eventi girino a favore del personaggio.
     * <p><b>In attacco:</b> Aumenta in modo imprevedibile la precisione o aggiunge effetti casuali positivi ai colpi
     * (es. applica un debuff elementale extra).</p>
     * <p><b>In difesa:</b> Permette di sopravvivere con 1 HP a colpi altrimenti letali, fa fallire le trappole
     * ambientali nemiche e riduce la probabilità di subire un Colpo Critico.</p>
     */
    FORTUNA("Influenza del fato sulle probabilità di successo."),

    /**
     * Precisione millimetrica nel colpire i punti vitali scoperti.
     * <p><b>In attacco:</b> Aumenta drasticamente la probabilità di sferrare un Colpo Critico, raddoppiando o
     * triplicando il danno finale.</p>
     * <p><b>In difesa:</b> Non ha alcun ruolo difensivo: contrastare i Colpi Critici subiti è compito della
     * FORTUNA.</p>
     */
    CRITICO("Probabilità e moltiplicatore dei colpi devastanti."),

    /**
     * Precisione oculare, stabilità della mano e coordinazione occhio-mano.
     * <p><b>In attacco:</b> Riduce a zero la probabilità di mancare il bersaglio (Miss), superando i bonus di schivata
     * dei nemici agili.</p>
     * <p><b>In difesa:</b> Aiuta a calcolare la traiettoria dei proiettili in arrivo per intercettarli prima
     * dell'impatto.</p>
     */
    PRECISIONE("Capacità di andare a segno senza mancare il bersaglio."),

    /**
     * Velocità d'azione, rapidità di movimento e iniziativa nel turno.
     * <p><b>In attacco:</b> Determina chi attacca per primo all'inizio del combattimento e permette di effettuare più
     * attacchi nello stesso turno.</p>
     * <p><b>In difesa:</b> Consente di fuggire rapidamente da una scontro svantaggioso o di riposizionarsi fuori dalle
     * zone di danno (AdE).</p>
     */
    VELOCITA("Iniziativa nei turni e rapidità di spostamento."),

    /**
     * Capacità di muoversi senza farsi notare e agire nell'ombra.
     * <p><b>In attacco:</b> Garantisce un bonus di danno massiccio (Attacco Furtivo) se si colpisce un nemico ignaro,
     * ignorando l'armatura fisica.</p>
     * <p><b>In difesa:</b> Impedisce ai nemici a distanza di agganciare il personaggio come bersaglio finché resta
     * nascosto.</p>
     */
    FURTIVITA("Capacità di occultamento e attacco a sorpresa."),

    /**
     * Abilità nel frapporre l'arma o lo scudo tra sé e il colpo nemico.
     * <p><b>In attacco:</b> Consente di eseguire "attacchi di contrasto", colpendo il nemico subito dopo averne
     * deviato la lama.</p>
     * <p><b>In difesa:</b> Riduce o azzera il danno fisico in arrivo. È la statistica chiave per i personaggi Tank
     * dotati di scudo.</p>
     */
    PARATA("Efficacia nel bloccare i colpi fisici diretti."),

    /**
     * Schermatura mistica contro le energie non terrene.
     * <p><b>In attacco:</b> Permette di infondere le armi con un'aura che spezza le barriere magiche nemiche.</p>
     * <p><b>In difesa:</b> Riduce percentualmente i danni subiti da fonti magiche pure, come Arcano, Necrotico, Vuoto
     * e Maledizioni.</p>
     */
    RESISTENZA_MAGICA("Filtro difensivo contro incantesimi e anatemi."),

    /**
     * Sensi acuti, vista sviluppata e udito sopraffino.
     * <p><b>In attacco:</b> Permette di scovare i punti deboli di boss o nemici corazzati e di vedere bersagli
     * invisibili o nascosti.</p>
     * <p><b>In difesa:</b> Evita di cadere nelle imboscate e riduce i malus dello stato ACCECATO o ASSORDATO.</p>
     */
    PERCEZIONE("Consapevolezza dell'ambiente e scoperta di segreti."),

    /**
     * Aura di terrore, maestosità o timore reverenziale emanata dall'arma o dal personaggio.
     * <p><b>In attacco:</b> Intimidisce i nemici all'impatto, applicando automaticamente lo stato SPAVENTATO o
     * riducendo il loro morale.</p>
     * <p><b>In difesa:</b> Esita i nemici ad attaccare direttamente il personaggio, spingendoli a scegliere bersagli
     * più facili.</p>
     */
    SOGGEZIONE("Presenza terrificante che debuffa il morale nemico."),

    /**
     * Uno stato di trance agonistica, rabbia incontrollata o adrenalina pura.
     * <p><b>In attacco:</b> Aumenta esponenzialmente il danno fisico man mano che la salute del personaggio diminuisce
     * (meccanica Berserk).</p>
     * <p><b>In difesa:</b> Rende il personaggio temporaneamente immune allo STORDITO o al RALLENTATO, sacrificando però
     * la capacità di parare.</p>
     */
    FURIA("Rabbia da battaglia che scambia difesa per attacco bruto."),

    /**
     * Capacità di carico, peso massimo trasportabile e stazza dell'equipaggiamento.
     * <p><b>In attacco:</b> Consente di brandire armi colossali a due mani con una sola mano senza subire penalità di
     * precisione.</p>
     * <p><b>In difesa:</b> Permette di indossare le armature più pesanti del gioco senza subire malus drastici alla
     * VELOCITA o alla schivata.</p>
     */
    CARICO("Capacità di trasporto e tolleranza al peso dell'armatura."),

    /**
     * Riserva di energia mistica e potenza magica accumulata.
     * <p><b>In attacco:</b> Determina la quantità massima di Punti Magia (MP/Mana) disponibili per lanciare incantesimi
     * devastanti e supporta la rigenerazione del mana durante il turno.</p>
     * <p><b>In difesa:</b> Può essere usata come scudo energetico (meccanica *Mana Shield*) per assorbire i danni
     * subiti convertendoli in perdita di mana anziché di salute.</p>
     */
    MAGIA("Riserva di mana ed energia magica canalizzabile."),

    /**
     * Fermezza d'animo e determinazione di fronte al pericolo e all'ignoto.
     * <p><b>In attacco:</b> Permette di sferrare attacchi implacabili contro nemici spaventosi o boss colossali senza
     * subire penalità di precisione dovute alla paura.</p>
     * <p><b>In difesa:</b> Fornisce una resistenza nativa e drastica contro gli effetti psichici e mentali di tipo
     * SPAVENTATO, permettendo al personaggio di mantenere la posizione.</p>
     */
    CORAGGIO("Fermezza mentale e resistenza alla paura."),

    /**
     * Onore in battaglia, spirito di sacrificio ed eroismo cavalleresco.
     * <p><b>In attacco:</b> Genera bonus ai danni e alla precisione quando si affrontano nemici in netta superiorità
     * numerica o quando si difendono alleati in fin di vita.</p>
     * <p><b>In difesa:</b> Aumenta la resistenza generale ai danni (Mitigazione) e permette di attivare abilità di
     * "intercettazione" per subire un colpo al posto di un alleato indifeso.</p>
     */
    VALORE("Spirito eroico e attitudini al sacrificio per il gruppo."),

    /**
     * Livello di affaticamento fisico e mentale accumulato durante l'azione.
     * <p><b>In attacco:</b> Un alto valore di stanchezza riduce progressivamente la precisione dei colpi e aumenta il
     * costo in vigore (Stamina) di ogni attacco o combo.</p>
     * <p><b>In difesa:</b> Penalizza pesantemente il valore di Schivata e aumenta il tempo di recupero dagli stati di
     * stordimento o atterramento (il personaggio si rialza più lentamente).</p>
     */
    STANCHEZZA("Livello di affaticamento che penalizza le prestazioni fisiche."),

    /**
     * Il numero massimo di bersagli che un personaggio può colpire in un turno.
     */
    NUMERO_BERSAGLI("Numero massimo di bersagli che un personaggio può colpire in un turno.");

    private final String descrizione;

    TipoAttributo(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
