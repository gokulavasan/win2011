instance matrixmult i1(level 3) inner
{
	entrypoint main[0];

	tunable mBlock = 256;
	tunable nBlock = 256;
	tunable pBlock = 256;

	control(level 2)
	{
		loop i() { spmd { fullrange = 0,1; ways = 1; iterblk = 1; } }
		loop j() { spmd { ways = 1; iterblk = 1; } }
		loop k() { }
		callsite matrixmult() 
		{ 
			target i2() { }  
		}
	}
}

instance matrixmult i2(level 2) inner
{
	tunable mBlock = 32;
	tunable nBlock = 32;
	tunable pBlock = 32;

	control(level 1)
	{
		loop i() { spmd { fullrange = 0,8; ways = 4; iterblk = 1; } }
		loop j() { spmd { ways = 2; iterblk = 1; } }
		loop k() { spmd { ways = 1; iterblk = 1;} }
		callsite matrixmult() 
		{ 
			target i3() { } 
		}
	}
}

instance matrixmult i3(level 1) inner
{
	tunable mBlock = 2;
	tunable nBlock = 2;
	tunable pBlock = 32;

	control(level 0)
	{
		loop i() { spmd { fullrange = 0,256; ways = 16; iterblk = 1; } }
		loop j() { spmd { ways = 16; iterblk = 1; } }
		loop k() { spmd { ways = 1; iterblk = 1; } }
		callsite matrixmult() 
		{ 
			target i4() { }
		}
	}
}

instance matrixmult i4(level 0) leaf { }

