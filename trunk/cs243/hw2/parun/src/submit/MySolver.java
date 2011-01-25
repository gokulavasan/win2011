package submit;

// some useful things to import. add any additional imports you need.
import joeq.Compiler.Quad.*;
import flow.Flow;
import java.util.*;
/**
 * Skeleton class for implementing the Flow.Solver interface.
 */
public class MySolver implements Flow.Solver {

    protected Flow.Analysis analysis;

    /**
     * Sets the analysis.  When visitCFG is called, it will
     * perform this analysis on a given CFG.
     *
     * @param analyzer The analysis to run
     */
    public void registerAnalysis(Flow.Analysis analyzer) {
        this.analysis = analyzer;
    }

    /**
     * Runs the solver over a given control flow graph.  Prior
     * to calling this, an analysis must be registered using
     * registerAnalysis
     *
     * @param cfg The control flow graph to analyze.
     */
   


    public void visitCFG(ControlFlowGraph cfg) {

        // this needs to come first.
        analysis.preprocess(cfg);	
	
	boolean onceMore = true;
        /***********************
         * Your code goes here *
         ***********************/

	do
	{
	    //System.out.println("\nEntering iteration!\n");
	    onceMore = false;
	    QuadIterator iter = new QuadIterator(cfg, analysis.isForward());
	    //Quad tempq = iter.next();
	    while (analysis.isForward() ? iter.hasNext() : iter.hasPrevious())
	    {
		Quad quad = analysis.isForward() ? iter.next() : iter.previous();
		//System.out.println("\nAnalysing Quad : " + quad.getID() + "\n");

		Flow.DataflowObject DfOIn = analysis.getIn(quad);
		Flow.DataflowObject DfOOut = analysis.getOut(quad);

		Flow.DataflowObject tempDf = analysis.newTempVar();

		try {
		    Iterator meetIter = (analysis.isForward() ? 
                                             iter.predecessors() : iter.successors());
		    if (meetIter == null)
		    {
			//System.out.println("\n\tSuccessors : " + newQ.getID() + "\n");
			tempDf.meetWith(
                          analysis.isForward() ? analysis.getEntry() : analysis.getExit()
                          );
		    }
		    else
		    {
			//int i = 0;
			while ( meetIter.hasNext())
			{
			    Quad newQ = (Quad) (meetIter.next()) ;
			    if (newQ != null)
			    {
				//i++;
				//System.out.println("\n\tSuccessors : " + newQ.getID() + "\n");
				tempDf.meetWith(analysis.isForward() ? 
                                                       analysis.getOut(newQ) : analysis.getIn(newQ)
                                                  );
			    } else {
				tempDf.meetWith(analysis.isForward() ? 
                                                       analysis.getEntry() : analysis.getExit()
                                                  );

			    }
			}
			//if (i!=0)
			//{
			//}
		    }
                    if (analysis.isForward()){
		      analysis.setIn(quad, tempDf);
                    } else {
                      analysis.setOut(quad,tempDf);
                    }

		} 
		catch (Exception e)
		{ 
		    System.out.println(e.getMessage()); 
		}

                 analysis.processQuad(quad);
                if (!(DfOOut.equals(analysis.getOut(quad))) || !(DfOIn.equals(analysis.getIn(quad)))){
		    onceMore = true;
		}	
                if (analysis.isForward()){
		  analysis.setExit(analysis.getOut(quad));
                }  else { 
		  analysis.setEntry(analysis.getIn(quad));
                }
	    }	
	} while (onceMore);

	// this needs to come last.
	analysis.postprocess(cfg);
    }
}
