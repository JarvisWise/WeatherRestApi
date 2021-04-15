# Documentation for weather rest api
This application is used to retrieve weather data from three different weather services. You can get information about the weather in a 
specific city and for a specific date. 
---
Weather data can be obtained in two different formats. This is json and xml. You can also save 
the received data to a docs file. 
---
In case of an incorrect request or failure of one of the services, a message will be displayed 
explaining the reasons for the problems.
## Examples of requests for receiving weather data from aerisWeather
If you would like to receive data from aerisWeather, use one of the following sample requests:
* http://localhost:9345/aerisWeather/weather-by-date?location=kiev,ukr&date=2021-03-20&contextType=json&output=show
* http://localhost:9345/aerisWeather/current-weather?location=kiev,ukr&contextType=xml
* http://localhost:9345/aerisWeather/current-weather?location=kiev,ukr&contextType=xml&output=save
* http://localhost:9345/aerisWeather/current-weather?location=kiev,ukr
* http://localhost:9345/aerisWeather/current-weather?location=london,uk
## Examples of requests for receiving weather data from visualCrossingWeather
If you would like to receive data from visualCrossingWeather, use one of the following sample requests:
* http://localhost:9345/visualCrossingWeather/weather-by-date?location=kiev,ukr&date=2021-03-20&contextType=json&output=show
* http://localhost:9345/visualCrossingWeather/current-weather?location=kiev,ukr&contextType=xml
* http://localhost:9345/visualCrossingWeather/current-weather?location=kiev,ukr&contextType=xml&output=save
* http://localhost:9345/visualCrossingWeather/current-weather?location=kiev,ukr
* http://localhost:9345/visualCrossingWeather/current-weather?location=london,uk    
## Examples of requests for receiving weather data from weatherAPI
If you would like to receive data from weatherAPI, use one of the following sample requests:
* http://localhost:9345/weatherAPI/weather-by-date?location=kiev,ukr&date=2021-03-20&contextType=json&output=show
* http://localhost:9345/weatherAPI/current-weather?location=kiev,ukr&contextType=xml
* http://localhost:9345/weatherAPI/current-weather?location=kiev,ukr&contextType=xml&output=save
* http://localhost:9345/weatherAPI/current-weather?location=kiev,ukr
* http://localhost:9345/weatherAPI/current-weather?location=london,uk
