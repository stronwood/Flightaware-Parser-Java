# Flightaware-Parser
Java program to parse flight data from flightaware.com

Rewritten in Java based on shivasiddharth's original python script here: 

[![Flightaware-Parser](https://img.shields.io/badge/FlightawareParser-blue)](https://github.com/shivasiddharth/Flightaware-Parser)

## Example Usage:    
```    
import com.foo.FlightAwareParser;

// pass in a flightcode (copy from flightaware.com/live/flight/HERE) and return a String[] array

FlightAwareParser flightAwareParser = new FlightAwareParser;
String[] variable = flightAwareParser.flightData("BA35");
// Pro tip: Use "random" in place of "BA35" for a random flight
```
____________________________

```
#Sample output of running for(String var : variable) -
('British Airways 35', 'Boeing 787-9 (twin-jet)', 'London, United Kingdom', 'Chennai / Madras, India', 'airborne', 370, 528, 'November 29 2019 14:30:00', 'November 29 2019 15:01:38', 'November 30 2019 00:03:00', 'November 30 2019 00:13:00', 'November 29 2019 14:33:00', 'November 29 2019 15:01:00', None, None, 'In air, covered 604 nautical miles with 3858 nautical miles remaining.')  
```
     
## Order of output:  
Flight number/code   
Aircraft type   
Flight origin   
Flight destination   
Flight status (whether airborne/landed)  
Flight altitude    
Flight ground speed     
Estimated gate departure time        
Estimated takeoff time     
Estimated landing time      
Estimated gate arrival time     
Actual gate departure time       
Actual takeoff time     
Actual landing time      
Actual gate arrival time   
Aircraftposition (If airborne returns distance covered and distance remaining, returns status such as taxiing to takeoff from gate or taxiing to gate after landing)   


