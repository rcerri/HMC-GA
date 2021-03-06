/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hmc_ga;

import java.io.File;

/**
 *
 * @author Ricardo Cerri <cerri@icmc.usp.br / cerrirc@gmail.com>
 */
public class Paths {

    private static String nameFilePredictions = "predictions.txt";
    private static String nameFileRules = "rules.txt";
    private static String nameFilePRclasses = "PRclasses.txt";
    private static String nameFileInterpolationClasses = "interpolationClasses.txt";
    private static String nameFileInterpolation = "interpolation.txt";
    private static String nameFileAUPRCclasses = "AUPRCclasses.txt";
    private static String nameFileFmeasureLevels = "fmeasureLevels.txt";
    private static String nameFileMeanFmeasureLevels = "meanFmeasureLevels.txt";
    private static String nameFileMeanAUPRCclasses = "meanAUPRCclasses.txt";
    private static String nameFileMeanAUPRC = "meanAUPRC.txt";
    private static String nameFileTrainingTimes = "trainingTimes.txt";
    private static String nameFileMeanTrainingTimes = "meanTrainingTimes.txt";

    //Path where the datasets are storaged
    //private static String pathDatasets = "/home/cerri/HMC/Datasets/New/";
    //private static String pathDatasets = "/home/cerri/HMC/Datasets/Datasets_KUL/New/";
    //private static String pathDatasets = "/home/cerri/Documentos/Doutorado/HMC/Datasets/Datasets_KUL/All/New/";
    private static String pathDatasets;
    
    //A Results folder will be created in this path
    private static String pathResults = "Results/";

    public Paths() {
        
        pathDatasets = Parameters.getPathDatasets();
        
        if(Parameters.getMultiLabel() == 1){
            pathResults = pathResults + "Multilabel/";
        }
        else{
            pathResults = pathResults + "SingleLabel/";
        }

        //Create directories to save results
        String pathResultsRuns = pathResults + Parameters.getHierarchyType() + "/" + Parameters.getFileDatasetTest() + "/";

        for (int i = 1; i <= Parameters.getNumberRuns(); i++) {
            new File(pathResultsRuns + "Run" + i).mkdirs();

            /*for (int j = 0; j < Parameters.getThresholdValues().size(); j++) {
                new File(pathResultsRuns + "Run" + i + "/Thresholds/" + Parameters.getThresholdValues().get(j)).mkdirs();
            }*/
        }
    }

    public static String getPathDatasets() {
        return pathDatasets;
    }

    public static String getPathResults() {
        return pathResults;
    }

    public static String getNameFileRules() {
        return nameFileRules;
    }

    public static String getNameFilePredictions() {
        return nameFilePredictions;
    }

    public static String getNameFilePRclasses() {
        return nameFilePRclasses;
    }

    public static String getNameFileInterpolationClasses() {
        return nameFileInterpolationClasses;
    }

    public static String getNameFileInterpolation() {
        return nameFileInterpolation;
    }

    public static String getNameFileAUPRCclasses() {
        return nameFileAUPRCclasses;
    }

    public static String getNameFileMeanAUPRCclasses() {
        return nameFileMeanAUPRCclasses;
    }

    public static String getNameFileMeanAUPRC() {
        return nameFileMeanAUPRC;
    }
    
    public static String getNameFileMeanFmeasureLevels() {
        return nameFileMeanFmeasureLevels;
    }

    public static String getNameFileFmeasureLevels() {
        return nameFileFmeasureLevels;
    }
    
    public static String getNameFileTrainingTimes() {
        return nameFileTrainingTimes;
    }

    public static String getNameFileMeanTrainingTimes() {
        return nameFileMeanTrainingTimes;
    }

}
