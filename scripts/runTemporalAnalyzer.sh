#classpath=/Users/vajih/Documents/workspace-git/alloy/bin:/Users/vajih/Documents/workspace-git/lib-sdg/jars-external/sat4j.jar:/Users/vajih/Documents/workspace-git/alloy/lib/apple-osx-ui.jar:/Users/vajih/Documents/workspace-git/alloy/lib/extra.jar:/Users/vajih/Documents/workspace-git/lib-sdg/jars-sdg/kodkod-int-overflow.jar:/Users/vajih/Downloads/eclipse/plugins/org.junit_4.11.0.v201303080030/junit.jar:/Users/vajih/Downloads/eclipse/plugins/org.hamcrest.core_1.3.0.v201303031735.jar:/Users/vajih/Documents/workspace-git/debugger-experiment/lib/alloy4.2.jar:/Users/vajih/Documents/workspace-git/lib-sdg/jars-external/rt.jar:/Users/vajih/Documents/workspace-git/lib-sdg/jars-external/functionaljava-3.0.jar:/Users/vajih/Documents/workspace-git/alloy/lib/guava-18.0.jar:/Users/vajih/Documents/workspace-git/alloy/lib/reflections-0.10-SNAPSHOT-jar-with-dependencies.jar
xms=256m
xmx=1024m

CP=$(ls -1 dist/*.jar | xargs | sed "s/\ /:/g"):$(ls -1 lib/*.jar | xargs | sed "s/\ /:/g")

echo $CP
#CP=$CP:classes:$(ls -1 dist/*.jar | xargs | sed "s/\ /:/g")

java -Xmx$xmx -Xms$xms -cp $CP edu.uw.ece.alloy.debugger.propgen.benchmarker.TemporalAnalyzerRunner

echo $classpath