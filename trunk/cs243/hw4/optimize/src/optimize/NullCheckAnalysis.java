package optimize;

import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Compiler.Quad.QuadVisitor;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.RegisterOperand;
//import java.util.HashSet;
//import java.util.TreeSet;
import java.util.*;
/**
 * Class of reaching definitions analysis.
 */
public class NullCheckAnalysis implements Flow.Analysis {
    /**
     * the arrays of in and out values
     */
    private VarSet[] in;
    private VarSet[] out;

    /**
     * the entry and exit nodes
     */
    private VarSet entry;
    private VarSet exit;

    /**
     * the transfer function
     */
    private TransferFunction transferFunction = new TransferFunction();

    /**
     * Performs preprocessing on the control flow graph.
     * @param cfg the control flow graph to preprocess
     */

    /**
     * Perform post processing
     * @param cfg the control flow graph to post process
     */


    public void preprocess(ControlFlowGraph cfg) {
        System.out.print(cfg.getMethod().getName().toString()+" " );
        /* Generate initial conditions. */
        QuadIterator qit = new QuadIterator(cfg);
        int max = 0;
        while (qit.hasNext()) {
            int x = qit.next().getID();
            if (x > max) max = x;
        }
        max += 1;
        in = new VarSet[max];
        out = new VarSet[max];
        qit = new QuadIterator(cfg);

        Set<String> s = new TreeSet<String>();
        VarSet.universalSet = s;

        /* Arguments are always there. */
        int numargs = cfg.getMethod().getParamTypes().length;
        for (int i = 0; i < numargs; i++) {
            s.add("R"+i);
        }

        while (qit.hasNext()) {
            Quad q = qit.next();
            for (RegisterOperand def : q.getDefinedRegisters()) {
                s.add(def.getRegister().toString());
            }
            for (RegisterOperand use : q.getUsedRegisters()) {
                s.add(use.getRegister().toString());
            }
        }

        entry = new VarSet();
        exit = new VarSet();
        transferFunction.value = new VarSet();
        for (int i=0; i<in.length; i++) {
            in[i] = new VarSet();
            out[i] = new VarSet();
        }

        //System.out.println("Initialization completed.");
    }



    public void postprocess(ControlFlowGraph cfg) 
    {
       /* System.out.println("entry: " + entry.toString());
        for (int i=1; i<in.length; i++){
           System.out.println(i + " in:  " + in[i].toString());
           System.out.println(i + " out: " + out[i].toString());
        }
        System.out.println("exit: " + exit.toString());
	*/
	QuadIterator qit = new QuadIterator(cfg);
	while(qit.hasNext())
	{
		Quad qd = qit.next();
		if (qd.getOperator() == Operator.NullCheck.NULL_CHECK.INSTANCE)
		{
			Flow.DataflowObject s = getIn(qd);
			for (Operand.RegisterOperand def : qd.getUsedRegisters())
			{
				if (checkInc2(s,def.getRegister().toString()))
				{
					System.out.print(qd.getID()+ " ");
				}
			}
		}
	}
	System.out.println();		
    }

    /**
     * Reaching definitions is a forward analysis.
     * @return true
     */
    public boolean isForward() { 
        return true; 
    }

    /**
     * @param dataflowObject the dataflow object to copy
     * @return a new copy of a given dataflow object
     */
    private Flow.DataflowObject getNewCopy(Flow.DataflowObject dataflowObject) {
        Flow.DataflowObject result = newTempVar();
        result.copy(dataflowObject);
        return result;
    }

    /**
     * @return a copy of the entry node
     */
    public Flow.DataflowObject getEntry() { 
        //return getNewCopy(entry);
	Flow.DataflowObject result = newTempVar();
	result.copy(entry);
	return result;
    }
    

    /**
     * @return a copy of the exit node
     */
    public Flow.DataflowObject getExit() { 
        //return getNewCopy(exit);
	Flow.DataflowObject result = newTempVar();
	result.copy(exit);
	return result;
    }

    /**
     * @param quad the quad to return the In value of
     * @return a copy of the In value of the given quad
     */
    public Flow.DataflowObject getIn(Quad quad) { 
        //return getNewCopy(in[quad.getID()]);
	Flow.DataflowObject result = newTempVar();
	result.copy(in[quad.getID()]);
	return result;
    }
    
    public boolean checkInc (Flow.DataflowObject o, String s)
    {
	VarSet ov = (VarSet) o;
	return ov.contains(s);
    }
	 
    public boolean checkInc2 (Flow.DataflowObject o, String s)
    {
	VarSet ov = (VarSet) o;
	return ov.contains2(s);
    }

    /**
     * @param quad the quad to return the Out value of
     * @return a copy of the Out value of the given quad
     */
    public Flow.DataflowObject getOut(Quad quad) { 
        //return getNewCopy(out[quad.getID()]);
	Flow.DataflowObject result = newTempVar();
	result.copy(out[quad.getID()]);
	return result;
    }

    /**
     * Sets the In value of a given quad
     * @param quad the quad to set the In value of
     * @param newIn the quad's new In value
     */
    public void setIn(Quad quad, Flow.DataflowObject newIn) { 
        in[quad.getID()].copy(newIn); 
    }

    /**
     * Sets the Out value of a given quad
     * @param quad the quad to set the Out value of
     * @param newOut the quad's new Out value
     */
    public void setOut(Quad quad, Flow.DataflowObject newOut) { 
        out[quad.getID()].copy(newOut); 
    }

    /**
     * Sets the entry node's value
     * @param newEntry the entry node's new value
     */
    public void setEntry(Flow.DataflowObject newEntry) { 
        entry.copy(newEntry); 
    }

    /**
     * Sets the exit node's value
     * @param newExit the exit node's new value
     */
    public void setExit(Flow.DataflowObject newExit) { 
        exit.copy(newExit); 
    }

    /**
     * @return a new definition set
     */
    public Flow.DataflowObject newTempVar() { 
        return new VarSet(); 
    }

    /**
     * Process a quad by applying the transfer function to it
     * @param quad the quad to process
     */
    public void processQuad(Quad quad) {
        transferFunction.value.copy(in[quad.getID()]);
        transferFunction.visitQuad(quad);
        out[quad.getID()].copy(transferFunction.value);
    }


    /**
     * The definition set object.
     * A set of definitions is a set of quads which hold the definitions.
     */

    public static class VarSet implements Flow.DataflowObject 
    {
        private Set<String> set;
	private Set<String> nullChecked;

        public static Set<String> universalSet;
        public VarSet() 
	{ set = new TreeSet<String>(); 
	  nullChecked = new TreeSet<String>();	
	}

        public void setToTop() 
	{ 
		set = new TreeSet<String>(); 
		nullChecked = new TreeSet<String>(universalSet);
	}
        public void setToBottom() 
	{ 
		set = new TreeSet<String>(universalSet); 
		nullChecked = new TreeSet<String>();
	}

	public void addChecked(String s)
	{
		nullChecked.add(s);
	}
        public void meetWith(Flow.DataflowObject o) 
        {
            VarSet a = (VarSet)o;
            set.addAll(a.set);
	    nullChecked.retainAll(a.nullChecked);	
        }

        public void copy(Flow.DataflowObject o) 
        {
            VarSet a = (VarSet) o;
            set = new TreeSet<String>(a.set);
	    nullChecked = new TreeSet<String>(a.nullChecked);	
        }
        public boolean contains (String s)
	{
		return set.contains(s);
	}
	public boolean contains2 (String s)
	{
		return nullChecked.contains(s);
	}
        @Override
        public boolean equals(Object o) 
        {
            if (o instanceof VarSet) 
            {
                VarSet a = (VarSet) o;
                return (set.equals(a.set)&&nullChecked.equals(a.nullChecked));
            }
            return false;
        }
        @Override
        public int hashCode() {
            return set.hashCode();
        }
        @Override
        public String toString() 
        {
            return set.toString();
        }

//        public void genVar(String v) {set.add(v);}
//        public void killVar(String v) {set.remove(v);}

	protected void add(String s)
	{
		set.add(s);
		nullChecked.remove(s);	
	}
	protected void kill (Quad quad) 
	{
		//String operandString = operand.getRegister().toString();
		if (quad.getOperator() == Operator.NullCheck.NULL_CHECK.INSTANCE)
	     	{
			Set<String> toRemove = new TreeSet<String>();		
	    		//set.remove(quad.getUsedRegisters());
                		for (Operand.RegisterOperand def : quad.getUsedRegisters())
				{
                    		// if quad defines the same register, kill it
                        			toRemove.add(def.getRegister().toString());
						//VarSet h = (VarSet)getIn(quad);
						//h.addChecked(def.getRegister().toString());
						if(set.contains(def.getRegister().toString()))
							nullChecked.add(def.getRegister().toString());
				}
   
			set.removeAll(toRemove);
             	}
	}
    }


    /**
     * The QuadVisitor that performs the computation of the new Out
     * definition set of a quad
     */
    public static class TransferFunction extends QuadVisitor.EmptyVisitor {
        /**
         * the value operated on
         */
        VarSet value;

        /**
         * Constructor
         */
        public TransferFunction() {
        }

        /**
         * Visits a quad and calculates the new Out value
         * @param quad the quad to visit
         */
        @Override
        public void visitQuad(Quad quad) {
            // first iterate over the quad's defined registers and kill
            // definitions
            for (Operand.RegisterOperand def : quad.getDefinedRegisters()) {
                value.add(def.getRegister().toString());
            }
	    if (!quad.getUsedRegisters().isEmpty())
		value.kill(quad);
            // now add the definition to the definition set if something
            // is defined in it
            //if (!quad.getDefinedRegisters().isEmpty())
            //    value.addDefinition(quad);
        }
    }
}
