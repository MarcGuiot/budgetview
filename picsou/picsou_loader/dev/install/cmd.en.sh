#!/bin/sh

#  attention le ./ est necessaire car dans le code de jsmooth, il recupere le repertoire parent en faisant getParent() qui retourne null
# java -classpath jsmoothgen.jar:jox116.jar  net.charabia.jsmoothgen.application.cmdline.CommandLine ./picsou_exe_gene.en.jsmooth

$LAUNCH4J_HOME/launch4j `pwd`/launch4j-en.xml
