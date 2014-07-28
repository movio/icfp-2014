# ghost-compiler

Small compiler for the ghost AIs which enables you to use labels in
your ghost code and other helper functions.

# Install

To build the ghost-compiler, run the following:

    sbt
    > package

This will first install missing libraries and then compile the project.

# Usage

Run the following command and pass the file you want to compile:

    java -cp "ghosts_2.11-0.1-SNAPSHOT.jar:scala-library-2.11.1.jar" com.kalmanb.Main <filename>

    ; Ghost X,Y coords
    g=ghostXCoord
    h=ghostYCoord


    ; Set next move direction
    move(up)
    move(down)
    move(left)
    move(right)
    move(0)
    move(1)
    move(c)
    move(d)


    ; Assign variables
    a=b
    a=1
    a=200


    ; Jump to locations
    jump(a=b,newLocation)
    jump(a>b,newLocation)
    jump(a<b,newLocation)
    hlt

    ; simple comment

    ;#newLocation
    hlt

