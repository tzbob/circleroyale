# Gavial Examples

To give the reader a better overview of the examples in the paper we also packaged them here.
This project contains the paper-example of CircleRoyale as well as the current state of the CircleRoyale application.

To run the examples go into the cloned repository and execute ```sbt run```. Sbt will download all dependencies and run the examples. The default example (set in ```build.sbt```) is `_4Chat.scala`.

The layout is as follows:

- `shared/src/main/scala/be/tzbob/examples/`: contains the code that was visible throughout the paper. 
- `.../_1Client.scala`: is the first stage of the example. Here we have an implementation of CircleRoyale that is completely singleplayer and client-side. A user can move around and may attack but it is purely client-side.
- `.../_2SessionServerTick.scala`: is similar to the previous stage. It contains the same functionality except that the game is now computer on the server at a specific pace. 
- `.../_3App.scala`: adds hit detection and multiplayer functionality into the mix.
- `../_4Chat.scala`: adds a simple chat that will always be consistent with the living players in the game.


- `shared/src/main/scala/be/tzbob/circleroyale/`: contains all code regarding the CircleRoyale game at the time of submission the attacking pattern is a bit buggy due to a faulty implementation of time APIs on the client.
However, what it does manage to show off is an implementation that is throttled at the client and server side while also being rendered at the browser's desired framerate.
The imlementation also supports game 'scenes', different page implementation in a single page application.
