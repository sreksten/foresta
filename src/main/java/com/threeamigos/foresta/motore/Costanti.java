package com.threeamigos.foresta.motore;

/**
 *
 * @author Stefano Reksten
 */
public class Costanti {

    // Incantesimi
    public static final int INCANTESIMO_ACQUA_COSTO_ACQUISTO = 5;
    public static final int INCANTESIMO_ACQUA_COSTO_LANCIO = 1;
    public static final int INCANTESIMO_ACQUA_DANNI = 45;

    public static final int INCANTESIMO_ARIA_COSTO_ACQUISTO = 5;
    public static final int INCANTESIMO_ARIA_COSTO_LANCIO = 1;
    public static final int INCANTESIMO_ARIA_DANNI = 35;

    public static final int INCANTESIMO_FULMINE_COSTO_ACQUISTO = 15;
    public static final int INCANTESIMO_FULMINE_COSTO_LANCIO = 10;
    public static final int INCANTESIMO_FULMINE_DANNI = 100;

    public static final int INCANTESIMO_FUOCO_COSTO_ACQUISTO = 5;
    public static final int INCANTESIMO_FUOCO_COSTO_LANCIO = 3;
    public static final int INCANTESIMO_FUOCO_DANNI = 60;

    public static final int INCANTESIMO_MORTE_COSTO_ACQUISTO = 5;
    public static final int INCANTESIMO_MORTE_COSTO_LANCIO = 5;
    public static final int INCANTESIMO_MORTE_DANNI = 0;

    public static final int INCANTESIMO_RESURREZIONE_COSTO_ACQUISTO = 15;
    public static final int INCANTESIMO_RESURREZIONE_COSTO_LANCIO = 15;

    public static final int INCANTESIMO_TERRA_COSTO_ACQUISTO = 5;
    public static final int INCANTESIMO_TERRA_COSTO_LANCIO = 2;
    public static final int INCANTESIMO_TERRA_DANNI = 50;

    // Pozioni
    public static final int RECUPERO_DA_POZIONE_SALUTE = 100;
    public static final int RECUPERO_DA_POZIONE_SALUTE_GRANDE = 150;
    public static final int AUMENTO_SALUTE_DA_POZIONE_SALUTE_GRANDE = 10;
    public static final int RECUPERO_DA_POZIONE_MAGIA = 10;

    // Oggetti
    public static final int ANELLO_MAGICO_AGGIUNTA_VALORE = 5;
    public static final int ANELLO_MAGICO_AGGIUNTA_CORAGGIO = 5;
    public static final int ANELLO_MAGICO_AGGIUNTA_CARISMA = 1;
    public static final int ANELLO_MAGICO_PUNTEGGIO = 50;

    // Gruppo
    public static final int MAX_PERSONAGGI_GRUPPO_GIOCATORE = 5;

    // Dall'alchimista
    public static final int COSTO_POZIONE_SALUTE = 5;
    public static final int COSTO_POZIONE_MAGIA = 10;
    public static final int COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO = 10;
    public static final int AUMENTO_MAGIA_PERSONAGGIO = 20; //  Da pozione o alchimista

    // In locanda
    public static final int COSTO_PASTO = 5;
    public static final int COSTO_PERNOTTAMENTO = 5;
    public static final int RECUPERO_SALUTE_DA_PASTO = 100;

    // Castelli
    public static final int COFANI_IN_CASTELLO_IDRA = 5;
    public static final int COFANI_IN_CASTELLO_LICH = 5;
    public static final int COFANI_IN_CASTELLO_MINOTAURO = 5;
    public static final int COFANI_IN_CASTELLO_STREGA = 5;

    // Foresta
    public static final int MAX_DIMENSIONE_LATO_FORESTA = 80;

    public static final int COSTO_MAPPA_DELLA_FORESTA = 10;

    // Personaggio
    public static final int MAX_STANCHEZZA = 9;
    public static final int MAX_CARISMA = 9;
    public static final int MAX_VALORE = 99;
    public static final int MAX_CORAGGIO = 99;

    // Statistiche dei vari tipi di personaggio

    // Personaggi giocabili

    public static final int BARDO_MAX_SALUTE = 400;
    public static final int BARDO_MAX_MAGIA = 50;
    public static final int BARDO_MAX_VALORE = 50;
    public static final int BARDO_MAX_CORAGGIO = 50;
    public static final int BARDO_MAX_CARISMA = 8;

    public static final int CANTASTORIE_MAX_SALUTE = 400;
    public static final int CANTASTORIE_MAX_MAGIA = 50;
    public static final int CANTASTORIE_MAX_VALORE = 50;
    public static final int CANTASTORIE_MAX_CORAGGIO = 50;
    public static final int CANTASTORIE_MAX_CARISMA = 8;

    public static final int ELFA_MAX_SALUTE = 350;
    public static final int ELFA_MAX_MAGIA = 60;
    public static final int ELFA_MAX_VALORE = 40;
    public static final int ELFA_MAX_CORAGGIO = 40;
    public static final int ELFA_MAX_CARISMA = 7;
    public static final int ELFA_RECUPERO_MAGIA = 2;
    public static final int ELFA_MODIFICATORE_DANNI_MAGIA = 2;
    public static final int ELFA_BERSAGLI_PER_INCANTESIMO_BONUS = 1;

    public static final int ELFO_MAX_SALUTE = 350;
    public static final int ELFO_MAX_MAGIA = 60;
    public static final int ELFO_MAX_VALORE = 40;
    public static final int ELFO_MAX_CORAGGIO = 40;
    public static final int ELFO_MAX_CARISMA = 7;
    public static final int ELFO_RECUPERO_MAGIA = 2;
    public static final int ELFO_MODIFICATORE_DANNI_MAGIA = 2;
    public static final int ELFO_BERSAGLI_PER_INCANTESIMO_BONUS = 1;

    public static final int GUERRIERA_MAX_SALUTE = 550;
    public static final int GUERRIERA_MAX_MAGIA = 30;
    public static final int GUERRIERA_MAX_VALORE = 70;
    public static final int GUERRIERA_MAX_CORAGGIO = 70;
    public static final int GUERRIERA_MAX_CARISMA = 5;
    public static final int GUERRIERA_MODIFICATORE_DANNI_FORZA = 2;

    public static final int GUERRIERO_MAX_SALUTE = 550;
    public static final int GUERRIERO_MAX_MAGIA = 30;
    public static final int GUERRIERO_MAX_VALORE = 70;
    public static final int GUERRIERO_MAX_CORAGGIO = 70;
    public static final int GUERRIERO_MAX_CARISMA = 5;
    public static final int GUERRIERO_MODIFICATORE_DANNI_FORZA = 2;

    public static final int LADRA_MAX_SALUTE = 450;
    public static final int LADRA_MAX_MAGIA = 40;
    public static final int LADRA_MAX_VALORE = 60;
    public static final int LADRA_MAX_CORAGGIO = 60;
    public static final int LADRA_MAX_CARISMA = 6;

    public static final int LADRO_MAX_SALUTE = 450;
    public static final int LADRO_MAX_MAGIA = 40;
    public static final int LADRO_MAX_VALORE = 60;
    public static final int LADRO_MAX_CORAGGIO = 60;
    public static final int LADRO_MAX_CARISMA = 6;

    public static final int MAGA_MAX_SALUTE = 350;
    public static final int MAGA_MAX_MAGIA = 70;
    public static final int MAGA_MAX_VALORE = 30;
    public static final int MAGA_MAX_CORAGGIO = 30;
    public static final int MAGA_MAX_CARISMA = 6;
    public static final int MAGA_RECUPERO_FORZA = 35;
    public static final int MAGA_RECUPERO_MAGIA = 3;
    public static final int MAGA_MODIFICATORE_DANNI_MAGIA = 3;
    public static final int MAGA_BERSAGLI_PER_INCANTESIMO_BONUS = 2;

    public static final int MAGO_MAX_SALUTE = 350;
    public static final int MAGO_MAX_MAGIA = 70;
    public static final int MAGO_MAX_VALORE = 30;
    public static final int MAGO_MAX_CORAGGIO = 30;
    public static final int MAGO_MAX_CARISMA = 6;
    public static final int MAGO_RECUPERO_FORZA = 35;
    public static final int MAGO_RECUPERO_MAGIA = 3;
    public static final int MAGO_MODIFICATORE_DANNI_MAGIA = 3;
    public static final int MAGO_BERSAGLI_PER_INCANTESIMO_BONUS = 2;

    // Personaggio giocabile segreto per test

    public static final int OMBRAFIAMMA_MAX_SALUTE = 500;
    public static final int OMBRAFIAMMA_MAX_MAGIA = 90;
    public static final int OMBRAFIAMMA_MAX_VALORE = 100;
    public static final int OMBRAFIAMMA_MAX_CORAGGIO = 100;
    public static final int OMBRAFIAMMA_MAX_CARISMA = 9;
    public static final int OMBRAFIAMMA_RECUPERO_MAGIA = 5;
    public static final int OMBRAFIAMMA_MODIFICATORE_DANNI_FORZA = 3;
    public static final int OMBRAFIAMMA_MODIFICATORE_DANNI_MAGIA = 5;
    public static final int OMBRAFIAMMA_BERSAGLI_PER_INCANTESIMO_BONUS = 4;

    // Personaggi non giocabili (avversari). Alcuno possono essere amichevoli o corrompibili e offrire supporto

    public static final int ARPIA_MAX_SALUTE = 30;
    public static final int ARPIA_MAX_MAGIA = 5;
    public static final int ARPIA_MAX_VALORE = 30;
    public static final int ARPIA_MAX_CORAGGIO = 30;
    public static final int ARPIA_MAX_CARISMA = 0;
    public static final int ARPIA_MAX_NUMERO = 4;

    public static final int CENTAURO_MAX_SALUTE = 70;
    public static final int CENTAURO_MAX_MAGIA = 0;
    public static final int CENTAURO_MAX_VALORE = 40;
    public static final int CENTAURO_MAX_CORAGGIO = 60;
    public static final int CENTAURO_MAX_CARISMA = 4;
    public static final int CENTAURO_MAX_NUMERO = 4;

    public static final int CHIMERA_MAX_SALUTE = 45;
    public static final int CHIMERA_MAX_MAGIA = 0;
    public static final int CHIMERA_MAX_VALORE = 50;
    public static final int CHIMERA_MAX_CORAGGIO = 60;
    public static final int CHIMERA_MAX_CARISMA = 0;
    public static final int CHIMERA_MAX_NUMERO = 5;

    public static final int CHIMERADRAGO_MAX_SALUTE = 80;
    public static final int CHIMERADRAGO_MAX_MAGIA = 30;
    public static final int CHIMERADRAGO_MAX_VALORE = 60;
    public static final int CHIMERADRAGO_MAX_CORAGGIO = 75;
    public static final int CHIMERADRAGO_MAX_CARISMA = 0;
    public static final int CHIMERADRAGO_MAX_NUMERO = 2;

    public static final int EREMITA_MAX_SALUTE = 70;
    public static final int EREMITA_MAX_MAGIA = 0;
    public static final int EREMITA_MAX_VALORE = 50;
    public static final int EREMITA_MAX_CORAGGIO = 80;
    public static final int EREMITA_MAX_CARISMA = 1;

    public static final int FANTASMA_MAX_SALUTE = 50;
    public static final int FANTASMA_MAX_MAGIA = 0;
    public static final int FANTASMA_MAX_VALORE = 70;
    public static final int FANTASMA_MAX_CORAGGIO = 60;
    public static final int FANTASMA_MAX_CARISMA = 0;
    public static final int FANTASMA_MAX_NUMERO = 3;

    public static final int FOLLETTO_MAX_SALUTE = 10;
    public static final int FOLLETTO_MAX_MAGIA = 1;
    public static final int FOLLETTO_MAX_VALORE = 40;
    public static final int FOLLETTO_MAX_CORAGGIO = 20;
    public static final int FOLLETTO_MAX_CARISMA = 1;
    public static final int FOLLETTO_MAX_NUMERO = 5;

    public static final int GARGOYLE_MAX_SALUTE = 50;
    public static final int GARGOYLE_MAX_MAGIA = 20;
    public static final int GARGOYLE_MAX_VALORE = 70;
    public static final int GARGOYLE_MAX_CORAGGIO = 70;
    public static final int GARGOYLE_MAX_CARISMA = 0;
    public static final int GARGOYLE_RECUPERO_FORZA = 20;
    public static final int GARGOYLE_RECUPERO_MAGIA = 2;
    public static final int GARGOYLE_MAX_NUMERO = 2;

    public static final int GIGANTE_MAX_SALUTE = 80;
    public static final int GIGANTE_MAX_MAGIA = 10;
    public static final int GIGANTE_MAX_VALORE = 60;
    public static final int GIGANTE_MAX_CORAGGIO = 80;
    public static final int GIGANTE_MAX_CARISMA = 3;
    public static final int GIGANTE_MAX_NUMERO = 3;

    public static final int GOBLIN_MAX_SALUTE = 40;
    public static final int GOBLIN_MAX_MAGIA = 0;
    public static final int GOBLIN_MAX_VALORE = 30;
    public static final int GOBLIN_MAX_CORAGGIO = 50;
    public static final int GOBLIN_MAX_CARISMA = 0;
    public static final int GOBLIN_MAX_NUMERO = 5;

    public static final int HOBGOBLIN_MAX_SALUTE = 65;
    public static final int HOBGOBLIN_MAX_MAGIA = 10;
    public static final int HOBGOBLIN_MAX_VALORE = 40;
    public static final int HOBGOBLIN_MAX_CORAGGIO = 60;
    public static final int HOBGOBLIN_MAX_CARISMA = 0;
    public static final int HOBGOBLIN_RECUPERO_FORZA = 8;
    public static final int HOBGOBLIN_MAX_NUMERO = 3;

    public static final int MINOTAURO_MAX_SALUTE = 70;
    public static final int MINOTAURO_MAX_MAGIA = 0;
    public static final int MINOTAURO_MAX_VALORE = 50;
    public static final int MINOTAURO_MAX_CORAGGIO = 50;
    public static final int MINOTAURO_MAX_CARISMA = 0;
    public static final int MINOTAURO_MAX_NUMERO = 3;

    public static final int OMBRANERA_MAX_SALUTE = 100;
    public static final int OMBRANERA_MAX_MAGIA = 50;
    public static final int OMBRANERA_MAX_VALORE = 70;
    public static final int OMBRANERA_MAX_CORAGGIO = 90;
    public static final int OMBRANERA_MAX_CARISMA = 0;
    public static final int OMBRANERA_RECUPERO_MAGIA = 2;
    public static final int OMBRANERA_MAX_NUMERO = 2;

    public static final int SCHELETRO_MAX_SALUTE = 50;
    public static final int SCHELETRO_MAX_MAGIA = 0;
    public static final int SCHELETRO_MAX_VALORE = 40;
    public static final int SCHELETRO_MAX_CORAGGIO = 50;
    public static final int SCHELETRO_MAX_CARISMA = 0;
    public static final int SCHELETRO_MAX_NUMERO = 4;

    public static final int SPETTRO_MAX_SALUTE = 40;
    public static final int SPETTRO_MAX_MAGIA = 0;
    public static final int SPETTRO_MAX_VALORE = 50;
    public static final int SPETTRO_MAX_CORAGGIO = 50;
    public static final int SPETTRO_MAX_CARISMA = 0;
    public static final int SPETTRO_MAX_NUMERO = 2;

    public static final int SPIRITO_MAX_SALUTE = 40;
    public static final int SPIRITO_MAX_MAGIA = 0;
    public static final int SPIRITO_MAX_VALORE = 50;
    public static final int SPIRITO_MAX_CORAGGIO = 50;
    public static final int SPIRITO_MAX_CARISMA = 0;
    public static final int SPIRITO_MAX_NUMERO = 2;

    public static final int TITANO_MAX_SALUTE = 100;
    public static final int TITANO_MAX_MAGIA = 15;
    public static final int TITANO_MAX_VALORE = 80;
    public static final int TITANO_MAX_CORAGGIO = 90;
    public static final int TITANO_MAX_CARISMA = 3;
    public static final int TITANO_MAX_NUMERO = 3;

    public static final int TROLL_MAX_SALUTE = 80;
    public static final int TROLL_MAX_MAGIA = 0;
    public static final int TROLL_MAX_VALORE = 60;
    public static final int TROLL_MAX_CORAGGIO = 80;
    public static final int TROLL_MAX_CARISMA = 0;
    public static final int TROLL_MAX_NUMERO = 3;

    public static final int VIVERNA_MAX_SALUTE = 120;
    public static final int VIVERNA_MAX_MAGIA = 50;
    public static final int VIVERNA_MAX_VALORE = 80;
    public static final int VIVERNA_MAX_CORAGGIO = 90;
    public static final int VIVERNA_MAX_CARISMA = 0;
    public static final int VIVERNA_MAX_NUMERO = 3;
    public static final int VIVERNA_RECUPERO_MAGIA = 3;

    // Boss

    public static final int IDRA_MAX_SALUTE = 600;
    public static final int IDRA_MAX_MAGIA = 0;
    public static final int IDRA_MAX_VALORE = 70;
    public static final int IDRA_MAX_CORAGGIO = 70;
    public static final int IDRA_MAX_CARISMA = 0;

    public static final int LICH_MAX_SALUTE = 700;
    public static final int LICH_MAX_MAGIA = 200;
    public static final int LICH_MAX_VALORE = 50;
    public static final int LICH_MAX_CORAGGIO = 70;
    public static final int LICH_MAX_CARISMA = 0;
    public static final int LICH_RECUPERO_MAGIA = 20;
    public static final int LICH_MODIFICATORE_DANNI_MAGIA = 3;
    public static final int LICH_BERSAGLI_PER_INCANTESIMO = 5;

    public static final int MINOTAUROGIGANTE_MAX_SALUTE = 700;
    public static final int MINOTAUROGIGANTE_MAX_MAGIA = 0;
    public static final int MINOTAUROGIGANTE_MAX_VALORE = 80;
    public static final int MINOTAUROGIGANTE_MAX_CORAGGIO = 90;
    public static final int MINOTAUROGIGANTE_MAX_CARISMA = 0;

    public static final int STREGA_MAX_SALUTE = 800;
    public static final int STREGA_MAX_MAGIA = 350;
    public static final int STREGA_MAX_VALORE = 50;
    public static final int STREGA_MAX_CORAGGIO = 60;
    public static final int STREGA_MAX_CARISMA = 0;
    public static final int STREGA_RECUPERO_MAGIA = 35;
    public static final int STREGA_MODIFICATORE_DANNI_MAGIA = 3;
    public static final int STREGA_BERSAGLI_PER_INCANTESIMO = 5;

    // Boss finale

    public static final int DRAGO_MAX_SALUTE = 1000;
    public static final int DRAGO_MAX_MAGIA = 500;
    public static final int DRAGO_MAX_VALORE = 80;
    public static final int DRAGO_MAX_CORAGGIO = 90;
    public static final int DRAGO_MAX_CARISMA = 8;
    public static final int DRAGO_RECUPERO_MAGIA = 50;
    public static final int DRAGO_MODIFICATORE_DANNI_MAGIA = 3;
    public static final int DRAGO_BERSAGLI_PER_INCANTESIMO = 5;

}
