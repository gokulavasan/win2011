SRC=ImageCleaner.sq main.sq
SQ_FLAG=-s6 -O -d -lsq_cstdio -lsq_cassert -lsq_jpeg -lsq_ctime -lsq_cstring -lsq_cmath

smp2:
	./sq++.cde $(SRC) smp2.m smp2.mp $(SQ_FLAG)
	
cuda:
	./sq++.cde $(SRC) cuda.m cuda.mp $(SQ_FLAG)	
	
clean:
	rm -rf out debug *_out.jpg
