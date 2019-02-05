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
public class Corpus implements Serializable {
    //Corpus
    ArrayList<ClasseDoc> CorpusFiles;
    //Calculer le nombre total des docs qui contient le terme dans le Corpus
    int TempDt = 0;
    
    //Recupérer le Corpus
    public ArrayList<ClasseDoc> getCorpusFiles() {
        return CorpusFiles;
    }
    //Modifier le Corpus
    public void setCorpusFiles(ArrayList<ClasseDoc> CorpusFiles) {
        this.CorpusFiles = CorpusFiles;
    }
    
    //Return le nombre des classe dans le Corpus
    public int getsize(){
        return this.CorpusFiles.size();
    }
    
    //Recupérer le nombre total des docs dans le Corpus
    public int nbrTotalDoc(){
        int total=0;
        for (int i = 0; i < this.CorpusFiles.size(); i++) {
            total = total + this.CorpusFiles.get(i).getTaille();
        }
        return total;
    }
    
    //Calculer le nombre total des docs qui contient le terme hashWord dans le Corpus et return leur poids associer
    public double getPoid(int hashWord, LinkedHashMap<Integer, Integer> hm){
        
        int nbrDoc = nbrTotalDoc();
        double freTermeDoc = (0.0+(double)hm.get(hashWord))/((double)hm.size());
        double dt = 0.0;
        for (int i = 0; i < CorpusFiles.size(); i++) {
            
            dt=dt+this.CorpusFiles.get(i).termDoc(hashWord);
            
        }
        
        double resultat = Math.log(((double)nbrDoc)/dt)*freTermeDoc ;
        TempDt=(int)dt;
        return resultat;
        
    }
    
    //Calcule le nombre des documents qui contient le terme hashWord et qui n'appartient pas à une classe donnée
    public int nbrDocNotInClass(int classe, Integer hashWord){
        
        int ni=0;
        for (int j = 0; j < getsize(); j++) {

            if (j!=classe) {
                ClasseDoc classTemp=this.CorpusFiles.get(j);
                //le nombre des documents qui contient le terme "hashWord" dans la classe j
                ni=ni+classTemp.termDoc(hashWord);
            }
        }
            
        
        return ni;
    }
   
    //recupérer l'intervalle des indexs des documents dans chaque classe
    public LinkedHashMap<Integer, List<Integer>> getIndexOfDocsInClass(){
        LinkedHashMap<Integer, List<Integer>> Classes = new LinkedHashMap<Integer, List<Integer>>();
        int xt=0, t;
        List<Integer> l;
        for (int i = 0; i < getsize(); i++) {
            t=CorpusFiles.get(i).getTaille();
            l = new ArrayList<Integer>();
            l.add(xt);
            l.add(t+xt-1);
            Classes.put(i, l);
            xt=t;
        }
        System.out.println("\n"+Classes);
        return Classes;
    }
    
    
}
