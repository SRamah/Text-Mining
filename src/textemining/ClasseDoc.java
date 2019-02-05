/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textemining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author salaheddine
 */
public class ClasseDoc implements Serializable{
    
    //La classe des documents
    ArrayList<LinkedHashMap<Integer, Integer>> Classe;
    //Le nom du dossier qui contients les docs
    String classeName;
    //Nombre des mots dans la classe
    int nbrWordsClass;

    //Recupérer le nom de la classe
    public String getClasseName() {
        return classeName;
    }

    //Modifier le nom de la classe
    public void setClasseName(String classeName) {
        this.classeName = classeName;
    }

    //Recupérer la classe des documents
    public ArrayList<LinkedHashMap<Integer, Integer>> getClasse() {
        return Classe;
    }

    //Modifier la classe des documents
    public void setClasse(ArrayList<LinkedHashMap<Integer, Integer>> Class) {
        this.Classe = Class;
    }
    
    //Return le nombre des docs dans la classe
    public int getTaille() {
        return Classe.size();
    }
    
    //Calculer le nombre des docs qui contient le terme dans la classe
    public int termDoc(int hashWord){
        int td=0;
        for (int i = 0; i < getTaille(); i++) {
            if(this.Classe.get(i).get(hashWord)!= null)
            {
                td++;
            }
        }
        return td;
    
    }
    
    //Calcule le nombre des mots dans la classe et return un LinkedHashMap des docs et leur vecteur assosier d'occurence (pour construire la matrice d'occurence) 
    public LinkedHashMap<Integer, ArrayList<Integer>> wordsInfo(ArrayList<Integer> selectedWord){
        LinkedHashMap<Integer, ArrayList<Integer>> vector = new LinkedHashMap<>();
        this.nbrWordsClass=0;
        for (int i = 0; i < getTaille(); i++) {
            ArrayList<Integer> value = new ArrayList<>();
            for (int j = 0; j < selectedWord.size(); j++) {
                Integer V = this.Classe.get(i).get(selectedWord.get(j));
                if(V != null){
                    this.nbrWordsClass = this.nbrWordsClass + V.intValue();
                    value.add(V);
                }else value.add(0);
            }
            vector.put(i, value);
        }
        return vector;
    }

    //Calculer le poids d'un terme
    public void getPoid(int hashWord, LinkedHashMap<Integer, Integer> hm){
        
        int nbrDoc = this.Classe.size();
        double refTermeDoc = (0.0+(double)hm.get(hashWord))/((double)hm.size());
        double dt = 0.0;
        for (int i = 0; i < nbrDoc; i++) {
            if(this.Classe.get(i).get(hashWord)!= null)
            {
                dt++;
            }
        }
        
        double resultat = Math.log(((double)nbrDoc)/dt)*refTermeDoc ;
        //return resultat;
        
    }
    
    
}
