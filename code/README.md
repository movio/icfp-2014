# Team Movio

Again, thank you for creating this challenge. It's definitely been a long
weekend!

After day one we got a compiler for a custom lisp language working which we
called MLISP. Slowly we ironed out the bugs so we could start writing our AI at
a much higher level.

## Code

While we built several distinct tools during the weekend we used different tools
for the lightning and the full rounds.

  * Lightning round
    * scala-gcc-labels: labels for raw GCC
    * ghost-compiler:   labels and variables for GHC
  * Full round
    * gcc:              MLISP compiler for GCC
    * ghost-compiler:   labels, variables and helper functions for GHC
    * gcc-lisp:         sources for lambdaman

Each tool has a dedicated README.md within its directory.

## Solution - Lambdaman

Our final solution evaluates all paths up to a predetermined PATH_LENGTH
distance by scoring them based on what is on each grid position. Grids closer to
lambdaman are weighted more heavily for this evaluation.

To try to prevent getting stuck, we use the direction of the first ghost as a
pseudo-random number generator, so to speak. This happens when multiple
directions evaluate to the same score, at which point we pick the first
available direction starting from the direction of the first ghost and rotating
clockwise.

Whereas we wrote our lightning solution mostly by hand, for the full round we
used our MLISP compiler. The sources are in code/gcc-lisp:

  * Core files:
    * util.lisp:      basic utility functions
    * list.lisp:      a list library modelled on Scala
    * tuple.lisp:     a small set of utility functions for tuples
    * lman-ai.lisp:   the main AI logic

  * Entry points:
    * debugger.lisp:  compile with this to execute in the reference Lambdaman
                      CPU emulator with a test game state
    * simulator.lisp: compile with this to execute in the reference game
                      simulator

See code/gcc/README.md for a more detailed look at our MLISP compiler.

## Solution - Ghosts

We wrote three different ghost AIs: Jai, Kai, Trai. To help with addressing in
the assembly we wrote a simple label implementation.

See code/ghost-compiler/README.md for more detailed instructions.

### Jai (ghost0 AI)

When not in fright mode, Jai is trying to follow Lambda-Man by getting closer
to him on either the x-ordinate or the y-ordinate. When in fright mode, he is
trying to distance himself from Lambda-Man so he won't get eaten.

### Kai (ghost1 AI)

Kai is not very interested in Lambda-Man and just tries to randomly pick a valid
direction. In fright mode he behaves the same as Jai.

### Trai (ghost2 AI)

Trai is a bit biased towards Lambda-Man. He changes his mind every 5 steps and
alternates between picking a direction randomly or following Lambda-Man. In
fright mode he behaves the same as Jai and Kai.
