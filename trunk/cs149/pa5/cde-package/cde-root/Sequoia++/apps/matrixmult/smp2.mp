instance matrixmult i(level 1) inner
{
	entrypoint main[0];
	footprint(215880, bytes);

	tunable mBlock = 128;
	tunable nBlock = 256;
	tunable pBlock = 256;

	data()
	{
		array a() { elements = 256, 256; }
		array b() { elements = 256, 256; }
		array c() { elements = 256, 256; }
	}

	control(level 0)
	{
		loop i() { spmd { fullrange = 0,2; ways = 2; iterblk = 1; } }
		loop j() { spmd { ways = 1; iterblk = 1; } }
		loop k() { spmd { ways = 1; iterblk = 1; } swp = 2; }
		callsite matrixmult() { target l() {} } 
	}
}

instance matrixmult l(level 0) leaf { }
