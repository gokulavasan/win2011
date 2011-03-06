#ifndef MATRIX_MULT_H
#define MATRIX_MULT_H

// Simple functions for implementing Simple Dense Matrix-Matrix Multiplication.
//
// Given MxP matrix a, and PxN matrix b, M*1 y, they compute the update:
// c = a * b

task<inner> void matrixmult(in float a[M][P], in float b[P][N], inout float c[M][N]);
task<leaf> void matrixmult(in float a[M][P], in float b[P][N], inout float c[M][N]);

void reference(float** a, float** b, float** c, unsigned int M, unsigned int N, unsigned int P);

#endif
