/*
 * (C) Copyright 2015 Kai Burjack

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 */
package org.joml;

/**
 * Useful geometry methods.
 * 
 * @author Kai Burjack
 * @author Richard Greenlees
 */
public class GeometryUtils {

    /**
     * Calculate the frustum planes of the given transformation matrix, which
     * can be a projection matrix or a combined modelview-projection matrix, and store the result
     * in the given <code>left</code>, <code>right</code>, <code>bottom</code>, <code>top</code>, <code>near</code> and <code>far</code> parameters.
     * <p>
     * Generally, this method computes the frustum planes in the local frame of
     * any coordinate system that existed before the given <code>mvp</code>
     * transformation was applied to it.
     * <p>
     * Each of the six planes <code>left</code>, <code>right</code>, <code>bottom</code>, <code>top</code>, <code>near</code> and <code>far</code>
     * are given in plane equations:
     * <tt>a*x + b*y + c*z + d = 0</tt>, where the {@link Vector4f} components
     * hold the <tt>(a, b, c, d)</tt> values of each plane equation.
     * <p>
     * The plane normals, which are the <tt>(a, b, c)</tt> parameters, are directed "inwards" of the frustum.
     * Any plane/point test using <tt>a*x + b*y + c*z + d</tt> therefore will yield a result greater than zero
     * if the point is within the frustum (i.e. at the <i>positive</i> side of each frustum plane).
     * 
     * @param mvp
     *            the transformation matrix whose frustum planes should be
     *            computed
     * @param left
     *            will hold the plane equation components of the left frustum plane
     * @param right
     *            will hold the plane equation components of the right frustum plane
     * @param bottom
     *            will hold the plane equation components of the bottom frustum plane
     * @param top
     *            will hold the plane equation components of the top frustum plane
     * @param near
     *            will hold the plane equation components of the near frustum plane
     * @param far
     *            will hold the plane equation components of the far frustum plane
     */
    public static void calculateFrustumPlanes(Matrix4f mvp, Vector4f left, Vector4f right, Vector4f bottom, Vector4f top, Vector4f near, Vector4f far) {
        // "http://www.cs.otago.ac.nz/postgrads/alexis/planeExtraction.pdf"
        // changed to use OpenGL's right-handed coordinate system
        // (and use column-major matrix indices, which the paper did not use,
        //  although it said it would in the code-section at the end).
        right.set(mvp.m03 - mvp.m00,
                  mvp.m13 - mvp.m10,
                  mvp.m23 - mvp.m20,
                  mvp.m33 - mvp.m30).normalize3();
        left.set(mvp.m03 + mvp.m00,
                 mvp.m13 + mvp.m10,
                 mvp.m23 + mvp.m20,
                 mvp.m33 + mvp.m30).normalize3();
        bottom.set(mvp.m03 + mvp.m01,
                   mvp.m13 + mvp.m11,
                   mvp.m23 + mvp.m21,
                   mvp.m33 + mvp.m31).normalize3();
        top.set(mvp.m03 - mvp.m01,
                mvp.m13 - mvp.m11,
                mvp.m23 - mvp.m21,
                mvp.m33 - mvp.m31).normalize3();
        near.set(mvp.m03 + mvp.m02,
                 mvp.m13 + mvp.m12,
                 mvp.m23 + mvp.m22,
                 mvp.m33 + mvp.m32).normalize3();
        far.set(mvp.m03 - mvp.m02,
                mvp.m13 - mvp.m12,
                mvp.m23 - mvp.m22,
                mvp.m33 - mvp.m32).normalize3();
    }

    /**
     * Calculate the normal of a surface defined by points <code>v1</code>, <code>v2</code> and <code>v3</code> and
     * store it in <code>dest</code>.
     * 
     * @param v1
     *          the first position
     * @param v2
     *          the second position
     * @param v3
     *          the third position
     * @param dest
     *          will hold the result
     */
    public static void normal(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f dest) {
        dest.x = ((v2.y - v1.y) * (v3.z - v1.z)) - ((v2.z - v1.z) * (v3.y - v1.y));
        dest.y = ((v2.z - v1.z) * (v3.x - v1.x)) - ((v2.x - v1.x) * (v3.z - v1.z));
        dest.z = ((v2.x - v1.x) * (v3.y - v1.y)) - ((v2.y - v1.y) * (v3.x - v1.x));
        dest.normalize();
    }

    /**
     * Calculate the surface tangent for the three supplied vertices and UV
     * coordinates and store the result in <code>dest</code>.
     *
     * @param v1 XYZ of first vertex
     * @param uv1 UV of first vertex
     * @param v2 XYZ of second vertex
     * @param uv2 UV of second vertex
     * @param v3 XYZ of third vertex
     * @param uv3 UV of third vertex
     * @param dest the tangent will be stored here
     */
    public static void tangent(Vector3f v1, Vector2f uv1, Vector3f v2, Vector2f uv2, Vector3f v3, Vector2f uv3, Vector3f dest) {
        float DeltaV1 = uv2.y - uv1.y;
        float DeltaV2 = uv3.y - uv1.y;

        float f = 1.0f / ((uv2.x - uv1.x) * DeltaV2 - (uv3.x - uv1.x) * DeltaV1);

        dest.x = f * (DeltaV2 * (v2.x - v1.x) - DeltaV1 * (v3.x - v1.x));
        dest.y = f * (DeltaV2 * (v2.y - v1.y) - DeltaV1 * (v3.y - v1.y));
        dest.z = f * (DeltaV2 * (v2.z - v1.z) - DeltaV1 * (v3.z - v1.z));

        dest.normalize();
    }

    /**
     * Calculate the surface bitangent for the three supplied vertices and UV
     * coordinates and store the result in <code>dest</code>.
     *
     * @param v1 XYZ of first vertex
     * @param uv1 UV of first vertex
     * @param v2 XYZ of second vertex
     * @param uv2 UV of second vertex
     * @param v3 XYZ of third vertex
     * @param uv3 UV of third vertex
     * @param dest the binormal will be stored here
     */
    public static void bitangent(Vector3f v1, Vector2f uv1, Vector3f v2, Vector2f uv2, Vector3f v3, Vector2f uv3, Vector3f dest) {
        float DeltaU1 = uv2.x - uv1.x;
        float DeltaU2 = uv3.x - uv1.x;

        float f = 1.0f / (DeltaU1 * (uv3.y - uv1.y) - DeltaU2 * (uv2.y - uv1.y));

        dest.x = f * (-DeltaU2 * (v2.x - v1.x) - DeltaU1 * (v3.x - v1.x));
        dest.y = f * (-DeltaU2 * (v2.y - v1.y) - DeltaU1 * (v3.y - v1.y));
        dest.z = f * (-DeltaU2 * (v2.z - v1.z) - DeltaU1 * (v3.z - v1.z));

        dest.normalize();
    }

    /**
     * Calculate the surface tangent and bitangent for the three supplied vertices
     * and UV coordinates and store the result in <code>dest</code>.
     *
     * @param v1 XYZ of first vertex
     * @param uv1 UV of first vertex
     * @param v2 XYZ of second vertex
     * @param uv2 UV of second vertex
     * @param v3 XYZ of third vertex
     * @param uv3 UV of third vertex
     * @param destTangent the tangent will be stored here
     * @param destBitangent the bitangent will be stored here
     */
    public static void tangentBitangent(Vector3f v1, Vector2f uv1, Vector3f v2, Vector2f uv2, Vector3f v3, Vector2f uv3, Vector3f destTangent, Vector3f destBitangent) {
        float DeltaV1 = uv2.y - uv1.y;
        float DeltaV2 = uv3.y - uv1.y;
        float DeltaU1 = uv2.x - uv1.x;
        float DeltaU2 = uv3.x - uv1.x;

        float f = 1.0f / (DeltaU1 * DeltaV2 - DeltaU2 * DeltaV1);

        destTangent.x = f * (DeltaV2 * (v2.x - v1.x) - DeltaV1 * (v3.x - v1.x));
        destTangent.y = f * (DeltaV2 * (v2.y - v1.y) - DeltaV1 * (v3.y - v1.y));
        destTangent.z = f * (DeltaV2 * (v2.z - v1.z) - DeltaV1 * (v3.z - v1.z));

        destTangent.normalize();

        destBitangent.x = f * (-DeltaU2 * (v2.x - v1.x) - DeltaU1 * (v3.x - v1.x));
        destBitangent.y = f * (-DeltaU2 * (v2.y - v1.y) - DeltaU1 * (v3.y - v1.y));
        destBitangent.z = f * (-DeltaU2 * (v2.z - v1.z) - DeltaU1 * (v3.z - v1.z));

        destBitangent.normalize();
    }

}
