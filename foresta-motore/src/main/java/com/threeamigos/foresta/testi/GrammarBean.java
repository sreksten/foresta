package com.threeamigos.foresta.testi;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.threeamigos.foresta.strumenti.Logger;
import com.threeamigos.foresta.strumenti.Dado;

/**
 * Legge un file di grammatica in formato testuale e lo usa per generare ("produrre") testo
 * in modo casuale, espandendo ricorsivamente i riferimenti alle produzioni.
 * <p>
 * Sintassi del file di grammatica:
 * <ul>
 *     <li>Una riga che inizia alla colonna 0 dichiara una <b>produzione</b>: il suo nome.</li>
 *     <li>Le righe rientrate con un TAB o con uno spazio sono i <b>figli</b> della produzione
 *     (le alternative): il testo che può essere scelto a caso quando la produzione viene
 *     espansa. Più alternative possono stare sulla stessa riga, separate da
 *     {@code |}.</li>
 *     <li>Un figlio può riferirsi a un'altra produzione racchiudendone il nome fra parentesi
 *     quadre, ad esempio {@code [AltraProduzione]}; all'espansione il riferimento viene
 *     sostituito da un'alternativa di {@code AltraProduzione} scelta a caso (e a sua volta
 *     espansa ricorsivamente).</li>
 *     <li>Un figlio può contenere un <b>gruppo di alternative in linea</b>,
 *     {@code {opz1|opz2|...}}: al caricamento viene trasformato in una produzione nuova, con un
 *     nome generato automaticamente (vedi {@link #expandInlineAlternations}), e il gruppo è
 *     sostituito da un riferimento a essa, così da quel momento si comporta esattamente come
 *     un normale riferimento {@code [Nome]}. Esempio:
 *     <pre>{@code
 *     FATTO_PROEMIO
 *         Il {vecchio|giovane|misterioso} viandante bussò alla porta
 *     }</pre></li>
 *     <li>Un tratto delimitato da virgolette doppie ({@code "..."}) è un <b>tratto letterale
 *     grezzo</b>: {@code {}}, {@code }}} e {@code |} al suo interno sono trattati come testo
 *     semplice invece che come sintassi dei gruppi di alternative in linea, e {@code \"} al
 *     suo interno è una virgoletta letterale con l'escape, che non chiude il tratto. Un
 *     tratto lasciato aperto a fine riga (o a fine contenuto di un gruppo di alternative in
 *     linea) è un errore. Si noti che questo protegge solo {@code {}}, {@code }}} e
 *     {@code |}: un testo letterale che somigli a {@code [Nome]} dentro un tratto verrebbe
 *     comunque interpretato come riferimento a una produzione.</li>
 *     <li>Un nome di produzione che termina con {@code $} è <b>monouso</b>: una volta scelta una
 *     delle sue alternative durante un ciclo di produzione, quell'alternativa (e, se era
 *     l'ultima rimasta, l'intera produzione e ogni riferimento a essa) viene rimossa per il
 *     resto del ciclo.</li>
 *     <li>Un riferimento preceduto da {@code *} (ad esempio {@code [*Nome]}) è una
 *     <b>produzione fissata globalmente</b>: la prima volta che viene espansa nel corso di una
 *     chiamata a {@link #produce()}, il risultato viene messo in cache e riusato per ogni
 *     occorrenza successiva, in qualsiasi punto del testo prodotto.</li>
 *     <li>Un riferimento preceduto da {@code !} (ad esempio {@code [!Nome]}) è una
 *     <b>produzione fissata localmente</b>: come quella globale, ma il valore in cache viene
 *     riusato solo all'interno dello stesso sottoalbero di produzione (cioè non trapela nei
 *     rami fratelli prodotti in modo indipendente). È visibile a tutto ciò che sta annidato
 *     sotto il punto che l'ha fissato, a qualsiasi profondità, così un {@code [#Nome]} dentro
 *     una produzione referenziata trova comunque il valore fissato da chi l'ha chiamata.</li>
 *     <li>Un riferimento della forma {@code [chiave=valore]} <b>assegna</b> un valore
 *     a {@code chiave} nella stessa cache globale usata dai riferimenti {@code *}, per
 *     recuperarlo poi con un riferimento {@code #} in qualsiasi punto del testo prodotto.
 *     {@code valore} può a sua volta contenere elementi {@code [...]} (ad esempio
 *     {@code [chiave=[AltraProduzione]]}), che vengono risolti completamente prima che il
 *     risultato sia memorizzato.</li>
 *     <li>Un riferimento preceduto da {@code #} (ad esempio {@code [#chiave]}) recupera il
 *     valore assegnato in precedenza a {@code chiave} con un elemento {@code chiave=valore}.</li>
 *     <li>Un riferimento di qualsiasi tipo può avere un <b>valore predefinito</b>, scritto dopo il
 *     nome referenziato come {@code ?} seguito da {@code |} e dal testo di ripiego:
 *     {@code [*EQUIPAGGIAMENTO? | fagotto]}. Gli spazi attorno a {@code ?} e a {@code |} sono
 *     ignorati, e {@code [Nome?]} da solo è la forma abbreviata per un predefinito vuoto. Il
 *     ripiego è normale testo di grammatica: un letterale, un {@code [Riferimento]}, un gruppo
 *     in linea, o un misto. Si usa ogni volta che in quel punto non c'è alcun valore
 *     disponibile, perché il nome non è mai stato dichiarato, perché il {@code [chiave=valore]}
 *     che lo avrebbe riempito non è ancora stato eseguito, o perché una produzione monouso è
 *     esaurita: è ciò che permette di scrivere una grammatica senza dichiarare una produzione
 *     segnaposto solo per superare la validazione. Il {@code ?} dà tolleranza su <i>quando</i>
 *     compare un valore, non sul fatto che il nome abbia un significato: il nome deve comunque
 *     essere una produzione dichiarata o una chiave assegnata da qualche parte nella
 *     grammatica, così un errore di battitura viene rifiutato comunque al caricamento (vedi
 *     {@link #checkTokenValidity}). Un nome di produzione quindi non può terminare con
 *     {@code ?}. Il valore di un'assegnazione è testo libero, perciò {@code ?} e {@code |}
 *     dentro {@code [chiave=valore]} restano testo semplice.</li>
 *     <li>Una riga che inizia con {@code //} è un commento a riga intera e viene ignorata, così
 *     come le righe vuote.</li>
 *     <li>Un'alternativa può iniziare con un elemento {@code [^N]} ({@code N} un numero
 *     positivo, ad esempio {@code 10} o {@code 2.5}) per darle un <b>peso</b> di selezione: con
 *     {@link TipoModalitaProduzioneTesti#CASUALE} viene scelta {@code N} volte più spesso di
 *     un'alternativa col peso predefinito di 1.
 *     L'elemento è risolto e tolto al caricamento della grammatica (a differenza di
 *     {@code [chiave=valore]}, che è risolto in modo pigro durante la produzione), perciò non
 *     compare mai nel testo prodotto. Un'alternativa senza {@code [^N]} ha peso 1,
 *     esattamente come la selezione uniforme che ogni alternativa aveva prima che questa
 *     funzione esistesse, comprese le produzioni con nome automatico create dai gruppi di
 *     alternative in linea, le cui opzioni possono avere a loro volta un elemento {@code [^N]}
 *     ma non richiedono alcun trattamento speciale per restare uniformemente casuali.</li>
 *     <li>Una volta che l'intera grammatica è stata analizzata e validata, il peso di ogni
 *     alternativa viene aumentato automaticamente di {@link #SCALE_FACTOR} volte il peso
 *     aggregato di ogni produzione che essa referenzia (ricorsivamente, dato che il peso di
 *     una produzione referenziata è a quel punto già stato aumentato da ciò che a sua volta
 *     referenzia), così le alternative che si espandono in sottoalberi strutturalmente più
 *     ricchi vengono scelte proporzionalmente più spesso con
 *     {@link TipoModalitaProduzioneTesti#CASUALE}, senza bisogno di scrivere a mano un
 *     elemento {@code [^N]}. Una produzione referenziata più di una volta nella stessa
 *     alternativa contribuisce all'aumento una volta per ogni occorrenza. Un riferimento con
 *     un predefinito conta o la produzione referenziata o ciò che il suo ripiego referenzia,
 *     mai entrambi, perché al momento della produzione se ne prende solo uno.
 *     Questo aggiustamento è calcolato una sola volta, al caricamento della grammatica, e
 *     incorporato nei pesi usati per tutta la vita del {@code GrammarBean}. I cicli di
 *     riferimenti (produzioni ricorsive su se stesse o a vicenda) sono gestiti usando il peso
 *     originale, non ancora aumentato, di una produzione ogni volta che la si reincontra
 *     mentre il suo stesso aumento è ancora in calcolo. Due membri strutturalmente identici
 *     di un ciclo quindi non finiscono con lo stesso peso: quello <b>dichiarato per primo nel
 *     file</b> viene aumentato per primo, e finisce per essere il più pesante dei due (vedi
 *     {@link #adjustWeightsForDescendants}).</li>
 *     <li>Un riferimento di qualsiasi tipo ({@code [Nome]}, {@code [*Nome]}, {@code [!Nome]} o
 *     {@code [#chiave]}) preceduto da {@code ^} immediatamente alla sua sinistra, <b>fuori</b>
 *     dalle parentesi quadre (ad esempio {@code ^[Nome]}), ha la prima lettera del valore
 *     risolto in maiuscolo. A differenza dell'elemento di peso {@code [^N]}, che sta subito
 *     dentro le parentesi, all'inizio del testo di un'alternativa, ed è risolto al caricamento
 *     della grammatica, questo marcatore è risolto al momento della produzione, via via che
 *     ogni riferimento viene espanso, perciò funziona in modo uniforme su qualunque cosa
 *     produca quello specifico riferimento. Non esiste una sequenza di escape per un
 *     {@code ^} letterale immediatamente prima di un riferimento, come per gli altri
 *     marcatori del file.</li>
 * </ul>
 * Entrambi i file sorgente sono decodificati come <b>UTF-8</b>, in modo esplicito e non
 * tramite il charset predefinito della JVM, così le lettere accentate di una grammatica
 * sopravvivono qualunque sia la piattaforma su cui gira il gioco. Anche una {@code String}
 * passata ai costruttori testuali è codificata in UTF-8 prima di essere riletta.
 * <p>
 * Un secondo file facoltativo, di post-produzione, elenca sostituzioni letterali di testo
 * ({@code pre:post}) applicate al testo finale prodotto, per correggere i problemi di lingua
 * naturale che nascono dalla concatenazione meccanica (ad esempio, in italiano, {@code "a il"}
 * che diventa {@code "al"}). Il {@code post} di una regola non può contenere il proprio
 * {@code pre} alla lettera, dato che ogni regola viene applicata finché il suo {@code pre}
 * non compare più (vedi {@link #setPostProductionFile} e {@link #postProduce}).
 * <p>
 * Ci si aspetta che ogni produzione produca una stringa senza spazi ai bordi (vedi
 * {@link #resolvePlainProduction} e {@link #resolveFixedProduction}): gli spazi iniziali o
 * finali rimasti dopo che l'alternativa scelta di una produzione è stata espansa del tutto
 * vengono tolti prima che il risultato sia inserito in ciò che l'ha referenziata. Conta in
 * particolare per i gruppi di alternative in linea, dove la foglia vuota di un gruppo
 * annidato, scelta accanto a testo letterale, lascerebbe altrimenti uno spazio spurio.
 * <p>
 * Questa rimozione protegge solo i bordi dell'espansione di un singolo riferimento: un
 * riferimento a una produzione che risolve intenzionalmente in una stringa vuota (ad esempio
 * un'alternativa pensata per rappresentare "nessun testo in più qui") lascia comunque gli
 * spazi o i tab letterali che il testo del modello metteva ai due lati del riferimento,
 * producendo una sequenza di due o più caratteri di spaziatura o, se il riferimento sta
 * subito prima di un segno di punteggiatura a fine frase, un singolo spazio o tab spurio prima
 * di quella punteggiatura. Per rendere utilizzabili queste alternative volutamente vuote
 * senza obbligare ogni modello a trattarle come caso speciale, ogni sequenza di due o più
 * spazi e/o tab rimasta nel testo prodotto finale viene ridotta automaticamente a un solo
 * spazio, e ogni spazio o tab rimasto subito prima di un segno di punteggiatura viene tolto,
 * una volta sola, dopo che tutte le sostituzioni di post-produzione sono state applicate (vedi
 * {@link #postProduce}).
 * <p>
 * <b>Le produzioni monouso rendono con stato una sequenza di chiamate a {@code produce()},
 * e il chiamante decide dove finisce una sequenza e dove comincia la successiva chiamando
 * {@link #reset()}.</b> Non esiste un'unica politica giusta, perché le due cose che una
 * grammatica può volere sono opposte:
 * <ul>
 *     <li><b>Risultati indipendenti.</b> Ogni chiamata deve reggersi da sola e attingere
 *     all'insieme completo delle alternative: una fiaba per ogni visita alla locanda, per
 *     dire. Si chiama {@link #reset()} dopo ogni {@code produce()}. Senza di esso i serbatoi
 *     monouso continuano a svuotarsi di chiamata in chiamata, e i risultati diventano via via
 *     più poveri fino a fallire del tutto.</li>
 *     <li><b>Una serie di risultati volutamente distinti.</b> Generare, ad esempio, cinque
 *     personaggi diversi che non devono ripetersi è esattamente lo scopo di una produzione
 *     monouso: <b>non</b> si fa il reset fra l'uno e l'altro, o potrebbero ripetersi. Si fa il
 *     reset solo quando si inizia una nuova serie.</li>
 * </ul>
 * Il modo in cui il secondo caso fallisce merita di essere conosciuto, perché arriva tardi e
 * il suo messaggio nomina una produzione innocente: una volta consumata l'ultima alternativa
 * di una produzione monouso, la produzione viene rimossa e la cascata in
 * {@link #removeProduction} elimina ogni alternativa che la referenzia, rimuovendo
 * ricorsivamente tutto ciò che resta vuoto. Una chiamata successiva muore allora con
 * {@code IllegalArgumentException: Production X is empty!}, dove {@code X} può trovarsi
 * parecchi livelli sopra il serbatoio che si è davvero esaurito. La lunghezza di una serie è
 * quindi limitata dal più piccolo serbatoio monouso da cui attinge: vedi
 * {@link #ONE_SHOT_MARKER} e {@link #getProduction}.
 * <p>
 * <b>Questa classe non è thread-safe.</b> Un'istanza deve restare confinata a un singolo
 * thread. Il punto delicato è che {@link #produce()} sembra una lettura ma non lo è: consuma
 * le alternative monouso da {@link #currentProductionsMap} (a cascata attraverso
 * {@link #removeProduction}), riempie e poi svuota {@link #globalFixedProductions}, e
 * attinge dalla sorgente condivisa {@link Dado#sorgente()}. Due thread che chiamano
 * {@code produce()} sulla stessa istanza possono quindi perdersi a vicenda i valori fissati,
 * consumare due volte la stessa alternativa monouso, o lasciare la mappa delle produzioni
 * potata a metà. {@link #reset()}, {@link #addFixedProduction}, {@link #setRootNode} e
 * {@link #setProductionMode} modificano lo stesso stato e sono ugualmente non protetti.
 * <p>
 * Ciò che <i>è</i> sicuro è condividere il <i>sorgente</i> della grammatica: tutto ciò che
 * viene analizzato al caricamento ({@link #productionsMap}, compresi i pesi incorporati da
 * {@link #adjustWeightsForDescendants}, e {@link #postProductions}) non viene più scritto
 * dopo che il costruttore è ritornato. Quindi il modo di produrre testo da più di un thread
 * è dare a ciascun thread il proprio {@code GrammarBean} costruito dallo stesso file, non
 * sincronizzare l'accesso a una sola istanza condivisa.
 */
public class GrammarBean {

	/**
	 * Apre il testo di un file incluso con {@code #include <file>}. La grammatica conosce solo il nome scritto nella
	 * direttiva, relativo alla cartella del file che lo include; dove sia quella cartella, e come si legga un file, lo
	 * decide il risolutore (vedi {@link #fromResource}).
	 */
	@FunctionalInterface
	public interface IncludeResolver {
		/**
		 * @param path il percorso del file, relativo alla cartella della grammatica principale: usa {@code /} come separatore
		 *             e non inizia mai con esso, né contiene segmenti {@code .}, {@code ..} o vuoti
		 * @return il testo del file, o {@code null} se non esiste
		 * @throws IOException se il file esiste ma non si può aprire
		 */
		InputStream open(String path) throws IOException;

		/**
		 * Un nome che distingue questo file da ogni altro in tutto il programma, così che {@code #include_static} possa
		 * condividerne le produzioni fra le grammatiche che lo includono. Il valore predefinito è null: il risolutore non
		 * supporta le sezioni statiche. {@link #fromResource} dà l'URL della risorsa.
		 * @param path lo stesso percorso di {@link #open}
		 * @return l'identità del file, o null se il risolutore non ne può dare una
		 */
		default String identity(String path) {
			return null;
		}
	}

	private static final String PRODUCTION = "Production ";
	private static final String LINE = "Line ";
	/**
	 * Prefisso che segna un commento a riga intera nei file sorgente di grammatica e di post-produzione.
	 */
	private static final String COMMENT_PREFIX = "//";
	/**
	 * Con cosa inizia una riga di direttiva, nella prima colonna. Una riga che inizia così e non è una direttiva nota
	 * è un errore: un commento inizia con {@link #COMMENT_PREFIX}.
	 */
	private static final String DIRECTIVE_PREFIX = "#";
	/**
	 * Direttiva che include un altro file, in stile C: {@code #include <file>} su una riga a sé, a partire dalla
	 * prima colonna. Vedi {@link IncludeResolver}.
	 */
	private static final String INCLUDE_DIRECTIVE = "#include";
	/**
	 * Direttiva che include un file come <i>sezione statica</i>: {@code #include_static <file>}. Le sue produzioni sono
	 * condivise da ogni grammatica che la include (vedi {@link #resetStaticProductions()}).
	 */
	private static final String INCLUDE_STATIC_DIRECTIVE = "#include_static";
	/**
	 * Quanti file possono includersi a vicenda, uno dentro l'altro: ferma le inclusioni senza fine.
	 */
	private static final int MAX_INCLUDE_DEPTH = 10;
	/**
	 * Suffisso che segna una produzione come monouso (rimossa una volta usata nel corso di un ciclo). Un solo
	 * carattere anziché {@code {1}}, per evitare confusione visiva con i gruppi di alternative in linea
	 * ({@link #OPENING_BRACE}/{@link #CLOSING_BRACE}), che usano le stesse parentesi graffe.
	 */
	private static final String ONE_SHOT_MARKER = "$";
	/**
	 * Marcatore per le produzioni che derivano i loro valori possibili anche da altre produzioni.
	 */
	private static final String DERIVED_MARKER = "=";

	private static final String OPENING_BRACKET = "[";
	private static final String CLOSING_BRACKET = "]";
	/**
	 * Caratteri che delimitano un gruppo di alternative in linea in una foglia ({@code {opz1|opz2|...}}).
	 */
	private static final char OPENING_BRACE = '{';
	private static final char CLOSING_BRACE = '}';
	/**
	 * Carattere che delimita un tratto letterale grezzo in una foglia ({@code "..."}), dentro il quale
	 * {@link #OPENING_BRACE}, {@link #CLOSING_BRACE} e {@code |} sono trattati come testo semplice.
	 */
	private static final char QUOTE_CHAR = '"';
	/**
	 * Carattere che mette in escape un {@link #QUOTE_CHAR}, un {@link #OPENING_BRACKET} o un
	 * {@link #CLOSING_BRACKET} dentro un tratto letterale grezzo, così che sia trattato come testo semplice.
	 */
	private static final char ESCAPE_CHAR = '\\';
	/**
	 * Caratteri che fanno le veci di un {@code [} e un {@code ]} letterali, scritti {@code \[} e
	 * {@code \]} dentro un tratto letterale grezzo. Sono messi al loro posto da {@link #unescapeQuotes} al
	 * caricamento della grammatica e riconvertiti in vere parentesi da {@link #postProduce}, una volta che ogni
	 * elemento è stato espanso: è ciò che rende una parentesi in escape invisibile agli scanner degli elementi
	 * ({@link #checkTokensValidity}, {@link #collectReferencedProductions},
	 * {@link #produceImpl(String, Map, Map, String)}), che cercano tutti una {@code [} nuda.
	 * Si usano caratteri dell'area a uso privato, così che nessun testo plausibile di grammatica possa
	 * scontrarsi con essi; un file sorgente che ne contenga uno viene rifiutato senza appello (vedi
	 * {@link #readSourceFileAndCreateProductionsMap}). Devono restare sopra {@code U+0020},
	 * perché l'espansione di ogni produzione passa per {@link String#trim()}, che altrimenti
	 * toglierebbe un segnaposto posto a uno dei suoi bordi.
	 */
	private static final char LITERAL_OPENING_BRACKET_PLACEHOLDER = '\uE000';
	private static final char LITERAL_CLOSING_BRACKET_PLACEHOLDER = '\uE001';
	/**
	 * Marcatore che, trovato come ultimo carattere di una riga, significa che la riga non è
	 * ancora finita: viene tolto e al suo posto si accoda la riga successiva.
	 */
	private static final String LINE_CONTINUATION_MARKER = "\\";
	/**
	 * Prefisso usato per dare il nome alle produzioni generate automaticamente dai gruppi di alternative in linea.
	 */
	private static final String INLINE_PRODUCTION_PREFIX = "PROD_";
	/**
	 * Marcatore di una produzione il cui risultato è fissato globalmente, per l'intera chiamata a produce().
	 */
	private static final String GLOBAL_FIXED_PRODUCTION_MARKER = "*";
	/**
	 * Marcatore di una produzione il cui risultato è fissato localmente, all'interno dell'albero di produzione corrente.
	 */
	private static final String LOCAL_FIXED_PRODUCTION_MARKER = "!";
	/**
	 * Marcatore che assegna un valore letterale a una chiave nella cache globale delle produzioni
	 * fissate (chiave=valore).
	 */
	private static final String ASSIGNMENT_MARKER = "=";
	/**
	 * Marcatore che referenzia il valore di una chiave assegnata in precedenza nella cache globale
	 * delle produzioni fissate.
	 */
	private static final String REFERENCE_MARKER = "#";
	/**
	 * Marcatore che precede l'elemento di peso {@code [^N]} iniziale di un'alternativa (ad esempio
	 * {@code [^10] del testo} o {@code [^2.5] del testo}), usato per orientare la scelta casuale
	 * verso di essa; vedi {@link #parseWeight}. Risolto e tolto al caricamento della grammatica,
	 * a differenza di {@link #ASSIGNMENT_MARKER}/{@link #REFERENCE_MARKER}, che sono risolti
	 * in modo pigro al momento della produzione.
	 */
	private static final String WEIGHT_MARKER = "^";
	/**
	 * Marcatore che mette in maiuscolo la prima lettera del valore risolto di un riferimento quando si trova
	 * immediatamente prima della sua parentesi di apertura, fuori da essa (ad esempio {@code ^[Nome]}). Stesso
	 * carattere di {@link #WEIGHT_MARKER} ma in posizione speculare (prima della parentesi
	 * anziché subito dentro), così i due non occorrono mai nella stessa posizione di carattere
	 * e non si possono confondere. Risolto al momento della produzione, in
	 * {@link #produceImpl(String, Map, Map, String)}.
	 */
	private static final String CAPITALIZE_MARKER = "^";
	/**
	 * Marcatore che rende un riferimento tollerante a un valore mancante: scritto subito dopo il nome
	 * referenziato e seguito da {@code |} e dal valore di ripiego
	 * ({@code [Nome? | ripiego]}), oppure da solo per un ripiego vuoto ({@code [Nome?]}). Risolto
	 * al momento della produzione, in {@link #resolveToken}. Diverso per posizione sia da
	 * {@link #WEIGHT_MARKER} (che segue la parentesi di apertura, all'inizio di
	 * un'alternativa) sia da {@link #CAPITALIZE_MARKER} (che precede la parentesi di apertura), così i
	 * tre non occupano mai la stessa posizione di carattere. Un nome di produzione quindi non può
	 * terminare con questo carattere (vedi {@link #handleProduction}).
	 */
	private static final String DEFAULT_MARKER = "?";
	/**
	 * Fattore di scala applicato al peso aggregato di una produzione referenziata quando si aumenta il
	 * peso di un'alternativa che la referenzia; vedi {@link #adjustWeightsForDescendants}.
	 */
	private static final double SCALE_FACTOR = 0.33;
	/**
	 * Separatore fra il testo di pre-produzione e quello di post-produzione nel file di post-produzione.
	 */
	private static final String POST_PRODUCTION_SEPARATOR = ":";
	/**
	 * Espressione regolare che divide il testo prodotto in singole righe.
	 */
	private static final String LINE_BREAK_REGEX = "\\n";
	/**
	 * Espressione regolare che trova una sequenza di due o più spazi e/o tab, ridotta a un solo spazio da
	 * {@link #postProduce}.
	 */
	private static final String MULTIPLE_SPACES_REGEX = "[ \t]{2,}";
	/**
	 * Espressione regolare che trova uno o più spazi e/o tab immediatamente prima di un segno di punteggiatura
	 * di frase/proposizione, tolti da {@link #postProduce}.
	 */
	private static final String SPACE_BEFORE_PUNCTUATION_REGEX = "[ \t]+(?=[.,;:!?])";
	/**
	 * Produzione radice da cui parte il processo di generazione.
	 */
	private String rootNode = null;
	/**
	 * Mappa di tutte le produzioni disponibili. Mantiene l'ordine di inserimento, così che iterarla segua
	 * l'ordine in cui le produzioni sono state dichiarate nel file di grammatica (con ogni produzione
	 * generata automaticamente da un gruppo di alternative in linea collocata dove è stata creata). Tre
	 * cose dipendono da questo: quale membro di un ciclo di riferimenti ottiene per primo l'aumento del peso dei
	 * discendenti (vedi {@link #adjustWeightsForDescendants}), quale riferimento errato viene segnalato per primo
	 * quando una grammatica ne ha diversi (vedi {@link #checkProductionsValidity}), e
	 * l'ordine in cui la cascata di una produzione monouso esaurita elimina le altre produzioni
	 * (vedi {@link #removeProduction}). Una semplice {@link HashMap} lasciava tutte e tre all'ordine
	 * dell'hash: riproducibile per una data grammatica e JVM, ma impossibile da prevedere leggendo
	 * il file.
	 */
	private final Map<String, List<WeightedAlternative>> productionsMap = new LinkedHashMap<>();
	/**
	 * Mappa di tutte le produzioni disponibili da riusare in un ciclo di produzioni. Mantiene l'ordine di inserimento
	 * come {@link #productionsMap}, da cui {@link #reset()} la copia, così la cascata di rimozione monouso
	 * percorre le produzioni nell'ordine di dichiarazione.
	 */
	private final Map<String, List<WeightedAlternative>> currentProductionsMap = new CurrentProductions();
	/**
	 * Le sezioni statiche che questa grammatica include con {@code #include_static}, nell'ordine delle direttive.
	 */
	private final List<StaticSection> staticSections = new ArrayList<>();
	/**
	 * Vero quando questa istanza è quella che carica una sezione statica: un sorgente simile non può includere altre
	 * sezioni statiche.
	 */
	private boolean staticSource;
	/**
	 * Prefisso dei nomi dati alle produzioni delle alternative in linea: una sezione statica ha il proprio, così che
	 * i suoi nomi non incontrino mai quelli della grammatica che la include.
	 */
	private String inlinePrefix = INLINE_PRODUCTION_PREFIX;
	/**
	 * I nomi delle produzioni statiche già esaurite, le cui alternative sono state potate da
	 * {@link #currentProductionsMap}.
	 */
	private final Set<String> prunedStaticNames = new HashSet<>();
	/**
	 * Quanto valevano {@link #staticEpoch} e {@link #staticVersion} l'ultima volta che questa grammatica ha guardato le sezioni statiche.
	 */
	private long seenStaticEpoch;
	private long seenStaticVersion;
	/**
	 * Mappa delle produzioni fissate
	 */
	private final Map<String, String> globalFixedProductions = new HashMap<>();
	/**
	 * Mappa di tutte le produzioni che avvengono dopo che la produzione principale è terminata, per aggiustare il risultato correggendo i problemi di grammatica della lingua naturale
	 * (ad esempio, in italiano, 'a il' viene trasformato in 'al').
	 * <p>
	 * Mantiene l'ordine di inserimento, così {@link #postProduce} applica le regole nell'ordine in cui
	 * sono scritte nel file di post-produzione. Conta ogni volta che due regole possono corrispondere allo stesso
	 * tratto di testo (un {@code pre} più corto che è prefisso di uno più lungo, per dire), perché
	 * quella eseguita per prima lo consuma. Con una semplice {@link HashMap} quell'esito era deciso
	 * dall'ordine dell'hash, così un file non poteva esprimere una propria precedenza.
	 */
	private final Map<String, String> postProductions = new LinkedHashMap<>();
	/**
	 * Per scegliere a caso una produzione: l'unica sorgente casuale del gioco, {@link Dado#sorgente()},
	 * così che un seme impostato con {@link Dado#impostaSeme} renda ripetibili anche i testi prodotti, e
	 * gli artefatti generati da una grammatica. Attingere direttamente da essa non consuma i
	 * tiri truccati con {@link Dado#trucca}, che restano riservati alle decisioni di gioco.
	 * (Una volta era un {@link Random} privato; uno inizializzato con {@link System#currentTimeMillis()}
	 * farebbe attingere la stessa sequenza a due bean costruiti nello stesso millisecondo, cosa che una
	 * sorgente condivisa non può fare.)
	 */
	private final Random rnd = Dado.sorgente();
	/**
	 * Controlla come viene scelta l'alternativa di una produzione; vedi {@link TipoModalitaProduzioneTesti}.
	 * Il valore predefinito è {@link TipoModalitaProduzioneTesti#CASUALE}. Impostarlo a {@link TipoModalitaProduzioneTesti#PRIMO}
	 * o {@link TipoModalitaProduzioneTesti#ULTIMO} rende {@link #produce()}/{@link #produce(String)} del tutto
	 * deterministici, il che serve soprattutto nei test.
	 */
	private TipoModalitaProduzioneTesti productionMode = TipoModalitaProduzioneTesti.CASUALE;
	/**
	 * Produzioni che devono essere rimosse una volta usate nel corso di un ciclo
	 */
	private final Set<String> oneShotProductions = new HashSet<>();
	/**
	 * Contatore progressivo usato per dare il nome alle produzioni generate automaticamente dai gruppi di
	 * alternative in linea ({@link #INLINE_PRODUCTION_PREFIX} + questo contatore).
	 */
	private int inlineProductionCounter = 0;

	private final Map<String, Collection<String>> derivedProductions = new HashMap<>();

	/**
	 * Un frame per ogni chiamata ricorsiva attiva (non ancora ritornata) a
	 * {@link #produceImpl(String, Map, Map, String)}: {@code label} nomina ciò che viene
	 * espanso (il nome di una produzione, o l'elemento iniziale {@code [rootNode]}) e
	 * {@code steps} contiene i valori successivi della variabile {@code production} propria
	 * di quella chiamata, uno per ogni parentesi risolta. Usato da {@link #describeProductionTrace()} per
	 * ricostruire il percorso seguito dal motore della grammatica, nel caso venga lanciata un'eccezione
	 * durante la produzione; vedi {@link #produce()}.
	 */
	private static final class TraceFrame {
		private final String label;
		private final List<String> steps = new ArrayList<>();

		private TraceFrame(String label, String initialText) {
			this.label = label;
			steps.add(initialText);
		}
	}

	/**
	 * Il frame più interno per ultimo, così che iterare in ordine di inserimento (prima l'esterno)
	 * dia il percorso dalla radice dell'espansione fino al punto in cui si trova ora
	 * (o si trovava, quando un'eccezione l'ha interrotta). Non thread-safe, come il resto di
	 * questa classe.
	 */
	private final Deque<TraceFrame> productionTrace = new ArrayDeque<>();

	/**
	 * Il testo di un'alternativa insieme al suo peso di selezione (vedi {@link #WEIGHT_MARKER},
	 * {@link #parseWeight}). Immutabile; le istanze sono condivise fra {@link #productionsMap}
	 * e le copie di {@link #currentProductionsMap} fatte da {@link #reset()}.
	 */
	static final class WeightedAlternative {
		private final String text;
		private final double weight;

		private WeightedAlternative(String text, double weight) {
			this.text = text;
			this.weight = weight;
		}

		String getText() {
			return text;
		}
	}

	/**
	 * Da dove si leggono i file delle direttive {@code #include}; null se la grammatica è stata costruita senza.
	 */
	private IncludeResolver includeResolver;
	/**
	 * Per ogni riga del sorgente dopo le inclusioni, il file e la riga da cui proviene ("nomi.txt:12"); null se
	 * il sorgente non ha {@code #include}, così che i messaggi continuino a dire solo "Line N".
	 */
	private List<String> sourceOrigins;

	/**
	 * Costruisce una grammatica da una risorsa, risolvendo allo stesso modo le sue direttive {@code #include}: un nome scritto in una
	 * direttiva è un file nella cartella della risorsa che lo contiene, o in una sua sottocartella.
	 * @param anchor classe usata per trovare le risorse, come per {@link Class#getResourceAsStream}
	 * @param grammarResource la risorsa con la grammatica, ad esempio {@code "/com/mio/pacchetto/locande.txt"}
	 * @param postProductionResource la risorsa con le sostituzioni di post-produzione, o null
	 * @throws InvalidGrammarException se una risorsa manca o la grammatica non è valida
	 * @throws IOException se una risorsa non si può leggere
	 */
	public static GrammarBean fromResource(Class<?> anchor, String grammarResource, String postProductionResource)
			throws InvalidGrammarException, IOException {
		String folder = grammarResource.substring(0, grammarResource.lastIndexOf('/') + 1);
		try (InputStream grammar = anchor.getResourceAsStream(grammarResource);
			 InputStream postProduction = postProductionResource == null ? null : anchor.getResourceAsStream(postProductionResource)) {
			if (grammar == null) {
				throw new InvalidGrammarException("Grammar resource not found: " + grammarResource);
			}
			if (postProductionResource != null && postProduction == null) {
				throw new InvalidGrammarException("Post-production resource not found: " + postProductionResource);
			}
			return new GrammarBean(grammar, postProduction, new ResourceIncludeResolver(anchor, folder));
		}
	}

	/**
	 * Legge i file inclusi dal class path, nella cartella della risorsa della grammatica principale.
	 */
	private static final class ResourceIncludeResolver implements IncludeResolver {
		private final Class<?> anchor;
		private final String folder;

		private ResourceIncludeResolver(Class<?> anchor, String folder) {
			this.anchor = anchor;
			this.folder = folder;
		}

		@Override
		public InputStream open(String path) {
			return anchor.getResourceAsStream(folder + path);
		}

		@Override
		public String identity(String path) {
			java.net.URL url = anchor.getResource(folder + path);
			return url == null ? null : url.toString();
		}
	}

	/**
	 * Costruisce una grammatica da una stringa sorgente, senza sostituzioni di post-produzione.
	 * @param grammar testo sorgente della grammatica, per la sintassi vedi la documentazione della classe
	 * @throws InvalidGrammarException se il testo non descrive una grammatica valida
	 * @throws IOException se il testo non si può leggere
	 */
	public GrammarBean(String grammar) throws InvalidGrammarException, IOException {
		if (grammar == null) {
			throw new InvalidGrammarException("Grammar text cannot be null");
		}
		init(grammar, null);
	}

	/**
	 * Costruisce una grammatica da una stringa sorgente e da una stringa facoltativa di post-produzione.
	 * @param grammar testo sorgente della grammatica, per la sintassi vedi la documentazione della classe
	 * @param postProduction il testo facoltativo (può essere {@code null}) di post-produzione, che elenca
	 *                       sostituzioni letterali {@code pre:post} applicate al testo prodotto
	 * @throws InvalidGrammarException se uno dei due testi non descrive una grammatica valida
	 * @throws IOException se uno dei due testi non si può leggere
	 */
	public GrammarBean(String grammar, String postProduction) throws InvalidGrammarException, IOException {
		if (grammar == null) {
			throw new InvalidGrammarException("Grammar text cannot be null");
		}
		init(grammar, postProduction == null ? null
				: new ByteArrayInputStream(postProduction.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Costruisce una grammatica da un file sorgente e da un file facoltativo di post-produzione.
	 * @param grammar file sorgente della grammatica, per la sintassi vedi la documentazione della classe
	 * @param postProduction il file facoltativo (può essere {@code null}) di post-produzione, che elenca
	 *                        sostituzioni letterali {@code pre:post} applicate al testo prodotto
	 * @throws InvalidGrammarException se uno dei due file non descrive una grammatica valida
	 * @throws IOException se uno dei due file non si può leggere
	 */
	public GrammarBean(InputStream grammar, InputStream postProduction) throws InvalidGrammarException, IOException {
		if (grammar == null) {
			throw new InvalidGrammarException("Grammar source file cannot be null");
		}
		init(grammar, postProduction);
	}

	private void init(String grammar, InputStream postProduction) throws InvalidGrammarException, IOException {
		init(new ByteArrayInputStream(grammar.getBytes(StandardCharsets.UTF_8)), postProduction);
	}

	/**
	 * Come {@link #GrammarBean(InputStream, InputStream)}, ma la grammatica può includere altri file con
	 * {@code #include <file>}: li apre il risolutore (vedi {@link IncludeResolver}).
	 * @param includeResolver da dove si leggono i file inclusi; null se la grammatica non ha {@code #include}
	 */
	public GrammarBean(InputStream grammar, InputStream postProduction, IncludeResolver includeResolver)
			throws InvalidGrammarException, IOException {
		if (grammar == null) {
			throw new InvalidGrammarException("Grammar source file cannot be null");
		}
		this.includeResolver = includeResolver;
		init(grammar, postProduction);
	}

	/**
	 * Costruisce la grammatica di una sezione statica (vedi {@link StaticSection}).
	 */
	private GrammarBean(InputStream grammar, IncludeResolver includeResolver, String inlinePrefix)
			throws InvalidGrammarException, IOException {
		this.staticSource = true;
		this.includeResolver = includeResolver;
		this.inlinePrefix = inlinePrefix;
		init(grammar, null);
	}

	private void init(InputStream grammar, InputStream postProduction) throws InvalidGrammarException, IOException {
		setSourceFile(grammar);
		setPostProductionFile(postProduction);
		reset();
	}

	/**
	 * Legge e analizza il file di grammatica.
	 */
	private void setSourceFile(InputStream inputStream) throws InvalidGrammarException, IOException {
		readSourceFileAndCreateProductionsMap(inputStream);
		checkStaticNameCollisions();
		checkProductionsValidity();
		adjustWeightsForDescendants();
	}

	/**
	 * Il sorgente della grammatica con ogni {@code #include <file>} sostituito, su una riga a sé, dalle righe di quel
	 * file (che può includerne altri, fino a {@link #MAX_INCLUDE_DEPTH} livelli), e ogni {@code #include_static <file>}
	 * rimosso dopo aver registrato la sua sezione statica (vedi {@link StaticSection}). Un file viene incluso una sola volta, ovunque
	 * lo si richieda di nuovo, così due file possono includere lo stesso terzo file e un file non può includere se stesso. Un sorgente
	 * senza direttive viene restituito com'è, e {@link #sourceOrigins} resta null.
	 */
	private InputStream withIncludes(InputStream inputStream) throws IOException, InvalidGrammarException {
		List<String> lines = readLines(inputStream);
		boolean needsExpansion = false;
		for (String line : lines) {
			if (line.startsWith(DIRECTIVE_PREFIX)) {
				needsExpansion = true;
				break;
			}
		}
		sourceOrigins = null;
		List<String> expanded = lines;
		if (needsExpansion) {
			expanded = new ArrayList<>();
			List<String> origins = new ArrayList<>();
			expandIncludes(lines, "", "source", 0, new HashSet<>(), expanded, origins);
			sourceOrigins = origins;
		}
		StringBuilder text = new StringBuilder();
		for (String line : expanded) {
			text.append(line).append('\n');
		}
		return new ByteArrayInputStream(text.toString().getBytes(StandardCharsets.UTF_8));
	}

	private static List<String> readLines(InputStream inputStream) throws IOException {
		List<String> lines = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		return lines;
	}

	/**
	 * Dice se la riga è un commento a riga intera: inizia con {@link #COMMENT_PREFIX}.
	 */
	private static boolean isComment(String line) {
		return line.startsWith(COMMENT_PREFIX);
	}

	/**
	 * La direttiva che una riga rappresenta, {@code "include"} o {@code "include_static"}, o null. Il nome deve essere l'intera parola
	 * dopo il {@code #}: {@code #includes} o {@code # include} non sono direttive.
	 */
	private static String directive(String line) {
		if (!line.startsWith(DIRECTIVE_PREFIX)) {
			return null;
		}
		int end = 1;
		while (end < line.length() && (Character.isLetter(line.charAt(end)) || line.charAt(end) == '_')) {
			end++;
		}
		String name = line.substring(1, end);
		return name.equals("include") || name.equals("include_static") ? name : null;
	}

	/**
	 * Copia le righe in {@code output}, sostituendo le direttive di inclusione con le righe dei file che nominano.
	 * @param folder la cartella di questo file, relativa alla grammatica principale ("" o che termina con "/")
	 * @param name come si chiama questo file nei messaggi
	 */
	private void expandIncludes(List<String> lines, String folder, String name, int depth, Set<String> alreadyIncluded,
								List<String> output, List<String> origins) throws IOException, InvalidGrammarException {
		boolean continued = false;
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			String where = name + ":" + (i + 1);
			// Una riga che continua la precedente (vedi LINE_CONTINUATION_MARKER) non è mai una direttiva
			String directive = continued ? null : directive(line);
			if (directive != null) {
				String path = includePath(line, directive, where);
				if (directive.equals("include")) {
					includeFile(path, folder, where, depth, alreadyIncluded, output, origins);
				} else {
					includeStatic(path, folder, where);
				}
			} else {
				if (!continued && line.startsWith(DIRECTIVE_PREFIX)) {
					throw new InvalidGrammarException(where + ": Unknown directive: comments start with " + COMMENT_PREFIX
							+ ", and a line starting with " + DIRECTIVE_PREFIX + " must be " + INCLUDE_DIRECTIVE
							+ " or " + INCLUDE_STATIC_DIRECTIVE);
				}
				output.add(line);
				origins.add(where);
			}
			if (continued) {
				continued = line.endsWith(LINE_CONTINUATION_MARKER);
			} else {
				continued = !line.isEmpty() && !isComment(line) && line.endsWith(LINE_CONTINUATION_MARKER);
			}
		}
		if (continued) {
			throw new InvalidGrammarException(name + ": Line continuation marker '" + LINE_CONTINUATION_MARKER
					+ "' at end of file");
		}
	}

	/**
	 * Il nome fra {@code <} e {@code >} in una direttiva di inclusione.
	 */
	private static String includePath(String line, String directive, String where) throws InvalidGrammarException {
		String shown = DIRECTIVE_PREFIX + directive;
		String rest = line.substring(shown.length()).trim();
		if (!rest.startsWith("<") || !rest.endsWith(">") || rest.length() < 3) {
			throw new InvalidGrammarException(where + ": Malformed " + shown + ", expected " + shown + " <file>");
		}
		String path = rest.substring(1, rest.length() - 1).trim();
		if (path.isEmpty() || path.startsWith("/") || path.contains("\\")) {
			throw new InvalidGrammarException(where + ": Invalid file name in " + shown + ": '" + path
					+ "' (it must be relative, with / as separator)");
		}
		for (String segment : path.split("/", -1)) {
			if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
				throw new InvalidGrammarException(where + ": Invalid file name in " + shown + ": '" + path
						+ "' (only the folder of the file that includes it, or its subfolders)");
			}
		}
		return path;
	}

	private void includeFile(String path, String folder, String where, int depth, Set<String> alreadyIncluded,
							 List<String> output, List<String> origins) throws IOException, InvalidGrammarException {
		if (includeResolver == null) {
			throw new InvalidGrammarException(where + ": " + INCLUDE_DIRECTIVE
					+ " cannot be resolved: the grammar was built without an IncludeResolver (see GrammarBean.fromResource)");
		}
		String fullPath = folder + path;
		if (!alreadyIncluded.add(fullPath)) {
			return;
		}
		if (depth >= MAX_INCLUDE_DEPTH) {
			throw new InvalidGrammarException(where + ": Too many nested " + INCLUDE_DIRECTIVE + " (more than "
					+ MAX_INCLUDE_DEPTH + ")");
		}
		List<String> includedLines;
		try (InputStream included = includeResolver.open(fullPath)) {
			if (included == null) {
				throw new InvalidGrammarException(where + ": Included file not found: " + fullPath);
			}
			includedLines = readLines(included);
		}
		if (!includedLines.isEmpty() && includedLines.get(0).startsWith("\uFEFF")) {
			includedLines.set(0, includedLines.get(0).substring(1));
		}
		expandIncludes(includedLines, fullPath.substring(0, fullPath.lastIndexOf('/') + 1), fullPath, depth + 1,
				alreadyIncluded, output, origins);
	}

	/**
	 * Registra la sezione statica di {@code #include_static <path>}, caricandola la prima volta che qualcuno la chiede.
	 * Non aggiunge nulla al testo di questa grammatica: le sue produzioni vengono cercate nella sezione condivisa.
	 */
	private void includeStatic(String path, String folder, String where) throws IOException, InvalidGrammarException {
		if (staticSource) {
			throw new InvalidGrammarException(where + ": " + INCLUDE_STATIC_DIRECTIVE
					+ " cannot be used inside a static section");
		}
		if (includeResolver == null) {
			throw new InvalidGrammarException(where + ": " + INCLUDE_STATIC_DIRECTIVE
					+ " cannot be resolved: the grammar was built without an IncludeResolver (see GrammarBean.fromResource)");
		}
		String fullPath = folder + path;
		String key = includeResolver.identity(fullPath);
		if (key == null) {
			try (InputStream probe = includeResolver.open(fullPath)) {
				if (probe == null) {
					throw new InvalidGrammarException(where + ": Included file not found: " + fullPath);
				}
			}
			throw new InvalidGrammarException(where + ": " + INCLUDE_STATIC_DIRECTIVE
					+ " needs a resolver that identifies its files (see GrammarBean.fromResource)");
		}
		StaticSection section = loadStaticSection(key, fullPath, where);
		if (staticSections.contains(section)) {
			return;
		}
		for (String production : section.productions.keySet()) {
			for (StaticSection other : staticSections) {
				if (other.productions.containsKey(production)) {
					throw new InvalidGrammarException(where + ": Production " + production + " is defined by two static sections ("
							+ other.key + " and " + section.key + ")");
				}
			}
		}
		staticSections.add(section);
	}

	// ---------------------------------------------------------------------------------------------------------
	// Sezioni statiche: #include_static <file>
	// ---------------------------------------------------------------------------------------------------------

	/**
	 * Protegge tutto ciò che è condiviso fra le grammatiche: {@link #STATIC_SECTIONS}, le produzioni consumabili di ogni
	 * {@link StaticSection}, {@link #staticEpoch} e {@link #staticVersion}. Un unico lock, così che due thread che
	 * producono da due grammatiche diverse non prendano mai due lock in ordine diverso.
	 */
	private static final Object STATIC_LOCK = new Object();
	/**
	 * Prefisso dei nomi delle produzioni in linea di una sezione statica, seguito dal suo numero: {@code PROD_S1_0}.
	 */
	private static final String STATIC_INLINE_PREFIX = "PROD_S";
	/**
	 * Le sezioni statiche caricate, per identità del file (vedi {@link IncludeResolver#identity}).
	 */
	private static final Map<String, StaticSection> STATIC_SECTIONS = new HashMap<>();
	/**
	 * Cambia con {@link #resetStaticProductions()}: una grammatica che ne vede uno nuovo ricostruisce le proprie produzioni.
	 */
	private static long staticEpoch = 0;
	/**
	 * Cambia ogni volta che una produzione statica si esaurisce (o a un reset): una grammatica che ne vede uno nuovo pota ciò che
	 * dipendeva da essa.
	 */
	private static long staticVersion = 0;

	/**
	 * Un file incluso con {@code #include_static}: analizzato una sola volta, per tutto il programma, come grammatica a sé (così non
	 * può riferirsi alle produzioni della grammatica che lo include, che invece può riferirsi alle sue). Le sue
	 * produzioni sono <b>condivise</b>: una produzione monouso consumata tramite una grammatica è consumata per ogni altra
	 * grammatica che include lo stesso file, finché non si chiama {@link GrammarBean#resetStaticProductions()}.
	 * <p>
	 * Non c'è alcun rifornimento automatico: una produzione esaurita resta esaurita, e le alternative che dipendevano da essa
	 * sono rimosse, esattamente come per una normale produzione monouso (vedi {@link #removeProduction}).
	 */
	private static final class StaticSection {
		final String key;
		/**
		 * Le produzioni come caricate, pesi compresi: mai modificate dopo.
		 */
		final Map<String, List<WeightedAlternative>> productions;
		final Set<String> oneShot;
		/**
		 * Ciò che si può ancora produrre: la copia condivisa e consumabile di {@link #productions}.
		 */
		final Map<String, List<WeightedAlternative>> current = new LinkedHashMap<>();

		StaticSection(String key, Map<String, List<WeightedAlternative>> productions, Set<String> oneShot) {
			this.key = key;
			this.productions = productions;
			this.oneShot = oneShot;
			restore();
		}

		void restore() {
			current.clear();
			for (Map.Entry<String, List<WeightedAlternative>> entry : productions.entrySet()) {
				current.put(entry.getKey(), new ArrayList<>(entry.getValue()));
			}
		}
	}

	/**
	 * Le produzioni che una grammatica può produrre ora: le proprie, consumate man mano che la grammatica produce, e, per un nome che non è
	 * suo, le produzioni condivise delle sue sezioni statiche.
	 */
	private final class CurrentProductions extends LinkedHashMap<String, List<WeightedAlternative>> {
		private static final long serialVersionUID = 1L;

		@Override
		public List<WeightedAlternative> get(Object key) {
			List<WeightedAlternative> own = super.get(key);
			if (own != null || staticSections.isEmpty() || !(key instanceof String)) {
				return own;
			}
			synchronized (STATIC_LOCK) {
				for (StaticSection section : staticSections) {
					List<WeightedAlternative> shared = section.current.get(key);
					if (shared != null) {
						return shared;
					}
				}
			}
			return null;
		}

		@Override
		public boolean containsKey(Object key) {
			return get(key) != null;
		}
	}

	/**
	 * Ripristina le produzioni condivise di ogni sezione statica caricata finora: ciò che era stato consumato tramite una qualsiasi grammatica
	 * torna disponibile. Ogni grammatica che include una sezione statica ricostruisce inoltre le proprie produzioni la volta
	 * successiva che viene usata (comprese quelle monouso proprie), così che nulla resti potato per un nome che è tornato. Si chiama
	 * all'inizio di una partita (vedi {@code ProduttoreDiTestiCasuale.resetProduzioni}).
	 */
	public static void resetStaticProductions() {
		synchronized (STATIC_LOCK) {
			for (StaticSection section : STATIC_SECTIONS.values()) {
				section.restore();
			}
			staticEpoch++;
			staticVersion++;
		}
	}

	/**
	 * La sezione statica di quel file, analizzata la prima volta e condivisa poi.
	 */
	private StaticSection loadStaticSection(String key, String fullPath, String where) throws IOException, InvalidGrammarException {
		synchronized (STATIC_LOCK) {
			StaticSection loaded = STATIC_SECTIONS.get(key);
			if (loaded != null) {
				return loaded;
			}
			String folder = fullPath.substring(0, fullPath.lastIndexOf('/') + 1);
			IncludeResolver resolver = includeResolver;
			GrammarBean sectionGrammar;
			try (InputStream stream = includeResolver.open(fullPath)) {
				if (stream == null) {
					throw new InvalidGrammarException(where + ": Included file not found: " + fullPath);
				}
				sectionGrammar = new GrammarBean(stream, path -> resolver.open(folder + path),
						STATIC_INLINE_PREFIX + (STATIC_SECTIONS.size() + 1) + "_");
			} catch (InvalidGrammarException e) {
				if (e.getMessage().startsWith(where)) {
					throw e;
				}
				throw new InvalidGrammarException(where + ": In the static section " + fullPath + ": " + e.getMessage());
			}
			StaticSection section = new StaticSection(key, sectionGrammar.productionsMap, sectionGrammar.oneShotProductions);
			STATIC_SECTIONS.put(key, section);
			return section;
		}
	}

	/**
	 * Una grammatica non può definire una produzione che una delle sue sezioni statiche definisce già.
	 */
	private void checkStaticNameCollisions() throws InvalidGrammarException {
		if (staticSections.isEmpty()) {
			return;
		}
		for (String name : productionsMap.keySet()) {
			for (StaticSection section : staticSections) {
				if (section.productions.containsKey(name)) {
					throw new InvalidGrammarException(PRODUCTION + name + " is repeated: it is also defined by the static section "
							+ section.key);
				}
			}
		}
	}

	private boolean isStaticName(String name) {
		return staticSectionOf(name) != null;
	}

	private StaticSection staticSectionOf(String name) {
		for (StaticSection section : staticSections) {
			if (section.productions.containsKey(name)) {
				return section;
			}
		}
		return null;
	}

	/**
	 * Dice se esiste una produzione con quel nome, in questa grammatica o in una delle sue sezioni statiche.
	 */
	private boolean isDefined(String name) {
		return productionsMap.containsKey(name) || isStaticName(name);
	}

	/**
	 * Sceglie un'alternativa di una produzione statica, consumandola se la produzione è monouso. Quando la produzione è
	 * esaurita viene rimossa dalle produzioni condivise insieme ai suoi dipendenti, e questa grammatica pota le proprie
	 * alternative che la usavano. Va chiamato tenendo {@link #STATIC_LOCK}.
	 */
	private String getStaticProduction(String production) {
		StaticSection section = staticSectionOf(production);
		List<WeightedAlternative> alternatives = section.current.get(production);
		if (alternatives == null || alternatives.isEmpty()) {
			throw new IllegalArgumentException(PRODUCTION + production + " is empty!");
		}
		WeightedAlternative chosen = pick(alternatives);
		if (section.oneShot.contains(production)) {
			alternatives.remove(chosen);
			if (alternatives.isEmpty()) {
				removeProduction(section.current, production);
				staticVersion++;
				syncStatic();
				seenStaticVersion = staticVersion;
			}
		}
		return chosen.text;
	}

	/**
	 * Toglie dalle produzioni proprie di questa grammatica le alternative che richiedono una produzione statica non più
	 * disponibile, insieme a ciò che dipendeva da esse (vedi {@link #removeProduction}). Va chiamato tenendo
	 * {@link #STATIC_LOCK}.
	 */
	private void syncStatic() {
		for (StaticSection section : staticSections) {
			for (String name : section.productions.keySet()) {
				if (!section.current.containsKey(name) && prunedStaticNames.add(name)) {
					removeProduction(currentProductionsMap, name);
				}
			}
		}
	}

	/**
	 * Aggiorna questa grammatica rispetto alle sezioni statiche, prima di produrre: se sono state resettate ricostruisce le proprie
	 * produzioni, e se qualcosa è stato esaurito da un'altra grammatica pota ciò che ne dipendeva.
	 */
	private void refreshStatic() {
		if (staticSections.isEmpty()) {
			return;
		}
		synchronized (STATIC_LOCK) {
			if (seenStaticEpoch != staticEpoch) {
				rebuildCurrentProductions();
			} else if (seenStaticVersion != staticVersion) {
				syncStatic();
				seenStaticVersion = staticVersion;
			}
		}
	}

	/**
	 * Dove si trova una riga del sorgente, per i messaggi di errore: "Line N", più il file e la riga da cui proviene quando il
	 * sorgente include altri file ("Line 120 (nomi.txt:12)").
	 */
	private String position(int lineNumber) {
		if (sourceOrigins == null || lineNumber < 1 || lineNumber > sourceOrigins.size()) {
			return LINE + lineNumber;
		}
		return LINE + lineNumber + " (" + sourceOrigins.get(lineNumber - 1) + ")";
	}

	/**
	 * Legge il file di grammatica riga per riga, popolando {@link #productionsMap}: ogni riga
	 * non rientrata inizia una nuova produzione (vedi {@link #handleProduction}), ogni riga
	 * rientrata con TAB/spazio aggiunge figli alla produzione corrente (vedi
	 * {@link #handleChildren}). I commenti e le righe vuote vengono saltati. Una riga che termina con
	 * {@link #LINE_CONTINUATION_MARKER} non è ancora finita: il marcatore viene tolto e
	 * la riga seguente viene accodata al suo posto, ripetendo finché non si trova una riga che non
	 * termina con il marcatore; raggiungere la fine del file mentre si attende ancora
	 * una continuazione è un errore.
	 * @throws InvalidGrammarException se il file termina con un marcatore di continuazione di riga pendente
	 */
	private void readSourceFileAndCreateProductionsMap(InputStream inputStream) throws IOException, InvalidGrammarException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(withIncludes(inputStream), StandardCharsets.UTF_8))) {
			String currentProduction = null;
			int currentLineNumber = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				currentLineNumber++;

				if (line.isEmpty() || isComment(line)) {
					continue;
				}

				while (line.endsWith(LINE_CONTINUATION_MARKER)) {
					String nextLine = reader.readLine();
					if (nextLine == null) {
						throw new InvalidGrammarException(position(currentLineNumber) + ": Line continuation marker '" + LINE_CONTINUATION_MARKER + "' at end of file");
					}
					currentLineNumber++;
					line = line.substring(0, line.length() - LINE_CONTINUATION_MARKER.length()) + nextLine;
				}

				if (line.indexOf(LITERAL_OPENING_BRACKET_PLACEHOLDER) >= 0
						|| line.indexOf(LITERAL_CLOSING_BRACKET_PLACEHOLDER) >= 0) {
					throw new InvalidGrammarException(position(currentLineNumber)
							+ ": Reserved placeholder character found; use '" + ESCAPE_CHAR + OPENING_BRACKET
							+ "' inside a quoted span for a literal bracket");
				}

				boolean isParent = !line.startsWith("\t") && !line.startsWith(" ");

				if (isParent) {
					validatePreviousProduction(currentLineNumber, currentProduction);
					currentProduction = handleProduction(currentLineNumber, line);
				} else {
					if (currentProduction == null) {
						throw new InvalidGrammarException(LINE + line + ": Missing parent production. Lines beginning with TAB or space must be preceded by a production.");
					}
					handleChildren(line, currentLineNumber, currentProduction);
				}
			}
			validatePreviousProduction(currentLineNumber, currentProduction);
		}

		for (Map.Entry<String, Collection<String>> entry : derivedProductions.entrySet()) {
			String derivedProduction = entry.getKey();
			Collection<String> values = entry.getValue();
			for (String value : values) {
                List<WeightedAlternative> valueProductions = productionsMap.get(value);
				if (valueProductions == null) {
					throw new InvalidGrammarException("Production " + derivedProduction + " refers to an undeclared production: " + value);
				}
				productionsMap.computeIfAbsent(derivedProduction, k -> new ArrayList<>()).addAll(valueProductions);
			}
		}
	}

	/**
	 * Verifica che la produzione appena chiusa dall'arrivo di una nuova intestazione di produzione
	 * (o dalla fine del file) abbia almeno un figlio, dato che una produzione che non produce
	 * nulla farebbe fallire la generazione più avanti. Viene chiamato con la produzione che era
	 * ancora in corso di riempimento, così che ogni produzione del file sia controllata esattamente
	 * una volta: quando si legge l'intestazione che la chiude oppure, per l'ultima, quando si
	 * raggiunge la fine del file.
	 * @throws InvalidGrammarException se {@code previousProduction} non ha alcun figlio
	 */
	private void validatePreviousProduction(int currentLineNumber, String previousProduction) throws InvalidGrammarException {
		if (previousProduction != null) {
			List<WeightedAlternative> previousProductionChildren = productionsMap.get(previousProduction);
			if (previousProductionChildren.isEmpty() && derivedProductions.get(previousProduction) == null) {
				throw new InvalidGrammarException(position(currentLineNumber) + ": Production " + previousProduction + " has no children.");
			}
		}
	}

	/**
	 * Registra una nuova riga di intestazione di produzione: toglie il suffisso {@link #ONE_SHOT_MARKER}
	 * se presente (segnando la produzione come monouso), la imposta come
	 * {@link #rootNode} se è la prima produzione trovata, e la aggiunge (inizialmente
	 * senza figli) a {@link #productionsMap}. Se la produzione deriva da altre,
	 * controlla che non ci siano derivazioni da sé, reciproche o ripetute.
	 *
	 * @return il nome semplice della produzione (senza il marcatore monouso)
	 * @throws InvalidGrammarException se una produzione con lo stesso nome è già stata dichiarata
	 */
	private String handleProduction(int currentLineNumber, String line) throws InvalidGrammarException {
		String currentProduction = line.trim();
		if (currentProduction.contains(DERIVED_MARKER)) {
			String derivingProductions = currentProduction.substring(currentProduction.indexOf(DERIVED_MARKER) + DERIVED_MARKER.length());
			// Ripulisce le produzioni da cui si deriva
			derivingProductions = derivingProductions.replaceAll("\\s+", " ").trim();

			currentProduction = currentProduction.substring(0, currentProduction.indexOf(DERIVED_MARKER)).trim();

			if (!derivingProductions.isEmpty()) {
				Collection<String> associatedProducers = derivedProductions.computeIfAbsent(currentProduction, k -> new HashSet<>());
				for (String derivingProduction : derivingProductions.split(" ")) {
					if (derivingProduction.equals(currentProduction)) {
						throw new InvalidGrammarException(position(currentLineNumber) + ": Production " + currentProduction + " derives from itself");
					}
					if (associatedProducers.contains(derivingProduction)) {
						throw new InvalidGrammarException(position(currentLineNumber) + ": Production " + derivingProduction + " is repeated more than once");
					}
					if (derivedProductions.containsKey(derivingProduction)) {
						throw new InvalidGrammarException(position(currentLineNumber) + ": Production " + derivingProduction + " and Production " + currentProduction + " are mutually derived");
					}
					associatedProducers.add(derivingProduction);
				}
			}
		}

		if (currentProduction.endsWith(ONE_SHOT_MARKER)) {
			currentProduction = currentProduction.substring(0, currentProduction.length() - ONE_SHOT_MARKER.length());
			if (Logger.isAttivo()) {
				Logger.log("One-shot production: " + currentProduction);
			}
			oneShotProductions.add(currentProduction);
		}
		if (currentProduction.endsWith(DEFAULT_MARKER)) {
			throw new InvalidGrammarException(position(currentLineNumber) + ": Production name " + currentProduction
					+ " may not end with '" + DEFAULT_MARKER + "', which marks a reference's default value");
		}
		if (rootNode == null) {
			rootNode = currentProduction;
		}
		if (productionsMap.containsKey(currentProduction)) {
			throw new InvalidGrammarException(position(currentLineNumber) + ": Production " + currentProduction + " is repeated.");
		}
		productionsMap.put(currentProduction, new ArrayList<>());
		return currentProduction;
	}

	/**
	 * Aggiunge le alternative trovate su una riga rientrata come figli di {@code currentProduction}.
	 * La riga viene prima divisa in alternative di primo livello su {@code |}
	 * (ignorando ogni {@code |} che si trovi dentro un gruppo di alternative in linea {@code {...}}, vedi
	 * {@link #splitTopLevelAlternatives}); poi, per ogni alternativa, si analizza e si toglie l'eventuale
	 * elemento di peso {@code [^N]} iniziale (vedi {@link #parseWeight}) prima che i suoi gruppi di
	 * alternative in linea siano espansi in produzioni generate automaticamente (vedi {@link #expandInlineAlternations}).
	 * @throws InvalidGrammarException se la riga non è preceduta da un'intestazione di produzione,
	 *                                  contiene un gruppo di alternative in linea malformato o un
	 *                                  elemento di peso malformato
	 */
	private void handleChildren(String line, int currentLine, String currentProduction) throws InvalidGrammarException {
		List<WeightedAlternative> currentProductionChildren = productionsMap.get(currentProduction);
		for (String child : splitTopLevelAlternatives(line, currentLine)) {
			WeightedAlternative weighted = parseWeight(child.trim(), currentLine);
			currentProductionChildren.add(new WeightedAlternative(expandInlineAlternations(weighted.text, currentLine), weighted.weight));
		}
	}

	/**
	 * Analizza e toglie da {@code trimmedChild} un eventuale elemento di peso {@code [^N]} iniziale,
	 * in modo che questa alternativa sia scelta {@code N} volte più spesso di una con il
	 * peso predefinito di 1 (vedi {@link #getProduction}). {@code N} può essere frazionario (ad esempio
	 * {@code [^2.5]}). Un'alternativa senza tale elemento ha peso 1, esattamente come
	 * la selezione uniforme che ogni alternativa aveva prima che questa funzione esistesse.
	 * @return una {@link WeightedAlternative} con il testo rimanente e il peso analizzato
	 *         (oppure peso 1 e il testo intatto, se non c'è alcun elemento di peso)
	 * @throws InvalidGrammarException se un elemento di peso è aperto ma non chiuso, o il suo
	 *                                  valore non è un numero positivo
	 */
	private WeightedAlternative parseWeight(String trimmedChild, int currentLineNumber) throws InvalidGrammarException {
		String weightTokenStart = OPENING_BRACKET + WEIGHT_MARKER;
		if (!trimmedChild.startsWith(weightTokenStart)) {
			return new WeightedAlternative(trimmedChild, 1);
		}
		int closingBracketIndex = trimmedChild.indexOf(CLOSING_BRACKET);
		if (closingBracketIndex < 0) {
			throw new InvalidGrammarException(position(currentLineNumber) + ": Missing '" + CLOSING_BRACKET + "' for weight token");
		}
		String weightText = trimmedChild.substring(weightTokenStart.length(), closingBracketIndex);
		double weight;
		try {
			weight = Double.parseDouble(weightText);
		} catch (NumberFormatException e) {
			throw new InvalidGrammarException(position(currentLineNumber) + ": Invalid weight '" + weightText + "': must be a positive number");
		}
		if (!(weight > 0) || Double.isInfinite(weight)) {
			throw new InvalidGrammarException(position(currentLineNumber) + ": Invalid weight " + weightText + ": must be a positive number");
		}
		return new WeightedAlternative(trimmedChild.substring(closingBracketIndex + 1).trim(), weight);
	}

	/**
	 * Divide una riga rientrata nelle sue alternative di primo livello su {@code |},
	 * trattando ogni {@code |} che si trovi fra una graffa di apertura e la corrispondente graffa di chiusura come parte di
	 * un gruppo di alternative in linea e non come separatore di alternative, così che una riga come
	 * <code>A {a|b} | C</code> sia divisa in due pezzi (<code>A {a|b} </code> e <code> C</code>),
	 * non in quattro. Allo stesso modo un {@code |} fra una parentesi quadra di apertura e la corrispondente
	 * parentesi di chiusura fa parte di un elemento e non è un separatore, ed è ciò che permette a un elemento di portare un
	 * valore predefinito (<code>[Nome? | ripiego]</code>, vedi {@link #splitDefault}) o
	 * un'assegnazione il cui valore contiene un {@code |} letterale. Un {@code |} (o {@code {}}/{@code }}})
	 * dentro un tratto letterale grezzo ({@code "..."}) non è mai trattato nemmeno come separatore;
	 * virgolette ed escape qui sono lasciate intatte (vengono risolte più tardi, in
	 * {@link #expandInlineAlternations}).
	 * @throws InvalidGrammarException se un tratto letterale grezzo è aperto ma mai chiuso
	 */
	private List<String> splitTopLevelAlternatives(String line, int currentLineNumber) throws InvalidGrammarException {
		List<String> alternatives = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		int braceDepth = 0;
		int bracketDepth = 0;
		boolean inQuotes = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < line.length() && line.charAt(i + 1) == QUOTE_CHAR) {
					current.append(c).append(line.charAt(i + 1));
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
					current.append(c);
				} else {
					current.append(c);
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
				current.append(c);
			} else if (c == OPENING_BRACE) {
				braceDepth++;
				current.append(c);
			} else if (c == CLOSING_BRACE) {
				braceDepth--;
				current.append(c);
			} else if (c == OPENING_BRACKET.charAt(0)) {
				bracketDepth++;
				current.append(c);
			} else if (c == CLOSING_BRACKET.charAt(0)) {
				bracketDepth = Math.max(0, bracketDepth - 1);
				current.append(c);
			} else if (c == '|' && braceDepth == 0 && bracketDepth == 0) {
				alternatives.add(current.toString());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		if (inQuotes) {
			throw new InvalidGrammarException(position(currentLineNumber) + ": Missing closing '" + QUOTE_CHAR + "'");
		}
		alternatives.add(current.toString());
		return alternatives;
	}

	/**
	 * Espande ogni gruppo di alternative in linea di primo livello (<code>{opz1|opz2|...}</code>) trovato in
	 * {@code child} in un riferimento a una produzione nuova con nome generato automaticamente (vedi
	 * {@link #createInlineProduction}), così che il testo restituito possa da lì in poi essere trattato esattamente come
	 * ogni altra foglia. Il contenuto di un gruppo può a sua volta contenere altri gruppi annidati (ad esempio
	 * <code>{a|{b|c}}</code>); questi sono espansi ricorsivamente da {@link #createInlineProduction} nelle
	 * loro produzioni al volo prima che il gruppo esterno sia registrato.
	 * @throws InvalidGrammarException se una graffa di apertura non ha la corrispondente di chiusura,
	 *                                  se un tratto letterale grezzo è aperto ma mai chiuso, o se la
	 *                                  produzione in linea risultante sarebbe vuota
	 */
	private String expandInlineAlternations(String child, int currentLineNumber) throws InvalidGrammarException {
		StringBuilder result = new StringBuilder();
		int index = 0;
		int openingBraceIndex;
		while ((openingBraceIndex = findNextUnquotedOpeningBrace(child, index)) >= 0) {
			int closingBraceIndex = findMatchingClosingBrace(child, openingBraceIndex, currentLineNumber);
			result.append(unescapeQuotes(child.substring(index, openingBraceIndex)));
			String inlineOptionsText = child.substring(openingBraceIndex + 1, closingBraceIndex);
			String inlineProductionName = createInlineProduction(inlineOptionsText, currentLineNumber);
			result.append(OPENING_BRACKET).append(inlineProductionName).append(CLOSING_BRACKET);
			index = closingBraceIndex + 1;
		}
		result.append(unescapeQuotes(child.substring(index)));
		return result.toString();
	}

	/**
	 * Trova l'indice della prossima {@link #OPENING_BRACE} a partire da {@code fromIndex} (compreso) che non si trovi
	 * dentro un tratto letterale grezzo ({@code "..."}), così che ad esempio in <code>"a {b} c"</code> l'intero
	 * testo (graffa compresa) sia trattato come letterale, non come gruppo di alternative in linea.
	 * @return l'indice della prossima graffa di apertura non fra virgolette, o {@code -1} se non ce n'è
	 */
	private int findNextUnquotedOpeningBrace(String child, int fromIndex) {
		boolean inQuotes = false;
		for (int i = fromIndex; i < child.length(); i++) {
			char c = child.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < child.length() && child.charAt(i + 1) == QUOTE_CHAR) {
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
			} else if (c == OPENING_BRACE) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Toglie i delimitatori {@link #QUOTE_CHAR} di ogni tratto letterale grezzo trovato in {@code text},
	 * trasformando un escape {@code \"} in una {@code "} letterale e un escape {@code \[}/{@code \]} nel
	 * segnaposto che {@link #postProduce} riconvertirà in una parentesi letterale. È l'unico punto in cui i marcatori
	 * di un tratto letterale grezzo sono effettivamente risolti: le scansioni precedenti ({@link #splitTopLevelAlternatives},
	 * {@link #findMatchingClosingBrace}, {@link #findNextUnquotedOpeningBrace}) devono solo sapere
	 * dove si trova un tratto, non toglierlo, dato che farlo prima che il riconoscimento dei gruppi sia finito renderebbe una
	 * {@link #OPENING_BRACE}/{@link #CLOSING_BRACE} protetta dalle virgolette indistinguibile dalla sintassi vera.
	 */
	private String unescapeQuotes(String text) {
		StringBuilder result = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < text.length() && text.charAt(i + 1) == QUOTE_CHAR) {
					result.append(QUOTE_CHAR);
					i++;
				} else if (c == ESCAPE_CHAR && i + 1 < text.length() && text.charAt(i + 1) == OPENING_BRACKET.charAt(0)) {
					result.append(LITERAL_OPENING_BRACKET_PLACEHOLDER);
					i++;
				} else if (c == ESCAPE_CHAR && i + 1 < text.length() && text.charAt(i + 1) == CLOSING_BRACKET.charAt(0)) {
					result.append(LITERAL_CLOSING_BRACKET_PLACEHOLDER);
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
				} else {
					result.append(c);
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * Trova la graffa di chiusura corrispondente a quella di apertura in {@code openingBraceIndex}, rispettando
	 * il livello di annidamento, così che ad esempio in <code>{a|{b|c}}</code> la corrispondente del gruppo esterno sia proprio l'ultima
	 * <code>}</code>, non la prima incontrata.
	 * Una {@link #OPENING_BRACE}/{@link #CLOSING_BRACE} dentro un tratto letterale grezzo
	 * ({@code "..."}) non influisce sul conteggio della profondità, così il testo di un gruppo può contenere
	 * graffe letterali senza compromettere l'appaiamento.
	 * @throws InvalidGrammarException se non esiste una graffa di chiusura corrispondente
	 */
	private int findMatchingClosingBrace(String child, int openingBraceIndex, int currentLineNumber) throws InvalidGrammarException {
		int depth = 0;
		boolean inQuotes = false;
		for (int i = openingBraceIndex; i < child.length(); i++) {
			char c = child.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < child.length() && child.charAt(i + 1) == QUOTE_CHAR) {
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
			} else if (c == OPENING_BRACE) {
				depth++;
			} else if (c == CLOSING_BRACE) {
				depth--;
				if (depth == 0) {
					return i;
				}
			}
		}
		throw new InvalidGrammarException(position(currentLineNumber) + ": Missing '" + CLOSING_BRACE
				+ "' for inline alternation started at '" + child.substring(openingBraceIndex) + "'");
	}

	/**
	 * Crea una nuova produzione dal contenuto di un gruppo di alternative in linea, con nome
	 * {@link #INLINE_PRODUCTION_PREFIX} seguito da un numero progressivo univoco, e
	 * la registra in {@link #productionsMap}. Le alternative sono divise con
	 * {@link #splitTopLevelAlternatives} (non con una divisione ingenua per espressione regolare), così che i separatori
	 * <code>|</code> di un gruppo annidato non siano scambiati per quelli di primo livello, e ogni alternativa viene passata
	 * di nuovo a {@link #expandInlineAlternations}, così che ogni gruppo annidato che contiene diventi la
	 * propria produzione al volo prima che questa sia registrata. Un'alternativa vuota (ad esempio
	 * l'ultima opzione vuota in {@code {grande|feroce|}}) è conservata come foglia a stringa vuota, esattamente come
	 * un {@code |} finale sulla riga dei figli di una produzione normale: è ciò che permette a un gruppo in linea
	 * di esprimere un elemento facoltativo (ad esempio un aggettivo facoltativo). Ogni opzione può anche iniziare
	 * con un elemento di peso {@code [^N]} (eventualmente frazionario; vedi {@link #parseWeight}), che permette
	 * di pesare una produzione al volo, benché sia del tutto facoltativo: un'opzione senza tale
	 * elemento ha il peso predefinito di 1, perciò i gruppi in linea non richiedono alcun trattamento speciale per restare
	 * uniformemente casuali.
	 * @param inlineOptionsText il testo grezzo fra la graffa di apertura e quella di chiusura
	 * @return il nome della produzione appena creata
	 * @throws InvalidGrammarException se un gruppo di alternative in linea annidato è malformato, o
	 *                                  l'elemento di peso di un'opzione è malformato
	 */
	private String createInlineProduction(String inlineOptionsText, int currentLineNumber) throws InvalidGrammarException {
		List<String> rawOptions = splitTopLevelAlternatives(inlineOptionsText, currentLineNumber);
		if (rawOptions.size() < 2) {
			throw new InvalidGrammarException(position(currentLineNumber) + ": Inline alternation group '"
					+ OPENING_BRACE + inlineOptionsText + CLOSING_BRACE + "' has no '|': a group with a single"
					+ " option only strips its own braces. Remove them, or quote them (\"" + OPENING_BRACE
					+ "\" ... \"" + CLOSING_BRACE + "\") to emit them as literal text");
		}
		List<WeightedAlternative> options = new ArrayList<>();
		for (String option : rawOptions) {
			WeightedAlternative weighted = parseWeight(option.trim(), currentLineNumber);
			options.add(new WeightedAlternative(expandInlineAlternations(weighted.text, currentLineNumber), weighted.weight));
		}
		String name;
		do {
			name = inlinePrefix + inlineProductionCounter++;
		} while (productionsMap.containsKey(name));
		productionsMap.put(name, options);
		return name;
	}

	/**
	 * Scorre ogni figlio di ogni produzione cercando i riferimenti {@code [Nome]} e
	 * convalida ciascuno con {@link #checkTokenValidity}. Viene chiamato una volta, subito dopo che
	 * l'intero file di grammatica (comprese le produzioni generate dai gruppi di alternative in linea)
	 * è stato analizzato.
	 * <p>Una chiave assegnata in un punto qualsiasi con {@code [chiave=valore]} è un bersaglio valido per un
	 * riferimento {@code *}/{@code !}/semplice altrove, anche se non esiste alcuna produzione di nome
	 * {@code chiave}, dato che a runtime un riferimento simile è soddisfatto dalla cache delle
	 * produzioni fissate e non dalla mappa delle produzioni. Serve quindi un
	 * primo passaggio ({@link #collectAssignedKeys()}) su tutta la grammatica prima del
	 * passaggio di convalida vero e proprio, dato che un'assegnazione può comparire nel testo dopo il
	 * riferimento che ne dipende.
	 * @throws InvalidGrammarException se un riferimento è malformato o punta a una produzione non definita
	 */
	private void checkProductionsValidity() throws InvalidGrammarException {
		Set<String> assignedKeys = collectAssignedKeys();
		for (String key : productionsMap.keySet()) {
			for (WeightedAlternative alternative : productionsMap.get(key)) {
				checkTokensValidity(alternative.text, assignedKeys);
			}
		}
	}

	/**
	 * Scorre ogni figlio di ogni produzione raccogliendo le chiavi assegnate con
	 * {@code [chiave=valore]}, comprese le assegnazioni annidate nel valore di un'altra
	 * assegnazione (ad esempio {@code [chiave=[AltraProduzione]]}).
	 */
	private Set<String> collectAssignedKeys() throws InvalidGrammarException {
		Set<String> assignedKeys = new HashSet<>();
		for (List<WeightedAlternative> alternatives : productionsMap.values()) {
			for (WeightedAlternative alternative : alternatives) {
				collectAssignedKeys(alternative.text, assignedKeys);
			}
		}
		return assignedKeys;
	}

	private void collectAssignedKeys(String text, Set<String> assignedKeys) throws InvalidGrammarException {
		int openingBracketIndex;
		while ((openingBracketIndex = text.indexOf(OPENING_BRACKET)) >= 0) {
			int closingBracketIndex = findClosingBracketOrThrow(text, openingBracketIndex);
			String tokenBody = text.substring(openingBracketIndex + 1, closingBracketIndex);
			TokenWithDefault withDefault = splitDefault(tokenBody);
			if (withDefault.fallback != null) {
				// un elemento con predefinito legge soltanto; il suo ripiego, però, può benissimo assegnare
				collectAssignedKeys(withDefault.fallback, assignedKeys);
			} else {
				int equalsPosition = tokenBody.indexOf(ASSIGNMENT_MARKER);
				if (equalsPosition > 0) {
					assignedKeys.add(tokenBody.substring(0, equalsPosition));
					collectAssignedKeys(tokenBody.substring(equalsPosition + ASSIGNMENT_MARKER.length()), assignedKeys);
				}
			}
			text = text.substring(closingBracketIndex + 1);
		}
	}

	/**
	 * Scorre ogni elemento {@code [...]} in {@code text} (tenendo conto dell'annidamento, così un elemento come
	 * {@code [chiave=[AltraProduzione]]} è trattato come un unico elemento esterno il cui valore è
	 * a sua volta convalidato ricorsivamente) e convalida ciascuno con {@link #checkTokenValidity}.
	 */
	private void checkTokensValidity(String text, Set<String> assignedKeys) throws InvalidGrammarException {
		int openingBracketIndex;
		while ((openingBracketIndex = text.indexOf(OPENING_BRACKET)) >= 0) {
			int closingBracketIndex = findClosingBracketOrThrow(text, openingBracketIndex);
			checkTokenValidity(text.substring(openingBracketIndex + 1, closingBracketIndex), assignedKeys);
			text = text.substring(closingBracketIndex + 1);
		}
	}

	/**
	 * Convalida il corpo di un singolo riferimento {@code [...]} (un marcatore di produzione fissata,
	 * un'assegnazione, un riferimento a valore, o un semplice nome di produzione).
	 * @throws InvalidGrammarException se un marcatore di produzione fissata non ha un nome dopo di sé,
	 *                                  un marcatore di assegnazione non ha una chiave prima di sé, o
	 *                                  l'elemento è un semplice nome che non è una produzione definita
	 *                                  e non è stato assegnato in nessun punto della grammatica
	 */
	private void checkTokenValidity(String thisProduction, Set<String> assignedKeys) throws InvalidGrammarException {
		TokenWithDefault withDefault = splitDefault(thisProduction);
		if (withDefault.fallback != null) {
			checkTokensValidity(withDefault.fallback, assignedKeys);
			String name = withDefault.name;
			if (name.startsWith(GLOBAL_FIXED_PRODUCTION_MARKER) || name.startsWith(LOCAL_FIXED_PRODUCTION_MARKER)
					|| name.startsWith(REFERENCE_MARKER)) {
				name = name.substring(1);
			}
			if (name.isEmpty()) {
				throw new InvalidGrammarException(DEFAULT_MARKER + " marker must be preceded by a node name");
			}
			if (productionsMap.get(name) == null && !assignedKeys.contains(name) && !isStaticName(name)) {
				throw new InvalidGrammarException(PRODUCTION + name + " is not defined: a '" + DEFAULT_MARKER
						+ "' default covers the order in which an assignment runs, not the existence of the name");
			}
			return;
		}
		if (thisProduction.startsWith(GLOBAL_FIXED_PRODUCTION_MARKER) || thisProduction.startsWith(LOCAL_FIXED_PRODUCTION_MARKER)) {
			thisProduction = thisProduction.substring(1);
			if (thisProduction.isEmpty()) {
				throw new InvalidGrammarException("Empty fixed production present (" + GLOBAL_FIXED_PRODUCTION_MARKER
						+ " or " + LOCAL_FIXED_PRODUCTION_MARKER + " alone)");
			}
		}
		if (thisProduction.startsWith(ASSIGNMENT_MARKER)) {
			throw new InvalidGrammarException(ASSIGNMENT_MARKER + " symbol must be preceded by a node name");
		}
		int equalsPosition = thisProduction.indexOf(ASSIGNMENT_MARKER);
		if (equalsPosition >= 0) {
			checkTokensValidity(thisProduction.substring(equalsPosition + ASSIGNMENT_MARKER.length()), assignedKeys);
			return;
		}
		if (productionsMap.get(thisProduction) == null && !assignedKeys.contains(thisProduction) && !thisProduction.startsWith(REFERENCE_MARKER)
				&& !isStaticName(thisProduction)) {
			throw new InvalidGrammarException(PRODUCTION + thisProduction + " is not defined");
		}
	}

	/**
	 * Come {@link #findClosingBracket}, ma segnala una parentesi non appaiata come
	 * {@link InvalidGrammarException} invece che come {@link IllegalArgumentException},
	 * per l'uso durante la convalida al caricamento della grammatica.
	 */
	private int findClosingBracketOrThrow(String text, int openingBracketIndex) throws InvalidGrammarException {
		try {
			return findClosingBracket(text, openingBracketIndex);
		} catch (IllegalArgumentException e) {
			throw new InvalidGrammarException("Missing '" + CLOSING_BRACKET + "' element after token " + text);
		}
	}

	/**
	 * Aumenta il peso di ogni alternativa di {@link #SCALE_FACTOR} volte il peso aggregato (aggiustato
	 * ricorsivamente) di ogni produzione che referenzia (vedi
	 * {@link #findReferencedProductions}), così che le alternative che si espandono in sottoalberi
	 * strutturalmente più ricchi siano scelte proporzionalmente più spesso con
	 * {@link TipoModalitaProduzioneTesti#CASUALE}, senza bisogno di un elemento {@code [^N]}
	 * scritto a mano su ogni alternativa. Viene chiamato una volta, subito dopo che l'intera grammatica (comprese
	 * le produzioni dei gruppi di alternative in linea) è stata analizzata e convalidata, e prima che
	 * {@link #reset()} copi mai {@link #productionsMap} in
	 * {@link #currentProductionsMap}.
	 * <p>
	 * Le produzioni sono visitate nell'ordine di dichiarazione, ed è ciò che rende l'esito per
	 * i cicli di riferimenti prevedibile dal file di grammatica; vedi
	 * {@link #computeAdjustedAggregateWeight}.
	 */
	private void adjustWeightsForDescendants() {
		Map<String, Double> aggregateWeightCache = new HashMap<>();
		Set<String> inProgressProductions = new HashSet<>();
		for (String name : productionsMap.keySet()) {
			computeAdjustedAggregateWeight(name, aggregateWeightCache, inProgressProductions);
		}
	}

	/**
	 * Restituisce il peso aggregato (somma di quelli di tutte le alternative) della produzione {@code name}
	 * dopo aver aumentato ricorsivamente ciascuna delle sue alternative tramite
	 * {@link #adjustWeightsForDescendants}, sostituendo come effetto collaterale la sua voce in {@link #productionsMap}
	 * con le alternative aumentate. I risultati sono memorizzati in
	 * {@code aggregateWeightCache}.
	 * <p>
	 * Sicuro rispetto ai cicli: i riferimenti fra produzioni possono formare cicli (produzioni ricorsive su se stesse
	 * o a vicenda). Una produzione ancora in calcolo più in alto nello stack delle chiamate (tracciata in
	 * {@code inProgressProductions}) non viene rientrata; si sommano invece direttamente le sue alternative
	 * attuali, non ancora aumentate, rompendo il ciclo con il peso base originale di quella produzione. Per un ciclo che attraversa più di una produzione,
	 * il membro raggiunto per primo finisce con un aumento maggiore degli altri; dato che {@link #productionsMap} mantiene
	 * l'ordine di inserimento, è il membro dichiarato per primo nel file di grammatica, perciò
	 * l'asimmetria si può prevedere (e riordinare) leggendo il file.
	 */
	private double computeAdjustedAggregateWeight(String name, Map<String, Double> aggregateWeightCache, Set<String> inProgressProductions) {
		Double cached = aggregateWeightCache.get(name);
		if (cached != null) {
			return cached;
		}
		if (!productionsMap.containsKey(name)) {
			// Una produzione di una sezione statica: i suoi pesi sono stati aggiustati quando la sezione è stata caricata
			double sectionTotal = 0;
			for (WeightedAlternative alternative : staticSectionOf(name).productions.get(name)) {
				sectionTotal += alternative.weight;
			}
			return sectionTotal;
		}
		List<WeightedAlternative> alternatives = productionsMap.get(name);
		if (!inProgressProductions.add(name)) {
			double baseSum = 0;
			for (WeightedAlternative alternative : alternatives) {
				baseSum += alternative.weight;
			}
			return baseSum;
		}
		List<WeightedAlternative> adjusted = new ArrayList<>(alternatives.size());
		double total = 0;
		for (WeightedAlternative alternative : alternatives) {
			double boost = 0;
			for (String referencedProduction : findReferencedProductions(alternative.text)) {
				boost += computeAdjustedAggregateWeight(referencedProduction, aggregateWeightCache, inProgressProductions);
			}
			double adjustedWeight = alternative.weight + SCALE_FACTOR * boost;
			adjusted.add(new WeightedAlternative(alternative.text, adjustedWeight));
			total += adjustedWeight;
		}
		inProgressProductions.remove(name);
		productionsMap.put(name, adjusted);
		aggregateWeightCache.put(name, total);
		return total;
	}

	/**
	 * Restituisce ogni nome di produzione referenziato (come {@code [Nome]}, {@code [*Nome]} o
	 * {@code [!Nome]}) direttamente in {@code text}, in ordine di occorrenza, mantenendo i
	 * duplicati: una produzione referenziata due volte nella stessa alternativa contribuisce
	 * due volte con il proprio peso aggregato all'aumento del peso dei discendenti di quell'alternativa (vedi
	 * {@link #adjustWeightsForDescendants}). Rispecchia il modo in cui {@link #checkTokenValidity}
	 * toglie i marcatori, così gli elementi {@code [chiave=valore]}/{@code [#chiave]}, che non
	 * referenziano una produzione, sono saltati, salvo che il valore di un'assegnazione viene a sua volta
	 * cercato ricorsivamente (così {@code [chiave=[AltraProduzione]]} conta comunque
	 * {@code AltraProduzione} come referenziata). Anche un nome {@code *}/{@code !}/semplice che non sia
	 * una vera produzione (cioè che viene popolato solo tramite {@code =} altrove) è saltato,
	 * dato che non ha un proprio peso aggregato. Presuppone che {@code text} sia già
	 * noto come ben formato (viene chiamato solo dopo che {@link #checkProductionsValidity} è
	 * riuscito).
	 */
	private List<String> findReferencedProductions(String text) {
		List<String> referenced = new ArrayList<>();
		collectReferencedProductions(text, referenced);
		return referenced;
	}

	private void collectReferencedProductions(String text, List<String> referenced) {
		String remaining = text;
		int openingBracketIndex;
		while ((openingBracketIndex = remaining.indexOf(OPENING_BRACKET)) >= 0) {
			int closingBracketIndex = findClosingBracket(remaining, openingBracketIndex);
			String tokenBody = remaining.substring(openingBracketIndex + 1, closingBracketIndex);
			TokenWithDefault withDefault = splitDefault(tokenBody);
			if (withDefault.fallback != null) {
				// dei due rami se ne prende esattamente uno al momento della produzione, quindi contarli
				// entrambi gonfierebbe il peso dell'alternativa: si preferisce il nome quando è una vera
				// produzione, e ci si ripiega su ciò che il predefinito stesso referenzia
				String name = withDefault.name;
				if (name.startsWith(GLOBAL_FIXED_PRODUCTION_MARKER) || name.startsWith(LOCAL_FIXED_PRODUCTION_MARKER)) {
					name = name.substring(1);
				}
				if (!name.startsWith(REFERENCE_MARKER) && isDefined(name)) {
					referenced.add(name);
				} else {
					collectReferencedProductions(withDefault.fallback, referenced);
				}
				remaining = remaining.substring(closingBracketIndex + 1);
				continue;
			}
			if (tokenBody.startsWith(GLOBAL_FIXED_PRODUCTION_MARKER) || tokenBody.startsWith(LOCAL_FIXED_PRODUCTION_MARKER)) {
				tokenBody = tokenBody.substring(1);
			}
			int equalsPosition = tokenBody.indexOf(ASSIGNMENT_MARKER);
			if (equalsPosition >= 0) {
				collectReferencedProductions(tokenBody.substring(equalsPosition + ASSIGNMENT_MARKER.length()), referenced);
			} else if (!tokenBody.startsWith(REFERENCE_MARKER) && isDefined(tokenBody)) {
				referenced.add(tokenBody);
			}
			remaining = remaining.substring(closingBracketIndex + 1);
		}
	}

	/**
	 * Legge il file facoltativo di post-produzione in {@link #postProductions}. Ogni riga
	 * deve avere la forma {@code pre:post}; un file mancante (uno stream {@code null}) comporta semplicemente
	 * nessuna sostituzione di post-produzione.
	 * <p>
	 * Una riga che inizia con {@link #COMMENT_PREFIX} è un commento a riga intera e una riga vuota è
	 * ignorata, esattamente come nel file di grammatica: una regola di sostituzione come {@code [,:[} ha bisogno
	 * di una parola di spiegazione accanto molto più di una riga di grammatica. Commenti e righe vuote
	 * contano comunque per il numero di riga riportato in un messaggio di errore, così che indichi
	 * la riga reale del file. Si noti che una riga è considerata vuota quando è
	 * <i>fatta di soli spazi</i>, non solo quando è vuota: a differenza di un file di grammatica, dove una riga
	 * rientrata e vuota è un'alternativa vuota significativa, qui non potrebbe mai esprimere una regola. Una regola il cui
	 * {@code pre} inizia con {@code #} non è di conseguenza esprimibile, lo stesso compromesso che fa il
	 * file di grammatica.
	 * <p>
	 * Una regola il cui {@code post} contiene alla lettera il proprio {@code pre} viene rifiutata qui,
	 * perché {@link #postProduce} riesamina il testo che ha appena riscritto e
	 * continuerebbe quindi a ritrovare lo stesso {@code pre} dentro il {@code post} appena scritto,
	 * ciclando all'infinito. Rifiutarla al caricamento lascia il ciclo di sostituzione in
	 * {@link #postProduce} esattamente com'è (applicazione ripetuta per regola, così che
	 * le occorrenze sovrapposte siano tutte riscritte) invece di rinunciare a quel comportamento
	 * a favore di una sostituzione in un solo passaggio.
	 * @throws InvalidGrammarException se in una riga manca il testo di pre- o post-produzione,
	 *                                  o il suo testo di post-produzione contiene il proprio
	 *                                  testo di pre-produzione
	 */
	private void setPostProductionFile(InputStream inputStream) throws InvalidGrammarException, IOException {
		if (inputStream == null) {
			return;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			String pre;
			String post;
			int currentLine = 0;
			while ((line = reader.readLine()) != null) {
				currentLine++;
				if (line.trim().isEmpty() || isComment(line)) {
					continue;
				}
				String[] parts = line.split(POST_PRODUCTION_SEPARATOR, 2);
				pre = parts[0];
				if (pre.trim().isEmpty()) {
					throw new InvalidGrammarException(LINE + currentLine + ": No pre production");
				}
				if (parts.length < 2) {
					throw new InvalidGrammarException(LINE + currentLine + ": No post production");
				}
				post = parts[1];
				if (post.trim().isEmpty()) {
					throw new InvalidGrammarException(LINE + currentLine + ": Empty post production");
				}
				if (post.contains(pre)) {
					throw new InvalidGrammarException(LINE + currentLine + ": Post production '" + post
							+ "' contains its own pre production '" + pre
							+ "': the substitution would never terminate");
				}
				postProductions.put(pre, post);
			}
		}
	}

	/**
	 * Cambia la produzione da cui parte {@link #produce()}.
	 * @param rootNode il nome semplice (senza parentesi) di una produzione esistente
	 * @throws IllegalArgumentException se {@code rootNode} non è una produzione definita
	 */
	public void setRootNode(String rootNode) {
		if (productionsMap.get(rootNode) == null && !isStaticName(rootNode)) {
			throw new IllegalArgumentException(rootNode + ": not a valid production");
		}
		this.rootNode = rootNode;
	}

	/**
	 * @return il nome semplice (senza parentesi) della produzione da cui {@link #produce()} parte attualmente
	 */
	public String getRootNode() {
		return rootNode;
	}

	/**
	 * Dice se la produzione può ancora produrre qualcosa nel ciclo corrente: esiste e ha
	 * ancora alternative. Una produzione monouso ({@code NOME$}) smette di poterlo fare quando la sua ultima
	 * alternativa è consumata, e così ogni produzione che referenziava solo lei (la cascata di
	 * rimozione, vedi {@link #removeProduction}), fino al prossimo {@link #reset()}. È il modo in cui un chiamante
	 * che estrae una serie di risultati unici scopre che la serie è finita, invece di aspettare che
	 * {@link #produce(String)} lanci un'eccezione.
	 * <p>
	 * Non guarda dentro le alternative: una che referenzia due volte la stessa produzione monouso esaurita
	 * ({@code [X] [X]}) conta comunque, e fallisce solo quando viene prodotta.
	 * @param production il nome semplice (senza parentesi) di una produzione
	 * @return false anche per una produzione mai definita
	 */
	public boolean canProduce(String production) {
		refreshStatic();
		List<WeightedAlternative> alternatives = currentProductionsMap.get(production);
		return alternatives != null && !alternatives.isEmpty();
	}

	List<WeightedAlternative> getProductions(String rootProduction) {
		return productionsMap.get(rootProduction);
	}

	/**
	 * @return la modalità corrente di scelta dell'alternativa (vedi {@link TipoModalitaProduzioneTesti})
	 */
	public TipoModalitaProduzioneTesti getProductionMode() {
		return productionMode;
	}

	/**
	 * Imposta come viene scelta l'alternativa di una produzione.
	 * @param productionMode la nuova modalità; vedi {@link TipoModalitaProduzioneTesti}
	 */
	public void setProductionMode(TipoModalitaProduzioneTesti productionMode) {
		this.productionMode = productionMode;
	}

	/**
	 * Ripristina {@link #currentProductionsMap} a una copia fresca di {@link #productionsMap} e
	 * svuota {@link #globalFixedProductions}, così che ogni alternativa monouso consumata da un
	 * ciclo di produzione precedente torni disponibile e nessun valore fissato vecchio trapeli nel ciclo
	 * successivo.
	 * <p>
	 * È la chiamata che traccia il confine fra una serie di produzioni e la successiva, quindi
	 * <b>quando</b> farla è una scelta, non una formalità:
	 * <ul>
	 *     <li>dopo ogni {@link #produce()}, quando ogni risultato deve essere indipendente dagli
	 *     altri e può riusare le stesse alternative;</li>
	 *     <li>solo all'inizio di una serie, quando lo scopo della serie è che i suoi risultati
	 *     <b>non</b> si ripetano a vicenda: fare il reset nel mezzo vanificherebbe le produzioni
	 *     monouso su cui la grammatica conta a tal fine.</li>
	 * </ul>
	 * Una grammatica senza produzioni monouso e senza riferimenti {@code [*Nome]} non ha nulla da
	 * ripristinare, quindi chiamarlo è innocuo ma inutile. Vedi la documentazione della classe per cosa
	 * succede a una serie che sopravvive al suo più piccolo serbatoio monouso.
	 */
	public void reset() {
		rebuildCurrentProductions();
		globalFixedProductions.clear();
	}

	/**
	 * Copia {@link #productionsMap} in {@link #currentProductionsMap} e pota ciò che le sezioni statiche hanno
	 * già esaurito. Non tocca le sezioni statiche: lo fa solo {@link #resetStaticProductions()}.
	 */
	private void rebuildCurrentProductions() {
		synchronized (STATIC_LOCK) {
			currentProductionsMap.clear();
			for (Map.Entry<String, List<WeightedAlternative>> entry : productionsMap.entrySet()) {
				currentProductionsMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
			}
			prunedStaticNames.clear();
			seenStaticEpoch = staticEpoch;
			syncStatic();
			seenStaticVersion = staticVersion;
		}
	}

	/**
	 * Preimposta il valore che sarà restituito per un riferimento a produzione fissata globalmente
	 * ({@code [*nome]}), prima di chiamare {@link #produce()}.
	 */
	public void addFixedProduction(String name, String value) {
		globalFixedProductions.put(name, value);
	}

	/**
	 * Produce un testo casuale a partire da {@link #rootNode}.
	 * <p>
	 * Se la grammatica ha produzioni monouso, se questa chiamata debba essere seguita da
	 * {@link #reset()} dipende da cosa vuole il chiamante: reset per rendere ogni chiamata
	 * indipendente, niente reset per costruire una serie di risultati che non devono ripetersi a vicenda.
	 * Vedi la documentazione della classe.
	 * @return il testo prodotto, diviso in una voce per riga
	 */
	public List<String> produce() {
		productionTrace.clear();
		try {
			return produceImpl(OPENING_BRACKET + rootNode + CLOSING_BRACKET);
		} catch (RuntimeException e) {
			Logger.log(() -> "Grammar production failed. Trace:\n" + describeProductionTrace());
			throw e;
		} finally {
			globalFixedProductions.clear();
		}
	}

	/**
	 * Produce un testo casuale a partire da una produzione data esplicitamente, senza
	 * cambiare {@link #rootNode}. Come {@link #produce()}, la cache globale delle produzioni fissate
	 * viene svuotata una volta prodotto il testo, così che un riferimento {@code [*Nome]}
	 * resti fissato all'interno di una singola chiamata invece di restare congelato per tutte
	 * le successive. Le produzioni monouso, invece, restano consumate finché non si chiama
	 * {@link #reset()}; vedi la documentazione della classe per quando è ciò che si vuole.
	 * @param rootNode il nome semplice (senza parentesi) di una produzione esistente
	 * @return il testo prodotto, diviso in una voce per riga
	 */
	public List<String> produce(String rootNode) {
		productionTrace.clear();
		try {
			return produceImpl(OPENING_BRACKET + rootNode + CLOSING_BRACKET);
		} catch (RuntimeException e) {
			Logger.log(() -> "Grammar production failed. Trace:\n" + describeProductionTrace());
			throw e;
		} finally {
			globalFixedProductions.clear();
		}
	}

	/**
	 * Espande {@code startNode} (già racchiuso fra parentesi da {@link #produce()} o
	 * {@link #produce(String)}) nel suo testo finale, poi applica le sostituzioni
	 * di post-produzione e divide il risultato in righe.
	 * <p>
	 * Visibile solo nel package di proposito: prende un elemento già fra parentesi ed espone l'ordine
	 * interno delle fasi di produzione, quindi non fa parte della superficie pubblica della classe.
	 * È visibile solo perché i test di questo package possano pilotare direttamente il passaggio finale
	 * di divisione in righe, che nessun file di grammatica può raggiungere (nessuno dei due file
	 * sorgente può incorporare un a capo letterale dentro una singola voce).
	 * @param startNode un singolo elemento {@code [Nome]} da espandere
	 * @return il testo prodotto, diviso in una voce per riga
	 */
	List<String> produceImpl(String startNode) {
		refreshStatic();
		String firstResult = produceImpl(startNode, currentProductionsMap, globalFixedProductions, startNode);
		String intermediateResult = postProduce(firstResult);
        return new ArrayList<>(Arrays.asList(intermediateResult.split(LINE_BREAK_REGEX)));
	}

	/**
	 * Descrive {@link #productionTrace} così com'è al momento, una riga per frame attivo
	 * (prima il più esterno), ciascuna con i valori successivi attraversati dalla propria espansione.
	 * Pensato per essere chiamato subito dopo che un'eccezione interrompe {@link #produce()}/
	 * {@link #produce(String)}, quando i frame rimasti in {@link #productionTrace} sono esattamente
	 * il percorso seguito dal motore della grammatica prima di fallire; chiamarlo in qualsiasi altro momento (ad esempio
	 * dopo una produzione riuscita) restituisce una stringa vuota, dato che ogni frame viene tolto
	 * al ritorno normale.
	 * @return la traccia, o una stringa vuota se non ce n'è di attiva
	 */
	String describeProductionTrace() {
		StringBuilder sb = new StringBuilder();
		for (TraceFrame frame : productionTrace) {
			sb.append(frame.label).append(": ").append(String.join(" -> ", frame.steps)).append('\n');
		}
		return sb.toString();
	}

	/**
	 * Trova ripetutamente l'elemento {@code [...]} più a sinistra in {@code production} e lo sostituisce
	 * con il suo valore risolto (vedi {@link #resolveToken}) finché non ne restano.
	 * @param production il testo da espandere, in genere all'inizio un unico elemento {@code [Nome]}
	 * @param superProductionsMap la mappa delle produzioni rispetto a cui risolvere i riferimenti semplici/fissati
	 *                            (quella dell'intera grammatica, o una sua copia potata dei monouso)
	 * @param superFixedProductions la cache delle produzioni fissate ereditata dal chiamante
	 *                              (la cache globale per una chiamata di primo livello, o la cache locale di un sottoalbero)
	 * @param label nomina ciò che viene espanso, per {@link #productionTrace}: un nome di produzione,
	 *              o l'elemento iniziale {@code [rootNode]} per la chiamata più esterna
	 * @return il testo completamente espanso, senza più elementi {@code [...]}
	 */
	private String produceImpl(String production, Map<String, List<WeightedAlternative>> superProductionsMap, Map<String, String> superFixedProductions, String label) {
		// Inizializzata con la cache del chiamante e non vuota, così che un valore fissato da un
		// [!Nome] più in alto sia visibile a un [#Nome] (o a un altro [!Nome]) in qualsiasi punto sotto
		// di esso nello stesso sottoalbero, a qualsiasi profondità. La copia è ciò che mantiene
		// indipendenti i rami fratelli: ognuno eredita lo stesso punto di partenza ma scrive solo nella
		// propria mappa, così nulla di ciò che un ramo fissa trapela verso l'alto o di lato.
		Map<String, String> localFixedProductions = new HashMap<>(superFixedProductions);
		TraceFrame frame = new TraceFrame(label, production);
		productionTrace.addLast(frame);
		int openingBracketIndex;
		while ((openingBracketIndex = production.indexOf(OPENING_BRACKET)) >= 0) {
			boolean capitalize = openingBracketIndex > 0
					&& production.charAt(openingBracketIndex - 1) == CAPITALIZE_MARKER.charAt(0);
			int prefixEnd = capitalize ? openingBracketIndex - 1 : openingBracketIndex;
			int closingBracketIndex = findClosingBracket(production, openingBracketIndex);
			String prefix = production.substring(0, prefixEnd);
			String tokenBody = production.substring(openingBracketIndex + 1, closingBracketIndex);
			String postfix = production.substring(closingBracketIndex + 1);
			String resolved = resolveToken(tokenBody, superProductionsMap, superFixedProductions, localFixedProductions);
			if (capitalize) {
				resolved = capitalizeFirstLetter(resolved);
			}
			production = prefix + resolved + postfix;
			frame.steps.add(production);
		}
		// Solo sul percorso di ritorno normale: se un'eccezione si propaga fuori da questa chiamata
		// (o da una annidata in essa), questo frame viene lasciato di proposito in productionTrace,
		// così che describeProductionTrace() possa ancora riportarlo.
		productionTrace.removeLast();
		return production;
	}

	/**
	 * Mette in maiuscolo il primo carattere di {@code text}, se c'è; serve a realizzare
	 * {@link #CAPITALIZE_MARKER}.
	 */
	private static String capitalizeFirstLetter(String text) {
		if (text.isEmpty()) {
			return text;
		}
		return Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}

	/**
	 * Il corpo di un elemento diviso nel nome che referenzia e nel valore di ripiego da usare quando quel
	 * nome non ha alcun valore disponibile. {@link #fallback} è {@code null} per un elemento senza
	 * {@link #DEFAULT_MARKER}, ed è ciò che dice a ogni chiamante di seguire il percorso ordinario.
	 */
	private static final class TokenWithDefault {
		private final String name;
		private final String fallback;

		private TokenWithDefault(String name, String fallback) {
			this.name = name;
			this.fallback = fallback;
		}
	}

	/**
	 * Trova il {@code |} che separa il nome referenziato da un elemento dal suo valore predefinito: il
	 * primo a profondità di annidamento zero (contano parentesi quadre, graffe e tratti letterali grezzi)
	 * il cui carattere non bianco immediatamente precedente è un {@link #DEFAULT_MARKER}.
	 * <p>
	 * Restituisce {@code -1} quando non ce n'è nessuno, e anche quando si trova prima un {@code =} a profondità zero:
	 * ciò rende l'elemento un'assegnazione ({@code [chiave=valore]}), il cui valore può liberamente
	 * contenere {@code ?} e {@code |} come testo semplice. Decide il primissimo {@code |} a profondità zero:
	 * se non è preceduto da un {@link #DEFAULT_MARKER} l'elemento semplicemente non ha
	 * predefinito, invece di proseguire la scansione a caccia di un candidato successivo.
	 */
	private int findDefaultSeparator(String tokenBody) {
		int braceDepth = 0;
		int bracketDepth = 0;
		boolean inQuotes = false;
		for (int i = 0; i < tokenBody.length(); i++) {
			char c = tokenBody.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < tokenBody.length()) {
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
			} else if (c == OPENING_BRACE) {
				braceDepth++;
			} else if (c == CLOSING_BRACE) {
				braceDepth = Math.max(0, braceDepth - 1);
			} else if (c == OPENING_BRACKET.charAt(0)) {
				bracketDepth++;
			} else if (c == CLOSING_BRACKET.charAt(0)) {
				bracketDepth = Math.max(0, bracketDepth - 1);
			} else if (braceDepth == 0 && bracketDepth == 0) {
				if (c == ASSIGNMENT_MARKER.charAt(0)) {
					return -1;
				}
				if (c == '|') {
					return endsWithDefaultMarker(tokenBody.substring(0, i)) ? i : -1;
				}
			}
		}
		return -1;
	}

	/**
	 * @return se {@code text}, ignorando gli spazi finali, termina con il
	 *         {@link #DEFAULT_MARKER}
	 */
	private static boolean endsWithDefaultMarker(String text) {
		String trimmed = trimTrailingWhitespace(text);
		return trimmed.endsWith(DEFAULT_MARKER);
	}

	private static String trimTrailingWhitespace(String text) {
		int end = text.length();
		while (end > 0 && Character.isWhitespace(text.charAt(end - 1))) {
			end--;
		}
		return text.substring(0, end);
	}

	/**
	 * Divide il corpo di un elemento nel nome che referenzia e nel suo valore predefinito, riconoscendo sia
	 * {@code Nome? | ripiego} sia la forma abbreviata {@code Nome?} (un ripiego vuoto). Gli spazi
	 * attorno al {@link #DEFAULT_MARKER} e al {@code |} sono ignorati; il ripiego stesso viene
	 * restituito alla lettera, per essere espanso e ripulito da chi ne ha bisogno.
	 * @return un {@link TokenWithDefault} il cui {@code fallback} è {@code null} quando l'elemento
	 *         non porta alcun predefinito, nel qual caso il suo {@code name} è {@code tokenBody}
	 *         intatto
	 */
	private TokenWithDefault splitDefault(String tokenBody) {
		int separatorIndex = findDefaultSeparator(tokenBody);
		if (separatorIndex >= 0) {
			String beforeSeparator = trimTrailingWhitespace(tokenBody.substring(0, separatorIndex));
			String name = beforeSeparator.substring(0, beforeSeparator.length() - DEFAULT_MARKER.length()).trim();
			return new TokenWithDefault(name, tokenBody.substring(separatorIndex + 1));
		}
		if (endsWithDefaultMarker(tokenBody) && !hasTopLevelAssignment(tokenBody)) {
			String trimmed = trimTrailingWhitespace(tokenBody);
			return new TokenWithDefault(trimmed.substring(0, trimmed.length() - DEFAULT_MARKER.length()).trim(), "");
		}
		return new TokenWithDefault(tokenBody, null);
	}

	/**
	 * @return se {@code tokenBody} contiene un {@link #ASSIGNMENT_MARKER} a profondità di annidamento
	 *         zero, cioè se è un'assegnazione e non un riferimento
	 */
	private boolean hasTopLevelAssignment(String tokenBody) {
		int braceDepth = 0;
		int bracketDepth = 0;
		boolean inQuotes = false;
		for (int i = 0; i < tokenBody.length(); i++) {
			char c = tokenBody.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < tokenBody.length()) {
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
			} else if (c == OPENING_BRACE) {
				braceDepth++;
			} else if (c == CLOSING_BRACE) {
				braceDepth = Math.max(0, braceDepth - 1);
			} else if (c == OPENING_BRACKET.charAt(0)) {
				bracketDepth++;
			} else if (c == CLOSING_BRACKET.charAt(0)) {
				bracketDepth = Math.max(0, bracketDepth - 1);
			} else if (c == ASSIGNMENT_MARKER.charAt(0) && braceDepth == 0 && bracketDepth == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Trova la {@code ]} corrispondente alla {@code [} in {@code openingBracketIndex}, rispettando
	 * l'annidamento, così che ad esempio in {@code [chiave=[AltraProduzione]]} l'elemento esterno sia appaiato
	 * con l'ultima {@code ]} esterna e non con la prima incontrata.
	 * @throws IllegalArgumentException se {@code production} non ha alcuna {@code ]} corrispondente dopo {@code openingBracketIndex}
	 */
	private int findClosingBracket(String production, int openingBracketIndex) {
		int depth = 1;
		for (int index = openingBracketIndex + 1; index < production.length(); index++) {
			char c = production.charAt(index);
			if (c == OPENING_BRACKET.charAt(0)) {
				depth++;
			} else if (c == CLOSING_BRACKET.charAt(0)) {
				depth--;
				if (depth == 0) {
					return index;
				}
			}
		}
		throw new IllegalArgumentException("Missing '" + CLOSING_BRACKET + "' element after token " + production);
	}

	/**
	 * Risolve il contenuto di un singolo [elemento], passando al gestore appropriato
	 * in base al suo marcatore ({@code *}, {@code !}, {@code =} o {@code #}).
	 */
	private String resolveToken(String tokenBody, Map<String, List<WeightedAlternative>> productionsMap, Map<String, String> superFixedProductions, Map<String, String> localFixedProductions) {
		TokenWithDefault withDefault = splitDefault(tokenBody);
		if (withDefault.fallback != null) {
			String resolved = resolveTokenOrNull(withDefault.name, productionsMap, superFixedProductions, localFixedProductions);
			if (resolved != null) {
				return resolved;
			}
			return produceImpl(withDefault.fallback, productionsMap, localFixedProductions, withDefault.name + " (default)").trim();
		}
		if (tokenBody.startsWith(GLOBAL_FIXED_PRODUCTION_MARKER)) {
			String name = tokenBody.substring(GLOBAL_FIXED_PRODUCTION_MARKER.length());
			return resolveFixedProduction(name, globalFixedProductions, productionsMap, superFixedProductions);
		}
		if (tokenBody.startsWith(LOCAL_FIXED_PRODUCTION_MARKER)) {
			String name = tokenBody.substring(LOCAL_FIXED_PRODUCTION_MARKER.length());
			return resolveFixedProduction(name, localFixedProductions, productionsMap, superFixedProductions);
		}
		if (tokenBody.indexOf(ASSIGNMENT_MARKER) >= 0) {
			assignFixedProduction(tokenBody, productionsMap, superFixedProductions);
			return "";
		}
		if (tokenBody.startsWith(REFERENCE_MARKER)) {
			return resolveReference(tokenBody, localFixedProductions);
		}
		return resolvePlainProduction(tokenBody, productionsMap, localFixedProductions);
	}

	/**
	 * Risolve un riferimento come fa {@link #resolveToken}, ma risponde {@code null}
	 * invece di lanciare un'eccezione quando non c'è alcun valore disponibile, così che il chiamante possa ripiegare sul
	 * predefinito dell'elemento. "Nessun valore disponibile" copre volutamente tutti i motivi insieme:
	 * <ul>
	 *     <li>il nome non è mai stato dichiarato come produzione e non è mai stato assegnato;</li>
	 *     <li>è una chiave che qualche {@code [chiave=valore]} assegna, ma quell'assegnazione non è ancora
	 *     stata eseguita a questo punto dell'espansione;</li>
	 *     <li>nomina una produzione monouso le cui alternative sono state tutte consumate, dato che
	 *     {@code productionsMap} qui è la {@link #currentProductionsMap} potata e una produzione
	 *     esaurita ne è stata rimossa.</li>
	 * </ul>
	 * Un elemento di assegnazione non arriva mai a questo metodo: {@link #findDefaultSeparator} si rifiuta di
	 * vedere un predefinito in un elemento che assegna.
	 */
	private String resolveTokenOrNull(String tokenBody, Map<String, List<WeightedAlternative>> productionsMap, Map<String, String> superFixedProductions, Map<String, String> localFixedProductions) {
		if (tokenBody.startsWith(GLOBAL_FIXED_PRODUCTION_MARKER)) {
			return resolveFixedProductionOrNull(tokenBody.substring(GLOBAL_FIXED_PRODUCTION_MARKER.length()),
					globalFixedProductions, productionsMap, superFixedProductions);
		}
		if (tokenBody.startsWith(LOCAL_FIXED_PRODUCTION_MARKER)) {
			return resolveFixedProductionOrNull(tokenBody.substring(LOCAL_FIXED_PRODUCTION_MARKER.length()),
					localFixedProductions, productionsMap, superFixedProductions);
		}
		if (tokenBody.startsWith(REFERENCE_MARKER)) {
			String key = tokenBody.substring(REFERENCE_MARKER.length());
			String value = localFixedProductions.get(key);
			return value != null ? value : globalFixedProductions.get(key);
		}
		if (!productionsMap.containsKey(tokenBody)) {
			return null;
		}
		return resolvePlainProduction(tokenBody, productionsMap, localFixedProductions);
	}

	/**
	 * La controparte nullable di {@link #resolveFixedProduction}: prima la cache, poi la
	 * mappa delle produzioni, poi {@code null}.
	 */
	private String resolveFixedProductionOrNull(String name, Map<String, String> fixedProductionsCache, Map<String, List<WeightedAlternative>> productionsMap, Map<String, String> superFixedProductions) {
		String cached = fixedProductionsCache.get(name);
		if (cached != null) {
			return cached;
		}
		if (!productionsMap.containsKey(name)) {
			return null;
		}
		return resolveFixedProduction(name, fixedProductionsCache, productionsMap, superFixedProductions);
	}

	/**
	 * Risolve una produzione il cui risultato è messo in cache (globalmente o localmente, a seconda di
	 * {@code fixedProductionsCache}) così che i riferimenti successivi allo stesso nome riusino
	 * il valore generato la prima volta. Come {@link #resolvePlainProduction}, il risultato viene
	 * ripulito dagli spazi ai bordi: ci si aspetta che ogni produzione produca una stringa senza spazi ai bordi.
	 */
	private String resolveFixedProduction(String name, Map<String, String> fixedProductionsCache, Map<String, List<WeightedAlternative>> productionsMap, Map<String, String> superFixedProductions) {
		String cached = fixedProductionsCache.get(name);
		if (cached != null) {
			return cached;
		}
		List<WeightedAlternative> productions = productionsMap.get(name);
		if (productions == null) {
			throw new IllegalArgumentException(PRODUCTION + name + " is empty!");
		}
		String result = produceImpl(getProduction(productionsMap, name), productionsMap, superFixedProductions, name).trim();
		fixedProductionsCache.put(name, result);
		return result;
	}

	/**
	 * Gestisce un elemento {@code [chiave=valore]} memorizzando {@code valore} sotto {@code chiave} in
	 * {@link #globalFixedProductions}, per recuperarlo poi con un riferimento {@code [#chiave]}
	 * in qualsiasi punto del testo prodotto (non solo all'interno del sottoalbero di produzione corrente).
	 * Se {@code valore} contiene a sua volta elementi {@code [...]} (ad esempio {@code [chiave=[AltraProduzione]]}),
	 * questi sono risolti completamente prima che il risultato sia memorizzato, così che il valore in cache sia sempre
	 * testo semplice.
	 */
	private void assignFixedProduction(String tokenBody, Map<String, List<WeightedAlternative>> productionsMap, Map<String, String> superFixedProductions) {
		int equalsPosition = tokenBody.indexOf(ASSIGNMENT_MARKER);
		String key = tokenBody.substring(0, equalsPosition);
		String value = tokenBody.substring(equalsPosition + ASSIGNMENT_MARKER.length());
		String resolvedValue = produceImpl(value, productionsMap, superFixedProductions, key);
		globalFixedProductions.put(key, resolvedValue);
	}

	/**
	 * Gestisce un elemento {@code [#chiave]} cercando il valore assegnato in precedenza a
	 * {@code chiave}: prima in {@code localFixedProductions} (che un {@code [!chiave]} nello
	 * stesso frame di chiamata, o in uno qualsiasi che lo racchiuda nello stesso sottoalbero, può aver
	 * popolato), poi in {@link #globalFixedProductions} (che
	 * {@link #assignFixedProduction} popola).
	 * @throws IllegalArgumentException se {@code key} non è mai stata assegnata
	 */
	private String resolveReference(String tokenBody, Map<String, String> localFixedProductions) {
		String key = tokenBody.substring(REFERENCE_MARKER.length());
		String value = localFixedProductions.get(key);
		if (value == null) {
			value = globalFixedProductions.get(key);
		}
		if (value == null) {
			throw new IllegalArgumentException(PRODUCTION + tokenBody + " not yet defined!");
		}
		return value;
	}

	/**
	 * Risolve un semplice riferimento {@code [Nome]}: sceglie un'alternativa di {@code name}
	 * (vedi {@link #getProduction}) e la espande ricorsivamente. Il risultato viene ripulito dagli spazi ai bordi: ci si aspetta che ogni
	 * produzione produca una stringa senza spazi ai bordi, così che ad esempio la foglia scelta da un gruppo di alternative in linea,
	 * se vuota, non lasci uno spazio spurio dovuto al testo letterale
	 * che circonda il riferimento a essa.
	 * @throws IllegalArgumentException se {@code name} non è una produzione (attualmente disponibile)
	 */
	private String resolvePlainProduction(String name, Map<String, List<WeightedAlternative>> productionsMap, Map<String, String> localFixedProductions) {
		List<WeightedAlternative> productions = productionsMap.get(name);
		if (productions == null) {
			throw new IllegalArgumentException(PRODUCTION + name + " is empty!");
		}
		return produceImpl(getProduction(productionsMap, name), productionsMap, localFixedProductions, name).trim();
	}

	/**
	 * Sceglie un'alternativa di {@code production} da {@code localProductionsMap}, secondo
	 * {@link #productionMode}: sempre la prima, sempre l'ultima, o una scelta a caso pesata
	 * (vedi {@link #pickWeighted}, che pesa ogni alternativa col suo {@link WeightedAlternative#weight}
	 * così che ad esempio un'alternativa di peso 10 sia scelta dieci volte più spesso di una col peso predefinito
	 * di 1). Se {@code production} è monouso, l'alternativa scelta (e, se era
	 * l'ultima, l'intera produzione, a cascata tramite {@link #removeProduction}) viene rimossa, così che non
	 * possa essere scelta di nuovo nello stesso ciclo di produzione.
	 */
	private String getProduction(Map<String, List<WeightedAlternative>> localProductionsMap, String production) {
		if (isStaticName(production)) {
			synchronized (STATIC_LOCK) {
				return getStaticProduction(production);
			}
		}
		List<WeightedAlternative> productions = localProductionsMap.get(production);
		WeightedAlternative chosen = pick(productions);
		if (oneShotProductions.contains(production)) {
			productions.remove(chosen);
			if (productions.isEmpty()) {
				removeProduction(localProductionsMap, production);
			}
		}
		return chosen.text;
	}

	/**
	 * Sceglie un'alternativa secondo {@link #productionMode}: la prima, l'ultima, o una scelta a caso pesata.
	 */
	private WeightedAlternative pick(List<WeightedAlternative> productions) {
		switch (productionMode) {
			case PRIMO:
				return productions.get(0);
			case ULTIMO:
				return productions.get(productions.size() - 1);
			default:
				return pickWeighted(productions);
		}
	}

	/**
	 * Sceglie a caso un'alternativa fra {@code productions}, pesando ciascuna col suo
	 * peso (eventualmente frazionario) {@link WeightedAlternative#weight}: si somma il peso totale, si estrae un
	 * valore casuale in {@code [0, totalWeight)}, e si restituisce l'alternativa il cui intervallo di
	 * peso cumulato lo contiene. Un'alternativa col peso predefinito di 1 è scelta
	 * esattamente tanto spesso quanto lo sarebbe con una semplice selezione uniforme, quindi una grammatica senza
	 * alcun elemento {@code [^N]} si comporta in modo identico a prima che questo metodo esistesse.
	 */
	private WeightedAlternative pickWeighted(List<WeightedAlternative> productions) {
		double totalWeight = 0;
		for (WeightedAlternative alternative : productions) {
			totalWeight += alternative.weight;
		}
		double r = rnd.nextDouble() * totalWeight;
		double cumulativeWeight = 0;
		for (WeightedAlternative alternative : productions) {
			cumulativeWeight += alternative.weight;
			if (r < cumulativeWeight) {
				return alternative;
			}
		}
		return productions.get(productions.size() - 1);
	}

	/**
	 * Rimuove da {@code localProductionsMap} una produzione monouso esaurita, e procede a cascata:
	 * toglie ogni riferimento {@code [production]}/{@code [*production]}/{@code [!production]} a essa
	 * dalle alternative di ogni altra produzione, rimuovendo ricorsivamente ogni produzione che
	 * resti così senza alternative proprie.
	 */
	private void removeProduction(Map<String, List<WeightedAlternative>> localProductionsMap, String production) {
		localProductionsMap.remove(production);
		String plainReference = OPENING_BRACKET + production + CLOSING_BRACKET;
		String globalReference = OPENING_BRACKET + GLOBAL_FIXED_PRODUCTION_MARKER + production + CLOSING_BRACKET;
		String localReference = OPENING_BRACKET + LOCAL_FIXED_PRODUCTION_MARKER + production + CLOSING_BRACKET;
		List<String> productionsToRemove = new ArrayList<>();
		for (Map.Entry<String, List<WeightedAlternative>> entry : localProductionsMap.entrySet()) {
			List<WeightedAlternative> options = entry.getValue();
			options.removeIf(option -> option.text.contains(plainReference) || option.text.contains(globalReference) || option.text.contains(localReference));
			if (options.isEmpty()) {
				productionsToRemove.add(entry.getKey());
			}
		}
		for (String productionToRemove : productionsToRemove) {
			removeProduction(localProductionsMap, productionToRemove);
		}
	}

	/**
	 * Applica ogni sostituzione {@code pre:post} di {@link #postProductions} al testo
	 * completamente espanso, correggendo i problemi di lingua naturale nati dalla concatenazione
	 * meccanica delle alternative di produzione, poi riduce a un solo spazio ogni sequenza di due o più
	 * spazi e/o tab rimasta (tipicamente per un'alternativa volutamente vuota), e infine toglie
	 * ogni spazio o tab rimasto subito prima di un segno di punteggiatura
	 * (tipicamente lasciato quando un riferimento proprio a fine frase si espande
	 * in una stringa vuota).
	 * <p>
	 * Ogni escape {@code \[}/{@code \]} scritto dentro un tratto letterale grezzo viene prima riconvertito in una
	 * vera parentesi, prima che le sostituzioni siano eseguite: ogni elemento è già stato espanso
	 * a quel punto, quindi una parentesi ripristinata qui non può più essere scambiata per un riferimento, e le
	 * regole di post-produzione e la ripulitura degli spazi vedono entrambe il testo finale.
	 * <p>
	 * Le regole sono applicate nell'ordine in cui compaiono nel file di post-produzione, così che un file che elenchi
	 * un {@code pre} più lungo prima di uno più corto che ne è il prefisso ottenga prima la corrispondenza più lunga.
	 * <p>
	 * Ogni regola è applicata ripetutamente finché il suo {@code pre} non compare più, riesaminando
	 * il testo dall'inizio ogni volta, così che le occorrenze sovrapposte
	 * siano tutte riscritte. Termina perché {@link #setPostProductionFile} rifiuta
	 * ogni regola il cui {@code post} contenga il proprio {@code pre}: senza questa garanzia una
	 * regola continuerebbe a trovare il testo che aveva appena scritto.
	 */
	private String postProduce(String intermediateProduction) {
		intermediateProduction = intermediateProduction
				.replace(LITERAL_OPENING_BRACKET_PLACEHOLDER, OPENING_BRACKET.charAt(0))
				.replace(LITERAL_CLOSING_BRACKET_PLACEHOLDER, CLOSING_BRACKET.charAt(0));
		for (Map.Entry<String, String> entry : postProductions.entrySet()) {
			String pre = entry.getKey();
			String post = entry.getValue();
			int pos;
			while ((pos = intermediateProduction.indexOf(pre)) >= 0) {
				StringBuilder sb = new StringBuilder(intermediateProduction.substring(0, pos)).append(post);
				int l = pos + pre.length();
				if (l < intermediateProduction.length()) {
					sb.append(intermediateProduction.substring(l));
				}
				intermediateProduction = sb.toString();
			}
		}
		intermediateProduction = intermediateProduction.replaceAll(MULTIPLE_SPACES_REGEX, " ");
		return intermediateProduction.replaceAll(SPACE_BEFORE_PUNCTUATION_REGEX, "");
	}

	public static class InvalidGrammarException extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidGrammarException(String error) {
			super(error);
		}
	}
}
