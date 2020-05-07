package com.borsoftlab.ozee;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

@DisplayName("Test class")
public class DeclareVarsTest {

    static String program0
                        = "int i";
    static String message0 
                        = "int i"   + '\n'
                        + "     ^"  + '\n'
                        + "Error in line 1: expected '=' or ';'" + '\n';
        

    final static String program1 
                        = "int i;";
    final static String message1
                        = "Ok";

    final static String program2
                        = "int i=";
    final static String message2
                        = "int i="  + '\n'
                        + "      ^" + '\n'
                        + "Error in line 1: unexpected EOF"   + '\n';
    
    final static String program3 
                        = "int i=5;";
    final static String message3
                        = "Ok";

    final static String program4 
                        = "int id = 180; // comment"   + '\n'
                        + "int j = id;"                + '\n'
                        + "byte l;"                    + '\n'
                        + "int t12;"                   + '\n'
                        + "float g;"                   + '\n'
                        + "int k= 17 + j + t12;"       + '\n'
                        + "/*"                         + '\n'
                        + " * comment"                 + '\n'
                        + " */"                        + '\n'
                        + "byte b = 45;"               + '\n'
                        + "float f = 0.523 * 12.3 - 41.6/32 * (32 + 76) + j;";
    final static String message4
                        = "Ok";

    OzParser parser   = new OzParser();
    OzScanner scanner = new OzScanner();

    @DisplayName("Test method")
    @ParameterizedTest(name = "{index}")
    @ArgumentsSource(CustomArgumentProvider.class)
    public void test(String program, String message) {
        System.out.println("::------------------------------------------::");
        try {     
            final InputStream programStream = new ByteArrayInputStream(program.getBytes());
            try {
                final OzText text = new OzText(programStream);
                scanner.resetText(text);
                parser.compile(scanner);
            } catch (final Exception e) {
            } finally {
                System.out.println(OzCompileError.messageString);
                try {
                    programStream.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        assertEquals(message, OzCompileError.messageString.toString());
    }

    static class CustomArgumentProvider implements ArgumentsProvider{
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                //Arguments.of( program0, message0 ),
                //Arguments.of( program1, message1 ), 
                Arguments.of( program2, message2 ) 
                //Arguments.of( program3, message3 ), 
                //Arguments.of( program4, message4 ) 
            );
        }
    }

}


    