package com.example.piceditor.utils

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import java.util.*

/**
 * All points of polygon must be ordered by clockwise along<br></br>
 * Created by admin on 5/4/2016.
 */
object GeometryUtils {
    fun isInCircle(center: PointF, radius: Float, p: PointF): Boolean {
        return Math.sqrt(((center.x - p.x) * (center.x - p.x) + (center.y - p.y) * (center.y - p.y)).toDouble()) <= radius
    }

    /**
     * Return true if the given point is contained inside the boundary.
     * See: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     *
     * @param test The point to check
     * @return true if the point is inside the boundary, false otherwise
     */
    fun contains(points: List<PointF>, test: PointF): Boolean {
        var i: Int
        var j: Int
        var result = false
        i = 0
        j = points.size - 1
        while (i < points.size) {
            if (points[i].y > test.y != points[j].y > test.y &&
                test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x
            ) {
                result = !result
            }
            j = i++
        }
        return result
    }

    fun createRectanglePath(outPath: Path, width: Float, height: Float, corner: Float) {
        val pointList: MutableList<PointF> = ArrayList()
        pointList.add(PointF(0F, 0F))
        pointList.add(PointF(width, 0F))
        pointList.add(PointF(width, height))
        pointList.add(PointF(0F, height))
        createPathWithCircleCorner(outPath, pointList, corner)
    }

    fun createRegularPolygonPath(outPath: Path, size: Float, vertexCount: Int, corner: Float) {
        createRegularPolygonPath(outPath, size, size / 2, size / 2, vertexCount, corner)
    }

    fun createRegularPolygonPath(
        outPath: Path,
        size: Float,
        centerX: Float,
        centerY: Float,
        vertexCount: Int,
        corner: Float
    ) {
        val section = (2.0 * Math.PI / vertexCount).toFloat()
        val radius = size / 2
        val pointList: MutableList<PointF> = ArrayList()
        pointList.add(
            PointF(
                (centerX + radius * Math.cos(0.0)).toFloat(),
                (centerY + radius * Math.sin(0.0)).toFloat()
            )
        )
        for (i in 1 until vertexCount) {
            pointList.add(
                PointF(
                    (centerX + radius * Math.cos((section * i).toDouble())).toFloat(),
                    (centerY + radius * Math.sin((section * i).toDouble())).toFloat()
                )
            )
        }
        createPathWithCircleCorner(outPath, pointList, corner)
    }

    fun shrinkPathCollageUsingMap(
        pointList: List<PointF>,
        space: Float,
        map: HashMap<PointF, PointF>
    ): List<PointF> {
        val result: MutableList<PointF> = ArrayList()
        for (p in pointList) {
            val add = map[p]
            result.add(PointF(p.x + add!!.x * space, p.y + add.y * space))
        }
        return result
    }

    /**
     * Resolve case frame collage 3_3
     *
     * @param pointList
     * @param space
     * @param bound
     * @return shrank points
     */
    fun shrinkPathCollage_3_3(
        pointList: List<PointF>,
        centerPointIdx: Int,
        space: Float,
        bound: RectF?
    ): List<PointF> {
        val result: MutableList<PointF> = ArrayList()
        val center = pointList[centerPointIdx]
        var left: PointF? = null
        var right: PointF? = null
        left = if (centerPointIdx > 0) {
            pointList[centerPointIdx - 1]
        } else {
            pointList[pointList.size - 1]
        }
        right = if (centerPointIdx < pointList.size - 1) {
            pointList[centerPointIdx + 1]
        } else {
            pointList[0]
        }
        var spaceX: Float
        var spaceY: Float
        for (p in pointList) {
            val pointF = PointF()
            spaceX = space
            spaceY = space
            if (bound != null) {
                if (bound.left == 0f && p.x < center.x || bound.right == 1f && p.x >= center.x) {
                    spaceX = 2 * space
                }
                if (bound.top == 0f && p.y < center.y || bound.bottom == 1f && p.y >= center.y) {
                    spaceY = 2 * space
                }
            }
            if (left.x == right.x) {
                if (left.x < center.x) {
                    if (p.x <= center.x) {
                        pointF.x = p.x + spaceX
                    } else {
                        pointF.x = p.x - spaceX
                    }
                } else {
                    if (p.x < center.x) {
                        pointF.x = p.x + spaceX
                    } else {
                        pointF.x = p.x - spaceX
                    }
                }
                if (p !== left && p !== right && p !== center) {
                    if (p.y < center.y) {
                        pointF.y = p.y + spaceY
                    } else {
                        pointF.y = p.y - spaceY
                    }
                } else if (p === left || p === right) {
                    if (p.y < center.y) {
                        pointF.y = p.y - space
                    } else {
                        pointF.y = p.y + space
                    }
                } else {
                    pointF.y = p.y
                }
            }
            result.add(pointF)
        }
        return result
    }

    fun shrinkPath(pointList: List<PointF>, space: Float, bound: RectF?): List<PointF> {
        val result: MutableList<PointF> = ArrayList()
        if (space == 0f) {
            result.addAll(pointList)
        } else {
            val center = PointF(0F, 0F)
            for (p in pointList) {
                center.x += p.x
                center.y += p.y
            }
            center.x = center.x / pointList.size
            center.y = center.y / pointList.size
            var spaceX: Float
            var spaceY: Float
            for (p in pointList) {
                val pointF = PointF()
                spaceX = space
                spaceY = space
                if (bound != null) {
                    if (bound.left == 0f && p.x < center.x || bound.right == 1f && p.x >= center.x) {
                        spaceX = 2 * space
                    }
                    if (bound.top == 0f && p.y < center.y || bound.bottom == 1f && p.y >= center.y) {
                        spaceY = 2 * space
                    }
                }
                if (Math.abs(center.x - p.x) >= 1) {
                    if (p.x < center.x) {
                        pointF.x = p.x + spaceX
                    } else if (p.x > center.x) {
                        pointF.x = p.x - spaceX
                    }
                } else {
                    pointF.x = p.x
                }
                if (Math.abs(center.y - p.y) >= 1) {
                    if (p.y < center.y) {
                        pointF.y = p.y + spaceY
                    } else if (p.y > center.y) {
                        pointF.y = p.y - spaceY
                    }
                } else {
                    pointF.y = p.y
                }
                result.add(pointF)
            }
        }
        return result
    }

    fun commonShrinkPath(
        pointList: List<PointF>,
        space: Float,
        shrunkPointLeftRightDistances: HashMap<PointF, PointF>
    ): List<PointF> {
        val result: MutableList<PointF> = ArrayList()
        if (space == 0f) {
            result.addAll(pointList)
        } else {
            val convexHull = jarvis(pointList)
            for (i in pointList.indices) {
                val center = pointList[i]
                var concave = true
                for (point in convexHull) if (center === point) {
                    concave = false
                    break
                }
                var left: PointF
                var right: PointF
                left = if (i == 0) {
                    pointList[pointList.size - 1]
                } else {
                    pointList[i - 1]
                }
                right = if (i == pointList.size - 1) {
                    pointList[0]
                } else {
                    pointList[i + 1]
                }
                val leftRightDistance = shrunkPointLeftRightDistances[center]
                val pointF = shrinkPoint(
                    center,
                    left,
                    right,
                    leftRightDistance!!.x * space,
                    leftRightDistance.y * space,
                    !concave,
                    !concave
                )
                if (pointF != null) {
                    result.add(pointF)
                } else {
                    result.add(PointF(0F, 0F))
                }
            }
        }
        return result
    }

    fun createPathWithCubicCorner(path: Path, pointList: List<PointF>, corner: Float) {
        path.reset()
        for (i in pointList.indices) {
            if (corner == 0f || pointList.size < 3) {
                if (i == 0) {
                    path.moveTo(pointList[i].x, pointList[i].y)
                } else {
                    path.lineTo(pointList[i].x, pointList[i].y)
                }
            } else {
                val center = PointF(pointList[i].x, pointList[i].y)
                val left = PointF()
                val right = PointF()
                if (i == 0) {
                    left.x = pointList[pointList.size - 1].x
                    left.y = pointList[pointList.size - 1].y
                } else {
                    left.x = pointList[i - 1].x
                    left.y = pointList[i - 1].y
                }
                if (i == pointList.size - 1) {
                    right.x = pointList[0].x
                    right.y = pointList[0].y
                } else {
                    right.x = pointList[i + 1].x
                    right.y = pointList[i + 1].y
                }
                val middleA = findPointOnSegment(center, left, corner.toDouble())
                val middleB = findPointOnSegment(center, right, corner.toDouble())
                val middle = findMiddlePoint(middleA, middleB, center)
                if (i == 0) {
                    path.moveTo(middleA.x, middleA.y)
                } else {
                    path.lineTo(middleA.x, middleA.y)
                }
                path.cubicTo(middleA.x, middleA.y, middle!!.x, middle.y, middleB.x, middleB.y)
            }
        }
    }

    private fun containPoint(points: List<PointF>, p: PointF): Boolean {
        for (pointF in points) if (pointF === p || pointF.x == p.x && pointF.y == p.y) {
            return true
        }
        return false
    }

    fun createPathWithCircleCorner(
        path: Path,
        pointList: List<PointF>?,
        cornerPointList: List<PointF>?,
        corner: Float
    ): Map<PointF, Array<PointF?>>? {
        if (pointList == null || pointList.isEmpty()) {
            return null
        }
        val cornerPointMap: MutableMap<PointF, Array<PointF?>> = HashMap()
        path.reset()
        var firstPoints = arrayOf<PointF?>(pointList[0], pointList[0], pointList[0])
        val convexHull = jarvis(pointList)
        for (i in pointList.indices) {
            if (corner == 0f || pointList.size < 3) {
                if (i == 0) {
                    path.moveTo(pointList[i].x, pointList[i].y)
                } else {
                    path.lineTo(pointList[i].x, pointList[i].y)
                }
            } else {
                var isCornerPoint = true
                if (cornerPointList != null && cornerPointList.size > 0) {
                    isCornerPoint = containPoint(
                        cornerPointList,
                        pointList[i]
                    )
                }
                if (!isCornerPoint) {
                    if (i == 0) {
                        path.moveTo(pointList[i].x, pointList[i].y)
                    } else {
                        path.lineTo(pointList[i].x, pointList[i].y)
                    }
                    if (i == pointList.size - 1) {
                        path.lineTo(firstPoints[1]!!.x, firstPoints[1]!!.y)
                    }
                } else {
                    var concave = true
                    for (p in convexHull) if (p === pointList[i]) {
                        concave = false
                        break
                    }
                    val center = PointF(pointList[i].x, pointList[i].y)
                    val left = PointF()
                    val right = PointF()
                    if (i == 0) {
                        left.x = pointList[pointList.size - 1].x
                        left.y = pointList[pointList.size - 1].y
                    } else {
                        left.x = pointList[i - 1].x
                        left.y = pointList[i - 1].y
                    }
                    if (i == pointList.size - 1) {
                        right.x = pointList[0].x
                        right.y = pointList[0].y
                    } else {
                        right.x = pointList[i + 1].x
                        right.y = pointList[i + 1].y
                    }
                    val pointFs = arrayOfNulls<PointF>(3)
                    val angles = DoubleArray(2)
                    createArc(center, left, right, corner, angles, pointFs, concave)
                    if (i == 0) {
                        path.moveTo(pointFs[1]!!.x, pointFs[1]!!.y)
                    } else {
                        path.lineTo(pointFs[1]!!.x, pointFs[1]!!.y)
                    }
                    val oval = RectF(
                        pointFs[0]!!.x - corner,
                        pointFs[0]!!.y - corner,
                        pointFs[0]!!.x + corner,
                        pointFs[0]!!.y + corner
                    )
                    path.arcTo(oval, angles[0].toFloat(), angles[1].toFloat(), false)
                    if (i == 0) {
                        firstPoints = pointFs
                    }
                    if (i == pointList.size - 1) {
                        path.lineTo(firstPoints[1]!!.x, firstPoints[1]!!.y)
                    }
                    cornerPointMap[pointList[i]] = pointFs
                }
            }
        }
        return cornerPointMap
    }

    fun createPathWithCircleCorner(path: Path, pointList: List<PointF>, corner: Float) {
        path.reset()
        var firstPoints: Array<PointF?>? = null
        val convexHull = jarvis(pointList)
        for (i in pointList.indices) {
            if (corner == 0f || pointList.size < 3) {
                if (i == 0) {
                    path.moveTo(pointList[i].x, pointList[i].y)
                } else {
                    path.lineTo(pointList[i].x, pointList[i].y)
                }
            } else {
                var concave = true
                for (p in convexHull) if (p === pointList[i]) {
                    concave = false
                    break
                }
                val center = PointF(pointList[i].x, pointList[i].y)
                val left = PointF()
                val right = PointF()
                if (i == 0) {
                    left.x = pointList[pointList.size - 1].x
                    left.y = pointList[pointList.size - 1].y
                } else {
                    left.x = pointList[i - 1].x
                    left.y = pointList[i - 1].y
                }
                if (i == pointList.size - 1) {
                    right.x = pointList[0].x
                    right.y = pointList[0].y
                } else {
                    right.x = pointList[i + 1].x
                    right.y = pointList[i + 1].y
                }
                val pointFs = arrayOfNulls<PointF>(3)
                val angles = DoubleArray(2)
                createArc(center, left, right, corner, angles, pointFs, concave)
                if (i == 0) {
                    path.moveTo(pointFs[1]!!.x, pointFs[1]!!.y)
                } else {
                    path.lineTo(pointFs[1]!!.x, pointFs[1]!!.y)
                }
                val oval = RectF(
                    pointFs[0]!!.x - corner,
                    pointFs[0]!!.y - corner,
                    pointFs[0]!!.x + corner,
                    pointFs[0]!!.y + corner
                )
                path.arcTo(oval, angles[0].toFloat(), angles[1].toFloat(), false)
                if (i == 0) {
                    firstPoints = pointFs
                }
                if (i == pointList.size - 1) {
                    path.lineTo(firstPoints!![1]!!.x, firstPoints[1]!!.y)
                }
            }
        }
    }

    fun findPointOnSegment(A: PointF, B: PointF, dA: Double): PointF {
        return if (dA == 0.0) {
            PointF(A.x, A.y)
        } else {
            val result = PointF()
            val dAB = Math.sqrt(((A.x - B.x) * (A.x - B.x) + (A.y - B.y) * (A.y - B.y)).toDouble())
                .toFloat()
            val dx = Math.abs(A.x - B.x) * dA / dAB
            val dy = Math.abs(A.y - B.y) * dA / dAB
            if (A.x > B.x) {
                result.x = (A.x - dx).toFloat()
            } else {
                result.x = (A.x + dx).toFloat()
            }
            if (A.y > B.y) {
                result.y = (A.y - dy).toFloat()
            } else {
                result.y = (A.y + dy).toFloat()
            }
            result
        }
    }

    fun findMiddlePoint(A: PointF, B: PointF, D: PointF): PointF? {
        val d =
            (Math.sqrt(((A.x - B.x) * (A.x - B.x) + (A.y - B.y) * (A.y - B.y)).toDouble()) / 2).toFloat()
        return findMiddlePoint(A, B, d, D)
    }

    fun findMiddlePoint(A: PointF, B: PointF, d: Float, D: PointF): PointF? {
        val a = B.y - A.y
        val b = A.x - B.x
        val c = B.x * A.y - A.x * B.y
        val middlePoints = findMiddlePoint(A, B, d)
        val f = a * D.x + b * D.y + c
        val f1 = a * middlePoints[0]!!.x + b * middlePoints[0]!!.y + c
        return if (f * f1 > Float.MIN_VALUE) {
            middlePoints[0]
        } else {
            middlePoints[1]
        }
    }

    fun createArc(
        A: PointF,
        B: PointF,
        C: PointF,
        dA: Float,
        outAngles: DoubleArray,
        outPoints: Array<PointF?>,
        isConcave: Boolean
    ): Boolean {
        outPoints[0] = findPointOnBisector(A, B, C, dA)
        var d =
            ((A.x - outPoints[0]!!.x) * (A.x - outPoints[0]!!.x) + (A.y - outPoints[0]!!.y) * (A.y - outPoints[0]!!.y) - dA * dA).toDouble()
        d = Math.sqrt(d)
        outPoints[1] = findPointOnSegment(A, B, d)
        outPoints[2] = findPointOnSegment(A, C, d)
        //find angles
        val dMA =
            Math.sqrt(((A.x - outPoints[0]!!.x) * (A.x - outPoints[0]!!.x) + (A.y - outPoints[0]!!.y) * (A.y - outPoints[0]!!.y)).toDouble())
        val halfSweepAngle = Math.acos(dA / dMA)
        val startAngle = Math.atan2(
            (outPoints[1]!!.y - outPoints[0]!!.y).toDouble(),
            (outPoints[1]!!.x - outPoints[0]!!.x).toDouble()
        )
        val endAngle = Math.atan2(
            (outPoints[2]!!.y - outPoints[0]!!.y).toDouble(),
            (outPoints[2]!!.x - outPoints[0]!!.x).toDouble()
        )
        var sweepAngle = endAngle - startAngle
        if (!isConcave) {
            sweepAngle = 2 * halfSweepAngle
        }
        outAngles[0] = Math.toDegrees(startAngle)
        outAngles[1] = Math.toDegrees(sweepAngle)
        val tmp = Math.toDegrees(2 * halfSweepAngle)
        if (Math.abs(tmp - Math.abs(outAngles[1])) > 1) {
            outAngles[1] = -tmp
        }
        return false
    }

    /**
     * @param A
     * @param B
     * @param C
     * @param dA
     * @return null if does not have solution, return PointF(Float.MaxValue, Float.MaxValue) if have infinite solution, other return the solution
     */
    fun findPointOnBisector(A: PointF, B: PointF, C: PointF, dA: Float): PointF? {
        val lineAB = getCoefficients(A, B)
        val lineAC = getCoefficients(A, C)
        val vB = lineAC[0] * B.x + lineAC[1] * B.y + lineAC[2]
        val vC = lineAB[0] * C.x + lineAB[1] * C.y + lineAB[2]
        val square1 = Math.sqrt(lineAB[0] * lineAB[0] + lineAB[1] * lineAB[1])
        val square2 = Math.sqrt(lineAC[0] * lineAC[0] + lineAC[1] * lineAC[1])
        return if (vC > 0) {
            if (vB > 0) {
                findIntersectPoint(
                    lineAB[0], lineAB[1], dA * square1 - lineAB[2],
                    lineAC[0], lineAC[1], dA * square2 - lineAC[2]
                )
            } else {
                findIntersectPoint(
                    lineAB[0], lineAB[1], dA * square1 - lineAB[2],
                    -lineAC[0], -lineAC[1], dA * square2 + lineAC[2]
                )
            }
        } else {
            if (vB > 0) {
                findIntersectPoint(
                    -lineAB[0], -lineAB[1], dA * square1 + lineAB[2],
                    lineAC[0],
                    lineAC[1], dA * square2 - lineAC[2]
                )
            } else {
                findIntersectPoint(
                    -lineAB[0], -lineAB[1], dA * square1 + lineAB[2],
                    -lineAC[0], -lineAC[1], dA * square2 + lineAC[2]
                )
            }
        }
    }

    fun distanceToLine(line: DoubleArray, P: PointF): Double {
        val bottom = Math.sqrt(line[0] * line[0] + line[1] * line[1])
        return Math.abs((line[0] * P.x + line[1] * P.y + line[2]) / bottom)
    }

    /**
     * @param A
     * @param B
     * @param C
     * @param dAB is the distance from shrunk point to AB line
     * @param dAC is the distance from shrunk point to AC line
     * @param b   is true if shrunk point and point B located on same half-plane
     * @param c   is true if shrunk point and point C located on same half-plane
     * @return shrunk point of point A
     */
    fun shrinkPoint(
        A: PointF,
        B: PointF,
        C: PointF,
        dAB: Float,
        dAC: Float,
        b: Boolean,
        c: Boolean
    ): PointF? {
        val ab = getCoefficients(A, B)
        val ac = getCoefficients(A, C)
        val m = dAB * Math.sqrt(ab[0] * ab[0] + ab[1] * ab[1]) - ab[2]
        val n = dAC * Math.sqrt(ac[0] * ac[0] + ac[1] * ac[1]) - ac[2]
        val p = -dAB * Math.sqrt(ab[0] * ab[0] + ab[1] * ab[1]) - ab[2]
        val q = -dAC * Math.sqrt(ac[0] * ac[0] + ac[1] * ac[1]) - ac[2]
        val P1 = findIntersectPoint(
            ab[0],
            ab[1], m, ac[0], ac[1], n
        )
        val P2 = findIntersectPoint(
            ab[0],
            ab[1], m, ac[0], ac[1], q
        )
        val P3 = findIntersectPoint(
            ab[0],
            ab[1], p, ac[0], ac[1], n
        )
        val P4 = findIntersectPoint(
            ab[0],
            ab[1], p, ac[0], ac[1], q
        )
        return if (testShrunkPoint(ab, ac, B, C, P1, b, c)) {
            P1
        } else if (testShrunkPoint(ab, ac, B, C, P2, b, c)) {
            P2
        } else if (testShrunkPoint(ab, ac, B, C, P3, b, c)) {
            P3
        } else if (testShrunkPoint(ab, ac, B, C, P4, b, c)) {
            P4
        } else {
            null
        }
    }

    private fun testShrunkPoint(
        ab: DoubleArray,
        ac: DoubleArray,
        B: PointF,
        C: PointF,
        P: PointF?,
        b: Boolean,
        c: Boolean
    ): Boolean {
        if (P != null && P.x < Float.MAX_VALUE && P.y < Float.MAX_VALUE) {
            val signC = (ab[0] * P.x + ab[1] * P.y + ab[2]) * (ab[0] * C.x + ab[1] * C.y + ab[2])
            val signB = (ac[0] * P.x + ac[1] * P.y + ac[2]) * (ac[0] * B.x + ac[1] * B.y + ac[2])
            val testC = signC > Double.MIN_VALUE
            val testB = signB > Double.MIN_VALUE
            if (testC == c && testB == b) {
                return true
            }
        }
        return false
    }

    /**
     * Solve equations
     * ax + by = c
     * dx + ey = f
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @param e
     * @param f
     * @return null if this equations does not has solution.
     * return PointF(Float.MaxValue, Float.MaxValue) if this equations has infinite solutions
     * other return the solution of this equations.
     */
    fun findIntersectPoint(
        a: Double,
        b: Double,
        c: Double,
        d: Double,
        e: Double,
        f: Double
    ): PointF? {
        val Dx: Double
        val Dy: Double
        val D: Double
        D = a * e - b * d
        Dx = c * e - b * f
        Dy = a * f - c * d
        return if (D == 0.0 && Dx == 0.0) {
            PointF(Float.MAX_VALUE, Float.MAX_VALUE)
        } else if (D == 0.0 && Dx != 0.0) {
            null
        } else {
            PointF((Dx / D).toFloat(), (Dy / D).toFloat())
        }
    }

    /**
     * Find bisector of angle <BAC @param A @param B @param C @return the Coefficients of bisector></BAC>
     */
    fun findBisector(A: PointF, B: PointF, C: PointF): DoubleArray {
        val ab = getCoefficients(A, B)
        val ac = getCoefficients(A, C)
        val sqrt1 = Math.sqrt(ab[0] * ab[0] + ab[1] * ab[1])
        val sqrt2 = Math.sqrt(ac[0] * ac[0] + ac[1] * ac[1])
        val a1 = ab[0] / sqrt1 + ac[0] / sqrt2
        val b1 = ab[1] / sqrt1 + ac[1] / sqrt2
        val c1 = ab[2] / sqrt1 + ac[2] / sqrt2
        val a2 = ab[0] / sqrt1 - ac[0] / sqrt2
        val b2 = ab[1] / sqrt1 - ac[1] / sqrt2
        val c2 = ab[2] / sqrt1 - ac[2] / sqrt2
        val fB = a1 * B.x + b1 * B.y + c1
        val fC = a1 * C.x + b1 * C.y + c1
        return if (fB * fC > Double.MIN_VALUE) {
            doubleArrayOf(a2, b2, c2)
        } else {
            doubleArrayOf(a1, b1, c1)
        }
    }

    fun getCoefficients(A: PointF, B: PointF): DoubleArray {
        val a = (B.y - A.y).toDouble()
        val b = (A.x - B.x).toDouble()
        val c = (B.x * A.y - A.x * B.y).toDouble()
        return doubleArrayOf(a, b, c)
    }

    fun findMiddlePoint(A: PointF, B: PointF, d: Float): Array<PointF?> {
        val result = arrayOfNulls<PointF>(2)
        val dx = B.x - A.x
        val dy = B.y - A.y
        val sx = (B.x + A.x) / 2.0f
        val sy = (B.y + A.y) / 2.0f
        if (dx == 0f) {
            result[0] = PointF(A.x + d, sy)
            result[1] = PointF(A.x - d, sy)
        } else if (dy == 0f) {
            result[0] = PointF(sx, A.y + d)
            result[1] = PointF(sx, A.y - d)
        } else {
            val deltaY = (d / Math.sqrt((1 + dy * dy / (dx * dx)).toDouble())).toFloat()
            result[0] = PointF(sx - dy / dx * deltaY, sy + deltaY)
            result[1] = PointF(sx + dy / dx * deltaY, sy - deltaY)
        }
        return result
    }

    fun CCW(p: PointF, q: PointF, r: PointF): Boolean {
        return if (((q.y.toInt() - p.y.toInt()) * (r.x.toInt() - q.x.toInt()) - (q.x.toInt() - p.x.toInt()) * (r.y.toInt() - q.y.toInt())) >= 0) false else true
    }

    /**
     * Implement Jarvis Algorithm. Jarvis algorithm or the gift wrapping algorithm is an algorithm for computing the convex hull of a given set of points.
     *
     * @param points
     * @return the convex hull of a given set of points
     */
    fun jarvis(points: List<PointF>): ArrayList<PointF> {
        val result = ArrayList<PointF>()
        val n = points.size
        /** if less than 3 points return  */
        if (n < 3) {
            for (p in points) result.add(p)
            return result
        }
        val next = IntArray(n)
        Arrays.fill(next, -1)
        /** find the leftmost point  */
        var leftMost = 0
        for (i in 1 until n) if (points[i].x.toInt() < points[leftMost].x.toInt()) leftMost = i
        var p = leftMost
        var q: Int
        /** iterate till p becomes leftMost  */
        do {
            /** wrapping  */
            q = (p + 1) % n
            for (i in 0 until n) if (CCW(
                    points[p],
                    points[i], points[q]
                )
            ) q = i
            next[p] = q
            p = q
        } while (p != leftMost)
        for (i in next.indices) if (next[i] != -1) result.add(points[i])
        return result
    }
}