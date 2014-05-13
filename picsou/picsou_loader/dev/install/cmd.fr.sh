#!/bin/sh

#  attention le ./ est necessaire car dans le code de jsmooth, il recupere le repertoire parent en faisant getParent() qui retourne null
# java -classpath jsmoothgen.jar:jox116.jar  net.charabia.jsmoothgen.application.cmdline.CommandLine ./picsou_exe_gene.fr.jsmooth

~/dev/installer/launch4j/3.4/launch4j/launch4j `pwd`/launch4j-fr.xml
~/dev/installer/launch4j/3.4/launch4j/launch4j `pwd`/launch4j-fr-in-memory.xml
