//package cs149.stm;
import java.util.concurrent.atomic.AtomicLong;

public class STMTreap implements IntSet {
    static class Node {
        final int key;
        final int priority;
        Node left;
        Node right;

        Node(final int key, final int priority) {
            this.key = key;
            this.priority = priority;
        }
        public String toString() {
            return "Node[key=" + key + ", prio=" + priority +
                    ", left=" + (left == null ? "null" : String.valueOf(left.key)) +
                    ", right=" + (right == null ? "null" : String.valueOf(right.key)) + "]";
        }
    }

    private void wrong(int i)
    {
     if(i==1)
      System.out.println("Priority mismatch; For details use PrintGraph()");
     else
      System.out.println("Key mismatch; For details use PrintGraph()");
    }
    private void workerCheck(final Node node)
    {
    	if (node == null)
           return;
        else
        {
	 if(node.right != null)
         {
           if(node.priority < node.right.priority)
	   {
		wrong(1);
           }
	   else if(node.key >= node.right.key)
           {
                wrong(2); 
	   }
	 }
	 if(node.left != null)
         {
           if(node.priority < node.left.priority)
           {
                wrong(1);
	   }
	   else if(node.key <= node.left.key)
	   {
		wrong(2);
	   }
         }
         workerCheck(node.left);
	 workerCheck(node.right); 
       }
    }	 
    
    
    public void checkUrself()
    {
     workerCheck(root);
    } 
     
    public void PrintGraph()
    {
	printoutNode(root);
    } 
    private void printoutNode(final Node node)
    {
      if(node != null)
      {
        System.out.println(node);
        printoutNode(node.left);
        printoutNode(node.right);
      }
    }
 //   private long randState = 0;
    private AtomicLong randState = new AtomicLong();
    private Node root;

    @org.deuce.Atomic
    public boolean contains(final int key) {
        return containsRecursive (root,key);
    }

    public boolean  containsRecursive (final Node node, final int key)
    {
        if (node == null){
          return false;
        }
        if (key == node.key) {
            return true;
        }
        if ( key < node.key) {
           if (node.left == null){
             return false;
           } else {
             return  containsRecursive(node.left,key);
           }
        } else {
          if (node.right == null){
             return false;
           } else {
             return  containsRecursive(node.right,key);
           } 
        }
    }

    @org.deuce.Atomic
    public void add(final int key) {
        Node temp = addImpl(root, key);
        if (temp != root){
          root = temp;
        }
    }

    private Node addImpl(final Node node, final int key) {
        Node temp;
        if (node == null) {
            return new Node(key, randPriority());
        }
        else if (key == node.key) {
            // no insert needed
            return node;
        }
        else if (key < node.key) {
            temp = addImpl(node.left, key);
            if (node.left != temp){
              node.left = temp;
            }
            if (node.left.priority > node.priority) {
                return rotateRight(node);
            }
            return node;
        }
        else {
            temp = addImpl(node.right, key);
            if (node.right != temp){
              node.right = temp;
            }
            if (node.right.priority > node.priority) {
                return rotateLeft(node);
            }
            return node;
        }
    }

    private int randPriority() {
        // The constants in this 64-bit linear congruential random number
        // generator are from http://nuclear.llnl.gov/CNP/rng/rngman/node4.html
	long c, temp;
	do 
        {
	 temp = randState.get();	
         c = (temp *  2862933555777941757L)+ 3037000493L;
        } while (!randState.compareAndSet(temp,c)); 
        return (int)(c >> 30);
    //    randState = randState * 2862933555777941757L + 3037000493L;
    //	return (int) (randState >> 30);
    }

    private Node rotateRight(final Node node) {
        //       node                  nL
        //     /      \             /      \
        //    nL       z     ==>   x       node
        //  /   \                         /   \
        // x   nLR                      nLR   z
        final Node nL = node.left;
        node.left = nL.right;
        nL.right = node;
        return nL;
    }

    private Node rotateLeft(final Node node) {
        final Node nR = node.right;
        node.right = nR.left;
        nR.left = node;
        return nR;
    }
    @org.deuce.Atomic
    public void remove(final int key) {
        Node temp = removeImpl(root, key);
        if (temp != root){
          root = temp;
        }
    }

    private Node removeImpl(final Node node, final int key) {
        Node temp;
        if (node == null) {
            // not present, nothing to do
            return null;
        }
        else if (key == node.key) {
            if (node.left == null) {
                // splice out this node
                return node.right;
            }
            else if (node.right == null) {
                return node.left;
            }
            else {
                // Two children, this is the hardest case.  We will pretend
                // that node has -infinite priority, move it down, then retry
                // the removal.
                if (node.left.priority > node.right.priority) {
                    // node.left needs to end up on top
                    final Node top = rotateRight(node);
                    top.right = removeImpl(top.right, key);
                    return top;
                } else {
                    final Node top = rotateLeft(node);
                    top.left = removeImpl(top.left, key);
                    return top;
                }
            }
        }
        else if (key < node.key) {
            temp = removeImpl(node.left, key);
            if (node.left != temp){
              node.left = temp;
            }
            return node;
        }
        else {
            temp = removeImpl(node.right, key);
            if (node.right != temp){
              node.right = temp;
            }
            return node;
        }
    }
}
