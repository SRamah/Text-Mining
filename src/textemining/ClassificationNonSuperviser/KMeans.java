/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textemining.ClassificationNonSuperviser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import textemining.Corpus;
import textemining.Distance;
import textemining.TexteMining;

/**
 *
 * @author salaheddine
 */
public class KMeans {
    
    public List<List<Integer>> CentreMobile(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MatricePoid, Set<Integer> words, int nbr_classe){
        Distance D = new Distance();
        
        //Initialisation
        List<List<Integer>> listeIndexClasse = new ArrayList<List<Integer>>();
        
        
        for (int i = 0; i < nbr_classe; i++) {
            listeIndexClasse.add(new ArrayList<Integer>());
        }
        
        List<Integer> centres = BestCenters(MatricePoid, words, nbr_classe);
        for (int i = 0; i < nbr_classe; i++) {
            listeIndexClasse.get(i).add(centres.get(i));
        }
        
        
        /*for (int i = 0; i < MatricePoid.size(); i++) {
        int l = (int) (Math.random()*nbr_classe);
        listeIndexClasse.get(l).add(i);
        }*/
        System.out.println("\n\nPartionnement Initiale = "+listeIndexClasse+"\n");
        
        //List<List<Double>> MD = D.matriceDistance(MatricePoid);
        //System.out.println("MD\n"+MD);
        
        //Partionnement
        List<List<Integer>> changement = listeIndexClasse;
        return Partionnement(MatricePoid, words, listeIndexClasse, changement, nbr_classe);
    }
    
    //Recupérer les k centre des clusters les plus éloigner dans le corpus 
    public List<Integer> BestCenters(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MatricePoid, Set<Integer> words, int nbr_classe){
        Distance D = new Distance();
        List<Integer> centres = new ArrayList<Integer>();
        
        //1) Trouver deux documents les plus éloigner 
        double temp,distanceMax = Double.MIN_VALUE;
        int docA=0, docB=0;
        for (int i = 0; i < MatricePoid.size(); i++) {
            for (int j = i+1; j < MatricePoid.size(); j++) {
                temp = D.distanceEuclidienne(getDocVectorFromSparceMP(MatricePoid, words, i), getDocVectorFromSparceMP(MatricePoid, words, j));
                //System.out.println("La distance entre doc"+i+" et doc"+j+" est : "+temp);
                if (distanceMax<temp) {
                    docB=j;
                    docA=i;
                    distanceMax = temp;
                }
            }
        }
        centres.add(docA);
        centres.add(docB);
        //System.out.println("La distance maximal est entre doc"+docA+" et doc"+docB+" ==> "+distanceMax);
        
        //2)
        while(centres.size()!=nbr_classe){
            LinkedHashMap<Integer,Double> dists = new LinkedHashMap<Integer,Double>();
            for (int j = 0; j < MatricePoid.size(); j++) {
                temp=0.0;
                for (int c : centres) {
                    if (c!=j) {
                        temp = temp + D.distanceEuclidienne( getDocVectorFromSparceMP(MatricePoid, words, j), getDocVectorFromSparceMP(MatricePoid, words, c));
                    }
                }
                dists.put(j, temp/centres.size());
            }
            double tt, max = Double.MIN_VALUE;
            int emp=0;
            for (int i : dists.keySet()) {
                tt=dists.get(i);
                if(max<tt){
                    max=tt;
                    emp=i;
                }
            }
            centres.add(emp);
        }
        
        System.out.println("Les centres sont : "+centres);
        return centres;
    
    }
    
    public List<List<Integer>> Partionnement(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MatricePoid, Set<Integer> words, List<List<Integer>> listeIndexClasse, List<List<Integer>> changement, int nbr_classe){
        Distance D = new Distance();
        //Etape affectation
        List<List<Double>> Centres = new ArrayList<List<Double>>();
        List<List<Integer>> NewListeIndexClasse = new ArrayList<List<Integer>>();
         
        for (int i = 0; i < nbr_classe; i++) {
            //System.out.println("\tlisteIndexClasse.get("+i+")="+listeIndexClasse.get(i));
            //Calcule de centre de chaque classe Ck
            Centres.add(D.distanceCentreGravite_SparceMat(MatricePoid, words, listeIndexClasse.get(i))); 
            //Generation d'une nouvelle partition
            NewListeIndexClasse.add(new ArrayList<Integer>());
            
        }
        
        /*
        /////////////////////////
        
        for (int i = 0; i < Centres.size(); i++) {
            System.out.println("Centres "+i+" : "+Centres.get(i));
        }
        
        /////////////////////////
        */
        
        for (int i = 0; i < MatricePoid.size(); i++) {
            List<Double> Min = new ArrayList<Double>();
            for (int j = 0; j < Centres.size(); j++) {
                //System.out.println("Avant listeIndexClasse.get("+j+") = "+listeIndexClasse.get(j));
                double min = D.distanceEuclidienne(getDocVectorFromSparceMP(MatricePoid, words, i), getCentres(MatricePoid, words, listeIndexClasse.get(j), -i, Centres.get(j)) );
                Min.add(min);
                //System.out.println("Apres listeIndexClasse.get("+j+") = "+listeIndexClasse.get(j));
            }
            //System.out.println("la distance entre le doc "+i+" et les "+nbr_classe+" classes est : "+Min+" min => "+getMin(Min));
            int newl= getMin(Min);
            NewListeIndexClasse.get(newl).add(i);
        }
        System.out.println("New Partitionnement : "+NewListeIndexClasse);
        
        List<List<Integer>> Newchangement = getChangement(NewListeIndexClasse, listeIndexClasse);
        if (ConditionArrete(NewListeIndexClasse, listeIndexClasse)) {
            if (ConditionArrete(Newchangement, changement)) {
                Partionnement(MatricePoid, words, NewListeIndexClasse, Newchangement, nbr_classe);
            }
            
        }
        return NewListeIndexClasse;
        
            
        
    }
    
    public List<Double> getCentres(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MatricePoid, Set<Integer> words, List<Integer> IndexClasses, Integer Di, List<Double> Centre){
        Distance D = new Distance();
        List<Integer> newIndexClasses = new ArrayList<Integer>(IndexClasses);
        if(newIndexClasses.remove(Di)){
            return D.distanceCentreGravite_SparceMat(MatricePoid, words, newIndexClasses);
        }
        else return Centre;
    }
    
    public List<List<Integer>> getChangement(List<List<Integer>> PartitionInitiale, List<List<Integer>> Partition){
        List<List<Integer>> change  = new  ArrayList<List<Integer>>();
        for (int i = 0; i < PartitionInitiale.size(); i++) {
            List<Integer> temp = new ArrayList<Integer>();
            HashMap<Integer,Integer> hmap = new HashMap<Integer,Integer>();
            for (int j = 0; j < PartitionInitiale.get(i).size(); j++) {
                hmap.put(PartitionInitiale.get(i).get(j), 1);
            }
            for (int j = 0; j < Partition.get(i).size(); j++) {
                int val = Partition.get(i).get(j);
                if (hmap.get(val)==null) {
                    temp.add(val);
                }
                else{
                    hmap.remove(val);
                }
            }
            temp.addAll(hmap.keySet());
            change.add(temp);
            
        }
        
        
        return change;
    }
    
    public boolean ConditionArrete(List<List<Integer>> PartitionInitiale, List<List<Integer>> Partition){
        int t=0;
        for (int i = 0; i < PartitionInitiale.size(); i++) {
            if(PartitionInitiale.get(i).containsAll(Partition.get(i))){
                t++;
            }else if(PartitionInitiale.contains(Partition.get(i))){
                t++;
            }
            
        }
        if (t==PartitionInitiale.size()) {
            return false;
        }
        else return true;
    }
    
    public int getMin(List<Double> Index){
        
                double min=Double.MAX_VALUE;
                int emp=Index.size()-1;
                for (int j = 0; j < Index.size(); j++) {
                    if (min>Index.get(j)) {
                        min=Index.get(j);
                        emp=j;
                    }
                }
        return emp;
    }
    
    public int getMax(List<Double> Index){
        
                double max=Double.MIN_VALUE;
                int emp=Index.size()-1;
                for (int j = 0; j < Index.size(); j++) {
                    if (max<Index.get(j)) {
                        max=Index.get(j);
                        emp=j;
                    }
                }
        return emp;
    }
    
    public List<Double> getDocVectorFromSparceMP(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> SparceMat, Set<Integer> words, int ligne){
        List<Double> DocVector = new ArrayList<Double>();
        for (int key : words) {
            DocVector.add( (SparceMat.get(ligne).containsKey(key))? SparceMat.get(ligne).get(key) : 0.0 );
        }
        //System.out.println("DocVector = "+DocVector);
        return DocVector;
        
    }
    
    
    
    
    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        long debut = System.currentTimeMillis();
        
        KMeans KM = new KMeans();
        
        //choisir la langue à utiliser ( "fr", "en" ou "ar" )!!! :
        String langue = "ar";
        String train_path = "./exemples/langue-ar";
        double seuil = 1.0; //seuil de reduction pour CHISQUARE
        int nbr_classe= 7;  //nombre de classe proposer
        /////////////////////////////////////////////////////////
        
        TexteMining TM = new TexteMining(langue);
        
        //Apprentissage
        Corpus corp = new Corpus();
        corp = TM.Training(langue, train_path, seuil);
        
        
        /*=============================== Classification non superviser ==================================*/
        
        //Récupérer la matrice des poids :
        LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MP = TM.getMatricePoid(corp, langue);
        
        //Classification non superviser : KMeans
        List<List<Integer>> Prediction = KM.CentreMobile(MP, TM.getReducedUniqueWords().keySet(), nbr_classe);
        
        
        /*
        ** Afficher les resultas de la classification.
        */
        LinkedHashMap<String, Integer> className ;
        for (int i = 0; i < Prediction.size(); i++) {
            className = new LinkedHashMap<String, Integer>();
            String Names="";
            String filesName="";
            for (int j = 0; j < Prediction.get(i).size(); j++) {
                String[] nn = TM.getNamesFiles().get(Prediction.get(i).get(j)).split("[ _.-0123456789]", 2);
                filesName=filesName+TM.getNamesFiles().get(Prediction.get(i).get(j))+"\n\t\t";
                if (className.get(nn[0])==null) {
                    className.put(nn[0],1);
                }else{
                    className.put(nn[0],className.get(nn[0])+1);
                }
                
            }
            //predire le nom de la classe finale :
            int nbb=0;
            for (String j : className.keySet()) {
                if(className.get(j)>nbb){
                    nbb=className.get(j);
                    Names=j;
                }
            }
            System.out.println("Le nom de la classe "+i+" sera : "+Names+"\n\t ou les documents sont : \n\t\t"+filesName);
            
        }
        
        for (int i = 0; i < Prediction.size(); i++) {
            System.out.println("CentreMobile "+i+" : "+Prediction.get(i));
        }
        
        /*================================================================================================*/
        
        
        
    }
}
