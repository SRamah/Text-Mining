/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textemining.Recheche;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import static textemining.BoyerMoore.match;
import textemining.Corpus;
import textemining.Recherche;
import textemining.TexteMining;

/**
 *
 * @author salaheddine
 */
public class QuickSearch {
    
    //Cherche les documents qui contient exactement la requete
    public List<String> checkMatching(TexteMining TM, List<String> resultats, String pattern){
        try {
            HashMap<String,Integer> matching = new HashMap<String,Integer>();
            List<String> AddToEnd = new ArrayList<String>();
            for(String path : resultats){
                String rows = TM.ReadFile(path);
                int Size = match(pattern, rows).size();
                if (Size==0) {
                    AddToEnd.add(path+" >> "+Size);
                }
                else {
                    //System.out.println("matching : "+path+" >> "+ Size);
                    matching.put(path, Size);
                }
                
                //System.out.println("doc = "+path+" \tnbr = "+matching.get(path));
                
                /*StringTokenizer st = new StringTokenizer(rows, ".\n;,:؛،");
                while (st.hasMoreTokens()) {
                String str = st.nextToken();
                List<Integer> matches = match(pattern, str);
                }*/
            }
            List<String> selected = new ArrayList<String>();
            
            for(String j : matching.keySet()){
                int val = matching.get(j);
                String emp = j;
                for (String i : matching.keySet()) {
                    if (matching.get(i)>val && !selected.contains(i)) {
                        val = matching.get(i);
                        emp = i;
                    }
                    
                }
                
                selected.add(emp);
                
                
            }
            selected.addAll(AddToEnd);
            
            
            return selected;
            
            
            
        } catch (Exception e) {
            System.out.println("prb  at checkMatching!!!");
        }
        return null;
    }

    
    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        
        long debut = System.currentTimeMillis();
        
        QuickSearch QS = new QuickSearch();
        
        //choisir la langue à utiliser ( "fr", "en" ou "ar" )!!! :
        String langue = "ar";
        String train_path = "./exemples/EASC-UTF-8";
        double seuil = 1.0; //seuil de reduction pour CHISQUARE
        /////////////////////////////////////////////////////////
        
        TexteMining TM = new TexteMining(langue);
        
        //Apprentissage
        Corpus corp = new Corpus();
        corp = TM.Training(langue, train_path, seuil);
        
        LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MDP = TM.getMatricePoid(corp, langue);
        
        
        /*========================================= Recherche ============================================*/
        
        LinkedHashMap<Integer, String> Docpaths = (LinkedHashMap<Integer, String>) TM.GetObject("./"+langue+"/Docpaths.LinkedHashMap_Integer_String");
        String Querry = "سامسونج انجنيرنج الكورية الجنوبية";
        Recherche R = new Recherche(Docpaths,TM.getUniqueWord());
        LinkedHashMap<Integer, Double> dt = TM.getNbrDocTermeInCorpus();
        //LinkedHashMap<Integer, Double> dt = (LinkedHashMap<Integer, Double>) GetObject("./"+langue+"/NbrDocTermeInCorpus.LinkedHashMap_Integer_Double");
        long rechtime = System.currentTimeMillis();
        List<String> resultats = R.Training(MDP, dt, Querry, langue);
        System.out.println("\n\nResultat de la recherche  :  "+Querry+"\n=========================================");
        System.out.println("resultats = "+resultats.size()+"\n");
        int LimAffiche = 0;
        for (String i : QS.checkMatching(TM, resultats, Querry)) {
            LimAffiche++;
            System.out.println("\t"+i);
            if (LimAffiche>=50) {
                System.out.println("\t......");
                break;
            }
        }
        System.out.println("\nle temps de la recherche est : "+(System.currentTimeMillis()-rechtime)+"(ms)");
        
        /*================================================================================================*/
        
        System.out.println("\nle temps d'exécution est : "+(System.currentTimeMillis()-debut)+"(ms)");
    }
}
