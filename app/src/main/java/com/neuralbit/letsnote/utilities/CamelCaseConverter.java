package com.neuralbit.letsnote.utilities;

public class CamelCaseConverter {
    // function to convert the string into lower camel case

    public static String convertString( String s )
    {
        // to keep track of spaces
        int ctr = 0 ;
        // variable to hold the length of the string
        int n = s.length( ) ;
        // converting the string expression to character array
        char ch[ ] = s.toCharArray( ) ;
        // keep track of indices of ch[ ] array
        int c = 0 ;
        // traversing through each character of the array
        for ( int i = 0; i < n; i++ )
        {
            // The first position of the array i.e., the first letter must be
            // converted to lower case as we are following lower camel case
            // in this program
            if( i == 0 )
                // converting to lower case using the toLowerCase( ) in-built function
                ch[ i ] = Character.toLowerCase( ch[ i ] ) ;
            // as we need to remove all the spaces in between, we check for empty
            // spaces
            if ( ch[ i ] == ' ' )
            {
                // incrementing the space counter by 1
                ctr++ ;
                // converting the letter immediately after the space to upper case
                ch[ i + 1 ] = Character.toUpperCase( ch[ i + 1 ] ) ;
                // continue the loop
                continue ;
            }
            // if the space is not encountered simply copy the character
            else
                ch[ c++ ] = ch[ i ] ;
        }
        // The size of new string will be reduced as the spaces have been removed
        // Thus, returning the new string with new size
        return String.valueOf( ch, 0, n - ctr ) ;
    }
}
