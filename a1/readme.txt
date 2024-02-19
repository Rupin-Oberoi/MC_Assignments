For the assignment, I first use a single activity and the onCreate function involves declaring the lists for stops and the the distances between them
and then it calls a Composable function JourneyApp which acts a central component as it calls other Composable functions for smaller purposes such as the Progress Bar.
Mainly I use Column for structuring the pieces such as the buttons, textbox, progress bar with parameters passed in the modifier to have a suitable UI such as maintaining a 
padding for appropriate spacing and using the complete width of the screen. The sizes for various components is controlled using dp or sp for fonts since font size can be changed by user in their settings.
Since Composable functions should not do any computation there are several other helper functions for utilities such as converting the units, formatting the distances to 
strings etc.
The Progress Indicator uses LinearProgressIndicator of Jetpack Compose and uses displays progress in terms of distance covered out of the total distance.
A boolean variable is responsible for storing the state of the user's preference of units.
Throughout the implementation I use the distances in km, which can be converted and formatted to miles when needed to show them on screen by a composable. 
In error handling, it is ensured that the curr index of stops never exceeds the number of stops, irrespective of whether the user keeps on clicking/tapping on the reached next stop button. Also it is ensured that the list of distances has length 1 less than the number of stops.