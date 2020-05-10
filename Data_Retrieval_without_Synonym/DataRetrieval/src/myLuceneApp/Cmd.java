package myLuceneApp;

import txtparsing.MyQuery;
import txtparsing.MyQueryParsing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class Cmd {
    //https://www.geeksforgeeks.org/java-program-open-command-prompt-insert-commands/
    public static void main(String[] args) throws Exception {
        try
        {


            int [] numberRetrieval = {20 , 30 , 50};//πρώτα συναφεί με το ερώτημα
            int max=50;

            String output="";
            for(int k:numberRetrieval){

                ////https://www-nlpir.nist.gov/projects/trecvid/trecvid.tools/trec_eval_video/A.README
                //-q: In addition to summary evaluation, give evaluation for each query
                if(k!=max){
                    output +=  "&& trec_eval -m map -m num_rel_ret -q qrels.txt my_results_file"+ k +".txt";
                }else{
                    output +=  "&& trec_eval -m map -m num_rel_ret -q -m map_cut qrels.txt my_results_file"+ k +".txt";
                }

                //put result of trec_eval for all
                output +=  "&& trec_eval -m map -m num_rel_ret qrels.txt my_results_file"+ k +".txt > eval" + k + ".txt";
            }

            // We are running "dir" and "ping" command on cmd
            Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd trec_eval " +
                        output +
//                     "&& trec_eval -m map -m num_rel_ret qrels.txt my_results_file"+ 20 +".txt > eval" + 20 + ".txt" +
//                     "&& trec_eval -m map -m num_rel_ret qrels.txt my_results_file"+ 30 +".txt > eval" + 30 + ".txt" +
//                     "&& trec_eval -m map -m num_rel_ret qrels.txt my_results_file"+ 50 +".txt > eval" + 50 + ".txt" +
                       "\"");

        }
        catch (Exception e)
        {
            System.out.println("HEY Buddy ! U r Doing Something Wrong ");
            e.printStackTrace();
        }

    }

}
