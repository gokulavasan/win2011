64 bit machine cuda
{
	managed cudaCPU level 3(4096 Mb @ 16 b) : 1 child;
	shared cudaGPU level 2(1024 Mb @ 16 b) : 8 children; 
	managed virtual shared cudaSM level 1(48 Kb @ 16 b) : 256 children;
	shared cudaThread level 0(48 Kb @ 16 b);
}
