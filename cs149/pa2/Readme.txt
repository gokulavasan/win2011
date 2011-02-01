Name : Josh Lang and Gokul Gunasekaran

We created a STMTreap.java in order to increase the throughput (operations/sec) on the Treap data structure using Software Transactional Memory (STM).

The following changes were done to the original algorithm in order to make it work better in a STM model.

i. Close-loop nested transacations - 'contains' : A major bottleneck for read heavy configurations. The contains function call was converted into a support function along with a recursive call. Both of the functions are atomic transactions. In the case when there is a conflict, the recursive call can roll back partially up the tree traversal. Compared to the original re-execution of the entire traversal encapsulated in a while loop, this greatly improves performance during conflicting cases.

ii. Reducing redundant writes to nodes : The add/remove implementations were modified to test data to be written back to the tree structure. Only if there was a change was the write back executed. Originally, all calls would save to the tree, in the worst example the root, every time no matter if the node had changed causing every call to cause a collision. By doing selective saving of nodes as described, many fewer collisions occur as the majority of node changes should have a small scope.

iii. Atomic Long variable - 'randStat' : The updates to the random state is done using AtomicLong class and getState/compareAndSet are used during modifications.

The above changes increased the throughput of the DeuceSTM implementation for LSA and TL2 algorithms on the STMTreap structure compared to the single threaded performance of CoarseLock implementation. 
