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
        System.out.println("Method: "+cfg.getMethod().getName().toString());
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

        System.out.println("Initialization completed.");
    }



    public void postprocess(ControlFlowGraph cfg) {
        System.out.println("entry: " + entry.toString());
        for (int i=1; i<in.length; i++){
            System.out.println(i + " in:  " + in[i].toString());
            System.out.println(i + " out: " + out[i].toString());
        }
        System.out.println("exit: " + exit.toString());
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
        public static Set<String> universalSet;
        public VarSet() { set = new TreeSet<String>(); }

        public void setToTop() { set = new TreeSet<String>(); }
        public void setToBottom() { set = new TreeSet<String>(universalSet); }

        public void meetWith(Flow.DataflowObject o) 
        {
            VarSet a = (VarSet)o;
            set.addAll(a.set);
        }

        public void copy(Flow.DataflowObject o) 
        {
            VarSet a = (VarSet) o;
            set = new TreeSet<String>(a.set);
        }

        @Override
        public boolean equals(Object o) 
        {
            if (o instanceof VarSet) 
            {
                VarSet a = (VarSet) o;
                return set.equals(a.set);
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
	}
	protected void kill (Quad quad) 
	{
		//String operandString = operand.getRegister().toString();
		if (quad.getOperator()==Operator.NullCheck.NULL_CHECK.INSTANCE)
	     	{
	    		set.remove(quad.getUsedRegisters());
             	}
	}
    }

    /*public static class DefinitionSet implements Flow.DataflowObject {
        public static final DefinitionSet allDefinitions = new DefinitionSet();

        /**
         * the set of definitions
         
        private HashSet<Quad> definitions;

        /**
         * Constructor
         
        public DefinitionSet() {
            definitions = new HashSet<Quad>();
        }

        /**
         * Sets the definition set to the top value, which is an empty set
         
        public void setToTop() {
            definitions.clear();
        }

        /**
         * Sets the definition set to the bottom value, which is the
         * universal set
         
        public void setToBottom() {
            copy(allDefinitions);
        }

        /**
         * Performs a meet with another definition set. This is simply the
         * union of both sets.
         * @param moreDefinitions the set of definitions to meet with
         
        public void meetWith(Flow.DataflowObject moreDefinitions) {
            definitions.addAll(((DefinitionSet)moreDefinitions).definitions);
        }

        /**
         * Copies another definition set by emptying all existing
         * definitions and copying the other set's definitions.
         * @param otherDefinitions the definition set to copy
         
        public void copy(Flow.DataflowObject otherDefinitions) {
            setToTop();
            meetWith(otherDefinitions);
        }

        /**
         * @return a string representation of the definition set.
         
        @Override
        public String toString() {
            // create a sorted set of the quad IDs
            TreeSet<Integer> quadIDs = new TreeSet<Integer>();
            for (Quad def : definitions) {
                quadIDs.add(def.getID());
            }

            // return the sorted ID set string
            return quadIDs.toString();
        }

        /**
         * Checks whether this definition set is equal to another.
         * @param otherDefinitions the definition set to compare with
         * @return true if the two definition sets are equal, false
         * otherwise
         
        @Override
        public boolean equals(Object otherDefinitions) {
            if(otherDefinitions instanceof DefinitionSet) {
                return definitions.equals(((DefinitionSet)otherDefinitions).definitions);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        /**
         * Adds a new definition to this definition set.
         * @param quad the quad holding the definition to add
         
        protected void addDefinition(Quad quad) {
            definitions.add(quad.getDefinedRegisters());
        }

        
        // Kills definitions that define the given operand
        // @param operand the operand to kill definitions with
         
        protected void kill(Operand.RegisterOperand operand) {
            // the operand's string
            String operandString = operand.getRegister().toString();
            // iterate over the definitions in this set
            HashSet<Quad> toRemove = new HashSet<Quad>();
            /*for (Quad quad : definitions) {
                // iterate over defined registers in the quad
                for (Operand.RegisterOperand def : quad.getDefinedRegisters())
                    // if quad defines the same register, kill it
                    if(operandString.equals(def.getRegister().toString()))
                        toRemove.add(quad);
            }
	    for (Quad quad : definitions) 
	    {
             if (quad.getOperator()==Operator.NullCheck.INSTANCE)
	     {
	    	toRemove.add(quad.getSrc);
             }	
            } 
            // remove killed definitions from the definition set
            definitions.removeAll(toRemove);
        }
    }*/


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
