package optimize;

import joeq.Class.jq_Class;
import joeq.Main.Helper;

public class FindRedundantNullChecks {
    
    /*
     * args is an array of class names 
     * method should print out a list of quad ids of redundant null checks
     * for each function as described on the course webpage     
     */
    
     
    public static void main(String[] args) {	        
        //fill me in
	int i;
	jq_Class[] classes = new jq_Class[args.length];
	for (i = 0; i < args.length; i++)
	{
		ReferenceSolver nullSolve = new ReferenceSolver();
		nullSolve.registerAnalysis(new NullCheckAnalysis());
		Helper.runPass(classes[i], nullSolve);
		System.out.println(args[i]);	
	}
    }
}
