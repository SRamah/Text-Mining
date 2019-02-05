/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textemining.ClassificationSuperviser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import static java.util.Objects.hash;
import textemining.Corpus;
import textemining.DiskFileExplorer;
import textemining.TexteMining;
import static textemining.TexteMining.GetObject;
import static textemining.TexteMining.SetObject;

/**
 *
 * @author salaheddine
 */
public class NB {
    
    public Corpus Training(TexteMining TM, String langue, ArrayList<ArrayList<String>> rep, double seuil) throws IOException, FileNotFoundException, ClassNotFoundException{
        long debut = System.currentTimeMillis();
        if (new File("./"+langue+"/corp.Corpus").exists()) {
            System.out.println("\nle temps de chargement de l'apprentissage est : "+(System.currentTimeMillis()-debut)+"(ms)\n");
            TM.setReducedUniqueWords((LinkedHashMap<Integer, String>) GetObject("./"+langue+"/reduceUniqueWord"+seuil+".LinkedHashMap_Integer_String"));
            return (Corpus) GetObject("./"+langue+"/corp.Corpus");
        }
        else{
            LinkedHashMap<Integer, Integer> stopwords;
            stopwords= TM.LoadStopWords(langue);
            System.out.println("StopWords loaded");

            String separateurs = "  , . ; : ! ? ( ) { } [ ] \\ > < + * - _ = / % \n  & ' $ @ £ \"";
            if(hash(langue.toLowerCase())==hash("ar")){
                separateurs = "  = . , «  » ، / ; ! { } \\ [ ] \" - _ ( ) > < + : × * ÷ % \n & ^ ' $ @ £ 0 1 2 3 4 5 6 7 8 9  ؛  ـ  ؟";
            }
            System.out.println("Separateurs loaded");

            Corpus corp = new Corpus();
            
            TM.SaveDocsPath(rep, langue);
            System.out.println("\nApprentissage ...");
            corp.setCorpusFiles(TM.AllClass(rep, separateurs, stopwords, langue, true));
            System.out.println("Fin de l'apprentissage");
            System.out.println("\nle temps de l'apprentissage est : "+(System.currentTimeMillis()-debut)+"(ms)\n");
            SetObject(corp, "./"+langue+"/corp.Corpus");
            System.out.println(" ... Saved");
            System.out.println("\nReducing Dimensions ...");
            SetObject(TM.getUniqueWord(),"./"+langue+"/uniqueWord.LinkedHashMap_Integer_String");
            TM.setReducedUniqueWords(TM.reduceDimension(corp, seuil, langue));

            return corp;
        }
    }
    
    public String Testing(TexteMining TM, String langue, Corpus corp, String path, LinkedHashMap<Integer, String> reduceUniqueWord ) throws IOException{
        long debut = System.currentTimeMillis();
        
        LinkedHashMap<Integer, Integer> stopwords;
        stopwords= TM.LoadStopWords(langue);
        
        
        String separateurs = "  , . ; : ! ? ( ) { } [ ] \\ > < + * - _ = / % \n  & ' $ @ £ \"";
        if(hash(langue.toLowerCase())==hash("ar")){
            separateurs = "  = . , «  » ، / ; ! { } \\ [ ] \" - _ ( ) > < + : × * ÷ % \n & ^ ' $ @ £ 0 1 2 3 4 5 6 7 8 9  ؛  ـ  ؟";
        }
        System.out.println("\nTesting ...");
        String resul = TM.ScanNewDoc(corp, path, separateurs, stopwords, langue, reduceUniqueWord);
        System.out.println("\nle temps du test est : "+(System.currentTimeMillis()-debut)+"(ms)\n");
        return resul;
    }
    
    public ArrayList<ArrayList<String>> cleanRep(String train_path, String langue) throws IOException{
        DiskFileExplorer dfe = new DiskFileExplorer(train_path,true);
        ArrayList<ArrayList<String>> rep = new ArrayList<>(dfe.list());
        for (int i = 0; i < rep.size(); i++) {
            if(rep.get(i).size()>1){
                String paths = rep.get(i).get(0);
                for (int j = 1; j < rep.get(i).size(); j++) {
                    String f = rep.get(i).get(j);
                    if (!f.startsWith(".")) {
                        
                    }
                    else rep.get(i).remove(j);
                }
                if(rep.get(i).size()==1) rep.remove(i);
            }
        }
        //System.out.println("rep \n"+rep);
        SetObject(rep,"./"+langue+"/rep_ArrayList_ArrayList_String");
        return rep;
        
    }
    
    public ArrayList<ArrayList<ArrayList<String>>> CrossValidation(ArrayList<ArrayList<String>> rep, String langue) throws IOException, FileNotFoundException, ClassNotFoundException{
        //System.out.println("rep = "+rep);
        ArrayList<ArrayList<ArrayList<String>>> trRep = new ArrayList<ArrayList<ArrayList<String>>>();
        ArrayList<String> valRep = new ArrayList<String>();
        
        
        
        for (int i = 0; i < rep.size(); i++) {
            for (int j = 1; j < rep.get(i).size(); j++) {
                ArrayList<ArrayList<String>> temprep = (ArrayList<ArrayList<String>>) GetObject("./"+langue+"/rep_ArrayList_ArrayList_String");
                temprep.get(i).remove(j);
                trRep.add(temprep);
            }
        }
        /*for (int i = 0; i < trRep.size(); i++) {
        System.out.println("trRep = "+trRep.get(i));
        }*/
        return trRep;
    }
    
    static public void deleteDirectory( String emplacement )
    {
        File path = new File( emplacement );
            if( path.exists() )
            {
                File[] files = path.listFiles();
                for( int i = 0 ; i < files.length ; i++ )
                {
                    if( files[ i ].isDirectory() )
                    {
                      deleteDirectory( path+"\\"+files[ i ] );
                    }
                    files[ i ].delete();
                }
            }
    }
    
    public void trainCV(String train_path, String langue, double seuil) throws IOException, FileNotFoundException, ClassNotFoundException{
        
        ArrayList<ArrayList<String>> rep = cleanRep(train_path, langue);
        ArrayList<ArrayList<ArrayList<String>>> trRep = CrossValidation(rep, langue);
        ArrayList<ArrayList<String>> Results = new ArrayList<ArrayList<String>>();
        ArrayList<String> UResults = new ArrayList<String>();
        ArrayList<String> RResults = new ArrayList<String>();
        ArrayList<String> Target = new ArrayList<String>();
        String path = "", test_path= "";
        LinkedHashMap<String, Integer> UHM = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Integer> RHM = new LinkedHashMap<String, Integer>();
        int nb = 0;
        
        for (int i = 0; i < rep.size(); i++) {
            path = rep.get(i).get(0);
            String classeName = new File(path).getName();
            for (int j = 1; j < rep.get(i).size(); j++) {
                deleteDirectory("./"+langue);
                TexteMining TM = new TexteMining(langue);
                Corpus corp = Training(TM, langue, trRep.get(nb), seuil);
                test_path = path+"/"+rep.get(i).get(j);
                UResults.add(Testing(TM, langue, corp, test_path, TM.getUniqueWord()));
                RResults.add(Testing(TM, langue, corp, test_path, TM.reduceDimension(corp, seuil, langue)));
                Target.add(classeName);
                RHM.put(RResults.get(nb), (RHM.containsKey(RResults.get(nb))) ? RHM.get(RResults.get(nb))+1 : 1);
                UHM.put(UResults.get(nb), (UHM.containsKey(UResults.get(nb))) ? UHM.get(UResults.get(nb))+1 : 1);
                nb++;
                System.out.println("\n==============================================================================\n");
            }
            
        }
        Results.add(Target);
        Results.add(UResults);
        Results.add(RResults);
        System.out.println("UResults = "+UResults);
        System.out.println("RResults = "+RResults);
        System.out.println("Target   = "+Target);
        
        int ind = 0;
        double FM=0.0;
        for (int i = 0; i < rep.size(); i++) {
            int S=UHM.get(new File(rep.get(i).get(0)).getName()), RS=0, R=rep.get(i).size()-1;
            
            for (int j = ind; j < R+ind; j++) {
                if (UResults.get(j).equals(Target.get(j))) {
                    RS++;
                }
            }
            double Prec = (S!=0) ? (RS/(S+0.0)) : 0.0;
            double Rap = RS/(R+0.0);
            double F_Mesure = (2*Prec*Rap)/(Prec+Rap);
            FM=FM+F_Mesure;
            System.out.println("____________________________ Unique _________________________________");
            System.out.println("Prec ("+new File(rep.get(i).get(0)).getName()+") = "+Prec);
            System.out.println("Rap ("+new File(rep.get(i).get(0)).getName()+") = "+Rap);
            System.out.println("F_Mesure ("+new File(rep.get(i).get(0)).getName()+") = "+F_Mesure);
            

            ind=R;
        }
        
        
        
        int indi = 0;
        double FMr=0.0;
        for (int i = 0; i < rep.size(); i++) {
        int S=RHM.get(new File(rep.get(i).get(0)).getName()), RS=0, R=rep.get(i).size()-1;
        
        for (int j = indi; j < R+indi; j++) {
        if (RResults.get(j).equals(Target.get(j))) {
        RS++;
        }
        }
        double Prec = (S!=0) ? (RS/(S+0.0)) : 0.0;
        double Rap = RS/(R+0.0);
        double F_Mesure = (2*Prec*Rap)/(Prec+Rap);
        FMr=FMr+F_Mesure;
        System.out.println("______________________________ Reduce ________________________________");
        System.out.println("Prec ("+new File(rep.get(i).get(0)).getName()+") = "+Prec);
        System.out.println("Rap ("+new File(rep.get(i).get(0)).getName()+") = "+Rap);
        System.out.println("F_Mesure ("+new File(rep.get(i).get(0)).getName()+") = "+F_Mesure);
        
        
        indi=R;
        }
        System.out.println("\n/////////// U //////////////\nF Mesure tot = "+FM/rep.size());
        System.out.println("\n//////////// R /////////////\nF Mesure tot = "+FMr/rep.size());
        
        
    }
    
    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        
        long debut = System.currentTimeMillis();
        
        NB naivebayes = new NB();
        
        //choisir la langue à utiliser ( "fr", "en" ou "ar" )!!! :
        String langue = "ar";
        String train_path = "./exemples/langue-ar";
        String test_path = "./exemples/finance (1).txt";
        double seuil = 1.0; //seuil de reduction pour CHISQUARE
        /////////////////////////////////////////////////////////
        
        //la validation croiśee:
        naivebayes.trainCV(train_path, langue, seuil);
        
        

        
        
        /*=============================== Classification superviser (N.B) =================================*/
        
        /*//Apprentissage
        TexteMining TM = new TexteMining(langue);
        Corpus corp = new Corpus();
        ArrayList<ArrayList<String>> rep = naivebayes.cleanRep(train_path, langue);
        corp = naivebayes.Training(TM, langue, rep, seuil);
        
        //Le nombre des mots uniques dans train :
        System.out.println("\nLe nombre des mots uniques dans train :  "+TM.getUniqueWord().size()+"\n");
        
        //Test (voir le fichier csv générer) :
        System.out.println("\nTest avant la reduction de la dimension :\nLe fichier suivant : "+test_path+"\nAppartient à la classe : "+naivebayes.Testing(TM, langue, corp, test_path, TM.getUniqueWord()));
        
        //Estimation d’indépendence entre termes et catégories (CHI-SQUARE):
        //System.out.println("\nScore of Terms categorical : "+TM.Reduce(corp).values());
        //Test avec reduce :
        System.out.println("\nTest apres la reduction de la dimension :\nLe fichier suivant : "+test_path+"\nAppartient à la classe : "+naivebayes.Testing(TM, langue, corp, test_path, TM.reduceDimension(corp, 1.0, langue)));
        */
        /*================================================================================================*/
        
        
        System.out.println("\nle temps d'exécution est : "+(System.currentTimeMillis()-debut)+"(ms)");
        
    }
    
}
