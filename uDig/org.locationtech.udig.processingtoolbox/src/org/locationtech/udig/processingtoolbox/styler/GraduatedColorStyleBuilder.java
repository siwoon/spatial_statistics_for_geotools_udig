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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.filter.function.RangedClassifier;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.geotools.util.logging.Logging;
import org.locationtech.udig.processingtoolbox.common.FeatureTypes;
import org.locationtech.udig.processingtoolbox.common.FeatureTypes.SimpleShapeType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;

/**
 * GraduatedColorStyleBuilder
 * 
 * @author MapPlus
 * 
 */
@SuppressWarnings("nls")
public class GraduatedColorStyleBuilder extends AbstractFeatureStyleBuilder {
    protected static final Logger LOGGER = Logging.getLogger(GraduatedColorStyleBuilder.class);

    String normalProperty;

    public void setNormalProperty(String normalProperty) {
        this.normalProperty = normalProperty;
    }

    public String getNormalProperty() {
        return normalProperty;
    }

    // Diverging: PuOr, BrBG, PRGn, PiYG, RdBu, RdGy, RdYlBu, Spectral, RdYlGn
    // Qualitative: Set1, Pastel1, Set2, Pastel2, Dark2, Set3, Paired, Accents,
    // Sequential: YlGn, YlGnBu, GnBu, BuGn, PuBuGn, PuBu, BuPu, RdPu, PuRd, OrRd, YlOrRd, YlOrBr,
    // Purples, Blues, Greens, Oranges, Reds, Grays,
    public Style createStyle(SimpleFeatureCollection inputFeatures, String propertyName,
            String methodName, int numClasses, String brewerPaletteName) {
        numClasses = numClasses < 3 ? 3 : numClasses;
        if (numClasses > 12) {
            numClasses = numClasses > 12 ? 12 : numClasses;
            LOGGER.log(Level.WARNING, "maximum numClasses cannot exceed 12!");
        }

        // get classifier
        RangedClassifier classifier = null;
        if (normalProperty == null || normalProperty.isEmpty()) {
            classifier = getClassifier(inputFeatures, propertyName, methodName, numClasses);
        } else {
            classifier = getClassifier(inputFeatures, propertyName, normalProperty, methodName,
                    numClasses);
        }

        double[] classBreaks = getClassBreaks(classifier);

        if (brewerPaletteName == null || brewerPaletteName.isEmpty()) {
            brewerPaletteName = "OrRd"; // default
        }

        ColorBrewer brewer = ColorBrewer.instance();
        // brewer.loadPalettes();
        BrewerPalette brewerPalette = brewer.getPalette(brewerPaletteName);

        Color[] colors = brewerPalette.getColors(classBreaks.length - 1);

        return createStyle(inputFeatures.getSchema(), propertyName, classBreaks, colors);
    }

    public Style createStyle(SimpleFeatureType inputFeatureType, String propertyName,
            double[] classBreaks, Color[] colors) {
        if (classBreaks.length - 1 != colors.length) {
            LOGGER.log(Level.FINE, "classBreaks's length dose not equal colors's length");
            return null;
        }

        GeometryDescriptor geomDesc = inputFeatureType.getGeometryDescriptor();
        String geometryPropertyName = geomDesc.getLocalName();
        SimpleShapeType shapeType = FeatureTypes
                .getSimpleShapeType(geomDesc.getType().getBinding());

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        for (int k = 0; k < classBreaks.length - 1; k++) {
            final Color uvColor = colors[k];

            Symbolizer symbolizer = null;
            switch (shapeType) {
            case POINT:
                Mark mark = sf.getCircleMark();
                Stroke markStroke = sf.createStroke(ff.literal(Color.WHITE),
                        ff.literal(outlineWidth), ff.literal(outlineOpacity));
                mark.setStroke(markStroke);
                mark.setFill(sf.createFill(ff.literal(uvColor), ff.literal(fillOpacity)));

                Graphic graphic = sf.createDefaultGraphic();
                graphic.graphicalSymbols().clear();
                graphic.graphicalSymbols().add(mark);
                graphic.setSize(ff.literal(markerSize));

                symbolizer = sf.createPointSymbolizer(graphic, geometryPropertyName);
                break;
            case LINESTRING:
                Stroke lineStroke = sf.createStroke(ff.literal(uvColor), ff.literal(lineWidth),
                        ff.literal(lineOpacity));

                symbolizer = sf.createLineSymbolizer(lineStroke, geometryPropertyName);
                break;
            case POLYGON:
                Stroke outlineStroke = sf.createStroke(ff.literal(outlineColor),
                        ff.literal(outlineWidth), ff.literal(outlineOpacity));

                Fill fill = sf.createFill(ff.literal(uvColor), ff.literal(fillOpacity));

                symbolizer = sf.createPolygonSymbolizer(outlineStroke, fill, geometryPropertyName);
                break;
            }

            Filter filter = ff.between(ff.property(propertyName), ff.literal(classBreaks[k]),
                    ff.literal(classBreaks[k + 1]));
            Rule rule = sf.createRule();
            rule.setName(classBreaks[k] + " - " + classBreaks[k + 1]);
            rule.setFilter(filter);

            rule.symbolizers().add(symbolizer);

            fts.rules().add(rule);
        }

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

}
