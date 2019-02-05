/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textemining;


//import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
public class TexteMining {

    /**
     * @param args the command line arguments
     */
    
    //tout les termes unique dans le corpus
    LinkedHashMap<Integer, String> uniqueWord = new LinkedHashMap();
    //les termes unique selectionné par le seuil de CHISQUARE dans le corpus
    LinkedHashMap<Integer, String> ReducedUniqueWords = new LinkedHashMap();
    //Nombre des termes dans chaque Classe
    LinkedHashMap<Integer, Integer> NbrOfWordsPerClass ;
    //Nom des documents
    List<String> namesFiles = new ArrayList<String>();
    //Nombre des documents qui contient le terme dans le corpus
    LinkedHashMap<Integer, Double> NbrDocTermeInCorpus ;
    
    //Construction + Getter + Sitter 
    public TexteMining(){}
    
    public TexteMining(String langue) throws IOException, FileNotFoundException, ClassNotFoundException{
        
        if (new File("./"+langue+"/NbrDocTermeInCorpus.LinkedHashMap_Integer_Double").exists()) {
            this.NbrDocTermeInCorpus=(LinkedHashMap<Integer, Double>) GetObject("./"+langue+"/NbrDocTermeInCorpus.LinkedHashMap_Integer_Double");
        }
        if (new File("./"+langue+"/uniqueWord.LinkedHashMap_Integer_String").exists()) {
            this.uniqueWord=(LinkedHashMap<Integer, String>) GetObject("./"+langue+"/uniqueWord.LinkedHashMap_Integer_String");
        }
        if (new File("./"+langue+"/namesFiles.List_String").exists()) {
            this.namesFiles=(List<String>) GetObject("./"+langue+"/namesFiles.List_String");
        }
    }
    
    public LinkedHashMap<Integer, String> getUniqueWord(){
        return this.uniqueWord;
    }
    
    public LinkedHashMap<Integer, String> getReducedUniqueWords() {
        return this.ReducedUniqueWords;
    }
    
    public void setReducedUniqueWords(LinkedHashMap<Integer, String> ruw) {
        this.ReducedUniqueWords=ruw;
    }
    
    public List<String> getNamesFiles(){
        return this.namesFiles;
    }
    
    public LinkedHashMap<Integer, Double> getNbrDocTermeInCorpus(){
        return this.NbrDocTermeInCorpus;
    }
    
    //Save l'obejts
    public static void SetObject(Object ish, String Name)throws IOException{
        FileOutputStream fos = new FileOutputStream(Name);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(ish);
        oos.close();
    }
    //Recupérer l'obejts
    public static Object GetObject(String Name) throws FileNotFoundException, IOException, ClassNotFoundException{
       FileInputStream fin = new FileInputStream(Name);
       ObjectInputStream ois = new ObjectInputStream(fin);
       Object myObj= (Object) ois.readObject();
       ois.close();
       return myObj;
    }
    //Lire un doc
    public String ReadFile(String path) throws IOException{
        
        String str = new String ();
        str = "";
        
        FileInputStream fis = null;
        FileChannel fc = null;

        try {
            //Création d'un nouveau flux de fichier
            fis = new FileInputStream(new File(path));
            //On récupère le canal
            fc = fis.getChannel();
            //On en déduit la taille
            int size = (int)fc.size();
            //On crée un buffer correspondant à la taille du fichier
            ByteBuffer bBuff = ByteBuffer.allocate(size);
            //Démarrage de la lecture
            fc.read(bBuff);
            //On prépare à la lecture avec l'appel à flip
            String converted = new String(bBuff.array(), "UTF-8");
            str = str +  converted;
            bBuff.flip();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Le fichier choisi est introuvable..... ");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fc != null) fc.close();
            if (fis != null) fis.close();
        }
        
        return str;
        
    }
    //Ecrire un fichier
    public void WriteFile(String path, String content, boolean reset) {
		try {

			File file = new File(path);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
                        else if(!reset) return;

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    //Recupérer les stop word pour une langue (Ar | Fr | En)
    public LinkedHashMap<Integer, Integer> getStopWords(String langue) throws IOException{
        String str = new String ();
        str = ReadFile("src/textemining/StopWords-"+langue.toLowerCase()+".txt");
        
        LinkedList<String> stopList = new LinkedList();
        StringTokenizer st = new StringTokenizer(str,", ");
                                        while (st.hasMoreTokens()) {
                                            stopList.add(st.nextToken());
                                            //System.out.println("Les mots Vides :" + st.nextToken());
                                        
                                        }
        
                                       
        LinkedHashMap<Integer, Integer> hashMap = new LinkedHashMap<Integer, Integer>();                                
        
        String content = ""; 
        
        for (int i = 0; i < stopList.size(); i++) {
            Integer temphash = hash(stopList.get(i).toLowerCase());
            content = content + temphash +" ";
            hashMap.put(temphash,0);
        }
        
        WriteFile("./HashOfStopWords-"+langue.toLowerCase()+".txt", content, true);
	    
        return hashMap;
        
    }
    //Recupérer les Hash des Stop Words
    public LinkedHashMap<Integer, Integer> getHashStopWords(String langue) throws IOException{
        String str = new String ();
        str = ReadFile("./HashOfStopWords-"+langue.toLowerCase()+".txt");
        
        LinkedHashMap<Integer, Integer> hashMap = new LinkedHashMap<Integer, Integer>(); 
        StringTokenizer st = new StringTokenizer(str," ");
                                        while (st.hasMoreTokens()) {
                                            
                                                Integer temphash = Integer.parseInt(st.nextToken());
                                                hashMap.put(temphash, 0);  
                                            
                                            
                                        }
        
            
        return hashMap;
        
    }
    
    //Convertir un doc en Vecteur LinkedHashMap<Integer, Integer> 
    public LinkedHashMap<Integer, Integer> FastProcessingFile(String path , String separateur, LinkedHashMap<Integer, Integer> StopWords, String langue, boolean save){
       
       LinkedHashMap<Integer, Integer> hm = new LinkedHashMap<Integer, Integer>();
       
		try {
                        String rows = ReadFile(path);
			StringTokenizer st = new StringTokenizer(rows, separateur);
                        
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
                                                if(save) uniqueWord.put(hashWord, tempStr);
                                                if(hm.get(hashWord) == null)
                                                    hm.put(hashWord, 1);
                                                else hm.put(hashWord, hm.get(hashWord)+1);
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
                                                if(save) uniqueWord.put(hashWord, tempStr);
                                                if(hm.get(hashWord) == null)
                                                    hm.put(hashWord, 1);
                                                else hm.put(hashWord, hm.get(hashWord)+1);
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
                                                if(save) uniqueWord.put(hashWord, tempStr);
                                                if(hm.get(hashWord) == null)
                                                    hm.put(hashWord, 1);
                                                else hm.put(hashWord, hm.get(hashWord)+1);
                                            
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
    
    //Calculer la matrice de poids 
    public LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> MatrixForClass(Corpus corp, String langue) throws IOException{
        NbrDocTermeInCorpus = new LinkedHashMap<Integer, Double>();
        
        //FileWriter fileWriter = new FileWriter("./"+langue+"/Matrice_de_poid.csv");
        //BufferedWriter bw = new BufferedWriter(fileWriter);
        
        
        //bw.write("Doc/Terme");
        LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> mat = new LinkedHashMap<Integer,LinkedHashMap<Integer,Double>>();
        
        List<Integer> lh = new ArrayList<>(uniqueWord.keySet());
        
        /*for (String s : uniqueWord.values()) {
        
        bw.write(","+s);
        }
        bw.write("\n");*/
        
        int docIndex = 0;
        ArrayList<ClasseDoc> repertoire = corp.getCorpusFiles();
        for (int i = 0; i < repertoire.size(); i++) {
            ClasseDoc Classe = repertoire.get(i);
            ArrayList<LinkedHashMap<Integer, Integer>> Docs = Classe.getClasse();

            //parcourir les docs de la classe
            for (int j = 0; j < Classe.getTaille(); j++) {
                LinkedHashMap<Integer,Double> M_temp = new LinkedHashMap<Integer,Double>();
                int cont=0;
                //bw.write(Classe.getClasseName()+"/"+(j+1));
                while(cont < uniqueWord.size()){
                    LinkedHashMap<Integer, Integer> hm = Docs.get(j);
                    if(hm.get(lh.get(cont)) != null){
                        double poid = corp.getPoid(lh.get(cont), hm);
                        M_temp.put(lh.get(cont),poid);
                        //bw.write(","+poid);

                    }
                    else{ 
                        //bw.write(",0");
                    }
                    
                    if (docIndex==0) {
                        int nbrDoc = corp.nbrTotalDoc();
                        double dt = 0.0;
                        for (int k = 0; k < corp.getsize(); k++) {
                            dt=dt+corp.CorpusFiles.get(k).termDoc(lh.get(cont));
                        }
                        double resultat = Math.log(((double)nbrDoc)/dt);
                        NbrDocTermeInCorpus.put(lh.get(cont), resultat);
                    }
                    cont++;
                }
                //bw.write("\n");
                mat.put(docIndex, M_temp);
                docIndex++;
            }

        }
        System.out.println("Matrice is done !!");
        //bw.close();
        SetObject(NbrDocTermeInCorpus, "./"+langue+"/NbrDocTermeInCorpus.LinkedHashMap_Integer_Double");
        SetObject(mat, "./"+langue+"/MDP.LinkedHashMap_Integer_LinkedHashMap_Integer_Double");
        System.out.println("Save Matrice is done !!");
        return mat;
    }
    
    //Créer le Corpus
    public ArrayList<ClasseDoc> AllClass(ArrayList<ArrayList<String>> rep,  String separateurs, LinkedHashMap<Integer, Integer> stopwords, String langue, boolean save) throws IOException{
        ArrayList<ClasseDoc> Files = new ArrayList();
        
        for (int i = 0; i < rep.size(); i++) {
            if(rep.get(i).size()>1){
                String paths = rep.get(i).get(0);
                String str = new File(paths).getName();
                ClasseDoc C = new ClasseDoc();
                C.setClasseName(str);
                ArrayList<LinkedHashMap<Integer, Integer>> list = new ArrayList<>();

                for (int j = 1; j < rep.get(i).size(); j++) {
                    String f = rep.get(i).get(j);
                    if (!f.startsWith(".")) {
                        LinkedHashMap<Integer, Integer> words = FastProcessingFile(paths+"/"+f, separateurs, stopwords, langue, save);
                        /*System.out.println("Classe  : "+str+" \t doc : "+rep.get(i).get(j));*/
                        //savegarder les nom des fichiers scaner.
                        namesFiles.add(rep.get(i).get(j).toLowerCase());
                        list.add(words);
                    }
                }
                if(list.size()!=0){
                    C.setClasse(list);
                    Files.add(C);
                }
            }
        }
        SetObject(namesFiles,"./"+langue+"/namesFiles.List_String");
        return Files;
    }
    
    //Recupérer la matrice d'occurence de puis le corpus en fonction de mots selectioné par CHISQUARE
    public LinkedHashMap<Integer, List<Integer>> StatMatrix(Corpus corp, ArrayList<Integer> selectedWord) throws IOException{
        FileWriter fileWriter = new FileWriter("./Matrice_Occurence.csv");
        
        LinkedHashMap<Integer, List<Integer>> Mstat = new LinkedHashMap<Integer, List<Integer>>();
        NbrOfWordsPerClass = new LinkedHashMap();
        
        ArrayList<Integer> temp = new ArrayList<>();
        temp.addAll(selectedWord);
        String head ="";
        for (int i = 0; i < selectedWord.size(); i++) {
            head=head+uniqueWord.get(selectedWord.get(i))+",";
        }
        fileWriter.append("Doc/Word,"+head+"Class Name\n");
        
        temp.add(null);
        Mstat.put(0, temp);
        
        int emp = 1;
        //for nbr classes
        for (int i = 0; i < corp.getsize(); i++) {
            ClasseDoc CD = corp.getCorpusFiles().get(i);
            LinkedHashMap<Integer, ArrayList<Integer>> vectors = CD.wordsInfo(selectedWord);
            NbrOfWordsPerClass.put(i, CD.nbrWordsClass);
            for (int j = 0; j < vectors.size(); j++) {
                ArrayList<Integer> plus = new ArrayList<>();
                plus.addAll(vectors.get(j));
                String str = plus.toString();
                plus.add(i);
                Mstat.put(emp, plus);
               
                fileWriter.append("Doc "+emp+","+str.substring(1,str.length()-1)+","+CD.classeName+"\n");
                emp++;
            }
        }
        if(Mstat.size() != emp || Mstat.get(0).size() != temp.size())
        {
            System.out.println("\n\n=====> ERROR OF SIZE MATRIX .....");
            System.out.println("emp = "+emp+"\t Mstat.size() = "+Mstat.size());
            System.out.println("temp = "+temp.size()+"\t Mstat[List] = "+Mstat.get(0).size());
            return null;
        }
        
        fileWriter.close();
        return Mstat;
        
    
    }
    
    //Calculer la Matrice des probabilités que pour les mots selectionés 
    public LinkedHashMap<Integer, List<Double>> ProbMatrix(Corpus corp, int mja, ArrayList<Integer> selectedWord) throws IOException{
        FileWriter fileWriter = new FileWriter("./Matrice_Probabilité.csv");
        
        /*List<Integer> lh = new ArrayList<>(uniqueWord.keySet());
        ArrayList<Integer> selecteHashdWord =new ArrayList<Integer>();
        for (int i = 0; i < selectedWord.size(); i++) {
            selecteHashdWord.add(lh.get(selectedWord.get(i)));
        }*/
        
        LinkedHashMap<Integer, List<Integer>> Mstat = StatMatrix(corp, selectedWord);
        
        LinkedHashMap<Integer, List<Double>> probWords = new LinkedHashMap<>();
        String classeName="Word";
        for (int i = 0; i < corp.getsize(); i++) {
            classeName=classeName+", P(WORD_i/"+corp.getCorpusFiles().get(i).classeName+")";
        }
        
        fileWriter.append(classeName+"\n");
        
        int endList = Mstat.get(0).size();
        for (int j = 0; j < endList-1; j++){
        //for (int j : selectedWord) {
            int dep=0;
            List<Double> listProb = new ArrayList();
            for (int i = 0; i < corp.getsize(); i++) {
                int somme=0;                
                
                for (int k = 1; k < Mstat.size() ; k++) {
                    if(Mstat.get(k).get(endList-1)==i){
                        somme=somme+Mstat.get(k).get(j);
                    }
                    
                }
                dep=dep+NbrOfWordsPerClass.get(i);
                double prob = (somme+1.0)/(double)(NbrOfWordsPerClass.get(i)+endList-1.0+mja);
                listProb.add(prob);
            }
            int key=Mstat.get(0).get(j);
            probWords.put(key, listProb);
            String values = listProb.toString();
            fileWriter.append(uniqueWord.get(key)+","+values.substring(1, values.length()-1)+"\n");
        }
        List<Double> listProb = new ArrayList();
        
        for (int i = 0; i < corp.getsize(); i++) {
            double prob = (corp.getCorpusFiles().get(i).getTaille()+0.0)/(double)(corp.nbrTotalDoc()+0.0);
            listProb.add(prob);
        }
        probWords.put(hash("###"), listProb);
        String values = listProb.toString();
        fileWriter.append("P(Classe) "+","+values.substring(1, values.length()-1)+"\n");
        
        fileWriter.close();
        return probWords;
        
        
        
        
    } 
    
    //Classifier un nouveau fichier
    public String ScanNewDoc(Corpus corp, String path, String separateur, LinkedHashMap<Integer, Integer> StopWords, String langue, LinkedHashMap<Integer, String> reduceUniqueWord) throws IOException{
        FileWriter fileWriter = new FileWriter("./FileClassification.csv");
        
        ArrayList<Integer> selectedWord = new ArrayList<>();
        int maj=0;
        
        LinkedHashMap<Integer, Integer> vector = FastProcessingFile(path, separateur, StopWords, langue, false);
        List<Integer> lh = new ArrayList<>(reduceUniqueWord.keySet());
        List<Integer> vh = new ArrayList<>(vector.keySet());
        for (int i = 0; i < vh.size(); i++) {
            if(reduceUniqueWord.get(vh.get(i))!=null){
                selectedWord.add(vh.get(i));   
            }//else{
                //maj++;
            //}
            
        }
        
        //La matrice des probabilités :
        LinkedHashMap<Integer, List<Double>> prob = ProbMatrix(corp, maj, selectedWord);
        /*System.out.println("Matice Prob !!!");
        for (int i : prob.keySet()) {
        System.out.println("i="+i+"  "+prob.get(i));
        }*/
        
        double max = -Double.MAX_VALUE;
        int classePredit = -1;
        
        for (int i = 0; i < prob.get(hash("###")).size(); i++) {
            double res = Math.log10(prob.get(hash("###")).get(i)*1.0) ;
            
            for (int j = 0; j < vh.size(); j++) {
                int n = vector.get(vh.get(j));
                if(prob.get(vh.get(j))!=null){
                    double pw = prob.get(vh.get(j)).get(i);
                    res=res+Math.log10(Math.pow(pw, n));
                }
                else{
                    //res = res+Math.log10(Math.pow( (1.0/(double)(NbrOfWordsPerClass.get(i)+lh.size()+maj)) ,n ));
                }
            }
            System.out.println("P(DOC/C"+i+") = "+res);
            
            if (res > max) {
                 max = res;
                 classePredit=i;
            }
        }
        if(classePredit!=-1){
            fileWriter.append("File , Classified as\n"+path+","+corp.getCorpusFiles().get(classePredit).classeName);
            fileWriter.close();
            return corp.getCorpusFiles().get(classePredit).classeName;
        }
        else{
            fileWriter.append("File , Classified as\n"+path+",NOT CLASSIFIED");
            fileWriter.close();
        }
        
        return "NOT CLASSIFIED";
        
    }
    
    //Recupérer les StopWords pour une langue
    public LinkedHashMap<Integer, Integer> LoadStopWords(String langue) throws IOException{
        if (hash(langue.toLowerCase())==hash("fr")) {
            File checkFile = new File("./HashOfStopWords-"+langue.toLowerCase()+".txt");
            if (checkFile.exists())
                return getHashStopWords(langue);
            else
                return getStopWords(langue);   
        }
        else if(hash(langue.toLowerCase())==hash("en")) {
            File checkFile = new File("./HashOfStopWords-"+langue.toLowerCase()+".txt");
            if (checkFile.exists())
                return getHashStopWords(langue);
            else
                return getStopWords(langue);   
        }
        else if(hash(langue.toLowerCase())==hash("ar")) {
            File checkFile = new File("./HashOfStopWords-"+langue.toLowerCase()+".txt");
            if (checkFile.exists())
                return getHashStopWords(langue);
            else
                return getStopWords(langue);   
        }
        
        
        return null;
    }
    
    //Return Corpus
    public Corpus Training(String langue, String path, double seuil) throws IOException, FileNotFoundException, ClassNotFoundException{
        long debut = System.currentTimeMillis();
        if (new File("./"+langue+"/corp.Corpus").exists()) {
            System.out.println("\nle temps de chargement de l'apprentissage est : "+(System.currentTimeMillis()-debut)+"(ms)\n");
            this.ReducedUniqueWords=(LinkedHashMap<Integer, String>) GetObject("./"+langue+"/reduceUniqueWord"+seuil+".LinkedHashMap_Integer_String");
            return (Corpus) GetObject("./"+langue+"/corp.Corpus");
        }
        else{
            LinkedHashMap<Integer, Integer> stopwords;
            stopwords= LoadStopWords(langue);
            System.out.println("StopWords loaded");

            String separateurs = "  , . ; : ! ? ( ) { } [ ] \\ > < + * - _ = / % \n  & ' $ @ £ \"";
            if(hash(langue.toLowerCase())==hash("ar")){
                separateurs = "  = . , «  » ، / ; ! { } \\ [ ] \" - _ ( ) > < + : × * ÷ % \n & ^ ' $ @ £ 0 1 2 3 4 5 6 7 8 9  ؛  ـ  ؟";
            }
            System.out.println("Separateurs loaded");

            Corpus corp = new Corpus();
            //le chemain de dossier d'apprentissage (exemple):
            DiskFileExplorer dfe = new DiskFileExplorer(path,true);
            ArrayList<ArrayList<String>> rep = new ArrayList<>(dfe.list());
            SaveDocsPath(rep, langue);
            System.out.println("\nApprentissage ...");
            corp.setCorpusFiles(AllClass(rep, separateurs, stopwords, langue, true));
            System.out.println("Fin de l'apprentissage");
            System.out.println("\nle temps de l'apprentissage est : "+(System.currentTimeMillis()-debut)+"(ms)\n");
            SetObject(corp, "./"+langue+"/corp.Corpus");
            System.out.println(" ... Saved");
            System.out.println("Le nombre des mots unique est : "+uniqueWord.size());
            System.out.println("Save unique words ...");
            SetObject(uniqueWord,"./"+langue+"/uniqueWord.LinkedHashMap_Integer_String");
            this.ReducedUniqueWords = reduceDimension(corp, seuil, langue);

            return corp;
        }
    }
    
    //Classifier un doc
    public String Testing(String langue, Corpus corp, String path, LinkedHashMap<Integer, String> reduceUniqueWord ) throws IOException{
        long debut = System.currentTimeMillis();
        
        LinkedHashMap<Integer, Integer> stopwords;
        stopwords= LoadStopWords(langue);
        
        
        String separateurs = "  , . ; : ! ? ( ) { } [ ] \\ > < + * - _ = / % \n  & ' $ @ £ \"";
        if(hash(langue.toLowerCase())==hash("ar")){
            separateurs = "  = . , «  » ، / ; ! { } \\ [ ] \" - _ ( ) > < + : × * ÷ % \n & ^ ' $ @ £ 0 1 2 3 4 5 6 7 8 9  ؛  ـ  ؟";
        }
        System.out.println("\nTesting ...");
        String resul = ScanNewDoc(corp, path, separateurs, stopwords, langue, reduceUniqueWord);
        System.out.println("\nle temps du test est : "+(System.currentTimeMillis()-debut)+"(ms)\n");
        return resul;
    }
    
    //calculer les terme de CHISQUARE
    public int getCHISQUARE_A(ClasseDoc classe, Integer hashWord){
        return classe.termDoc(hashWord);
    }
    
    public int getCHISQUARE_B(Corpus corp, int classe, Integer hashWord){ 
        return corp.nbrDocNotInClass(classe, hashWord);
    }
    
    //Calcule CHISQUARE
    public LinkedHashMap<Integer, Double> Reduce(Corpus corp){
        LinkedHashMap<Integer, Double> Chi2Max = new LinkedHashMap<Integer, Double>();
        double Xmax,X;
        int A,B,C,D,N=corp.nbrTotalDoc(),dimi;
        for (int t : uniqueWord.keySet()) {
            Xmax=Double.MIN_VALUE;
            for (int i = 0; i < corp.getsize(); i++) {
                A=getCHISQUARE_A(corp.getCorpusFiles().get(i), t);
                B=getCHISQUARE_B(corp, i, t);
                C=(N-corp.getCorpusFiles().get(i).getTaille())-A;
                D= corp.getCorpusFiles().get(i).getTaille() - B;
                dimi=(A+B)*(D+C)-(B+D)*(C+A);
                X = N*Math.pow((A*D - B*C),2)/(dimi);
                if (Xmax < X) {
                    Xmax=X;
                }
            }
            Chi2Max.put(t, Xmax);
        }
        return Chi2Max;
        
    }
    
    //Selectionner les Termes dont le Max(ChiSquareValue) > seuil donné
    public LinkedHashMap<Integer, String> reduceDimension(Corpus corp, double seuil, String langue) throws IOException, FileNotFoundException, ClassNotFoundException{
        long debut = System.currentTimeMillis();
        if (new File("./"+langue+"/reduceUniqueWord"+seuil+".LinkedHashMap_Integer_String").exists()) {
            LinkedHashMap<Integer, String> reduceUniqueWord = (LinkedHashMap<Integer, String>) GetObject("./"+langue+"/reduceUniqueWord"+seuil+".LinkedHashMap_Integer_String");
            System.out.println("\nLe temps pour charger les Dimensions réduites est : "+(System.currentTimeMillis()-debut)+"(ms)\n");
            return reduceUniqueWord;
        }
        else {
            System.out.println("\nReducing Dimensions ...");
            LinkedHashMap<Integer, Double> Chi2Max = Reduce(corp);
            LinkedHashMap<Integer, String> reduceUniqueWord = new LinkedHashMap<Integer, String>();
            for (int t : Chi2Max.keySet()) {
                if (Chi2Max.get(t)>seuil) {
                    reduceUniqueWord.put(t, ">");
                }
            }
            System.out.println("\nReduceUniqueWord size :  "+reduceUniqueWord.size()+"\n");
            System.out.println("Le temps pour réduire les Dimensions est : "+(System.currentTimeMillis()-debut)+"(ms)\n");
            SetObject(reduceUniqueWord, "./"+langue+"/reduceUniqueWord"+seuil+".LinkedHashMap_Integer_String");
            return reduceUniqueWord;
        }
    }
    
    //Recupérer la Matrice Sparce de poids si possible si non la recalculer
    public LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> getMatricePoid(Corpus corp, String langue) throws IOException, FileNotFoundException, ClassNotFoundException{
        long debut = System.currentTimeMillis();
        //Where Keys are hash of termes
        File f = new File("./"+langue+"/MDP.LinkedHashMap_Integer_LinkedHashMap_Integer_Double");
        if (f.exists()) {
            System.out.println("Chargement de la matrice de poids ...");
            LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> MDP = (LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>) GetObject("./"+langue+"/MDP.LinkedHashMap_Integer_LinkedHashMap_Integer_Double");
            System.out.println("\nle temps pour charger MDP au mémoire est : "+(System.currentTimeMillis()-debut)+"(ms)");
            return MDP;
        }
        else {
            System.out.println("Calcule + Save de la matrice de poids ...");
            //Calculer MPD
            LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> MDP = MatrixForClass(corp, langue);
            System.out.println("\nle temps pour calculer et sauvegarder MDP est : "+(System.currentTimeMillis()-debut)+"(ms)");
            return MDP;
        }
    
    }
    
    //Convertir un vecteur sparce de la matrice de poids en un vecteur plein
    public List<Double> getDocVectorFromSparceMP(LinkedHashMap<Integer,LinkedHashMap<Integer,Double>> SparceMat, Set<Integer> words, int ligne){
        List<Double> DocVector = new ArrayList<Double>();
        for (int key : words) {
            DocVector.add( (SparceMat.get(ligne).containsKey(key))? SparceMat.get(ligne).get(key) : 0.0 );
        }
        //System.out.println("DocVector = "+DocVector);
        return DocVector;
        
    }
    
    //Save-garder les chemins vers les docs
    public void SaveDocsPath(ArrayList<ArrayList<String>> rep, String langue) throws IOException{
        LinkedHashMap<Integer, String> Docpaths = new LinkedHashMap();
        int emp=0;
        for (int i = 0; i < rep.size(); i++) {
            for (int j = 1; j < rep.get(i).size(); j++) {
                if (!rep.get(i).get(j).startsWith(".")) {
                    String chemin = rep.get(i).get(0)+"/"+rep.get(i).get(j);
                    Docpaths.put(emp, chemin);
                    emp++;
                }
                
            }
            
        }
        System.out.println("Le nombre total des documents est "+Docpaths.size());
        SetObject(Docpaths, "./"+langue+"/Docpaths.LinkedHashMap_Integer_String");
        
        
    }
    
    
    
}
