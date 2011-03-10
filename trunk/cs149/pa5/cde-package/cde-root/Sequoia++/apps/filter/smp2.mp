// FILTER IMAGE ...............................................................
// ............................................................................

// DO NOT MODIFY THIS INSTANCE!
instance filterImage i1(level 1) inner
{
	entrypoint main[0];

	data()
	{
		array real_image() { elements = 512,512; }
		array imag_image() { elements = 512,512; }
		array tR1() { elements = 512,512; }
		array tI1() { elements = 512,512; }
		array tR2() { elements = 512,512; }
		array tI2() { elements = 512,512; }
		array tR3() { elements = 512,512; }
		array tI3() { elements = 512,512; }
		array tR4() { elements = 512,512; }
		array tI4() { elements = 512,512; }
		array RO() { elements = 512,512; }
		array IO() { elements = 512,512; }
	}
	
	control(level 1)
	{
		callsite fftX() { target fftX_1() { } }
		callsite fftY() { target fftY_1() { } }
		callsite lowPass() { target lowPass_1() { } }
		callsite ifftY() { target ifftY_1() { } }
		callsite ifftX() { target ifftX_1() { } }
	}
}

// FFTX .......................................................................
// ............................................................................

instance fftX fftX_1(level 1) inner
{
 tunable mBlock = 512;
 tunable nBlock = 512;
 footprint (1024, bytes);
 data()
 {
  array oldReal() { elements = 512, 512;}
  array oldImag() { elements = 512, 512;}
  array newReal() { elements = 512, 512;}
  array newImag() { elements = 512, 512;}   
 }
 control (level 0)
 {
  loop i() { spmd { fullrange = 0,1; ways = 1; iterblk = 1; }}
  loop j() { spmd { ways = 1; iterblk = 1; }}
  callsite fftX() {target fftX_2() {}} 
 }
}

instance fftX fftX_2 (level 0) leaf {
footprint(1024, bytes);
 } 

// IFFTX ......................................................................
// ............................................................................

instance ifftX ifftX_1(level 1) inner
{
 tunable mBlock = 512;
 tunable nBlock = 512;

 footprint (1024, bytes);
 data()
 {
  array oldReal() { elements = 512, 512;}
  array oldImag() { elements = 512, 512;}
  array newReal() { elements = 512, 512;}
  array newImag() { elements = 512, 512;}   
 }
 control (level 0)
 {
  loop i() { spmd { fullrange = 0,1; ways = 1; iterblk = 1; }}
  loop j() { spmd { ways = 1; iterblk = 1; }}
  callsite ifftX() {target ifftX_2() {}} 
 }
}

instance ifftX ifftX_2 (level 0) leaf { 
 footprint (1024, bytes);
}

// FFTY .......................................................................
// ............................................................................

instance fftY fftY_1(level 1) inner
{
 tunable mBlock = 512;
 tunable nBlock = 512;

 footprint (1024, bytes);
 data()
 {
  array oldReal() { elements = 512, 512;}
  array oldImag() { elements = 512, 512;}
  array newReal() { elements = 512, 512;}
  array newImag() { elements = 512, 512;}   
 }
 control (level 0)
 {
  loop i() { spmd { fullrange = 0,1; ways = 1; iterblk = 1; }}
  loop j() { spmd { ways = 1; iterblk = 1; }}
  callsite fftY() {target fftY_2() {}} 
 }
}

instance fftY fftY_2 (level 0) leaf {
 footprint(1024, bytes);
 }

// IFFTY ......................................................................
// ............................................................................

instance ifftY ifftY_1(level 1) inner
{
 tunable mBlock = 512;
 tunable nBlock = 512;

 footprint (1024, bytes);
 data()
 {
  array oldReal() { elements = 512, 512;}
  array oldImag() { elements = 512, 512;}
  array newReal() { elements = 512, 512;}
  array newImag() { elements = 512, 512;}   
 }
 control (level 0)
 {
  loop i() { spmd { fullrange = 0,1; ways = 1; iterblk = 1; }}
  loop j() { spmd { ways = 1; iterblk = 1; }}
  callsite ifftY() {target ifftY_2() {}} 
 }
}

instance ifftY ifftY_2 (level 0) leaf {
 footprint(1024, bytes);
 } 

// LOW PASS ...................................................................
// ............................................................................

instance lowPass lowPass_1(level 1) inner
{
 tunable mBlock = 512;
 tunable nBlock = 512;
 footprint (1024, bytes); 
 data()
 {
  array oldReal() { elements = 512,512;}
  array oldImag() { elements = 512,512;}
  array newReal() { elements = 512,512;}
  array newImag() { elements = 512,512;}
 }
 control (level 0)
 {
  loop i() { spmd { fullrange = 0,1; ways = 1; iterblk = 1; }}
  loop j() { spmd { ways = 1; iterblk = 1;}}
  callsite lowPass() {target lowPass_2() {}}
 }
} 

instance lowPass lowPass_2 (level 0) leaf {
 footprint (1024, bytes);
 } 
