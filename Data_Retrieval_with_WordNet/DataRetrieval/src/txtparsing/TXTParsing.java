package txtparsing;

import utils.IO;
import java.util.ArrayList;
import java.util.List;

//Διαβάσει το αρχείο προσπαθόντας να τα σπάσει σε docs(κείμενα)
//τα σπάει σε τόσα τμήματα,όσα έχει η class MyDocs
public class TXTParsing {

    //επιστρέφει λίστα από List<MyDoc>
    public static List<MyDoc> parse(String file) throws Exception {
        try{
            //Parse txt file
            //μέσο φακέλου Utils,η class IO,για να διαβάσει όλο το αρχείο
            String txt_file = IO.ReadEntireFileIntoAString(file);
            //το κάνω split με βάση το /// και \s+
            //\s+: whitespace  //https://javarevisited.blogspot.com/2016/10/how-to-split-string-in-java-by-whitespace-or-tabs.html
            String[] docs = txt_file.split("///\\s+");

            System.out.println("Read: "+docs.length + " docs");

            //Parse each document from the txt file
            List<MyDoc> parsed_docs= new ArrayList<MyDoc>();
            for (String doc:docs){
                //για να πάρω το DocID (πρώτη σειρά)
                String[] adoc = doc.split("\n",2);
                //για να πάρω ξεχωριστά το τίτλο από το κείμενο
                String[] textDoc = adoc[1].split(":",2);

                //το mydoc πέρνει τα 3 πεδία από adoc που θέλει η ΜyDoc class
                MyDoc mydoc = new MyDoc(adoc[0],textDoc[0],textDoc[1]);

                parsed_docs.add(mydoc);
            }

            //επιστρέγονται όλα τα αντικείμενα τύπου myDoc
            return parsed_docs;
        } catch (Throwable err) {
            err.printStackTrace();
            return null;
        }
        
    }

}
