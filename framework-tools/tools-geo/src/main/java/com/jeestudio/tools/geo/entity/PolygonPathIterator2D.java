package com.jeestudio.tools.geo.entity;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * @Description: 多边形路径迭代器
 */
public class PolygonPathIterator2D implements PathIterator {
        Polygon2D poly;
        AffineTransform transform;
        int index;

        public PolygonPathIterator2D(Polygon2D pg, AffineTransform at) {
            poly = pg;
            transform = at;
            if (pg.npoints == 0) {
                // Prevent a spurious SEG_CLOSE segment
                index = 1;
            }
        }

        /**
         * Returns the winding rule for determining the interior of the
         * path.
         * @return an integer representing the current winding rule.
         * @see PathIterator#WIND_NON_ZERO
         */
        public int getWindingRule() {
            return WIND_EVEN_ODD;
        }

        /**
         * Tests if there are more points to read.
         * @return <code>true</code> if there are more points to read;
         *          <code>false</code> otherwise.
         */
        public boolean isDone() {
            return index > poly.npoints;
        }

        /**
         * Moves the iterator forwards, along the primary direction of
         * traversal, to the next segment of the path when there are
         * more points in that direction.
         */
        public void next() {
            index++;
        }

        /**
         * Returns the coordinates and type of the current path segment in
         * the iteration.
         * The return value is the path segment type:
         * SEG_MOVETO, SEG_LINETO, or SEG_CLOSE.
         * A <code>float</code> array of length 2 must be passed in and
         * can be used to store the coordinates of the point(s).
         * Each point is stored as a pair of <code>float</code> x,&nbsp;y
         * coordinates.  SEG_MOVETO and SEG_LINETO types return one
         * point, and SEG_CLOSE does not return any points.
         * @param coords a <code>float</code> array that specifies the
         * coordinates of the point(s)
         * @return an integer representing the type and coordinates of the
         *              current path segment.
         * @see PathIterator#SEG_MOVETO
         * @see PathIterator#SEG_LINETO
         * @see PathIterator#SEG_CLOSE
         */
        public int currentSegment(float[] coords) {
            if (index >= poly.npoints) {
                return SEG_CLOSE;
            }
            coords[0] = (float) poly.xpoints[index];
            coords[1] = (float) poly.ypoints[index];
            if (transform != null) {
                transform.transform(coords, 0, coords, 0, 1);
            }
            return (index == 0 ? SEG_MOVETO : SEG_LINETO);
        }

        /**
         * Returns the coordinates and type of the current path segment in
         * the iteration.
         * The return value is the path segment type:
         * SEG_MOVETO, SEG_LINETO, or SEG_CLOSE.
         * A <code>double</code> array of length 2 must be passed in and
         * can be used to store the coordinates of the point(s).
         * Each point is stored as a pair of <code>double</code> x,&nbsp;y
         * coordinates.
         * SEG_MOVETO and SEG_LINETO types return one point,
         * and SEG_CLOSE does not return any points.
         * @param coords a <code>double</code> array that specifies the
         * coordinates of the point(s)
         * @return an integer representing the type and coordinates of the
         *              current path segment.
         * @see PathIterator#SEG_MOVETO
         * @see PathIterator#SEG_LINETO
         * @see PathIterator#SEG_CLOSE
         */
        public int currentSegment(double[] coords) {
            if (index >= poly.npoints) {
                return SEG_CLOSE;
            }
            coords[0] = poly.xpoints[index];
            coords[1] = poly.ypoints[index];
            if (transform != null) {
                transform.transform(coords, 0, coords, 0, 1);
            }
            return (index == 0 ? SEG_MOVETO : SEG_LINETO);
        }
    }
