# gcc

Small compiler for MLISP down to GCC written in Clojure.

Main approach:

1. Use the reader to parse a set of given files into an S-Expression AST.
2. Convert the AST for all top-level `defun`s into GCC instructions with relative and absolute references.
3. Concatenate all instructions, moving a defun with name `main` to the front if given.
4. Replace references with line numbers.
5. Write output to file and STDOUT.

## Implementation details

The main mapping from MLISP to GCC happens in `to-instruction-ast` based on a big
`cond` statement. Given that the assembly has so support for LISPy
functionality, the mapping from our MLISP to GCC is mostly straight-forward.

There is support for the main primitives `CEQ`, `CGT`, `CGTE`, `CONS`, `CAR`,
`CDR`, `ADD`, `SUB`, `MUL`, `DIV`, `ATOM`, `DBUG`, `BRK` and `true`, `false` and
`nil` are mapped to the respective constants. It adds a couple if built-in
functions with varargs for convenience:

```lisp
    (mklist 1 1 2 3 5)
    (mktuple 8 13 21 34 55)
```

gcc attempts to find tail calls and produces the respective `TSEL` and `TAP`
instructions automatically.

`defun`, `lambda` and `let` support multiple forms in their bodies.

`let` is implemented via a translation to lambda applications.

## MLISP examples

The top-level defuns are wrapped in a pair of parenthesis for parsing.

```lisp
    (
      (defun add (lhs rhs) (+ lhs rhs))
    )
```

Or more interestingly:    
    
```lisp
    (
      (defun fold-left (lst acc fun)
        (if (atom? lst)
          acc
          (fold-left (cdr lst)
                     (fun acc (car lst))
                     fun)))
                     
      (defun reverse (lst)
        (fold-left lst nil (lambda (acc next)
                                   (cons next acc))))
    )
```
    
Which compiles to:

    LD 0 1 ; $lambda-1
    LD 0 0
    CONS
    RTN
    LD 0 0 ; reverse
    LDC 0
    LDF 0
    LDF 10
    AP 3
    RTN
    LD 0 0 ; fold-left
    ATOM
    TSEL 13 15
    LD 0 1
    RTN
    LD 0 0
    CDR
    LD 0 1
    LD 0 0
    CAR
    LD 0 2
    AP 2
    LD 0 2
    LDF 10
    TAP 3

## Usage

You'll need [leiningen](http://leiningen.org/) to build or run gcc.

Command line:

    $ lein uberjar
    $ java -jar $PWD/target/gcc-0.1.0-SNAPSHOT-standalone.jar $INPUTFILE1 $INPUTFILE2

Interactively:

    $ lein repl
    > (use 'gcc.core)
    > (gcc '((defun x () 23)))

Run tests for hacking:

    $ lein repl
    > (use 'midje.repl)
    > (autotest)

## License

No restrictions.

This was written for the very fun ICFP Contest 2014, if this can be of any help to you -- happy hacking!