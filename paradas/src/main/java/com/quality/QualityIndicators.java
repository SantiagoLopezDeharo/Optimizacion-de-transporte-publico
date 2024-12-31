package com.quality;

import org.uma.jmetal.qualityindicator.impl.ErrorRatio;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.GeneralizedSpread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.util.NormalizeUtils;
import org.uma.jmetal.util.VectorUtils;

import java.text.DecimalFormatSymbols;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class QualityIndicators {
    public static void main(String[] args) {

        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.####", decimalFormatSymbols);
        df.setRoundingMode(RoundingMode.CEILING);

        try {
            double[][] referenceFront = VectorUtils.readVectors("src/main/resources/approximated_pareto_front_pgh.csv", ",");
            double[][] normalizedReferenceFront = NormalizeUtils.normalize(referenceFront);

            System.out.println("FUN,TASA_ERROR,DISTANCIA_GENERACIONAL,SPREAD,HIPERVOLUMEN");

            for (int i = 1; i < 811; i++) {
            
                double[][] solutionFront = VectorUtils.readVectors("src/main/resources/FUN_PGH/FUN" + i + ".csv", ",");
                double[][] normalizedSolutionFront = NormalizeUtils.normalize(
                                                                solutionFront,
                                                                NormalizeUtils.getMinValuesOfTheColumnsOfAMatrix(referenceFront),
                                                                NormalizeUtils.getMaxValuesOfTheColumnsOfAMatrix(referenceFront));

                ErrorRatio errorRatio = new ErrorRatio(normalizedReferenceFront);
                double errorRatioValue = errorRatio.compute(normalizedSolutionFront);

                GenerationalDistance generationalDistance = new GenerationalDistance(normalizedReferenceFront);
                double generationalDistanceValue = generationalDistance.compute(normalizedSolutionFront);

                GeneralizedSpread generalizedSpread = new GeneralizedSpread(normalizedReferenceFront);
                double generalizedSpreadValue = generalizedSpread.compute(normalizedSolutionFront);

                PISAHypervolume hypervolume = new PISAHypervolume(normalizedReferenceFront);
                double hypervolumeValue = hypervolume.compute(normalizedSolutionFront);

                System.out.println(i + "," + df.format(errorRatioValue) + "," + df.format(generationalDistanceValue) + "," + df.format(generalizedSpreadValue) + "," + df.format(hypervolumeValue));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}