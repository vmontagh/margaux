#!/bin/bash

java -cp /Users/vajih/Documents/workspace2/lib-sdg/jars-external/JFlex.jar JFlex.Main --nobak -d . Alloy.lex

sed -i -e 's/public java_cup.runtime.Symbol next_token() throws java.io.IOException/public java_cup.runtime.Symbol next_token() throws java.io.IOException, Err/' CompLexer.java

