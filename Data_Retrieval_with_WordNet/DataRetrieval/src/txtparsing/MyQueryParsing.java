package txtparsing;

import utils.IO;

import java.util.ArrayList;
import java.util.List;

//Diabazei ta arxeia prospa8ontas na ta smasei/apospasei
//ta 2 tmimata poy exoume stin class Query
public class MyQueryParsing {
    public static List<MyQuery> parse(String file) throws Exception {

        try{
            //Parse txt file
            String txt_file0 = IO.ReadEntireFileIntoAString(file); //meso fakelou Utils,i class IO, gia na diabasei ;olo to arxeio

            String txt_file = txt_file0.substring(0,txt_file0.length()-3); //για να βγάλω τα τελαυταια ///,ώστε να γίνεται σωστό split

            //System.out.println(txt_file );

            String[] quers = txt_file.split("///\\s+");//to kano split me basei \n ,giati ka8e keimeno e;inai se ka8e diaforetiki grammi
            //\s+: whitespace  //https://javarevisited.blogspot.com/2016/10/how-to-split-string-in-java-by-whitespace-or-tabs.html

            //στο τελος τελειώνει /// ,άρα αφαιρώ το τελευταιο query,είναι κενό
            //miss code

           // System.out.println("Read: "+quers.length + " quers");

            //Parse each document from the txt file
            List<MyQuery> parsed_quers= new ArrayList<MyQuery>();
            for (String quer:quers){
                String[] adoc = quer.split("\r\n"); //split me basei to / backslash
                MyQuery mydoc = new MyQuery(adoc[0],adoc[1]);//to mydoc pernei ta apotelesmata ta 3(pou 8elei oi class MyDoc) apo to adoc
                parsed_quers.add(mydoc);
            }

            return parsed_quers; //eksodos antikeimena tupou MyDoc
        } catch (Throwable err) {
            err.printStackTrace();
            return null;
        }

    }

}
