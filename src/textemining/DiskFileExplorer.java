/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textemining;


import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
 
/**
 * Lister le contenu d'un répertoire
 * http://www.fobec.com/java/964/lister-fichiers-dossiers-repertoire.html
 * @author fobec 2010
 * @updated salaheddine
 */
public class DiskFileExplorer {
 
    private String initialpath = "";
    private Boolean recursivePath = false;
    public int filecount = 0;
    public int dircount = 0;
    public ArrayList<ArrayList<String>> rep = new ArrayList<>();
 
/**
 * Constructeur
 * @param path chemin du répertoire
 * @param subFolder analyse des sous dossiers
 */
    public DiskFileExplorer(String path, Boolean subFolder) {
        super();
        this.initialpath = path;
        this.recursivePath = subFolder;
    }

 
    public ArrayList<ArrayList<String>> list() {
        rep.add(this.listDirectory(this.initialpath, new ArrayList<String>()));
        //System.out.println("  dossier: " + new File(rep.get(1).get(0)).getName());
        return rep;
    }
 
    private ArrayList<String> listDirectory(String dir, ArrayList<String> fileList) {
        File file = new File(dir);
        File[] files = file.listFiles();
        
        if (files != null) {
            fileList.add(dir);
            
            for (int i = 0; i < files.length; i++) {
                
                if (files[i].isDirectory() == false) {                    
                    fileList.add(files[i].getName());
                    //System.out.println("  Fichier: " + files[i].getName());
                    this.filecount++;
                }
                else this.dircount++;
                if (files[i].isDirectory() == true && this.recursivePath == true) {
                    rep.add(this.listDirectory(files[i].getAbsolutePath(), new ArrayList<String>()));
                }
            }
        }
        
        return fileList;
    }
    
    
    
}
