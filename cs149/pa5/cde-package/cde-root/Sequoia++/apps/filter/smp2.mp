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

}

// IFFTX ......................................................................
// ............................................................................

instance ifftX ifftX_1(level 1) inner
{

}

// FFTY .......................................................................
// ............................................................................

instance fftY fftY_1(level 1) inner
{

}

// IFFTY ......................................................................
// ............................................................................

instance ifftY ifftY_1(level 1) inner
{

}

// LOW PASS ...................................................................
// ............................................................................

instance lowPass lowPass_1(level 1) inner
{

} 

