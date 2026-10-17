package com.threeamigos.foresta.tipi;

/**
 *
 * @author Stefano Reksten
 */
// Ogni tipo coperto da almeno una missione vera ha sopra un commento "Coperto da:" con le classi che lo coprono
// (vedi passi_missioni.md, "Regole per chi aggiunge missioni"); i tipi impossibili nella Foresta sono commentati.
/**
 * Il catalogo dei tipi di missione che il gioco può proporre, raggruppati per {@link CategoriaMissione}: ciascuno con un esempio. Non sono le classi concrete (quelle
 * sono le {@code ClasseMissione}), ma i generi di incarico. Sopra ogni tipo un commento "Coperto da:" dice quali missioni lo realizzano (vedi passi_missioni.md,
 * "Regole per chi aggiunge missioni"); quelli impossibili nella Foresta sono commentati.
 */
public enum TipoMissione {

    // Coperto da: RichiestaDiMateriali (alchimista: le erbe)
    /**
     * Esempio: raccogliere funchi luminescenti o fiori di loto nero per un alchimista locale
     */
    RACCOLTA_INGREDIENTI(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: RichiestaDiMateriali (capitano delle guardie)
    /**
     * Esempio: raccogliere trofei da nemici sconfitti, scalpi, reliquie
     */
    RACCOLTA_TROFEI(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: RichiestaDiMateriali (armaiolo: i cristalli delle grotte)
    /**
     * Esempio: raccogliere cristalli magici, cristalli energetici, cristalli rari
     */
    RACCOLTA_CRISTALLI(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: RichiestaDiMateriali (alchimista: le parti dei mostri)
    /**
     * Esempio: raccogliere essenza magica, estratto vitale, energia pura
     */
    RACCOLTA_ESSENZA(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: RichiestaDiMateriali (armaiolo: i minerali delle grotte e delle rovine)
    /**
     * Esempio: estrarre minerali, gemme, cristalli da una miniera
     */
    MINIERA(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: IncaricoDiCombattimento (un oggetto rubato o nascosto si recupera con una riga di INCARICO_DI_COMBATTIMENTO)
    /**
     * Esempio: ritrovare un amuleto o un cimelio di famiglia e riportarlo al legittimo proprietario
     */
    RECUPERO(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: IlCorriere (lettere, pegni, forzieri)
    /**
     * Esempio: portare un amuleto sacro o una lettera sigillata da un punto A ad un punto B
     */
    TRASPORTO(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: LaLeggendaDellArmaiolo, LaLeggendaDelLocandiere (la leggenda è l'indizio, i guardiani del tempio, il leggendario il tesoro)
    /**
     * Esempio: esplorare un dungeon alla ricerca di un tesoro
     */
    CACCIA_AL_TESORO(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: forgiare un'arma leggendaria, creare una pozione rara, realizzare un artefatto magico
     */
    ARTIGIANATO(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: IlNegozioDellArmaiolo, IlNegozioDellAlchimista, IlNegozioDelVenditoreDiPergamene, IlNegozioDellIncantatore (disinfestate le rovine, diventano una bottega)
    /**
     * Esempio: costruire una torre, erigere una statua, realizzare un ponte
     */
    COSTRUZIONE(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: RichiestaDiMateriali (armaiolo: pelli e corni)
    /**
     * Esempio: cacciare cervi per le pelli, raccogliere ingredienti da animali, procacciare cibo
     */
    CACCIA_ANIMALI(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: RichiestaDiMateriali (locandiere: i pesci e le rane delle paludi)
    /**
     * Esempio: pescare nel fiume, raccogliere dal mare, catturare creature acquatiche
     */
    PESCA(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: LaSorveglianza (le rape giganti)
    /**
     * Esempio: piantare raccolti, coltivare orto, raccogliere grano
     */
    AGRICOLTURA(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: RichiestaDiMateriali (alchimista: le erbe)
    /**
     * Esempio: raccogliere erbe, piante medicinali, funghi, bacche
     */
    FORAGGIAMENTO(CategoriaMissione.ACQUISIZIONE),
    // Coperto da: LaSorveglianza (le oche da guardia)
    /**
     * Esempio: allevare animali, bestiame, creature magiche
     */
    ALLEVAMENTO(CategoriaMissione.ACQUISIZIONE),
    // Non fattibile nella Foresta: nella Foresta non ci sono né borse né banchieri che speculano.
    // /**
    //  * Esempio: speculazione, investimenti, gioco d'azzardo, trading
    //  */
    // BORSA(SupertipoMissione.ACQUISIZIONE),

    //----------

    // Coperto da: IlCorriere (il trattato d'alleanza)
    /**
     * Esempio: convincere un nobile a schierarsi dalla vostra parte o negoziare una tregua
     */
    DIPLOMAZIA(CategoriaMissione.NEGOZIAZIONE),
    // Coperto da: IlCorriere (l'accordo fra il mugnaio e il pescatore)
    /**
     * Esempio: fare da intermediario tra due fazioni in guerra, mediare una disputa commerciale
     */
    MEDIAZIONE(CategoriaMissione.NEGOZIAZIONE),
    // Non adatto al tono del gioco: comprare e rivendere merci è un lavoro da mercanti, non un'avventura.
    // /**
    //  * Esempio: vendere merci, speculare su prezzi, guadagnare da commercio
    //  */
    // COMMERCIO(SupertipoMissione.NEGOZIAZIONE),
    // Da non sviluppare per ora: servirebbero rilanci e avversari all'asta, una meccanica nuova.
    /**
     * Esempio: partecipare ad asta, acquistare oggetti rari, offerta al ribasso
     */
    ASTA(CategoriaMissione.NEGOZIAZIONE),
    // Non adatto al tono del gioco: fare da sensale fra due mercanti non è un'avventura.
    // /**
    //  * Esempio: fare da broker, intermediario commerciale, mediatore
    //  */
    // SENSERIA(SupertipoMissione.NEGOZIAZIONE),
    // Coperto da: IlCorriere (il contratto di compravendita)
    /**
     * Esempio: negoziare contratto, accordo legale, documento vincolante
     */
    CONTRATTO(CategoriaMissione.NEGOZIAZIONE),
    /**
     * Esempio: scambiare ostaggi, negoziazione di rilascio, baratto persone
     */
    SCAMBIO_OSTAGGI(CategoriaMissione.NEGOZIAZIONE),
    // Coperto da: IlCorriere (la tregua per il ponte)
    /**
     * Esempio: negoziare tregua, cessate il fuoco, armistizio
     */
    NEGOZIAZIONE_TREGUA(CategoriaMissione.NEGOZIAZIONE),

    //----------

    // Coperto da: CacciatoreDiTaglie, LaTagliaSullaBanda
    /**
     * Esempio: eliminare una specifica bestia o un bandito
     */
    CACCIATORE_DI_TAGLIE(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (il troll del mulino, le arpie del fornaio, il cavaliere spergiuro, il mago del rospo)
    /**
     * Esempio: affrontare e sconfiggere un nemico specifico per motivi personali, riscatto, onore perso
     */
    VENDETTA(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (il campione del maestro d'armi, la campionessa delle guardie)
    /**
     * Esempio: combattere un duello per ripristinare l'onore, affrontare un campione in arena
     */
    DUELLO(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (l'esercito della radura, la notte dei morti: a ondate)
    /**
     * Esempio: partecipare a battaglia large-scale, combattimento di massa, schieramento di truppe
     */
    BATTAGLIA(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: SconfiggiLaStrega, SconfiggiIlLich, SconfiggiIlMinotauroGigante, SconfiggiLIdra, SconfiggiIlDrago
    /**
     * Esempio: assaltare una fortezza, conquistare una posizione, assedio di un castello
     */
    ASSALTO(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (i predoni nella radura)
    /**
     * Esempio: tendere imboscata, intrappolamento tattico, attacchi a sorpresa
     */
    IMBOSCATA(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: CacciaAiGoblin
    /**
     * Esempio: attacchi rapidi, guerre di logoramento, tattiche di guerriglia
     */
    GUERRIGLIA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: ritirata ordinata, battere in ritirata, riposizionamento tattico
     */
    RITIRATA_TATTICA(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (le chimere della radura)
    /**
     * Esempio: attacco diretto, cavalry charge, assalto frontale
     */
    CARICA(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (gli hobgoblin accerchiati)
    /**
     * Esempio: circondare nemici, accerchiamento tattico, movimento a pinza
     */
    CIRCONDAMENTO(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (la strada sbarrata dagli hobgoblin, il cavaliere nero)
    /**
     * Esempio: bloccare passaggio, creare barricata, sbarramento
     */
    BLOCCO(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (la pattuglia di hobgoblin)
    /**
     * Esempio: combattimento minore, scaramucce, scontri limitati
     */
    SCHERMAGLIA(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: LaSorveglianza (la torre assediata)
    /**
     * Esempio: difendere durante assedio, resistenza assediata
     */
    ASSEDIO_DIFESA(CategoriaMissione.COMBATTIMENTO),
    // Non fattibile nella Foresta: servono il mare e le navi.
    // /**
    //  * Esempio: battaglia navale, combattimento via mare, flotta
    //  */
    // FLOTTA(SupertipoMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (la fattoria assediata)
    /**
     * Esempio: uscita tattica da fortezza, attacco sorpresa, sortita
     */
    SORTITA(CategoriaMissione.COMBATTIMENTO),
    // Non fattibile nella Foresta: nella Foresta non ci sono cavalli.
    // /**
    //  * Esempio: attacco di cavalleria, carica montata, charges
    //  */
    // CAVALLERIA(SupertipoMissione.COMBATTIMENTO),
    // Coperto da: LaSorveglianza (il guado da tenere)
    /**
     * Esempio: guerra di trincea, bunker, difesa statica
     */
    TRINCEA(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (il ponte dei troll)
    /**
     * Esempio: battaglia su ponte strategico, controllo passaggio, ponte conteso
     */
    PONTE_TATTICO(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (la rocca dei briganti)
    /**
     * Esempio: assedio offensivo, assalto a fortezza, conquista della rocca
     */
    ASSEDIO_OFFENSIVO(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (il rito del minotauro)
    /**
     * Esempio: combattimento rituale, duello cerimoniale, combattimento sacro
     */
    COMBATTIMENTO_RITUALE(CategoriaMissione.COMBATTIMENTO),
    // Non fattibile nella Foresta: nessuno vola, nella Foresta.
    // /**
    //  * Esempio: battaglia aerea, combattimento in volo, scontro fra le nuvole
    //  */
    // BATTAGLIA_AEREA(SupertipoMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (la maga delle rovine)
    /**
     * Esempio: duello magico specializzato, scontro magico puro, battaglia di maghi
     */
    DUELLO_MAGICO(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (la chimera-drago, le viverne)
    /**
     * Esempio: combattimento contro bestia rara, dominio selvatico, caccia bestia
     */
    COMBATTIMENTO_BESTIA(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (l'elfo dell'antica usanza)
    /**
     * Esempio: duello in stile antico, combattimento tradizionale, scontro eroico
     */
    DUELLO_ANTICO(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: IncaricoDiCombattimento (la cripta degli scheletri, la miniera dei goblin; a ondate: il nido sotto la montagna, le segrete del castello, il pozzo degli esperimenti)
    /**
     * Esempio: ripulire una cripta o una miniera invasa a non morti o ragni
     */
    PULIZIA_DEI_DUNGEON(CategoriaMissione.COMBATTIMENTO),

    //----------

    // Coperto da: IlRapimento, IlSoccorso (l'apprendista dei gargoyle)
    /**
     * Esempio: liberare un fabbro elfo o un mago imprigionato
     */
    SALVATAGGIO(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlPellegrino, NonSparateSulPianista, IlViaggioDellaMoglie, IlRapimento
    /**
     * Esempio: proteggere un mercante che attraversa un passo di montagna
     */
    SCORTA(CategoriaMissione.PROTEZIONE),
    // Coperto da: LaSorveglianza (le galline del fattore)
    /**
     * Esempio: difendere un villaggio dagli attacchi di goblin o da un drago
     */
    DIFESA(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlCorriere (erbe e tinture per un malato)
    /**
     * Esempio: curare una malattia rara, guarire da un avvelenamento, resuscitare un alleato
     */
    GUARIGIONE(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlSoccorso (il taglialegna, il minatore, il cacciatore, il pastore)
    /**
     * Esempio: salvare qualcuno intrappolato in una caverna, estrarre feriti da un'area pericolosa
     */
    SOCCORSO(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlSoccorso (il pastore della radura)
    /**
     * Esempio: evacuare civili da una zona di guerra, portare in sicurezza rifugiati
     */
    EVACUAZIONE(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlCorriere (rimedi per un lazzaretto, sale contro il contagio)
    /**
     * Esempio: contenere un'epidemia, aiutare i malati, disinfestare un'area
     */
    EPIDEMIA(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlRituale (il pastore posseduto)
    /**
     * Esempio: esorcizzare una possessione demoniaca, liberare da controllo mentale
     */
    POSSESSIONE(CategoriaMissione.PROTEZIONE),
    // Coperto da: LaSorveglianza (la capanna dei raffreddati)
    /**
     * Esempio: isolare malati, contenimento, prevenzione contagio
     */
    QUARANTENA(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlRituale (la barriera di cristallo contro le arpie)
    /**
     * Esempio: creare barriera magica, protezione magica, scudo incantato
     */
    BARRIERA_MAGICA(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlSoccorso (il messaggero ferito)
    /**
     * Esempio: aiuto immediato a feriti, intervento d'emergenza, primo intervento
     */
    PRIMO_SOCCORSO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: creare rifugio, accogliere profughi, fornire riparo
     */
    RIFUGIO(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlCorriere (talismani e acqua sacra, urgenti)
    /**
     * Esempio: guarigione tramite magia, incantesimi curativi
     */
    CURA_MAGICA(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlCorriere (antidoti e unguenti, urgenti)
    /**
     * Esempio: contrastare avvelenamento, antidoto, cura veleno
     */
    ANTI_VELENO(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlRituale (il santuario dei viandanti)
    /**
     * Esempio: creare santuario, luogo sacro, area protetta consacrata
     */
    SANTUARIO(CategoriaMissione.PROTEZIONE),
    // Coperto da: LaSorveglianza (la grotta murata)
    /**
     * Esempio: contenere minaccia, limitazione, isolamento pericolo
     */
    CONTENIMENTO(CategoriaMissione.PROTEZIONE),
    // Coperto da: LaSorveglianza (il cerchio di pietre, la tomba dell'eremita)
    /**
     * Esempio: stare di guardia, sorveglianza notturna, vigilanza
     */
    VIGILIA(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlContrabbandiere (i gioielli di famiglia)
    /**
     * Esempio: nascondere, mascheramento, cela, nascondiglio
     */
    OCCULTAMENTO(CategoriaMissione.PROTEZIONE),
    // Coperto da: IncaricoDiCombattimento (i fantasmi del pozzo, per una notte)
    /**
     * Esempio: protezione temporanea, scudo temporale, difesa limitata
     */
    PROTEZIONE_TEMPORALE(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlSoccorso (il copista in fuga)
    /**
     * Esempio: fornire asilo politico, rifugio da persecuzione, protezione rifugiati
     */
    ASILO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: rinforzare strutture, blindare difese, potenziare protezioni
     */
    BLINDATURA(CategoriaMissione.PROTEZIONE),

    //----------

    // Coperto da: LIndagine (i viandanti scomparsi)
    /**
     * Esempio: indagare su sparizioni di persone scoprendo dietro congiure di streghe o vampiri
     */
    INVESTIGAZIONE(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: IlCartografo
    /**
     * Esempio: esplorare le terre selvagge per scoprire nuovi insediamenti o rovine sconosciute
     */
    ESPLORAZIONE(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (l'evaso)
    /**
     * Esempio: trovare persona scomparsa, rintracciare creatura selvatica, localizzare fuggitivo, cercare persona specifica, ricerca mirata, localizzazione target
     */
    RINTRACCIAMENTO(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (la cura dimenticata)
    /**
     * Esempio: cercare informazioni in archivi, ricercare in biblioteche, studiare testi antichi, raccogliere dati
     */
    RICERCA(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (le lucciole della palude)
    /**
     * Esempio: approfondire uno studio accademico, scoprire il funzionamento di una magia, risolvere un mistero scientifico per pura conoscenza
     */
    CURIOSITA_ACCADEMICA(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LaSorveglianza (i contrabbandieri del bosco, i rumori della grotta)
    /**
     * Esempio: osservare, monitorare, pedinare qualcuno senza essere scoperti, seguire movimenti
     */
    SORVEGLIANZA(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (la spia dei goblin)
    /**
     * Esempio: scoprire una spia nel nostro accampamento, smascherare un traditore, impedire lo spionaggio
     */
    CONTROSPIONAGGIO(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (i due goblin prigionieri)
    /**
     * Esempio: interrogare prigionieri, estrarre informazioni, interrogatorio forzato, estorsione confessioni
     */
    INTERROGATORIO(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (il grano maledetto)
    /**
     * Esempio: indagine profonda, inchiesta giudiziaria, investigazione complessa, analisi approfondita
     */
    INCHIESTA(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (la morte del pescatore)
    /**
     * Esempio: analisi scene crimine, raccolta prove, indizi forensici, esaminare corpi, cause di morte, autopsia
     */
    FORENSICA(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (il ladro dei tetti)
    /**
     * Esempio: analizzare profilo criminale, psicologia criminale, prevedere comportamenti, profilazione psicologica
     */
    PROFILING(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (la scia viola)
    /**
     * Esempio: seguire tracce magiche, tracciamento soprannaturale, aura tracking, leggere presenze invisibili
     */
    TRACCIA_MAGICA(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (le campane rubate)
    /**
     * Esempio: raccogliere testimonianze, interviste, deposizioni, interrogare testimoni
     */
    TESTIMONI(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (le luci nelle rovine)
    /**
     * Esempio: scoprire segreto, rivelare mistero, smascheramento, investigare fantasmi, paranormale, entità spettrali
     */
    SCOPERTA_SEGRETO(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LOggettoSmarrito (oggetti smarriti vicino a un posto; i leggendari hanno missioni a parte)
    /**
     * Esempio: cercare oggetto specifico, localizzazione oggetto, ricerca mirata, leggere storia di oggetti, psicometria
     */
    RICERCA_OGGETTO(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (il drago di cartapesta)
    /**
     * Esempio: scoprire inganno, smascherare falsità, rivelare bugia, denudare inganni
     */
    SCOPERTA_INGANNO(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (le rune sulla porta)
    /**
     * Esempio: leggere rune, decifrare simboli magici, interpretazione runica, leggere aura
     */
    LETTURA_RUNE(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LaDocumentazione (le iscrizioni, gli affreschi, l'erbario, la guida delle locande)
    /**
     * Esempio: mappare un territorio sconosciuto, catalogare reperti archeologici, documenting scoperte
     */
    DOCUMENTAZIONE(CategoriaMissione.INVESTIGAZIONE),
    // Coperto da: LIndagine (la tomba del re antico)
    /**
     * Esempio: esplorare rovine perdute per ricomporre la storia di una civiltà attraverso iscrizioni magiche
     */
    DECIFRAZIONE(CategoriaMissione.INVESTIGAZIONE),

    //----------

    /**
     * Esempio: scalare i vertici di una data fazione o gilda
     */
    FAZIONE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: insegnare a un apprendista la magia, allenare una guardia, diventare maestro di una disciplina
     */
    ADDESTRAMENTO(CategoriaMissione.PROGRESSIONE),
    // Coperto da: IncaricoDiCombattimento (la gara di magia)
    /**
     * Esempio: vincere una gara di magia, battere un campione in un duello, guadagnare un'elezione
     */
    COMPETIZIONE(CategoriaMissione.PROGRESSIONE),
    // Coperto da: IlTorneo (la Rosa Appassita, la giostra degli incantatori, il Boccale Rovesciato)
    /**
     * Esempio: partecipare a torneo multi-round, competizione a eliminazione diretta
     */
    TORNEO(CategoriaMissione.PROGRESSIONE),
    // Non fattibile nella Foresta: eredità e testamenti sono affari da notai, non da avventurieri.
    // /**
    //  * Esempio: ricevere eredità, ereditare titoli, ricevere lasciti
    //  */
    // EREDITA(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: essere nominato a carica ufficiale, ricevere incarico importante
     */
    NOMINA(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: aderire a gilda, fratellanza, unirsi a ordine
     */
    FRATELLANZA(CategoriaMissione.PROGRESSIONE),
    // Non fattibile nella Foresta: nessuno del gruppo diventerà re: nella Foresta c'è già Sua Maestà.
    // /**
    //  * Esempio: diventare re/regina, ascendere al trono, incoronazione
    //  */
    // CORONAZIONE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: migliorare status, avanzamento sociale, migliorare condizione
     */
    ASCESA_SOCIALE(CategoriaMissione.PROGRESSIONE),
    // Coperto da: IncaricoDiCombattimento (i titoli buffi di Sua Maestà, nella palude e fra le rovine)
    /**
     * Esempio: ottenere titolo nobile, blasone, titolo nobiliare
     */
    TITOLO_NOBILIARE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: diventare maestro di un'arte, eccellenza, maestria
     */
    MAESTRIA(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: specializzarsi in disciplina specifica, focus expertise
     */
    SPECIALIZZAZIONE(CategoriaMissione.PROGRESSIONE),
    // Da non sviluppare per ora: servirebbe un conto della fama del gruppo, che il gioco non tiene.
    /**
     * Esempio: guadagnare fama, notorietà, celebrità, rinomanza
     */
    FAMA(CategoriaMissione.PROGRESSIONE),
    // Da non sviluppare per ora: servirebbe un conto della reputazione del gruppo, che il gioco non tiene.
    /**
     * Esempio: migliorare reputazione, onore, prestigio, credibilità
     */
    REPUTAZIONE(CategoriaMissione.PROGRESSIONE),
    // Da non sviluppare per ora: accumulare monete non è una missione, è il gioco.
    /**
     * Esempio: accumulare ricchezza, prosperità economica, tesoro
     */
    RICCHEZZA(CategoriaMissione.PROGRESSIONE),
    // Non fattibile nella Foresta: le genealogie nobili non si giocano a colpi di spada.
    // /**
    //  * Esempio: stabilire discendenza nobile, genealogia, stirpe
    //  */
    // LIGNAGGIO(SupertipoMissione.PROGRESSIONE),
    // Non fattibile nella Foresta: eredità e testamenti sono affari da notai, non da avventurieri.
    // /**
    //  * Esempio: diventare legatario, eredità nominata, designazione eredità
    //  */
    // LEGATARIO(SupertipoMissione.PROGRESSIONE),
    // Non fattibile nella Foresta: eredità e testamenti sono affari da notai, non da avventurieri.
    // /**
    //  * Esempio: diventare erede ufficiale, diritto successorio, linea successoria
    //  */
    // EREDE(SupertipoMissione.PROGRESSIONE),
    // Coperto da: LaBenedizione (con il favore, IlFavore, come missione secondaria)
    /**
     * Esempio: ricevere benedizione, favore divino, grazia divina
     */
    BENEDIZIONE_RICEVERE(CategoriaMissione.PROGRESSIONE),
    // Non fattibile nella Foresta: le successioni al trono non sono affari del gruppo.
    // /**
    //  * Esempio: assicurare successione, diritto successorio organizzato
    //  */
    // SUCCESSIONE(SupertipoMissione.PROGRESSIONE),
    /**
     * Esempio: accumulare risorse per potenziare una base o una fortezza
     */
    GESTIONE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: ricostruire una fortezza abbandonata
     */
    RICOSTRUZIONE(CategoriaMissione.PROGRESSIONE),

    //----------

    // Coperto da: LaLealta
    /**
     * Esempio: aiutare uno dei membri del gruppo a risolvere un problema personale
     */
    LEALTA(CategoriaMissione.RELAZIONI),
    // Da non sviluppare per ora: sarebbe solo una raccolta di materiali (un regalo per gli sposi), già coperta.
    /**
     * Esempio: organizzare un matrimonio, celebrare nozze, preparare una cerimonia
     */
    MATRIMONIO(CategoriaMissione.RELAZIONI),
    // Da non sviluppare per ora: sarebbe solo una raccolta di materiali (le provviste per la festa), già coperta.
    /**
     * Esempio: celebrare una vittoria, organizzare festa, carnevale, banchetto
     */
    CELEBRAZIONE(CategoriaMissione.RELAZIONI),
    // Non fattibile nella Foresta: nessuno del gruppo si sposa per politica.
    // /**
    //  * Esempio: matrimonio diplomatico, unione politica, alleanza famigliare
    //  */
    // ALLEANZA_MATRIMONIALE(SupertipoMissione.RELAZIONI),
    // Coperto da: IlRituale (la vita nuova del brigante)
    /**
     * Esempio: riscattarsi, redenzione, cambio di vita
     */
    RISCATTO(CategoriaMissione.RELAZIONI),
    // Coperto da: IlRituale (il perdono del fratello)
    /**
     * Esempio: ottenere perdono, riconciliazione, pace interiore
     */
    PERDONO(CategoriaMissione.RELAZIONI),
    // Coperto da: IlRituale (il falò del cavaliere)
    /**
     * Esempio: redenzione pubblica, riabilitazione sociale, ristabilire reputazione
     */
    REDENZIONE_PUBBLICA(CategoriaMissione.RELAZIONI),

    //----------

    // Coperto da: IlColpo (il libro dei conti dell'usuraio)
    /**
     * Esempio: rubare un artefatto da una camera del tesoro o un documento da una biblioteca
     */
    FURTO(CategoriaMissione.ILLECITO),
    // Non adatto al tono del gioco: niente omicidi a sangue freddo, nemmeno tentati.
    // /**
    //  * Esempio: assassinare silenziosamente un nemico senza farsi scoprire
    //  */
    // ASSASSINIO(SupertipoMissione.ILLECITO),
    // Coperto da: IlContrabbandiere (le uova di viverna)
    /**
     * Esempio: commercio nel mercato nero, merci illegali, contrabbando organizzato
     */
    MERCATO_NERO(CategoriaMissione.NEGOZIAZIONE),
    // Coperto da: IlColpo (le scale dei goblin)
    /**
     * Esempio: sabotare i rifornimenti di un nemico o sabotare le difese di una fortezza
     */
    SABOTAGGIO(CategoriaMissione.ILLECITO),
    // Coperto da: IlColpo (la diga dei goblin)
    /**
     * Esempio: sabotaggio con esplosivi, demolizione, esplosione tattica
     */
    ESPLOSIONE(CategoriaMissione.COMBATTIMENTO),
    // Coperto da: LaSorveglianza (l'accampamento degli hobgoblin, il laboratorio del rivale)
    /**
     * Esempio: spiare una riunione importante, raccogliere informazioni senza farsi scoprire
     */
    SPIONAGGIO(CategoriaMissione.ILLECITO),
    // Non adatto al tono del gioco: ricattare qualcuno è meschino, non sgangherato.
    // /**
    //  * Esempio: ricattare qualcuno, estorcere denaro, blackmail, estorsione, estorsione sotto minaccia, minaccia per guadagni
    //  */
    // RICATTO(SupertipoMissione.ILLECITO),
    /**
     * Esempio: truffare un mercante, ingannare un nobile, corrompere un ufficiale
     */
    INGANNO(CategoriaMissione.ILLECITO),
    // Coperto da: IlContrabbandiere (il vino senza dazio, le sete, il libro proibito)
    /**
     * Esempio: contrabbandare merci proibite attraverso un checkpoint
     */
    CONTRABBANDO(CategoriaMissione.ILLECITO),
    // Non fattibile nella Foresta: nella Foresta non ci sono fazioni da tradire.
    // /**
    //  * Esempio: tradire una fazione, passare ai nemici, rivegliarsi come spia
    //  */
    // TRADIMENTO(SupertipoMissione.ILLECITO),
    // Coperto da: IlContrabbandiere (il sacchetto per il gabelliere)
    /**
     * Esempio: corrompere un ufficiale, offrire tangenti, bribery
     */
    CORRUZIONE(CategoriaMissione.ILLECITO),
    // Coperto da: IlColpo (il registro dei confini)
    /**
     * Esempio: falsificare documenti, coniare monete false, creare oggetti contraffatti, falsificare firma, dipinto falso, sigillo ufficiale
     */
    FALSIFICAZIONE(CategoriaMissione.ILLECITO),
    /**
     * Esempio: rapire qualcuno per riscatto, prendere in ostaggio
     */
    RAPIMENTO(CategoriaMissione.ILLECITO),
    // Non fattibile nella Foresta: servono il mare e le navi; le carovane da saccheggiare le copre BRIGANTAGGIO.
    // /**
    //  * Esempio: attaccare commercianti, razziare una costa, saccheggiare una carovana
    //  */
    // PIRATERIA(SupertipoMissione.ILLECITO),
    // Coperto da: IlColpo (il sonno dei troll)
    /**
     * Esempio: avvelenare cibo/acqua, envenenonment, contaminazione
     */
    AVVELENAMENTO(CategoriaMissione.ILLECITO),
    // Coperto da: IlColpo (i baffi alla statua)
    /**
     * Esempio: distruggere proprietà, vandalizzare edifici, rovinare strutture
     */
    VANDALISMO(CategoriaMissione.ILLECITO),
    // Coperto da: IlContrabbandiere (le reliquie false)
    /**
     * Esempio: frode finanziaria, schema fraudolento, imbroglio, matrimonio fraudolento
     */
    FRODE(CategoriaMissione.ILLECITO),
    // Coperto da: IlContrabbandiere (i fogli satirici sul borgomastro)
    /**
     * Esempio: diffamare qualcuno, spargere voci calunniose, danneggiare reputazione
     */
    DIFFAMAZIONE(CategoriaMissione.ILLECITO),
    // Non adatto al tono del gioco: la schiavitù non fa ridere nemmeno nella Foresta.
    // /**
    //  * Esempio: ridurre in schiavitù, sfruttamento, servitù forzata
    //  */
    // SCHIAVITU(SupertipoMissione.ILLECITO),
    // Non fattibile nella Foresta: nella Foresta non ci sono rivolte da sobillare: Sua Maestà ha già abbastanza debiti.
    // /**
    //  * Esempio: incitare ribellione, sedizione, sommossa, insurrezione, incitare disordini, sommossa pubblica
    //  */
    // SEDIZIONE(SupertipoMissione.ILLECITO),
    // Coperto da: IlColpo (il deposito di frecce)
    /**
     * Esempio: incendiare, fuoco appiccato, piromane, incendio intenzionale, fuoco premeditato, fuoco criminale
     */
    INCENDIO(CategoriaMissione.ILLECITO),
    // Coperto da: IncaricoDiCombattimento (il rivale dell'alchimista)
    /**
     * Esempio: assassinare per denaro, omicidio su commissione, killer
     */
    SICARIO(CategoriaMissione.ILLECITO),
    // Coperto da: IlContrabbandiere (le monete col marchio)
    /**
     * Esempio: riciclare denaro sporco, lavaggio denaro
     */
    RICICLAGGIO(CategoriaMissione.ILLECITO),
    // Coperto da: IlColpo (la capanna della guaritrice)
    /**
     * Esempio: irruzione, invasione casa, violazione proprietà privata, imprigionamento illegale, sequestro
     */
    VIOLAZIONE_DOMICILIO(CategoriaMissione.ILLECITO),
    // Coperto da: IlColpo (l'idolo dei goblin)
    /**
     * Esempio: profanare luogo sacro, offesa religiosa, sacrilegio, bestemmia, profanazione religiosa
     */
    SACRILEGIO(CategoriaMissione.ILLECITO),
    // Coperto da: IlContrabbandiere (le erbe proibite, le pozioni rubate, i folletti)
    /**
     * Esempio: traffico di merci proibite, smuggling su larga scala
     */
    TRAFFICO(CategoriaMissione.ILLECITO),
    // Non adatto al tono del gioco: rubare la vita a qualcuno non è una burla.
    // /**
    //  * Esempio: rubare identità, usurpazione, furto identità
    //  */
    // FURTO_IDENTITA(SupertipoMissione.ILLECITO),
    // Coperto da: IncaricoDiCombattimento (la carovana dell'usuraio)
    /**
     * Esempio: rapire, furto a mano armata, rapinare
     */
    BRIGANTAGGIO(CategoriaMissione.ILLECITO),
    // Non adatto al tono del gioco: mandare in rovina un innocente con un falso giuramento non è sgangherato, è cattivo.
    // /**
    //  * Esempio: testimonianza falsa, perjury, giuramento falso
    //  */
    // FALSA_TESTIMONIANZA(SupertipoMissione.ILLECITO),
    /**
     * Esempio: imbrogliare al gioco, baro, truffa al gioco d'azzardo
     */
    IMBROGLIONE(CategoriaMissione.ILLECITO),
    // Coperto da: IncaricoDiCombattimento (il brigante da catturare vivo)
    /**
     * Esempio: imprigionare qualcuno, tenere in carcere, reclusione
     */
    CONFINAMENTO(CategoriaMissione.ILLECITO),
    // Non adatto al tono del gioco: niente torture, nemmeno ai goblin.
    // /**
    //  * Esempio: tortura, estorsione confessione, interrogatorio forzato
    //  */
    // TORTURA(SupertipoMissione.ILLECITO),
    // Non adatto al tono del gioco: niente omicidi a sangue freddo, nemmeno tentati.
    // /**
    //  * Esempio: tentativo di omicidio, assassinio fallito, aggressione mortale
    //  */
    // TENTATIVO_OMICIDIO(SupertipoMissione.ILLECITO),

    //----------

    // Coperto da: IlRituale (il bosco corrotto)
    /**
     * Esempio: purificare una terra corrotta, bonificare un luogo maledetto
     */
    PURIFICAZIONE(CategoriaMissione.PROTEZIONE),
    // Coperto da: IlRituale (lo spirito della palude)
    /**
     * Esempio: spezzare una maledizione su uno spirito tormentato
     */
    SPEZZATURA(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (la fonte dei neonati)
    /**
     * Esempio: benedire un luogo maledetto, purificare un'anima corrotta, benedire una coppia
     */
    BENEDIZIONE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (la voce del conte)
    /**
     * Esempio: parlare con spiriti, comunicare con divinità, ottenere messaggi soprannaturali, negoziare con entità
     */
    COMUNICAZIONE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (la memoria delle pietre)
    /**
     * Esempio: visione del passato, retrocognizione, rivivere momenti passati, leggere storia di oggetti, psicometria
     */
    VISIONE_PASSATO(CategoriaMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo legge nel pensiero.
    // /**
    //  * Esempio: leggere mente, telepathy, penetrazione psichica, percepire pensieri altrui
    //  */
    // LETTURA_MENTE(SupertipoMissione.SPIRITUALE),
    // Coperto da: IlRituale (lo stagno del domani)
    /**
     * Esempio: visione del futuro, precognizione, profezia, astrologia, lettura stelle, predizioni, divinazione
     */
    VISIONE_FUTURO(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (il fuoco del solstizio)
    /**
     * Esempio: eseguire un rituale di evocazione per contattare uno spirito o sigillare un portale
     */
    RITUALE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (il testamento del nonno)
    /**
     * Esempio: animare i non-morti, controllare scheletri, risvegliare cadaveri
     */
    NECROMANZIA(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (il guardiano di pietra)
    /**
     * Esempio: evocare creature, convocare entità soprannaturali, richiamare spiriti
     */
    EVOCAZIONE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (le zucche del vicino)
    /**
     * Esempio: lanciare maledizioni, infliggere curse, danneggiare tramite magia nera
     */
    MALEDIZIONE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (la torre pendente)
    /**
     * Esempio: lanciare incantesimi complessi, magia avanzata, effetti magici potenti
     */
    INCANTESIMO(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (il varco degli spettri)
    /**
     * Esempio: sigillare portali, incantesimi sigillo, chiudere varchi magici
     */
    SIGILLO(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (il piombo e l'oro)
    /**
     * Esempio: trasformare metalli, alchemy, alchimia, trasmutazione
     */
    TRASMUTAZIONE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (la congiunzione degli astri)
    /**
     * Esempio: astrologia, lettura stelle, predizioni, divinazione
     */
    ASTRI(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (l'occhio della palude)
    /**
     * Esempio: leggere il futuro, predizioni magiche, visioni
     */
    DIVINAZIONE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (il drago che non c'è)
    /**
     * Esempio: creare illusioni magiche, inganno illusorio, miraggio
     */
    ILLUSIONE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (la pioggia di rane)
    /**
     * Esempio: contrastare magia, dispellare incantesimi, anti-magia
     */
    ANTI_MAGIA(CategoriaMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo sa diventare invisibile (per non farsi vedere c'è il passare inosservati).
    // /**
    //  * Esempio: diventare invisibile, magia occultamento, mimetizzazione
    //  */
    // INVISIBILITA(SupertipoMissione.SPIRITUALE),
    // Coperto da: IlRituale (i lampioni della città)
    /**
     * Esempio: canale energie magiche, conduit, canalizzazione
     */
    CHANNELING(CategoriaMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nella Foresta si va a piedi, non ci sono portali.
    // /**
    //  * Esempio: teletrasporto magico, portale, viaggio istantaneo
    //  */
    // TELEPORTAZIONE(SupertipoMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo cambia forma.
    // /**
    //  * Esempio: trasformarsi in bestia, mutazione, cambio forma
    //  */
    // SHAPE_SHIFT(SupertipoMissione.SPIRITUALE),
    // Coperto da: IlRituale (la pioggia per il mulino)
    /**
     * Esempio: controllare elementi, dominio elementale, magia elementare
     */
    CONTROLLO_ELEMENTALE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (lo spirito della bottega)
    /**
     * Esempio: legare spiriti, connessione soprannaturale, legame spirituale
     */
    LEGAME_SPIRITUALE(CategoriaMissione.SPIRITUALE),
    // Non fattibile nella Foresta: non c'è un piano astrale in cui andare.
    // /**
    //  * Esempio: viaggio astrale, proiezione eterica, corpo astrale, proiezione astrale conscia, controllo corpo astrale, viaggio etereo controllato
    //  */
    // VIAGGIO_ASTRALE(SupertipoMissione.SPIRITUALE),
    // Coperto da: IlRituale (la dea del raccolto)
    /**
     * Esempio: comunione con entità divina, connessione divina, unione sacra
     */
    COMUNIONE(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (la trance di fratello Anselmo)
    /**
     * Esempio: entrare in trance, meditazione profonda, stato alterato
     */
    TRANCE(CategoriaMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo fonde anime o esseri.
    // /**
    //  * Esempio: fusione di esseri/anime, unione spirituale, sincretismo
    //  */
    // FUSIONE(SupertipoMissione.SPIRITUALE),
    // Non fattibile nella Foresta: non ci sono altre epoche in cui andare.
    // /**
    //  * Esempio: viaggio nel tempo, manipolazione temporale, viaggio temporale
    //  */
    // VIAGGIO_TEMPO(SupertipoMissione.SPIRITUALE),
    // Non fattibile nella Foresta: non ci sono altre dimensioni in cui andare.
    // /**
    //  * Esempio: accesso a realtà parallela, dimensioni alternative, mondi alternativi
    //  */
    // REALTA_PARALLELA(SupertipoMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo sa possedere un corpo (le possessioni da scacciare le copre POSSESSIONE).
    // /**
    //  * Esempio: possedere corpo, occupazione fisica, usurpazione corporea, possessione dell'anima, usurpazione spirituale, controllo anima
    //  */
    // POSSESSO_CORPO(SupertipoMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo sa assorbire la potenza altrui.
    // /**
    //  * Esempio: assorbire potenza, estrazione di energia, vampirismo magico
    //  */
    // ASSORBIMENTO(SupertipoMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo controlla le menti.
    // /**
    //  * Esempio: controllo mentale, dominio psichico, schiavitù mentale
    //  */
    // CONTROLLO_MENTE(SupertipoMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo scambia i corpi.
    // /**
    //  * Esempio: scambio di corpi, trasferimento corporeo, swap souls
    //  */
    // SCAMBIO_CORPI(SupertipoMissione.SPIRITUALE),
    // Coperto da: IlRituale (il golem d'argilla)
    /**
     * Esempio: creazione di golem, animazione artificiale, creatura magica
     */
    CREAZIONE_GOLEM(CategoriaMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo divide esseri o anime.
    // /**
    //  * Esempio: divisione di esseri, scissione di anima, separazione magica
    //  */
    // FISSIONE(SupertipoMissione.SPIRITUALE),
    // Non fattibile nella Foresta: nessuno nel gruppo sa far dormire per sempre (né svegliare chi dorme così).
    // /**
    //  * Esempio: morte temporale, ibernazione magica, sonno eterno
    //  */
    // MORTE_TEMPORALE(SupertipoMissione.SPIRITUALE),
    // Coperto da: IlRituale (lo spaventapasseri vivo)
    /**
     * Esempio: animazione di oggetti inanimati, enchantment animato, vita artificiale
     */
    ANIMAZIONE_OGGETTI(CategoriaMissione.SPIRITUALE),
    // Coperto da: IlRituale (il patto con lo spirito del fiume)
    /**
     * Esempio: patto con demoni/entità, contratto spirituale, alleanza soprannaturale
     */
    PATTO_ANIMA(CategoriaMissione.SPIRITUALE),

    ;

    private final CategoriaMissione categoriaMissione;

    TipoMissione(CategoriaMissione categoriaMissione) {
        this.categoriaMissione = categoriaMissione;
    }

    public CategoriaMissione getSupertipoMissione() {
        return categoriaMissione;
    }

}
