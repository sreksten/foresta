package com.threeamigos.foresta.tipi;

/**
 * Questo è un elenco dei vari tipi di missione secondaria che si ritrovano anche nei giochi commerciali odierni.
 * L'elenco è molto lungo e non tutti i tipi di missione secondaria sono coperti da missioni nella Foresta, perché non
 * sono adatti al tono del gioco, quindi sono stati esclusi.
 * <p>
 * Vedi anche: passi_missioni.md, "Regole per chi aggiunge missioni".
 *
 * @author Stefano Reksten
 */
public enum TipoMissione {

    /**
     * Esempio: raccogliere funchi luminescenti o fiori di loto nero per un alchimista locale.
     * Coperto da: RichiestaDiMateriali (alchimista: le erbe)
     */
    RACCOLTA_INGREDIENTI(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: raccogliere trofei da nemici sconfitti, scalpi, reliquie.
     * Coperto da: RichiestaDiMateriali (capitano delle guardie)
     */
    RACCOLTA_TROFEI(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: raccogliere cristalli magici, cristalli energetici, cristalli rari.
     * Coperto da: RichiestaDiMateriali (armaiolo: i cristalli delle grotte)
     */
    RACCOLTA_CRISTALLI(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: raccogliere essenza magica, estratto vitale, energia pura.
     * Coperto da: RichiestaDiMateriali (alchimista: le parti dei mostri)
     */
    RACCOLTA_ESSENZA(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: estrarre minerali, gemme, cristalli da una miniera.
     * Coperto da: RichiestaDiMateriali (armaiolo: i minerali delle grotte e delle rovine)
     */
    MINIERA(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: ritrovare un amuleto o un cimelio di famiglia e riportarlo al legittimo proprietario.
     * Coperto da: IncaricoDiCombattimento (un oggetto rubato o nascosto si recupera con una riga di INCARICO_DI_COMBATTIMENTO)
     */
    RECUPERO(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: portare un amuleto sacro o una lettera sigillata da un punto A ad un punto B.
     * Coperto da: IlCorriere (lettere, pegni, forzieri)
     */
    TRASPORTO(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: esplorare un dungeon alla ricerca di un tesoro.
     * Coperto da: LaLeggendaDellArmaiolo, LaLeggendaDelLocandiere (la leggenda è l'indizio, i guardiani del tempio,
     * il leggendario il tesoro)
     */
    CACCIA_AL_TESORO(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: forgiare un'arma leggendaria, creare una pozione rara, realizzare un artefatto magico.
     * NON COPERTO
     */
    ARTIGIANATO(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: costruire una torre, erigere una statua, realizzare un ponte.
     * Coperto da: IlNegozioDellArmaiolo, IlNegozioDellAlchimista, IlNegozioDelVenditoreDiPergamene,
     * IlNegozioDellIncantatore (disinfestate le rovine, diventano una bottega)
     */
    COSTRUZIONE(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: cacciare cervi per le pelli, raccogliere ingredienti da animali, procacciare cibo.
     * Coperto da: RichiestaDiMateriali (armaiolo: pelli e corni)
     */
    CACCIA_ANIMALI(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: pescare nel fiume, raccogliere dal mare, catturare creature acquatiche.
     * Coperto da: RichiestaDiMateriali (locandiere: i pesci e le rane delle paludi)
     */
    PESCA(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: piantare raccolti, coltivare orto, raccogliere grano.
     * Coperto da: LaSorveglianza (le rape giganti)
     */
    AGRICOLTURA(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: raccogliere erbe, piante medicinali, funghi, bacche.
     * Coperto da: RichiestaDiMateriali (alchimista: le erbe)
     */
    FORAGGIAMENTO(CategoriaMissione.ACQUISIZIONE),
    /**
     * Esempio: allevare animali, bestiame, creature magiche.
     * Coperto da: LaSorveglianza (le oche da guardia)
     */
    ALLEVAMENTO(CategoriaMissione.ACQUISIZIONE),
    // /**
    //  * Esempio: speculazione, investimenti, gioco d'azzardo, trading
    //  * Non fattibile nella Foresta: nella Foresta non ci sono né borse né banchieri che speculano.
    //  */
    // BORSA(CategoriaMissione.ACQUISIZIONE),

    //----------

    /**
     * Esempio: convincere un nobile a schierarsi dalla vostra parte o negoziare una tregua.
     * Coperto da: IlCorriere (il trattato d'alleanza)
     */
    DIPLOMAZIA(CategoriaMissione.NEGOZIAZIONE),
    /**
     * Esempio: fare da intermediario tra due fazioni in guerra, mediare una disputa commerciale.
     * Coperto da: IlCorriere (l'accordo fra il mugnaio e il pescatore)
     */
    MEDIAZIONE(CategoriaMissione.NEGOZIAZIONE),
    // /**
    //  * Esempio: vendere merci, speculare su prezzi, guadagnare da commercio
    //  * Non adatto al tono del gioco: comprare e rivendere merci è un lavoro da mercanti, non un'avventura.
    //  */
    // COMMERCIO(CategoriaMissione.NEGOZIAZIONE),
    // /**
    //  * Esempio: partecipare ad asta, acquistare oggetti rari, offerta al ribasso.
    //  * NON COPERTO. Da non sviluppare per ora: servirebbero rilanci e avversari all'asta, una meccanica nuova.
    //  */
    // ASTA(CategoriaMissione.NEGOZIAZIONE),
    // /**
    //  * Esempio: fare da broker, intermediario commerciale, mediatore
    //  * Non adatto al tono del gioco: fare da sensale fra due mercanti non è un'avventura.
    //  */
    // SENSERIA(CategoriaMissione.NEGOZIAZIONE),
    /**
     * Esempio: negoziare contratto, accordo legale, documento vincolante.
     * Coperto da: IlCorriere (il contratto di compravendita)
     */
    CONTRATTO(CategoriaMissione.NEGOZIAZIONE),
    /**
     * Esempio: scambiare ostaggi, negoziazione di rilascio, baratto persone.
     * NON COPERTO
     */
    SCAMBIO_OSTAGGI(CategoriaMissione.NEGOZIAZIONE),
    /**
     * Esempio: negoziare tregua, cessate il fuoco, armistizio.
     * Coperto da: IlCorriere (la tregua per il ponte)
     */
    NEGOZIAZIONE_TREGUA(CategoriaMissione.NEGOZIAZIONE),

    //----------

    /**
     * Esempio: eliminare una specifica bestia o un bandito.
     * Coperto da: CacciatoreDiTaglie, LaTagliaSullaBanda
     */
    CACCIATORE_DI_TAGLIE(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: affrontare e sconfiggere un nemico specifico per motivi personali, riscatto, onore perso.
     * Coperto da: IncaricoDiCombattimento (il troll del mulino, le arpie del fornaio, il cavaliere spergiuro, il mago
     * del rospo)
     */
    VENDETTA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: combattere un duello per ripristinare l'onore, affrontare un campione in arena.
     * Coperto da: IncaricoDiCombattimento (il campione del maestro d'armi, la campionessa delle guardie)
     */
    DUELLO(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: partecipare a battaglia large-scale, combattimento di massa, schieramento di truppe.
     * Coperto da: IncaricoDiCombattimento (l'esercito della radura, la notte dei morti: a ondate)
     */
    BATTAGLIA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: assaltare una fortezza, conquistare una posizione, assedio di un castello.
     * Coperto da: SconfiggiLaStrega, SconfiggiIlLich, SconfiggiIlMinotauroGigante, SconfiggiLIdra, SconfiggiIlDrago
     */
    ASSALTO(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: tendere imboscata, intrappolamento tattico, attacchi a sorpresa.
     * Coperto da: IncaricoDiCombattimento (i predoni nella radura)
     */
    IMBOSCATA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: attacchi rapidi, guerre di logoramento, tattiche di guerriglia.
     * Coperto da: CacciaAiGoblin
     */
    GUERRIGLIA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: ritirata ordinata, battere in ritirata, riposizionamento tattico.
     * NON COPERTO
     */
    RITIRATA_TATTICA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: attacco diretto, cavalry charge, assalto frontale.
     * Coperto da: IncaricoDiCombattimento (le chimere della radura)
     */
    CARICA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: circondare nemici, accerchiamento tattico, movimento a pinza.
     * Coperto da: IncaricoDiCombattimento (gli hobgoblin accerchiati)
     */
    CIRCONDAMENTO(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: bloccare passaggio, creare barricata, sbarramento.
     * Coperto da: IncaricoDiCombattimento (la strada sbarrata dagli hobgoblin, il cavaliere nero)
     */
    BLOCCO(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: combattimento minore, scaramucce, scontri limitati.
     * Coperto da: IncaricoDiCombattimento (la pattuglia di hobgoblin)
     */
    SCHERMAGLIA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: difendere durante assedio, resistenza assediata.
     * Coperto da: LaSorveglianza (la torre assediata)
     */
    ASSEDIO_DIFESA(CategoriaMissione.COMBATTIMENTO),
    // /**
    //  * Esempio: battaglia navale, combattimento via mare, flotta
    //  * Non fattibile nella Foresta: servono il mare e le navi.
    //  */
    // FLOTTA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: uscita tattica da fortezza, attacco sorpresa, sortita.
     * Coperto da: IncaricoDiCombattimento (la fattoria assediata)
     */
    SORTITA(CategoriaMissione.COMBATTIMENTO),
    // /**
    //  * Esempio: attacco di cavalleria, carica montata, charges
    //  * Non fattibile nella Foresta: nella Foresta non ci sono cavalli.
    //  */
    // CAVALLERIA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: guerra di trincea, bunker, difesa statica.
     * Coperto da: LaSorveglianza (il guado da tenere)
     */
    TRINCEA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: battaglia su ponte strategico, controllo passaggio, ponte conteso.
     * Coperto da: IncaricoDiCombattimento (il ponte dei troll)
     */
    PONTE_TATTICO(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: assedio offensivo, assalto a fortezza, conquista della rocca.
     * Coperto da: IncaricoDiCombattimento (la rocca dei briganti)
     */
    ASSEDIO_OFFENSIVO(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: combattimento rituale, duello cerimoniale, combattimento sacro.
     * Coperto da: IncaricoDiCombattimento (il rito del minotauro)
     */
    COMBATTIMENTO_RITUALE(CategoriaMissione.COMBATTIMENTO),
    // /**
    //  * Esempio: battaglia aerea, combattimento in volo, scontro fra le nuvole
    //  * Non fattibile nella Foresta: nessuno vola, nella Foresta.
    //  */
    // BATTAGLIA_AEREA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: duello magico specializzato, scontro magico puro, battaglia di maghi.
     * Coperto da: IncaricoDiCombattimento (la maga delle rovine)
     */
    DUELLO_MAGICO(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: combattimento contro bestia rara, dominio selvatico, caccia bestia.
     * Coperto da: IncaricoDiCombattimento (la chimera-drago, le viverne)
     */
    COMBATTIMENTO_BESTIA(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: duello in stile antico, combattimento tradizionale, scontro eroico.
     * Coperto da: IncaricoDiCombattimento (l'elfo dell'antica usanza)
     */
    DUELLO_ANTICO(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: ripulire una cripta o una miniera invasa a non morti o ragni.
     * Coperto da: IncaricoDiCombattimento (la cripta degli scheletri, la miniera dei goblin; a ondate: il nido sotto la
     * montagna, le segrete del castello, il pozzo degli esperimenti)
     */
    PULIZIA_DEI_DUNGEON(CategoriaMissione.COMBATTIMENTO),

    //----------

    /**
     * Esempio: liberare un fabbro elfo o un mago imprigionato.
     * Coperto da: IlRapimento, IlSoccorso (l'apprendista dei gargoyle)
     */
    SALVATAGGIO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: proteggere un mercante che attraversa un passo di montagna.
     * Coperto da: IlPellegrino, NonSparateSulPianista, IlViaggioDellaMoglie, IlRapimento
     */
    SCORTA(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: difendere un villaggio dagli attacchi di goblin o da un drago.
     * Coperto da: LaSorveglianza (le galline del fattore)
     */
    DIFESA(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: curare una malattia rara, guarire da un avvelenamento, resuscitare un alleato.
     * Coperto da: IlCorriere (erbe e tinture per un malato)
     */
    GUARIGIONE(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: salvare qualcuno intrappolato in una caverna, estrarre feriti da un'area pericolosa.
     * Coperto da: IlSoccorso (il taglialegna, il minatore, il cacciatore, il pastore)
     */
    SOCCORSO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: evacuare civili da una zona di guerra, portare in sicurezza rifugiati.
     * Coperto da: IlSoccorso (il pastore della radura)
     */
    EVACUAZIONE(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: contenere un'epidemia, aiutare i malati, disinfestare un'area.
     * Coperto da: IlCorriere (rimedi per un lazzaretto, sale contro il contagio)
     */
    EPIDEMIA(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: esorcizzare una possessione demoniaca, liberare da controllo mentale.
     * Coperto da: IlRituale (il pastore posseduto)
     */
    POSSESSIONE(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: isolare malati, contenimento, prevenzione contagio.
     * Coperto da: LaSorveglianza (la capanna dei raffreddati)
     */
    QUARANTENA(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: creare barriera magica, protezione magica, scudo incantato.
     * Coperto da: IlRituale (la barriera di cristallo contro le arpie)
     */
    BARRIERA_MAGICA(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: aiuto immediato a feriti, intervento d'emergenza, primo intervento.
     * Coperto da: IlSoccorso (il messaggero ferito)
     */
    PRIMO_SOCCORSO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: creare rifugio, accogliere profughi, fornire riparo.
     * NON COPERTO
     */
    RIFUGIO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: guarigione tramite magia, incantesimi curativi.
     * Coperto da: IlCorriere (talismani e acqua sacra, urgenti)
     */
    CURA_MAGICA(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: contrastare avvelenamento, antidoto, cura veleno.
     * Coperto da: IlCorriere (antidoti e unguenti, urgenti)
     */
    ANTI_VELENO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: creare santuario, luogo sacro, area protetta consacrata.
     * Coperto da: IlRituale (il santuario dei viandanti)
     */
    SANTUARIO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: contenere minaccia, limitazione, isolamento pericolo.
     * Coperto da: LaSorveglianza (la grotta murata)
     */
    CONTENIMENTO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: stare di guardia, sorveglianza notturna, vigilanza.
     * Coperto da: LaSorveglianza (il cerchio di pietre, la tomba dell'eremita)
     */
    VIGILIA(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: nascondere, mascheramento, cela, nascondiglio.
     * Coperto da: IlContrabbandiere (i gioielli di famiglia)
     */
    OCCULTAMENTO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: protezione temporanea, scudo temporale, difesa limitata.
     * Coperto da: IncaricoDiCombattimento (i fantasmi del pozzo, per una notte)
     */
    PROTEZIONE_TEMPORALE(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: fornire asilo politico, rifugio da persecuzione, protezione rifugiati.
     * Coperto da: IlSoccorso (il copista in fuga)
     */
    ASILO(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: rinforzare strutture, blindare difese, potenziare protezioni.
     * NON COPERTO
     */
    BLINDATURA(CategoriaMissione.PROTEZIONE),

    //----------

    /**
     * Esempio: indagare su sparizioni di persone scoprendo dietro congiure di streghe o vampiri.
     * Coperto da: LIndagine (i viandanti scomparsi)
     */
    INVESTIGAZIONE(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: esplorare le terre selvagge per scoprire nuovi insediamenti o rovine sconosciute.
     * Coperto da: IlCartografo
     */
    ESPLORAZIONE(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: trovare persona scomparsa, rintracciare creatura selvatica, localizzare fuggitivo,
     * cercare persona specifica, ricerca mirata, localizzazione target.
     * Coperto da: LIndagine (l'evaso)
     */
    RINTRACCIAMENTO(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: cercare informazioni in archivi, ricercare in biblioteche, studiare testi antichi, raccogliere dati.
     * Coperto da: LIndagine (la cura dimenticata)
     */
    RICERCA(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: approfondire uno studio accademico, scoprire il funzionamento di una magia,
     * risolvere un mistero scientifico per pura conoscenza.
     * Coperto da: LIndagine (le lucciole della palude)
     */
    CURIOSITA_ACCADEMICA(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: osservare, monitorare, pedinare qualcuno senza essere scoperti, seguire movimenti.
     * Coperto da: LaSorveglianza (i contrabbandieri del bosco, i rumori della grotta)
     */
    SORVEGLIANZA(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: scoprire una spia nel nostro accampamento, smascherare un traditore, impedire lo spionaggio.
     * Coperto da: LIndagine (la spia dei goblin)
     */
    CONTROSPIONAGGIO(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: interrogare prigionieri, estrarre informazioni, interrogatorio forzato, estorsione confessioni.
     * Coperto da: LIndagine (i due goblin prigionieri)
     */
    INTERROGATORIO(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: indagine profonda, inchiesta giudiziaria, investigazione complessa, analisi approfondita.
     * Coperto da: LIndagine (il grano maledetto)
     */
    INCHIESTA(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: analisi scene crimine, raccolta prove, indizi forensici, esaminare corpi, cause di morte, autopsia.
     * Coperto da: LIndagine (la morte del pescatore)
     */
    FORENSICA(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: analizzare profilo criminale, psicologia criminale, prevedere comportamenti, profilazione psicologica.
     * Coperto da: LIndagine (il ladro dei tetti)
     */
    PROFILING(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: seguire tracce magiche, tracciamento soprannaturale, aura tracking, leggere presenze invisibili.
     * Coperto da: LIndagine (la scia viola)
     */
    TRACCIA_MAGICA(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: raccogliere testimonianze, interviste, deposizioni, interrogare testimoni.
     * Coperto da: LIndagine (le campane rubate)
     */
    TESTIMONI(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: scoprire segreto, rivelare mistero, smascheramento, investigare fantasmi, paranormale, entità spettrali.
     * Coperto da: LIndagine (le luci nelle rovine)
     */
    SCOPERTA_SEGRETO(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: cercare oggetto specifico, localizzazione oggetto, ricerca mirata, leggere storia di oggetti.
     * Coperto da: LOggettoSmarrito (oggetti smarriti vicino a un posto; i leggendari hanno missioni a parte)
     */
    RICERCA_OGGETTO(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: scoprire inganno, smascherare falsità, rivelare bugia, denudare inganni.
     * Coperto da: LIndagine (il drago di cartapesta)
     */
    SCOPERTA_INGANNO(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: leggere rune, decifrare simboli magici, interpretazione runica, leggere aura.
     * Coperto da: LIndagine (le rune sulla porta)
     */
    LETTURA_RUNE(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: mappare un territorio sconosciuto, catalogare reperti archeologici, documentare scoperte.
     * Coperto da: LaDocumentazione (le iscrizioni, gli affreschi, l'erbario, la guida delle locande)
     */
    DOCUMENTAZIONE(CategoriaMissione.INVESTIGAZIONE),
    /**
     * Esempio: esplorare rovine perdute per ricomporre la storia di una civiltà attraverso iscrizioni magiche.
     * Coperto da: LIndagine (la tomba del re antico)
     */
    DECIFRAZIONE(CategoriaMissione.INVESTIGAZIONE),

    //----------

    /**
     * Esempio: scalare i vertici di una data fazione o gilda.
     * NON COPERTO
     */
    FAZIONE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: insegnare a un apprendista la magia, allenare una guardia, diventare maestro di una disciplina.
     * NON COPERTO
     */
    ADDESTRAMENTO(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: vincere una gara di magia, battere un campione in un duello, guadagnare un'elezione.
     * Coperto da: IncaricoDiCombattimento (la gara di magia)
     */
    COMPETIZIONE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: partecipare a torneo multi-round, competizione a eliminazione diretta.
     * Coperto da: IlTorneo (la Rosa Appassita, la giostra degli incantatori, il Boccale Rovesciato)
     */
    TORNEO(CategoriaMissione.PROGRESSIONE),
    // /**
    //  * Esempio: ricevere eredità, ereditare titoli, ricevere lasciti
    //  * Non fattibile nella Foresta: eredità e testamenti sono affari da notai, non da avventurieri.
    //  */
    // EREDITA(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: essere nominato a carica ufficiale, ricevere incarico importante.
     * NON COPERTO
     */
    NOMINA(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: aderire a gilda, fratellanza, unirsi a ordine.
     * NON COPERTO
     */
    FRATELLANZA(CategoriaMissione.PROGRESSIONE),
    // /**
    //  * Esempio: diventare re/regina, ascendere al trono, incoronazione
    //  * Non fattibile nella Foresta: nessuno del gruppo diventerà re: nella Foresta c'è già Sua Maestà.
    //  */
    // CORONAZIONE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: migliorare status, avanzamento sociale, migliorare condizione.
     * NON COPERTO
     */
    ASCESA_SOCIALE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: ottenere titolo nobile, blasone, titolo nobiliare.
     * Coperto da: IncaricoDiCombattimento (i titoli buffi di Sua Maestà, nella palude e fra le rovine)
     */
    TITOLO_NOBILIARE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: diventare maestro di un'arte, eccellenza, maestria.
     * NON COPERTO
     */
    MAESTRIA(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: specializzarsi in disciplina specifica.
     * NON COPERTO
     */
    SPECIALIZZAZIONE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: guadagnare fama, notorietà, celebrità, rinomanza.
     * NON COPERTO. Da non sviluppare per ora: servirebbe un conto della fama del gruppo, che il gioco non tiene.
     */
    FAMA(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: migliorare reputazione, onore, prestigio, credibilità.
     * NON COPERTO. Da non sviluppare per ora: servirebbe un conto della reputazione del gruppo, che il gioco non tiene.
     */
    REPUTAZIONE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: accumulare ricchezza, prosperità economica, tesoro.
     * NON COPERTO. Da non sviluppare per ora: accumulare monete non è una missione, è il gioco.
     */
    RICCHEZZA(CategoriaMissione.PROGRESSIONE),
    // /**
    //  * Esempio: stabilire discendenza nobile, genealogia, stirpe
    //  * Non fattibile nella Foresta: le genealogie nobili non si giocano a colpi di spada.
    //  */
    // LIGNAGGIO(CategoriaMissione.PROGRESSIONE),
    // /**
    //  * Esempio: diventare legatario, eredità nominata, designazione eredità
    //  * Non fattibile nella Foresta: eredità e testamenti sono affari da notai, non da avventurieri.
    //  */
    // LEGATARIO(CategoriaMissione.PROGRESSIONE),
    // /**
    //  * Esempio: diventare erede ufficiale, diritto successorio, linea successoria
    //  * Non fattibile nella Foresta: eredità e testamenti sono affari da notai, non da avventurieri.
    //  */
    // EREDE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: ricevere benedizione, favore divino, grazia divina.
     * Coperto da: LaBenedizione (con il favore, IlFavore, come missione secondaria)
     */
    BENEDIZIONE_RICEVERE(CategoriaMissione.PROGRESSIONE),
    // /**
    //  * Esempio: assicurare successione, diritto successorio organizzato
    //  * Non fattibile nella Foresta: le successioni al trono non sono affari del gruppo.
    //  */
    // SUCCESSIONE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: accumulare risorse per potenziare una base o una fortezza.
     * NON COPERTO
     */
    GESTIONE(CategoriaMissione.PROGRESSIONE),
    /**
     * Esempio: ricostruire una fortezza abbandonata.
     * NON COPERTO
     */
    RICOSTRUZIONE(CategoriaMissione.PROGRESSIONE),

    //----------

    /**
     * Esempio: aiutare uno dei membri del gruppo a risolvere un problema personale.
     * Coperto da: LaLealta
     */
    LEALTA(CategoriaMissione.RELAZIONI),
    /**
     * Esempio: organizzare un matrimonio, celebrare nozze, preparare una cerimonia.
     * NON COPERTO. Da non sviluppare per ora: sarebbe solo una raccolta di materiali (un regalo per gli sposi), già
     * coperta.
     */
    MATRIMONIO(CategoriaMissione.RELAZIONI),
    /**
     * Esempio: celebrare una vittoria, organizzare festa, carnevale, banchetto.
     * NON COPERTO. Da non sviluppare per ora: sarebbe solo una raccolta di materiali (le provviste per la festa), già
     * coperta.
     */
    CELEBRAZIONE(CategoriaMissione.RELAZIONI),
    // /**
    //  * Esempio: matrimonio diplomatico, unione politica, alleanza famigliare
    //  * Non fattibile nella Foresta: nessuno del gruppo si sposa per politica.
    //  */
    // ALLEANZA_MATRIMONIALE(CategoriaMissione.RELAZIONI),
    /**
     * Esempio: riscattarsi, redenzione, cambio di vita.
     * Coperto da: IlRituale (la vita nuova del brigante)
     */
    RISCATTO(CategoriaMissione.RELAZIONI),
    /**
     * Esempio: ottenere perdono, riconciliazione, pace interiore.
     * Coperto da: IlRituale (il perdono del fratello)
     */
    PERDONO(CategoriaMissione.RELAZIONI),
    /**
     * Esempio: redenzione pubblica, riabilitazione sociale, ristabilire reputazione.
     * Coperto da: IlRituale (il falò del cavaliere)
     */
    REDENZIONE_PUBBLICA(CategoriaMissione.RELAZIONI),

    //----------

    /**
     * Esempio: rubare un artefatto da una camera del tesoro o un documento da una biblioteca.
     * Coperto da: IlColpo (il libro dei conti dell'usuraio)
     */
    FURTO(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: assassinare silenziosamente un nemico senza farsi scoprire
    //  * Non adatto al tono del gioco: niente omicidi a sangue freddo, nemmeno tentati.
    //  */
    // ASSASSINIO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: commercio nel mercato nero, merci illegali, contrabbando organizzato.
     * Coperto da: IlContrabbandiere (le uova di viverna)
     */
    MERCATO_NERO(CategoriaMissione.NEGOZIAZIONE),
    /**
     * Esempio: sabotare i rifornimenti di un nemico o sabotare le difese di una fortezza.
     * Coperto da: IlColpo (le scale dei goblin)
     */
    SABOTAGGIO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: sabotaggio con esplosivi, demolizione, esplosione tattica.
     * Coperto da: IlColpo (la diga dei goblin)
     */
    ESPLOSIONE(CategoriaMissione.COMBATTIMENTO),
    /**
     * Esempio: spiare una riunione importante, raccogliere informazioni senza farsi scoprire.
     * Coperto da: LaSorveglianza (l'accampamento degli hobgoblin, il laboratorio del rivale)
     */
    SPIONAGGIO(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: ricattare qualcuno, estorcere denaro, blackmail, estorsione, estorsione sotto minaccia, minaccia per guadagni
    //  * Non adatto al tono del gioco: ricattare qualcuno è meschino, non sgangherato.
    //  */
    // RICATTO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: truffare un mercante, ingannare un nobile, corrompere un ufficiale.
     * NON COPERTO
     */
    INGANNO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: contrabbandare merci proibite attraverso un checkpoint.
     * Coperto da: IlContrabbandiere (il vino senza dazio, le sete, il libro proibito)
     */
    CONTRABBANDO(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: tradire una fazione, passare ai nemici, rivegliarsi come spia
    //  * Non fattibile nella Foresta: nella Foresta non ci sono fazioni da tradire.
    //  */
    // TRADIMENTO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: corrompere un ufficiale, offrire tangenti.
     * Coperto da: IlContrabbandiere (il sacchetto per il gabelliere)
     */
    CORRUZIONE(CategoriaMissione.ILLECITO),
    /**
     * Esempio: falsificare documenti, coniare monete false, creare oggetti contraffatti, falsificare firma,
     * dipinto falso, sigillo ufficiale.
     * Coperto da: IlColpo (il registro dei confini)
     */
    FALSIFICAZIONE(CategoriaMissione.ILLECITO),
    /**
     * Esempio: rapire qualcuno per riscatto, prendere in ostaggio.
     * NON COPERTO
     */
    RAPIMENTO(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: attaccare commercianti, razziare una costa, saccheggiare una carovana
    //  * Non fattibile nella Foresta: servono il mare e le navi; le carovane da saccheggiare le copre BRIGANTAGGIO.
    //  */
    // PIRATERIA(CategoriaMissione.ILLECITO),
    /**
     * Esempio: avvelenare cibo/acqua, contaminazione.
     * Coperto da: IlColpo (il sonno dei troll)
     */
    AVVELENAMENTO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: distruggere proprietà, vandalizzare edifici, rovinare strutture.
     * Coperto da: IlColpo (i baffi alla statua)
     */
    VANDALISMO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: frode finanziaria, schema fraudolento, imbroglio, matrimonio fraudolento.
     * Coperto da: IlContrabbandiere (le reliquie false)
     */
    FRODE(CategoriaMissione.ILLECITO),
    /**
     * Esempio: diffamare qualcuno, spargere voci calunniose, danneggiare reputazione.
     * Coperto da: IlContrabbandiere (i fogli satirici sul borgomastro)
     */
    DIFFAMAZIONE(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: ridurre in schiavitù, sfruttamento, servitù forzata
    //  * Non adatto al tono del gioco: la schiavitù non fa ridere nemmeno nella Foresta.
    //  */
    // SCHIAVITU(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: incitare ribellione, sedizione, sommossa, insurrezione, incitare disordini, sommossa pubblica
    //  * Non fattibile nella Foresta: nella Foresta non ci sono rivolte da sobillare: Sua Maestà ha già abbastanza
    //  * debiti.
    //  */
    // SEDIZIONE(CategoriaMissione.ILLECITO),
    /**
     * Esempio: incendiare, fuoco appiccato, piromane, incendio intenzionale, fuoco premeditato, fuoco criminale.
     * Coperto da: IlColpo (il deposito di frecce)
     */
    INCENDIO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: assassinare per denaro, omicidio su commissione, killer.
     * Coperto da: IncaricoDiCombattimento (il rivale dell'alchimista)
     */
    SICARIO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: riciclare denaro sporco, lavaggio denaro.
     * Coperto da: IlContrabbandiere (le monete col marchio)
     */
    RICICLAGGIO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: irruzione, invasione casa, violazione proprietà privata, imprigionamento illegale, sequestro.
     * Coperto da: IlColpo (la capanna della guaritrice)
     */
    VIOLAZIONE_DOMICILIO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: profanare luogo sacro, offesa religiosa, sacrilegio, bestemmia, profanazione religiosa.
     * Coperto da: IlColpo (l'idolo dei goblin)
     */
    SACRILEGIO(CategoriaMissione.ILLECITO),
    /**
     * Esempio: traffico di merci proibite, contrabbando su larga scala.
     * Coperto da: IlContrabbandiere (le erbe proibite, le pozioni rubate, i folletti)
     */
    TRAFFICO(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: rubare identità, usurpazione, furto identità
    //  * Non adatto al tono del gioco: rubare la vita a qualcuno non è una burla.
    //  */
    // FURTO_IDENTITA(CategoriaMissione.ILLECITO),
    /**
     * Esempio: rapire, furto a mano armata, rapinare.
     * Coperto da: IncaricoDiCombattimento (la carovana dell'usuraio)
     */
    BRIGANTAGGIO(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: testimonianza falsa, perjury, giuramento falso
    //  * Non adatto al tono del gioco: mandare in rovina un innocente con un falso giuramento non è sgangherato, è
    //  * cattivo.
    //  */
    // FALSA_TESTIMONIANZA(CategoriaMissione.ILLECITO),
    /**
     * Esempio: imbrogliare al gioco, baro, truffa al gioco d'azzardo.
     * NON COPERTO
     */
    IMBROGLIONE(CategoriaMissione.ILLECITO),
    /**
     * Esempio: imprigionare qualcuno, tenere in carcere, reclusione.
     * Coperto da: IncaricoDiCombattimento (il brigante da catturare vivo)
     */
    CONFINAMENTO(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: tortura, estorsione confessione, interrogatorio forzato
    //  * Non adatto al tono del gioco: niente torture, nemmeno ai goblin.
    //  */
    // TORTURA(CategoriaMissione.ILLECITO),
    // /**
    //  * Esempio: tentativo di omicidio, assassinio fallito, aggressione mortale
    //  * Non adatto al tono del gioco: niente omicidi a sangue freddo, nemmeno tentati.
    //  */
    // TENTATIVO_OMICIDIO(CategoriaMissione.ILLECITO),

    //----------

    /**
     * Esempio: purificare una terra corrotta, bonificare un luogo maledetto.
     * Coperto da: IlRituale (il bosco corrotto)
     */
    PURIFICAZIONE(CategoriaMissione.PROTEZIONE),
    /**
     * Esempio: spezzare una maledizione su uno spirito tormentato.
     * Coperto da: IlRituale (lo spirito della palude)
     */
    SPEZZATURA(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: benedire un luogo maledetto, purificare un'anima corrotta, benedire una coppia.
     * Coperto da: IlRituale (la fonte dei neonati)
     */
    BENEDIZIONE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: parlare con spiriti, comunicare con divinità, ottenere messaggi soprannaturali, negoziare con entità.
     * Coperto da: IlRituale (la voce del conte)
     */
    COMUNICAZIONE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: visione del passato, retrocognizione, rivivere momenti passati, leggere storia di oggetti, psicometria.
     * Coperto da: IlRituale (la memoria delle pietre)
     */
    VISIONE_PASSATO(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: leggere mente, telepathy, penetrazione psichica, percepire pensieri altrui
    //  * Non fattibile nella Foresta: nessuno nel gruppo legge nel pensiero.
    //  */
    // LETTURA_MENTE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: visione del futuro, precognizione, profezia, astrologia, lettura stelle, predizioni, divinazione.
     * Coperto da: IlRituale (lo stagno del domani)
     */
    VISIONE_FUTURO(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: eseguire un rituale di evocazione per contattare uno spirito o sigillare un portale.
     * Coperto da: IlRituale (il fuoco del solstizio)
     */
    RITUALE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: animare i non-morti, controllare scheletri, risvegliare cadaveri.
     * Coperto da: IlRituale (il testamento del nonno)
     */
    NECROMANZIA(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: evocare creature, convocare entità soprannaturali, richiamare spiriti.
     * Coperto da: IlRituale (il guardiano di pietra)
     */
    EVOCAZIONE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: lanciare maledizioni, infliggere curse, danneggiare tramite magia nera.
     * Coperto da: IlRituale (le zucche del vicino)
     */
    MALEDIZIONE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: lanciare incantesimi complessi, magia avanzata, effetti magici potenti.
     * Coperto da: IlRituale (la torre pendente)
     */
    INCANTESIMO(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: sigillare portali, incantesimi sigillo, chiudere varchi magici.
     * Coperto da: IlRituale (il varco degli spettri)
     */
    SIGILLO(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: trasformare metalli, alchemy, alchimia, trasmutazione.
     * Coperto da: IlRituale (il piombo e l'oro)
     */
    TRASMUTAZIONE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: astrologia, lettura stelle, predizioni, divinazione.
     * Coperto da: IlRituale (la congiunzione degli astri)
     */
    ASTRI(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: leggere il futuro, predizioni magiche, visioni.
     * Coperto da: IlRituale (l'occhio della palude)
     */
    DIVINAZIONE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: creare illusioni magiche, inganno illusorio, miraggio.
     * Coperto da: IlRituale (il drago che non c'è)
     */
    ILLUSIONE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: contrastare magia, dispellare incantesimi, anti-magia.
     * Coperto da: IlRituale (la pioggia di rane)
     */
    ANTI_MAGIA(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: diventare invisibile, magia occultamento, mimetizzazione
    //  * Non fattibile nella Foresta: nessuno nel gruppo sa diventare invisibile (per non farsi vedere c'è il passare
    //  * inosservati).
    //  */
    // INVISIBILITA(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: canale energie magiche, conduit, canalizzazione.
     * Coperto da: IlRituale (i lampioni della città)
     */
    CHANNELING(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: teletrasporto magico, portale, viaggio istantaneo
    //  * Non fattibile nella Foresta: nella Foresta si va a piedi, non ci sono portali.
    //  */
    // TELEPORTAZIONE(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: trasformarsi in bestia, mutazione, cambio forma
    //  * Non fattibile nella Foresta: nessuno nel gruppo cambia forma.
    //  */
    // SHAPE_SHIFT(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: controllare elementi, dominio elementale, magia elementare.
     * Coperto da: IlRituale (la pioggia per il mulino)
     */
    CONTROLLO_ELEMENTALE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: legare spiriti, connessione soprannaturale, legame spirituale.
     * Coperto da: IlRituale (lo spirito della bottega)
     */
    LEGAME_SPIRITUALE(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: viaggio astrale, proiezione eterica, corpo astrale, proiezione astrale conscia, controllo corpo astrale, viaggio etereo controllato
    //  * Non fattibile nella Foresta: non c'è un piano astrale in cui andare.
    //  */
    // VIAGGIO_ASTRALE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: comunione con entità divina, connessione divina, unione sacra.
     * Coperto da: IlRituale (la dea del raccolto)
     */
    COMUNIONE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: entrare in trance, meditazione profonda, stato alterato.
     * Coperto da: IlRituale (la trance di fratello Anselmo)
     */
    TRANCE(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: fusione di esseri/anime, unione spirituale, sincretismo
    //  * Non fattibile nella Foresta: nessuno nel gruppo fonde anime o esseri.
    //  */
    // FUSIONE(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: viaggio nel tempo, manipolazione temporale, viaggio temporale
    //  * Non fattibile nella Foresta: non ci sono altre epoche in cui andare.
    //  */
    // VIAGGIO_TEMPO(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: accesso a realtà parallela, dimensioni alternative, mondi alternativi
    //  * Non fattibile nella Foresta: non ci sono altre dimensioni in cui andare.
    //  */
    // REALTA_PARALLELA(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: possedere corpo, occupazione fisica, usurpazione corporea, possessione dell'anima, usurpazione spirituale, controllo anima
    //  * Non fattibile nella Foresta: nessuno nel gruppo sa possedere un corpo (le possessioni da scacciare le copre
    //  * POSSESSIONE).
    //  */
    // POSSESSO_CORPO(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: assorbire potenza, estrazione di energia, vampirismo magico
    //  * Non fattibile nella Foresta: nessuno nel gruppo sa assorbire la potenza altrui.
    //  */
    // ASSORBIMENTO(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: controllo mentale, dominio psichico, schiavitù mentale
    //  * Non fattibile nella Foresta: nessuno nel gruppo controlla le menti.
    //  */
    // CONTROLLO_MENTE(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: scambio di corpi, trasferimento corporeo, swap souls
    //  * Non fattibile nella Foresta: nessuno nel gruppo scambia i corpi.
    //  */
    // SCAMBIO_CORPI(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: creazione di golem, animazione artificiale, creatura magica.
     * Coperto da: IlRituale (il golem d'argilla)
     */
    CREAZIONE_GOLEM(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: divisione di esseri, scissione di anima, separazione magica
    //  * Non fattibile nella Foresta: nessuno nel gruppo divide esseri o anime.
    //  */
    // FISSIONE(CategoriaMissione.SPIRITUALE),
    // /**
    //  * Esempio: morte temporale, ibernazione magica, sonno eterno
    //  * Non fattibile nella Foresta: nessuno nel gruppo sa far dormire per sempre (né svegliare chi dorme così).
    //  */
    // MORTE_TEMPORALE(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: animazione di oggetti inanimati, enchantment animato, vita artificiale.
     * Coperto da: IlRituale (lo spaventapasseri vivo)
     */
    ANIMAZIONE_OGGETTI(CategoriaMissione.SPIRITUALE),
    /**
     * Esempio: patto con demoni/entità, contratto spirituale, alleanza soprannaturale.
     * Coperto da: IlRituale (il patto con lo spirito del fiume)
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
