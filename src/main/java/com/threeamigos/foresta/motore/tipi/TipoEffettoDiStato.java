package com.threeamigos.foresta.motore.tipi;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoEffettoDiStato {

    // =========================================================================
    // Alterazioni di Movimento / Controllo (Crowd Control - CC)
    // =========================================================================

    // RALLENTATO (Slow): Riduce la velocità di movimento e d'attacco.
    // 🪨 TERRA / 🔨 CONTUNDENTE ➔ Inciampo: Il bersaglio non può scartare l'impatto. Subisce il 30% di danno in più e viene ATTERRATO.
    RALLENTATO("Rallentato"),

    // IMMOBILIZZATO (Root): Impedisce il movimento fisico, ma permette di attaccare o lanciare incantesimi sul posto.
    // 🔥 FUOCO ➔ Incendio Liberatorio: Il fuoco distrugge all'istante l'immobilizzazione e infligge il 50% di danni da fuoco extra al bersaglio.
    // 🔨 CONTUNDENTE / 🪨 TERRA ➔ Impatto Rigido: Non potendo assecondare il colpo muovendo le gambe, il bersaglio subisce il 30% di danno in più.
    IMMOBILIZZATO("Immobilizzato"),

    // STORDITO (Stun): Il bersaglio è completamente inerme; non può muoversi, attaccare o usare abilità.
    // 🎯 PERFORANTE / 🗡️ TAGLIENTE ➔ Colpo di Grazia: Il bersaglio non può difendersi. Gli attacchi fisici di precisione sono colpi critici automatici (x2.0 danno).
    STORDITO("Stordito"),

    // ATTERRATO (Prone): Il personaggio cade a terra; deve spendere movimento per rialzarsi ed è più facile da colpire in mischia.
    // 🔨 CONTUNDENTE ➔ Schiacciamento: Colpire un bersaglio già a terra con un martello o un masso raddoppia il danno fisico e ne estende l'atterramento.
    ATTERRATO("Atterrato"),

    // SPAVENTATO (Fear): Costringe il bersaglio a fuggire lontano dalla fonte della paura.
    // 🔮 ARCANO ➔ Sovraccarico Mentale: (Vedi CONFUSO) Trasforma la paura in uno STORDITO pesante di 1-2 turni.
    // 🔨 FISICO ➔ Shock di Realtà: (Vedi CONFUSO) Il dolore fisico acuto interrompe immediatamente lo stato di paura e infligge il 20% di danno in più.
    SPAVENTATO("Spaventato"),

    // =========================================================================
    // Danni nel Tempo (Damage over Time - DoT)
    // =========================================================================

    // SANGUINAMENTO (Bleed): Danno fisico periodico causato da ferite aperte. Spesso ignora la difesa dell'armatura.
    // 💧 ACQUA ➔ Diluizione Ematica: L'acqua lava la ferita, dimezzando il danno periodico da sanguinamento e infliggendo il 5% di danno in più.
    // ❄️ GHIACCIO ➔ Coagulazione Forzata: Il freddo blocca il dissanguamento (rimuove lo stato), congela i vasi sanguigni applicando immediatamente RALLENTATO e infligge il 5% di danno in più.
    SANGUINAMENTO("Sanguinamento"),

    // BRUCIATO (Burn): Danno da fuoco periodico. Può propagarsi ad altri elementi o oggetti vicini.
    // 💧 ACQUA ➔ Estinzione: Lo stato BRUCIATO viene rimosso istantaneamente, ma consuma lo stato Bagnato.
    // ❄️ GHIACCIO ➔ Scioglimento: Il ghiaccio si scioglie, l'attacco infligge il 50% di danni in più, rimuove BRUCIATO e lascia il bersaglio BAGNATO.
    // 💨 ARIA ➔ Alimentazione: Il vento alimenta le fiamme, aumentando la durata o raddoppiando il danno del DoT nel turno successivo.
    // 🤢 VELENO ➔ Esplosione: Il veleno è infiammabile. Genera un'esplosione immediata di fuoco ad area (AdE) e rimuove entrambi gli stati.
    BRUCIATO("Bruciato"),

    // AVVELENATO (Poisoned): Danno periodico costante, spesso accompagnato da un malus alle statistiche.
    // 🧪 ACIDO ➔ Reazione Tossica: infligge il 15% di danno in più (non gestiamo corrosione/usura dell'armatura).
    // 🔥 FUOCO ➔ Vampata Tossica: Se il bersaglio avvelenato viene colpito da fuoco, il veleno si incendia: rimuove AVVELENATO, applica BRUCIATO e infligge il 30% di danno in più (effetto simile a Esplosione di Gas, vedi BRUCIATO, ma nel verso opposto).
    AVVELENATO("Avvelenato"),

    // INFETTATO (Diseased): Danno necrotico nel tempo o blocco totale di qualsiasi effetto di cura ricevuto.
    // ✨ SACRO ➔ Purificazione Violenta: L'attacco Sacro infligge il 50% di danni in più, rimuove INFETTATO e sprigiona un'onda che cura gli alleati vicini.
    // 🔮 ARCANO ➔ Sifone Vitale: Il 30% del danno grezzo dell'attacco Arcano viene convertito in MP per l'attaccante (l'eccedenza oltre il massimo di MP diventa HP).
    // 🤢 VELENO ➔ Tossicità Settica: Lo stato AVVELENATO applicato sul bersaglio infetto infligge il doppio dei danni periodici.
    INFETTATO("Infettato"),

    // =========================================================================
    // Debilitazioni Mentali e Sensoriali
    // =========================================================================

    // CONFUSO (Confused): Il bersaglio attacca alleati o nemici a caso, oppure vaga senza meta.
    // 🔮 ARCANO ➔ Sovraccarico Mentale: (Vedi SPAVENTATO) L'impatto trasforma istantaneamente la Confusione in uno STORDITO (Stun) pesante che dura 1 o 2 turni.
    // 🌌 VUOTO ➔ Follia Cosmica: Il bersaglio subisce il 50% di danni in più; rimuove CONFUSO e applica MENTE_FRATTURATA.
    // 🔨 FISICO ➔ Shock di Realtà: (Vedi SPAVENTATO) Qualsiasi danno fisico ricevuto interrompe immediatamente lo stato CONFUSO e infligge il 20% di danno in più.
    CONFUSO("Confuso"),

    // MENTE_FRATTURATA (Shattered Mind): applicato da Follia Cosmica (Vedi CONFUSO). Non è permanente: dura a turni
    // come gli altri stati. Penalizza del 20% la statistica offensiva quando il personaggio attacca con danno
    // magico/elementale, e del 20% la statistica difensiva quando lo subisce.
    MENTE_FRATTURATA("Mente Fratturata"),

    // ACCECATO (Blinded): Riduce drasticamente la precisione degli attacchi fisici o la gittata degli incantesimi.
    // 💨 ARIA ➔ Dispersione: Un forte attacco d'aria (folata di vento) spazza via la sabbia dagli occhi, rimuovendo istantaneamente lo stato ACCECATO e infliggendo il 5% di danno in più.
    // 💧 ACQUA ➔ Fango: L'acqua impasta la terra/sabbia sul bersaglio. Lo stato ACCECATO si somma a uno stato di RALLENTATO molto severo, con il 5% di danno in più.
    ACCECATO("Accecato"),

    // ASSORDATO (Deafened): Impedisce di sentire, può far fallire incantesimi verbali e riduce la percezione.
    // 🔊 SONICO ➔ Disorientamento: Un attacco sonico contro un bersaglio già assordato distrugge i canali auricolari interni, trasformando l'effetto in uno STORDITO di 1 turno e infliggendo il 5% di danno in più.
    ASSORDATO("Assordato"),

    // SILENZIATO (Silenced): Impedisce totalmente il lancio di incantesimi o l'uso di abilità magiche.
    // 🔮 ARCANO ➔ Risonanza Sigillata: L'attacco Arcano causa un feedback magico che infligge il 30% di danni bonus come danno Puro (ignora le difese).
    // 🧠 PSICHICO ➔ Isolamento Sensoriale: I danni Psichici aumentano del 50% e lo stato di SILENZIATO si estende anche in un effetto di ACCECATO.
    SILENZIATO("Silenziato"),

    // MALEDETTO (Curse): Il personaggio ha l'anima o le statistiche temporaneamente compromesse da un influsso oscuro.
    // 💀 NECROTICO ➔ Mietitura d'Anima: Il danno viene raddoppiato (x2.0). Se il bersaglio è un minion sotto il 20% di vita, muore istantaneamente e diventa uno scheletro alleato.
    // ✨ SACRO ➔ Reazione di Rigetto: La maledizione si spezza, ma l'energia repressa esplode infliggendo danni da Tuono/Sonico a tutti i nemici vicini.
    // 🌌 VUOTO ➔ Collasso Entropico: Rimuove la maledizione, brucia il 25% dei MP massimi del bersaglio e infligge un danno alla salute pari ai MP persi.
    MALEDETTO("Maledetto"),

    // =========================================================================
    // Stati Elementali Alterati
    // =========================================================================

    // BAGNATO (Wet): Rende vulnerabili ai danni da Fulmine e Ghiaccio, ma aumenta la resistenza al Fuoco.
    // ⚡ FULMINE ➔ Elettroconduzione: Il danno aumenta del 50% e si propaga automaticamente a tutti i bersagli vicini BAGNATI o nella stessa pozza.
    // ❄️ GHIACCIO ➔ Congelamento: Il bersaglio perde lo stato BAGNATO e viene CONGELATO (Immobile, +Difesa Fisica, ma subisce x2.0 danni da Contundente).
    // 🔥 FUOCO ➔ Vaporizzazione: I danni da fuoco sono dimezzati (-50%). Rimuove lo stato BAGNATO e genera una nuvola di vapore che può accecare.
    BAGNATO("Bagnato"),

    // CONGELATO (Frozen): Il bersaglio è completamente bloccato nel ghiaccio.
    // 🔨 CONTUNDENTE ➔ Frantumazione: Rompe il ghiaccio istantaneamente infliggendo un colpo critico automatico (x2.0 danno fisico) e rimuove lo stato.
    // 🔥 FUOCO ➔ Disgelo: Rimuove lo stato congelato ma infligge il 50% di danni in più per lo shock termico.
    // ⚡ FULMINE ➔ Superconduzione: Il fulmine attraversa i cristalli di ghiaccio, infligge danni bonus e riduce la Difesa Fisica del bersaglio del 30%.
    CONGELATO("Congelato"),

    // =========================================================================
    // Stati Speciali
    // =========================================================================

    // BERSERK (Berserk): Esclusivo ai Guerrieri. Attivato quando la salute scende sotto una soglia critica e il Guerriero ha FURIA > 0.
    // Mentre in stato BERSERK, il danno fisico del Guerriero scala esponenzialmente sulla proporzione di salute persa (più è ferito, più forte colpisce).
    // Lo stato termina automaticamente quando il Guerriero abbandona la locazione (ritorno alla mappa).
    BERSERK("Berserk");

    private final String descrizione;

    TipoEffettoDiStato(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
