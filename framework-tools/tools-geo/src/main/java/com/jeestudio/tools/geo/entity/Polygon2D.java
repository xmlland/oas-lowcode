package com.jeestudio.tools.geo.entity;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

/**
 * @Description: Polygon子类，点位为double类型数据
 */
public class Polygon2D extends java.awt.Polygon {
    public double xpoints[];
    public double ypoints[];
    /**
     * The bounds of this {@code Polygon}.
     * This value can be null.
     *
     * @serial
     * @see #getBoundingBox()
     * @see #getBounds()
     * @since 1.0
     */
    protected Rectangle2D.Double bounds;
    public Polygon2D() {
        xpoints = new double[MIN_LENGTH];
        ypoints = new double[MIN_LENGTH];
    }
    /*
     * Default length for xpoints and ypoints.
     */
    private static final int MIN_LENGTH = 4;

    public Polygon2D(double xpoints[], double ypoints[], int npoints) {
        // Fix 4489009: should throw IndexOutofBoundsException instead
        // of OutofMemoryException if npoints is huge and > {x,y}points.length
        if (npoints > xpoints.length || npoints > ypoints.length) {
            throw new IndexOutOfBoundsException("npoints > xpoints.length || "+
                    "npoints > ypoints.length");
        }
        // Fix 6191114: should throw NegativeArraySizeException with
        // negative npoints
        if (npoints < 0) {
            throw new NegativeArraySizeException("npoints < 0");
        }
        // Fix 6343431: Applet compatibility problems if arrays are not
        // exactly npoints in length
        this.npoints = npoints;
        this.xpoints = Arrays.copyOf(xpoints, npoints);
        this.ypoints = Arrays.copyOf(ypoints, npoints);
    }

    public void addPoint2D(double x, double y) {
        if (npoints >= xpoints.length || npoints >= ypoints.length) {
            int newLength = npoints * 2;
            // Make sure that newLength will be greater than MIN_LENGTH and
            // aligned to the power of 2
            if (newLength < MIN_LENGTH) {
                newLength = MIN_LENGTH;
            } else if ((newLength & (newLength - 1)) != 0) {
                newLength = Integer.highestOneBit(newLength);
            }

            xpoints = Arrays.copyOf(xpoints, newLength);
            ypoints = Arrays.copyOf(ypoints, newLength);
        }
        xpoints[npoints] = x;
        ypoints[npoints] = y;
        npoints++;
        if (bounds != null) {
            updateBounds(x, y);
        }
    }

    void updateBounds(double x, double y) {
        if (x < bounds.x) {
            bounds.width = bounds.width + (bounds.x - x);
            bounds.x = x;
        }
        else {
            bounds.width = Math.max(bounds.width, x - bounds.x);
            // bounds.x = bounds.x;
        }

        if (y < bounds.y) {
            bounds.height = bounds.height + (bounds.y - y);
            bounds.y = y;
        }
        else {
            bounds.height = Math.max(bounds.height, y - bounds.y);
            // bounds.y = bounds.y;
        }
    }

    public Rectangle2D getBounds2D() {
        if (npoints == 0) {
            return new Rectangle();
        }
        if (bounds == null) {
            calculateBounds(xpoints, ypoints, npoints);
        }
        return this.bounds;
    }
    public Rectangle2D.Double getBoundsBox() {
        if (npoints == 0) {
            return new Rectangle2D.Double(0,0,0,0);
        }
        if (bounds == null) {
            calculateBounds(xpoints, ypoints, npoints);
        }
        return new Rectangle2D.Double(bounds.x, bounds.y, bounds.width,bounds.height);
    }
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y, double w, double h) {
        if (npoints <= 0 || !getBoundsBox().intersects(x, y, w, h)) {
            return false;
        }
        return toPath2D().contains(x, y, w, h);
    }
    public boolean contains(double x, double y) {
        if (npoints <= 2 || !getBoundsBox().contains(x, y)) {
            return false;
        }
        int hits = 0;

        double lastx = xpoints[npoints - 1];
        double lasty = ypoints[npoints - 1];
        double curx, cury;

        // Walk the edges of the polygon
        for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
            curx = xpoints[i];
            cury = ypoints[i];

            if (cury == lasty) {
                continue;
            }

            double leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }

            double test1, test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }

            if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }

        return ((hits & 1) != 0);
    }
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public boolean intersects(double x, double y, double w, double h) {
        if (npoints <= 0 || !getBoundsBox().intersects(x, y, w, h)) {
            return false;
        }
        return toPath2D().intersects(x, y, w, h);
    }

    private Path2D toPath2D() {
        Path2D path = new Path2D.Double();
        if (npoints > 0) {
            path.moveTo(xpoints[0], ypoints[0]);
            for (int i = 1; i < npoints; i++) {
                path.lineTo(xpoints[i], ypoints[i]);
            }
            path.closePath();
        }
        return path;
    }

    void calculateBounds(double xpoints[], double ypoints[], int npoints) {
        double boundsMinX = Double.MAX_VALUE;
        double boundsMinY = Double.MAX_VALUE;
        double boundsMaxX = Double.MIN_VALUE;
        double boundsMaxY = Double.MIN_VALUE;

        for (int i = 0; i < npoints; i++) {
            double x = xpoints[i];
            boundsMinX = Math.min(boundsMinX, x);
            boundsMaxX = Math.max(boundsMaxX, x);
            double y = ypoints[i];
            boundsMinY = Math.min(boundsMinY, y);
            boundsMaxY = Math.max(boundsMaxY, y);
        }
        bounds = new Rectangle2D.Double(boundsMinX, boundsMinY,
                boundsMaxX - boundsMinX,
                boundsMaxY - boundsMinY);
    }
    public PolygonPathIterator2D getPathIterator2D(AffineTransform at) {
        return new PolygonPathIterator2D(this, at);
    }



}
