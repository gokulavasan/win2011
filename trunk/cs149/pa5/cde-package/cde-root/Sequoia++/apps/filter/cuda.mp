// FILTER IMAGE ...............................................................
// ............................................................................

// DO NOT MODIFY THIS INSTANCE!
instance filterImage i1(level 3) inner
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
	control(level 3)
	{
		callsite fftX() { target fftX_3() {} }
		callsite fftY() { target fftY_3() {} }
		callsite lowPass() { target lowPass_3() {} }
		callsite ifftY() { target ifftY_3() {} }
		callsite ifftX() { target ifftX_3() {} }
	}
}

// FFTX .......................................................................
// ............................................................................

instance fftX fftX_3(level 3) inner
{

}

// IFFTX ......................................................................
// ............................................................................

instance ifftX ifftX_3(level 3) inner
{

}

// FFTY .......................................................................
// ............................................................................

instance fftY fftY_3(level 3) inner
{

}

// IFFTY ......................................................................
// ............................................................................

instance ifftY ifftY_3(level 3) inner
{

}

// LOW PASS ...................................................................
// ............................................................................

instance lowPass lowPass_3(level 3) inner
{

}

