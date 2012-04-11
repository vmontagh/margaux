#!/bin/bash

export LIB_SDG='/Users/macbookpro/Documents/AlloyMOOP/lib-sdg/trunk'

java -cp $LIB_SDG/jars-external/JFlex.jar JFlex.Main --nobak -d . Alloy.lex

sed -i -e 's/public java_cup.runtime.Symbol next_token() throws java.io.IOException/public java_cup.runtime.Symbol next_token() throws java.io.IOException, Err/' CompLexer.java

