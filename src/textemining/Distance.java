/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textemining;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author salaheddine
 */
public class Distance {
    
    public double distanceEuclidienne(List<Double> X, List<Double> Y){
        double d=0.0;
        for (int i = 0; i < X.size(); i++) {
            d = d + Math.pow((X.get(i).doubleValue()-Y.get(i).doubleValue()),2);
            
        }
        
        return Math.sqrt(d);
    }
    
    public double distanceEuclidienne2(List<Integer> X, List<Integer> Y){
        double d=0.0;
        for (int i = 0; i < X.size(); i++) {
            d = d + Math.pow((X.get(i).intValue()-Y.get(i).intValue()),2);
            
        }
        
        return Math.sqrt(d);
    }
    
    public List<Double> distanceCentreGravite_SparceMat(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> M, Set<Integer> words, List<Integer> classek){
        TexteMining TM = new TexteMining();
        double som=0;
        List<Double> G =  new ArrayList<Double>();
        
        for (int j = 0; j < words.size(); j++) {
                G.add(0.0);
        }
        int nb=0;
        for (int i :classek){
            nb++;
            
            int ind = 0;
            for (int key : words) {
                som= (M.get(i).containsKey(key))? M.get(i).get(key) + G.get(ind) : G.get(ind) ;
                G.set(ind, som);
                ind++;
            }
            
        }
        
        int ind = 0;
        for (int key : words) {
            G.set(ind, ((double)G.get(ind)/(nb+0.0)));
            ind++;
        }
        
        return G;
    }
    
    public List<Double> distanceCentreGravite(List<List<Double>> M, List<Integer> classek){
        double som=0;
        List<Double> G =  new ArrayList<Double>();
        
        for (int j = 0; j < M.get(0).size(); j++) {
                G.add(0.0);
        }
        int nb=0;
        for (int i :classek){
            nb++;
            for (int j = 0; j < M.get(i).size(); j++) {
                som=G.get(j) + M.get(i).get(j);
                G.set(j, som);
            }
            
        }
        
        for (int j = 0; j < M.get(0).size(); j++) {
               G.set(j, ((double)G.get(j)/(nb+0.0)));
        }
        
        return G;
    }
    
    public List<List<Double>> matriceDistance(List<List<Double>> M){
        List<List<Double>> MD = new ArrayList<List<Double>>();
        List<Double> temp;
        for (int i = 0; i < M.size()-1; i++) {
            temp = new ArrayList<Double>();
            for (int j = i+1; j < M.size(); j++) {
                //System.out.println(i+" <==> "+j+" --> "+distanceEuclidienne(M.get(i),M.get(j)));
                temp.add(distanceEuclidienne(M.get(i),M.get(j)));
            }
            MD.add(temp);
            
        }
        /*         d1 : d2 d3 d4 ...
            MD <=> d2 : d3 d4 ...
                   d3 : d4 ...
        */
        
        return MD;
    }
    
    
    public double distanceClassMinimal(List<List<Double>> MD, List<Integer> classe1, List<Integer> classe2){
        double min=Double.MAX_VALUE,temp;
        
        for (int i : classe1) {
            for (int j : classe2) {
                if (i<j) {
                    temp=MD.get(i).get(j);
                    if (min>temp) {
                        min=temp;
                    }
                }else{
                    temp=MD.get(j).get(i);
                    if (min>temp) {
                        min=temp;
                    }
                }
            }
        }
        return min;
        
    }
  
    public double distanceClassMaximal(List<List<Double>> MD, List<Integer> classe1, List<Integer> classe2){
        double max=0,temp;
        for (int i : classe1) {
            for (int j : classe2) {
                if (i<j) {
                    temp=MD.get(i).get(j);
                    if (max<temp) {
                        max=temp;
                    }
                }else{
                    temp=MD.get(j).get(i);
                    if (max<temp) {
                        max=temp;
                    }
                }
            }
        }
        return max;
        
    }
    
    public double distanceClassMoyenne(List<List<Double>> M, List<Integer> classe1, List<Integer> classe2){
        double avg=0;
        for (int i : classe1) {
            for (int j : classe2) {
                if (i<j) {
                    avg=avg + M.get(i).get(j);
                    
                }else{
                    avg=avg + M.get(j).get(i);
                    
                }
            }
        }
        return avg/((classe1.size())*(classe2.size()));
        
    }
    
    public double distanceWard(List<List<Double>> M, List<Integer> classe1, List<Integer> classe2){
        double res=0;
        
        res=Math.pow(distanceEuclidienne(distanceCentreGravite(M, classe1),distanceCentreGravite(M, classe2)),2);
        res=res*((classe1.size())*(classe2.size()))/((classe1.size())+(classe2.size()));
        return res;
    }
    
    
    
    
}
