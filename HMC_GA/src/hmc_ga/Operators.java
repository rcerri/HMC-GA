/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hmc_ga;

import java.util.Random;

/**
 *
 * @author Ricardo Cerri <cerri@icmc.usp.br / cerrirc@gmail.com>
 */
public class Operators {
    
    //Use these if initiate the rules using seeding
    private static String[] numericOperators = {"<=",">=","<= <=","<="};
    private static String[] categoricOperators = {"=","!=","in"}; 
    //private static String[] categoricOperators = {"="}; 
    
    /* ===========================================================
     * Randomly select a numeric operator
     * =========================================================== */
    public static double getInitialFlagValue(double probabilityFlag){
        
        double flagValue = 0.0;
        
        Random generator = new Random();
        double num = generator.nextDouble();
        
        if(num <= probabilityFlag){
            flagValue = 1.0;
        }
                
        return flagValue;
    }
    
    /* ===========================================================
     * Randomly select a numeric operator
     * =========================================================== */
    public static double getNumericOperator(){
        
        Random generator = new Random();
        double operator = generator.nextInt(numericOperators.length);
        
        return operator;
    }
    
    
    /* ===========================================================
     * Randomly select a categoric operator
     * =========================================================== */
    public static double getCategoricOperator(){
        
        Random generator = new Random();
        double operator = generator.nextInt(categoricOperators.length);
                
        return operator;
    }
    
}
