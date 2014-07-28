This was a simple implementation of labels with raw GCC. We used this for the
lightning round, but not for the full round.

## Compiling GCC/labels

The "compiler" is pretty straightforward. It's in the code/scala-gcc-labels
directory. The project can be built with SBT. Navigate to the root directory of
the source (i.e., code/scala-gcc-labels) and run these commands:

  sbt compile
  sbt test

It takes a single .gccr file as the argument and prints the .gcc output to
stdout. For example,

  sbt "run src/main/resources/world.gccr"

Note that the "run" command and the path to the file should be passed to sbt as
one parameter. Alternative, you can load SBT and type in the sbt prompt:

  sbt> run src/main/resources/world.gccr

## Helper Functions

A set of common utility functions that operate on lists were implemented to make
things easier for us to write the AI by hand. You can find these in
code/scala-gcc-labels/src/main/resources/list.gccr. We started with `fold-left`
and went on from there.
