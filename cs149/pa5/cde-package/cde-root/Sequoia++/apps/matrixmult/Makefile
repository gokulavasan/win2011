SRC=matrixmult.sq main.sq
SQ_FLAG=-s6 -O -d -lsq_cstdio -lsq_ctime 

smp2:
	./sq++.cde $(SRC) smp2.m smp2.mp $(SQ_FLAG)
	
cuda:
	./sq++.cde $(SRC) cuda.m cuda.mp $(SQ_FLAG)	
	
clean:
	rm -rf out debug 
