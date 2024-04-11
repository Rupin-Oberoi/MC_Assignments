I have implemented the app using Jetpack Compose, and the date is selected by the user by a DatePicker component and the date is validated, if it is incorrect the user is informed.
The weather API used is Open Meteo. Since the API requires longitude and latitude, for the purpose of this app they are hardcoded to 0.0.
For the API calls I am using Retrofit library and also parse the JSON response. The response received is also stored into the database, which has been implemented by Room.
The error handling is robust and the user gets an appropriate error message, such as "Data not available in database", or "Error fetching data from API".
Network connectivity is checked using ConnectivityManager on the basis of which it is decided whether to call the API or use local database.
