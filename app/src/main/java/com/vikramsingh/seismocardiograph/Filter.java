package com.vikramsingh.seismocardiograph;

import java.math.BigDecimal;

/**
 * Created by Vikram Singh on 7/8/2017.
 *
 * Filter class used to filter data using
 *  a 10th order butterworth filter.
 *
 *  This filter is translated from the matlab version
 */

public class Filter {

    // the precision of the filter
    private int precision = 16;

    /*
    The a and b values for the butterworth filter
     */
    private BigDecimal[] b = {
            new BigDecimal("0.662015837202617").setScale(precision, BigDecimal.ROUND_HALF_UP),
            new BigDecimal("2.64806334881047").setScale(precision, BigDecimal.ROUND_HALF_UP),
            new BigDecimal("3.97209502321570").setScale(precision, BigDecimal.ROUND_HALF_UP),
            new BigDecimal("2.64806334881047").setScale(precision, BigDecimal.ROUND_HALF_UP),
            new BigDecimal("0.662015837202617").setScale(precision, BigDecimal.ROUND_HALF_UP)};
    private BigDecimal[] a = {
            new BigDecimal("1").setScale(precision, BigDecimal.ROUND_HALF_UP),
            new BigDecimal("3.18063854887472").setScale(precision, BigDecimal.ROUND_HALF_UP),
            new BigDecimal("3.86119434899421").setScale(precision, BigDecimal.ROUND_HALF_UP),
            new BigDecimal("2.11215535511097").setScale(precision, BigDecimal.ROUND_HALF_UP),
            new BigDecimal("0.438265142261979").setScale(precision, BigDecimal.ROUND_HALF_UP)};

    private BigDecimal[] z;

    private int n;

    public Filter(){
        init();
    }

    private void init(){

        //get n length
        n = b.length;

        //Initialize values
        z = new BigDecimal[n];

        //Fill filtered delay with zeros
        fillZeros(z);

        //Normalize vectors
        divideEach(b, a[0]);
        divideEach(a, a[0]);

    }

    /*
    Returns the filtered value using the butterworth filter algorithm
     */
    public BigDecimal getFilteredValue(double value){
        //Y[m] = (b[0] * values) + z[0]
        BigDecimal Y = b[0].multiply(new BigDecimal(value)).add(z[0]).setScale(precision, BigDecimal.ROUND_HALF_UP);

        for(int i = 1; i < n; i++){
            //z[i-1] = (b[i] * value) + z[i] - (a[i] * Y[m])
            z[i-1] = b[i].multiply(new BigDecimal(value)).add(z[i]).subtract(a[i].multiply(Y)).setScale(precision, BigDecimal.ROUND_HALF_UP);
        }

        return Y;
    }

    //Divides a BigDecimal array by a big decimal
    private void divideEach(BigDecimal[] array, BigDecimal divisor){
        for(int i = 0; i < array.length; i++){
            array[i] = array[i].divide(divisor).setScale(precision, BigDecimal.ROUND_HALF_UP);
        }
    }

    //Fills a big decimal array with zeros
    private void fillZeros(BigDecimal[] array){
        for(int i = 0; i < array.length; i++){
            array[i] = BigDecimal.ZERO;
        }
    }

    public void setPrecision(int precision){

        this.precision = precision;

    }

}