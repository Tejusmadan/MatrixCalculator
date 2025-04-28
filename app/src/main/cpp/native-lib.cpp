#include <jni.h>
#include <vector>
#include <cmath>

inline int idx(int cols, int r, int c) { return r * cols + c; }

static std::vector<double> invertImpl(int n, const std::vector<double>& M) {
    std::vector<double> A(n * 2 * n, 0.0), Inv(n*n);
    // build [M|I]
    for(int i=0;i<n;++i){
        for(int j=0;j<n;++j) A[i*(2*n)+j] = M[idx(n,i,j)];
        A[i*(2*n)+(n+i)] = 1.0;
    }
    // Gauss–Jordan
    for(int i=0;i<n;++i){
        double piv = A[i*(2*n)+i]; int sel = i;
        for(int r=i+1;r<n;++r){
            double v = std::fabs(A[r*(2*n)+i]);
            if(v > std::fabs(piv)){ piv = A[r*(2*n)+i]; sel = r; }
        }
        if(std::fabs(piv) < 1e-12) return {};
        if(sel!=i)
            for(int c=0;c<2*n;++c)
                std::swap(A[i*(2*n)+c], A[sel*(2*n)+c]);
        for(int c=0;c<2*n;++c) A[i*(2*n)+c] /= piv;
        for(int r=0;r<n;++r) if(r!=i){
                double f = A[r*(2*n)+i];
                for(int c=0;c<2*n;++c)
                    A[r*(2*n)+c] -= f * A[i*(2*n)+c];
            }
    }
    for(int i=0;i<n;++i)
        for(int j=0;j<n;++j)
            Inv[idx(n,i,j)] = A[i*(2*n)+(n+j)];
    return Inv;
}

extern "C" {

// add
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MainActivity_addMatrices(
        JNIEnv* env,
        jobject /*thiz*/,
        jint rows,
        jint cols,
        jdoubleArray aArr,
        jdoubleArray bArr
) {
    int R = rows, C = cols, N = R*C;
    std::vector<double> A(N), B(N), out(N);
    env->GetDoubleArrayRegion(aArr, 0, N, A.data());
    env->GetDoubleArrayRegion(bArr, 0, N, B.data());
    for(int i=0;i<N;++i) out[i] = A[i] + B[i];
    jdoubleArray result = env->NewDoubleArray(N);
    env->SetDoubleArrayRegion(result, 0, N, out.data());
    return result;
}

// subtract
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MainActivity_subtractMatrices(
        JNIEnv* env,
        jobject /*thiz*/,
        jint rows,
        jint cols,
        jdoubleArray aArr,
        jdoubleArray bArr
) {
    int R = rows, C = cols, N = R*C;
    std::vector<double> A(N), B(N), out(N);
    env->GetDoubleArrayRegion(aArr, 0, N, A.data());
    env->GetDoubleArrayRegion(bArr, 0, N, B.data());
    for(int i=0;i<N;++i) out[i] = A[i] - B[i];
    jdoubleArray result = env->NewDoubleArray(N);
    env->SetDoubleArrayRegion(result, 0, N, out.data());
    return result;
}

// multiply
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MainActivity_multiplyMatrices(
        JNIEnv* env,
        jobject /*thiz*/,
        jint rA, jint cA, jint rB, jint cB,
        jdoubleArray aArr,
        jdoubleArray bArr
) {
    int RA = rA, CA = cA, RB = rB, CB = cB;
    std::vector<double> A(RA*CA), B(RB*CB), out(RA*CB, 0.0);
    env->GetDoubleArrayRegion(aArr, 0, RA*CA, A.data());
    env->GetDoubleArrayRegion(bArr, 0, RB*CB, B.data());
    for(int i=0;i<RA;++i)
        for(int j=0;j<CB;++j){
            double sum = 0;
            for(int k=0;k<CA;++k)
                sum += A[idx(CA,i,k)] * B[idx(CB,k,j)];
            out[idx(CB,i,j)] = sum;
        }
    jdoubleArray result = env->NewDoubleArray(RA*CB);
    env->SetDoubleArrayRegion(result, 0, RA*CB, out.data());
    return result;
}

// divide = A × B⁻¹
JNIEXPORT jdoubleArray JNICALL
Java_com_example_matrixcalculator_MainActivity_divideMatrices(
        JNIEnv* env,
        jobject /*thiz*/,
        jint rA, jint cA, jint rB, jint cB,
        jdoubleArray aArr,
        jdoubleArray bArr
) {
    int RA = rA, CA = cA, N = rB;  // rB==cB==CA
    std::vector<double> A(RA*CA), B(N*N);
    env->GetDoubleArrayRegion(aArr, 0, RA*CA, A.data());
    env->GetDoubleArrayRegion(bArr, 0, N*N, B.data());
    auto invB = invertImpl(N, B);
    if(invB.empty()) return env->NewDoubleArray(0);
    std::vector<double> out(RA*N, 0.0);
    for(int i=0;i<RA;++i)
        for(int j=0;j<N;++j){
            double sum=0;
            for(int k=0;k<CA;++k)
                sum += A[idx(CA,i,k)] * invB[idx(N,k,j)];
            out[idx(N,i,j)] = sum;
        }
    jdoubleArray result = env->NewDoubleArray(RA*N);
    env->SetDoubleArrayRegion(result, 0, RA*N, out.data());
    return result;
}

}  // extern "C"
