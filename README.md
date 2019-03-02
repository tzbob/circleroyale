# Gavial Examples

To give the reader a better overview of the examples in the paper we also packaged them here.
This project contains the paper-example of CircleRoyale as well as the current state of the CircleRoyale application .

The layout is as follows:

- `shared/src/main/scala/be/tzbob/circleroyale/`: contains all code regarding the CircleRoyale game at the time of submission the attacking pattern is a bit buggy due to a faulty implementation of time APIs on the client.
However, what it does manage to show off is an implementation that is throttled at the client and server side while also being rendered at the browser's desired framerate.
The imlementation also supports game 'scenes', different page implementation in a single page application.

- `shared/src/main/scala/be/tzbob/examples/`: contains the code that was visible throughout the paper.
