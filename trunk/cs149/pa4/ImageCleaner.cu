
#include "ImageCleaner.h"

//YOU CAN CHANGE THESE TO MATCH YOUR IMAGE SIZE
#define SIZEX    512
#define SIZEY    512

#define PI 3.1415926536
#define BLOCK_SIZE 512
//----------------------------------------------------------------
// TODO:  CREATE NEW KERNELS HERE.  YOU CAN PLACE YOUR CALLS TO
//        THEM IN THE INDICATED SECTION INSIDE THE 'filterImage'
//        FUNCTION.
//
// BEGIN ADD KERNEL DEFINITIONS
//----------------------------------------------------------------

__device__ float GetElement(const float *image, const int row, int col)
{
    return image[row * SIZEX + col];
}

__device__ void SetElement(float *image, const int row, int col, float val)
{
    image[row * SIZEX + col] = val;
}



// This is an example kernel defintion that you should consider using
__global__ void DFTKernel (float *real_image, float *imag_image,
                            int size_x, int size_y, int direction, int forward)
{
  
  int blockId = blockIdx.x;
  int threadId = threadIdx.x;


  float real_Xvalue[SIZEX/BLOCK_SIZE];
  float imag_Xvalue[SIZEX/BLOCK_SIZE];
  __shared__ float real_vect[BLOCK_SIZE];
  __shared__ float imag_vect[BLOCK_SIZE];

  for (int z = 0; z < SIZEX/BLOCK_SIZE ; ++z){
    real_Xvalue[z] = 0;
    imag_Xvalue[z] = 0;
  }

  for (int i = 0; i < SIZEX/BLOCK_SIZE; ++i){
    
    if (direction == 0) {
      // Row direction 
      real_vect[threadId] = GetElement(real_image, blockId, threadId + i*BLOCK_SIZE);
      imag_vect[threadId] = GetElement(imag_image, blockId, threadId + i*BLOCK_SIZE);
    }  else {
      // Col direction
      real_vect[threadId] = GetElement(real_image, threadId + i*BLOCK_SIZE, blockId);
      imag_vect[threadId] = GetElement(imag_image, threadId + i*BLOCK_SIZE, blockId);
    }
  
    __syncthreads();
    
    // need to calculate the 'theta' value, based on thread id, 
    //   and also different for forward and reverse
    float theta = 0;
    for (int z = 0; z < SIZEX/BLOCK_SIZE; ++z){
      for (int e = 0; e < BLOCK_SIZE; ++e){
        if (forward == 1) {
          // Forward DFT
          theta = -2*PI*threadId*(e+z*BLOCK_SIZE)/SIZEX;
        } else {
          // Inverse DFT
          theta = 2*PI*threadId*(e+z*BLOCK_SIZE)/SIZEX;
        }
        real_Xvalue[z] += real_vect[e]*cosf(theta)-imag_vect[e]*sinf(theta);
        imag_Xvalue[z] += imag_vect[e]*cosf(theta)+real_vect[e]*sinf(theta);
      }
    }
    __syncthreads();
  }

  for (int z = 0; z < SIZEX/BLOCK_SIZE ;++z){
    if (forward != 1){
      real_Xvalue[z] /= SIZEX;
      imag_Xvalue[z] /= SIZEX;
    }
    if (forward == 1 && direction ==1) {
      const int eightX = SIZEX/8;
      const int eight7X = SIZEX - eightX;
      const int eightY = SIZEY/8;
      const int eight7Y = SIZEY - eightY;
  
      int x = threadId+z*BLOCK_SIZE;
      int y = blockId;
  
      if (!(x < eightX && y < eightY) &&
          !(x < eightX && y >= eight7Y) &&
          !(x >= eight7X && y < eightY) &&
          !(x >= eight7X && y >= eight7Y))
         {
         real_Xvalue[z]=0;
         imag_Xvalue[z]=0;
      }
    }

    if (direction ==0){
      SetElement(real_image, blockId, threadId+z*BLOCK_SIZE, real_Xvalue[z]);
      SetElement(imag_image, blockId, threadId+z*BLOCK_SIZE, imag_Xvalue[z]);
    } else {
      SetElement(real_image, threadId+z*BLOCK_SIZE, blockId, real_Xvalue[z]);
      SetElement(imag_image, threadId+z*BLOCK_SIZE, blockId, imag_Xvalue[z]);
    }
  }

}

//----------------------------------------------------------------
// END ADD KERNEL DEFINTIONS
//----------------------------------------------------------------

__host__ float filterImage(float *real_image, float *imag_image, int size_x, int size_y)
{
  // check that the sizes match up
  assert(size_x == SIZEX);
  assert(size_y == SIZEY);

  int matSize = size_x * size_y * sizeof(float);

  // These variables are for timing purposes
  float transferDown = 0, transferUp = 0, execution = 0;
  cudaEvent_t start,stop;
  CUDA_ERROR_CHECK(cudaEventCreate(&start));
  CUDA_ERROR_CHECK(cudaEventCreate(&stop));

  // Create a stream and initialize it
  cudaStream_t filterStream;
  CUDA_ERROR_CHECK(cudaStreamCreate(&filterStream));

  // Alloc space on the device
  float *device_real, *device_imag;
  CUDA_ERROR_CHECK(cudaMalloc((void**)&device_real, matSize));
  CUDA_ERROR_CHECK(cudaMalloc((void**)&device_imag, matSize));

  // Start timing for transfer down
  CUDA_ERROR_CHECK(cudaEventRecord(start,filterStream));
  
  // Here is where we copy matrices down to the device 
  CUDA_ERROR_CHECK(cudaMemcpy(device_real,real_image,matSize,cudaMemcpyHostToDevice));
  CUDA_ERROR_CHECK(cudaMemcpy(device_imag,imag_image,matSize,cudaMemcpyHostToDevice));
  
  // Stop timing for transfer down
  CUDA_ERROR_CHECK(cudaEventRecord(stop,filterStream));
  CUDA_ERROR_CHECK(cudaEventSynchronize(stop));
  CUDA_ERROR_CHECK(cudaEventElapsedTime(&transferDown,start,stop));

  // Start timing for the execution
  CUDA_ERROR_CHECK(cudaEventRecord(start,filterStream));
 
  //----------------------------------------------------------------
  // TODO:  YOU SHOULD PLACE ALL YOUR KERNEL EXECUTIONS
  //        HERE BETWEEN THE CALLS FOR STARTING AND
  //        FINISHING TIMING FOR THE EXECUTION PHASE
  //
  // BEGIN ADD KERNEL CALLS
  //----------------------------------------------------------------

  // This is an example kernel call, you should feel free to create
  // as many kernel calls as you feel are needed for your program
  // Each of the parameters are as follows:
  //    1. Number of thread blocks, can be either int or dim3 (see CUDA manual)
  //    2. Number of threads per thread block, can be either int or dim3 (see CUDA manual)
  //    3. Always should be '0' unless you read the CUDA manual and learn about dynamically allocating shared memory
  //    4. Stream to execute kernel on, should always be 'filterStream'
  //
  // Also note that you pass the pointers to the device memory to the kernel call
  DFTKernel<<<size_y,BLOCK_SIZE,0,filterStream>>>(device_real,device_imag,size_x,size_y,0,1);
  DFTKernel<<<size_x,BLOCK_SIZE,0,filterStream>>>(device_real,device_imag,size_x,size_y,1,1);


 
  DFTKernel<<<size_y,BLOCK_SIZE,0,filterStream>>>(device_real,device_imag,size_x,size_y,0,0);
  DFTKernel<<<size_x,BLOCK_SIZE,0,filterStream>>>(device_real,device_imag,size_x,size_y,1,0);



  //---------------------------------------------------------------- 
  // END ADD KERNEL CALLS
  //----------------------------------------------------------------
  // Finish timimg for the execution 
  CUDA_ERROR_CHECK(cudaEventRecord(stop,filterStream));
  CUDA_ERROR_CHECK(cudaEventSynchronize(stop));
  CUDA_ERROR_CHECK(cudaEventElapsedTime(&execution,start,stop));

  // Start timing for the transfer up
  CUDA_ERROR_CHECK(cudaEventRecord(start,filterStream));

  // Here is where we copy matrices back from the device 
  CUDA_ERROR_CHECK(cudaMemcpy(real_image,device_real,matSize,cudaMemcpyDeviceToHost));
  CUDA_ERROR_CHECK(cudaMemcpy(imag_image,device_imag,matSize,cudaMemcpyDeviceToHost));

  // Finish timing for transfer up
  CUDA_ERROR_CHECK(cudaEventRecord(stop,filterStream));
  CUDA_ERROR_CHECK(cudaEventSynchronize(stop));
  CUDA_ERROR_CHECK(cudaEventElapsedTime(&transferUp,start,stop));

  // Synchronize the stream
  CUDA_ERROR_CHECK(cudaStreamSynchronize(filterStream));
  // Destroy the stream
  CUDA_ERROR_CHECK(cudaStreamDestroy(filterStream));
  // Destroy the events
  CUDA_ERROR_CHECK(cudaEventDestroy(start));
  CUDA_ERROR_CHECK(cudaEventDestroy(stop));

  // Free the memory
  CUDA_ERROR_CHECK(cudaFree(device_real));
  CUDA_ERROR_CHECK(cudaFree(device_imag));

  // Dump some usage statistics
  printf("CUDA IMPLEMENTATION STATISTICS:\n");
  printf("  Host to Device Transfer Time: %f ms\n", transferDown);
  printf("  Kernel(s) Execution Time: %f ms\n", execution);
  printf("  Device to Host Transfer Time: %f ms\n", transferUp);
  float totalTime = transferDown + execution + transferUp;
  printf("  Total CUDA Execution Time: %f ms\n\n", totalTime);
  // Return the total time to transfer and execute
  return totalTime;
}

