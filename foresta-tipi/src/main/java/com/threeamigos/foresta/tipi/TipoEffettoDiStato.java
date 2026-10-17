package com.threeamigos.foresta.tipi;

/**
 * Gli stati alterati che possono colpire un personaggio, dal rallentamento al berserk.
 * <p>
 * Dagli effetti di stato nascono le reazioni con i tipi di danno: sono tutte dichiarate in {@link TipoInterazioneConEffettiDiStato}
 * (precondizioni, innescanti, moltiplicatore, stati tolti e aggiunti), e il motore le applica da lì (vedi
 * interazioni_effetti_di_stato.md). I commenti accanto agli stati dicono solo quello che le reazioni dovrebbero fare e
 * ancora non fanno. Quasi tutti gli stati sono un male da togliere; i vantaggi sono due
 * (vedi {@link #isBenefico()}).
 * <p>
 * I testi di ogni costante (DESCRIZIONE) non stanno qui ma nel file {@code tipo_effetto_di_stato_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoEffettoDiStato {

    // =========================================================================
    // Alterazioni di Movimento / Controllo (Crowd Control - CC)
    // =========================================================================

    /**
     * Riduce la velocità di movimento e d'attacco.
     */
    RALLENTATO,

    /**
     * Impedisce il movimento fisico, ma permette di attaccare o lanciare incantesimi sul posto.
     */
    IMMOBILIZZATO,

    /**
     * Il bersaglio è completamente inerme; non può muoversi, attaccare o usare abilità.
     */
    STORDITO,

    /**
     * Il personaggio cade a terra; deve spendere movimento per rialzarsi ed è più facile da colpire in mischia.
     */
    ATTERRATO,

    /**
     * Costringe il bersaglio a fuggire lontano dalla fonte della paura.
     */
    SPAVENTATO,

    // =========================================================================
    // Danni nel Tempo (Damage over Time - DoT)
    // =========================================================================

    /**
     * Danno fisico periodico causato da ferite aperte. Spesso ignora la difesa dell'armatura.
     */
    SANGUINAMENTO,

    /**
     * Danno da fuoco periodico. Può propagarsi ad altri elementi o oggetti vicini.
     */
    // 🤢 VELENO ➔ Esplosione: dovrebbe essere anche un'esplosione di fuoco ad area sui nemici vicini (oggi colpisce solo il bersaglio).
    BRUCIATO,

    /**
     * Danno periodico costante, spesso accompagnato da un malus alle statistiche.
     */
    AVVELENATO,

    /**
     * Danno necrotico nel tempo o blocco totale di qualsiasi effetto di cura ricevuto.
     */
    INFETTATO,

    // =========================================================================
    // Debilitazioni Mentali e Sensoriali
    // =========================================================================

    /**
     * Il bersaglio attacca alleati o nemici a caso, oppure vaga senza meta.
     */
    CONFUSO,

    /**
     * applicato da Follia Cosmica (Vedi CONFUSO). Non è permanente: dura a turni
     * come gli altri stati. Penalizza del 20% la statistica offensiva quando il personaggio attacca con danno
     * magico/elementale, e del 20% la statistica difensiva quando lo subisce.
     */
    MENTE_FRATTURATA,

    /**
     * Riduce drasticamente la precisione degli attacchi fisici o la gittata degli incantesimi.
     */
    ACCECATO,

    /**
     * Impedisce di sentire, può far fallire incantesimi verbali e riduce la percezione.
     */
    ASSORDATO,

    /**
     * Impedisce totalmente il lancio di incantesimi o l'uso di abilità magiche.
     */
    SILENZIATO,

    /**
     * Il personaggio ha l'anima o le statistiche temporaneamente compromesse da un influsso oscuro.
     */
    // ✨ SACRO ➔ Reazione di Rigetto: l'energia repressa dovrebbe esplodere con danni da Tuono/Sonico a tutti i nemici vicini (oggi toglie solo MALEDETTO).
    MALEDETTO,

    // =========================================================================
    // Stati Elementali Alterati
    // =========================================================================

    /**
     * Rende vulnerabili ai danni da Fulmine e Ghiaccio, ma aumenta la resistenza al Fuoco.
     */
    // ⚡ FULMINE ➔ Elettroconduzione: il danno dovrebbe propagarsi a tutti i bersagli vicini BAGNATI o nella stessa pozza (oggi solo +50% sul bersaglio).
    BAGNATO,

    /**
     * Il bersaglio è completamente bloccato nel ghiaccio.
     */
    CONGELATO,

    // =========================================================================
    // Stati Speciali
    // =========================================================================

    /**
     * Esclusivo ai Guerrieri. Attivato quando la salute scende sotto una soglia critica e il Guerriero ha FURIA &gt; 0.
     * Mentre in stato BERSERK, il danno fisico del Guerriero scala esponenzialmente sulla proporzione di salute persa (più è ferito, più forte colpisce).
     * Lo stato termina automaticamente quando il Guerriero abbandona la locazione (ritorno alla mappa).
     */
    BERSERK,

    /**
     * L'effetto del canto di valore del Bardo (vedi bardo.md): coraggio e precisione crescono di una
     * percentuale che dipende dal livello di chi canta (EffettoDiStatoMD.getIntensita), per qualche turno.
     * Non ha danni nel tempo né interazioni con i tipi di danno.
     */
    ISPIRATO;


    TipoEffettoDiStato() {
    }

    public String getDescrizione() {
        return Testi.testoOCostante(this, "DESCRIZIONE");
    }

    /**
     * Se l'effetto è un vantaggio per chi lo ha: l'ispirazione del Bardo e il berserk del Guerriero. Gli altri sono
     * un male da togliere (per esempio con l'Alba sacra).
     */
    public boolean isBenefico() {
        return this == ISPIRATO || this == BERSERK;
    }
}
