/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hmc_ga;

import java.util.ArrayList;

/**
 *
 * @author Ricardo Cerri <cerri@icmc.usp.br / cerrirc@gmail.com>
 */
public class Individual implements Comparable {

    private double fitness;
    private double[] rule;
    private int numberCoveredExamples;
    //private ArrayList<String[]> coveredExamples;
    private ArrayList<Integer> indexCoveredExamples;
    private ArrayList<Integer> indexUncoveredExamples;
    private double[] meanClassLabelVectorCovered;
    private double[] meanClassLabelVectorUncovered;
    private ArrayList<Integer> posActiveTerms;
    private double percentageCoverage;

    public Individual(double[] rule, ArrayList<Integer> activeTerms) {
        this.rule = rule;
        this.posActiveTerms = activeTerms;
        calculateFitness();
    }

    /* ===========================================================
     * Calculate the fitness of the rules
     * =========================================================== */
    public void calculateFitness() {

        fitness = 0.0;
        numberCoveredExamples = 0;
        percentageCoverage = 0;
        //coveredExamples = new ArrayList<String[]>();
        indexCoveredExamples = new ArrayList<Integer>();
        indexUncoveredExamples = new ArrayList<Integer>();
        meanClassLabelVectorCovered = new double[Classes.getClasses().length];
        meanClassLabelVectorUncovered = new double[Classes.getClasses().length];

        for (int i = 0; i < Datasets.getDatasetTrain().size(); i++) {

            String[] example = Datasets.getDatasetTrain().get(i);

            //Verify if the rule covers the example
            int coverage = verifyRule(example, rule, 0);
            numberCoveredExamples += coverage;

            if (coverage == 1) { //Rule satisfies example
                //coveredExamples.add(example);
                indexCoveredExamples.add(i);
            } else {
                indexUncoveredExamples.add(i);
            }
        }

        //Calculate the mean class label vector of the classes
        setMeanClassLabelVector();

        //Percentage of covered examples
        percentageCoverage = (double) numberCoveredExamples / Datasets.getDatasetTrain().size();

        if (Parameters.getMultiLabel() == 1) {

            //Calculate variance gain
            varianceGainRule();

            //Obtain the predictions
            /*double[][] matrixPredictions = new double[numberCoveredExamples][Classes.getClasses().length];
            for (int i = 0; i < numberCoveredExamples; i++) {
                System.arraycopy(meanClassLabelVectorCovered, 0, matrixPredictions[i], 0, Classes.getClasses().length);
            }

            Evaluation evaluation = new Evaluation();

            //AUPRC
            double term1 = 0;
            if (indexCoveredExamples.size() >= Parameters.getMinCoveredExamplesRule()) {
                term1 = evaluation.evaluationAUPRCFitness(matrixPredictions, indexCoveredExamples);

                //Fitness. We are giving much more value to fmeasure
                //fitness = 0.9 * term1 + 0.1 * percentageCoverage;

                //fitness = (2 * term1 * percentageCoverage) / (term1 + percentageCoverage);
            }
            fitness = term1;*/

        } else {

            //Obtain the predictions
            double[][] matrixPredictions = new double[numberCoveredExamples][Classes.getClasses().length];
            for (int i = 0; i < numberCoveredExamples; i++) {
                System.arraycopy(meanClassLabelVectorCovered, 0, matrixPredictions[i], 0, Classes.getClasses().length);
            }

            //Get the higher probabilities
            matrixPredictions = Results.getHigherProbabilities(matrixPredictions);

            //Evaluate the predictions in the test dataset
            Evaluation evaluation = new Evaluation();

            //F-measure
            double term1 = 0;
            if (indexCoveredExamples.size() >= Parameters.getMinCoveredExamplesRule()) {
                term1 = evaluation.evaluationFmeasureFitness(matrixPredictions, indexCoveredExamples);

                //fitness = term1;
                //Fitness. We are giving much more value to fmeasure
                //fitness = 0.9 * term1 + 0.1 * percentageCoverage;
                fitness = (2 * term1 * percentageCoverage) / (term1 + percentageCoverage);
            }
        }

        //System.out.println();
    }

    /* ===========================================================
     * Calculate the variance gain of a rule
     * =========================================================== */
    public void varianceGainRule() {

        //Variance gain of the set of all training examples
        double[] meanClassesLabelAll = Classes.getMeanClassLabelVectorAllClasses();
        /*if (Parameters.getMultiLabel() == 0) {
         meanClassesLabelAll = Results.getHigherProbabilities(meanClassesLabelAll);
         }*/
        double varianceGainAll = getVarianceGain(Datasets.getDatasetTrain().size(), meanClassesLabelAll);

        //Variance gain of the set of covered examples
        double varianceGainCovered = getVarianceGain(indexCoveredExamples, meanClassLabelVectorCovered);

        //Variance gain of the set of uncovered examples
        double varianceGainUncovered = getVarianceGain(indexUncoveredExamples, meanClassLabelVectorUncovered);

        int numTotalExamples = Datasets.getDatasetTrain().size();
        double term11 = (double) indexCoveredExamples.size() / (double) numTotalExamples;
        double term1 = term11 * varianceGainCovered;

        double term22 = (double) indexUncoveredExamples.size() / (double) numTotalExamples;
        double term2 = term22 * varianceGainUncovered;

        //if (indexCoveredExamples.size() >= Parameters.getMinCoveredExamplesRule()) {
        fitness = varianceGainAll - term1 - term2;
        //fitness = (0.9 * (varianceGainAll - term1 - term2)) + (0.1 * percentageCoverage);
        //fitness = (2 * fitness * percentageCoverage) / (fitness + percentageCoverage);
        //}

    }

    /* ===========================================================
     * Calculate the variance gain of a set of examples given the
     * examples and their meanClassLabel
     * =========================================================== */
    public double getVarianceGain(int numExamples, double[] meanClassLabel) {

        double varianceGain = 0;
        double[] weights = Classes.getWeightingScheme();
        ArrayList<int[]> binaryClasses = Classes.getBinaryClassesTrain();

        for (int i = 0; i < numExamples; i++) {
            double sum = 0;
            for (int j = 0; j < meanClassLabel.length; j++) {
                sum += weights[j] * Math.pow((binaryClasses.get(i)[j] - meanClassLabel[j]), 2);
            }
            varianceGain += sum;
        }

        varianceGain = varianceGain / numExamples;

        return varianceGain;

    }

    public double getVarianceGain(ArrayList<Integer> indexExamples, double[] meanClassLabel) {

        double varianceGain = 0;
        double[] weights = Classes.getWeightingScheme();
        ArrayList<int[]> binaryClasses = Classes.getBinaryClassesTrain();

        for (int i = 0; i < indexExamples.size(); i++) {
            double sum = 0;
            int[] binaryVector = binaryClasses.get(indexExamples.get(i));
            for (int j = 0; j < meanClassLabel.length; j++) {
                sum += weights[j] * Math.pow((binaryVector[j] - meanClassLabel[j]), 2);
            }
            varianceGain += sum;
        }

        if (indexExamples.size() > 0) {
            varianceGain = varianceGain / indexExamples.size();
        }

        return varianceGain;
    }


    /* ===========================================================
     * Calculate the mean class label vector of the classes of a
     * given set of examples
     * =========================================================== */
    public void setMeanClassLabelVector() {


        ArrayList<int[]> binaryClasses = Classes.getBinaryClassesTrain();

        //Covered examples
        for (int i = 0; i < indexCoveredExamples.size(); i++) {

            int[] binaryVector = binaryClasses.get(indexCoveredExamples.get(i));

            for (int j = 0; j < binaryVector.length; j++) {

                meanClassLabelVectorCovered[j] += binaryVector[j];

            }
        }

        if (indexCoveredExamples.size() > 0) {
            for (int i = 0; i < meanClassLabelVectorCovered.length; i++) {
                meanClassLabelVectorCovered[i] = meanClassLabelVectorCovered[i] / indexCoveredExamples.size();
            }
        }


        //Uncovered examples
        for (int i = 0; i < indexUncoveredExamples.size(); i++) {

            int[] binaryVector = binaryClasses.get(indexUncoveredExamples.get(i));

            for (int j = 0; j < binaryVector.length; j++) {

                meanClassLabelVectorUncovered[j] += binaryVector[j];

            }
        }

        if (indexUncoveredExamples.size() > 0) {
            for (int i = 0; i < meanClassLabelVectorUncovered.length; i++) {
                meanClassLabelVectorUncovered[i] = meanClassLabelVectorUncovered[i] / indexUncoveredExamples.size();
            }
        }

        /*if (Parameters.getMultiLabel() == 0) {
         meanClassLabelVectorCovered = Results.getHigherProbabilities(meanClassLabelVectorCovered);
         meanClassLabelVectorUncovered = Results.getHigherProbabilities(meanClassLabelVectorUncovered);
         }*/

        //System.out.println();

    }

    /* ===========================================================
     * Verify if the rule covers a given example
     * =========================================================== */
    public int verifyRule(String[] example, double[] rule, int data) {

        int coverage = 0;

        for (int i = 0; i < posActiveTerms.size(); i++) {

            int pos = posActiveTerms.get(i);
            int posAttribute = pos / 4;
            //Verify if the term is used in the rule

            //Term information
            int operator = (int) rule[pos + 1];
            double infLim = rule[pos + 2];
            double supLim = rule[pos + 3];

            double attributeValue = 0.0;

            if (Datasets.getInfoAttributes().get(posAttribute) == 1) { //Numeric attribute
                if (Datasets.getTokenMissingValue().equals(example[posAttribute]) == true) {
                    attributeValue = Datasets.getMeanValues().get(data)[posAttribute];
                } else {
                    attributeValue = Double.parseDouble(example[posAttribute]);
                }

                //Verify the type of operator
                switch (operator) { //attributeValue <= supLim, attributeValue >= infLim
                    case 0: //attributeValue <= supLim 
                        coverage = lessEqual(attributeValue, supLim);
                        break;

                    case 1: //attributeValue >= infLim     
                        coverage = greaterEqual(attributeValue, infLim);
                        break;

                    case 2: // infLim <= attributeValue <= supLim
                        coverage = compoundTerm(infLim, supLim, attributeValue);
                        break;

                    case 3: // attribute1 <= attribute2
                        coverage = compareAttributes((int) infLim, attributeValue, example);
                        break;
                }


            } else { //Categoric attribute
                int posCatAttribute = Datasets.getCategoricAttributes().indexOf(posAttribute);

                String catAttributeValue = "";

                //If the attribute value is missing, put the mode value
                if (Datasets.getTokenMissingValue().equals(example[posAttribute]) == true) {
                    int posCatValue = (int) Datasets.getMeanValues().get(data)[posAttribute];
                    catAttributeValue = Datasets.getModeValues().get(data).get(posCatValue);
                } else {
                    catAttributeValue = example[posAttribute];
                }

                //Verify the type of operator
                switch (operator) {
                    case 0: //attributeValue = infLim 
                        attributeValue = Population.getCategoricToNumericValue(posCatAttribute, catAttributeValue);
                        coverage = equal(attributeValue, infLim);
                        break;

                    case 1: //attributeValue != infLim
                        attributeValue = Population.getCategoricToNumericValue(posCatAttribute, catAttributeValue);
                        coverage = different(attributeValue, infLim);
                        break;

                    case 2: //attributeValue in {values in infLim}
                        String[] categoricValuesCondition = Datasets.getCategoricMapping().get(posCatAttribute).get((int) infLim);
                        coverage = among(catAttributeValue, categoricValuesCondition);
                        break;
                }
            }

            if (coverage == 0) {
                break;
            }
        }

        return coverage;
    }

    /* ===========================================================
     * Verify if a given attribute value is lower than 
     * another attribute value
     * =========================================================== */
    public static int compareAttributes(int pos1, double attributeValue2, String[] example) {

        double valueAttribute1 = 0;

        if (Datasets.getTokenMissingValue().equals(example[pos1]) == true) {
            valueAttribute1 = Datasets.getMeanValues().get(0)[pos1];
        } else {
            valueAttribute1 = Double.parseDouble(example[pos1]);
        }

        if (valueAttribute1 <= attributeValue2) {
            return 1;
        } else {
            return 0;
        }

    }

    /* ===========================================================
     * Verify if the categoric example value X is among a given
     * set os categorical values
     * =========================================================== */
    public static int among(String catAttributeValue, String[] categoricValuesCondition) {

        int found = 0;

        for (int i = 0; i < categoricValuesCondition.length; i++) {
            if (catAttributeValue.equals(categoricValuesCondition[i]) == true) {
                found = 1;
                break;
            }
        }

        return found;
    }

    /* ===========================================================
     * Verify if the example value X satisfies: X != infLim
     * =========================================================== */
    public static int different(double attributeValue, double infLim) {

        if (attributeValue != infLim) {
            return 1;
        } else {
            return 0;
        }
    }

    /* ===========================================================
     * Verify if the example value X satisfies: X = infLim
     * =========================================================== */
    public static int equal(double attributeValue, double infLim) {

        if (attributeValue == infLim) {
            return 1;
        } else {
            return 0;
        }
    }

    /* ===========================================================
     * Verify if the example value X satisfies: infLim <= X <= supLim
     * =========================================================== */
    private static int compoundTerm(double infLim, double supLim, double attributeValue) {

        int left = greaterEqual(attributeValue, infLim);
        int right = lessEqual(attributeValue, supLim);

        if (left == 1 && right == 1) {
            return 1;
        } else {
            return 0;
        }
    }

    /* ===========================================================
     * Verify if the example value X satisfies: X <= supLim
     * =========================================================== */
    private static int lessEqual(double attributeValue, double supLim) {

        if (attributeValue <= supLim) {
            return 1;
        } else {
            return 0;
        }
    }

    /* ===========================================================
     * Verify if the example value X satisfies: X >= infLim
     * =========================================================== */
    public static int greaterEqual(double attributeValue, double infLim) {

        if (attributeValue >= infLim) {
            return 1;
        } else {
            return 0;
        }
    }

    /* ===========================================================
     * Sorts the rules according to their fitness values
     * =========================================================== */
    public int compareTo(Object o) {

        if (this.getFitness() > ((Individual) o).getFitness()) {
            return -1;
        } else if (this.getFitness() < ((Individual) o).getFitness()) {
            return 1;
        } else {
            return 0;
        }
    }

    public double getFitness() {
        return fitness;
    }

    public double[] getRule() {
        return rule;
    }

    /*public ArrayList<String[]> getCoveredExamples() {
     return coveredExamples;
     }*/
    public ArrayList<Integer> getIndexCoveredExamples() {
        return indexCoveredExamples;
    }

    public ArrayList<Integer> getIndexUncoveredExamples() {
        return indexUncoveredExamples;
    }

    public int getNumberCoveredExamples() {
        return numberCoveredExamples;
    }

    public double[] getMeanClassLabelVectorCovered() {
        return meanClassLabelVectorCovered;
    }

    public double[] getMeanClassLabelVectorUncovered() {
        return meanClassLabelVectorUncovered;
    }

    public ArrayList<Integer> getPosActiveTerms() {
        return posActiveTerms;
    }
}
