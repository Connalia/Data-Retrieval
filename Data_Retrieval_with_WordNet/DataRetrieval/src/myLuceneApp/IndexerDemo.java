package myLuceneApp;

// tested for lucene 7.7.2 and jdk13
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilterFactory;
import org.apache.lucene.analysis.en.PorterStemFilterFactory;
import org.apache.lucene.analysis.standard.ClassicFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.synonym.SynonymGraphFilterFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import txtparsing.*;

/**
 * Creates a lucene's inverted index from an xml file.
 */
public class IndexerDemo {
    
    /**
     * Configures IndexWriter.
     * Creates a lucene's inverted index.
     *
     */
    public IndexerDemo() throws Exception{

        //ορίζω που βρήσκετε
        //txt file to be parsed and indexed, it contains one document per ///
        String txtfile =  "docs//documents.txt";

        //που θα αποθηκευτεί το ευρετήριο
        //στο φάκελο index (που αρχικά είναι άδειος)
        //define were to store the index
        String indexLocation = ("index");
        
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexLocation + "'...");

            //Ορίζω το Directory που θα αποθηκευτεί το ευρετήριο, πχ. RAMDirectory, FSDirectory.
            Directory dir = FSDirectory.open(Paths.get(indexLocation));
            // define which analyzer to use for the normalization of documents
            CustomAnalyzer analyzer = customAnalyzerForQueryExpansion();
            //Συνάρτηση ομοίοτητας, by default αυτή,άμα δεν την είχαμε δηλώσει
            Similarity similarity = new BM25Similarity();
            // configure IndexWriter
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setSimilarity(similarity);

            //.CREATE: αν υπάρχει ήδη ευρετήριο στην τοποθεσία διεγραψέ το
            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(OpenMode.CREATE); //Αν είχα .UPDATE,θα ενημέρωνε ένα ήδη υπάρχων

            //Υπεύθυνος για τη μετατροπή του κειμένου σε internal Lucene format, φορματπου καταλαβαίνει η lucene
            //και Κατασκευή ανεστραμμένου ευρετηρίου
            //δίνουμε την τοποθεσία του ευρετηρίου και το configuration που όρισα στον IndexWriterConfig
            // create the IndexWriter with the configuration as above 
            IndexWriter indexWriter = new IndexWriter(dir, iwc);
            
            // parse txt document using TXT parser and index it
            List<MyDoc> docs = TXTParsing.parse(txtfile);
            //διαβάζει την λιστα και για κάθε doc το βάζει μέσα στο index writer
            for (MyDoc doc : docs){
                indexDoc(indexWriter, doc);
            } //θα τρέξει τόσες φορές όσα και τα κείμενα

            //κλείνω το ευρετήριο
            indexWriter.close();

            //υπολογίζω πόσο χρόνο μου πήρε
            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");
            
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
        
        
    }
    
    /**
     * Creates a Document by adding Fields in it and 
     * indexes the Document with the IndexWriter
     *
     * @param indexWriter the indexWriter that will index Documents
     * @param mydoc the document to be indexed
     *
     */
    private void indexDoc(IndexWriter indexWriter, MyDoc mydoc){
        
        try {
            
            // make a new, empty document
            Document doc = new Document();

            //stored, ένα πεδίο η τιμή του οποίου αποθηκεύεται χωρίς να αναλυθεί στο ευρετήριο, για απλή ανάκτηση
            //απλή ανάκτηση
            // create the fields of the document and add them to the document
            StoredField docID = new StoredField("docID", mydoc.getDocID());
            doc.add(docID);
            StoredField title = new StoredField("title", mydoc.getTitle());
            doc.add(title);
            StoredField text = new StoredField("text", mydoc.getText());
            doc.add(text);

            /**η αναζήτηση θα γίνει ώς εξής //ΔΕΝ πρεπει να περιλαμβάνει το docID */
            //concat σε ένα string
            String fullSearchableText = mydoc.getTitle() + " " + mydoc.getText();
            //TextField: θα μπει στο ευρετήριο και θα γίνει και tokenzation και analyze
            //Field.Store.NO: δεν θα αποθηκευτεί γιατί έχω τα πάνω πεδία ήδη αποθηκευμένα,θα τα ανκτήση αμα χρειαστεί
            TextField contents = new TextField("contents", fullSearchableText, Field.Store.NO);
            doc.add(contents);
            
            if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE) {
                // New index, so we just add the document (no old document can be there):
                System.out.println("adding " + mydoc);
                indexWriter.addDocument(doc);
            } 
        } catch(Exception e){
            e.printStackTrace();
        }
        
    }

    private static CustomAnalyzer customAnalyzerForQueryExpansion() throws IOException {
        //Read synonyms from wn_s.pl file //wn_s.pl : αρχειο με συνωνυμους όρους
        Map<String, String> sffargs = new HashMap<>();
        sffargs.put("synonyms", "wn_s.pl");
        sffargs.put("format", "wordnet"); //το αρχειο είναι τυπου Wordnet,δηλαδη κάντω Parse σαν wordnet,όπως ξέρεις Lucene

        //    Create custom analyzer for analyzing query text.
        //    Custom analyzer should analyze query text like the EnglishAnalyzer and have
        //    an extra filter for finding the synonyms of each token from the Map sffargs
        //    and add them to the query. //English analyzer απο πηγαιο κωδικα της Lucene
        CustomAnalyzer.Builder builder = CustomAnalyzer.builder()
                .withTokenizer(WhitespaceTokenizerFactory.class)
                .addTokenFilter(StandardFilterFactory.class) //πιθανον να αφαιρεθει???????????
                .addTokenFilter(EnglishPossessiveFilterFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .addTokenFilter(PorterStemFilterFactory.class);
                //μεχρι εδώ ότι έχει ο English analyzer


        CustomAnalyzer analyzer = builder.build();
        return analyzer;
    }
    /**
     * Initializes an IndexerDemo
     */
    public static void main(String[] args) {
        try {
            IndexerDemo indexerDemo = new IndexerDemo();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
