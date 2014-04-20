#version 110

uniform mat4 uMVPMatrix;
attribute vec4 vPosition;

void main() {
    // the matrix must be included as a modifier of gl_Position
    // Note that the uMVPMatrix factor *must be first* in order
    // for the matrix multiplication product to be correct.
    gl_Position = uMVPMatrix * vPosition;
}