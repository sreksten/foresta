package com.threeamigos.foresta.missioni;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoMissione {

    /**
     * Esempio: raccogliere funchi luminescenti o fiori di loto nero per un alchimista locale
     */
    RACCOLTA_INGREDIENTI(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: raccogliere trofei da nemici sconfitti, scalpi, reliquie
     */
    RACCOLTA_TROFEI(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: raccogliere cristalli magici, cristalli energetici, cristalli rari
     */
    RACCOLTA_CRISTALLI(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: raccogliere essenza magica, estratto vitale, energia pura
     */
    RACCOLTA_ESSENZA(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: estrarre minerali, gemme, cristalli da una miniera
     */
    MINIERA(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: ritrovare un amuleto o un cimelio di famiglia e riportarlo al legittimo proprietario
     */
    RECUPERO(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: portare un amuleto sacro o una lettera sigillata da un punto A ad un punto B
     */
    TRASPORTO(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: esplorare un dungeon alla ricerca di un tesoro
     */
    CACCIA_AL_TESORO(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: forgiare un'arma leggendaria, creare una pozione rara, realizzare un artefatto magico
     */
    ARTIGIANATO(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: costruire una torre, erigere una statua, realizzare un ponte
     */
    COSTRUZIONE(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: cacciare cervi per le pelli, raccogliere ingredienti da animali, procacciare cibo
     */
    CACCIA_ANIMALI(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: pescare nel fiume, raccogliere dal mare, catturare creature acquatiche
     */
    PESCA(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: piantare raccolti, coltivare orto, raccogliere grano
     */
    AGRICOLTURA(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: raccogliere erbe, piante medicinali, funghi, bacche
     */
    FORAGGIAMENTO(SupertipoMissione.ACQUISIZIONE),
    /**
     * Esempio: allevare animali, bestiame, creature magiche
     */
    ALLEVAMENTO(SupertipoMissione.ACQUISIZIONE),

    //----------

    /**
     * Esempio: convincere un nobile a schierarsi dalla vostra parte o negoziare una tregua
     */
    DIPLOMAZIA(SupertipoMissione.NEGOZIAZIONE),
    /**
     * Esempio: fare da intermediario tra due fazioni in guerra, mediare una disputa commerciale
     */
    MEDIAZIONE(SupertipoMissione.NEGOZIAZIONE),
    /**
     * Esempio: vendere merci, speculare su prezzi, guadagnare da commercio
     */
    COMMERCIO(SupertipoMissione.NEGOZIAZIONE),
    /**
     * Esempio: partecipare ad asta, acquistare oggetti rari, offerta al ribasso
     */
    ASTA(SupertipoMissione.NEGOZIAZIONE),
    /**
     * Esempio: speculazione, investimenti, gioco d'azzardo, trading
     */
    BORSA(SupertipoMissione.NEGOZIAZIONE),
    /**
     * Esempio: fare da broker, intermediario commerciale, mediatore
     */
    SENSERIA(SupertipoMissione.NEGOZIAZIONE),
    /**
     * Esempio: negoziare contratto, accordo legale, documento vincolante
     */
    CONTRATTO(SupertipoMissione.NEGOZIAZIONE),
    /**
     * Esempio: scambiare ostaggi, negoziazione di rilascio, baratto persone
     */
    SCAMBIO_OSTAGGI(SupertipoMissione.NEGOZIAZIONE),
    /**
     * Esempio: commercio nel mercato nero, merci illegali, contrabbando organizzato
     */
    MERCATO_NERO(SupertipoMissione.NEGOZIAZIONE),
    /**
     * Esempio: negoziare tregua, cessate il fuoco, armistizio
     */
    NEGOZIAZIONE_TREGUA(SupertipoMissione.NEGOZIAZIONE),

    //----------

    /**
     * Esempio: eliminare una specifica bestia o un bandito
     */
    CACCIATORE_DI_TAGLIE(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: affrontare e sconfiggere un nemico specifico per motivi personali, riscatto, onore perso
     */
    VENDETTA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: combattere un duello per ripristinare l'onore, affrontare un campione in arena
     */
    DUELLO(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: dominare una bestia selvaggia, controllare un demone, sottomettere un nemico
     */
    CONTROLLO_CREATURA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: partecipare a battaglia large-scale, combattimento di massa, schieramento di truppe
     */
    BATTAGLIA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: assaltare una fortezza, conquistare una posizione, assedio di un castello
     */
    ASSALTO(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: tendere imboscata, intrappolamento tattico, attacchi a sorpresa
     */
    IMBOSCATA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: attacchi rapidi, guerre di logoramento, tattiche di guerriglia
     */
    GUERRIGLIA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: ritirata ordinata, battere in ritirata, riposizionamento tattico
     */
    RITIRATA_TATTICA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: attacco diretto, cavalry charge, assalto frontale
     */
    CARICA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: circondare nemici, accerchiamento tattico, movimento a pinza
     */
    CIRCONDAMENTO(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: bloccare passaggio, creare barricata, sbarramento
     */
    BLOCCO(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: combattimento minore, scaramucce, scontri limitati
     */
    SCHERMAGLIA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: difendere durante assedio, resistenza assediata
     */
    ASSEDIO_DIFESA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: battaglia navale, combattimento via mare, flotta
     */
    FLOTTA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: uscita tattica da fortezza, attacco sorpresa, sortita
     */
    SORTITA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: attacco di cavalleria, carica montata, charges
     */
    CAVALLERIA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: guerra di trincea, bunker, difesa statica
     */
    TRINCEA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: sabotaggio con esplosivi, demolizione, esplosione tattica
     */
    ESPLOSIONE(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: battaglia su ponte strategico, controllo passaggio, ponte conteso
     */
    PONTE_TATTICO(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: assedio offensivo, assalto a fortezza, conquista della rocca
     */
    ASSEDIO_OFFENSIVO(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: combattimento rituale, duello cerimoniale, combattimento sacro
     */
    COMBATTIMENTO_RITUALE(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: battaglia aerea, combattimento in volo, scontro fra le nuvole
     */
    BATTAGLIA_AEREA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: duello magico specializzato, scontro magico puro, battaglia di maghi
     */
    DUELLO_MAGICO(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: combattimento contro bestia rara, dominio selvatico, caccia bestia
     */
    COMBATTIMENTO_BESTIA(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: duello in stile antico, combattimento tradizionale, scontro eroico
     */
    DUELLO_ANTICO(SupertipoMissione.COMBATTIMENTO),
    /**
     * Esempio: ripulire una cripta o una miniera invasa a non morti o ragni
     */
    PULIZIA_DEI_DUNGEON(SupertipoMissione.COMBATTIMENTO),

    //----------

    /**
     * Esempio: liberare un fabbro elfo o un mago imprigionato
     */
    SALVATAGGIO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: proteggere un mercante che attraversa un passo di montagna
     */
    SCORTA(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: difendere un villaggio dagli attacchi di goblin o da un drago
     */
    DIFESA(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: curare una malattia rara, guarire da un avvelenamento, resuscitare un alleato
     */
    GUARIGIONE(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: salvare qualcuno intrappolato in una caverna, estrarre feriti da un'area pericolosa
     */
    SOCCORSO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: evacuare civili da una zona di guerra, portare in sicurezza rifugiati
     */
    EVACUAZIONE(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: contenere un'epidemia, aiutare i malati, disinfestare un'area
     */
    EPIDEMIA(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: purificare una terra corrotta, bonificare un luogo maledetto
     */
    PURIFICAZIONE(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: esorcizzare una possessione demoniaca, liberare da controllo mentale
     */
    POSSESSIONE(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: isolare malati, contenimento, prevenzione contagio
     */
    QUARANTENA(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: imprigionare qualcuno, tenere in carcere, reclusione
     */
    CONFINAMENTO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: creare barriera magica, protezione magica, scudo incantato
     */
    BARRIERA_MAGICA(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: aiuto immediato a feriti, intervento d'emergenza, primo intervento
     */
    PRIMO_SOCCORSO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: creare rifugio, accogliere profughi, fornire riparo
     */
    RIFUGIO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: guarigione tramite magia, incantesimi curativi
     */
    CURA_MAGICA(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: contrastare avvelenamento, antidoto, cura veleno
     */
    ANTI_VELENO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: creare santuario, luogo sacro, area protetta consacrata
     */
    SANTUARIO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: contenere minaccia, limitazione, isolamento pericolo
     */
    CONTENIMENTO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: stare di guardia, sorveglianza notturna, vigilanza
     */
    VIGILIA(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: nascondere, mascheramento, cela, nascondiglio
     */
    OCCULTAMENTO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: protezione temporanea, scudo temporale, difesa limitata
     */
    PROTEZIONE_TEMPORALE(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: fornire asilo politico, rifugio da persecuzione, protezione rifugiati
     */
    ASILO(SupertipoMissione.PROTEZIONE),
    /**
     * Esempio: rinforzare strutture, blindare difese, potenziare protezioni
     */
    BLINDATURA(SupertipoMissione.PROTEZIONE),

    //----------

    /**
     * Esempio: indagare su sparizioni di persone scoprendo dietro congiure di streghe o vampiri
     */
    INVESTIGAZIONE(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: esplorare le terre selvagge per scoprire nuovi insediamenti o rovine sconosciute
     */
    ESPLORAZIONE(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: trovare persona scomparsa, rintracciare creatura selvatica, localizzare fuggitivo, cercare persona specifica, ricerca mirata, localizzazione target
     */
    RINTRACCIAMENTO(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: parlare con spiriti, comunicare con divinità, ottenere messaggi soprannaturali, negoziare con entità
     */
    COMUNICAZIONE(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: cercare informazioni in archivi, ricercare in biblioteche, studiare testi antichi, raccogliere dati
     */
    RICERCA(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: approfondire uno studio accademico, scoprire il funzionamento di una magia, risolvere un mistero scientifico per pura conoscenza
     */
    CURIOSITA_ACCADEMICA(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: osservare, monitorare, pedinare qualcuno senza essere scoperti, seguire movimenti
     */
    SORVEGLIANZA(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: interrogare prigionieri, estrarre informazioni, interrogatorio forzato, estorsione confessioni
     */
    INTERROGATORIO(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: indagine profonda, inchiesta giudiziaria, investigazione complessa, analisi approfondita
     */
    INCHIESTA(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: analisi scene crimine, raccolta prove, indizi forensici, esaminare corpi, cause di morte, autopsia
     */
    FORENSICA(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: infiltrarsi in organizzazione, spionaggio interno, penetrazione sociale, reclutamento spie
     */
    INFILTRAZIONE(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: analizzare profilo criminale, psicologia criminale, prevedere comportamenti, profilazione psicologica
     */
    PROFILING(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: seguire tracce magiche, tracciamento soprannaturale, aura tracking, leggere presenze invisibili
     */
    TRACCIA_MAGICA(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: raccogliere testimonianze, interviste, deposizioni, interrogare testimoni
     */
    TESTIMONI(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: visione del passato, retrocognizione, rivivere momenti passati, leggere storia di oggetti, psicometria
     */
    VISIONE_PASSATO(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: leggere mente, telepathy, penetrazione psichica, percepire pensieri altrui
     */
    LETTURA_MENTE(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: visione del futuro, precognizione, profezia, astrologia, lettura stelle, predizioni, divinazione
     */
    VISIONE_FUTURO(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: scoprire segreto, rivelare mistero, smascheramento, investigare fantasmi, paranormale, entità spettrali
     */
    SCOPERTA_SEGRETO(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: cercare oggetto specifico, localizzazione oggetto, ricerca mirata, leggere storia di oggetti, psicometria
     */
    RICERCA_OGGETTO(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: scoprire inganno, smascherare falsità, rivelare bugia, denudare inganni
     */
    SCOPERTA_INGANNO(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: leggere rune, decifrare simboli magici, interpretazione runica, leggere aura
     */
    LETTURA_RUNE(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: mappare un territorio sconosciuto, catalogare reperti archeologici, documenting scoperte
     */
    DOCUMENTAZIONE(SupertipoMissione.INVESTIGAZIONE),
    /**
     * Esempio: esplorare rovine perdute per ricomporre la storia di una civiltà attraverso iscrizioni magiche
     */
    DECIFRAZIONE(SupertipoMissione.INVESTIGAZIONE),

    //----------

    /**
     * Esempio: scalare i vertici di una data fazione o gilda
     */
    FAZIONE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: insegnare a un apprendista la magia, allenare una guardia, diventare maestro di una disciplina
     */
    ADDESTRAMENTO(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: vincere una gara di magia, battere un campione in un duello, guadagnare un'elezione
     */
    COMPETIZIONE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: partecipare a torneo multi-round, competizione a eliminazione diretta
     */
    TORNEO(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: ricevere eredità, ereditare titoli, ricevere lasciti
     */
    EREDITA(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: essere nominato a carica ufficiale, ricevere incarico importante
     */
    NOMINA(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: aderire a gilda, fratellanza, unirsi a ordine
     */
    FRATELLANZA(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: diventare re/regina, ascendere al trono, incoronazione
     */
    CORONAZIONE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: migliorare status, avanzamento sociale, migliorare condizione
     */
    ASCESA_SOCIALE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: ottenere titolo nobile, blasone, titolo nobiliare
     */
    TITOLO_NOBILIARE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: diventare maestro di un'arte, eccellenza, maestria
     */
    MAESTRIA(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: specializzarsi in disciplina specifica, focus expertise
     */
    SPECIALIZZAZIONE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: guadagnare fama, notorietà, celebrità, rinomanza
     */
    FAMA(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: migliorare reputazione, onore, prestigio, credibilità
     */
    REPUTAZIONE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: accumulare ricchezza, prosperità economica, tesoro
     */
    RICCHEZZA(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: stabilire discendenza nobile, genealogia, stirpe
     */
    LIGNAGGIO(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: diventare legatario, eredità nominata, designazione eredità
     */
    LEGATARIO(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: diventare erede ufficiale, diritto successorio, linea successoria
     */
    EREDE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: ricevere benedizione, favore divino, grazia divina
     */
    BENEDIZIONE_RICEVERE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: assicurare successione, diritto successorio organizzato
     */
    SUCCESSIONE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: accumulare risorse per potenziare una base o una fortezza
     */
    GESTIONE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: ricostruire una fortezza abbandonata
     */
    RICOSTRUZIONE(SupertipoMissione.PROGRESSIONE),

    //----------

    /**
     * Esempio: aiutare uno dei membri del gruppo a risolvere un problema personale
     */
    LEALTA(SupertipoMissione.RELAZIONI),
    /**
     * Esempio: organizzare un matrimonio, celebrare nozze, preparare una cerimonia
     */
    MATRIMONIO(SupertipoMissione.RELAZIONI),
    /**
     * Esempio: celebrare una vittoria, organizzare festa, carnevale, banchetto
     */
    CELEBRAZIONE(SupertipoMissione.RELAZIONI),
    /**
     * Esempio: matrimonio diplomatico, unione politica, alleanza famigliare
     */
    ALLEANZA_MATRIMONIALE(SupertipoMissione.RELAZIONI),
    /**
     * Esempio: riscattarsi, redenzione, cambio di vita
     */
    RISCATTO(SupertipoMissione.RELAZIONI),
    /**
     * Esempio: ottenere perdono, riconciliazione, pace interiore
     */
    PERDONO(SupertipoMissione.RELAZIONI),
    /**
     * Esempio: redenzione pubblica, riabilitazione sociale, ristabilire reputazione
     */
    REDENZIONE_PUBBLICA(SupertipoMissione.RELAZIONI),

    //----------

    /**
     * Esempio: rubare un artefatto da una camera del tesoro o un documento da una biblioteca
     */
    FURTO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: assassinare silenziosamente un nemico senza farsi scoprire
     */
    ASSASSINIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: sabotare i rifornimenti di un nemico o sabotare le difese di una fortezza
     */
    SABOTAGGIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: spiare una riunione importante, raccogliere informazioni senza farsi scoprire
     */
    SPIONAGGIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: scoprire una spia nel nostro accampamento, smascherare un traditore, impedire lo spionaggio
     */
    CONTROSPIONAGGIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: ricattare qualcuno, estorcere denaro, blackmail, estorsione, estorsione sotto minaccia, minaccia per guadagni
     */
    RICATTO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: truffare un mercante, ingannare un nobile, corrompere un ufficiale
     */
    INGANNO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: contrabbandare merci proibite attraverso un checkpoint
     */
    CONTRABBANDO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: tradire una fazione, passare ai nemici, rivegliarsi come spia
     */
    TRADIMENTO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: corrompere un ufficiale, offrire tangenti, bribery
     */
    CORRUZIONE(SupertipoMissione.ILLECITO),
    /**
     * Esempio: falsificare documenti, coniare monete false, creare oggetti contraffatti, falsificare firma, dipinto falso, sigillo ufficiale
     */
    FALSIFICAZIONE(SupertipoMissione.ILLECITO),
    /**
     * Esempio: rapire qualcuno per riscatto, prendere in ostaggio
     */
    RAPIMENTO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: attaccare commercianti, razziare una costa, saccheggiare una carovana
     */
    PIRATERIA(SupertipoMissione.ILLECITO),
    /**
     * Esempio: avvelenare cibo/acqua, envenenonment, contaminazione
     */
    AVVELENAMENTO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: distruggere proprietà, vandalizzare edifici, rovinare strutture
     */
    VANDALISMO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: frode finanziaria, schema fraudolento, imbroglio, matrimonio fraudolento
     */
    FRODE(SupertipoMissione.ILLECITO),
    /**
     * Esempio: diffamare qualcuno, spargere voci calunniose, danneggiare reputazione
     */
    DIFFAMAZIONE(SupertipoMissione.ILLECITO),
    /**
     * Esempio: ridurre in schiavitù, sfruttamento, servitù forzata
     */
    SCHIAVITU(SupertipoMissione.ILLECITO),
    /**
     * Esempio: incitare ribellione, sedizione, sommossa, insurrezione, incitare disordini, sommossa pubblica
     */
    SEDIZIONE(SupertipoMissione.ILLECITO),
    /**
     * Esempio: incendiare, fuoco appiccato, piromane, incendio intenzionale, fuoco premeditato, fuoco criminale
     */
    INCENDIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: assassinare per denaro, omicidio su commissione, killer
     */
    SICARIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: riciclare denaro sporco, lavaggio denaro
     */
    RICICLAGGIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: irruzione, invasione casa, violazione proprietà privata, imprigionamento illegale, sequestro
     */
    VIOLAZIONE_DOMICILIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: profanare luogo sacro, offesa religiosa, sacrilegio, bestemmia, profanazione religiosa
     */
    SACRILEGIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: traffico di merci proibite, smuggling su larga scala
     */
    TRAFFICO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: rubare identità, usurpazione, furto identità
     */
    FURTO_IDENTITA(SupertipoMissione.ILLECITO),
    /**
     * Esempio: offendere divinità, bestemmia, profanazione religiosa
     */
    BLASFEMIA(SupertipoMissione.ILLECITO),
    /**
     * Esempio: rapire, furto a mano armata, brigantaggio
     */
    PARRUCCHIO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: testimonianza falsa, perjury, giuramento falso
     */
    FALSA_TESTIMONIANZA(SupertipoMissione.ILLECITO),
    /**
     * Esempio: imbrogliare al gioco, baro, truffa al gioco d'azzardo
     */
    IMBROGLIONE(SupertipoMissione.ILLECITO),
    /**
     * Esempio: imprigionamento illegale, reclusione non autorizzata, sequestro, carcere illegale
     */
    CARCERE_ILLEGALE(SupertipoMissione.ILLECITO),
    /**
     * Esempio: tortura, estorsione confessione, interrogatorio forzato
     */
    TORTURA(SupertipoMissione.ILLECITO),
    /**
     * Esempio: tentativo di omicidio, assassinio fallito, aggressione mortale
     */
    TENTATIVO_OMICIDIO(SupertipoMissione.ILLECITO),

    //----------

    /**
     * Esempio: spezzare una maledizione su uno spirito tormentato
     */
    SPEZZATURA(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: eseguire un rituale di evocazione per contattare uno spirito o sigillare un portale
     */
    RITUALE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: benedire un luogo maledetto, purificare un'anima corrotta, benedire una coppia
     */
    BENEDIZIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: animare i non-morti, controllare scheletri, risvegliare cadaveri
     */
    NECROMANZIA(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: evocare creature, convocare entità soprannaturali, richiamare spiriti
     */
    EVOCAZIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: lanciare maledizioni, infliggere curse, danneggiare tramite magia nera
     */
    MALEDIZIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: lanciare incantesimi complessi, magia avanzata, effetti magici potenti
     */
    INCANTESIMO(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: sigillare portali, incantesimi sigillo, chiudere varchi magici
     */
    SIGILLO(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: trasformare metalli, alchemy, alchimia, trasmutazione
     */
    TRASMUTAZIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: astrologia, lettura stelle, predizioni, divinazione
     */
    ASTRI(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: leggere il futuro, predizioni magiche, visioni
     */
    DIVINAZIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: creare illusioni magiche, inganno illusorio, miraggio
     */
    ILLUSIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: contrastare magia, dispellare incantesimi, anti-magia
     */
    ANTI_MAGIA(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: diventare invisibile, magia occultamento, mimetizzazione
     */
    INVISIBILITA(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: canale energie magiche, conduit, canalizzazione
     */
    CHANNELING(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: teletrasporto magico, portale, viaggio istantaneo
     */
    TELEPORTAZIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: trasformarsi in bestia, mutazione, cambio forma
     */
    SHAPE_SHIFT(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: controllare elementi, dominio elementale, magia elementare
     */
    CONTROLLO_ELEMENTALE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: legare spiriti, connessione soprannaturale, legame spirituale
     */
    LEGAME_SPIRITUALE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: viaggio astrale, proiezione eterica, corpo astrale, proiezione astrale conscia, controllo corpo astrale, viaggio etereo controllato
     */
    VIAGGIO_ASTRALE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: comunione con entità divina, connessione divina, unione sacra
     */
    COMUNIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: entrare in trance, meditazione profonda, stato alterato
     */
    TRANCE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: fusione di esseri/anime, unione spirituale, sincretismo
     */
    FUSIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: viaggio nel tempo, manipolazione temporale, viaggio temporale
     */
    VIAGGIO_TEMPO(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: accesso a realtà parallela, dimensioni alternative, mondi alternativi
     */
    REALTA_PARALLELA(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: possedere corpo, occupazione fisica, usurpazione corporea, possessione dell'anima, usurpazione spirituale, controllo anima
     */
    POSSESSO_CORPO(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: assorbire potenza, estrazione di energia, vampirismo magico
     */
    ASSORBIMENTO(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: controllo mentale, dominio psichico, schiavitù mentale
     */
    CONTROLLO_MENTE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: scambio di corpi, trasferimento corporeo, swap souls
     */
    SCAMBIO_CORPI(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: creazione di golem, animazione artificiale, creatura magica
     */
    CREAZIONE_GOLEM(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: divisione di esseri, scissione di anima, separazione magica
     */
    FISSIONE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: morte temporale, ibernazione magica, sonno eterno
     */
    MORTE_TEMPORALE(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: animazione di oggetti inanimati, enchantment animato, vita artificiale
     */
    ANIMAZIONE_OGGETTI(SupertipoMissione.SPIRITUALE),
    /**
     * Esempio: patto con demoni/entità, contratto spirituale, alleanza soprannaturale
     */
    PATTO_ANIMA(SupertipoMissione.SPIRITUALE),

    ;

    private SupertipoMissione supertipoMissione;

    TipoMissione(SupertipoMissione supertipoMissione) {
        this.supertipoMissione = supertipoMissione;
    }

    public SupertipoMissione getSupertipoMissione() {
        return supertipoMissione;
    }

}
