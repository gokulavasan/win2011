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

	while(onceMore)
	{
		//System.out.println("\nEntering iteration!\n");
		onceMore = false;
		QuadIterator iter = new QuadIterator(cfg, analysis.isForward());
		//Quad tempq = iter.next();
		while (iter.hasNext())
		{
			Quad quad = iter.next();
			//System.out.println("\nAnalysing Quad : " + quad.getID() + "\n");
				
			Flow.DataflowObject DfOIn = analysis.getIn(quad);
			Flow.DataflowObject DfOOut = analysis.getOut(quad);
	
			Flow.DataflowObject tempDfOIn = analysis.newTempVar();

			try {
				Iterator preIter = iter.predecessors();
				if (preIter == null)
				{
					
				}
				else
				{
					//int i = 0;
					while (preIter.hasNext())
					{
						Quad newQ = (Quad) preIter.next();
						if (newQ != null)
						{
							i++;
							//System.out.println("\n\tSuccessors : " + newQ.getID() + "\n");
							tempDfOIn.meetWith(analysis.getOut(newQ));
						}
					}
					//if (i!=0)
					//{
						analysis.setIn(quad, tempDfOIn);
					//}
				}
				
			} 
			catch (Exception e)
			{ 
				System.out.println(e.getMessage()); 
			}
			
			
			analysis.processQuad(quad);
			
			if (DfOOut.equals(analysis.getOut(quad)))
			{
				continue;
			}
			else
			{
				onceMore = true;
			}	

		}	
	}

		
        // this needs to come last.
        analysis.postprocess(cfg);
    }
}
