/*
 * uDig - User Friendly Desktop Internet GIS client
 * (C) MangoSystem - www.mangosystem.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the HydroloGIS BSD
 * License v1.0 (http://udig.refractions.net/files/hsd3-v10.html).
 */
package org.locationtech.udig.processingtoolbox.styler;

import java.awt.Color;
import java.util.logging.Logger;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.function.RangedClassifier;
import org.geotools.process.spatialstatistics.core.StringHelper;
import org.geotools.styling.StyleFactory;
import org.geotools.util.logging.Logging;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Function;

/**
 * AbstractFeatureStyleBuilder
 * 
 * @author MapPlus
 * 
 */
@SuppressWarnings("nls")
public abstract class AbstractFeatureStyleBuilder {
    protected static final Logger LOGGER = Logging.getLogger(AbstractFeatureStyleBuilder.class);

    protected final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    protected final StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);


    protected float lineWidth = 1.0f;

    protected float outlineWidth = 0.5f;

    protected float lineOpacity = 1.0f;

    protected float outlineOpacity = 1.0f;

    protected float fillOpacity = 1.0f;

    protected Color outlineColor = new Color(225, 225, 225);

    protected float markerSize = 7.0f;

    public float getMarkerSize() {
        return markerSize;
    }

    public void setMarkerSize(float markerSize) {
        this.markerSize = markerSize;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getOutlineWidth() {
        return outlineWidth;
    }

    public void setOutlineWidth(float outlineWidth) {
        this.outlineWidth = outlineWidth;
    }

    public float getLineOpacity() {
        return lineOpacity;
    }

    public void setLineOpacity(float lineOpacity) {
        this.lineOpacity = lineOpacity;
    }

    public float getOutlineOpacity() {
        return outlineOpacity;
    }

    public void setOutlineOpacity(float outlineOpacity) {
        this.outlineOpacity = outlineOpacity;
    }

    public float getFillOpacity() {
        return fillOpacity;
    }

    public void setFillOpacity(float fillOpacity) {
        this.fillOpacity = fillOpacity;
    }

    public Color getOutlineColor() {
        return outlineColor;
    }

    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
    }

    protected RangedClassifier getClassifier(SimpleFeatureCollection inputFeatures,
            String propertyName, String methodName, int numClass) {
        // ClassificationFunction : JenksNaturalBreaksFunction, EqualIntervalFunction,
        // StandardDeviationFunction, QuantileFunction, UniqueIntervalFunction, CategorizeFunction

        String functionName = getFunctionName(methodName);
        Function function = ff.function(functionName, ff.property(propertyName),
                ff.literal(numClass));
        return (RangedClassifier) function.evaluate(inputFeatures);
    }

    protected RangedClassifier getClassifier(SimpleFeatureCollection inputFeatures,
            String propertyName, String normalProeprtyName, String methodName, int numClass) {
        // ClassificationFunction : JenksNaturalBreaksFunction, EqualIntervalFunction,
        // StandardDeviationFunction, QuantileFunction, UniqueIntervalFunction, CategorizeFunction

        String functionName = getFunctionName(methodName);
        Divide divide = ff.divide(ff.property(propertyName), ff.property(normalProeprtyName));
        Function function = ff.function(functionName, divide, ff.literal(numClass));

        return (RangedClassifier) function.evaluate(inputFeatures);
    }

    private String getFunctionName(String methodName) {
        if (StringHelper.isNullOrEmpty(methodName)) {
            methodName = "Jenks";
        }

        methodName = methodName.toUpperCase();

        String functionName = "Jenks";
        if (methodName.contains("NA") || methodName.contains("JENK")) {
            functionName = "Jenks";
        } else if (methodName.contains("QU")) {
            functionName = "Quantile";
        } else if (methodName.contains("EQ")) {
            functionName = "EqualInterval";
        } else if (methodName.contains("ST")) {
            functionName = "StandardDeviation";
        } else if (methodName.contains("UN")) {
            functionName = "UniqueInterval";
        } else {
            functionName = "Jenks"; // default
        }

        return functionName;
    }

    protected double[] getClassBreaks(RangedClassifier classifier) {
        double[] classBreaks = new double[classifier.getSize() + 1];

        for (int slot = 0; slot < classifier.getSize(); slot++) {
            classBreaks[slot] = (Double) classifier.getMin(slot);
        }

        classBreaks[classifier.getSize()] = (Double) classifier.getMax(classifier.getSize() - 1);

        return classBreaks;
    }
}
