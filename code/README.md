# Team Movio

First off, thank you for creating this wonderful challenge. It's been a really
fun and exciting day for us!

We spent most of the day trying to make higher level functions for GCC (see
Helper Functions below). We got started on a Lisp compiler but it's not
completed so we have in place of that a simple extension to GCC that allows
labels which compiles down to absolute addresses. This was implemented in Scala;
see below for instructions to run.

The algorithm is pretty straightforward - look at the 4 squares around lambdaman
and find the one with the highest worth and go in that direction. The naive
approach of looking at all 4 squares every time meant that lambdaman could get
stuck when more than one direction is equal in worth, especially in corners:

                                  +-+-+
                                     \|
                                  +-+ +
                                    | |
                                    + +

We got around that by disallowing going back the way you came, but this can
prove fatal if ghosts are coming your way. We didn't get far enough to take
ghosts into account.

## Solution

Our solution is generated from the "world.gccr" file. It is located in
code/scala-gcc-labels/src/main/resources.

## Ghosts

We tested our code against a custom ghost AI that one of us wrote. Those ghosts
home in on lambdaman pretty fast. The ghost AI is in the code/ghost-ai
directory.

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
