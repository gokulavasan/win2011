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
	    onceMore = false;
	    QuadIterator iter = new QuadIterator(cfg, analysis.isForward());
	    while (analysis.isForward() ? iter.hasNext() : iter.hasPrevious())
	    {
		Quad quad = analysis.isForward() ? iter.next() : iter.previous();

		Flow.DataflowObject DfOIn = analysis.getIn(quad);
		Flow.DataflowObject DfOOut = analysis.getOut(quad);

		Flow.DataflowObject tempDf = analysis.newTempVar();

		try {
		    Iterator meetIter = (analysis.isForward() ? 
                                             iter.predecessors() : iter.successors());
		    if (meetIter == null)
		    {
			tempDf.meetWith(
                          analysis.isForward() ? analysis.getEntry() : analysis.getExit()
                          );
		    }
		    else
		    {
			while ( meetIter.hasNext())
			{
			    Quad newQ = (Quad) (meetIter.next()) ;
			    if (newQ != null)
			    {
				tempDf.meetWith(analysis.isForward() ? 
                                                       analysis.getOut(newQ) : analysis.getIn(newQ)
                                                  );
			    } else {
				tempDf.meetWith(analysis.isForward() ? 
                                                       analysis.getEntry() : analysis.getExit()
                                                  );

			    }
			}
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
                
                
	    }	
	} while (onceMore);
        
          Flow.DataflowObject tempDf = analysis.newTempVar();
          if (analysis.isForward() && cfg.exit().size()==0){
            Iterator meetIter = cfg.exit().getPredecessors().iterator();
            while (meetIter.hasNext()){
              BasicBlock bb = (BasicBlock)meetIter.next();
              if (bb.size()!=0){
                tempDf.meetWith(analysis.getOut(bb.getLastQuad()));
              }
            }
            analysis.setExit(tempDf);
          } else if (!analysis.isForward() && cfg.entry().size()==0) { 
             Iterator meetIter = cfg.entry().getSuccessors().iterator();
            while (meetIter.hasNext()){
              BasicBlock bb = (BasicBlock)meetIter.next();
              if (bb.size()!=0){
                tempDf.meetWith(analysis.getIn(bb.getQuad(0)));
              }
            }
            analysis.setEntry(tempDf);
          }

	// this needs to come last.
	analysis.postprocess(cfg);
    }
}
