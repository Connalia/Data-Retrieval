package myLuceneApp;

// tested for lucene 7.7.2 and jdk13
import java.io.*;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import txtparsing.MyQuery;
import txtparsing.MyQueryParsing;

public class SearcherDemo {
    
    public SearcherDemo(){
        try{
            String indexLocation = ("index"); //define where the index is stored

            //το πεδίο που θα γίνει η αναζήτηση
            //contents είπαμε ότι έχει 2 πεδία, χωρίς το docID
            String field = "contents"; //define which field will be searched            
            
            //Access the index using indexReaderFSDirectory.open(Paths.get(index))
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation))); //IndexReader is an abstract class, providing an interface for accessing an index.
            IndexSearcher indexSearcher = new IndexSearcher(indexReader); //Creates a searcher searching the provided index, Implements search over a single IndexReader.
            indexSearcher.setSimilarity(new BM25Similarity());
            
            //Search the index using indexSearcher
            search(indexSearcher, field);
            
            //Close indexReader
            indexReader.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Searches the index given a specific user query.
     */
    private void search(IndexSearcher indexSearcher, String field){
        try{
            // define which analyzer to use for the normalization of user's query
            Analyzer analyzer = new EnglishAnalyzer();
            
            // create a query parser on the field "contents"
            QueryParser parser = new QueryParser(field, analyzer);

            //read query from query.txt
            String txtQuer =  "docs//queries.txt";
            // parse txt document using TXT parser and index it
            List<MyQuery> quers = MyQueryParsing.parse(txtQuer);
//            for (MyQuery quer : quers){
//                System.out.println(quer);
//            }

            int [] numberRetrieval = {20 , 30 , 50};//πρώτα συναφεί με το ερώτημα
            for(int k:numberRetrieval){

                System.out.println("##################################");
                System.out.println("First: "+ k+" with high score\n");

                Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("my_results_file"+k+".txt"), "utf-8"));

                int numQ=0;
                for (MyQuery quer : quers){

                    numQ++;

                    String q_id;
                    if(numQ < 10){
                        q_id = "Q0"+quer.getQueryID();
                        //System.out.println(q_id);
                    }else{
                        q_id = "Q"+quer.getQueryID();
                        //System.out.println(q_id);
                    }

                    //Writer writerQ = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("my_results_file"+k + q_id +".txt"), "utf-8"));



                    String line=quer.getQueryText();

                    // parse the query according to QueryParser
                    Query query = parser.parse(line);
                    System.out.println("Searching for: " + query.toString(field));

                    // search the index using the indexSearcher
                    //τα k πρώτα συναφεί με το ερώτημα
                    TopDocs results = indexSearcher.search(query, k);
                    ScoreDoc[] hits = results.scoreDocs;
                    long numTotalHits = results.totalHits;
                    System.out.println(numTotalHits + " total matching documents");

                    //display results
                    for(int i=0; i<hits.length; i++){
                        Document hitDoc = indexSearcher.doc(hits[i].doc);
                        //System.out.println("\tScore "+hits[i].score +"\tdocID="+hitDoc.get("docID")+"\ttitle:"+hitDoc.get("title")+"\ttext:"+hitDoc.get("text"));
                    }



                    //Write Result from txt



                    //mot work Integer.parseInt(quer.getQueryID())
                    for(int i=0; i<hits.length; i++){
                        Document hitDoc = indexSearcher.doc(hits[i].doc);

//                        System.out.print("@##@#");
//                        System.out.println(q_id);
//                        System.out.print("!!!!!!!!!");

                        String all= q_id + " " + 0 + " " + hitDoc.get("docID")+ " " + 0 +" " + hits[i].score +" "+" DataRetrieval"+"\n";
                        writer.write(all);


                        //writerQ.write(all);

                    }
                    //writerQ.close();
                }
                writer.close();

            }




/**           // read user's query from stdin
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter query or 'q' to quit: ");
            System.out.print(">>");
            String line = br.readLine(); //του δίνει ερώτημα ο χρήστης

            while(line!=null && !line.equals("") && !line.equalsIgnoreCase("q")){ //q:για να σταμητήση γράφω
                // parse the query according to QueryParser
                Query query = parser.parse(line);
                System.out.println("Searching for: " + query.toString(field));

                // search the index using the indexSearcher
                //τα 100 πρώτα συναφεί με το ερώτημα
                TopDocs results = indexSearcher.search(query, 100);
                ScoreDoc[] hits = results.scoreDocs;
                long numTotalHits = results.totalHits;
                System.out.println(numTotalHits + " total matching documents");

                //display results
                for(int i=0; i<hits.length; i++){
                    Document hitDoc = indexSearcher.doc(hits[i].doc);
                    System.out.println("\tScore "+hits[i].score +"\ttitle="+hitDoc.get("title")+"\tcaption:"+hitDoc.get("caption")+"\tmesh:"+hitDoc.get("mesh"));
                }

                System.out.println("Enter query or 'q' to quit: ");
                System.out.print(">>");
                line = br.readLine();
            }
  */
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Initialize a SearcherDemo
     */
    public static void main(String[] args){
        SearcherDemo searcherDemo = new SearcherDemo();
    }
}
