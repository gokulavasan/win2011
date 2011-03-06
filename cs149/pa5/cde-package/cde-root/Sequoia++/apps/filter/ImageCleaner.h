#ifndef __IMAGE_CLEANER__
#define __IMAGE_CLEANER__

// This is the entrypoint function
task<inner> void filterImage(inout float real_image[N][N], inout float imag_image[N][N], inout float tR1[N][N], inout float tI1[N][N], inout float tR2[N][N], inout float tI2[N][N], inout float tR3[N][N], inout float tI3[N][N], inout float tR4[N][N], inout float tI4[N][N], inout float RO[N][N], inout float IO[N][N]);

// These are the forward declarations for the tasks that you are responsible for implementing.
// You should not have to modify these declarations.
task<inner> void fftX(in float oldReal[MO][NO], in float oldImag[MO][NO], inout float newReal[MN][NN], inout float newImag[MN][NN], in int position_y);
task<leaf> void fftX(in float oldReal[MO][NO], in float oldImag[MO][NO], inout float newReal[MN][NN], inout float newImag[MN][NN], in int position_y);

task<inner> void ifftX(in float oldReal[MO][NO], in float oldImag[MO][NO], inout float newReal[MN][NN], inout float newImag[MN][NN], in int position_y);
task<leaf> void ifftX(in float oldReal[MO][NO], in float oldImag[MO][NO], inout float newReal[MN][NN], inout float newImag[MN][NN], in int position_y);

task<inner> void fftY(in float oldReal[MO][NO], in float oldImag[MO][NO], inout float newReal[MN][NN], inout float newImag[MN][NN], in int position_x);
task<leaf> void fftY(in float oldReal[MO][NO], in float oldImag[MO][NO], inout float newReal[MN][NN], inout float newImag[MN][NN], in int position_x);

task<inner> void ifftY(in float oldReal[MO][NO], in float oldImag[MO][NO], inout float newReal[MN][NN], inout float newImag[MN][NN], in int position_x);
task<leaf> void ifftY(in float oldReal[MO][NO], in float oldImag[MO][NO], inout float newReal[MN][NN], inout float newImag[MN][NN], in int position_x);

task<inner> void lowPass(in float oldReal[M][N], in float oldImag[M][N], inout float newReal[M][N], inout float newImag[M][N], in int position_x, in int position_y);
task<leaf> void lowPass(in float oldReal[M][N], in float oldImag[M][N], inout float newReal[M][N], inout float newImag[M][N], in int position_x, in int position_y);

#endif
