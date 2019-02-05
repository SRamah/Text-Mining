/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textemining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import static java.util.Objects.hash;
import java.util.Set;
import java.util.StringTokenizer;
import khoja.ArabicStemmerKhoja;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.frenchStemmer;

/**
 *
 * @author salaheddine
 */
public class Recherche {
    
    LinkedHashMap<Integer, String> Docpaths = new LinkedHashMap();
    LinkedHashMap<Integer, String> uniqueWord = new LinkedHashMap();
    
    public Recherche(LinkedHashMap<Integer, String> Docpaths, LinkedHashMap<Integer, String> uniqueWord){
        this.Docpaths=Docpaths;
        this.uniqueWord=uniqueWord;
    }
    
    //Fonction principale pour lancer la recherche
    public List<String> Training(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MDP, LinkedHashMap<Integer, Double> dt, String request, String langue) throws IOException{
        TexteMining TM = new TexteMining();
        LinkedHashMap<Integer, Integer> stopwords;
        stopwords= TM.LoadStopWords(langue);
        String separateurs = "  , . ; : ! ? ( ) { } [ ] \\ > < + * - _ = / % \n  & ' $ @ £ \"";
        if(hash(langue.toLowerCase())==hash("ar")){
            separateurs = "  = . , «  » ، / ; ! { } \\ [ ] \" - _ ( ) > < + : × * ÷ % \n & ^ ' $ @ £ 0 1 2 3 4 5 6 7 8 9  ؛  ـ  ؟";
        }
        
        LinkedHashMap<Integer, Integer> WordsQuerry = FastProcessingRequest(dt, request, separateurs, stopwords, langue);
        LinkedHashMap<Integer,List<Double>> Mini_MDP = getMiniMDP(MDP, WordsQuerry.keySet());
        
        return searchMotor(Mini_MDP, dt, WordsQuerry);
        
        
    }
    
    //Reduire la Matrice de poids (intersection des mots unique du corpus avec les mots de la requete !!)
    public LinkedHashMap<Integer,List<Double>> getMiniMDP(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MDP, Set<Integer> HWQ ){
        LinkedHashMap<Integer,List<Double>> MiniMDP = new LinkedHashMap<Integer,List<Double>>();
        //System.out.println("\n\n\tMiniMDP");
        for (int i = 0; i < MDP.size(); i++) {
            List<Double> liste = new ArrayList<Double>();
            double sum = 0.0;
            for (int k : HWQ ) {
                double temp;
                if (MDP.get(i).get(k)!=null) {
                    temp = MDP.get(i).get(k);
                }
                else temp = 0.0;
                
                sum=sum+temp;
                liste.add(temp);
            }
            if (sum!=0.0) {
                 MiniMDP.put(i, liste);
                 //System.out.println(i+"  :  "+liste);
            }
           
        }
        
        return MiniMDP;
    
    }
    
    //Vactoriser la requette
    public LinkedHashMap<Integer, Integer> FastProcessingRequest(LinkedHashMap<Integer, Double> dt, String request , String separateur, LinkedHashMap<Integer, Integer> StopWords, String langue){
       
       LinkedHashMap<Integer, Integer> hm = new LinkedHashMap<Integer, Integer>();
       
		try {
                        StringTokenizer st = new StringTokenizer(request, separateur);
                        
                        if(hash(langue.toLowerCase()) == hash("fr")){
                            frenchStemmer getRacine = new frenchStemmer();
                                    while (st.hasMoreTokens()) {
                                        String str = st.nextToken();
                                        Integer hashStr = hash(str.toLowerCase());
                                        if(StopWords.get(hashStr) == null){
                                            
                                            getRacine.setCurrent(str.toLowerCase());
                                            if(getRacine.stem()){
                                                String tempStr = ""+getRacine.getCurrent();
                                                //list.add(tempStr);
                                                Integer hashWord = hash(tempStr);
                                                if (dt.get(hashWord)!=null) {
                                                    if(hm.get(hashWord) == null)
                                                        hm.put(hashWord, 1);
                                                    else hm.put(hashWord, hm.get(hashWord)+1);
                                                }
                                            }
                                        }
                                    }
                        }
                        else if(hash(langue.toLowerCase()) == hash("en")){
                            englishStemmer getRacine = new englishStemmer();
                                    while (st.hasMoreTokens()) {
                                        String str = st.nextToken();
                                        Integer hashStr = hash(str.toLowerCase());
                                        if(StopWords.get(hashStr) == null){
                                            
                                            getRacine.setCurrent(str.toLowerCase());
                                            if(getRacine.stem()){
                                                String tempStr = ""+getRacine.getCurrent();
                                                //list.add(tempStr);
                                                Integer hashWord = hash(tempStr);
                                                if (dt.get(hashWord)!=null) {
                                                    if(hm.get(hashWord) == null)
                                                        hm.put(hashWord, 1);
                                                    else hm.put(hashWord, hm.get(hashWord)+1);
                                                }
                                            }
                                        }
                                    }
                        }
                        else if(hash(langue.toLowerCase()) == hash("ar")){
                            ArabicStemmerKhoja getRacine = new ArabicStemmerKhoja();
                                    while (st.hasMoreTokens()) {
                                        String str = st.nextToken();
                                        Integer hashStr = hash(str.toLowerCase());
                                        if(StopWords.get(hashStr) == null){
                                            
                                                String tempStr = ""+getRacine.stem(str.toLowerCase());
                                                //list.add(tempStr);
                                                Integer hashWord = hash(tempStr);
                                                if (dt.get(hashWord)!=null) {
                                                    if(hm.get(hashWord) == null)
                                                        hm.put(hashWord, 1);
                                                    else hm.put(hashWord, hm.get(hashWord)+1);
                                                }
                                                
                                            
                                        }
                                    }
                        }
                        
		}
		catch(Exception e) {
			System.out.println("Erreur : " + e.getMessage());
		}
                
                //System.out.println("\n\nSize of the HashMap: "+hm.size());
                
        return hm;
   }
    
    //Rechercher les docs qui contients les termes de la requete
    public List<String> searchMotor(LinkedHashMap<Integer,List<Double>> Mini_MDP, LinkedHashMap<Integer, Double> dt, LinkedHashMap<Integer, Integer> WordsQuerry) throws IOException{
        TexteMining TM = new TexteMining();
        //LinkedHashMap<Integer, List<Integer>> Mstat = TM.StatMatrix(corp, (new ArrayList<Integer>(WordsQuerry.keySet()) ));
        System.out.println("");
        for (int hw : WordsQuerry.keySet()) {
            System.out.print(hw+" >> "+uniqueWord.get(hw)+"\t\t");
        }
        System.out.println("\nWordsQuerry :\n "+WordsQuerry);
        LinkedHashMap<Integer,Double> Max = new LinkedHashMap<Integer,Double>();
        
         
        List<Double> QuerryVectorPoid = getPoid(WordsQuerry, Docpaths.size(), dt);
        
        for (int i : Mini_MDP.keySet()){
            double max = sim(QuerryVectorPoid, Mini_MDP.get(i));
            Max.put(i,max);
        }
        return getOrder(Max);
    }
    
    //Classer les resultats de la recherche (le plus important en premier ainsi de suite ... 
    public List<String> getOrder(LinkedHashMap<Integer,Double> Max){
        List<Integer> selected = new ArrayList<Integer>();
        List<String> resultats = new ArrayList<String>();
        //System.out.print("Scores :  [");
        for(int j : Max.keySet()){
            double val = Double.MIN_VALUE;
            int emp = -1;
            for (int i : Max.keySet()) {
                if (Max.get(i)>val && !selected.contains(i)) {
                    val = Max.get(i);
                    emp = i;
                }
            }
            //System.out.print(val+"  ");
            selected.add(emp);
            resultats.add(Docpaths.get(emp));
        }
        //System.out.print(" ]");
        return resultats;
    }
    
    //Calculer la Similarité entre deux Vecteurs
    public double sim(List<Double> X, List<Double> Y){
        double d1=0.0,s=0.0, d2=0.0;
        //System.out.println("X = "+X);
        //System.out.println("Y = "+Y);
        for (int i = 0; i < X.size(); i++) {
           s = s + ((X.get(i).doubleValue())*(Y.get(i).doubleValue()));
            d1 = d1 + Math.pow((X.get(i).doubleValue()),2);
            d2 = d2 + Math.pow((Y.get(i).doubleValue()),2);
            
        }
        
        return s/(Math.sqrt(d1) + Math.sqrt(d2));
    }
    
    //Calculer le poids des termes de la requete
    public List<Double> getPoid(LinkedHashMap<Integer, Integer> hm, int nbrDocCorp, LinkedHashMap<Integer, Double> dt){
        List<Double> QuerryVectorPoid = new ArrayList<Double>();
        for (int hashWord : hm.keySet()) {
            double freTermeDoc = (0.0+(double)hm.get(hashWord))/((double)hm.size());
            double resultat = Math.log(((double)nbrDocCorp)/dt.get(hashWord).doubleValue())*freTermeDoc ;
            QuerryVectorPoid.add(resultat);
        }
        
        return QuerryVectorPoid;
        
    }
    
    
}
