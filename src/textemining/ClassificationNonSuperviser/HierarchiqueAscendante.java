/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textemining.ClassificationNonSuperviser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
public class HierarchiqueAscendante {
    /*
    ** CURE (Clustering Using Representatives):
    */
    
    public List<List<Integer>> HierarchiqueAsc(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MatricePoid, Set<Integer> words, int nbr_classe){
        Distance D = new Distance();
        
        //Initialisation
        List<List<Integer>> listeIndexClasse = new ArrayList<List<Integer>>();
        nbr_classe=(nbr_classe<1)? 1 : nbr_classe;
        for (int i = 0; i < MatricePoid.size(); i++) {
            ArrayList<Integer> temp = new ArrayList<Integer>();
            temp.add(i);
            listeIndexClasse.add(temp);
        }
        
        
        System.out.println("\n\nPartionnement Initiale = "+listeIndexClasse+"\n");
        
        
        //Partionnement
        return Partionnement(MatricePoid, words, listeIndexClasse, nbr_classe);
    }
    
    public List<List<Integer>> Partionnement(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MatricePoid, Set<Integer> words, List<List<Integer>> listeIndexClasse, int nbr_classe){
        Distance D = new Distance();
        //Etape affectation
        List<List<Double>> Centres = new ArrayList<List<Double>>();
        
        
        for (int i = 0; i < listeIndexClasse.size(); i++) {
            //System.out.println("\tlisteIndexClasse.get("+i+")="+listeIndexClasse.get(i));
            //Calcule de centre de chaque classe Ck
            Centres.add(D.distanceCentreGravite_SparceMat(MatricePoid, words, listeIndexClasse.get(i))); 
        }
        
        /*
        /////////////////////////
        
        for (int i = 0; i < Centres.size(); i++) {
            System.out.println("Centres "+i+" : "+Centres.get(i));
        }
        
        /////////////////////////
        */
        double dist_min = Double.MAX_VALUE;
        int C1=-1,C2=-1;
        for (int i = 0; i < Centres.size(); i++) {
            List<Double> Min = new ArrayList<Double>();
            for (int j = i+1; j < Centres.size(); j++) {
                //System.out.println("Avant listeIndexClasse.get("+j+") = "+listeIndexClasse.get(j));
                double min = D.distanceEuclidienne(Centres.get(i), Centres.get(j) );
                if (dist_min>min) {
                    C1=i;
                    C2=j;
                    dist_min=min;
                }                
            }   
        }
        
        for (int c : listeIndexClasse.get(C1)) {
            listeIndexClasse.get(C2).add(c);
        }
        listeIndexClasse.remove(C1);
        
        
        System.out.println("New Partitionnement : "+listeIndexClasse);
        
        if (nbr_classe!=listeIndexClasse.size()) {
                Partionnement(MatricePoid, words, listeIndexClasse, nbr_classe);
        }
        return listeIndexClasse;
        
            
        
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
        
        HierarchiqueAscendante HA = new HierarchiqueAscendante();
        
        //choisir la langue à utiliser ( "fr", "en" ou "ar" )!!! :
        String langue = "fr";
        String train_path = "./exemples/classification_non_superviser";
        double seuil = 1.0; //seuil de reduction pour CHISQUARE
        int nbr_classe= 2;  //nombre de classe proposer
        /////////////////////////////////////////////////////////
        
        TexteMining TM = new TexteMining(langue);
        
        //Apprentissage
        Corpus corp = new Corpus();
        corp = TM.Training(langue, train_path, seuil);
        
        
        /*=============================== Classification non superviser ==================================*/
        
        //Récupérer la matrice des poids :
        LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MP = TM.getMatricePoid(corp, langue);
        
        //Classification non superviser : HierarchiqueAsc
        List<List<Integer>> Prediction = HA.HierarchiqueAsc(MP, TM.getReducedUniqueWords().keySet(), nbr_classe);
        
        
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
            System.out.println("HierarchiqueAsc "+i+" : "+Prediction.get(i));
        }
        
        /*================================================================================================*/
     
        
    }
    
}
